/*
 * ==========================================================================
 * Copyright (C) 2019-2021 HCL America, Inc. ( http://www.hcl.com/ )
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
