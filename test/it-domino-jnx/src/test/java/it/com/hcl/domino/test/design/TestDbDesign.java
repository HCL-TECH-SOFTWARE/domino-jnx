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
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.design.CollectionDesignElement;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.DbProperties;
import com.hcl.domino.design.FileResource;
import com.hcl.domino.design.Folder;
import com.hcl.domino.design.Form;
import com.hcl.domino.design.ImageResource;
import com.hcl.domino.design.Subform;
import com.hcl.domino.design.SubformReference;
import com.hcl.domino.design.View;
import com.hcl.domino.design.format.FieldListDelimiter;
import com.hcl.domino.design.format.FieldListDisplayDelimiter;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.FormField;
import com.hcl.domino.richtext.process.GetImageResourceSizeProcessor;
import com.ibm.commons.util.io.StreamUtil;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDbDesign extends AbstractNotesRuntimeTest {
  private static String dbPath;

  @AfterAll
  public static void termDesignDb() {
    try {
      Files.deleteIfExists(Paths.get(TestDbDesign.dbPath));
    } catch (final Throwable t) {
      System.err.println("Unable to delete database " + TestDbDesign.dbPath + ": " + t);
    }
  }

  private Database database;

  @Test
  public void getGetDbDesign() throws Exception {
    this.withTempDb(database -> {
      Assertions.assertNotNull(database.getDesign());
    });
  }

  @BeforeEach
  public void initDesignDb() throws IOException, URISyntaxException {
    if (this.database == null) {
      final DominoClient client = this.getClient();
      if (TestDbDesign.dbPath == null) {
        this.database = AbstractNotesRuntimeTest.createTempDb(client);
        TestDbDesign.dbPath = this.database.getAbsoluteFilePath();
        AbstractNotesRuntimeTest.populateResourceDxl("/dxl/testDbDesign", this.database);
      } else {
        this.database = client.openDatabase("", TestDbDesign.dbPath);
      }
    }
  }

  @Test
  public void testCreateFolder() throws Exception {
    this.withTempDb(database -> {
      final DbDesign design = database.getDesign();
      final Folder element = design.createFolder("foo bar");
      Assertions.assertNotNull(element);
      Assertions.assertEquals("foo bar", element.getTitle());

      Assertions.assertNull(design.getFolder("foo bar").orElse(null));
      element.save();
      Assertions.assertNotNull(design.getFolder("foo bar").orElse(null));
      Assertions.assertNull(design.getView("foo bar").orElse(null));
      element.setTitle("other title");
      element.save();
      Assertions.assertNull(design.getFolder("foo bar").orElse(null));
      Assertions.assertNotNull(design.getFolder("other title").orElse(null));
    });
  }

  @Test
  public void testCreateForm() throws Exception {
    this.withTempDb(database -> {
      final DbDesign design = database.getDesign();
      {
        final Form element = design.createForm("foo bar");
        Assertions.assertNotNull(element);
        Assertions.assertEquals("foo bar", element.getTitle());

        Assertions.assertNull(design.getForm("foo bar").orElse(null));
        element.save();
        Assertions.assertNotNull(design.getForm("foo bar").orElse(null));
        Assertions.assertNull(design.getSubform("foo bar").orElse(null));
        element.setTitle("other title");
        element.save();
        Assertions.assertNull(design.getForm("foo bar").orElse(null));
        Assertions.assertNotNull(design.getForm("other title").orElse(null));

        element.setHideFromMobile(true);
        element.save();
      }
      {
        final Form element = design.getForm("other title").orElse(null);
        Assertions.assertNotNull(element);
        Assertions.assertTrue(element.isHideFromMobile());
      }
    });
  }

  @Test
  public void testCreateSubform() throws Exception {
    this.withTempDb(database -> {
      final DbDesign design = database.getDesign();
      final Subform element = design.createSubform("foo bar");
      Assertions.assertNotNull(element);
      Assertions.assertEquals("foo bar", element.getTitle());

      Assertions.assertNull(design.getSubform("foo bar").orElse(null));
      element.save();
      Assertions.assertNotNull(design.getSubform("foo bar").orElse(null));
      Assertions.assertNull(design.getForm("foo bar").orElse(null));
      element.setTitle("other title");
      element.save();
      Assertions.assertNull(design.getSubform("foo bar").orElse(null));
      Assertions.assertNotNull(design.getSubform("other title").orElse(null));
    });
  }

  @Test
  public void testCreateView() throws Exception {
    this.withTempDb(database -> {
      final DbDesign design = database.getDesign();
      final View element = design.createView("foo bar");
      Assertions.assertNotNull(element);
      Assertions.assertEquals("foo bar", element.getTitle());

      Assertions.assertNull(design.getView("foo bar").orElse(null));
      element.save();
      Assertions.assertNotNull(design.getView("foo bar").orElse(null));
      Assertions.assertNull(design.getFolder("foo bar").orElse(null));
      element.setTitle("other title");
      element.save();
      Assertions.assertNull(design.getView("foo bar").orElse(null));
      Assertions.assertNotNull(design.getCollection("other title").orElse(null));
    });
  }

  @Test
  public void testDbProperties() {
    final DbDesign dbDesign = this.database.getDesign();
    final DbProperties props = dbDesign.getDatabaseProperties();
    Assertions.assertNotNull(props);

    Assertions.assertFalse(props.isGenerateEnhancedHtml());
    props.setGenerateEnhancedHtml(true);
    Assertions.assertTrue(props.isGenerateEnhancedHtml());
    props.setGenerateEnhancedHtml(false);
    Assertions.assertFalse(props.isGenerateEnhancedHtml());
  }

  @Test
  public void testFileResourceFileCss() throws IOException {
    final DbDesign dbDesign = this.database.getDesign();
    final FileResource res = dbDesign.getFileResource("file.css").get();
    Assertions.assertEquals("file.css", res.getTitle());
    Assertions.assertEquals("text/css", res.getMimeType());
    Assertions.assertEquals("Windows-1252", res.getCharsetName());

    String content;
    try (InputStream is = res.getFileData()) {
      content = StreamUtil.readString(is);
    }
    Assertions.assertEquals("/* I'm a file resource named CSS */", content);
  }

  @Test
  public void testFileResourceFileCssQuery() throws IOException {
    final DbDesign dbDesign = this.database.getDesign();
    final FileResource res = (FileResource) dbDesign.queryDesignElements("$TITLE='file.css'").findFirst().get();
    Assertions.assertEquals("file.css", res.getTitle());
    Assertions.assertEquals("text/css", res.getMimeType());
    Assertions.assertEquals("Windows-1252", res.getCharsetName());
    Assertions.assertEquals(res.getDocument().getLastModified().toTemporal().get(), res.getFileModified().toTemporal().get());

    String content;
    try (InputStream is = res.getFileData()) {
      content = StreamUtil.readString(is);
    }
    Assertions.assertEquals("/* I'm a file resource named CSS */", content);
  }

  @Test
  public void testFileResourceLargelsTxt() throws IOException {
    final DbDesign dbDesign = this.database.getDesign();
    final FileResource res = dbDesign.getFileResource("largels.txt").get();
    Assertions.assertEquals("largels.txt", res.getTitle());
    Assertions.assertEquals("text/plain", res.getMimeType());
    Assertions.assertEquals("UTF-8", res.getCharsetName());

    String expected;
    try (InputStream is = this.getClass().getResourceAsStream("/text/largels_crlf.txt")) {
      expected = StreamUtil.readString(is);
    }

    String content;
    try (InputStream is = res.getFileData()) {
      content = StreamUtil.readString(is);
    }
    Assertions.assertEquals(expected.replace("\r\n", "\n"), content.replace("\r\n", "\n"));
  }

  @Test
  public void testFileResources() {
    final DbDesign dbDesign = this.database.getDesign();
    final List<FileResource> resources = dbDesign.getFileResources().collect(Collectors.toList());
    Assertions.assertEquals(3, resources.size());

    Assertions.assertTrue(resources.stream().anyMatch(res -> Arrays.asList("file.css").equals(res.getFileNames())));
    Assertions.assertTrue(resources.stream().anyMatch(res -> Arrays.asList("test.txt").equals(res.getFileNames())));
    Assertions.assertTrue(resources.stream().anyMatch(res -> Arrays.asList("largels.txt").equals(res.getFileNames())));
  }

  @Test
  public void testFileResourceTestTxt() throws IOException {
    final DbDesign dbDesign = this.database.getDesign();
    final FileResource res = dbDesign.getFileResource("test.txt").get();
    Assertions.assertEquals("test.txt", res.getTitle());
    Assertions.assertEquals("text/plain", res.getMimeType());
    Assertions.assertEquals("UTF-8", res.getCharsetName());

    // 20210619T150226,17-04
    final OffsetDateTime expected = OffsetDateTime.of(2021, 6, 19, 14, 2, 26, 17 * 1000 * 1000 * 10, ZoneOffset.ofHours(-5));
    Assertions.assertEquals(expected, res.getFileModified().toOffsetDateTime());

    String content;
    try (InputStream is = res.getFileData()) {
      content = StreamUtil.readString(is);
    }
    Assertions.assertEquals("I am test text", content);
  }

  @Test
  public void testFolders() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<CollectionDesignElement> collections = dbDesign.getFolders().collect(Collectors.toList());
    Assertions.assertEquals(1, collections.size());

    {
      CollectionDesignElement view = collections.stream().filter(v -> "test folder".equals(v.getTitle())).findFirst().orElse(null);
      Assertions.assertNotNull(view);

      view = dbDesign.getCollection("test folder").orElse(null);
      Assertions.assertNotNull(view);
      Assertions.assertEquals("test folder", view.getTitle());
    }
  }

  @Test
  public void testFoldersAndViews() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<CollectionDesignElement> collections = dbDesign.getCollections().collect(Collectors.toList());
    Assertions.assertEquals(3, collections.size()); // 2 imported + 1 default view

    {
      CollectionDesignElement view = collections.stream().filter(v -> "test view".equals(v.getTitle())).findFirst().orElse(null);
      Assertions.assertNotNull(view);

      view = dbDesign.getCollection("test view").orElse(null);
      Assertions.assertNotNull(view);
      Assertions.assertEquals("test view", view.getTitle());
    }
    {
      CollectionDesignElement view = collections.stream().filter(v -> "test folder".equals(v.getTitle())).findFirst().orElse(null);
      Assertions.assertNotNull(view);

      view = dbDesign.getCollection("test folder").orElse(null);
      Assertions.assertNotNull(view);
      Assertions.assertEquals("test folder", view.getTitle());
    }
  }

  @Test
  public void testForms() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<Form> forms = dbDesign.getForms().collect(Collectors.toList());
    Assertions.assertEquals(2, forms.size());
    {
      Form form = forms.stream().filter(f -> "Content".equals(f.getTitle())).findFirst().orElse(null);
      Assertions.assertNotNull(form);

      form = dbDesign.getForm("Content").orElse(null);
      Assertions.assertNotNull(form);
      Assertions.assertEquals("Content", form.getTitle());
    }
    {
      final Form form = dbDesign.getForm("Alias").orElse(null);
      Assertions.assertNotNull(form);
      Assertions.assertEquals("Alias", form.getTitle());

      final List<FormField> fields = form.getFields();
      Assertions.assertEquals(5, fields.size());
      Assertions.assertEquals(
          Arrays.asList("Host", "From", "To", "$$Title", "$$Creator"),
          fields.stream().map(FormField::getName).collect(Collectors.toList()));
      {
        final FormField hostField = fields.stream().filter(f -> "Host".equals(f.getName())).findFirst().orElse(null);
        Assertions.assertNotNull(hostField);
        Assertions.assertEquals(EnumSet.of(FieldListDelimiter.COMMA, FieldListDelimiter.SEMICOLON, FieldListDelimiter.NEWLINE),
            hostField.getListInputDelimiters());
        Assertions.assertEquals(FieldListDisplayDelimiter.SEMICOLON, hostField.getListDispayDelimiter());
        Assertions.assertEquals("host-id", hostField.getHtmlId());
        Assertions.assertEquals("host-class", hostField.getHtmlClassName());
        Assertions.assertEquals("host: style", hostField.getHtmlStyle());
        Assertions.assertEquals("host=\"attr\"", hostField.getHtmlExtraAttr());
        Assertions.assertEquals("Name", hostField.getHtmlTitle());
        Assertions.assertEquals(ItemDataType.TYPE_TEXT_LIST, hostField.getDataType().get());
      }

      // Check for its two subforms
      final List<SubformReference> subforms = form.getSubforms();
      Assertions.assertEquals(2, subforms.size());
      {
        final SubformReference computed = subforms.get(0);
        Assertions.assertEquals(SubformReference.Type.FORMULA, computed.getType());
        Assertions.assertEquals("@If(@True; \"Computed Target\"; \"Computed Target\")", computed.getValue());
      }
      {
        final SubformReference explicit = subforms.get(1);
        Assertions.assertEquals(SubformReference.Type.EXPLICIT, explicit.getType());
        Assertions.assertEquals("Footer", explicit.getValue());
      }
    }

    Assertions.assertNull(dbDesign.getForm("Printer Agent").orElse(null));
  }

  @Test
  public void testImageResources() {
    final DbDesign dbDesign = this.database.getDesign();
    final List<ImageResource> resources = dbDesign.getImageResources().collect(Collectors.toList());
    Assertions.assertEquals(2, resources.size());

    Assertions.assertTrue(resources.stream().anyMatch(res -> Arrays.asList("Untitled.gif").equals(res.getFileNames())));
    // The copied image resource is known to be broken, as an effect of a Designer
    // bug. That leaves it as useful
    // test data, but not for this check
    // assertTrue(resources.stream().anyMatch(res -> Arrays.asList("Untitled
    // 2.gif").equals(res.getFileNames())));
    Assertions.assertTrue(resources.stream().anyMatch(res -> "Untitled.gif".equals(res.getTitle())));
  }

  @Test
  public void testImageResUntitled() throws IOException {
    final DbDesign dbDesign = this.database.getDesign();
    final ImageResource res = dbDesign.getImageResource("Untitled.gif").get();
    Assertions.assertEquals("Untitled.gif", res.getTitle());
    Assertions.assertEquals("image/gif", res.getMimeType());
    Assertions.assertFalse(res.isWebReadOnly());
    Assertions.assertFalse(res.isWebCompatible());
    Assertions.assertEquals(1, res.getImagesDown());
    Assertions.assertEquals(1, res.getImagesAcross());
    Assertions.assertEquals(890, res.getFileSize());
    Assertions.assertEquals(890,
        new GetImageResourceSizeProcessor().apply(res.getDocument().getRichTextItem(NotesConstants.ITEM_NAME_IMAGE_DATA)));

    final byte[] expected = IOUtils.resourceToByteArray("/images/Untitled.gif");

    byte[] content;
    try (InputStream is = res.getFileData()) {
      content = IOUtils.toByteArray(is);
    }
    Assertions.assertArrayEquals(expected, content);
  }

  @Test
  public void testImageResUntitled2() throws IOException {
    final DbDesign dbDesign = this.database.getDesign();
    final ImageResource res = dbDesign.getImageResource("Untitled 2.gif").get();
    Assertions.assertEquals("Untitled 2.gif", res.getTitle());
    Assertions.assertEquals("image/gif", res.getMimeType());
    Assertions.assertTrue(res.isWebReadOnly());
    Assertions.assertTrue(res.isWebCompatible());
    Assertions.assertEquals(2, res.getImagesDown());
    Assertions.assertEquals(4, res.getImagesAcross());

    // This is known to have diverged, where the $FileSize reflects one of the image
    // tiles for some reason
    Assertions.assertEquals(890, res.getFileSize());
    Assertions.assertEquals(839, res.getDocument().get("$FileSize", int.class, 0));
    Assertions.assertEquals(890,
        GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem(NotesConstants.ITEM_NAME_IMAGE_DATA)));

    // The images-down/across values caused the creation of many $ImageData values
    Assertions.assertEquals(824, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem("$ImageData0000")));
    Assertions.assertEquals(833, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem("$ImageData0001")));
    Assertions.assertEquals(835, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem("$ImageData0002")));
    Assertions.assertEquals(839, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem("$ImageData0003")));
    Assertions.assertEquals(824, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem("$ImageData0100")));
    Assertions.assertEquals(833, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem("$ImageData0101")));
    Assertions.assertEquals(824, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem("$ImageData0102")));
    Assertions.assertEquals(839, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem("$ImageData0103")));

    final byte[] expected = IOUtils.resourceToByteArray("/images/Untitled.gif");

    byte[] content;
    try (InputStream is = res.getFileData()) {
      content = IOUtils.toByteArray(is);
    }
    Assertions.assertArrayEquals(expected, content);
  }

  @Test
  public void testSubforms() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<Subform> forms = dbDesign.getSubforms().collect(Collectors.toList());
    Assertions.assertEquals(2, forms.size());
    {
      Subform subform = forms.stream().filter(f -> "Footer".equals(f.getTitle())).findFirst().orElse(null);
      Assertions.assertNotNull(subform);

      subform = dbDesign.getSubform("Footer").orElse(null);
      Assertions.assertNotNull(subform);
      Assertions.assertEquals("Footer", subform.getTitle());
    }

    Assertions.assertNull(dbDesign.getSubform("Content").orElse(null));
  }

  @Test
  public void testViews() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<CollectionDesignElement> collections = dbDesign.getViews().collect(Collectors.toList());
    Assertions.assertEquals(2, collections.size()); // 1 imported + 1 default view

    {
      CollectionDesignElement view = collections.stream().filter(v -> "test view".equals(v.getTitle())).findFirst().orElse(null);
      Assertions.assertNotNull(view);

      view = dbDesign.getCollection("test view").orElse(null);
      Assertions.assertNotNull(view);
      Assertions.assertEquals("test view", view.getTitle());
      Assertions.assertEquals("8.5.3", view.getDesignerVersion());

      {
        Assertions.assertTrue(view.isProhibitRefresh());
        view.setProhibitRefresh(false);
        Assertions.assertFalse(view.isProhibitRefresh());
        view.setProhibitRefresh(true);
        Assertions.assertTrue(view.isProhibitRefresh());
      }

      {
        Assertions.assertFalse(view.isHideFromWeb());
        view.setHideFromWeb(true);
        Assertions.assertTrue(view.isHideFromWeb());
        view.setHideFromWeb(false);
        Assertions.assertFalse(view.isHideFromWeb());
      }
      {
        Assertions.assertFalse(view.isHideFromNotes());
        view.setHideFromNotes(true);
        Assertions.assertTrue(view.isHideFromNotes());
        view.setHideFromNotes(false);
        Assertions.assertFalse(view.isHideFromNotes());
      }
      {
        Assertions.assertFalse(view.isHideFromMobile());
        view.setHideFromMobile(true);
        Assertions.assertTrue(view.isHideFromMobile());
        view.setHideFromMobile(false);
        Assertions.assertFalse(view.isHideFromMobile());
      }
    }
  }
}
