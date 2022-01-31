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
package com.hcl.domino.design.format;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;

/**
 * Represents border style options for action bars and other elements.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum BorderStyle implements INumberEnum<Short> {
  NONE(RichTextConstants.CDBORDERSTYLE_NONE),
  SOLID(RichTextConstants.CDBORDERSTYLE_SOLID),
  DOUBLE(RichTextConstants.CDBORDERSTYLE_DOUBLE),
  INSET(RichTextConstants.CDBORDERSTYLE_INSET),
  OUTSET(RichTextConstants.CDBORDERSTYLE_OUTSET),
  RIDGE(RichTextConstants.CDBORDERSTYLE_RIDGE),
  GROOVE(RichTextConstants.CDBORDERSTYLE_GROOVE),
  DOTTED(RichTextConstants.CDBORDERSTYLE_DOTTED),
  DASHED(RichTextConstants.CDBORDERSTYLE_DASHED),
  PICTURE(RichTextConstants.CDBORDERSTYLE_PICTURE),
  GRAPHIC(RichTextConstants.CDBORDERSTYLE_GRAPHIC)
  ;
  
  private final short value;
  
  private BorderStyle(short value) {
    this.value = value;
  }

  @Override
  public long getLongValue() {
    return value;
  }

  @Override
  public Short getValue() {
    return value;
  }
}
