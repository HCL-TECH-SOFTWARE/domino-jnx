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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.design.AboutDocument;
import com.hcl.domino.design.ActionBar;
import com.hcl.domino.design.CollectionDesignElement;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.DbProperties;
import com.hcl.domino.design.FileResource;
import com.hcl.domino.design.Folder;
import com.hcl.domino.design.Form;
import com.hcl.domino.design.ImageResource;
import com.hcl.domino.design.Page;
import com.hcl.domino.design.SharedActions;
import com.hcl.domino.design.SharedField;
import com.hcl.domino.design.Subform;
import com.hcl.domino.design.SubformReference;
import com.hcl.domino.design.UsingDocument;
import com.hcl.domino.design.View;
import com.hcl.domino.design.action.ActionBarAction;
import com.hcl.domino.design.action.ActionContent;
import com.hcl.domino.design.action.EventId;
import com.hcl.domino.design.action.FormulaActionContent;
import com.hcl.domino.design.action.ScriptEvent;
import com.hcl.domino.design.action.SimpleActionActionContent;
import com.hcl.domino.design.action.ActionBarAction.IconType;
import com.hcl.domino.design.format.ActionBarControlType;
import com.hcl.domino.design.format.FieldListDelimiter;
import com.hcl.domino.design.format.FieldListDisplayDelimiter;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.FormField;
import com.hcl.domino.richtext.process.GetImageResourceSizeProcessor;
import com.hcl.domino.richtext.records.CDEmbeddedOutline;
import com.hcl.domino.richtext.records.CDField;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.records.CDText;
import com.ibm.commons.util.io.StreamUtil;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDbDesign extends AbstractDesignTest {
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
      assertNotNull(database.getDesign());
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
      assertNotNull(element);
      assertEquals("foo bar", element.getTitle());

      Assertions.assertNull(design.getFolder("foo bar").orElse(null));
      element.save();
      assertNotNull(design.getFolder("foo bar").orElse(null));
      Assertions.assertNull(design.getView("foo bar").orElse(null));
      element.setTitle("other title");
      element.save();
      Assertions.assertNull(design.getFolder("foo bar").orElse(null));
      assertNotNull(design.getFolder("other title").orElse(null));
    });
  }

  @Test
  public void testCreateForm() throws Exception {
    this.withTempDb(database -> {
      final DbDesign design = database.getDesign();
      {
        final Form element = design.createForm("foo bar");
        assertNotNull(element);
        assertEquals("foo bar", element.getTitle());

        Assertions.assertNull(design.getForm("foo bar").orElse(null));
        element.save();
        assertNotNull(design.getForm("foo bar").orElse(null));
        Assertions.assertNull(design.getSubform("foo bar").orElse(null));
        element.setTitle("other title");
        element.save();
        Assertions.assertNull(design.getForm("foo bar").orElse(null));
        assertNotNull(design.getForm("other title").orElse(null));

        element.setHideFromMobile(true);
        element.save();
      }
      {
        final Form element = design.getForm("other title").orElse(null);
        assertNotNull(element);
        assertTrue(element.isHideFromMobile());
      }
    });
  }

  @Test
  public void testCreateSubform() throws Exception {
    this.withTempDb(database -> {
      final DbDesign design = database.getDesign();
      final Subform element = design.createSubform("foo bar");
      assertNotNull(element);
      assertEquals("foo bar", element.getTitle());

      Assertions.assertNull(design.getSubform("foo bar").orElse(null));
      element.save();
      assertNotNull(design.getSubform("foo bar").orElse(null));
      Assertions.assertNull(design.getForm("foo bar").orElse(null));
      element.setTitle("other title");
      element.save();
      Assertions.assertNull(design.getSubform("foo bar").orElse(null));
      assertNotNull(design.getSubform("other title").orElse(null));
    });
  }

  @Test
  public void testCreateView() throws Exception {
    this.withTempDb(database -> {
      final DbDesign design = database.getDesign();
      final View element = design.createView("foo bar");
      assertNotNull(element);
      assertEquals("foo bar", element.getTitle());

      Assertions.assertNull(design.getView("foo bar").orElse(null));
      element.save();
      assertNotNull(design.getView("foo bar").orElse(null));
      Assertions.assertNull(design.getFolder("foo bar").orElse(null));
      element.setTitle("other title");
      element.save();
      Assertions.assertNull(design.getView("foo bar").orElse(null));
      assertNotNull(design.getCollection("other title").orElse(null));
    });
  }

  @Test
  public void testDbProperties() {
    final DbDesign dbDesign = this.database.getDesign();
    final DbProperties props = dbDesign.getDatabaseProperties();
    assertNotNull(props);

    assertFalse(props.isGenerateEnhancedHtml());
    props.setGenerateEnhancedHtml(true);
    assertTrue(props.isGenerateEnhancedHtml());
    props.setGenerateEnhancedHtml(false);
    assertFalse(props.isGenerateEnhancedHtml());
    
    String unid = props.getDocument().getUNID();
    Optional<DbProperties> optProps = dbDesign.getDesignElementByUNID(unid);
    assertInstanceOf(DbProperties.class, optProps.get());
  }

  @Test
  public void testFileResourceFileCss() throws IOException {
    final DbDesign dbDesign = this.database.getDesign();
    final FileResource res = dbDesign.getFileResource("file.css").get();
    assertEquals("file.css", res.getTitle());
    assertEquals("text/css", res.getMimeType());
    assertEquals("Windows-1252", res.getCharsetName());

    String content;
    try (InputStream is = res.getFileData()) {
      content = StreamUtil.readString(is);
    }
    assertEquals("/* I'm a file resource named CSS */", content);
  }

  @Test
  public void testFileResourceFileCssQuery() throws IOException {
    final DbDesign dbDesign = this.database.getDesign();
    final FileResource res = (FileResource) dbDesign.queryDesignElements("$TITLE='file.css'").findFirst().get();
    assertEquals("file.css", res.getTitle());
    assertEquals("text/css", res.getMimeType());
    assertEquals("Windows-1252", res.getCharsetName());
    assertEquals(res.getDocument().getLastModified().toTemporal().get(), res.getFileModified().toTemporal().get());

    String content;
    try (InputStream is = res.getFileData()) {
      content = StreamUtil.readString(is);
    }
    assertEquals("/* I'm a file resource named CSS */", content);
  }

  @Test
  public void testFileResourceLargelsTxt() throws IOException {
    final DbDesign dbDesign = this.database.getDesign();
    final FileResource res = dbDesign.getFileResource("largels.txt").get();
    assertEquals("largels.txt", res.getTitle());
    assertEquals("text/plain", res.getMimeType());
    assertEquals("UTF-8", res.getCharsetName());

    String expected;
    try (InputStream is = this.getClass().getResourceAsStream("/text/largels_crlf.txt")) {
      expected = StreamUtil.readString(is);
    }

    String content;
    try (InputStream is = res.getFileData()) {
      content = StreamUtil.readString(is);
    }
    assertEquals(expected.replace("\r\n", "\n"), content.replace("\r\n", "\n"));
  }

  @Test
  public void testFileResources() {
    final DbDesign dbDesign = this.database.getDesign();
    final List<FileResource> resources = dbDesign.getFileResources().collect(Collectors.toList());
    assertEquals(3, resources.size());

    assertTrue(resources.stream().anyMatch(res -> Arrays.asList("file.css").equals(res.getFileNames())));
    assertTrue(resources.stream().anyMatch(res -> Arrays.asList("test.txt").equals(res.getFileNames())));
    assertTrue(resources.stream().anyMatch(res -> Arrays.asList("largels.txt").equals(res.getFileNames())));
  }

  @Test
  public void testFileResourceTestTxt() throws IOException {
    final DbDesign dbDesign = this.database.getDesign();
    final FileResource res = dbDesign.getFileResource("test.txt").get();
    assertEquals("test.txt", res.getTitle());
    assertEquals("text/plain", res.getMimeType());
    assertEquals("UTF-8", res.getCharsetName());

    // 20210619T150226,17-04
    final OffsetDateTime expected = OffsetDateTime.of(2021, 6, 19, 14, 2, 26, 17 * 1000 * 1000 * 10, ZoneOffset.ofHours(-5));
    assertEquals(expected, res.getFileModified().toOffsetDateTime());

    String content;
    try (InputStream is = res.getFileData()) {
      content = StreamUtil.readString(is);
    }
    assertEquals("I am test text", content);
  }

  @Test
  public void testFolders() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<CollectionDesignElement> collections = dbDesign.getFolders().collect(Collectors.toList());
    assertEquals(1, collections.size());

    {
      CollectionDesignElement view = collections.stream().filter(v -> "test folder".equals(v.getTitle())).findFirst().orElse(null);
      assertNotNull(view);

      view = dbDesign.getCollection("test folder").orElse(null);
      assertNotNull(view);
      assertEquals("test folder", view.getTitle());
    }
  }

  @Test
  public void testFoldersAndViews() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<CollectionDesignElement> collections = dbDesign.getCollections().collect(Collectors.toList());
    assertEquals(3, collections.size()); // 2 imported + 1 default view

    {
      CollectionDesignElement view = collections.stream().filter(v -> "test view".equals(v.getTitle())).findFirst().orElse(null);
      assertNotNull(view);

      view = dbDesign.getCollection("test view").orElse(null);
      assertNotNull(view);
      assertEquals("test view", view.getTitle());
    }
    {
      CollectionDesignElement view = collections.stream().filter(v -> "test folder".equals(v.getTitle())).findFirst().orElse(null);
      assertNotNull(view);

      view = dbDesign.getCollection("test folder").orElse(null);
      assertNotNull(view);
      assertEquals("test folder", view.getTitle());
    }
  }

  @Test
  public void testForms() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<Form> forms = dbDesign.getForms().collect(Collectors.toList());
    assertEquals(2, forms.size());
    {
      Form form = forms.stream().filter(f -> "Content".equals(f.getTitle())).findFirst().orElse(null);
      assertNotNull(form);

      form = dbDesign.getForm("Content").orElse(null);
      assertNotNull(form);
      assertEquals("Content", form.getTitle());
    }
    {
      final Form form = dbDesign.getForm("Alias").orElse(null);
      assertNotNull(form);
      assertEquals("Alias", form.getTitle());

      final List<FormField> fields = form.getFields();
      assertEquals(5, fields.size());
      assertEquals(
          Arrays.asList("Host", "From", "To", "$$Title", "$$Creator"),
          fields.stream().map(FormField::getName).collect(Collectors.toList()));
      {
        final FormField hostField = fields.stream().filter(f -> "Host".equals(f.getName())).findFirst().orElse(null);
        assertNotNull(hostField);
        assertEquals(EnumSet.of(FieldListDelimiter.COMMA, FieldListDelimiter.SEMICOLON, FieldListDelimiter.NEWLINE),
            hostField.getListInputDelimiters());
        assertEquals(FieldListDisplayDelimiter.SEMICOLON, hostField.getListDispayDelimiter());
        assertEquals("host-id", hostField.getHtmlId());
        assertEquals("host-class", hostField.getHtmlClassName());
        assertEquals("host: style", hostField.getHtmlStyle());
        assertEquals("host=\"attr\"", hostField.getHtmlExtraAttr());
        assertEquals("Name", hostField.getHtmlTitle());
        assertEquals(ItemDataType.TYPE_TEXT_LIST, hostField.getDataType().get());
      }

      // Check for its two subforms
      final List<SubformReference> subforms = form.getSubforms();
      assertEquals(2, subforms.size());
      {
        final SubformReference computed = subforms.get(0);
        assertEquals(SubformReference.Type.FORMULA, computed.getType());
        assertEquals("@If(@True; \"Computed Target\"; \"Computed Target\")", computed.getValue());
      }
      {
        final SubformReference explicit = subforms.get(1);
        assertEquals(SubformReference.Type.EXPLICIT, explicit.getType());
        assertEquals("Footer", explicit.getValue());
      }
    }

    Assertions.assertNull(dbDesign.getForm("Printer Agent").orElse(null));
  }

  @Test
  public void testImageResources() {
    final DbDesign dbDesign = this.database.getDesign();
    final List<ImageResource> resources = dbDesign.getImageResources().collect(Collectors.toList());
    assertEquals(2, resources.size());

    assertTrue(resources.stream().anyMatch(res -> Arrays.asList("Untitled.gif").equals(res.getFileNames())));
    // The copied image resource is known to be broken, as an effect of a Designer
    // bug. That leaves it as useful test data, but not for this check
    // assertTrue(resources.stream().anyMatch(res -> Arrays.asList("Untitled
    // 2.gif").equals(res.getFileNames())));
    assertTrue(resources.stream().anyMatch(res -> "Untitled.gif".equals(res.getTitle())));
    
    String unid = resources.stream().filter(res -> "Untitled.gif".equals(res.getTitle())).map(res -> res.getDocument().getUNID()).findFirst().get();
    Optional<ImageResource> untitled = dbDesign.getDesignElementByUNID(unid);
    assertEquals("Untitled.gif", untitled.get().getTitle());
  }

  @Test
  public void testImageResUntitled() throws IOException {
    final DbDesign dbDesign = this.database.getDesign();
    final ImageResource res = dbDesign.getImageResource("Untitled.gif").get();
    assertEquals("Untitled.gif", res.getTitle());
    assertEquals("image/gif", res.getMimeType());
    assertFalse(res.isWebReadOnly());
    assertFalse(res.isWebCompatible());
    assertEquals(1, res.getImagesDown());
    assertEquals(1, res.getImagesAcross());
    assertEquals(890, res.getFileSize());
    assertEquals(890,
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
    assertEquals("Untitled 2.gif", res.getTitle());
    assertEquals("image/gif", res.getMimeType());
    assertTrue(res.isWebReadOnly());
    assertTrue(res.isWebCompatible());
    assertEquals(2, res.getImagesDown());
    assertEquals(4, res.getImagesAcross());

    // This is known to have diverged, where the $FileSize reflects one of the image
    // tiles for some reason
    assertEquals(890, res.getFileSize());
    assertEquals(839, res.getDocument().get("$FileSize", int.class, 0));
    assertEquals(890,
        GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem(NotesConstants.ITEM_NAME_IMAGE_DATA)));

    // The images-down/across values caused the creation of many $ImageData values
    assertEquals(824, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem("$ImageData0000")));
    assertEquals(833, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem("$ImageData0001")));
    assertEquals(835, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem("$ImageData0002")));
    assertEquals(839, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem("$ImageData0003")));
    assertEquals(824, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem("$ImageData0100")));
    assertEquals(833, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem("$ImageData0101")));
    assertEquals(824, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem("$ImageData0102")));
    assertEquals(839, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem("$ImageData0103")));

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
    assertEquals(2, forms.size());
    {
      Subform subform = forms.stream().filter(f -> "Footer".equals(f.getTitle())).findFirst().orElse(null);
      assertNotNull(subform);

      subform = dbDesign.getSubform("Footer").orElse(null);
      assertNotNull(subform);
      assertEquals("Footer", subform.getTitle());
    }

    Assertions.assertNull(dbDesign.getSubform("Content").orElse(null));
  }

  @Test
  public void testViews() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<CollectionDesignElement> collections = dbDesign.getViews().collect(Collectors.toList());
    assertEquals(2, collections.size()); // 1 imported + 1 default view

    {
      CollectionDesignElement view = collections.stream().filter(v -> "test view".equals(v.getTitle())).findFirst().orElse(null);
      assertNotNull(view);

      view = dbDesign.getCollection("test view").orElse(null);
      assertNotNull(view);
      assertEquals("test view", view.getTitle());
      assertEquals("8.5.3", view.getDesignerVersion());

      {
        assertTrue(view.isProhibitRefresh());
        view.setProhibitRefresh(false);
        assertFalse(view.isProhibitRefresh());
        view.setProhibitRefresh(true);
        assertTrue(view.isProhibitRefresh());
      }

      {
        assertFalse(view.isHideFromWeb());
        view.setHideFromWeb(true);
        assertTrue(view.isHideFromWeb());
        view.setHideFromWeb(false);
        assertFalse(view.isHideFromWeb());
      }
      {
        assertFalse(view.isHideFromNotes());
        view.setHideFromNotes(true);
        assertTrue(view.isHideFromNotes());
        view.setHideFromNotes(false);
        assertFalse(view.isHideFromNotes());
      }
      {
        assertFalse(view.isHideFromMobile());
        view.setHideFromMobile(true);
        assertTrue(view.isHideFromMobile());
        view.setHideFromMobile(false);
        assertFalse(view.isHideFromMobile());
      }
    }
  }
  
  @Test
  public void testPages() {
    DbDesign design = database.getDesign();
    
    List<Page> pages = design.getPages().collect(Collectors.toList());
    assertEquals(2, pages.size());
    assertTrue(pages.stream().anyMatch(p -> "Navigation Header".equals(p.getTitle())));
    assertTrue(pages.stream().anyMatch(p -> "Test Page".equals(p.getTitle())));
    
    String unid = pages.stream().filter(p -> "Test Page".equals(p.getTitle())).findFirst().map(p -> p.getDocument().getUNID()).get();
    Optional<Page> testPage = design.getDesignElementByUNID(unid);
    assertEquals("Test Page", testPage.get().getTitle());
  }
  
  @Test
  public void testTestPage() throws IOException {
    DbDesign design = database.getDesign();
    
    Page page = design.getPage("Test Page").get();
    assertEquals("Test Page", page.getTitle());
    
    List<?> body = page.getBody();
    assertTrue(
      body.stream()
        .filter(CDText.class::isInstance)
        .map(CDText.class::cast)
        .anyMatch(t -> "I am page content.".equals(t.getText()))
    );
    
    assertFalse(page.isUseInitialFocus());
    assertFalse(page.isFocusOnF6());
    assertFalse(page.isRenderPassThroughHtmlInClient());
    
    Page.WebRenderingSettings web = page.getWebRenderingSettings();
    assertTrue(web.isRenderRichContentOnWeb());
    assertFalse(web.getWebMimeType().isPresent());
    assertFalse(web.getWebCharset().isPresent());
    assertColorEquals(web.getActiveLinkColor(), 255, 0, 0);
    assertColorEquals(web.getUnvisitedLinkColor(), 33, 129, 255);
    assertColorEquals(web.getVisitedLinkColor(), 128, 0, 128);
    
    ActionBar actions = page.getActionBar();
    List<ActionBarAction> actionList = actions.getActions();
    assertEquals(1, actionList.size());
    {
      ActionBarAction action = actionList.get(0);
      assertEquals("fsdffd", action.getName());
      
      ActionContent content = action.getActionContent();
      assertInstanceOf(FormulaActionContent.class, content);
      assertEquals("@StatusBar(\"hey\")", ((FormulaActionContent)content).getFormula());
    }
    
    Collection<ScriptEvent> events = page.getJavaScriptEvents();
    assertEquals(5, events.size());
    
    assertTrue(
      events.stream()
        .filter(evt -> evt.isClient())
        .anyMatch(evt -> "/* I am the Notes JS header */\n".equals(evt.getScript()))
    );
    assertTrue(
      events.stream()
        .filter(evt -> !evt.isClient())
        .anyMatch(evt -> "/* I am the web JS header */\n".equals(evt.getScript()))
    );
    assertTrue(
      events.stream()
        .filter(evt -> evt.isClient())
        .anyMatch(evt -> "/* I'm in-common help */\n".equals(evt.getScript()))
    );
    assertTrue(
      events.stream()
        .filter(evt -> !evt.isClient())
        .anyMatch(evt -> "/* I'm in-common help */\n".equals(evt.getScript()))
    );
    assertTrue(
      events.stream()
        .anyMatch(evt -> "alert(\"I am on dbl click\")\n".equals(evt.getScript()))
    );
    
    String lsExpected = IOUtils.resourceToString("/text/testDbDesign/testPageLs.txt", StandardCharsets.UTF_8);
    assertEquals(lsExpected, page.getLotusScript());
    
    Map<EventId, String> formulas = page.getFormulaEvents();
    assertEquals(1, formulas.size());
    assertEquals("@StatusBar(\"I am page postopen\")", formulas.get(EventId.CLIENT_FORM_POSTOPEN));
    
    CDEmbeddedOutline outline = page.getBody()
      .stream()
      .filter(CDEmbeddedOutline.class::isInstance)
      .map(CDEmbeddedOutline.class::cast)
      .findFirst()
      .get();
    assertEquals(
      EnumSet.of(
        CDEmbeddedOutline.Flag.SHOWTWISTIE, CDEmbeddedOutline.Flag.TREE_STYLE, CDEmbeddedOutline.Flag.HASNAME,
        CDEmbeddedOutline.Flag.HASTARGETFRAME, CDEmbeddedOutline.Flag.EXPAND_SAVED,
        CDEmbeddedOutline.Flag.HASROOTNAME
      ),
      outline.getFlags()
    );
    assertEquals(288, outline.getSubLevelHorizontalOffset());
    assertColorEquals(outline.getSelectionFontColors()[0], 0, 0, 0);
    assertColorEquals(outline.getSelectionFontColors()[1], 130, 193, 104);
    assertColorEquals(outline.getSelectionFontColors()[2], 0, 193, 194);
    assertColorEquals(outline.getNormalFontColors()[0], 0, 0, 0);
    assertColorEquals(outline.getNormalFontColors()[1], 127, 255, 127);
    assertColorEquals(outline.getNormalFontColors()[2], 0, 0, 0);
    
    assertArrayEquals(
      new CDEmbeddedOutline.Repeat[] {
        CDEmbeddedOutline.Repeat.SIZE_TO_FIT, CDEmbeddedOutline.Repeat.ONCE,
        CDEmbeddedOutline.Repeat.SIZE_TO_FIT, CDEmbeddedOutline.Repeat.HORIZONTAL
      },
      outline.getBackgroundRepeatModes()
    );
    
    assertEquals("i am name", outline.getName());
    assertEquals("i am target", outline.getTargetFrame());
    assertEquals("i am root", outline.getRootEntry());
  }
  
  @Test
  public void testAboutDocument() {
    DbDesign design = database.getDesign();
    
    AboutDocument about = design.getAboutDocument().get();
    assertTrue(
      about.getBody()
        .stream()
        .filter(CDText.class::isInstance)
        .map(CDText.class::cast)
        .anyMatch(text -> "I'm about".equals(text.getText()))
    );
  }
  
  @Test
  public void testUsingDocument() {
    DbDesign design = database.getDesign();
    
    UsingDocument using = design.getUsingDocument().get();
    assertTrue(
      using.getBody()
        .stream()
        .filter(CDText.class::isInstance)
        .map(CDText.class::cast)
        .anyMatch(text -> "I'm using".equals(text.getText()))
    );
  }
  
  @Test
  public void testSharedFields() {
    DbDesign design = database.getDesign();
    
    List<SharedField> fields = design.getSharedFields().collect(Collectors.toList());
    assertEquals(2, fields.size());
    assertTrue(fields.stream().anyMatch(field -> "testfield".equals(field.getTitle())));
    assertTrue(fields.stream().anyMatch(field -> "testfield2".equals(field.getTitle())));
  }
  
  @Test
  public void testSharedField2() throws IOException {
    DbDesign design = database.getDesign();
    
    SharedField field = design.getSharedField("testfield2").get();
    String expected = IOUtils.resourceToString("/text/testDbDesign/testfield2ls.txt", StandardCharsets.UTF_8).replace('\r', '\n');
    assertEquals(expected, field.getLotusScript().replace('\r', '\n'));
    
    assertTrue(
      field.getFieldBody()
        .stream()
        .filter(CDField.class::isInstance)
        .map(CDField.class::cast)
        .anyMatch(f -> "@If(@False; @Success; @Failure(\"nooo\"))".equals(f.getInputValidationFormula()))
    );
  }
  
  @Test
  public void testSharedActions() {
    DbDesign design = database.getDesign();
    
    SharedActions actions = design.getSharedActions().get();
    List<ActionBarAction> actionList = actions.getActions();
    assertEquals(5, actionList.size());
    // NB: these are stored in a different order from displayed in Designer
    {
      // Edit
      ActionBarAction action = actionList.get(0);
      assertEquals("Edit", action.getName());
      assertFalse(action.getLabelFormula().isPresent());
      assertFalse(action.getTargetFrame().isPresent());
      assertEquals(ActionBarControlType.BUTTON, action.getDisplayType());
      assertTrue(action.isIncludeInActionBar());
      assertFalse(action.isIconOnlyInActionBar());
      assertFalse(action.isOppositeAlignedInActionBar());
      assertFalse(action.isDisplayAsSplitButton());
      assertTrue(action.isIncludeInActionMenu());
      assertFalse(action.isIncludeInMobileActions());
      assertFalse(action.isIncludeInMobileSwipeLeft());
      assertFalse(action.isIncludeInMobileSwipeRight());
      assertFalse(action.isIncludeInContextMenu());
      
      assertFalse(action.isDisplayIconOnRight());
      assertEquals(IconType.NOTES, action.getIconType());
      assertEquals(5, action.getNotesIconIndex());
      
      ActionContent content = action.getActionContent();
      assertInstanceOf(FormulaActionContent.class, content);
      assertEquals("@Command([EditDocument])", ((FormulaActionContent)content).getFormula());
    }
    {
      // Save
      ActionBarAction action = actionList.get(1);
      assertEquals("Save", action.getName());
      assertFalse(action.getLabelFormula().isPresent());
      assertFalse(action.getTargetFrame().isPresent());
      assertEquals(ActionBarControlType.BUTTON, action.getDisplayType());
      assertTrue(action.isIncludeInActionBar());
      assertFalse(action.isIconOnlyInActionBar());
      assertFalse(action.isOppositeAlignedInActionBar());
      assertFalse(action.isDisplayAsSplitButton());
      assertTrue(action.isIncludeInActionMenu());
      assertFalse(action.isIncludeInMobileActions());
      assertFalse(action.isIncludeInMobileSwipeLeft());
      assertFalse(action.isIncludeInMobileSwipeRight());
      assertFalse(action.isIncludeInContextMenu());
      
      assertFalse(action.isDisplayIconOnRight());
      assertEquals(IconType.CUSTOM, action.getIconType());
      CDResource res = action.getIconResource().get();
      assertEquals("tango/document-save.png", res.getNamedElement().get());
      
      ActionContent content = action.getActionContent();
      assertInstanceOf(FormulaActionContent.class, content);
      assertEquals("@If(\n"
          + "	@ClientType=\"Web\" | @IsValid; @Do(\n"
          + "		@Command([FileSave]);\n"
          + "		@Command([EditDocument])\n"
          + "	);\n"
          + "	\"\"\n"
          + ")", ((FormulaActionContent)content).getFormula());
    }
    {
      // Save and Close
      ActionBarAction action = actionList.get(2);
      assertEquals("Save and Close", action.getName());
      assertFalse(action.getLabelFormula().isPresent());
      assertFalse(action.getTargetFrame().isPresent());
      assertEquals(ActionBarControlType.BUTTON, action.getDisplayType());
      assertTrue(action.isIncludeInActionBar());
      assertFalse(action.isIconOnlyInActionBar());
      assertFalse(action.isOppositeAlignedInActionBar());
      assertFalse(action.isDisplayAsSplitButton());
      assertTrue(action.isIncludeInActionMenu());
      assertFalse(action.isIncludeInMobileActions());
      assertFalse(action.isIncludeInMobileSwipeLeft());
      assertFalse(action.isIncludeInMobileSwipeRight());
      assertFalse(action.isIncludeInContextMenu());
      
      assertFalse(action.isDisplayIconOnRight());
      assertEquals(IconType.CUSTOM, action.getIconType());
      CDResource res = action.getIconResource().get();
      assertEquals("tango/system-log-out.png", res.getNamedElement().get());
      
      ActionContent content = action.getActionContent();
      assertInstanceOf(FormulaActionContent.class, content);
      assertEquals("@If(\n"
          + "	@ClientType=\"Web\" | @IsValid; @Do(\n"
          + "		@Command([FileSave]);\n"
          + "		@Command([FileCloseWindow])\n"
          + "	);\n"
          + "	\"\"\n"
          + ")", ((FormulaActionContent)content).getFormula());
    }
    {
      // Delete
      ActionBarAction action = actionList.get(3);
      assertEquals("Delete", action.getName());
      assertFalse(action.getLabelFormula().isPresent());
      assertFalse(action.getTargetFrame().isPresent());
      assertEquals(ActionBarControlType.BUTTON, action.getDisplayType());
      assertTrue(action.isIncludeInActionBar());
      assertFalse(action.isIconOnlyInActionBar());
      assertTrue(action.isOppositeAlignedInActionBar());
      assertFalse(action.isDisplayAsSplitButton());
      assertTrue(action.isIncludeInActionMenu());
      assertFalse(action.isIncludeInMobileActions());
      assertFalse(action.isIncludeInMobileSwipeLeft());
      assertFalse(action.isIncludeInMobileSwipeRight());
      assertFalse(action.isIncludeInContextMenu());
      
      assertFalse(action.isDisplayIconOnRight());
      assertEquals(IconType.NOTES, action.getIconType());
      assertEquals(4, action.getNotesIconIndex());
      
      ActionContent content = action.getActionContent();
      assertInstanceOf(FormulaActionContent.class, content);
      assertEquals("@Command([EditClear])", ((FormulaActionContent)content).getFormula());
    }
    {
      // tester
      ActionBarAction action = actionList.get(4);
      assertEquals("tester", action.getName());
      assertFalse(action.getLabelFormula().isPresent());
      assertFalse(action.getTargetFrame().isPresent());
      assertEquals(ActionBarControlType.BUTTON, action.getDisplayType());
      assertTrue(action.isIncludeInActionBar());
      assertFalse(action.isIconOnlyInActionBar());
      assertFalse(action.isOppositeAlignedInActionBar());
      assertFalse(action.isDisplayAsSplitButton());
      assertTrue(action.isIncludeInActionMenu());
      assertFalse(action.isIncludeInMobileActions());
      assertFalse(action.isIncludeInMobileSwipeLeft());
      assertFalse(action.isIncludeInMobileSwipeRight());
      assertFalse(action.isIncludeInContextMenu());
      
      assertFalse(action.isDisplayIconOnRight());
      assertEquals(IconType.NONE, action.getIconType());
      
      ActionContent content = action.getActionContent();
      assertInstanceOf(SimpleActionActionContent.class, content);
      assertTrue(((SimpleActionActionContent)content).getActions().isEmpty());
    }
  }
  
  @Test
  public void testNoSharedActions() throws Exception {
    withTempDb(database -> {
      DbDesign design = database.getDesign();
      assertFalse(design.getSharedActions().isPresent());
    });
  }
  
  @Test
  public void testGetByUnid() {
    DbDesign design = database.getDesign();
    
    String unid;
    {
      SharedActions actions = design.getSharedActions().get();
      unid = actions.getDocument().getUNID();
    }
    {
      Optional<SharedActions> optActions = design.getDesignElementByUNID(unid);
      SharedActions actions = optActions.get();
      List<ActionBarAction> actionList = actions.getActions();
      assertEquals(5, actionList.size());
    }
  }
  
  @Test
  public void testMismatchedGetByUnid() {
    DbDesign design = database.getDesign();
    
    String unid;
    {
      SharedActions actions = design.getSharedActions().get();
      unid = actions.getDocument().getUNID();
    }
    {
      Optional<View> optActions = design.getDesignElementByUNID(unid);
      assertThrows(ClassCastException.class, () -> {
        @SuppressWarnings("unused")
        View view = optActions.get();
      });
    }
  }
}
