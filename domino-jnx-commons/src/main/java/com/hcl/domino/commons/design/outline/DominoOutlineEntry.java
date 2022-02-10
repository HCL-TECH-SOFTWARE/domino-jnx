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
package com.hcl.domino.commons.design.outline;

import java.util.Optional;
import java.util.Set;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.design.OutlineEntry;
import com.hcl.domino.design.format.SiteMapEntry.Flag;
import com.hcl.domino.richtext.records.CDResource;

public class DominoOutlineEntry implements IAdaptable, OutlineEntry {
  
  private Type resourceType;
  private CDResource.ResourceClass resourceClass;
  private int resourceDesignType;
  private Set<Flag> flags;
  private int level;
  private int id;
  private DominoOutlineEntry.Type oldEntryType;
  private DominoOutlineEntry.ReplType replyResourceType;
  private int replFlags;
  private int replTruncType;
  private int gridRow;
  private int gridColumn;
  private int wReplTruncDocs;
  private int wReplTruncAtts;
  
  private DominoOutlineEntryData title;
  private DominoOutlineEntryData imageData;
  private DominoOutlineEntryData targetFrame;
  private DominoOutlineEntryData onclickData;
  private DominoOutlineEntryData hideWhenData;
  private DominoOutlineEntryData alias;
  private DominoOutlineEntryData sourceData;
  private DominoOutlineEntryData preferredServer;
  private DominoOutlineEntryData toolbarManager;
  private DominoOutlineEntryData toolbarEntry;
  private DominoOutlineEntryData popup;

  @Override
  public <T> T getAdapter(Class<T> clazz) {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public Type getResourceType() {
    return resourceType;
  }

  public void setResourceType(Type entryType) {
    this.resourceType = entryType;
  }
  
  @Override
  public CDResource.ResourceClass getResourceClass() {
    return resourceClass;
  }

  public void setResourceClass(CDResource.ResourceClass entryClass) {
    this.resourceClass = entryClass;
  }

  @Override
  public int getResourceDesignType() {
    return resourceDesignType;
  }

  public void setResourceDesignType(int entryDesignType) {
    this.resourceDesignType = entryDesignType;
  }
  
  @Override
  public Set<Flag> getFlags() {
    return flags;
  }

  public void setFlags(Set<Flag> entryFlags) {
    this.flags = entryFlags;
  }

  @Override
  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  @Override
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

  public int getReplTruncType() {
    return replTruncType;
  }

  public void setReplTruncType(int replTruncType) {
    this.replTruncType = replTruncType;
  }

  @Override
  public int getGridRow() {
    return gridRow;
  }

  public void setGridRow(int gridRow) {
    this.gridRow = gridRow;
  }

  @Override
  public int getGridColumn() {
    return gridColumn;
  }

  public void setGridColumn(int gridColumn) {
    this.gridColumn = gridColumn;
  }

  public int getwReplTruncDocs() {
    return wReplTruncDocs;
  }

  public void setwReplTruncDocs(int wReplTruncDocs) {
    this.wReplTruncDocs = wReplTruncDocs;
  }

  public int getwReplTruncAtts() {
    return wReplTruncAtts;
  }

  public void setwReplTruncAtts(int wReplTruncAtts) {
    this.wReplTruncAtts = wReplTruncAtts;
  }

  @Override
  public Optional<Object> getTitle() {
    if (title == null) {
      return Optional.empty();
    }
    return title.getDataValue();
  }

  public void setTitle(DominoOutlineEntryData title) {
    this.title = title;
  }

  @Override
  public Optional<Object> getImageData() {
    if (imageData == null) {
      return Optional.empty();
    }
    return imageData.getDataValue();
  }

  public void setImageData(DominoOutlineEntryData imageData) {
    this.imageData = imageData;
  }

  @Override
  public Optional<Object> getTargetFrame() {
    if (targetFrame == null) {
      return Optional.empty();
    }
    return targetFrame.getDataValue();
  }

  public void setTargetFrame(DominoOutlineEntryData targetFrame) {
    this.targetFrame = targetFrame;
  }

  @Override
  public Optional<Object> getOnclickData() {
    if (onclickData == null) {
      return Optional.empty();
    }
    return onclickData.getDataValue();
  }

  public void setOnclickData(DominoOutlineEntryData onclickData) {
    this.onclickData = onclickData;
  }

  @Override
  public Optional<Object> getHideWhenFormula() {
    if (hideWhenData == null) {
      return Optional.empty();
    }
    return this.hideWhenData.getDataValue();
  }

  public void setHideWhenFormula(DominoOutlineEntryData hideWhenData) {
    this.hideWhenData = hideWhenData;
  }

  @Override
  public Optional<Object> getAlias() {
    if (alias == null) {
      return Optional.empty();
    }
    return alias.getDataValue();
  }

  public void setAlias(DominoOutlineEntryData alias) {
    this.alias = alias;
  }

  @Override
  public Optional<Object> getSourceData() {
    if (sourceData == null) {
      return Optional.empty();
    }
    return sourceData.getDataValue();
  }

  public void setSourceData(DominoOutlineEntryData sourceData) {
    this.sourceData = sourceData;
  }

  @Override
  public Optional<Object> getPreferredServer() {
    if (preferredServer == null) {
      return Optional.empty();
    }
    return preferredServer.getDataValue();
  }

  public void setPreferredServer(DominoOutlineEntryData preferredServer) {
    this.preferredServer = preferredServer;
  }

  @Override
  public Optional<Object> getToolbarManager() {
    if (toolbarManager == null) {
      return Optional.empty();
    }
    return toolbarManager.getDataValue();
  }

  public void setToolbarManager(DominoOutlineEntryData toolbarManager) {
    this.toolbarManager = toolbarManager;
  }

  @Override
  public Optional<Object> getToolbarEntry() {
    if (toolbarEntry == null) {
      return Optional.empty();
    }
    return toolbarEntry.getDataValue();
  }

  public void setToolbarEntry(DominoOutlineEntryData toolbarEntry) {
    this.toolbarEntry = toolbarEntry;
  }

  @Override
  public Optional<Object> getPopup() {
    if (popup == null) {
      return Optional.empty();
    }
    return popup.getDataValue();
  }

  public void setPopup(DominoOutlineEntryData popup) {
    this.popup = popup;
  }

}
