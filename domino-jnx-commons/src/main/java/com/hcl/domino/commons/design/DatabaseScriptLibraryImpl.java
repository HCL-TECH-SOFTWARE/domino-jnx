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
package com.hcl.domino.commons.design;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.data.NativeItemCoder;
import com.hcl.domino.design.DatabaseScriptLibrary;
import com.hcl.domino.design.NativeDesignSupport;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.Pair;

/**
 * Implementation of {@link DatabaseScriptLibrary}
 * 
 * @author Karsten Lehmann
 * @since 1.0.48
 */
public class DatabaseScriptLibraryImpl extends AbstractDesignElement<DatabaseScriptLibrary>
    implements DatabaseScriptLibrary, IDefaultNamedDesignElement {

  public DatabaseScriptLibraryImpl(Document doc) {
    super(doc);
  }

  @SuppressWarnings("nls")
  @Override
  public void initializeNewDesignNote() {
    Document doc = getDocument();
    doc.replaceItemValue(NotesConstants.DESIGNER_VERSION, "8.5.3"); //$NON-NLS-1$
    doc.replaceItemValue(NotesConstants.FIELD_PUBLICACCESS, EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), "1"); //$NON-NLS-1$

    setFlags("t34Q"); //$NON-NLS-1$
    setScript("Option Declare\n");
  }

  @Override
  public String getScript() {
    // This must be stored in $DBScript items
    return this.getDocument().allItems()
        .filter(item -> "$DBScript".equalsIgnoreCase(item.getName())) //$NON-NLS-1$
        .map(item -> item.getValue().get(0))
        .map(String::valueOf)
        .collect(Collectors.joining());

  }
  
  @Override
  public DatabaseScriptLibrary setScript(String script) {
    //format LS code to be Designer compatible
    NativeDesignSupport designSupport = NativeDesignSupport.get();
    Pair<String,String> formattedCodeAndErrors = designSupport.formatLSForDesigner(script, "NOTESUIDATABASE"); //$NON-NLS-1$
    String formattedCode = formattedCodeAndErrors.getValue1();

    Document doc = getDocument();
    Charset lmbcsCharset = NativeItemCoder.get().getLmbcsCharset();

    //remove old items with source and compiled code
    doc.removeItem("$DBScript"); //$NON-NLS-1$
    doc.removeItem("$DBScript_O"); //$NON-NLS-1$

    //split code into LMBCS 
    List<ByteBuffer> chunks = designSupport.splitAsLMBCS(formattedCode, true, false, 61310); // 61310 -> retrieved by inspecting db design
    
    //split up code across multiple $DBScript items
    chunks.forEach((chunk) -> {
      byte[] data = new byte[chunk.limit()];
      chunk.get(data);
      String chunkStr = new String(data, lmbcsCharset);

      doc.appendItemValue("$DBScript", EnumSet.of(ItemFlag.SIGNED, ItemFlag.KEEPLINEBREAKS), chunkStr); //$NON-NLS-1$
    });

    //compile and sign
    doc.compileLotusScript();
    doc.sign();
    
    return this;
  }
  
}
