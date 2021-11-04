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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import com.hcl.domino.commons.design.AbstractDesignAgentImpl;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.design.NativeDesignSupport;
import com.hcl.domino.design.agent.DesignFormulaAgent;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.records.CDActionFormula;
import com.hcl.domino.richtext.records.CDActionHeader;
import com.hcl.domino.richtext.records.CDActionJavaAgent;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.records.RecordType.Area;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;

/**
 * Implementation of {@link DesignFormulaAgent}
 */
public class DesignFormulaAgentImpl extends AbstractDesignAgentImpl<DesignFormulaAgent> implements DesignFormulaAgent {

  public DesignFormulaAgentImpl(Document doc) {
    super(doc);
  }

  @Override
  public Optional<DocumentAction> getDocumentAction() {
    Document doc = this.getDocument();
    if(doc.hasItem(NotesConstants.ASSIST_ACTION_ITEM)) {
      // Find the first CDACTIONFORMULA and read the contents
      return doc.getRichTextItem(NotesConstants.ASSIST_ACTION_ITEM, Area.TYPE_ACTION)
          .stream()
          .filter(CDActionFormula.class::isInstance)
          .map(CDActionFormula.class::cast)
          .findFirst()
          .map(action -> {
            DocumentAction docAction;
            final Set<CDActionFormula.Flag> flags = action.getFlags();
            if (flags.contains(CDActionFormula.Flag.NEWCOPY)) {
              docAction = DocumentAction.CREATE;
            } else if (flags.contains(CDActionFormula.Flag.SELECTDOCS)) {
              docAction = DocumentAction.SELECT;
            } else {
              docAction = DocumentAction.MODIFY;
            }
            return docAction;
          });
    } else {
      DocumentAction docAction;
      String action = doc.get(NotesConstants.FILTER_OPERATION_ITEM, String.class, "0"); //$NON-NLS-1$
      if(StringUtil.isEmpty(action)) {
        docAction = DocumentAction.MODIFY;
      } else {
        switch(Integer.parseInt(action)) {
          case NotesConstants.FILTER_OP_SELECT:
            docAction = DocumentAction.SELECT;
            break;
          case NotesConstants.FILTER_OP_NEW_COPY:
            docAction = DocumentAction.CREATE;
            break;
          case NotesConstants.FILTER_OP_UPDATE:
          default:
            docAction = DocumentAction.MODIFY;
            break;
        }
      }
      
      return Optional.of(docAction);
    }
  }

  @Override
  public void setDocumentAction(DocumentAction action) {
    Document doc = getDocument();
    //reset old item
    doc.removeItem(NotesConstants.FILTER_OPERATION_ITEM);
    
    withFormulaCDRecord((record) -> {
      Collection<CDActionFormula.Flag> newFlags;
      
      if (action == DocumentAction.CREATE) {
        newFlags = Arrays.asList(CDActionFormula.Flag.NEWCOPY);
      }
      else if (action == DocumentAction.SELECT) {
        newFlags = Arrays.asList(CDActionFormula.Flag.SELECTDOCS);
      }
      else if (action == DocumentAction.MODIFY) {
        newFlags = Collections.emptyList();
      }
      else {
        throw new IllegalArgumentException(MessageFormat.format("Invalid document action: {0}", action));
      }
      
      record.setFlags(newFlags);
    });
  }
  
  @Override
  public Optional<String> getFormula() {
    Document doc = this.getDocument();
    if(doc.hasItem(NotesConstants.ASSIST_ACTION_ITEM)) {
      // Find the first CDACTIONFORMULA and read the contents
      return this.getDocument().getRichTextItem(NotesConstants.ASSIST_ACTION_ITEM, Area.TYPE_ACTION)
          .stream()
          .filter(CDActionFormula.class::isInstance)
          .map(CDActionFormula.class::cast)
          .findFirst()
          .map(CDActionFormula::getFormula);
    } else {
      // Ancient agents use "$Formula" and related fields
      return Optional.ofNullable(doc.get(NotesConstants.FILTER_FORMULA_ITEM, String.class, null));
    }
  }

  /**
   * Rewrites the {@link CDActionJavaAgent} with applied changes, creating a new one if not there yet
   * 
   * @param consumer consumer of {@link CDActionJavaAgent} record
   */
  private void withFormulaCDRecord(Consumer<CDActionFormula> consumer) {
    Document doc = getDocument();
    
    List<RichTextRecord<?>> records = new ArrayList<>(doc.getRichTextItem(NotesConstants.ASSIST_ACTION_ITEM, Area.TYPE_ACTION));
    
    CDActionHeader actionHeaderRecord = records.stream()
        .filter(CDActionHeader.class::isInstance)
        .map(CDActionHeader.class::cast)
        .findFirst().orElse(null);

    if (actionHeaderRecord==null) {
      actionHeaderRecord = RichTextRecord.create(RecordType.ACTION_HEADER, 0);
      records.add(actionHeaderRecord);
    }

    CDActionFormula formulaAgentRecord = records.stream()
        .filter(CDActionFormula.class::isInstance)
        .map(CDActionFormula.class::cast)
        .findFirst().orElse(null);
    
    if (formulaAgentRecord==null) {
      formulaAgentRecord = RichTextRecord.create(RecordType.ACTION_FORMULA, 0);
      records.add(formulaAgentRecord);
    }
    
    consumer.accept(formulaAgentRecord);
    
    doc.removeItem(NotesConstants.ASSIST_ACTION_ITEM);
    try (RichTextWriter rtWriter = doc.createRichTextItem(NotesConstants.ASSIST_ACTION_ITEM)) {
      records.forEach(rtWriter::addRichTextRecord);
    }
    
    //fix wrong item type TYPE_COMPOSITE
    doc.forEachItem(NotesConstants.ASSIST_ACTION_ITEM, (item, loop) -> {
      item.setSigned(true);
      NativeDesignSupport.get().setCDRecordItemType(doc, item, ItemDataType.TYPE_ACTION);
    });
  }
  
  @Override
  public void setFormula(String formula) {
    withFormulaCDRecord((action) -> {
      action.setFormula(formula);
    });
  }
  
}
