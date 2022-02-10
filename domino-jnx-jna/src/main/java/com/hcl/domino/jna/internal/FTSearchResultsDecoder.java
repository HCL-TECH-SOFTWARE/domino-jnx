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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.hcl.domino.data.FTQuery;
import com.hcl.domino.data.NoteIdWithScore;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Pointer;

public class FTSearchResultsDecoder {

	public static List<NoteIdWithScore> decodeNoteIdsWithStoreSearchResult(Pointer ptr, 
			EnumSet<FTQuery> searchOptions) {
		
		int numHits = ptr.getInt(0);
		ptr = ptr.share(4);
		
		short flags = ptr.getShort(0);
		ptr = ptr.share(2);

		if ((flags & NotesConstants.FT_RESULTS_EXPANDED) == NotesConstants.FT_RESULTS_EXPANDED) {
			throw new IllegalArgumentException("Domain searches are currently unsupported");
		}
		if ((flags & NotesConstants.FT_RESULTS_URL) == NotesConstants.FT_RESULTS_URL) {
			throw new IllegalArgumentException("FT results with URL are currently unsupported");
		}

		@SuppressWarnings("unused")
		short varLength = ptr.getShort(0);
		ptr = ptr.share(2);
		
		List<Integer> noteIds = new ArrayList<>();
		List<Integer> scores = new ArrayList<>();
		
		for (int i=0; i<numHits; i++) {
			int currNoteId = ptr.getInt(0);
			noteIds.add(currNoteId);
			ptr = ptr.share(4);
		}
		
		if ((flags & NotesConstants.FT_RESULTS_SCORES) == NotesConstants.FT_RESULTS_SCORES) {
			for (int i=0; i<numHits; i++) {
				byte currScore = ptr.getByte(0);
				scores.add(currScore & 0xff);
				ptr = ptr.share(1);
			}
		}

		List<NoteIdWithScore> noteIdsWithScore = new ArrayList<>();
		for (int i=0; i<numHits; i++) {
			int currNoteId = noteIds.get(i);
			int currScore = (i < scores.size()) ? scores.get(i) : 0;
			
			noteIdsWithScore.add(new NoteIdWithScore(currNoteId, currScore));
		}
		
		return noteIdsWithScore;
	}
}
