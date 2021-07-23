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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	@Test
	public void testReadAclEntries() throws Exception {
		withTempDb((database) -> {
			Acl acl=database.getACL();
			
			assertTrue(acl.getEntries().size()>0, "Read no ACL entries");
		});
	}
	
	@Test
	public void testReadAclEntryByName() throws Exception {
		withTempDb((database) -> {
			Acl acl=database.getACL();
			
			List<AclEntry> entries=acl.getEntries();
			
			assertTrue(entries.size()>0, "Read no ACL entries");
			
			for (AclEntry e:entries) {
				AclEntry searchEntry=acl.getEntry(e.getName()).orElse(null);
				
				assertNotNull(searchEntry, "Did not find ACL entry with name " + e.getName());
			}
			
			String nonExistingName="-__-_-__-";
			assertNull(acl.getEntry(nonExistingName).orElse(null), "Should not be able to find entry with name " +nonExistingName);
		
			assertThrows(IllegalArgumentException.class, new Executable() {
				@Override
				public void execute() throws Throwable {
					acl.getEntry("");
				}
			}, "Did not throw an error when name was empty");
			
			assertThrows(IllegalArgumentException.class, new Executable() {
				@Override
				public void execute() throws Throwable {
					acl.getEntry(null);
				}
			}, "Did not throw an error when name was null");

		});
		
	}
	
	@Test
	public void testAddAclEntries() throws Exception {
		withTempDb((dbFakenames) -> {
			Acl acl=dbFakenames.getACL();
			
			final AclLevel level=AclLevel.AUTHOR;;
			final EnumSet<AclFlag> flags=EnumSet.of(AclFlag.PERSON, AclFlag.GROUP);
			
			final List<String> newRoles=new ArrayList<String>();

			for (int j=1;j<4;j++) {
				newRoles.add("[__role_" + j + "]");
				
				acl.addRole(newRoles.get(newRoles.size()-1));
			}
			
			assertTrue(containsAll(newRoles, acl.getRoles()), "Didn't find all added ACL roles");
			
			final List<AclEntry> newEntries=new ArrayList<AclEntry>();
			for (int i=1;i<10;i++) {
				final String name="__entry_"+i;
				
				AclEntry newEntry=new AclEntry() {
					@Override
					public String getName() {
						return name;
					}

					@Override
					public List<String> getRoles() {
						return newRoles;
					}

					@Override
					public AclLevel getAclLevel() {
						return level;
					}

					@Override
					public Set<AclFlag> getAclFlags() {
						return flags;
					}
				};
				
				acl.addEntry(newEntry.getName(), newEntry.getAclLevel(), newEntry.getRoles(), flags);

				newEntries.add(newEntry);
			}
			
			assertTrue(matchesAllEntries(newEntries, acl.getEntries()), "Didn't find all added ACL entries");
		
			assertThrows(DominoException.class, new Executable() {
				@Override
				public void execute() throws Throwable {
					acl.addEntry("__entry_1", level, newRoles, flags);
				}
			}, "Did not throw exception when adding entry with existing name");
			
			assertThrows(IllegalArgumentException.class, new Executable() {
				@Override
				public void execute() throws Throwable {
					acl.addEntry("", level, newRoles, flags);
				}
			}, "Did not throw exception when adding entry with empty name");
			
			assertThrows(IllegalArgumentException.class, new Executable() {
				@Override
				public void execute() throws Throwable {
					acl.addEntry(null, level, newRoles, flags);
				}
			}, "Did not throw exception when adding entry with null name");
			
		});
	}
	
	@Test
	public void testUpdateAclEntries() throws Exception {
		withTempDb((database) -> {
			Acl acl=database.getACL();
			
			final AclLevel level=AclLevel.AUTHOR;
			final EnumSet<AclFlag> flags=EnumSet.of(AclFlag.PERSON, AclFlag.GROUP);
			final AclLevel updateLevel=AclLevel.EDITOR;
			final EnumSet<AclFlag> updateFlags=EnumSet.of(AclFlag.PERSON);
			
			// setup roles
			final List<String> newRoles=new ArrayList<String>();

			for (int j=1;j<4;j++) {
				newRoles.add("[__role_" + j + "]");
				
				acl.addRole(newRoles.get(newRoles.size()-1));
			}
			
			assertTrue(containsAll(newRoles, acl.getRoles()), "Didn't find all added ACL roles");
			
			final List<String> updateRoles=new ArrayList<String>();

			for (int j=1;j<4;j++) {
				updateRoles.add("[__role_" + j + "-update]");
				
				acl.addRole(updateRoles.get(updateRoles.size()-1));
			}
			
			assertTrue(containsAll(updateRoles, acl.getRoles()), "Didn't find all added ACL roles");
			
			final List<AclEntry> newEntries=new ArrayList<AclEntry>();
			for (int i=1;i<10;i++) {
				final String name="__entry_"+i;
				
				AclEntry newEntry=new AclEntry() {
					@Override
					public String getName() {
						return name;
					}

					@Override
					public List<String> getRoles() {
						return newRoles;
					}

					@Override
					public AclLevel getAclLevel() {
						return level;
					}

					@Override
					public Set<AclFlag> getAclFlags() {
						return flags;
					}
				};
				
				acl.updateEntry(newEntry.getName(), newEntry.getName(), newEntry.getAclLevel(), newEntry.getRoles(), flags);

				newEntries.add(newEntry);
			}
			
			// Acl#updateEntry will add non existing entries, hence we can check if they appeared
			assertTrue(matchesAllEntries(newEntries, acl.getEntries()), "Didn't find all added ACL entries");
			
			// now update all entries
			final List<AclEntry> updatedEntries=new ArrayList<AclEntry>();
			
			for (AclEntry oldEntry:newEntries) {
				final String name=oldEntry.getName() + "-updated";
				
				AclEntry updatedEntry=new AclEntry() {
					@Override
					public String getName() {
						return name;
					}

					@Override
					public List<String> getRoles() {
						return updateRoles;
					}

					@Override
					public AclLevel getAclLevel() {
						return updateLevel;
					}

					@Override
					public Set<AclFlag> getAclFlags() {
						return updateFlags;
					}
				};
				
				acl.updateEntry(oldEntry.getName(), updatedEntry.getName(), updatedEntry.getAclLevel(), updatedEntry.getRoles(), updateFlags);
				
				updatedEntries.add(updatedEntry);
			}
			
			assertTrue(matchesAllEntries(updatedEntries, acl.getEntries()), "Didn't find all updated ACL entries");

			assertThrows(
				IllegalArgumentException.class,
				(Executable) () -> acl.updateEntry("__entry_1-updated", "", level, newRoles, flags),
				"Did not throw exception when updating entry to empty name"
			);
			
			assertDoesNotThrow(
				(Executable) () -> acl.updateEntry("__entry_1-updated", null, level, newRoles, flags),
				"Threw exception when updating entry to null name"
			);
			
			assertThrows(IllegalArgumentException.class, new Executable() {
				@Override
				public void execute() throws Throwable {
					acl.updateEntry("", "xyz", level, newRoles, flags);
				}
			}, "Did not throw exception when updating entry with empty name");
			
			assertThrows(IllegalArgumentException.class, new Executable() {
				@Override
				public void execute() throws Throwable {
					acl.updateEntry(null, "xyz", level, newRoles, flags);
				}
			}, "Did not throw exception when updating entry with null name");			
		});
		

	}
	
	@Test
	public void testRemoveAclEntries() throws Exception {
		withTempDb((database) -> {
			Acl acl=database.getACL();
			
			final AclLevel level=AclLevel.AUTHOR;
			final EnumSet<AclFlag> flags=EnumSet.of(AclFlag.PERSON, AclFlag.GROUP);
			
			final List<String> newRoles=new ArrayList<String>();

			for (int j=1;j<4;j++) {
				newRoles.add("[__role_" + j + "]");
				
				acl.addRole(newRoles.get(newRoles.size()-1));
			}
			
			assertTrue(containsAll(newRoles, acl.getRoles()), "Didn't find all added ACL roles");
			
			final List<AclEntry> newEntries=new ArrayList<AclEntry>();
			for (int i=1;i<10;i++) {
				final String name="__entry_"+i;
				
				AclEntry newEntry=new AclEntry() {
					@Override
					public String getName() {
						return name;
					}

					@Override
					public List<String> getRoles() {
						return newRoles;
					}

					@Override
					public AclLevel getAclLevel() {
						return level;
					}

					@Override
					public Set<AclFlag> getAclFlags() {
						return flags;
					}
				};
				
				acl.addEntry(newEntry.getName(), newEntry.getAclLevel(), newEntry.getRoles(), flags);

				newEntries.add(newEntry);
			}
			
			assertTrue(matchesAllEntries(newEntries, acl.getEntries()), "Didn't find all added ACL entries");
		
			for (AclEntry entry:newEntries) {
				acl.removeEntry(entry.getName());
			}
			
			assertTrue(containsNone(newEntries, acl.getEntries()), "Still found some removed ACL entries");

			assertThrows(DominoException.class, new Executable() {
				@Override
				public void execute() throws Throwable {
					acl.removeEntry("__entry_1000");
				}
			}, "Did not throw exception when removing not existing entry");
			
			assertThrows(IllegalArgumentException.class, new Executable() {
				@Override
				public void execute() throws Throwable {
					acl.removeEntry("");
				}
			}, "Did not throw exception when removing entry with empty name");
			
			assertThrows(IllegalArgumentException.class, new Executable() {
				@Override
				public void execute() throws Throwable {
					acl.removeEntry(null);
				}
			}, "Did not throw exception when removing entry with empty name");			
		});
	}
	
	
	private boolean containsAll(List<String> expected, List<String> actual) {
		if (expected.size()>actual.size())
			return false;
		
		Set<String> allValues=new HashSet<String>(actual);
		
		for (int i=0;i<expected.size();i++) {
			if (!allValues.contains(expected.get(i))) {
				return false;
			}
		}
		return true;
	}
	
	
	private boolean matchesAllEntries(List<AclEntry> expected, List<AclEntry> actual) {
		if (expected.size()>actual.size())
			return false;
		
		Map<String, AclEntry> allByName=new HashMap<String, AclEntry>();
		for (AclEntry e:actual) {
			allByName.put(e.getName(), e);
		}
		
		for (AclEntry expectedEntry:expected) {
			AclEntry actualEntry=allByName.get(expectedEntry.getName());
			
			if (actualEntry==null) {
				return false;
			}
			
			assertEquals(expectedEntry.getAclLevel(), actualEntry.getAclLevel(), "Entry " + actualEntry.getName() + " does not have the expected level");

			assertTrue(containsAll(expectedEntry.getRoles(), actualEntry.getRoles()), "Entry " + actualEntry.getName() + " does not contain all expected roles " + expectedEntry.getRoles().toString());

			for (AclFlag expectedFlag:expectedEntry.getAclFlags()) {
				assertTrue(actualEntry.getAclFlags().contains(expectedFlag), "Entry " + actualEntry.getName() + " does not contain expected flag " + expectedFlag.name());
			}
		}
		return true;
	}
	
	private boolean containsNone(List<AclEntry> expected, List<AclEntry> actual) {
		Map<String, AclEntry> allByName=new HashMap<String, AclEntry>();
		for (AclEntry e:actual) {
			allByName.put(e.getName(), e);
		}
		
		for (int i=0;i<expected.size();i++) {
			if (allByName.containsKey(expected.get(i).getName())) {
				return false;
			}
		}
		return true;
	}
}
