package com.hcl.domino.commons.data.types;

import java.util.Objects;
import java.util.Optional;
import com.hcl.domino.DominoException;
import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.data.types.STATUS;
import com.hcl.domino.misc.NotesConstants;

public class StatusImpl implements STATUS {
  public static final StatusImpl NOERROR = new StatusImpl(INotesErrorConstants.NOERROR);
  public static final StatusImpl NOT_FOUND = new StatusImpl(INotesErrorConstants.ERR_NOT_FOUND);
  private static final StatusImpl NSF_INTERRUPT = new StatusImpl(INotesErrorConstants.ERR_NSF_INTERRUPT);
  private static final StatusImpl ITEM_NOT_FOUND = new StatusImpl(INotesErrorConstants.ERR_ITEM_NOT_FOUND);
  
  public static StatusImpl of(short value) {
    switch(value) {
      case INotesErrorConstants.NOERROR: return NOERROR;
      case INotesErrorConstants.ERR_NOT_FOUND: return NOT_FOUND;
      case INotesErrorConstants.ERR_NSF_INTERRUPT: return NSF_INTERRUPT;
      case INotesErrorConstants.ERR_ITEM_NOT_FOUND: return ITEM_NOT_FOUND;
      default: return NotesErrorUtils.STATUS_CACHE.computeIfAbsent(value, StatusImpl::new);
    } 
  }
  
  private final short value;
  private final short maskedValue;
  private final boolean remoteError;
  
  public StatusImpl(short value) {
    this.value = value;
    this.maskedValue = (short)(value & NotesConstants.ERR_MASK);
    this.remoteError = (value & NotesConstants.STS_REMOTE) == NotesConstants.STS_REMOTE;
  }
  
  @Override
  public short getValue() {
    return value;
  }
  
  @Override
  public short getMaskedValue() {
    return maskedValue;
  }
  
  @Override
  public boolean isRemoteError() {
    return remoteError;
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
  
  @Override
  public String toString() {
    return "STATUS: 0x" + Integer.toHexString(value);
  }
}