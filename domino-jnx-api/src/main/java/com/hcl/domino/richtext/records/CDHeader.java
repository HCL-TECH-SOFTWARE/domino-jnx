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
package com.hcl.domino.richtext.records;

import java.nio.charset.Charset;
import java.util.Arrays;

import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.34
 */
@StructureDefinition(
  name = "CDHEADER",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "FontPitchAndFamily", type = byte.class),
    @StructureMember(name = "FontName", type = byte[].class, length = RichTextConstants.MAXFACESIZE),
    @StructureMember(name = "Font", type = FontStyle.class),
    @StructureMember(name = "HeadLength", type = short.class, unsigned = true)
  }
)
public interface CDHeader extends RichTextRecord<WSIG> {
  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("FontName")
  byte[] getFontNameRaw();
  
  @StructureSetter("FontName")
  CDHeader setFontNameRaw(char[] fontName);
  
  @StructureGetter("Font")
  FontStyle getFontStyle();
  
  @StructureGetter("HeadLength")
  int getTextLength();
  
  @StructureSetter("HeadLength")
  CDHeader setTextLength(int len);
  
  default String getFontName() {
    int firstNull = 0;
    byte[] name = getFontNameRaw();
    for(firstNull = 0; firstNull < RichTextConstants.MAXFACESIZE; firstNull++) {
      if(name[firstNull] == '\0') {
        break;
      }
    }
    return new String(name, 0, firstNull, Charset.forName("LMBCS")); //$NON-NLS-1$
  }
  
  default String getText() {
    return StructureSupport.extractStringValue(
      this,
      0,
      getTextLength()
    );
  }
  
  default CDHeader setText(String text) {
    return StructureSupport.writeStringValue(
      this,
      0,
      getTextLength(),
      text,
      this::setTextLength
    );
  }
}
