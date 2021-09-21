package com.hcl.domino.richtext.structures;

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

/**
 * Represents ARGB color values stored as DWORDs.
 * 
 * @author Jesse Gallagher
 * @since 1.0.41
 */
@StructureDefinition(
  name = "RAW_COLOR_VALUE", // NB: not a real structure name
  endianSensitive = true,
  members = {
    @StructureMember(name = "Red", type = byte.class, unsigned = true),
    @StructureMember(name = "Green", type = byte.class, unsigned = true),
    @StructureMember(name = "Blue", type = byte.class, unsigned = true),
    @StructureMember(name = "Alpha", type = byte.class, unsigned = true),
  }
)
public interface RawColorValue extends MemoryStructure {
  @StructureGetter("Red")
  short getRed();
  
  @StructureSetter("Red")
  RawColorValue setRed(short red);

  @StructureGetter("Green")
  short getGreen();
  
  @StructureSetter("Green")
  RawColorValue setGreen(short green);
  
  @StructureGetter("Blue")
  short getBlue();
  
  @StructureSetter("Blue")
  RawColorValue setBlue(short blue);
}
