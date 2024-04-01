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
 * Represents a File Resource design element.
 *
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public interface FileResource extends NamedFileElement<FileResource> {
  /**
   * Determines whether this resource shows up in the "WebContent" virtual
   * directory
   * 
   * @return {@code true} if this is a WebContent file; {@code false} otherwise
   * @since 1.39.0
   */
  boolean isWebContentFile();
  
  /**
   * Sets whether this resource should show up in the "WebContent" virtual
   * directory
   * 
   * @param webContentFile {@code true} if this should be marked as a
   *        WebContent file; {@code false} otherwise
   * @since 1.39.0
   */
  void setWebContentFile(boolean webContentFile);
}
