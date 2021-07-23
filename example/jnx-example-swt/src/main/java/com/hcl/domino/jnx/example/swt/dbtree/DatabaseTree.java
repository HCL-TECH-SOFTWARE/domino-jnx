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
package com.hcl.domino.jnx.example.swt.dbtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

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

import com.hcl.domino.jnx.example.swt.bean.DatabasesBean;

import jakarta.enterprise.inject.spi.CDI;

public class DatabaseTree extends Composite {
  private TreeViewer databaseBrowser;
  private final ResourceManager resourceManager;
  private Composite target;

  public DatabaseTree(final Composite parent, final ResourceManager resourceManager) {
    super(parent, SWT.NONE);
    this.resourceManager = resourceManager;
    this.setLayout(new FillLayout(SWT.HORIZONTAL));

    this.createChildren();
    this.connectActions();
  }

  @Override
  protected void checkSubclass() {
  }

  private void connectActions() {
    this.databaseBrowser.getTree().addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        if (e.item != null) {
          final Object item = e.item.getData();
          if (item instanceof DBListTreeNode) {
            ((DBListTreeNode) item).displayInfoPane(DatabaseTree.this.target);
          }
        } else {
          Arrays.stream(DatabaseTree.this.target.getChildren()).forEach(Control::dispose);
        }
      }
    });
  }

  private void createChildren() {
    this.databaseBrowser = new TreeViewer(this, SWT.BORDER | SWT.FULL_SELECTION);
    this.databaseBrowser.setContentProvider(new TreeNodeContentProvider());
    this.databaseBrowser.setLabelProvider(new DatabaseTreeLabelProvider());

    final Tree tree = this.databaseBrowser.getTree();
    final Font font = tree.getFont();
    tree.setFont(this.resourceManager.createFont(FontDescriptor.createFrom(font.getFontData()[0].getName(), 10, SWT.NORMAL)));
    tree.setLinesVisible(false);

    final Collection<String> serverNames = new ArrayList<>();
    serverNames.add(""); //$NON-NLS-1$
    serverNames.addAll(CDI.current().select(DatabasesBean.class).get().getKnownServers());
    this.databaseBrowser.setInput(
        serverNames.stream()
            .map(ServerTreeNode::new)
            .toArray(TreeNode[]::new));
  }

  public void setTarget(final Composite target) {
    this.target = target;
  }
}
