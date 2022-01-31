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

import java.nio.ByteBuffer;

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.15
 */
@StructureDefinition(
  name = "CDBLOBPART",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "OwnerSig", type = short.class),
    @StructureMember(name = "Length", type = short.class, unsigned = true),
    @StructureMember(name = "BlobMax", type = short.class, unsigned = true),
    @StructureMember(name = "Reserved", type = byte[].class, length = 8)
  }
)
public interface CDBlobPart extends RichTextRecord<WSIG> {
  @StructureGetter("BlobMax")
  int getBlobMax();

  @StructureGetter("Header")
  @Override
  WSIG getHeader();

  @StructureGetter("Length")
  int getLength();

  @StructureGetter("OwnerSig")
  short getOwnerSig();

  @StructureGetter("Reserved")
  byte[] getReserved();

  @StructureSetter("BlobMax")
  CDBlobPart setBlobMax(int blobMax);

  @StructureSetter("Length")
  CDBlobPart setLength(int length);

  @StructureSetter("OwnerSig")
  CDBlobPart setOwnerSig(short ownerSig);

  @StructureSetter("Reserved")
  CDBlobPart setReserved(byte[] reserved);

  /**
   * Returns the raw data of this blob part
   *
   * @return the blob part data as a byte array
   */
  default byte[] getBlobPartData() {
    final ByteBuffer buf = this.getVariableData();
    final int len = this.getLength();
    final byte[] result = new byte[len];
    buf.get(result);
    return result;
  }

  default CDBlobPart setBlobPartData(final byte[] data) {
    resizeVariableData(data.length);
    final ByteBuffer buf = this.getVariableData();
    buf.put(data);
    setLength(data.length);
    return this;
  }
}
