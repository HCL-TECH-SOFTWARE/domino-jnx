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

import com.hcl.domino.DominoException;

/**
 * Represents error code ERR_SERVER_RESTRICTED (0x09FD), "Access to this server
 * has been restricted by the administrator"
 *
 * @author Jesse Gallagher
 * @since 1.0.20
 */
public class ServerRestrictedException extends DominoException {
  private static final long serialVersionUID = 1L;

  public ServerRestrictedException(final int id, final String message) {
    super(id, message);
  }

  public ServerRestrictedException(final int id, final String message, final Throwable cause) {
    super(id, message, cause);
  }

  public ServerRestrictedException(final String msg) {
    super(msg);
  }

  public ServerRestrictedException(final String msg, final Throwable cause) {
    super(msg, cause);
  }

}
