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
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.data.StandardColors;
import com.hcl.domino.data.StandardFonts;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.SharedField;
import com.hcl.domino.design.action.EventId;
import com.hcl.domino.design.format.FieldListDisplayDelimiter;
import com.hcl.domino.design.format.TimeZoneFormat;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.conversion.IRichTextConversion;
import com.hcl.domino.richtext.records.CDBegin;
import com.hcl.domino.richtext.records.CDDocument;
import com.hcl.domino.richtext.records.CDEnd;
import com.hcl.domino.richtext.records.CDEventEntry;
import com.hcl.domino.richtext.records.CDEventEntry.ActionType;
import com.hcl.domino.richtext.records.CDEventEntry.Platform;
import com.hcl.domino.richtext.records.CDExt2Field;
import com.hcl.domino.richtext.records.CDField;
import com.hcl.domino.richtext.records.CDField.Flag;
import com.hcl.domino.richtext.records.CDPabDefinition;
import com.hcl.domino.richtext.records.CDPabDefinition.Justify;
import com.hcl.domino.richtext.records.CDPabReference;
import com.hcl.domino.richtext.records.CDParagraph;
import com.hcl.domino.richtext.records.CDText;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.NFMT.Format;
import com.hcl.domino.richtext.structures.TFMT.DateFormat;
import com.hcl.domino.richtext.structures.TFMT.TimeFormat;
import com.hcl.domino.richtext.structures.TFMT.TimeStructure;

public class SharedFieldImpl extends AbstractDesignElement<SharedField> implements SharedField, IDefaultNamedDesignElement {
  //reserved internal item names for which we do not create placeholder items to not overwrite stuff
  //(Designer overwrites items btw, which is bad)
  private static final Set<String> reservedItemNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
  static {
    reservedItemNames.addAll(
        Arrays.asList(
            "$body", //$NON-NLS-1$
            "$comment", //$NON-NLS-1$
            "$designerversion", //$NON-NLS-1$
            "$fields", //$NON-NLS-1$
            "$flags", //$NON-NLS-1$
            "$htmlcode", //$NON-NLS-1$
            "$info", //$NON-NLS-1$
            "$title", //$NON-NLS-1$
            "$updatedby" //$NON-NLS-1$
            )
        );
  }

  public SharedFieldImpl(Document doc) {
    super(doc);
  }

