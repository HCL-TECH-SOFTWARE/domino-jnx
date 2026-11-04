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

import com.hcl.domino.misc.NotesConstants;

/**
 * Utility interface to represent a Notes API STATUS, which is a
 * 16-bit unsigned integer, typically represented as an {@code short}.
 * 
 * @since 1.57.0
 */
public interface STATUS {
  /**
   * Retrieves the internal {@code short} value for this STATUS.
   * 
   * @return a {@code short} representing the value
   */
  short getValue();
  
  /**
   * Determines whether the status value can be safely considered an error.
   * 
   * @return {@code true} if the status is an error
   */
  default boolean isError() {
    return getMaskedValue() != 0;
  }
  
  /**
   * Retrieves the status value with flag bits removed.
   * 
   * @return the underlying status code without flag bits
   */
  default short getMaskedValue() {
    return (short)(getValue() & NotesConstants.ERR_MASK);
  }
  
  /**
   * Determines whether the status represents a remote-based error.
   * 
   * @return {@code true} if the status is a remote error
   */
  default boolean isRemoteError() {
    return (getValue() & NotesConstants.STS_REMOTE) == NotesConstants.STS_REMOTE;
  }
}
