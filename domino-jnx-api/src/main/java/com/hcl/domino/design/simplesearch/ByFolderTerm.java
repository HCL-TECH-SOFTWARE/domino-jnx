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

/**
 * Represents a search for documents that are within a named form or view.
 * 
 * @author Jesse Gallagher
 * @since 1.0.38
 */
public interface ByFolderTerm extends SimpleSearchTerm {
  /**
   * Determines whether the view or folder referenced by this term
   * is expected to be private.
   * 
   * @return {@code true} if the referenced collection is expected
   *         to be private; {@code false} if it is shared 
   */
  boolean isPrivate();
  
  /**
   * Retrieves the folder or view name queried by this term.
   * 
   * @return the name of folder or view
   */
  String getFolderName();
}
