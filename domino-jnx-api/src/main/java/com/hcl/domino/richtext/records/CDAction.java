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
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Set;

import com.hcl.domino.formula.FormulaCompiler;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.StructureSupport;
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
    SHOW_IN_MENU(0x00000001),
    SHOW_IN_BAR(0x00000002),
    SHOW_WHEN_PREVIEWING(0x00000004),
    SHOW_WHEN_READING(0x00000008),

    SHOW_WHEN_EDITING(0x00000010),
    SHOW_ON_OLE_LAUNCH(0x00000020),
    OLE_CLOSE_WHEN_CHOSEN(0x00000040),
    NO_FORMULA(0x00000080),
    SHOW_WHEN_PREVEDITING(0x00000100),

    OLE_DOC_WINDOW_TO_FRONT(0x00001000),
    HIDE_FROM_NOTES(0x00002000),
    HIDE_FROM_WEB(0x00004000),
    READING_ORDER_RTL(0x00008000),
    SHARED(0x00010000), /* action is shared*/
    MODIFIED(0x00020000), /* action has been modified (not saved on disk) */
    ALWAYS_SHARED(0x00040000), /* flag not saved on disk */
    ALIGN_ICON_RIGHT(0x00080000),
    IMAGE_RESOURCE_ICON(0x00100000),
    FRAME_TARGET(0x00400000),
    TEXT_ONLY_IN_MENU(0x00800000),
    BUTTON_TO_RIGHT(0x01000000), /* Show button on opposite side from action bar direction */
    HIDE_FROM_MOBILE(0x04000000), /* action is hidden from mobile */
    SHOW_IN_POPUPMENU(0x10000000),
    MAKE_SPLIT_BUTTON(0x20000000); /* LI: 4602.02, Provide support for "Split button" for java action bar */

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

  enum Type implements INumberEnum<Short> {
    RUN_FORMULA(1),
    RUN_SCRIPT(2),
    RUN_AGENT(3),
    OLDSYS_COMMAND(4),
    SYS_COMMAND(5),
    PLACEHOLDER(6),
    RUN_JAVASCRIPT(7);

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

  default byte[] getActionData() {
    final int titleLen = this.getTitleLength();
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
    if (this.getActionType() != Type.RUN_FORMULA) {
      throw new UnsupportedOperationException("Unable to retrieve formula data for a non-formula action");
    }
    final byte[] compiledFormula = this.getActionData();
    return FormulaCompiler.get().decompile(compiledFormula);
  }

  default int getActionLength() {
    int titleLen = this.getTitleLength();
    titleLen += titleLen % 2;
    int hideWhenLen = this.getHideWhenFormulaLength();
    hideWhenLen += hideWhenLen % 2;
    return this.getHeader().getLength().intValue() - 22 // sizeOf(CDACTION)
        - titleLen
        - hideWhenLen;
  }

  /**
   * Retrieves the LotusScript for this action as a string.
   *
   * @return the LotusScript for this action
   * @throws UnsupportedOperationException if the action's type is not
   *                                       {@link Type#RUN_SCRIPT}
   */
  default String getActionLotusScript() {
    if (this.getActionType() != Type.RUN_SCRIPT) {
      throw new UnsupportedOperationException("Unable to retrieve script data for a non-LotusScript action");
    }
    final byte[] scriptData = this.getActionData();
    return new String(scriptData, Charset.forName("LMBCS-native")); //$NON-NLS-1$
  }

  @StructureGetter("Type")
  Type getActionType();

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

  @StructureGetter("Flags")
  Set<Flag> getFlags();

  @StructureGetter("Header")
  @Override
  LSIG getHeader();

  /**
   * Retrieves the hide-when formula for this action as a string.
   *
   * @return the decompiled hide-when formula for this action
   */
  default String getHideWhenFormula() {
    return StructureSupport.extractCompiledFormula(
      this,
      getTitleLength() + getActionLength(),
      getHideWhenFormulaLength()
    );
  }

  @StructureGetter("FormulaLen")
  int getHideWhenFormulaLength();

  @StructureGetter("IconIndex")
  int getIconIndex();

  @StructureGetter("ShareId")
  int getShareId();

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
      return new String(lmbcs, 0, lmbcs.length - 1, Charset.forName("LMBCS-native")); //$NON-NLS-1$
    } else {
      return new String(lmbcs, Charset.forName("LMBCS-native")); //$NON-NLS-1$
    }
  }

  @StructureGetter("TitleLen")
  int getTitleLength();

  default CDAction setActionData(final byte[] actionData) {
    final int titleLen = this.getTitleLength();
    final int hideWhenLen = this.getHideWhenFormulaLength();
    final byte[] hideWhenData = this.getCompiledHideWhenFormula();

    this.resizeVariableData(titleLen + actionData.length + hideWhenLen);
    final ByteBuffer buf = this.getVariableData();
    buf.position(titleLen);
    buf.put(actionData);
    buf.put(hideWhenData);

    return this;
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
    final byte[] actionData = formula == null ? new byte[0] : FormulaCompiler.get().compile(formula);
    this.setActionType(Type.RUN_FORMULA);
    return this.setActionData(actionData);
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
    final byte[] actionData = script == null ? new byte[0] : script.getBytes(Charset.forName("LMBCS-native")); //$NON-NLS-1$
    this.setActionType(Type.RUN_SCRIPT);
    return this.setActionData(actionData);
  }

  /**
   * Sets the type for this action.
   * <p>
   * This type is also set implicitly by {@link #setActionFormula(String)} and
   * {@link #setActionLotusScript(String)}.
   * When setting the type explicitly, you are responsible for setting
   * {@link #setActionData(byte[])} to an appropriate
   * value.
   * </p>
   *
   * @param type the new type for the action
   * @return this action
   */
  @StructureSetter("Type")
  CDAction setActionType(Type type);

  @StructureSetter("Flags")
  CDAction setFlags(Collection<Flag> flags);

  /**
   * Sets the action's hide-when formula to the provided string.
   *
   * @param formula the formula-language string to set
   * @return this action
   */
  default CDAction setHideWhenFormula(final String formula) {
    final int titleLen = this.getTitleLength();
    final int actionLen = this.getActionLength();

    final byte[] compiled = FormulaCompiler.get().compile(formula);
    final int hideWhenLen = compiled.length + compiled.length % 2;
    this.setHideWhenFormulaLength(hideWhenLen);
    this.resizeVariableData(titleLen + actionLen + compiled.length);

    final ByteBuffer buf = this.getVariableData();
    buf.position(titleLen + actionLen);
    buf.put(compiled);

    return this;
  }

  @StructureSetter("FormulaLen")
  CDAction setHideWhenFormulaLength(int formulaLength);

  @StructureSetter("IconIndex")
  CDAction setIconIndex(int iconIndex);

  // TODO implement remaining action types

  @StructureSetter("ShareId")
  CDAction setShareId(int shareId);

  default CDAction setTitle(final String title) {
    final byte[] lmbcs = title == null ? new byte[0] : title.getBytes(Charset.forName("LMBCS-native")); //$NON-NLS-1$
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

  @StructureSetter("TitleLen")
  CDAction setTitleLength(int titleLength);
}
