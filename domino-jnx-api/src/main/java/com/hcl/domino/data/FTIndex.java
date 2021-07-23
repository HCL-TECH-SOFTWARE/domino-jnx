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

import java.util.Set;

import com.hcl.domino.misc.INumberEnum;

/**
 * These values define the options used when creating a full text
 * index for a database.<br>
 * These options may be combined as a {@link Set}.<br>
 * However, {@link #AUTOOPTIONS} will ignore all other indexing
 * options and therefore should not be OR-ed with any of the other
 * indexing options.
 */
public enum FTIndex implements INumberEnum<Short> {
  /**
   * Forces re-indexing the database from scratch. This option is useful if the
   * indexing
   * options for the database are being changed.
   */
  REINDEX(0x0002),
  /** Build case sensitive index */
  CASE_SENSITIVE(0x0004),
  /**
   * Build an index that includes word variants (stems). This allows searching to
   * include word variants.
   * A full text search index built with the Notes user interface, is
   * automatically stemmed.
   */
  STEM_INDEX(0x0008),
  /**
   * Build index with word, sentence, and paragraph index break option which
   * allows a search for
   * words within a sentence or paragraph.
   */
  INDEX_SENTENCE_PARAGRAPH_BREAKS(0x0010),
  /** Optimize index (e.g. for CDROM) (Not used) */
  OPTIMIZE(0x0020),
  /** Index Attachments */
  INDEX_ATTACHED_FILES(0x0040),
  /** Index Encrypted Fields */
  ENCRYPTED_FIELDS(0x0080),
  /**
   * Use the index options that are in database. If the database has never been
   * indexed, use the
   * default indexing options. Database indexing options include the Stop Word
   * File name, case
   * sensitivity, the PSW option, reindexing, and the stem index. Note that the
   * stem index will
   * be set if {@link #AUTOOPTIONS} is used.
   */
  AUTOOPTIONS(0x0100),
  /** Index summary data only */
  SUMMARY_ONLY(0x0200),
  /** Index all attachments including BINARY formats */
  USE_CONVERSION_FILTERS_FOR_FILES(0x1000);

  private Short m_val;

  FTIndex(final int val) {
    this.m_val = (short) (val & 0xffff);
  }

  @Override
  public long getLongValue() {
    return this.m_val;
  }

  @Override
  public Short getValue() {
    return this.m_val;
  }

}
