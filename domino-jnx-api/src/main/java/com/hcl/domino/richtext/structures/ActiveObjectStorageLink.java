package com.hcl.domino.richtext.structures;

import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

/**
 * Represents the {@code ACTIVEOBJECTSTORAGELINK} structure.
 * 
 * @author Jesse Gallagher
 * @since 1.0.44
 */
@StructureDefinition(
  name = "ACTIVEOBJECTSTORAGELINK",
  members = {
    @StructureMember(name = "Length", type = short.class, unsigned = true),
    @StructureMember(name = "LinkType", type = short.class),
    @StructureMember(name = "Reserved", type = int.class)
  }
)
public interface ActiveObjectStorageLink extends ResizableMemoryStructure {
  @StructureGetter("Length")
  int getLength();
  
  @StructureSetter("Length")
  ActiveObjectStorageLink setLength(int len);
  
  default String getLink() {
    return StructureSupport.extractStringValue(
      this,
      0,
      getLength()
    );
  }
  
  default ActiveObjectStorageLink setLink(String link) {
    return StructureSupport.writeStringValue(
      this,
      0,
      getLength(),
      link,
      this::setLength
    );
  }
}
