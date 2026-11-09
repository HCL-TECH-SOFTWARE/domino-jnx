package com.hcl.domino.commons.data.types;

import com.hcl.domino.data.types.STATUS;
import com.hcl.domino.data.types.WORD;
import com.hcl.domino.misc.INumberEnum;

public enum JNXTypeUtil {
  ;
  
  public static WORD word(char value) {
    return WordImpl.of(value);
  }
  
  public static WORD word(int value) {
    return WordImpl.of((char)value);
  }
  
  public static WORD word(long value) {
    return WordImpl.of((char)value);
  }
  
  public static WORD word(short value) {
    return WordImpl.of((char)(Short.toUnsignedInt(value)));
  }
  
  public static WORD word(boolean value) {
    return word(value ? 1 : 0);
  }
  
  public static WORD word(INumberEnum<Short> value) {
    return word(value.getValue());
  }
  
  public static STATUS status(short value) {
    return StatusImpl.of(value);
  }
}
