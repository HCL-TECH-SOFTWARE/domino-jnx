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
