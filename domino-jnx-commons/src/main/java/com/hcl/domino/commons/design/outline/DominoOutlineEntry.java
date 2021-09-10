/*
 * ==========================================================================
 * Copyright (C) 2019-2021 HCL America, Inc. ( http://www.hcl.com/ )
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
package com.hcl.domino.commons.design.outline;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.design.format.SiteMapEntry.Flag;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.OutlineConstants;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.records.RichTextRecord;

public class DominoOutlineEntry implements IAdaptable {
  
  private DominoOutlineEntry.Type resourceType;
  private CDResource.ResourceClass resourceClass;
  private short resourceDesignType;
  private Set<Flag> flags;
  private short level;
  private int id;
  private DominoOutlineEntry.Type oldEntryType;
  private DominoOutlineEntry.ReplType replyResourceType;
  private int replFlags;
  private short replTruncType;
  private short gridRow;
  private short gridColumn;
  private short wReplTruncDocs;
  private short wReplTruncAtts;
  
  private String title;
  private List<RichTextRecord<?>> imageData;
  private String targetFrame;
  private List<RichTextRecord<?>> onclickData;
  private String hideWhenData;
  private String alias;
  private String sourceData;
  private String preferredServer;
  private String toolbarManager;
  private String toolbarEntry;
  private String popup;

  @Override
  public <T> T getAdapter(Class<T> clazz) {
    // TODO Auto-generated method stub
    return null;
  }
  
  enum Type implements INumberEnum<Short> {
    EMPTY(RichTextConstants.CDRESOURCE_TYPE_EMPTY),
    URL(RichTextConstants.CDRESOURCE_TYPE_URL),
    NOTELINK(RichTextConstants.CDRESOURCE_TYPE_NOTELINK),
    NAMEDELEMENT(RichTextConstants.CDRESOURCE_TYPE_NAMEDELEMENT),
    /** Currently not written to disk only used in RESOURCELINK */
    NOTEIDLINK(RichTextConstants.CDRESOURCE_TYPE_NOTEIDLINK),
    /**
     * This would be used in conjunction with the formula flag. The formula is
     * an @Command that would
     * perform some action, typically it would also switch to a Notes UI element.
     * This will be used to
     * reference the replicator page and other UI elements.
     */
    ACTION(RichTextConstants.CDRESOURCE_TYPE_ACTION),
    /** Currently not written to disk only used in RESOURCELINK */
    NAMEDITEMELEMENT(RichTextConstants.CDRESOURCE_TYPE_NAMEDITEMELEMENT),
    /* private outline entries  */
    SPECIAL_ENTRY_START(OutlineConstants.SITEMAP_SPECIAL_ENTRY_START),
    DEFAULT_HOME_PAGE_ENTRY(OutlineConstants.SITEMAP_DEFAULT_HOME_PAGE_ENTRY),    /*DefaultHomePage*/
    BROWSEDB_ENTRY(OutlineConstants.SITEMAP_BROWSEDB_ENTRY),    /*Browse Databases*/
    GLOBAL_WORKBENCH_ENTRY(OutlineConstants.SITEMAP_GLOBAL_WORKBENCH_ENTRY),   /*Domino Global Workbench*/
    MOBILE_DESIGNER_ENTRY(OutlineConstants.SITEMAP_MOBILE_DESIGNER_ENTRY),    /*Mobile Notes Designer*/
    WEB_STUDIO_ENTRY(OutlineConstants.SITEMAP_WEB_STUDIO_ENTRY),    /*WebSphere Studio*/
    NOTES_ENTRY(OutlineConstants.SITEMAP_NOTES_ENTRY),    /*Notes*/
    SAMETIME_ENTRY(OutlineConstants.SITEMAP_SAMETIME_ENTRY),    /*Sametime*/
    ADMIN_ENTRY(OutlineConstants.SITEMAP_ADMIN_ENTRY),    /*Admin*/
    DESIGNER_ENTRY(OutlineConstants.SITEMAP_DESIGNER_ENTRY),    /*Designer*/
    DESK_ENTRY(OutlineConstants.SITEMAP_DESK_ENTRY),    /*Workspace*/
    BCASE_ENTRY(OutlineConstants.SITEMAP_BCASE_ENTRY),    /*Replication*/
    HOME_PAGE_ENTRY(OutlineConstants.SITEMAP_HOME_PAGE_ENTRY),    /*Home Page*/

    IMPLIED_FOLDER_ENTRY(OutlineConstants.SITEMAP_IMPLIED_FOLDER_ENTRY),    /* Implied folder  */
    ARCHIVE_ENTRY(OutlineConstants.SITEMAP_ARCHIVE_ENTRY),    /* archive entry expanded from
                                                            archive placeholder */

    ARCHIVE_PLACEHOLDER_END_ENTRY(OutlineConstants.SITEMAP_ARCHIVE_PLACEHOLDER_END_ENTRY),    /* An ending indicator
                                                            for subsequent refreshes. 
                                                            Temporary, not saved
                                                            to disk. */
    ARCHIVE_PLACEHOLDER_ENTRY(OutlineConstants.SITEMAP_ARCHIVE_PLACEHOLDER_ENTRY),    /* A placeholder to be replaced by all
                                                                    archive profile folders */

    /* Taking advantage of the sitemap creation checks for special mail folders */
    TOOLBAR_ENTRY(OutlineConstants.SITEMAP_TOOLBAR_ENTRY),    /* Toolbar entry */
    INBOX_ENTRY(OutlineConstants.SITEMAP_INBOX_ENTRY),    /* Mail Inbox folder  */
    RULES_ENTRY(OutlineConstants.SITEMAP_RULES_ENTRY),    /* Mail Rules folder  */
    DRAFTS_ENTRY(OutlineConstants.SITEMAP_DRAFTS_ENTRY),    /* Mail Drafts folder */
    TRASH_ENTRY(OutlineConstants.SITEMAP_TRASH_ENTRY),    /* Mail Trash folder  */
    SENT_ENTRY(OutlineConstants.SITEMAP_SENT_ENTRY),    /* Mail Sent folder   */


    HISTORY_ENTRY(OutlineConstants.SITEMAP_HISTORY_ENTRY), /* History folder */

    OTHER_VIEWS_END_ENTRY(OutlineConstants.SITEMAP_OTHER_VIEWS_END_ENTRY), /* An ending indicator
                                                            for subsequent refreshes. 
                                                            Temporary, not saved
                                                            to disk. */
    OTHER_FOLDERS_END_ENTRY(OutlineConstants.SITEMAP_OTHER_FOLDERS_END_ENTRY), /* An ending indicator
                                                            for subsequent refreshes. 
                                                            Temporary, not saved
                                                            to disk. */

    NS_WEBBROWSER_ENTRY(OutlineConstants.SITEMAP_NS_WEBBROWSER_ENTRY), /* Loads in user's favorites from NS.
                                                            Added by the UI and
                                                            populated by sitemap
                                                            during an expand. */

    IE_WEBBROWSER_ENTRY(OutlineConstants.SITEMAP_IE_WEBBROWSER_ENTRY), /* Loads in user's favorites from IE.
                                                            Added by the UI and
                                                            populated by sitemap
                                                            during an expand. */

    CREATE_ENTRY(OutlineConstants.SITEMAP_CREATE_ENTRY), /* A drop point which causes
                                                            a new entry to get created. */

    OTHER_VIEWS_ENTRY(OutlineConstants.SITEMAP_OTHER_VIEWS_ENTRY), /* A placeholder to be replaced by all
                                                            shared views not specifically 
                                                            mentioned */
    OTHER_FOLDERS_ENTRY(OutlineConstants.SITEMAP_OTHER_FOLDERS_ENTRY), /* A placeholder to be replaced by all
                                                            shared folders not specifically 
                                                            mentioned */
    OPEN_ARCHIVE_LOGS_NBP(OutlineConstants.SITEMAP_OPEN_ARCHIVE_LOGS_NBP); /* Opening the archive logs from Notes Web using Notes Browser Plugin
                                                              for local archiving feature (Notes Web Companion)*/

    private final short value;

    Type(final short value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Short getValue() {
      return this.value;
    }
  }
  
  public DominoOutlineEntry.Type getResourceType() {
    return resourceType;
  }

  public void setResourceType(DominoOutlineEntry.Type entryType) {
    this.resourceType = entryType;
  }
  
  public void setResourceTypeFromRaw(short entryType) {
    final Optional<DominoOutlineEntry.Type> optEntryType = DominoEnumUtil.valueOf((Class) DominoOutlineEntry.Type.class, entryType);
    if (optEntryType.isPresent())
      this.setResourceType(optEntryType.get());
  }
  
  enum ReplType implements INumberEnum<Short> {
    EMPTY(RichTextConstants.CDRESOURCE_TYPE_EMPTY),
    /* TODO get  the values from Domino and replace them */
    BCASE_DATABASE_ENTRY((short)-1),
    BCASE_IMAP_DB_ENTRY((short)-2),
    BCASE_NEWS_DB_ENTRY((short)-3),
    BCASE_SCHEDRQST_ENTRY((short)-4),
    BCASE_SCHEDULE_ENTRY((short)-5),
    BCASE_LAST_ENTRY((short)-6),
    BCASE_MAILBOX_ENTRY((short)-7);

    private final short value;

    ReplType(final short value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Short getValue() {
      return this.value;
    }
  }

  public CDResource.ResourceClass getResourceClass() {
    return resourceClass;
  }

  public void setResourceClass(CDResource.ResourceClass entryClass) {
    this.resourceClass = entryClass;
  }
  
  public void setResourceClassFromRaw(short entryClass) {
    final Optional<CDResource.ResourceClass> optEntryClass = DominoEnumUtil.valueOf((Class) CDResource.ResourceClass.class, entryClass);
    if (optEntryClass.isPresent())
      this.setResourceClass(optEntryClass.get());
  }

  public short getResourceDesignType() {
    return resourceDesignType;
  }

  public void setResourceDesignType(short entryDesignType) {
    this.resourceDesignType = entryDesignType;
  }
  
  

  public Set<Flag> getFlags() {
    return flags;
  }

  public void setFlags(Set<Flag> entryFlags) {
    this.flags = entryFlags;
  }

  public short getLevel() {
    return level;
  }

  public void setLevel(short level) {
    this.level = level;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public DominoOutlineEntry.Type getOldEntryType() {
    return oldEntryType;
  }

  public void setOldEntryType(DominoOutlineEntry.Type oldEntryType) {
    this.oldEntryType = oldEntryType;
  }

  public DominoOutlineEntry.ReplType getReplyResourceType() {
    return replyResourceType;
  }

  public void setReplyResourceType(DominoOutlineEntry.ReplType replyResourceType) {
    this.replyResourceType = replyResourceType;
  }

  public int getReplFlags() {
    return replFlags;
  }

  public void setReplFlags(int replFlags) {
    this.replFlags = replFlags;
  }

  public short getReplTruncType() {
    return replTruncType;
  }

  public void setReplTruncType(short replTruncType) {
    this.replTruncType = replTruncType;
  }

  public short getGridRow() {
    return gridRow;
  }

  public void setGridRow(short gridRow) {
    this.gridRow = gridRow;
  }

  public short getGridColumn() {
    return gridColumn;
  }

  public void setGridColumn(short gridColumn) {
    this.gridColumn = gridColumn;
  }

  public short getwReplTruncDocs() {
    return wReplTruncDocs;
  }

  public void setwReplTruncDocs(short wReplTruncDocs) {
    this.wReplTruncDocs = wReplTruncDocs;
  }

  public short getwReplTruncAtts() {
    return wReplTruncAtts;
  }

  public void setwReplTruncAtts(short wReplTruncAtts) {
    this.wReplTruncAtts = wReplTruncAtts;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public List<RichTextRecord<?>> getImageData() {
    return imageData;
  }

  public void setImageData(List<RichTextRecord<?>> imageData) {
    this.imageData = imageData;
  }

  public String getTargetFrame() {
    return targetFrame;
  }

  public void setTargetFrame(String targetFrame) {
    this.targetFrame = targetFrame;
  }

  public List<RichTextRecord<?>> getOnclickData() {
    return onclickData;
  }

  public void setOnclickData(List<RichTextRecord<?>> onclickData) {
    this.onclickData = onclickData;
  }

  public String getHideWhenData() {
    return hideWhenData;
  }

  public void setHideWhenData(String hideWhenData) {
    this.hideWhenData = hideWhenData;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public String getSourceData() {
    return sourceData;
  }

  public void setSourceData(String sourceData) {
    this.sourceData = sourceData;
  }

  public String getPreferredServer() {
    return preferredServer;
  }

  public void setPreferredServer(String preferredServer) {
    this.preferredServer = preferredServer;
  }

  public String getToolbarManager() {
    return toolbarManager;
  }

  public void setToolbarManager(String toolbarManager) {
    this.toolbarManager = toolbarManager;
  }

  public String getToolbarEntry() {
    return toolbarEntry;
  }

  public void setToolbarEntry(String toolbarEntry) {
    this.toolbarEntry = toolbarEntry;
  }

  public String getPopup() {
    return popup;
  }

  public void setPopup(String popup) {
    this.popup = popup;
  }

}
