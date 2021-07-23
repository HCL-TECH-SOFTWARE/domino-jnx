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
package com.hcl.domino.data;

import java.text.MessageFormat;

/**
 * Container for one FT search result entry, containing a note id and the search score (or 0 if
 * no scores have been collected).
 * 
 * @author Karsten Lehmann
 */
public class NoteIdWithScore {
	private int m_noteId;
	private int m_score;
	
	public NoteIdWithScore(int noteId, int score) {
		m_noteId = noteId;
		m_score = score;
	}
	
	public int getNoteId() {
		return m_noteId;
	}
	
	public int getScore() {
		return m_score;
	}

	@Override
	public String toString() {
		return MessageFormat.format("NoteIdWithScore [noteId={0}, score={1}]", m_noteId, m_score); //$NON-NLS-1$
	}
	
}
