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

import java.util.Map;
import java.util.Optional;

/**
 * Individual entry in a DominoCollection. Can have a type (data, category)
 *
 * @author t.b.d
 */
public interface CollectionEntry extends TypedAccess, IndexedTypedAccess, Map<String, Object>, IAdaptable {

  public enum SpecialValue {
    /**
     * Special value to read collection index position of collection entry
     */
    INDEXPOSITION("@docnumber"), //$NON-NLS-1$
    /**
     * Special value to read child count of collection entry
     */
    CHILDCOUNT("@docchildren"), //$NON-NLS-1$
    /**
     * Special value to read sibling count of collection entry
     */
    SIBLINGCOUNT("@docsiblings"), //$NON-NLS-1$
    /**
     * Special value to read descendant count of collection entry
     */
    DESCENDANTCOUNT("@docdescendants"), //$NON-NLS-1$
    /**
     * Special value to read sequence number of collection entry
     */
    SEQUENCENUMBER("@sequencenumber"), //$NON-NLS-1$
    /**
     * Special value to read sequence number of collection entry
     */
    SEQUENCETIME("@sequencetime"), //$NON-NLS-1$
    /**
     * Special value to check if the collection entry is unread
     */
    UNREAD("@unread"), //$NON-NLS-1$
    /**
     * Special value to check if the collection entry or any descendant is unread
     */
    ANYUNREAD("@anyunread"); //$NON-NLS-1$

    private final String m_value;

    SpecialValue(final String value) {
      this.m_value = value;
    }

    public String getValue() {
      return this.m_value;
    }
  }

  /**
   * Retrieves the document class for the entry, if read.
   *
   * @return a {@link Optional} describing this {@link DocumentClass} for the
   *         entry, or
   *         an empty one if that is not applicable
   */
  Optional<DocumentClass> getDocumentClass();

  /**
   * Retrieves the note ID for the entry. This may be the ID of the document for a
   * document row, or a special ID for a category row.
   *
   * @return the note ID value for the entry
   */
  int getNoteID();

  /**
   * Read meta data of the collection entry
   *
   * @param <T>          type of return value
   * @param value        special value to read
   * @param clazz        class of return value
   * @param defaultValue default value if not available
   * @return special value
   */
  <T> T getSpecialValue(SpecialValue value, Class<T> clazz, T defaultValue);

  /**
   * Retrieves the UNID associated with the entry, if it is a document.
   *
   * @return the UNID of the entry's document, or an all-zero string if is not a
   *         document
   */
  String getUNID();

  /**
   * Determines whether the collection entry represents a category, as opposed to
   * a document or total row.
   *
   * @return {@code true} if the entry represents a category; {@code false}
   *         otherwise
   */
  boolean isCategory();

  /**
   * Determines whether the collection entry represents a document, as opposed to
   * a category or total row.
   *
   * @return {@code true} if the entry represents a document; {@code false}
   *         otherwise
   */
  boolean isDocument();

  /**
   * Open the database document for this collection entry
   *
   * @return an {@link Optional} describing the document associated with this
   *         entry, or
   *         an empty one if the entry does not represent a document
   */
  Optional<Document> openDocument();
  
  /**
   * Retrieves the indent level of the entry. Generally speaking, this is equivalent
   * to the number of "slots" in the entry's position.
   * 
   * @return the 1-based index of the entry's indentation level in the collection
   * @since 1.0.46
   */
  int getIndentLevel();
}
