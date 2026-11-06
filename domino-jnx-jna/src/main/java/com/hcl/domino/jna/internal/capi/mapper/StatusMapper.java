package com.hcl.domino.jna.internal.capi.mapper;

import com.hcl.domino.commons.data.types.StatusImpl;
import com.hcl.domino.data.types.STATUS;
import com.sun.jna.FromNativeContext;
import com.sun.jna.FromNativeConverter;
import com.sun.jna.ToNativeContext;
import com.sun.jna.ToNativeConverter;

public enum StatusMapper implements FromNativeConverter, ToNativeConverter {
  INSTANCE;
  
  @Override
  public Short toNative(Object value, ToNativeContext context) {
    if(value == null) {
      return 0;
    }
    
    STATUS val = (STATUS)value;
    return val.getValue();
  }

  @Override
  public STATUS fromNative(Object nativeValue, FromNativeContext context) {
    if(nativeValue == null) {
      return StatusImpl.NOERROR;
    }
    
    Short val = (Short)nativeValue;
    return StatusImpl.of(val);
  }

  @Override
  public Class<?> nativeType() {
    return short.class;
  }
  
}