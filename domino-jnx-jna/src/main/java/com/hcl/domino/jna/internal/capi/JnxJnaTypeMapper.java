package com.hcl.domino.jna.internal.capi;

import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.sun.jna.FromNativeContext;
import com.sun.jna.FromNativeConverter;
import com.sun.jna.Pointer;
import com.sun.jna.ToNativeContext;
import com.sun.jna.ToNativeConverter;
import com.sun.jna.TypeMapper;

/**
 * @since 1.48.0
 */
public enum JnxJnaTypeMapper implements TypeMapper {
  INSTANCE;
  
  private enum DHandleByReferenceMapper implements FromNativeConverter, ToNativeConverter {
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
  
  private enum DHandleByValueMapper implements FromNativeConverter, ToNativeConverter {
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
  
  @SuppressWarnings("unchecked")
  private <T extends FromNativeConverter & ToNativeConverter> T getMapper(Class<?> javaType) {
    if(DHANDLE.ByReference.class.isAssignableFrom(javaType)) {
      return (T)DHandleByReferenceMapper.INSTANCE;
    } else if(DHANDLE.ByValue.class.isAssignableFrom(javaType)) {
      return (T)DHandleByValueMapper.INSTANCE;
    }
    return null;
  }

  @Override
  public FromNativeConverter getFromNativeConverter(Class<?> javaType) {
    return getMapper(javaType);
  }

  @Override
  public ToNativeConverter getToNativeConverter(Class<?> javaType) {
    return getMapper(javaType);
  }


}
