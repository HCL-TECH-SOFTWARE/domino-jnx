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
package it.com.hcl.domino.test.richtext;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import com.hcl.domino.commons.richtext.RichTextUtil;
import com.hcl.domino.data.Attachment;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Document.IAttachmentProducer;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.FormulaQueryResult;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.TextStyle.Justify;
import com.hcl.domino.richtext.conversion.RemoveAttachmentIconConversion;
import com.hcl.domino.richtext.process.ExtractFileResourceProcessor;
import com.hcl.domino.richtext.process.ExtractImageResourceProcessor;
import com.hcl.domino.richtext.process.GetFileResourceSizeProcessor;
import com.hcl.domino.richtext.process.GetImageResourceSizeProcessor;
import com.hcl.domino.richtext.records.CDBlobPart;
import com.hcl.domino.richtext.records.CDEvent;
import com.hcl.domino.richtext.records.CDHotspotBegin;
import com.hcl.domino.richtext.records.CDHotspotEnd;
import com.hcl.domino.richtext.records.CDImageHeader;
import com.hcl.domino.richtext.records.CDParagraph;
import com.hcl.domino.richtext.records.CDText;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.FontStyle.StandardFonts;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestRichtextNavigator extends AbstractNotesRuntimeTest {

	@Test
	public void testRTNav() throws Exception {
		withTempDb((database) -> {
			Document doc = database.createDocument();

			//we create a document with an attached file that is visible in the body richtext
			
			Attachment att = doc.attachFile("test.txt",
					Instant.now(), Instant.now(),
					new IAttachmentProducer() {

				@Override
				public void produceAttachment(OutputStream out) throws IOException {
					out.write("TEST!".getBytes(Charset.forName("UTF-8")));
				}

				@Override
				public long getSizeEstimation() {
					return -1;
				}
			});

			try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
				rtWriter.addText("Hello");
				rtWriter.addAttachmentIcon(att, "testfile.txt");
			}

			//first we use the richtext navigator to check if all expected CD records are there
			
//			System.out.println("Before conversion:\n");
			{
//				JNARichTextRecord [type=[PARAGRAPH], totallength=2, headerlength=2 datalength=0]
//				JNARichTextRecord [type=[TEXT], totallength=13, headerlength=4 datalength=9]
//				JNARichTextRecord [type=[BEGIN], totallength=6, headerlength=2 datalength=4]
//				JNARichTextRecord [type=[HOTSPOTBEGIN], totallength=34, headerlength=4 datalength=30]
//				JNARichTextRecord [type=[GRAPHIC], totallength=28, headerlength=6 datalength=22]
//				JNARichTextRecord [type=[IMAGEHEADER], totallength=28, headerlength=6 datalength=22]
//				JNARichTextRecord [type=[IMAGESEGMENT], totallength=764, headerlength=6 datalength=758]
//				JNARichTextRecord [type=[CAPTION], totallength=40, headerlength=4 datalength=36]
//				JNARichTextRecord [type=[HOTSPOTEND], totallength=2, headerlength=2 datalength=0]
//				JNARichTextRecord [type=[END], totallength=6, headerlength=2 datalength=4]
								
				boolean hasParagraph = false;
				boolean hasText = false;
				boolean hasImageHeader = false;
				boolean hasHotspotBegin = false;
				boolean hasHotspotEnd = false;
				
				List<RichTextRecord<?>> rtNav = doc.getRichTextItem("body");
				for(RichTextRecord<?> record : rtNav) {
					if (record instanceof CDParagraph) {
						hasParagraph = true;
					}
					if (record instanceof CDText) {
						hasText = true;
					}
					if (record instanceof CDImageHeader) {
						hasImageHeader = true;
					}
					if (record instanceof CDHotspotBegin) {
						hasHotspotBegin = true;
					}
					if (record instanceof CDHotspotEnd) {
						hasHotspotEnd = true;
					}
					
//						System.out.println(record);
				}
				
				assertTrue(hasParagraph);
				assertTrue(hasText);
				assertTrue(hasImageHeader);
				assertTrue(hasHotspotBegin);
				assertTrue(hasHotspotEnd);
			}
			
			//now use a richtext conversion to remove the attachment icon from the richtext
			doc.convertRichTextItem("Body", new RemoveAttachmentIconConversion(att));

			//let's see if the image and hotspot could be removed
			
//			System.out.println("After conversion:\n");
			{
//				JNARichTextRecord [type=[PARAGRAPH], totallength=2, headerlength=2 datalength=0]
//				JNARichTextRecord [type=[TEXT], totallength=13, headerlength=4 datalength=9]
//				JNARichTextRecord [type=[TEXT], totallength=8, headerlength=4 datalength=4]
				
				boolean hasParagraph = false;
				boolean hasText = false;
				boolean hasImageHeader = false;
				boolean hasHotspotBegin = false;
				boolean hasHotspotEnd = false;
				
				List<RichTextRecord<?>> rtNav = doc.getRichTextItem("body");
				for(RichTextRecord<?> record : rtNav) {
					if (record.getType().contains(RecordType.PARAGRAPH)) {
						hasParagraph = true;
					}
					if (record.getType().contains(RecordType.TEXT)) {
						hasText = true;
					}
					if (record.getType().contains(RecordType.IMAGEHEADER)) {
						hasImageHeader = true;
					}
					if (record.getType().contains(RecordType.HOTSPOTBEGIN)) {
						hasHotspotBegin = true;
					}
					if (record.getType().contains(RecordType.HOTSPOTEND)) {
						hasHotspotEnd = true;
					}
					
//						System.out.println(record);
				}
				
				assertTrue(hasParagraph);
				assertTrue(hasText);
				
				assertFalse(hasImageHeader);
				assertFalse(hasHotspotBegin);
				assertFalse(hasHotspotEnd);
			}
			
		});
	}
	
	@Test
	public void testRichTextImageResource() throws Exception {
		withResourceDxl("/dxl/testRichTextNavigator", database -> {
			FormulaQueryResult result = database.queryFormula("$TITLE='help_vampire.gif'", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.ALLNONDATA));
			Document image = result.getDocuments().findFirst().orElseThrow(() -> new RuntimeException("Couldn't find design note"));
			byte[] expected = IOUtils.resourceToByteArray("/images/help_vampire.gif");
			
			_testImageResource(expected, image);
		});
	}
	
	@Test
	public void testRoundTripImageResource() throws Exception {
		withTempDb(database -> {
			byte[] expected = IOUtils.resourceToByteArray("/images/help_vampire.gif");
			
			Document image = database.createDocument();
			
			try(RichTextWriter w = image.createRichTextItem("$ImageData")) {
				try(InputStream is = new ByteArrayInputStream(expected)) {
					w.addImageResource(is, expected.length);
				}
			}
			
			_testImageResource(expected, image);
		});
	}
	
	@Test
	public void testRoundTripImageResourceFilesystem() throws Exception {
		withTempDb(database -> {
			byte[] expected = IOUtils.resourceToByteArray("/images/help_vampire.gif");
			Path tempFile = Files.createTempFile(getClass().getName(), ".gif");
			try(InputStream is = new ByteArrayInputStream(expected)) {
				Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
			}
			try {
				Document image = database.createDocument();
				
				try(RichTextWriter w = image.createRichTextItem("$ImageData")) {
					w.addImageResource(tempFile);
				}
				
				_testImageResource(expected, image);
			} finally {
				Files.deleteIfExists(tempFile);
			}
		});
	}
	
	@Test
	public void testRoundTripImageResourceGuessLength() throws Exception {
		withTempDb(database -> {
			byte[] expected = IOUtils.resourceToByteArray("/images/help_vampire.gif");
			
			Document image = database.createDocument();
			
			try(RichTextWriter w = image.createRichTextItem("$ImageData")) {
				try(InputStream is = new ByteArrayInputStream(expected)) {
					w.addImageResource(is, -1);
				}
			}
			
			_testImageResource(expected, image);
		});
	}
	
	private void _testImageResource(byte[] expected, Document image) {
		// Make sure the stored length is correct
		long imageSize = image.getRichTextItem("$ImageData").process(new GetImageResourceSizeProcessor());
		assertEquals(imageSize, expected.length);
		
		// Read out the image data
		ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
		image.getRichTextItem("$ImageData").process(new ExtractImageResourceProcessor(imageStream));
		
		// Read in the expected data
		assertArrayEquals(expected, imageStream.toByteArray());
	}

	// Test in case the implementation switches to using EnumComposite*
	// This also uses lower-level APIs for checking record types vs. what testRichTextImageResource uses
	@Test
	public void testRichTextImageResourceForEach() throws Exception {
		withResourceDxl("/dxl/testRichTextNavigator", database -> {
			FormulaQueryResult result = database.queryFormula("$TITLE='help_vampire.gif'", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.ALLNONDATA));
			Document image = result.getDocuments().findFirst().orElseThrow(() -> new RuntimeException("Couldn't find design note"));
			ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
			image.getRichTextItem("$ImageData").forEach(record -> {
					if(!record.getType().contains(RecordType.IMAGESEGMENT)) {
						return;
					}
					// LSIG Header (6)
					// WORD DataSize (2)
					// WORD SegSize (2)
					// Data
					
					ByteBuffer data = record.getDataWithoutHeader();
					int dataLen = Short.toUnsignedInt(data.getShort(2));
					byte[] segData = new byte[dataLen];
					data.position(4);
					data.get(segData);
					try {
						imageStream.write(segData);
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			
			// Read in the expected data
			byte[] expected = IOUtils.resourceToByteArray("/images/help_vampire.gif");
			assertArrayEquals(expected, imageStream.toByteArray());
		});
	}
	
	@Test
	public void testReadCDText() throws Exception {
		withResourceDxl("/dxl/testRichTextNavigator", database -> {
			FormulaQueryResult result = database.queryFormula("$$TITLE='CD Record test'", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.DOCUMENT));
			Document doc = result.getDocuments().findFirst().orElseThrow(() -> new RuntimeException("Couldn't find 'CD Record test' post note"));
			List<RichTextRecord<?>> rt = doc.getRichTextItem("Body")
					.stream()
					.filter(CDText.class::isInstance)
					.collect(Collectors.toList());
			
			{
				CDText normalText = (CDText)rt.get(0);
				assertEquals("This is normal text.", normalText.getText());
				FontStyle style = normalText.getStyle();
				assertEquals(FontStyle.StandardFonts.SWISS, style.getFontFace());
				assertTrue(style.getAttributes().isEmpty(), "Text should have no attributes");
				
				style.setBold(true)
					.setPointSize(24)
					.setSub(true);
				assertEquals(EnumSet.of(FontStyle.Attribute.BOLD, FontStyle.Attribute.SUB), style.getAttributes(), "Text should now be bold and subscript");
				assertEquals(24, style.getPointSize(), "Point size should be 24");
				
				style = doc.createRichTextItem("foo").createFontStyle();
				style.setItalic(true)
					.setPointSize(22)
					.setSuper(true)
					.setFontFace(StandardFonts.USERINTERFACE);
				normalText.setStyle(style);
				style = normalText.getStyle();
				assertEquals(EnumSet.of(FontStyle.Attribute.ITALIC, FontStyle.Attribute.SUPER), style.getAttributes(), "Text should now be italic and superscript");
				style.setExtrude(true);
				assertEquals(22, style.getPointSize(), "Point size should be 22");
				assertEquals(StandardFonts.USERINTERFACE, style.getFontFace(), "Font should be USERINTERFACE");
			}
			
			{
				CDText monospaceText = (CDText)rt.get(2);
				assertEquals("This is monospace text", monospaceText.getText());
				FontStyle style = monospaceText.getStyle();
				assertEquals(FontStyle.StandardFonts.TYPEWRITER, style.getFontFace());
				assertTrue(style.getAttributes().isEmpty(), "Text should have no attributes");
			}
			
			{
				CDText serifText = (CDText)rt.get(4);
				assertEquals("This is serif italic bold text", serifText.getText());
				FontStyle style = serifText.getStyle();
				assertEquals(FontStyle.StandardFonts.SWISS, style.getFontFace());
				assertEquals(EnumSet.of(FontStyle.Attribute.BOLD, FontStyle.Attribute.ITALIC), style.getAttributes(), "Text should be bold and italic");
			}
		});
	}
	
	@Test
	public void testCDTextInFlightChange() throws Exception {
		withResourceDxl("/dxl/testRichTextNavigator", database -> {
			FormulaQueryResult result = database.queryFormula("$$TITLE='CD Record test'", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.DOCUMENT));
			Document doc = result.getDocuments().findFirst().orElseThrow(() -> new RuntimeException("Couldn't find 'CD Record test' post note"));
			List<RichTextRecord<?>> body = doc.getRichTextItem("Body");
			try(RichTextWriter w = doc.createRichTextItem("Body2")) {
				body.forEach(record -> {
					if(record instanceof CDText) {
						int size = record.getCDRecordLength();
						String text = ((CDText) record).getText();
						((CDText) record).setText(text + " and then some");
						int expectedSize = size+" and then some".length();
						assertEquals(expectedSize, record.getCDRecordLength());
						assertEquals(expectedSize, record.getHeader().getLength().intValue());
						assertEquals(text + " and then some", ((CDText) record).getText());
					}
					w.addRichTextRecord(record);
				});
			}
			
			List<RichTextRecord<?>> rt2 = doc.getRichTextItem("Body2");
			assertEquals(body.size(), rt2.size(), "Body2 should have the same record count as Body");
			List<RichTextRecord<?>> rt = rt2
				.stream()
				.filter(CDText.class::isInstance)
				.collect(Collectors.toList());
			
			{
				CDText normalText = (CDText)rt.get(0);
				assertEquals("This is normal text. and then some", normalText.getText());
				FontStyle style = normalText.getStyle();
				assertEquals(FontStyle.StandardFonts.SWISS, style.getFontFace());
				assertTrue(style.getAttributes().isEmpty(), "Text should have no attributes");
				
				style.setBold(true)
					.setPointSize(24)
					.setSub(true);
				assertEquals(EnumSet.of(FontStyle.Attribute.BOLD, FontStyle.Attribute.SUB), style.getAttributes(), "Text should now be bold and subscript");
				assertEquals(24, style.getPointSize(), "Point size should be 24");
				
				style = doc.createRichTextItem("foo").createFontStyle();
				style.setItalic(true)
					.setPointSize(22)
					.setSuper(true)
					.setFontFace(StandardFonts.USERINTERFACE);
				normalText.setStyle(style);
				style = normalText.getStyle();
				assertEquals(EnumSet.of(FontStyle.Attribute.ITALIC, FontStyle.Attribute.SUPER), style.getAttributes(), "Text should now be italic and superscript");
				style.setExtrude(true);
				assertEquals(22, style.getPointSize(), "Point size should be 22");
				assertEquals(StandardFonts.USERINTERFACE, style.getFontFace(), "Font should be USERINTERFACE");
			}
			
			{
				CDText monospaceText = (CDText)rt.get(2);
				assertEquals("This is monospace text and then some", monospaceText.getText());
				FontStyle style = monospaceText.getStyle();
				assertEquals(FontStyle.StandardFonts.TYPEWRITER, style.getFontFace());
				assertTrue(style.getAttributes().isEmpty(), "Text should have no attributes");
			}
			
			{
				CDText serifText = (CDText)rt.get(4);
				assertEquals("This is serif italic bold text and then some", serifText.getText());
				FontStyle style = serifText.getStyle();
				assertEquals(FontStyle.StandardFonts.SWISS, style.getFontFace());
				assertEquals(EnumSet.of(FontStyle.Attribute.BOLD, FontStyle.Attribute.ITALIC), style.getAttributes(), "Text should be bold and italic");
			}
		});
	}
	
	@Test
	public void testRichTextItemValue() throws Exception {
		withResourceDxl("/dxl/testRichTextNavigator", database -> {
			FormulaQueryResult result = database.queryFormula("$TITLE='help_vampire.gif'", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.ALLNONDATA));
			Document image = result.getDocuments().findFirst().orElseThrow(() -> new RuntimeException("Couldn't find design note"));
			List<Object> value = image.getFirstItem("$ImageData").get().getValue();
			assertNotNull(value);
			assertFalse(value.isEmpty());
			assertTrue(value.get(0) instanceof RichTextRecord);
		});
	}
	
	@Test
	public void testReadClientJSLibrary() throws Exception {
		withResourceDxl("/dxl/testRichTextNavigator", database -> {
			FormulaQueryResult result = database.queryFormula("$TITLE='Client JS Library'", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.ALLNONDATA));
			Document library = result.getDocuments().findFirst().orElseThrow(() -> new RuntimeException("Couldn't find design note"));
			String expected = IOUtils.resourceToString("/text/clientjs.js", StandardCharsets.UTF_8) + "\r\n";

			// Make sure the blob part length is correct
			long len = library.getRichTextItem("$JavaScriptLibrary")
				.stream()
				.filter(CDEvent.class::isInstance)
				.map(CDEvent.class::cast)
				.findFirst()
				.map(CDEvent::getActionLength)
				.orElseThrow(() -> new IllegalStateException("Couldn't find CDEvent record"));
			byte[] expectedBytes = expected.getBytes(RichTextUtil.LMBCS);
			int expectedLen = expectedBytes.length;
			// Account for a final null that Domino adds
			expectedLen += expectedLen % 2;
			assertEquals(expectedLen, len);
			
			// Read out the script data
			ByteArrayOutputStream libraryStream = new ByteArrayOutputStream();
			library.getRichTextItem("$JavaScriptLibrary")
				.stream()
				.filter(CDBlobPart.class::isInstance)
				.map(CDBlobPart.class::cast)
				.forEach(record -> {
					try {
						libraryStream.write(record.getBlobPartData());
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			String libraryString = new String(libraryStream.toByteArray(), RichTextUtil.LMBCS);
			
			byte[] lineEndingExpected = expected.replace('\n', '\r').getBytes();
			byte[] libraryBytes = libraryString.getBytes();
			// Things get weird at the end of the string for some reason
			if(lineEndingExpected[lineEndingExpected.length-1] == '\r' && libraryBytes[libraryBytes.length-1] == '\n') {
				lineEndingExpected[lineEndingExpected.length-1] = '\n';
			}
			assertArrayEquals(lineEndingExpected, libraryBytes);
		});
	}
	
	@Test
	public void testRoundTripClientJSLibrary() throws Exception {
		withTempDb(database -> {
			String scriptContent = IOUtils.resourceToString("/text/clientjs.js", StandardCharsets.UTF_8);
			Document library = database.createDocument();
			
			// Write the content
			try(RichTextWriter w = library.createRichTextItem("$JavaScriptLibrary")) {
				w.addJavaScriptLibraryData(scriptContent);
			}
			
			{
				// Make sure the blob part length is correct
				long len = library.getRichTextItem("$JavaScriptLibrary")
					.stream()
					.filter(CDEvent.class::isInstance)
					.map(CDEvent.class::cast)
					.findFirst()
					.map(CDEvent::getActionLength)
					.orElseThrow(() -> new IllegalStateException("Couldn't find CDEvent record"));
				byte[] expectedBytes = scriptContent.getBytes(RichTextUtil.LMBCS);
				int expectedLen = expectedBytes.length + 1; // For the above added \0
				// Account for word boundaries
				expectedLen += expectedLen % 2;
				assertEquals(expectedLen, len);
				
				// Read out the script data
				ByteArrayOutputStream libraryStream = new ByteArrayOutputStream();
				library.getRichTextItem("$JavaScriptLibrary")
					.stream()
					.filter(CDBlobPart.class::isInstance)
					.map(CDBlobPart.class::cast)
					.forEach(record -> {
						try {
							libraryStream.write(record.getBlobPartData());
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					});
				String libraryString = new String(libraryStream.toByteArray(), RichTextUtil.LMBCS);
				// It ends up with an extra newline thanks to the code ensuring that there's a trailing newline
				String expectedScript = scriptContent + "\n";
				
				assertArrayEquals(expectedScript.getBytes(StandardCharsets.UTF_8), libraryString.getBytes(StandardCharsets.UTF_8));
			}
		});
	}
	
	@Test
	public void testRichTextFileResource() throws Exception {
		withResourceDxl("/dxl/testRichTextNavigator", database -> {
			FormulaQueryResult result = database.queryFormula("$TITLE='file-help_vampire.gif'", null, Collections.emptySet(), null, EnumSet.of(DocumentClass.ALLNONDATA));
			Document file = result.getDocuments().findFirst().orElseThrow(() -> new RuntimeException("Couldn't find design note"));
			byte[] expected = IOUtils.resourceToByteArray("/images/help_vampire.gif");
			_testFileResource(expected, file);
		});
	}
	
	@Test
	public void testRoundTripFileResource() throws Exception {
		withTempDb(database -> {
			byte[] fileData = IOUtils.resourceToByteArray("/images/help_vampire.gif");
			
			Document file = database.createDocument();
			try(RichTextWriter w = file.createRichTextItem("$FileData")) {
				try(InputStream is = new ByteArrayInputStream(fileData)) {
					w.addFileResource(is, fileData.length);
				}
			}
			
			_testFileResource(fileData, file);
		});
	}
	
	@Test
	public void testRoundTripFileResourceGuessLength() throws Exception {
		withTempDb(database -> {
			byte[] fileData = IOUtils.resourceToByteArray("/images/help_vampire.gif");
			
			Document file = database.createDocument();
			try(RichTextWriter w = file.createRichTextItem("$FileData")) {
				try(InputStream is = new ByteArrayInputStream(fileData)) {
					w.addFileResource(is, -1);
				}
			}
			
			_testFileResource(fileData, file);
		});
	}
	
	@Test
	public void testRoundTripFileResourceFilesystem() throws Exception {
		withTempDb(database -> {
			Path tempFile = Files.createTempFile(getClass().getName(), ".dat");
			try {
				try(InputStream is = getClass().getResourceAsStream("/images/help_vampire.gif")) {
					Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
				}
				
				Document file = database.createDocument();
				try(RichTextWriter w = file.createRichTextItem("$FileData")) {
					w.addFileResource(tempFile);
				}
				
				_testFileResource(IOUtils.resourceToByteArray("/images/help_vampire.gif"), file);
			} finally {
				Files.deleteIfExists(tempFile);
			}
		});
	}
	
	private void _testFileResource(byte[] expected, Document file) {
		// Make sure the stored length is correct
		long imageSize = file.getRichTextItem("$FileData").process(new GetFileResourceSizeProcessor());
		assertEquals(imageSize, expected.length);
		
		// Read out the image data
		ByteArrayOutputStream fileStream = new ByteArrayOutputStream();
		file.getRichTextItem("$FileData").process(new ExtractFileResourceProcessor(fileStream));
		
		// Read in the expected data
		assertArrayEquals(expected, fileStream.toByteArray());
	}
	
	@Test
	public void testTextExtraction() throws Exception {
		withTempDb((database) -> {
			Document doc = database.createDocument();
			
			String uuid1 = UUID.randomUUID().toString();
			String uuid2 = UUID.randomUUID().toString();
			
			String txtIn = uuid1 + System.lineSeparator() + uuid2;
			
			try (RichTextWriter w = doc.createRichTextItem("Body")) {
				w.addText(txtIn,
						w.createTextStyle("Default").setAlign(Justify.RIGHT),
						w.createFontStyle().setItalic(true),
						true // createParagraphOnLinebreak=true
						);
			}
			
			String txtOut = doc.getRichTextItem("Body").extractText();
			assertEquals(txtIn, txtOut);
		});
	}
}
