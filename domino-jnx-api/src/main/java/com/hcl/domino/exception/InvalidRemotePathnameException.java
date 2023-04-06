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
 * Represents error code 0x22f, "Remote pathnames must be relative to the Data directory"
 *
 * @author Jesse Gallagher
 * @since 1.26.0
 */
public class InvalidRemotePathnameException extends DominoException {
  private static final long serialVersionUID = 1L;

  public InvalidRemotePathnameException(final int id, final String message) {
    super(id, message);
  }

  public InvalidRemotePathnameException(final int id, final String message, final Throwable cause) {
    super(id, message, cause);
  }

  public InvalidRemotePathnameException(final String msg) {
    super(msg);
  }

  public InvalidRemotePathnameException(final String msg, final Throwable cause) {
    super(msg, cause);
  }

}
