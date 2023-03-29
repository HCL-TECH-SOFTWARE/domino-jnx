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

import java.util.Optional;

import com.hcl.domino.data.StandardColors;
import com.hcl.domino.design.DesignType;
import com.hcl.domino.design.navigator.NavigatorLineStyle;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.NOTELINK;
import com.hcl.domino.richtext.structures.WSIG;

@StructureDefinition(
  name = "VIEWMAP_ACTION_RECORD", 
  members = { 
    @StructureMember(name = "Header", type = WSIG.class), 
    @StructureMember(name = "bHighlightTouch", type = short.class, unsigned = true), 
    @StructureMember(name = "bHighlightCurrent", type = short.class, unsigned = true), 
    @StructureMember(name = "HLOutlineColor", type = short.class, unsigned = true), 
    @StructureMember(name = "HLFillColor", type = short.class, unsigned = true), 
    @StructureMember(name = "ClickAction", type = ViewmapActionRecord.Action.class), 
    @StructureMember(name = "ActionStringLen", type = short.class, unsigned = true), 
    @StructureMember(name = "HLOutlineWidth", type = short.class, unsigned = true), 
    @StructureMember(name = "HLOutlineStyle", type = NavigatorLineStyle.class), 
    @StructureMember(name = "LinkInfo", type = NOTELINK.class), 
    @StructureMember(name = "ExtDataLen", type = short.class, unsigned = true), /* length of extended action data, e.g. compiled script */
    @StructureMember(name = "ActionDataDesignType", type = DesignType.class),
    @StructureMember(name = "spare", type = int[].class, length = 2), /* reserved for future use */
    /* Followed by the Action Name string */
})
public interface ViewmapActionRecord extends RichTextRecord<WSIG> {
  enum Action implements INumberEnum<Short> {
    NONE(NotesConstants.VM_ACTION_NONE),
    SWITCHVIEW(NotesConstants.VM_ACTION_SWITCHVIEW),
    SWITCHNAV(NotesConstants.VM_ACTION_SWITCHNAV),
    ALIAS_FOLDER(NotesConstants.VM_ACTION_ALIAS_FOLDER),
    GOTO_LINK(NotesConstants.VM_ACTION_GOTO_LINK),
    RUNSCRIPT(NotesConstants.VM_ACTION_RUNSCRIPT),
    RUNFORMULA(NotesConstants.VM_ACTION_RUNFORMULA),
    GOTO_URL(NotesConstants.VM_ACTION_GOTO_URL);
    private final short value;
    
