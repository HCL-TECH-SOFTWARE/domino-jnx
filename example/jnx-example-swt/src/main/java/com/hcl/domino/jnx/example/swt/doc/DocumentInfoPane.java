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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import jakarta.enterprise.inject.spi.CDI;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Item;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.jnx.example.swt.info.AbstractInfoPane;

import jakarta.json.bind.Jsonb;

public class DocumentInfoPane extends AbstractInfoPane {

	private Text editor;
	
	public DocumentInfoPane(Composite parent, String serverName, String databasePath, String unid) {
		super(parent, unid);
		
		info("UNID", unid); //$NON-NLS-1$
		ExecutorService exec = CDI.current().select(ExecutorService.class).get();
		
		try {
			@SuppressWarnings("unchecked")
			List<String> categories = exec.submit(() -> {
				DominoClient client = CDI.current().select(DominoClient.class).get();
				Database database = client.openDatabase(serverName, databasePath);
				Document doc = database.getDocumentByUNID(unid).get();
				Item catItem = doc.getFirstItem("Categories").orElse(null); //$NON-NLS-1$
				
				if(catItem != null && (catItem.getType() == ItemDataType.TYPE_TEXT || catItem.getType() == ItemDataType.TYPE_TEXT_LIST)) {
					return catItem.getAsList(String.class, Collections.emptyList());
				} else {
					return Collections.EMPTY_LIST;
				}
			}).get();
			
			info("Categories", categories);
			
			editor = new Text(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);
			editor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			// TODO pick a non-named font - https://stackoverflow.com/questions/221568/swt-os-agnostic-way-to-get-monospaced-font
			editor.setFont(new Font(parent.getDisplay(), "Consolas", 12, SWT.NORMAL)); //$NON-NLS-1$
			
			
			Jsonb jsonb = CDI.current().select(Jsonb.class).get();
			String json = exec.submit(() -> {
				DominoClient client = CDI.current().select(DominoClient.class).get();
				Database database = client.openDatabase(serverName, databasePath);
				Document doc = database.getDocumentByUNID(unid).get();
				return jsonb.toJson(doc);
			}).get();
			editor.setText(json);
			layout();
			
			editor.clearSelection();
		} catch(ExecutionException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public String getJsonString() {
		return editor.getText();
	}
	
}
