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
import java.util.Optional;
import java.util.function.Consumer;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.StandardColors;
import com.hcl.domino.data.StandardFonts;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.design.ClassicThemeBehavior;
import com.hcl.domino.design.DesignColorsAndFonts;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.Page;
import com.hcl.domino.design.action.EventId;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.records.CDDocument;
import com.hcl.domino.richtext.records.CDEventEntry;
import com.hcl.domino.richtext.records.CDLinkColors;
import com.hcl.domino.richtext.records.CDPabDefinition;
import com.hcl.domino.richtext.records.CDPabReference;
import com.hcl.domino.richtext.records.CDParagraph;
import com.hcl.domino.richtext.records.CDText;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.CDEventEntry.ActionType;
import com.hcl.domino.richtext.records.CDEventEntry.Platform;
import com.hcl.domino.richtext.records.CDPabDefinition.Justify;
import com.hcl.domino.richtext.structures.ColorValue;

public class PageImpl extends AbstractPageElement<Page> implements Page, IDefaultAutoFrameElement {

  public PageImpl(Document doc) {
    super(doc);
  }

  @Override
  public void initializeNewDesignNote() {
    setComment(""); //$NON-NLS-1$
    Document doc = getDocument();
    doc.replaceItemValue(NotesConstants.DESIGNER_VERSION, "8.5.3"); //$NON-NLS-1$
    setFlags("C34WQ"); //$NON-NLS-1$

    //write initial $body content
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
    }
    
    doc.forEachItem(NotesConstants.ITEM_NAME_TEMPLATE, (item, loop) -> {
      item.setSigned(true);
      item.setEncrypted(false);
    });
    
    try (RichTextWriter writer = doc.createRichTextItem("$HTMLCode")) { //$NON-NLS-1$
      writer.addRichTextRecord(RecordType.EVENT_LANGUAGE_ENTRY, (Consumer<CDEventEntry>) (record) -> {
        record.setPlatform(Platform.WEB)
        .setHtmlEventId(EventId.HEADER)
        .setActionType(ActionType.JAVASCRIPT);
      });
    }
    
    doc.forEachItem("$HTMLCode", (item, loop) -> { //$NON-NLS-1$
      item.setSigned(true);
      item.setEncrypted(false);
    });

    try (RichTextWriter writer = doc.createRichTextItem("$Info")) { //$NON-NLS-1$
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
    
    doc.forEachItem("$Info", (item, loop) -> { //$NON-NLS-1$
      item.setSigned(true);
      item.setEncrypted(false);
    });

  }
  
  @Override
  public ClassicThemeBehavior getClassicThemeBehavior() {
    return getDocumentRecord()
      .flatMap(rec -> {
        short flags = rec.getFlags3Raw();
        byte themeVal = (byte)((flags & DesignConstants.TPL_FLAG_THEMESETTING) >> DesignConstants.TPL_SHIFT_THEMESETTING);
        return DominoEnumUtil.valueOf(ClassicThemeBehavior.class, themeVal);
      })
      .orElse(ClassicThemeBehavior.USE_DATABASE_SETTING);
  }
  
  @Override
  public WebRenderingSettings getWebRenderingSettings() {
    return new DefaultWebRenderingSettings();
  }
  
  @Override
  public boolean isUseInitialFocus() {
    return !getDocumentFlags3().contains(CDDocument.Flag3.NOINITIALFOCUS);
  }

  @Override
  public boolean isFocusOnF6() {
    return !getDocumentFlags3().contains(CDDocument.Flag3.NOFOCUSWHENF6);
  }
  
  // *******************************************************************************
  // * Internal implementation
  // *******************************************************************************
  
  private class DefaultWebRenderingSettings implements WebRenderingSettings {

    @Override
    public boolean isRenderRichContentOnWeb() {
      return !getWebFlags().contains(NotesConstants.WEBFLAG_NOTE_IS_HTML);
    }

    @Override
    public WebRenderingSettings setRenderRichContentOnWeb(boolean b) {
      setWebFlag(NotesConstants.WEBFLAG_NOTE_IS_HTML, b);
      return this;
    }
    
    @Override
    public Optional<String> getWebMimeType() {
      if(isRenderRichContentOnWeb()) {
        return Optional.empty();
      } else {
        return Optional.of(getDocument().getAsText(NotesConstants.ITEM_NAME_FILE_MIMETYPE, ' '));
      }
    }

    @Override
    public Optional<String> getWebCharset() {
      String charset = getDocument().getAsText(NotesConstants.ITEM_NAME_FILE_MIMECHARSET, ' ');
      return charset.isEmpty() ? Optional.empty() : Optional.of(charset);
    }

    @Override
    public ColorValue getActiveLinkColor() {
      return getHtmlCodeItem()
        .stream()
        .filter(CDLinkColors.class::isInstance)
        .map(CDLinkColors.class::cast)
        .findFirst()
        .map(CDLinkColors::getActiveColor)
        .orElseGet(DesignColorsAndFonts::defaultActiveLink);
    }

    @Override
    public ColorValue getUnvisitedLinkColor() {
      return getHtmlCodeItem()
        .stream()
        .filter(CDLinkColors.class::isInstance)
        .map(CDLinkColors.class::cast)
        .findFirst()
        .map(CDLinkColors::getUnvisitedColor)
        .orElseGet(DesignColorsAndFonts::defaultUnvisitedLink);
    }

    @Override
    public ColorValue getVisitedLinkColor() {
      return getHtmlCodeItem()
        .stream()
        .filter(CDLinkColors.class::isInstance)
        .map(CDLinkColors.class::cast)
        .findFirst()
        .map(CDLinkColors::getVisitedColor)
        .orElseGet(DesignColorsAndFonts::defaultVisitedLink);
    }
  }
  
  @Override
  public void setTitle(String... title) {
    Document doc = getDocument();
    doc.replaceItemValue(NotesConstants.FIELD_TITLE, title);
    if (doc.isNew()) {
      doc.replaceItemValue(NotesConstants.DOC_SCRIPT_NAME, EnumSet.of(ItemFlag.SIGNED), title);
    }
  }

}
