package com.hcl.domino.commons.design;

import java.util.Optional;
import java.util.Set;

import com.hcl.domino.data.FontAttribute;
import com.hcl.domino.data.NotesFont;
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
  
  /**
   * Looks up the font name for a face ID in the underlying implementation mechanism.
   * 
   * @param faceId the face ID to look up, which will be a number greater than 4
   * @return the corresponding font name
   * @throws IllegalStateException if the given ID does not exist in the font table
   */
  protected abstract String lookUpFaceID(int faceId);

}
