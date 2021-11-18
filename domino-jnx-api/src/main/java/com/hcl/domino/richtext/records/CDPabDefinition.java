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

import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.34
 */
@StructureDefinition(
  name = "CDPABDEFINITION",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "PABID", type = short.class, unsigned = true),
    @StructureMember(name = "JustifyMode", type = CDPabDefinition.Justify.class),
    @StructureMember(name = "LineSpacing", type = short.class, unsigned = true),
    @StructureMember(name = "ParagraphSpacingBefore", type = short.class, unsigned = true),
    @StructureMember(name = "ParagraphSpacingAfter", type = short.class, unsigned = true),
    @StructureMember(name = "LeftMargin", type = short.class, unsigned = true),
    @StructureMember(name = "RightMargin", type = short.class, unsigned = true),
    @StructureMember(name = "FirstLineLeftMargin", type = short.class, unsigned = true),
    @StructureMember(name = "Tabs", type = short.class, unsigned = true),
    @StructureMember(name = "Tab", type = short[].class, length = RichTextConstants.MAXTABS),
    @StructureMember(name = "Flags", type = CDPabDefinition.Flag.class, bitfield = true),
    @StructureMember(name = "TabTypes", type = int.class),
    @StructureMember(name = "Flags2", type = CDPabDefinition.Flag2.class, bitfield = true)
  }
)
public interface CDPabDefinition extends RichTextRecord<WSIG> {
  enum Justify implements INumberEnum<Short> {
    /** flush left, ragged right */
    LEFT(NotesConstants.JUSTIFY_LEFT),
    /** flush right, ragged left */
    RIGHT(NotesConstants.JUSTIFY_RIGHT),
    /** full block justification */
    BLOCK(NotesConstants.JUSTIFY_BLOCK),
    /** centered */
    CENTER(NotesConstants.JUSTIFY_CENTER),
    /** no line wrapping AT ALL (except hard CRs) */
    NONE(NotesConstants.JUSTIFY_NONE);
    private final short value;
    private Justify(short value) {
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
  
  enum Flag implements INumberEnum<Short> {
    /** start new page with this par */
    PAGINATE_BEFORE(NotesConstants.PABFLAG_PAGINATE_BEFORE),
    /** don't separate this and next par */
    KEEP_WITH_NEXT(NotesConstants.PABFLAG_KEEP_WITH_NEXT),
    /** don't split lines in paragraph */
    KEEP_TOGETHER(NotesConstants.PABFLAG_KEEP_TOGETHER),
    /** propagate even PAGINATE_BEFORE and KEEP_WITH_NEXT */
    PROPAGATE(NotesConstants.PABFLAG_PROPAGATE),
    /** hide paragraph in R/O mode */
    HIDE_RO(NotesConstants.PABFLAG_HIDE_RO),
    /** hide paragraph in R/W mode */
    HIDE_RW(NotesConstants.PABFLAG_HIDE_RW),
    /** hide paragraph when printing */
    HIDE_PR(NotesConstants.PABFLAG_HIDE_PR),
    DISPLAY_RM(NotesConstants.PABFLAG_DISPLAY_RM),
    /** the pab was saved in V4. */
    HIDE_UNLINK(NotesConstants.PABFLAG_HIDE_UNLINK),
    /** hide paragraph when copying/forwarding */
    HIDE_CO(NotesConstants.PABFLAG_HIDE_CO),
    /** display paragraph with bullet */
    BULLET(NotesConstants.PABFLAG_BULLET),
    /**  use the hide when formula even if there is one.    */
    HIDE_IF(NotesConstants.PABFLAG_HIDE_IF),
    /** display paragraph with number */
    NUMBEREDLIST(NotesConstants.PABFLAG_NUMBEREDLIST),
    /** hide paragraph when previewing*/
    HIDE_PV(NotesConstants.PABFLAG_HIDE_PV),
    /** hide paragraph when editing in the preview pane.    */
    HIDE_PVE(NotesConstants.PABFLAG_HIDE_PVE),
    /** hide paragraph from Notes clients */
    HIDE_NOTES(NotesConstants.PABFLAG_HIDE_NOTES);
    private final short value;
    private Flag(short value) {
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

  enum Flag2 implements INumberEnum<Short> {
    HIDE_WEB(NotesConstants.PABFLAG2_HIDE_WEB),
    CHECKEDLIST(NotesConstants.PABFLAG2_CHECKEDLIST),
    /**  PAB.LeftMargin is an offset value.  */
    LM_OFFSET(NotesConstants.PABFLAG2_LM_OFFSET),
    /**  PAB.LeftMargin is a percentage value.  */
    LM_PERCENT(NotesConstants.PABFLAG2_LM_PERCENT),
    /**  PAB.LeftMargin is an offset value.  */
    FLLM_OFFSET(NotesConstants.PABFLAG2_FLLM_OFFSET),
    /**  PAB.LeftMargin is a percentage value.  */
    FLLM_PERCENT(NotesConstants.PABFLAG2_FLLM_PERCENT),
    /**  PAB.RightMargin is an offset value.    */
    RM_OFFSET(NotesConstants.PABFLAG2_RM_OFFSET),
    /**  PAB.RightMargin is a percentage value.    */
    RM_PERCENT(NotesConstants.PABFLAG2_RM_PERCENT),
    /**  If to use default value instead of PAB.LeftMargin.  */
    LM_DEFAULT(NotesConstants.PABFLAG2_LM_DEFAULT),
    /**  If to use default value instead of PAB.FirstLineLeftMargin.  */
    FLLM_DEFAULT(NotesConstants.PABFLAG2_FLLM_DEFAULT),
    /**  If to use default value instead of PAB.RightMargin.  */
    RM_DEFAULT(NotesConstants.PABFLAG2_RM_DEFAULT),
    CIRCLELIST(NotesConstants.PABFLAG2_CIRCLELIST),
    SQUARELIST(NotesConstants.PABFLAG2_SQUARELIST),
    UNCHECKEDLIST(NotesConstants.PABFLAG2_UNCHECKEDLIST),
    /**  set if right to left reading order  */
    BIDI_RTLREADING(NotesConstants.PABFLAG2_BIDI_RTLREADING),
    /**  TRUE if Pab needs to Read more Flags */
    MORE_FLAGS(NotesConstants.PABFLAG2_MORE_FLAGS);
    private final short value;
    private Flag2(short value) {
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
  
  enum Flag3 implements INumberEnum<Integer> {
    /** True, if Hide when embedded */
    HIDE_EE(NotesConstants.PABFLAG3_HIDE_EE),
    /** True, if hidden from mobile clients */
    HIDE_MOBILE(NotesConstants.PABFLAG3_HIDE_MOBILE),
    /** True if boxes in a layer have set PABFLAG_DISPLAY_RM on pabs */
    LAYER_USES_DRM(NotesConstants.PABFLAG3_LAYER_USES_DRM);
    
    private final int value;
    private Flag3(int value) {
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
  
  @StructureGetter("PABID")
  int getPabId();
  
  @StructureSetter("PABID")
  CDPabDefinition setPabId(int id);
  
  @StructureGetter("JustifyMode")
  Justify getJustifyMode();
  
  @StructureSetter("JustifyMode")
  CDPabDefinition setJustifyMode(Justify mode);
  
  @StructureGetter("LineSpacing")
  int getLineSpacing();
  
  @StructureSetter("LineSpacing")
  CDPabDefinition setLineSpacing(int spacing);
  
  @StructureGetter("ParagraphSpacingBefore")
  int getParagraphSpacingBefore();
  
  @StructureSetter("ParagraphSpacingBefore")
  CDPabDefinition setParagraphSpacingBefore(int spacing);
  
  @StructureGetter("ParagraphSpacingAfter")
  int getParagraphSpacingAfter();
  
  @StructureSetter("ParagraphSpacingAfter")
  CDPabDefinition setParagraphSpacingAfter(int spacing);
  
  @StructureGetter("LeftMargin")
  int getLeftMargin();
  
  @StructureSetter("LeftMargin")
  CDPabDefinition setLeftMargin(int margin);
  
  @StructureGetter("RightMargin")
  int getRightMargin();
  
  @StructureSetter("RightMargin")
  CDPabDefinition setRightMargin(int margin);
  
  @StructureGetter("FirstLineLeftMargin")
  int getFirstLineLeftMargin();
  
  @StructureSetter("FirstLineLeftMargin")
  CDPabDefinition setFirstLineLeftMargin(int margin);
  
  @StructureGetter("Tabs")
  int getTabStopCount();
  
  @StructureSetter("Tabs")
  CDPabDefinition setTabStopCount(int count);
  
  @StructureGetter("Tab")
  short[] getTabStops();
  
  @StructureSetter("Tab")
  CDPabDefinition setTabStops(short[] stops);
  
  @StructureGetter("Flags")
  Set<Flag> getFlags();
  
  @StructureSetter("Flags")
  CDPabDefinition setFlags(Collection<Flag> flags);
  
  @StructureGetter("TabTypes")
  int getTabTypesRaw();
  
  @StructureSetter("TabTypes")
  CDPabDefinition setTabTypesRaw(int tabTypes);
  
  @StructureGetter("Flags2")
  Set<Flag2> getFlags2();
  
  @StructureSetter("Flags2")
  CDPabDefinition setFlags2Raw(short val);
  
  default CDPabDefinition setFlags2(Collection<Flag2> flags) {
    if (flags.contains(CDPabDefinition.Flag2.MORE_FLAGS)) {
      //make sure we have space for the 6 margin WORDs followed by two DWORDs
      //(first contains EXTENDEDPABFLAGS3, next contains R6 flags like PABFLAG3_HIDE_EE)
      ByteBuffer varData = getVariableData();
      if (varData.capacity()<16) {
        resizeVariableData(16);
        varData = getVariableData();
      }
    }
    setFlags2Raw(DominoEnumUtil.toBitField(Flag2.class, flags));
    return this;
  }
  
//  As of R5, a CDPABDEFINITION structure can be followed by an array of six WORDs of margin values:
//    Margin [0]  - Left margin offset in twips
//    Margin [1]  - Left margin offset in percent (0 - 100)
//    Margin [2]  - First line left margin offset in twips
//    Margin [3]  - First line left margin offset in percent (0 - 100)
//    Margin [4]  - Right margin offset in twips
//    Margin [5]  - Right margin offset in percent (0 - 100)

  /**
   * Checks if this record contains extended margin values that have been introduced in R5
   * 
   * @return true if we have data for R5 or later
   * @see #setLeftMarginOffsetInTWIPS(int)
   * @see #setLeftMarginOffsetInPercent(int)
   * @see #setFirstLineLeftMarginOffsetInTwips(int)
   * @see #setFirstLineMarginOffsetInPercent(int)
   * @see #setRightMarginOffsetInTwips(int)
   * @see #setRightMarginOffsetInPercent(int)
   */
  default boolean isR5OrNewer() {
    int varLength = getVariableData().capacity();
    if (varLength >= 12) {
      return true;
    }
    return false;
  }

  /**
   * Returns the left margin offset in twips (R6+)
   * 
   * @return margin
   */
  default Optional<Integer> getLeftMarginOffsetInTWIPS() {
    ByteBuffer varData = getVariableData();
    if (varData.capacity()>=12) {
      short val = varData.getShort(0);
      return Optional.of(Short.toUnsignedInt(val));
    }
    return Optional.empty();
  }
  
  /**
   * Sets the left margin offset in twips (R6+)
   * 
   * @param val new value
   * @return this record
   */
  default CDPabDefinition setLeftMarginOffsetInTWIPS(int val) {
    if (val<0 || val>65535) {
      throw new IllegalArgumentException(MessageFormat.format("Value {0} exceeds allowed range (0-65535)", val));
    }
    ByteBuffer varData = getVariableData();
    if (varData.capacity()<12) {
      resizeVariableData(12);
      varData = getVariableData();
    }
    varData.position(0);
    varData.putShort((short) (val & 0xffff));
    return this;
  }
  
  /**
   * Returns the left margin offset in percent (0 - 100)
   * 
   * @return margin
   */
  default Optional<Integer> getLeftMarginOffsetInPercent() {
    ByteBuffer varData = getVariableData();
    if (varData.capacity()>=12) {
      short val = varData.getShort(1);
      return Optional.of(Short.toUnsignedInt(val));
    }
    return Optional.empty();
  }
  
  /**
   * Sets the left margin offset in percent (0 - 100)
   * 
   * @param val new value
   * @return this record
   */
  default CDPabDefinition setLeftMarginOffsetInPercent(int val) {
    if (val<0 || val>100) {
      throw new IllegalArgumentException(MessageFormat.format("Value {0} exceeds allowed range (0-100)", val));
    }
    ByteBuffer varData = getVariableData();
    if (varData.capacity()<12) {
      resizeVariableData(12);
      varData = getVariableData();
    }
    varData.position(1*2);
    varData.putShort((short) (val & 0xffff));
    return this;
  }
  
  /**
   * Returns the first line left margin offset in twips
   * 
   * @return margin
   */
  default Optional<Integer> getFirstLineLeftMarginOffsetInTwips() {
    ByteBuffer varData = getVariableData();
    if (varData.capacity()>=12) {
      short val = varData.getShort(2);
      return Optional.of(Short.toUnsignedInt(val));
    }
    return Optional.empty();
  }
  
  /**
   * Sets the first line left margin offset in twips
   * 
   * @param val new value
   * @return this record
   */
  default CDPabDefinition setFirstLineLeftMarginOffsetInTwips(int val) {
    if (val<0 || val>65535) {
      throw new IllegalArgumentException(MessageFormat.format("Value {0} exceeds allowed range (0-65535)", val));
    }
    ByteBuffer varData = getVariableData();
    if (varData.capacity()<12) {
      resizeVariableData(12);
      varData = getVariableData();
    }
    varData.position(2*2);
    varData.putShort((short) (val & 0xffff));
    return this;
  }
  
  /**
   * Returns the first line left margin offset in percent (0 - 100)
   * 
   * @return margin
   */
  default Optional<Integer> getFirstLineMarginOffsetInPercent() {
    ByteBuffer varData = getVariableData();
    if (varData.capacity()>=12) {
      short val = varData.getShort(3);
      return Optional.of(Short.toUnsignedInt(val));
    }
    return Optional.empty();
  }
  
  /**
   * Sets the first line left margin offset in percent (0 - 100)
   * 
   * @param val new value
   * @return this record
   */
  default CDPabDefinition setFirstLineMarginOffsetInPercent(int val) {
    if (val<0 || val>100) {
      throw new IllegalArgumentException(MessageFormat.format("Value {0} exceeds allowed range (0-100)", val));
    }
    ByteBuffer varData = getVariableData();
    if (varData.capacity()<12) {
      resizeVariableData(12);
      varData = getVariableData();
    }
    varData.position(3*2);
    varData.putShort((short) (val & 0xffff));
    return this;
  }
  
  /**
   * Returns the right margin offset in twips
   * 
   * @return margin
   */
  default Optional<Integer> getRightMarginOffsetInTwips() {
    ByteBuffer varData = getVariableData();
    if (varData.capacity()>=12) {
      short val = varData.getShort(4);
      return Optional.of(Short.toUnsignedInt(val));
    }
    return Optional.empty();
  }
  
  /**
   * Sets the right margin offset in twips
   * 
   * @param val new value
   * @return this record
   */
  default CDPabDefinition setRightMarginOffsetInTwips(int val) {
    if (val<0 || val>65535) {
      throw new IllegalArgumentException(MessageFormat.format("Value {0} exceeds allowed range (0-65535)", val));
    }
    ByteBuffer varData = getVariableData();
    if (varData.capacity()<12) {
      resizeVariableData(12);
      varData = getVariableData();
    }
    varData.position(4*2);
    varData.putShort((short) (val & 0xffff));
    return this;
  }
  
  /**
   * Returns the right margin offset in percent (0 - 100)
   * 
   * @return margin
   */
  default Optional<Integer> getRightMarginOffsetInPercent() {
    ByteBuffer varData = getVariableData();
    if (varData.capacity()>=12) {
      short val = varData.getShort(5);
      return Optional.of(Short.toUnsignedInt(val));
    }
    return Optional.empty();
  }
  
  /**
   * Sets the right margin offset in percent (0 - 100)
   * 
   * @param val new value
   * @return this record
   */
  default CDPabDefinition setRightMarginOffsetInPercent(int val) {
    if (val<0 || val>100) {
      throw new IllegalArgumentException(MessageFormat.format("Value {0} exceeds allowed range (0-100)", val));
    }
    ByteBuffer varData = getVariableData();
    if (varData.capacity()<12) {
      resizeVariableData(12);
      varData = getVariableData();
    }
    varData.position(5*2);
    varData.putShort((short) (val & 0xffff));
    return this;
  }
  
  /**
   * Checks if this paragraph definition contains R6 specific flags
   * 
   * @return true if we have data for R6 or later
   * @see #getFlags3()
   */
  default boolean isR6OrNewer() {
    Set<Flag2> flags2 = getFlags2();
    
    if (flags2.contains(Flag2.MORE_FLAGS)) {
      ByteBuffer varData = getVariableData();
      if (varData.capacity()>=20) {
        varData.position(12);
        int whatFollows = varData.getInt();
        if ((whatFollows & NotesConstants.EXTENDEDPABFLAGS3) == NotesConstants.EXTENDEDPABFLAGS3) {
          //we have a following DWORD with R6 flags
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * Returns R6 specific flags (if set)
   * 
   * @return flags
   */
  default Set<Flag3> getFlags3() {
    Set<Flag2> flags2 = getFlags2();
    
    if (flags2.contains(Flag2.MORE_FLAGS)) {
      ByteBuffer varData = getVariableData();
      if (varData.capacity()>=20) { // 6 WORDs + 2 DWORDs
        varData.position(12);
        int whatFollows = varData.getInt();
        if ((whatFollows & NotesConstants.EXTENDEDPABFLAGS3) == NotesConstants.EXTENDEDPABFLAGS3) {
          //we have a following DWORD with R6 flags
          int flags3 = varData.getInt();
          return DominoEnumUtil.valuesOf(Flag3.class, flags3);
        }
      }
    }
    
    return Collections.emptySet();
  }
  
  /**
   * Sets R6 specific flags
   * 
   * @param flags flags
   * @return this record
   */
  default CDPabDefinition setFlags3(Collection<Flag3> flags) {
    Set<Flag2> flags2 = getFlags2();
    if (!flags2.contains(Flag2.MORE_FLAGS)) {
      flags2.add(Flag2.MORE_FLAGS);
      setFlags2(flags2);
    }
    
    ByteBuffer varData = getVariableData();
    if (varData.capacity()<20) {
      resizeVariableData(20);
      varData = getVariableData();
    }
    //skip 6 margin WORDs
    varData.position(12);
    
    //make sure the following DWORD at least contains the flag EXTENDEDPABFLAGS3 (may be extended later)
    int whatFollows = varData.getInt();
    if ((whatFollows & NotesConstants.EXTENDEDPABFLAGS3) != NotesConstants.EXTENDEDPABFLAGS3) {
      whatFollows |= NotesConstants.EXTENDEDPABFLAGS3;
      varData.position(12);
      varData.putInt(whatFollows);
    }
    
    int flags3Bitfield = DominoEnumUtil.toBitField(Flag3.class, flags);
    varData.putInt(flags3Bitfield);
    
    return this;
  }
}
