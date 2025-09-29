package com.hcl.domino.jna.internal.gc.handles;

import com.sun.jna.FromNativeContext;
import com.sun.jna.FromNativeConverter;
import com.sun.jna.ToNativeContext;
import com.sun.jna.ToNativeConverter;

public enum HandleByValueMapper implements FromNativeConverter, ToNativeConverter {
  INSTANCE;
  
  @Override
  public Object toNative(Object value, ToNativeContext context) {
    if(value == null) {
      return 0;
    }
    
    HANDLE.ByValue val = (HANDLE.ByValue)value;
    return val.getValue();
  }

  @Override
  public Object fromNative(Object nativeValue, FromNativeContext context) {
    if(nativeValue == null) {
      return HANDLE.newInstanceByValue(0);
    }
    
    Long val = (Long)nativeValue;;
    return HANDLE.newInstanceByValue(val);
  }

  @Override
  public Class<?> nativeType() {
    return long.class;
  }
  
}