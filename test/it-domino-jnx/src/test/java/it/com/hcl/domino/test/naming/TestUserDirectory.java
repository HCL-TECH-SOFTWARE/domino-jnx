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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
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
  public void testErrorMissingItems() {
    final UserDirectory dir = this.getClient().openUserDirectory(null);
    Assertions.assertNotNull(dir);

    Assertions.assertThrows(IllegalArgumentException.class, () -> dir.query()
        .names(Collections.singleton("foo"))
        .stream());
    Assertions.assertThrows(IllegalArgumentException.class, () -> dir.query()
        .items((Collection<String>) null)
        .names(Collections.singleton("foo"))
        .stream());
    Assertions.assertThrows(IllegalArgumentException.class, () -> dir.query()
        .items(Collections.emptySet())
        .names(Collections.singleton("foo"))
        .stream());
    Assertions.assertThrows(IllegalArgumentException.class, () -> dir.query()
        .items()
        .names(Collections.singleton("foo"))
        .stream());
  }

  @Test
  public void testGetDirectoryPathsLocal() {
    final UserDirectory dir = this.getClient().openUserDirectory(null);
    Assertions.assertNotNull(dir);
    final Set<String> dirNames = dir.getDirectoryPaths();
    if (TestUserDirectory.log.isLoggable(Level.INFO)) {
      TestUserDirectory.log.info("Got local directory names: " + dirNames);
    }
    Assertions.assertNotNull(dirNames);
    Assertions.assertFalse(dirNames.isEmpty());
  }

  @Test
  public void testGetPrimaryDirectoryPathLocal() {
    final UserDirectory dir = this.getClient().openUserDirectory(null);
    Assertions.assertNotNull(dir);
    Optional<String> primaryDirPath = dir.getPrimaryDirectoryPath();
    
    if (TestUserDirectory.log.isLoggable(Level.INFO)) {
      TestUserDirectory.log.info("Got primary local directory name: " + primaryDirPath);
    }

    Assertions.assertTrue(primaryDirPath.isPresent());
    
    final Set<String> dirNames = dir.getDirectoryPaths();
    Assertions.assertNotNull(dirNames);
    Assertions.assertFalse(dirNames.isEmpty());
    Assertions.assertTrue(dirNames.contains(primaryDirPath.get()));
  }

  @Test
  @EnabledIfEnvironmentVariable(named = TestValidateCredentials.VALIDATE_CREDENTIALS_SERVER, matches = ".+")
  public void testGetDirectoryPathsRemote() {
    final String credServer = System.getenv(TestValidateCredentials.VALIDATE_CREDENTIALS_SERVER);

    final UserDirectory dir = this.getClient().openUserDirectory(credServer);
    Assertions.assertNotNull(dir);
    final Set<String> dirNames = dir.getDirectoryPaths();
    if (TestUserDirectory.log.isLoggable(Level.INFO)) {
      TestUserDirectory.log.info("Got remote directory names: " + dirNames);
    }
    Assertions.assertNotNull(dirNames);
    Assertions.assertFalse(dirNames.isEmpty());
  }

  @Test
  public void testListAllLocal() {
    final UserDirectory dir = this.getClient().openUserDirectory(null);
    Assertions.assertNotNull(dir);

    final List<List<Map<String, List<Object>>>> result = dir.query()
        .items(Collections.singleton("FullName"))
        .stream()
        .collect(Collectors.toList());
    Assertions.assertNotNull(result);
    Assertions.assertEquals(1, result.size(), "result should have one entry");
    final List<Map<String, List<Object>>> result0 = result.get(0);
    Assertions.assertFalse(result0.isEmpty(), "result[0] should not be empty");
  }

  @Test
  @EnabledIfEnvironmentVariable(named = TestValidateCredentials.VALIDATE_CREDENTIALS_SERVER, matches = ".+")
  @EnabledIfEnvironmentVariable(named = TestUserDirectory.USER_DIRECTORY_EMAILUSER, matches = ".+")
  public void testLookupEmail() {
    final String credServer = System.getenv(TestValidateCredentials.VALIDATE_CREDENTIALS_SERVER);
    final String emailUser = System.getenv(TestUserDirectory.USER_DIRECTORY_EMAILUSER);
    final String expected = System.getenv(TestUserDirectory.USER_DIRECTORY_EMAILADDRESS);

    final UserDirectory dir = this.getClient().openUserDirectory(credServer);
    Assertions.assertNotNull(dir);
    final List<Map<String, List<Object>>> queriedName = dir.query()
        .names(emailUser)
        .items("InternetAddress")
        .stream()
        .findFirst()
        .orElse(null);
    Assertions.assertNotNull(queriedName);
    Assertions.assertFalse(queriedName.isEmpty());

    final Map<String, List<Object>> namespaceMatch = queriedName.get(0);
    Assertions.assertNotNull(namespaceMatch);
    Assertions.assertEquals(expected, namespaceMatch.get("InternetAddress").get(0));
  }

  @Test
  @EnabledIfEnvironmentVariable(named = TestValidateCredentials.VALIDATE_CREDENTIALS_SERVER, matches = ".+")
  @EnabledIfEnvironmentVariable(named = TestUserDirectory.USER_DIRECTORY_EMAILUSER, matches = ".+")
  public void testLookupEmail2() {
    final String credServer = System.getenv(TestValidateCredentials.VALIDATE_CREDENTIALS_SERVER);
    final String emailUser = System.getenv(TestUserDirectory.USER_DIRECTORY_EMAILUSER);
    final String expected = System.getenv(TestUserDirectory.USER_DIRECTORY_EMAILADDRESS);

    final UserDirectory dir = this.getClient().openUserDirectory(credServer);
    Assertions.assertNotNull(dir);

    final Map<String, List<Object>> namespaceMatch = dir.lookupUserValue(emailUser, "InternetAddress").orElse(null);
    Assertions.assertNotNull(namespaceMatch);
    Assertions.assertEquals(expected, namespaceMatch.get("InternetAddress").get(0));
  }

  @Test
  public void testNotFoundName() {
    final UserDirectory dir = this.getClient().openUserDirectory(null);
    Assertions.assertNotNull(dir);

    final List<List<Map<String, List<Object>>>> result = dir.query()
        .names(Arrays.asList("I expect that I don't exist as a name in the local directory"))
        .items(Collections.singleton("FullName"))
        .stream()
        .collect(Collectors.toList());
    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.isEmpty(), "result should be empty");
  }
}
