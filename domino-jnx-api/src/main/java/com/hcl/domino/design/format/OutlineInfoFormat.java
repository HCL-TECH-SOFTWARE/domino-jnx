package com.hcl.domino.design.format;

import java.nio.ByteBuffer;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.structures.MemoryStructure;

@StructureDefinition(name = "OUTLINE_INFO_FORMAT", members = {
    @StructureMember(name = "dwSize", type = int.class),
    @StructureMember(name = "szName", type = char.class),
    @StructureMember(name = "szAlias", type = char.class),
    @StructureMember(name = "Flags", type = ViewTableFormat.Flag.class, bitfield = true),
    @StructureMember(name = "Flags2", type = ViewTableFormat.Flag2.class, bitfield = true),
})
public class OutlineInfoFormat implements MemoryStructure {

  @Override
  public ByteBuffer getData() {
    // TODO Auto-generated method stub
    return null;
  }

}
