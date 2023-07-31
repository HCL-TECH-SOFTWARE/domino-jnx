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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.data.StandardFonts;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.SharedField;
import com.hcl.domino.design.format.FieldListDelimiter;
import com.hcl.domino.design.format.FieldListDisplayDelimiter;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.conversion.IRichTextConversion;
import com.hcl.domino.richtext.records.CDBegin;
import com.hcl.domino.richtext.records.CDEnd;
import com.hcl.domino.richtext.records.CDExt2Field;
import com.hcl.domino.richtext.records.CDField;
import com.hcl.domino.richtext.records.CDPabDefinition;
import com.hcl.domino.richtext.records.CDPabDefinition.Justify;
import com.hcl.domino.richtext.records.CDPabReference;
import com.hcl.domino.richtext.records.CDParagraph;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RichTextRecord;

public class SharedFieldImpl extends AbstractDesignElement<SharedField> implements SharedField, IDefaultNamedDesignElement {

  public SharedFieldImpl(Document doc) {
    super(doc);
  }

  @Override
  public void initializeNewDesignNote() {
    Document doc = getDocument();
    
    //write initial $body content
    // This is basically a default form body with a single item
    try (RichTextWriter writer = doc.createRichTextItem(NotesConstants.ITEM_NAME_TEMPLATE)) {
      
      writer.addRichTextRecord(RecordType.PARAGRAPH, (Consumer<CDParagraph>) (record) -> {
      });
      
      writer.addRichTextRecord(RecordType.PABDEFINITION, (Consumer<CDPabDefinition>) (record) -> {
        record
        .setPabId(1)
        .setJustifyMode(Justify.LEFT)
        .setLineSpacing(0)
        .setParagraphSpacingBefore(0)
        .setParagraphSpacingAfter(0)
        .setLeftMargin(1440)
        .setRightMargin(0)
        .setFirstLineLeftMargin(1440)
        .setTabStopCount(0)
        .setTabStops(new short[20])
        .setFlags(Arrays.asList(CDPabDefinition.Flag.HIDE_UNLINK))
        .setTabTypesRaw(0)
        .setFlags2(Arrays.asList(
            CDPabDefinition.Flag2.LM_OFFSET,
            CDPabDefinition.Flag2.FLLM_OFFSET,
            CDPabDefinition.Flag2.RM_PERCENT,
            CDPabDefinition.Flag2.LM_DEFAULT,
            CDPabDefinition.Flag2.FLLM_DEFAULT,
            CDPabDefinition.Flag2.RM_DEFAULT,
            CDPabDefinition.Flag2.MORE_FLAGS
            ))

        //R5 flags
        .setLeftMarginOffsetInTWIPS(0)
        .setLeftMarginOffsetInPercent(0)
        .setFirstLineLeftMarginOffsetInTwips(0)
        .setFirstLineMarginOffsetInPercent(0)
        .setRightMarginOffsetInTwips(0)
        .setRightMarginOffsetInPercent(0)
        
        //R6 flags
        .setFlags3(EnumSet.of(CDPabDefinition.Flag3.LAYER_USES_DRM));
      });
      
      writer.addRichTextRecord(RecordType.PABREFERENCE, (Consumer<CDPabReference>) (record) -> {
        record.setPabId(1);
      });

      DesignUtil.addEmptyText(writer);
      
      writer.addRichTextRecord(CDBegin.class, begin -> begin.setSignature(RichTextConstants.SIG_CD_FIELD));
      writer.addRichTextRecord(CDExt2Field.class, field -> {});
      writer.addRichTextRecord(CDField.class, field -> {
        field.setName("UntitledField");
        field.setFieldType(ItemDataType.TYPE_TEXT);
        field.setFlags(EnumSet.of(CDField.Flag.EDITABLE, CDField.Flag.STOREDV, CDField.Flag.V3FAB));
        field.setListDisplayDelimiter(FieldListDisplayDelimiter.SEMICOLON);
        field.setListDelimiters(EnumSet.of(FieldListDelimiter.SEMICOLON));
        field.getNumberFormat().setDigits((short)2);
        field.getFontStyle().setStandardFont(StandardFonts.SWISS);
        field.getFontStyle().setPointSize(10);
      });
      
      // Designer makes a record of type 62 here, which has no known mapping
      
      writer.addRichTextRecord(CDEnd.class, end -> end.setSignature(RichTextConstants.SIG_CD_FIELD));
      DesignUtil.addEmptyText(writer);
    }
    
    doc.forEachItem(NotesConstants.ITEM_NAME_TEMPLATE, (item, loop) -> {
      item.setSigned(true);
      item.setEncrypted(false);
    });

    DesignUtil.initFormBasics(doc);
  }
  
  @Override
  public void setTitle(String... title) {
    Document doc = getDocument();
    String[] oldTitle = doc.get(NotesConstants.FIELD_TITLE, String[].class, new String[0]);
    IDefaultNamedDesignElement.super.setTitle(title);
    doc.replaceItemValue(DesignConstants.ITEM_NAME_FIELDS, title);
    
    // Remove any old placeholder field and add a new one
    if(oldTitle.length > 0) {
      doc.removeItem(oldTitle[oldTitle.length-1]);
    }
    doc.replaceItemValuePlaceholder(title[title.length-1]);
    
    // Update the field name in the body
    doc.convertRichTextItem(NotesConstants.ITEM_NAME_TEMPLATE, new IRichTextConversion() {
      @Override
      public boolean isMatch(List<RichTextRecord<?>> nav) {
        return true;
      }
      @Override
      public void convert(List<RichTextRecord<?>> source, RichTextWriter target) {
        source.stream()
          .peek(record -> {
            if(record instanceof CDField) {
              ((CDField)record).setName(title[title.length-1]);
            }
          })
          .forEach(target::addRichTextRecord);
      }
    });
  }

  @Override
  public List<?> getFieldBody() {
    return DesignUtil.encapsulateRichTextBody(getDocument(), NotesConstants.ITEM_NAME_TEMPLATE);
  }

  @Override
  public String getLotusScript() {
    StringBuilder result = new StringBuilder();
    getDocument().forEachItem("$$" + getTitle(), (item, loop) -> { //$NON-NLS-1$
      result.append(item.get(String.class, "")); //$NON-NLS-1$
    });
    return result.toString();
  }
}
