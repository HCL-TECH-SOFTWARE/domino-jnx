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
package com.hcl.domino.jna.data;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.CollectionSearchQuery.CollectionEntryProcessor;
import com.hcl.domino.data.DQLQueryResult;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.misc.Loop;

/**
 * Contains the computation result for a DQL query
 * 
 * @author Karsten Lehmann
 */
public class JNADQLQueryResult extends DQLQueryResult {
	private JNADatabase parentDb;
	private String m_query;
	private JNAIDTable m_idTable;
	private int m_idTableCountSaved;
	private String m_explainTxt;
	private long m_durationInMillis;
	private DbQueryResultUtil<DQLQueryResult> m_queryUtil;
	
	JNADQLQueryResult(JNADatabase parentDb,
			String query, JNAIDTable idTable, String explainTxt, long durationInMillis) {
		
		this.parentDb = parentDb;
		this.m_query = query;
		this.m_idTable = idTable;
		this.m_idTableCountSaved = idTable==null ? 0 : idTable.size();
		this.m_explainTxt = explainTxt;
		this.m_durationInMillis = durationInMillis;
		this.m_queryUtil = new DbQueryResultUtil<DQLQueryResult>() {

			@Override
			public Database getParentDatabase() {
				return JNADQLQueryResult.this.getParentDatabase();
			}

			@Override
			public Optional<IDTable> getNoteIds() {
				return JNADQLQueryResult.this.getNoteIds();
			}
			
		};
	}
	
	@Override
	public Database getParentDatabase() {
		return parentDb;
	}
	
	@Override
	public String getQuery() {
		return this.m_query;
	}
	
	@Override
	public Optional<IDTable> getNoteIds() {
		return Optional.ofNullable(this.m_idTable);
	}
	
	@Override
	public String getExplainText() {
		return this.m_explainTxt;
	}

	@Override
	public long getDurationInMillis() {
		return this.m_durationInMillis;
	}
	
	@Override
	public String toString() {
		if (this.m_idTable!=null && this.m_idTable.isDisposed()) {
			return MessageFormat.format(
				"JNADQLQueryResult [duration={0}, count={1}, IDTable recycled, query={2}]", //$NON-NLS-1$
				this.m_durationInMillis, this.m_idTableCountSaved, this.m_query
			);
		}
		else {
			return MessageFormat.format(
				"JNADQLQueryResult [duration={0}, count={1}, query={2}]", //$NON-NLS-1$
				this.m_durationInMillis, (this.m_idTable==null ? "0" : this.m_idTable.size()), this.m_query //$NON-NLS-1$
			);
		}
	}

	@Override
	public <T> T build(int skip, int count, CollectionEntryProcessor<T> processor) {
		return this.m_queryUtil.build(skip, count, processor);
	}

	@Override
	public Set<Integer> collectIds(int skip, int count) {
		return this.m_queryUtil.collectIds(skip, count);
	}

	@Override
	public void collectIds(int skip, int count, Collection<Integer> idTable) {
		this.m_queryUtil.collectIds(skip, count, idTable);
	}

	@Override
	public List<CollectionEntry> collectEntries(int skip, int count) {
		return this.m_queryUtil.collectEntries(skip, count);
	}

	@Override
	public void collectEntries(int skip, int count, Collection<CollectionEntry> collection) {
		this.m_queryUtil.collectEntries(skip, count, collection);
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
	public DQLQueryResult computeValues(Map<String, String> itemsAndFormulas) {
		m_queryUtil.computeValues(itemsAndFormulas);
		return this;
	}
	
	@Override
	public DQLQueryResult computeValues(String... itemsAndFormulas) {
		m_queryUtil.computeValues(itemsAndFormulas);
		return this;
	}
	
	@Override
	public DQLQueryResult sort(DominoCollection collection) {
		m_queryUtil.sort(collection);
		return this;
	}
	
}
