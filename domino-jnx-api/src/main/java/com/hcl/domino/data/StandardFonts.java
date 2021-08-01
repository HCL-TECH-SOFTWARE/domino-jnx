package com.hcl.domino.data;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.structures.FontStyle;

/**
 * These symbols define the standard type faces.
 * The Face member of the {@link FontStyle} may be either one of these standard
 * font faces,
 * or a font ID resolved by a font table.
 */
public enum StandardFonts implements INumberEnum<Byte> {
  /** (e.g. Times Roman family) */
  ROMAN(0),
  /** (e.g. Helv family) */
  SWISS(1),
  /** (e.g. Monotype Sans WT) */
  UNICODE(2),
  /** (e.g. Arial */
  USERINTERFACE(3),
  /** (e.g. Courier family) */
  TYPEWRITER(4),
  /**
   * returned if font is not in the standard table; cannot be set via
   * {@link FontStyle#setFontFace(StandardFonts)}
   */
  CUSTOMFONT((byte) 255);

  private final byte m_face;

  StandardFonts(final int face) {
    this.m_face = (byte) (face & 0xff);
  }

  @Override
  public long getLongValue() {
    return this.m_face;
  }

  @Override
  public Byte getValue() {
    return this.m_face;
  }
}