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
import java.util.Arrays;
import java.util.Formatter;

import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.DominoOriginatorId;
import com.hcl.domino.data.DominoUniversalNoteId;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.jna.internal.structs.NotesOriginatorIdStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class JNADominoOriginatorId implements DominoOriginatorId, IAdaptable {
	private String unid;
	private int sequence;
	/** C type : TIMEDATE */
	private DominoDateTime sequenceTime;
	
	/** structure is lazily created */
	private NotesOriginatorIdStruct m_struct;
	
	public JNADominoOriginatorId(IAdaptable adaptable) {
		NotesOriginatorIdStruct struct = adaptable.getAdapter(NotesOriginatorIdStruct.class);
		if (struct!=null) {
			m_struct = struct;
			this.unid = m_struct.getUNIDAsString();
			this.sequence = m_struct.Sequence;
			this.sequenceTime = m_struct.SequenceTime==null ? null : new JNADominoDateTime(m_struct.SequenceTime);
			return;
		}
		Pointer p = adaptable.getAdapter(Pointer.class);
		if (p!=null) {
			m_struct = NotesOriginatorIdStruct.newInstance(p);
			m_struct.read();
			this.unid = m_struct.getUNIDAsString();
			this.sequence = m_struct.Sequence;
			this.sequenceTime = m_struct.SequenceTime==null ? null : new JNADominoDateTime(m_struct.SequenceTime);
			return;
		}
		throw new IllegalArgumentException("Constructor argument cannot provide a supported datatype");
	}
	
	@Override
	public DominoDateTime getFile() {
		//creating structure converts UNID to file/note
		NotesOriginatorIdStruct struct = getAdapter(NotesOriginatorIdStruct.class);
		if (struct==null) {
			throw new IllegalStateException("NotesOriginatorIdStruct expected not to be null");
		}
		
		if (struct.File!=null) {
			return new JNADominoDateTime(struct.File.Innards);
		} else {
			throw new IllegalStateException("NotesOriginatorIdStruct had a null File value");
		}
	}

	@Override
	public DominoDateTime getNote() {
		//creating structure converts UNID to file/note
		NotesOriginatorIdStruct struct = getAdapter(NotesOriginatorIdStruct.class);
		if (struct==null) {
			throw new IllegalStateException("NotesOriginatorIdStruct expected not to be null");
		}
		
		if (struct.Note!=null) {
			return new JNADominoDateTime(struct.Note.Innards);
		} else {
			throw new IllegalStateException("NotesOriginatorIdStruct had a null Note value");
		}
	}

	@Override
	public int getSequence() {
		if (m_struct!=null) {
			this.sequence = m_struct.Sequence;
		}
		return this.sequence;
	}
	
	public void setSequence(int newSeq) {
		if (m_struct==null) {
			m_struct = NotesOriginatorIdStruct.newInstance();
		}
		m_struct.Sequence = newSeq;
		this.sequence = newSeq;
		
		//make sture SequenceTime has a value
		if (m_struct.SequenceTime==null) {
			m_struct.SequenceTime = NotesTimeDateStruct.newInstance();
			m_struct.SequenceTime.setNow();
			this.sequenceTime = new JNADominoDateTime(m_struct.SequenceTime);
		}
		m_struct.write();
	}
	
	public void setSequenceTime(TemporalAccessor td) {
		if (m_struct==null) {
			m_struct = NotesOriginatorIdStruct.newInstance();
		}
		if (m_struct.SequenceTime==null) {
			m_struct.SequenceTime = NotesTimeDateStruct.newInstance();
		}
		int[] innards = new JNADominoDateTime(td).getInnards();
		m_struct.SequenceTime.Innards[0] = innards[0];
		m_struct.SequenceTime.Innards[1] = innards[1];
		m_struct.SequenceTime.write();
		m_struct.write();
		this.sequenceTime = new JNADominoDateTime(innards);
	}
	
	@Override
	public DominoDateTime getSequenceTime() {
		if (this.m_struct!=null && this.m_struct.SequenceTime!=null) {
			if (this.sequenceTime==null ||
					Arrays.equals(this.sequenceTime.getAdapter(int[].class), this.m_struct.SequenceTime.Innards)) {
				
				this.sequenceTime = new JNADominoDateTime(this.m_struct.SequenceTime.Innards);
			}
		}
		return this.sequenceTime;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> clazz) {
		if (clazz == NotesOriginatorIdStruct.class || clazz == Structure.class) {
			if (m_struct==null) {
				m_struct = NotesOriginatorIdStruct.newInstance();
				m_struct.Sequence = this.sequence;
				m_struct.SequenceTime = this.sequenceTime==null ? null : this.sequenceTime.getAdapter(NotesTimeDateStruct.class);
				
				if (this.unid!=null) {
					m_struct.setUNID(this.unid);
				}
				m_struct.write();
			}
			return (T) m_struct;
		}
		else if (clazz == DominoUniversalNoteId.class || clazz == JNADominoUniversalNoteId.class) {
			return (T) getUNIDAsObj();
		}
		
		return null;
	}
	
	@Override
	public DominoUniversalNoteId getUNIDAsObj() {
		NotesOriginatorIdStruct struct = getAdapter(NotesOriginatorIdStruct.class);
		if (struct==null) {
			throw new IllegalStateException("NotesOriginatorIdStruct expected not to be null");
		}
		
		return new JNADominoUniversalNoteId(m_struct.getUNID());
	}
	
	@Override
	public String getUNID() {
		if (this.unid==null) {
			NotesOriginatorIdStruct struct = getAdapter(NotesOriginatorIdStruct.class);
			if (struct==null) {
				throw new IllegalStateException("NotesOriginatorIdStruct expected not to be null");
			}

			this.unid = struct.getUNIDAsString();
		}
		return this.unid;

	}
	
	@Override
	public String toString() {
	  StringBuilder result = new StringBuilder();
	  result.append(getUNID());
	  
	  int[] seqTime = getSequenceTime().getAdapter(int[].class);
	  
	  try(Formatter formatter = new Formatter()) {
	    formatter.format("%08x", getSequence()); //$NON-NLS-1$
      formatter.format("%08x", seqTime[0]); //$NON-NLS-1$
      formatter.format("%08x", seqTime[1]); //$NON-NLS-1$
      result.append(formatter.toString().toUpperCase());
	  }
	  
		return result.toString();
	}
}
