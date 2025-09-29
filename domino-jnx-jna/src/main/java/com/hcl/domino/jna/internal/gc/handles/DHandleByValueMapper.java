package com.hcl.domino.jna.internal.gc.handles;

import com.sun.jna.FromNativeContext;
import com.sun.jna.FromNativeConverter;
import com.sun.jna.ToNativeContext;
import com.sun.jna.ToNativeConverter;

public enum DHandleByValueMapper implements FromNativeConverter, ToNativeConverter {
  INSTANCE;
  
  @Override
  public Object toNative(Object value, ToNativeContext context) {
    if(value == null) {
      return 0;
    }
    
    DHANDLE.ByValue val = (DHANDLE.ByValue)value;
    return val.getValue();
  }

  @Override
  public Object fromNative(Object nativeValue, FromNativeContext context) {
    if(nativeValue == null) {
      return DHANDLE.newInstanceByValue(0);
    }
    
    Long val = (Long)nativeValue;;
    return DHANDLE.newInstanceByValue(val);
  }

  @Override
  public Class<?> nativeType() {
    return long.class;
  }
  
}