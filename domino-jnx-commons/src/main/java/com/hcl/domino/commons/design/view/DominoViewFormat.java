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
package com.hcl.domino.commons.design.view;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.data.CollectionColumn;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.design.format.ViewColumnFormat;
import com.hcl.domino.design.format.ViewColumnFormat2;
import com.hcl.domino.design.format.ViewColumnFormat3;
import com.hcl.domino.design.format.ViewColumnFormat4;
import com.hcl.domino.design.format.ViewColumnFormat5;
import com.hcl.domino.design.format.ViewColumnFormat6;
import com.hcl.domino.design.format.ViewTableFormat;
import com.hcl.domino.design.format.ViewTableFormat2;
import com.hcl.domino.design.format.ViewTableFormat3;
import com.hcl.domino.design.format.ViewTableFormat4;
import com.hcl.domino.richtext.records.CDResource;

/**
 * @author Jesse Gallagher
 * @since 1.0.27
 */
public class DominoViewFormat implements IAdaptable {
  private final List<CollectionColumn> columns = new ArrayList<>();

  private ViewTableFormat format1;
  private ViewTableFormat2 format2;
  private ViewTableFormat3 format3;
  private ViewTableFormat4 format4;
  private CDResource backgroundResource;
  
  public DominoViewFormat() {
    this.format1 = ViewTableFormat.newInstance();
    this.format2 = ViewTableFormat2.newInstance();
    this.format3 = ViewTableFormat3.newInstance();
    this.format4 = ViewTableFormat4.newInstance();
  }
  
  /**
   * Adds a new empty column format object at the specified position
   * 
   * @param index insertion index, use -1 to append at the end
   * @return column format
   */
  public CollectionColumn addColumn(int index) {
    final ViewTableFormat format1 = Objects.requireNonNull(this.format1, "VIEW_TABLE_FORMAT not read");
    final DominoCollectionColumn col = new DominoCollectionColumn();
    
    if (index==-1) {
      this.columns.add(col);
    }
    else {
      this.columns.add(index, col);
    }
    
    format1.setColumnCount(this.columns.size());
    
    return col;
  }

  /**
   * Removes a view column format
   * 
   * @param index column index
   */
  public void removeColumn(CollectionColumn column) {
    final ViewTableFormat format1 = Objects.requireNonNull(this.format1, "VIEW_TABLE_FORMAT not read");
    int index = columns.indexOf(column);
    if (index!=-1) {
      columns.remove(index);
      format1.setColumnCount(this.columns.size());
    }
  }
  
  /**
   * Returns the view column format for the specified index
   * 
   * @param index index
   * @return format
   */
  public CollectionColumn getColumn(int index) {
    return columns.get(index);
  }
  
  public void swapColumns(CollectionColumn a, CollectionColumn b) {
    int idx1 = columns.indexOf(a);
    int idx2 = columns.indexOf(b);
    
    if (idx1!=-1 && idx2!=-1) {
      swapColumns(idx1, idx2);
    }
  }

