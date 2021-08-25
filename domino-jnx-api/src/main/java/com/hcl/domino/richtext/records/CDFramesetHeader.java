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

import java.util.Optional;

import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.misc.INumberEnum;
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
  name = "CDFRAMESETHEADER",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Version", type = CDFramesetHeader.Version.class),
    @StructureMember(name = "RecCount", type = short.class, unsigned = true),
    @StructureMember(name = "Reserved", type = int[].class, length = 4)
  }
)
public interface CDFramesetHeader extends RichTextRecord<WSIG> {
  enum Version implements INumberEnum<Short> {
    VERSION2(DesignConstants.FRAMESETHEADER_VERSION);
    private final short value;
    private Version(short value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return value;
    }

    @Override
    public Short getValue() {
      return value;
    }
  }
  
  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("Version")
  Optional<Version> getVersion();
  
  @StructureSetter("Version")
  CDFramesetHeader setVersion(Version version);
  
  @StructureGetter("RecCount")
  int getRecordCount();
  
  @StructureSetter("RecCount")
  CDFramesetHeader setRecordCount(int count);
}
