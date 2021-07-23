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

import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import com.hcl.domino.data.CollectionEntry.SpecialValue;
import com.hcl.domino.data.Database.Action;
import com.hcl.domino.dql.DQL.DQLTerm;
import com.hcl.domino.misc.Loop;

public interface CollectionSearchQuery extends SearchQuery {
	
	// decide where to start navigating the view
	
	/**
	 * Start reading view data at first entry
	 * 
	 * @return this search query
	 */
	CollectionSearchQuery startAtFirstEntry();
	
	/**
	 * Start reading view data at an entry with the specified note id
	 * 
	 * @param noteId note id
	 * @return this search query
	 */
	CollectionSearchQuery startAtEntryId(int noteId);
	
	/**
	 * Start reading at a fixed collection position. Please note that this
	 * position is not a unique identifier for entries, it may change
	 * over time when the view index changes.
	 * 
	 * @param pos position string, e.g. "1.2.1"
	 * @return this search query
	 */
	CollectionSearchQuery startAtPosition(String pos);
	
	/**
	 * Start reading at last view row
	 * 
	 * @return this search query
	 */
	CollectionSearchQuery startAtLastEntry();

	/**
	 * Restrict search result to a single category
	 * 
	 * @param category category, use "\" for multiple category levels
	 * @return this search query
	 */
	CollectionSearchQuery startAtCategory(String category);

	/**
	 * Restricts search result to a single category
	 * 
	 * @param categoryLevels different levels of category values, supports strings and {@link TemporalAccessor} objects
	 * @return this search query
	 */
	CollectionSearchQuery startAtCategory(List<Object> categoryLevels);

	
	// configure expanded and selected navigation, e.g. next selected, next expanded, next selected expanded
	
	/**
	 * Configure which elements are expanded
	 * 
	 * @param expandedEntries use {@link ExpandedEntries#expandAll()} or {@link ExpandedEntries#collapseAll()} to start building this object
	 * @return this search query
	 */
	CollectionSearchQuery expand(ExpandedEntries expandedEntries);
	
	/**
	 * Configure which elements are selected
	 * 
	 * @param selectedEntries use {@link SelectedEntries#selectAll()} or {@link SelectedEntries#deselectAll()} to start building this object
	 * @return this search query
	 */
	CollectionSearchQuery select(SelectedEntries selectedEntries);

	/**
	 * Convenience method that calls {@link #select(SelectedEntries)} with
	 * {@link SelectedEntries#deselectAll()} and {@link AllDeselectedEntries#selectByKey(String, boolean)}.
	 * 
	 * @param key lookup key
	 * @param exact true for exact match, false for prefix search
	 * @return this search query
	 */
	CollectionSearchQuery selectByKey(String key, boolean exact);

	/**
	 * Convenience method that calls {@link #select(SelectedEntries)} with
	 * {@link SelectedEntries#selectAll()} and {@link AllSelectedEntries#deselectByKey(List, boolean)}.
	 * 
	 * @param key lookup key
	 * @param exact true for exact match, false for prefix search
	 * @return this search query
	 */
	CollectionSearchQuery selectByKey(List<Object> key, boolean exact);

	/**
	 * Convenience method that calls {@link #select(SelectedEntries)} with
	 * {@link SelectedEntries#selectAll()} and {@link AllSelectedEntries#deselectByKey(String, boolean)}.
	 * 
	 * @param key lookup key
	 * @param exact true for exact match, false for prefix search
	 * @return this search query
	 */
	CollectionSearchQuery deselectByKey(String key, boolean exact);

	/**
	 * Convenience method that calls {@link #select(SelectedEntries)} with
	 * {@link SelectedEntries#deselectAll()} and {@link AllDeselectedEntries#selectByKey(List, boolean)}.
	 * 
	 * @param key lookup key
	 * @param exact true for exact match, false for prefix search
	 * @return this search query
	 */
	CollectionSearchQuery deselectByKey(List<Object> key, boolean exact);

	
	// decide how entries are read
	
