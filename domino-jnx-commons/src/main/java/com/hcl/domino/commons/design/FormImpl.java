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
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.hcl.domino.commons.richtext.DefaultNotesBitmap;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.StandardColors;
import com.hcl.domino.data.StandardFonts;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.design.ClassicThemeBehavior;
import com.hcl.domino.design.DesignColorsAndFonts;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.Form;
import com.hcl.domino.design.ImageRepeatMode;
import com.hcl.domino.design.action.EventId;
import com.hcl.domino.design.forms.AutoLaunchHideWhen;
import com.hcl.domino.design.forms.AutoLaunchType;
import com.hcl.domino.design.forms.AutoLaunchWhen;
import com.hcl.domino.design.frameset.FrameScrollStyle;
import com.hcl.domino.design.frameset.FrameSizingType;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.NotesBitmap;
import com.hcl.domino.richtext.RichTextRecordList;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.records.CDDECSField;
import com.hcl.domino.richtext.records.CDDocAutoLaunch;
import com.hcl.domino.richtext.records.CDDocument;
import com.hcl.domino.richtext.records.CDEventEntry;
import com.hcl.domino.richtext.records.CDEventEntry.ActionType;
import com.hcl.domino.richtext.records.CDEventEntry.Platform;
import com.hcl.domino.richtext.records.CDField;
import com.hcl.domino.richtext.records.CDFrame;
import com.hcl.domino.richtext.records.CDGraphic;
import com.hcl.domino.richtext.records.CDHeader;
import com.hcl.domino.richtext.records.CDLinkColors;
import com.hcl.domino.richtext.records.CDPabDefinition;
import com.hcl.domino.richtext.records.CDPabDefinition.Justify;
import com.hcl.domino.richtext.records.CDPabReference;
import com.hcl.domino.richtext.records.CDParagraph;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.records.CDText;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RecordType.Area;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.FramesetLength;

public class FormImpl extends AbstractFormOrSubform<Form> implements Form, IDefaultAutoFrameElement, IDefaultReadersRestrictedElement {

  public FormImpl(final Document doc) {
    super(doc);
  }

  @Override
  public Optional<String> getNotesXPageAlternative() {
    final String val = this.getDocument().get(DesignConstants.XPAGE_ALTERNATE_CLIENT, String.class, null);
    if (val == null || val.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(val);
    }
  }

