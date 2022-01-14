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

import com.hcl.domino.DominoException;

/**
 * Represents error code 259, "File does not exist"
 */
public class FileDoesNotExistException extends DominoException {
  private static final long serialVersionUID = 1L;

  public FileDoesNotExistException(final int id, final String message) {
    super(id, message);
  }

  public FileDoesNotExistException(final int id, final String message, final Throwable cause) {
    super(id, message, cause);
  }

  public FileDoesNotExistException(final String msg) {
    super(msg);
  }

  public FileDoesNotExistException(final String msg, final Throwable cause) {
    super(msg, cause);
  }

}
