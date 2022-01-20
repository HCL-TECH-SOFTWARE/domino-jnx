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


import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import com.hcl.domino.data.StandardColors;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.BSIG;

/**
 * VMODSdrobj
 * 
 * @author artcnot
 * @since 1.0.39
 */
@StructureDefinition(
  name = "VMODSdrobj", 
  members = { 
    @StructureMember(name = "Header", type = BSIG.class), /* Signature identifying the type of Navigator CD record. */
    @StructureMember(name = "ObjRect", type = VMODSrect.class), /* Bounding rectangle for this graphical object. */
    @StructureMember(name = "flags", type = ViewmapDrawingObject.Flag.class, bitfield = true), /* Option flags. Set to 7. */
    @StructureMember(name = "NameLen", type = short.class, unsigned = true), /* Graphical object name length (may be 0). */
    @StructureMember(name = "LabelLen", type = short.class, unsigned = true), /* Graphical object displayed label length (may be 0). */
    @StructureMember(name = "FontID", type = FontStyle.class), /* FontID to use when displaying the label. */
    @StructureMember(name = "TextColor", type = short.class), /* Color to use for the label text. Use NOTES_COLOR_xxx value. */
    @StructureMember(name = "Alignment", type = short.class), /* Alignment of the label text. Set to 0. */
    @StructureMember(name = "bWrap", type = short.class), /* If TRUE, apply word-wrap when displaying the label. */
    @StructureMember(name = "spare", type = int[].class, length = 4) /* Reserved. Must be 0. */
    /* Header field contains WORD length subfield. Some Navigator CD records use VMODSbigobj, which contains a BYTE length subfield. */
})
public interface ViewmapDrawingObject extends RichTextRecord<BSIG> {

  enum Flag implements INumberEnum<Short> {
    /** Set if obj is visible */
    VISIBLE(NotesConstants.VM_DROBJ_FLAGS_VISIBLE),
    /** Set if obj can be select (i.e. is not background) */
    SELECTABLE(NotesConstants.VM_DROBJ_FLAGS_SELECTABLE),
    /** Set if obj can't be edited */
    LOCKED(NotesConstants.VM_DROBJ_FLAGS_LOCKED),
    /**
     * Bitmap representing runtime image of the navigator.  Use to create
     * imagemaps from navigators.
     */
    IMAGEMAP_BITMAP(NotesConstants.VM_DROBJ_FLAGS_IMAGEMAP_BITMAP)
    ;

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

  @StructureGetter("Header")
  @Override
  BSIG getHeader();

  @StructureGetter("ObjRect")
  VMODSrect getObjRect();


  @StructureGetter("flags")
  Set<ViewmapDrawingObject.Flag> getFlags();
  
  @StructureGetter("NameLen")
  int getNameLen();

  @StructureGetter("LabelLen")
  int getLabelLen();

  @StructureGetter("FontID")
  FontStyle getFontID();

  @StructureGetter("TextColor")
  int getTextColorRaw();

  @StructureGetter("TextColor")
  Optional<StandardColors> getTextColor();

  @StructureGetter("Alignment")
  short getAlignment();

  @StructureGetter("bWrap")
  short getbWrap();

  @StructureSetter("flags")
  ViewmapDrawingObject setFlags(Collection<ViewmapDrawingObject.Flag> flags);

  @StructureSetter("NameLen")
  ViewmapDrawingObject setNameLen(int length);

  @StructureSetter("LabelLen")
  ViewmapDrawingObject setLabelLen(int length);

  @StructureSetter("TextColor")
  ViewmapDrawingObject setTextColorRaw(int color);

  @StructureSetter("TextColor")
  ViewmapDrawingObject setTextColor(StandardColors color);

  @StructureSetter("Alignment")
  ViewmapDrawingObject setAlignment(short alignment);

  @StructureSetter("bWrap")
  ViewmapDrawingObject setbWrap(short bWrap);

}
