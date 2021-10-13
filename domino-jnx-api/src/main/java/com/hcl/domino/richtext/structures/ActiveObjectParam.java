package com.hcl.domino.richtext.structures;

import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

/**
 * Represents the {@code ACTIVEOBJECTPARAM} structure.
 * 
 * @author Jesse Gallagher
 * @since 1.0.44
 */
@StructureDefinition(
  name = "ACTIVEOBJECTPARAM",
  members = {
    @StructureMember(name = "Length", type = short.class, unsigned = true),
    @StructureMember(name = "FormulaLength", type = short.class, unsigned = true),
    @StructureMember(name = "Reserved", type = short.class)
  }
)
public interface ActiveObjectParam extends ResizableMemoryStructure {
  @StructureGetter("Length")
  int getLength();
  
  @StructureSetter("Length")
  ActiveObjectParam setLength(int len);
  
  @StructureGetter("FormulaLength")
  int getFormulaLength();
  
  @StructureSetter("FormulaLength")
  ActiveObjectParam setFormulaLength(int len);
  
  default String getParam() {
    return StructureSupport.extractStringValue(
      this,
      0,
      getLength()
    );
  }
  
  default ActiveObjectParam setParam(String param) {
    return StructureSupport.writeStringValue(
      this,
      0,
      getLength(),
      param,
      this::setLength
    );
  }
  
  default String getFormula() {
    return StructureSupport.extractCompiledFormula(
      this,
      getLength(),
      getFormulaLength()
    );
  }
  
  default ActiveObjectParam setFormula(String formula) {
    return StructureSupport.writeCompiledFormula(
      this,
      getLength(),
      getFormulaLength(),
      formula,
      this::setFormulaLength
    );
  }
}
