package com.hcl.domino.commons.design.simplesearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.hcl.domino.design.simplesearch.ExampleFormTerm;

public class DefaultExampleFormTerm implements ExampleFormTerm {
  
  private final String formName;
  private final Map<String, List<String>> fieldMatches;

  public DefaultExampleFormTerm(String formName, Map<String, List<String>> fieldMatches) {
    this.formName = formName;
    Map<String, List<String>> matches = new LinkedHashMap<>();
    for(Map.Entry<String, List<String>> entry : fieldMatches.entrySet()) {
      matches.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
    }
    this.fieldMatches = Collections.unmodifiableMap(matches);
  }

  @Override
  public String getFormName() {
    return formName;
  }

  @Override
  public Map<String, List<String>> getFieldMatches() {
    return fieldMatches;
  }

}
