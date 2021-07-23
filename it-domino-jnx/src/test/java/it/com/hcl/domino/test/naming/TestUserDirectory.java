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
package it.com.hcl.domino.test.naming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import com.hcl.domino.naming.UserDirectory;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;
import it.com.hcl.domino.test.TestValidateCredentials;

@SuppressWarnings("nls")
public class TestUserDirectory extends AbstractNotesRuntimeTest {
	private static final Logger log = Logger.getLogger(TestUserDirectory.class.getPackage().getName());
	
	public static final String USER_DIRECTORY_EMAILUSER = "USER_DIRECTORY_EMAILUSER";
	public static final String USER_DIRECTORY_EMAILADDRESS = "USER_DIRECTORY_EMAILADDRESS";
	
	@Test
	public void testGetDirectoryPathsLocal() {
		UserDirectory dir = getClient().openUserDirectory(null);
		assertNotNull(dir);
		Set<String> dirNames = dir.getDirectoryPaths();
		if(log.isLoggable(Level.INFO)) {
			log.info("Got local directory names: " + dirNames);
		}
		assertNotNull(dirNames);
		assertFalse(dirNames.isEmpty());
	}

	@Test
	@EnabledIfEnvironmentVariable(named = TestValidateCredentials.VALIDATE_CREDENTIALS_SERVER, matches = ".+")
	public void testGetDirectoryPathsRemote() {
		String credServer = System.getenv(TestValidateCredentials.VALIDATE_CREDENTIALS_SERVER);
		
		UserDirectory dir = getClient().openUserDirectory(credServer);
		assertNotNull(dir);
		Set<String> dirNames = dir.getDirectoryPaths();
		if(log.isLoggable(Level.INFO)) {
			log.info("Got remote directory names: " + dirNames);
		}
		assertNotNull(dirNames);
		assertFalse(dirNames.isEmpty());
	}
	
	@Test
	public void testListAllLocal() {
		UserDirectory dir = getClient().openUserDirectory(null);
		assertNotNull(dir);
		
		List<List<Map<String, List<Object>>>> result = dir.query()
			.items(Collections.singleton("FullName"))
			.stream()
			.collect(Collectors.toList());
		assertNotNull(result);
		assertEquals(1, result.size(), "result should have one entry");
		List<Map<String, List<Object>>> result0 = result.get(0);
		assertFalse(result0.isEmpty(), "result[0] should not be empty");
	}
	
	@Test
	public void testNotFoundName() {
		UserDirectory dir = getClient().openUserDirectory(null);
		assertNotNull(dir);
		
		List<List<Map<String, List<Object>>>> result = dir.query()
			.names(Arrays.asList("I expect that I don't exist as a name in the local directory"))
			.items(Collections.singleton("FullName"))
			.stream()
			.collect(Collectors.toList());
		assertNotNull(result);
		assertTrue(result.isEmpty(), "result should be empty");
	}
	
	@Test
	public void testErrorMissingItems() {
		UserDirectory dir = getClient().openUserDirectory(null);
		assertNotNull(dir);
		
		assertThrows(IllegalArgumentException.class, () -> 
			dir.query()
				.names(Collections.singleton("foo"))
				.stream()
		);
		assertThrows(IllegalArgumentException.class, () -> 
			dir.query()
				.items((Collection<String>)null)
				.names(Collections.singleton("foo"))
				.stream()
		);
		assertThrows(IllegalArgumentException.class, () -> 
			dir.query()
				.items(Collections.emptySet())
				.names(Collections.singleton("foo"))
				.stream()
		);
		assertThrows(IllegalArgumentException.class, () -> 
			dir.query()
				.items()
				.names(Collections.singleton("foo"))
				.stream()
		);
	}

	@Test
	@EnabledIfEnvironmentVariable(named = TestValidateCredentials.VALIDATE_CREDENTIALS_SERVER, matches = ".+")
	@EnabledIfEnvironmentVariable(named = USER_DIRECTORY_EMAILUSER, matches = ".+")
	public void testLookupEmail() {
		String credServer = System.getenv(TestValidateCredentials.VALIDATE_CREDENTIALS_SERVER);
		String emailUser = System.getenv(USER_DIRECTORY_EMAILUSER);
		String expected = System.getenv(USER_DIRECTORY_EMAILADDRESS);
		
		UserDirectory dir = getClient().openUserDirectory(credServer);
		assertNotNull(dir);
		List<Map<String, List<Object>>> queriedName = dir.query()
			.names(emailUser)
			.items("InternetAddress")
			.stream()
			.findFirst()
			.orElse(null);
		assertNotNull(queriedName);
		assertFalse(queriedName.isEmpty());
		
		Map<String, List<Object>> namespaceMatch = queriedName.get(0);
		assertNotNull(namespaceMatch);
		assertEquals(expected, namespaceMatch.get("InternetAddress").get(0));
	}

	@Test
	@EnabledIfEnvironmentVariable(named = TestValidateCredentials.VALIDATE_CREDENTIALS_SERVER, matches = ".+")
	@EnabledIfEnvironmentVariable(named = USER_DIRECTORY_EMAILUSER, matches = ".+")
	public void testLookupEmail2() {
		String credServer = System.getenv(TestValidateCredentials.VALIDATE_CREDENTIALS_SERVER);
		String emailUser = System.getenv(USER_DIRECTORY_EMAILUSER);
		String expected = System.getenv(USER_DIRECTORY_EMAILADDRESS);
		
		UserDirectory dir = getClient().openUserDirectory(credServer);
		assertNotNull(dir);
		
		Map<String, List<Object>> namespaceMatch = dir.lookupUserValue(emailUser, "InternetAddress").orElse(null);
		assertNotNull(namespaceMatch);
		assertEquals(expected, namespaceMatch.get("InternetAddress").get(0));
	}
}
