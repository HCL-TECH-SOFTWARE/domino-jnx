/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
 *                            All rights reserved.
 * ==========================================================================
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may
 * not use this file except in compliance with the License.  You may obtain a
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.
 *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the  specific language  governing permissions  and limitations
 * under the License.
 * ==========================================================================
 */
package com.hcl.domino.jna.data;

import java.io.IOException;
import java.io.Writer;
import java.lang.ref.ReferenceQueue;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.StringTokenizerExt;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Agent;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Database.OpenDocumentMode;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.exception.ObjectDisposedException;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.NotesNamingUtils;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNAAgentAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNADatabaseAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNADocumentAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNAUserNamesListAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.structs.NotesUniversalNoteIdStruct;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class JNAAgent extends BaseJNAAPIObject<JNAAgentAllocations> implements Agent {
	private int m_agentNoteId;
	private JNADocument m_agentDoc;
	private String m_agentNameAndAliases;
	private String m_comment;
	
	public JNAAgent(JNADatabase parentDb, int agentNoteId, IAdaptable adaptable) {
		super(parentDb);
		
		m_agentNoteId = agentNoteId;
		
		DHANDLE handle = adaptable.getAdapter(DHANDLE.class);
		if (handle==null) {
			throw new DominoException(0, "Missing expected agent handle");
		}
		getAllocations().setAgentHandle(handle);

		setInitialized();
	}

	@Override
	public Database getParentDatabase() {
		return (Database) getParent();
	}
	
	/**
	 * Opens the agent note and stores it in a variable for reuse
	 * 
	 * @return agent note
	 */
	private Document getAgentDoc() {
		if (m_agentDoc==null || m_agentDoc.isDisposed()) {
			m_agentDoc = (JNADocument) getParentDatabase().getDocumentById(m_agentNoteId, EnumSet.noneOf(OpenDocumentMode.class)).get();
		}
		return m_agentDoc;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected JNAAgentAllocations createAllocations(IGCDominoClient<?> parentDominoClient, APIObjectAllocations parentAllocations,
			ReferenceQueue<? super IAPIObject> queue) {

		return new JNAAgentAllocations(parentDominoClient, parentAllocations, this, queue);
	}
	
	@Override
	public String getName() {
		if (m_agentNameAndAliases==null) {
			m_agentNameAndAliases = getAgentDoc().get("$Title", String.class, ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		int iPos = m_agentNameAndAliases.indexOf('|');
		return iPos!=-1 ? m_agentNameAndAliases.substring(0, iPos) : m_agentNameAndAliases;
	}

	@Override
	public List<String> getAliases() {
		if (m_agentNameAndAliases==null) {
			m_agentNameAndAliases = getAgentDoc().get("$Title", String.class, ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		int iPos = m_agentNameAndAliases.indexOf('|');
		if (iPos==-1) {
			return Collections.emptyList();
		}
		List<String> aliases = new ArrayList<>();
		
		StringTokenizerExt st = new StringTokenizerExt(m_agentNameAndAliases.substring(iPos+1), "|"); //$NON-NLS-1$
		while (st.hasMoreTokens()) {
			String currToken = st.nextToken().trim();
			if (!StringUtil.isEmpty(currToken)) {
				aliases.add(currToken);
			}
		}
		return aliases;
	}

	@Override
	public String getUNID() {
		return getParentDatabase().toUNID(m_agentNoteId);
	}

	@Override
	public int getNoteID() {
		return m_agentNoteId;
	}

	@Override
	public String getComment() {
		if (m_comment==null) {
			m_comment = getAgentDoc().get("$Comment", String.class, ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return m_comment;
	}

	@Override
	public boolean isEnabled() {
		checkDisposed();

		boolean enabled = LockUtil.lockHandle(getAllocations().getAgentHandle(), (hAgentByVal) -> {
			return NotesCAPI.get().AgentIsEnabled(hAgentByVal);
		});
		
		return enabled;
	}

	@Override
	public boolean isRunAsWebUser() {
		checkDisposed();

		boolean isRunAsWebUser = LockUtil.lockHandle(getAllocations().getAgentHandle(), (hAgentByVal) -> {
			return NotesCAPI.get().IsRunAsWebUser(hAgentByVal);
		});
		
		return isRunAsWebUser;
	}

	@Override
	public AgentRunContext createAgentContext() {
		return new JNAAgentRunContext();
	}

	@Override
	public void run(AgentRunContext runCtx) {
		checkDisposed();
		
		Document doc = runCtx.getDocumentContext().orElse(null);
		if (doc instanceof JNADocument && ((JNADocument)doc).isDisposed()) {
			throw new ObjectDisposedException(doc);
		}
		
		int ctxFlags = 0;
		int runFlags = 0;
		
		boolean reopenDbAsSigner = runCtx.isReopenDbAsSigner();
		if (reopenDbAsSigner) {
			runFlags = NotesConstants.AGENT_REOPEN_DB;
		}
		boolean checkSecurity = runCtx.isCheckSecurity();
		if (checkSecurity) {
			ctxFlags = NotesConstants.AGENT_SECURITY_ON;
		}

		final int fCtxFlags = ctxFlags;
		final int fRunFlags = runFlags;
		
		Optional<Writer> stdOut = runCtx.getOutputWriter();
		int timeoutSeconds = runCtx.getTimeoutSeconds();
		int paramDocId = runCtx.getParamDocId();
		
		String effectiveUserName = StringUtil.isEmpty(runCtx.getUsername()) ? getParentDominoClient().getEffectiveUserName() : runCtx.getUsername();

		LockUtil.lockHandle(getAllocations().getAgentHandle(), (hAgentByVal) -> {
			DHANDLE.ByReference rethContext = DHANDLE.newInstanceByReference();
			
			short result = NotesCAPI.get().AgentCreateRunContextExt(hAgentByVal, null, 0, fCtxFlags, rethContext);
			NotesErrorUtils.checkResult(result);
			
			LockUtil.lockHandle(rethContext, (hContextByVal) -> {
				
				JNAUserNamesList namesListToFree = null;
				
				try {
					if (stdOut.isPresent()) {
						//redirect stdout to in memory buffer
						short redirType = NotesConstants.AGENT_REDIR_MEMORY;
						short resultSetOut = NotesCAPI.get().AgentRedirectStdout(hContextByVal, redirType);
						NotesErrorUtils.checkResult(resultSetOut);
					}

					if (timeoutSeconds!=0) {
						NotesCAPI.get().AgentSetTimeExecutionLimit(hContextByVal, timeoutSeconds);
					}

					if (doc!=null) {
						JNADocumentAllocations docAllocations = (JNADocumentAllocations) doc.getAdapter(APIObjectAllocations.class);
						if (docAllocations!=null) {
							LockUtil.lockHandle(docAllocations.getNoteHandle(), (hNoteByVal) -> {
								NotesCAPI.get().AgentSetDocumentContext(hContextByVal, hNoteByVal);
								return 0;
							});
						}
					}
					
					if (paramDocId!=0) {
						NotesCAPI.get().SetParamNoteID(hContextByVal, paramDocId);
					}
					

					namesListToFree = NotesNamingUtils.buildNamesList(JNAAgent.this, effectiveUserName);
					JNAUserNamesListAllocations namesListAllocations = (JNAUserNamesListAllocations) namesListToFree.getAdapter(APIObjectAllocations.class);
					
					LockUtil.lockHandle(namesListAllocations.getHandle(), (hNamesListByVal) -> {
						short localResult = NotesCAPI.get().AgentSetUserName(hContextByVal, hNamesListByVal);
						NotesErrorUtils.checkResult(localResult);
						
						return 0;
					});
				
					short localResult = NotesCAPI.get().AgentRun(hAgentByVal, hContextByVal, null, fRunFlags);
					NotesErrorUtils.checkResult(localResult);
					
					if (stdOut.isPresent()) {
						DHANDLE.ByReference retBufHandle = DHANDLE.newInstanceByReference();
						IntByReference retSize = new IntByReference();
						
						NotesCAPI.get().AgentQueryStdoutBuffer(hContextByVal, retBufHandle, retSize);
						int iRetSize = retSize.getValue();
						if (iRetSize!=0) {
							LockUtil.lockHandle(retBufHandle, (hBufByVal) -> {
								Pointer bufPtr = Mem.OSLockObject(hBufByVal);
								try {
									//decode std out buffer content
									String bufContentUnicode = NotesStringUtils.fromLMBCS(bufPtr, iRetSize);
									try {
										stdOut.get().write(bufContentUnicode);
									} catch (IOException e) {
										throw new DominoException("Error writing agent output", e);
									}
									
									return 0;
								}
								finally {
									Mem.OSUnlockObject(hBufByVal);
								}
							});
						}
					}
				}
				finally {
					NotesCAPI.get().AgentDestroyRunContext(hContextByVal);
					
					if (namesListToFree!=null) {
						namesListToFree.dispose();
					}
				}
				return 0;
			});
			
			return 0;
		});
	}

	@Override
	public void runOnServer(boolean suppressPrintToConsole) {
		runOnServer(0, suppressPrintToConsole);
	}

	@Override
	public void runOnServer(int noteIdParamDoc, boolean suppressPrintToConsole) {
		checkDisposed();
		
		boolean bForeignServer = false;
		
		JNADatabaseAllocations dbAllocations = (JNADatabaseAllocations) getParentDatabase().getAdapter(APIObjectAllocations.class);
		
		short result = LockUtil.lockHandles(dbAllocations.getDBHandle(), getAllocations().getAgentHandle(),
				(hDbByVal, hAgentByVal) -> {
					
			return NotesCAPI.get().ClientRunServerAgent(hDbByVal,
					m_agentNoteId, noteIdParamDoc, bForeignServer ? 1 : 0, suppressPrintToConsole ? 1 : 0);
		});
		
		NotesErrorUtils.checkResult(result);
	}

	@Override
	public String toStringLocal() {
		Database parentDb = getParentDatabase();
		return MessageFormat.format(
			"JNAAgent [db={0}!!{1}, noteid={2}, name={3}]", //$NON-NLS-1$
			parentDb.getServer(), parentDb.getRelativeFilePath(), m_agentNoteId, getName()
		);
	}

	@Override
	public Optional<Document> getAgentSavedData() {
		checkDisposed();
		
		Database db = getParentDatabase();
		JNADatabaseAllocations dbAllocations = (JNADatabaseAllocations) db.getAdapter(APIObjectAllocations.class);
		
		NotesUniversalNoteIdStruct.ByReference retUNID = NotesUniversalNoteIdStruct.newInstanceByReference();
		
		short result = LockUtil.lockHandle(dbAllocations.getDBHandle(), (hDbByVal) -> {
			return NotesCAPI.get().AssistantGetLSDataNote(hDbByVal, m_agentNoteId, retUNID);
		});
		NotesErrorUtils.checkResult(result);
		
		String unid = retUNID.toString();
		if (StringUtil.isEmpty(unid) || "00000000000000000000000000000000".equals(unid)) { //$NON-NLS-1$
			return Optional.empty();
		}
		else {
			return db.getDocumentByUNID(unid);
		}
	}
}
