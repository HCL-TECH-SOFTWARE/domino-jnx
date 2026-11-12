package com.hcl.domino.commons.richtext.structures;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import com.hcl.domino.richtext.structures.OpaqueTimeDate;

/**
 * {@link OpaqueTimeDate} implementation that is useful for reading from
 * a pre-read buffer, but which will not write back to native memory.
 *  
 * @since 1.57.0
 */
public class DefaultOpaqueTimeDate implements OpaqueTimeDate {
  private int[] innards;
  
  public DefaultOpaqueTimeDate(int[] innards) {
    this.innards = innards;
  }
  
  @Override
  public ByteBuffer getData() {
    ByteBuffer buf = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder());
    buf.asIntBuffer().put(innards);
    return buf;
  }

  @Override
  public int[] getInnards() {
    return this.innards;
  }

  @Override
  public OpaqueTimeDate setInnards(int[] innards) {
    this.innards = innards;
    return this;
  }

}
