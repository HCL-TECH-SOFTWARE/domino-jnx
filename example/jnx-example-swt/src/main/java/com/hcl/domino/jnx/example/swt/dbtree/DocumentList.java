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

import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.FormulaQueryResult;
import com.hcl.domino.dbdirectory.DirectorySearchQuery.SearchFlag;
import com.hcl.domino.exception.FormulaCompilationException;
import com.hcl.domino.jnx.example.swt.doc.DocumentShell;

import jakarta.enterprise.inject.spi.CDI;

public class DocumentList extends Composite {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  private Text searchBox;
  private TableViewer docs;
  private final String serverName;
  private final String databasePath;
  private final StoreTreeNode.Type type;
  private String searchQuery;

  public DocumentList(final Composite parent, final String serverName, final String databasePath, final StoreTreeNode.Type type) {
    super(parent, SWT.NONE);
    this.setLayout(new GridLayout(1, false));
    this.serverName = serverName;
    this.databasePath = databasePath;
    this.type = type;

    this.createChildren();
    this.connectActions();
    this.resetDocList();

    this.layout();
  }

  private void connectActions() {
    this.docs.addDoubleClickListener(event -> {
      @SuppressWarnings("unchecked")
      final Map<String, Object> entry = (Map<String, Object>) this.docs.getStructuredSelection().getFirstElement();

      final DocumentShell shell = new DocumentShell(this.getDisplay(), this.serverName, this.databasePath,
          (String) entry.get("$unid")); //$NON-NLS-1$
      shell.open();
      shell.layout();
    });

    this.searchBox.addListener(SWT.KeyDown, event -> {
      if (event.keyCode == SWT.CR) {
        final String query = this.searchBox.getText();
        this.searchQuery = query;
        this.resetDocList();
      }
    });
  }

  private void createChildren() {

    this.searchBox = new Text(this, SWT.BORDER | SWT.SEARCH);
    this.searchBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    this.searchBox.setMessage("Formula query");

    this.docs = new TableViewer(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
    this.docs.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    this.docs.setUseHashlookup(true);
    this.docs.setContentProvider(new DocumentListContentProvider(this.docs));

    final Table table = this.docs.getTable();
    table.setHeaderVisible(true);
    table.setLinesVisible(true);
    final FontDescriptor tableFont = FontDescriptor.createFrom(table.getFont()).increaseHeight(-2);
    table.setFont(tableFont.createFont(this.getDisplay()));

    // Initialize for the plain document search
    this.initPlainDocsTable(this.docs);
  }

  private void initPlainDocsTable(final TableViewer docs) {
    final TableViewerColumn unidCol = new TableViewerColumn(docs, SWT.NONE);
    unidCol.getColumn().setText("UNID");
    unidCol.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public void update(final ViewerCell cell) {
        @SuppressWarnings("unchecked")
        final Map<String, Object> entry = (Map<String, Object>) cell.getElement();
        cell.setText(String.valueOf(entry.get("$unid"))); //$NON-NLS-1$
      }
    });
    unidCol.getColumn().setWidth(250);

    final TableViewerColumn createdCol = new TableViewerColumn(docs, SWT.NONE);
    createdCol.getColumn().setText("Created");
    createdCol.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public void update(final ViewerCell cell) {
        @SuppressWarnings("unchecked")
        final Map<String, Object> entry = (Map<String, Object>) cell.getElement();
        cell.setText(DocumentList.DATE_FORMAT.format((Temporal) entry.get("$created"))); //$NON-NLS-1$
      }
    });
    createdCol.getColumn().setWidth(125);

    final TableViewerColumn formCol = new TableViewerColumn(docs, SWT.NONE);
    formCol.getColumn().setText("Form");
    formCol.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public void update(final ViewerCell cell) {
        @SuppressWarnings("unchecked")
        final Map<String, Object> entry = (Map<String, Object>) cell.getElement();
        cell.setText((String) entry.get("form")); //$NON-NLS-1$
      }
    });
    formCol.getColumn().setWidth(125);
  }

  private void resetDocList() {
    String searchQuery = this.searchQuery;
    if (searchQuery == null || searchQuery.isEmpty()) {
      searchQuery = "@All"; //$NON-NLS-1$
    }

    try {
      final String fSearchQuery = searchQuery;
      final ExecutorService exec = CDI.current().select(ExecutorService.class).get();
      final FormulaQueryResult result = exec.submit(() -> {
        final DominoClient client = CDI.current().select(DominoClient.class).get();
        final Database database = client.openDatabase(this.serverName, this.databasePath);
        return database.queryFormula(fSearchQuery, null, EnumSet.noneOf(SearchFlag.class), null, this.type.getDocumentClass());
      }).get();

      this.docs.setInput(result);
      this.docs.setItemCount(exec.submit(() -> result.size()).get());
    } catch (FormulaCompilationException | InterruptedException | ExecutionException e) {
      MessageDialog.openError(this.getShell(), "Error Interpreting Search", e.getMessage());
    }
  }
}