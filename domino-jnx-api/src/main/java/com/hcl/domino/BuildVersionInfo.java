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
package com.hcl.domino;

/**
 * Version information about local or remote environments
 */
public interface BuildVersionInfo {
  /**
   * Returns the Fixpack/feature version installed on machine
   *
   * @return fixpack/feature version
   */
  int getFixpackNumber();

  /**
   * Returns the Hotfixes installed on machine
   *
   * @return hotfixes
   */
  int getHotfixNumber();

  /**
   * Returns the major version identifier
   *
   * @return identifier
   */
  int getMajorVersion();

  /**
   * Returns the minor version identifier
   *
   * @return identifier
   */
  int getMinorVersion();

  /**
   * Returns the Maintenance Release identifier
   *
   * @return identifier
   */
  int getQMRNumber();

  /**
   * Returns the Maintenance Update identifier
   *
   * @return identifier
   */
  int getQMUNumber();

  /**
   * Returns whether this is a non-production style build (internal only)
   *
   * @return true if non-production build
   */
  boolean isNonProductionBuild();

}