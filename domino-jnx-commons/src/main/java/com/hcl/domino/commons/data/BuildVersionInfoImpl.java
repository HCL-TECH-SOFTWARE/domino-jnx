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
package com.hcl.domino.commons.data;

import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.BuildVersionInfo;

/**
 * @since 1.0.19
 */
public class BuildVersionInfoImpl implements BuildVersionInfo {
	/** Major version identifier */
	private int majorVersion;
	/** Minor version identifier */
	private int minorVersion;
	/** Maintenance Release identifier */
	private int qmrNumber;
	/** Maintenance Update identifier */
	private int qmuNumber;
	/** Hotfixes installed on machine */
	private int hotfixNumber;
	/** See BUILDVERFLAGS_xxx */
	private int flags;
	/** Fixpack version installed on machine */
	private int fixpackNumber;

	public BuildVersionInfoImpl(int majorVersion, int minorVersion, int qmrNumber, int qmuNumber, int hotfixNumber, int flags, int fixpackNumber) {
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.qmrNumber = qmrNumber;
		this.qmuNumber = qmuNumber;
		this.hotfixNumber = hotfixNumber;
		this.flags = flags;
		this.fixpackNumber = fixpackNumber;
	}
	
	@Override
	public int getMajorVersion() {
		return majorVersion;
	}
	
	@Override
	public int getMinorVersion() {
		return minorVersion;
	}
	
	@Override
	public int getQMRNumber() {
		return qmrNumber;
	}
	
	@Override
	public int getQMUNumber() {
		return qmuNumber;
	}
	
	@Override
	public int getHotfixNumber() {
		return hotfixNumber;
	}
	
	@Override
	public int getFixpackNumber() {
		return fixpackNumber;
	}
	
	@Override
	public boolean isNonProductionBuild() {
		return (flags & NotesConstants.BLDVERFLAGS_NONPRODUCTION) == NotesConstants.BLDVERFLAGS_NONPRODUCTION;
	}

	@Override
	public String toString() {
		return "BuildVersionInfoImpl [majorVersion=" + majorVersion + ", minorVersion=" + minorVersion + ", qmrNumber=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ qmrNumber + ", qmuNumber=" + qmuNumber + ", hotfixNumber=" + hotfixNumber + ", isNonProdBuild=" + isNonProductionBuild() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ ", fixpackNumber=" + fixpackNumber + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	
}
