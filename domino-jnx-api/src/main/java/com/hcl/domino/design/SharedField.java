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
package com.hcl.domino.design;

import java.util.List;

import com.hcl.domino.design.DesignElement.NamedDesignElement;

/**
 * Represents a Shared Field design element.
 * 
 * @author Jesse Gallagher
 * @since 1.0.34
 */
public interface SharedField extends NamedDesignElement {
  /**
   * Retrieves the field components as a list of rich-text entities.
   * 
   * @return a {@link List} of rich-text entities
   * @since 1.0.34
   */
  List<?> getFieldBody();
  
  /**
   * Retrieves the element-global LotusScript associated with the field.
   * 
   * @return a {@link String} representing the IDE-formatted LotusScript for the element
   * @since 1.0.34
   */
  String getLotusScript();
}