  @Override
  public void initializeNewDesignNote() {
    Document doc = getDocument();
    setFlag(getLotusScript(), isAllowPublicAccess());
    doc.replaceItemValue(NotesConstants.DESIGN_FLAGS, ""); //$NON-NLS-1$
    
    doc.replaceItemValue(DesignConstants.DESIGNER_VERSION, "8.5.3"); //$NON-NLS-1$
    
    doc.replaceItemValue(NotesConstants.VIEW_COMMENT_ITEM,
        EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), ""); //$NON-NLS-1$
    doc.replaceItemValue(DesignConstants.VIEW_INDEX_ITEM,
        EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), ""); //$NON-NLS-1$
    doc.replaceItemValue(DesignConstants.VIEW_CLASSES_ITEM, EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), "1");
    

    //write initial $HTMLCode
    try (RichTextWriter writer = doc.createRichTextItem(DesignConstants.ITEM_NAME_HTMLCODE)) {
      writer.addRichTextRecord(RecordType.EVENT_LANGUAGE_ENTRY, (Consumer<CDEventEntry>) (record) -> {
        record.setPlatform(Platform.WEB)
        .setHtmlEventId(EventId.HEADER)
        .setActionType(ActionType.JAVASCRIPT);
      });
    }
    
    doc.forEachItem(DesignConstants.ITEM_NAME_HTMLCODE, (item, loop) -> {
      item.setSigned(true);
      item.setEncrypted(false);
    });

    
    //write initial $Info
    try (RichTextWriter writer = doc.createRichTextItem(NotesConstants.ITEM_NAME_DOCUMENT)) {
      writer.addRichTextRecord(RecordType.DOCUMENT, (Consumer<CDDocument>) (record) -> {
        record.setPaperColor(StandardColors.White)
        .setFlags(EnumSet.of(CDDocument.Flag.SPARESOK))
        .setFlags2(EnumSet.of(CDDocument.Flag2.UPDATE_SIBLING))
        .setPaperColorRaw(1)
        .setFlags3(EnumSet.of(CDDocument.Flag3.RENDERPASSTHROUGH));
        
        record
        .getPaperColorValue()
        .setFlags(EnumSet.of(ColorValue.Flag.ISRGB))
        .setRed((short) (255 & 0xffff))
        .setGreen((short) (255 & 0xffff))
        .setBlue((short) (255 & 0xffff))
        .setComponent4((short) 0);
        
      });
    }
    
    doc.forEachItem(NotesConstants.ITEM_NAME_DOCUMENT, (item, loop) -> { //$NON-NLS-1$
      item.setSigned(true);
      item.setEncrypted(false);
    });

  }

  private void writeFieldBody(String fieldName) {
    Document doc = getDocument();

    String oldFieldName = getTitle();
    
    if (doc.hasItem(NotesConstants.ITEM_NAME_TEMPLATE)) {
      //rename existing field
      doc.convertRichTextItem(NotesConstants.ITEM_NAME_TEMPLATE, new IRichTextConversion() {
        
        @Override
        public boolean isMatch(List<RichTextRecord<?>> nav) {
          return true;
        }
        
        @Override
        public void convert(List<RichTextRecord<?>> source, RichTextWriter target) {
          source.forEach((record) -> {
            if (record instanceof CDField) {
              CDField fieldRecord = (CDField) record;
              fieldRecord.setName(fieldName);
            }
            target.addRichTextRecord(record);
          });
        }
      });
    }
    else {
      //write initial $Body content
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
        
        writer.addRichTextRecord(RecordType.TEXT, (Consumer<CDText>) (record) -> {
          record.getStyle()
          .setEmboss(false)
          .setSub(false)
          .setColor(StandardColors.Black)
          .setShadow(false)
          .setSuper(false)
          .setExtrude(false)
          .setUnderline(false)
          .setStandardFont(StandardFonts.SWISS)
          .setItalic(false)
          .setBold(false)
          .setStrikeout(false)
          .setPointSize(10);
        });
        
        writer.addRichTextRecord(RecordType.BEGIN, (Consumer<CDBegin>) (record) -> {
          record.setVersion(0);
        });

        writer.addRichTextRecord(RecordType.EXT2_FIELD, (Consumer<CDExt2Field>) (record) -> {
          record.setThumbnailImageHeight(120);
          record.setThumbnailImageWidth(120);
        });
        
        writer.addRichTextRecord(RecordType.FIELD, (Consumer<CDField>) (record) -> {
          record.setName(fieldName);
          record.getNumberFormat()
          .setFormat(Format.GENERAL)
          .setDigits((short) 2);
          record.setFieldType(ItemDataType.TYPE_TEXT);
          record.getTimeFormat()
          .setZoneFormat(TimeZoneFormat.NEVER)
          .setTimeStructure(TimeStructure.DATETIME)
          .setTimeFormat(TimeFormat.FULL)
          .setDateFormat(DateFormat.FULL);
          record.setFlags(Arrays.asList(Flag.EDITABLE, Flag.V3FAB, Flag.KEYWORDS_UI_STANDARD));
          record.getFontStyle()
          .setPointSize(10)
          .setColor(StandardColors.Black)
          .setStandardFont(StandardFonts.SWISS);
          record.setListDisplayDelimiter(FieldListDisplayDelimiter.SEMICOLON);
//          record.setListDelimiters(null)
        });
        
        
        
        writer.addRichTextRecord(RecordType.END, (Consumer<CDEnd>) (record) -> {
          record.setVersion(0);
        });
      }
      
    }
    
    doc.forEachItem(NotesConstants.ITEM_NAME_TEMPLATE, (item, loop) -> {
      item.setSigned(true);
      item.setEncrypted(false);
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
  
  @Override
  public boolean save() {
    Document doc = getDocument();
    String fieldName = doc.get(NotesConstants.FIELD_TITLE, String.class, "");
    doc.replaceItemValue(DesignConstants.ITEM_NAME_FIELDS, fieldName);
    
    return super.save();
  }
 
  @Override
  public String getTitle() {
    //shared fields only have one name
    return this.getDocument().get(NotesConstants.FIELD_TITLE, String.class, ""); //$NON-NLS-1$
  }

  @Override
  public void setTitle(String... title) {
    Document doc = getDocument();
    
    String oldTitle = getTitle();
    
    if (!reservedItemNames.contains(oldTitle)) {
      //remove old placeholder
      doc.removeItem(oldTitle);
    }
    
    if (!reservedItemNames.contains(title[0])) {
      //create a the placeholder value for the field name
      ByteBuffer placeholderDataBuf = ByteBuffer.allocate(2).order(ByteOrder.nativeOrder());
      placeholderDataBuf.putShort(0, ItemDataType.TYPE_INVALID_OR_UNKNOWN.getValue());
      
      doc.replaceItemValue(title[0],
          EnumSet.of(ItemFlag.PLACEHOLDER), placeholderDataBuf);
    }
    
    //ignore alias values, shared fields only have one name
    doc.replaceItemValue(NotesConstants.FIELD_TITLE, title[0]);
  }
  
}
