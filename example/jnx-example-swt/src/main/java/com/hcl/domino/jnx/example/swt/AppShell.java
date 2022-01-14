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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.hcl.domino.jnx.example.swt.console.ConsolePane;
import com.hcl.domino.jnx.example.swt.dbtree.DatabaseTree;
import com.hcl.domino.jnx.example.swt.dump.DumpDBPane;
import com.hcl.domino.jnx.example.swt.exporter.DXLExporterPane;

public class AppShell extends Shell {

  public static ResourceManager resourceManager;
  private final DatabaseTree databaseBrowser;

  public AppShell(final Display display) {
    super(display, SWT.SHELL_TRIM);

    this.setText(App.APP_NAME);
    this.setSize(1024, 768);
    this.setLayout(new FillLayout(SWT.HORIZONTAL));

    AppShell.resourceManager = new LocalResourceManager(JFaceResources.getResources(), this);

    
    TabFolder tabFolder = new TabFolder(this, SWT.NONE);
    
    // Database Browser
    {
      TabItem tab = new TabItem(tabFolder, SWT.NONE);
      tab.setText("Database Browser");
      
      final SashForm sashForm = new SashForm(tabFolder, SWT.NONE);
      tab.setControl(sashForm);
      this.databaseBrowser = new DatabaseTree(sashForm, AppShell.resourceManager);

      final Composite infoPane = new Composite(sashForm, SWT.NONE);
      infoPane.setLayout(new FillLayout());
      this.databaseBrowser.setTarget(infoPane);
      sashForm.setWeights(1, 3);
    }
    
    // DXL Exporter
    {
      TabItem tab = new TabItem(tabFolder, SWT.NONE);
      tab.setText("DXL Exporter");
      
      DXLExporterPane pane = new DXLExporterPane(tabFolder, SWT.NONE);
      tab.setControl(pane);
    }
    
    // DXL Dump
    {
      TabItem tab = new TabItem(tabFolder, SWT.NONE);
      tab.setText("DXL Dump");
      
      DumpDBPane pane = new DumpDBPane(tabFolder, SWT.NONE);
      tab.setControl(pane);
    }

    // Server Console
    {
      TabItem tab = new TabItem(tabFolder, SWT.NONE);
      tab.setText("Server Console");
      
      ConsolePane pane = new ConsolePane(tabFolder);
      tab.setControl(pane);
    }
  }

  @Override
  protected void checkSubclass() {
    // Disable the check that prevents subclassing of SWT components
  }
}
