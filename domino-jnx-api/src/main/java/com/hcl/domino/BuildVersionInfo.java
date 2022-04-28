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
  
  /**
   * Determines whether the build version is at least a Domino version number.
   * 
   * <p>Note: this is expected to check only number values, and does not take into
   * account release dates. For example, version 12.0.0 is considered "newer"
   * than 11.0.1FP4 despite coming out several months previous.</p>
   * 
   * @param majorVersion the {@link #getMajorVersion() major version} to check
   * @param minorVersion the {@link #getMinorVersion() minor version} to check
   * @param qmrNumber the {@link #getQMRNumber() QMR version} to check
   * @param qmuNumber the {@link #getQMUNumber() QMU number} to check
   * @param hotfixNumber the {@link #getHotfixNumber() hotfix number} to check
   * @return {@code true} if this build version represents a version considered
   *         the same or higher than the provided values; {@code false} otherwise
   * @since 1.1.1
   */
  boolean isAtLeast(int majorVersion, int minorVersion, int qmrNumber, int qmuNumber, int hotfixNumber);

  /**
   * This function returns the "major" portion of the build number of the Domino or
   * Notes executable running on the system where the specified database resides.
   * Use this information to determine what Domino or Notes release is running on a given system.
   * The database handle input may represent a local database, or a database that resides
   * on a Lotus Domino Server.<br>
   * <br>
   * Domino or Notes Release 1.0 (all preliminary and final versions) are build numbers 1 to 81.<br>
   * Domino or Notes Release 2.0 (all preliminary and final versions) are build numbers 82 to 93.<br>
   * Domino or Notes Release 3.0 (all preliminary and final versions) are build numbers 94 to 118.<br>
   * Domino or Notes Release 4.0 (all preliminary and final versions) are build numbers 119 to 136.<br>
   * Domino or Notes Release 4.1 (all preliminary and final versions) are build number 138.<br>
   * Domino or Notes Release 4.5 (all preliminary and final versions) are build number 140 - 145.<br>
   * Domino or Notes Release 4.6 (all preliminary and final versions) are build number 147.<br>
   * Domino or Notes Release 5.0 Beta 1 is build number 161.<br>
   * Domino or Notes Release 5.0 Beta 2 is build number 163.<br>
   * Domino or Notes Releases 5.0 - 5.0.11 are build number 166.<br>
   * Domino or Notes Release Rnext Beta 1 is build number 173.<br>
   * Domino or Notes Release Rnext Beta 2 is build number 176.<br>
   * Domino or Notes Release Rnext Beta 3 is build number 178.<br>
   * Domino or Notes Release Rnext Beta 4 is build number 179.<br>
   * Domino or Notes 6  Pre-release 1 is build number 183.<br>
   * Domino or Notes 6  Pre-release 2 is build number 185.<br>
   * Domino or Notes 6  Release Candidate is build number 190.<br>
   * Domino or Notes 6 - 6.0.2 are build number 190.<br>
   * Domino or Notes 6.0.3 - 6.5 are build numbers 191 to 194.<br>
   * Domino or Notes 7.0 Beta 2 is build number 254.<br>
   * Domino or Notes 9.0 is build number 400.<br>
   * Domino or Notes 9.0.1 is build number 405.<br>
   * 
   * @return build number
   */
  int getBuildNumber();
  
}