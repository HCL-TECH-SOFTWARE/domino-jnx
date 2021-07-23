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
package it.com.hcl.domino.test.mime;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoException;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Item;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.mime.MimeData;
import com.hcl.domino.mime.MimeWriter;
import com.hcl.domino.mime.RichTextMimeConversionSettings;
import com.hcl.domino.mime.RichTextMimeConversionSettings.MessageContentEncoding;
import com.hcl.domino.mime.attachments.ByteArrayMimeAttachment;
import com.hcl.domino.mime.attachments.IMimeAttachment;
import com.hcl.domino.mime.attachments.UrlMimeAttachment;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.TextStyle.Justify;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestMimeDataItemType extends AbstractNotesRuntimeTest {
  private static final String TEST_IMAGE_PATH = "/images/test-png-large.png";

  /**
   * We check how the API behaves when reading {@link MimeData} from
   * non MIME_PART items
   * 
   * @throws Exception in case of errors
   */
  @Test
  public void testReadInvalidMimeData() throws Exception {
    this.withTempDb(db -> {
      final DominoClient client = this.getClient();

      final Document doc = db.createDocument();

      final MimeData mimeData = doc.get("ItemDoesNotExist", MimeData.class, null);
      Assertions.assertNull(mimeData);

      doc.replaceItemValue("TextItem", "Hello world.");

      {
        // using the MIME stream C API on a non MIME_PART item should throw an error
        DominoException ex = null;
        try {
          doc.get("TextItem", MimeData.class, null);
        } catch (final DominoException e) {
          ex = e;
        }
        Assertions.assertNotNull(ex);
        Assertions.assertTrue(ex.getId() == 1184); // "MIME part not found"
      }

      // now we create formatted richtext content
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body");) {

        rtWriter.addText("Hello World.",
            rtWriter.createTextStyle("MyStyle").setAlign(Justify.RIGHT),
            rtWriter.createFontStyle().setBold(true));
      }

      Item itm = doc.getFirstItem("Body").get();
      // make sure it's richtext
      Assertions.assertEquals(itm.getType(), ItemDataType.TYPE_COMPOSITE);

      {
        // should also throw an error, still no MIME_PART item
        DominoException ex = null;
        try {
          doc.get("Body", MimeData.class, null);
        } catch (final DominoException e) {
          ex = e;
        }
        Assertions.assertNotNull(ex);
        Assertions.assertTrue(ex.getId() == 1184); // "MIME part not found"
      }

      // now we convert the richtext item to mime
      final MimeWriter mimeWriter = client.getMimeWriter();
      final RichTextMimeConversionSettings convSettings = mimeWriter.createRichTextMimeConversionSettings().setDefaults()
          .setMessageContentEncoding(MessageContentEncoding.TEXT_PLAIN_AND_HTML_WITH_IMAGES_ATTACHMENTS);

      mimeWriter.convertToMime(doc, convSettings);

      // make sure it's really MIME_PART
      itm = doc.getFirstItem("Body").get();
      Assertions.assertEquals(itm.getType(), ItemDataType.TYPE_MIME_PART);

      final MimeData mimeDataFromRichText = doc.get("Body", MimeData.class, null);
      Assertions.assertNotNull(mimeDataFromRichText);

      // <html><body><div align="right"><font size="1" face="serif"><b>Hello
      // World.</b></font></div></body></html>
      final String html = mimeDataFromRichText.getHtml();
      // Hello World.
      final String text = mimeDataFromRichText.getPlainText();

      Assertions.assertTrue(html.length() > 0);
      Assertions.assertTrue(text.length() > 0);
    });
  }

  @Test
  public void testWriteReadMimeData() throws Exception {
    this.withTempDb(db -> {
      // compose the MIME data to write:
      final MimeData writtenMimeData = new MimeData();

      // embed an image via URL and remember its content id (cid)
      final URL url = this.getClass().getResource(TestMimeDataItemType.TEST_IMAGE_PATH);
      Assertions.assertNotNull(url, "Test image can be found: " + TestMimeDataItemType.TEST_IMAGE_PATH);
      final String cid = writtenMimeData.embed(new UrlMimeAttachment(url));

      // add the first test file
      final int writtenAttachment1Size = 50000;
      final byte[] writtenAttachment1Data = this.produceTestData(writtenAttachment1Size);
      writtenMimeData.attach(new ByteArrayMimeAttachment(writtenAttachment1Data, "test.txt"));

      // and the second one
      final int writtenAttachment2Size = 20000;
      final byte[] writtenAttachment2Data = this.produceTestData(writtenAttachment2Size);
      writtenMimeData.attach(new ByteArrayMimeAttachment(writtenAttachment2Data, "test2.txt"));

      // set html (with link to embedded image) and alternative plaintext
      final String html = "<html><body>This is <b>formatted</b> text and an image:<br><img src=\"cid:" + cid + "\"></body></html>";
      writtenMimeData.setHtml(html);
      final String plainText = "This is alternative plaintext";
      writtenMimeData.setPlainText(plainText);

      // and write it to a temp document
      final Document doc = db.createDocument();
      doc.replaceItemValue("BodyMime", writtenMimeData);

      // now read the MIME data and compare it with what we have just written
      final Optional<Item> itm = doc.getFirstItem("BodyMime");
      Assertions.assertTrue(itm.isPresent());
      Assertions.assertEquals(ItemDataType.TYPE_MIME_PART, itm.get().getType());

      final MimeData checkMimeData = doc.get("BodyMime", MimeData.class, null);
      Assertions.assertNotNull(checkMimeData, "MIMEItemData not null");

      final String checkHtml = checkMimeData.getHtml();
      Assertions.assertEquals(html, checkHtml);
      final String checkPlainText = checkMimeData.getPlainText();
      Assertions.assertEquals(plainText, checkPlainText);

      // check that no embed has been removed
      for (final String currWrittenCid : writtenMimeData.getContentIds()) {
        final IMimeAttachment checkEmbed = checkMimeData.getEmbed(currWrittenCid).orElse(null);

        Assertions.assertNotNull(checkEmbed, "Embed with cid " + currWrittenCid + " not null");
      }

      // check that no embed has been added
      for (final String currCheckCid : checkMimeData.getContentIds()) {
        final IMimeAttachment currEmbed = writtenMimeData.getEmbed(currCheckCid).orElse(null);

        Assertions.assertNotNull(currEmbed, "Embed with cid " + currCheckCid + " not null");
      }

      final List<IMimeAttachment> writtenAttachments = writtenMimeData.getAttachments();
      final List<IMimeAttachment> checkAttachments = checkMimeData.getAttachments();

      // check that no attachment has been removed
      for (final IMimeAttachment currAtt : writtenAttachments) {
        final String currFilename = currAtt.getFileName();

        IMimeAttachment checkAtt = null;
        for (final IMimeAttachment currCheckAtt : checkAttachments) {
          if (currFilename.equals(currCheckAtt.getFileName())) {
            checkAtt = currCheckAtt;
            break;
          }
        }

        Assertions.assertNotNull(checkAtt, "file " + currFilename + " could be found");
      }

      // check that no attachment has been added
      for (final IMimeAttachment currCheckAtt : checkAttachments) {
        final String currFilename = currCheckAtt.getFileName();

        IMimeAttachment writtenAtt = null;
        for (final IMimeAttachment currWrittenAtt : writtenAttachments) {
          if (currFilename.equals(currWrittenAtt.getFileName())) {
            writtenAtt = currCheckAtt;
            break;
          }
        }

        Assertions.assertNotNull(writtenAtt, "file " + currFilename + " could be found");
      }
    });

  }

  @Test
  public void testWriteReadMimeDataBasic() throws Exception {
    this.withTempDb(db -> {
      // compose the MIME data to write:
      final MimeData writtenMimeData = new MimeData();

      // set html (with link to embedded image) and alternative plaintext
      final String html = "<html><body>This is <b>formatted</b> text and no image.</body></html>";
      writtenMimeData.setHtml(html);
      final String plainText = "This is alternative plaintext";
      writtenMimeData.setPlainText(plainText);

      // and write it to a temp document
      final Document doc = db.createDocument();
      doc.replaceItemValue("BodyMime", writtenMimeData);

      // now read the MIME data and compare it with what we have just written
      final Optional<Item> itm = doc.getFirstItem("BodyMime");
      Assertions.assertTrue(itm.isPresent());
      Assertions.assertEquals(ItemDataType.TYPE_MIME_PART, itm.get().getType());

      final MimeData checkMimeData = doc.get("BodyMime", MimeData.class, null);
      Assertions.assertNotNull(checkMimeData, "MIMEItemData not null");

      final String checkHtml = checkMimeData.getHtml();
      Assertions.assertEquals(html, checkHtml);
      final String checkPlainText = checkMimeData.getPlainText();
      Assertions.assertEquals(plainText, checkPlainText);

      // check that no embed has been removed
      for (final String currWrittenCid : writtenMimeData.getContentIds()) {
        final IMimeAttachment checkEmbed = checkMimeData.getEmbed(currWrittenCid).orElse(null);

        Assertions.assertNotNull(checkEmbed, "Embed with cid " + currWrittenCid + " not null");
      }

      // check that no embed has been added
      for (final String currCheckCid : checkMimeData.getContentIds()) {
        final IMimeAttachment currEmbed = writtenMimeData.getEmbed(currCheckCid).orElse(null);

        Assertions.assertNotNull(currEmbed, "Embed with cid " + currCheckCid + " not null");
      }

      final List<IMimeAttachment> writtenAttachments = writtenMimeData.getAttachments();
      final List<IMimeAttachment> checkAttachments = checkMimeData.getAttachments();

      // check that no attachment has been removed
      for (final IMimeAttachment currAtt : writtenAttachments) {
        final String currFilename = currAtt.getFileName();

        IMimeAttachment checkAtt = null;
        for (final IMimeAttachment currCheckAtt : checkAttachments) {
          if (currFilename.equals(currCheckAtt.getFileName())) {
            checkAtt = currCheckAtt;
            break;
          }
        }

        Assertions.assertNotNull(checkAtt, "file " + currFilename + " could be found");
      }

      // check that no attachment has been added
      for (final IMimeAttachment currCheckAtt : checkAttachments) {
        final String currFilename = currCheckAtt.getFileName();

        IMimeAttachment writtenAtt = null;
        for (final IMimeAttachment currWrittenAtt : writtenAttachments) {
          if (currFilename.equals(currWrittenAtt.getFileName())) {
            writtenAtt = currCheckAtt;
            break;
          }
        }

        Assertions.assertNotNull(writtenAtt, "file " + currFilename + " could be found");
      }
    });

  }

}
