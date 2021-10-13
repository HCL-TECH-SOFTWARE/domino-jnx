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
package com.hcl.domino.commons.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

/**
 * This {@link OutputStream} implementation uses an underlying
 * {@link ByteArrayOutputStream} to house the data, and then provides
 * a mechanism for a callback when the stream is closed and a way
 * to get the current byte array data.
 * 
 * @author Jesse Gallagher
 * @since 1.0.39
 */
public class BufferingCallbackOutputStream extends OutputStream {
  private final Consumer<byte[]> closeCallback;
  private final ByteArrayOutputStream delegate;

  public BufferingCallbackOutputStream(Consumer<byte[]> closeCallback) {
    this.closeCallback = closeCallback;
    this.delegate = new ByteArrayOutputStream();
  }

  @Override
  public void write(int b) throws IOException {
    delegate.write(b);
  }
  
  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    delegate.write(b, off, len);
  }
  
  @Override
  public String toString() {
    return delegate.toString();
  }
  
  public byte[] toByteArray() {
    return delegate.toByteArray();
  }
  
  @Override
  public void close() throws IOException {
    delegate.close();
    if(closeCallback != null) {
      closeCallback.accept(toByteArray());
    }
  }  
}
