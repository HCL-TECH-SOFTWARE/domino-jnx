package com.hcl.domino.richtext.records;

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.structures.BSIG;

@StructureDefinition(
    name = "CDLAYER",
    members = {
      @StructureMember(name = "Header", type = BSIG.class),
      @StructureMember(name = "Reserved", type = int[].class, length = 4)
    }
  )
public interface CDLayer extends RichTextRecord<BSIG> {

  @StructureGetter("Header")
  @Override
  BSIG getHeader();
}
