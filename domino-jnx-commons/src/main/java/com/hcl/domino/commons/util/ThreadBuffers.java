package com.hcl.domino.commons.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import com.hcl.domino.commons.data.types.WordByReference;
import com.hcl.domino.misc.NotesConstants;

/**
 * Contains thread-local buffers of various common sizes to allow
 * classes to re-use native memory for frequent operations.
 * 
 * @since 1.57.0
 */
public interface ThreadBuffers {

  ThreadLocal<ByteBuffer> BUFFER_TIMEDATE = ThreadLocal.withInitial(() -> ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder()));
  ThreadLocal<ByteBuffer> BUFFER_DHANDLE = ThreadLocal.withInitial(() -> ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder()));
  ThreadLocal<ByteBuffer> BUFFER_DWORD = ThreadLocal.withInitial(() -> ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()));
  ThreadLocal<ByteBuffer> BUFFER_WORD = ThreadLocal.withInitial(() -> ByteBuffer.allocateDirect(2).order(ByteOrder.nativeOrder()));
  
  ThreadLocal<ByteBuffer> BUFFER_USERNAME = ThreadLocal.withInitial(() -> ByteBuffer.allocateDirect(NotesConstants.MAXUSERNAME).order(ByteOrder.nativeOrder()));

  ThreadLocal<ByteBuffer> BUFFER_SIXTY_DB = ThreadLocal.withInitial(() -> ByteBuffer.allocateDirect(60 * 1024).order(ByteOrder.nativeOrder()));
  
  ThreadLocal<WordByReference> WORD_BY_REFERENCE = ThreadLocal.withInitial(WordByReference::new);
}
