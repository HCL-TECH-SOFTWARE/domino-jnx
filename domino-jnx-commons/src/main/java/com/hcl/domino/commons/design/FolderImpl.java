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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.design.Folder;

/**
 * @since 1.0.18
 */
public class FolderImpl extends AbstractCollectionDesignElement<Folder> implements Folder {

  public FolderImpl(final Document doc) {
    super(doc);
  }

  @Override
  public void initializeNewDesignNote() {
    super.initializeNewDesignNote();
    this.setFlags("F3Y"); //$NON-NLS-1$
  }

  @Override
  public void setTitle(String... title) {
    super.setTitle(title);
    
    this.getDocument().replaceItemValue("$Name", //$NON-NLS-1$
        EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY),
        Arrays.asList(title).stream().collect(Collectors.joining("|"))); //$NON-NLS-1$
  }
 
  @Override
  public boolean save() {
    boolean result = super.save();
    
    Document doc = getDocument();
    if (doc.isNew()) {
      //triggers creation of the $Collection item
      Database db = doc.getParentDatabase();
      DominoCollection collection = db.openCollectionByUNID(doc.getUNID()).get();
      collection.getAllIds(true, true);
    }
    
    return result;
  }
  
}
