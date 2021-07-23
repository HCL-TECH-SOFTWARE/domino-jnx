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
package com.hcl.domino.jna.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Attachment;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Document.IAttachmentProducer;
import com.hcl.domino.jna.data.JNADominoDateTime;

@SuppressWarnings("nls")
public class TestAttachments extends AbstractJNARuntimeTest {

  private byte[] produceTestData(final int size) {
    final byte[] data = new byte[size];

    int offset = 0;

    while (offset < size) {
      for (char c = 'A'; c <= 'Z' && offset < size; c++) {
        data[offset++] = (byte) (c & 0xff);
      }
    }

    return data;
  }

  @Test
  public void testAttachFile() throws IOException {
    final DominoClient client = this.getClient();

    final Database dbTest = client.openDatabase("", "log.nsf");
    final Document doc = dbTest.createDocument();

    final Instant fileCreated = Instant.now().minus(2, ChronoUnit.HOURS);
    final Instant fileModified = Instant.now().minus(1, ChronoUnit.HOURS);

    final int testDataSize = 1000000;

    final byte[] dataWrittenToFile = this.produceTestData(testDataSize);

    final Attachment att = doc.attachFile("file.txt", fileCreated, fileModified,
        new IAttachmentProducer() {

          @Override
          public long getSizeEstimation() {
            return testDataSize;
          }

          @Override
          public void produceAttachment(final OutputStream out) throws IOException {
            out.write(dataWrittenToFile);
          }

        });

    final AtomicInteger attCount = new AtomicInteger();
    final AtomicBoolean attFound = new AtomicBoolean();

    doc.forEachAttachment((currAtt, loop) -> {
      attCount.incrementAndGet();

      if (currAtt.getFileName().equals(att.getFileName())) {
        attFound.set(Boolean.TRUE);
      }
    });
    Assertions.assertTrue(attFound.get(), "Attachment found in forEach loop");
    Assertions.assertEquals(attCount.get(), 1, "Number of attachment in forEach loop ok");

    Assertions.assertNotNull(att, "Created attachment could be found");
    Assertions.assertEquals(att.getFileSize(), testDataSize, "Created attachment has the right size");
    Assertions.assertEquals(JNADominoDateTime.from(att.getFileCreated()), JNADominoDateTime.from(fileCreated),
        "Created attachment has the right creation date");
    Assertions.assertEquals(JNADominoDateTime.from(att.getFileModified()), JNADominoDateTime.from(fileModified),
        "Created attachment has the right modified date");

    final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
    final byte[] buffer = new byte[4096];
    int len;

    try (InputStream in = att.getInputStream();) {
      while ((len = in.read(buffer)) > 0) {
        bOut.write(buffer, 0, len);
      }
    }

    final byte[] dataFromFromFile = bOut.toByteArray();
    Assertions.assertTrue(Arrays.equals(dataWrittenToFile, dataFromFromFile), "Attachment content is correct");

    att.deleteFromDocument();

    final Attachment attAfterDeletion = doc.getAttachment(att.getFileName()).orElse(null);
    Assertions.assertNull(attAfterDeletion, "Attachment has been deleted");
  }
}
