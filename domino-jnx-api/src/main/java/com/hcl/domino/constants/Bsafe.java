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
package com.hcl.domino.constants;

/**
 * Represents constants originally from the {@code bsafe.h} header file.
 * 
 * @author Jesse Gallagher
 * @since 1.29.0
 */
public interface Bsafe {
  /** @since Notes/Domino 14.0 */
  int fJWT_validate_AllowExpired = 0x00000001;
  /** @since Notes/Domino 14.0 */
  int fJWT_validate_AllowMSWorkarounds = 0x00000002;
  /** @since Notes/Domino 14.0 */
  int fJWT_validate_UseCustomEmailClaim = 0x00000010;
  /** @since Notes/Domino 14.0 */
  int fJWT_validate_AllowAlternateAud = 0x00000020;
  /** @since Notes/Domino 14.0 */
  int fJWT_validate_EnforceAllowedClients = 0x00000040;

}
