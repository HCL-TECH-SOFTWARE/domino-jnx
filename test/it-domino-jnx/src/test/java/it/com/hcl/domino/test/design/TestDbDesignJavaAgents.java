/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.DesignAgent;
import com.hcl.domino.design.agent.AgentInterval;
import com.hcl.domino.design.agent.AgentTrigger;
import com.hcl.domino.design.agent.DesignImportedJavaAgent;
import com.hcl.domino.design.agent.DesignJavaAgent;
import com.ibm.commons.util.io.StreamUtil;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
// Currently disabled for Java > 8 due to incompatibilities in the
//   DXL import process for Java agents
@DisabledForJreRange(min=JRE.JAVA_9)
public class TestDbDesignJavaAgents extends AbstractNotesRuntimeTest {
  public static final int EXPECTED_IMPORT_AGENTS = 3;
  private static String dbPath;

  @AfterAll
  public static void termDesignDb() {
    try {
      Files.deleteIfExists(Paths.get(TestDbDesignJavaAgents.dbPath));
    } catch (final Throwable t) {
      System.err.println("Unable to delete database " + TestDbDesignJavaAgents.dbPath + ": " + t);
    }
  }

  private Database database;

  @BeforeEach
  public void initDesignDb() throws IOException, URISyntaxException {
    if (this.database == null) {
      final DominoClient client = this.getClient();
      if (TestDbDesignJavaAgents.dbPath == null) {
        this.database = AbstractNotesRuntimeTest.createTempDb(client);
        TestDbDesignJavaAgents.dbPath = this.database.getAbsoluteFilePath();
        AbstractNotesRuntimeTest.populateResourceDxl("/dxl/testDbDesignJavaAgents", this.database);
      } else {
        this.database = client.openDatabase("", TestDbDesignJavaAgents.dbPath);
      }
    }
  }

  @Test
  public void testAgentsCount() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<DesignAgent> agents = dbDesign.getAgents().collect(Collectors.toList());
    assertEquals(EXPECTED_IMPORT_AGENTS, agents.size());

