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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.ViewFormatConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.MemoryStructure;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;

@StructureDefinition(name = "VIEW_TABLE_FORMAT3", members = {
    @StructureMember(name = "Length", type = short.class, unsigned = true),
    @StructureMember(name = "Flags", type = ViewTableFormat3.Flag.class, bitfield = true),
    @StructureMember(name = "BackgroundColor", type = ColorValue.class),
    @StructureMember(name = "AlternateBackgroundColor", type = ColorValue.class),
    @StructureMember(name = "GridColorValue", type = ColorValue.class),
    @StructureMember(name = "wViewMarginTop", type = short.class, unsigned = true),
    @StructureMember(name = "wViewMarginLeft", type = short.class, unsigned = true),
    @StructureMember(name = "wViewMarginRight", type = short.class, unsigned = true),
    @StructureMember(name = "wViewMarginBottom", type = short.class, unsigned = true),
    @StructureMember(name = "MarginBackgroundColor", type = ColorValue.class),
    @StructureMember(name = "HeaderBackgroundColor", type = ColorValue.class),
    @StructureMember(name = "wViewMarginTopUnder", type = short.class, unsigned = true),
    @StructureMember(name = "UnreadColor", type = ColorValue.class),
    @StructureMember(name = "TotalsColor", type = ColorValue.class),
    @StructureMember(name = "wMaxRows", type = short.class, unsigned = true),
    @StructureMember(name = "wThemeSetting", type = short.class),
    @StructureMember(name = "dwReserved", type = int[].class, length = 1),
})
public interface ViewTableFormat3 extends MemoryStructure {
  public static ViewTableFormat3 newInstanceWithDefaults() {
    ViewTableFormat3 format3 = MemoryStructureWrapperService.get().newStructure(ViewTableFormat3.class, 0);
    
    format3
    .getHeaderBackgroundColor()
    .setBlue((short) 255)
    .setRed((short) 255)
    .setGreen((short) 255)
    .setFlags(Arrays.asList(ColorValue.Flag.ISRGB));
    
    format3
    .getAlternateBackgroundColor()
    .setBlue((short) 255)
    .setRed((short) 255)
    .setGreen((short) 255)
    .setFlags(Arrays.asList(ColorValue.Flag.ISRGB, ColorValue.Flag.NOCOLOR));
    
    format3
    .getMarginBackgroundColor()
    .setBlue((short) 255)
    .setRed((short) 255)
    .setGreen((short) 255)
    .setFlags(Arrays.asList(ColorValue.Flag.ISRGB));
    
    format3
    .getUnreadColor()
    .setBlue((short) 0)
    .setRed((short) 0)
    .setGreen((short) 0)
    .setFlags(Arrays.asList(ColorValue.Flag.ISRGB));
    
    format3.setMaxRows(1);
    
    format3
    .getBackgroundColor()
    .setBlue((short) 255)
    .setRed((short) 255)
    .setGreen((short) 255)
    .setFlags(Arrays.asList(ColorValue.Flag.ISRGB));
    
    format3
    .getTotalsColor()
    .setBlue((short) 0)
    .setRed((short) 0)
    .setGreen((short) 0)
    .setFlags(Arrays.asList(ColorValue.Flag.ISRGB));
    
    format3
    .getGridColor()
    .setBlue((short) 0)
    .setRed((short) 0)
    .setGreen((short) 0)
    .setFlags(Arrays.asList(ColorValue.Flag.ISRGB));
    
    return format3;
  }
  
