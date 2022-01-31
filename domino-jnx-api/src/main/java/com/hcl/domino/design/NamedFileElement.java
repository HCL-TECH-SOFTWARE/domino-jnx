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

import java.util.Collection;
import java.util.List;

import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.design.DesignElement.NamedDesignElement;

/**
 * Represents common behavior for file-resource-esque design elements with
 * "$FileData" and "$FileNames" or equivalent items.
 *
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public interface NamedFileElement<T extends NamedFileElement<T>> extends NamedDesignElement, FileSystemDesignElement {
  String getCharsetName();

  DominoDateTime getFileModified();

  List<String> getFileNames();
  
  /**
   * Sets the file names associated with this design element. This item
   * is most commonly a single-value list.
   * 
   * @param fileNames the file names to set
   * @return this element
   * @since 1.1.2
   */
  T setFileNames(Collection<String> fileNames);

  long getFileSize();

  String getMimeType();
  
  /**
   * Sets the MIME type for the file resource, which may be sent to
   * web clients.
   * 
   * @param mimeType the MIME type to set
   * @return this element
   * @since 1.1.2
   */
  T setMimeType(String mimeType);
  
  /**
   * Determines whether the design element is marked as needing a refresh (e.g.
   * it was opened for modification in an external editor).
   * 
   * @return {@code true} if the element is marked as needing refresh;
   *         {@code false} otherwise
   * @since 1.1.2
   */
  boolean isNeedsRefresh();
  
  /**
   * Sets whether the design element is marked as needing a refresh (e.g.
   * it was opened for modification in an external editor).
   * 
   * @param refresh {@code true} if the element is marked as needing refresh;
   *                {@code false} otherwise
   * @return this element
   * @since 1.1.2
   */
  T setNeedsRefresh(boolean refresh);
}
