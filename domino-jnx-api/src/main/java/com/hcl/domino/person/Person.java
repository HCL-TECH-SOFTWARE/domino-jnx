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
package com.hcl.domino.person;

import com.hcl.domino.UserNamesList;
import com.hcl.domino.data.Database;

/**
 * Provides system information about a single person
 */
public interface Person {

  /**
   * Returns the canonical username of this person
   *
   * @return username
   */
  String getUsername();

  /**
   * Computes the {@link UserNamesList} of the person on the specified server.
   *
   * @param server server or null/empty string for local environment
   * @return names list
   */
  UserNamesList getUserNamesList(String server);

  /**
   * Opens the out of office information for the user with read/write access
   *
   * @param homeMailServer   Canonical or abbreviated name of the server where the
   *                         lookup for user information should be made
   *                         (optional). If the server name is not a home mail
   *                         server, an attempt will be made to figure out the
   *                         home mail server by looking first locally and, if
   *                         configured, in the extended directory. The lookups
   *                         can be suppressed by providing the server name in
   *                         <code>homeMailServer</code> parameter and setting the
   *                         <code>isHomeMailServer</code> parameter to TRUE.
   *                         Suppressing lookups is a more efficient option.
   * @param isHomeMailServer TRUE if the <code>homeMailServer</code> is user’s
   *                         home mail(optional). Set it only if you are sure that
   *                         user’s home mail server was specified. If FALSE the
   *                         look up for user’s home mail will be performed.
   * @param dbMail           If the application already has the mail file opened
   *                         they can pass it in for better better efficiency.
   * @return out of office context
   */
  OutOfOffice openOutOfOffice(String homeMailServer, boolean isHomeMailServer, Database dbMail);

}
