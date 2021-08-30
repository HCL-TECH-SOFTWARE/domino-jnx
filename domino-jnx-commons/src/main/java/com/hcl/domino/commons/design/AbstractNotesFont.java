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
package com.hcl.domino.commons.design;

import java.util.Optional;
import java.util.Set;

import com.hcl.domino.data.FontAttribute;
import com.hcl.domino.data.NotesFont;
import com.hcl.domino.data.StandardColors;
import com.hcl.domino.data.StandardFonts;
import com.hcl.domino.richtext.structures.FontStyle;

/**
 * Base implementation logic for {@link NotesFont} that use a {@link FontStyle}
 * object for most properties and delegates to a specific implementation for
 * looking up non-standard font names.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public abstract class AbstractNotesFont implements NotesFont {
  private final FontStyle fontStyle;

  public AbstractNotesFont(FontStyle fontStyle) {
    this.fontStyle = fontStyle;
  }

  @Override
  public Set<FontAttribute> getAttributes() {
    return fontStyle.getAttributes();
  }

  @Override
  public int getPointSize() {
    return fontStyle.getPointSize();
  }

  @Override
  public Optional<StandardFonts> getStandardFont() {
    Optional<StandardFonts> font = fontStyle.getStandardFont();
    if(font.isPresent() && font.get() != StandardFonts.CUSTOMFONT) {
      return font;
    } else {
      return Optional.empty();
    }
  }

  @Override
  public Optional<String> getFontName() {
    Optional<StandardFonts> font = fontStyle.getStandardFont();
    if(font.isPresent() && font.get() != StandardFonts.CUSTOMFONT) {
      return Optional.empty();
    }
    // Then look up the font by column index in the parent.
    // These are stored as three-element tuples where the first element matches the numeric value
    //   of FontStyle#getFontFace
    int faceId = Byte.toUnsignedInt(fontStyle.getFontFace());
    return Optional.of(lookUpFaceID(faceId));
  }
  
  @Override
  public Optional<StandardColors> getStandardColor() {
    return fontStyle.getColor();
  }
  
  /**
   * Looks up the font name for a face ID in the underlying implementation mechanism.
   * 
   * @param faceId the face ID to look up, which will be a number greater than 4
   * @return the corresponding font name
   * @throws IllegalStateException if the given ID does not exist in the font table
   */
  protected abstract String lookUpFaceID(int faceId);

}
