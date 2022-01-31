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
package com.hcl.domino.jnx.example.swt.dbtree;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.hcl.domino.jnx.example.swt.App;
import com.hcl.domino.jnx.example.swt.AppShell;
import com.hcl.domino.jnx.example.swt.bean.DatabasesBean;
import com.hcl.domino.jnx.example.swt.info.ServerInfoPane;

import jakarta.enterprise.inject.spi.CDI;

public class ServerTreeNode extends InfoPaneTreeNode {
  private TreeNode[] children;
  private final String displayName;

  public ServerTreeNode(String displayName, final String serverName) {
    super(serverName);
    this.displayName = displayName;
  }

  @Override
  public void displayInfoPane(final Composite target) {
    super.displayInfoPane(target);

    new ServerInfoPane(target, this.getServerName());

    target.layout();
  }

  @Override
  public TreeNode[] getChildren() {
    if (this.children == null) {
      try {
        final DatabasesBean databasesBean = CDI.current().select(DatabasesBean.class).get();
        return databasesBean.getDatabasePaths(this.getServerName()).stream()
            .map(dbName -> new DatabaseTreeNode(this.getServerName(), dbName))
            .toArray(TreeNode[]::new);

      } catch (final Throwable t) {
        Throwable c = t;
        while (c.getCause() != null) {
          c = c.getCause();
        }
        MessageDialog.openError(Display.getCurrent().getActiveShell(), "Unable to List Databases",
            MessageFormat.format("Encountered exception listing databases: {0}", c.getMessage()));
        this.children = new TreeNode[0];
      }
    }
    return this.children;
  }

  @Override
  public Image getImage() {
    return AppShell.resourceManager.createImage(App.IMAGE_SERVER);
  }

  public String getServerName() {
    return (String) this.getValue();
  }
  
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public boolean hasChildren() {
    return true;
  }

  @Override
  public String toString() {
    return this.getServerName();
  }
}
