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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.dxl.DxlExporter;
import com.hcl.domino.jnx.example.swt.AppShell;
import com.hcl.domino.jnx.example.swt.dbtree.DatabaseTree;
import com.hcl.domino.jnx.example.swt.info.AbstractInfoPane;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.bind.Jsonb;

public class DocumentInfoPane extends AbstractInfoPane {

  public DocumentInfoPane(final Composite parent, final String serverName, final String databasePath, final String unid) {
    super(parent, unid);
    
    final ExecutorService exec = CDI.current().select(ExecutorService.class).get();

    this.info("UNID", unid); //$NON-NLS-1$
    
    try {
      int noteId = exec.submit(() -> {
        final DominoClient client = CDI.current().select(DominoClient.class).get();
        final Database database = client.openDatabase(serverName, databasePath);
        return database.toNoteId(unid);
      }).get();
      this.info("Note ID", "0x" + Integer.toHexString(noteId));
      
      TabFolder tabFolder = new TabFolder(this, SWT.NONE);
      tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 2, 1));
      
      // Item list
      {
        TabItem tab = new TabItem(tabFolder, SWT.NONE);
        tab.setText("Items");
        
        final SashForm sashForm = new SashForm(tabFolder, SWT.NONE);
        tab.setControl(sashForm);
        DocumentItemTree tree = new DocumentItemTree(sashForm, AppShell.resourceManager, serverName, databasePath, unid);

        final Composite infoPane = new Composite(sashForm, SWT.NONE);
        infoPane.setLayout(new FillLayout());
        tree.setTarget(infoPane);
        sashForm.setWeights(1, 3);
      }
      
      // JSON Export
      {
        TabItem tab = new TabItem(tabFolder, SWT.NONE);
        tab.setText("JSON");
        
        Text editor = new Text(tabFolder, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);
        tab.setControl(editor);
        editor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        // TODO pick a non-named font -
        // https://stackoverflow.com/questions/221568/swt-os-agnostic-way-to-get-monospaced-font
        editor.setFont(new Font(parent.getDisplay(), "Consolas", 12, SWT.NORMAL)); //$NON-NLS-1$

        final Jsonb jsonb = CDI.current().select(Jsonb.class).get();
        final String json = exec.submit(() -> {
          final DominoClient client = CDI.current().select(DominoClient.class).get();
          final Database database = client.openDatabase(serverName, databasePath);
          final Document doc = database.getDocumentByUNID(unid).get();
          return jsonb.toJson(doc);
        }).get();
        editor.setText(json);

        editor.clearSelection();
      }
      
      // DXL Export
      {
        TabItem tab = new TabItem(tabFolder, SWT.NONE);
        tab.setText("DXL");
        
        Text editor = new Text(tabFolder, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);
        tab.setControl(editor);
        editor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        // TODO pick a non-named font -
        // https://stackoverflow.com/questions/221568/swt-os-agnostic-way-to-get-monospaced-font
        editor.setFont(new Font(parent.getDisplay(), "Consolas", 12, SWT.NORMAL)); //$NON-NLS-1$

        // TODO delay until display
        final String dxl = exec.submit(() -> {
          final DominoClient client = CDI.current().select(DominoClient.class).get();
          final Database database = client.openDatabase(serverName, databasePath);
          final Document doc = database.getDocumentByUNID(unid).get();
          DxlExporter exporter = client.createDxlExporter();
          return exporter.exportDocument(doc);
        }).get();
        editor.setText(dxl);

        editor.clearSelection();
      }
      
      this.layout();

    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

}
