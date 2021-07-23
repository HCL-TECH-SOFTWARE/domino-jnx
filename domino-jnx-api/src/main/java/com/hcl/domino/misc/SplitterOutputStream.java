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
package com.hcl.domino.misc;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link OutputStream} implementation that delegates all calls to
 * two destination {@link OutputStream}s.
 * <p>
 * Note: this class makes no guarantees about behavior if one destination
 * encounters an exception.
 * </p>
 *
 * @author Jesse Gallagher
 */
public class SplitterOutputStream extends OutputStream {

  private final OutputStream delegate1;
  private final OutputStream delegate2;

  public SplitterOutputStream(final OutputStream delegate1, final OutputStream delegate2) {
    this.delegate1 = delegate1;
    this.delegate2 = delegate2;
  }

  @Override
  public void close() throws IOException {
    this.delegate1.close();
    this.delegate2.close();
  }

  @Override
  public void flush() throws IOException {
    this.delegate1.flush();
    this.delegate2.flush();
  }

  @Override
  public void write(final byte[] b) throws IOException {
    this.delegate1.write(b);
    this.delegate2.write(b);
  }

  @Override
  public void write(final byte[] b, final int off, final int len) throws IOException {
    this.delegate1.write(b, off, len);
    this.delegate2.write(b, off, len);
  }

  @Override
  public void write(final int b) throws IOException {
    this.delegate1.write(b);
  }
}
