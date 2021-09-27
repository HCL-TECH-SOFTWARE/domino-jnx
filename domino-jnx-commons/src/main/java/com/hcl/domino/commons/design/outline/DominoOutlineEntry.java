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
import java.util.Set;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.design.OutlineEntry;
import com.hcl.domino.design.format.SiteMapEntry.Flag;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.records.RichTextRecord;

public class DominoOutlineEntry implements IAdaptable, OutlineEntry {
  
  private Type resourceType;
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
  public short getResourceDesignType() {
    return resourceDesignType;
  }

  public void setResourceDesignType(short entryDesignType) {
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
  public short getLevel() {
    return level;
  }

  public void setLevel(short level) {
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

  public short getReplTruncType() {
    return replTruncType;
  }

  public void setReplTruncType(short replTruncType) {
    this.replTruncType = replTruncType;
  }

  @Override
  public short getGridRow() {
    return gridRow;
  }

  public void setGridRow(short gridRow) {
    this.gridRow = gridRow;
  }

  @Override
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

  @Override
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public List<RichTextRecord<?>> getImageData() {
    return imageData;
  }

  public void setImageData(List<RichTextRecord<?>> imageData) {
    this.imageData = imageData;
  }

  @Override
  public String getTargetFrame() {
    return targetFrame;
  }

  public void setTargetFrame(String targetFrame) {
    this.targetFrame = targetFrame;
  }

  @Override
  public List<RichTextRecord<?>> getOnclickData() {
    return onclickData;
  }

  public void setOnclickData(List<RichTextRecord<?>> onclickData) {
    this.onclickData = onclickData;
  }

  @Override
  public String getHideWhenFormula() {
    return hideWhenData;
  }

  public void setHideWhenFormula(String hideWhenData) {
    this.hideWhenData = hideWhenData;
  }

  @Override
  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  @Override
  public String getSourceData() {
    return sourceData;
  }

  public void setSourceData(String sourceData) {
    this.sourceData = sourceData;
  }

  @Override
  public String getPreferredServer() {
    return preferredServer;
  }

  public void setPreferredServer(String preferredServer) {
    this.preferredServer = preferredServer;
  }

  @Override
  public String getToolbarManager() {
    return toolbarManager;
  }

  public void setToolbarManager(String toolbarManager) {
    this.toolbarManager = toolbarManager;
  }

  @Override
  public String getToolbarEntry() {
    return toolbarEntry;
  }

  public void setToolbarEntry(String toolbarEntry) {
    this.toolbarEntry = toolbarEntry;
  }

  @Override
  public String getPopup() {
    return popup;
  }

  public void setPopup(String popup) {
    this.popup = popup;
  }

}
