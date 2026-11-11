package com.hcl.domino.jna.internal.capi;

import java.util.IdentityHashMap;
import java.util.Map;
import com.hcl.domino.commons.data.types.StatusImpl;
import com.hcl.domino.commons.data.types.WordByReference;
import com.hcl.domino.commons.data.types.WordImpl;
import com.hcl.domino.data.types.STATUS;
import com.hcl.domino.data.types.WORD;
import com.hcl.domino.jna.internal.capi.mapper.DHandleByReferenceMapper;
import com.hcl.domino.jna.internal.capi.mapper.DHandleByValueMapper;
import com.hcl.domino.jna.internal.capi.mapper.HandleByReferenceMapper;
import com.hcl.domino.jna.internal.capi.mapper.HandleByValueMapper;
import com.hcl.domino.jna.internal.capi.mapper.StatusMapper;
import com.hcl.domino.jna.internal.capi.mapper.WordByReferenceMapper;
import com.hcl.domino.jna.internal.capi.mapper.WordMapper;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE64;
import com.hcl.domino.jna.internal.gc.handles.HANDLE;
import com.hcl.domino.jna.internal.gc.handles.HANDLE64;
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
  
  private final Map<Class<?>, Object> cache = new IdentityHashMap<>();
  
  private JnxJnaTypeMapper() {
    cache.put(DHANDLE.ByReference.class, DHandleByReferenceMapper.INSTANCE);
    cache.put(DHANDLE64.ByReference.class, DHandleByReferenceMapper.INSTANCE);
    cache.put(DHANDLE.ByValue.class, DHandleByValueMapper.INSTANCE);
    cache.put(DHANDLE64.ByValue.class, DHandleByValueMapper.INSTANCE);
    cache.put(HANDLE.ByReference.class, HandleByReferenceMapper.INSTANCE);
    cache.put(HANDLE64.ByReference.class, HandleByReferenceMapper.INSTANCE);
    cache.put(HANDLE.ByValue.class, HandleByValueMapper.INSTANCE);
    cache.put(HANDLE64.ByValue.class, HandleByValueMapper.INSTANCE);
    cache.put(STATUS.class, StatusMapper.INSTANCE);
    cache.put(StatusImpl.class, StatusMapper.INSTANCE);
    cache.put(WORD.class, WordMapper.INSTANCE);
    cache.put(WordImpl.class, WordMapper.INSTANCE);
    cache.put(WordByReference.class, WordByReferenceMapper.INSTANCE);
  }

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
    Object known = cache.get(javaType);
    if(known != null) {
      return (T)known;
    } else if(MemoryStructure.class.isAssignableFrom(javaType)) {
      return (T)MemoryStructureMapper.INSTANCE;
    }
    return null;
  }
}
