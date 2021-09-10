package com.hcl.domino.commons.design.simplesearch;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.hcl.domino.design.simplesearch.ByFormTerm;

public class DefaultByFormTerm implements ByFormTerm {
  private final Set<String> formNames;
  
  public DefaultByFormTerm(Collection<String> formNames) {
    this.formNames = Collections.unmodifiableSet(new LinkedHashSet<>(formNames));
  }

  @Override
  public Set<String> getFormNames() {
    return formNames;
  }

}
