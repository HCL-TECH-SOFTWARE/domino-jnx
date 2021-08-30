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
    /**  TRUE if Pab needs to Read more Flafs */
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
  CDPabDefinition setFlags2(Collection<Flag2> flags);
}
