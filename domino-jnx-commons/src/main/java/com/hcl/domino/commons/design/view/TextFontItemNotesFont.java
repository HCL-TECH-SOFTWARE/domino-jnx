package com.hcl.domino.commons.design.view;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.FontAttribute;
import com.hcl.domino.data.NotesFont;
import com.hcl.domino.data.StandardFonts;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.richtext.structures.FontStyle;

/**
 * This {@link NotesFont} implementation reads its custom font information
 * from a TEXT_LIST item named "$Fonts".
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public class TextFontItemNotesFont implements NotesFont {
  private final Document doc;
  private final FontStyle fontStyle;
  
  public TextFontItemNotesFont(Document doc, FontStyle fontStyle) {
    this.doc = doc;
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
    List<String> fontTable = doc.getAsList(DesignConstants.ITEM_NAME_FONTS, String.class, Collections.emptyList());
    for(int i = 0; i < fontTable.size(); i += 3) {
      if(String.valueOf(faceId).equals(fontTable.get(i))) {
        // Then we have a match. The font name will be the third value
        return Optional.of(fontTable.get(i+2));
      }
    }
    throw new IllegalStateException(MessageFormat.format("Unable to find font name in list {0}", fontTable));
  }
  
}