  @Override
  public Optional<String> getWebXPageAlternative() {
    final String val = this.getDocument().get(DesignConstants.XPAGE_ALTERNATE, String.class, null);
    if (val == null || val.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(val);
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
  
  @Override
  public void initializeNewDesignNote() {
    setComment(""); //$NON-NLS-1$
    Document doc = getDocument();
    doc.replaceItemValue(NotesConstants.DESIGNER_VERSION, "8.5.3"); //$NON-NLS-1$
    setFlags(""); //$NON-NLS-1$
    
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
  public Type getType() {
    return getDocumentRecord()
      .map(CDDocument::getFlags)
      .map(flags -> {
        return flags.contains(CDDocument.Flag.NOTEREF_MAIN) ? Type.RESPONSE
            : flags.contains(CDDocument.Flag.NOTEREF) ? Type.RESPONSE_TO_RESPONSE
            : Type.DOCUMENT;
      })
      .orElse(Type.DOCUMENT);
  }

  @Override
  public MenuInclusion getMenuInclusionMode() {
    String flags = getFlags();
    if(flags.contains(NotesConstants.DESIGN_FLAG_OTHER_DLG)) {
      return MenuInclusion.CREATE_OTHER;
    } else if(flags.contains(NotesConstants.DESIGN_FLAG_NO_COMPOSE)) {
      return MenuInclusion.NONE;
    } else {
      return MenuInclusion.CREATE;
    }
  }

  @Override
  public boolean isIncludeInSearchBuilder() {
    return !getFlags().contains(NotesConstants.DESIGN_FLAG_NO_QUERY);
  }

  @Override
  public boolean isIncludeInPrint() {
    return getFlags().contains(NotesConstants.DESIGN_FLAG_PRINTFORM);
  }

  @Override
  public VersioningBehavior getVersioningBehavior() {
    return getDocumentRecord()
      .map(rec -> {
        Set<CDDocument.Flag> flags = rec.getFlags();
        Set<CDDocument.Flag2> flags2 = getDocumentFlags2();
        if(flags2.contains(CDDocument.Flag2.UPDATE_SIBLING)) {
          return VersioningBehavior.NEW_AS_SIBLINGS;
        } else if(flags.contains(CDDocument.Flag.UPDATE_RESPONSE)) {
          return VersioningBehavior.NEW_AS_RESPONSES;
        } else if(flags.contains(CDDocument.Flag.UPDATE_PARENT)) {
          return VersioningBehavior.PRIOR_AS_RESPONSES;
        } else {
          return VersioningBehavior.NONE;
        }
      })
      .orElse(VersioningBehavior.NONE);
  }

  @Override
  public boolean isVersionCreationAutomatic() {
    return !getDocumentFlags2().contains(CDDocument.Flag2.MANVCREATE);
  }

  @Override
  public boolean isDefaultForm() {
    return getDocument().getDocumentClass().contains(DocumentClass.DEFAULT);
  }

  @Override
  public boolean isStoreFormInDocument() {
    return getDocumentFlags().contains(CDDocument.Flag.BOILERPLATE);
  }

  @Override
  public boolean isAllowFieldExchange() {
    return !getDocumentFlags2().contains(CDDocument.Flag2.DISABLE_FX);
  }

  @Override
  public boolean isAutomaticallyRefreshFields() {
    return getDocumentFlags().contains(CDDocument.Flag.RECALC);
  }

  @Override
  public boolean isAnonymousForm() {
    return getDocumentFlags2().contains(CDDocument.Flag2.ANONYMOUS);
  }

  @Override
  public boolean isUseInitialFocus() {
    return !getDocumentFlags3().contains(CDDocument.Flag3.NOINITIALFOCUS);
  }

  @Override
  public boolean isFocusOnF6() {
    return !getDocumentFlags3().contains(CDDocument.Flag3.NOFOCUSWHENF6);
  }

  @Override
  public boolean isSignDocuments() {
    return getDocumentFlags3().contains(CDDocument.Flag3.SIGNWHENSAVED);
  }

  @Override
  public boolean isAllowAutosave() {
    return getDocumentFlags3().contains(CDDocument.Flag3.CANAUTOSAVE);
  }

  @Override
  public ConflictBehavior getConflictBehavior() {
    String action = getDocument().getAsText(NotesConstants.ITEM_CONFLICT_ACTION, ' ');
    switch(action) {
    case NotesConstants.CONFLICT_AUTOMERGE:
      return ConflictBehavior.MERGE_CONFLICTS;
    case NotesConstants.CONFLICT_NONE:
      return ConflictBehavior.MERGE_NO_CONFLICTS;
    case NotesConstants.CONFLICT_BEST_MERGE:
      return ConflictBehavior.MERGE_NO_CONFLICTS;
    default:
      return ConflictBehavior.CREATE_CONFLICTS;
    }
  }

  @Override
  public boolean isInheritSelectedDocumentValues() {
    return getDocumentFlags().contains(CDDocument.Flag.REFERENCE);
  }

  @Override
  public Optional<InheritanceSettings> getSelectedDocumentInheritanceBehavior() {
    Set<CDDocument.Flag2> flags = getDocumentFlags2();
    if(flags.contains(CDDocument.Flag2.INCLUDEREF)) {
      return Optional.of(new DefaultInheritanceSettings());
    } else {
      return Optional.empty();
    }
  }

  @Override
  public boolean isAutomaticallyEnableEditMode() {
    return getDocumentFlags2().contains(CDDocument.Flag2.EDITONOPEN);
  }

  @Override
  public ContextPaneBehavior getContextPaneBehavior() {
    Set<CDDocument.Flag2> flags = getDocumentFlags2();
    if(!flags.contains(CDDocument.Flag2.OPENCNTXT)) {
      return ContextPaneBehavior.NONE;
    } else if(flags.contains(CDDocument.Flag2.CNTXTPARENT)) {
      return ContextPaneBehavior.PARENT;
    } else {
      return ContextPaneBehavior.DOCLINK;
    }
  }

  @Override
  public boolean isShowMailDialogOnClose() {
    return getDocumentFlags().contains(CDDocument.Flag.MAIL);
  }

  @Override
  public WebRenderingSettings getWebRenderingSettings() {
    return new DefaultWebRenderingSettings();
  }

  @Override
  public Optional<String> getDefaultDataConnectionName() {
    return getDecsField().map(CDDECSField::getDcrName);
  }

  @Override
  public Optional<String> getDefaultDataConnectionObject() {
    return getDecsField().map(CDDECSField::getMetadataName);
  }
  
  @Override
  public AutoLaunchSettings getAutoLaunchSettings() {
    return new DefaultAutoLaunchSettings();
  }
  
  @Override
  public BackgroundSettings getBackgroundSettings() {
    return new DefaultBackgroundSettings();
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
  public HeaderFrameSettings getHeaderFrameSettings() {
    return new DefaultHeaderFrameSettings();
  }
  
  @Override
  public PrintSettings getPrintSettings() {
    return new DefaultPrintSettings();
  }
  
  @Override
  public Optional<String> getWebQueryOpenFormula() {
    Document doc = getDocument();
    if(doc.hasItem(DesignConstants.ITEM_NAME_WEBQUERYOPEN)) {
      return Optional.of(doc.get(DesignConstants.ITEM_NAME_WEBQUERYOPEN, String.class, "")); //$NON-NLS-1$
    } else {
      return Optional.empty();
    }
  }
  
  @Override
  public Optional<String> getWebQuerySaveFormula() {
    Document doc = getDocument();
    if(doc.hasItem(DesignConstants.ITEM_NAME_WEBQUERYSAVE)) {
      return Optional.of(doc.get(DesignConstants.ITEM_NAME_WEBQUERYSAVE, String.class, "")); //$NON-NLS-1$
    } else {
      return Optional.empty();
    }
  }
  
  // *******************************************************************************
  // * Internal implementation utilities
  // *******************************************************************************
  
  private Optional<CDDECSField> getDecsField() {
    Document doc = getDocument();
    if(doc.hasItem(DesignConstants.ITEM_NAME_DEFAULTDECSINFO)) {
      return doc.getRichTextItem(DesignConstants.ITEM_NAME_DEFAULTDECSINFO)
        .stream()
        .filter(CDDECSField.class::isInstance)
        .map(CDDECSField.class::cast)
        .findFirst();
    } else {
      return Optional.empty();
    }
  }
  
  private Optional<CDDocAutoLaunch> getAutoLaunchRecord() {
    Document doc = getDocument();
    if(doc.hasItem(DesignConstants.FORM_AUTOLAUNCH_ITEM)) {
      return doc.getRichTextItem(DesignConstants.FORM_AUTOLAUNCH_ITEM, Area.RESERVED_INTERNAL)
        .stream()
        .filter(CDDocAutoLaunch.class::isInstance)
        .map(CDDocAutoLaunch.class::cast)
        .findFirst();
    } else {
      return Optional.empty();
    }
  }
  
  private Optional<DominoFramesetFormat> getRegionFrameset() {
    Document doc = getDocument();
    if(doc.hasItem(DesignConstants.ITEM_NAME_REGIONFRAMESET)) {
      return Optional.of(new DominoFramesetFormat(doc.getRichTextItem(DesignConstants.ITEM_NAME_REGIONFRAMESET, Area.FRAMESETS)));
    } else {
      return Optional.empty();
    }
  }
  
  private class DefaultInheritanceSettings implements InheritanceSettings {
    
    @Override
    public String getTargetField() {
      // NB: the field name is not always actually stored in the CDDOCUMENT
      //   record. In this case, we'll have to see if inheritance is on at all
      //   and, if so, find the first field in the Body
      return getDocumentRecord()
        .map(CDDocument::getInheritanceFieldName)
        .flatMap(name -> {
          if(name.isEmpty()) {
            // Look up the first field in the body
            return getFormBodyItem()
              .stream()
              .filter(CDField.class::isInstance)
              .map(CDField.class::cast)
              .map(CDField::getName)
              .findFirst();
          } else {
            return Optional.of(name);
          }
        })
        .orElse(""); //$NON-NLS-1$
    }

    @Override
    public InheritanceFieldType getType() {
      Set<CDDocument.Flag2> flags = getDocumentFlags2();
      if(flags.contains(CDDocument.Flag2.RENDCOLLAPSE)) {
        return InheritanceFieldType.COLLAPSIBLE_RICH_TEXT;
      } else if(flags.contains(CDDocument.Flag2.RENDERREF)) {
        return InheritanceFieldType.RICH_TEXT;
      } else {
        return InheritanceFieldType.LINK;
      }
    }
  }
  
  private class DefaultWebRenderingSettings implements WebRenderingSettings {

    @Override
    public boolean isRenderRichContentOnWeb() {
      return !getWebFlags().contains(NotesConstants.WEBFLAG_NOTE_IS_HTML);
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
    public boolean isGenerateHtmlForAllFields() {
      return getWebFlags().contains(NotesConstants.WEBFLAG_NOTE_HTML_ALL_FLDS);
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
  
  private class DefaultAutoLaunchSettings implements AutoLaunchSettings {

    @Override
    public AutoLaunchType getType() {
      return getAutoLaunchRecord()
        .flatMap(CDDocAutoLaunch::getObjectType)
        .orElse(AutoLaunchType.NONE);
    }

    @Override
    public Optional<String> getOleType() {
      return getAutoLaunchRecord()
        .flatMap(rec -> {
          if(rec.getObjectType().orElse(AutoLaunchType.NONE) == AutoLaunchType.OLE_CLASS) {
            return Optional.of(rec.getOleObjClass().toGuidString());
          } else {
            return Optional.empty();
          }
        });
    }

    @Override
    public boolean isLaunchInPlace() {
      return getAutoLaunchRecord()
        .map(CDDocAutoLaunch::getOleFlags)
        .map(flags -> flags.contains(CDDocAutoLaunch.OleFlag.EDIT_INPLACE))
        .orElse(false);
    }

    @Override
    public boolean isPresentDocumentAsModal() {
      return getAutoLaunchRecord()
        .map(CDDocAutoLaunch::getOleFlags)
        .map(flags -> flags.contains(CDDocAutoLaunch.OleFlag.MODAL_WINDOW))
        .orElse(false);
    }

    @Override
    public boolean isCreateObjectInFirstRichTextField() {
      return getAutoLaunchRecord()
        .map(CDDocAutoLaunch::getCopyToFieldFlags)
        .map(flags -> flags.contains(CDDocAutoLaunch.CopyToFieldFlag.COPY_FIRST))
        .orElse(false);
    }

    @Override
    public Optional<String> getTargetRichTextField() {
      return getAutoLaunchRecord()
        .flatMap(rec -> {
          if(rec.getCopyToFieldFlags().contains(CDDocAutoLaunch.CopyToFieldFlag.COPY_NAMED)) {
            return Optional.of(rec.getFieldName());
          } else {
            return Optional.empty();
          }
        });
    }

    @Override
    public Set<AutoLaunchWhen> getLaunchWhen() {
      return getAutoLaunchRecord()
        .map(CDDocAutoLaunch::getLaunchWhenFlags)
        .orElseGet(Collections::emptySet);
    }

    @Override
    public Set<AutoLaunchHideWhen> getHideWhen() {
      return getAutoLaunchRecord()
        .map(CDDocAutoLaunch::getHideWhenFlags)
        .orElseGet(Collections::emptySet);
    }
  }
  
  private class DefaultBackgroundSettings implements BackgroundSettings {

    @Override
    public Optional<StandardColors> getStandardBackgroundColor() {
      // This could be stored in either in PaperColor (pre-V4) or PaperColorExt.
      //   Check the flags to know
      return getDocumentRecord()
        .flatMap(doc -> {
          if(doc.getFlags().contains(CDDocument.Flag.SPARESOK)) {
            // Indicates V4+ friendly
            return doc.getPaperColor();
          } else {
            // Ancient data - translate pre-V4 color
            return doc.getPreV4PaperColor()
              .map(DesignColorsAndFonts::toStandardColor);
          }
        });
    }

    @Override
    public ColorValue getBackgroundColor() {
      return getDocumentRecord()
        .map(CDDocument::getPaperColorValue)
        .orElseGet(DesignColorsAndFonts::whiteColor);
    }

    @Override
    public Optional<CDResource> getBackgroundImageResource() {
      Document doc = getDocument();
      if(doc.hasItem(DesignConstants.ITEM_NAME_BACKGROUNDGRAPHICR5)) {
        return doc.getRichTextItem(DesignConstants.ITEM_NAME_BACKGROUNDGRAPHICR5)
          .stream()
          .filter(CDResource.class::isInstance)
          .map(CDResource.class::cast)
          .findFirst();
      } else {
        return Optional.empty();
      }
    }

    @Override
    public Optional<NotesBitmap> getBackgroundImage() {
      Document doc = getDocument();
      if(doc.hasItem(DesignConstants.ITEM_NAME_BACKGROUNDGRAPHICR5)) {
        RichTextRecordList item = doc.getRichTextItem(DesignConstants.ITEM_NAME_BACKGROUNDGRAPHICR5);
        if(!item.isEmpty() && item.get(0) instanceof CDGraphic) {
          return Optional.of(new DefaultNotesBitmap(item));
        } else {
          return Optional.empty();
        }
      } else {
        return Optional.empty();
      }
    }

    @Override
    public boolean isHideGraphicInDesignMode() {
      return getDocumentRecord()
        .map(CDDocument::getFlags2)
        .map(flags -> flags.contains(CDDocument.Flag2.HIDEBKGRAPHIC))
        .orElse(false);
    }

    @Override
    public boolean isHideGraphicOn4BitColor() {
      return getDocumentRecord()
        .map(CDDocument::getFlags)
        // Though this flag looks unrelated, it's set when setting this value in Designer
        .map(flags -> flags.contains(CDDocument.Flag.SHOW_WINDOW_READ))
        .orElse(false);
    }

    @Override
    public boolean isUserCustomizable() {
      return !DesignConstants.RESTRICTBK_FLAG_NOOVERRIDE.equals(getDocument().getAsText(DesignConstants.ITEM_NAME_RESTRICTBKOVERRIDE, ' '));
    }

    @Override
    public ImageRepeatMode getBackgroundImageRepeatMode() {
      String repeatVal = getDocument().getAsText(DesignConstants.ITEM_NAME_BACKGROUNDGRAPHIC_REPEAT, ' ');
      return DominoEnumUtil.valueOfString(ImageRepeatMode.class, repeatVal)
        .orElse(ImageRepeatMode.TILE);
    }
  }
  
  private class DefaultHeaderFrameSettings implements HeaderFrameSettings {

    @Override
    public boolean isUseHeader() {
      return getDocument().get(DesignConstants.ITEM_NAME_HEADERAREA, int.class, 0) == 1;
    }
    

    // This frameset will have two frames, the first of which is the header

    @Override
    public Optional<FrameSizingType> getHeaderSizingType() {
      if(isUseHeader()) {
        return getRegionFrameset()
          .flatMap(DominoFramesetFormat::getFramesetRecord)
          .map(rec -> rec.getLengths().stream())
          .flatMap(Stream::findFirst)
          .map(FramesetLength::getType)
          .filter(Optional::isPresent)
          .map(Optional::get);
      } else {
        return Optional.empty();
      }
    }

    @Override
    public OptionalInt getHeaderSize() {
      if(isUseHeader()) {
        return getRegionFrameset()
          .flatMap(DominoFramesetFormat::getFramesetRecord)
          .map(rec -> rec.getLengths().stream())
          .flatMap(Stream::findFirst)
          .map(len -> OptionalInt.of(len.getValue()))
          .orElseGet(OptionalInt::empty);
      } else {
        return OptionalInt.empty();
      }
    }

    @Override
    public Optional<FrameScrollStyle> getScrollStyle() {
      if(isUseHeader()) {
        return getRegionFrameset()
          .map(DominoFramesetFormat::getFrameRecords)
          .flatMap(Stream::findFirst)
          .flatMap(CDFrame::getScrollBarStyle);
      } else {
        return Optional.empty();
      }
    }

    @Override
    public boolean isAllowResizing() {
      if(isUseHeader()) {
        return getRegionFrameset()
          .map(DominoFramesetFormat::getFrameRecords)
          .flatMap(Stream::findFirst)
          .map(rec -> rec.getNoResize() != 1)
          .orElse(true);
      } else {
        return true;
      }
    }

    @Override
    public OptionalInt getBorderWidth() {
      if(isUseHeader()) {
        return getRegionFrameset()
          .flatMap(DominoFramesetFormat::getFramesetRecord)
          .map(rec -> OptionalInt.of(rec.getFrameBorderWidth()))
          .orElseGet(OptionalInt::empty);
      } else {
        return OptionalInt.empty();
      }
    }

    @Override
    public Optional<ColorValue> getBorderColor() {
      if(isUseHeader()) {
        return getRegionFrameset()
          .map(DominoFramesetFormat::getFrameRecords)
          .flatMap(Stream::findFirst)
          .map(CDFrame::getFrameBorderColor);
      } else {
        return Optional.empty();
      }
    }

    @Override
    public boolean isUse3DShading() {
      if(isUseHeader()) {
        return getRegionFrameset()
          .map(DominoFramesetFormat::getFrameRecords)
          .flatMap(Stream::findFirst)
          .map(rec -> rec.getBorderEnable() == 1)
          .orElse(true);
      } else {
        return true;
      }
    }
  }
  
  private class DefaultPrintSettings implements PrintSettings {

    @Override
    public boolean isPrintHeaderAndFooterOnFirstPage() {
      return !getDocument().getAsText(DesignConstants.ITEM_NAME_HFFLAGS, ' ').contains(DesignConstants.HFFLAGS_NOPRINTONFIRSTPAGE);
    }

    @Override
    public Optional<CDHeader> getPrintHeader() {
      Document doc = getDocument();
      if(doc.hasItem(DesignConstants.ITEM_NAME_HEADER)) {
        return doc.getRichTextItem(DesignConstants.ITEM_NAME_HEADER)
          .stream()
          .filter(CDHeader.class::isInstance)
          .map(CDHeader.class::cast)
          .findFirst();
      } else {
        return Optional.empty();
      }
    }

    @Override
    public Optional<CDHeader> getPrintFooter() {
      Document doc = getDocument();
      if(doc.hasItem(DesignConstants.ITEM_NAME_FOOTER)) {
        return doc.getRichTextItem(DesignConstants.ITEM_NAME_FOOTER)
          .stream()
          .filter(CDHeader.class::isInstance)
          .map(CDHeader.class::cast)
          .findFirst();
      } else {
        return Optional.empty();
      }
    }
  }
}
