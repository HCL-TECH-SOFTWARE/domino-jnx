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

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.data.NativeItemCoder;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.LSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.15
 */
@StructureDefinition(
  name = "CDACTION",
  members = {
    @StructureMember(name = "Header", type = LSIG.class),
    @StructureMember(name = "Type", type = CDAction.Type.class),
    @StructureMember(name = "IconIndex", type = short.class, unsigned = true),
    @StructureMember(name = "Flags", type = CDAction.Flag.class, bitfield = true),
    @StructureMember(name = "TitleLen", type = short.class, unsigned = true),
    @StructureMember(name = "FormulaLen", type = short.class, unsigned = true),
    @StructureMember(name = "ShareId", type = int.class)
  }
)
public interface CDAction extends RichTextRecord<LSIG> {
  enum Flag implements INumberEnum<Integer> {
    SHOW_IN_MENU(RichTextConstants.ACTION_SHOW_IN_MENU),
    SHOW_IN_BAR(RichTextConstants.ACTION_SHOW_IN_BAR),
    SHOW_WHEN_PREVIEWING(RichTextConstants.ACTION_SHOW_WHEN_PREVIEWING),
    SHOW_WHEN_READING(RichTextConstants.ACTION_SHOW_WHEN_READING),
    SHOW_WHEN_EDITING(RichTextConstants.ACTION_SHOW_WHEN_EDITING),
    SHOW_ON_OLE_LAUNCH(RichTextConstants.ACTION_SHOW_ON_OLE_LAUNCH),
    OLE_CLOSE_WHEN_CHOSEN(RichTextConstants.ACTION_OLE_CLOSE_WHEN_CHOSEN),
    NO_FORMULA(RichTextConstants.ACTION_NO_FORMULA),
    SHOW_WHEN_PREVEDITING(RichTextConstants.ACTION_SHOW_WHEN_PREVEDITING),
    OLE_DOC_WINDOW_TO_FRONT(RichTextConstants.ACTION_OLE_DOC_WINDOW_TO_FRONT),
    HIDE_FROM_NOTES(RichTextConstants.ACTION_HIDE_FROM_NOTES),
    HIDE_FROM_WEB(RichTextConstants.ACTION_HIDE_FROM_WEB),
    READING_ORDER_RTL(RichTextConstants.ACTION_READING_ORDER_RTL),
    SHARED(RichTextConstants.ACTION_SHARED),
    MODIFIED(RichTextConstants.ACTION_MODIFIED),
    ALWAYS_SHARED(RichTextConstants.ACTION_ALWAYS_SHARED),
    ALIGN_ICON_RIGHT(RichTextConstants.ACTION_ALIGN_ICON_RIGHT),
    IMAGE_RESOURCE_ICON(RichTextConstants.ACTION_IMAGE_RESOURCE_ICON),
    FRAME_TARGET(RichTextConstants.ACTION_FRAME_TARGET),
    TEXT_ONLY_IN_MENU(RichTextConstants.ACTION_TEXT_ONLY_IN_MENU),
    BUTTON_TO_RIGHT(RichTextConstants.ACTION_BUTTON_TO_RIGHT),
    HIDE_FROM_MOBILE(RichTextConstants.ACTION_HIDE_FROM_MOBILE),
    SHOW_IN_POPUPMENU(RichTextConstants.ACTION_SHOW_IN_POPUPMENU),
    MAKE_SPLIT_BUTTON(RichTextConstants.ACTION_MAKE_SPLIT_BUTTON),
    SHOW_IN_MOBILE_ACTIONS(RichTextConstants.ACTION_SHOW_IN_MOBILE_ACTIONS);

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
  /**
   * Represents the available language types for an action bar action.
   */
  public enum Type implements INumberEnum<Short> {
    RUN_FORMULA(RichTextConstants.ACTION_RUN_FORMULA),
    RUN_SCRIPT(RichTextConstants.ACTION_RUN_SCRIPT),
    RUN_AGENT(RichTextConstants.ACTION_RUN_AGENT),
    OLDSYS_COMMAND(RichTextConstants.ACTION_OLDSYS_COMMAND),
    SYS_COMMAND(RichTextConstants.ACTION_SYS_COMMAND),
    PLACEHOLDER(RichTextConstants.ACTION_PLACEHOLDER),
    RUN_JAVASCRIPT(RichTextConstants.ACTION_RUN_JAVASCRIPT);

