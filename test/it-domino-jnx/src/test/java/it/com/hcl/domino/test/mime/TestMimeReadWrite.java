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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.FormulaQueryResult;
import com.hcl.domino.data.Item;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.exception.MimePartNotFoundException;
import com.hcl.domino.html.HtmlConversionResult;
import com.hcl.domino.html.HtmlConvertOption;
import com.hcl.domino.mime.MimeData;
import com.hcl.domino.mime.MimeReader;
import com.hcl.domino.mime.MimeReader.ReadMimeDataType;
import com.hcl.domino.mime.MimeWriter;
import com.hcl.domino.mime.MimeWriter.WriteMimeDataType;
import com.hcl.domino.mime.RichTextMimeConversionSettings.MessageContentEncoding;
import com.hcl.domino.richtext.RichTextWriter;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;
import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

@SuppressWarnings("nls")
public class TestMimeReadWrite extends AbstractNotesRuntimeTest {
  private static final String TEST_IMAGE_PATH = "/images/test-png-large.png";

  @Test
  public void testConvertAndOpenMimeItem() throws Exception {
    this.withResourceDxl("/dxl/testConvertAndOpenMimeItem", database -> {
      final FormulaQueryResult result = database.queryFormula("$$TITLE='CD Record test'", null, Collections.emptySet(), null,
          EnumSet.of(DocumentClass.DOCUMENT));
      final Document doc = result.getDocuments().findFirst()
          .orElseThrow(() -> new RuntimeException("Couldn't find 'CD Record test' post note"));

      final MimeWriter w = database.getParentDominoClient().getMimeWriter();
      w.convertToMime(doc,
          w.createRichTextMimeConversionSettings()
              .setMessageContentEncoding(MessageContentEncoding.TEXT_PLAIN_AND_HTML_WITH_IMAGES_ATTACHMENTS));
      final MimeMessage mime = database.getParentDominoClient().getMimeReader().readMIME(doc, "Body",
          EnumSet.allOf(MimeReader.ReadMimeDataType.class));
      Assertions.assertNotNull(mime);
      Assertions.assertTrue(mime.getContentType().startsWith("multipart/alternative"));
      Assertions.assertTrue(mime.getContent() instanceof MimeMultipart);
      final MimeMultipart content = (MimeMultipart) mime.getContent();

      final BodyPart textPlain = content.getBodyPart(0);
      Assertions.assertTrue(textPlain.getContentType().startsWith("text/plain"));
      Assertions.assertTrue(String.valueOf(textPlain.getContent()).contains("This is monospace text"));

      final BodyPart html = content.getBodyPart(1);
      Assertions.assertTrue(html.getContentType().startsWith("text/html"));
      Assertions.assertTrue(String.valueOf(html.getContent()).contains("<tt><font size=\"2\">This is monospace text</font></tt>"));
    });
  }

