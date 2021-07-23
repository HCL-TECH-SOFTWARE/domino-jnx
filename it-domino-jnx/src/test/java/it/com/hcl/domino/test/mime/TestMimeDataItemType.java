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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.util.List;
import java.util.Optional;

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
	private static final String TEST_IMAGE_PATH = "/images/test-png-large.png";;

	/**
	 * We check how the API behaves when reading {@link MimeData} from
	 * non MIME_PART items
	 * 
	 * @throws Exception in case of errors
	 */
	@Test
	public void testReadInvalidMimeData() throws Exception {
		withTempDb((db) -> {
			DominoClient client = getClient();

			Document doc = db.createDocument();

			MimeData mimeData = doc.get("ItemDoesNotExist", MimeData.class, null);
			assertNull(mimeData);

			doc.replaceItemValue("TextItem", "Hello world.");

			{
				//using the MIME stream C API on a non MIME_PART item should throw an error
				DominoException ex = null;
				try {
					doc.get("TextItem", MimeData.class, null);
				}
				catch (DominoException e) {
					ex = e;
				}
				assertNotNull(ex);
				assertTrue(ex.getId() == 1184); // "MIME part not found"
			}

			//now we create formatted richtext content
			try (RichTextWriter rtWriter = doc.createRichTextItem("Body");) {
				
				rtWriter.addText("Hello World.",
						rtWriter.createTextStyle("MyStyle").setAlign(Justify.RIGHT),
						rtWriter.createFontStyle().setBold(true));
			}

			Item itm = doc.getFirstItem("Body").get();
			//make sure it's richtext
			assertEquals(itm.getType(), ItemDataType.TYPE_COMPOSITE);

			{
				//should also throw an error, still no MIME_PART item
				DominoException ex = null;
				try {
					doc.get("Body", MimeData.class, null);
				}
				catch (DominoException e) {
					ex = e;
				}
				assertNotNull(ex);
				assertTrue(ex.getId() == 1184); // "MIME part not found"
			}

			//now we convert the richtext item to mime
			MimeWriter mimeWriter = client.getMimeWriter();
			RichTextMimeConversionSettings convSettings = 
					mimeWriter.createRichTextMimeConversionSettings().setDefaults()
					.setMessageContentEncoding(MessageContentEncoding.TEXT_PLAIN_AND_HTML_WITH_IMAGES_ATTACHMENTS);

			mimeWriter.convertToMime(doc, convSettings);

			//make sure it's really MIME_PART
			itm = doc.getFirstItem("Body").get();
			assertEquals(itm.getType(), ItemDataType.TYPE_MIME_PART);

			MimeData mimeDataFromRichText = doc.get("Body", MimeData.class, null);
			assertNotNull(mimeDataFromRichText);

			// <html><body><div align="right"><font size="1" face="serif"><b>Hello World.</b></font></div></body></html>
			String html = mimeDataFromRichText.getHtml();
			//                                                                Hello World.
			String text = mimeDataFromRichText.getPlainText();

			assertTrue(html.length()>0);
			assertTrue(text.length()>0);
		});
	}

	@Test
	public void testWriteReadMimeData() throws Exception {
		withTempDb((db) -> {
			//compose the MIME data to write:
			MimeData writtenMimeData = new MimeData();

			//embed an image via URL and remember its content id (cid)
			URL url = getClass().getResource(TEST_IMAGE_PATH);
			assertNotNull(url, "Test image can be found: "+TEST_IMAGE_PATH);
			String cid = writtenMimeData.embed(new UrlMimeAttachment(url));

			//add the first test file
			int writtenAttachment1Size = 50000;
			byte[] writtenAttachment1Data = produceTestData(writtenAttachment1Size);
			writtenMimeData.attach(new ByteArrayMimeAttachment(writtenAttachment1Data, "test.txt"));

			//and the second one
			int writtenAttachment2Size = 20000;
			byte[] writtenAttachment2Data = produceTestData(writtenAttachment2Size);
			writtenMimeData.attach(new ByteArrayMimeAttachment(writtenAttachment2Data, "test2.txt"));

			//set html (with link to embedded image) and alternative plaintext
			String html = "<html><body>This is <b>formatted</b> text and an image:<br><img src=\"cid:"+cid+"\"></body></html>";
			writtenMimeData.setHtml(html);
			String plainText = "This is alternative plaintext";
			writtenMimeData.setPlainText(plainText);

			//and write it to a temp document
			Document doc = db.createDocument();
			doc.replaceItemValue("BodyMime", writtenMimeData);

			//now read the MIME data and compare it with what we have just written
			Optional<Item> itm = doc.getFirstItem("BodyMime");
			assertTrue(itm.isPresent());
			assertEquals(ItemDataType.TYPE_MIME_PART, itm.get().getType());
			
			MimeData checkMimeData = doc.get("BodyMime", MimeData.class, null);
			assertNotNull(checkMimeData, "MIMEItemData not null");

			String checkHtml = checkMimeData.getHtml();
			assertEquals(html, checkHtml);
			String checkPlainText = checkMimeData.getPlainText();
			assertEquals(plainText, checkPlainText);

			//check that no embed has been removed
			for (String currWrittenCid : writtenMimeData.getContentIds()) {
				IMimeAttachment checkEmbed = checkMimeData.getEmbed(currWrittenCid).orElse(null);

				assertNotNull(checkEmbed, "Embed with cid "+currWrittenCid+" not null");
			}

			//check that no embed has been added
			for (String currCheckCid : checkMimeData.getContentIds()) {
				IMimeAttachment currEmbed = writtenMimeData.getEmbed(currCheckCid).orElse(null);

				assertNotNull(currEmbed, "Embed with cid "+currCheckCid+" not null");
			}

			List<IMimeAttachment> writtenAttachments = writtenMimeData.getAttachments();
			List<IMimeAttachment> checkAttachments = checkMimeData.getAttachments();

			//check that no attachment has been removed
			for (IMimeAttachment currAtt : writtenAttachments) {
				String currFilename = currAtt.getFileName();

				IMimeAttachment checkAtt = null;
				for (IMimeAttachment currCheckAtt : checkAttachments) {
					if (currFilename.equals(currCheckAtt.getFileName())) {
						checkAtt = currCheckAtt;
						break;
					}
				}

				assertNotNull(checkAtt, "file "+currFilename+" could be found");
			}

			//check that no attachment has been added
			for (IMimeAttachment currCheckAtt : checkAttachments) {
				String currFilename = currCheckAtt.getFileName();

				IMimeAttachment writtenAtt = null;
				for (IMimeAttachment currWrittenAtt : writtenAttachments) {
					if (currFilename.equals(currWrittenAtt.getFileName())) {
						writtenAtt = currCheckAtt;
						break;
					}
				}

				assertNotNull(writtenAtt, "file "+currFilename+" could be found");
			}
		});

	}

	@Test
	public void testWriteReadMimeDataBasic() throws Exception {
		withTempDb((db) -> {
			//compose the MIME data to write:
			MimeData writtenMimeData = new MimeData();
			
			//set html (with link to embedded image) and alternative plaintext
			String html = "<html><body>This is <b>formatted</b> text and no image.</body></html>";
			writtenMimeData.setHtml(html);
			String plainText = "This is alternative plaintext";
			writtenMimeData.setPlainText(plainText);

			//and write it to a temp document
			Document doc = db.createDocument();
			doc.replaceItemValue("BodyMime", writtenMimeData);

			//now read the MIME data and compare it with what we have just written
			Optional<Item> itm = doc.getFirstItem("BodyMime");
			assertTrue(itm.isPresent());
			assertEquals(ItemDataType.TYPE_MIME_PART, itm.get().getType());
			
			MimeData checkMimeData = doc.get("BodyMime", MimeData.class, null);
			assertNotNull(checkMimeData, "MIMEItemData not null");

			String checkHtml = checkMimeData.getHtml();
			assertEquals(html, checkHtml);
			String checkPlainText = checkMimeData.getPlainText();
			assertEquals(plainText, checkPlainText);

			//check that no embed has been removed
			for (String currWrittenCid : writtenMimeData.getContentIds()) {
				IMimeAttachment checkEmbed = checkMimeData.getEmbed(currWrittenCid).orElse(null);

				assertNotNull(checkEmbed, "Embed with cid "+currWrittenCid+" not null");
			}

			//check that no embed has been added
			for (String currCheckCid : checkMimeData.getContentIds()) {
				IMimeAttachment currEmbed = writtenMimeData.getEmbed(currCheckCid).orElse(null);

				assertNotNull(currEmbed, "Embed with cid "+currCheckCid+" not null");
			}

			List<IMimeAttachment> writtenAttachments = writtenMimeData.getAttachments();
			List<IMimeAttachment> checkAttachments = checkMimeData.getAttachments();

			//check that no attachment has been removed
			for (IMimeAttachment currAtt : writtenAttachments) {
				String currFilename = currAtt.getFileName();

				IMimeAttachment checkAtt = null;
				for (IMimeAttachment currCheckAtt : checkAttachments) {
					if (currFilename.equals(currCheckAtt.getFileName())) {
						checkAtt = currCheckAtt;
						break;
					}
				}

				assertNotNull(checkAtt, "file "+currFilename+" could be found");
			}

			//check that no attachment has been added
			for (IMimeAttachment currCheckAtt : checkAttachments) {
				String currFilename = currCheckAtt.getFileName();

				IMimeAttachment writtenAtt = null;
				for (IMimeAttachment currWrittenAtt : writtenAttachments) {
					if (currFilename.equals(currWrittenAtt.getFileName())) {
						writtenAtt = currCheckAtt;
						break;
					}
				}

				assertNotNull(writtenAtt, "file "+currFilename+" could be found");
			}
		});

	}

}
