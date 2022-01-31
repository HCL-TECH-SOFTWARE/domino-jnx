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
package it.com.hcl.domino.test.acl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoException;
import com.hcl.domino.data.Database;
import com.hcl.domino.security.Acl;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestAclRoles extends AbstractNotesRuntimeTest {

  private boolean containsAll(final List<String> expected, final List<String> actual) {
    if (expected.size() > actual.size()) {
      return false;
    }

    final Set<String> allValues = new HashSet<>(actual);

    for (final String element : expected) {
      if (!allValues.contains(element)) {
        return false;
      }
    }
    return true;
  }

  private boolean containsNone(final List<String> expected, final List<String> actual) {
    final Set<String> allValues = new HashSet<>(actual);

    for (final String element : expected) {
      if (allValues.contains(element)) {
        return false;
      }
    }
    return true;
  }

  @Test
  public void renameAddAclRole() throws Exception {
    this.withTempDb(database -> {
      final Acl acl = database.getACL();

      // verify existing role
      final String role1 = "[role_a]";
      final String role3 = "[role_c]";

      acl.addRole(role1);
      acl.addRole(role3);

      Assertions.assertTrue(this.containsAll(Arrays.asList(role1, role3), acl.getRoles()), "Didn't find added ACL roles");

      final String role2 = "[role_b]";

      acl.renameRole(role1, role2);

      Assertions.assertTrue(this.containsAll(Arrays.asList(role2), acl.getRoles()), "Didn't find renamed ACL role");
      Assertions.assertTrue(this.containsNone(Arrays.asList(role1), acl.getRoles()), "Still find renamed ACL role");

      // verify exception for not existing old role, existing new roles and other
      // illegal arguments
      Assertions.assertThrows(DominoException.class, (Executable) () -> acl.renameRole("-__-_-__-", "xyz_345"), "Did not throw an error when old role does not exist");

      Assertions.assertThrows(DominoException.class, (Executable) () -> acl.renameRole("role_b", role3), "Did not throw an error when new role already exists");

      Assertions.assertThrows(IllegalArgumentException.class, (Executable) () -> acl.renameRole(null, "x"), "Did not throw an error when old role was null");

      Assertions.assertThrows(IllegalArgumentException.class, (Executable) () -> acl.renameRole("", "x"), "Did not throw an error when old role was empty");

      Assertions.assertThrows(IllegalArgumentException.class, (Executable) () -> acl.renameRole("x", null), "Did not throw an error when new role was null");

      Assertions.assertThrows(IllegalArgumentException.class, (Executable) () -> acl.renameRole("a", ""), "Did not throw an error when new role was empty");

    });

  }

  @Test
  public void testAddAclRoles() throws Exception {
    this.withTempDb(database -> {
      final Acl acl = database.getACL();

      final List<String> newRoles = new ArrayList<>();
      for (int i = 1; i < 10; i++) {
        newRoles.add("[role_" + i + "]");

        acl.addRole(newRoles.get(newRoles.size() - 1));
      }

      Assertions.assertTrue(this.containsAll(newRoles, acl.getRoles()), "Didn't find all added ACL roles");
    });
  }

  @Test
  public void testAddLongRolenames() throws Exception {
    this.withTempDb(database -> {
      final Acl acl = database.getACL();
      Assertions.assertThrows(IllegalArgumentException.class, () -> {
        acl.addRole("1234567890123456");
      });
    });
  }

  @Test
  public void testReadAclRoles() throws IOException {
    final DominoClient client = this.getClient();

    final Database dbNames = client.openDatabase("", "names.nsf");

    final Acl acl = dbNames.getACL();

    Assertions.assertTrue(acl.getRoles().size() > 0, "Read no ACL roles");
  }

  @Test
  public void testRemoveAclRoles() throws Exception {
    this.withTempDb(database -> {
      final Acl acl = database.getACL();

      final List<String> newRoles = new ArrayList<>();
      for (int i = 1; i < 10; i++) {
        newRoles.add("[role_" + i + "]");

        acl.addRole(newRoles.get(newRoles.size() - 1));
      }

      Assertions.assertTrue(this.containsAll(newRoles, acl.getRoles()), "Didn't find all added ACL roles");

      for (final String role : newRoles) {
        acl.removeRole(role);
      }

      Assertions.assertTrue(this.containsNone(newRoles, acl.getRoles()), "Still found some of the removed ACL roles");

    });

  }
}
