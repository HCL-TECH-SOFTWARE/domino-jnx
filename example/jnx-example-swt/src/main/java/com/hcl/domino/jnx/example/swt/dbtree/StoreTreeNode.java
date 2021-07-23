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

import java.util.EnumSet;
import java.util.Set;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.jnx.example.swt.App;
import com.hcl.domino.jnx.example.swt.AppShell;
import com.hcl.domino.jnx.example.swt.info.StoreInfoPane;

public class StoreTreeNode extends DBListTreeNode {
  public enum Type {
    DATA(EnumSet.of(DocumentClass.DATA)),
    DESIGN(EnumSet.of(DocumentClass.ALLNONDATA));

    private final Set<DocumentClass> documentClass;

    Type(final Set<DocumentClass> documentClass) {
      this.documentClass = documentClass;
    }

    public Set<DocumentClass> getDocumentClass() {
      return this.documentClass;
    }
  }

  private final Type type;
  private final String serverName;
  private final String databasePath;

  public StoreTreeNode(final String serverName, final String databasePath, final Type type) {
    super(serverName + databasePath + type);

    this.serverName = serverName;
    this.databasePath = databasePath;
    this.type = type;
  }

  @Override
  public void displayInfoPane(final Composite target) {
    super.displayInfoPane(target);

    new StoreInfoPane(target, this.serverName, this.databasePath, this.getType());

    target.layout();
  }

  public String getDatabasePath() {
    return this.databasePath;
  }

  @Override
  public Image getImage() {
    switch (this.getType()) {
      case DESIGN:
        return AppShell.resourceManager.createImage(App.IMAGE_STORE_LOCAL);
      case DATA:
      default:
        return AppShell.resourceManager.createImage(App.IMAGE_STORE);
    }
  }

  public Type getType() {
    return this.type;
  }

  @Override
  public String toString() {
    return this.getType().name();
  }

}
