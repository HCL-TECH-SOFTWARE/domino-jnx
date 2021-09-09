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
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * Rich text record of type CDMAPELEMENT
 */
@StructureDefinition(name = "CDMAPELEMENT", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Flags", type = int.class),
    @StructureMember(name = "MapNameLength", type = short.class, unsigned = true),
    @StructureMember(name = "LastDefaultRegionID", type = short.class, unsigned = true),
    @StructureMember(name = "LastRectRegionID", type = short.class, unsigned = true),
    @StructureMember(name = "LastCircleRegionID", type = short.class, unsigned = true),
    @StructureMember(name = "LastPolyRegionID", type = short.class, unsigned = true),
    @StructureMember(name = "Reserved", type = byte[].class, length = 16)
})
public interface CDMapElement extends RichTextRecord<WSIG> {
  
  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("Flags")
  int getFlags();
  
  @StructureSetter("Flags")
  CDMapElement setFlags(int flags);
  
  @StructureGetter("MapNameLength")
  int getMapNameLength();
  
  @StructureSetter("MapNameLength")
  CDMapElement setMapNameLength(int mapNameLength);
  
  @StructureGetter("LastDefaultRegionID")
  int getLastDefaultRegionID();
  
  @StructureSetter("LastDefaultRegionID")
  CDMapElement setLastDefaultRegionID(int lastDefaultRegionID);
  
  @StructureGetter("LastRectRegionID")
  int getLastRectRegionID();
  
  @StructureSetter("LastRectRegionID")
  CDMapElement setLastRectRegionID(int lastRectRegionID);
  
  @StructureGetter("LastCircleRegionID")
  int getLastCircleRegionID();
  
  @StructureSetter("LastCircleRegionID")
  CDMapElement setLastCircleRegionID(int lastCircleRegionID);
  
  @StructureGetter("LastPolyRegionID")
  int getLastPolyRegionID();
  
  @StructureSetter("LastPolyRegionID")
  CDMapElement setLastPolyRegionID(int lastPolyRegionID);
  
  @StructureGetter("Reserved")
  byte[] getReserved();
  
  @StructureSetter("Reserved")
  CDMapElement setReserved(byte[] reserved);
  
  default Optional<String> getMapName() {
    int mapNameLength = this.getMapNameLength();
    if (mapNameLength > 0) {
      return Optional.of(
          StructureSupport.extractStringValue(
            this,
            0,
            mapNameLength
          )
        );
    }
    
    return Optional.empty();
  }
  
  default CDMapElement setMapName(String mapName) {
    return StructureSupport.writeStringValue(
        this,
        0,
        mapName.length(),
        mapName,
        this::setMapNameLength
      );
  }
}
