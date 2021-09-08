package com.hcl.domino.commons.design.simplesearch;

import com.hcl.domino.design.simplesearch.ByAuthorTerm;

public class DefaultByAuthorTerm extends DefaultByFieldTerm implements ByAuthorTerm {

  public DefaultByAuthorTerm(TextRule textRule, String fieldName, String textValue) {
    super(textRule, fieldName, textValue);
  }

}
