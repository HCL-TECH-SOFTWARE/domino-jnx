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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.junit.jupiter.api.Assertions;
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
  // Tests a DXL file seen to crash the JVM in a downstream app
  // This does not crash here, but it also doesn't import anything - this, though,
  // is consistent with lsxbe in Java
  @Test
  @Disabled
  public void testCrashingDxl() throws Exception {
    this.withTempDb(database -> {
      final DxlImporter importer = this.getClient().createDxlImporter();
      Assertions.assertNotNull(importer);
      try (InputStream is = this.getClass().getResourceAsStream("/dxl/testCrasherDxl/dxl.xml")) {
        importer.importDxl(is, database);
      }
      Assertions.assertEquals(1, importer.getImportedNoteIds().get().size());
    });
  }

  @Test
  public void testCreate() {
    final DxlImporter importer = this.getClient().createDxlImporter();
    Assertions.assertNotNull(importer);
  }

  @Test
  public void testImportBasicNote() throws Exception {
    this.withTempDb(database -> {
      final DxlImporter importer = this.getClient().createDxlImporter();
      Assertions.assertNotNull(importer);
      try (InputStream is = this.getClass().getResourceAsStream("/dxl/basicnote.xml")) {
        importer.importDxl(is, database);
      }
      final IDTable ids = importer.getImportedNoteIds().orElse(null);
      Assertions.assertNotEquals(null, ids);
      Assertions.assertEquals(1, ids.size());
      final int id = ids.iterator().next();
      final Document doc = database.getDocumentById(id).get();
      Assertions.assertEquals("Example", doc.get("Form", String.class, ""));
      Assertions.assertEquals("Bar", doc.get("Foo", String.class, ""));
    });
  }

  @Test
  public void testImportBasicNoteReader() throws Exception {
    this.withTempDb(database -> {
      final DxlImporter importer = this.getClient().createDxlImporter();
      Assertions.assertNotNull(importer);
      try (InputStream is = this.getClass().getResourceAsStream("/dxl/basicnote.xml")) {
        try (Reader r = new InputStreamReader(is)) {
          importer.importDxl(r, database);
        }
      }
      final IDTable ids = importer.getImportedNoteIds().orElse(null);
      Assertions.assertNotEquals(null, ids);
      Assertions.assertEquals(1, ids.size());
      final int id = ids.iterator().next();
      final Document doc = database.getDocumentById(id).get();
      Assertions.assertEquals("Example", doc.get("Form", String.class, ""));
      Assertions.assertEquals("Bar", doc.get("Foo", String.class, ""));
    });
  }

  @Test
  public void testImportBasicNoteString() throws Exception {
    this.withTempDb(database -> {
      final DxlImporter importer = this.getClient().createDxlImporter();
      Assertions.assertNotNull(importer);
      String dxl;
      try (InputStream is = this.getClass().getResourceAsStream("/dxl/basicnote.xml")) {
        dxl = StreamUtil.readString(is);
      }
      importer.importDxl(dxl, database);
      final IDTable ids = importer.getImportedNoteIds().orElse(null);
      Assertions.assertNotEquals(null, ids);
      Assertions.assertEquals(1, ids.size());
      final int id = ids.iterator().next();
      final Document doc = database.getDocumentById(id).get();
      Assertions.assertEquals("Example", doc.get("Form", String.class, ""));
      Assertions.assertEquals("Bar", doc.get("Foo", String.class, ""));
    });
  }

  @Test
  public void testInvalidDxl() throws Exception {
    this.withTempDb(database -> {
      final DxlImporter importer = this.getClient().createDxlImporter();
      Assertions.assertNotNull(importer);
      Assertions.assertThrows(DxlImportException.class, () -> importer.importDxl("invalid dxl", database));
      try {
        importer.importDxl("invalid dxl", database);
      } catch (final DxlImportException e) {
        final DxlImporterLog log = e.getLog();
        Assertions.assertNotNull(log);
        final List<DxlImporterLog.DxlFatalError> errors = log.getFatalErrors();
        Assertions.assertEquals(2, errors.size());
        {
          final DxlImporterLog.DxlFatalError error = errors.get(0);
          Assertions.assertEquals(1, error.getLine());
          Assertions.assertEquals(1, error.getColumn());
          Assertions.assertEquals("NotesInputSource", error.getSource());
          // NB: this may be freely discarded as a test if it turns out that this is
          // different per-language
          Assertions.assertEquals("Invalid document structure", error.getText());
        }
        {
          final DxlImporterLog.DxlFatalError error = errors.get(1);
          Assertions.assertEquals(1, error.getLine());
          Assertions.assertEquals(12, error.getColumn());
          Assertions.assertEquals("NotesInputSource", error.getSource());
          Assertions.assertEquals("The main XML document cannot be empty", error.getText());
        }
      }
    });
  }

  @Test
  public void testResourceDirDxl() throws Exception {
    this.withResourceDxl("/dxl/testResourceDirDxl", database -> {
      Assertions.assertEquals(2, database.openCollection("All").get().getDocumentCount());
    });
  }
}
