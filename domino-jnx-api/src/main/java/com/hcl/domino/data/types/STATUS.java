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

import java.util.Optional;
import com.hcl.domino.DominoException;

/**
 * Utility interface to represent a Notes API STATUS, which is a
 * 16-bit unsigned integer, typically represented as a {@code short}.
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
  short getMaskedValue();
  
  /**
   * Determines whether the status represents a remote-based error.
   * 
   * @return {@code true} if the status is a remote error
   */
  boolean isRemoteError();
  
  /**
   * Checks whether the status value is an error and throws an appropriate
   * exception if so.
   * 
   * @return this value
   * @throws DominoException if the status represents an error code
   */
  STATUS orElseThrow();
  
  /**
   * Converts the status value to an error string from the Notes runtime.
   * 
   * @return a string describing the error
   */
  String toErrorString();
  
  /**
   * Checks whether the status value is an error other than one of the
   * allowed errors and throws an appropriate exception if so
   * 
   * @param allowedErrors status codes other than 0 that should be treated
   *        as non-errors
   * @return this value
   * @throws DominoException if the status represents an error code other than
   *         the allowed
   */
  STATUS throwIfNot(short... allowedErrors);
  
  /**
   * Converts the status to a corresponding {@link DominoException} type, returning
   * an empty optional if the status is not an error.
   * 
   * @return an {@link Optional} describing a {@link DominoException} or subclass, or
   *         an empty one if this status is not an error
   */
  Optional<DominoException> toDominoException();
}
