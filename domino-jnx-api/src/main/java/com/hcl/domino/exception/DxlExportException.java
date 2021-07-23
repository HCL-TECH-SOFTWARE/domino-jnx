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
package com.hcl.domino.exception;

import com.hcl.domino.dxl.DxlExporterLog;

/**
 * Represents an error log from a failed DXL export.
 *
 * @author Jesse Gallagher
 */
public class DxlExportException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private final int errorId;
  private final DxlExporterLog log;

  public DxlExportException(final String message, final int errorId, final DxlExporterLog log) {
    super(message);
    this.errorId = errorId;
    this.log = log;
  }

  /**
   * @return the ID of the encountered DXL export error
   */
  public int getErrorId() {
    return this.errorId;
  }

  /**
   * @return the full log from the export operation
   * @since 1.0.24
   */
  public DxlExporterLog getLog() {
    return this.log;
  }
}
