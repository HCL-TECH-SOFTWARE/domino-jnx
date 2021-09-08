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
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.JavaLibrary;
import com.hcl.domino.design.JavaScriptLibrary;
import com.hcl.domino.design.LotusScriptLibrary;
import com.hcl.domino.design.ScriptLibrary;
import com.hcl.domino.design.ServerJavaScriptLibrary;
import com.hcl.domino.design.agent.JavaAgentContent;
import com.ibm.commons.util.io.StreamUtil;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDbDesignLibraries extends AbstractNotesRuntimeTest {
  private static String dbPath;

  @AfterAll
  public static void termDesignDb() {
    try {
      Files.deleteIfExists(Paths.get(TestDbDesignLibraries.dbPath));
    } catch (final Throwable t) {
      System.err.println("Unable to delete database " + TestDbDesignLibraries.dbPath + ": " + t);
    }
  }

  private Database database;

  @BeforeEach
  public void initDesignDb() throws IOException, URISyntaxException {
    if (this.database == null) {
      final DominoClient client = this.getClient();
      if (TestDbDesignLibraries.dbPath == null) {
        this.database = AbstractNotesRuntimeTest.createTempDb(client);
        TestDbDesignLibraries.dbPath = this.database.getAbsoluteFilePath();
        AbstractNotesRuntimeTest.populateResourceDxl("/dxl/testDbDesignLib", this.database);
      } else {
        this.database = client.openDatabase("", TestDbDesignLibraries.dbPath);
      }
    }
  }

  @Test
  public void testJava() throws IOException {
    final DbDesign design = this.database.getDesign();
    final ScriptLibrary scriptLibrary = design.getScriptLibrary("java lib").get();
    assertInstanceOf(JavaLibrary.class, scriptLibrary);

    final JavaLibrary lib = (JavaLibrary) scriptLibrary;
    final JavaAgentContent content = lib.getScriptContent();
    assertEquals("%%source%%.jar", content.getSourceAttachmentName().get());
    assertEquals("%%object%%.jar", content.getObjectAttachmentName().get());
    
    String unid = scriptLibrary.getDocument().getUNID();
    Optional<ScriptLibrary> optLib = design.getDesignElementByUNID(unid);
    assertInstanceOf(JavaLibrary.class, optLib.get());
  }

  @Test
  public void testJava4() throws IOException {
    final DbDesign design = this.database.getDesign();
    final ScriptLibrary scriptLibrary = design.getScriptLibrary("java lib 4").get();
    assertEquals("j", scriptLibrary.getComment());
    assertInstanceOf(JavaLibrary.class, scriptLibrary);

    final JavaLibrary lib = (JavaLibrary) scriptLibrary;
    final JavaAgentContent content = lib.getScriptContent();
    assertFalse(content.getSourceAttachmentName().isPresent());
    assertFalse(content.getObjectAttachmentName().isPresent());
  }

  @Test
  public void testLargeLs() throws IOException {
    final DbDesign design = this.database.getDesign();
    final ScriptLibrary scriptLibrary = design.getScriptLibrary("Test Large LS").get();
    assertInstanceOf(LotusScriptLibrary.class, scriptLibrary);

    final LotusScriptLibrary lib = (LotusScriptLibrary) scriptLibrary;
    final String expected = IOUtils.resourceToString("/text/largelslibrary.txt", Charset.forName("UTF-8"));
    assertEquals(expected, lib.getScript());
    
    String unid = scriptLibrary.getDocument().getUNID();
    Optional<ScriptLibrary> optLib = design.getDesignElementByUNID(unid);
    assertInstanceOf(LotusScriptLibrary.class, optLib.get());
  }

  @Test
  public void testLargeSsjs() throws IOException {
    final DbDesign design = this.database.getDesign();
    final ScriptLibrary scriptLibrary = design.getScriptLibrary("ssjs large lib").get();
    assertInstanceOf(ServerJavaScriptLibrary.class, scriptLibrary);

    final ServerJavaScriptLibrary lib = (ServerJavaScriptLibrary) scriptLibrary;
    final String expected = IOUtils.resourceToString("/text/largessjs.txt", Charset.forName("UTF-8"));
    assertEquals(expected.replace("\r\n", "\n"), lib.getScript().replace("\r\n", "\n"));
    
    String unid = scriptLibrary.getDocument().getUNID();
    Optional<ScriptLibrary> optLib = design.getDesignElementByUNID(unid);
    assertInstanceOf(ServerJavaScriptLibrary.class, optLib.get());
  }

  @Test
  public void testLs() throws IOException {
    final DbDesign design = this.database.getDesign();
    final ScriptLibrary scriptLibrary = design.getScriptLibrary("Test LS").get();
    assertInstanceOf(LotusScriptLibrary.class, scriptLibrary);

    final LotusScriptLibrary lib = (LotusScriptLibrary) scriptLibrary;
    final String expected = IOUtils.resourceToString("/text/lslibrary.txt", Charset.forName("UTF-8"));
    assertEquals(expected, lib.getScript());
  }

  @Test
  public void testScriptLibraries() {
    final DbDesign dbDesign = this.database.getDesign();
    final List<ScriptLibrary> libraries = dbDesign.getScriptLibraries().collect(Collectors.toList());
    assertEquals(9, libraries.size());
  }

  @Test
  public void testSsjs() throws IOException {
    final DbDesign design = this.database.getDesign();
    final ScriptLibrary scriptLibrary = design.getScriptLibrary("ssjs lib").get();
    assertInstanceOf(ServerJavaScriptLibrary.class, scriptLibrary);

    final ServerJavaScriptLibrary lib = (ServerJavaScriptLibrary) scriptLibrary;
    final String expected = IOUtils.resourceToString("/text/ssjs.txt", Charset.forName("UTF-8"));
    assertEquals(expected.replace("\r\n", "\n"), lib.getScript().replace("\r\n", "\n"));
    
    // Read as a file resource
    {
      String script;
      try(InputStream is = design.getResourceAsStream("ssjs lib").get()) {
        script = StreamUtil.readString(is);
      }
      assertEquals(expected.replace("\r\n", "\n"), script.replace("\r\n", "\n"));
    }
  }

  @Test
  public void testJs() throws IOException {
    final DbDesign design = this.database.getDesign();
    final ScriptLibrary scriptLibrary = design.getScriptLibrary("js lib").get();
    assertInstanceOf(JavaScriptLibrary.class, scriptLibrary);

    final JavaScriptLibrary lib = (JavaScriptLibrary) scriptLibrary;
    assertEquals("alert(\"Hi, I'm JS\")", lib.getScript());
    
    String unid = scriptLibrary.getDocument().getUNID();
    Optional<ScriptLibrary> optLib = design.getDesignElementByUNID(unid);
    assertInstanceOf(JavaScriptLibrary.class, optLib.get());
  }
}
