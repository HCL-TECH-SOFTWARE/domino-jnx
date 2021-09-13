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
