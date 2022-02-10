/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
 *                            All rights reserved.
 * ==========================================================================
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may
 * not use this file except in compliance with the License.  You may obtain a
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.
 *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the  specific language  governing permissions  and limitations
 * under the License.
 * ==========================================================================
 */
package com.hcl.domino.design.format;

import java.util.Collection;
import java.util.Set;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.OutlineConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.MemoryStructure;

@StructureDefinition(name = "SITEMAP_ENTRY", members = {
    @StructureMember(name = "EntryFixedSize", type = short.class, unsigned = true),
    @StructureMember(name = "EntryVarSize", type = short.class, unsigned = true),
    @StructureMember(name = "EntryFlags", type = SiteMapEntry.Flag.class, bitfield = true),
    @StructureMember(name = "Id", type = int.class, unsigned = false),
    @StructureMember(name = "OldEntryType", type = short.class, unsigned = true),
    @StructureMember(name = "Level", type = short.class, unsigned = true),
    @StructureMember(name = "TitleSize", type = short.class, unsigned = true),
    @StructureMember(name = "PopupSize", type = short.class, unsigned = true),
    @StructureMember(name = "OnClickSize", type = short.class, unsigned = true),
    @StructureMember(name = "SourceSize", type = short.class, unsigned = true),
    @StructureMember(name = "ImagesSize", type = short.class, unsigned = true),
    @StructureMember(name = "Unused3", type = short.class),
    @StructureMember(name = "TargetFrameSize", type = short.class, unsigned = true),
    @StructureMember(name = "Unused4", type = short.class),
    @StructureMember(name = "HideWhenSize", type = short.class, unsigned = true),
    @StructureMember(name = "Unused5", type = short.class),
    @StructureMember(name = "AliasSize", type = short.class, unsigned = true),
    @StructureMember(name = "EntryType", type = short.class, unsigned = true),
    @StructureMember(name = "EntryClass", type = short.class, unsigned = true),
    @StructureMember(name = "EntryDesignType", type = short.class, unsigned = true),
    @StructureMember(name = "ReplEntryType", type = short.class, unsigned = true),
    @StructureMember(name = "ReplFlags", type = short.class, unsigned = true),
    @StructureMember(name = "ReplTruncType", type = short.class, unsigned = true),
    @StructureMember(name = "PreferredServerSize", type = short.class, unsigned = true),
    @StructureMember(name = "GridRow", type = short.class, unsigned = true),
    @StructureMember(name = "GridColumn", type = short.class, unsigned = true),
    @StructureMember(name = "ToolbarManagerSize", type = short.class, unsigned = true),
    @StructureMember(name = "ToolbarEntrySize", type = short.class, unsigned = true),
    @StructureMember(name = "wReplTruncDocs", type = short.class, unsigned = true),
    @StructureMember(name = "wReplTruncAtts", type = short.class, unsigned = true),
    @StructureMember(name = "Spare", type = short[].class, length = 4)
})
public interface SiteMapEntry extends MemoryStructure {
  
  @StructureGetter("EntryFixedSize")
  int getEntryFixedSize();
  
  @StructureSetter("EntryFixedSize")
  SiteMapEntry setEntryFixedSize(int entryFixedSize);

  @StructureGetter("EntryVarSize")
  int getEntryVarSize();
  
  @StructureSetter("EntryVarSize")
  SiteMapEntry setEntryVarSize(int entryVarSize);
  
