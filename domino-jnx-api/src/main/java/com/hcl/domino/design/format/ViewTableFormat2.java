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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.hcl.domino.data.StandardColors;
import com.hcl.domino.data.StandardFonts;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.ViewFormatConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.MemoryStructure;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;

@StructureDefinition(name = "VIEW_TABLE_FORMAT2", members = {
    @StructureMember(name = "Length", type = short.class, unsigned = true),
    @StructureMember(name = "BackgroundColor", type = short.class),
    @StructureMember(name = "V2BorderColor", type = short.class),
    @StructureMember(name = "TitleFont", type = FontStyle.class),
    @StructureMember(name = "UnreadFont", type = FontStyle.class),
    @StructureMember(name = "TotalsFont", type = FontStyle.class),
    @StructureMember(name = "AutoUpdateSeconds", type = short.class, unsigned = true),
    @StructureMember(name = "AlternateBackgroundColor", type = short.class),
    @StructureMember(name = "wSig", type = ViewTableFormat2.FormatSignature.class),
    @StructureMember(name = "LineCount", type = byte.class, unsigned = true),
    @StructureMember(name = "Spacing", type = ViewLineSpacing.class),
    @StructureMember(name = "BackgroundColorExt", type = short.class),
    @StructureMember(name = "HeaderLineCount", type = byte.class, unsigned = true),
    @StructureMember(name = "Flags1", type = ViewTableFormat2.Flag.class, bitfield = true),
    @StructureMember(name = "Spare", type = short[].class, length = 4),
})
public interface ViewTableFormat2 extends MemoryStructure {
  public static ViewTableFormat2 newInstanceWithDefaults() {
    ViewTableFormat2 format2 = MemoryStructureWrapperService.get().newStructure(ViewTableFormat2.class, 0);
    format2.setSignature(FormatSignature.VALID);
    format2.setSpacing(ViewLineSpacing.SINGLE_SPACE);
    format2.setAlternateBackgroundColor((short) 1);
    format2.setBackgroundColor((short) 1);
    format2.setHeaderLineCount((short) 1);
    format2.setBackgroundColorExt((short) 1);
    format2.getTitleFont()
    .setBold(true)
    .setPointSize(9)
    .setFontFace((byte) 1)
    .setStandardFont(StandardFonts.SWISS);
    
    format2.getTotalsFont()
    .setColor(StandardColors.Gray)
    .setBold(true)
    .setStandardFont(StandardFonts.SWISS)
    .setPointSize(10)
    .setFontFace((byte) 1);
    
    format2.setLineCount((short) 1);
    
    format2.getUnreadFont()
    .setStandardFont(StandardFonts.SWISS)
    .setPointSize(10)
    .setFontFace((byte) 1);
    
    return format2;
  }
  enum Flag implements INumberEnum<Byte> {
    HAS_LINK_COLUMN(ViewFormatConstants.VIEW_TABLE_HAS_LINK_COLUMN),
    HTML_PASSTHRU(ViewFormatConstants.VIEW_TABLE_HTML_PASSTHRU);

    private final byte value;

    Flag(final byte value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Byte getValue() {
      return this.value;
    }
  }

  enum FormatSignature implements INumberEnum<Short> {
    PRE_V4((short) 0),
    VALID(ViewFormatConstants.VALID_VIEW_FORMAT_SIG);

    private final short value;

    FormatSignature(final short value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Short getValue() {
      return this.value;
    }
  }

  @StructureGetter("AlternateBackgroundColor")
  short getAlternateBackgroundColor();

  @StructureGetter("AutoUpdateSeconds")
  int getAutoUpdateSeconds();

  @StructureGetter("BackgroundColor")
  short getBackgroundColor();

  @StructureGetter("BackgroundColorExt")
  short getBackgroundColorExt();

  @StructureGetter("Flags1")
  Set<Flag> getFlags();

  @StructureGetter("HeaderLineCount")
  short getHeaderLineCount();

  @StructureGetter("Length")
  int getLength();

  @StructureGetter("LineCount")
  short getLineCount();

  @StructureGetter("wSig")
  FormatSignature getSignature();

  @StructureGetter("Spacing")
  ViewLineSpacing getSpacing();

  @StructureGetter("TitleFont")
  FontStyle getTitleFont();

  @StructureGetter("TotalsFont")
  FontStyle getTotalsFont();

  @StructureGetter("UnreadFont")
  FontStyle getUnreadFont();

  @StructureGetter("V2BorderColor")
  short getV2BorderColor();

  @StructureSetter("AlternateBackgroundColor")
  ViewTableFormat2 setAlternateBackgroundColor(short color);

  @StructureSetter("AutoUpdateSeconds")
  ViewTableFormat2 setAutoUpdateSeconds(int seconds);

  @StructureSetter("BackgroundColor")
  ViewTableFormat2 setBackgroundColor(short color);

  @StructureSetter("BackgroundColorExt")
  ViewTableFormat2 setBackgroundColorExt(short color);

  @StructureSetter("Flags1")
  ViewTableFormat2 setFlags(Collection<Flag> flags);

  default ViewTableFormat2 setFlag(Flag flag, boolean b) {
    Set<Flag> oldFlags = getFlags();
    if (b) {
      if (!oldFlags.contains(flag)) {
        Set<Flag> newFlags = new HashSet<>(oldFlags);
        newFlags.add(flag);
        setFlags(newFlags);
      }
    }
    else {
      if (oldFlags.contains(flag)) {
        Set<Flag> newFlags = oldFlags
            .stream()
            .filter(currAttr -> !flag.equals(currAttr))
            .collect(Collectors.toSet());
        setFlags(newFlags);
      }
    }
    return this;
  }
  
  @StructureSetter("HeaderLineCount")
  ViewTableFormat2 setHeaderLineCount(short lineCount);

  @StructureSetter("Length")
  ViewTableFormat2 setLength(int len);

  @StructureSetter("LineCount")
  ViewTableFormat2 setLineCount(short lineCount);

  @StructureSetter("wSig")
  ViewTableFormat2 setSignature(FormatSignature sig);

  @StructureSetter("Spacing")
  ViewTableFormat2 setSpacing(ViewLineSpacing spacing);

  @StructureSetter("V2BorderColor")
  ViewTableFormat2 setV2BorderColor(short color);
}