	/**
	 * Direction and read mode
	 * 
	 * @param mode mode
	 * @return this search query
	 */
	CollectionSearchQuery direction(Navigate mode);
	
	/**
	 * Decodes the collection column values, they can be read via
	 * {@link CollectionEntry#get(String, Class, Object)} or
	 * {@link CollectionEntry#getAsList(String, Class, List)}.
	 * 
	 * @return this search query
	 */
	CollectionSearchQuery readColumnValues();
	
	/**
	 * Reads the UNID of each collection entry.
	 * 
	 * @return this search query
	 */
	CollectionSearchQuery readUNID();

	// methods to control reading expanded entries in categorized views
	
	/**
	 * Builder interface that takes the collection entries and
	 * produces a result.
	 *
	 * @param <T> type of computation result
	 */
	public interface CollectionEntryProcessor<T> {
		
		/**
		 * This method is called when we start reading collection entries.<br>
		 * <br>
		 * Since the view index might change while reading entries, we might
		 * need to restart reading from the beginning.
		 * 
		 * @return an object to be passed to {@link #entryRead(Object, CollectionEntry)} for each read entry
		 */
		T start();
		
		/**
		 * Method is called for each read collection entry
		 * 
		 * @param result result object to add the entry data
		 * @param entry entry
		 * @return action whether to continue or stop reading
		 */
		Action entryRead(T result, CollectionEntry entry);
		
		/**
		 * This method is called when view reading is done. Add code
		 * here to post process your lookup result.
		 * 
		 * @param result lookup result
		 * @return processed result
		 */
		T end(T result);
	}
	
	/**
	 * @since 1.0.18
	 */
	@FunctionalInterface
	public interface BasicCollectionEntryProcessor {
		/**
		 * Method is called for each read collection entry
		 * 
		 * @param entry the read entry
		 * @return action whether to continue or stop reading
		 */
		Action entryRead(CollectionEntry entry);
	}
	
	
	// decide what to do with the view content
	
	/**
	 * Build a result out of the collection entries
	 * 
	 * @param <T> result type
	 * @param skip paging offset
	 * @param count paging count
	 * @param processor builder code to produce the result
	 * @return result
	 */
	<T> T build(int skip, int count, CollectionEntryProcessor<T> processor);
	
	/**
	 * Iterates through all selected entries and performs an action on each.
	 * 
	 * <p>This is equivalent to calling {@link #build(int, int, CollectionEntryProcessor)} with
	 * a processor that performs no action on start/end and returns no value.</p>
	 * 
	 * @param processor the processor to execute on each entry
	 * @since 1.0.18
	 */
	default void forEach(BasicCollectionEntryProcessor processor) {
		build(0, Integer.MAX_VALUE, new CollectionEntryProcessor<Void>() {
			@Override public Void start() { return null; }

			@Override
			public Action entryRead(Void result, CollectionEntry entry) {
				processor.entryRead(entry);
				return null;
			}

			@Override public Void end(Void result) { return null; }
		});
	}
	
	/**
	 * Return the note ids of the search result as an ordered {@link Set}.
	 * 
	 * <p>Implementations are likely, but not guaranteed, to return a {@link LinkedHashSet}.</p>
	 * 
	 * @param skip paging offset
	 * @param count paging count
	 * @return set of note ids
	 */
	Set<Integer> collectIds(int skip, int count);

	/**
	 * Adds all note ids of the search result to a note ID collection, with special
	 * support for {@link IDTable}
	 * 
	 * @param skip paging offset
	 * @param count paging count
	 * @param idTable note ID collection
	 */
	void collectIds(int skip, int count, Collection<Integer> idTable);

	/**
	 * Collect all {@link CollectionEntry} objects as list
	 * 
	 * @param skip paging offset
	 * @param count paging count
	 * @return list of collection entries
	 */
	List<CollectionEntry> collectEntries(int skip, int count);

