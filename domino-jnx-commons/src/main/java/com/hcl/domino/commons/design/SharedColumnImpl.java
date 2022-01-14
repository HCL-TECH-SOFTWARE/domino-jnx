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
