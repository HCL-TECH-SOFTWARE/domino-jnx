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

import java.util.Optional;

import com.hcl.domino.constants.WindowsConstants;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.LSIG;
import com.hcl.domino.richtext.structures.RectSize;

/**
 * Rich text record of type CDWINMETAHEADER
 */
@StructureDefinition(name = "CDWINMETAHEADER", members = {
    @StructureMember(name = "Header", type = LSIG.class),
    @StructureMember(name = "mm", type = CDWinMetaHeader.MappingMode.class),
    @StructureMember(name = "xExt", type = short.class),
    @StructureMember(name = "yExt", type = short.class),
    @StructureMember(name = "OriginalDisplaySize", type = RectSize.class),
    @StructureMember(name = "MetafileSize", type = int.class, unsigned = true),
    @StructureMember(name = "SegCount", type = short.class, unsigned = true)
})
public interface CDWinMetaHeader extends RichTextRecord<LSIG> {
  public enum MappingMode implements INumberEnum<Short> {
    TEXT(WindowsConstants.MM_TEXT),
    LOMETRIC(WindowsConstants.MM_LOMETRIC),
    HIMETRIC(WindowsConstants.MM_HIMETRIC),
    LOENGLISH(WindowsConstants.MM_LOENGLISH),
    HIENGLISH(WindowsConstants.MM_HIENGLISH),
    TWIPS(WindowsConstants.MM_TWIPS),
    ISOTROPIC(WindowsConstants.MM_ISOTROPIC),
    ANISOTROPIC(WindowsConstants.MM_ANISOTROPIC);
    private final short value;
    private MappingMode(short value) {
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
  LSIG getHeader();
  
  @StructureGetter("mm")
  Optional<MappingMode> getMappingMode();
  
  @StructureGetter("mm")
  short getMappingModeRaw();
  
  @StructureSetter("mm")
  CDWinMetaHeader setMappingMode(MappingMode mode);
  
  @StructureGetter("xExt")
  short getXExtent();
  
  @StructureSetter("xExt")
  CDWinMetaHeader setXExtent(short xExt);
  
  @StructureGetter("yExt")
  short getYExtent();
  
  @StructureSetter("yExt")
  CDWinMetaHeader setYExtent(short yExt);
  
  @StructureGetter("OriginalDisplaySize")
  RectSize getOriginalDisplaySize();
  
  @StructureGetter("MetafileSize")
  long getMetafileSize();
  
  @StructureSetter("MetafileSize")
  CDWinMetaHeader setMetafileSize(long metafileSize);
  
  @StructureGetter("SegCount")
  int getSegCount();
  
  @StructureSetter("SegCount")
  CDWinMetaHeader setSegCount(int segCount);
}
