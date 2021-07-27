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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.data.Attachment;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Document.IAttachmentProducer;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

public class TestDocumentAttachments extends AbstractNotesRuntimeTest {

  @Test
  public void testDocAttachments() throws Exception {
    this.withTempDb(db -> {
      final int testDataSize = 20000000;
      final byte[] testData = this.produceTestData(testDataSize);

      final String fileName = "testfile.bin"; //$NON-NLS-1$

      Document doc = db.createDocument();

      final TemporalAccessor dtCreated = Instant.now().minusSeconds(1234).with(ChronoField.MILLI_OF_SECOND, 120); // only store
                                                                                                                  // 1/100 seconds,
                                                                                                                  // more is not
                                                                                                                  // supported by
                                                                                                                  // Domino
      final TemporalAccessor dtModified = Instant.now().minusSeconds(543).with(ChronoField.MILLI_OF_SECOND, 560);

      try (InputStream in = new ByteArrayInputStream(testData)) {
        doc.attachFile(fileName, dtCreated, dtModified, new IAttachmentProducer() {

          @Override
          public long getSizeEstimation() {
            // report a smaller size so that the DB object needs to grow and we can test if
            // it keeps its data
            return 100000;
          }

          @Override
          public void produceAttachment(final OutputStream out) throws IOException {
            // write data in small chunks to test db object generation
            final byte[] buf = new byte[16384];
            int len;

            while ((len = in.read(buf)) > 0) {
              out.write(buf, 0, len);
            }
          }
        });
      }

      int i = 0;
      do {
        Assertions.assertTrue(doc.hasItem("$file"));
        final List<?> fileItemValue = doc.getItemValue("$file");
        Assertions.assertNotNull(fileItemValue);
        Assertions.assertEquals(1, fileItemValue.size());
        Assertions.assertInstanceOf(Attachment.class, fileItemValue.get(0));

        final Attachment attachmentViaFileItem = (Attachment) fileItemValue.get(0);
        Assertions.assertEquals(fileName, attachmentViaFileItem.getFileName());
        Assertions.assertEquals(testData.length, attachmentViaFileItem.getFileSize());
        Assertions.assertEquals(Instant.from(dtCreated), Instant.from(attachmentViaFileItem.getFileCreated()));
        Assertions.assertEquals(Instant.from(dtModified), Instant.from(attachmentViaFileItem.getFileModified()));

        final Attachment attachmentFromDoc = doc.getAttachment(fileName).orElse(null);
        Assertions.assertEquals(fileName, attachmentFromDoc.getFileName());
        Assertions.assertEquals(testData.length, attachmentFromDoc.getFileSize());
        Assertions.assertEquals(Instant.from(dtCreated), Instant.from(attachmentFromDoc.getFileCreated()));
        Assertions.assertEquals(Instant.from(dtModified), Instant.from(attachmentFromDoc.getFileModified()));

        // use Attachment.getInputStream() for simplicity of the testcase; has not the
        // best performance,
        // because it extracts the whole attachment to disk first
        try (InputStream inOrig = new ByteArrayInputStream(testData);
            InputStream inFromDoc = attachmentViaFileItem.getInputStream()) {

          final byte[] bufOrig = new byte[1000000];
          final byte[] bufFromDoc = new byte[1000000];
          int len1;
          int len2;
          int totalLenOrig = 0;
          int totalLenFromDoc = 0;

          while ((len1 = inOrig.read(bufOrig)) > 0
              &&
              (len2 = inFromDoc.read(bufFromDoc)) > 0) {
            totalLenOrig += len1;
            totalLenFromDoc += len2;
            Assertions.assertEquals(len1, len2);

            for (int p = 0; p < len1; p++) {
              Assertions.assertEquals(bufOrig[p], bufFromDoc[p], "File equality at pos " + p);
            }
          }

          Assertions.assertEquals(testData.length, totalLenOrig);
          Assertions.assertEquals(testData.length, totalLenFromDoc);
        }

        if (i == 0) {
          // close and reopen the doc
          doc.save();
          final int noteId = doc.getNoteID();
          doc.autoClosable().close();

          doc = db.getDocumentById(noteId).orElse(null);
          Assertions.assertNotNull(doc);
        } else if (i == 1) {
          // run this test twice; one with in-memory doc, on with doc loaded from db
          break;
        }

        i++;
      } while (true);

    });
  }
}
