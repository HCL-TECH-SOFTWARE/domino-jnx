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

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.34
 */
@StructureDefinition(
  name = "CDREGIONBEGIN",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Version", type = CDRegionBegin.Version.class),
    @StructureMember(name = "Flags", type = short.class),
    @StructureMember(name = "RegionNum", type = short.class, unsigned = true),
    @StructureMember(name = "RegionName", type = byte[].class, length = RichTextConstants.MAXREGIONNAME+1)
  }
)
public interface CDRegionBegin extends RichTextRecord<WSIG> {
  enum Version implements INumberEnum<Short> {
    VERSION_1((short)1);

    private final short value;

    Version(final short value) {
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
  
  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("Version")
  Optional<Version> getVersion();
  
  /**
   * Retrieves the version as a raw {@code short}.
   * 
   * @return the version as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("Version")
  short getVersionRaw();
  
  @StructureSetter("Version")
  CDRegionBegin setVersion(Version version);
  
  /**
   * Sets the version as a raw {@code short}.
   * 
   * @param version the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("Version")
  CDRegionBegin setVersionRaw(short version);
  
  @StructureGetter("RegionNum")
  int getRegionNumber();
  
  @StructureSetter("RegionNum")
  CDRegionBegin setRegionNumber(int num);
  
  @StructureGetter("RegionName")
  byte[] getRegionNameRaw();
  
  @StructureSetter("RegionName")
  CDRegionBegin setRegionNameRaw(byte[] name);
  
  default String getRegionName() {
    return StructureSupport.readLmbcsValue(getRegionNameRaw());
  }
}
