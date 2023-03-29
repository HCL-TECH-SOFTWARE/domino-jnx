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

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.CropRect;
import com.hcl.domino.richtext.structures.LSIG;
import com.hcl.domino.richtext.structures.RectSize;

/**
 * @author Jesse Gallagher
 * @since 1.0.15
 */
@StructureDefinition(name = "CDGRAPHIC", members = {
    @StructureMember(name = "Header", type = LSIG.class),
    @StructureMember(name = "DestSize", type = RectSize.class),
    @StructureMember(name = "CropSize", type = RectSize.class),
    @StructureMember(name = "CropOffset", type = CropRect.class),
    @StructureMember(name = "fResize", type = short.class),
    @StructureMember(name = "Version", type = CDGraphic.Version.class),
    @StructureMember(name = "bFlags", type = CDGraphic.Flag.class, bitfield = true),
    @StructureMember(name = "wReserved", type = short.class)
})
public interface CDGraphic extends RichTextRecord<LSIG> {
  enum Flag implements INumberEnum<Byte> {
    DESTSIZE_IS_PIXELS((byte) 0x01),
    SPANSLINES((byte) 0x02);

    private final byte value;

    Flag(final byte value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Byte getValue() {
      return this.value;
    }
  }

  enum Version implements INumberEnum<Byte> {
    VERSION1((byte) 0),
    VERSION2((byte) 1),
    VERSION3((byte) 2);

    private final byte value;

    Version(final byte value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Byte getValue() {
      return this.value;
    }
  }

  @StructureGetter("CropOffset")
  CropRect getCropOffset();

  @StructureGetter("CropSize")
  RectSize getCropSize();

  @StructureGetter("DestSize")
  RectSize getDestSize();

  @StructureGetter("bFlags")
  Set<Flag> getFlags();

  @StructureGetter("Header")
  @Override
  LSIG getHeader();

  @StructureGetter("fResize")
  short getResize();

  @StructureGetter("Version")
  Optional<Version> getVersion();

  /**
   * Retrieves the version as a raw {@code byte}.
   * 
   * @return the version as a {@code byte}
   * @since 1.24.0
   */
  @StructureGetter("Version")
  byte getVersionRaw();

  @StructureSetter("bFlags")
  CDGraphic setFlags(Collection<Flag> flags);

  @StructureSetter("fResize")
  CDGraphic setResize(short resize);

  @StructureSetter("Version")
  CDGraphic setVersion(Version version);

  /**
   * Sets the version as a raw {@code byte}.
   * 
   * @param version the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("Version")
  CDGraphic setVersionRaw(short version);
}
