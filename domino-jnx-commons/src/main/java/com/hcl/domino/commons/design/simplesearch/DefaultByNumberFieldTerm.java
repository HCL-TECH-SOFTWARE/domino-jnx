/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
 *                            All rights reserved.
 * ==========================================================================
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may
 * not use this file except in compliance with the License.  You may obtain a
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.
 *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the  specific language  governing permissions  and limitations
 * under the License.
 * ==========================================================================
 */
package com.hcl.domino.commons.design.simplesearch;

import java.util.Optional;
import java.util.OptionalDouble;

import com.hcl.domino.design.simplesearch.ByNumberFieldTerm;
import com.hcl.domino.misc.Pair;

public class DefaultByNumberFieldTerm extends DefaultByFieldTerm implements ByNumberFieldTerm {
  private final NumberRule numberRule;
  private final OptionalDouble number;
  private final Optional<Pair<Double, Double>> numberRange;

  public DefaultByNumberFieldTerm(TextRule textRule, String fieldName, String textValue, NumberRule numberRule, double number) {
    super(textRule, fieldName, textValue);
    this.numberRule = numberRule;
    this.number = OptionalDouble.of(number);
    this.numberRange = Optional.empty();
  }
  
  public DefaultByNumberFieldTerm(TextRule textRule, String fieldName, String textValue, NumberRule numberRule, Pair<Double, Double> numberRange) {
    super(textRule, fieldName, textValue);
    this.numberRule = numberRule;
    this.number = OptionalDouble.empty();
    this.numberRange = Optional.of(numberRange);
  }

  @Override
  public NumberRule getNumberRule() {
    return this.numberRule;
  }

  @Override
  public OptionalDouble getNumber() {
    return this.number;
  }

  @Override
  public Optional<Pair<Double, Double>> getNumberRange() {
    return this.numberRange;
  }

}
