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
package com.hcl.domino.design.format;

import java.util.Collection;
import java.util.Set;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.ResizableMemoryStructure;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
@StructureDefinition(name = "VIEW_COLUMN_FORMAT6", members = {
    @StructureMember(name = "Signature", type = short.class),
    // NB: this is a WORD in the API despite the "dw" prefix
    @StructureMember(name = "dwLength", type = short.class, unsigned = true),
    @StructureMember(name = "dwFlags6", type = ViewColumnFormat6.Flag.class, bitfield = true),
    @StructureMember(name = "SequenceNumber", type = short.class, unsigned = true),
    @StructureMember(name = "IfViewIsNarrowDo", type = short.class),
    @StructureMember(name = "wAttributesLength", type = short.class, unsigned = true),
    @StructureMember(name = "wPubFieldLength", type = short.class, unsigned = true),
    @StructureMember(name = "LineNumber", type = short.class, unsigned = true),
    @StructureMember(name = "TileViewer", type = short.class),
    @StructureMember(name = "dwReserved", type = int[].class, length = 16),
})
public interface ViewColumnFormat6 extends ResizableMemoryStructure {
  enum Flag implements INumberEnum<Integer> {
    BeginWrapUnder(NotesConstants.VCF6_M_BeginWrapUnder),
    PublishColumn(NotesConstants.VCF6_M_PublishColumn),
    ExtendColWidthToAvailWindowWidth(NotesConstants.VCF6_M_ExtendColWidthToAvailWindowWidth),
    BuildCollationOnDemand(NotesConstants.VCF6_M_BuildCollationOnDemand),
    UserDefinableExtended(NotesConstants.VCF6_M_UserDefinableExtended),
    IgnorePrefixes(NotesConstants.VCF6_M_IgnorePrefixes),
    AbbreviatedDate(NotesConstants.VCF6_M_AbbreviatedDate),
    AbbreviatedDateSet(NotesConstants.VCF6_M_AbbreviatedDateSet);

    private final int value;

    Flag(final int value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Integer getValue() {
      return this.value;
    }
  }

  default String getAttributes() {
    return StructureSupport.extractStringValue(this,
        0,
        this.getAttributesLength());
  }

  @StructureGetter("wAttributesLength")
  int getAttributesLength();

  @StructureGetter("dwFlags6")
  Set<Flag> getFlags();

  @StructureGetter("IfViewIsNarrowDo")
  short getIfViewIsNarrowDo();

  @StructureGetter("dwLength")
  int getLength();

  @StructureGetter("LineNumber")
  int getLineNumber();

  default String getPublishFieldName() {
    return StructureSupport.extractStringValue(this,
        this.getAttributesLength(),
        this.getPublishFieldNameLength());
  }

  @StructureGetter("wPubFieldLength")
  int getPublishFieldNameLength();

  @StructureGetter("SequenceNumber")
  int getSequenceNumber();

  @StructureGetter("Signature")
  short getSignature();

  @StructureGetter("TileViewer")
  short getTileViewer();

  default ViewColumnFormat6 setAttributes(final String attributes) {
    return StructureSupport.writeStringValue(
        this,
        0,
        this.getAttributesLength(),
        attributes,
        (final int newLen) -> {
          this.setLength(148 + newLen + this.getPublishFieldNameLength());
          this.setAttributesLength(newLen);
        });
  }

  @StructureSetter("wAttributesLength")
  ViewColumnFormat6 setAttributesLength(int len);

  @StructureSetter("dwFlags6")
  ViewColumnFormat6 setFlags(Collection<Flag> flags);

  @StructureSetter("IfViewIsNarrowDo")
  ViewColumnFormat6 setIfViewIsNarrowDo(short ifDo);

  @StructureSetter("dwLength")
  ViewColumnFormat6 setLength(int length);

  @StructureSetter("LineNumber")
  ViewColumnFormat6 setLineNumber(int lineNo);

  default ViewColumnFormat6 setPublishFieldName(final String name) {
    return StructureSupport.writeStringValue(
        this,
        this.getAttributesLength(),
        this.getPublishFieldNameLength(),
        name,
        (final int newLen) -> {
          this.setLength(148 + this.getAttributesLength() + newLen);
          this.setPublishFieldNameLength(newLen);
        });
  }

  @StructureSetter("wPubFieldLength")
  ViewColumnFormat6 setPublishFieldNameLength(int len);

  @StructureSetter("SequenceNumber")
  ViewColumnFormat6 setSequenceNumber(int length);

  @StructureSetter("Signature")
  ViewColumnFormat6 setSignature(short signature);

  @StructureSetter("TileViewer")
  ViewColumnFormat6 setTileViewer(short tileViewer);
}
