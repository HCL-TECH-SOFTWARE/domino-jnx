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
package com.hcl.domino.data;

import com.hcl.domino.richtext.structures.LicenseID;

/**
 * Represents the "V1V2_Author" pseudo-structure used for {@link ItemDataType#TYPE_USERID}
 * values in pre-V3 documents.
 * 
 * @author Jesse Gallagher
 * @since 1.0.41
 */
public interface PreV3Author {
  
  /**
   * Retrieves the user name stored in the structure.
   * 
   * @return a string user name
   */
  String getName();
  
  /**
   * Retrieves the semi-opaque license ID information stored
   * with the name.
   * 
   * @return a {@link LicenseID} instance
   */
  LicenseID getLicenseID();
}
