package com.hcl.domino.commons.data.types;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import com.hcl.domino.data.types.WORD;

public class WordByReference implements WORD.ByReference {
  private final ByteBuffer value;
  
  public WordByReference(ByteBuffer value) {
    if(!value.isDirect()) {
      throw new IllegalArgumentException("value must be direct");
    }
    this.value = value;
  }
  
  public WordByReference() {
    this((char) 0);
  }

  public WordByReference(char value) {
    this.value = ByteBuffer.allocateDirect(2).order(ByteOrder.nativeOrder());
    setValue(value);
  }

  public WordByReference(int value) {
    this.value = ByteBuffer.allocateDirect(2).order(ByteOrder.nativeOrder());
    setValue((char)value);
  }

  public void setValue(char value) {
    this.value.putShort(0, (short)value);
  }
  
  public void setValue(WORD value) {
    setValue(value == null ? (char)0 : value.getValue());
  }

  @Override
  public char getValue() {
    return (char)Short.toUnsignedInt(this.value.getShort(0));
  }
  
  public WORD getWord() {
    return WordImpl.of(getValue());
  }
  
  public ByteBuffer getBuffer() {
    return this.value;
  }

  @Override
  public String toString() {
    return "[WordByReference: 0x" + Integer.toHexString(getValue()) + ']';
  }
}
