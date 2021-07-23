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

import static java.text.MessageFormat.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.hcl.domino.DominoException;
import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.CollectionSearchQuery.CollectionEntryProcessor;
import com.hcl.domino.data.CollectionSearchQuery.SelectedEntries;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Database.Action;
import com.hcl.domino.exception.IncompatibleImplementationException;
import com.hcl.domino.data.DbQueryResult;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.data.Navigate;
import com.hcl.domino.jna.data.DocumentSummaryIterator.DocumentData;
import com.hcl.domino.jna.internal.search.NotesSearch.JNASearchMatch;
import com.hcl.domino.misc.Loop;

public abstract class DbQueryResultUtil<QUERYTYPE extends DbQueryResult<?>> implements DbQueryResult<QUERYTYPE> {
	private Map<String,String> m_computeValues;
	private JNADominoCollection m_sortCollection;
	private Integer m_total;
	
	public DbQueryResultUtil() {
		m_computeValues = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	}
	
	@Override
	public abstract Database getParentDatabase();
	
	@Override
	public abstract Optional<IDTable> getNoteIds();

	@SuppressWarnings("unchecked")
	@Override
	public QUERYTYPE computeValues(Map<String, String> itemsAndFormulas) {
		m_computeValues.putAll(itemsAndFormulas);
		return (QUERYTYPE) this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public QUERYTYPE computeValues(String... itemsAndFormulas) {
		if (itemsAndFormulas==null || itemsAndFormulas.length==0) {
			return (QUERYTYPE) this;
		}
		
		if ((itemsAndFormulas.length % 2)==1) {
			throw new IllegalArgumentException(format("List of item/formulas must have an even number of entries: {0}", Arrays.toString(itemsAndFormulas)));
		}
		
		for (int i=0; i<itemsAndFormulas.length; i+=2) {
			m_computeValues.put(itemsAndFormulas[i], itemsAndFormulas[i+1]);
		}
		return (QUERYTYPE) this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public QUERYTYPE sort(DominoCollection collection) {
		if (!(collection instanceof JNADominoCollection)) {
			throw new IncompatibleImplementationException(collection, JNADominoCollection.class);
		}
		m_sortCollection = (JNADominoCollection) collection;
		return (QUERYTYPE) this;
	}

	@Override
	public Stream<Document> getDocuments() {
		Iterator<Integer> idsIt;
		
		if (m_sortCollection!=null) {
			//produce note id iterator in collection order
			idsIt = collectIds(0, Integer.MAX_VALUE).iterator();
		}
		else {
			idsIt = getNoteIds().get().iterator();
		}
		
		Database db = getParentDatabase();
		
		Spliterator<Integer> noteIdsSplitIt = Spliterators.spliteratorUnknownSize(idsIt, 0);
		Stream<Integer> noteIdStream = StreamSupport.stream(noteIdsSplitIt, false);
		
		return noteIdStream
			.map(db::getDocumentById)
			.filter(Optional::isPresent)
			.map(Optional::get);
	}

	@Override
	public <T> T build(int skip, int count, CollectionEntryProcessor<T> processor) {
		IDTable ids = getNoteIds().get();
		JNADatabase parentDb = (JNADatabase) getParentDatabase();

		if (m_sortCollection!=null) {
			T result = processor.start();

			if (count>0) {
				//select note ids in view and find out which of them are in the requested page (skip, count)
				Set<Integer> idsInCollectionOrder = m_sortCollection
						.query()
						.select(
								SelectedEntries
								.deselectAll()
								.select(ids)
								)
						.direction(Navigate.NEXT_SELECTED)
						.collectIds(skip, count);

				int pageSize = Math.min(count, 20000);

				DocumentSummaryIterator summaryIterator = new DocumentSummaryIterator(parentDb,
						pageSize, idsInCollectionOrder.iterator(),
						skip, count, m_computeValues);
				
				while (summaryIterator.hasNext()) {
					DocumentData currDocData = summaryIterator.next();
					
					JNADocSummaryCollectionEntry entry = toCollectionEntry(parentDb, m_sortCollection, currDocData);
					
					Action action = processor.entryRead(result, entry);
					if (action == Action.Stop) {
						break;
					}
				}
			}
			
			result = processor.end(result);
			return result;
		}
		else {
			JNADominoCollection defaultCollection = (JNADominoCollection) parentDb.openDefaultCollection().orElseThrow(
				() -> new DominoException(format("No default collection found in database {0}!!{1}", parentDb.getServer(), parentDb.getRelativeFilePath()))
			);
			
			T result = processor.start();
			
			if (count>0) {
				Iterator<Integer> noteIdIt = ids.iterator();

				int pageSize = Math.min(count, 20000);

				DocumentSummaryIterator summaryIterator = new DocumentSummaryIterator(parentDb,
						pageSize, noteIdIt,
						skip, count, m_computeValues);
				
				while (summaryIterator.hasNext()) {
					DocumentData currDocData = summaryIterator.next();
					
					JNADocSummaryCollectionEntry entry = toCollectionEntry(parentDb, defaultCollection, currDocData);
					
					Action action = processor.entryRead(result, entry);
					if (action == Action.Stop) {
						break;
					}
				}
			}

			result = processor.end(result);
			return result;
		}
	}

	private JNADocSummaryCollectionEntry toCollectionEntry(JNADatabase parentDb, JNADominoCollection parentCollection,
			DocumentData currDocData) {
		JNASearchMatch searchMatch = currDocData.getSearchMatch();
		
		TreeMap<String,Object> caseInsensitiveSummaryData = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		caseInsensitiveSummaryData.putAll(currDocData.getAllSummaryData());
		
		JNADocSummaryCollectionEntry entry = new JNADocSummaryCollectionEntry(parentDb, parentCollection,
				searchMatch.getNoteID(), searchMatch.getUNID(), searchMatch.getSequenceNumber(),
				searchMatch.getSequenceTime(), caseInsensitiveSummaryData);
		
		return entry;
	}
	
	@Override
	public Set<Integer> collectIds(int skip, int count) {
		if (count==0) {
			return new LinkedHashSet<>();
		}
		
		IDTable ids = getNoteIds().get();

		if (m_sortCollection!=null) {
			//select note ids in view and find out which of them are in the requested page (skip, count)
			Set<Integer> idsInCollectionOrder = m_sortCollection
					.query()
					.select(
							SelectedEntries
							.deselectAll()
							.select(ids)
							)
					.direction(Navigate.NEXT_SELECTED)
					.collectIds(skip, count);

			return idsInCollectionOrder;
		}
		else {
			LinkedHashSet<Integer> result = new LinkedHashSet<>();
			int skipped = 0;
			int processed = 0;
			
			Iterator<Integer> idIt = ids.iterator();
			while (idIt.hasNext()) {
				Integer currNoteId = idIt.next();
				if (skipped < skip) {
					skipped++;
					continue;
				}
				else {
					result.add(currNoteId);
					processed++;
				}
				
				if (processed >= count) {
					break;
				}
			}
			
			return result;
		}
	}

	@Override
	public void collectIds(int skip, int count, Collection<Integer> idTable) {
		if (count==0) {
			return;
		}
		
		IDTable ids = getNoteIds().get();

		if (m_sortCollection!=null) {
			//select note ids in view and find out which of them are in the requested page (skip, count)
			Set<Integer> idsInCollectionOrder = m_sortCollection
					.query()
					.select(
							SelectedEntries
							.deselectAll()
							.select(ids)
							)
					.direction(Navigate.NEXT_SELECTED)
					.collectIds(skip, count);

			idTable.addAll(idsInCollectionOrder);
		}
		else {
			int skipped = 0;
			int processed = 0;
			
			Iterator<Integer> idIt = ids.iterator();
			while (idIt.hasNext()) {
				Integer currNoteId = idIt.next();
				if (skipped < skip) {
					skipped++;
					continue;
				}
				else {
					idTable.add(currNoteId);
					processed++;
				}
				
				if (processed >= count) {
					break;
				}
			}
		}
	}

	@Override
	public List<CollectionEntry> collectEntries(int skip, int count) {
		return build(skip, count, new CollectionEntryProcessor<List<CollectionEntry>>() {

			@Override
			public List<CollectionEntry> start() {
				return new ArrayList<>();
			}

			@Override
			public Action entryRead(List<CollectionEntry> result, CollectionEntry entry) {
				result.add(entry);
				return Action.Continue;
			}

			@Override
			public List<CollectionEntry> end(List<CollectionEntry> result) {
				return result;
			}
		});
	}

	@Override
	public void collectEntries(int skip, int count, Collection<CollectionEntry> collection) {
		build(skip, count, new CollectionEntryProcessor<Collection<CollectionEntry>>() {

			@Override
			public Collection<CollectionEntry> start() {
				return collection;
			}

			@Override
			public Action entryRead(Collection<CollectionEntry> result, CollectionEntry entry) {
				result.add(entry);
				return Action.Continue;
			}

			@Override
			public Collection<CollectionEntry> end(Collection<CollectionEntry> result) {
				return result;
			}
		});
	}

	private static class LoopImpl extends Loop {
		
		public void next() {
			super.setIndex(getIndex()+1);
		}
		
		@Override
		public void setIsLast() {
			super.setIsLast();
		}
	}
	
	@Override
	public void forEachDocument(int skip, int count, BiConsumer<Document, Loop> consumer) {
		JNADatabase parentDb = (JNADatabase) getParentDatabase();
		IDTable ids = getNoteIds().get();
		LoopImpl loop = new LoopImpl();

		Iterator<Document> docIt;

		if (m_sortCollection!=null) {
			//select note ids in view and find out which of them are in the requested page (skip, count)
			Set<Integer> idsInCollectionOrder = m_sortCollection
					.query()
					.select(
							SelectedEntries
							.deselectAll()
							.select(ids)
							)
					.direction(Navigate.NEXT_SELECTED)
					.collectIds(skip, count);

			docIt = idsInCollectionOrder.stream()
				.map(parentDb::getDocumentById)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.iterator();
		}
		else {
			Set<Integer> idsInPage = collectIds(skip, count);
			docIt = idsInPage.stream()
				.map(parentDb::getDocumentById)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.iterator();
		}

		while (docIt.hasNext()) {
			Document currDoc = docIt.next();

			if (!docIt.hasNext()) {
				loop.setIsLast();
			}

			consumer.accept(currDoc, loop);

			if (loop.isStopped()) {
				break;
			}

			loop.next();
		}
	}
	
	@Override
	public int size() {
		IDTable ids = getNoteIds().get();
		
		if (m_total==null) {
			if (m_sortCollection!=null) {
				m_total = m_sortCollection
						.query()
						.select(
								SelectedEntries
								.deselectAll()
								.select(ids)
								)
						.direction(Navigate.NEXT_SELECTED)
						.size();
			}
			else {
				m_total = ids.size();
			}
		}
		return m_total;
	}
		
}
