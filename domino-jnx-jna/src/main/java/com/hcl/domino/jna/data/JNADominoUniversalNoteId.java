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

import java.time.temporal.TemporalAccessor;
import java.util.Optional;

import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.DominoUniversalNoteId;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.hcl.domino.jna.internal.structs.NotesUniversalNoteIdStruct;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class JNADominoUniversalNoteId implements DominoUniversalNoteId, IAdaptable {
	private NotesUniversalNoteIdStruct m_struct;
	
	public JNADominoUniversalNoteId(IAdaptable adaptable) {
		NotesUniversalNoteIdStruct struct = adaptable.getAdapter(NotesUniversalNoteIdStruct.class);
		if (struct!=null) {
			m_struct = struct;
			return;
		}
		Pointer p = adaptable.getAdapter(Pointer.class);
		if (p!=null) {
			m_struct = NotesUniversalNoteIdStruct.newInstance(p);
			return;
		}
		throw new IllegalArgumentException("Constructor argument cannot provide a supported datatype");
	}
	
	public JNADominoUniversalNoteId(String unidStr) {
		m_struct = NotesUniversalNoteIdStruct.fromString(unidStr);
	}
	
	@Override
	public Optional<DominoDateTime> getFile() {
		return Optional.ofNullable(m_struct.File==null ? null : new JNADominoDateTime(m_struct.File));
	}

	@Override
	public Optional<DominoDateTime> getNote() {
		return Optional.ofNullable(m_struct.Note==null ? null : new JNADominoDateTime(m_struct.Note));
	}

	@Override
	public void setNote(TemporalAccessor td) {
		if (m_struct==null) {
			m_struct = NotesUniversalNoteIdStruct.newInstance();
		}
		if (m_struct.Note==null) {
			m_struct.Note = NotesTimeDateStruct.newInstance();
		}
		int[] innards = new JNADominoDateTime(td).getInnards();
		m_struct.Note.Innards[0] = innards[0];
		m_struct.Note.Innards[1] = innards[1];
		m_struct.Note.write();
		m_struct.write();
	}

	@Override
	public void setFile(TemporalAccessor td) {
		if (m_struct==null) {
			m_struct = NotesUniversalNoteIdStruct.newInstance();
		}
		if (m_struct.File==null) {
			m_struct.File = NotesTimeDateStruct.newInstance();
		}
		
		int[] innards = new JNADominoDateTime(td).getInnards();
		m_struct.File.Innards[0] = innards[0];
		m_struct.File.Innards[1] = innards[1];
		m_struct.File.write();
		m_struct.write();
	}

	@Override
	public String toString() {
		return String.valueOf(m_struct);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> clazz) {
		if (clazz == NotesUniversalNoteIdStruct.class || clazz == Structure.class) {
			return (T) m_struct;
		}
		else if (clazz == String.class) {
			return (T) toString();
		}
		
		return null;
	}
}
