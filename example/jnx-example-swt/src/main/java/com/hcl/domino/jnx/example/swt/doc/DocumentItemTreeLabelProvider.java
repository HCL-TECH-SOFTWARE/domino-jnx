package com.hcl.domino.jnx.example.swt.doc;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

public class DocumentItemTreeLabelProvider extends LabelProvider implements ILabelProvider, ITableColorProvider {

  @Override
  public Color getBackground(final Object var1, final int var2) {
    return null;
  }

  @Override
  public Color getForeground(final Object var1, final int var2) {
    return null;
  }

  @Override
  public Image getImage(final Object element) {
//    if (element instanceof DBListTreeNode) {
//      return ((DBListTreeNode) element).getImage();
//    }
    return super.getImage(element);
  }

  @Override
  public String getText(final Object element) {
//    if (element instanceof ServerTreeNode) {
//      return ((ServerTreeNode) element).getDisplayName();
//    }
    return String.valueOf(element);
  }

}