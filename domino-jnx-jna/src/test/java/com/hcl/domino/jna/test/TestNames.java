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
package com.hcl.domino.jna.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.hcl.domino.Name;
import com.hcl.domino.naming.Names;

@SuppressWarnings("nls")
public class TestNames extends AbstractJNARuntimeTest {

	@Test
	public void testNameOperations() throws IOException {
		
		String nameStr = "CN=John B Goode/OU=Guitars/OU=Music/OU=Sales/OU=East/O=Acme/C=US";
		Name name = Names.createName(nameStr);
		
		assertNotNull(name, "Name is not null");
		
		assertEquals("John B Goode", name.getCommon(), "Common name ok");
		assertEquals("East", name.getOrgUnit1(), "OU1 ok");
		assertEquals("Sales", name.getOrgUnit2(), "OU2 ok");
		assertEquals("Music", name.getOrgUnit3(), "OU3 ok");
		assertEquals("Guitars", name.getOrgUnit4(), "OU4 ok");
		assertEquals("Acme", name.getOrganisation(), "Org ok");
		assertEquals("US", name.getCountry(), "Country ok");

		assertEquals(
				Arrays.asList(
						name.getOrgUnit1(),
						name.getOrgUnit2(),
						name.getOrgUnit3(),
						name.getOrgUnit4()
						)
				.stream()
				.filter((el) -> { return el!=null && el.length()>0; })
				.collect(Collectors.toList()),
				name.getOrgUnits(), "OrgUnits ok");

		assertTrue(Names.equalNames(nameStr, "john b goode/guitars/music/sales/east/acme/us"), "Name equality");
	
		assertEquals("John B Goode", Names.toCommon(nameStr), "toCommon ok");
		assertEquals("John B Goode/Guitars/Music/Sales/East/Acme/US", Names.toAbbreviated(nameStr), "toAbbreviated ok");
		
		assertEquals(nameStr, Names.toCanonical("John B Goode/Guitars/Music/Sales/East/Acme/US"), "toCanonical ok");

		String nameOU3Str = "CN=John B Goode/OU=Music/OU=Sales/OU=East/O=Acme/C=US";
		assertEquals(nameOU3Str, Names.toCanonical("John B Goode/Music/Sales/East/Acme/US"), "Name with 3 OUs ok");
		
		String nameOU2Str = "CN=John B Goode/OU=Sales/OU=East/O=Acme/C=US";
		assertEquals(nameOU2Str, Names.toCanonical("John B Goode/Sales/East/Acme/US"), "Name with 2 OUs ok");
		
		String nameOU1Str = "CN=John B Goode/OU=East/O=Acme/C=US";
		assertEquals(nameOU1Str, Names.toCanonical("John B Goode/East/Acme/US"), "Name with 1 OUs ok");
		
		String nameOU0Str = "CN=John B Goode/O=Acme/C=US";
		assertEquals(nameOU0Str, Names.toCanonical("John B Goode/Acme/US"), "Name with 0 OUs ok");
		
		String nameNoCountryStr = "CN=John B Goode/O=Acme";
		assertEquals(nameNoCountryStr, Names.toCanonical("John B Goode/Acme"), "Name without country ok");

		String nameNoOrgStr = "John B Goode";
		assertEquals(nameNoOrgStr, Names.toCanonical("John B Goode"), "Name without org ok");

	}

}
