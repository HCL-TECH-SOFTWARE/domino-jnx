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
package com.hcl.domino.design.simplesearch;

import java.util.Optional;
import java.util.OptionalInt;

import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.data.DominoDateTime;

/**
 * Represents a search for documents by date fields, including the "meta" fields
 * of creation and modification date.
 * 
 * <p>This is a specialization of {@link ByFieldTerm} that applies to date
 * fields. In such cases, {@link ByFieldTerm#getTextValue()} will be set
 * to a text version of the date, but should be ignored in preference to the
 * methods found in this type.</p>
 * 
 * @author Jesse Gallagher
 * @since 1.0.38
 */
public interface ByDateFieldTerm extends ByFieldTerm {
  enum DateType {
    /**
     * Indicates that the term applies to an named item, determined by
     * {@link #getFieldName()}.
     */
    FIELD,
    /**
     * Indicates that the term applies to the creation date of the document,
     * which is represented by the pseudo-item name {@code "_CreationDate"}.
     */
    CREATED,
    /**
     * Indicates that the term applies to the modification date of the document,
     * which is represented by the pseudo-item name {@code "_RevisionDate"}.
     */
    MODIFIED
  }
  enum DateRule {
    ON, AFTER, BEFORE, NOT_ON, IN_LAST, IN_NEXT,
    OLDER_THAN, AFTER_NEXT, BETWEEN, NOT_BETWEEN
  }
  
  /**
   * Determines the date field this term operates on.
   * 
   * @return a {@link DateType} instance
   */
  DateType getDateType();
  
  /**
   * Determines the type of date query this term represents.
   * 
   * @return a {@link DateRule} instance
   */
  DateRule getDateRule();
  
  /**
   * Retrieves the date component of this term, if applicable.
   * 
   * <p>This value applies when {@link #getDateRule()} is one of:</p>
   * 
   * <ul>
   *   <li>{@link DateRule#ON ON}</li>
   *   <li>{@link DateRule#AFTER AFTER}</li>
   *   <li>{@link DateRule#BEFORE BEFORE}</li>
   *   <li>{@link DateRule#NOT_ON NOT_ON}</li>
   * </ul>
   * 
   * @return an {@link Optional} describing the date component for the term,
   *         or an empty one if that is not applicable
   */
  Optional<DominoDateTime> getDate();
  
  /**
   * Retrieves the day count for the term, if applicable.
   * 
   * <p>This value applies when {@link #getDateRule()} is one of:</p>
   * 
   * <ul>
   *   <li>{@link DateRule#IN_LAST IN_LAST}</li>
   *   <li>{@link DateRule#IN_NEXT IN_NEXT}</li>
   *   <li>{@link DateRule#OLDER_THAN OLDER_THAN}</li>
   *   <li>{@link DateRule#AFTER_NEXT AFTER_NEXT}</li>
   * </ul>
   * 
   * @return an {@link OptionalInt} describing the day count for the term,
   *         or an empty one if that is not applicable
   */
  OptionalInt getDayCount();
  
  /**
   * Retrieves the date range component of this term, if applicable.
   * 
   * <p>This value applies when {@link #getDateRule()} is one of:</p>
   * 
   * <ul>
   *   <li>{@link DateRule#BETWEEN BETWEEN}</li>
   *   <li>{@link DateRule#NOT_BETWEEN NOT_BETWEEN}</li>
   * </ul>
   * 
   * @return an {@link Optional} describing the date range component for
   *         the term, or an empty one if that is not applicable
   */
  Optional<DominoDateRange> getDateRange();
}
