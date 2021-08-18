package com.hcl.domino.design.format;

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.structures.MemoryStructure;

@StructureDefinition(name = "SITEMAP_ENTRY", members = {
    @StructureMember(name = "EntryFixedSize", type = short.class, unsigned = true),
    @StructureMember(name = "EntryVarSize", type = short.class, unsigned = false),
    @StructureMember(name = "EntryFlags", type = int.class, unsigned = false),
    @StructureMember(name = "Id", type = int.class, unsigned = false),
    @StructureMember(name = "OldEntryType", type = short.class, unsigned = false),
    @StructureMember(name = "Level", type = short.class, unsigned = false),
    @StructureMember(name = "TitleSize", type = short.class, unsigned = false),
    @StructureMember(name = "PopupSize", type = short.class),
    @StructureMember(name = "OnClickSize", type = short.class, unsigned = false),
    @StructureMember(name = "SourceSize", type = short.class, unsigned = false),
    @StructureMember(name = "ImagesSize", type = short.class, unsigned = false),
    @StructureMember(name = "Unused3", type = short.class, unsigned = false),
    @StructureMember(name = "TargetFrameSize", type = short.class, unsigned = false),
    @StructureMember(name = "Unused4", type = short.class, unsigned = false),
    @StructureMember(name = "HideWhenSize", type = short.class, unsigned = false),
    @StructureMember(name = "Unused5", type = short.class, unsigned = false),
    @StructureMember(name = "AliasSize", type = short.class, unsigned = false),
    @StructureMember(name = "EntryType", type = short.class, unsigned = false),
    @StructureMember(name = "EntryClass", type = short.class, unsigned = false),
    @StructureMember(name = "EntryDesignType", type = short.class, unsigned = false),
    @StructureMember(name = "ReplEntryType", type = short.class, unsigned = false),
    @StructureMember(name = "ReplFlags", type = short.class, unsigned = false),
    @StructureMember(name = "ReplTruncType", type = short.class, unsigned = false),
    @StructureMember(name = "PreferredServerSize", type = short.class, unsigned = false),
    @StructureMember(name = "GridRow", type = short.class, unsigned = false),
    @StructureMember(name = "GridColumn", type = short.class, unsigned = false),
    @StructureMember(name = "ToolbarManagerSize", type = short.class, unsigned = false),
    @StructureMember(name = "ToolbarEntrySize", type = short.class, unsigned = false),
    @StructureMember(name = "wReplTruncDocs", type = short.class, unsigned = false),
    @StructureMember(name = "wReplTruncAtts", type = short.class, unsigned = false),
    @StructureMember(name = "Spare", type = short[].class, length = 4)
})
public interface SiteMapEntry extends MemoryStructure {
  @StructureGetter("EntryFixedSize")
  int getEntryFixedSize();

  @StructureGetter("EntryVarSize")
  short getEntryVarSize();
  
  @StructureGetter("EntryFlags")
  int getEntryFlags();
  
  @StructureGetter("Id")
  int getId();
  
  @StructureGetter("OldEntryType")
  short getOldEntryType();
  
  @StructureGetter("Level")
  short getLevel();
  
  @StructureGetter("TitleSize")
  short getTitleSize();
  
  @StructureGetter("PopupSize")
  short getPopupSize();
  
  @StructureGetter("OnClickSize")
  short getOnClickSize();
  
  @StructureGetter("SourceSize")
  short getSourceSize();
  
  @StructureGetter("ImagesSize")
  short getImagesSize();
  
  @StructureGetter("Unused3")
  short getUnused3();
  
  @StructureGetter("TargetFrameSize")
  short getTargetFrameSize();
  
  @StructureGetter("Unused4")
  short getUnused4();
  
  @StructureGetter("HideWhenSize")
  short getHideWhenSize();
  
  @StructureGetter("Unused5")
  short getUnused5();
  
  @StructureGetter("AliasSize")
  short getAliasSize();
  
  @StructureGetter("EntryType")
  short getEntryType();
  
  @StructureGetter("EntryClass")
  short getEntryClass();
  
  @StructureGetter("EntryDesignType")
  short getEntryDesignType();
  
  @StructureGetter("ReplEntryType")
  short getReplEntryType();
  
  @StructureGetter("ReplFlags")
  short getReplFlags();
  
  @StructureGetter("ReplTruncType")
  short getReplTruncType();
  
  @StructureGetter("PreferredServerSize")
  short getPreferredServerSize();
  
  @StructureGetter("GridRow")
  short getGridRow();
  
  @StructureGetter("GridColumn")
  short getGridColumn();
  
  @StructureGetter("ToolbarManagerSize")
  short getToolbarManagerSize();
  
  @StructureGetter("ToolbarEntrySize")
  short getToolbarEntrySize();
  
  @StructureGetter("wReplTruncDocs")
  short getwReplTruncDocs();
  
  @StructureGetter("wReplTruncAtts")
  short getwReplTruncAtts();
  
  @StructureGetter("Spare")
  short[] getSpare();
}
