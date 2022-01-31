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
package com.hcl.domino.commons.data;

import com.hcl.domino.BuildVersionInfo;
import com.hcl.domino.misc.NotesConstants;

/**
 * @since 1.0.19
 */
public class BuildVersionInfoImpl implements BuildVersionInfo {
  /** Major version identifier */
  private final int majorVersion;
  /** Minor version identifier */
  private final int minorVersion;
  /** Maintenance Release identifier */
  private final int qmrNumber;
  /** Maintenance Update identifier */
  private final int qmuNumber;
  /** Hotfixes installed on machine */
  private final int hotfixNumber;
  /** See BUILDVERFLAGS_xxx */
  private final int flags;
  /** Fixpack version installed on machine */
  private final int fixpackNumber;

  public BuildVersionInfoImpl(final int majorVersion, final int minorVersion, final int qmrNumber, final int qmuNumber,
      final int hotfixNumber, final int flags, final int fixpackNumber) {
    this.majorVersion = majorVersion;
    this.minorVersion = minorVersion;
    this.qmrNumber = qmrNumber;
    this.qmuNumber = qmuNumber;
    this.hotfixNumber = hotfixNumber;
    this.flags = flags;
    this.fixpackNumber = fixpackNumber;
  }

  @Override
  public int getFixpackNumber() {
    return this.fixpackNumber;
  }

  @Override
  public int getHotfixNumber() {
    return this.hotfixNumber;
  }

  @Override
  public int getMajorVersion() {
    return this.majorVersion;
  }

  @Override
  public int getMinorVersion() {
    return this.minorVersion;
  }

  @Override
  public int getQMRNumber() {
    return this.qmrNumber;
  }

  @Override
  public int getQMUNumber() {
    return this.qmuNumber;
  }

  @Override
  public boolean isNonProductionBuild() {
    return (this.flags & NotesConstants.BLDVERFLAGS_NONPRODUCTION) == NotesConstants.BLDVERFLAGS_NONPRODUCTION;
  }
  
  @Override
  public boolean isAtLeast(int majorVersion, int minorVersion, int qmrNumber, int qmuNumber, int hotfixNumber) {
    if(this.majorVersion < majorVersion) {
      return false;
    } else if(this.majorVersion > majorVersion) {
      return true;
    } else if(this.minorVersion < minorVersion) {
      return false;
    } else if(this.minorVersion > minorVersion) {
      return true;
    } else if(this.qmrNumber < qmrNumber) {
      return false;
    } else if(this.qmrNumber > qmrNumber) {
      return true;
    } else if(this.qmuNumber < qmuNumber) {
      return false;
    } else if(this.qmuNumber > qmuNumber) {
      return true;
    } else if(this.hotfixNumber < hotfixNumber) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "BuildVersionInfoImpl [majorVersion=" + this.majorVersion + ", minorVersion=" + this.minorVersion + ", qmrNumber=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        + this.qmrNumber + ", qmuNumber=" + this.qmuNumber + ", hotfixNumber=" + this.hotfixNumber + ", isNonProdBuild=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        + this.isNonProductionBuild()
        + ", fixpackNumber=" + this.fixpackNumber + "]"; //$NON-NLS-1$ //$NON-NLS-2$
  }

}
