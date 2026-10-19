package com.hcl.domino.jna.internal.structs;

import java.nio.ByteBuffer;
import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.richtext.structures.MemoryStructure;
import com.hcl.domino.richtext.structures.ResizableMemoryStructure;
import com.sun.jna.FromNativeContext;
import com.sun.jna.FromNativeConverter;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ToNativeContext;
import com.sun.jna.ToNativeConverter;

public enum MemoryStructureMapper implements FromNativeConverter, ToNativeConverter {
  INSTANCE;

  @Override
  public Object toNative(Object value, ToNativeContext context) {
    MemoryStructure struct = (MemoryStructure)value;
    ByteBuffer buf = struct.getData();
    int size = MemoryStructureUtil.sizeOf(value.getClass());
    Memory mem = new Memory(size);
    mem.write(0, buf.array(), 0, size);
    return mem;
  }

  @Override
  public Object fromNative(Object nativeValue, FromNativeContext context) {
    @SuppressWarnings("unchecked")
    Class<? extends MemoryStructure> type = (Class<? extends MemoryStructure>)context.getTargetType();
    if(ResizableMemoryStructure.class.isAssignableFrom(type)) {
      throw new UnsupportedOperationException("Unable to read non-fixed-size structures");
    }
    Pointer ptr = (Pointer)nativeValue;
    int size = MemoryStructureUtil.sizeOf(context.getTargetType());
    // Make a copy of the memory to avoid trouble with deallocated C-side structures
    byte[] bytes = ptr.getByteArray(0, size);
    ByteBuffer buf = ByteBuffer.wrap(bytes);
    return MemoryStructureUtil.forStructure(type, buf);
  }

  @Override
  public Class<?> nativeType() {
    return Pointer.class;
  }
}
