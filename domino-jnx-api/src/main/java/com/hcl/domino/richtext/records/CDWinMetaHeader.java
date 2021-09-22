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

import com.hcl.domino.richtext.RectangleSize;
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
    @StructureMember(name = "mm", type = short.class, unsigned = false),
    @StructureMember(name = "xExt", type = short.class, unsigned = false),
    @StructureMember(name = "yExt", type = short.class, unsigned = false),
    @StructureMember(name = "OriginalDisplaySize", type = RectSize.class),
    @StructureMember(name = "MetafileSize", type = int.class),
    @StructureMember(name = "SegCount", type = short.class, unsigned = true)
})
public interface CDWinMetaHeader extends RichTextRecord<LSIG> {

  @StructureGetter("Header")
  @Override
  LSIG getHeader();
  
  @StructureGetter("mm")
  short getmm();
  
  @StructureSetter("mm")
  CDWinMetaHeader setmm(short length);
  
  @StructureGetter("xExt")
  short getxExt();
  
  @StructureSetter("xExt")
  CDWinMetaHeader setxExt(short xExt);
  
  @StructureGetter("yExt")
  short getyExt();
  
  @StructureSetter("yExt")
  CDWinMetaHeader setyExt(short yExt);
  
  @StructureGetter("OriginalDisplaySize")
  RectangleSize getOriginalDisplaySize();
  
  @StructureGetter("MetafileSize")
  int getMetafileSize();
  
  @StructureSetter("MetafileSize")
  CDWinMetaHeader setMetafileSize(int metafileSize);
  
  @StructureGetter("SegCount")
  int getSegCount();
  
  @StructureSetter("SegCount")
  CDWinMetaHeader setSegCount(int segCount);
}
