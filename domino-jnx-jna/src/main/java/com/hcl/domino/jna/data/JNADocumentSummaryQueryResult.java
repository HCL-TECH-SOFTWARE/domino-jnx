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

import java.util.Collection;
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
import com.hcl.domino.data.DocumentSummaryQueryResult;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.data.FormulaQueryResult;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.misc.Loop;

public class JNADocumentSummaryQueryResult implements DocumentSummaryQueryResult {
	private JNADatabase m_parentDb;
	private DbQueryResultUtil<FormulaQueryResult> m_queryUtil;
	private Collection<Integer> m_noteIds;
	
	public JNADocumentSummaryQueryResult(JNADatabase parentDb, Collection<Integer> noteIds) {
		m_parentDb = parentDb;
		m_noteIds = noteIds;
		
		this.m_queryUtil = new DbQueryResultUtil<FormulaQueryResult>() {

			@Override
			public Database getParentDatabase() {
				return JNADocumentSummaryQueryResult.this.getParentDatabase();
			}

			@Override
			public Optional<IDTable> getNoteIds() {
				return JNADocumentSummaryQueryResult.this.getNoteIds();
			}
		};
	}
	
	@Override
	public Database getParentDatabase() {
		return m_parentDb;
	}

	@Override
	public Stream<Document> getDocuments() {
		return m_queryUtil.getDocuments();
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
	public JNADocumentSummaryQueryResult computeValues(String... itemsAndFormulas) {
		m_queryUtil.computeValues(itemsAndFormulas);
		return this;
	}

	@Override
	public JNADocumentSummaryQueryResult computeValues(Map<String, String> itemsAndFormulas) {
		m_queryUtil.computeValues(itemsAndFormulas);
		return this;
	}

	@Override
	public JNADocumentSummaryQueryResult sort(DominoCollection collection) {
		m_queryUtil.sort(collection);
		return this;
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
	public int size() {
		return m_queryUtil.size();
	}

	@Override
	public Optional<IDTable> getNoteIds() {
		return Optional.of(new JNAIDTable(m_parentDb.getParentDominoClient(), m_noteIds));
	}

}
