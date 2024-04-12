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
package it.com.hcl.domino.test.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClient.Encryption;
import com.hcl.domino.DominoException;
import com.hcl.domino.UserNamesList;
import com.hcl.domino.crypt.DatabaseEncryptionState;
import com.hcl.domino.data.CompactMode;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.Database.EncryptionInfo;
import com.hcl.domino.exception.CompactionRequiredException;
import com.hcl.domino.data.DominoCollectionInfo;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.html.HtmlConversionResult;
import com.hcl.domino.html.HtmlConvertOption;
import com.hcl.domino.mime.MimeWriter;
import com.hcl.domino.mime.RichTextMimeConversionSettings.MessageContentEncoding;
import com.hcl.domino.misc.Ref;
import com.hcl.domino.security.Acl;
import com.ibm.commons.util.StringUtil;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDatabase extends AbstractNotesRuntimeTest {
  private static final Logger log = Logger.getLogger(TestDatabase.class.getName());

  public static final String DATABASE_LOCALENC_PATH = "DATABASE_LOCALENC_PATH";

  @Test
  public void testCreateDbFromTemplate() throws IOException {
    final DominoClient client = this.getClient();
    final Path tempDest = Files.createTempFile(this.getClass().getName(), ".nsf");
    Files.delete(tempDest);

    final String sourceServerName = "";
    String sourceFilePath = "pernames.ntf";

    final Database templateDb = client.openDatabase(sourceServerName, sourceFilePath);
    final String templateDbReplicaId = templateDb.getReplicaID();

    final Set<String> viewNamesInTemplate = templateDb
        .getAllCollections()
        .map(DominoCollectionInfo::getTitle)
        .collect(Collectors.toSet());

    final String templateName = templateDb.getTemplateName();
    // use absolute filepath here, because the pernames.ntf is located in the
    // SharedDataDirectory location configured in the Notes.ini.
    // Unfortunately, NSFDbCreateAndCopyExtended which we need to use to
    // create the new DB from the template does not support resolving relative
    // database paths in this shared folder
    sourceFilePath = templateDb.getAbsoluteFilePath();
    templateDb.close();

    final Database database = client.createDatabaseFromTemplate(sourceServerName, sourceFilePath,
        null, tempDest.toString(), Encryption.None);
    Assertions.assertNotNull(database);

    try {
      Assertions.assertNotEquals(templateDbReplicaId, database.getReplicaID());

      final String designTemplateName = database.getDesignTemplateName();
      Assertions.assertEquals(designTemplateName, templateName);

      // compare views between template and created DB
      final Set<String> viewNamesInNewDb = database
          .getAllCollections()
          .map(DominoCollectionInfo::getTitle)
          .collect(Collectors.toSet());

      Assertions.assertTrue(!viewNamesInNewDb.isEmpty());

      Assertions.assertEquals(viewNamesInTemplate, viewNamesInNewDb);
    } finally {
      database.close();
      client.deleteDatabase(null, tempDest.toString());
    }
  }

  @Test
  public void testCreateDbReplica() throws Exception {
    final DominoClient client = this.getClient();

    this.withResourceDxl("/dxl/testCreateDbReplica", testDb -> {
      final String dbNamesReplicaId = testDb.getReplicaID();
      final String testDbPath = testDb.getAbsoluteFilePath();
      testDb.close();

      final Path tempDest = Files.createTempFile(this.getClass().getName(), ".nsf");
      Files.delete(tempDest);

      final Database database = client.createDatabaseReplica("", testDbPath,
          "", tempDest.toString(), Encryption.None);
      Assertions.assertNotNull(database);
      try {
        Assertions.assertEquals(dbNamesReplicaId, database.getReplicaID());

        final AtomicBoolean hasViews = new AtomicBoolean();

        database.forEachCollection((colInfo, loop) -> {
          hasViews.set(Boolean.TRUE);
          loop.stop();
        });
        Assertions.assertTrue(hasViews.get());
      } finally {
        database.close();
        client.deleteDatabase(null, tempDest.toString());
      }
    });

  }

  @Test
  public void testCreateDbReplicaLocalNames() throws Exception {
    final DominoClient client = this.getClient();

    final Database testDb = client.openDatabase("names.nsf");
    final String dbNamesReplicaId = testDb.getReplicaID();
    final String testDbPath = testDb.getAbsoluteFilePath();
    testDb.close();

    final Path tempDest = Files.createTempFile(this.getClass().getName(), ".nsf");
    Files.delete(tempDest);

    final Database database = client.createDatabaseReplica("", testDbPath,
        "", tempDest.toString(), Encryption.None);
    Assertions.assertNotNull(database);
    try {
      Assertions.assertEquals(dbNamesReplicaId, database.getReplicaID());

      final AtomicBoolean hasViews = new AtomicBoolean();

      database.forEachCollection((colInfo, loop) -> {
        hasViews.set(Boolean.TRUE);
        loop.stop();
      });
      Assertions.assertTrue(hasViews.get());
    } finally {
      database.close();
      client.deleteDatabase(null, tempDest.toString());
    }

  }

  @Test
  public void testCreateDelete() throws IOException {
    final DominoClient client = this.getClient();
    final Path tempDest = Files.createTempFile(this.getClass().getName(), ".nsf");
    Files.delete(tempDest);
    final Database database = client.createDatabase(null, tempDest.toString(), false, false, Encryption.None);
    Assertions.assertNotNull(database);
    database.close();
    client.deleteDatabase(null, tempDest.toString());
  }

  @Test
  public void testCreateDeleteWithDesignInit() throws IOException {
    final DominoClient client = this.getClient();
    final Path tempDest = Files.createTempFile(this.getClass().getName(), ".nsf");
    Files.delete(tempDest);
    final Database database = client.createDatabase(null, tempDest.toString(), false, true, Encryption.None);
    Assertions.assertNotNull(database);
    try {
      final AtomicBoolean hasViews = new AtomicBoolean();

      database.forEachCollection((colInfo, loop) -> {
        hasViews.set(Boolean.TRUE);
      });
      Assertions.assertTrue(hasViews.get());
    } finally {
      database.close();
      client.deleteDatabase(null, tempDest.toString());
    }
  }

  @Test
  public void testEffectiveAccess() throws Exception {
    final DominoClient client = this.getClient();

    final Database names = client.openDatabase("names.nsf");
    Assertions.assertNotNull(names);

    final Database.AccessInfo info = names.getEffectiveAccessInfo();
    Assertions.assertNotNull(info);
    Assertions.assertNotNull(info.getAclLevel());
    Assertions.assertNotNull(info.getAclFlags());
  }

  @Test
  @EnabledIfEnvironmentVariable(named = TestDatabase.DATABASE_LOCALENC_PATH, matches = ".+")
  public void testLocalEncryption() throws Exception {
    final String localencpath = System.getenv(TestDatabase.DATABASE_LOCALENC_PATH);
    final DominoClient client = this.getClient();

    final Database localenc = client.openDatabase(localencpath);
    Assertions.assertNotNull(localenc);
    Assertions.assertTrue(localenc.isLocallyEncrypted());

    final Database.EncryptionInfo info = localenc.getLocalEncryptionInfo();
    Assertions.assertNotNull(info);
    Assertions.assertEquals(DatabaseEncryptionState.ENCRYPTED, info.getState().orElse(null));
    Assertions.assertEquals(Encryption.AES128, info.getStrength().orElse(null));
  }

  @Test
  public void testEncryptDecryptNSF() throws Exception {
    final Encryption newEncryption = Encryption.AES128;
    
    final DominoClient client = this.getClient();
    Database db = AbstractNotesRuntimeTest.createTempDb(client);
    final String tempDbPath = db.getAbsoluteFilePath();
    
    Set<CompactMode> compactMode = EnumSet.of(CompactMode.IGNORE_ERRORS, CompactMode.FORCE);

    try {
      {
        //first we mark the database for encryption, compact it
        //and make sure it got encrypted

        EncryptionInfo encInfo = db.getLocalEncryptionInfo();

        assertNotNull(encInfo);
        assertEquals(DatabaseEncryptionState.UNENCRYPTED, encInfo.getState().orElse(null));
        assertEquals(Encryption.None, encInfo.getStrength().orElse(null));

        Database fDb = db;
        assertThrows(CompactionRequiredException.class, () -> {
          fDb.setLocalEncryptionInfo(newEncryption, null);
        });

        EncryptionInfo encInfoAfterSet = db.getLocalEncryptionInfo();

        assertEquals(DatabaseEncryptionState.PENDING_ENCRYPTION, encInfoAfterSet.getState().orElse(null));
        assertEquals(newEncryption, encInfoAfterSet.getStrength().orElse(null));

        //close db
        db.close();

        client.compact(tempDbPath, compactMode);

        db = client.openDatabase(tempDbPath);

        EncryptionInfo encInfoAfterCompact = db.getLocalEncryptionInfo();

        assertEquals(DatabaseEncryptionState.ENCRYPTED, encInfoAfterCompact.getState().orElse(null));
        assertEquals(newEncryption, encInfoAfterCompact.getStrength().orElse(null));
      }

      {
        //next we mark the database for decryption
        //and check if this works
        Database fDb = db;
        assertThrows(CompactionRequiredException.class, () -> {
          fDb.setLocalEncryptionInfo(Encryption.None, null);
        });

        EncryptionInfo encInfoAfterReset = db.getLocalEncryptionInfo();

        assertEquals(DatabaseEncryptionState.PENDING_DECRYPTION, encInfoAfterReset.getState().orElse(null));
        assertEquals(newEncryption, encInfoAfterReset.getStrength().orElse(null));

        db.close();

        client.compact(tempDbPath, compactMode);

        db = client.openDatabase(tempDbPath);

        EncryptionInfo encInfoAfterCompact2 = db.getLocalEncryptionInfo();

        assertEquals(DatabaseEncryptionState.UNENCRYPTED, encInfoAfterCompact2.getState().orElse(null));
        assertEquals(Encryption.None, encInfoAfterCompact2.getStrength().orElse(null));
      }

      
    } finally {
      db.close();
      try {
        client.deleteDatabase(null, tempDbPath);
      } catch (final Throwable t) {
        System.err.println("Unable to delete database " + tempDbPath + ": " + t);
      }
    }
    
  }
  
  @Test
  public void testMultiOpen() throws Exception {
    final DominoClient client = this.getClient();

    final ExecutorService exec = Executors.newFixedThreadPool(50, client.getThreadFactory());
    for (int i = 0; i < 50; i++) {
      final int j = i;
      exec.submit(() -> {
        final Database names = client.openDatabase("doclbs7.ntf"); // Likely to be in Shared
        names.queryDocuments().forEachDocument(0, Integer.MAX_VALUE, (doc, loop) -> {
          final MimeWriter w = client.getMimeWriter();
          w.convertToMime(doc,
              w.createRichTextMimeConversionSettings()
                  .setMessageContentEncoding(MessageContentEncoding.TEXT_HTML_WITH_IMAGES_ATTACHMENTS));

          final HtmlConversionResult html = client.getRichTextHtmlConverter()
              .render(doc)
              .option(HtmlConvertOption.DisablePassThruHTML, "1")
              .convert();

          try {
            Thread.sleep(5000);
          } catch (final InterruptedException e) {
            e.printStackTrace();
          }

          TestDatabase.log.info(j + " - " + html);
        });
      });
    }
    exec.shutdown();
    exec.awaitTermination(10, TimeUnit.SECONDS);
  }

  @Test
  public void testMultiOpenNonexistent() throws Exception {
    final DominoClient client = this.getClient();

    final ExecutorService exec = Executors.newFixedThreadPool(50, client.getThreadFactory());
    for (int i = 0; i < 50; i++) {
      exec.submit(() -> {
        Assertions.assertThrows(DominoException.class, () -> client.openDatabase("aaabbbbbcccccssssssddddd.nsf")); // Not too likely
                                                                                                                   // to exist
      });
    }
    exec.shutdown();
    exec.awaitTermination(10, TimeUnit.SECONDS);
  }

  @Test
  public void testNamesNsfAcl() {
    final DominoClient client = this.getClient();
    final Database names = client.openDatabase("names.nsf");
    final Acl acl = names.getACL();
    Assertions.assertNotNull(acl);
  }

  @Test
  public void testOpenNamesNsf() {
    final DominoClient client = this.getClient();
    final Database names = client.openDatabase("names.nsf");
    final Optional<UserNamesList> namesList = names.getNamesList();
    Assertions.assertTrue(!namesList.isPresent());

    Assertions.assertFalse(StringUtil.isEmpty(names.getTitle()));
    Assertions.assertFalse(StringUtil.isEmpty(names.getAbsoluteFilePath()));
    Assertions.assertFalse(StringUtil.isEmpty(names.getRelativeFilePath()));
    Assertions.assertFalse(StringUtil.isEmpty(names.getReplicaID()));

    final Ref<DominoDateTime> retDataModified = new Ref<>();
    final Ref<DominoDateTime> retNonDataModified = new Ref<>();
    names.getModifiedTime(retDataModified, retNonDataModified);
    Assertions.assertNotNull(retDataModified.get());
    Assertions.assertNotNull(retNonDataModified.get());
  }
  
  @Test
  public void testCreated() throws Exception {
    withTempDb(database -> {
      DominoDateTime created = database.getCreated();
      Assertions.assertNotNull(created);
      // Make sure it seems reasonable enough
      Assertions.assertTrue(Instant.from(created).isAfter(Instant.now().minus(10, ChronoUnit.SECONDS)));
    });
  }
  
  @Test
  public void testGetAllUnidsRepeatedly() throws Exception {
    withTempDb(database -> {
      Set<String> expected = new HashSet<>();
      for(int i = 0; i < 10; i++) {
        Document doc = database.createDocument();
        doc.save();
        expected.add(doc.getUNID());
      }
      
      for(int i = 0; i < 100; i++) {
        IDTable ids = database.getModifiedNoteIds(EnumSet.of(DocumentClass.DATA), null, true);
        Set<String> unids = ids.stream().map(database::toUNID).collect(Collectors.toCollection(HashSet::new));
        assertEquals(expected, unids);
      }
    });
  }
}
