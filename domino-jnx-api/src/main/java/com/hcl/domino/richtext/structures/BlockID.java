package com.hcl.domino.richtext.structures;

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

/**
 * @since 1.48.0
 */
@StructureDefinition(
  name = "BLOCKID",
  members = {
    @StructureMember(name = "pool", type = int.class),
    @StructureMember(name = "block", type = short.class)
  }
)
public interface BlockID extends MemoryStructure {
  @StructureGetter("pool")
  int getPool();
  
  @StructureSetter("pool")
  BlockID setPool(int pool);
  
  @StructureGetter("block")
  short getBlock();
  
  @StructureSetter("block")
  BlockID setBlock(short block);
}
