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

import jakarta.enterprise.inject.spi.CDI;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import com.hcl.domino.data.DbQueryResult;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.data.Item;
import com.hcl.domino.data.ItemDataType;

public class DocumentListContentProvider implements ILazyContentProvider {

	private final TableViewer tableViewer;
	private DbQueryResult<?> result;
	private int[] noteIds;
	private List<Map<String, Object>> entries;
	
	public DocumentListContentProvider(TableViewer tableViewer) {
		this.tableViewer = tableViewer;
	}
	
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		DbQueryResult<?> result = (DbQueryResult<?>)newInput;
		this.entries = new ArrayList<>();
		this.result = result;
		if(result != null) {
			ExecutorService exec = CDI.current().select(ExecutorService.class).get();
			try {
				int count = exec.submit(result::size).get();
				tableViewer.setItemCount(count);
				noteIds = exec.submit(() -> result.getNoteIds().map(IDTable::toIntArray).orElseGet(() -> new int[0])).get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		} else {
			tableViewer.setItemCount(0);
		}
	}

	@Override
	public void updateElement(int index) {
		fetchPageTo(index);
		tableViewer.replace(entries.get(index), index);
	}
	
	private void fetchPageTo(int index) {
		int start = entries.size();
		if(index > start-1) {
			ExecutorService exec = CDI.current().select(ExecutorService.class).get();
			try {
				exec.submit(() -> 
					Arrays.stream(this.noteIds)
						.skip(start)
						.limit(index-start+1)
						.mapToObj(noteId -> result.getParentDatabase().getDocumentById(noteId))
						.map(Optional::get)
						.map(doc -> {
							Map<String, Object> entryData = new HashMap<>();
							entryData.put("$unid", doc.getUNID()); //$NON-NLS-1$
							entryData.put("$created", doc.getCreated()); //$NON-NLS-1$
							Item formItem = doc.getFirstItem("Form").orElse(null); //$NON-NLS-1$
							if(formItem != null && (formItem.getType() == ItemDataType.TYPE_TEXT || formItem.getType() == ItemDataType.TYPE_TEXT)) {
								entryData.put("form", formItem.get(String.class, "")); //$NON-NLS-1$ //$NON-NLS-2$
							} else {
								entryData.put("form", ""); //$NON-NLS-1$ //$NON-NLS-2$
							}
							return entryData;
						})
						.forEach(entries::add)
				).get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
