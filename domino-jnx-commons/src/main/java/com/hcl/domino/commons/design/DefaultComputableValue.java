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
package com.hcl.domino.commons.design;

import com.hcl.domino.design.ComputableValue;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public class DefaultComputableValue implements ComputableValue {
  private final String value;
  private final boolean formula;

  public DefaultComputableValue(final String value, final boolean formula) {
    this.value = value;
    this.formula = formula;
  }

  @Override
  public String getValue() {
    return this.value;
  }

  @Override
  public boolean isFormula() {
    return this.formula;
  }

}
