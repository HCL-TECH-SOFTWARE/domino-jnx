package com.hcl.domino.commons.design.simplesearch;

import com.hcl.domino.design.simplesearch.ByFieldTerm;

public class DefaultByFieldTerm implements ByFieldTerm {
  private final TextRule textRule;
  private final String fieldName;
  private final String textValue;

  public DefaultByFieldTerm(TextRule textRule, String fieldName, String textValue) {
    this.textRule = textRule;
    this.fieldName = fieldName;
    this.textValue = textValue;
  }

  @Override
  public TextRule getTextRule() {
    return textRule;
  }

  @Override
  public String getFieldName() {
    return fieldName;
  }

  @Override
  public String getTextValue() {
    return textValue;
  }

}
