package com.hcl.domino.richtext.records;

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.structures.BSIG;
import com.hcl.domino.richtext.structures.LengthValue;

@StructureDefinition(
    name = "CDBOXSIZE",
    members = {
      @StructureMember(name = "Header", type = BSIG.class),
      @StructureMember(name = "Width", type = LengthValue.class),
      @StructureMember(name = "Height", type = LengthValue.class),
      @StructureMember(name = "Reserved", type = LengthValue[].class, length = 4),
      @StructureMember(name = "dwReserved", type = int[].class, length = 4)
    }
  )
public interface CDBoxSize extends RichTextRecord<BSIG> {

  @StructureGetter("Header")
  @Override
  BSIG getHeader();
  
  @StructureGetter("Width")
  LengthValue getWidth();
  
  @StructureGetter("Height")
  LengthValue getHeight();
}
