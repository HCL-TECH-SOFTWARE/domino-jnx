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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClient.Encryption;
import com.hcl.domino.data.Database;
import com.hcl.domino.security.Acl;
import com.hcl.domino.security.AclAccess;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestAcl extends AbstractNotesRuntimeTest {

	@Test
	public void testUniformAccess() throws Exception {
		withTempDb((database) -> {
			Acl acl=database.getACL();
			
			boolean isUniformAccess=acl.isUniformAccess();
			
			acl.setUniformAccess(!isUniformAccess);
			
			assertEquals(!isUniformAccess, acl.isUniformAccess(), "ACL uniform access has not changed");
			assertNotEquals(isUniformAccess, acl.isUniformAccess(), "ACL uniform access did not toggle");
		});
	}
	
	@Test
	public void testReadAdminServer() throws Exception {
		withTempDb((database) -> {
			Acl acl=database.getACL();
			
			assertNotNull(acl.getAdminServer(), "Admin server was null");
		});
	}
	
	@Test
	public void testLookupAccess() throws Exception {
		withTempDb((database) -> {
			DominoClient client = getClient();
			
			Acl acl=database.getACL();
			
			String name=client.getEffectiveUserName();
			
			AclAccess entry=acl.lookupAccess(name);
			
			assertNotNull(entry, "Access level could not be determined");
		});
	}
	
	@Test
	public void testSave() throws Exception {
		withTempDb((tempDb) -> {
			String tempDbServer = tempDb.getServer();
			String tempDbFilePath = tempDb.getAbsoluteFilePath();
			
			DominoClient client = getClient();
			Acl acl=tempDb.getACL();
			
			// to verify if saving works, we will add some roles, save and close the database
			// then we will re-open it and verify the level
			List<String> newRoles=new ArrayList<String>();
			for (int i=1;i<10;i++) {
				newRoles.add("[role_"+i+"]");
			
				acl.addRole(newRoles.get(newRoles.size()-1));
			}
			
			assertTrue(containsAll(newRoles, acl.getRoles()), "Didn't find all added ACL roles");

			// now this worked -> save, close, re-open
			acl.save();
			
			//recycle all and recreate Domino client
			client = reloadClient();
			
			Path tempDbPath2 = Files.createTempFile(getClass().getName(), ".nsf"); //$NON-NLS-1$
			Files.delete(tempDbPath2);
			
			Database tempDb2 = client.createDatabaseFromTemplate(tempDbServer, tempDbFilePath,
					"", tempDbPath2.toString(), Encryption.None);
			
			try {
				acl=tempDb2.getACL();
				
				assertTrue(containsAll(newRoles, acl.getRoles()), "Didn't find all added ACL roles");
			}
			finally {
				tempDb2.close();
				try {
					client.deleteDatabase(null, tempDbPath2.toString());
				} catch(Throwable t) {
					System.err.println("Unable to delete database " + tempDb2 + ": " + t);
				}
			}

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
	
}
