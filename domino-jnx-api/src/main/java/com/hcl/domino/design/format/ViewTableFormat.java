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

import com.hcl.domino.design.format.ViewFormatHeader.Version;
import com.hcl.domino.design.format.ViewFormatHeader.ViewStyle;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.ViewFormatConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.MemoryStructure;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;

@StructureDefinition(name = "VIEW_TABLE_FORMAT", members = {
    @StructureMember(name = "Header", type = ViewFormatHeader.class),
    @StructureMember(name = "Columns", type = short.class, unsigned = true),
    @StructureMember(name = "ItemSequenceNumber", type = short.class, unsigned = true),
    @StructureMember(name = "Flags", type = ViewTableFormat.Flag.class, bitfield = true),
    @StructureMember(name = "Flags2", type = ViewTableFormat.Flag2.class, bitfield = true),
})
public interface ViewTableFormat extends MemoryStructure {
  public static ViewTableFormat newInstanceWithDefaults() {
    ViewTableFormat format = MemoryStructureWrapperService.get().newStructure(ViewTableFormat.class, 0);
    format
    .getHeader()
    .setVersion(Version.VERSION1)
    .setStyle(ViewStyle.TABLE);
    format.setItemSequenceNumber(1);
    format.setFlag(Flag.CONFLICT, true);
    return format;
  }
  
  enum Flag implements INumberEnum<Short> {
    COLLAPSED(ViewFormatConstants.VIEW_TABLE_FLAG_COLLAPSED),
    FLATINDEX(ViewFormatConstants.VIEW_TABLE_FLAG_FLATINDEX),
    DISP_ALLUNREAD(ViewFormatConstants.VIEW_TABLE_FLAG_DISP_ALLUNREAD),
    CONFLICT(ViewFormatConstants.VIEW_TABLE_FLAG_CONFLICT),
    DISP_UNREADDOCS(ViewFormatConstants.VIEW_TABLE_FLAG_DISP_UNREADDOCS),
    GOTO_TOP_ON_OPEN(ViewFormatConstants.VIEW_TABLE_GOTO_TOP_ON_OPEN),
    GOTO_BOTTOM_ON_OPEN(ViewFormatConstants.VIEW_TABLE_GOTO_BOTTOM_ON_OPEN),
    ALTERNATE_ROW_COLORING(ViewFormatConstants.VIEW_TABLE_ALTERNATE_ROW_COLORING),
    HIDE_HEADINGS(ViewFormatConstants.VIEW_TABLE_HIDE_HEADINGS),
    HIDE_LEFT_MARGIN(ViewFormatConstants.VIEW_TABLE_HIDE_LEFT_MARGIN),
    SIMPLE_HEADINGS(ViewFormatConstants.VIEW_TABLE_SIMPLE_HEADINGS),
    VARIABLE_LINE_COUNT(ViewFormatConstants.VIEW_TABLE_VARIABLE_LINE_COUNT),
    GOTO_TOP_ON_REFRESH(ViewFormatConstants.VIEW_TABLE_GOTO_TOP_ON_REFRESH),
    GOTO_BOTTOM_ON_REFRESH(ViewFormatConstants.VIEW_TABLE_GOTO_BOTTOM_ON_REFRESH),
    EXTEND_LAST_COLUMN(ViewFormatConstants.VIEW_TABLE_EXTEND_LAST_COLUMN),
    RTLVIEW(ViewFormatConstants.VIEW_TABLE_RTLVIEW);

    private final short value;

    Flag(final short value) {
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

  enum Flag2 implements INumberEnum<Short> {
    FLAT_HEADINGS(ViewFormatConstants.VIEW_TABLE_FLAT_HEADINGS),
    COLORIZE_ICONS(ViewFormatConstants.VIEW_TABLE_COLORIZE_ICONS),
    HIDE_SB(ViewFormatConstants.VIEW_TABLE_HIDE_SB),
    HIDE_CAL_HEADER(ViewFormatConstants.VIEW_TABLE_HIDE_CAL_HEADER),
    NOT_CUSTOMIZED(ViewFormatConstants.VIEW_TABLE_NOT_CUSTOMIZED),
    SHOW_PARTIAL_THREADS(ViewFormatConstants.VIEW_TABLE_SHOW_PARITAL_THREADS),
    PARTIAL_FLATINDEX(ViewFormatConstants.VIEW_TABLE_FLAG_PARTIAL_FLATINDEX),
    NARROW_VIEW(ViewFormatConstants.VIEW_TABLE_NARROW_VIEW),
    SHOW_ABSTRACT(ViewFormatConstants.VIEW_TABLE_SHOW_ABSTRACT);

    private final short value;

    Flag2(final short value) {
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

  @StructureGetter("Columns")
  int getColumnCount();

  @StructureGetter("Flags")
  Set<Flag> getFlags();

  default ViewTableFormat setFlag(Flag flag, boolean b) {
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
            .filter(currFlag -> !flag.equals(currFlag))
            .collect(Collectors.toSet());
        setFlags(newFlags);
      }
    }
    return this;
  }
  
  @StructureGetter("Flags2")
  Set<Flag2> getFlags2();

  default ViewTableFormat setFlag(Flag2 flag, boolean b) {
    Set<Flag2> oldFlags = getFlags2();
    if (b) {
      if (!oldFlags.contains(flag)) {
        Set<Flag2> newFlags = new HashSet<>(oldFlags);
        newFlags.add(flag);
        setFlags2(newFlags);
      }
    }
    else {
      if (oldFlags.contains(flag)) {
        Set<Flag2> newFlags = oldFlags
            .stream()
            .filter(currFlag -> !flag.equals(currFlag))
            .collect(Collectors.toSet());
        setFlags2(newFlags);
      }
    }
    return this;
  }
  
  @StructureGetter("Header")
  ViewFormatHeader getHeader();

  /**
   * Returns the sequence number for unique item names
   * 
   * @return number
   */
  @StructureGetter("ItemSequenceNumber")
  int getItemSequenceNumber();

  @StructureSetter("Columns")
  ViewTableFormat setColumnCount(int count);

  @StructureSetter("Flags")
  ViewTableFormat setFlags(Collection<Flag> flags);

  @StructureSetter("Flags2")
  ViewTableFormat setFlags2(Collection<Flag2> flags);

  @StructureSetter("ItemSequenceNumber")
  ViewTableFormat setItemSequenceNumber(int seqNum);
}
