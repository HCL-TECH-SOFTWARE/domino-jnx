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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.hcl.domino.design.simplesearch.ExampleFormTerm;

public class DefaultExampleFormTerm implements ExampleFormTerm {
  
  private final String formName;
  private final Map<String, List<Object>> fieldMatches;

  public DefaultExampleFormTerm(String formName, Map<String, List<Object>> fieldMatches) {
    this.formName = formName;
    Map<String, List<Object>> matches = new LinkedHashMap<>();
    for(Map.Entry<String, List<Object>> entry : fieldMatches.entrySet()) {
      matches.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
    }
    this.fieldMatches = Collections.unmodifiableMap(matches);
  }

  @Override
  public String getFormName() {
    return formName;
  }

  @Override
  public Map<String, List<Object>> getFieldMatches() {
    return fieldMatches;
  }

}
