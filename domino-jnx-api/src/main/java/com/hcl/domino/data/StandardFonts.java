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
   * {@link FontStyle#setStandardFont(StandardFonts)}
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