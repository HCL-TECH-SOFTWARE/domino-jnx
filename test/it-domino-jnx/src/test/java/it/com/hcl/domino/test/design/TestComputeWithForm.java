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
package it.com.hcl.domino.test.design;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Document.ComputeWithFormAction;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.dbdirectory.DirectorySearchQuery.SearchFlag;
import com.hcl.domino.dxl.DxlExporter;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestComputeWithForm extends AbstractNotesRuntimeTest {

  @Test
  public void exportForm() throws Exception {
    final boolean exportForm = "true".equalsIgnoreCase(System.getProperty("jnx.cwftest.exportform"));
    if (!exportForm) {
      if (this.log.isLoggable(Level.FINE)) {
        this.log.fine(MessageFormat.format("Skipping {0}#exportform; set -Djnx.cwftest.exportform=true to execute",
            this.getClass().getSimpleName()));
      }
      return;
    }

    final DominoClient client = this.getClient();
    final Database db = client.openDatabase("", "jnx/cwftest.nsf");

    final DxlExporter dxlExporter = client.createDxlExporter();

    final Path exportDir = Paths.get("src/test/resources/dxl/testComputeWithForm").toAbsolutePath();
    Files.createDirectories(exportDir);

    db.queryFormula("@IsMember(\"Person\";$TITLE)", null,
        EnumSet.of(SearchFlag.SUMMARY), null, EnumSet.of(DocumentClass.FORM))
        .forEachDocument(0, Integer.MAX_VALUE, (doc, loop) -> {
          final Path outFilePath = exportDir.resolve("Person.xml");

          try (OutputStream fOut = Files.newOutputStream(outFilePath, StandardOpenOption.CREATE,
              StandardOpenOption.TRUNCATE_EXISTING)) {
            dxlExporter.exportDocument(doc, fOut);
          } catch (final IOException e) {
            e.printStackTrace();
          }
        });
  }

  @Test
  public void testCWF() throws Exception {
    // we import a form with three fields, "firstname", "lastname" (both with
    // validation formula
    // that checks if the field is empty) and "defaulttest" that computes a default
    // value
    this.withResourceDxl("/dxl/testComputeWithForm", database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("Form", "Person");

      {
        final List<String> expectedErrorTexts = Arrays.asList(
            "Firstname is missing",
            "Lastname is missing");

        final Set<String> errorFields = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        doc.computeWithForm(false, (fieldInfo, phase, errorTxt, status) -> {

      errorFields.add(fieldInfo.getName());

      Assertions.assertTrue(expectedErrorTexts.contains(errorTxt));
      return ComputeWithFormAction.NEXT_FIELD;
     });

        Assertions.assertEquals(2, errorFields.size());
        Assertions.assertTrue(errorFields.contains("firstname"));
        Assertions.assertTrue(errorFields.contains("lastname"));
        Assertions.assertTrue(!"".equals(doc.get("defaulttest", String.class, "")));
      }

      doc.replaceItemValue("firstname", "John");

      {
        final Set<String> errorFields = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        doc.computeWithForm(false, (fieldInfo, phase, errorTxt, status) -> {

      errorFields.add(fieldInfo.getName());

      return ComputeWithFormAction.NEXT_FIELD;
     });

        Assertions.assertEquals(1, errorFields.size());
        Assertions.assertTrue(errorFields.contains("lastname"));
      }

      doc.replaceItemValue("lastname", "Doe");

      {
        final Set<String> errorFields = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        doc.computeWithForm(false, (fieldInfo, phase, errorTxt, status) -> {
      errorFields.add(fieldInfo.getName());

      return ComputeWithFormAction.NEXT_FIELD;
     });

        Assertions.assertEquals(0, errorFields.size());
      }
    });
  }

}
