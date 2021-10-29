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

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

/**
 * CDPMMETAHEADER
 * 
 * @author 
 * @since 1.0.46
 */

@StructureDefinition(
  name = "CDPMMETAHEADER", 
  members = { 
    @StructureMember(name = "Header", type = LSIG.class),                        /* Signature and Length of this record */
    @StructureMember(name = "mm", type = short.class),                           /* PM mapping mode, i.e. PU_??? */
    @StructureMember(name = "xExt", type = short.class),                         /* size in mapping mode units */
    @StructureMember(name = "yExt", type = short.class),                         /* size in mapping mode units */
    @StructureMember(name = "OriginalDisplaySize", type = RECTSIZE.class),       /* Original display size of metafile in twips */
    @StructureMember(name = "MetafileSize", type = int.class),                   /* Total size of metafile raw data in bytes */
    @StructureMember(name = "SegCount", type = short.class, unsigned = true),    /* Number of CDPMMETASEG records */
})
public interface CDPmMetaHeader extends RichTextRecord<LSIG> {

  @StructureGetter("Header")
  LSIG getHeader();

  @StructureGetter("mm")
  short getmm();

  @StructureGetter("xExt")
  short getxExt();

  @StructureGetter("yExt")
  short getyExt();

  @StructureGetter("OriginalDisplaySize")
  RECTSIZE getOriginalDisplaySize();

  @StructureGetter("MetafileSize")
  int getMetafileSize();

  @StructureGetter("SegCount")
  int getSegCount();

  @StructureSetter("mm")
  CDPmMetaHeader setmm(short mm);

  @StructureSetter("xExt")
  CDPmMetaHeader setxExt(short xExt);

  @StructureSetter("yExt")
  CDPmMetaHeader setyExt(short yExt);

  @StructureSetter("MetafileSize")
  CDPmMetaHeader setMetafileSize(int metafileSize);

  @StructureSetter("SegCount")
  CDPmMetaHeader setSegCount(int segCount);

}
