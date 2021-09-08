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

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * VMODSbigobj
 * 
 * @author artcnot
 * @since 1.0.38
 */
@StructureDefinition(
  name = "VMODSbigobj", 
  members = { 
    @StructureMember(name = "Header", type = WSIG.class), /* Signature identifying the type of Navigator CD record. */
    @StructureMember(name = "ObjRect", type = VMODSrect.class), /* Bounding rectangle for this graphical object. */
    @StructureMember(name = "flags", type = ViewmapDatasetRecord.Flags.class, bitfield = true), /* Option flags. Set to 7. */
    @StructureMember(name = "NameLen", type = short.class, unsigned = true), /* Graphical object name length (may be 0). */
    @StructureMember(name = "LabelLen", type = short.class, unsigned = true), /* Graphical object displayed label length (may be 0). */
    @StructureMember(name = "FontID", type = FontStyle.class), /* FontID to use when displaying the label. */
    @StructureMember(name = "TextColor", type = short.class, unsigned = true), /* Color to use for the label text. Use NOTES_COLOR_xxx value. */
    @StructureMember(name = "Alignment", type = short.class, unsigned = true), /* Alignment of the label text. Set to 0. */
    @StructureMember(name = "bWrap", type = short.class, unsigned = true), /* If TRUE, apply word-wrap when displaying the label. */
    @StructureMember(name = "Spare", type = int[].class, length = 4) /* Reserved. Must be 0. */
    /* Header field contains WORD length subfield. Some Navigator CD records use VMODSdrobj, which contains a BYTE length subfield. */
})
public interface VMODSbigobj extends RichTextRecord<WSIG> {

  enum Flags implements INumberEnum<Short> {
    /* no ENUM values defined in doc or .h file */
    ;

    private final short value;
    private Flags(short value) {
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

  @StructureGetter("ObjRect")
  VMODSrect getObjRect();

  @StructureGetter("NameLen")
  int getNameLen();

  @StructureGetter("LabelLen")
  int getLabelLen();

  @StructureGetter("FontID")
  FontStyle getFontID();

  @StructureGetter("TextColor")
  int getTextColor();

  @StructureGetter("Alignment")
  int getAlignment();

  @StructureGetter("bWrap")
  int getbWrap();

  @StructureGetter("Spare")
  int[] getSpare();

  @StructureSetter("NameLen")
  VMODSbigobj setNameLen(int length);

  @StructureSetter("LabelLen")
  VMODSbigobj setLabelLen(int length);

  @StructureSetter("TextColor")
  VMODSbigobj setTextColor(int color);

  @StructureSetter("Alignment")
  VMODSbigobj setAlignment(int alignment);

  @StructureSetter("bWrap")
  VMODSbigobj setbWrap(int bWrap);

}
