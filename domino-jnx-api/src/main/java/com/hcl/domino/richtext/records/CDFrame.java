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
package com.hcl.domino.richtext.records;

import java.util.Collection;
import java.util.Set;

import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.frameset.FrameScrollStyle;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.34
 */
@StructureDefinition(
  name = "CDFRAME",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Flags", type = CDFrame.Flag.class, bitfield = true),
    @StructureMember(name = "DataFlags", type = CDFrame.DataFlag.class, bitfield = true),
    @StructureMember(name = "BorderEnable", type = byte.class),
    @StructureMember(name = "NoResize", type = byte.class),
    @StructureMember(name = "ScrollBarStyle", type = FrameScrollStyle.class),
    @StructureMember(name = "MarginWidth", type = short.class, unsigned = true),
    @StructureMember(name = "MarginHeight", type = short.class, unsigned = true),
    @StructureMember(name = "dwReserved", type = int.class),
    @StructureMember(name = "FrameNameLength", type = short.class, unsigned = true),
    @StructureMember(name = "Reserved1", type = short.class),
    @StructureMember(name = "FrameTargetLength", type = short.class, unsigned = true),
    @StructureMember(name = "FrameBorderColor", type = ColorValue.class),
    @StructureMember(name = "wReserved", type = short.class),
  }
)
public interface CDFrame extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Integer> {
    /**  Set if BorderEnable is specified  */
    BorderEnable(DesignConstants.fFRBorderEnable),
    /**  Set if MarginWidth is specified  */
    MarginWidth(DesignConstants.fFRMarginWidth),
    /**  Set if MarginHeight is specified  */
    MarginHeight(DesignConstants.fFRMarginHeight),
    /**  Set if FrameBorderColor is specified  */
    FrameBorderColor(DesignConstants.fFRFrameBorderColor),
    /**  Set if ScrollBarStyle is specified  */
    Scrolling(DesignConstants.fFRScrolling),
    /**  Set if this frame has a notes only border */
    NotesOnlyBorder(DesignConstants.fFRNotesOnlyBorder),
    /**  Set if this frame wants arrows shown in Notes */
    NotesOnlyArrows(DesignConstants.fFRNotesOnlyArrows),
    /**  Open value specified for Border caption is in percent. */
    NotesOpenPercent(DesignConstants.fFRNotesOpenPercent),
    /**  if set, set initial focus to this frame  */
    NotesInitialFocus(DesignConstants.fFRNotesInitialFocus),
    /**  Set if this fram caption reading order is Right-To-Left */
    NotesReadingOrder(DesignConstants.fFRNotesReadingOrder);
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
  enum DataFlag implements INumberEnum<Short> {
    NotesBorder(DesignConstants.fFRNotesBorder),
    NotesBorderFontAndColor(DesignConstants.fFRNotesBorderFontAndColor),
    NotesBorderCaption(DesignConstants.fFRNotesBorderCaption),
    NotesCaptionFontName(DesignConstants.fFRNotesCaptionFontName),
    /**  set this if frame has a sequence set other than the default 0  */
    Sequence(DesignConstants.fFRSequence),;
    private final short value;
    private DataFlag(short value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return value;
    }

    @Override
    public Short getValue() {
      return value;
    }
  }
  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("Flags")
  Set<Flag> getFlags();
  
  @StructureSetter("Flags")
  CDFrame setFlags(Collection<Flag> flags);
  
  @StructureGetter("DataFlags")
  Set<DataFlag> getDataFlags();
  
  @StructureSetter("DataFlags")
  CDFrame setDataFlags(Collection<DataFlag> flags);
  
  @StructureGetter("BorderEnable")
  byte getBorderEnable();
  
  @StructureSetter("BorderEnable")
  CDFrame setBorderEnable(byte borderEnable);
  
  @StructureGetter("NoResize")
  byte getNoResize();
  
  @StructureSetter("NoResize")
  CDFrame setNoResize(byte noResize);
  
  @StructureGetter("ScrollBarStyle")
  FrameScrollStyle getScrollBarStyle();
  
  @StructureSetter("ScrollBarStyle")
  CDFrame setScrollBarStyle(FrameScrollStyle style);
  
  @StructureGetter("MarginWidth")
  int getMarginWidth();
  
  @StructureSetter("MarginWidth")
  CDFrame setMarginWidth(int width);
  
  @StructureGetter("MarginHeight")
  int getMarginHeight();
  
  @StructureSetter("MarginHeight")
  CDFrame setMarginHeight(int height);

  @StructureGetter("FrameNameLength")
  int getFrameNameLength();
  
  @StructureSetter("FrameNameLength")
  CDFrame setFrameNameLength(int len);

  @StructureGetter("FrameTargetLength")
  int getFrameTargetLength();
  
  @StructureSetter("FrameTargetLength")
  CDFrame setFrameTargetLength(int len);
  
  @StructureGetter("FrameBorderColor")
  ColorValue getFrameBorderColor();
  
  default String getFrameName() {
    return StructureSupport.extractStringValue(
      this,
      0,
      getFrameNameLength()
    );
  }
  
  default CDFrame setFrameName(String name) {
    return StructureSupport.writeStringValue(
      this,
      0,
      getFrameNameLength(),
      name,
      this::setFrameNameLength
    );
  }
  
  default String getFrameTarget() {
    return StructureSupport.extractStringValue(
      this,
      getFrameNameLength(),
      getFrameTargetLength()
    );
  }
  
  default CDFrame setFrameTarget(String target) {
    return StructureSupport.writeStringValue(
      this,
      getFrameNameLength(),
      getFrameTargetLength(),
      target,
      this::setFrameTargetLength
    );
  }
  
  // TODO add support for data from DataFlags
}
