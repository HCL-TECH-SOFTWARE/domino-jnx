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

import com.hcl.domino.data.Database.NSFVersionInfo;

/**
 * @since 1.0.19
 */
public class NSFVersionInfoImpl implements NSFVersionInfo {
  private final int majorVersion;
  private final int minorVersion;

  public NSFVersionInfoImpl(final int majorVersion, final int minorVersion) {
    this.majorVersion = majorVersion;
    this.minorVersion = minorVersion;
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
  public String toString() {
    return "NSFVersionInfoImpl [majorVersion=" + this.majorVersion + ", minorVersion=" + this.minorVersion + "]";
  }

}
