package com.hcl.domino.jna.data.types;

import com.hcl.domino.commons.data.types.WordImpl;
import com.hcl.domino.data.types.WORD;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByReference;

public class WordByReference extends ByReference implements WORD.ByReference {
  public WordByReference() {
    this((char) 0);
  }

  public WordByReference(char value) {
    super(2);
    setValue(value);
  }

  public WordByReference(int value) {
    super(2);
    setValue((char)value);
  }

  public void setValue(char value) {
    getPointer().setShort(0, (short)value);
  }
  
  public void setValue(WORD value) {
    setValue(value == null ? (char)0 : value.getValue());
  }

  @Override
  public char getValue() {
    return (char)(getPointer().getShort(0) & 0xFFFF);
  }
  
  public WORD getWord() {
    return WordImpl.of(getValue());
  }

  @Override
  public String toString() {
    return String.format("char@0x%1$x=0x%2$x (%2$d)", Pointer.nativeValue(getPointer()),
        getValue());
  }
}
