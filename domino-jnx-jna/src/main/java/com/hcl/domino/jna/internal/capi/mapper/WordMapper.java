package com.hcl.domino.jna.internal.capi.mapper;

import com.hcl.domino.commons.data.types.WordImpl;
import com.hcl.domino.data.types.WORD;
import com.sun.jna.FromNativeContext;
import com.sun.jna.FromNativeConverter;
import com.sun.jna.ToNativeContext;
import com.sun.jna.ToNativeConverter;

public enum WordMapper implements FromNativeConverter, ToNativeConverter {
  INSTANCE;
  
  @Override
  public Character toNative(Object value, ToNativeContext context) {
    if(value == null) {
      return 0;
    }
    
    WORD val = (WORD)value;
    return val.getValue();
  }

  @Override
  public WORD fromNative(Object nativeValue, FromNativeContext context) {
    if(nativeValue == null) {
      return WordImpl.ZERO;
    }
    
    Character val = (Character)nativeValue;
    if(val == 0) {
      return WordImpl.ZERO;
    } else {
      return new WordImpl(val);
    }
  }

  @Override
  public Class<?> nativeType() {
    return char.class;
  }
  
}