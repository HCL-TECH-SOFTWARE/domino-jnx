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

import java.util.Collection;
import java.util.Set;

import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.33
 */
@StructureDefinition(
  name = "CDDECSFIELD",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Flags", type = CDDECSField.Flag.class, bitfield = true),
    @StructureMember(name = "ExternalNameLength", type = short.class, unsigned = true),
    @StructureMember(name = "MetadataNameLength", type = short.class, unsigned = true),
    @StructureMember(name = "DCRNameLength", type = short.class, unsigned = true),
    @StructureMember(name = "Spare", type = short[].class, length = 8)
  }
)
public interface CDDECSField extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Short> {
    KEY_FIELD(DesignConstants.FDECS_KEY_FIELD),
    STORE_LOCALLY(DesignConstants.FDECS_STORE_LOCALLY)
    ;

    private final short value;

    Flag(final int value) {
      this.value = (short)value;
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
  WSIG getHeader();
  
  @StructureGetter("Flags")
  Set<Flag> getFlags();
  
  @StructureSetter("Flags")
  CDDECSField setFlags(Collection<Flag> flags);
  
  @StructureGetter("ExternalNameLength")
  int getExternalNameLength();
  
  @StructureSetter("ExternalNameLength")
  CDDECSField setExternalNameLength(int len);
  
  @StructureGetter("MetadataNameLength")
  int getMetadataNameLength();
  
  @StructureSetter("MetadataNameLength")
  CDDECSField setMetadataNameLength(int len);
  
  @StructureGetter("DCRNameLength")
  int getDcrNameLength();
  
  @StructureSetter("DCRNameLength")
  CDDECSField setDcrNameLength(int len);
  
  default String getExternalName() {
    return StructureSupport.extractStringValue(
      this,
      0,
      getExternalNameLength()
    );
  }
  
  default CDDECSField setExternalName(String name) {
    return StructureSupport.writeStringValue(
      this,
      0,
      getExternalNameLength(),
      name,
      this::setExternalNameLength
    );
  }
  
  default String getMetadataName() {
    return StructureSupport.extractStringValue(
      this,
      getExternalNameLength(),
      getMetadataNameLength()
    );
  }
  
  default CDDECSField setMetadataName(String name) {
    return StructureSupport.writeStringValue(
      this,
      getExternalNameLength(),
      getMetadataNameLength(),
      name,
      this::setMetadataNameLength
    );
  }
  
  default String getDcrName() {
    return StructureSupport.extractStringValue(
      this,
      getExternalNameLength() + getMetadataNameLength(),
      getDcrNameLength()
    );
  }
  
  default CDDECSField setDcrName(String name) {
    return StructureSupport.writeStringValue(
      this,
      getExternalNameLength() + getMetadataNameLength(),
      getDcrNameLength(),
      name,
      this::setDcrNameLength
    );
  }
}
