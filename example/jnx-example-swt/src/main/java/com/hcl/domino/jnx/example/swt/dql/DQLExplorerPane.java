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
package com.hcl.domino.jnx.example.swt.dql;

import static com.hcl.domino.jnx.example.swt.util.SwtUtil.bindInput;
import static com.hcl.domino.jnx.example.swt.util.SwtUtil.loadData;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.hcl.domino.jnx.example.swt.App;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;
import jakarta.json.stream.JsonGenerator;

public class DQLExplorerPane extends Composite {
  private Text text;

  public DQLExplorerPane(Composite parent, int style) {
    super(parent, style);

    setLayout(new GridLayout(2, false));

    Label sourceDbLabel = new Label(this, SWT.NONE);
    sourceDbLabel.setText("Source Database");
    final Text sourceDbInput = new Text(this, SWT.BORDER);
    sourceDbInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    bindInput("dqlexplorer.sourceDatabase", sourceDbInput); //$NON-NLS-1$

    Label queryLabel = new Label(this, SWT.NONE);
    queryLabel.setText("Query");
    final Text queryInput = new Text(this, SWT.BORDER);
    queryInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    bindInput("dqlexplorer.query", queryInput); //$NON-NLS-1$

    Label sourceUnidLabel = new Label(this, SWT.NONE);
    sourceUnidLabel.setText("Extract Fields");
    final Text sourceUnidInput = new Text(this, SWT.BORDER);
    sourceUnidInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    bindInput("dqlexplorer.extractFields", sourceUnidInput); //$NON-NLS-1$

    final Button runQuery = new Button(this, SWT.PUSH);
    runQuery.setText("Run Query");
    runQuery.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));

    text = new Text(this, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
    text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

    runQuery.addListener(SWT.Selection, e -> {
      getDisplay().asyncExec(() -> runQuery.setEnabled(false));
      String sourceDb = loadData("dqlexplorer.sourceDatabase"); //$NON-NLS-1$
      String dql = loadData("dqlexplorer.query"); //$NON-NLS-1$
      String extract = loadData("dqlexplorer.extractFields"); //$NON-NLS-1$

      String json;
      try {
        json = App.getExecutor().submit(new DQLQueryCallable(sourceDb, dql, extract.split("\\,"))).get(); //$NON-NLS-1$
        
        try(
          Reader r = new StringReader(json);
          JsonReader jr = Json.createReader(r);
        ) {
          JsonObject o = jr.readObject();
          
          Map<String, Object> props = Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true);
          try(
            StringWriter w = new StringWriter();
            JsonWriter jw = Json.createWriterFactory(props).createWriter(w);
          ) {
            jw.write(o);
            
            json = w.toString();
          }
        }
        String fjson = json;
        getDisplay().asyncExec(() -> text.setText(fjson));
      } catch (InterruptedException | ExecutionException | IOException e1) {
        e1.printStackTrace();
      } finally {
        getDisplay().asyncExec(() -> runQuery.setEnabled(true));
      }
    });
  }

}