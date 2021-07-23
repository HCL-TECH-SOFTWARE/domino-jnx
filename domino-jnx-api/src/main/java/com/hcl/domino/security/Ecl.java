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
package com.hcl.domino.security;

import java.util.Collection;
import java.util.Set;

import com.hcl.domino.DominoClient.ECLType;
import com.hcl.domino.UserNamesList;
import com.hcl.domino.data.Document;

/**
 * Execution Control List (ECL) on server or client
 */
public interface Ecl {

  /**
   * Reads the current capabilities
   *
   * @return set of capabilities
   */
  Set<ECLCapability> getCapabilities();

  /**
   * Returns the names list for which we compute the ECL
   *
   * @return names list
   */
  UserNamesList getNamesList();

  /**
   * Returns true if ECL can be modified by the user
   *
   * @return true if editable
   */
  boolean isEditable();

  /**
   * Method to modify the ECL for "-No signature-" and add trusted capabilities
   *
   * @param type         ECL type
   * @param capabilities capabilities to trust
   * @param sessionOnly  true to not permanently change the ECL
   * @return new capabilities
   */
  Set<ECLCapability> trustNoSignatureUser(ECLType type, Collection<ECLCapability> capabilities, boolean sessionOnly);

  /**
   * Method to modify the ECL for the signer of the specified document and add
   * trusted
   * capabilities
   *
   * @param doc          signed document (we read $Signature internally)
   * @param type         ECL type
   * @param capabilities capabilities to trust
   * @param sessionOnly  true to not permanently change the ECL
   * @return new capabilities
   */
  Set<ECLCapability> trustSignerOfDocument(Document doc, ECLType type, Collection<ECLCapability> capabilities, boolean sessionOnly);

}
