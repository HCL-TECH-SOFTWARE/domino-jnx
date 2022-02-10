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
package com.hcl.domino.jna.internal;

import com.hcl.domino.jna.data.JNADominoOriginatorId;
import com.hcl.domino.jna.internal.structs.NotesOriginatorIdStruct;

/**
 * Data container for the {@link NotesOriginatorIdStruct} fields used
 * for the sync. Use to speed up the sync process, avoiding unnecessary
 * JNA structures.
 * 
 * Karsten Lehmann
 */
public class NotesOriginatorIdData {
	private String m_unid;
	private int m_seq;
	private int[] m_seqTimeInnards;
	
	public NotesOriginatorIdData(String unid, int seq, int[] seqTimeInnards) {
		m_unid = unid;
		m_seq = seq;
		m_seqTimeInnards = seqTimeInnards;
	}
	
	public NotesOriginatorIdData(JNADominoOriginatorId oid) {
		m_unid = oid.getUNID();
		m_seq = oid.getSequence();
		m_seqTimeInnards = oid.getSequenceTime().getAdapter(int[].class);
	}
	
	public String getUNID() {
		return m_unid;
	}
	
	public int getSequence() {
		return m_seq;
	}
	
	public int[] getSequenceTimeInnards() {
		return m_seqTimeInnards;
	}
}
