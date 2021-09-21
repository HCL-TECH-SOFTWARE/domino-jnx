package com.hcl.domino.richtext.records;

import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * Rich text record of type CDEMBEDEXTRAINFO
 */
@StructureDefinition(name = "CDEMBEDEXTRAINFO", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "NameLength", type = short.class, unsigned = true),
    @StructureMember(name = "Flags", type = int.class),
    @StructureMember(name = "Reserved", type = int[].class, length = 5)
})
public interface CDEmbeddedExtraInfo extends RichTextRecord<WSIG> {

  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("NameLength")
  int getNameLength();
  
  @StructureSetter("NameLength")
  CDEmbeddedExtraInfo setNameLength(int nameLength);
  
  default String getName() {
    return StructureSupport.extractStringValue(
        this,
        0,
        this.getNameLength()
        );
  }

  default CDEmbeddedExtraInfo setName(final String name) {
    return StructureSupport.writeStringValue(
        this,
        0,
        this.getNameLength(),
        name,
        this::setNameLength
        );
  }
}