	/**
	 * Returns the first {@link CollectionEntry}
	 * 
	 * @return an {@code Optional} describing the first entry,
     * 			or an empty {@code Optional} if there are no
     * 			matched entries
	 */
	default Optional<CollectionEntry> firstEntry() {
		List<CollectionEntry> entries = collectEntries(0, 1);
		return Optional.ofNullable(entries.isEmpty() ? null : entries.get(0));
	}
	
	/**
	 * Returns the first {@link CollectionEntry}
	 * 
	 * @return an {@code Optional} describing the first entry's note ID,
     * 			or an empty {@code Optional} if there are no
     * 			matched entries
	 */
	default Optional<Integer> firstId() {
		Set<Integer> entries = collectIds(0, 1);
		return Optional.ofNullable(entries.isEmpty() ? null : entries.iterator().next());
	}

	/**
	 * Collect all {@link CollectionEntry} objects in a {@link Collection}
	 * 
	 * @param skip the number of entries to skip when collecting
	 * @param count the maximum number of entries to collect
	 * @param collection collection to add entries
	 */
	void collectEntries(int skip, int count, Collection<CollectionEntry> collection);

	/**
	 * Slow method to compute the total number of collection entries
	 * 
	 * @return total
	 */
	int size();
	
	// which data should be read from the view index or from the document summary data	
	
	/**
	 * Read additional meta data for the collection entries or their document
	 * 
	 * @param values special values
	 * @return this search query
	 */
	CollectionSearchQuery readSpecialValues(SpecialValue... values);

	/**
	 * Read additional meta data for the collection entries or their document
	 * 
	 * @param values special values
	 * @return this search query
	 */
	CollectionSearchQuery readSpecialValues(Collection<SpecialValue> values);
	
	/**
	 * Reads the document class for each view entry.
	 * 
	 * @return this search query
	 * @since 1.0.18
	 */
	CollectionSearchQuery readDocumentClass();

	void forEachDocument(int skip, int count, BiConsumer<Document, Loop> consumer);
	
	public enum ExpandMode { AllExpanded, AllCollapsed }
	
	public static class AllExpandedEntries extends ExpandedEntries {
		
		public AllExpandedEntries() {
			super(ExpandMode.AllExpanded);
		}
		
		public AllExpandedEntries collapse(int... noteIds) {
			if (getMode() == ExpandMode.AllExpanded) {
				if (noteIds!=null) {
					if (noteIds.length==1) {
						getNoteIds().add(noteIds[0]);
					}
					else {
						Collection<Integer> noteIdsAsList = new ArrayList<>(noteIds.length);
						for (int i=0; i<noteIds.length; i++) {
							noteIdsAsList.add(noteIds[i]);
						}
						getNoteIds().addAll(noteIdsAsList);
					}
				}
			}
			
			return this;
		}

		public AllExpandedEntries collapse(Set<Integer> noteIds) {
			if (getMode() == ExpandMode.AllExpanded) {
				getNoteIds().addAll(noteIds);
			}
			
			return this;
		}

		public AllExpandedEntries unselect(DQLTerm dql) {
			if (getMode() == ExpandMode.AllExpanded) {
				getDQLQueries().add(dql);
			}
			
			return this;
		}

		public AllExpandedEntries unselect(String ftQuery) {
			if (getMode() == ExpandMode.AllExpanded) {
				getFTQueries().add(ftQuery);
			}
			
			return this;
		}

	}
	
	public static class AllCollapsedEntries extends ExpandedEntries {
		
		public AllCollapsedEntries() {
			super(ExpandMode.AllCollapsed);
		}
	
		public AllCollapsedEntries expand(int... noteIds) {
			if (getMode() == ExpandMode.AllCollapsed) {
				if (noteIds!=null) {
					if (noteIds.length==1) {
						getNoteIds().add(noteIds[0]);
					}
					else {
						Collection<Integer> noteIdsAsList = new ArrayList<>(noteIds.length);
						for (int i=0; i<noteIds.length; i++) {
							noteIdsAsList.add(noteIds[i]);
						}
						getNoteIds().addAll(noteIdsAsList);
					}
				}
			}
			
			return this;
		}
		
