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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

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

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;

import jakarta.enterprise.inject.spi.CDI;

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
//          if (item instanceof DBListTreeNode) {
//            ((DBListTreeNode) item).displayInfoPane(DocumentItemTree.this.target);
//          }
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
    
    ExecutorService exec = CDI.current().select(ExecutorService.class).get();
    
    TreeNode[] nodes;
    try {
      nodes = exec.submit(() -> {
        final DominoClient client = CDI.current().select(DominoClient.class).get();
        final Database database = client.openDatabase(serverName, databasePath);
        final Document doc = database.getDocumentByUNID(unid).get();
        return doc.allItems()
          .map(item -> new DocumentItemTreeNode(item.getName(), item.getType()))
          .toArray(TreeNode[]::new);
      }).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }

    this.itemList.setInput(nodes);
  }

  public void setTarget(final Composite target) {
    this.target = target;
  }
}
