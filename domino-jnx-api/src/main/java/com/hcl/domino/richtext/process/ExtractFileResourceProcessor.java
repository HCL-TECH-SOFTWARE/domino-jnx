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
package com.hcl.domino.richtext.process;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;

import com.hcl.domino.richtext.records.CDFileSegment;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * Extracts the contents of a file-resource CD item (e.g. {@code $FileData}) and
 * writes
 * the content to the provided {@link OutputStream}.
 *
 * @author Jesse Gallagher
 * @since 1.0.15
 */
public class ExtractFileResourceProcessor implements IRichTextProcessor<Void> {

  private final OutputStream os;

  /**
   * Constructs an extraction processor to output to the provided stream.
   *
   * @param os the non-null {@link OutputStream} to target
   */
  public ExtractFileResourceProcessor(final OutputStream os) {
    this.os = Objects.requireNonNull(os, "OutputStream must not be null");
  }

  @Override
  public Void apply(final List<RichTextRecord<?>> t) {
    t.stream()
        .filter(CDFileSegment.class::isInstance)
        .map(CDFileSegment.class::cast)
        .forEach(record -> {
          try {
            this.os.write(record.getFileSegmentData());
          } catch (final IOException e) {
            throw new UncheckedIOException(e);
          }
        });
    return null;
  }

}