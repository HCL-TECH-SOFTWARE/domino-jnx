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
package com.hcl.domino.richtext.records;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.data.StandardColors;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.INumberEnum;
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
  name = "CDBAR",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Flags", type = CDBar.Flag.class, bitfield = true),
    @StructureMember(name = "FontID", type = FontStyle.class)
  }
)
public interface CDBar extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Integer> {
    DISABLED_FOR_NON_EDITORS(RichTextConstants.BARREC_DISABLED_FOR_NON_EDITORS),
    EXPANDED(RichTextConstants.BARREC_EXPANDED),
    PREVIEW(RichTextConstants.BARREC_PREVIEW),
    BORDER_INVISIBLE(RichTextConstants.BARREC_BORDER_INVISIBLE),
    ISFORMULA(RichTextConstants.BARREC_ISFORMULA),
    HIDE_EXPANDED(RichTextConstants.BARREC_HIDE_EXPANDED),
    POSTREPLYSECTION(RichTextConstants.BARREC_POSTREPLYSECTION),
    INTENDED(RichTextConstants.BARREC_INTENDED),
    HAS_COLOR(RichTextConstants.BARREC_HAS_COLOR),
    INDENTED(RichTextConstants.BARREC_INDENTED);
    private final int value;
    private Flag(int value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return value;
    }

    @Override
    public Integer getValue() {
      return value;
    }
  }
  
  enum BorderType implements INumberEnum<Integer> {
    SHADOW(RichTextConstants.BARREC_BORDER_SHADOW),
    NONE(RichTextConstants.BARREC_BORDER_NONE),
    SINGLE(RichTextConstants.BARREC_BORDER_SINGLE),
    DOUBLE(RichTextConstants.BARREC_BORDER_DOUBLE),
    TRIPLE(RichTextConstants.BARREC_BORDER_TRIPLE),
    TWOLINE(RichTextConstants.BARREC_BORDER_TWOLINE),
    WINDOWCAPTION(RichTextConstants.BARREC_BORDER_WINDOWCAPTION),
    GRADIENT(RichTextConstants.BARREC_BORDER_GRADIENT),
    TAB(RichTextConstants.BARREC_BORDER_TAB),
    DIAG(RichTextConstants.BARREC_BORDER_DIAG),
    DUOCOLOR(RichTextConstants.BARREC_BORDER_DUOCOLOR);
    private final int value;
    private BorderType(int value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return value;
    }

    @Override
    public Integer getValue() {
      return value;
    }
  }
  
  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("Flags")
  Set<Flag> getFlags();
  
  @StructureSetter("Flags")
  CDBar setFlags(Collection<Flag> flags);
  
  @StructureGetter("Flags")
  int getFlagsRaw();
  
  @StructureSetter("Flags")
  CDBar setFlagsRaw(int flags);
  
  @StructureGetter("FontID")
  FontStyle getFontID();
  
  default BorderType getBorderType() {
    int flags = getFlagsRaw();
    int val = (flags & RichTextConstants.BARREC_BORDER_MASK) >> RichTextConstants.BARREC_BORDER_SHIFT;
    return DominoEnumUtil.valueOf(BorderType.class, val).orElse(BorderType.SHADOW);
  }
  
  default CDBar setBorderType(BorderType type) {
    int val = type == null ? 0 : type.getValue();
    int flags = getFlagsRaw() & ~RichTextConstants.BARREC_BORDER_MASK;
    setFlagsRaw(flags | (val << RichTextConstants.BARREC_BORDER_SHIFT));
    return this;
  }
  
  /**
   * Retrieves the color value for this bar, if specified.
   * 
   * @return an {@link Optional} describing the corresponding {@link StandardColors}
   *         value, or an empty one of this does not have a color
   */
  default Optional<StandardColors> getColor() {
    if(!getFlags().contains(Flag.HAS_COLOR)) {
      return Optional.empty();
    }
    short val = getVariableData().getShort();
    return DominoEnumUtil.valueOf(StandardColors.class, val);
  }
  
  /**
   * Retrieves the caption formula for this bar, if set.
   * 
   * @return an {@link Optional} describing the caption formula text, or an empty one
   *         if this is not set
   */
  default Optional<String> getCaptionFormula() {
    if(!getFlags().contains(Flag.ISFORMULA)) {
      return Optional.empty();
    }
    
    // Preceded by a WORD if HAS_COLOR is set
    int preLen = getFlags().contains(Flag.HAS_COLOR) ? 2 : 0;
    return Optional.of(
      StructureSupport.extractCompiledFormula(
        this,
        preLen,
        getVariableData().remaining()-preLen
      )
    );
  }

  /**
   * Returns the static caption text for this bar, if set.
   * 
   * @return an {@link Optional} describing the caption text, or an empty one if this
   *         is not set
   */
  default Optional<String> getCaption() {
    if(getFlags().contains(Flag.ISFORMULA)) {
      return Optional.empty();
    }
    
    // Preceded by a WORD if HAS_COLOR is set
    int preLen = getFlags().contains(Flag.HAS_COLOR) ? 2 : 0;
    return Optional.of(
      StructureSupport.extractStringValue(
        this,
        preLen,
        getVariableData().remaining()-preLen
      )
    );
  }
}
