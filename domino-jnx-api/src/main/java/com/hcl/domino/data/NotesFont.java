package com.hcl.domino.data;

import java.util.Optional;
import java.util.Set;

import com.hcl.domino.richtext.structures.FontStyle;

/**
 * Represents a font definition that is based around settings in a {@link FontStyle}
 * structure and a contextual note.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public interface NotesFont {
  /**
   * Retrieves the style attributes set for this font.
   * 
   * @return a {@link Set} of {@link FontAttribute} values
   */
  Set<FontAttribute> getAttributes();
  
  /**
   * Retrieves the size, in Notes-style points, for this font.
   * 
   * @return the font size in points
   */
  int getPointSize();
  
  /**
   * Retrieves the standard font type specified, if configured as such.
   * 
   * @return an {@link Optional} describing the {@link StandardFonts} value,
   *         or an empty one if this uses a custom font
   */
  Optional<StandardFonts> getStandardFont();
  
  /**
   * Retrieves the name of the font specified, if not a standard one.
   * 
   * @return an {@link Optional} describing the non-standard font name, or an
   *         empty one if this uses a standard font
   */
  Optional<String> getFontName();
  
}
