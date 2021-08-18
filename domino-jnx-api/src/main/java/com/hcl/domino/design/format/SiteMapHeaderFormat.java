package com.hcl.domino.design.format;

import java.nio.ByteBuffer;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.structures.MemoryStructure;
import com.hcl.domino.richtext.structures.OpaqueTimeDate;

@StructureDefinition(name = "SITEMAP_HEADER_FORMAT", members = {
    @StructureMember(name = "MajorVersion", type = byte.class, unsigned = true),
    @StructureMember(name = "MinorVersion", type = byte.class, unsigned = true),
    @StructureMember(name = "SiteMapStyle", type = byte.class, unsigned = true),
    @StructureMember(name = "OddSpare", type = byte.class, unsigned = true),
    @StructureMember(name = "Items", type = short.class, unsigned = false),
    @StructureMember(name = "Entries", type = short.class, unsigned = false),
    @StructureMember(name = "Length", type = short.class, unsigned = false),
    @StructureMember(name = "tdLastChecked", type = OpaqueTimeDate.class),
    @StructureMember(name = "Spare", type = short[].class, length = 16)
})
public interface SiteMapHeaderFormat extends MemoryStructure {

  @StructureGetter("MajorVersion")
  byte getMajorVersion();
  
  @StructureGetter("MinorVersion")
  byte getMinorVersion();
  
  @StructureGetter("SiteMapStyle")
  byte getSiteMapStyle();
  
  @StructureGetter("OddSpare")
  byte getOddSpare();
  
  @StructureGetter("Items")
  short getItems();
  
  @StructureGetter("Entries")
  short getEntries();
  
  @StructureGetter("Length")
  short getLength();
  
  @StructureGetter("tdLastChecked")
  OpaqueTimeDate gettdLastChecked();
  
  @StructureGetter("Spare")
  short[] getSpare();

}
