/*
 * ==========================================================================
 * Copyright (C) 2019-2021 HCL America, Inc. ( http://www.hcl.com/ )
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
package com.hcl.domino.jna.internal.views;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import com.hcl.domino.commons.util.StringTokenizerExt;
import com.hcl.domino.commons.views.OpenCollection;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.data.DominoCollectionInfo;
import com.hcl.domino.jna.data.JNACollectionEntry;
import com.hcl.domino.jna.data.JNADatabase;
import com.hcl.domino.jna.data.JNADominoCollection;
import com.hcl.domino.misc.NotesConstants;

/**
 * View and folder information read from the database design collection
 * 
 * @author Karsten Lehmann
 */
public class JNADominoCollectionInfo implements DominoCollectionInfo {
	private String m_title;
	private List<String> m_aliases;
	private String m_flags;
	private JNADatabase m_parentDb;
	private int m_noteId;
	private String m_comment;
	private String m_language;
	
	private JNADominoCollection m_collection;
	
	public JNADominoCollectionInfo(JNADatabase parentDb) {
		m_parentDb = parentDb;
	}

	public void initFromDesignCollectionEntry(JNACollectionEntry entry) {
		String titleAndAliases = entry.get("$title", String.class, ""); //$NON-NLS-1$ //$NON-NLS-2$
		StringTokenizerExt st = new StringTokenizerExt(titleAndAliases, "|"); //$NON-NLS-1$
		m_title = st.nextToken();
		
		m_aliases = new ArrayList<>();
		while (st.hasMoreTokens()) {
			m_aliases.add(st.nextToken());
		}
		
		m_flags = entry.get(NotesConstants.DESIGN_FLAGS, String.class, ""); //$NON-NLS-1$
		m_noteId = entry.getNoteID();
		m_comment = entry.get("$comment", String.class, ""); //$NON-NLS-1$ //$NON-NLS-2$
		m_language = entry.get("$language", String.class, ""); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public <T> T getAdapter(Class<T> clazz) {
		return null;
	}

	public void setTitle(String title) {
		m_title = title;
	}
	
	@Override
	public String getTitle() {
		return m_title==null ? "" : m_title; //$NON-NLS-1$
	}

	public void setAliases(List<String> aliases) {
		m_aliases = aliases;
	}
	
	@Override
	public List<String> getAliases() {
		return m_aliases==null ? Collections.emptyList() : m_aliases;
	}
	
	public void setFlags(String flags) {
		m_flags = flags;
	}
	
	@Override
	public boolean isFolder() {
		if (m_flags!=null) {
			return m_flags.contains(NotesConstants.DESIGN_FLAG_FOLDER_VIEW);
		}
		return false;
	}

	public void setNoteID(int noteId) {
		m_noteId = noteId;
	}
	
	@Override
	public int getNoteID() {
		return m_noteId;
	}
	
	public void setComment(String comment) {
		m_comment = comment;
	}
	
	@Override
	public String getComment() {
		return m_comment==null ? "" : m_comment; //$NON-NLS-1$
	}
	
	public void setLanguage(String language) {
		m_language = language;
	}
	
	@Override
	public String getLanguage() {
		return m_language==null ? "" : m_language; //$NON-NLS-1$
	}
	
	@Override
	public Database getParent() {
		return m_parentDb;
	}
	
	@Override
	public DominoCollection openCollection() {
		if (m_collection==null || m_collection.isDisposed()) {
			JNADatabase db = (JNADatabase) getParent();
			m_collection = (JNADominoCollection) db.openCollection(getNoteID(), (EnumSet<OpenCollection>) null);
		}
		return m_collection;
	}

	@Override
	public String toString() {
		return MessageFormat.format(
			"JNACollectionSummary [title={0}, aliases={1}, isfolder={2}, noteid={3}]",
			getTitle(), getAliases(), isFolder(), getNoteID()
		);
	}
}
