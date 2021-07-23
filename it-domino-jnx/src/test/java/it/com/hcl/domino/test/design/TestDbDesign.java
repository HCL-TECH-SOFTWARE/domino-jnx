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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.format.FieldListDelimiter;
import com.hcl.domino.design.format.FieldListDisplayDelimiter;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.design.DbProperties;
import com.hcl.domino.design.FileResource;
import com.hcl.domino.design.Folder;
import com.hcl.domino.design.Form;
import com.hcl.domino.design.CollectionDesignElement;
import com.hcl.domino.design.ImageResource;
import com.hcl.domino.design.Subform;
import com.hcl.domino.design.SubformReference;
import com.hcl.domino.design.View;
import com.hcl.domino.richtext.FormField;
import com.hcl.domino.richtext.process.GetImageResourceSizeProcessor;
import com.ibm.commons.util.io.StreamUtil;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDbDesign extends AbstractNotesRuntimeTest {
	private static String dbPath;
	private Database database;
	
	@BeforeEach
	public void initDesignDb() throws IOException, URISyntaxException {
		if(database == null) {
			DominoClient client = getClient();
			if(dbPath == null) {
				database = createTempDb(client);
				dbPath = database.getAbsoluteFilePath();
				populateResourceDxl("/dxl/testDbDesign", database);
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
	public void getGetDbDesign() throws Exception {
		withTempDb(database -> {
			assertNotNull(database.getDesign());
		});
	}
	
	@Test
	public void testForms() {
		DbDesign dbDesign = database.getDesign();
		Collection<Form> forms = dbDesign.getForms().collect(Collectors.toList());
		assertEquals(2, forms.size());
		{
			Form form = forms.stream().filter(f -> "Content".equals(f.getTitle())).findFirst().orElse(null);
			assertNotNull(form);
			
			form = dbDesign.getForm("Content").orElse(null);
			assertNotNull(form);
			assertEquals("Content", form.getTitle());
		}
		{
			Form form = dbDesign.getForm("Alias").orElse(null);
			assertNotNull(form);
			assertEquals("Alias", form.getTitle());
			
			List<FormField> fields = form.getFields();
			assertEquals(5, fields.size());
			assertEquals(
				Arrays.asList("Host", "From", "To", "$$Title", "$$Creator"),
				fields.stream().map(FormField::getName).collect(Collectors.toList())
			);
			{
				FormField hostField = fields.stream().filter(f -> "Host".equals(f.getName())).findFirst().orElse(null);
				assertNotNull(hostField);
				assertEquals(EnumSet.of(FieldListDelimiter.COMMA, FieldListDelimiter.SEMICOLON, FieldListDelimiter.NEWLINE), hostField.getListInputDelimiters());
				assertEquals(FieldListDisplayDelimiter.SEMICOLON, hostField.getListDispayDelimiter());
				assertEquals("host-id", hostField.getHtmlId());
				assertEquals("host-class", hostField.getHtmlClassName());
				assertEquals("host: style", hostField.getHtmlStyle());
				assertEquals("host=\"attr\"", hostField.getHtmlExtraAttr());
				assertEquals("Name", hostField.getHtmlTitle());
				assertEquals(ItemDataType.TYPE_TEXT_LIST, hostField.getDataType().get());
			}
			
			// Check for its two subforms
			List<SubformReference> subforms = form.getSubforms();
			assertEquals(2, subforms.size());
			{
				SubformReference computed = subforms.get(0);
				assertEquals(SubformReference.Type.FORMULA, computed.getType());
				assertEquals("@If(@True; \"Computed Target\"; \"Computed Target\")", computed.getValue());
			}
			{
				SubformReference explicit = subforms.get(1);
				assertEquals(SubformReference.Type.EXPLICIT, explicit.getType());
				assertEquals("Footer", explicit.getValue());
			}
		}

		assertNull(dbDesign.getForm("Printer Agent").orElse(null));
	}
	
	@Test
	public void testCreateForm() throws Exception {
		withTempDb(database -> {
			DbDesign design = database.getDesign();
			{
				Form element = design.createForm("foo bar");
				assertNotNull(element);
				assertEquals("foo bar", element.getTitle());
				
				assertNull(design.getForm("foo bar").orElse(null));
				element.save();
				assertNotNull(design.getForm("foo bar").orElse(null));
				assertNull(design.getSubform("foo bar").orElse(null));
				element.setTitle("other title");
				element.save();
				assertNull(design.getForm("foo bar").orElse(null));
				assertNotNull(design.getForm("other title").orElse(null));
				
				element.setHideFromMobile(true);
				element.save();
			}
			{
				Form element = design.getForm("other title").orElse(null);
				assertNotNull(element);
				assertTrue(element.isHideFromMobile());
			}
		});
	}
	
	@Test
	public void testSubforms() {
		DbDesign dbDesign = database.getDesign();
		Collection<Subform> forms = dbDesign.getSubforms().collect(Collectors.toList());
		assertEquals(2, forms.size());
		{
			Subform subform = forms.stream().filter(f -> "Footer".equals(f.getTitle())).findFirst().orElse(null);
			assertNotNull(subform);
			
			subform = dbDesign.getSubform("Footer").orElse(null);
			assertNotNull(subform);
			assertEquals("Footer", subform.getTitle());
		}

		assertNull(dbDesign.getSubform("Content").orElse(null));
	}
	
	@Test
	public void testCreateSubform() throws Exception {
		withTempDb(database -> {
			DbDesign design = database.getDesign();
			Subform element = design.createSubform("foo bar");
			assertNotNull(element);
			assertEquals("foo bar", element.getTitle());
			
			assertNull(design.getSubform("foo bar").orElse(null));
			element.save();
			assertNotNull(design.getSubform("foo bar").orElse(null));
			assertNull(design.getForm("foo bar").orElse(null));
			element.setTitle("other title");
			element.save();
			assertNull(design.getSubform("foo bar").orElse(null));
			assertNotNull(design.getSubform("other title").orElse(null));
		});
	}
	
	@Test
	public void testFoldersAndViews() {
		DbDesign dbDesign = database.getDesign();
		Collection<CollectionDesignElement> collections = dbDesign.getCollections().collect(Collectors.toList());
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
	public void testViews() {
		DbDesign dbDesign = database.getDesign();
		Collection<CollectionDesignElement> collections = dbDesign.getViews().collect(Collectors.toList());
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
	public void testCreateView() throws Exception {
		withTempDb(database -> {
			DbDesign design = database.getDesign();
			View element = design.createView("foo bar");
			assertNotNull(element);
			assertEquals("foo bar", element.getTitle());
			
			assertNull(design.getView("foo bar").orElse(null));
			element.save();
			assertNotNull(design.getView("foo bar").orElse(null));
			assertNull(design.getFolder("foo bar").orElse(null));
			element.setTitle("other title");
			element.save();
			assertNull(design.getView("foo bar").orElse(null));
			assertNotNull(design.getCollection("other title").orElse(null));
		});
	}
	
	@Test
	public void testFolders() {
		DbDesign dbDesign = database.getDesign();
		Collection<CollectionDesignElement> collections = dbDesign.getFolders().collect(Collectors.toList());
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
	public void testCreateFolder() throws Exception {
		withTempDb(database -> {
			DbDesign design = database.getDesign();
			Folder element = design.createFolder("foo bar");
			assertNotNull(element);
			assertEquals("foo bar", element.getTitle());
			
			assertNull(design.getFolder("foo bar").orElse(null));
			element.save();
			assertNotNull(design.getFolder("foo bar").orElse(null));
			assertNull(design.getView("foo bar").orElse(null));
			element.setTitle("other title");
			element.save();
			assertNull(design.getFolder("foo bar").orElse(null));
			assertNotNull(design.getFolder("other title").orElse(null));
		});
	}

	@Test
	public void testDbProperties() {
		DbDesign dbDesign = database.getDesign();
		DbProperties props = dbDesign.getDatabaseProperties();
		assertNotNull(props);
		
		assertFalse(props.isGenerateEnhancedHtml());
		props.setGenerateEnhancedHtml(true);
		assertTrue(props.isGenerateEnhancedHtml());
		props.setGenerateEnhancedHtml(false);
		assertFalse(props.isGenerateEnhancedHtml());
	}
	
	@Test
	public void testFileResources() {
		DbDesign dbDesign = database.getDesign();
		List<FileResource> resources = dbDesign.getFileResources().collect(Collectors.toList());
		assertEquals(3, resources.size());
		
		assertTrue(resources.stream().anyMatch(res -> Arrays.asList("file.css").equals(res.getFileNames())));
		assertTrue(resources.stream().anyMatch(res -> Arrays.asList("test.txt").equals(res.getFileNames())));
		assertTrue(resources.stream().anyMatch(res -> Arrays.asList("largels.txt").equals(res.getFileNames())));
	}
	
	@Test
	public void testFileResourceFileCss() throws IOException {
		DbDesign dbDesign = database.getDesign();
		FileResource res = dbDesign.getFileResource("file.css").get();
		assertEquals("file.css", res.getTitle());
		assertEquals("text/css", res.getMimeType());
		assertEquals("Windows-1252", res.getCharsetName());
		
		String content;
		try(InputStream is = res.getFileData()) {
			content = StreamUtil.readString(is);
		}
		assertEquals("/* I'm a file resource named CSS */", content);
	}
	
	@Test
	public void testFileResourceFileCssQuery() throws IOException {
		DbDesign dbDesign = database.getDesign();
		FileResource res = (FileResource)dbDesign.queryDesignElements("$TITLE='file.css'").findFirst().get();
		assertEquals("file.css", res.getTitle());
		assertEquals("text/css", res.getMimeType());
		assertEquals("Windows-1252", res.getCharsetName());
		assertEquals(res.getDocument().getLastModified().toTemporal().get(), res.getFileModified().toTemporal().get());
		
		String content;
		try(InputStream is = res.getFileData()) {
			content = StreamUtil.readString(is);
		}
		assertEquals("/* I'm a file resource named CSS */", content);
	}
	
	@Test
	public void testFileResourceTestTxt() throws IOException {
		DbDesign dbDesign = database.getDesign();
		FileResource res = dbDesign.getFileResource("test.txt").get();
		assertEquals("test.txt", res.getTitle());
		assertEquals("text/plain", res.getMimeType());
		assertEquals("UTF-8", res.getCharsetName());
		
		// 20210619T150226,17-04
		OffsetDateTime expected = OffsetDateTime.of(2021, 6, 19, 14, 2, 26, 17 * 1000 * 1000 * 10, ZoneOffset.ofHours(-5));
		assertEquals(expected, res.getFileModified().toOffsetDateTime());
		
		String content;
		try(InputStream is = res.getFileData()) {
			content = StreamUtil.readString(is);
		}
		assertEquals("I am test text", content);
	}
	
	@Test
	public void testFileResourceLargelsTxt() throws IOException {
		DbDesign dbDesign = database.getDesign();
		FileResource res = dbDesign.getFileResource("largels.txt").get();
		assertEquals("largels.txt", res.getTitle());
		assertEquals("text/plain", res.getMimeType());
		assertEquals("UTF-8", res.getCharsetName());
		
		String expected;
		try(InputStream is = getClass().getResourceAsStream("/text/largels_crlf.txt")) {
			expected = StreamUtil.readString(is);
		}
		
		String content;
		try(InputStream is = res.getFileData()) {
			content = StreamUtil.readString(is);
		}
		assertEquals(expected.replace("\r\n", "\n"), content.replace("\r\n", "\n"));
	}
	
	@Test
	public void testImageResources() {
		DbDesign dbDesign = database.getDesign();
		List<ImageResource> resources = dbDesign.getImageResources().collect(Collectors.toList());
		assertEquals(2, resources.size());
		
		assertTrue(resources.stream().anyMatch(res -> Arrays.asList("Untitled.gif").equals(res.getFileNames())));
		// The copied image resource is known to be broken, as an effect of a Designer bug. That leaves it as useful
		//   test data, but not for this check
		//assertTrue(resources.stream().anyMatch(res -> Arrays.asList("Untitled 2.gif").equals(res.getFileNames())));
		assertTrue(resources.stream().anyMatch(res -> "Untitled.gif".equals(res.getTitle())));
	}
	
	@Test
	public void testImageResUntitled() throws IOException {
		DbDesign dbDesign = database.getDesign();
		ImageResource res = dbDesign.getImageResource("Untitled.gif").get();
		assertEquals("Untitled.gif", res.getTitle());
		assertEquals("image/gif", res.getMimeType());
		assertFalse(res.isWebReadOnly());
		assertFalse(res.isWebCompatible());
		assertEquals(1, res.getImagesDown());
		assertEquals(1, res.getImagesAcross());
		assertEquals(890, res.getFileSize());
		assertEquals(890, new GetImageResourceSizeProcessor().apply(res.getDocument().getRichTextItem(NotesConstants.ITEM_NAME_IMAGE_DATA)));
		
		byte[] expected = IOUtils.resourceToByteArray("/images/Untitled.gif");
		
		byte[] content;
		try(InputStream is = res.getFileData()) {
			content = IOUtils.toByteArray(is);
		}
		assertArrayEquals(expected, content);
	}
	
	@Test
	public void testImageResUntitled2() throws IOException {
		DbDesign dbDesign = database.getDesign();
		ImageResource res = dbDesign.getImageResource("Untitled 2.gif").get();
		assertEquals("Untitled 2.gif", res.getTitle());
		assertEquals("image/gif", res.getMimeType());
		assertTrue(res.isWebReadOnly());
		assertTrue(res.isWebCompatible());
		assertEquals(2, res.getImagesDown());
		assertEquals(4, res.getImagesAcross());

		// This is known to have diverged, where the $FileSize reflects one of the image tiles for some reason
		assertEquals(890, res.getFileSize());
		assertEquals(839, res.getDocument().get("$FileSize", int.class, 0));
		assertEquals(890, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem(NotesConstants.ITEM_NAME_IMAGE_DATA)));
		
		// The images-down/across values caused the creation of many $ImageData values
		assertEquals(824, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem("$ImageData0000")));
		assertEquals(833, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem("$ImageData0001")));
		assertEquals(835, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem("$ImageData0002")));
		assertEquals(839, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem("$ImageData0003")));
		assertEquals(824, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem("$ImageData0100")));
		assertEquals(833, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem("$ImageData0101")));
		assertEquals(824, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem("$ImageData0102")));
		assertEquals(839, GetImageResourceSizeProcessor.instance.apply(res.getDocument().getRichTextItem("$ImageData0103")));
		
		byte[] expected = IOUtils.resourceToByteArray("/images/Untitled.gif");
		
		byte[] content;
		try(InputStream is = res.getFileData()) {
			content = IOUtils.toByteArray(is);
		}
		assertArrayEquals(expected, content);
	}
}
