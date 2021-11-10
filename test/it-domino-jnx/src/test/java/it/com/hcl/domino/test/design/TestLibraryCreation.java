package it.com.hcl.domino.test.design;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.junit.jupiter.api.Test;

import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.DesignElement;
import com.hcl.domino.design.JavaLibrary;
import com.hcl.domino.design.JavaScriptLibrary;
import com.hcl.domino.design.LotusScriptLibrary;
import com.hcl.domino.design.ServerJavaScriptLibrary;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

public class TestLibraryCreation extends AbstractNotesRuntimeTest {

  @SuppressWarnings("nls")
  @Test
  public void testCreateLSLibrary() throws Exception {
    withTempDb((db) -> {
      DbDesign dbDesign = db.getDesign();

      LotusScriptLibrary lib = dbDesign.createScriptLibrary(LotusScriptLibrary.class, "lslib");
      String script = "%REM\n\tLibrary lslib\n"
          + "\tDescription: Comments for Library\n"
          + "%END REM\nOption Public\n"
          + "Option Declare\n"
          + "\n"
          + "\n"
          + "Declare Sub Initialize\n"
          + "\n"
          + "Sub Initialize\n"
          + "\tDim session As New NotesSession\n"
          + "End Sub\n";
      lib.setScript(script);

      lib.sign();
      lib.save();

      String unid = lib.getDocument().getUNID();

      DesignElement testDE = dbDesign.getDesignElementByUNID(unid).orElseThrow(() -> new IllegalStateException("Library not found via UNID"));
      assertInstanceOf(LotusScriptLibrary.class, testDE);
      lib = (LotusScriptLibrary) testDE;

      String testScript = lib.getScript();
      assertNotNull(testScript);
      assertTrue(testScript.contains("Comments for Library"));
    });
  }

  @SuppressWarnings("nls")
  @Test
  public void testCreateJavaLibrary() throws Exception {
    withTempDb((db) -> {
      String mainClassName = "JavaAgent.class";
      String sourceTarget = "1.3";
      String objectTarget = "1.2";
      String codeFilesystemPath = "c:\\data";
      List<String> sharedLibs = Arrays.asList("lib1", "lib2");

      DbDesign dbDesign = db.getDesign();
      JavaLibrary lib = dbDesign.createScriptLibrary(JavaLibrary.class, "javalib");
      assertNotNull(lib);

      //write initial source/object jars
      lib.initJavaContent();
      lib.setMainClassName(mainClassName);
      lib.setJavaCompilerSource(sourceTarget);
      lib.setJavaCompilerTarget(objectTarget);
      lib.setCodeFilesystemPath(codeFilesystemPath);
      lib.setSharedLibraryList(sharedLibs);

      lib.sign();
      lib.save();

      String unid = lib.getDocument().getUNID();

      DesignElement testDE = dbDesign.getDesignElementByUNID(unid).orElseThrow(() -> new IllegalStateException("Library not found via UNID"));
      assertInstanceOf(JavaLibrary.class, testDE);
      lib = (JavaLibrary) testDE;

      assertEquals(mainClassName, lib.getMainClassName());
      assertEquals(sourceTarget, lib.getJavaCompilerSource());
      assertEquals(objectTarget, lib.getJavaCompilerTarget());
      assertEquals(codeFilesystemPath, lib.getCodeFilesystemPath());
      assertEquals(sharedLibs, lib.getSharedLibraryList());

      {
        final String LIB_DEFAULT_SOURCE_JAR_RESOURCEPATH = "/com/hcl/domino/commons/design/initialdesign/javalibrary/%%source%%.jar";
        InputStream inOrig = getClass().getResourceAsStream(LIB_DEFAULT_SOURCE_JAR_RESOURCEPATH);
        assertNotNull(inOrig);

        InputStream inDesign = lib.getSourceAttachment().orElse(null);
        assertNotNull(inOrig);

        assertEqualStreams(inOrig, inDesign, "Source attachment mismatch");
      }
      {
        final String LIB_DEFAULT_OBJECT_JAR_RESOURCEPATH = "/com/hcl/domino/commons/design/initialdesign/javalibrary/%%object%%.jar";
        InputStream inOrig = getClass().getResourceAsStream(LIB_DEFAULT_OBJECT_JAR_RESOURCEPATH);
        assertNotNull(inOrig);

        InputStream inDesign = lib.getObjectAttachment().orElse(null);
        assertNotNull(inOrig);

        assertEqualStreams(inOrig, inDesign, "Object attachment mismatch");
      }

      {
        //build resources jar on the fly
        final String[] testResources = {
            "/images/file-icon.gif",
            "/images/help_vampire.gif"
        };
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        ByteArrayOutputStream jarOut = new ByteArrayOutputStream();

        try (JarOutputStream target = new JarOutputStream(jarOut, manifest);) {
          for (String currResource : testResources) {
            int iPos = currResource.lastIndexOf("/");
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

        lib.setResourceAttachment(new ByteArrayInputStream(jarOut.toByteArray()));
        assertTrue(StringUtil.isNotEmpty(lib.getResourcesAttachmentName().get()));

        assertEqualStreams(new ByteArrayInputStream(jarOut.toByteArray()), lib.getResourcesAttachment().get(),
            "Resource attachment mismatch");
      }
    });
  }

  @SuppressWarnings("nls")
  @Test
  public void testCreateSSJSLibrary() throws Exception {
    withTempDb((db) -> {
      DbDesign dbDesign = db.getDesign();
      ServerJavaScriptLibrary lib = dbDesign.createScriptLibrary(ServerJavaScriptLibrary.class, "ssjslib");
      assertNotNull(lib);

      String script = "function test() {\n"
          + "  \n"
          + "}";
      lib.setScript(script);

      lib.sign();
      lib.save();

      String unid = lib.getDocument().getUNID();

      DesignElement testDE = dbDesign.getDesignElementByUNID(unid).orElseThrow(() -> new IllegalStateException("Library not found via UNID"));
      assertInstanceOf(ServerJavaScriptLibrary.class, testDE);
      lib = (ServerJavaScriptLibrary) testDE;

      String testScript = lib.getScript();
      assertNotNull(testScript);
      assertTrue(testScript.contains("test()"));
    });
  }

  @SuppressWarnings("nls")
  @Test
  public void testCreateJSLibrary() throws Exception {
    withTempDb((db) -> {
      DbDesign dbDesign = db.getDesign();
      JavaScriptLibrary lib = dbDesign.createScriptLibrary(JavaScriptLibrary.class, "jslib");
      assertNotNull(lib);

      String script = "function test() {\n"
          + "  \n"
          + "}";
      lib.setScript(script);

      lib.sign();
      lib.save();

      String unid = lib.getDocument().getUNID();

      DesignElement testDE = dbDesign.getDesignElementByUNID(unid).orElseThrow(() -> new IllegalStateException("Library not found via UNID"));
      assertInstanceOf(JavaScriptLibrary.class, testDE);
      lib = (JavaScriptLibrary) testDE;

      String testScript = lib.getScript();
      assertNotNull(testScript);
      assertTrue(testScript.contains("test()"));
    });
  }

}