    private Action(short value) {
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

  @StructureGetter("bHighlightTouch")
  boolean isHighlightTouch();

  @StructureGetter("bHighlightCurrent")
  boolean isHighlightCurrent();

  @StructureGetter("HLOutlineColor")
  int getHighlightOutlineColorRaw();
  
  @StructureGetter("HLOutlineColor")
  Optional<StandardColors> getHighlightOutlineColor();

  @StructureGetter("HLFillColor")
  int getHighlightFillColorRaw();
  
  @StructureGetter("HLFillColor")
  Optional<StandardColors> getHighlightFillColor();

  @StructureGetter("ClickAction")
  Optional<Action> getClickAction();

  /**
   * Retrieves the click action as a raw {@code short}.
   * 
   * @return the click action as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("ClickAction")
  short getClickActionRaw();

  @StructureGetter("ActionStringLen")
  int getActionStringLen();

  @StructureGetter("HLOutlineWidth")
  int getHighlightOutlineWidth();

  @StructureGetter("HLOutlineStyle")
  Optional<NavigatorLineStyle> getHighlightOutlineStyle();

  /**
   * Retrieves the highlight outline style as a raw {@code short}.
   * 
   * @return the highlight outline style as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("HLOutlineStyle")
  short getHighlightOutlineStyleRaw();

  @StructureGetter("LinkInfo")
  NOTELINK getLinkInfo();

  @StructureGetter("ExtDataLen")
  int getExtDataLen();

  @StructureGetter("ActionDataDesignType")
  Optional<DesignType> getActionDataDesignType();

  /**
   * Retrieves the action data type as a raw {@code short}.
   * 
   * @return the action data type as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("ActionDataDesignType")
  short getActionDataDesignTypeRaw();

  @StructureSetter("bHighlightTouch")
  ViewmapActionRecord setHighlightTouch(boolean highlightTouch);

  @StructureSetter("bHighlightCurrent")
  ViewmapActionRecord setHighlightCurrent(boolean highlightCurrent);

  @StructureSetter("HLOutlineColor")
  ViewmapActionRecord setOutlineColorRaw(int hLOutlineColor);
  
  @StructureSetter("HLOutlineColor")
  ViewmapActionRecord setHighlightOutlineColor(StandardColors outlineColor);

  @StructureSetter("HLFillColor")
  ViewmapActionRecord setHighlightFillColorRaw(int hLFillColor);
  
  @StructureSetter("HLFillColor")
  ViewmapActionRecord setHighlightFillColor(StandardColors outlineColor);

  @StructureSetter("ClickAction")
  ViewmapActionRecord setClickAction(Action clickAction);

  /**
   * Sets the click action as a raw {@code short}.
   * 
   * @param clickAction the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("ClickAction")
  ViewmapActionRecord setClickActionRaw(short clickAction);

  @StructureSetter("ActionStringLen")
  ViewmapActionRecord setActionStringLen(int actionStringLen);

  @StructureSetter("HLOutlineWidth")
  ViewmapActionRecord setHighlightOutlineWidth(int hLOutlineWidth);

  @StructureSetter("HLOutlineStyle")
  ViewmapActionRecord setHighlightOutlineStyle(NavigatorLineStyle hLOutlineStyle);

  /**
   * Sets the highlight outline style as a raw {@code short}.
   * 
   * @param hLOutlineStyle the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("HLOutlineStyle")
  ViewmapActionRecord setHighlightOutlineStyleRaw(short hLOutlineStyle);

  @StructureSetter("ExtDataLen")
  ViewmapActionRecord setExtDataLen(int extDataLen);

  @StructureSetter("ActionDataDesignType")
  ViewmapActionRecord setActionDataDesignType(DesignType actionDataDesignType);

  /**
   * Sets the action data type as a raw {@code short}.
   * 
   * @param actionDataDesignType the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("ActionDataDesignType")
  ViewmapActionRecord setActionDataDesignTypeRaw(short actionDataDesignType);

  /**
   * Retrieves the action string for this record. The meaning depends on the value
   * of {@link #getClickAction()}, and may be a design-element name or LotusScript.
   * 
   * <p>If the action type is {@link Action#RUNFORMULA}, use {@link #getActionFormula()}
   * instead.</p>
   * 
   * @return an {@link Optional} describing the string value of the action, or
   *         an empty one if that is not applicable
   */
  default Optional<String> getActionString() {
    if(getClickAction().map(Action.RUNFORMULA::equals).orElse(false)) {
      return Optional.empty();
    } else {
      return Optional.of(StructureSupport.extractStringValue(
        this,
        0, // The total of all variable elements before this one
        this.getActionStringLen()  // the length of this element
      ));
    }
  }
  
  /**
   * Retrieves the action formula for this record, if{@link #getClickAction()}
   * is {@link Action#RUNFORMULA}.
   * 
   * @return an {@link Optional} describing the string formula of the action, or
   *         an empty one if that is not applicable
   * @since 1.1.2
   */
  default Optional<String> getActionFormula() {
    if(getClickAction().map(Action.RUNFORMULA::equals).orElse(false)) {
      return Optional.of(StructureSupport.extractCompiledFormula(
        this,
        0, // The total of all variable elements before this one
        this.getActionStringLen()  // the length of this element
      ));
    } else {
      return Optional.empty();
    }
  }

  default ViewmapActionRecord setActionString(final String actionString) {
    final int actionNameLen = this.getActionStringLen();
    return StructureSupport.writeStringValue(
      this,
      0, // The total of all variable elements before this one
      actionNameLen,  // the length of this element
      actionString,
      this::setActionStringLen
    );
  }
  
  /**
   * Sets the action formula for this record. This should be used when
   * {@link #getClickAction()} is {@link Action#RUNFORMULA}.
   * 
   * @param actionFormula the formula to set
   * @return this structure
   * @since 1.1.2
   */
  default ViewmapActionRecord setActionFormula(String actionFormula) {
    return StructureSupport.writeCompiledFormula(
      this,
      0,
      this.getActionStringLen(),
      actionFormula,
      this::setActionStringLen
    );
  }

  
}
