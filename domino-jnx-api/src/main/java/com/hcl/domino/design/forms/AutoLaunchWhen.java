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
package com.hcl.domino.design.forms;

import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.misc.INumberEnum;

/**
 * Represents the conditions for when to auto-launch an object when
 * {@link com.hcl.domino.design.Form.AutoLaunchSettings#getType() AutoLaunchSettings#getType()}
 * is an object type.
 * 
 * @author Jesse Gallagher
 * @since 1.0.34
 */
public enum AutoLaunchWhen implements INumberEnum<Integer> {
  CREATE(DesignConstants.LAUNCH_WHEN_CREATE),
  EDIT(DesignConstants.LAUNCH_WHEN_EDIT),
  READ(DesignConstants.LAUNCH_WHEN_READ);
  
  private final int value;

  AutoLaunchWhen(final int value) {
    this.value = value;
  }

  @Override
  public long getLongValue() {
    return this.value;
  }

  @Override
  public Integer getValue() {
    return this.value;
  }
}