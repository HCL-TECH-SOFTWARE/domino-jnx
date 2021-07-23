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
  private final Listener closeListener = event -> {
    if (this.isDisposed()) {
      return;
    }
    if (this.getDisplay().getActiveShell() == this && event.stateMask == SWT.COMMAND && event.keyCode == 'w') {
      this.close();
    }
  };

  public DocumentShell(final Display display, final String serverName, final String databasePath, final String unid) {
    super(display);

    this.setText("Document " + unid + " - " + App.APP_NAME);

    this.setLayout(new GridLayout(1, false));
    this.setSize(1024, 768);

    final DocumentInfoPane docInfo = new DocumentInfoPane(this, serverName, databasePath, unid);
    docInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

    final Composite actions = new Composite(this, SWT.NONE);
    final GridData actionBarData = new GridData(SWT.RIGHT, SWT.FILL, true, false, 1, 1);
    actions.setLayoutData(actionBarData);
    actions.setLayout(new RowLayout());

    final Button cancel = new Button(actions, SWT.PUSH);
    cancel.setText("Cancel");
    final RowData cancelData = new RowData();
    cancelData.width = 100;
    cancel.setLayoutData(cancelData);
    cancel.setSize(100, cancel.getSize().y);
    cancel.addListener(SWT.Selection, evt -> this.close());

    this.layout();

    display.addFilter(SWT.KeyDown, this.closeListener);
  }

  @Override
  protected void checkSubclass() {

  }

  @Override
  public void close() {
    this.getDisplay().removeFilter(SWT.KeyDown, this.closeListener);
    super.close();
  }
}