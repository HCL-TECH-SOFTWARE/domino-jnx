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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoException;
import com.hcl.domino.data.Database;
import com.hcl.domino.security.Acl;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestAclRoles extends AbstractNotesRuntimeTest {

	@Test
	public void testReadAclRoles() throws IOException {
		DominoClient client = getClient();

		Database dbNames = client.openDatabase("", "names.nsf");
		
		Acl acl=dbNames.getACL();
		
		assertTrue(acl.getRoles().size()>0, "Read no ACL roles");
	}
	
	@Test
	public void testAddLongRolenames() throws Exception {
		withTempDb((database) -> {
			Acl acl=database.getACL();
			assertThrows(IllegalArgumentException.class, () -> {
				acl.addRole("1234567890123456");
			});
		});
	}
	
	@Test
	public void testAddAclRoles() throws Exception {
		withTempDb((database) -> {
			Acl acl=database.getACL();
			
			List<String> newRoles=new ArrayList<String>();
			for (int i=1;i<10;i++) {
				newRoles.add("[role_"+i+"]");
			
				acl.addRole(newRoles.get(newRoles.size()-1));
			}
			
			assertTrue(containsAll(newRoles, acl.getRoles()), "Didn't find all added ACL roles");
		});
	}
	
	@Test
	public void renameAddAclRole() throws Exception {
		withTempDb((database) -> {
			Acl acl=database.getACL();
			
			// verify existing role
			String role1="[role_a]";
			final String role3="[role_c]";
			
			acl.addRole(role1);
			acl.addRole(role3);
			
			assertTrue(containsAll(Arrays.asList(new String[] {role1, role3}), acl.getRoles()), "Didn't find added ACL roles");

			String role2="[role_b]";
			
			acl.renameRole(role1, role2);
			
			assertTrue(containsAll(Arrays.asList(new String[] {role2}), acl.getRoles()), "Didn't find renamed ACL role");
			assertTrue(containsNone(Arrays.asList(new String[] {role1}), acl.getRoles()), "Still find renamed ACL role");
			
			
			// verify exception for not existing old role, existing new roles and other illegal arguments
			assertThrows(DominoException.class, new Executable() {
				@Override
				public void execute() throws Throwable {
					acl.renameRole("-__-_-__-", "xyz_345");
				}
			}, "Did not throw an error when old role does not exist");
			
			assertThrows(DominoException.class, new Executable() {
				@Override
				public void execute() throws Throwable {
					acl.renameRole("role_b", role3);
				}
			}, "Did not throw an error when new role already exists");
			
			assertThrows(IllegalArgumentException.class, new Executable() {
				@Override
				public void execute() throws Throwable {
					acl.renameRole(null, "x");
				}
			}, "Did not throw an error when old role was null");
			
			assertThrows(IllegalArgumentException.class, new Executable() {
				@Override
				public void execute() throws Throwable {
					acl.renameRole("", "x");
				}
			}, "Did not throw an error when old role was empty");
			
			assertThrows(IllegalArgumentException.class, new Executable() {
				@Override
				public void execute() throws Throwable {
					acl.renameRole("x", null);
				}
			}, "Did not throw an error when new role was null");
			
			assertThrows(IllegalArgumentException.class, new Executable() {
				@Override
				public void execute() throws Throwable {
					acl.renameRole("a", "");
				}
			}, "Did not throw an error when new role was empty");
			
		});
		
	}
	
	@Test
	public void testRemoveAclRoles() throws Exception {
		withTempDb((database) -> {
			Acl acl=database.getACL();
			
			List<String> newRoles=new ArrayList<String>();
			for (int i=1;i<10;i++) {
				newRoles.add("[role_"+i+"]");
			
				acl.addRole(newRoles.get(newRoles.size()-1));
			}
			
			assertTrue(containsAll(newRoles, acl.getRoles()), "Didn't find all added ACL roles");
			
			for (String role:newRoles) {
				acl.removeRole(role);
			}
			
			assertTrue(containsNone(newRoles, acl.getRoles()), "Still found some of the removed ACL roles");
			
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
	
	
	private boolean containsNone(List<String> expected, List<String> actual) {
		Set<String> allValues=new HashSet<String>(actual);
		
		for (int i=0;i<expected.size();i++) {
			if (allValues.contains(expected.get(i))) {
				return false;
			}
		}
		return true;
	}
}