		public AllCollapsedEntries expand(Set<Integer> noteIds) {
			if (getMode() == ExpandMode.AllCollapsed) {
				getNoteIds().addAll(noteIds);
			}
			
			return this;
		}

		public AllCollapsedEntries expand(DQLTerm dql) {
			if (getMode() == ExpandMode.AllCollapsed) {
				getDQLQueries().add(dql);
			}
			
			return this;
		}

		public AllCollapsedEntries expand(String ftQuery) {
			if (getMode() == ExpandMode.AllCollapsed) {
				getFTQueries().add(ftQuery);
			}
			
			return this;
		}

	}

	public abstract static class ExpandedEntries {
		private ExpandMode m_mode;
		private Set<Integer> m_noteIds;
		private List<DQLTerm> m_dqlQueries;
		private List<String> m_ftQueries;
		
		private ExpandedEntries(ExpandMode mode) {
			m_mode = mode;
			m_noteIds = new HashSet<>();
			m_dqlQueries = new ArrayList<>();
			m_ftQueries = new ArrayList<>();
		}
		
		public ExpandMode getMode() {
			return m_mode;
		}
		
		public Set<Integer> getNoteIds() {
			return m_noteIds;
		}
		
		public List<DQLTerm> getDQLQueries() {
			return m_dqlQueries;
		}

		public List<String> getFTQueries() {
			return m_ftQueries;
		}
		
		public static AllExpandedEntries expandAll() {
			return new AllExpandedEntries();
		}
		
		public static AllCollapsedEntries collapseAll() {
			return new AllCollapsedEntries();
		}
		
	}
	
	public enum SelectMode { AllSelected, AllDeselected }
	
	public static class AllSelectedEntries extends SelectedEntries {
		
		public AllSelectedEntries() {
			super(SelectMode.AllSelected);
		}
		
		public AllSelectedEntries deselect(int... noteIds) {
			if (getMode() == SelectMode.AllSelected) {
				if (noteIds!=null) {
					if (noteIds.length==1) {
						getNoteIds().add(noteIds[0]);
					}
					else {
						Collection<Integer> noteIdsAsList = new ArrayList<>(noteIds.length);
						for (int i=0; i<noteIds.length; i++) {
							noteIdsAsList.add(noteIds[i]);
						}
						getNoteIds().addAll(noteIdsAsList);
					}
				}
			}
			
			return this;
		}

		public AllSelectedEntries deselect(Set<Integer> noteIds) {
			if (getMode() == SelectMode.AllSelected) {
				getNoteIds().addAll(noteIds);
			}
			
			return this;
		}
		
		public AllSelectedEntries deselect(DQLTerm dql) {
			if (getMode() == SelectMode.AllSelected) {
				getDQLQueries().add(dql);
			}
			
			return this;
		}

		public AllSelectedEntries deselect(String ftQuery) {
			if (getMode() == SelectMode.AllSelected) {
				getFTQueries().add(ftQuery);
			}
			
			return this;
		}

		/**
		 * Deselects collection entries via key lookup
		 * 
		 * @param key lookup key
		 * @param exact true for exact match, false for prefix search
		 * @return this search query
		 */
		public AllSelectedEntries deselectByKey(String key, boolean exact) {
			getLookupKeysSingleCol().add(new SingleColumnLookupKey(key, exact));

			return this;
		}
		
		/**
		 * Deselects collection entries via key lookup
		 * 
		 * @param key lookup key
		 * @param exact true for exact match, false for prefix search
		 * @return this search query
		 */
		public AllSelectedEntries deselectByKey(List<Object> key, boolean exact) {
			getLookupKeysMultiCol().add(new MultiColumnLookupKey(key, exact));

			return this;
		}
	}

	public static class AllDeselectedEntries extends SelectedEntries {
		
		public AllDeselectedEntries() {
			super(SelectMode.AllDeselected);
		}
		
