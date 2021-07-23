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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import com.hcl.domino.richtext.records.CDParagraph;
import com.hcl.domino.richtext.records.CDText;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * Extracts the contents of {@link CDText} CD item and writes the content
 * to the provided {@link Appendable}.
 *
 * @author Karsten Lehmann
 * @since 1.0.20
 */
public class ExtractTextProcessor implements IRichTextProcessor<Void> {

  private final Appendable out;
  private final boolean isWindows;

  /**
   * Constructs a text extraction processor to output to the provided appendable.
   *
   * @param os the non-null {@link Appendable} to target
   */
  public ExtractTextProcessor(final Appendable os) {
    this.out = Objects.requireNonNull(os, "Appendable must not be null");

    final String osName = AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty("os.name")); //$NON-NLS-1$
    if (osName != null && osName.toLowerCase().startsWith("windows")) { //$NON-NLS-1$
      this.isWindows = true;
    } else {
      this.isWindows = false;
    }
  }

  @Override
  public Void apply(final List<RichTextRecord<?>> t) {
    // each line starts with CDPARAGRAPH, skip the one before the first line
    final AtomicBoolean skippedFirstParagraph = new AtomicBoolean();

    t.stream()
        .filter(record -> (record instanceof CDText || record instanceof CDParagraph))
        .forEach(record -> {
          try {
            if (record instanceof CDText) {
              this.out.append(((CDText) record).getText());
            } else if (record instanceof CDParagraph) {
              if (skippedFirstParagraph.get()) {
                if (this.isWindows && this.isUseOSLineBreaks()) {
                  this.out.append(System.lineSeparator());
                } else {
                  this.out.append("\n"); //$NON-NLS-1$
                }
                this.out.append("\n"); //$NON-NLS-1$
              } else {
                skippedFirstParagraph.set(true);
              }
            }
          } catch (final IOException e) {
            throw new UncheckedIOException(e);
          }
        });

    return null;
  }

  /**
   * By default we use "\r\n" as line delimiter on Windows (same behavior as
   * the legacy Notes API). Override this method to use "\n" instead.
   *
   * @return true by default
   */
  protected boolean isUseOSLineBreaks() {
    return true;
  }

}