    assertNull(dbDesign.getAgent("Content").orElse(null));
  }

  @Test
  public void testImportedJavaAgent() throws IOException {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<DesignAgent> agents = dbDesign.getAgents().collect(Collectors.toList());
    final DesignAgent agent = agents.stream().filter(a -> "imported java agent".equals(a.getTitle())).findFirst().orElse(null);
    assertNotNull(agent);
    
    assertInstanceOf(DesignImportedJavaAgent.class, agent);
    DesignImportedJavaAgent importedJavaAgent = (DesignImportedJavaAgent) agent;

    assertNotNull(dbDesign.getAgent("imported java agent"));

    assertEquals("ImportedJavaAgentContent.class", importedJavaAgent.getMainClassName());
    assertEquals("H:\\", importedJavaAgent.getCodeFilesystemPath());
    assertEquals(Arrays.asList("ImportedJavaAgentContent.class", "JavaAgentContent.class", "bar.txt", "foo.jar"),
        importedJavaAgent.getFilenames());
    
    // Try to read the Java code
    {
      JavaClass clazz;
      try(InputStream is = importedJavaAgent.getFile("ImportedJavaAgentContent.class").get()) {
        ClassParser parser = new ClassParser(is, "ImportedJavaAgentContent.class");
        clazz = parser.parse();
      }
      assertEquals("com.hcl.domino.design.agent.ImportedJavaAgentContent", clazz.getClassName());
    }
    {
      JavaClass clazz;
      try(InputStream is = importedJavaAgent.getFile("JavaAgentContent.class").get()) {
        ClassParser parser = new ClassParser(is, "JavaAgentContent.class");
        clazz = parser.parse();
      }
      assertEquals("com.hcl.domino.design.agent.JavaAgentContent", clazz.getClassName());
      Method[] methods = clazz.getMethods();
      assertTrue(Stream.of(methods).anyMatch(m -> "getResourcesAttachmentName".equals(m.getName())));
    }
  }

  @Test
  public void testJavaAgent() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<DesignAgent> agents = dbDesign.getAgents().collect(Collectors.toList());
    final DesignAgent agent = agents.stream().filter(a -> "java agent".equals(a.getTitle())).findFirst().orElse(null);
    assertNotNull(agent);
    
    assertInstanceOf(DesignJavaAgent.class, agent);
    DesignJavaAgent javaAgent = (DesignJavaAgent) agent;

    assertNotNull(dbDesign.getAgent("java agent").orElse(null));
    
    assertEquals("JavaAgent.class", javaAgent.getMainClassName());
    assertEquals("c:\\Notes\\Data", javaAgent.getCodeFilesystemPath());
    assertEquals("%%source%%.jar", javaAgent.getSourceAttachmentName().get());
    assertEquals("%%object%%.jar", javaAgent.getObjectAttachmentName().get());
    assertFalse(javaAgent.getResourcesAttachmentName().isPresent());
    assertEquals(Collections.emptyList(), javaAgent.getSharedLibraryList());
  }

  @Test
  public void testMultiFileJavaAgent() throws IOException {
    final DbDesign dbDesign = this.database.getDesign();
    final DesignAgent agent = dbDesign.getAgent("Multi-File Java").orElse(null);
    assertNotNull(agent);

    assertInstanceOf(DesignJavaAgent.class, agent);
    DesignJavaAgent javaAgent = (DesignJavaAgent) agent;

    assertEquals(AgentTrigger.SCHEDULED, agent.getTrigger());
    assertEquals(AgentInterval.MINUTES, agent.getIntervalType());
    assertTrue(agent.getStartDate().isPresent());
    assertEquals(LocalDate.of(2021, 6, 14), agent.getStartDate().get().toLocalDate());
    assertFalse(agent.getEndDate().isPresent());
    assertEquals(LocalTime.of(10, 0), agent.getRunLocalTime().get());
    assertEquals(LocalTime.of(16, 0), agent.getRunEndLocalTime().get());
    assertEquals(3 * 60 + 30, agent.getInterval().getAsInt());
    assertEquals("CN=Arcturus/O=Frost", agent.getRunLocation());
    assertFalse(agent.isRunOnWeekends());

    assertEquals("lotus.domino.axis.JavaAgentRenamed.class", javaAgent.getMainClassName());
    assertEquals("c:\\Notes\\Data", javaAgent.getCodeFilesystemPath());
    assertEquals("%%source%%.jar", javaAgent.getSourceAttachmentName().get());
    assertEquals("%%object%%.jar", javaAgent.getObjectAttachmentName().get());
    assertEquals("%%resource%%.jar", javaAgent.getResourcesAttachmentName().get());
    assertEquals(Arrays.asList("foo.jar", "bar.jar"), javaAgent.getEmbeddedJarNames());
    assertEquals(Arrays.asList("java lib", "java consumer", "java lib 2", "java lib 3", "java lib 4"),
        javaAgent.getSharedLibraryList());
    
    try(
      InputStream is = javaAgent.getSourceAttachment().get();
      JarInputStream jis = new JarInputStream(is)
    ) {
      boolean foundBar = false;
      boolean foundAgent = false;
      
      for(JarEntry entry = jis.getNextJarEntry(); entry != null; entry = jis.getNextJarEntry()) {
        if("foo/Bar.java".equals(entry.getName())) {
          foundBar = true;
          String actual = toLf(StreamUtil.readString(jis));
          String expected = toLf(IOUtils.resourceToString("/text/testDbDesignAgents/Bar.java", StandardCharsets.UTF_8));
          assertEquals(expected, actual);
        } else if("lotus/domino/axis/JavaAgentRenamed.java".equals(entry.getName())) {
          foundAgent = true;
          String actual = toLf(StreamUtil.readString(jis));
          String expected = toLf(IOUtils.resourceToString("/text/testDbDesignAgents/JavaAgentRenamed.java", StandardCharsets.UTF_8));
          assertEquals(expected, actual);
        }
        
        jis.closeEntry();
      }
      
      assertTrue(foundBar);
      assertTrue(foundAgent);
    }

    try(
      InputStream is = javaAgent.getObjectAttachment().get();
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
      InputStream is = javaAgent.getResourcesAttachment().get();
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
          String expected = toLf(IOUtils.resourceToString("/text/testDbDesignAgents/desktop.ini.txt", StandardCharsets.UTF_8));
          assertEquals(expected, actual);
        }
        
        jis.closeEntry();
      }
      
      assertTrue(foundBar);
      assertTrue(foundDesktop);
    }

    try(
      InputStream is = javaAgent.getEmbeddedJar("foo.jar").get();
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
      InputStream is = javaAgent.getEmbeddedJar("bar.jar").get();
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
