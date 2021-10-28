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
package com.hcl.domino.jnx.example.swt.dump;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.Callable;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.dbdirectory.DirectorySearchQuery.SearchFlag;
import com.hcl.domino.dxl.DxlExporter;
import com.hcl.domino.dxl.DxlExporter.DXLRichTextOption;
import com.hcl.domino.exception.InvalidDocumentException;

public class DumpDBCallable implements Callable<String> {

  private final String sourceDb;
  private final String destDir;
  private final boolean rawNoteFormat;

  public DumpDBCallable(String sourceDb, String destDir, boolean rawNoteFormat) {
    this.sourceDb = sourceDb;
    this.destDir = destDir;
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
        
        Path dest = Paths.get(destDir);
        if(!Files.exists(dest)) {
          Files.createDirectories(dest);
        }

        Collection<Integer> ids = database.queryFormula("@True", null, EnumSet.noneOf(SearchFlag.class), null, EnumSet.allOf(DocumentClass.class)) //$NON-NLS-1$
            .getNoteIds()
            .get();
        for(int id : ids) {
          try {
            Document doc = database.getDocumentById(id).get();
            Path destFile = dest.resolve(id + ".xml"); //$NON-NLS-1$
            try(OutputStream os = Files.newOutputStream(destFile, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
              exporter.exportDocument(doc, os);
            }
          } catch(InvalidDocumentException e) {
            // Very likely to show up in ancient data
            System.err.println("Unable to export document 0x" + Integer.toHexString(id) + ": " + e.getMessage());
          }
        }
        
        return "Exported notes to " + destDir;
      } catch(Exception ne) {
        ne.printStackTrace();
        MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error exporting document", ne.getMessage());
      }
    }
    return ""; //$NON-NLS-1$
  }

}