package com.hcl.domino.commons.design.outline;

import java.util.ArrayList;
import java.util.List;
import com.hcl.domino.data.IAdaptable;

public class DominoOutlineFormat implements IAdaptable {
  
  private final List<IAdaptable> outlineEntries = new ArrayList();

  @Override
  public <T> T getAdapter(Class<T> clazz) {
    return null;
  }

  public List<IAdaptable> getOutlineEntries() {
    return this.outlineEntries;
  }
  
  public void addOutlineEntry(DominoOutlineEntry outlineEntry) {
    this.outlineEntries.add(outlineEntry);
  }
}
