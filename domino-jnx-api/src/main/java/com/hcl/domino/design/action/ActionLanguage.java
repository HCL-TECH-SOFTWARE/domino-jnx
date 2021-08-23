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
package com.hcl.domino.design.action;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;

/**
 * Represents the available language types for an action bar action.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum ActionLanguage implements INumberEnum<Short> {
  RUN_FORMULA(RichTextConstants.ACTION_RUN_FORMULA),
  RUN_SCRIPT(RichTextConstants.ACTION_RUN_SCRIPT),
  RUN_AGENT(RichTextConstants.ACTION_RUN_AGENT),
  OLDSYS_COMMAND(RichTextConstants.ACTION_OLDSYS_COMMAND),
  SYS_COMMAND(RichTextConstants.ACTION_SYS_COMMAND),
  PLACEHOLDER(RichTextConstants.ACTION_PLACEHOLDER),
  RUN_JAVASCRIPT(RichTextConstants.ACTION_RUN_JAVASCRIPT);

  private final short value;

  ActionLanguage(final int value) {
    this.value = (short) value;
  }

  @Override
  public long getLongValue() {
    return this.value;
  }

  @Override
  public Short getValue() {
    return this.value;
  }
}