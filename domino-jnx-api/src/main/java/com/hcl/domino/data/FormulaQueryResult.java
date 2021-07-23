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

import java.util.List;
import java.util.Optional;

import com.hcl.domino.data.Database.SearchMatch;

public interface FormulaQueryResult extends DbQueryResult<FormulaQueryResult> {

  /**
   * Returns detail infos about documents that got deleted
   * since the specified <code>since</code> date/time.
   * Can be used to update external
   * indexes, e.g. remove a document from an external index
   * when it got deleted in Domino.
   *
   * @return deletions
   */
  List<SearchMatch> getDeletions();

  /**
   * Returns detail infos about search matches like
   * modified dates, document classes and the UNID.
   *
   * @return search matches
   */
  List<SearchMatch> getMatches();

  /**
   * Returns detail infos about documents not matching
   * the search formula, if a <code>since</code> date/time
   * has been specified. Can be used to update external
   * indexes, e.g. remove a document from an external index
   * when it has matched the search formula before, but the
   * last doc change made it a no-match.
   *
   * @return search non-matches
   */
  List<SearchMatch> getNonMatches();

  @Override
  Optional<IDTable> getNoteIds();

  /**
   * Same as {@link IDTable#getDateTime()} of the IDTable returned by
   * {@link #getNoteIds()}. Can be used for incremental database searches
   * if set as <code>since</code> value in the next search.
   *
   * @return date/time of search
   */
  DominoDateTime getUntil();

}
