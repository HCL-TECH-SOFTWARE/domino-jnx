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
package it.com.hcl.domino.test.acl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClient.Encryption;
import com.hcl.domino.data.Database;
import com.hcl.domino.security.Acl;
import com.hcl.domino.security.AclAccess;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestAcl extends AbstractNotesRuntimeTest {

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

  @Test
  public void testLookupAccess() throws Exception {
    this.withTempDb(database -> {
      final DominoClient client = this.getClient();

      final Acl acl = database.getACL();

      final String name = client.getEffectiveUserName();

      final AclAccess entry = acl.lookupAccess(name);

      Assertions.assertNotNull(entry, "Access level could not be determined");
    });
  }

  @Test
  public void testReadAdminServer() throws Exception {
    this.withTempDb(database -> {
      final Acl acl = database.getACL();

      Assertions.assertNotNull(acl.getAdminServer(), "Admin server was null");
    });
  }

  @Test
  public void testSave() throws Exception {
    this.withTempDb(tempDb -> {
      final String tempDbServer = tempDb.getServer();
      final String tempDbFilePath = tempDb.getAbsoluteFilePath();

      DominoClient client = this.getClient();
      Acl acl = tempDb.getACL();

      // to verify if saving works, we will add some roles, save and close the
      // database
      // then we will re-open it and verify the level
      final List<String> newRoles = new ArrayList<>();
      for (int i = 1; i < 10; i++) {
        newRoles.add("[role_" + i + "]");

        acl.addRole(newRoles.get(newRoles.size() - 1));
      }

      Assertions.assertTrue(this.containsAll(newRoles, acl.getRoles()), "Didn't find all added ACL roles");

      // now this worked -> save, close, re-open
      acl.save();

      // recycle all and recreate Domino client
      client = this.reloadClient();

      final Path tempDbPath2 = Files.createTempFile(this.getClass().getName(), ".nsf"); //$NON-NLS-1$
      Files.delete(tempDbPath2);

      final Database tempDb2 = client.createDatabaseFromTemplate(tempDbServer, tempDbFilePath,
          "", tempDbPath2.toString(), Encryption.None);

      try {
        acl = tempDb2.getACL();

        Assertions.assertTrue(this.containsAll(newRoles, acl.getRoles()), "Didn't find all added ACL roles");
      } finally {
        tempDb2.close();
        try {
          client.deleteDatabase(null, tempDbPath2.toString());
        } catch (final Throwable t) {
          System.err.println("Unable to delete database " + tempDb2 + ": " + t);
        }
      }

    });

  }

  @Test
  public void testUniformAccess() throws Exception {
    this.withTempDb(database -> {
      final Acl acl = database.getACL();

      final boolean isUniformAccess = acl.isUniformAccess();

      acl.setUniformAccess(!isUniformAccess);

      Assertions.assertEquals(!isUniformAccess, acl.isUniformAccess(), "ACL uniform access has not changed");
      Assertions.assertNotEquals(isUniformAccess, acl.isUniformAccess(), "ACL uniform access did not toggle");
    });
  }

}
