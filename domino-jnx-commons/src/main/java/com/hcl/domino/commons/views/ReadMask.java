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
package com.hcl.domino.commons.views;

import java.util.Set;

/**
 * These flags control what information is returned by NIFReadEntries
 * for each note that is found. All of the information requested is returned in
 * the
 * buffer that NIFReadEntries creates.
 *
 * @author Karsten Lehmann
 */
public enum ReadMask {
  /** NOTEID of note */
  NOTEID(0x00000001),
  /** UNID of note */
  NOTEUNID(0x00000002),
  /** Note class of note */
  NOTECLASS(0x00000004),
  /** Number of siblings in view or folder */
  INDEXSIBLINGS(0x00000008),
  /**
   * Number of immediate children in view or folder. Subcategories are included in
   * the count
   */
  INDEXCHILDREN(0x00000010),
  /**
   * Number of descendents in view or folder. Subcategories are not included in
   * the count
   */
  INDEXDESCENDANTS(0x00000020),
  /** TRUE if unread (or any descendents unread), FALSE otherwise */
  INDEXANYUNREAD(0x00000040),
  /**
   * Number of levels that this entry should be indented in a formatted view or
   * folder
   */
  INDENTLEVELS(0x00000080),
  /**
   * Relevancy score of an entry. Occupies one WORD in the buffer.
   * FTSearch must be called prior to NIFReadEntries. The FT_SEARCH_SET_COLL
   * search option or'd with the FT_SEARCH_SCORES search option must be specified
   * in the call to FTSearch.
   */
  SCORE(0x00000200),
  /** TRUE if this entry is unread, FALSE otherwise */
  INDEXUNREAD(0x00000400),
  /** Collection statistics (as a {@link NotesCollectionStats} object) */
  COLLECTIONSTATS(0x00000100),
  /**
   * Return SIBLINGS, CHILDREN, DESCENDANTS, COLLECTIONSTATS, and
   * COLLECTIONPOSITION in DWORDs
   */
  RETURN_DWORD(0x00001000),
  /** Return the position of an entry in the collection */
  INDEXPOSITION(0x00004000),
  /**
   * IndexPos.Tumbler[0] is a NOTEID for initial position
   * (looks like this only works with document note ids, categories return err
   * 1028 entry not found)
   */
  INIT_POS_NOTEID(0x00020000),
  /** Return the column values of the entry in the collection */
  SUMMARYVALUES(0x00002000),
  /** Return the summary buffer data for collection entries */
  SUMMARY(0x00008000),
  /** Permuted summary buffer with item names */
  SUMMARY_PERMUTED(0x00040000),
  /** Don't return subtotals */
  NO_SUBTOTALS(0x00080000),
  /**
   * Do DbColumn logic - if categories have non-empty values for
   * single-column reading mode, just read the categories
   */
  CATS_ONLY_FOR_COLUMN(0x00200000),
  /** DWORD/WORD of # direct children of entry - not done for categories */
  INDEXCHILDREN_NOCATS(0x00400000),
  /** DWORD/WORD of # descendants below entry - not done for categories */
  INDEXDESCENDANTS_NOCATS(0x00800000),
  /**
   * Return the readers list as field "$C1$" in the summary buffer;
   * requires {@link ReadMask#SUMMARY} to be set as well.
   * Value can be read via {@code JNACollectionEntry#getReadersList()}
   */
  RETURN_READERSLIST(0x01000000),
  /**
   * Return only entries which hNames would disallow (requires full access set)
   */
  PRIVATE_ONLY(0x02000000),
  /**
   * If ColumnNumber specifies a valid value, return all column values up to and
   * including that column rather than just that column's values
   */
  ALL_TO_COLUMN(0x04000000),
  /** Exclude all columns that have been programmatically generated ( eg $1 ) */
  EXCLUDE_LEADING_PROGRAMMATIC_COLUMNS(0x08000000),
  /** Exclude internal entries - readers list field, $REF, $CONFLICT */
  NO_INTERNAL_ENTRIES(0x10000000),
  /** Compute subtotals */
  COMPUTE_SUBTOTALS(0x20000000);

  public static int toBitMask(final Set<ReadMask> readMaskSet) {
    int result = 0;
    if (readMaskSet != null) {
      for (final ReadMask currNav : ReadMask.values()) {
        if (readMaskSet.contains(currNav)) {
          result = result | currNav.getValue();
        }
      }
    }
    return result;
  }

  private int m_val;

  ReadMask(final int val) {
    this.m_val = val;
  }

  public int getValue() {
    return this.m_val;
  }

}
