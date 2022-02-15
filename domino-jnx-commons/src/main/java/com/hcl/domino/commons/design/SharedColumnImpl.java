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
package com.hcl.domino.commons.design;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.hcl.domino.commons.design.view.CollationEncoder;
import com.hcl.domino.commons.design.view.DominoCollationInfo;
import com.hcl.domino.commons.design.view.DominoViewFormat;
import com.hcl.domino.data.CollectionColumn;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.SharedColumn;
import com.hcl.domino.formula.FormulaCompiler;
import com.hcl.domino.misc.NotesConstants;

public class SharedColumnImpl extends AbstractDesignElement<SharedColumn> implements IDefaultNamedDesignElement, SharedColumn {
  private DominoViewFormat format;
  
  public SharedColumnImpl(Document doc) {
    super(doc);
  }

  @Override
  public void initializeNewDesignNote() {
    setFlag(NotesConstants.DESIGN_FLAG_SHARED_COL, true);
    setFlag(NotesConstants.DESIGN_FLAG_NO_MENU, true);

    Document doc = getDocument();
    
    DominoCollationInfo collationInfo = new DominoCollationInfo();
    doc.replaceItemValue(DesignConstants.VIEW_COLLATION_ITEM,
        EnumSet.of(ItemFlag.SUMMARY), collationInfo);
    doc.replaceItemValue(NotesConstants.VIEW_COMMENT_ITEM,
        EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), ""); //$NON-NLS-1$
    doc.replaceItemValue(DesignConstants.DESIGNER_VERSION, "8.5.3"); //$NON-NLS-1$
    doc.replaceItemValue(DesignConstants.VIEW_INDEX_ITEM,
        EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), ""); //$NON-NLS-1$
    doc.replaceItemValue(DesignConstants.VIEW_CLASSES_ITEM, EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), "1");
    
    this.format = new DominoViewFormat(doc);
    CollectionColumn col = this.format.addColumn(-1);
    
    col.setTitle("#"); //$NON-NLS-1$
    col.setItemName("$0"); //$NON-NLS-1$
    col.setFormula("@DocNumber"); //$NON-NLS-1$

    this.format.setDirty(true);
  }

  @Override
  public CollectionColumn getColumn() {
    Optional<DominoViewFormat> viewFormat = getViewFormat(false);
    if (viewFormat.isPresent()) {
      List<CollectionColumn> columns = viewFormat.get().getColumns();
      if (!columns.isEmpty()) {
        return columns.get(0);
      }
    }
    //should not be the case, we add a column on creation and so does Domino Designer
    throw new IllegalStateException("SharedColumn has no content and needs to be initialized first");
  }

  //*******************************************************************************
  // * Internal utility methods
  // *******************************************************************************

   private synchronized Optional<DominoViewFormat> getViewFormat(boolean createIfMissing) {
     final Document doc = this.getDocument();
     if (this.format==null) {
       List<?> viewFormatAsList = doc.getItemValue(DesignConstants.VIEW_VIEW_FORMAT_ITEM);
       if (!viewFormatAsList.isEmpty()) {
         this.format = (DominoViewFormat) viewFormatAsList.get(0);
       }
     }
     
     if (this.format == null && createIfMissing) {
       this.format = new DominoViewFormat(doc);
       this.format.setDirty(true);
     }
     
     return Optional.ofNullable(this.format);
   }
   
   public boolean isViewFormatDirty() {
     if (this.format!=null) {
       return this.format.isDirty();
     }
     else {
       return false;
     }
   }
   
   public void setViewFormatDirty(boolean b) {
     if (this.format!=null) {
       this.format.setDirty(b);
     }
   }

   @Override
  public boolean save() {
     if (isViewFormatDirty()) {
       if (this.format!=null) {
         final Document doc = this.getDocument();
         doc.replaceItemValue(DesignConstants.VIEW_VIEW_FORMAT_ITEM,
             EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY),
             this.format);
         
         List<DominoCollationInfo> collations = Objects.requireNonNull(CollationEncoder.newCollationFromViewFormat(this.format));
         if (collations.isEmpty()) {
           throw new IllegalStateException("CollationEncoder returned empty list");
         }
         
         //remove old collations
         doc.removeItem(DesignConstants.VIEW_COLLATION_ITEM); //$NON-NLS-1$
         int idx = 1;
         while (doc.hasItem(DesignConstants.VIEW_COLLATION_ITEM+Integer.toString(idx))) {
           doc.removeItem(DesignConstants.VIEW_COLLATION_ITEM+Integer.toString(idx));
           idx++;
         }
         
         //write new collations
         doc.replaceItemValue(DesignConstants.VIEW_COLLATION_ITEM,
             EnumSet.of(ItemFlag.SUMMARY), collations.get(0));
         
         if (collations.size()>1) {
           for (int i=1; i<collations.size(); i++) {
             DominoCollationInfo currCollation = collations.get(i);
             doc.replaceItemValue(DesignConstants.VIEW_COLLATION_ITEM+Integer.toString(i),
                 EnumSet.of(ItemFlag.SUMMARY), currCollation);
           }
         }

         //rebuild $FORMULA item
         {
           LinkedHashMap<String,String> columnItemNamesAndFormulas = new LinkedHashMap<String, String>();
           for (CollectionColumn currCol : this.format.getColumns()) {
             String currItemName = currCol.getItemName();
             String currFormula = currCol.getFormula();
             columnItemNamesAndFormulas.put(currItemName, currFormula);
           }

           final String selectionFormula = "SELECT @All"; //$NON-NLS-1$
           byte[] formulaItemData = FormulaCompiler.get().compile(selectionFormula, columnItemNamesAndFormulas);
           ByteBuffer formulaItemDataBuf = ByteBuffer.allocate(formulaItemData.length + 2).order(ByteOrder.nativeOrder());
           formulaItemDataBuf.putShort(ItemDataType.TYPE_FORMULA.getValue());
           formulaItemDataBuf.put(formulaItemData);
           
           formulaItemDataBuf.position(0);
           
           doc.replaceItemValue(DesignConstants.VIEW_FORMULA_ITEM, EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), formulaItemDataBuf);
           doc.getFirstItem(DesignConstants.VIEW_FORMULA_ITEM).get().setSummary(true);
         }

         doc.sign();
       }
       
       setViewFormatDirty(false);
     }
     
     return super.save();
  }

   public void setTitle(final String... title) {
     //$TITLE for views/folders/sharedcolumns are stored differently than for forms (concatenated with |, no string list)
     this.getDocument().replaceItemValue(NotesConstants.FIELD_TITLE,
         EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY),
         Arrays.asList(title).stream().collect(Collectors.joining("|"))); //$NON-NLS-1$
   }

}
