package com.hcl.domino.richtext.records;

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.BSIG;

/**
 * @author Karsten Lehmann
 * @since 1.0.32
 */
@StructureDefinition(name = "CDPABREFERENCE", members = {
    @StructureMember(name = "Header", type = BSIG.class),
    @StructureMember(name = "PABID", type = short.class, unsigned = true)
})
public interface CDPabReference extends RichTextRecord<BSIG> {

	  @StructureGetter("PABID")
	  int getPabId();
	  
	  @StructureSetter("PABID")
	  CDPabReference setPabId(int id);
}