  public void swapColumns(int a, int b) {
    if (a >= columns.size()) {
      throw new IndexOutOfBoundsException(MessageFormat.format("Index: {0}, Size: {1}", a, columns.size()));
    }
    
    if (b >= columns.size()) {
      throw new IndexOutOfBoundsException(MessageFormat.format("Index: {0}, Size: {1}", b, columns.size()));
    }
    
    CollectionColumn colA = columns.get(a);
    CollectionColumn colB = columns.get(b);
    columns.set(a, colB);
    columns.set(b, colA);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getAdapter(final Class<T> clazz) {
    if (ViewTableFormat.class == clazz) {
      return (T) this.format1;
    } else if (ViewTableFormat2.class == clazz) {
      return (T) this.format2;
    } else if (ViewTableFormat3.class == clazz) {
      return (T) this.format3;
    } else if (ViewTableFormat4.class == clazz) {
      return (T) this.format4;
    } else if (ViewColumnFormat[].class == clazz) {
      ViewColumnFormat[] colFormats = new ViewColumnFormat[columns.size()];
      for (int i=0; i<columns.size(); i++) {
        CollectionColumn currCol = columns.get(i);
        if (currCol instanceof IAdaptable) {
          colFormats[i] = ((IAdaptable)currCol).getAdapter(ViewColumnFormat.class);
        }
      }
      return (T) colFormats;
    } else if (ViewColumnFormat2[].class == clazz) {
      ViewColumnFormat2[] colFormats = new ViewColumnFormat2[columns.size()];
      for (int i=0; i<columns.size(); i++) {
        CollectionColumn currCol = columns.get(i);
        if (currCol instanceof IAdaptable) {
          colFormats[i] = ((IAdaptable)currCol).getAdapter(ViewColumnFormat2.class);
        }
      }
      return (T) colFormats;
    } else if (ViewColumnFormat3[].class == clazz) {
      ViewColumnFormat3[] colFormats = new ViewColumnFormat3[columns.size()];
      for (int i=0; i<columns.size(); i++) {
        CollectionColumn currCol = columns.get(i);
        if (currCol instanceof IAdaptable) {
          colFormats[i] = ((IAdaptable)currCol).getAdapter(ViewColumnFormat3.class);
        }
      }
      return (T) colFormats;
    } else if (ViewColumnFormat4[].class == clazz) {
      ViewColumnFormat4[] colFormats = new ViewColumnFormat4[columns.size()];
      for (int i=0; i<columns.size(); i++) {
        CollectionColumn currCol = columns.get(i);
        if (currCol instanceof IAdaptable) {
          colFormats[i] = ((IAdaptable)currCol).getAdapter(ViewColumnFormat4.class);
        }
      }
      return (T) colFormats;
    } else if (ViewColumnFormat5[].class == clazz) {
      ViewColumnFormat5[] colFormats = new ViewColumnFormat5[columns.size()];
      for (int i=0; i<columns.size(); i++) {
        CollectionColumn currCol = columns.get(i);
        if (currCol instanceof IAdaptable) {
          colFormats[i] = ((IAdaptable)currCol).getAdapter(ViewColumnFormat5.class);
        }
      }
      return (T) colFormats;
    } else if (ViewColumnFormat6[].class == clazz) {
      ViewColumnFormat6[] colFormats = new ViewColumnFormat6[columns.size()];
      for (int i=0; i<columns.size(); i++) {
        CollectionColumn currCol = columns.get(i);
        if (currCol instanceof IAdaptable) {
          colFormats[i] = ((IAdaptable)currCol).getAdapter(ViewColumnFormat6.class);
        }
      }
      return (T) colFormats;
    }
    
    return null;
  }

  public int getColumnCount() {
    final ViewTableFormat format1 = Objects.requireNonNull(this.format1, "VIEW_TABLE_FORMAT not read");
    return format1.getColumnCount();
  }

  public List<CollectionColumn> getColumns() {
    return Collections.unmodifiableList(this.columns);
  }

  public boolean isCollapsed() {
    return this.getFlags1().contains(ViewTableFormat.Flag.COLLAPSED);
  }

  public DominoViewFormat setCollapsed(boolean b) {
    this.getFormat1().setFlag(ViewTableFormat.Flag.COLLAPSED, b);
    return this;
  }
  
  public boolean isConflict() {
    return this.getFlags1().contains(ViewTableFormat.Flag.CONFLICT);
  }

  public DominoViewFormat setConflict(boolean b) {
    this.getFormat1().setFlag(ViewTableFormat.Flag.CONFLICT, b);
    return this;
  }
  
  public boolean isExtendLastColumn() {
    return this.getFlags1().contains(ViewTableFormat.Flag.EXTEND_LAST_COLUMN);
  }

  public DominoViewFormat setExtendLastColumn(boolean b) {
    this.getFormat1().setFlag(ViewTableFormat.Flag.EXTEND_LAST_COLUMN, b);
    return this;
  }
  
  public boolean isGotoBottomOnOpen() {
    return this.getFlags1().contains(ViewTableFormat.Flag.GOTO_BOTTOM_ON_OPEN);
  }

  public DominoViewFormat setGotoBottomOnOpen(boolean b) {
    this.getFormat1().setFlag(ViewTableFormat.Flag.GOTO_BOTTOM_ON_OPEN, b);
    return this;
  }
  
  public boolean isGotoBottomOnRefresh() {
    return this.getFlags1().contains(ViewTableFormat.Flag.GOTO_BOTTOM_ON_REFRESH);
  }

  public DominoViewFormat setGotoBottomOnRefresh(boolean b) {
    this.getFormat1().setFlag(ViewTableFormat.Flag.GOTO_BOTTOM_ON_REFRESH, b);
    return this;
  }
  
  public boolean isGotoTopOnOpen() {
    return this.getFlags1().contains(ViewTableFormat.Flag.GOTO_TOP_ON_OPEN);
  }
  
  public DominoViewFormat setGotoTopOnOpen(boolean b) {
    this.getFormat1().setFlag(ViewTableFormat.Flag.GOTO_TOP_ON_OPEN, b);
    return this;
  }
  
  public CDResource getBackgroundResource() {
    return backgroundResource;
  }

  public DominoViewFormat setBackgroundResource(CDResource backgroundResource) {
    this.backgroundResource = backgroundResource;
    return this;
  }
  
  public boolean isGotoTopOnRefresh() {
    return this.getFlags1().contains(ViewTableFormat.Flag.GOTO_TOP_ON_REFRESH);
  }

  public DominoViewFormat setGotoTopOnRefresh(boolean b) {
    this.getFormat1().setFlag(ViewTableFormat.Flag.GOTO_TOP_ON_REFRESH, b);
    return this;
  }
  
  public boolean isHierarchical() {
    return !this.getFlags1().contains(ViewTableFormat.Flag.FLATINDEX);
  }

  public DominoViewFormat setHierarchical(boolean b) {
    this.getFormat1().setFlag(ViewTableFormat.Flag.FLATINDEX, !b);
    return this;
  }
  
  // *******************************************************************************
  // * Layout-reader hooks
  // *******************************************************************************

  public void read(final ViewTableFormat format1) {
    this.format1 = format1;
  }

  public void read(final ViewTableFormat2 format2) {
    this.format2 = format2;
  }

  public void read(final ViewTableFormat3 format3) {
    this.format3 = format3;
  }

  public void read(final ViewTableFormat4 format4) {
    this.format4 = format4;
  }

  public void readBackgroundResource(final CDResource resource) {
    this.backgroundResource = resource;
  }
  
  // *******************************************************************************
  // * Internal implementation utilities
  // *******************************************************************************

  public ViewTableFormat getFormat1() {
    return Objects.requireNonNull(this.format1, "VIEW_TABLE_FORMAT not read");
  }

  public Optional<ViewTableFormat2> getFormat2(boolean createIfMissing) {
    if (this.format2==null && createIfMissing) {
      this.format2 = ViewTableFormat2.newInstance();
    }
    
    return Optional.ofNullable(this.format2);
  }

  public Optional<ViewTableFormat3> getFormat3(boolean createIfMissing) {
    if (createIfMissing) {
      getFormat2(true);
    }
    
    if (this.format3==null && createIfMissing) {
      this.format3 = ViewTableFormat3.newInstance();
    }
    
    return Optional.ofNullable(this.format3);
  }

  public Optional<ViewTableFormat4> getFormat4(boolean createIfMissing) {
    if (createIfMissing) {
      getFormat3(true);
    }
    
    if (this.format4==null && createIfMissing) {
      this.format4 = ViewTableFormat4.newInstance();
    }
    
    return Optional.ofNullable(this.format4);
  }

  private Set<ViewTableFormat.Flag> getFlags1() {
    final ViewTableFormat format1 = getFormat1();
    final Set<ViewTableFormat.Flag> flags = format1.getFlags();
    return flags;
  }
}
