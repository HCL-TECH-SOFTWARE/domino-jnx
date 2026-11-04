package com.hcl.domino.jna.internal.capi;

import com.hcl.domino.data.types.STATUS;
import com.hcl.domino.jna.internal.capi.mapper.DHandleByReferenceMapper;
import com.hcl.domino.jna.internal.capi.mapper.DHandleByValueMapper;
import com.hcl.domino.jna.internal.capi.mapper.HandleByReferenceMapper;
import com.hcl.domino.jna.internal.capi.mapper.HandleByValueMapper;
import com.hcl.domino.jna.internal.capi.mapper.StatusMapper;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.HANDLE;
import com.hcl.domino.jna.internal.structs.MemoryStructureMapper;
import com.hcl.domino.richtext.structures.MemoryStructure;
import com.sun.jna.FromNativeConverter;
import com.sun.jna.ToNativeConverter;
import com.sun.jna.TypeMapper;

/**
 * @since 1.48.0
 */
public enum JnxJnaTypeMapper implements TypeMapper {
  INSTANCE;

  @Override
  public FromNativeConverter getFromNativeConverter(Class<?> javaType) {
    return getMapper(javaType);
  }

  @Override
  public ToNativeConverter getToNativeConverter(Class<?> javaType) {
    return getMapper(javaType);
  }

  @SuppressWarnings("unchecked")
  private <T extends FromNativeConverter & ToNativeConverter> T getMapper(Class<?> javaType) {
    if(DHANDLE.ByReference.class.isAssignableFrom(javaType)) {
      return (T)DHandleByReferenceMapper.INSTANCE;
    } else if(DHANDLE.ByValue.class.isAssignableFrom(javaType)) {
      return (T)DHandleByValueMapper.INSTANCE;
    } else if(HANDLE.ByReference.class.isAssignableFrom(javaType)) {
      return (T)HandleByReferenceMapper.INSTANCE;
    } else if(HANDLE.ByValue.class.isAssignableFrom(javaType)) {
      return (T)HandleByValueMapper.INSTANCE;
    } else if(MemoryStructure.class.isAssignableFrom(javaType)) {
      return (T)MemoryStructureMapper.INSTANCE;
    } else if(STATUS.class.isAssignableFrom(javaType)) {
      return (T)StatusMapper.INSTANCE;
    }
    return null;
  }
}
