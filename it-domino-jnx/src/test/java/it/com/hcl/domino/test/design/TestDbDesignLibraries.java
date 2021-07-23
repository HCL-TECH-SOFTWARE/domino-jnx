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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.JavaLibrary;
import com.hcl.domino.design.LotusScriptLibrary;
import com.hcl.domino.design.ScriptLibrary;
import com.hcl.domino.design.ServerJavaScriptLibrary;
import com.hcl.domino.design.agent.JavaAgentContent;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDbDesignLibraries extends AbstractNotesRuntimeTest {
	private static String dbPath;
	private Database database;
	
	@BeforeEach
	public void initDesignDb() throws IOException, URISyntaxException {
		if(database == null) {
			DominoClient client = getClient();
			if(dbPath == null) {
				database = createTempDb(client);
				dbPath = database.getAbsoluteFilePath();
				populateResourceDxl("/dxl/testDbDesignLib", database);
			} else {
				database = client.openDatabase("", dbPath);
			}
		}
	}
	
	@AfterAll
	public static void termDesignDb() {
		try {
			Files.deleteIfExists(Paths.get(dbPath));
		} catch(Throwable t) {
			System.err.println("Unable to delete database " + dbPath + ": " + t);
		}
	}

	@Test
	public void testScriptLibraries() {
		DbDesign dbDesign = database.getDesign();
		List<ScriptLibrary> libraries = dbDesign.getScriptLibraries().collect(Collectors.toList());
		assertEquals(9, libraries.size());
	}

	@Test
	public void testLs() throws IOException {
		DbDesign design = database.getDesign();
		ScriptLibrary scriptLibrary = design.getScriptLibrary("Test LS").get();
		assertInstanceOf(LotusScriptLibrary.class, scriptLibrary);
		
		LotusScriptLibrary lib = (LotusScriptLibrary)scriptLibrary;
		String expected = IOUtils.resourceToString("/text/lslibrary.txt", Charset.forName("UTF-8"));
		assertEquals(expected, lib.getScript());
	}

	@Test
	public void testLargeLs() throws IOException {
		DbDesign design = database.getDesign();
		ScriptLibrary scriptLibrary = design.getScriptLibrary("Test Large LS").get();
		assertInstanceOf(LotusScriptLibrary.class, scriptLibrary);
		
		LotusScriptLibrary lib = (LotusScriptLibrary)scriptLibrary;
		String expected = IOUtils.resourceToString("/text/largelslibrary.txt", Charset.forName("UTF-8"));
		assertEquals(expected, lib.getScript());
	}

	@Test
	public void testSsjs() throws IOException {
		DbDesign design = database.getDesign();
		ScriptLibrary scriptLibrary = design.getScriptLibrary("ssjs lib").get();
		assertInstanceOf(ServerJavaScriptLibrary.class, scriptLibrary);
		
		ServerJavaScriptLibrary lib = (ServerJavaScriptLibrary)scriptLibrary;
		String expected = IOUtils.resourceToString("/text/ssjs.txt", Charset.forName("UTF-8"));
		assertEquals(expected.replace("\r\n", "\n"), lib.getScript().replace("\r\n", "\n"));
	}

	@Test
	public void testLargeSsjs() throws IOException {
		DbDesign design = database.getDesign();
		ScriptLibrary scriptLibrary = design.getScriptLibrary("ssjs large lib").get();
		assertInstanceOf(ServerJavaScriptLibrary.class, scriptLibrary);
		
		ServerJavaScriptLibrary lib = (ServerJavaScriptLibrary)scriptLibrary;
		String expected = IOUtils.resourceToString("/text/largessjs.txt", Charset.forName("UTF-8"));
		assertEquals(expected.replace("\r\n", "\n"), lib.getScript().replace("\r\n", "\n"));
	}

	@Test
	public void testJava() throws IOException {
		DbDesign design = database.getDesign();
		ScriptLibrary scriptLibrary = design.getScriptLibrary("java lib").get();
		assertInstanceOf(JavaLibrary.class, scriptLibrary);
		
		JavaLibrary lib = (JavaLibrary)scriptLibrary;
		JavaAgentContent content = lib.getScriptContent();
		assertEquals("%%source%%.jar", content.getSourceAttachmentName().get());
		assertEquals("%%object%%.jar", content.getObjectAttachmentName().get());
	}

	@Test
	public void testJava4() throws IOException {
		DbDesign design = database.getDesign();
		ScriptLibrary scriptLibrary = design.getScriptLibrary("java lib 4").get();
		assertEquals("j", scriptLibrary.getComment());
		assertInstanceOf(JavaLibrary.class, scriptLibrary);
		
		JavaLibrary lib = (JavaLibrary)scriptLibrary;
		JavaAgentContent content = lib.getScriptContent();
		assertFalse(content.getSourceAttachmentName().isPresent());
		assertFalse(content.getObjectAttachmentName().isPresent());
	}
}