  enum Flag implements INumberEnum<Integer> {
    /**  */
    GridStyleSolid(ViewFormatConstants.VTF3_M_GridStyleSolid),
    GridStyleDash(ViewFormatConstants.VTF3_M_GridStyleDash),
    GridStyleDot(ViewFormatConstants.VTF3_M_GridStyleDot),
    GridStyleDashDot(ViewFormatConstants.VTF3_M_GridStyleDashDot),
    AllowCustomizations(ViewFormatConstants.VTF3_M_AllowCustomizations),
    EvaluateActionsHideWhen(ViewFormatConstants.VTF3_M_EvaluateActionsHideWhen),
    /** V6 - Hide border after left margin */
    HideLeftMarginBorder(ViewFormatConstants.VTF3_M_HideLeftMarginBorder),
    /** V6 - bold the unread rows. */
    BoldUnreadRows(ViewFormatConstants.VTF3_M_BoldUnreadRows),
    /** V6 - inviewedit-newdocs in view */
    AllowCreateNewDoc(ViewFormatConstants.VTF3_M_AllowCreateNewDoc),
    /** V6 - View has background image. */
    HasBackgroundImage(ViewFormatConstants.VTF3_M_HasBackgroundImage),
    /** V7 - Limit the max rows returned for a Query View */
    MaxRowsLimit(ViewFormatConstants.VTF3_M_MaxRowsLimit),
    /** Hannover */
    ShowVerticalHorizontalSwitcher(ViewFormatConstants.VTF3_M_ShowVerticalHorizontalSwitcher),
    /** Hannover Java Views */
    ShowTabNavigator(ViewFormatConstants.VTF3_M_ShowTabNavigator),
    /** Hannover Java Views */
    AllowThreadGathering(ViewFormatConstants.VTF3_M_AllowThreadGathering),
    /** Hannover Java Views */
    DisableHideJavaView(ViewFormatConstants.VTF3_M_DisableHideJaveView),
    /** Hannover */
    HideColumnHeader(ViewFormatConstants.VTF3_M_HideColumnHeader);

    private final int value;

    Flag(final int value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Integer getValue() {
      return this.value;
    }
  }

  @StructureGetter("AlternateBackgroundColor")
  ColorValue getAlternateBackgroundColor();

  @StructureGetter("BackgroundColor")
  ColorValue getBackgroundColor();

  @StructureGetter("Flags")
  Set<Flag> getFlags();

  default ViewTableFormat3 setFlag(Flag flag, boolean b) {
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
  
  @StructureGetter("GridColorValue")
  ColorValue getGridColor();

  @StructureGetter("HeaderBackgroundColor")
  ColorValue getHeaderBackgroundColor();

  @StructureGetter("Length")
  int getLength();

  @StructureGetter("MarginBackgroundColor")
  ColorValue getMarginBackgroundColor();

  @StructureGetter("wMaxRows")
  int getMaxRows();

  @StructureGetter("TotalsColor")
  ColorValue getTotalsColor();

  @StructureGetter("UnreadColor")
  ColorValue getUnreadColor();

  @StructureGetter("wViewMarginBottom")
  int getViewMarginBottom();

  @StructureGetter("wViewMarginLeft")
  int getViewMarginLeft();

  @StructureGetter("wViewMarginRight")
  int getViewMarginRight();

  @StructureGetter("wViewMarginTop")
  int getViewMarginTop();

  @StructureGetter("wViewMarginTopUnder")
  int getViewMarginTopUnder();

  @StructureSetter("Flags")
  ViewTableFormat3 setFlags(Collection<Flag> flags);

  @StructureSetter("Length")
  ViewTableFormat3 setLength(int len);

  @StructureSetter("wMaxRows")
  ViewTableFormat3 setMaxRows(int margin);

  @StructureSetter("wViewMarginBottom")
  ViewTableFormat3 setViewMarginBottom(int margin);

  @StructureSetter("wViewMarginLeft")
  ViewTableFormat3 setViewMarginLeft(int margin);

  @StructureSetter("wViewMarginRight")
  ViewTableFormat3 setViewMarginRight(int margin);

  @StructureSetter("wViewMarginTop")
  ViewTableFormat3 setViewMarginTop(int margin);

  @StructureSetter("wViewMarginTopUnder")
  ViewTableFormat3 setViewMarginTopUnder(int margin);
  
  @StructureGetter("wThemeSetting")
  short getThemeSetting();
  
  @StructureSetter("wThemeSetting")
  ViewTableFormat3 setThemeSetting(short themeSetting);
}
