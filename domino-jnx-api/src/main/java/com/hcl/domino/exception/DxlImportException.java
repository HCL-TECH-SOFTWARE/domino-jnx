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
package com.hcl.domino.exception;

import com.hcl.domino.dxl.DxlImporterLog;

/**
 * Represents an error log from a failed DXL import.
 *
 * @author Jesse Gallagher
 */
public class DxlImportException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private final DxlImporterLog log;

  public DxlImportException(final String message, final DxlImporterLog log) {
    super(message);
    this.log = log;
  }

  /**
   * @return the full log from the import operation
   * @since 1.0.24
   */
  public DxlImporterLog getLog() {
    return this.log;
  }

}
