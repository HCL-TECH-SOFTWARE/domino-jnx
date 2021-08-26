package com.hcl.domino.design.format;

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.structures.MemoryStructure;

@StructureDefinition(name = "SITEMAP_OUTLINE_HEADER", members = {
    @StructureMember(name = "Flags", type = int.class, unsigned = false),
    @StructureMember(name = "Spare", type = byte[].class, length = 20)
})
public interface SiteMapOutlineHeader extends MemoryStructure {
  @StructureGetter("Flags")
  int getFlags();
  
  @StructureGetter("Spare")
  byte[] getSpare();
}
