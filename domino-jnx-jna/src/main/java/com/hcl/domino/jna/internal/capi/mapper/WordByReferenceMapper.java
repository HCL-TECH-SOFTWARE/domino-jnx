package com.hcl.domino.jna.internal.capi.mapper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import com.hcl.domino.commons.data.types.WordByReference;
import com.sun.jna.FromNativeContext;
import com.sun.jna.FromNativeConverter;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ToNativeContext;
import com.sun.jna.ToNativeConverter;

public enum WordByReferenceMapper implements FromNativeConverter, ToNativeConverter {
  INSTANCE;
  
  @Override
  public Pointer toNative(Object value, ToNativeContext context) {
    if(value == null) {
      return null;
    }
    
    WordByReference val = (WordByReference)value;
    return Native.getDirectBufferPointer(val.getBuffer());
  }

  @Override
  public WordByReference fromNative(Object nativeValue, FromNativeContext context) {
    if(nativeValue == null) {
      return null;
    }
    
    Pointer val = (Pointer)nativeValue;
    ByteBuffer buf = val.getByteBuffer(0, 2).order(ByteOrder.nativeOrder());
    return new WordByReference(buf);
  }

  @Override
  public Class<?> nativeType() {
    return Pointer.class;
  }
  
}