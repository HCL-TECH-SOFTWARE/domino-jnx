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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.junit.jupiter.api.Test;

import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.DesignAgent;
import com.hcl.domino.design.DesignElement;
import com.hcl.domino.design.agent.DesignFormulaAgent;
import com.hcl.domino.design.agent.DesignImportedJavaAgent;
import com.hcl.domino.design.agent.DesignJavaAgent;
import com.hcl.domino.design.agent.DesignSimpleActionAgent;
import com.hcl.domino.design.agent.JavaLanguageAgent;
import com.hcl.domino.design.simpleaction.RunFormulaAction.DocumentAction;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

public class TestAgentCreation extends AbstractNotesRuntimeTest {

  @Test
  public void testCreateFormulaAgent() throws Exception {
    withTempDb((db) -> {
      DbDesign dbDesign = db.getDesign();

      String agentName = "formulaagent"; //$NON-NLS-1$

      DocumentAction docAction1 = DocumentAction.SELECT;
      String formula1 = "@SetField(\"abc\";1);\n@All"; //$NON-NLS-1$

      DocumentAction docAction2 = DocumentAction.MODIFY;
      String formula2 = "@SetField(\"def\";0);\n@All"; //$NON-NLS-1$

      DesignFormulaAgent agent = dbDesign.createAgent(DesignFormulaAgent.class, agentName);
      assertNotNull(agent);

      agent.setFormula(formula1);
      agent.setDocumentAction(docAction1);
      agent.sign();
      agent.save();
      String unid = agent.getDocument().getUNID();

      DesignElement testDE = dbDesign.getDesignElementByUNID(unid).orElseThrow(() -> new IllegalStateException("Agent not found via UNID"));
      assertInstanceOf(DesignFormulaAgent.class, testDE);
      agent = (DesignFormulaAgent) testDE;

      assertEquals(toLf(formula1), toLf(agent.getFormula().get()));
      assertEquals(docAction1, agent.getDocumentAction().get());

      agent.setDocumentAction(docAction2);
      agent.setFormula(formula2);
      agent.sign();
      agent.save();

      agent = (DesignFormulaAgent) dbDesign.getDesignElementByName(DesignAgent.class, agentName).orElseThrow(() -> new IllegalStateException("Agent not found via name"));

      assertEquals(toLf(formula2), toLf(agent.getFormula().get()));
      assertEquals(docAction2, agent.getDocumentAction().get());

    });
  }

  @Test
  public void testCreateJavaAgent() throws Exception {
    withTempDb((db) -> {
      String mainClassName = "JavaAgent.class"; //$NON-NLS-1$
      String sourceTarget = "1.3"; //$NON-NLS-1$
      String objectTarget = "1.2"; //$NON-NLS-1$
      String codeFilesystemPath = "c:\\data"; //$NON-NLS-1$
      List<String> sharedLibs = Arrays.asList("lib1", "lib2"); //$NON-NLS-1$ //$NON-NLS-2$
      
      DbDesign dbDesign = db.getDesign();
      DesignJavaAgent agent = dbDesign.createAgent(DesignJavaAgent.class, "javaagent"); //$NON-NLS-1$
      assertNotNull(agent);
      
      //write initial source/object jars
      agent.initJavaContent();
      agent.setMainClassName(mainClassName);
      agent.setJavaCompilerSource(sourceTarget);
      agent.setJavaCompilerTarget(objectTarget);
      agent.setCodeFilesystemPath(codeFilesystemPath);
      agent.setSharedLibraryList(sharedLibs);
      
      agent.sign();
      agent.save();
      
      String unid = agent.getDocument().getUNID();
      
      DesignElement testDE = dbDesign.getDesignElementByUNID(unid).orElseThrow(() -> new IllegalStateException("Agent not found via UNID"));
      assertInstanceOf(DesignJavaAgent.class, testDE);
      assertInstanceOf(JavaLanguageAgent.class, testDE);
      agent = (DesignJavaAgent) testDE;

      assertEquals(mainClassName, agent.getMainClassName());
      assertEquals(sourceTarget, agent.getJavaCompilerSource());
      assertEquals(objectTarget, agent.getJavaCompilerTarget());
      assertEquals(codeFilesystemPath, agent.getCodeFilesystemPath());
      assertEquals(sharedLibs, agent.getSharedLibraryList());
      
      {
        final String AGENT_DEFAULT_SOURCE_JAR_RESOURCEPATH = "/com/hcl/domino/commons/design/initialdesign/javaagent/%%source%%.jar"; //$NON-NLS-1$
        InputStream inOrig = getClass().getResourceAsStream(AGENT_DEFAULT_SOURCE_JAR_RESOURCEPATH);
        assertNotNull(inOrig);
        
        InputStream inDesign = agent.getSourceAttachment().orElse(null);
        assertNotNull(inOrig);
        
        assertEqualStreams(inOrig, inDesign, "Source attachment mismatch");
      }
      {
        final String AGENT_DEFAULT_OBJECT_JAR_RESOURCEPATH = "/com/hcl/domino/commons/design/initialdesign/javaagent/%%object%%.jar"; //$NON-NLS-1$
        InputStream inOrig = getClass().getResourceAsStream(AGENT_DEFAULT_OBJECT_JAR_RESOURCEPATH);
        assertNotNull(inOrig);
        
        InputStream inDesign = agent.getObjectAttachment().orElse(null);
        assertNotNull(inOrig);
        
        assertEqualStreams(inOrig, inDesign, "Object attachment mismatch");
      }
      
      {
        //build resources jar on the fly
        final String[] testResources = {
            "/images/file-icon.gif", //$NON-NLS-1$
            "/images/help_vampire.gif" //$NON-NLS-1$
        };
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0"); //$NON-NLS-1$
        ByteArrayOutputStream jarOut = new ByteArrayOutputStream();
        
        try (JarOutputStream target = new JarOutputStream(jarOut, manifest);) {
          for (String currResource : testResources) {
            int iPos = currResource.lastIndexOf("/"); //$NON-NLS-1$
            String fileName = currResource.substring(iPos+1);
            
            JarEntry entry = new JarEntry(fileName);
            entry.setTime(System.currentTimeMillis());
            target.putNextEntry(entry);
            
            InputStream in = getClass().getResourceAsStream(currResource);
            assertNotNull(in, currResource);
            
            byte[] buffer = new byte[1024];
            while (true)
            {
              int len = in.read(buffer);
              if (len == -1)
                break;
              target.write(buffer, 0, len);
            }
            target.closeEntry();
          }
        }
        
        agent.setResourceAttachment(new ByteArrayInputStream(jarOut.toByteArray()));
        assertTrue(StringUtil.isNotEmpty(agent.getResourcesAttachmentName().get()));
        
        assertEqualStreams(new ByteArrayInputStream(jarOut.toByteArray()), agent.getResourcesAttachment().get(),
            "Resource attachment mismatch");
      }
    });
  }
  
