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
package com.hcl.domino.commons.design.simpleaction;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.hcl.domino.design.simpleaction.ModifyByFormAction;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public class DefaultModifyByFormAction implements ModifyByFormAction {
  private final String formName;
  private final Map<String, List<String>> modifications;

  public DefaultModifyByFormAction(final String formName, final Map<String, List<String>> modifications) {
    this.formName = formName;
    this.modifications = new LinkedHashMap<>(modifications);
  }

  @Override
  public String getFormName() {
    return this.formName;
  }

  @Override
  public Map<String, List<String>> getModifications() {
    return Collections.unmodifiableMap(this.modifications);
  }

}
