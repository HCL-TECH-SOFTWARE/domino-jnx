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
package it.com.hcl.domino.test.richtext;

import static it.com.hcl.domino.test.util.ITUtil.toCr;
import static it.com.hcl.domino.test.util.ITUtil.toLf;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.commons.richtext.RichTextUtil;
import com.hcl.domino.data.Attachment;
import com.hcl.domino.data.Attachment.Compression;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Document.IAttachmentProducer;
import com.hcl.domino.html.RichTextHTMLConverter;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.FontAttribute;
import com.hcl.domino.data.FormulaQueryResult;
import com.hcl.domino.data.Item;
import com.hcl.domino.data.StandardFonts;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.TextStyle.Justify;
import com.hcl.domino.richtext.conversion.AppendFileHotspotConversion;
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

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestRichtextNavigator extends AbstractNotesRuntimeTest {

  private void _testFileResource(final byte[] expected, final Document file) {
    // Make sure the stored length is correct
    final long imageSize = file.getRichTextItem("$FileData").process(new GetFileResourceSizeProcessor());
    Assertions.assertEquals(imageSize, expected.length);

    // Read out the image data
    final ByteArrayOutputStream fileStream = new ByteArrayOutputStream();
    file.getRichTextItem("$FileData").process(new ExtractFileResourceProcessor(fileStream));

    // Read in the expected data
    Assertions.assertArrayEquals(expected, fileStream.toByteArray());
  }

  private void _testImageResource(final byte[] expected, final Document image) {
    // Make sure the stored length is correct
    final long imageSize = image.getRichTextItem("$ImageData").process(new GetImageResourceSizeProcessor());
    Assertions.assertEquals(imageSize, expected.length);

    // Read out the image data
    final ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
    image.getRichTextItem("$ImageData").process(new ExtractImageResourceProcessor(imageStream));

    // Read in the expected data
    Assertions.assertArrayEquals(expected, imageStream.toByteArray());
  }

  @Test
  public void testCDTextInFlightChange() throws Exception {
    this.withResourceDxl("/dxl/testRichTextNavigator", database -> {
      final FormulaQueryResult result = database.queryFormula("$$TITLE='CD Record test'", null, Collections.emptySet(), null,
          EnumSet.of(DocumentClass.DOCUMENT));
      final Document doc = result.getDocuments().findFirst()
          .orElseThrow(() -> new RuntimeException("Couldn't find 'CD Record test' post note"));
      final List<RichTextRecord<?>> body = doc.getRichTextItem("Body");
      try (RichTextWriter w = doc.createRichTextItem("Body2")) {
        body.forEach(record -> {
          if (record instanceof CDText) {
            final int size = record.getCDRecordLength();
            final String text = ((CDText) record).getText();
            ((CDText) record).setText(text + " and then some");
            final int expectedSize = size + " and then some".length();
            Assertions.assertEquals(expectedSize, record.getCDRecordLength());
            Assertions.assertEquals(expectedSize, record.getHeader().getLength().intValue());
            Assertions.assertEquals(text + " and then some", ((CDText) record).getText());
          }
          w.addRichTextRecord(record);
        });
      }

      final List<RichTextRecord<?>> rt2 = doc.getRichTextItem("Body2");
      Assertions.assertEquals(body.size(), rt2.size(), "Body2 should have the same record count as Body");
      final List<RichTextRecord<?>> rt = rt2
          .stream()
          .filter(CDText.class::isInstance)
          .collect(Collectors.toList());

      {
        final CDText normalText = (CDText) rt.get(0);
        Assertions.assertEquals("This is normal text. and then some", normalText.getText());
        FontStyle style = normalText.getStyle();
        Assertions.assertEquals(StandardFonts.SWISS, style.getStandardFont().get());
        Assertions.assertTrue(style.getAttributes().isEmpty(), "Text should have no attributes");

        style.setBold(true)
            .setPointSize(24)
            .setSub(true);
        Assertions.assertEquals(EnumSet.of(FontAttribute.BOLD, FontAttribute.SUB), style.getAttributes(),
            "Text should now be bold and subscript");
        Assertions.assertEquals(24, style.getPointSize(), "Point size should be 24");

        style = doc.createRichTextItem("foo").createFontStyle();
        style.setItalic(true)
            .setPointSize(22)
            .setSuper(true)
            .setStandardFont(StandardFonts.USERINTERFACE);
        normalText.setStyle(style);
        style = normalText.getStyle();
        Assertions.assertEquals(EnumSet.of(FontAttribute.ITALIC, FontAttribute.SUPER), style.getAttributes(),
            "Text should now be italic and superscript");
        style.setExtrude(true);
        Assertions.assertEquals(22, style.getPointSize(), "Point size should be 22");
        Assertions.assertEquals(StandardFonts.USERINTERFACE, style.getStandardFont().get(), "Font should be USERINTERFACE");
      }

      {
        final CDText monospaceText = (CDText) rt.get(2);
        Assertions.assertEquals("This is monospace text and then some", monospaceText.getText());
        final FontStyle style = monospaceText.getStyle();
        Assertions.assertEquals(StandardFonts.TYPEWRITER, style.getStandardFont().get());
        Assertions.assertTrue(style.getAttributes().isEmpty(), "Text should have no attributes");
      }

      {
        final CDText serifText = (CDText) rt.get(4);
        Assertions.assertEquals("This is serif italic bold text and then some", serifText.getText());
        final FontStyle style = serifText.getStyle();
        Assertions.assertEquals(StandardFonts.SWISS, style.getStandardFont().get());
        Assertions.assertEquals(EnumSet.of(FontAttribute.BOLD, FontAttribute.ITALIC), style.getAttributes(),
            "Text should be bold and italic");
      }
    });
  }

  @Test
  public void testReadCDText() throws Exception {
    this.withResourceDxl("/dxl/testRichTextNavigator", database -> {
      final FormulaQueryResult result = database.queryFormula("$$TITLE='CD Record test'", null, Collections.emptySet(), null,
          EnumSet.of(DocumentClass.DOCUMENT));
      final Document doc = result.getDocuments().findFirst()
          .orElseThrow(() -> new RuntimeException("Couldn't find 'CD Record test' post note"));
      final List<RichTextRecord<?>> rt = doc.getRichTextItem("Body")
          .stream()
          .filter(CDText.class::isInstance)
          .collect(Collectors.toList());

      {
        final CDText normalText = (CDText) rt.get(0);
        Assertions.assertEquals("This is normal text.", normalText.getText());
        FontStyle style = normalText.getStyle();
        Assertions.assertEquals(StandardFonts.SWISS, style.getStandardFont().get());
        Assertions.assertTrue(style.getAttributes().isEmpty(), "Text should have no attributes");

        style.setBold(true)
            .setPointSize(24)
            .setSub(true);
        Assertions.assertEquals(EnumSet.of(FontAttribute.BOLD, FontAttribute.SUB), style.getAttributes(),
            "Text should now be bold and subscript");
        Assertions.assertEquals(24, style.getPointSize(), "Point size should be 24");

        style = doc.createRichTextItem("foo").createFontStyle();
        style.setItalic(true)
            .setPointSize(22)
            .setSuper(true)
            .setStandardFont(StandardFonts.USERINTERFACE);
        normalText.setStyle(style);
        style = normalText.getStyle();
        Assertions.assertEquals(EnumSet.of(FontAttribute.ITALIC, FontAttribute.SUPER), style.getAttributes(),
            "Text should now be italic and superscript");
        style.setExtrude(true);
        Assertions.assertEquals(22, style.getPointSize(), "Point size should be 22");
        Assertions.assertEquals(StandardFonts.USERINTERFACE, style.getStandardFont().get(), "Font should be USERINTERFACE");
      }

      {
        final CDText monospaceText = (CDText) rt.get(2);
        Assertions.assertEquals("This is monospace text", monospaceText.getText());
        final FontStyle style = monospaceText.getStyle();
        Assertions.assertEquals(StandardFonts.TYPEWRITER, style.getStandardFont().get());
        Assertions.assertTrue(style.getAttributes().isEmpty(), "Text should have no attributes");
      }

      {
        final CDText serifText = (CDText) rt.get(4);
        Assertions.assertEquals("This is serif italic bold text", serifText.getText());
        final FontStyle style = serifText.getStyle();
        Assertions.assertEquals(StandardFonts.SWISS, style.getStandardFont().get());
        Assertions.assertEquals(EnumSet.of(FontAttribute.BOLD, FontAttribute.ITALIC), style.getAttributes(),
            "Text should be bold and italic");
      }
    });
  }
  
  @Test
  public void testReadCDTextAsText() throws Exception {
    this.withResourceDxl("/dxl/testRichTextNavigator", database -> {
      final FormulaQueryResult result = database.queryFormula("$$TITLE='CD Record test'", null, Collections.emptySet(), null,
          EnumSet.of(DocumentClass.DOCUMENT));
      final Document doc = result.getDocuments().findFirst()
          .orElseThrow(() -> new RuntimeException("Couldn't find 'CD Record test' post note"));
      
      final Item item = doc.getFirstItem("Body").get();
      Assertions.assertEquals("This is normal text.\r\n\r\nThis is monospace text\r\n\r\nThis is serif italic bold text", item.getAsText(' '));
    });
  }

  @Test
  public void testReadClientJSLibrary() throws Exception {
    this.withResourceDxl("/dxl/testRichTextNavigator", database -> {
      final FormulaQueryResult result = database.queryFormula("$TITLE='Client JS Library'", null, Collections.emptySet(), null,
          EnumSet.of(DocumentClass.ALLNONDATA));
      final Document library = result.getDocuments().findFirst()
          .orElseThrow(() -> new RuntimeException("Couldn't find design note"));
      final String expected = IOUtils.resourceToString("/text/clientjs.js", StandardCharsets.UTF_8) + "\r\n";

      // Make sure the blob part length is correct
      final long len = library.getRichTextItem("$JavaScriptLibrary")
          .stream()
          .filter(CDEvent.class::isInstance)
          .map(CDEvent.class::cast)
          .findFirst()
          .map(CDEvent::getActionLength)
          .orElseThrow(() -> new IllegalStateException("Couldn't find CDEvent record"));
      final byte[] expectedBytes = expected.getBytes(RichTextUtil.LMBCS);
      int expectedLen = expectedBytes.length;
      // Account for a final null that Domino adds
      expectedLen += expectedLen % 2;
      Assertions.assertEquals(expectedLen, len);

      // Read out the script data
      final ByteArrayOutputStream libraryStream = new ByteArrayOutputStream();
      library.getRichTextItem("$JavaScriptLibrary")
          .stream()
          .filter(CDBlobPart.class::isInstance)
          .map(CDBlobPart.class::cast)
          .forEach(record -> {
            try {
              libraryStream.write(record.getBlobPartData());
            } catch (final IOException e) {
              throw new UncheckedIOException(e);
            }
          });
      final String libraryString = new String(libraryStream.toByteArray(), RichTextUtil.LMBCS);

      final byte[] lineEndingExpected = toCr(expected).getBytes();
      final byte[] libraryBytes = toCr(toLf(libraryString)).getBytes();
      Assertions.assertArrayEquals(lineEndingExpected, libraryBytes);
    });
  }

  @Test
  public void testRichTextFileResource() throws Exception {
    this.withResourceDxl("/dxl/testRichTextNavigator", database -> {
      final FormulaQueryResult result = database.queryFormula("$TITLE='file-help_vampire.gif'", null, Collections.emptySet(), null,
          EnumSet.of(DocumentClass.ALLNONDATA));
      final Document file = result.getDocuments().findFirst().orElseThrow(() -> new RuntimeException("Couldn't find design note"));
      final byte[] expected = IOUtils.resourceToByteArray("/images/help_vampire.gif");
      this._testFileResource(expected, file);
    });
  }

  @Test
  public void testRichTextImageResource() throws Exception {
    this.withResourceDxl("/dxl/testRichTextNavigator", database -> {
      final FormulaQueryResult result = database.queryFormula("$TITLE='help_vampire.gif'", null, Collections.emptySet(), null,
          EnumSet.of(DocumentClass.ALLNONDATA));
      final Document image = result.getDocuments().findFirst().orElseThrow(() -> new RuntimeException("Couldn't find design note"));
      final byte[] expected = IOUtils.resourceToByteArray("/images/help_vampire.gif");

      this._testImageResource(expected, image);
    });
  }

  // Test in case the implementation switches to using EnumComposite*
  // This also uses lower-level APIs for checking record types vs. what
  // testRichTextImageResource uses
  @Test
  public void testRichTextImageResourceForEach() throws Exception {
    this.withResourceDxl("/dxl/testRichTextNavigator", database -> {
      final FormulaQueryResult result = database.queryFormula("$TITLE='help_vampire.gif'", null, Collections.emptySet(), null,
          EnumSet.of(DocumentClass.ALLNONDATA));
      final Document image = result.getDocuments().findFirst().orElseThrow(() -> new RuntimeException("Couldn't find design note"));
      final ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
      image.getRichTextItem("$ImageData").forEach(record -> {
        if (!record.getType().contains(RecordType.IMAGESEGMENT)) {
          return;
        }
        // LSIG Header (6)
        // WORD DataSize (2)
        // WORD SegSize (2)
        // Data

        final ByteBuffer data = record.getDataWithoutHeader();
        final int dataLen = Short.toUnsignedInt(data.getShort(2));
        final byte[] segData = new byte[dataLen];
        data.position(4);
        data.get(segData);
        try {
          imageStream.write(segData);
        } catch (final IOException e) {
          throw new UncheckedIOException(e);
        }
      });

      // Read in the expected data
      final byte[] expected = IOUtils.resourceToByteArray("/images/help_vampire.gif");
      Assertions.assertArrayEquals(expected, imageStream.toByteArray());
    });
  }

  @Test
  public void testRichTextItemValue() throws Exception {
    this.withResourceDxl("/dxl/testRichTextNavigator", database -> {
      final FormulaQueryResult result = database.queryFormula("$TITLE='help_vampire.gif'", null, Collections.emptySet(), null,
          EnumSet.of(DocumentClass.ALLNONDATA));
      final Document image = result.getDocuments().findFirst().orElseThrow(() -> new RuntimeException("Couldn't find design note"));
      final List<Object> value = image.getFirstItem("$ImageData").get().getValue();
      Assertions.assertNotNull(value);
      Assertions.assertFalse(value.isEmpty());
      Assertions.assertTrue(value.get(0) instanceof RichTextRecord);
    });
  }

  @Test
  public void testRoundTripClientJSLibrary() throws Exception {
    this.withTempDb(database -> {
      final String scriptContent = IOUtils.resourceToString("/text/clientjs.js", StandardCharsets.UTF_8);
      final Document library = database.createDocument();

      // Write the content
      try (RichTextWriter w = library.createRichTextItem("$JavaScriptLibrary")) {
        w.addJavaScriptLibraryData(scriptContent);
      }

      {
        // Make sure the blob part length is correct
        final long len = library.getRichTextItem("$JavaScriptLibrary")
            .stream()
            .filter(CDEvent.class::isInstance)
            .map(CDEvent.class::cast)
            .findFirst()
            .map(CDEvent::getActionLength)
            .orElseThrow(() -> new IllegalStateException("Couldn't find CDEvent record"));
        final byte[] expectedBytes = scriptContent.getBytes(RichTextUtil.LMBCS);
        int expectedLen = expectedBytes.length + 1; // For the above added \0
        // Account for word boundaries
        expectedLen += expectedLen % 2;
        Assertions.assertEquals(expectedLen, len);

        // Read out the script data
        final ByteArrayOutputStream libraryStream = new ByteArrayOutputStream();
        library.getRichTextItem("$JavaScriptLibrary")
            .stream()
            .filter(CDBlobPart.class::isInstance)
            .map(CDBlobPart.class::cast)
            .forEach(record -> {
              try {
                libraryStream.write(record.getBlobPartData());
              } catch (final IOException e) {
                throw new UncheckedIOException(e);
              }
            });
        final String libraryString = new String(libraryStream.toByteArray(), RichTextUtil.LMBCS);
        // It ends up with an extra newline thanks to the code ensuring that there's a
        // trailing newline
        final String expectedScript = scriptContent + "\n";

        Assertions.assertArrayEquals(toLf(expectedScript).getBytes(StandardCharsets.UTF_8),
            toLf(libraryString).getBytes(StandardCharsets.UTF_8));
      }
    });
  }

  @Test
  public void testRoundTripFileResource() throws Exception {
    this.withTempDb(database -> {
      final byte[] fileData = IOUtils.resourceToByteArray("/images/help_vampire.gif");

      final Document file = database.createDocument();
      try (RichTextWriter w = file.createRichTextItem("$FileData")) {
        try (InputStream is = new ByteArrayInputStream(fileData)) {
          w.addFileResource(is, fileData.length);
        }
      }

      this._testFileResource(fileData, file);
    });
  }

  @Test
  public void testRoundTripFileResourceFilesystem() throws Exception {
    this.withTempDb(database -> {
      final Path tempFile = Files.createTempFile(this.getClass().getName(), ".dat");
      try {
        try (InputStream is = this.getClass().getResourceAsStream("/images/help_vampire.gif")) {
          Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }

        final Document file = database.createDocument();
        try (RichTextWriter w = file.createRichTextItem("$FileData")) {
          w.addFileResource(tempFile);
        }

        this._testFileResource(IOUtils.resourceToByteArray("/images/help_vampire.gif"), file);
      } finally {
        Files.deleteIfExists(tempFile);
      }
    });
  }

  @Test
  public void testRoundTripFileResourceGuessLength() throws Exception {
    this.withTempDb(database -> {
      final byte[] fileData = IOUtils.resourceToByteArray("/images/help_vampire.gif");

      final Document file = database.createDocument();
      try (RichTextWriter w = file.createRichTextItem("$FileData")) {
        try (InputStream is = new ByteArrayInputStream(fileData)) {
          w.addFileResource(is, -1);
        }
      }

      this._testFileResource(fileData, file);
    });
  }

  @Test
  public void testRoundTripImageResource() throws Exception {
    this.withTempDb(database -> {
      final byte[] expected = IOUtils.resourceToByteArray("/images/help_vampire.gif");

      final Document image = database.createDocument();

      try (RichTextWriter w = image.createRichTextItem("$ImageData")) {
        try (InputStream is = new ByteArrayInputStream(expected)) {
          w.addImageResource(is, expected.length);
        }
      }

      this._testImageResource(expected, image);
    });
  }

  @Test
  public void testRoundTripImageResourceFilesystem() throws Exception {
    this.withTempDb(database -> {
      final byte[] expected = IOUtils.resourceToByteArray("/images/help_vampire.gif");
      final Path tempFile = Files.createTempFile(this.getClass().getName(), ".gif");
      try (InputStream is = new ByteArrayInputStream(expected)) {
        Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
      }
      try {
        final Document image = database.createDocument();

        try (RichTextWriter w = image.createRichTextItem("$ImageData")) {
          w.addImageResource(tempFile);
        }

        this._testImageResource(expected, image);
      } finally {
        Files.deleteIfExists(tempFile);
      }
    });
  }

  @Test
  public void testRoundTripImageResourceGuessLength() throws Exception {
    this.withTempDb(database -> {
      final byte[] expected = IOUtils.resourceToByteArray("/images/help_vampire.gif");

      final Document image = database.createDocument();

      try (RichTextWriter w = image.createRichTextItem("$ImageData")) {
        try (InputStream is = new ByteArrayInputStream(expected)) {
          w.addImageResource(is, -1);
        }
      }

      this._testImageResource(expected, image);
    });
  }

  @Test
  public void testRTNav() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();

      // we create a document with an attached file that is visible in the body
      // richtext

      final Attachment att = doc.attachFile("test.txt",
          Instant.now(), Instant.now(),
          new IAttachmentProducer() {

            @Override
            public long getSizeEstimation() {
              return -1;
            }

            @Override
            public void produceAttachment(final OutputStream out) throws IOException {
              out.write("TEST!".getBytes(Charset.forName("UTF-8")));
            }
          });

      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addText("Hello");
        rtWriter.addAttachmentIcon(att, "testfile.txt");
      }

      // first we use the richtext navigator to check if all expected CD records are
      // there

      // System.out.println("Before conversion:\n");
      {
        // JNARichTextRecord [type=[PARAGRAPH], totallength=2, headerlength=2
        // datalength=0]
        // JNARichTextRecord [type=[TEXT], totallength=13, headerlength=4 datalength=9]
        // JNARichTextRecord [type=[BEGIN], totallength=6, headerlength=2 datalength=4]
        // JNARichTextRecord [type=[HOTSPOTBEGIN], totallength=34, headerlength=4
        // datalength=30]
        // JNARichTextRecord [type=[GRAPHIC], totallength=28, headerlength=6
        // datalength=22]
        // JNARichTextRecord [type=[IMAGEHEADER], totallength=28, headerlength=6
        // datalength=22]
        // JNARichTextRecord [type=[IMAGESEGMENT], totallength=764, headerlength=6
        // datalength=758]
        // JNARichTextRecord [type=[CAPTION], totallength=40, headerlength=4
        // datalength=36]
        // JNARichTextRecord [type=[HOTSPOTEND], totallength=2, headerlength=2
        // datalength=0]
        // JNARichTextRecord [type=[END], totallength=6, headerlength=2 datalength=4]

        boolean hasParagraph = false;
        boolean hasText = false;
        boolean hasImageHeader = false;
        boolean hasHotspotBegin = false;
        boolean hasHotspotEnd = false;

        final List<RichTextRecord<?>> rtNav = doc.getRichTextItem("body");
        for (final RichTextRecord<?> record : rtNav) {
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

          // System.out.println(record);
        }

        Assertions.assertTrue(hasParagraph);
        Assertions.assertTrue(hasText);
        Assertions.assertTrue(hasImageHeader);
        Assertions.assertTrue(hasHotspotBegin);
        Assertions.assertTrue(hasHotspotEnd);
      }

      // now use a richtext conversion to remove the attachment icon from the richtext
      doc.convertRichTextItem("Body", new RemoveAttachmentIconConversion(att));

      // let's see if the image and hotspot could be removed

      // System.out.println("After conversion:\n");
      {
        // JNARichTextRecord [type=[PARAGRAPH], totallength=2, headerlength=2
        // datalength=0]
        // JNARichTextRecord [type=[TEXT], totallength=13, headerlength=4 datalength=9]
        // JNARichTextRecord [type=[TEXT], totallength=8, headerlength=4 datalength=4]

        boolean hasParagraph = false;
        boolean hasText = false;
        boolean hasImageHeader = false;
        boolean hasHotspotBegin = false;
        boolean hasHotspotEnd = false;

        final List<RichTextRecord<?>> rtNav = doc.getRichTextItem("body");
        for (final RichTextRecord<?> record : rtNav) {
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

          // System.out.println(record);
        }

        Assertions.assertTrue(hasParagraph);
        Assertions.assertTrue(hasText);

        Assertions.assertFalse(hasImageHeader);
        Assertions.assertFalse(hasHotspotBegin);
        Assertions.assertFalse(hasHotspotEnd);
      }

    });
  }

  @Test
  public void testTextExtraction() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();

      final String uuid1 = UUID.randomUUID().toString();
      final String uuid2 = UUID.randomUUID().toString();

      final String txtIn = uuid1 + System.lineSeparator() + uuid2;

      try (RichTextWriter w = doc.createRichTextItem("Body")) {
        w.addText(txtIn,
            w.createTextStyle("Default").setAlign(Justify.RIGHT),
            w.createFontStyle().setItalic(true),
            true // createParagraphOnLinebreak=true
        );
      }

      final String txtOut = doc.getRichTextItem("Body").extractText();
      Assertions.assertEquals(txtIn, txtOut);
    });
  }
  
  /**
   * Tests for correct behavior when attaching multiple files to a
   * document and then adding references to them in the same RT field
   */
  @Test
  public void testMultipleAttachmentsInBody() throws Exception {
    withTempDb(database -> {
      Path tempA = Files.createTempFile("tempA", ".txt");
      Path tempB = Files.createTempFile("tempB", ".txt");
      Path tempC = Files.createTempFile("tempC", ".txt");
      try {
        Files.write(tempA, Collections.singleton("hello A"));
        Files.write(tempB, Collections.singleton("hello B"));
        Files.write(tempC, Collections.singleton("hello C"));
        
        Document doc = database.createDocument();
        
        // Attach each, saving after each one as upstream does
        {
          Attachment att = doc.attachFile(tempA.toString(), tempA.getFileName().toString(), Compression.NONE);
          final RichTextWriter rtWriter = doc.createRichTextItem("Body");
          rtWriter.addAttachmentIcon(att, tempA.getFileName().toString());
          doc.save();
        }
        {
          Attachment att = doc.attachFile(tempB.toString(), tempB.getFileName().toString(), Compression.NONE);
          doc.convertRichTextItem("Body", new AppendFileHotspotConversion(att, tempB.getFileName().toString()));
          doc.save();
        }
        {
          Attachment att = doc.attachFile(tempC.toString(), tempC.getFileName().toString(), Compression.NONE);
          doc.convertRichTextItem("Body", new AppendFileHotspotConversion(att, tempC.getFileName().toString()));
          doc.save();
        }
        
        // Convert the Body field to HTML
        RichTextHTMLConverter conv = database.getParentDominoClient().getRichTextHtmlConverter();
        String html = conv.renderItem(doc, "Body").convert().getHtml();
        assertTrue(html.contains(tempA.getFileName().toString()), () -> "Body is missing tempA");
        assertTrue(html.contains(tempB.getFileName().toString()), () -> "Body is missing tempB");
        assertTrue(html.contains(tempC.getFileName().toString()), () -> "Body is missing tempC");
      } finally {
        Files.deleteIfExists(tempA);
        Files.deleteIfExists(tempB);
        Files.deleteIfExists(tempC);
      }
    });
  }
  
  /**
   * Tests for correct behavior when attaching multiple files to a
   * document and then adding references to them in the same RT field
   */
  @Test
  public void testMultipleAttachmentsInBodyReopen() throws Exception {
    withTempDb(database -> {
      Path tempA = Files.createTempFile("tempA", ".txt");
      Path tempB = Files.createTempFile("tempB", ".txt");
      Path tempC = Files.createTempFile("tempC", ".txt");
      try {
        Files.write(tempA, Collections.singleton("hello A"));
        Files.write(tempB, Collections.singleton("hello B"));
        Files.write(tempC, Collections.singleton("hello C"));
        
        Document doc = database.createDocument();
        String unid;
        
        // Attach each, saving after each one as upstream does
        {
          Attachment att = doc.attachFile(tempA.toString(), tempA.getFileName().toString(), Compression.NONE);
          final RichTextWriter rtWriter = doc.createRichTextItem("Body");
          rtWriter.addAttachmentIcon(att, tempA.getFileName().toString());
          doc.save();
          unid = doc.getUNID();
        }
        {
          Database db = database.reopen();
          doc = db.getDocumentByUNID(unid).get();
          Attachment att = doc.attachFile(tempB.toString(), tempB.getFileName().toString(), Compression.NONE);
          doc.convertRichTextItem("Body", new AppendFileHotspotConversion(att, tempB.getFileName().toString()));
          doc.save();
        }
        {
          Database db = database.reopen();
          doc = db.getDocumentByUNID(unid).get();
          Attachment att = doc.attachFile(tempC.toString(), tempC.getFileName().toString(), Compression.NONE);
          doc.convertRichTextItem("Body", new AppendFileHotspotConversion(att, tempC.getFileName().toString()));
          doc.save();
        }
        
        // Convert the Body field to HTML
        {
          Database db = database.reopen();
          doc = db.getDocumentByUNID(unid).get();
          RichTextHTMLConverter conv = database.getParentDominoClient().getRichTextHtmlConverter();
          String html = conv.renderItem(doc, "Body").convert().getHtml();
          assertTrue(html.contains(tempA.getFileName().toString()), () -> "Body is missing tempA");
          assertTrue(html.contains(tempB.getFileName().toString()), () -> "Body is missing tempB");
          assertTrue(html.contains(tempC.getFileName().toString()), () -> "Body is missing tempC");
        }
      } finally {
        Files.deleteIfExists(tempA);
        Files.deleteIfExists(tempB);
        Files.deleteIfExists(tempC);
      }
    });
  }
}
