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

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.MemoryStructure;
import com.hcl.domino.richtext.structures.OpaqueTimeDate;

@StructureDefinition(name = "SITEMAP_HEADER_FORMAT", members = {
    @StructureMember(name = "MajorVersion", type = byte.class, unsigned = true),
    @StructureMember(name = "MinorVersion", type = byte.class, unsigned = true),
    @StructureMember(name = "SiteMapStyle", type = byte.class, unsigned = true),
    @StructureMember(name = "OddSpare", type = byte.class, unsigned = true),
    @StructureMember(name = "Items", type = short.class),
    @StructureMember(name = "Entries", type = short.class),
    @StructureMember(name = "Length", type = short.class),
    @StructureMember(name = "tdLastChecked", type = OpaqueTimeDate.class),
    @StructureMember(name = "Spare", type = short[].class, length = 16)
})
public interface SiteMapHeaderFormat extends MemoryStructure {

  @StructureGetter("MajorVersion")
  short getMajorVersion();
  
  @StructureSetter("MajorVersion")
  SiteMapHeaderFormat setMajorVersion(short majorVersion);
  
  @StructureGetter("MinorVersion")
  short getMinorVersion();
  
  @StructureSetter("MinorVersion")
  SiteMapHeaderFormat setMinorVersion(short minorVersion);
  
  @StructureGetter("SiteMapStyle")
  short getSiteMapStyle();
  
  @StructureSetter("SiteMapStyle")
  SiteMapHeaderFormat setSiteMapStyle(short siteMapStyle);
  
  @StructureGetter("OddSpare")
  short getOddSpare();
  
  @StructureSetter("OddSpare")
  SiteMapHeaderFormat setOddSpare(short oddSpare);
  
  @StructureGetter("Items")
  short getItems();
  
  @StructureSetter("Items")
  SiteMapHeaderFormat setItems(short items);
  
  @StructureGetter("Entries")
  short getEntries();
  
  @StructureSetter("Entries")
  SiteMapHeaderFormat setEntries(short entries);
  
  @StructureGetter("Length")
  short getLength();
  
  @StructureSetter("Length")
  SiteMapHeaderFormat setLength(short length);
  
  @StructureGetter("tdLastChecked")
  OpaqueTimeDate getTdLastChecked();
}
