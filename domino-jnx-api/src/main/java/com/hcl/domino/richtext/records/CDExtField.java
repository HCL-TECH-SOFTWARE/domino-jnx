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
import java.util.Collection;
import java.util.Set;

import com.hcl.domino.data.NativeItemCoder;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
@StructureDefinition(name = "CDEXTFIELD", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Flags1", type = CDExtField.Flag.class, bitfield = true),
    @StructureMember(name = "Flags2", type = CDExtField.Flag2.class, bitfield = true),
    @StructureMember(name = "EntryHelper", type = CDExtField.HelperType.class),
    @StructureMember(name = "EntryDBNameLen", type = short.class, unsigned = true),
    @StructureMember(name = "EntryViewNameLen", type = short.class, unsigned = true),
    @StructureMember(name = "EntryColumnNumber", type = short.class, unsigned = true)
})
public interface CDExtField extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Integer> {
    /** lookup name as each char typed */
    LOOKUP_EACHCHAR(RichTextConstants.FEXT_LOOKUP_EACHCHAR),
    /** recalc on new keyword selection */
    KWSELRECALC(RichTextConstants.FEXT_KWSELRECALC),
    /** suppress showing field hinky minky */
    KWHINKYMINKY(RichTextConstants.FEXT_KWHINKYMINKY),
    /** recalc after validation */
    AFTERVALIDATION(RichTextConstants.FEXT_AFTERVALIDATION),
    /** the first field with this bit set will accept the caret */
    ACCEPT_CARET(RichTextConstants.FEXT_ACCEPT_CARET),
    KEYWORD_COLS_SHIFT(RichTextConstants.FEXT_KEYWORD_COLS_SHIFT),
    KEYWORD_COLS_MASK(RichTextConstants.FEXT_KEYWORD_COLS_MASK),
    KEYWORD_FRAME_3D(RichTextConstants.FEXT_KEYWORD_FRAME_3D),
    KEYWORD_FRAME_STANDARD(RichTextConstants.FEXT_KEYWORD_FRAME_STANDARD),
    KEYWORD_FRAME_NONE(RichTextConstants.FEXT_KEYWORD_FRAME_NONE),
    KEYWORD_FRAME_MASK(RichTextConstants.FEXT_KEYWORD_FRAME_MASK),
    KEYWORDS_UI_COMBO(RichTextConstants.FEXT_KEYWORDS_UI_COMBO),
    KEYWORDS_UI_LIST(RichTextConstants.FEXT_KEYWORDS_UI_LIST);

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

  enum Flag2 implements INumberEnum<Integer> {
    /** TRUE to recalc the value choices. */
    KW_CHOICE_RECALC(RichTextConstants.FEXT_KW_CHOICE_RECALC),
    /** TRUE means we have a CD_EXTHTML field */
    HTML_IN_FIELDDEF(RichTextConstants.FEXT_HTML_IN_FIELDDEF),
    /** TRUE if hiding delimeters */
    HIDEDELIMITERS(RichTextConstants.FEXT_HIDEDELIMITERS),
    KW_RTL_READING_ORDER(RichTextConstants.FEXT_KW_RTL_READING_ORDER),
    /** TRUE if tab will exit field (used for richtext only) */
    ALLOWTABBINGOUT(RichTextConstants.FEXT_ALLOWTABBINGOUT),
    /** TRUE if field is a password field */
    PASSWORD(RichTextConstants.FEXT_PASSWORD),
    /** TRUE if an applet should be used for a browser (richtext only) */
    USEAPPLETINBROWSER(RichTextConstants.FEXT_USEAPPLETINBROWSER),
    /** TRUE if field is a control */
    CONTROL(RichTextConstants.FEXT_CONTROL),
    /**
     * TRUE if this is a formula field which should have item substitution based
     * on items on the form. This is the counterpart to computed formula which
     * is a formula programmatically generated through at-formulas.
     */
    LITERALIZE(RichTextConstants.FEXT_LITERALIZE),
    /** TRUE if field is a dynamic control */
    CONTROLDYNAMIC(RichTextConstants.FEXT_CONTROLDYNAMIC),
    /**
     * TRUE if should run exiting event when value changes. Currently only
     * implemented
     * for native date/time
     */
    RUNEXITINGONCHANGE(RichTextConstants.FEXT_RUNEXITINGONCHANGE),
    /** TRUE if this is a time zone field */
    TIMEZONE(RichTextConstants.FEXT_TIMEZONE),
    /** TRUE if field has proportional height */
    PROPORTIONALHEIGHT(RichTextConstants.FEXT_PROPORTIONALHEIGHT),
    /** TRUE if field has proportional width */
    PROPORTIONALWIDTH(RichTextConstants.FEXT_PROPORTIONALWIDTH),
    /** TRUE if a names type field displays im online status */
    SHOWIMSTATUS(RichTextConstants.FEXT_SHOWIMSTATUS),
    /** TRUE if we should use a JS Control in the browser */
    USEJSCTLINBROWSER(RichTextConstants.FEXT_USEJSCTLINBROWSER),
    ;

