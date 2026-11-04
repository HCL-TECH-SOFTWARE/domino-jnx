package com.hcl.domino.commons.data.types;

import java.util.Objects;
import java.util.Optional;
import com.hcl.domino.DominoException;
import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.data.types.STATUS;

public class StatusImpl implements STATUS {
  public static final StatusImpl NOERROR = new StatusImpl(INotesErrorConstants.NOERROR);
  public static final StatusImpl NOT_FOUND = new StatusImpl(INotesErrorConstants.ERR_NOT_FOUND);
  
  public static StatusImpl of(short value) {
    if(value == 0) {
      return NOERROR;
    } else {
      return new StatusImpl(value);
    }
  }
  
  
  private final short value;
  
  public StatusImpl(short value) {
    this.value = value;
  }
  
  @Override
  public short getValue() {
    return value;
  }
  
  @Override
  public STATUS orElseThrow() {
    NotesErrorUtils.checkResult(value);
    return this;
  }
  
  @Override
  public String toErrorString() {
    return NotesErrorUtils.errToString(value);
  }
  
  @Override
  public STATUS throwIfNot(short... allowedErrors) {
    short masked = getMaskedValue();
    if(masked != 0) {
      for(short allowed : allowedErrors) {
        if(masked == allowed) {
          return this;
        }
      }
      NotesErrorUtils.checkResult(value);
    }
    return this;
  }
  
  @Override
  public Optional<DominoException> toDominoException() {
    return NotesErrorUtils.toNotesError(value);
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(Short.valueOf(value));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    StatusImpl other = (StatusImpl) obj;
    return value == other.value;
  }
}