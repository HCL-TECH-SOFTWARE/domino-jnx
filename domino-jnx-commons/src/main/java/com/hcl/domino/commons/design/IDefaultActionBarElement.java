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

import com.hcl.domino.design.ActionBar;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.DesignElement.ActionBarElement;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public interface IDefaultActionBarElement extends ActionBarElement {
  @Override
  default ActionBar getActionBar() {
    // TODO account for pre-V5 actions
    return new DefaultActionBar(getDocument(), DesignConstants.V5ACTION_ITEM);
  }
}
