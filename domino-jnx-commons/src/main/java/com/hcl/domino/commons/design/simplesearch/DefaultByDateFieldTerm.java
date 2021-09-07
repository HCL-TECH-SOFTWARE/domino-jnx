package com.hcl.domino.commons.design.simplesearch;

import java.util.Optional;
import java.util.OptionalInt;

import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.design.simplesearch.ByDateFieldTerm;

public class DefaultByDateFieldTerm extends DefaultByFieldTerm implements ByDateFieldTerm {
  private final DateType dateType;
  private final DateRule dateRule;
  private final Optional<DominoDateTime> date;
  private final OptionalInt dayCount;
  private final Optional<DominoDateRange> range;

  public DefaultByDateFieldTerm(TextRule textRule, String fieldName, String textValue, DateType dateType, DateRule dateRule, DominoDateTime date) {
    super(textRule, fieldName, textValue);
    this.dateType = dateType;
    this.dateRule = dateRule;
    this.date = Optional.of(date);
    this.dayCount = OptionalInt.empty();
    this.range = Optional.empty();
  }
  
  public DefaultByDateFieldTerm(TextRule textRule, String fieldName, String textValue, DateType dateType, DateRule dateRule, int dayCount) {
    super(textRule, fieldName, textValue);
    this.dateType = dateType;
    this.dateRule = dateRule;
    this.date = Optional.empty();
    this.dayCount = OptionalInt.of(dayCount);
    this.range = Optional.empty();
  }
  
  public DefaultByDateFieldTerm(TextRule textRule, String fieldName, String textValue, DateType dateType, DateRule dateRule, DominoDateRange range) {
    super(textRule, fieldName, textValue);
    this.dateType = dateType;
    this.dateRule = dateRule;
    this.date = Optional.empty();
    this.dayCount = OptionalInt.empty();
    this.range = Optional.of(range);
  }

  @Override
  public DateType getDateType() {
    return this.dateType;
  }

  @Override
  public DateRule getDateRule() {
    return this.dateRule;
  }

  @Override
  public Optional<DominoDateTime> getDate() {
    return this.date;
  }

  @Override
  public OptionalInt getDayCount() {
    return this.dayCount;
  }

  @Override
  public Optional<DominoDateRange> getDateRange() {
    return this.range;
  }

}
