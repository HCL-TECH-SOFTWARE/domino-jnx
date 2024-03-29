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
package it.com.hcl.domino.test.data;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.Document.IAttachmentProducer;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDocSize extends AbstractNotesRuntimeTest {

  @Test
  public void testDocSize() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("item1", "itemvalue1");
      final long size1 = doc.size();
      Assertions.assertTrue(size1 > 0);

      doc.replaceItemValue("item2", "itemvalue2");
      final long size2 = doc.size();
      Assertions.assertTrue(size2 > size1);

      doc.attachFile("myfilename1",
          Instant.now(), Instant.now(), new IAttachmentProducer() {

            @Override
            public long getSizeEstimation() {
              return -1;
            }

            @Override
            public void produceAttachment(final OutputStream out) throws IOException {
              out.write("test123".getBytes());
            }

          });

      final long size3 = doc.size();
      Assertions.assertTrue(size3 > size2);
    });

  }

}
