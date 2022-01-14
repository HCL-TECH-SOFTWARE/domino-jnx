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
package com.hcl.domino.jnx.example.swt.dump;

import static com.hcl.domino.jnx.example.swt.util.SwtUtil.bindCheckbox;
import static com.hcl.domino.jnx.example.swt.util.SwtUtil.bindInput;
import static com.hcl.domino.jnx.example.swt.util.SwtUtil.loadData;
import static com.hcl.domino.jnx.example.swt.util.SwtUtil.loadDataBoolean;

import java.util.concurrent.ExecutionException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.hcl.domino.jnx.example.swt.App;

public class DumpDBPane extends Composite {
  private Text text;

  public DumpDBPane(Composite parent, int style) {
    super(parent, style);

    setLayout(new GridLayout(2, false));

    Label sourceDbLabel = new Label(this, SWT.NONE);
    sourceDbLabel.setText("Source Database");
    final Text sourceDbInput = new Text(this, SWT.BORDER);
    sourceDbInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    bindInput("dump.sourceDatabase", sourceDbInput); //$NON-NLS-1$

    Label sourceUnidLabel = new Label(this, SWT.NONE);
    sourceUnidLabel.setText("Destination Directory");
    final Text sourceUnidInput = new Text(this, SWT.BORDER);
    sourceUnidInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    bindInput("dump.destDir", sourceUnidInput); //$NON-NLS-1$
    
    {
      final Button rawNoteFormat = new Button(this, SWT.CHECK);
      rawNoteFormat.setText("Raw Note Format");
      bindCheckbox("dump.useRawNoteFormat", rawNoteFormat); //$NON-NLS-1$
      rawNoteFormat.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    }

    final Button export = new Button(this, SWT.PUSH);
    export.setText("Dump Notes");
    export.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));

    text = new Text(this, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
    text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

    export.addListener(SWT.Selection, e -> {
      getDisplay().asyncExec(() -> export.setEnabled(false));
      String sourceDb = loadData("dump.sourceDatabase"); //$NON-NLS-1$
      String destDir = loadData("dump.destDir"); //$NON-NLS-1$
      boolean rawNoteFormat = loadDataBoolean("dump.useRawNoteFormat"); //$NON-NLS-1$

      String dxl;
      try {
        dxl = App.getExecutor().submit(new DumpDBCallable(sourceDb, destDir, rawNoteFormat)).get();
        getDisplay().asyncExec(() -> text.setText(dxl));
      } catch (InterruptedException | ExecutionException e1) {
        e1.printStackTrace();
      } finally {
        getDisplay().asyncExec(() -> export.setEnabled(true));
      }
    });
  }

}