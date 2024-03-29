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
import java.util.Set;

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
@StructureDefinition(name = "CDEMBEDDEDCTL", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "CtlStyle", type = CDEmbeddedControl.Style.class, bitfield = true),
    @StructureMember(name = "Flags", type = CDEmbeddedControl.Flag.class, bitfield = true),
    @StructureMember(name = "Width", type = short.class, unsigned = true),
    @StructureMember(name = "Height", type = short.class, unsigned = true),
    @StructureMember(name = "Version", type = CDEmbeddedControl.Version.class),
    @StructureMember(name = "CtlType", type = CDEmbeddedControl.Type.class),
    @StructureMember(name = "MaxChars", type = short.class, unsigned = true),
    @StructureMember(name = "MaxLines", type = short.class, unsigned = true),
    @StructureMember(name = "Percentage", type = short.class, unsigned = true),
    @StructureMember(name = "Spare", type = short[].class, length = 3),
})
public interface CDEmbeddedControl extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Short> {
    UNITS(RichTextConstants.EC_FLAG_UNITS),
    /** Width/Height are in dialog units, not twips */
    DIALOGUNITS(RichTextConstants.EC_FLAG_DIALOGUNITS),
    /** Width/Height should be adjusted to fit contents */
    FITTOCONTENTS(RichTextConstants.EC_FLAG_FITTOCONTENTS),
    /** this control is active regardless of docs R/W status */
    ALWAYSACTIVE(RichTextConstants.EC_FLAG_ALWAYSACTIVE),
    /** let placeholder automatically fit to window */
    FITTOWINDOW(RichTextConstants.EC_FLAG_FITTOWINDOW),
    /** position control to top of paragraph */
    POSITION_TOP(RichTextConstants.EC_FLAG_POSITION_TOP),
    /** position control to bottom of paragraph */
    POSITION_BOTTOM(RichTextConstants.EC_FLAG_POSITION_BOTTOM),
    /** position control to ascent of paragraph */
    POSITION_ASCENT(RichTextConstants.EC_FLAG_POSITION_ASCENT),
    /** position control to height of paragraph */
    POSITION_HEIGHT(RichTextConstants.EC_FLAG_POSITION_HEIGHT),
    ;

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

  enum Style implements INumberEnum<Integer> {
    EDITMULTILINE(RichTextConstants.EC_STYLE_EDITMULTILINE),
    EDITVSCROLL(RichTextConstants.EC_STYLE_EDITVSCROLL),
    EDITPASSWORD(RichTextConstants.EC_STYLE_EDITPASSWORD),
    EDITCOMBO(RichTextConstants.EC_STYLE_EDITCOMBO),
    LISTMULTISEL(RichTextConstants.EC_STYLE_LISTMULTISEL),
    CALENDAR(RichTextConstants.EC_STYLE_CALENDAR),
    TIME(RichTextConstants.EC_STYLE_TIME),
    DURATION(RichTextConstants.EC_STYLE_DURATION),
    TIMEZONE(RichTextConstants.EC_STYLE_TIMEZONE),
    VALID(RichTextConstants.EC_STYLE_VALID),
    ;

    private final int value;

    Style(final int value) {
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
    EDIT(RichTextConstants.EMBEDDEDCTL_EDIT),
    COMBO(RichTextConstants.EMBEDDEDCTL_COMBO),
    LIST(RichTextConstants.EMBEDDEDCTL_LIST),
    TIME(RichTextConstants.EMBEDDEDCTL_TIME),
    KEYGEN(RichTextConstants.EMBEDDEDCTL_KEYGEN),
    FILE(RichTextConstants.EMBEDDEDCTL_FILE),
    TIMEZONE(RichTextConstants.EMBEDDEDCTL_TIMEZONE),
    COLOR(RichTextConstants.EMBEDDEDCTL_COLOR);

    private final short value;

    Type(final short value) {
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

  enum Version implements INumberEnum<Short> {
    VERSION1(RichTextConstants.EMBEDDEDCTL_VERSION1),
    ;

    private final short value;

    Version(final short value) {
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

  @StructureGetter("CtlType")
  Optional<Type> getControlType();
  
  /**
   * Retrieves the type of this control as its raw
   * {@code short} value.
   * 
   * @return the control type as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("CtlType")
  short getControlTypeRaw();

  @StructureGetter("Flags")
  Set<Flag> getFlags();

  @StructureGetter("Header")
  @Override
  WSIG getHeader();

  @StructureGetter("Height")
  int getHeight();

  @StructureGetter("MaxChars")
  int getMaxChars();

  @StructureGetter("Percentage")
  int getPercentage();

  /**
   * Retrieves potential styles for this control. Due to the way this information
   * is
   * stored, multiple {@link Style} values may match a given value, and which is
   * in
   * effect is based on the type of control.
   *
   * @return a {@link Set} of matching styles
   */
  @StructureGetter("CtlStyle")
  Set<Style> getStyle();

  @StructureGetter("Version")
  Optional<Version> getVersion();
  
  /**
   * Retrieves the version value as its raw {@code short}
   * value.
   * 
   * @return the version value as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("Version")
  short getVersionRaw();

  @StructureGetter("Width")
  int getWidth();

  @StructureSetter("CtlType")
  CDEmbeddedControl setControlType(Type type);
  
  /**
   * Sets the control type as a raw {@code short}.
   * 
   * @param type the type to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("CtlStyle")
  CDEmbeddedControl setControlTypeRaw(short type);

  @StructureSetter("Flags")
  CDEmbeddedControl setFlags(Set<Flag> flags);

  @StructureSetter("Height")
  CDEmbeddedControl setHeight(int height);

  @StructureSetter("MaxChars")
  CDEmbeddedControl setMaxChars(int chars);

  @StructureSetter("Percentage")
  CDEmbeddedControl setPercentage(int percentage);

  @StructureSetter("CtlStyle")
  CDEmbeddedControl setStyle(Style style);
  
  /**
   * Sets the control style as a raw {@code int}.
   * 
   * @param style the style to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("CtlStyle")
  CDEmbeddedControl setStyleRaw(int style);

  @StructureSetter("Version")
  CDEmbeddedControl setVersion(Version version);
  
  /**
   * Sets the control version as a raw {@code short}.
   * 
   * @param version the version to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("CtlStyle")
  CDEmbeddedControl setVersionRaw(short version);

  @StructureSetter("Width")
  CDEmbeddedControl setWidth(int width);
}
