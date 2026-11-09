package com.hcl.domino.commons.data.types;

import java.util.Objects;
import com.hcl.domino.data.types.WORD;
import com.hcl.domino.misc.NotesConstants;

public class WordImpl implements WORD {
  public static final WordImpl ZERO;
  private static final WordImpl[] PREALLOC = new WordImpl[17];
  static {
    for(char i = 0; i < 17; i++) {
      PREALLOC[i] = new WordImpl(i);
    }
    ZERO = PREALLOC[0];
  }
  private static final WordImpl MAXUSERNAME = new WordImpl((char)NotesConstants.MAXUSERNAME);
  private static final WordImpl MAXWORD = new WordImpl((char)(NotesConstants.MAXWORD & 0xFFFF));
  private static final WordImpl FIVE_TWELVE = new WordImpl((char)512);
  private static final WordImpl SPECIAL_NOTE_ICON = new WordImpl((char)(NotesConstants.SPECIAL_ID_NOTE | NotesConstants.NOTE_CLASS_ICON));
  private static final WordImpl SPECIAL_NOTE_VIEW = new WordImpl((char)(NotesConstants.SPECIAL_ID_NOTE | NotesConstants.NOTE_CLASS_VIEW));
  
  public static final WordImpl of(char value) {
    if(value < 17) {
      return PREALLOC[value];
    }
    switch(value) {
      case 0: return ZERO;
      case 512: return FIVE_TWELVE;
      case NotesConstants.MAXUSERNAME: return MAXUSERNAME;
      case (char)(NotesConstants.MAXWORD & 0xFFFF): return MAXWORD;
      case (char)(NotesConstants.SPECIAL_ID_NOTE | NotesConstants.NOTE_CLASS_ICON): return SPECIAL_NOTE_ICON;
      case (char)(NotesConstants.SPECIAL_ID_NOTE | NotesConstants.NOTE_CLASS_VIEW): return SPECIAL_NOTE_VIEW;
      default: return new WordImpl(value);
    }
  }
  
  private final char value;
  
  public WordImpl(char value) {
    this.value = value;
  }

  @Override
  public char getValue() {
    return this.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(Character.valueOf(value));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    WordImpl other = (WordImpl) obj;
    return value == other.value;
  }
  
  @Override
  public String toString() {
    return "0x" + Integer.toHexString(value);
  }

}
