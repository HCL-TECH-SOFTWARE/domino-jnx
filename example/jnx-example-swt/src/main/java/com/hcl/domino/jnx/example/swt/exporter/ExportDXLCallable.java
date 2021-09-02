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
package com.hcl.domino.jnx.example.swt.exporter;

import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.Callable;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.dxl.DxlExporter;
import com.hcl.domino.dxl.DxlExporter.DXLRichTextOption;

public class ExportDXLCallable implements Callable<String> {

  private final String sourceDb;
  private final String sourceUnid;
  private final boolean rawNoteFormat;

  public ExportDXLCallable(String sourceDb, String sourceUnid, boolean rawNoteFormat) {
    this.sourceDb = sourceDb;
    this.sourceUnid = sourceUnid;
    this.rawNoteFormat = rawNoteFormat;
  }

  @Override
  public String call() throws Exception {
    if(StringUtil.isNotEmpty(sourceDb)) {
      try(DominoClient client = DominoClientBuilder.newDominoClient().build()) {
        Database database = client.openDatabase(sourceDb);
        if(database == null) {
          throw new RuntimeException("Unable to open database " + sourceDb); //$NON-NLS-1$
        }
        
        DxlExporter exporter = client.createDxlExporter();
        if(rawNoteFormat) {
          exporter.setForceNoteFormat(true);
          exporter.setRichTextOption(DXLRichTextOption.ITEMDATA);
        }
        
        if(StringUtil.isNotEmpty(sourceUnid)) {
          Document document = database.getDocumentByUNID(sourceUnid).get();
          return exporter.exportDocument(document);
        } else {
          // Then export the whole DB, minus attachments
//          exporter.setOmitRichtextAttachments(true);
          exporter.setOmitMiscFileObjects(true);
          exporter.setOmitOLEObjects(true);
//          exporter.setOmitRichtextPictures(true);
          exporter.setOmitItemNames(new Vector<String>(Arrays.asList("ApprovalHistory", "ContractDrawings"))); //$NON-NLS-1$ //$NON-NLS-2$
          return exporter.exportDatabase(database);
        }
      } catch(Exception ne) {
        MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error exporting document", ne.getMessage());
      }
    }
    return ""; //$NON-NLS-1$
  }

}