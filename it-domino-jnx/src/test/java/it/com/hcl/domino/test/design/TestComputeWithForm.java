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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoException;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Document.ComputeWithFormAction;
import com.hcl.domino.data.Document.ComputeWithFormCallback;
import com.hcl.domino.data.Document.ComputeWithFormPhase;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.dbdirectory.DirectorySearchQuery.SearchFlag;
import com.hcl.domino.dxl.DxlExporter;
import com.hcl.domino.richtext.FormField;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestComputeWithForm extends AbstractNotesRuntimeTest {

	@Test
	public void exportForm() throws Exception {
		boolean exportForm = "true".equalsIgnoreCase(System.getProperty("jnx.cwftest.exportform"));
		if(!exportForm) {
			if(log.isLoggable(Level.FINE)) {
				log.fine(MessageFormat.format("Skipping {0}#exportform; set -Djnx.cwftest.exportform=true to execute", getClass().getSimpleName()));
			}
			return;
		}

		DominoClient client = getClient();
		Database db = client.openDatabase("", "jnx/cwftest.nsf");

		DxlExporter dxlExporter = client.createDxlExporter();

		Path exportDir = Paths.get("src/test/resources/dxl/testComputeWithForm").toAbsolutePath();
		Files.createDirectories(exportDir);

		db.queryFormula("@IsMember(\"Person\";$TITLE)", null,
				EnumSet.of(SearchFlag.SUMMARY), null, EnumSet.of(DocumentClass.FORM))
		.forEachDocument(0, Integer.MAX_VALUE, (doc, loop) -> {
			Path outFilePath = exportDir.resolve("Person.xml");

			try (OutputStream fOut = Files.newOutputStream(outFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
				dxlExporter.exportDocument(doc, fOut);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	@Test
	public void testCWF() throws Exception {
		//we import a form with three fields, "firstname", "lastname" (both with validation formula
		//that checks if the field is empty) and "defaulttest" that computes a default value
		withResourceDxl("/dxl/testComputeWithForm", database -> {
			Document doc = database.createDocument();
			doc.replaceItemValue("Form", "Person");

			{
				List<String> expectedErrorTexts = Arrays.asList(
						"Firstname is missing",
						"Lastname is missing"
						);

				Set<String> errorFields = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

				doc.computeWithForm(false, new ComputeWithFormCallback() {

					@Override
					public ComputeWithFormAction errorRaised(FormField fieldInfo, ComputeWithFormPhase phase,
							String errorTxt,
							DominoException status) {
						
						errorFields.add(fieldInfo.getName());

						assertTrue(expectedErrorTexts.contains(errorTxt));
						return ComputeWithFormAction.NEXT_FIELD;
					}
				});

				assertEquals(2, errorFields.size());
				assertTrue(errorFields.contains("firstname"));
				assertTrue(errorFields.contains("lastname"));
				assertTrue(!"".equals(doc.get("defaulttest", String.class, "")));
			}

			doc.replaceItemValue("firstname", "John");

			{
				Set<String> errorFields = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

				doc.computeWithForm(false, new ComputeWithFormCallback() {

					@Override
					public ComputeWithFormAction errorRaised(FormField fieldInfo, ComputeWithFormPhase phase,
							String errorTxt,
							DominoException status) {

						errorFields.add(fieldInfo.getName());

						return ComputeWithFormAction.NEXT_FIELD;
					}
				});

				assertEquals(1, errorFields.size());
				assertTrue(errorFields.contains("lastname"));
			}

			doc.replaceItemValue("lastname", "Doe");

			{
				Set<String> errorFields = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

				doc.computeWithForm(false, new ComputeWithFormCallback() {

					@Override
					public ComputeWithFormAction errorRaised(FormField fieldInfo, ComputeWithFormPhase phase,
							String errorTxt,
							DominoException status) {
						errorFields.add(fieldInfo.getName());

						return ComputeWithFormAction.NEXT_FIELD;
					}
				});

				assertEquals(0, errorFields.size());
			}
		});
	}

}
