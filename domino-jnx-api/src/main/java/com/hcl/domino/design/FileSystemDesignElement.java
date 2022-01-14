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

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents a design element that participates in the virtual filesystem
 * view of the NSF.
 * 
 * @author Jesse Gallagher
 * @since 1.0.39
 */
public interface FileSystemDesignElement extends DesignElement {
  InputStream getFileData();
  
  /**
   * Opens a new output stream to replace the content of the file.
   * 
   * <p>Note: it is not guaranteed that the data written to this stream
   * will be saved to the resource until {@link OutputStream#close()} is
   * called.</p>
   * 
   * @return a new {@link OutputStream} that writes new data to replace
   *         the content of the file
   * @since 1.0.39
   */
  OutputStream newOutputStream();
}
