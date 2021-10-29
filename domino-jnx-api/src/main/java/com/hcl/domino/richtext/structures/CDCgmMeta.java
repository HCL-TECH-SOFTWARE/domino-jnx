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
 * CDCGMMETA
 * 
 * @author 
 * @since 1.0.46
 */

@StructureDefinition(
  name = "CDCGMMETA", 
  members = { 
    @StructureMember(name = "Header", type = LSIG.class),                        /* Signature and Length */
    @StructureMember(name = "mm", type = short.class),                           /* see above CGM_MAPMODE_??? */
    @StructureMember(name = "xExt", type = short.class),                         /* Extents of drawing in world coordinates */
    @StructureMember(name = "yExt", type = short.class),                         /* Extents of drawing in world coordinates */
    @StructureMember(name = "OriginalSize", type = RECTSIZE.class),              /* Original display size of metafile in twips */
FIX ME >>> /*	CGM Metafile Bits Follow, must be <= 64K bytes total */
})
public interface CDCgmMeta extends RichTextRecord<LSIG> {
  static int CGM_MAPMODE_ABSTRACT = 0; /* Virtual coordinate system. This is default */
  static int CGM_MAPMODE_METRIC = 1; /* Currently unsupported */

  @StructureGetter("Header")
  LSIG getHeader();

  @StructureGetter("mm")
  short getmm();

  @StructureGetter("xExt")
  short getxExt();

  @StructureGetter("yExt")
  short getyExt();

  @StructureGetter("OriginalSize")
  RECTSIZE getOriginalSize();

  @StructureSetter("mm")
  CDCgmMeta setmm(short mm);

  @StructureSetter("xExt")
  CDCgmMeta setxExt(short xExt);

  @StructureSetter("yExt")
  CDCgmMeta setyExt(short yExt);

}
