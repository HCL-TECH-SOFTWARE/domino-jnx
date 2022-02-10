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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

import com.hcl.domino.data.Database.Action;
import com.hcl.domino.commons.data.AbstractTypedAccess;
import com.hcl.domino.commons.views.IItemTableData;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.dbdirectory.DirectorySearchQuery.SearchFlag;
import com.hcl.domino.jna.internal.search.NotesSearch;
import com.hcl.domino.jna.internal.search.NotesSearch.JNASearchMatch;

/**
 * Utility class to dynamically compute summary buffer values for an ordered list
 * of documents, specified as an {@link Iterator} of their note ids.<br>
 * <br>
 * The method leverages NSFSearchExtended3, which supports both specifying the documents
 * to process as an IDTable (so it does not run on the whole database) and passing
 * "column formulas" that we want to be computed.
 * 
 * @author Karsten Lehmann
 */
public class DocumentSummaryIterator implements Iterator<DocumentSummaryIterator.DocumentData> {
	private PagedDocumentSummaryIterator m_pagedIterator;
	private List<DocumentSummaryIterator.DocumentData> m_nextPage;

	public DocumentSummaryIterator(JNADatabase db, int pageSize, Iterator<Integer> noteIdIt,
			int skip, int count, Map<String,String> columnFormulas) {
		this(db, pageSize, noteIdIt, skip, count, columnFormulas, EnumSet.of(DocumentClass.DATA));
	}
	
	public DocumentSummaryIterator(JNADatabase db, int pageSize, Iterator<Integer> noteIdIt,
			int skip, int count,
			Map<String,String> columnFormulas, Set<DocumentClass> documentClasses) {
		
		m_pagedIterator = new PagedDocumentSummaryIterator(db, pageSize, noteIdIt, skip, count, columnFormulas, documentClasses);
		m_nextPage = fetchNextPage();
	}
	
	private List<DocumentSummaryIterator.DocumentData> fetchNextPage() {
		if (m_pagedIterator.hasNext()) {
			return m_pagedIterator.next();
		}
		else {
			return null;
		}
	}
	
	@Override
	public boolean hasNext() {
		return m_nextPage!=null;
	}
	
	@Override
	public DocumentData next() {
		if (m_nextPage==null) {
			throw new NoSuchElementException();
		}
		DocumentData data = m_nextPage.remove(0);
		if (m_nextPage.isEmpty()) {
			m_nextPage = fetchNextPage();
		}
		return data;
	}
	
	public static class DocumentData extends AbstractTypedAccess {
		private NotesSearch.JNASearchMatch m_searchMatch;
		private Map<String,List<?>> m_summaryData;
		
		public DocumentData(NotesSearch.JNASearchMatch searchMatch, Map<String,List<?>> summaryData) {
			m_searchMatch = searchMatch;
			m_summaryData = summaryData;
		}
		
		@Override
		public List<?> getItemValue(String itemName) {
			return m_summaryData.get(itemName);
		}
		
		public NotesSearch.JNASearchMatch getSearchMatch() {
			return m_searchMatch;
		}

		@Override
		public boolean hasItem(String itemName) {
			return m_summaryData.containsKey(itemName);
		}

		@Override
		public List<String> getItemNames() {
			return new ArrayList<>(m_summaryData.keySet());
		}
		
		public Map<String,List<?>> getAllSummaryData() {
			return m_summaryData;
		}
	}

	private class PagedDocumentSummaryIterator implements Iterator<List<DocumentSummaryIterator.DocumentData>> {
		private JNADatabase m_db;
		private int m_pageSize;
		private Iterator<Integer> m_noteIdIt;
		private int m_skip;
		private int m_count;
		private int m_skipped;
		private int m_processed;
		private boolean m_done;
		private Map<String,String> m_columnFormulas;
		private Set<DocumentClass> m_documentClasses;
		
		private List<DocumentData> m_nextPage;
		
		public PagedDocumentSummaryIterator(JNADatabase db, int pageSize, Iterator<Integer> noteIdIt,
				int skip, int count,
				Map<String,String> columnFormulas, Set<DocumentClass> documentClasses) {
			
			m_db = db;
			m_pageSize = pageSize;
			m_noteIdIt = noteIdIt;
			m_skip = skip;
			m_count = count;
			m_columnFormulas = columnFormulas;
			m_documentClasses = documentClasses;
			
			m_nextPage = produceNextPage();
		}
		
