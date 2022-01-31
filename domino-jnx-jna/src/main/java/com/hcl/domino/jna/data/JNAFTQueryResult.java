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

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.CollectionSearchQuery.CollectionEntryProcessor;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.data.FTQueryResult;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.data.NoteIdWithScore;
import com.hcl.domino.misc.Loop;

/**
 * Container for a FT search result
 * 
 * @author Karsten Lehmann
 */
public class JNAFTQueryResult implements FTQueryResult {
	private JNADatabase m_parentDb;
	private JNAIDTable m_matchesIDTable;
	private int m_numDocs;
	private int m_numHits;
	private List<String> m_highlightStrings;
	private List<NoteIdWithScore> m_noteIdsWithScore;
	private long m_searchDurationMS;
	private DbQueryResultUtil<FTQueryResult> m_queryUtil;

	public JNAFTQueryResult(JNADatabase parentDb, JNAIDTable matchesIDTable, int numDocs, int numHits,
			List<String> highlightStrings,
			List<NoteIdWithScore> noteIdsWithScore, long searchDurationMS) {
		
		m_parentDb = parentDb;
		m_matchesIDTable = matchesIDTable;
		m_numDocs = numDocs;
		m_numHits = numHits;
		m_highlightStrings = highlightStrings;
		m_noteIdsWithScore = noteIdsWithScore;
		m_searchDurationMS = searchDurationMS;
		this.m_queryUtil = new DbQueryResultUtil<FTQueryResult>() {

			@Override
			public Database getParentDatabase() {
				return JNAFTQueryResult.this.getParentDatabase();
			}

			@Override
			public Optional<IDTable> getNoteIds() {
				return JNAFTQueryResult.this.getNoteIds();
			}
		};
	}

	@Override
	public Database getParentDatabase() {
		return m_parentDb;
	}
	
	@Override
	public long getSearchDuration() {
		return m_searchDurationMS;
	}
	
	@Override
	public int getNumHits() {
		return m_numHits;
	}
	
	@Override
	public int getNumDocs() {
		return m_numDocs;
	}
	
	@Override
	public Optional<IDTable> getNoteIds() {
		return Optional.ofNullable(m_matchesIDTable);
	}
	
	@Override
	public List<NoteIdWithScore> getMatchesWithScore() {
		return m_noteIdsWithScore==null ? Collections.emptyList() : m_noteIdsWithScore;
	}
	
	@Override
	public List<String> getHighlightStrings() {
		return m_highlightStrings == null ? Collections.emptyList() : m_highlightStrings;
	}
	
	@Override
	public String toString() {
		return MessageFormat.format(
			"JNAFTSearchResult [numhits={0}, numdocs={1}, highlights={2}, hasidtable={3}, hasmatcheswithscore={4}, duration={5}ms]", //$NON-NLS-1$
			getNumHits(), getNumDocs(), getHighlightStrings(), (m_matchesIDTable!=null), (m_noteIdsWithScore!=null), m_searchDurationMS
		);
	}

	@Override
	public <T> T build(int skip, int count, CollectionEntryProcessor<T> processor) {
		return m_queryUtil.build(skip, count, processor);
	}

	@Override
	public Set<Integer> collectIds(int skip, int count) {
		return m_queryUtil.collectIds(skip, count);
	}

	@Override
	public void collectIds(int skip, int count, Collection<Integer> idTable) {
		m_queryUtil.collectIds(skip, count, idTable);
	}

	@Override
	public List<CollectionEntry> collectEntries(int skip, int count) {
		return m_queryUtil.collectEntries(skip, count);
	}

	@Override
	public void collectEntries(int skip, int count, Collection<CollectionEntry> collection) {
		m_queryUtil.collectEntries(skip, count, collection);
	}
	
	@Override
	public void forEachDocument(int skip, int count, BiConsumer<Document, Loop> consumer) {
		m_queryUtil.forEachDocument(skip, count, consumer);
	}
	
	@Override
	public Stream<Document> getDocuments() {
		return m_queryUtil.getDocuments();
	}
	
	@Override
	public int size() {
		return m_queryUtil.size();
	}


	@Override
	public FTQueryResult computeValues(Map<String, String> itemsAndFormulas) {
		m_queryUtil.computeValues(itemsAndFormulas);
		return this;
	}
	
	@Override
	public FTQueryResult computeValues(String... itemsAndFormulas) {
		m_queryUtil.computeValues(itemsAndFormulas);
		return this;
	}
	
	@Override
	public FTQueryResult sort(DominoCollection collection) {
		m_queryUtil.sort(collection);
		return this;
	}
	
}