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
