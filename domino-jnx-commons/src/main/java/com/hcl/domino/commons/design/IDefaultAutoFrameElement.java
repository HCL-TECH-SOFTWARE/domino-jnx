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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.DesignElement;
import com.hcl.domino.design.DesignElement.AutoFrameElement;

/**
 * This mixin interface adds default behavior to implement {@link AutoFrameElement}.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public interface IDefaultAutoFrameElement extends DesignElement, AutoFrameElement {
  @Override
  default Optional<String> getAutoFrameFrameset() {
    List<String> frameInfo = getDocument().getAsList(DesignConstants.ITEM_NAME_FRAMEINFO, String.class, Collections.emptyList());
    if(frameInfo.isEmpty() || StringUtil.isEmpty(frameInfo.get(0))) {
      return Optional.empty();
    } else {
      return Optional.of(frameInfo.get(0));
    }
  }
  
  @Override
  default Optional<String> getAutoFrameTarget() {
    List<String> frameInfo = getDocument().getAsList(DesignConstants.ITEM_NAME_FRAMEINFO, String.class, Collections.emptyList());
    if(frameInfo.size() < 2 || StringUtil.isEmpty(frameInfo.get(1))) {
      return Optional.empty();
    } else {
      return Optional.of(frameInfo.get(1));
    }
  }
}
