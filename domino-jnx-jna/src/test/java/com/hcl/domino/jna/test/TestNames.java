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
package com.hcl.domino.jna.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.Name;
import com.hcl.domino.naming.Names;

@SuppressWarnings("nls")
public class TestNames extends AbstractJNARuntimeTest {

  @Test
  public void testNameOperations() throws IOException {

    final String nameStr = "CN=John B Goode/OU=Guitars/OU=Music/OU=Sales/OU=East/O=Acme/C=US";
    final Name name = Names.createName(nameStr);

    Assertions.assertNotNull(name, "Name is not null");

    Assertions.assertEquals("John B Goode", name.getCommon(), "Common name ok");
    Assertions.assertEquals("East", name.getOrgUnit1(), "OU1 ok");
    Assertions.assertEquals("Sales", name.getOrgUnit2(), "OU2 ok");
    Assertions.assertEquals("Music", name.getOrgUnit3(), "OU3 ok");
    Assertions.assertEquals("Guitars", name.getOrgUnit4(), "OU4 ok");
    Assertions.assertEquals("Acme", name.getOrganisation(), "Org ok");
    Assertions.assertEquals("US", name.getCountry(), "Country ok");

    Assertions.assertEquals(
        Arrays.asList(
            name.getOrgUnit1(),
            name.getOrgUnit2(),
            name.getOrgUnit3(),
            name.getOrgUnit4())
            .stream()
            .filter(el -> (el != null && el.length() > 0))
            .collect(Collectors.toList()),
        name.getOrgUnits(), "OrgUnits ok");

    Assertions.assertTrue(Names.equalNames(nameStr, "john b goode/guitars/music/sales/east/acme/us"), "Name equality");

    Assertions.assertEquals("John B Goode", Names.toCommon(nameStr), "toCommon ok");
    Assertions.assertEquals("John B Goode/Guitars/Music/Sales/East/Acme/US", Names.toAbbreviated(nameStr), "toAbbreviated ok");

    Assertions.assertEquals(nameStr, Names.toCanonical("John B Goode/Guitars/Music/Sales/East/Acme/US"), "toCanonical ok");

    final String nameOU3Str = "CN=John B Goode/OU=Music/OU=Sales/OU=East/O=Acme/C=US";
    Assertions.assertEquals(nameOU3Str, Names.toCanonical("John B Goode/Music/Sales/East/Acme/US"), "Name with 3 OUs ok");

    final String nameOU2Str = "CN=John B Goode/OU=Sales/OU=East/O=Acme/C=US";
    Assertions.assertEquals(nameOU2Str, Names.toCanonical("John B Goode/Sales/East/Acme/US"), "Name with 2 OUs ok");

    final String nameOU1Str = "CN=John B Goode/OU=East/O=Acme/C=US";
    Assertions.assertEquals(nameOU1Str, Names.toCanonical("John B Goode/East/Acme/US"), "Name with 1 OUs ok");

    final String nameOU0Str = "CN=John B Goode/O=Acme/C=US";
    Assertions.assertEquals(nameOU0Str, Names.toCanonical("John B Goode/Acme/US"), "Name with 0 OUs ok");

    final String nameNoCountryStr = "CN=John B Goode/O=Acme";
    Assertions.assertEquals(nameNoCountryStr, Names.toCanonical("John B Goode/Acme"), "Name without country ok");

    final String nameNoOrgStr = "John B Goode";
    Assertions.assertEquals(nameNoOrgStr, Names.toCanonical("John B Goode"), "Name without org ok");

  }

}