  @Test
  public void testCreateImportedJavaAgent() throws Exception {
    withTempDb((db) -> {
      DbDesign dbDesign = db.getDesign();
      DesignImportedJavaAgent agent = dbDesign.createAgent(DesignImportedJavaAgent.class, "importedjavaagent"); //$NON-NLS-1$
      assertNotNull(agent);

      String mainClassName = "JavaAgent.class"; //$NON-NLS-1$
      String codeFilesystemPath = "c:\\data"; //$NON-NLS-1$

      //write initial source/object jars
      agent.setMainClassName(mainClassName);
      agent.setCodeFilesystemPath(codeFilesystemPath);
      
      final List<String> testResources = Arrays.asList(
          "/images/file-icon.gif", //$NON-NLS-1$
          "/images/help_vampire.gif" //$NON-NLS-1$
      );

      Map<String, String> writtenFileNames = new HashMap<>();
      
      for (String currResource : testResources) {
        int iPos = currResource.lastIndexOf("/"); //$NON-NLS-1$
        String fileName = currResource.substring(iPos+1);

        InputStream in = getClass().getResourceAsStream(currResource);
        assertNotNull(in, currResource);
        
        agent.setFile(fileName, in);
        writtenFileNames.put(fileName, currResource);
      }
      
      agent.sign();
      agent.save();
      
      String unid = agent.getDocument().getUNID();

      DesignElement testDE = dbDesign.getDesignElementByUNID(unid).orElseThrow(() -> new IllegalStateException("Agent not found via UNID"));
      assertInstanceOf(DesignImportedJavaAgent.class, testDE);
      assertInstanceOf(JavaLanguageAgent.class, testDE);
      agent = (DesignImportedJavaAgent) testDE;

      assertEquals(mainClassName, agent.getMainClassName());
      assertEquals(codeFilesystemPath, agent.getCodeFilesystemPath());

      List<String> testfileNames = agent.getFilenames();
      assertEquals(testResources.size(), testfileNames.size());
      
      for (String currFilename : testfileNames) {
        assertTrue(writtenFileNames.containsKey(currFilename), currFilename);
        String currResource = writtenFileNames.get(currFilename);
        
        try (InputStream in = agent.getFile(currFilename).get();) {
          try (InputStream inOrig = getClass().getResourceAsStream(currResource)) {
            assertEqualStreams(inOrig, in, currFilename);
          }
        }
      }
      
      
    });
  }
  
  
  @Test
  public void testCreateSimpleActionAgent() throws Exception {
    withTempDb((db) -> {
      DbDesign dbDesign = db.getDesign();
      DesignSimpleActionAgent agent = dbDesign.createAgent(DesignSimpleActionAgent.class, "simpleactions"); //$NON-NLS-1$
      assertNotNull(agent);

      // test for simple action agent is missing; we don't have APIs to write simple actions yet

    });
  }
}
