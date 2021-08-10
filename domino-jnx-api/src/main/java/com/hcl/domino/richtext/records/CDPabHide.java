package com.hcl.domino.richtext.records;

import java.util.function.IntConsumer;

import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Karsten Lehmann
 * @since 1.0.32
 */
@StructureDefinition(name = "CDPABHIDE", members = {
		@StructureMember(name = "Header", type = WSIG.class),
		@StructureMember(name = "PABID", type = short.class, unsigned = true),
		@StructureMember(name = "Reserved", type = byte[].class, length = 8)
})
public interface CDPabHide extends RichTextRecord<WSIG> {

	@StructureGetter("PABID")
	int getPabId();

	@StructureSetter("PABID")
	CDPabHide setPabId(int id);

	@StructureGetter("Reserved")
	byte[] getReserved();

	@StructureSetter("Reserved")
	CDPabHide setReserved(byte[] reserved);

	default int getFormulaLength() {
		return getCDRecordLength() - getRecordHeaderLength() - 8 - 2;
	}
	
	default String getFormula() {
		return StructureSupport.extractCompiledFormula(this, 0, getFormulaLength());
	}

	default void setFormula(String formula) {
		StructureSupport.writeCompiledFormula(this, 0, getFormulaLength(), formula, (IntConsumer) null);
	}
	
}
