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
  private final String m_nameCanonical;

  public DefaultJNXName(final String name) {
    this.m_nameCanonical = Names.toCanonical(name);
  }

  @Override
  public String getAbbreviated() {
    return Names.toAbbreviated(this.m_nameCanonical);
  }

  @Override
  public String getCanonical() {
    return this.m_nameCanonical;
  }

  @Override
  public String getCommon() {
    return Names.toCommon(this.m_nameCanonical);
  }

  @Override
  public String getCountry() {
    final StringTokenizerExt st = new StringTokenizerExt(this.m_nameCanonical, "/"); //$NON-NLS-1$
    while (st.hasMoreTokens()) {
      final String currToken = st.nextToken();
      if (StringUtil.startsWithIgnoreCase(currToken, "c=")) { //$NON-NLS-1$
        return currToken.substring(2);
      }
    }
    return ""; //$NON-NLS-1$
  }

  @Override
  public String getOrganisation() {
    final StringTokenizerExt st = new StringTokenizerExt(this.m_nameCanonical, "/"); //$NON-NLS-1$
    while (st.hasMoreTokens()) {
      final String currToken = st.nextToken();
      if (StringUtil.startsWithIgnoreCase(currToken, "o=")) { //$NON-NLS-1$
        return currToken.substring(2);
      }
    }
    return ""; //$NON-NLS-1$
  }

  private String getOrgUnit(final String name, final int idx) {
    final ReverseStringTokenizer st = new ReverseStringTokenizer(this.m_nameCanonical, "/"); //$NON-NLS-1$
    int currIdx = 0;
    while (st.hasMoreTokens()) {
      final String currToken = st.nextToken();
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
    return this.getOrgUnit(this.m_nameCanonical, 1);
  }

  @Override
  public String getOrgUnit2() {
    return this.getOrgUnit(this.m_nameCanonical, 2);
  }

  @Override
  public String getOrgUnit3() {
    return this.getOrgUnit(this.m_nameCanonical, 3);
  }

  @Override
  public String getOrgUnit4() {
    return this.getOrgUnit(this.m_nameCanonical, 4);
  }

  @Override
  public List<String> getOrgUnits() {
    final List<String> orgUnits = new ArrayList<>();

    final ReverseStringTokenizer st = new ReverseStringTokenizer(this.m_nameCanonical, "/"); //$NON-NLS-1$

    while (st.hasMoreTokens()) {
      final String currToken = st.nextToken();
      if (StringUtil.startsWithIgnoreCase(currToken, "ou=")) { //$NON-NLS-1$
        orgUnits.add(currToken.substring(3));
      }
    }

    return orgUnits;
  }

  @Override
  public String toString() {
    return MessageFormat.format("JNAName [name={0}]", this.m_nameCanonical); //$NON-NLS-1$
  }

}
