package com.hcl.domino.commons.design.outline;

import java.util.ArrayList;
import java.util.List;
import com.hcl.domino.data.IAdaptable;

public class DominoOutlineFormat implements IAdaptable {
  
  private final List<String> titles = new ArrayList<>();

  @Override
  public <T> T getAdapter(Class<T> clazz) {
    return null;
  }

  public List<String> getTitles() {
    return this.titles;
  }
  
  public void addTitle(String title) {
    this.titles.add(title);
  }
}
