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
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * Wraps the C API IDTable, a very efficient set of note ids that is sorted in
 * ascending order
 */
public interface IDTable extends IAdaptable, Set<Integer> {
  /**
   * special bitflag that is applied to note ids if a document has been reported
   * as deleted
   */
  long NOTEID_FLAG_DELETED = 0x80000000L;

  /**
   * Creates a copy of this IDTable
   *
   * @return IDTable copy
   */
  Object clone();

  /**
   * Returns a {@link DominoDateTime} set in this {@link IDTable} via
   * a database search (e.g. a cutoff date for incremental searches)
   *
   * @return an {@link Optional} describing the "until" time set in this table, or
   *         an empty one if this has not been set
   */
  Optional<DominoDateTime> getDateTime();

  /**
   * This function creates the intersection of two ID sets.<br>
   * The resulting table contains those IDs that are common to both source tables.
   *
   * @param noteIds other {@link IDTable} or Set of note ids
   * @return intersection
   */
  IDTable intersect(Collection<Integer> noteIds);

  /**
   * Sense of list inverted (reserved for use by caller only)
   *
   * @return true if inverted
   */
  boolean isInverted();

  /**
   * Set by Insert/Delete and can be cleared by caller if desired
   *
   * @return true if modified
   */
  boolean isModified();

  /**
   * Returns a note id iterator in reverse order
   *
   * @return iterator
   */
  Iterator<Integer> reverseIterator();

  /**
   * Changes the datetime value stored in this {@link IDTable}
   *
   * @param dt new datetime or null
   */
  void setDateTime(TemporalAccessor dt);

  /**
   * Sets the inverted flag
   *
   * @param b new value of inverted flag
   */
  void setInverted(boolean b);

  /**
   * Sets the modified flag
   *
   * @param modified new modified flag value
   */
  void setModified(boolean modified);

  /**
   * Copies the contents of the ID table into an array of {@code int}s
   *
   * @return the contents of this ID table as an {@code int[]}
   * @since 1.0.9
   */
  int[] toIntArray();

}
