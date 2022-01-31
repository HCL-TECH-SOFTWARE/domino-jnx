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
import com.hcl.domino.design.LotusScriptLibrary;
import com.hcl.domino.design.NativeDesignSupport;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.Pair;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public class LotusScriptLibraryImpl extends AbstractScriptLibrary<LotusScriptLibrary> implements LotusScriptLibrary {

  public LotusScriptLibraryImpl(final Document doc) {
    super(doc);
  }

  @Override
  public String getScript() {
    // This must be stored in $ScriptLib items
    return this.getDocument().allItems()
        .filter(item -> NotesConstants.SCRIPTLIB_ITEM_NAME.equalsIgnoreCase(item.getName()))
        .map(item -> item.getValue().get(0))
        .map(String::valueOf)
        .collect(Collectors.joining());
  }
  
  @Override
  public LotusScriptLibrary setScript(String script) {
    //format LS code to be Designer compatible
    NativeDesignSupport designSupport = NativeDesignSupport.get();
    Pair<String,String> formattedCodeAndErrors = designSupport.formatLSForDesigner(script, ""); //$NON-NLS-1$
    String formattedCode = formattedCodeAndErrors.getValue1();

    Document doc = getDocument();
    Charset lmbcsCharset = NativeItemCoder.get().getLmbcsCharset(); //$NON-NLS-1$

    //remove old items with source and compiled code
    doc.removeItem(NotesConstants.SCRIPTLIB_ITEM_NAME);
    doc.removeItem(NotesConstants.SCRIPTLIB_OBJECT);

    //split code into LMBCS 
    List<ByteBuffer> chunks = designSupport.splitAsLMBCS(formattedCode, true, false, 61310); // 61310 -> retrieved by inspecting db design
    
    //split up code across multiple $ScriptLib items
    chunks.forEach((chunk) -> {
      byte[] data = new byte[chunk.limit()];
      chunk.get(data);
      String chunkStr = new String(data, lmbcsCharset);

      doc.appendItemValue(NotesConstants.SCRIPTLIB_ITEM_NAME, EnumSet.of(ItemFlag.SIGNED, ItemFlag.KEEPLINEBREAKS), chunkStr);
    });

    //compile and sign
    doc.compileLotusScript();
    doc.sign();
    
    return this;
  }
}
