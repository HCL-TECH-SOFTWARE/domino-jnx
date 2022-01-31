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
package com.hcl.domino.jna.dxl;

import static com.hcl.domino.commons.dxl.DxlImportProperty.ACLImportOption;
import static com.hcl.domino.commons.dxl.DxlImportProperty.CreateFullTextIndex;
import static com.hcl.domino.commons.dxl.DxlImportProperty.DesignImportOption;
import static com.hcl.domino.commons.dxl.DxlImportProperty.DocumentsImportOption;
import static com.hcl.domino.commons.dxl.DxlImportProperty.ExitOnFirstFatalError;
import static com.hcl.domino.commons.dxl.DxlImportProperty.ImportedNoteList;
import static com.hcl.domino.commons.dxl.DxlImportProperty.InputValidationOption;
import static com.hcl.domino.commons.dxl.DxlImportProperty.ReplaceDbProperties;
import static com.hcl.domino.commons.dxl.DxlImportProperty.ReplicaRequiredForReplaceOrUpdate;
import static com.hcl.domino.commons.dxl.DxlImportProperty.ResultLog;
import static com.hcl.domino.commons.dxl.DxlImportProperty.ResultLogComment;
import static com.hcl.domino.commons.dxl.DxlImportProperty.UnknownTokenLogOption;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.ReferenceQueue;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hcl.domino.commons.dxl.DxlImportProperty;
import com.hcl.domino.commons.dxl.DxlImporterLogImpl;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.PlatformUtils;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.dxl.DxlImporter;
import com.hcl.domino.dxl.DxlImporterLog;
import com.hcl.domino.exception.DxlImportException;
import com.hcl.domino.jna.data.JNAIDTable;
import com.hcl.domino.jna.internal.callbacks.NotesCallbacks;
import com.hcl.domino.jna.internal.callbacks.Win32NotesCallbacks;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNADxlImporterAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.HANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.utils.JNADominoUtils;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.INumberEnum;
import com.sun.jna.Pointer;

/**
 * @author Jesse Gallagher
 */
public class JNADxlImporter extends AbstractDxlProcessor<JNADxlImporterAllocations, DxlImportProperty> implements DxlImporter {

	public JNADxlImporter(IAPIObject<?> parent, int hDxlImporter) {
		super(parent);
		
		getAllocations().setDxlImporterHandle(hDxlImporter);
		
		setInitialized();
	}

	@Override
	public boolean importErrorWasLogged() {
		checkDisposed();
		
		return NotesCAPI.get().DXLImportWasErrorLogged(getAllocations().getDxlImporterHandle());
	}

	@Override
	public DXLImportOption getACLImportOption() {
		int value = getPropInt(ACLImportOption);
		return DominoEnumUtil.valueOf(DXLImportOption.class, value)
			.orElseThrow(() -> new IllegalStateException(MessageFormat.format("Cannot identify import option for {0}", value)));
	}

	@Override
	public void setACLImportOption(DXLImportOption option) {
		setProp(ACLImportOption, option.getValue());
	}

	@Override
	public DXLImportOption getDesignImportOption() {
		int value = getPropInt(DesignImportOption);
		return DominoEnumUtil.valueOf(DXLImportOption.class, value)
			.orElseThrow(() -> new IllegalStateException(MessageFormat.format("Cannot identify import option for {0}", value)));
	}

	@Override
	public void setDesignImportOption(DXLImportOption option) {
		setProp(DesignImportOption, option.getValue());
	}

	@Override
	public DXLImportOption getDocumentsImportOption() {
		int value = getPropInt(DocumentsImportOption);
		return DominoEnumUtil.valueOf(DXLImportOption.class, value)
			.orElseThrow(() -> new IllegalStateException(MessageFormat.format("Cannot identify import option for {0}", value)));
	}

	@Override
	public void setDocumentsImportOption(DXLImportOption option) {
		setProp(DocumentsImportOption, option.getValue());
	}

	@Override
	public boolean isCreateFullTextIndex() {
		return isProp(CreateFullTextIndex);
	}

	@Override
	public void setCreateFullTextIndex(boolean b) {
		setProp(CreateFullTextIndex, b);
	}

	@Override
	public boolean isReplaceDbProperties() {
		return isProp(ReplaceDbProperties);
	}

	@Override
	public void setReplaceDbProperties(boolean b) {
		setProp(ReplaceDbProperties, b);
	}

	@Override
	public XMLValidationOption getInputValidationOption() {
		int value = getPropInt(InputValidationOption);
		return DominoEnumUtil.valueOf(XMLValidationOption.class, value)
			.orElseThrow(() -> new IllegalStateException(MessageFormat.format("Cannot identify validation option for {0}", value)));
	}

	@Override
	public void setInputValidationOption(XMLValidationOption option) {
		setProp(InputValidationOption, option.getValue());
	}

	@Override
	public boolean isReplicaRequiredForReplaceOrUpdate() {
		return isProp(ReplicaRequiredForReplaceOrUpdate);
	}

	@Override
	public void setReplicaRequiredForReplaceOrUpdate(boolean b) {
		setProp(ReplicaRequiredForReplaceOrUpdate, b);
	}

	@Override
	public boolean isExitOnFirstFatalError() {
		return isProp(ExitOnFirstFatalError);
	}

	@Override
	public void setExitOnFirstFatalError(boolean b) {
		setProp(ExitOnFirstFatalError, b);
	}

	@Override
	public DXLLogOption getUnknownTokenLogOption() {
		int value = getPropInt(UnknownTokenLogOption);
		return DominoEnumUtil.valueOf(DXLLogOption.class, value)
			.orElseThrow(() -> new IllegalStateException(MessageFormat.format("Cannot identify log option for {0}", value)));
	}