    private final short value;

    Type(final int value) {
      this.value = (short) value;
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

  @StructureGetter("Type")
  Optional<Type> getActionType();

  /**
   * Retrieves the action type as a raw {@code short}.
   * 
   * @return the action type as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("Type")
  short getActionTypeRaw();

  @StructureGetter("Flags")
  Set<Flag> getFlags();

  @StructureGetter("Header")
  @Override
  LSIG getHeader();

  @StructureGetter("FormulaLen")
  int getHideWhenFormulaLength();

  @StructureGetter("IconIndex")
  int getIconIndex();

  @StructureGetter("ShareId")
  int getShareId();

  @StructureGetter("TitleLen")
  int getTitleLength();

  /**
   * Sets the type for this action.
   * <p>
   * This type is also set implicitly by {@link #setActionFormula(String)} and
   * {@link #setActionLotusScript(String)}.
   * </p>
   *
   * @param type the new type for the action
   * @return this action
   */
  @StructureSetter("Type")
  CDAction setActionType(Type type);

  /**
   * Sets the type as a raw {@code short}
   *
   * @param type the new type for the action
   * @return this action
   * @since 1.24.0
   */
  @StructureSetter("Type")
  CDAction setActionTypeRaw(short type);

  @StructureSetter("Flags")
  CDAction setFlags(Collection<Flag> flags);

  @StructureSetter("FormulaLen")
  CDAction setHideWhenFormulaLength(int formulaLength);

  @StructureSetter("IconIndex")
  CDAction setIconIndex(int iconIndex);

  // TODO implement remaining action types

  @StructureSetter("ShareId")
  CDAction setShareId(int shareId);

  @StructureSetter("TitleLen")
  CDAction setTitleLength(int titleLength);

  default int getActionLength() {
    int titleLen = this.getTitleLength();
    titleLen += titleLen % 2;
    int hideWhenLen = this.getHideWhenFormulaLength();
    return this.getHeader().getLength().intValue() - 22 // sizeOf(CDACTION)
        - titleLen
        - hideWhenLen;
  }

  default String getTitle() {
    final ByteBuffer buf = this.getVariableData();
    final int len = this.getTitleLength();
    if (len == 0) {
      return ""; //$NON-NLS-1$
    }
    final byte[] lmbcs = new byte[len];
    buf.get(lmbcs);
    if (lmbcs[lmbcs.length - 1] == 0) {
      // This is an optional padding byte
      return new String(lmbcs, 0, lmbcs.length - 1, NativeItemCoder.get().getLmbcsCharset());
    } else {
      return new String(lmbcs, NativeItemCoder.get().getLmbcsCharset());
    }
  }

  default CDAction setTitle(final String title) {
    final byte[] lmbcs = title == null ? new byte[0] : title.getBytes(NativeItemCoder.get().getLmbcsCharset());
    final byte[] actionData = this.getActionData();
    final byte[] hideWhenData = this.getCompiledHideWhenFormula();
    final int titleLen = lmbcs.length + lmbcs.length % 2;
    this.setTitleLength(titleLen);

    // Pad the title stored to match an even number of bytes
    this.resizeVariableData(actionData.length + hideWhenData.length + titleLen);
    final ByteBuffer buf = this.getVariableData();
    buf.put(lmbcs);
    if (lmbcs.length % 2 == 1) {
      buf.position(buf.position() + 1);
    }
    buf.put(actionData);
    buf.put(hideWhenData);

    return this;
  }

  default byte[] getActionData() {
    int titleLen = this.getTitleLength();
    titleLen += titleLen % 2;
    final int actionLen = this.getActionLength();

    final ByteBuffer buf = this.getVariableData();
    buf.position(buf.position() + titleLen);
    final byte[] result = new byte[actionLen];
    buf.get(result);
    return result;
  }

  /**
   * Retrieves the formula for this action as a string.
   *
   * @return the decompiled formula for this action
   * @throws UnsupportedOperationException if the action's type is not
   *                                       {@link Type#RUN_FORMULA}
   */
  default String getActionFormula() {
    if (this.getActionType().orElse(null) != Type.RUN_FORMULA) {
      throw new UnsupportedOperationException("Unable to retrieve formula data for a non-formula action");
    }
    int titleLen = getTitleLength();
    return StructureSupport.extractCompiledFormula(
      this,
      titleLen + (titleLen % 2),
      getActionLength()
    );
  }

  /**
   * Sets the action's code to the provided formula language string. This method
   * has the side effect
   * of setting the action type to {@link Type#RUN_FORMULA}.
   *
   * @param formula the formula-language string to set
   * @return this action
   */
  default CDAction setActionFormula(final String formula) {
    this.setActionType(Type.RUN_FORMULA);
    int titleLen = getTitleLength();
    titleLen += titleLen % 2;
    return StructureSupport.writeCompiledFormula(
      this,
      titleLen,
      getActionLength(),
      formula,
      (int lmbcsLen) -> {}
    );
  }

  /**
   * Sets the action's code to the provided LotusScript string. This method has
   * the side effect
   * of setting the action type to {@link Type#RUN_SCRIPT}.
   *
   * @param script the LotusScript string to set
   * @return this action
   */
  default CDAction setActionLotusScript(final String script) {
    // TODO sanity check to throw an exception if the script can't fit - that
    // rollover should be handled in RichTextWriter
    this.setActionType(Type.RUN_SCRIPT);
    int titleLen = getTitleLength();
    titleLen += titleLen % 2;
    return StructureSupport.writeStringValue(
      this,
      titleLen,
      getActionLength(),
      script,
      (int lmbcsLen) -> {}
    );
  }

  /**
   * Retrieves the LotusScript for this action as a string.
   *
   * @return the LotusScript for this action
   * @throws UnsupportedOperationException if the action's type is not
   *                                       {@link Type#RUN_SCRIPT}
   */
  default String getActionLotusScript() {
    if (this.getActionType().orElse(null) != Type.RUN_SCRIPT) {
      throw new UnsupportedOperationException("Unable to retrieve script data for a non-LotusScript action");
    }
    int titleLen = getTitleLength();
    titleLen += titleLen % 2;
    return StructureSupport.extractStringValue(
      this,
      titleLen,
      getActionLength()
    );
  }

  /**
   * Retrieves the hide-when formula for this action as a string.
   *
   * @return the decompiled hide-when formula for this action
   */
  default String getHideWhenFormula() {
    int titleLen = getTitleLength();
    return StructureSupport.extractCompiledFormula(
      this,
      getTitleLength() + (titleLen % 2) + getActionLength(),
      getHideWhenFormulaLength()
    );
  }

  /**
   * Sets the action's hide-when formula to the provided string.
   *
   * @param formula the formula-language string to set
   * @return this action
   */
  default CDAction setHideWhenFormula(final String formula) {
    this.setActionType(Type.RUN_FORMULA);
    int titleLen = getTitleLength();
    titleLen += titleLen % 2;
    return StructureSupport.writeCompiledFormula(
      this,
      titleLen + getActionLength(),
      getHideWhenFormulaLength(),
      formula,
      this::setHideWhenFormulaLength
    );
  }

  /**
   * Retrieves the compiled hide-when formula for this action as a byte array.
   *
   * @return the compiled hide-when formula for this action
   */
  default byte[] getCompiledHideWhenFormula() {
    final int titleLen = this.getTitleLength();
    final int actionLen = this.getActionLength();
    final int hideWhenLen = this.getHideWhenFormulaLength();

    final ByteBuffer buf = this.getVariableData();
    buf.position(buf.position() + titleLen + actionLen);
    final byte[] result = new byte[hideWhenLen];
    buf.get(result);
    return result;
  }
}
