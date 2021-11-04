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
package com.hcl.domino.commons.design.agent;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import com.hcl.domino.commons.design.AbstractDesignAgentImpl;
import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.design.NativeDesignSupport;
import com.hcl.domino.design.agent.DesignLotusScriptAgent;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.Pair;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.records.CDActionHeader;
import com.hcl.domino.richtext.records.CDActionLotusScript;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RecordType.Area;

public class DesignLotusScriptAgentImpl extends AbstractDesignAgentImpl<DesignLotusScriptAgent> implements DesignLotusScriptAgent {
  private String script;
  
  public DesignLotusScriptAgentImpl(Document doc) {
    super(doc);
    
    // Could be represented two ways: either as a CDACTIONLOTUSSCRIPT or as multiple
    // $AgentHScript text items
    final CDActionLotusScript action = this.getDocument().getRichTextItem(NotesConstants.ASSIST_ACTION_ITEM, Area.TYPE_ACTION)
        .stream()
        .filter(CDActionLotusScript.class::isInstance)
        .map(CDActionLotusScript.class::cast)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Unable to find LotusScript action data"));
    
    if (action.getScriptLength() == 0) {
      // This must be stored in $AgentHScript items
      this.script = doc.allItems()
          .filter(item -> NotesConstants.AGENT_HSCRIPT_ITEM.equalsIgnoreCase(item.getName()))
          .map(item -> item.getValue().get(0))
          .map(String::valueOf)
          .collect(Collectors.joining());
    } else {
      this.script = action.getScript();
    }
  }

  @Override
  public String getScript() {
    return script;
  }
  
  @Override
  public void setScript(String script) {
    this.setFlag(NotesConstants.DESIGN_FLAG_HIDE_FROM_V3, true);
    this.setFlag(NotesConstants.DESIGN_FLAG_V4AGENT, true);
    this.setFlag(NotesConstants.DESIGN_FLAG_LOTUSSCRIPT_AGENT, true);

    this.setFlag(NotesConstants.DESIGN_FLAG_JAVA_AGENT_WITH_SOURCE, false);
    this.setFlag(NotesConstants.DESIGN_FLAG_JAVA_AGENT, false);

    //format LS code to be Designer compatible
    NativeDesignSupport designSupport = NativeDesignSupport.get();
    Pair<String,String> formattedCodeAndErrors = designSupport.formatLSForDesigner(script, ""); //$NON-NLS-1$
    String formattedCode = formattedCodeAndErrors.getValue1();

    Document doc = getDocument();
    Charset lmbcsCharset = Charset.forName("LMBCS"); //$NON-NLS-1$

    //remove old items with source and compiled code
    doc.removeItem(NotesConstants.ASSIST_ACTION_ITEM);
    doc.removeItem(NotesConstants.AGENT_HSCRIPT_ITEM);
    doc.removeItem(NotesConstants.ASSIST_EXACTION_ITEM);

    //split code into LMBCS 
    List<ByteBuffer> chunks = designSupport.splitAsLMBCS(formattedCode, true, false, 61310); // 61310 -> retrieved by inspecting db design
    if (chunks.size() == 1) {
      //code fits into a single item $AssistAction stored as CDACTIONLOTUSSCRIPT record
      ByteBuffer chunk = chunks.get(0);
      byte[] data = new byte[chunk.limit()];
      chunk.get(data);
      String chunkStr = new String(data, lmbcsCharset);

      try (RichTextWriter rtWriter = doc.createRichTextItem(NotesConstants.ASSIST_ACTION_ITEM);) {
        rtWriter.addRichTextRecord(CDActionHeader.class, (record) -> {
          record.getHeader().setSignature((byte) (RecordType.ACTION_HEADER.getConstant() & 0xff));
          record.getHeader().setLength((short) (MemoryStructureUtil.sizeOf(CDActionHeader.class) & 0xffff));
        });

        rtWriter.addRichTextRecord(CDActionLotusScript.class, (record) -> {
          record.getHeader().setSignature(RecordType.ACTION_LOTUSSCRIPT.getConstant());
          record.getHeader().setLength(MemoryStructureUtil.sizeOf(CDActionLotusScript.class));
          record.setScript(chunkStr);
        });
      }
    }
    else {
      //split up code across multiple $AgentHScript items
      chunks.forEach((chunk) -> {
        byte[] data = new byte[chunk.limit()];
        chunk.get(data);
        String chunkStr = new String(data, lmbcsCharset);

        doc.appendItemValue(NotesConstants.AGENT_HSCRIPT_ITEM, EnumSet.of(ItemFlag.SIGNED, ItemFlag.KEEPLINEBREAKS), chunkStr);
      });

      try (RichTextWriter rtWriter = doc.createRichTextItem(NotesConstants.ASSIST_ACTION_ITEM);) {
        rtWriter.addRichTextRecord(CDActionHeader.class, (record) -> {
          record.getHeader().setSignature((byte) (RecordType.ACTION_HEADER.getConstant() & 0xff));
          record.getHeader().setLength((short) (MemoryStructureUtil.sizeOf(CDActionHeader.class) & 0xffff));
        });

        rtWriter.addRichTextRecord(CDActionLotusScript.class, (record) -> {
          record.getHeader().setSignature(RecordType.ACTION_LOTUSSCRIPT.getConstant());
          record.getHeader().setLength(MemoryStructureUtil.sizeOf(CDActionLotusScript.class));
          record.setScriptLength(0);
        });
      }
    }

    //switch action items from TYPE_COMPOSITE to TYPE_ACTION
    doc.forEachItem(NotesConstants.ASSIST_ACTION_ITEM, (item,loop) -> {
      item.setSigned(true);
      designSupport.setCDRecordItemType(doc, item, ItemDataType.TYPE_ACTION);
    });

    //compile and sign
    doc.compileLotusScript();
    doc.sign();
  }

}
