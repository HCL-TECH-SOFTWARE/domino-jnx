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
package com.hcl.domino.jnx.example.swt;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.hcl.domino.jnx.example.swt.dbtree.DatabaseTree;

public class AppShell extends Shell {

	private DatabaseTree databaseBrowser;
	public static ResourceManager resourceManager;

	public AppShell(Display display) {
		super(display, SWT.SHELL_TRIM);
		
		setText(App.APP_NAME);
		setSize(1024, 768);
		setLayout(new FillLayout(SWT.HORIZONTAL));

		resourceManager = new LocalResourceManager(JFaceResources.getResources(), this);
		
		SashForm sashForm = new SashForm(this, SWT.NONE);
	
		databaseBrowser = new DatabaseTree(sashForm, resourceManager);
		
		Composite infoPane = new Composite(sashForm, SWT.NONE);
		infoPane.setLayout(new FillLayout());
		databaseBrowser.setTarget(infoPane);

		sashForm.setWeights(new int[] { 1, 3 });
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