		@Override
		public boolean hasNext() {
			return m_nextPage!=null;
		}
		
		@Override
		public List<DocumentSummaryIterator.DocumentData> next() {
			if (m_nextPage==null) {
				throw new NoSuchElementException();
			}
			
			List<DocumentSummaryIterator.DocumentData> page = m_nextPage;
			m_nextPage = produceNextPage();
			return page;
		}
		
		private List<DocumentSummaryIterator.DocumentData> produceNextPage() {
			if (!m_noteIdIt.hasNext() || m_done) {
				return null;
			}
			
			JNAIDTable idTable = new JNAIDTable(m_db.getParentDominoClient());
			
			List<Integer> noteIdsInPage = new ArrayList<>();
			
			while (m_skipped < m_skip && m_noteIdIt.hasNext()) {
				m_noteIdIt.next();
				m_skipped++;
			}
			
			//collect ids for next page
			for (int i=0; i<m_pageSize; i++) {
				if (m_noteIdIt.hasNext()) {
					if (m_processed < m_count) {
						Integer noteId = m_noteIdIt.next();
						noteIdsInPage.add(noteId);
						m_processed++;
					}
					else {
						m_done = true;
						break;
					}
				}
				else {
					break;
				}
			}
			idTable.addAll(noteIdsInPage);
			
			LinkedHashMap<Integer,DocumentData> dataByNoteId = new LinkedHashMap<>();
			
			//read summary data for note ids to produce next page
			NotesSearch.search(m_db, idTable, "@true", m_columnFormulas, "-", EnumSet.of(SearchFlag.SUMMARY, //$NON-NLS-1$ //$NON-NLS-2$
					SearchFlag.SESSION_USERNAME),
					m_documentClasses, null, new NotesSearch.SearchCallback() {
						
				private Map<String,List<?>> getSummaryData(IItemTableData summaryBufferData) {
					Map<String,List<?>> data = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

					if (summaryBufferData!=null) {
						if (!m_columnFormulas.isEmpty()) {
							for (String currItemName : m_columnFormulas.keySet()) {
								Object currItemValue = summaryBufferData.get(currItemName, Object.class, null);
								if (currItemValue!=null) {
									if (currItemValue instanceof List) {
										data.put(currItemName, (List<?>) currItemValue);
									}
									else {
										data.put(currItemName, Arrays.asList(currItemValue));
									}
								}
							}
						}
						else {
							for (String currItemName : summaryBufferData.getItemNames()) {
								Object currItemValue = summaryBufferData.get(currItemName, Object.class, null);
								if (currItemValue!=null) {
									if (currItemValue instanceof List) {
										data.put(currItemName, (List<?>) currItemValue);
									}
									else {
										data.put(currItemName, Arrays.asList(currItemValue));
									}
								}
							}
						}
					}
					
					return data;
				}

				@Override
				public Action noteFound(JNADatabase parentDb, JNASearchMatch searchMatch, IItemTableData summaryBufferData) {
					Map<String,List<?>> summaryData = getSummaryData(summaryBufferData);

					DocumentData docInfo = new DocumentData(searchMatch, summaryData);
					dataByNoteId.put(searchMatch.getNoteID(), docInfo);

					return Action.Continue;
				}

				@Override
				public Action deletionStubFound(JNADatabase parentDb, JNASearchMatch searchMatch,
						IItemTableData summaryBufferData) {

					return Action.Continue;
				}

				@Override
				public Action noteFoundNotMatchingFormula(JNADatabase parentDb, JNASearchMatch searchMatch,
						IItemTableData summaryBufferData) {

					return Action.Continue;
				}
			}

					);
			idTable.dispose();
			
			List<DocumentData> page = new ArrayList<>();

			for (Integer currNoteId : noteIdsInPage) {
				DocumentData currNoteData = dataByNoteId.get(currNoteId);
				if (currNoteData!=null) {
					page.add(currNoteData);
				}
			}

			//repeat search if we could not find any data
			while (page!=null && page.isEmpty()) {
				List<DocumentData> nextPage = produceNextPage();
				if (nextPage==null) {
					return null;
				}
				else {
					page.clear();
					page.addAll(nextPage);
				}
			}
			
			return page;
		}
		

	}
}