    private final int value;

    Flag2(final int value) {
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

  enum HelperType implements INumberEnum<Short> {
    NONE(RichTextConstants.FIELD_HELPER_NONE),
    ADDRDLG(RichTextConstants.FIELD_HELPER_ADDRDLG),
    ACLDLG(RichTextConstants.FIELD_HELPER_ACLDLG),
    VIEWDLG(RichTextConstants.FIELD_HELPER_VIEWDLG);

    private final short value;

    HelperType(final short value) {
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

  @StructureGetter("EntryColumnNumber")
  int getEntryColumnNumber();

  default String getEntryDBName() {
    final int len = this.getEntryDBNameLength();
    if (len == 0) {
      return ""; //$NON-NLS-1$
    }
    final ByteBuffer buf = this.getVariableData();
    final byte[] lmbcs = new byte[len];
    buf.get(lmbcs);
    return new String(lmbcs, NativeItemCoder.get().getLmbcsCharset());
  }

  @StructureGetter("EntryDBNameLen")
  int getEntryDBNameLength();

  default String getEntryViewName() {
    final int len = this.getEntryViewNameLength();
    if (len == 0) {
      return ""; //$NON-NLS-1$
    }
    final int preLen = this.getEntryDBNameLength();
    final ByteBuffer buf = this.getVariableData();
    buf.position(preLen);
    final byte[] lmbcs = new byte[len];
    buf.get(lmbcs);
    return new String(lmbcs, NativeItemCoder.get().getLmbcsCharset());
  }

  @StructureGetter("EntryViewNameLen")
  int getEntryViewNameLength();

  @StructureGetter("Flags1")
  Set<Flag> getFlags1();

  @StructureGetter("Flags2")
  Set<Flag2> getFlags2();

  @StructureGetter("Header")
  @Override
  WSIG getHeader();

  @StructureGetter("EntryHelper")
  HelperType getHelperType();

  @StructureSetter("EntryColumnNumber")
  CDExtField setEntryColumnNumber(int columnNumber);

  default CDExtField setEntryDBName(final String name) {
    final int viewNameLen = this.getEntryViewNameLength();
    final byte[] lmbcs = name == null ? new byte[0] : name.getBytes(NativeItemCoder.get().getLmbcsCharset());
    this.setEntryDBNameLength(lmbcs.length);
    this.resizeVariableData(viewNameLen + lmbcs.length);

    final ByteBuffer buf = this.getVariableData();
    buf.put(lmbcs);

    return this;
  }

  @StructureSetter("EntryDBNameLen")
  CDExtField setEntryDBNameLength(int len);

  default CDExtField setEntryViewName(final String name) {
    final int dbNameLen = this.getEntryDBNameLength();
    final byte[] lmbcs = name == null ? new byte[0] : name.getBytes(NativeItemCoder.get().getLmbcsCharset());
    this.setEntryViewNameLength(lmbcs.length);
    this.resizeVariableData(dbNameLen + lmbcs.length);

    final ByteBuffer buf = this.getVariableData();
    buf.position(dbNameLen);
    buf.put(lmbcs);

    return this;
  }

  @StructureSetter("EntryViewNameLen")
  CDExtField setEntryViewNameLength(int len);

  @StructureSetter("Flags1")
  CDExtField setFlags1(Collection<Flag> flags);

  @StructureSetter("Flags2")
  CDExtField setFlags2(Collection<Flag2> flags);

  @StructureSetter("EntryHelper")
  CDExtField setHelperType(HelperType type);

}
