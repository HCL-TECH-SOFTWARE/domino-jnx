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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import com.hcl.domino.data.structures.CollectionData;
import com.hcl.domino.misc.DominoClientDescendant;
import com.hcl.domino.misc.Loop;

/**
 * Term borrowed from MongoDB. Covers Domino views/folders
 * Collection Type (static = view)
 * Contains CollectionEntry, Hibernate compliant
 *
 * @author t.b.d
 */
public interface DominoCollection extends IAdaptable, DominoClientDescendant {
  /** Available column sort directions */
  public enum Direction {
    Ascending, Descending
  }

  void forEachDocument(int skip, int count, BiConsumer<Document, Loop> consumer);

  List<String> getAliases();

  /**
   * Returns an ordered set of note ids for collection entries that the current
   * user is
   * allowed to see.
   * <p>
   * Implementations are likely, but not guaranteed, to return a
   * {@link LinkedHashSet}.
   * </p>
   *
   * @param withDocuments  true to return document note ids
   * @param withCategories true to return category note ids
   * @return sorted set of note ids
   */
  Set<Integer> getAllIds(boolean withDocuments, boolean withCategories);

  /**
   * Very fast way to get all note ids in a view. For <code>checkRights=false</code>
   * and "show response documents in hierarchy" turned off, it is very likely that the
   * method produces the result in no time, because it just copies an already existing internal
   * IDTable.
   * 
   * @param checkRights true to check access rights
   * @return IDTable with note ids
   */
  IDTable getAllIdsAsIDTable(boolean checkRights);

  /**
   * Returns an ordered set of note ids for collection entries that match the
   * specified lookup key (supporting multi column lookups)
   * <p>
   * Implementations are likely, but not guaranteed, to return a
   * {@link LinkedHashSet}.
   * </p>
   *
   * @param findFlags flags to configure the lookup operation
   * @param key       lookup key (list of String, Number, DominoDateRange)
   * @return sorted set of note ids
   */
  Set<Integer> getAllIdsByKey(Set<Find> findFlags, Collection<Object> key);

  /**
   * Returns an ordered set of note ids for collection entries that match the
   * specified lookup key
   * <p>
   * Implementations are likely, but not guaranteed, to return a
   * {@link LinkedHashSet}.
   * </p>
   *
   * @param findFlags flags to configure the lookup operation
   * @param key       lookup key (String, Number, DominoDateRange)
   * @return sorted set of note ids
   */
  Set<Integer> getAllIdsByKey(Set<Find> findFlags, Object key);
  
  /**
   * Retrieve detailed information about the collection itself, such
   * as the number of documents in the collection and the total size
   * of the document entries in the collection.
   * 
   * @return a {@link CollectionData} instance representing information
   *         about the collection
   * @since 1.44.0
   */
  CollectionData getCollectionData();

  List<CollectionColumn> getColumns();

  int getDocumentCount();

  /**
   * Returns the {@link DominoDateTime} when this view was last accessed
   *
   * @return last access date/time
   */
  DominoDateTime getLastAccessedTime();

  String getName();

  /**
   * Returns the {@link DominoDateTime} when the view index will be discarded
   *
   * @return discard date/time
   */
  DominoDateTime getNextDiscardTime();

  int getNoteId();

  Database getParentDatabase();

  /**
   * Returns the first position matching the provided key based on
   * the find flags.
   *
   * @param findFlags flags to configure the lookup operation
   * @param key       lookup key (list of String, Number, DominoDateRange)
   * @return optional "."-delimited position
   * @since 1.45.0
   */
  Optional<String> getPositionByKey(Set<Find> findFlags, Collection<Object> key);

  /**
   * Returns the first position matching the provided key based on
   * the find flags.
   *
   * @param findFlags flags to configure the lookup operation
   * @param key       lookup key (String, Number, DominoDateRange)
   * @return optional "."-delimited position
   * @since 1.45.0
   */
  Optional<String> getPositionByKey(Set<Find> findFlags, Object key);

  String getSelectionFormula();

  int getTopLevelEntries();

  String getUNID();

  boolean isFolder();

  CollectionSearchQuery query();

  void refresh();

  void resetViewSortingToDefault();

  void resortView(String progColumnName, Direction direction);
}
