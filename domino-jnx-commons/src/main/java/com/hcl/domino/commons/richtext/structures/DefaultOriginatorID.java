package com.hcl.domino.commons.richtext.structures;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import com.hcl.domino.richtext.structures.OpaqueTimeDate;
import com.hcl.domino.richtext.structures.OriginatorID;

/**
 * {@link OriginatorID} implementation that is useful for reading from
 * a pre-read buffer, but which is not guaranteed to write back to
 * the original memory.
 * 
 * @since 1.57.0
 */
public class DefaultOriginatorID implements OriginatorID {
  private final ByteBuffer data;
  
  public DefaultOriginatorID(ByteBuffer data) {
    this.data = data;
  }

  @Override
  public ByteBuffer getData() {
    return data.slice().order(ByteOrder.nativeOrder());
  }

  @Override
  public OpaqueTimeDate getFile() {
    int[] value = new int[2];
    value[0] = data.getInt(0);
    value[1] = data.getInt(4);
    return new DefaultOpaqueTimeDate(value);
  }

  @Override
  public OpaqueTimeDate getNote() {
    int[] value = new int[2];
    value[0] = data.getInt(8);
    value[1] = data.getInt(12);
    return new DefaultOpaqueTimeDate(value);
  }

  @Override
  public int getSequence() {
    return data.getInt(16);
  }

  @Override
  public OriginatorID setSequence(int sequence) {
    return this;
  }

  @Override
  public OpaqueTimeDate getSequenceTime() {
    int[] value = new int[2];
    value[0] = data.getInt(20);
    value[1] = data.getInt(24);
    return new DefaultOpaqueTimeDate(value);
  }

}