	@Override
	public void setUnknownTokenLogOption(DXLLogOption option) {
		setProp(UnknownTokenLogOption, option.getValue());
	}

	@Override
	public String getResultLogComment() {
		return getPropString(ResultLogComment);
	}

	@Override
	public void setResultLogComment(String comment) {
		setProp(ResultLogComment, comment);
	}

	@Override
	public String getResultLog() {
		return getPropString(ResultLog);
	}

	@Override
	public Optional<IDTable> getImportedNoteIds() {
		DHANDLE hTable = getPropDHANDLE(ImportedNoteList);
		if(!hTable.isNull()) {
			return Optional.of(new JNAIDTable(getParentDominoClient(), hTable, false));
		} else {
			return Optional.empty();
		}
	}
	
	@Override
	public void importDxl(InputStream in, Database db) throws IOException {
		Objects.requireNonNull(in, "InputStream cannot be null");
		Objects.requireNonNull(db, "Database cannot be null");

		checkDisposed();
		
		LockUtil.lockHandle(db.getAdapter(HANDLE.class), handle -> {
			NotesCallbacks.XML_READ_FUNCTION func;
			if (PlatformUtils.isWin32()) {
				func = (Win32NotesCallbacks.XML_READ_FUNCTIONWin32) (pBuffer, length, pAction) -> {
					try {
						int remaining = in.available();
						if(remaining < 1) {
							return 0;
						}
						
						byte[] block;
						if(remaining > length) {
							block = new byte[length];
						} else {
							block = new byte[remaining];
						}
						in.read(block);
						pBuffer.write(0, block, 0, block.length);
						return block.length;
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				};
			}
			else {
				func = (pBuffer, length, pAction) -> {
					try {
						int remaining = in.available();
						if(remaining < 1) {
							return 0;
						}
						
						byte[] block;
						if(remaining > length) {
							block = new byte[length];
						} else {
							block = new byte[remaining];
						}
						in.read(block);
						pBuffer.write(0, block, 0, block.length);
						return block.length;
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				};
			}
			
			short result = AccessController.doPrivileged((PrivilegedAction<Short>) ()-> {
				return NotesCAPI.get().DXLImport(getAllocations().getDxlImporterHandle(), func, handle, null);
			});

			NotesErrorUtils.checkResult(result);
			
			checkError();
			return null;
		});
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected JNADxlImporterAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		return new JNADxlImporterAllocations(parentDominoClient, parentAllocations, this, queue);
	}

	// *******************************************************************************
	// * Internal implementation methods
	// *******************************************************************************
	
	@Override
	protected void setProperty(int hDxl, INumberEnum<Integer> prop, Pointer propValue) {
		checkDisposed();
		NotesCAPI.get().DXLSetImporterProperty(hDxl, prop.getValue(), propValue);
	}
	
	@Override
	protected void getProperty(int hDxl, INumberEnum<Integer> prop, Pointer retPropValue) {
		checkDisposed();
		NotesCAPI.get().DXLGetImporterProperty(hDxl, prop.getValue(), retPropValue);
	}
	
	@Override
	protected void checkError() {
		if(importErrorWasLogged()) {
			String xml = getResultLog();
			try {
				org.w3c.dom.Document xmlDoc = JNADominoUtils.parseXml(xml);
				List<DxlImporterLog.DxlError> errors;
				List<DxlImporterLog.DxlFatalError> fatalErrors;
				{
					NodeList errorNodes = xmlDoc.getElementsByTagName("error"); //$NON-NLS-1$
					errors = new ArrayList<>(errorNodes.getLength());
					for(int i = 0; i < errorNodes.getLength(); i++) {
						Element errorNode = (Element)errorNodes.item(i);
						DxlImporterLogImpl.DxlErrorImpl error = new DxlImporterLogImpl.DxlErrorImpl();
						error.setColumn(Integer.parseInt(errorNode.getAttribute("column"))); //$NON-NLS-1$
						error.setLine(Integer.parseInt(errorNode.getAttribute("line"))); //$NON-NLS-1$
						error.setSource(errorNode.getAttribute("source")); //$NON-NLS-1$
						error.setId(Integer.parseInt(errorNode.getAttribute("id"))); //$NON-NLS-1$
						error.setText(errorNode.getTextContent());
						errors.add(error);
					}
				}
				{
					NodeList errorNodes = xmlDoc.getElementsByTagName("fatalerror"); //$NON-NLS-1$
					fatalErrors = new ArrayList<>(errorNodes.getLength());
					for(int i = 0; i < errorNodes.getLength(); i++) {
						Element errorNode = (Element)errorNodes.item(i);
						DxlImporterLogImpl.DxlFatalErrorImpl error = new DxlImporterLogImpl.DxlFatalErrorImpl();
						error.setColumn(Integer.parseInt(errorNode.getAttribute("column"))); //$NON-NLS-1$
						error.setLine(Integer.parseInt(errorNode.getAttribute("line"))); //$NON-NLS-1$
						error.setSource(errorNode.getAttribute("source")); //$NON-NLS-1$
						error.setText(errorNode.getTextContent());
						fatalErrors.add(error);
					}
				}
				
				DxlImporterLog log = new DxlImporterLogImpl(errors, fatalErrors);
				if(log.getFatalErrors().isEmpty()) {
					throw new DxlImportException(String.valueOf(log.getErrors()), log);
				} else {
					throw new DxlImportException(String.valueOf(log.getFatalErrors()), log);
				}
			} catch (ParserConfigurationException | SAXException e) {
				throw new RuntimeException("Encountered exception parsing DXL log", e);
			}
		}
	}
	
	@Override
	protected int getHandle() {
		return getAllocations().getDxlImporterHandle();
	}
}
