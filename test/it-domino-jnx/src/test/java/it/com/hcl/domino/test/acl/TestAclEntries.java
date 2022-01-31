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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.hcl.domino.DominoException;
import com.hcl.domino.security.Acl;
import com.hcl.domino.security.AclEntry;
import com.hcl.domino.security.AclFlag;
import com.hcl.domino.security.AclLevel;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestAclEntries extends AbstractNotesRuntimeTest {

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

  private boolean containsNone(final List<AclEntry> expected, final List<AclEntry> actual) {
    final Map<String, AclEntry> allByName = new HashMap<>();
    for (final AclEntry e : actual) {
      allByName.put(e.getName(), e);
    }

    for (final AclEntry element : expected) {
      if (allByName.containsKey(element.getName())) {
        return false;
      }
    }
    return true;
  }

  private boolean matchesAllEntries(final List<AclEntry> expected, final List<AclEntry> actual) {
    if (expected.size() > actual.size()) {
      return false;
    }

    final Map<String, AclEntry> allByName = new HashMap<>();
    for (final AclEntry e : actual) {
      allByName.put(e.getName(), e);
    }

    for (final AclEntry expectedEntry : expected) {
      final AclEntry actualEntry = allByName.get(expectedEntry.getName());

      if (actualEntry == null) {
        return false;
      }

      Assertions.assertEquals(expectedEntry.getAclLevel(), actualEntry.getAclLevel(),
          "Entry " + actualEntry.getName() + " does not have the expected level");

      Assertions.assertTrue(this.containsAll(expectedEntry.getRoles(), actualEntry.getRoles()),
          "Entry " + actualEntry.getName() + " does not contain all expected roles " + expectedEntry.getRoles().toString());

      for (final AclFlag expectedFlag : expectedEntry.getAclFlags()) {
        Assertions.assertTrue(actualEntry.getAclFlags().contains(expectedFlag),
            "Entry " + actualEntry.getName() + " does not contain expected flag " + expectedFlag.name());
      }
    }
    return true;
  }

  @Test
  public void testAddAclEntries() throws Exception {
    this.withTempDb(dbFakenames -> {
      final Acl acl = dbFakenames.getACL();

      final AclLevel level = AclLevel.AUTHOR;
      final EnumSet<AclFlag> flags = EnumSet.of(AclFlag.PERSON, AclFlag.GROUP);

      final List<String> newRoles = new ArrayList<>();

      for (int j = 1; j < 4; j++) {
        newRoles.add("[__role_" + j + "]");

        acl.addRole(newRoles.get(newRoles.size() - 1));
      }

      Assertions.assertTrue(this.containsAll(newRoles, acl.getRoles()), "Didn't find all added ACL roles");

      final List<AclEntry> newEntries = new ArrayList<>();
      for (int i = 1; i < 10; i++) {
        final String name = "__entry_" + i;

        final AclEntry newEntry = new AclEntry() {
          @Override
          public Set<AclFlag> getAclFlags() {
            return flags;
          }

          @Override
          public AclLevel getAclLevel() {
            return level;
          }

          @Override
          public String getName() {
            return name;
          }

          @Override
          public List<String> getRoles() {
            return newRoles;
          }
        };

        acl.addEntry(newEntry.getName(), newEntry.getAclLevel(), newEntry.getRoles(), flags);

        newEntries.add(newEntry);
      }

      Assertions.assertTrue(this.matchesAllEntries(newEntries, acl.getEntries()), "Didn't find all added ACL entries");

      Assertions.assertThrows(DominoException.class, (Executable) () -> acl.addEntry("__entry_1", level, newRoles, flags), "Did not throw exception when adding entry with existing name");

      Assertions.assertThrows(IllegalArgumentException.class, (Executable) () -> acl.addEntry("", level, newRoles, flags), "Did not throw exception when adding entry with empty name");

      Assertions.assertThrows(IllegalArgumentException.class, (Executable) () -> acl.addEntry(null, level, newRoles, flags), "Did not throw exception when adding entry with null name");

    });
  }

  @Test
  public void testReadAclEntries() throws Exception {
    this.withTempDb(database -> {
      final Acl acl = database.getACL();

      Assertions.assertTrue(acl.getEntries().size() > 0, "Read no ACL entries");
    });
  }

  @Test
  public void testReadAclEntryByName() throws Exception {
    this.withTempDb(database -> {
      final Acl acl = database.getACL();

      final List<AclEntry> entries = acl.getEntries();

      Assertions.assertTrue(entries.size() > 0, "Read no ACL entries");

      for (final AclEntry e : entries) {
        final AclEntry searchEntry = acl.getEntry(e.getName()).orElse(null);

        Assertions.assertNotNull(searchEntry, "Did not find ACL entry with name " + e.getName());
      }

      final String nonExistingName = "-__-_-__-";
      Assertions.assertNull(acl.getEntry(nonExistingName).orElse(null),
          "Should not be able to find entry with name " + nonExistingName);

      Assertions.assertThrows(IllegalArgumentException.class, (Executable) () -> acl.getEntry(""), "Did not throw an error when name was empty");

      Assertions.assertThrows(IllegalArgumentException.class, (Executable) () -> acl.getEntry(null), "Did not throw an error when name was null");

    });

  }

  @Test
  public void testRemoveAclEntries() throws Exception {
    this.withTempDb(database -> {
      final Acl acl = database.getACL();

      final AclLevel level = AclLevel.AUTHOR;
      final EnumSet<AclFlag> flags = EnumSet.of(AclFlag.PERSON, AclFlag.GROUP);

      final List<String> newRoles = new ArrayList<>();

      for (int j = 1; j < 4; j++) {
        newRoles.add("[__role_" + j + "]");

        acl.addRole(newRoles.get(newRoles.size() - 1));
      }

      Assertions.assertTrue(this.containsAll(newRoles, acl.getRoles()), "Didn't find all added ACL roles");

      final List<AclEntry> newEntries = new ArrayList<>();
      for (int i = 1; i < 10; i++) {
        final String name = "__entry_" + i;

        final AclEntry newEntry = new AclEntry() {
          @Override
          public Set<AclFlag> getAclFlags() {
            return flags;
          }

          @Override
          public AclLevel getAclLevel() {
            return level;
          }

          @Override
          public String getName() {
            return name;
          }

          @Override
          public List<String> getRoles() {
            return newRoles;
          }
        };

        acl.addEntry(newEntry.getName(), newEntry.getAclLevel(), newEntry.getRoles(), flags);

        newEntries.add(newEntry);
      }

      Assertions.assertTrue(this.matchesAllEntries(newEntries, acl.getEntries()), "Didn't find all added ACL entries");

      for (final AclEntry entry : newEntries) {
        acl.removeEntry(entry.getName());
      }

      Assertions.assertTrue(this.containsNone(newEntries, acl.getEntries()), "Still found some removed ACL entries");

      Assertions.assertThrows(DominoException.class, (Executable) () -> acl.removeEntry("__entry_1000"), "Did not throw exception when removing not existing entry");

      Assertions.assertThrows(IllegalArgumentException.class, (Executable) () -> acl.removeEntry(""), "Did not throw exception when removing entry with empty name");

      Assertions.assertThrows(IllegalArgumentException.class, (Executable) () -> acl.removeEntry(null), "Did not throw exception when removing entry with empty name");
    });
  }

  @Test
  public void testUpdateAclEntries() throws Exception {
    this.withTempDb(database -> {
      final Acl acl = database.getACL();

      final AclLevel level = AclLevel.AUTHOR;
      final EnumSet<AclFlag> flags = EnumSet.of(AclFlag.PERSON, AclFlag.GROUP);
      final AclLevel updateLevel = AclLevel.EDITOR;
      final EnumSet<AclFlag> updateFlags = EnumSet.of(AclFlag.PERSON);

      // setup roles
      final List<String> newRoles = new ArrayList<>();

      for (int j = 1; j < 4; j++) {
        newRoles.add("[__role_" + j + "]");

        acl.addRole(newRoles.get(newRoles.size() - 1));
      }

      Assertions.assertTrue(this.containsAll(newRoles, acl.getRoles()), "Didn't find all added ACL roles");

      final List<String> updateRoles = new ArrayList<>();

      for (int j = 1; j < 4; j++) {
        updateRoles.add("[__role_" + j + "-update]");

        acl.addRole(updateRoles.get(updateRoles.size() - 1));
      }

      Assertions.assertTrue(this.containsAll(updateRoles, acl.getRoles()), "Didn't find all added ACL roles");

      final List<AclEntry> newEntries = new ArrayList<>();
      for (int i = 1; i < 10; i++) {
        final String name = "__entry_" + i;

        final AclEntry newEntry = new AclEntry() {
          @Override
          public Set<AclFlag> getAclFlags() {
            return flags;
          }

          @Override
          public AclLevel getAclLevel() {
            return level;
          }

          @Override
          public String getName() {
            return name;
          }

          @Override
          public List<String> getRoles() {
            return newRoles;
          }
        };

        acl.updateEntry(newEntry.getName(), newEntry.getName(), newEntry.getAclLevel(), newEntry.getRoles(), flags);

        newEntries.add(newEntry);
      }

      // Acl#updateEntry will add non existing entries, hence we can check if they
      // appeared
      Assertions.assertTrue(this.matchesAllEntries(newEntries, acl.getEntries()), "Didn't find all added ACL entries");

      // now update all entries
      final List<AclEntry> updatedEntries = new ArrayList<>();

      for (final AclEntry oldEntry : newEntries) {
        final String name = oldEntry.getName() + "-updated";

        final AclEntry updatedEntry = new AclEntry() {
          @Override
          public Set<AclFlag> getAclFlags() {
            return updateFlags;
          }

          @Override
          public AclLevel getAclLevel() {
            return updateLevel;
          }

          @Override
          public String getName() {
            return name;
          }

          @Override
          public List<String> getRoles() {
            return updateRoles;
          }
        };

        acl.updateEntry(oldEntry.getName(), updatedEntry.getName(), updatedEntry.getAclLevel(), updatedEntry.getRoles(),
            updateFlags);

        updatedEntries.add(updatedEntry);
      }

      Assertions.assertTrue(this.matchesAllEntries(updatedEntries, acl.getEntries()), "Didn't find all updated ACL entries");

      Assertions.assertThrows(
          IllegalArgumentException.class,
          (Executable) () -> acl.updateEntry("__entry_1-updated", "", level, newRoles, flags),
          "Did not throw exception when updating entry to empty name");

      Assertions.assertDoesNotThrow(
          (Executable) () -> acl.updateEntry("__entry_1-updated", null, level, newRoles, flags),
          "Threw exception when updating entry to null name");

      Assertions.assertThrows(IllegalArgumentException.class, (Executable) () -> acl.updateEntry("", "xyz", level, newRoles, flags), "Did not throw exception when updating entry with empty name");

      Assertions.assertThrows(IllegalArgumentException.class, (Executable) () -> acl.updateEntry(null, "xyz", level, newRoles, flags), "Did not throw exception when updating entry with null name");
    });

  }
}