  enum Flag implements INumberEnum<Integer> {
    /** Column contains a name. */
    IS_NAME(NotesConstants.VCF5_M_IS_NAME),
    TOTHISDB_ENTRYFLAG(OutlineConstants.SITEMAP_TOTHISDB_ENTRYFLAG),   /* Link resolves to this database */
    HIDDEN_ENTRYFLAG(OutlineConstants.SITEMAP_HIDDEN_ENTRYFLAG),   /* Used to specifically hide a view or folder which
                                                                    otherwise would have been displayed in a
                                                                    default placeholder list. */
    PRIVATE_ENTRYFLAG(OutlineConstants.SITEMAP_PRIVATE_ENTRYFLAG), /* Applicable to other views and other folders only. */
    SORT_ENTRYFLAG(OutlineConstants.SITEMAP_SORT_ENTRYFLAG),   /* Sort this entry with respect to its peers
                                                that are marked for sorting. Sorting will only occur within
                                                contiguous sorted entries. This is powerful where
                                                the names may change for internationalzation
                                                or where the display may be computed. */
    HIDDEN_WEB_ENTRYFLAG(OutlineConstants.SITEMAP_HIDDEN_WEB_ENTRYFLAG),  /*  this entry is hidden from the web */
    USEHIDEWHENFORMULA_ENTRYFLAG(OutlineConstants.SITEMAP_USEHIDEWHENFORMULA_ENTRYFLAG), /*  use the hidewhen formula if we have one */
    EXPANDED_ENTRYFLAG(OutlineConstants.SITEMAP_EXPANDED_ENTRYFLAG),  /* Persist the expansion state.  This can be
                                                used by the designer to pre-expand a branch,
                                                as well as bookmarks to remember state of
                                                each page as the user left it. */
    DEFAULT_ENTRYFLAG(OutlineConstants.SITEMAP_DEFAULT_ENTRYFLAG),  /* An individual item in the sitemap may be
                                                marked as a default. Setting this on an
                                                entry will clear it on an previous entry. 
                                                In bookmarks, this is used to mark the home
                                                link that is opened in a special way on startup. */
    EXPANDABLE_ENTRYFLAG(OutlineConstants.SITEMAP_EXPANDABLE_ENTRYFLAG),  /* Used to indicate that an element may be 
                                                expanded, even if there are no children.  Used
                                                by bookmarks to expand a folder on a click. */
    REFUSESEL_ENTRYFLAG(OutlineConstants.SITEMAP_REFUSESEL_ENTRYFLAG),  /* used to signal an entry that should never accept 
                                                selection */ 
    NEVERIMAGE_ENTRYFLAG(OutlineConstants.SITEMAP_NEVERIMAGE_ENTRYFLAG), /* used to indicate that this entry never, never
                                                wants an image */    
    MODIFIEDTITLE_ENTRYFLAG(OutlineConstants.SITEMAP_MODIFIEDTITLE_ENTRYFLAG),  /* Set if the user explicitly modified the title of an
                                                      entry (mostly for bookmarks). */
    RTLREADING_ENTRYFLAG(OutlineConstants.SITEMAP_RTLREADING_ENTRYFLAG),     /* Set if the user sets the text reading order of an entry is to be 
                                                      Right-to-Left reading order. */
    OSELEMENT_ENTRYFLAG(OutlineConstants.SITEMAP_OSELEMENT_ENTRYFLAG),  /* Entry is a link to an operating system item */

    NAP_STILL_EXISTS_ENTRYFLAG(OutlineConstants.SITEMAP_NAP_STILL_EXISTS_ENTRYFLAG),  /* Notes App Plugin - still exist flag */
    WORKSPACE_ENTRYFLAG(OutlineConstants.SITEMAP_WORKSPACE_ENTRYFLAG),  /* Temporary Workspace entry */
    NAP_CHECK_FOLDER_ENTRYFLAG(OutlineConstants.SITEMAP_NAP_CHECK_FOLDER_ENTRYFLAG),  /* Notes App Plugin - folder changed and need checked */

    POSITION_SPECIFIED_ENTRYFLAG(OutlineConstants.SITEMAP_POSITION_SPECIFIED_ENTRYFLAG),  /* Entries position in a grid is specified */
    READONLY_ENTRYFLAG(OutlineConstants.SITEMAP_READONLY_ENTRYFLAG),  /* if user shouldn't be allowed to change title */
    IMAGE_DONT_STRECH_ENTRYFLAG(OutlineConstants.SITEMAP_IMAGE_DONT_STRECH_ENTRYFLAG),  /* don't strech the entry image */
    QUERYVIEW_ENTRYFLAG(OutlineConstants.SITEMAP_QUERYVIEW_ENTRYFLAG),  /* applicable to DB2 based query views */
    ADD_SEPARATOR_ENTRYFLAG(OutlineConstants.SITEMAP_ADD_SEPARATOR_ENTRYFLAG),  /* add separator above the entry */
    IS_INHERITED_ENTRYFLAG(OutlineConstants.SITEMAP_IS_INHERITED_ENTRYFLAG),  /* add separator above the entry */
    DONOTSAVE_ENTRYFLAG(OutlineConstants.SITEMAP_DONOTSAVE_ENTRYFLAG);  /* Used to add something to the internal list
                                                          which should not be saved back when the list is saved. */

    private final int value;

    Flag(final int value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Integer getValue() {
      return this.value;
    }
  }
  
  @StructureGetter("EntryFlags")
  Set<Flag> getEntryFlags();
  
  @StructureSetter("EntryFlags")
  SiteMapEntry setFlags(Collection<Flag> flags);
  
  @StructureGetter("Id")
  int getId();
  
  @StructureSetter("Id")
  SiteMapEntry setId(int id);
  
  @StructureGetter("OldEntryType")
  int getOldEntryType();
  
  @StructureSetter("OldEntryType")
  SiteMapEntry setOldEntryType(int oldEntryType);
  
  @StructureGetter("Level")
  int getLevel();
  
