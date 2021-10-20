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

import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.45
 */
@StructureDefinition(
  name = "CDTEXTPROPERTY",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "TextStyleName", type = byte[].class, length = NotesConstants.MAX_TEXTSTYLE_NAME),
    @StructureMember(name = "LangName", type = byte[].class, length = NotesConstants.MAX_ISO_LANG_SIZE),
    @StructureMember(name = "PropID", type = int.class),
    @StructureMember(name = "Flags", type = int.class),
    @StructureMember(name = "Reserved2", type = int.class),
    @StructureMember(name = "Reserved3", type = int.class)
  }
)
public interface CDTextProperty extends RichTextRecord<WSIG> {
  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("TextStyleName")
  byte[] getTextStyleNameRaw();
  
  @StructureSetter("TextStyleName")
  CDTextProperty setTextStyleName(byte[] name);
  
  @StructureGetter("LangName")
  byte[] getLangNameRaw();
  
  @StructureSetter("LangName")
  CDTextProperty setLangName();
  
  @StructureGetter("PropID")
  int getPropId();
  
  @StructureSetter("PropID")
  CDTextProperty setPropId(int id);
  
  default String getTextStyleName() {
    return StructureSupport.readLmbcsValue(getTextStyleNameRaw());
  }
  
  default String getLangName() {
    return StructureSupport.readLmbcsValue(getLangNameRaw());
  }
}
