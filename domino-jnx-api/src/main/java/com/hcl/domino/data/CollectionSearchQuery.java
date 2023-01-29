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

  public static class AllCollapsedEntries extends ExpandedEntries {

    public AllCollapsedEntries() {
      super(ExpandMode.AllCollapsed);
    }

    public AllCollapsedEntries expand(final DQLTerm dql) {
      if (this.getMode() == ExpandMode.AllCollapsed) {
        this.getDQLQueries().add(dql);
      }

      return this;
    }

    public AllCollapsedEntries expand(final int... noteIds) {
      if (this.getMode() == ExpandMode.AllCollapsed) {
        if (noteIds != null) {
          if (noteIds.length == 1) {
            this.getNoteIds().add(noteIds[0]);
          } else {
            final Collection<Integer> noteIdsAsList = new ArrayList<>(noteIds.length);
            for (final int noteId : noteIds) {
              noteIdsAsList.add(noteId);
            }
            this.getNoteIds().addAll(noteIdsAsList);
          }
        }
      }

      return this;
    }

    public AllCollapsedEntries expand(final Set<Integer> noteIds) {
      if (this.getMode() == ExpandMode.AllCollapsed) {
        this.getNoteIds().addAll(noteIds);
      }

      return this;
    }

    public AllCollapsedEntries expand(final String ftQuery) {
      if (this.getMode() == ExpandMode.AllCollapsed) {
        this.getFTQueries().add(ftQuery);
      }

      return this;
    }

    /**
     * Expands collection entries via key lookup
     *
     * @param key   lookup key
     * @param exact true for exact match, false for prefix search
     * @return this instance
     */
    public AllCollapsedEntries expandByKey(final List<Object> key, final boolean exact) {
      this.getLookupKeysMultiCol().add(new MultiColumnLookupKey(key, exact));

      return this;
    }

    /**
     * Expands collection entries via key lookup
     *
     * @param key   lookup key
     * @param exact true for exact match, false for prefix search
     * @return this instance
     */
    public AllCollapsedEntries expandByKey(final String key, final boolean exact) {
      this.getLookupKeysSingleCol().add(new SingleColumnLookupKey(key, exact));

      return this;
    }

    /**
     * Expands a category by name
     * 
     * @param category category name
     * @return this instance
     */
    public AllCollapsedEntries expandCategory(String category) {
      this.getCategories().add(category);
      
      return this;
    }
  }

  public static class AllDeselectedEntries extends SelectedEntries {

    public AllDeselectedEntries() {
      super(SelectMode.AllDeselected);
    }

    public AllDeselectedEntries select(final DQLTerm dql) {
      if (this.getMode() == SelectMode.AllDeselected) {
        this.getDQLQueries().add(dql);
      }

      return this;
    }

    public AllDeselectedEntries select(final int... noteIds) {
      if (this.getMode() == SelectMode.AllDeselected) {
        if (noteIds != null) {
          if (noteIds.length == 1) {
            this.getNoteIds().add(noteIds[0]);
          } else {
            final Collection<Integer> noteIdsAsList = new ArrayList<>(noteIds.length);
            for (final int noteId : noteIds) {
              noteIdsAsList.add(noteId);
            }
            this.getNoteIds().addAll(noteIdsAsList);
          }
        }
      }

      return this;
    }

    public AllDeselectedEntries select(final Set<Integer> noteIds) {
      if (this.getMode() == SelectMode.AllDeselected) {
        this.getNoteIds().addAll(noteIds);
      }

      return this;
    }

    public AllDeselectedEntries selectWithFTQuery(final String ftQuery) {
      if (this.getMode() == SelectMode.AllDeselected) {
        this.getFTQueries().add(ftQuery);
      }

      return this;
    }

    /**
     * Selects collection entries via key lookup
     *
     * @param key   lookup key
     * @param exact true for exact match, false for prefix search
     * @return this instance
     */
    public AllDeselectedEntries selectByKey(final List<Object> key, final boolean exact) {
      this.getLookupKeysMultiCol().add(new MultiColumnLookupKey(key, exact));

      return this;
    }

    /**
     * Selects collection entries via key lookup
     *
     * @param key   lookup key
     * @param exact true for exact match, false for prefix search
     * @return this instance
     */
    public AllDeselectedEntries selectByKey(final String key, final boolean exact) {
      this.getLookupKeysSingleCol().add(new SingleColumnLookupKey(key, exact));

      return this;
    }
  }

  public static class AllExpandedEntries extends ExpandedEntries {

    public AllExpandedEntries() {
      super(ExpandMode.AllExpanded);
    }

    public AllExpandedEntries collapse(final int... noteIds) {
      if (this.getMode() == ExpandMode.AllExpanded) {
        if (noteIds != null) {
          if (noteIds.length == 1) {
            this.getNoteIds().add(noteIds[0]);
          } else {
            final Collection<Integer> noteIdsAsList = new ArrayList<>(noteIds.length);
            for (final int noteId : noteIds) {
              noteIdsAsList.add(noteId);
            }
            this.getNoteIds().addAll(noteIdsAsList);
          }
        }
      }

      return this;
    }

    public AllExpandedEntries collapse(final Set<Integer> noteIds) {
      if (this.getMode() == ExpandMode.AllExpanded) {
        this.getNoteIds().addAll(noteIds);
      }

      return this;
    }

    public AllExpandedEntries unselect(final DQLTerm dql) {
      if (this.getMode() == ExpandMode.AllExpanded) {
        this.getDQLQueries().add(dql);
      }

      return this;
    }

    public AllExpandedEntries unselect(final String ftQuery) {
      if (this.getMode() == ExpandMode.AllExpanded) {
        this.getFTQueries().add(ftQuery);
      }

      return this;
    }

  }

  public static class AllSelectedEntries extends SelectedEntries {

    public AllSelectedEntries() {
      super(SelectMode.AllSelected);
    }

    public AllSelectedEntries deselect(final DQLTerm dql) {
      if (this.getMode() == SelectMode.AllSelected) {
        this.getDQLQueries().add(dql);
      }

      return this;
    }

    public AllSelectedEntries deselect(final int... noteIds) {
      if (this.getMode() == SelectMode.AllSelected) {
        if (noteIds != null) {
          if (noteIds.length == 1) {
            this.getNoteIds().add(noteIds[0]);
          } else {
            final Collection<Integer> noteIdsAsList = new ArrayList<>(noteIds.length);
            for (final int noteId : noteIds) {
              noteIdsAsList.add(noteId);
            }
            this.getNoteIds().addAll(noteIdsAsList);
          }
        }
      }

      return this;
    }

    public AllSelectedEntries deselect(final Set<Integer> noteIds) {
      if (this.getMode() == SelectMode.AllSelected) {
        this.getNoteIds().addAll(noteIds);
      }

      return this;
    }

    public AllSelectedEntries deselect(final String ftQuery) {
      if (this.getMode() == SelectMode.AllSelected) {
        this.getFTQueries().add(ftQuery);
      }

      return this;
    }

    /**
     * Deselects collection entries via key lookup
     *
     * @param key   lookup key
     * @param exact true for exact match, false for prefix search
     * @return this search query
     */
    public AllSelectedEntries deselectByKey(final List<Object> key, final boolean exact) {
      this.getLookupKeysMultiCol().add(new MultiColumnLookupKey(key, exact));

      return this;
    }

    /**
     * Deselects collection entries via key lookup
     *
     * @param key   lookup key
     * @param exact true for exact match, false for prefix search
     * @return this search query
     */
    public AllSelectedEntries deselectByKey(final String key, final boolean exact) {
      this.getLookupKeysSingleCol().add(new SingleColumnLookupKey(key, exact));

      return this;
    }
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

  /**
   * Builder interface that takes the collection entries and
   * produces a result.
   *
   * @param <T> type of computation result
   */
  public interface CollectionEntryProcessor<T> {

    /**
     * This method is called when view reading is done. Add code
     * here to post process your lookup result.
     *
     * @param result lookup result
     * @return processed result
     */
    T end(T result);

    /**
     * Method is called for each read collection entry
     *
     * @param result result object to add the entry data
     * @param entry  entry
     * @return action whether to continue or stop reading
     */
    Action entryRead(T result, CollectionEntry entry);

    /**
     * This method is called when we start reading collection entries.<br>
     * <br>
     * Since the view index might change while reading entries, we might
     * need to restart reading from the beginning.
     *
     * @return an object to be passed to {@link #entryRead(Object, CollectionEntry)}
     *         for each read entry
     */
    T start();
  }

  // configure expanded and selected navigation, e.g. next selected, next
  // expanded, next selected expanded

  public abstract static class ExpandedEntries {
    public static AllCollapsedEntries collapseAll() {
      return new AllCollapsedEntries();
    }

    public static AllExpandedEntries expandAll() {
      return new AllExpandedEntries();
    }

    private final ExpandMode m_mode;
    private final Set<Integer> m_noteIds;
    private final List<DQLTerm> m_dqlQueries;
    private final List<String> m_ftQueries;

    private final List<SingleColumnLookupKey> m_lookupKeysSingleCol;
    private final List<MultiColumnLookupKey> m_lookupKeysMultiCol;
    private final Set<String> m_expandedCategories;
    
    private ExpandedEntries(final ExpandMode mode) {
      this.m_mode = mode;
      this.m_noteIds = new HashSet<>();
      this.m_dqlQueries = new ArrayList<>();
      this.m_ftQueries = new ArrayList<>();
      this.m_lookupKeysSingleCol = new ArrayList<>();
      this.m_lookupKeysMultiCol = new ArrayList<>();
      this.m_expandedCategories = new LinkedHashSet<>();
    }

    public List<DQLTerm> getDQLQueries() {
      return this.m_dqlQueries;
    }

    public List<String> getFTQueries() {
      return this.m_ftQueries;
    }

    public List<MultiColumnLookupKey> getLookupKeysMultiCol() {
      return this.m_lookupKeysMultiCol;
    }

    public List<SingleColumnLookupKey> getLookupKeysSingleCol() {
      return this.m_lookupKeysSingleCol;
    }

    public Set<String> getCategories() {
      return this.m_expandedCategories;
    }
    
    public ExpandMode getMode() {
      return this.m_mode;
    }

    public Set<Integer> getNoteIds() {
      return this.m_noteIds;
    }

  }

  public enum ExpandMode {
    AllExpanded, AllCollapsed
  }

  public static class MultiColumnLookupKey {
    private final List<Object> m_key;
    private final boolean m_exact;

    public MultiColumnLookupKey(final List<Object> key, final boolean exact) {
      this.m_key = key;
      this.m_exact = exact;
    }

    public List<Object> getKey() {
      return this.m_key;
    }

    public boolean isExact() {
      return this.m_exact;
    }
  }

  public abstract static class SelectedEntries {
    public static AllDeselectedEntries deselectAll() {
      return new AllDeselectedEntries();
    }

    public static AllSelectedEntries selectAll() {
      return new AllSelectedEntries();
    }

    private final SelectMode m_mode;
    private final Set<Integer> m_noteIds;
    private final List<DQLTerm> m_dqlQueries;
    private final List<String> m_ftQueries;

    private final List<SingleColumnLookupKey> m_lookupKeysSingleCol;
    private final List<MultiColumnLookupKey> m_lookupKeysMultiCol;

    private SelectedEntries(final SelectMode mode) {
      this.m_mode = mode;
      this.m_noteIds = new HashSet<>();
      this.m_dqlQueries = new ArrayList<>();
      this.m_ftQueries = new ArrayList<>();
      this.m_lookupKeysSingleCol = new ArrayList<>();
      this.m_lookupKeysMultiCol = new ArrayList<>();
    }

    public List<DQLTerm> getDQLQueries() {
      return this.m_dqlQueries;
    }

    public List<String> getFTQueries() {
      return this.m_ftQueries;
    }

    public List<MultiColumnLookupKey> getLookupKeysMultiCol() {
      return this.m_lookupKeysMultiCol;
    }

    public List<SingleColumnLookupKey> getLookupKeysSingleCol() {
      return this.m_lookupKeysSingleCol;
    }

    public SelectMode getMode() {
      return this.m_mode;
    }

    public Set<Integer> getNoteIds() {
      return this.m_noteIds;
    }

  }

  public enum SelectMode {
    AllSelected, AllDeselected
  }

  public static class SingleColumnLookupKey {
    private final String m_key;
    private final boolean m_exact;

    public SingleColumnLookupKey(final String key, final boolean exact) {
      this.m_key = key;
      this.m_exact = exact;
    }

    public String getKey() {
      return this.m_key;
    }

    public boolean isExact() {
      return this.m_exact;
    }
  }

  // decide how entries are read

  /**
   * Build a result out of the collection entries
   *
   * @param <T>       result type
   * @param skip      paging offset
   * @param count     paging count
   * @param processor builder code to produce the result
   * @return result
   */
  <T> T build(int skip, int count, CollectionEntryProcessor<T> processor);

  /**
   * Collect all {@link CollectionEntry} objects as list
   *
   * @param skip  paging offset
   * @param count paging count
   * @return list of collection entries
   */
  List<CollectionEntry> collectEntries(int skip, int count);

  /**
   * Collect all {@link CollectionEntry} objects in a {@link Collection}
   *
   * @param skip       the number of entries to skip when collecting
   * @param count      the maximum number of entries to collect
   * @param collection collection to add entries
   */
  void collectEntries(int skip, int count, Collection<CollectionEntry> collection);

  // methods to control reading expanded entries in categorized views

  /**
   * Return the note ids of the search result as an ordered {@link Set}.
   * <p>
   * Implementations are likely, but not guaranteed, to return a
   * {@link LinkedHashSet}.
   * </p>
   *
   * @param skip  paging offset
   * @param count paging count
   * @return set of note ids
   */
  Set<Integer> collectIds(int skip, int count);

  /**
   * Adds all note ids of the search result to a note ID collection, with special
   * support for {@link IDTable}
   *
   * @param skip    paging offset
   * @param count   paging count
   * @param idTable note ID collection
   */
  void collectIds(int skip, int count, Collection<Integer> idTable);

  // decide what to do with the view content

  /**
   * Convenience method that calls {@link #select(SelectedEntries)} with
   * {@link SelectedEntries#deselectAll()} and
   * {@link AllDeselectedEntries#selectByKey(List, boolean)}.
   *
   * @param key   lookup key
   * @param exact true for exact match, false for prefix search
   * @return this search query
   */
  CollectionSearchQuery deselectByKey(List<Object> key, boolean exact);

  /**
   * Convenience method that calls {@link #select(SelectedEntries)} with
   * {@link SelectedEntries#selectAll()} and
   * {@link AllSelectedEntries#deselectByKey(String, boolean)}.
   *
   * @param key   lookup key
   * @param exact true for exact match, false for prefix search
   * @return this search query
   */
  CollectionSearchQuery deselectByKey(String key, boolean exact);

  /**
   * Direction and read mode
   *
   * @param mode mode
   * @return this search query
   */
  CollectionSearchQuery direction(Navigate mode);

  /**
   * Configure which elements are expanded
   *
   * @param expandedEntries use {@link ExpandedEntries#expandAll()} or
   *                        {@link ExpandedEntries#collapseAll()} to start
   *                        building this object
   * @return this search query
   */
  CollectionSearchQuery expand(ExpandedEntries expandedEntries);

  /**
   * Returns the first {@link CollectionEntry}
   *
   * @return an {@code Optional} describing the first entry,
   *         or an empty {@code Optional} if there are no
   *         matched entries
   */
  default Optional<CollectionEntry> firstEntry() {
    final List<CollectionEntry> entries = this.collectEntries(0, 1);
    return Optional.ofNullable(entries.isEmpty() ? null : entries.get(0));
  }

  /**
   * Returns the first {@link CollectionEntry}
   *
   * @return an {@code Optional} describing the first entry's note ID,
   *         or an empty {@code Optional} if there are no
   *         matched entries
   */
  default Optional<Integer> firstId() {
    final Set<Integer> entries = this.collectIds(0, 1);
    return Optional.ofNullable(entries.isEmpty() ? null : entries.iterator().next());
  }

  /**
   * Iterates through all selected entries and performs an action on each.
   * <p>
   * This is equivalent to calling
   * {@link #build(int, int, CollectionEntryProcessor)} with
   * a processor that performs no action on start/end and returns no value.
   * </p>
   *
   * @param processor the processor to execute on each entry
   * @since 1.0.18
   */
  default void forEach(final BasicCollectionEntryProcessor processor) {
    this.build(0, Integer.MAX_VALUE, new CollectionEntryProcessor<Void>() {
      @Override
      public Void end(final Void result) {
        return null;
      }

      @Override
      public Action entryRead(final Void result, final CollectionEntry entry) {
        processor.entryRead(entry);
        return null;
      }

      @Override
      public Void start() {
        return null;
      }
    });
  }

  void forEachDocument(int skip, int count, BiConsumer<Document, Loop> consumer);

  /**
   * Decodes the collection column values, they can be read via
   * {@link CollectionEntry#get(String, Class, Object)} or
   * {@link CollectionEntry#getAsList(String, Class, List)}.
   *
   * @return this search query
   */
  CollectionSearchQuery readColumnValues();

  // which data should be read from the view index or from the document summary
  // data

  /**
   * Reads the document class for each view entry.
   *
   * @return this search query
   * @since 1.0.18
   */
  CollectionSearchQuery readDocumentClass();

  /**
   * Read additional meta data for the collection entries or their document
   *
   * @param values special values
   * @return this search query
   */
  CollectionSearchQuery readSpecialValues(Collection<SpecialValue> values);

  /**
   * Read additional meta data for the collection entries or their document
   *
   * @param values special values
   * @return this search query
   */
  CollectionSearchQuery readSpecialValues(SpecialValue... values);

  /**
   * Reads the UNID of each collection entry.
   *
   * @return this search query
   */
  CollectionSearchQuery readUNID();

  /**
   * Configure which elements are selected
   *
   * @param selectedEntries use {@link SelectedEntries#selectAll()} or
   *                        {@link SelectedEntries#deselectAll()} to start
   *                        building this object
   * @return this search query
   */
  CollectionSearchQuery select(SelectedEntries selectedEntries);

  /**
   * Convenience method that calls {@link #select(SelectedEntries)} with
   * {@link SelectedEntries#selectAll()} and
   * {@link AllSelectedEntries#deselectByKey(List, boolean)}.
   *
   * @param key   lookup key
   * @param exact true for exact match, false for prefix search
   * @return this search query
   */
  CollectionSearchQuery selectByKey(List<Object> key, boolean exact);

  /**
   * Convenience method that calls {@link #select(SelectedEntries)} with
   * {@link SelectedEntries#deselectAll()} and
   * {@link AllDeselectedEntries#selectByKey(String, boolean)}.
   *
   * @param key   lookup key
   * @param exact true for exact match, false for prefix search
   * @return this search query
   */
  CollectionSearchQuery selectByKey(String key, boolean exact);

  /**
   * Slow method to compute the total number of collection entries
   *
   * @return total
   */
  int size();

  /**
   * Restricts search result to a single category
   *
   * @param categoryLevels different levels of category values, supports strings
   *                       and {@link TemporalAccessor} objects
   * @return this search query
   */
  CollectionSearchQuery restrictToCategory(List<Object> categoryLevels);

  /**
   * Restrict search result to a single category
   *
   * @param category category, use "\" for multiple category levels
   * @return this search query
   */
  CollectionSearchQuery restrictToCategory(String category);

  /**
   * Start reading view data at an entry with the specified note id
   *
   * @param noteId note id
   * @return this search query
   */
  CollectionSearchQuery startAtEntryId(int noteId);

  /**
   * Start reading view data at first entry
   *
   * @return this search query
   */
  CollectionSearchQuery startAtFirstEntry();

  /**
   * Start reading at last view row
   *
   * @return this search query
   */
  CollectionSearchQuery startAtLastEntry();

  /**
   * Start reading at a fixed collection position. Please note that this
   * position is not a unique identifier for entries, it may change
   * over time when the view index changes.
   *
   * @param pos position string, e.g. "1.2.1"
   * @return this search query
   */
  CollectionSearchQuery startAtPosition(String pos);
}