  @StructureSetter("Level")
  SiteMapEntry setLevel(int level);
  
  @StructureGetter("TitleSize")
  int getTitleSize();
  
  @StructureSetter("TitleSize")
  SiteMapEntry setTitleSize(int titleSize);
  
  @StructureGetter("PopupSize")
  int getPopupSize();
  
  @StructureSetter("PopupSize")
  SiteMapEntry setPopupSize(int popupSize);
  
  @StructureGetter("OnClickSize")
  int getOnClickSize();
  
  @StructureSetter("OnClickSize")
  SiteMapEntry setOnClickSize(int onClickSize);
  
  @StructureGetter("SourceSize")
  int getSourceSize();
  
  @StructureSetter("SourceSize")
  SiteMapEntry setSourceSize(int sourceSize);
  
  @StructureGetter("ImagesSize")
  int getImagesSize();
  
  @StructureSetter("ImagesSize")
  SiteMapEntry setImagesSize(int imagesSize);
  
  @StructureGetter("Unused3")
  short getUnused3();
  
  @StructureSetter("Unused3")
  SiteMapEntry setUnused3(short unused3);
  
  @StructureGetter("TargetFrameSize")
  int getTargetFrameSize();
  
  @StructureSetter("TargetFrameSize")
  SiteMapEntry setTargetFrameSize(int targetFrameSize);
  
  @StructureGetter("Unused4")
  short getUnused4();
  
  @StructureSetter("Unused4")
  SiteMapEntry setUnused4(short unused4);
  
  @StructureGetter("HideWhenSize")
  int getHideWhenSize();
  
  @StructureSetter("HideWhenSize")
  SiteMapEntry setHideWhenSize(int hideWhenSize);
  
  @StructureGetter("Unused5")
  short getUnused5();
  
  @StructureSetter("Unused5")
  SiteMapEntry setUnused5(short unused5);
  
  @StructureGetter("AliasSize")
  int getAliasSize();
  
  @StructureSetter("AliasSize")
  SiteMapEntry setAliasSize(int aliasSize);
  
  @StructureGetter("EntryType")
  int getEntryType();
  
  @StructureSetter("EntryType")
  SiteMapEntry setEntryType(int entryType);
  
  @StructureGetter("EntryClass")
  int getEntryClass();
  
  @StructureSetter("EntryClass")
  SiteMapEntry setEntryClass(int entryClass);
  
  @StructureGetter("EntryDesignType")
  int getEntryDesignType();
  
  @StructureSetter("EntryDesignType")
  SiteMapEntry setEntryDesignType(int entryDesignType);
  
  @StructureGetter("ReplEntryType")
  int getReplEntryType();
  
  @StructureSetter("ReplEntryType")
  SiteMapEntry setReplEntryType(int replEntryType);
  
  @StructureGetter("ReplFlags")
  int getReplFlags();
  
  @StructureSetter("ReplFlags")
  SiteMapEntry setReplFlags(int replFlags);
  
  @StructureGetter("ReplTruncType")
  int getReplTruncType();
  
  @StructureSetter("ReplTruncType")
  SiteMapEntry setReplTruncType(int replTruncType);
  
  @StructureGetter("PreferredServerSize")
  int getPreferredServerSize();
  
  @StructureSetter("PreferredServerSize")
  SiteMapEntry setPreferredServerSize(int preferredServerSize);
  
  @StructureGetter("GridRow")
  int getGridRow();
  
  @StructureSetter("GridRow")
  SiteMapEntry setGridRow(int gridRow);
  
  @StructureGetter("GridColumn")
  int getGridColumn();
  
  @StructureSetter("GridColumn")
  SiteMapEntry setGridColumn(int gridColumn);
  
  @StructureGetter("ToolbarManagerSize")
  int getToolbarManagerSize();
  
  @StructureSetter("ToolbarManagerSize")
  SiteMapEntry setToolbarManagerSize(int toolbarManagerSize);
  
  @StructureGetter("ToolbarEntrySize")
  int getToolbarEntrySize();
  
  @StructureSetter("ToolbarEntrySize")
  SiteMapEntry setToolbarEntrySize(int toolbarEntrySize);
  
  @StructureGetter("wReplTruncDocs")
  int getwReplTruncDocs();
  
  @StructureSetter("wReplTruncDocs")
  SiteMapEntry setWReplTruncDocs(int wReplTruncDocs);
  
  @StructureGetter("wReplTruncAtts")
  int getwReplTruncAtts();
  
  @StructureSetter("wReplTruncAtts")
  SiteMapEntry setWReplTruncAtts(int wReplTruncAtts);
  
  @StructureGetter("Spare")
  short[] getSpare();
  
  @StructureSetter("Spare")
  SiteMapEntry setSpare(short[] Spare);
}
