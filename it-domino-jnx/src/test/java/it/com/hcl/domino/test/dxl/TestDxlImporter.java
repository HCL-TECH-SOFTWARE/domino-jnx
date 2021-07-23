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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.dxl.DxlImporter;
import com.hcl.domino.dxl.DxlImporterLog;
import com.hcl.domino.exception.DxlImportException;
import com.ibm.commons.util.io.StreamUtil;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDxlImporter extends AbstractNotesRuntimeTest {
	@Test
	public void testCreate() {
		DxlImporter importer = getClient().createDxlImporter();
		assertNotNull(importer);
	}
	
	@Test
	public void testImportBasicNote() throws Exception {
		withTempDb(database -> {
			DxlImporter importer = getClient().createDxlImporter();
			assertNotNull(importer);
			try(InputStream is = getClass().getResourceAsStream("/dxl/basicnote.xml")) {
				importer.importDxl(is, database);
			}
			IDTable ids = importer.getImportedNoteIds().orElse(null);
			assertNotEquals(null, ids);
			assertEquals(1, ids.size());
			int id = ids.iterator().next();
			Document doc = database.getDocumentById(id).get();
			assertEquals("Example", doc.get("Form", String.class, ""));
			assertEquals("Bar", doc.get("Foo", String.class, ""));
		});
	}
	
	@Test
	public void testImportBasicNoteReader() throws Exception {
		withTempDb(database -> {
			DxlImporter importer = getClient().createDxlImporter();
			assertNotNull(importer);
			try(InputStream is = getClass().getResourceAsStream("/dxl/basicnote.xml")) {
				try(Reader r = new InputStreamReader(is)) {
					importer.importDxl(r, database);
				}
			}
			IDTable ids = importer.getImportedNoteIds().orElse(null);
			assertNotEquals(null, ids);
			assertEquals(1, ids.size());
			int id = ids.iterator().next();
			Document doc = database.getDocumentById(id).get();
			assertEquals("Example", doc.get("Form", String.class, ""));
			assertEquals("Bar", doc.get("Foo", String.class, ""));
		});
	}
	
	@Test
	public void testImportBasicNoteString() throws Exception {
		withTempDb(database -> {
			DxlImporter importer = getClient().createDxlImporter();
			assertNotNull(importer);
			String dxl;
			try(InputStream is = getClass().getResourceAsStream("/dxl/basicnote.xml")) {
				dxl = StreamUtil.readString(is);
			}
			importer.importDxl(dxl, database);
			IDTable ids = importer.getImportedNoteIds().orElse(null);
			assertNotEquals(null, ids);
			assertEquals(1, ids.size());
			int id = ids.iterator().next();
			Document doc = database.getDocumentById(id).get();
			assertEquals("Example", doc.get("Form", String.class, ""));
			assertEquals("Bar", doc.get("Foo", String.class, ""));
		});
	}
	
	@Test
	public void testResourceDirDxl() throws Exception {
		withResourceDxl("/dxl/testResourceDirDxl", database -> {
			assertEquals(2, database.openCollection("All").get().getDocumentCount());
		});
	}
	
	@Test
	public void testInvalidDxl() throws Exception {
		withTempDb(database -> {
			DxlImporter importer = getClient().createDxlImporter();
			assertNotNull(importer);
			assertThrows(DxlImportException.class, () -> importer.importDxl("invalid dxl", database));
			try {
				importer.importDxl("invalid dxl", database);
			} catch(DxlImportException e) {
				DxlImporterLog log = e.getLog();
				assertNotNull(log);
				List<DxlImporterLog.DxlFatalError> errors = log.getFatalErrors();
				assertEquals(2, errors.size());
				{
					DxlImporterLog.DxlFatalError error = errors.get(0);
					assertEquals(1, error.getLine());
					assertEquals(1, error.getColumn());
					assertEquals("NotesInputSource", error.getSource());
					// NB: this may be freely discarded as a test if it turns out that this is different per-language 
					assertEquals("Invalid document structure", error.getText());
				}
				{
					DxlImporterLog.DxlFatalError error = errors.get(1);
					assertEquals(1, error.getLine());
					assertEquals(12, error.getColumn());
					assertEquals("NotesInputSource", error.getSource());
					assertEquals("The main XML document cannot be empty", error.getText());
				}
			}
		});
	}
	
	// Tests a DXL file seen to crash the JVM in a downstream app
	// This does not crash here, but it also doesn't import anything - this, though, is consistent with lsxbe in Java
	@Test
	@Disabled
	public void testCrashingDxl() throws Exception {
		withTempDb(database -> {
			DxlImporter importer = getClient().createDxlImporter();
			assertNotNull(importer);
			try(InputStream is = getClass().getResourceAsStream("/dxl/testCrasherDxl/dxl.xml")) {
				importer.importDxl(is, database);
			}
			assertEquals(1, importer.getImportedNoteIds().get().size());
		});
	}
}
