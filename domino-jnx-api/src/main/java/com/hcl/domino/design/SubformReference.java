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
package com.hcl.domino.design;

/**
 * Represents a reference to a subform, either by name or by formula
 *
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public class SubformReference {
  public enum Type {
    EXPLICIT, FORMULA
  }

  private final Type type;
  private final String value;

  public SubformReference(final Type type, final String value) {
    this.type = type;
    this.value = value;
  }

  /**
   * @return the type of the subform reference
   */
  public Type getType() {
    return this.type;
  }

  /**
   * Retrieves the name or formula for the referenced subform, based on the value
   * of {@link #getType()}.
   *
   * @return the name or formula for the referenced subform
   */
  public String getValue() {
    return this.value;
  }
}
