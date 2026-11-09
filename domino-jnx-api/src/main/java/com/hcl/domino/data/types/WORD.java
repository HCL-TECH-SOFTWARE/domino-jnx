/*
 * ==========================================================================
 * Copyright (C) 2019-2026 HCL America, Inc. ( http://www.hcl.com/ )
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
package com.hcl.domino.data.types;

/**
 * Utility interface to represent a Notes API WORD, which is a
 * 16-bit unsigned integer, typically represented as a {@code char}.
 * 
 * @since 1.57.0
 */
public interface WORD {
  /**
   * Retrieves the internal {@code char} value for this STATUS.
   * 
   * @return a {@code char} representing the value
   */
  char getValue();
  
  default boolean isZero() {
    return getValue() == 0;
  }
}
