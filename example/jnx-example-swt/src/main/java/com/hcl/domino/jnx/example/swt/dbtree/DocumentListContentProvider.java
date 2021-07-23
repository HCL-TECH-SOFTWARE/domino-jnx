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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import com.hcl.domino.data.DbQueryResult;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.data.Item;
import com.hcl.domino.data.ItemDataType;

import jakarta.enterprise.inject.spi.CDI;

public class DocumentListContentProvider implements ILazyContentProvider {

  private final TableViewer tableViewer;
  private DbQueryResult<?> result;
  private int[] noteIds;
  private List<Map<String, Object>> entries;

  public DocumentListContentProvider(final TableViewer tableViewer) {
    this.tableViewer = tableViewer;
  }

  private void fetchPageTo(final int index) {
    final int start = this.entries.size();
    if (index > start - 1) {
      final ExecutorService exec = CDI.current().select(ExecutorService.class).get();
      try {
        exec.submit(() -> Arrays.stream(this.noteIds)
            .skip(start)
            .limit(index - start + 1)
            .mapToObj(noteId -> this.result.getParentDatabase().getDocumentById(noteId))
            .map(Optional::get)
            .map(doc -> {
              final Map<String, Object> entryData = new HashMap<>();
              entryData.put("$unid", doc.getUNID()); //$NON-NLS-1$
              entryData.put("$created", doc.getCreated()); //$NON-NLS-1$
              final Item formItem = doc.getFirstItem("Form").orElse(null); //$NON-NLS-1$
              if (formItem != null
                  && (formItem.getType() == ItemDataType.TYPE_TEXT || formItem.getType() == ItemDataType.TYPE_TEXT)) {
                entryData.put("form", formItem.get(String.class, "")); //$NON-NLS-1$ //$NON-NLS-2$
              } else {
                entryData.put("form", ""); //$NON-NLS-1$ //$NON-NLS-2$
              }
              return entryData;
            })
            .forEach(this.entries::add)).get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
    final DbQueryResult<?> result = (DbQueryResult<?>) newInput;
    this.entries = new ArrayList<>();
    this.result = result;
    if (result != null) {
      final ExecutorService exec = CDI.current().select(ExecutorService.class).get();
      try {
        final int count = exec.submit(result::size).get();
        this.tableViewer.setItemCount(count);
        this.noteIds = exec.submit(() -> result.getNoteIds().map(IDTable::toIntArray).orElseGet(() -> new int[0])).get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    } else {
      this.tableViewer.setItemCount(0);
    }
  }

  @Override
  public void updateElement(final int index) {
    this.fetchPageTo(index);
    this.tableViewer.replace(this.entries.get(index), index);
  }
}
