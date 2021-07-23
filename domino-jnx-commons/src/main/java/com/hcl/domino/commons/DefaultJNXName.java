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
package com.hcl.domino.commons;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.hcl.domino.Name;
import com.hcl.domino.commons.util.ReverseStringTokenizer;
import com.hcl.domino.commons.util.StringTokenizerExt;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.naming.Names;

public class DefaultJNXName implements Name {
	private String m_nameCanonical;
	
	public DefaultJNXName(String name) {
		m_nameCanonical = Names.toCanonical(name);
	}
	
	@Override
	public List<String> getOrgUnits() {
		List<String> orgUnits = new ArrayList<>();
		
		ReverseStringTokenizer st = new ReverseStringTokenizer(m_nameCanonical, "/"); //$NON-NLS-1$

		while (st.hasMoreTokens()) {
			String currToken = st.nextToken();
			if (StringUtil.startsWithIgnoreCase(currToken, "ou=")) { //$NON-NLS-1$
				orgUnits.add(currToken.substring(3));
			}
		}

		return orgUnits;
	}

	@Override
	public String getCommon() {
		return Names.toCommon(m_nameCanonical);
	}

	private String getOrgUnit(String name, int idx) {
		ReverseStringTokenizer st = new ReverseStringTokenizer(m_nameCanonical, "/"); //$NON-NLS-1$
		int currIdx=0;
		while (st.hasMoreTokens()) {
			String currToken = st.nextToken();
			if (StringUtil.startsWithIgnoreCase(currToken, "ou=")) { //$NON-NLS-1$
				currIdx++;
				if (currIdx == idx) {
					return currToken.substring(3);
				}
			}
		}
		return ""; //$NON-NLS-1$
	}
	
	@Override
	public String getOrgUnit1() {
		return getOrgUnit(m_nameCanonical, 1);
	}

	@Override
	public String getOrgUnit2() {
		return getOrgUnit(m_nameCanonical, 2);
	}

	@Override
	public String getOrgUnit3() {
		return getOrgUnit(m_nameCanonical, 3);
	}

	@Override
	public String getOrgUnit4() {
		return getOrgUnit(m_nameCanonical, 4);
	}

	@Override
	public String getOrganisation() {
		StringTokenizerExt st = new StringTokenizerExt(m_nameCanonical, "/"); //$NON-NLS-1$
		while (st.hasMoreTokens()) {
			String currToken = st.nextToken();
			if (StringUtil.startsWithIgnoreCase(currToken, "o=")) { //$NON-NLS-1$
				return currToken.substring(2);
			}
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public String getCountry() {
		StringTokenizerExt st = new StringTokenizerExt(m_nameCanonical, "/"); //$NON-NLS-1$
		while (st.hasMoreTokens()) {
			String currToken = st.nextToken();
			if (StringUtil.startsWithIgnoreCase(currToken, "c=")) { //$NON-NLS-1$
				return currToken.substring(2);
			}
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public String getAbbreviated() {
		return Names.toAbbreviated(m_nameCanonical);
	}

	@Override
	public String getCanonical() {
		return m_nameCanonical;
	}

	@Override
	public String toString() {
		return MessageFormat.format("JNAName [name={0}]", m_nameCanonical); //$NON-NLS-1$
	}
	
}
