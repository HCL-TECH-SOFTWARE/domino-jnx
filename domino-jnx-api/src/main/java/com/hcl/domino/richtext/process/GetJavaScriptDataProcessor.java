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
package com.hcl.domino.richtext.process;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.hcl.domino.richtext.records.CDBlobPart;
import com.hcl.domino.richtext.records.CDEvent;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * Extracts the contents of a JavaScript CD item (e.g. {@code $JavaScriptData})
 * as a
 * byte array.
 *
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public class GetJavaScriptDataProcessor implements IRichTextProcessor<byte[]> {
  public static final GetJavaScriptDataProcessor instance = new GetJavaScriptDataProcessor();

  @Override
  public byte[] apply(final List<RichTextRecord<?>> t) {
    // TODO don't pre-read all data
    // TODO probably validate the CD stream

    final AtomicLong len = new AtomicLong();
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      t.forEach(record -> {
        if (record instanceof CDEvent) {
          len.set(((CDEvent) record).getActionLength());
        } else if (record instanceof CDBlobPart) {
          try {
            final byte[] partData = ((CDBlobPart) record).getBlobPartData();
            final int currentLen = os.size();
            final long remaining = len.get() - currentLen;
            if (partData.length > remaining) {
              // We must be at the end - only write the declared part
              os.write(Arrays.copyOf(partData, (int) (len.get() - currentLen)));
            } else {
              os.write(partData);
            }
          } catch (final IOException e) {
            throw new UncheckedIOException(e);
          }
        }
      });
      return os.toByteArray();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

}
