package com.hcl.domino.commons.design;

import com.hcl.domino.commons.design.view.DominoViewColumnFormat;
import com.hcl.domino.commons.design.view.DominoViewFormat;
import com.hcl.domino.data.CollectionColumn;
import com.hcl.domino.data.Document;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.SharedColumn;

public class SharedColumnImpl extends AbstractDesignElement<SharedColumn> implements IDefaultNamedDesignElement, SharedColumn {
  private DominoViewFormat format;

  public SharedColumnImpl(Document doc) {
    super(doc);
  }

  @Override
  public void initializeNewDesignNote() {
    // TODO Auto-generated method stub
  }

  @Override
  public CollectionColumn getColumn() {
    return readViewFormat().getColumns().get(0);
  }

  //*******************************************************************************
  // * Internal utility methods
  // *******************************************************************************

   private synchronized DominoViewFormat readViewFormat() {
     if (this.format == null) {
       final Document doc = this.getDocument();
       this.format = (DominoViewFormat) doc.getItemValue(DesignConstants.VIEW_VIEW_FORMAT_ITEM).get(0);
       this.format.getColumns()
         .stream()
         .map(DominoViewColumnFormat.class::cast)
         .forEach(col -> col.setParent(this));
     }
     return this.format;
   }
}
