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
package com.hcl.domino.jnx.example.swt.doc;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;

import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Item;
import com.hcl.domino.jnx.example.swt.bean.DominoContextBean;
import com.hcl.domino.jnx.example.swt.dbtree.InfoPaneTreeNode;

public class DocumentItemTree extends Composite {
  private TreeViewer itemList;
  private final ResourceManager resourceManager;
  private Composite target;
  private final String serverName;
  private final String databasePath;
  private final String unid;

  public DocumentItemTree(final Composite parent, final ResourceManager resourceManager, final String serverName, final String databasePath, final String unid) {
    super(parent, SWT.NONE);
    this.serverName = serverName;
    this.databasePath = databasePath;
    this.unid = unid;
    
    this.resourceManager = resourceManager;
    this.setLayout(new FillLayout(SWT.HORIZONTAL));

    this.createChildren();
    this.connectActions();
  }

  @Override
  protected void checkSubclass() {
  }

  private void connectActions() {
    this.itemList.getTree().addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        if (e.item != null) {
          final Object item = e.item.getData();
          if (item instanceof InfoPaneTreeNode) {
            ((InfoPaneTreeNode) item).displayInfoPane(DocumentItemTree.this.target);
          }
        } else {
          Arrays.stream(DocumentItemTree.this.target.getChildren()).forEach(Control::dispose);
        }
      }
    });
  }

  private void createChildren() {
    this.itemList = new TreeViewer(this, SWT.BORDER | SWT.FULL_SELECTION);
    this.itemList.setContentProvider(new TreeNodeContentProvider());
    this.itemList.setLabelProvider(new DocumentItemTreeLabelProvider());

    final Tree tree = this.itemList.getTree();
    final Font font = tree.getFont();
    tree.setFont(this.resourceManager.createFont(FontDescriptor.createFrom(font.getFontData()[0].getName(), 10, SWT.NORMAL)));
    tree.setLinesVisible(false);
    
    DominoContextBean.submit(client -> {
      final Database database = client.openDatabase(serverName, databasePath);
      final Document doc = database.getDocumentByUNID(unid).get();
      List<Item> items = doc.allItems().collect(Collectors.toList());
      TreeNode[] nodes = new TreeNode[items.size()];
      
      for(int i = 0; i < items.size(); i++) {
        Item item = items.get(i);
        
        nodes[i] = new DocumentItemTreeNode(item);
      }
      
      getDisplay().asyncExec(() -> this.itemList.setInput(nodes));
    });
  }

  public void setTarget(final Composite target) {
    this.target = target;
  }
}
