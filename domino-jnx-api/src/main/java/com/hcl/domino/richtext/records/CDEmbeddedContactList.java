package com.hcl.domino.richtext.records;

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.WSIG;

@StructureDefinition(name = "CDEMBEDDEDCONTACTLIST", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Flags", type = int.class),
    @StructureMember(name = "SelectedBackground", type = ColorValue.class),
    @StructureMember(name = "SelectedText", type = ColorValue.class),
    @StructureMember(name = "ControlBackground", type = ColorValue.class),
    @StructureMember(name = "Spare", type = int[].class, length = 10)
})
public interface CDEmbeddedContactList extends RichTextRecord<WSIG> {

  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("SelectedBackground")
  ColorValue getSelectedBackground();

  @StructureGetter("SelectedText")
  ColorValue getSelectedText();
  
  @StructureGetter("ControlBackground")
  ColorValue getControlBackground();
}
