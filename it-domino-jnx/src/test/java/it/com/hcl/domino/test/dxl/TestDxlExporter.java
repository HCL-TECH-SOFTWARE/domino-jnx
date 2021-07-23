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
package it.com.hcl.domino.test.dxl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.design.DesignAgent.AgentLanguage;
import com.hcl.domino.dxl.DxlExporter;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;
import it.com.hcl.domino.test.TestValidateCredentials;

@SuppressWarnings("nls")
public class TestDxlExporter extends AbstractNotesRuntimeTest {
	@Test
	public void testCreateExporter() {
		DxlExporter exporter = getClient().createDxlExporter();
		assertNotNull(exporter);
	}
	
	@Test
	public void testExportNewDatabaseWriter() throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("foo", "bar");
			doc.save();
			
			DxlExporter exporter = getClient().createDxlExporter();
			assertNotNull(exporter);
			try(StringWriter w = new StringWriter()) {
				exporter.exportDatabase(database, w);
				w.flush();
				String val = w.toString();
				assertFalse(StringUtils.isEmpty(val));
				assertTrue(val.contains("<database"));
				assertTrue(val.contains("<note"));
			}
		});
	}
	
	@Test
	public void testExportNewDatabaseOutputStream() throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("foo", "bar");
			doc.save();
			
			DxlExporter exporter = getClient().createDxlExporter();
			assertNotNull(exporter);
			try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
				exporter.exportDatabase(database, os);
				os.flush();
				String val = os.toString();
				assertFalse(StringUtils.isEmpty(val));
				assertTrue(val.contains("<database"));
				assertTrue(val.contains("<note"));
			}
		});
	}
	
	@Test
	public void testExportNewNoteWriter() throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("foo", "bar");
			doc.save();
			
			DxlExporter exporter = getClient().createDxlExporter();
			assertNotNull(exporter);
			try(StringWriter w = new StringWriter()) {
				exporter.exportDocument(doc, w);
				w.flush();
				String val = w.toString();
				assertFalse(StringUtils.isEmpty(val));
				assertTrue(val.contains("<note"));
			}
		});
	}
	
	@Test
	public void testExportNewNoteOutputStream() throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("foo", "bar");
			doc.save();
			
			DxlExporter exporter = getClient().createDxlExporter();
			assertNotNull(exporter);
			try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
				exporter.exportDocument(doc, os);
				os.flush();
				String val = os.toString();
				assertFalse(StringUtils.isEmpty(val));
				assertTrue(val.contains("<note"));
			}
		});
	}
	
	@Test
	public void testExportNewNoteTableWriter() throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("foo", "bar");
			doc.save();
			
			DxlExporter exporter = getClient().createDxlExporter();
			assertNotNull(exporter);
			try(StringWriter w = new StringWriter()) {
				exporter.exportDocument(doc, w);
				w.flush();
				String val = w.toString();
				assertFalse(StringUtils.isEmpty(val));
				assertTrue(val.contains("<note"));
			}
		});
	}
	
	@Test
	public void testExportNewNoteTableOutputStream() throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("foo", "bar");
			doc.save();
			
			DxlExporter exporter = getClient().createDxlExporter();
			assertNotNull(exporter);
			try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
				exporter.exportDocument(doc, os);
				os.flush();
				String val = os.toString();
				assertFalse(StringUtils.isEmpty(val));
				assertTrue(val.contains("<note"));
			}
		});
	}
	
	@Test
	public void testExportNamesAclWriter() throws IOException {
		DominoClient client = getClient();
		Database database = client.openDatabase("names.nsf");
		assertNotEquals(null, database);
		
		DxlExporter exporter = getClient().createDxlExporter();
		assertNotNull(exporter);
		try(StringWriter w = new StringWriter()) {
			exporter.exportACL(database, w);
			w.flush();
			String val = w.toString();
			assertFalse(StringUtils.isEmpty(val));
			assertTrue(val.contains("<acl"));
		}
		
		database.close();
	}
	
	@Test
	public void testExportNamesAclOutputStream() throws IOException {
		DominoClient client = getClient();
		Database database = client.openDatabase("names.nsf");
		assertNotEquals(null, database);
		
		DxlExporter exporter = getClient().createDxlExporter();
		assertNotNull(exporter);
		try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			exporter.exportACL(database, os);
			os.flush();
			String val = os.toString();
			assertFalse(StringUtils.isEmpty(val));
			assertTrue(val.contains("<acl"));
		}
		
		database.close();
	}
	
	@Test
	public void testItemNamesRoundTrip() {
		DxlExporter exporter = getClient().createDxlExporter();
		assertNotNull(exporter);
		
		List<String> itemNames = Arrays.asList("foo", "bar");
		exporter.setRestrictToItemNames(itemNames);
		assertEquals(itemNames, exporter.getRestrictToItemNames());
	}
	
	// Enabled only when a known Domino server is defined, since names.nsf has known Java agents
	@Test
	@EnabledIfEnvironmentVariable(named = TestValidateCredentials.VALIDATE_CREDENTIALS_SERVER, matches = ".+")
	public void testExportNamesNsf() throws IOException {
		String namesServer = System.getenv(TestValidateCredentials.VALIDATE_CREDENTIALS_SERVER);
		
		DxlExporter exporter = getClient().createDxlExporter();
		assertNotNull(exporter);

		exporter.setOutputDoctype(false);
		exporter.setOutputXmlDecl(false);
		exporter.setOmitMiscFileObjects(true);
//		exporter.setForceNoteFormat(true);
		
		Database names = getClient().openDatabase(namesServer, "names.nsf");
		names.getDesign().getAgents()
			.filter(agent -> agent.getAgentLanguage() == AgentLanguage.JAVA)
			.forEach(agent -> {
				Document designNote = agent.getDocument();
		        try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
		          exporter.exportDocument(designNote, os);
		        } catch (final IOException e) {
		          throw new UncheckedIOException(e);
		        }
			});
	}
}
