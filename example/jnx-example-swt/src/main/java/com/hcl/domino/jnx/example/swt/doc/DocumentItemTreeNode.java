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
package com.hcl.domino.jnx.example.swt.doc;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import com.hcl.domino.data.Item;
import com.hcl.domino.jnx.example.swt.dbtree.InfoPaneTreeNode;

public class DocumentItemTreeNode extends InfoPaneTreeNode {
  private String itemName;

  public DocumentItemTreeNode(Item item) {
    super(item);
    this.itemName = item.getName();
  }

  @Override
  public Image getImage() {
    // TODO Auto-generated method stub
    return null;
  }
  
  public Item getItem() {
    return (Item)getValue();
  }
  
  public String getItemName() {
    return itemName;
  }
  
  @Override
  public String toString() {
    return getItemName();
  }
  
  @Override
  public void displayInfoPane(Composite target) {
    super.displayInfoPane(target);
    
    target.getDisplay().asyncExec(() -> {
      new ItemInfoPane(target, getItem());
      target.layout();
    });
  }

}
