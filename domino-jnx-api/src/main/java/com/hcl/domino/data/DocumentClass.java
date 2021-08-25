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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;

/**
 * These bit masks define the types of notes in a database. The bit masks may be
 * or'ed together
 * to specify more than one type of note.
 */
public enum DocumentClass implements INumberEnum<Short> {

  /** old name for document note */
  DATA(NotesConstants.NOTE_CLASS_DATA),

  /** document note */
  DOCUMENT(NotesConstants.NOTE_CLASS_DOCUMENT),

  /** notefile info (help-about) note */
  INFO(NotesConstants.NOTE_CLASS_INFO),

  /** form note */
  FORM(NotesConstants.NOTE_CLASS_FORM),

  /** view note */
  VIEW(NotesConstants.NOTE_CLASS_VIEW),

  /** icon note */
  ICON(NotesConstants.NOTE_CLASS_ICON),

  /** design note collection */
  DESIGNCOLLECTION(NotesConstants.NOTE_CLASS_DESIGN),

  /** acl note */
  ACL(NotesConstants.NOTE_CLASS_ACL),

  /** Notes product help index note */
  HELP_INDEX(NotesConstants.NOTE_CLASS_HELP_INDEX),

  /** designer's help note */
  HELP(NotesConstants.NOTE_CLASS_HELP),

  /** filter note */
  FILTER(NotesConstants.NOTE_CLASS_FILTER),

  /** field note */
  FIELD(NotesConstants.NOTE_CLASS_FIELD),

  /** replication formula */
  REPLFORMULA(NotesConstants.NOTE_CLASS_REPLFORMULA),

  /** Private design note, use $PrivateDesign view to locate/classify */
  PRIVATE(NotesConstants.NOTE_CLASS_PRIVATE),

  /** MODIFIER - default version of each */
  DEFAULT(NotesConstants.NOTE_CLASS_DEFAULT),

  /** marker included in deletion stubs found in a query result */
  NOTIFYDELETION(NotesConstants.NOTE_CLASS_NOTIFYDELETION),

  /** all note types */
  ALL(NotesConstants.NOTE_CLASS_ALL),

  /** all non-data notes */
  ALLNONDATA(NotesConstants.NOTE_CLASS_ALLNONDATA),

  /** no notes */
  NONE(NotesConstants.NOTE_CLASS_NONE),

  /** Define symbol for those note classes that allow only one such in a file */
  SINGLE_INSTANCE(NotesConstants.NOTE_CLASS_SINGLE_INSTANCE);

  private static Map<Short, DocumentClass> classesByValue = new HashMap<>();
  static {
    for (final DocumentClass currClass : DocumentClass.values()) {
      DocumentClass.classesByValue.put(currClass.getValue(), currClass);
    }
  }

  public static boolean isDesignElement(final Set<DocumentClass> docClass) {
    return docClass.contains(DocumentClass.INFO) ||
        docClass.contains(DocumentClass.FORM) ||
        docClass.contains(DocumentClass.VIEW) ||
        docClass.contains(DocumentClass.ICON) ||
        docClass.contains(DocumentClass.DESIGNCOLLECTION) ||
        docClass.contains(DocumentClass.HELP_INDEX) ||
        docClass.contains(DocumentClass.HELP) ||
        docClass.contains(DocumentClass.FILTER) ||
        docClass.contains(DocumentClass.FIELD);
  }

  public static DocumentClass toNoteClass(final int val) {
    return DocumentClass.classesByValue.get((short) val);
  }

  private short m_val;

  DocumentClass(final short val) {
    this.m_val = val;
  }

  @Override
  public long getLongValue() {
    return Short.toUnsignedLong(this.m_val);
  }

  @Override
  public Short getValue() {
    return this.m_val;
  }
}
