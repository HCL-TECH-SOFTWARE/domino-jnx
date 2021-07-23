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
package com.hcl.domino.commons.admin;

import java.util.ArrayList;
import java.util.List;

import com.hcl.domino.admin.ServerAdmin.DominoSSOToken;
import com.hcl.domino.data.DominoDateTime;

/**
 * SSO token data class
 * 
 * @author Karsten Lehmann
 */
public class DefaultDominoSSOToken implements DominoSSOToken {
	private String m_name;
	private List<String> m_domains;
	private boolean m_secureOnly;
	private String m_data;
	private DominoDateTime m_renewalDate;
	
	/**
	 * Creates a new instance
	 * 
	 * @param name name
	 * @param domains DNS domains
	 * @param secureOnly true to recommend that the token only be set on a secure connection
	 * @param data token data
	 * @param renewalDate optional date when the token needs a renewal (if specified on token generation)
	 */
	public DefaultDominoSSOToken(String name, List<String> domains, boolean secureOnly, String data, DominoDateTime renewalDate) {
		m_name = name;
		m_domains = domains;
		m_secureOnly = secureOnly;
		m_data = data;
		m_renewalDate = renewalDate;
	}
	
	@Override
	public <T> T getAdapter(Class<T> clazz) {
		return null;
	}
	
	@Override
	public String getName() {
		return m_name;
	}
	
	@Override
	public List<String> getDomains() {
		return m_domains;
	}
	
	@Override
	public boolean isSecureOnly() {
		return m_secureOnly;
	}
	
	@Override
	public String getData() {
		return m_data;
	}
	
	@Override
	public DominoDateTime getRenewalDate() {
		return m_renewalDate;
	}
	
	/**
	 * Produces a string to be used for the "LtpaToken" cookie for
	 * every domain
	 * 
	 * @return cookie strings
	 */
	@Override
	public List<String> toHTTPCookieStrings() {
		final String DOMAIN_STRING = ";Domain="; //$NON-NLS-1$
		final String PATH_STRING = ";Path=/"; //$NON-NLS-1$
		final String SECURE_ONLY = ";Secure"; //$NON-NLS-1$
		
		List<String> cookieStrings = new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		
		for (String currDomain : m_domains) {
			sb.append(m_data);
			sb.append(DOMAIN_STRING);
			sb.append(currDomain);
			sb.append(PATH_STRING);
			if (m_secureOnly) {
				sb.append(SECURE_ONLY);
			}
			
			cookieStrings.add(sb.toString());
			sb.setLength(0);
		}
		
		return cookieStrings;
	}
}