  @Test
  public void testCreateMail() throws Exception {
    final DominoClient client = this.getClient();

    this.withTempDb(dbMail -> {
      final HtmlEmail mail = new HtmlEmail();

      // add some required fields required by Apache Commons Email
      mail.setFrom("mr.sender@acme.com", "Mr. Sender");
      mail.addTo("mr.receiver@acme.com", "Mr. Receiver");
      mail.setHostName("acme.com");

      final String subjectWrite = "Testmail";
      mail.setSubject(subjectWrite);
      mail.setCharset("UTF-8");

      // embed an online image
      final URL url = this.getClass().getResource(TestMimeReadWrite.TEST_IMAGE_PATH);
      Assertions.assertNotNull(url, "Test image can be found");

      final String cid = mail.embed(url, "Test icon");

      final int testAttSize = 50000;
      final byte[] testAttachmentData = this.produceTestData(testAttSize);
      final ByteArrayDataSource testAttachmentDataSource = new ByteArrayDataSource(testAttachmentData, "text/plain");
      // add the attachment
      mail.attach(testAttachmentDataSource, "testascii.txt", "A text ascii file", EmailAttachment.ATTACHMENT);

      // set text content as plaintext and HTML
      final String mailPlainTxt = "This is plain text";
      final String mailHtmlTxt = "<html><body>This is <b>formatted</b> text and an image:<br><img src=\"cid:" + cid
          + "\"></body></html>";
      mail.setTextMsg(mailPlainTxt);
      mail.setHtmlMsg(mailHtmlTxt);

      mail.buildMimeMessage();
      final MimeMessage mimeMsg = mail.getMimeMessage();

      final Document doc = dbMail.createDocument();
      doc.replaceItemValue("Form", "Memo");

      final MimeWriter mimeWriter = client.getMimeWriter();
      try {
        mimeWriter.writeMime(doc, null, mimeMsg, EnumSet.of(WriteMimeDataType.HEADERS, WriteMimeDataType.BODY));
      } catch (final MessagingException e) {
        e.printStackTrace();
      }

      // doc.save();

      {
        final Item subjectItem = doc.getFirstItem("Subject").orElse(null);
        Assertions.assertNotNull(subjectItem, "Subject should exist");
        Assertions.assertEquals(ItemDataType.TYPE_RFC822_TEXT, subjectItem.getType(), "Subject should be RFC822");
        subjectItem.convertRFC822TextItem();
        Assertions.assertEquals(ItemDataType.TYPE_TEXT, subjectItem.getType(), "Subject is text after conversion");
        final String subjectRead = subjectItem.get(String.class, "");
        Assertions.assertEquals(subjectWrite, subjectRead, "Subject ok");
      }

      {
        final Item postedDateItem = doc.getFirstItem("PostedDate").orElse(null);
        Assertions.assertNotNull(postedDateItem, "PostedDate should exist");
        Assertions.assertEquals(ItemDataType.TYPE_RFC822_TEXT, postedDateItem.getType(), "PostedDate should be RFC822");
        postedDateItem.convertRFC822TextItem();
        Assertions.assertEquals(ItemDataType.TYPE_TIME, postedDateItem.getType(), "PostedDate is time after conversion");
        final OffsetDateTime postedDate = postedDateItem.get(OffsetDateTime.class, null);
        Assertions.assertNotNull(postedDate, "PostedDate not null");
      }

      {
        final Item item = doc.getFirstItem("SendTo").orElse(null);
        Assertions.assertNotNull(item, "SendTo should exist");
        Assertions.assertEquals(ItemDataType.TYPE_RFC822_TEXT, item.getType(), "SendTo should be RFC822");
        item.convertRFC822TextItem();
        Assertions.assertEquals(ItemDataType.TYPE_TEXT, item.getType(), "SendTo is text after conversion");
        final String read = item.get(String.class, "");
        Assertions.assertEquals("\"Mr. Receiver\" <mr.receiver@acme.com>", read, "SendTo ok");
      }

      {
        final Item item = doc.getFirstItem("From").orElse(null);
        Assertions.assertNotNull(item, "From should exist");
        Assertions.assertEquals(ItemDataType.TYPE_RFC822_TEXT, item.getType(), "From should be RFC822");
        item.convertRFC822TextItem();
        Assertions.assertEquals(ItemDataType.TYPE_TEXT, item.getType(), "From is text after conversion");
        final String read = item.get(String.class, "");
        Assertions.assertEquals("\"Mr. Sender\" <mr.sender@acme.com>", read, "From ok");
      }

      {
        final Item bodyItem = doc.getFirstItem("Body").orElse(null);
        Assertions.assertNotNull(bodyItem, "Body should exist");
        Assertions.assertEquals(ItemDataType.TYPE_MIME_PART, bodyItem.getType(), "Body should be MIME");
      }

      // now read the created mime content via OutputStream and InputStream interface and compare both results
      final MimeReader mimeReader = client.getMimeReader();

      final Set<ReadMimeDataType> readDataType = EnumSet.of(ReadMimeDataType.MIMEHEADERS, ReadMimeDataType.RFC822HEADERS);

      final ByteArrayOutputStream rawMimeOut = new ByteArrayOutputStream();
      mimeReader.readMIME(doc, "body", readDataType, rawMimeOut);

      Assertions.assertTrue(rawMimeOut.size() > 0);

      final ByteArrayOutputStream rawMimeOutViaInputStream = new ByteArrayOutputStream();
      final byte[] buffer = new byte[2000];
      int len;

      try (InputStream in = mimeReader.readMIMEAsStream(doc, "body", readDataType);) {
        while ((len = in.read(buffer)) > 0) {
          rawMimeOutViaInputStream.write(buffer, 0, len);
        }
      }

      Assertions.assertTrue(rawMimeOutViaInputStream.size() > 0);

      byte[] rawMimeOutArr = rawMimeOut.toByteArray();
      byte[] rawMimeOutViaInputStreamArr = rawMimeOutViaInputStream.toByteArray();
      
      Assertions.assertArrayEquals(rawMimeOutArr, rawMimeOutViaInputStreamArr);
    });
  }

  @Test
  public void testMimeReadOnRichtextItem() throws Exception {
    this.withTempDb(db -> {
      final Document doc = db.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body");) {
        rtWriter.addText("Hello.");
      }
      Assertions.assertThrows(MimePartNotFoundException.class, () -> doc.get("body", MimeData.class, null));
    });
  }

  @Test
  public void testMultithreadRead() throws Exception {
    final ExecutorService exec = Executors.newCachedThreadPool(this.getClient().getThreadFactory());
    IntStream.range(0, 20)
        .mapToObj(iteration -> (Callable<String>) () -> {
          Thread.currentThread().setName("testMultithreadRead thread " + iteration);
          try (DominoClient client = DominoClientBuilder.newDominoClient().build()) {
            final Database names = client.openDatabase("names.nsf");
            names.queryDocuments().forEachDocument(0, Integer.MAX_VALUE, (doc, loop) -> {
            	//skip any encrypted document, unable to render those without decryption
            	if (!doc.isEncrypted()) {
                    final MimeWriter w = client.getMimeWriter();
                    w.convertToMime(doc,
                        w.createRichTextMimeConversionSettings()
                            .setMessageContentEncoding(MessageContentEncoding.TEXT_HTML_WITH_IMAGES_ATTACHMENTS));

                    final HtmlConversionResult html = client.getRichTextHtmlConverter()
                        .render(doc)
                        .option(HtmlConvertOption.DisablePassThruHTML, "1")
                        .convert();
                    html.getHtml();
            	}
            });
            return "finished iteration " + iteration;
          }
        })
        .map(exec::submit)
        .parallel()
        .forEach(t -> {
          try {
            t.get();
          } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
          }
        });

    exec.shutdownNow();
    exec.awaitTermination(2, TimeUnit.MINUTES);
  }
}
