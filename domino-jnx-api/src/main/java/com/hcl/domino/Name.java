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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Used to handle Domino usernames
 * 
 * @author t.b.d
 * @since 0.5.0
 */
public interface Name {

	/**
	 * Retrieves the CN element of this name, without the "CN=" prefix"
	 * 
	 * @return common name format, e.g. John Doe, Jane Doe
	 */
	String getCommon();

	/**
	 * Retrieves all OU elements of this name as a List
	 * 
	 * @return List of OU elements e.g. {East,Sales}
	 */
	default List<String> getOrgUnits() {
		List<String> orgUnits = new ArrayList<>();
		orgUnits.add(getOrgUnit1());
		orgUnits.add(getOrgUnit2());
		orgUnits.add(getOrgUnit3());
		orgUnits.add(getOrgUnit4());
		orgUnits = orgUnits.stream().filter(t -> t != null && t.length()>0).collect(Collectors.toList());
		return orgUnits;
	}

	/**
	 * Retrieves the first OU element of this name
	 * 
	 * @return First OU element as string, e.g. East or empty string
	 */
	String getOrgUnit1();

	/**
	 * Retrieves the second OU element of this name
	 * 
	 * @return Second OU element as string, e.g. Sales or empty string
	 */
	String getOrgUnit2();

	/**
	 * Retrieves the third OU element of this name
	 * 
	 * @return Third OU element as string, e.g. Sales or empty string
	 */
	String getOrgUnit3();

	/**
	 * Retrieves the fourth OU element of this name
	 * 
	 * @return Fourth OU element as string, e.g. Sales or empty string
	 */
	String getOrgUnit4();

	/**
	 * Retrieves the O element of this name
	 * 
	 * @return Third OU element as string, e.g. MyOrg, or empty string
	 */
	String getOrganisation();

	/**
	 * Retrieves the C element of this name
	 * 
	 * @return C element as string, e.g. US or empty string
	 */
	String getCountry();

	/**
	 * Retrieves the abbreviated format of this name (without CN=, OU=, O= or C=)
	 * 
	 * @return abbreviated format, e.g. John Doe/HR/MyOrg, Jane Doe/East/Sales/MyOrg
	 */
	String getAbbreviated();

	/**
	 * Retrieves the canonical format of this name (with CN=, OU= if applicable, O=, C= if available)
	 * 
	 * @return canonical format, e.g. CN=John Doe/OU=HR/O=MyOrg/C=US, CN=Jane Doe/OU=Sales/OU=East/O=MyOrg
	 */
	String getCanonical();

}
