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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.hcl.domino.jnx.example.swt.App;

public class DocumentShell extends Shell {
	private Listener closeListener = event -> {
		if(isDisposed()) { return; }
		if(getDisplay().getActiveShell() == this && event.stateMask == SWT.COMMAND && event.keyCode == 'w') {
			close();
		}
	};
	
	public DocumentShell(Display display, String serverName, String databasePath, String unid) {
		super(display);
		
		setText("Document " + unid + " - " + App.APP_NAME);
		
		setLayout(new GridLayout(1, false));
		setSize(1024, 768);
		
		DocumentInfoPane docInfo = new DocumentInfoPane(this, serverName, databasePath, unid);
		docInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite actions = new Composite(this, SWT.NONE);
		GridData actionBarData = new GridData(SWT.RIGHT, SWT.FILL, true, false, 1, 1);
		actions.setLayoutData(actionBarData);
		actions.setLayout(new RowLayout());
		
		Button cancel = new Button(actions, SWT.PUSH);
		cancel.setText("Cancel");
		RowData cancelData = new RowData();
		cancelData.width = 100;
		cancel.setLayoutData(cancelData);
		cancel.setSize(100, cancel.getSize().y);
		cancel.addListener(SWT.Selection, evt -> close());
		
		layout();
		
		display.addFilter(SWT.KeyDown, this.closeListener);
	}
	
	@Override
	protected void checkSubclass() {
		
	}

	@Override
	public void close() {
		getDisplay().removeFilter(SWT.KeyDown, this.closeListener);
		super.close();
	}
}