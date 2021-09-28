package com.hcl.domino.design;

import java.util.Optional;

public interface OutlineEntryData {

  public short getDataType();
  
  public Optional<Object> getDataValue();
}
