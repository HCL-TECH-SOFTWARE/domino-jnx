package com.hcl.domino.commons.design.simplesearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hcl.domino.design.simplesearch.TextTerm;

public class DefaultTextTerm implements TextTerm {
  private final Type type;
  private final List<String> values;
  
  public DefaultTextTerm(Type type, List<String> values) {
    this.type = type;
    this.values = Collections.unmodifiableList(new ArrayList<>(values));
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public List<String> getValues() {
    return values;
  }

}
