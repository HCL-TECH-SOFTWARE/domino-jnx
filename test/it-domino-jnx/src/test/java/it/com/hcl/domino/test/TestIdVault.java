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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import com.hcl.domino.DominoClient;
import com.hcl.domino.admin.idvault.IdVault;
import com.hcl.domino.admin.idvault.IdVault.IdFlag;
import com.hcl.domino.admin.idvault.UserId;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Document.EncryptionMode;
import com.ibm.commons.util.StringUtil;

@SuppressWarnings("nls")
public class TestIdVault extends AbstractNotesRuntimeTest {

  @Test
  @EnabledIfEnvironmentVariable(named = "IDFILE_PATH", matches = ".+")
  @EnabledIfEnvironmentVariable(named = "IDFILE_PASSWORD", matches = ".+")
  public void testIDFilePath() throws Exception {
    final DominoClient client = this.getClient();
    final IdVault vault = client.getIdVault();

    final String idFilePathStr = System.getenv("IDFILE_PATH");
    final String idFilePassword = System.getenv("IDFILE_PASSWORD");

    final Path idFilePath = idFilePathStr == null ? null : Paths.get(idFilePathStr);

    // load local ID file
    vault.openUserIdFile(idFilePath, idFilePassword, id -> {
      final Set<IdFlag> flags = vault.getIDFlags(id);
      Assertions.assertNotNull(flags);

      // System.out.println(flags);

      final String idUsername = id.getUsername();
      Assertions.assertTrue(!StringUtil.isEmpty(idUsername));

      return null;
    });
  }

  @Test
  public void testIDUsage() throws Exception {
    final DominoClient client = this.getClient();

    // user active ID:
    final UserId userId = null;

    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.sign(userId, true);
      Assertions.assertTrue(doc.isSigned());

      final String signer = doc.getSigner();
      Assertions.assertEquals(client.getIDUserName(), signer);

      // save document to get a note id
      doc.save();

      Assertions.assertTrue(doc.getNoteID() != 0);

      // check that returned encrypted document has the same note id / UNID
      final Document docEncrypted = doc.copyAndEncrypt(userId, EnumSet.of(EncryptionMode.ENCRYPT_WITH_USER_PUBLIC_KEY));
      Assertions.assertTrue(docEncrypted.isEncrypted());

      Assertions.assertEquals(doc.getUNID(), docEncrypted.getUNID());
      Assertions.assertEquals(doc.getNoteID(), docEncrypted.getNoteID());

      docEncrypted.decrypt(userId);
      Assertions.assertTrue(!docEncrypted.isEncrypted());

      Assertions.assertTrue(docEncrypted.isSigned());
    });
  }

  @Test
  @EnabledIfEnvironmentVariable(named = "IDVAULT_USERNAME", matches = ".+")
  @EnabledIfEnvironmentVariable(named = "IDVAULT_PASSWORD", matches = ".+")
  @EnabledIfEnvironmentVariable(named = "IDVAULT_IDVAULTSERVER", matches = ".+")
  public void testIDVaultServer() throws Exception {
    final DominoClient client = this.getClient();
    final IdVault vault = client.getIdVault();

    // load ID from ID Vault and run some sign/encryption tests
    final String idVaultUserName = System.getenv("IDVAULT_USERNAME");
    final String idVaultPassword = System.getenv("IDVAULT_PASSWORD");
    final String idVaultServer = System.getenv("IDVAULT_IDVAULTSERVER");

    // fetch id from vault
    final UserId userId = vault.getUserIdFromVault(idVaultUserName, idVaultPassword, idVaultServer);

    Assertions.assertTrue(!StringUtil.isEmpty(userId.getUsername()));
    // System.out.println("Id user: "+userId.getUsername());

    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.sign(userId, true);
      Assertions.assertTrue(doc.isSigned());

      final String signer = doc.getSigner();
      Assertions.assertEquals(userId.getUsername(), signer);

      final Document docEncrypted = doc.copyAndEncrypt(userId, EnumSet.of(EncryptionMode.ENCRYPT_WITH_USER_PUBLIC_KEY));
      Assertions.assertTrue(docEncrypted.isEncrypted());

      docEncrypted.decrypt(userId);
      Assertions.assertTrue(docEncrypted.isSigned());
    });
  }

}
