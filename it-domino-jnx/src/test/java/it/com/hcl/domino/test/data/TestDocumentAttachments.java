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
package it.com.hcl.domino.test.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.hcl.domino.data.Attachment;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Document.IAttachmentProducer;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

public class TestDocumentAttachments extends AbstractNotesRuntimeTest {

	@Test
	public void testDocAttachments() throws Exception {
		withTempDb((db) -> {
			int testDataSize = 20000000;
			byte[] testData = produceTestData(testDataSize);
			
			final String fileName = "testfile.bin"; //$NON-NLS-1$
			
			Document doc = db.createDocument();
			
			TemporalAccessor dtCreated = Instant.now().minusSeconds(1234).with(ChronoField.MILLI_OF_SECOND, 120); // only store 1/100 seconds, more is not supported by Domino
			TemporalAccessor dtModified = Instant.now().minusSeconds(543).with(ChronoField.MILLI_OF_SECOND, 560);
			
			try (InputStream in = new ByteArrayInputStream(testData)) {
				doc.attachFile(fileName, dtCreated, dtModified, new IAttachmentProducer() {
					
					@Override
					public void produceAttachment(OutputStream out) throws IOException {
						//write data in small chunks to test db object generation
						byte[] buf = new byte[16384];
						int len;
						
						while ((len = in.read(buf))>0) {
							out.write(buf, 0, len);
						}
					}
					
					@Override
					public long getSizeEstimation() {
						//report a smaller size so that the DB object needs to grow and we can test if it keeps its data
						return 100000;
					}
				});
			}

			int i=0;
			do {
				assertTrue(doc.hasItem("$file"));
				List<?> fileItemValue = doc.getItemValue("$file");
				assertNotNull(fileItemValue);
				assertEquals(1, fileItemValue.size());
				assertInstanceOf(Attachment.class, fileItemValue.get(0));
				
				Attachment attachmentViaFileItem = (Attachment) fileItemValue.get(0);
				assertEquals(fileName, attachmentViaFileItem.getFileName());
				assertEquals(testData.length, attachmentViaFileItem.getFileSize());
				assertEquals(Instant.from(dtCreated), Instant.from(attachmentViaFileItem.getFileCreated()));
				assertEquals(Instant.from(dtModified), Instant.from(attachmentViaFileItem.getFileModified()));
				
				Attachment attachmentFromDoc = doc.getAttachment(fileName).orElse(null);
				assertEquals(fileName, attachmentFromDoc.getFileName());
				assertEquals(testData.length, attachmentFromDoc.getFileSize());
				assertEquals(Instant.from(dtCreated), Instant.from(attachmentFromDoc.getFileCreated()));
				assertEquals(Instant.from(dtModified), Instant.from(attachmentFromDoc.getFileModified()));
				
				//use Attachment.getInputStream() for simplicity of the testcase; has not the best performance,
				//because it extracts the whole attachment to disk first
				try (InputStream inOrig = new ByteArrayInputStream(testData);
						InputStream inFromDoc = attachmentViaFileItem.getInputStream()) {
					
					byte[] bufOrig = new byte[1000000];
					byte[] bufFromDoc = new byte[1000000];
					int len1;
					int len2;
					int totalLenOrig = 0;
					int totalLenFromDoc = 0;
					
					while (
							((len1 = inOrig.read(bufOrig)) > 0)
							&&
							((len2 = inFromDoc.read(bufFromDoc)) > 0)
							) {
						totalLenOrig += len1;
						totalLenFromDoc += len2;
						assertEquals(len1, len2);
						
						for (int p=0; p<len1; p++) {
							assertEquals(bufOrig[p], bufFromDoc[p], "File equality at pos "+p);
						}
					}
					
					assertEquals(testData.length, totalLenOrig);
					assertEquals(testData.length, totalLenFromDoc);
				}
				
				if (i==0) {
					//close and reopen the doc
					doc.save();
					int noteId = doc.getNoteID();
					doc.autoClosable().close();
					
					doc = db.getDocumentById(noteId).orElse(null);
					assertNotNull(doc);
				}
				else if (i==1) {
					//run this test twice; one with in-memory doc, on with doc loaded from db
					break;
				}
				
				i++;
			}
			while (true);
			
		});
	}
}
