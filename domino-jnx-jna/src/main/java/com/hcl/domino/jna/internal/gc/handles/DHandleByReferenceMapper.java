package com.hcl.domino.jna.internal.gc.handles;

import com.sun.jna.FromNativeContext;
import com.sun.jna.FromNativeConverter;
import com.sun.jna.Pointer;
import com.sun.jna.ToNativeContext;
import com.sun.jna.ToNativeConverter;

public enum DHandleByReferenceMapper implements FromNativeConverter, ToNativeConverter {
  INSTANCE;
  
  @Override
  public Object toNative(Object value, ToNativeContext context) {
    if(value == null) {
      return null;
    }
    
    DHANDLE.ByReference ref = (DHANDLE.ByReference)value;
    return ref.getPointer();
  }

  @Override
  public Object fromNative(Object nativeValue, FromNativeContext context) {
    if(nativeValue == null) {
      return null;
    }
    
    Pointer p = (Pointer)nativeValue;
    return DHANDLE.newInstance(p);
  }

  @Override
  public Class<?> nativeType() {
    return Pointer.class;
  }
  
}