		public AllDeselectedEntries select(int... noteIds) {
			if (getMode() == SelectMode.AllDeselected) {
				if (noteIds!=null) {
					if (noteIds.length==1) {
						getNoteIds().add(noteIds[0]);
					}
					else {
						Collection<Integer> noteIdsAsList = new ArrayList<>(noteIds.length);
						for (int i=0; i<noteIds.length; i++) {
							noteIdsAsList.add(noteIds[i]);
						}
						getNoteIds().addAll(noteIdsAsList);
					}
				}
			}
			
			return this;
		}
		
		public AllDeselectedEntries select(Set<Integer> noteIds) {
			if (getMode() == SelectMode.AllDeselected) {
				getNoteIds().addAll(noteIds);
			}
			
			return this;
		}
		
		public AllDeselectedEntries select(DQLTerm dql) {
			if (getMode() == SelectMode.AllDeselected) {
				getDQLQueries().add(dql);
			}
			
			return this;
		}

		public AllDeselectedEntries select(String ftQuery) {
			if (getMode() == SelectMode.AllDeselected) {
				getFTQueries().add(ftQuery);
			}
			
			return this;
		}

		/**
		 * Selects collection entries via key lookup
		 * 
		 * @param key lookup key
		 * @param exact true for exact match, false for prefix search
		 * @return this search query
		 */
		public AllDeselectedEntries selectByKey(String key, boolean exact) {
			getLookupKeysSingleCol().add(new SingleColumnLookupKey(key, exact));

			return this;
		}
		
		/**
		 * Selects collection entries via key lookup
		 * 
		 * @param key lookup key
		 * @param exact true for exact match, false for prefix search
		 * @return this search query
		 */
		public AllDeselectedEntries selectByKey(List<Object> key, boolean exact) {
			getLookupKeysMultiCol().add(new MultiColumnLookupKey(key, exact));

			return this;
		}
	}

	public static class SingleColumnLookupKey {
		private String m_key;
		private boolean m_exact;
		
		public SingleColumnLookupKey(String key, boolean exact) {
			m_key = key;
			m_exact = exact;
		}
		
		public String getKey() {
			return m_key;
		}
		
		public boolean isExact() {
			return m_exact;
		}
	}
	
	public static class MultiColumnLookupKey {
		private List<Object> m_key;
		private boolean m_exact;
		
		public MultiColumnLookupKey(List<Object> key, boolean exact) {
			m_key = key;
			m_exact = exact;
		}
		
		public List<Object> getKey() {
			return m_key;
		}
		
		public boolean isExact() {
			return m_exact;
		}
	}
	
	public abstract static class SelectedEntries {
		private SelectMode m_mode;
		private Set<Integer> m_noteIds;
		private List<DQLTerm> m_dqlQueries;
		private List<String> m_ftQueries;
		private List<SingleColumnLookupKey> m_lookupKeysSingleCol;
		private List<MultiColumnLookupKey> m_lookupKeysMultiCol;
		
		private SelectedEntries(SelectMode mode) {
			m_mode = mode;
			m_noteIds = new HashSet<>();
			m_dqlQueries = new ArrayList<>();
			m_ftQueries = new ArrayList<>();
			m_lookupKeysSingleCol = new ArrayList<>();
			m_lookupKeysMultiCol = new ArrayList<>();
		}
		
		public SelectMode getMode() {
			return m_mode;
		}
		
		public Set<Integer> getNoteIds() {
			return m_noteIds;
		}
		
		public List<DQLTerm> getDQLQueries() {
			return m_dqlQueries;
		}
		
		public List<String> getFTQueries() {
			return m_ftQueries;
		}

		public List<SingleColumnLookupKey> getLookupKeysSingleCol() {
			return m_lookupKeysSingleCol;
		}
		
		public List<MultiColumnLookupKey> getLookupKeysMultiCol() {
			return m_lookupKeysMultiCol;
		}
		
		public static AllSelectedEntries selectAll() {
			return new AllSelectedEntries();
		}
		
		public static AllDeselectedEntries deselectAll() {
			return new AllDeselectedEntries();
		}

	}
}
