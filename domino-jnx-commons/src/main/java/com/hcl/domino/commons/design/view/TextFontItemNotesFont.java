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
package com.hcl.domino.commons.design.view;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import com.hcl.domino.commons.design.AbstractNotesFont;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.NotesFont;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.richtext.structures.FontStyle;

/**
 * This {@link NotesFont} implementation reads its custom font information
 * from a TEXT_LIST item named "$Fonts".
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public class TextFontItemNotesFont extends AbstractNotesFont {
  private final Document doc;
  
  public TextFontItemNotesFont(Document doc, FontStyle fontStyle) {
    super(fontStyle);
    this.doc = doc;
  }
  
  @Override
  protected String lookUpFaceID(int faceId) {
    List<String> fontTable = doc.getAsList(DesignConstants.ITEM_NAME_FONTS, String.class, Collections.emptyList());
    for(int i = 0; i < fontTable.size(); i += 3) {
      if(String.valueOf(faceId).equals(fontTable.get(i))) {
        // Then we have a match. The font name will be the third value
        return fontTable.get(i+2);
      }
    }
    throw new IllegalStateException(MessageFormat.format("Unable to find font name in list {0}", fontTable));
  }
}