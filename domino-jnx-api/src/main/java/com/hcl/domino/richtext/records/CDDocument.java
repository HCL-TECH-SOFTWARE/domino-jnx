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
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.data.StandardColors;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.BSIG;
import com.hcl.domino.richtext.structures.ColorValue;

/**
 * @author Jesse Gallagher
 * @since 1.0.33
 */
@StructureDefinition(
  name = "CDDOCUMENT",
  members = {
    @StructureMember(name = "Header", type = BSIG.class),
    @StructureMember(name = "PaperColor", type = short.class, unsigned = true),
    @StructureMember(name = "FormFlags", type = CDDocument.Flag.class, bitfield = true),
    @StructureMember(name = "NotePrivileges", type = short.class),
    @StructureMember(name = "FormFlags2", type = CDDocument.Flag2.class, bitfield = true),
    @StructureMember(name = "InherFieldNameLength", type = short.class, unsigned = true),
    @StructureMember(name = "PaperColorExt", type = short.class, unsigned = true),
    @StructureMember(name = "PaperColorValue", type = ColorValue.class),
    @StructureMember(name = "FormFlags3", type = CDDocument.Flag3.class, bitfield = true),
    @StructureMember(name = "Spare", type = short.class),
  }
)
public interface CDDocument extends RichTextRecord<BSIG> {
  enum PreV4PaperColor implements INumberEnum<Short> {
    BLACK(0),
    WHITE(1),
    GRAY(2),
    LTGREEN(3),
    GREEN(4),
    LTYELLOW(5),
    YELLOW(6),
    CYAN(7),
    LTCYAN(8),
    RED(9),
    GREEN2(10),
    BLUE(11),
    MAGENTA(12),
    YELLOW2(13),
    CYAN2(14),
    DKRED(15),
    DKGREEN(16),
    DKBLUE(17),
    DKMAGENTA(18),
    DKYELLOW(19),
    DKCYAN(20)
    ;

    private final short value;

