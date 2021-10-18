package com.hcl.domino.jnx.example.swt.doc;

import org.eclipse.swt.graphics.Image;

import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.jnx.example.swt.dbtree.InfoPaneTreeNode;

public class DocumentItemTreeNode extends InfoPaneTreeNode {

  public DocumentItemTreeNode(String itemName, ItemDataType type) {
    super(itemName);
  }

  @Override
  public Image getImage() {
    // TODO Auto-generated method stub
    return null;
  }
  
  public String getItemName() {
    return (String)getValue();
  }
  
  @Override
  public String toString() {
    return getItemName();
  }

}
