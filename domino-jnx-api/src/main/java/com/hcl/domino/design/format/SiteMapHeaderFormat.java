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
    @StructureMember(name = "Items", type = short.class, unsigned = false),
    @StructureMember(name = "Entries", type = short.class, unsigned = false),
    @StructureMember(name = "Length", type = short.class, unsigned = false),
    @StructureMember(name = "tdLastChecked", type = OpaqueTimeDate.class),
    @StructureMember(name = "Spare", type = short[].class, length = 16)
})
public interface SiteMapHeaderFormat extends MemoryStructure {

  @StructureGetter("MajorVersion")
  byte getMajorVersion();
  
  @StructureSetter("MajorVersion")
  SiteMapHeaderFormat setMajorVersion(byte majorVersion);
  
  @StructureGetter("MinorVersion")
  byte getMinorVersion();
  
  @StructureSetter("MinorVersion")
  SiteMapHeaderFormat setMinorVersion(byte minorVersion);
  
  @StructureGetter("SiteMapStyle")
  byte getSiteMapStyle();
  
  @StructureSetter("SiteMapStyle")
  SiteMapHeaderFormat setSiteMapStyle(byte siteMapStyle);
  
  @StructureGetter("OddSpare")
  byte getOddSpare();
  
  @StructureSetter("OddSpare")
  SiteMapHeaderFormat setOddSpare(byte oddSpare);
  
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
  
  @StructureGetter("Spare")
  short[] getSpare();

  @StructureSetter("Spare")
  SiteMapHeaderFormat setSpare(short[] Spare);
}
