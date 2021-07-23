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
package it.com.hcl.domino.test.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClient.Encryption;
import com.hcl.domino.DominoException;
import com.hcl.domino.UserNamesList;
import com.hcl.domino.crypt.DatabaseEncryptionState;
import com.hcl.domino.crypt.EncryptionStrength;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.DominoDateTime;
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
	public void testOpenNamesNsf() {
		DominoClient client = getClient();
		Database names = client.openDatabase("names.nsf");
		Optional<UserNamesList> namesList = names.getNamesList();
		assertTrue(!namesList.isPresent());
		
		assertFalse(StringUtil.isEmpty(names.getTitle()));
		assertFalse(StringUtil.isEmpty(names.getAbsoluteFilePath()));
		assertFalse(StringUtil.isEmpty(names.getRelativeFilePath()));
		assertFalse(StringUtil.isEmpty(names.getReplicaID()));
		
		Ref<DominoDateTime> retDataModified = new Ref<>();
		Ref<DominoDateTime> retNonDataModified = new Ref<>();
		names.getModifiedTime(retDataModified, retNonDataModified);
		assertNotNull(retDataModified.get());
		assertNotNull(retNonDataModified.get());
	}
	
	@Test
	public void testNamesNsfAcl() {
		DominoClient client = getClient();
		Database names = client.openDatabase("names.nsf");
		Acl acl = names.getACL();
		assertNotNull(acl);
	}
	
	@Test
	public void testCreateDelete() throws IOException {
		DominoClient client = getClient();
		Path tempDest = Files.createTempFile(getClass().getName(), ".nsf");
		Files.delete(tempDest);
		Database database = client.createDatabase(null, tempDest.toString(), false, false, Encryption.None);
		assertNotNull(database);
		database.close();
		client.deleteDatabase(null, tempDest.toString());
	}
	
	@Test
	public void testCreateDeleteWithDesignInit() throws IOException {
		DominoClient client = getClient();
		Path tempDest = Files.createTempFile(getClass().getName(), ".nsf");
		Files.delete(tempDest);
		Database database = client.createDatabase(null, tempDest.toString(), false, true, Encryption.None);
		assertNotNull(database);
		try {
			AtomicBoolean hasViews = new AtomicBoolean();

			database.forEachCollection((colInfo, loop) -> {
				hasViews.set(Boolean.TRUE);
			});
			assertTrue(hasViews.get());
		}
		finally {
			database.close();
			client.deleteDatabase(null, tempDest.toString());
		}
	}
	
	@Test
	public void testCreateDbFromTemplate() throws IOException {
		DominoClient client = getClient();
		Path tempDest = Files.createTempFile(getClass().getName(), ".nsf");
		Files.delete(tempDest);
		
		String sourceServerName = "";
		String sourceFilePath = "pernames.ntf";
		
		Database templateDb = client.openDatabase(sourceServerName, sourceFilePath);
		String templateDbReplicaId = templateDb.getReplicaID();
		
		Set<String> viewNamesInTemplate = templateDb
				.getAllCollections()
				.map((colInfo) -> {
					return colInfo.getTitle();
				})
				.collect(Collectors.toSet());

		String templateName = templateDb.getTemplateName();
		//use absolute filepath here, because the pernames.ntf is located in the
		//SharedDataDirectory location configured in the Notes.ini.
		//Unfortunately, NSFDbCreateAndCopyExtended which we need to use to
		//create the new DB from the template does not support resolving relative
		//database paths in this shared folder
		sourceFilePath = templateDb.getAbsoluteFilePath();
		templateDb.close();
		
		Database database = client.createDatabaseFromTemplate(sourceServerName, sourceFilePath,
				null, tempDest.toString(), Encryption.None);
		assertNotNull(database);
		
		try {
			assertNotEquals(templateDbReplicaId, database.getReplicaID());

			String designTemplateName = database.getDesignTemplateName();
			assertEquals(designTemplateName, templateName);
			
			//compare views between template and created DB
			Set<String> viewNamesInNewDb = database
					.getAllCollections()
					.map((colInfo) -> {
						return colInfo.getTitle();
					})
					.collect(Collectors.toSet());

			assertTrue(!viewNamesInNewDb.isEmpty());

			assertEquals(viewNamesInTemplate, viewNamesInNewDb);
		}
		finally {
			database.close();
			client.deleteDatabase(null, tempDest.toString());
		}
	}
	
	@Test
	public void testCreateDbReplica() throws Exception {
		DominoClient client = getClient();
		
		withResourceDxl("/dxl/testCreateDbReplica", testDb -> {
			String dbNamesReplicaId = testDb.getReplicaID();
			String testDbPath = testDb.getAbsoluteFilePath();
			testDb.close();
			
			Path tempDest = Files.createTempFile(getClass().getName(), ".nsf");
			Files.delete(tempDest);
			
			Database database = client.createDatabaseReplica("", testDbPath,
					"", tempDest.toString(), Encryption.None);
			assertNotNull(database);
			try {
				assertEquals(dbNamesReplicaId, database.getReplicaID());
				
				AtomicBoolean hasViews = new AtomicBoolean();

				database.forEachCollection((colInfo, loop) -> {
					hasViews.set(Boolean.TRUE);
					loop.stop();
				});
				assertTrue(hasViews.get());
			}
			finally {
				database.close();
				client.deleteDatabase(null, tempDest.toString());
			}
		});
		
	}
	
	@Test
	public void testCreateDbReplicaLocalNames() throws Exception {
		DominoClient client = getClient();
		
		Database testDb = client.openDatabase("names.nsf");
		String dbNamesReplicaId = testDb.getReplicaID();
		String testDbPath = testDb.getAbsoluteFilePath();
		testDb.close();
		
		Path tempDest = Files.createTempFile(getClass().getName(), ".nsf");
		Files.delete(tempDest);
		
		Database database = client.createDatabaseReplica("", testDbPath,
				"", tempDest.toString(), Encryption.None);
		assertNotNull(database);
		try {
			assertEquals(dbNamesReplicaId, database.getReplicaID());
			
			AtomicBoolean hasViews = new AtomicBoolean();

			database.forEachCollection((colInfo, loop) -> {
				hasViews.set(Boolean.TRUE);
				loop.stop();
			});
			assertTrue(hasViews.get());
		}
		finally {
			database.close();
			client.deleteDatabase(null, tempDest.toString());
		}
		
	}
	
	@Test
	public void testMultiOpen() throws Exception {
		DominoClient client = getClient();
		
		ExecutorService exec = Executors.newFixedThreadPool(50, client.getThreadFactory());
		for(int i = 0; i < 50; i++) {
			final int j = i;
			exec.submit(() -> {
				Database names = client.openDatabase("doclbs7.ntf"); // Likely to be in Shared
				names.queryDocuments().forEachDocument(0, Integer.MAX_VALUE, (doc, loop) -> {
					MimeWriter w = client.getMimeWriter();
					w.convertToMime(doc,
						w.createRichTextMimeConversionSettings()
							.setMessageContentEncoding(MessageContentEncoding.TEXT_HTML_WITH_IMAGES_ATTACHMENTS)
					);
					
					HtmlConversionResult html = client.getRichTextHtmlConverter()
						.render(doc)
						.option(HtmlConvertOption.DisablePassThruHTML, "1")
						.convert();
					
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					log.info(j + " - " + html);
				});
			});
		}
		exec.shutdown();
		exec.awaitTermination(10, TimeUnit.SECONDS);
	}
	
	@Test
	public void testMultiOpenNonexistent() throws Exception {
		DominoClient client = getClient();
		
		ExecutorService exec = Executors.newFixedThreadPool(50, client.getThreadFactory());
		for(int i = 0; i < 50; i++) {
			exec.submit(() -> {
				assertThrows(DominoException.class, () -> client.openDatabase("aaabbbbbcccccssssssddddd.nsf")); // Not too likely to exist
			});
		}
		exec.shutdown();
		exec.awaitTermination(10, TimeUnit.SECONDS);
	}
	
	@Test
	@EnabledIfEnvironmentVariable(named = DATABASE_LOCALENC_PATH, matches = ".+")
	public void testLocalEncryption() throws Exception {
		String localencpath = System.getenv(DATABASE_LOCALENC_PATH);
		DominoClient client = getClient();
		
		Database localenc = client.openDatabase(localencpath);
		assertNotNull(localenc);
		assertTrue(localenc.isLocallyEncrypted());
		
		Database.EncryptionInfo info = localenc.getLocalEncryptionInfo();
		assertNotNull(info);
		assertEquals(DatabaseEncryptionState.ENCRYPTED, info.getState().orElse(null));
		assertEquals(EncryptionStrength.AES128, info.getStrength().orElse(null));
	}
	
	@Test
	public void testEffectiveAccess() throws Exception {
		DominoClient client = getClient();
		
		Database names = client.openDatabase("names.nsf");
		assertNotNull(names);
		
		Database.AccessInfo info = names.getEffectiveAccessInfo();
		assertNotNull(info);
		assertNotNull(info.getAclLevel());
		assertNotNull(info.getAclFlags());
	}
}
