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
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.LSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.2
 */
@StructureDefinition(name = "CDIMAGEHEADER", members = {
    @StructureMember(name = "Header", type = LSIG.class),
    @StructureMember(name = "ImageType", type = CDImageHeader.ImageType.class),
    @StructureMember(name = "Width", type = short.class, unsigned = true),
    @StructureMember(name = "Height", type = short.class, unsigned = true),
    @StructureMember(name = "ImageDataSize", type = int.class, unsigned = true),
    @StructureMember(name = "SegCount", type = int.class, unsigned = true),
    @StructureMember(name = "Flags", type = int.class),
    @StructureMember(name = "Reserved", type = int.class)
})
public interface CDImageHeader extends RichTextRecord<LSIG> {
  enum ImageType implements INumberEnum<Short> {
    GIF(RichTextConstants.CDIMAGETYPE_GIF),
    JPEG(RichTextConstants.CDIMAGETYPE_JPEG),
    BMP(RichTextConstants.CDIMAGETYPE_BMP),
    PNG(RichTextConstants.CDIMAGETYPE_PNG);

    private final short value;

    ImageType(final short value) {
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

  @StructureGetter("Flags")
  int getFlags();

  @StructureGetter("Header")
  @Override
  LSIG getHeader();

  @StructureGetter("Height")
  int getHeight();

  @StructureGetter("ImageDataSize")
  long getImageDataSize();

  @StructureGetter("ImageType")
  ImageType getImageType();

  @StructureGetter("Reserved")
  int getReserved();

  @StructureGetter("SegCount")
  long getSegCount();

  @StructureGetter("Width")
  int getWidth();

  @StructureSetter("Flags")
  CDImageHeader setFlags(int flags);

  @StructureSetter("Height")
  CDImageHeader setHeight(int height);

  @StructureSetter("ImageDataSize")
  CDImageHeader setImageDataSize(long imageDataSize);

  @StructureSetter("ImageType")
  CDImageHeader setImageType(ImageType imageType);

  @StructureSetter("Reserved")
  CDImageHeader setReserved(int reserved);

  @StructureSetter("SegCount")
  CDImageHeader setSegCount(long segCount);

  @StructureSetter("Width")
  CDImageHeader setWidth(int width);
}
