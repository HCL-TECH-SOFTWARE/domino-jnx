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

import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.swt.graphics.Image;

import com.hcl.domino.jnx.example.swt.App;
import com.hcl.domino.jnx.example.swt.AppShell;

public class DatabaseTreeNode extends InfoPaneTreeNode {
  private TreeNode[] children;
  private final String serverName;
  private final String databasePath;

  public DatabaseTreeNode(final String serverName, final String databasePath) {
    super(serverName + "!!" + databasePath); //$NON-NLS-1$

    this.serverName = serverName;
    this.databasePath = databasePath;
  }

  @Override
  public synchronized TreeNode[] getChildren() {
    if (this.children == null) {
      return new TreeNode[] {
          new StoreTreeNode(this.serverName, this.databasePath, StoreTreeNode.Type.DESIGN),
          new StoreTreeNode(this.serverName, this.databasePath, StoreTreeNode.Type.DATA)
      };
    }
    return this.children;
  }

  public String getDatabasePath() {
    return this.databasePath;
  }

  @Override
  public Image getImage() {
    return AppShell.resourceManager.createImage(App.IMAGE_DATABASE);
  }

  public String getServerName() {
    return this.serverName;
  }

  @Override
  public boolean hasChildren() {
    return true;
  }

  @Override
  public String toString() {
    return this.getDatabasePath();
  }
}
