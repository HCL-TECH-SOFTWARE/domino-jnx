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

import static it.com.hcl.domino.test.util.ITUtil.toLf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
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
    assertEquals(toLf(expected), toLf(lib.getScript()));
    
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
    assertEquals(toLf(expected), toLf(lib.getScript()));
  }

  @Test
  public void testScriptLibraries() {
    final DbDesign dbDesign = this.database.getDesign();
    final List<ScriptLibrary> libraries = dbDesign.getScriptLibraries().collect(Collectors.toList());
    assertEquals(10, libraries.size());
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
  public void testOverwriteSsjs() throws Exception {
    withResourceDxl("/dxl/testDbDesignLib", database -> {
      final DbDesign design = database.getDesign();

      // Overwrite as a library
      String expected = "/* I am new content */";
      {
        final ScriptLibrary scriptLibrary = design.getScriptLibrary("ssjs lib").get();
        assertInstanceOf(ServerJavaScriptLibrary.class, scriptLibrary);
        final ServerJavaScriptLibrary lib = (ServerJavaScriptLibrary) scriptLibrary;
        lib.setScript(expected);
        lib.save();
      }
      
      // Check as a library
      {
        final ScriptLibrary scriptLibrary = design.getScriptLibrary("ssjs lib").get();
        assertInstanceOf(ServerJavaScriptLibrary.class, scriptLibrary);
        final ServerJavaScriptLibrary lib = (ServerJavaScriptLibrary) scriptLibrary;
        assertEquals(expected, lib.getScript());
      }
      
      // Read as a file resource
      {
        String script;
        try(InputStream is = design.getResourceAsStream("ssjs lib").get()) {
          script = StreamUtil.readString(is);
        }
        assertEquals(expected.replace("\r\n", "\n"), script.replace("\r\n", "\n"));
      }
      
      expected = "/* I am new streamed content */";
      
      // Overwrite as a stream
      try(OutputStream os = design.newResourceOutputStream("/ssjs lib")) {
        os.write(expected.getBytes());
      }
      
      // Check as a library
      {
        final ScriptLibrary scriptLibrary = design.getScriptLibrary("ssjs lib").get();
        assertInstanceOf(ServerJavaScriptLibrary.class, scriptLibrary);
        final ServerJavaScriptLibrary lib = (ServerJavaScriptLibrary) scriptLibrary;
        assertEquals(expected, lib.getScript());
      }
      
      // Read as a file resource
      {
        String script;
        try(InputStream is = design.getResourceAsStream("ssjs lib").get()) {
          script = StreamUtil.readString(is);
        }
        assertEquals(expected.replace("\r\n", "\n"), script.replace("\r\n", "\n"));
      }
    });
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
    
    // Read as a file resource
    {
      String script;
      try(InputStream is = design.getResourceAsStream("js lib").get()) {
        script = StreamUtil.readString(is);
      }
      assertEquals("alert(\"Hi, I'm JS\")", script.replace("\r\n", "\n"));
    }
  }
  
  @Test
  public void testMultiFileJava() throws IOException {
    final DbDesign design = this.database.getDesign();
    final ScriptLibrary scriptLibrary = design.getScriptLibrary("Multi-File Java Lib").get();
    assertInstanceOf(JavaLibrary.class, scriptLibrary);

    final JavaLibrary lib = (JavaLibrary) scriptLibrary;
    final JavaAgentContent content = lib.getScriptContent();
    assertEquals("%%source%%.jar", content.getSourceAttachmentName().get());
    assertEquals("%%object%%.jar", content.getObjectAttachmentName().get());
    assertEquals("%%resource%%.jar", content.getResourcesAttachmentName().get());
    assertEquals(Arrays.asList("bar.jar", "foo.jar"), content.getEmbeddedJars());
    assertEquals(Collections.emptyList(), content.getSharedLibraryList());
    
    try(
      InputStream is = content.getSourceAttachment().get();
      JarInputStream jis = new JarInputStream(is)
    ) {
      boolean foundBar = false;
      boolean foundAgent = false;
      
      for(JarEntry entry = jis.getNextJarEntry(); entry != null; entry = jis.getNextJarEntry()) {
        if("foo/Bar.java".equals(entry.getName())) {
          foundBar = true;
          String actual = toLf(StreamUtil.readString(jis));
          String expected = toLf(IOUtils.resourceToString("/text/testDbDesignLibraries/Bar.java", StandardCharsets.UTF_8));
          assertEquals(expected, actual);
        } else if("lotus/domino/axis/JavaAgentRenamed.java".equals(entry.getName())) {
          foundAgent = true;
          String actual = toLf(StreamUtil.readString(jis));
          String expected = toLf(IOUtils.resourceToString("/text/testDbDesignLibraries/JavaAgentRenamed.java", StandardCharsets.UTF_8));
          assertEquals(expected, actual);
        }
        
        jis.closeEntry();
      }
      
      assertTrue(foundBar);
      assertTrue(foundAgent);
    }

    try(
      InputStream is = content.getObjectAttachment().get();
      JarInputStream jis = new JarInputStream(is)
    ) {
      boolean foundBar = false;
      boolean foundAgent = false;
      
      for(JarEntry entry = jis.getNextJarEntry(); entry != null; entry = jis.getNextJarEntry()) {
        if("foo/Bar.class".equals(entry.getName())) {
          foundBar = true;
        } else if("lotus/domino/axis/JavaAgentRenamed.class".equals(entry.getName())) {
          foundAgent = true;
        }
        
        jis.closeEntry();
      }
      
      assertTrue(foundBar);
      assertTrue(foundAgent);
    }

    try(
      InputStream is = content.getResourcesAttachment().get();
      JarInputStream jis = new JarInputStream(is)
    ) {
      boolean foundBar = false;
      boolean foundDesktop = false;
      
      for(JarEntry entry = jis.getNextJarEntry(); entry != null; entry = jis.getNextJarEntry()) {
        if("bar.txt".equals(entry.getName())) {
          foundBar = true;
        } else if("desktop.ini".equals(entry.getName())) {
          foundDesktop = true;
          
          String actual = toLf(IOUtils.toString(jis, StandardCharsets.UTF_16));
          String expected = toLf(IOUtils.resourceToString("/text/testDbDesignLibraries/desktop.ini.txt", StandardCharsets.UTF_8));
          assertEquals(expected, actual);
        }
        
        jis.closeEntry();
      }
      
      assertTrue(foundBar);
      assertTrue(foundDesktop);
    }

    try(
      InputStream is = content.getEmbeddedJar("foo.jar").get();
      JarInputStream jis = new JarInputStream(is)
    ) {
      boolean foundFoo = false;
      
      for(JarEntry entry = jis.getNextJarEntry(); entry != null; entry = jis.getNextJarEntry()) {
        if("foo.txt".equals(entry.getName())) {
          foundFoo = true;
        }
        
        jis.closeEntry();
      }
      
      assertTrue(foundFoo);
    }

    try(
      InputStream is = content.getEmbeddedJar("bar.jar").get();
      JarInputStream jis = new JarInputStream(is)
    ) {
      boolean foundBar = false;
      
      for(JarEntry entry = jis.getNextJarEntry(); entry != null; entry = jis.getNextJarEntry()) {
        if("bar.txt".equals(entry.getName())) {
          foundBar = true;
        }
        
        jis.closeEntry();
      }
      
      assertTrue(foundBar);
    }
  }
}
