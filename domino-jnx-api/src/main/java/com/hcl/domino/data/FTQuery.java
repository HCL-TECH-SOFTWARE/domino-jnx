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

import com.hcl.domino.misc.INumberEnum;

/**
 * These values define the options you may specify in the options
 * parameter when performing a full text search in a database.
 * These options may be combined.
 * 
 * @author Karsten Lehmann
 */
public enum FTQuery implements INumberEnum<Integer> {
//	/** Store search results in NIF collections; Don't return them to caller */
//	SET_COLL(0x00000001),
	/** Return # hits only; not the documents */
	NUMDOCS_ONLY(0x00000002),
	/** Refine the query using the {@link IDTable} */
	REFINE(0x00000004),
	/** Return document scores (default sort) */
	SCORES(0x00000008),
	/** Return ID table, can be read via {@link FTQueryResult#getMatchesWithScore()} */
	RETURN_IDTABLE(0x00000010),
	
	/** Use Limit arg. to return only top scores */
	TOP_SCORES(0x00000080),
	/** Stem words in this query */
	STEM_WORDS(0x00000200),
	/** Thesaurus words in this query */
	THESAURUS_WORDS(0x00000400),
	/** Search w/o index, requires a {@link IDTable} to specify
	 * the docs to create a temporary index. By default, not more
	 * than 5000 docs can be specified here. Use Notes.ini variable
	 * TEMP_INDEX_MAX_DOC to increase this limit.<br>
	 * See this technote for details:<br>
	 * <a href="https://www.ibm.com/support/pages/error-maximum-allowable-documents-exceeded-temporary-index-log">Error: '...Maximum allowable documents exceeded for a temporary index' in log</a> */ 
	NOINDEX(0x00000800),
	/** set if fuzzy search wanted */
	FUZZY(0x00004000),
//	/** Return url-based results (FTSearchExt only) */
//	EXT_RET_URL(0x00008000),
//	/** this is a domain search */
//	EXT_DOMAIN(0x00040000),
//	/** search the filesystem index (Domain Search only) */
//	EXT_FILESYSTEM(0x00100000),
//	/** search the database index (Domain Search only) */
//	EXT_DATABASE(0x00200000),
	/** return highlight strings */ 
	RETURN_HIGHLIGHT_STRINGS(0x00080000),
	
	// sort options; if SORT_DATE_MODIFIED or SORT_DATE_CREATED are not specified and we retrieved SCORES,
	// results are sorted by scores in descending order. Use SORT_ASCEND to reverse the sort order
	
	/** Sort results by last modified date (modified in this replica, returned by
	 * {@link DominoOriginatorId#getSequenceTime()} in the OID read via {@link Document#getOID()}  */
	SORT_DATE_MODIFIED(0x00000020),
	/** Sort by created date (default is to sort by modified date) */
	SORT_DATE_CREATED(0x00010000),
	/** Sort in ascending order (e.g. ascending score when combined with {@link #SCORES} ) */
	SORT_ASCENDING(0x00000040);

	private int m_val;
	
	FTQuery(int val) {
		m_val = val;
	}
	
	@Override
	public Integer getValue() {
		return m_val;
	}
	
	@Override
	public long getLongValue() {
		return m_val;
	}
	
}
