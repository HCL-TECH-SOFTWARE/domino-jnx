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
package com.hcl.domino.commons.design;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import com.hcl.domino.commons.design.view.DominoViewColumnFormat;
import com.hcl.domino.commons.design.view.DominoViewFormat;
import com.hcl.domino.commons.design.view.ViewFormatEncoder;
import com.hcl.domino.data.CollectionColumn;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.NativeItemCoder;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.NativeDesignSupport;
import com.hcl.domino.design.SharedColumn;

public class SharedColumnImpl extends AbstractDesignElement<SharedColumn> implements IDefaultNamedDesignElement, SharedColumn {
  private Optional<DominoViewFormat> format;
  private boolean viewFormatDirty;
  
  public SharedColumnImpl(Document doc) {
    super(doc);
  }

  @Override
  public void initializeNewDesignNote() {
    // TODO Auto-generated method stub
  }

  @Override
  public CollectionColumn getColumn() {
    Optional<DominoViewFormat> viewFormat = readViewFormat();
    if (viewFormat.isPresent()) {
      List<CollectionColumn> columns = viewFormat.get().getColumns();
      if (!columns.isEmpty()) {
        return columns.get(0);
      }
    }
    //should not be the case, we add a column on creation
    throw new IllegalStateException("SharedColumn has no content and needs to be initialized first");
  }

  //*******************************************************************************
  // * Internal utility methods
  // *******************************************************************************

   private synchronized Optional<DominoViewFormat> readViewFormat() {
     if (this.format == null) {
       final Document doc = this.getDocument();
       this.format = doc.getOptional(DesignConstants.VIEW_VIEW_FORMAT_ITEM, DominoViewFormat.class);
       this.format.ifPresent(format ->
         format.getColumns()
           .stream()
           .map(DominoViewColumnFormat.class::cast)
           .forEach(col -> col.setParent(this))
       );
     }
     return this.format;
   }
   
   public boolean isViewFormatDirty() {
     return viewFormatDirty;
   }
   
   public void setViewFormatDirty(boolean b) {
     this.viewFormatDirty = b;
   }

   @Override
  public boolean save() {
     if (isViewFormatDirty()) {
       if (this.format!=null && this.format.isPresent()) {
         final Document doc = this.getDocument();
         doc.replaceItemValue(DesignConstants.VIEW_VIEW_FORMAT_ITEM,
             EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY),
             this.format.get());
         doc.sign();
       }
       
       setViewFormatDirty(false);
     }
     
     return super.save();
  }
   
}
