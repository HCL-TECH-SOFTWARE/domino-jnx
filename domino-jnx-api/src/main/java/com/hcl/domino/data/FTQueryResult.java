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

import java.util.List;
import java.util.Optional;

/**
 * Container for a FT search result
 * 
 * @author Karsten Lehmann
 */
public interface FTQueryResult extends DbQueryResult<FTQueryResult> {
	
	/**
	 * Returns the duration the search took in milliseconds
	 * 
	 * @return duration
	 */
	long getSearchDuration();
	
	/**
	 * Returns the actual number of documents found for this search. This number may be greater than {@link #getNumDocs()}
	 * if {@link Database#queryFTIndex(String, int, java.util.Set, java.util.Set, int, int)}
	 * is used with paging parameters.
	 * 
	 * @return hits
	 */
	int getNumHits();
	
	/**
	 * Returns the number of documents returned in the results, may be less than {@link #getNumHits()} if
	 * {@link Database#queryFTIndex(String, int, java.util.Set, java.util.Set, int, int)}
	 * is used with paging parameters.
	 * 
	 * @return count
	 */
	int getNumDocs();
	
	/**
	 * Returns an {@link IDTable} of documents matching the search.
	 * 
	 * @return an {@link Optional} describing the IDTable if ftSearch method has been used or ftSearchExt has been called with
	 *      {@link FTQuery#RETURN_IDTABLE} option (and we have any hits), or an empty one if it was not
	 */
	@Override Optional<IDTable> getNoteIds();
	
	/**
	 * Returns the sorted note ids of search matches with their search score (0-255).
	 * 
	 * @return matches with note id and search score
	 */
	List<NoteIdWithScore> getMatchesWithScore();
	
	/**
	 * When using {@link FTQuery#RETURN_HIGHLIGHT_STRINGS}, this method returns
	 * the search strings parsed from the FT query. E.g. for a
	 * query "(greg* or west*) and higg*", the list contains
	 * "greg*", "west*" and "higg*".
	 * 
	 * @return hightlight strings or empty list
	 */
	List<String> getHighlightStrings();
	
}
