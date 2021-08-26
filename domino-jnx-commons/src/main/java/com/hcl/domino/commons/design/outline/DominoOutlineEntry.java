package com.hcl.domino.commons.design.outline;

import java.util.List;
import java.util.Optional;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.records.RichTextRecord;

public class DominoOutlineEntry implements IAdaptable {
  
  private DominoOutlineEntry.Type resourceType;
  private CDResource.ResourceClass resourceClass;
  private short resourceDesignType;
  private int flags;
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
    OTHERVIEWS(OutlineConstants.SITEMAP_OTHER_VIEWS_ENTRY),
    OTHERFOLDERS(OutlineConstants.SITEMAP_OTHER_FOLDERS_ENTRY);

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

  public int getFlags() {
    return flags;
  }

  public void setFlags(int entryFlags) {
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
