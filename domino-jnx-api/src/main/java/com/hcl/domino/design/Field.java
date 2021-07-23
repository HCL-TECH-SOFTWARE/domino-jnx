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
package com.hcl.domino.design;

public interface Field extends DesignElement {
  public enum Kind {
    COMPUTED, COMPUTEDFORDISPLAY, COMPUTEDWHENCOMPOSED, EDITABLE
  }

  public enum Type {
    TEXT, DATETIME, NUMBER, DIALOGLIST, CHECKBOX, RADIOBUTTON, COMBOBOX, RICHTEXT, AUTHORS, NAMES, READERS, PASSWORD, FORMULA,
    TIMEZONE
  }

  String getDefaultValueFormula();

  Type getFieldType();

  Kind getKind();

  String getName();

  boolean isAllowMultiValues();

  boolean isProtected();

  boolean isSeal();

  boolean isSign();

  Field setAllowMultiValues(boolean allowMultiValues);

  Field setDefaultValueFormula(String defaultValueFormula);

  Field setFieldType(Type fieldType);

  Field setKind(Kind kind);

  Field setName(String name);

  Field setProtected(boolean _protected);

  Field setSeal(boolean seal);

  Field setSign(boolean sign);
}