    PreV4PaperColor(final int value) {
      this.value = (short)value;
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
  enum Flag implements INumberEnum<Short> {
    /**  Use Reference Note  */
    REFERENCE(DesignConstants.TPL_FLAG_REFERENCE),
    /**  Mail during DocSave  */
    MAIL(DesignConstants.TPL_FLAG_MAIL),
    /**  Add note ref. to "reference note"  */
    NOTEREF(DesignConstants.TPL_FLAG_NOTEREF),
    /**  Add note ref. to main parent of "reference note"  */
    NOTEREF_MAIN(DesignConstants.TPL_FLAG_NOTEREF_MAIN),
    /**  Recalc when leaving fields  */
    RECALC(DesignConstants.TPL_FLAG_RECALC),
    /**  Store form item in with note  */
    BOILERPLATE(DesignConstants.TPL_FLAG_BOILERPLATE),
    /**  Use foreground color to paint  */
    FGCOLOR(DesignConstants.TPL_FLAG_FGCOLOR),
    /**  Spare DWORDs have been zeroed  */
    SPARESOK(DesignConstants.TPL_FLAG_SPARESOK),
    /**  Activate OLE objects when composing a new doc  */
    ACTIVATE_OBJECT_COMP(DesignConstants.TPL_FLAG_ACTIVATE_OBJECT_COMP),  
    /**  Activate OLE objects when editing an existing doc  */
    ACTIVATE_OBJECT_EDIT(DesignConstants.TPL_FLAG_ACTIVATE_OBJECT_EDIT),  
    /**  Activate OLE objects when reading an existing doc  */
    ACTIVATE_OBJECT_READ(DesignConstants.TPL_FLAG_ACTIVATE_OBJECT_READ),  
    /**  Show Editor window if TPL_FLAG_ACTIVATE_OBJECT_COMPOSE  */
    SHOW_WINDOW_COMPOSE(DesignConstants.TPL_FLAG_SHOW_WINDOW_COMPOSE),
    /**  Show Editor window if TPL_FLAG_ACTIVATE_OBJECT_EDIT  */
    SHOW_WINDOW_EDIT(DesignConstants.TPL_FLAG_SHOW_WINDOW_EDIT),
    /**  Show Editor window if TPL_FLAG_ACTIVATE_OBJECT_READ  */
    SHOW_WINDOW_READ(DesignConstants.TPL_FLAG_SHOW_WINDOW_READ),
    /**  V3 Updates become responses  */
    UPDATE_RESPONSE(DesignConstants.TPL_FLAG_UPDATE_RESPONSE),
    /**  V3 Updates become parents  */
    UPDATE_PARENT(DesignConstants.TPL_FLAG_UPDATE_PARENT);

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
    /**  insert copy of ref note  */
    INCLUDEREF(DesignConstants.TPL_FLAG_INCLUDEREF),
    /**  render ref (else it's a doclink)  */
    RENDERREF(DesignConstants.TPL_FLAG_RENDERREF),
    /**  render it collapsed?  */
    RENDCOLLAPSE(DesignConstants.TPL_FLAG_RENDCOLLAPSE),
    /**  edit mode on open  */
    EDITONOPEN(DesignConstants.TPL_FLAG_EDITONOPEN),
    /**  open context panes  */
    OPENCNTXT(DesignConstants.TPL_FLAG_OPENCNTXT),
    /**  context pane is parent  */
    CNTXTPARENT(DesignConstants.TPL_FLAG_CNTXTPARENT),
    /**  manual versioning  */
    MANVCREATE(DesignConstants.TPL_FLAG_MANVCREATE),
    /**  V4 versioning - updates are sibblings  */
    UPDATE_SIBLING(DesignConstants.TPL_FLAG_UPDATE_SIBLING),
    /**  V4 Anonymous form  */
    ANONYMOUS(DesignConstants.TPL_FLAG_ANONYMOUS),
    /**  Doclink dive into same window  */
    NAVIG_DOCLINK_IN_PLACE(DesignConstants.TPL_FLAG_NAVIG_DOCLINK_IN_PLACE),
    /**  InterNotes special form  */
    INTERNOTES(DesignConstants.TPL_FLAG_INTERNOTES),
    /**  Disable FX for this doc */
    DISABLE_FX(DesignConstants.TPL_FLAG_DISABLE_FX),
    /**  Disable menus for this DOC  */
    NOMENUS(DesignConstants.TPL_FLAG_NOMENUS),
    /**  check display before displaying background  */
    CHECKDISPLAY(DesignConstants.TPL_FLAG_CHECKDISPLAY),
    /**  This is a Right To Left Form  */
    FORMISRTL(DesignConstants.TPL_FLAG_FORMISRTL),
    /**  hide background graphic in design mode  */
    HIDEBKGRAPHIC(DesignConstants.TPL_FLAG_HIDEBKGRAPHIC);

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
  enum Flag3 implements INumberEnum<Short> {
    /**  editor resizes header area to contents  */
    RESIZEHEADER(DesignConstants.TPL_FLAG_RESIZEHEADER),
    /**  No initial focus to any object on a form or page */
    NOINITIALFOCUS(DesignConstants.TPL_FLAG_NOINITIALFOCUS),
    /**  Sign this document when it gets saved  */
    SIGNWHENSAVED(DesignConstants.TPL_FLAG_SIGNWHENSAVED),
    /**  No focus when doing F6 or tabbing.  */
    NOFOCUSWHENF6(DesignConstants.TPL_FLAG_NOFOCUSWHENF6),
    /**  Render pass through HTML in the client.  */
    RENDERPASSTHROUGH(DesignConstants.TPL_FLAG_RENDERPASSTHROUGH),
    /**  Don't automatically add form fields to field index  */
    NOADDFIELDNAMESTOINDEX(DesignConstants.TPL_FLAG_NOADDFIELDNAMESTOINDEX),
    /**  Autosave Documents created using this form  */
    CANAUTOSAVE(DesignConstants.TPL_FLAG_CANAUTOSAVE);

    private final short value;

    Flag3(final short value) {
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

  @StructureGetter("Header")
  @Override
  BSIG getHeader();
  
  @StructureGetter("PaperColor")
  int getPreV4PaperColorRaw();
  
  @StructureSetter("PaperColor")
  CDDocument setPreV4PaperColorRaw(int paperColor);
  
  @StructureGetter("PaperColor")
  Optional<PreV4PaperColor> getPreV4PaperColor();
  
  @StructureSetter("PaperColor")
  CDDocument setPreV4PaperColor(PreV4PaperColor paperColor);

  @StructureGetter("FormFlags")
  Set<Flag> getFlags();

  @StructureSetter("FormFlags")
  CDDocument setFlags(Collection<Flag> flags);
  
  @StructureGetter("NotePrivileges")
  short getPreV3Privileges();
  
  @StructureSetter("NotePrivileges")
  CDDocument setPreV3Privileges(short privileges);

  @StructureGetter("FormFlags2")
  Set<Flag2> getFlags2();

  @StructureSetter("FormFlags2")
  CDDocument setFlags2(Collection<Flag2> flags);
  
  @StructureGetter("InherFieldNameLength")
  int getInheritanceFieldNameLength();
  
  @StructureSetter("InherFieldNameLength")
  CDDocument setInheritanceFieldNameLength(int len);

  @StructureGetter("PaperColorExt")
  int getPaperColorRaw();
  
  @StructureSetter("PaperColorExt")
  CDDocument setPaperColorRaw(int paperColor);
  
  @StructureGetter("PaperColorValue")
  ColorValue getPaperColorValue();

  @StructureGetter("FormFlags3")
  Set<Flag3> getFlags3();

  @StructureSetter("FormFlags3")
  CDDocument setFlags3(Collection<Flag3> flags);
  
  @StructureGetter("FormFlags3")
  short getFlags3Raw();
  
  @StructureSetter("FormFlags3")
  CDDocument setFlags3Raw(short flags);
  
  default String getInheritanceFieldName() {
    return StructureSupport.extractStringValue(
      this,
      0,
      getInheritanceFieldNameLength()
    );
  }
  
  default CDDocument setInheritanceFieldName(String fieldName) {
    return StructureSupport.writeStringValue(
      this,
      0,
      getInheritanceFieldNameLength(),
      fieldName,
      this::setInheritanceFieldNameLength
    );
  }
  
  default String getVersionFieldName() {
    int inheritanceLength = getInheritanceFieldNameLength();
    return StructureSupport.extractStringValue(
      this,
      inheritanceLength,
      getVariableData().remaining() - inheritanceLength
    );
  }
  
  default CDDocument setVersionFieldName(String fieldName) {
    int inheritanceLength = getInheritanceFieldNameLength();
    return StructureSupport.writeStringValue(
      this,
      inheritanceLength,
      getVariableData().remaining() - inheritanceLength,
      fieldName,
      (int len) -> {}
    );
  }
  
  default Optional<StandardColors> getPaperColor() {
    return DominoEnumUtil.valueOf(StandardColors.class, getPaperColorRaw());
  }
  
  default CDDocument setPaperColor(StandardColors color) {
    setPaperColorRaw(color == null ? 0 : color.getValue());
    return this;
  }
}
