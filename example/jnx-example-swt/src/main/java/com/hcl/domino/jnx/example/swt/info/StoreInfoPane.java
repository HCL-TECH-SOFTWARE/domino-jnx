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
package com.hcl.domino.jnx.example.swt.info;

import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.jnx.example.swt.dbtree.DocumentList;
import com.hcl.domino.jnx.example.swt.dbtree.StoreTreeNode;

import jakarta.enterprise.inject.spi.CDI;

public class StoreInfoPane extends AbstractInfoPane {

  private final String serverName;
  private final String databasePath;
  private final StoreTreeNode.Type type;

  public StoreInfoPane(final Composite parent, final String serverName, final String databasePath, final StoreTreeNode.Type type) {
    super(parent, MessageFormat.format("Store: {0}!!{1} {2}", serverName, databasePath, type));
    this.serverName = serverName;
    this.databasePath = databasePath;
    this.type = type;

    this.createChildren();
  }

  protected void createChildren() {
    try {
      final int noteCount = CDI.current().select(ExecutorService.class).get().submit(() -> {
        final Database database = CDI.current().select(DominoClient.class).get().openDatabase(this.serverName, this.databasePath);
        return database.getModifiedNoteIds(this.type.getDocumentClass(), null, false).size();
      }).get();
      this.info("Note Count", noteCount);

      final DocumentList docList = new DocumentList(this, this.serverName, this.databasePath, this.type);
      docList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }
}
