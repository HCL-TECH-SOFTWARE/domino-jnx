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
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.BSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.15
 */
@StructureDefinition(name = "CDIMAGEHEADER2", members = {
    @StructureMember(name = "Header", type = BSIG.class),
    @StructureMember(name = "ImageType", type = CDImageHeader.ImageType.class),
    @StructureMember(name = "ImageDataSize", type = int.class, unsigned = true),
    @StructureMember(name = "SegCount", type = int.class, unsigned = true),
    @StructureMember(name = "Flags", type = int.class),
    @StructureMember(name = "Reserved", type = int.class)
})
public interface CDImageHeader2 extends RichTextRecord<BSIG> {
  @StructureGetter("Flags")
  int getFlags();

  @StructureGetter("Header")
  @Override
  BSIG getHeader();

  @StructureGetter("ImageDataSize")
  long getImageDataSize();

  @StructureGetter("ImageType")
  Optional<CDImageHeader.ImageType> getImageType();

  /**
   * Retrieves the image type as a raw {@code short}.
   * 
   * @return the image type as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("ImageType")
  short getImageTypeRaw();

  @StructureGetter("Reserved")
  int getReserved();

  @StructureGetter("SegCount")
  long getSegCount();

  @StructureSetter("Flags")
  CDImageHeader2 setFlags(int flags);

  @StructureSetter("ImageDataSize")
  CDImageHeader2 setImageDataSize(long imageDataSize);

  @StructureSetter("ImageType")
  CDImageHeader2 setImageType(CDImageHeader.ImageType imageType);

  /**
   * Sets the image type as a raw {@code short}.
   * 
   * @param imageType the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("ImageType")
  CDImageHeader2 setImageTypeRaw(short imageType);

  @StructureSetter("Reserved")
  CDImageHeader2 setReserved(int reserved);

  @StructureSetter("SegCount")
  CDImageHeader2 setSegCount(long segCount);
}
