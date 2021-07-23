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
package it.com.hcl.domino.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import com.hcl.domino.DominoClient;
import com.hcl.domino.admin.idvault.IdVault;
import com.hcl.domino.admin.idvault.IdVault.IDAccessCallback;
import com.hcl.domino.admin.idvault.IdVault.IdFlag;
import com.hcl.domino.admin.idvault.UserId;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Document.EncryptionMode;
import com.ibm.commons.util.StringUtil;

@SuppressWarnings("nls")
public class TestIdVault extends AbstractNotesRuntimeTest {

	@Test
	public void testIDUsage() throws Exception {
		DominoClient client = getClient();
		
		//user active ID:
		UserId userId = null;
		
		withTempDb((database) -> {
			Document doc = database.createDocument();
			doc.sign(userId, true);
			assertTrue(doc.isSigned());
			
			String signer = doc.getSigner();
			assertEquals(client.getIDUserName(), signer);
			
			//save document to get a note id
			doc.save();
			
			assertTrue(doc.getNoteID() != 0);
			
			//check that returned encrypted document has the same note id / UNID
			Document docEncrypted = doc.copyAndEncrypt(userId, EnumSet.of(EncryptionMode.ENCRYPT_WITH_USER_PUBLIC_KEY));
			assertTrue(docEncrypted.isEncrypted());
			
			assertEquals(doc.getUNID(), docEncrypted.getUNID());
			assertEquals(doc.getNoteID(), docEncrypted.getNoteID());
			
			docEncrypted.decrypt(userId);
			assertTrue(!docEncrypted.isEncrypted());
			
			assertTrue(docEncrypted.isSigned());
		});
	}
	
	@Test
	@EnabledIfEnvironmentVariable(named = "IDFILE_PATH", matches = ".+")
	@EnabledIfEnvironmentVariable(named = "IDFILE_PASSWORD", matches = ".+")
	public void testIDFilePath() throws Exception {
		DominoClient client = getClient();
		IdVault vault = client.getIdVault();
		
		String idFilePathStr = System.getenv("IDFILE_PATH");
		String idFilePassword = System.getenv("IDFILE_PASSWORD");

		Path idFilePath = idFilePathStr==null ? null : Paths.get(idFilePathStr);
		
		// load local ID file
		vault.openUserIdFile(idFilePath, idFilePassword, new IDAccessCallback<Object>() {

			@Override
			public Object accessId(UserId id) {
				Set<IdFlag> flags = vault.getIDFlags(id);
				assertNotNull(flags);
				
//					System.out.println(flags);

				String idUsername = id.getUsername();
				assertTrue(!StringUtil.isEmpty(idUsername));
				
				return null;
			}
		});
	}
	
	@Test
	@EnabledIfEnvironmentVariable(named = "IDVAULT_USERNAME", matches = ".+")
	@EnabledIfEnvironmentVariable(named = "IDVAULT_PASSWORD", matches = ".+")
	@EnabledIfEnvironmentVariable(named = "IDVAULT_IDVAULTSERVER", matches = ".+")
	public void testIDVaultServer() throws Exception {
		DominoClient client = getClient();
		IdVault vault = client.getIdVault();
		
		// load ID from ID Vault and run some sign/encryption tests
		String idVaultUserName = System.getenv("IDVAULT_USERNAME");
		String idVaultPassword = System.getenv("IDVAULT_PASSWORD");
		String idVaultServer = System.getenv("IDVAULT_IDVAULTSERVER");
		
			
		//fetch id from vault
		UserId userId = vault.getUserIdFromVault(idVaultUserName, idVaultPassword, idVaultServer);
		
		assertTrue(!StringUtil.isEmpty(userId.getUsername()));
//			System.out.println("Id user: "+userId.getUsername());
		
		withTempDb((database) -> {
			Document doc = database.createDocument();
			doc.sign(userId, true);
			assertTrue(doc.isSigned());
			
			String signer = doc.getSigner();
			assertEquals(userId.getUsername(), signer);
			
			Document docEncrypted = doc.copyAndEncrypt(userId, EnumSet.of(EncryptionMode.ENCRYPT_WITH_USER_PUBLIC_KEY));
			assertTrue(docEncrypted.isEncrypted());
			
			docEncrypted.decrypt(userId);
			assertTrue(docEncrypted.isSigned());
		});
	}

}
