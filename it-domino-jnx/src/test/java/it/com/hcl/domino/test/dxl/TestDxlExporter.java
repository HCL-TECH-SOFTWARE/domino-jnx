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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
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
    final DxlExporter exporter = this.getClient().createDxlExporter();
    Assertions.assertNotNull(exporter);
  }

  @Test
  public void testExportNamesAclOutputStream() throws IOException {
    final DominoClient client = this.getClient();
    final Database database = client.openDatabase("names.nsf");
    Assertions.assertNotEquals(null, database);

    final DxlExporter exporter = this.getClient().createDxlExporter();
    Assertions.assertNotNull(exporter);
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      exporter.exportACL(database, os);
      os.flush();
      final String val = os.toString();
      Assertions.assertFalse(StringUtils.isEmpty(val));
      Assertions.assertTrue(val.contains("<acl"));
    }

    database.close();
  }

  @Test
  public void testExportNamesAclWriter() throws IOException {
    final DominoClient client = this.getClient();
    final Database database = client.openDatabase("names.nsf");
    Assertions.assertNotEquals(null, database);

    final DxlExporter exporter = this.getClient().createDxlExporter();
    Assertions.assertNotNull(exporter);
    try (StringWriter w = new StringWriter()) {
      exporter.exportACL(database, w);
      w.flush();
      final String val = w.toString();
      Assertions.assertFalse(StringUtils.isEmpty(val));
      Assertions.assertTrue(val.contains("<acl"));
    }

    database.close();
  }

  // Enabled only when a known Domino server is defined, since names.nsf has known
  // Java agents
  @Test
  @EnabledIfEnvironmentVariable(named = TestValidateCredentials.VALIDATE_CREDENTIALS_SERVER, matches = ".+")
  public void testExportNamesNsf() throws IOException {
    final String namesServer = System.getenv(TestValidateCredentials.VALIDATE_CREDENTIALS_SERVER);

    final DxlExporter exporter = this.getClient().createDxlExporter();
    Assertions.assertNotNull(exporter);

    exporter.setOutputDoctype(false);
    exporter.setOutputXmlDecl(false);
    exporter.setOmitMiscFileObjects(true);
    // exporter.setForceNoteFormat(true);

    final Database names = this.getClient().openDatabase(namesServer, "names.nsf");
    names.getDesign().getAgents()
        .filter(agent -> agent.getAgentLanguage() == AgentLanguage.JAVA)
        .forEach(agent -> {
          final Document designNote = agent.getDocument();
          try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            exporter.exportDocument(designNote, os);
          } catch (final IOException e) {
            throw new UncheckedIOException(e);
          }
        });
  }

  @Test
  public void testExportNewDatabaseOutputStream() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("foo", "bar");
      doc.save();

      final DxlExporter exporter = this.getClient().createDxlExporter();
      Assertions.assertNotNull(exporter);
      try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
        exporter.exportDatabase(database, os);
        os.flush();
        final String val = os.toString();
        Assertions.assertFalse(StringUtils.isEmpty(val));
        Assertions.assertTrue(val.contains("<database"));
        Assertions.assertTrue(val.contains("<note"));
      }
    });
  }

  @Test
  public void testExportNewDatabaseWriter() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("foo", "bar");
      doc.save();

      final DxlExporter exporter = this.getClient().createDxlExporter();
      Assertions.assertNotNull(exporter);
      try (StringWriter w = new StringWriter()) {
        exporter.exportDatabase(database, w);
        w.flush();
        final String val = w.toString();
        Assertions.assertFalse(StringUtils.isEmpty(val));
        Assertions.assertTrue(val.contains("<database"));
        Assertions.assertTrue(val.contains("<note"));
      }
    });
  }

  @Test
  public void testExportNewNoteOutputStream() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("foo", "bar");
      doc.save();

      final DxlExporter exporter = this.getClient().createDxlExporter();
      Assertions.assertNotNull(exporter);
      try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
        exporter.exportDocument(doc, os);
        os.flush();
        final String val = os.toString();
        Assertions.assertFalse(StringUtils.isEmpty(val));
        Assertions.assertTrue(val.contains("<note"));
      }
    });
  }

  @Test
  public void testExportNewNoteTableOutputStream() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("foo", "bar");
      doc.save();

      final DxlExporter exporter = this.getClient().createDxlExporter();
      Assertions.assertNotNull(exporter);
      try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
        exporter.exportDocument(doc, os);
        os.flush();
        final String val = os.toString();
        Assertions.assertFalse(StringUtils.isEmpty(val));
        Assertions.assertTrue(val.contains("<note"));
      }
    });
  }

  @Test
  public void testExportNewNoteTableWriter() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("foo", "bar");
      doc.save();

      final DxlExporter exporter = this.getClient().createDxlExporter();
      Assertions.assertNotNull(exporter);
      try (StringWriter w = new StringWriter()) {
        exporter.exportDocument(doc, w);
        w.flush();
        final String val = w.toString();
        Assertions.assertFalse(StringUtils.isEmpty(val));
        Assertions.assertTrue(val.contains("<note"));
      }
    });
  }

  @Test
  public void testExportNewNoteWriter() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("foo", "bar");
      doc.save();

      final DxlExporter exporter = this.getClient().createDxlExporter();
      Assertions.assertNotNull(exporter);
      try (StringWriter w = new StringWriter()) {
        exporter.exportDocument(doc, w);
        w.flush();
        final String val = w.toString();
        Assertions.assertFalse(StringUtils.isEmpty(val));
        Assertions.assertTrue(val.contains("<note"));
      }
    });
  }

  @Test
  public void testItemNamesRoundTrip() {
    final DxlExporter exporter = this.getClient().createDxlExporter();
    Assertions.assertNotNull(exporter);

    final List<String> itemNames = Arrays.asList("foo", "bar");
    exporter.setRestrictToItemNames(itemNames);
    Assertions.assertEquals(itemNames, exporter.getRestrictToItemNames());
  }
}
