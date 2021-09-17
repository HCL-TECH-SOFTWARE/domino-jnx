package com.hcl.domino.commons.design;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import com.hcl.domino.commons.design.FramesetStorage.IFramesetRecordAccess;
import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.design.DesignColorsAndFonts;
import com.hcl.domino.design.DesignElement;
import com.hcl.domino.design.Folder;
import com.hcl.domino.design.Form;
import com.hcl.domino.design.Frame;
import com.hcl.domino.design.Frameset;
import com.hcl.domino.design.FramesetLayout;
import com.hcl.domino.design.FramesetLayout.FrameSizeUnit;
import com.hcl.domino.design.Navigator;
import com.hcl.domino.design.Page;
import com.hcl.domino.design.View;
import com.hcl.domino.design.frameset.FrameScrollStyle;
import com.hcl.domino.richtext.records.CDFrame;
import com.hcl.domino.richtext.records.CDFrame.BorderAlignment;
import com.hcl.domino.richtext.records.CDFrame.DataFlag;
import com.hcl.domino.richtext.records.CDFrame.Flag;
import com.hcl.domino.richtext.records.CDFrame.TextAlignment;
import com.hcl.domino.richtext.records.CDIDName;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.records.CDResource.ResourceClass;
import com.hcl.domino.richtext.records.CDResource.Type;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.structures.CDFrameVariableData;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.NOTELINK;

/**
 * Implementation for {@link Frame}
 * 
 * @author Karsten Lehmann
 */
public class FrameImpl implements Frame {
  private Optional<FramesetLayout> parent = Optional.empty();
  private List<RichTextRecord<?>> frameRecords;
  private boolean layoutDirty;

  //will be picked when the frameset record is constructed
  int size;
  Optional<FrameSizeUnit> sizeUnit;

  public FrameImpl() {
    this(Collections.emptyList());
    initDefaults();
  }

  @Override
  public void setParent(FramesetLayout parent) {
    this.parent = Optional.ofNullable(parent);
  }

  @Override
  public Optional<FramesetLayout> getParent() {
    return parent;
  }

  void initDefaults() {
    withFrameRecord((record) -> {
      size = 1;
      sizeUnit = Optional.of(FrameSizeUnit.RELATIVE);
      
      record.setFlags(Arrays.asList(Flag.Scrolling));
      record.setScrollBarStyle(FrameScrollStyle.AUTO);
      record.setBorderEnable((byte) 1);
      record.setMarginWidth(1);
      record.setMarginHeight(1);
      record.setDataFlags(Arrays.asList(DataFlag.NotesBorderFontAndColor));
      record.setTextColor(DesignColorsAndFonts.blackColor());
      record.setBackgroundColor(DesignColorsAndFonts.whiteColor());
      record.setFontStyle(DesignColorsAndFonts.defaultFont());
      layoutDirty = true;
    });
    
    resetLayoutDirty();
  }

  /**
   * Creates a new frame
   * 
   * @param parent parent frameset
   * @param frame frame CD record
   * @param resource optional CD resource record
   */
  public FrameImpl(List<RichTextRecord<?>> frameRecords) {
    this.frameRecords = new ArrayList<>(frameRecords);
  }

  /**
   * Locates the {@link CDFrame} record
   * 
   * @return record if it exists
   */
  private Optional<CDFrame> getFrameRecord() {
    return frameRecords
        .stream()
        .filter(CDFrame.class::isInstance)
        .map(CDFrame.class::cast)
        .findFirst();
  }

  /**
   * Gets (and creates if it does not exist) the {@link CDFrame} record
   * and passes it to the consumer.
   * 
   * @param consumer consumer
   */
  private void withFrameRecord(Consumer<CDFrame> consumer) {
    Optional<CDFrame> optFrameRecord = getFrameRecord();
    CDFrame frameRecord;
    if (!optFrameRecord.isPresent()) {
      frameRecord = MemoryStructureUtil.newStructure(CDFrame.class, 0);
      CDFrameVariableData frameVarData = new CDFrameVariableData(frameRecord);
      frameRecord.writeVariableFrameData(frameVarData);

      frameRecords.add(0, frameRecord);
      layoutDirty = true;
    }
    else {
      frameRecord = optFrameRecord.get();
    }

    consumer.accept(frameRecord);
  }

  /**
   * Locates the {@link CDResource} record
   * 
   * @return record if it exists
   */
  private Optional<CDResource> getResourceRecord() {
    return frameRecords
        .stream()
        .filter(CDResource.class::isInstance)
        .map(CDResource.class::cast)
        .findFirst();
  }

  /**
   * Gets (and creates if it does not exist) the {@link CDResource} record
   * and passes it to the consumer.
   * 
   * @param consumer consumer
   */
  private void withResourceRecord(Consumer<CDResource> consumer) {
    Optional<CDResource> optResourceRecord = getResourceRecord();
    CDResource resourceRecord;
    if (!optResourceRecord.isPresent()) {
      resourceRecord = MemoryStructureUtil.newStructure(CDResource.class, 0);
      int insertPos = frameRecords.size();

      for (int i=0; i<frameRecords.size(); i++) {

        if (frameRecords.get(i) instanceof CDFrame) {
          insertPos = i+1;
          break;
        }
      }

      frameRecords.add(insertPos, resourceRecord);
      layoutDirty = true;
    }
    else {
      resourceRecord = optResourceRecord.get();
    }

    consumer.accept(resourceRecord);
  }

  /**
   * Locates the {@link CDIDName} record
   * 
   * @return record if it exists
   */
  private Optional<CDIDName> getHtmlAttrRecord() {
    return frameRecords
        .stream()
        .filter(CDIDName.class::isInstance)
        .map(CDIDName.class::cast)
        .findFirst();
  }

  /**
   * Gets (and creates if it does not exist) the {@link CDIDName} record
   * and passes it to the consumer.
   * 
   * @param consumer consumer
   */
  private void withHtmlAttrRecord(Consumer<CDIDName> consumer) {
    Optional<CDIDName> optHtmlAttrRecord = getHtmlAttrRecord();
    CDIDName htmlAttrRecord;
    if (!optHtmlAttrRecord.isPresent()) {
      htmlAttrRecord = MemoryStructureUtil.newStructure(CDIDName.class, 0);
      int insertPos = frameRecords.size();

      for (int i=0; i<frameRecords.size(); i++) {

        if (frameRecords.get(i) instanceof CDFrame) {
          insertPos = i+1;
          //go on searching
        }
        else if (frameRecords.get(i) instanceof CDResource) {
          insertPos = i+1;
          break;
        }
      }

      frameRecords.add(insertPos, htmlAttrRecord);
      layoutDirty = true;
    }
    else {
      htmlAttrRecord = optHtmlAttrRecord.get();
    }

    consumer.accept(htmlAttrRecord);
  }

  @Override
  public Frame setName(String name) {
    withFrameRecord((record) -> {
      record.setFrameName(name);
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public Frame setSequenceNo(int n) {
    withFrameRecord((record) -> {
      record.setSequenceNo(n);
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public int getSequenceNo() {
    return getFrameRecord()
        .map((record) -> {return record.getSequenceNo(); })
        .orElse(0);
  }

  @Override
  public Frame setNamedElement(Class<? extends DesignElement> type, String name) {
    withResourceRecord((record) -> {
      if (type == Page.class) {
        record.setResourceClass(ResourceClass.PAGE);
      }
      else if (type == Form.class) {
        record.setResourceClass(ResourceClass.FORM);
      }
      else if (type == Frameset.class) {
        record.setResourceClass(ResourceClass.FRAMESET);
      }
      else if (type == View.class) {
        record.setResourceClass(ResourceClass.VIEW);
      }
      else if (type == Folder.class) {
        record.setResourceClass(ResourceClass.FOLDER);
      }
      else if (type == Navigator.class) {
        record.setResourceClass(ResourceClass.NAVIGATOR);
      }
      else {
        throw new IllegalArgumentException(MessageFormat.format("Unsupported design element type: ", type));
      }
      
      record.setNamedElement(name);
      record.setResourceType(Type.NAMEDELEMENT);
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public Frame setContentUrl(String url) {
    withResourceRecord((record) -> {
      record.setResourceUrl(url);
      record.setResourceType(Type.NAMEDELEMENT);
      layoutDirty = true;
    });
    return this;
  }
  
  @Override
  public Frame setContentLink(NOTELINK link) {
    withResourceRecord((record) -> {
      record.setLink(link);
      layoutDirty = true;
    });
    return this;
  }
  
  @Override
  public Frame setContentLink(String replicaId, String viewUnid, String docUnid) {
    withResourceRecord((record) -> {
      if (!StringUtil.isEmpty(replicaId)) {
        NOTELINK link = MemoryStructureUtil.newStructure(NOTELINK.class, 0);
        link.setReplicaId(replicaId);
        
        if (!StringUtil.isEmpty(viewUnid)) {
          link.setViewUnid(viewUnid);
          
          if (!StringUtil.isEmpty(docUnid)) {
            //create doc link
            link.setDocUnid(docUnid);
          }
        }
        
        record.setLink(link);
        layoutDirty = true;
      }
      else {
        throw new IllegalArgumentException("DB replicaid is missing");
      }
    });
    return this;
  }

  @Override
  public Frame setTargetName(String target) {
    withFrameRecord((record) -> {
      record.setFrameTarget(target);
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public Frame setScrollType(ScrollType type) {
    withFrameRecord((record) -> {
      Set<Flag> flags;

      switch (type) {
      case On:
        record.setScrollBarStyle(FrameScrollStyle.ALWAYS);
        flags = record.getFlags();
        flags.add(Flag.Scrolling);
        record.setFlags(flags);
        break;
      case Off:
        record.setScrollBarStyle(FrameScrollStyle.NEVER);
        flags = record.getFlags();
        flags.add(Flag.Scrolling);
        record.setFlags(flags);
        break;
      case Auto:
        record.setScrollBarStyle(FrameScrollStyle.AUTO);
        flags = record.getFlags();
        flags.add(Flag.Scrolling);
        record.setFlags(flags);
        break;
      case Default:
        flags = record.getFlags();
        flags.remove(Flag.Scrolling);
        record.setFlags(flags);
        break;
      default:
        throw new IllegalArgumentException(
            MessageFormat.format("Invalid scroll type: {0}", type));
      }
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public Frame setAllowResizing(boolean b) {
    withFrameRecord((record) -> {
      record.setNoResize((byte) (b ? 0 : 1));
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public Frame setInitialFocus(boolean b) {
    withFrameRecord((record) -> {
      Set<Flag> flags = record.getFlags();
      if (b) {
        flags.add(Flag.NotesInitialFocus);
      }
      else {
        flags.remove(Flag.NotesInitialFocus);
      }
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public Frame setBorderEnabled(boolean b) {
    withFrameRecord((record) -> {
      record.setBorderEnable((byte) (b ? 1 : 0));
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public Frame setCaptionFormula(String formula) {
    withFrameRecord((record) -> {
      Set<DataFlag> dataFlags = record.getDataFlags();
      dataFlags.add(DataFlag.NotesBorderCaption);
      record.setDataFlags(dataFlags);
      
      record.setCaptionFormula(formula);
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public Frame setCaptionMode(CaptionMode mode) {
    withFrameRecord((record) -> {
      boolean isNotesOnlyBorder;
      boolean isNotesOnlyArrows;

      //BOTH:
      //"flags" : [ "BorderEnable", "NotesOnlyBorder", "NotesOnlyArrows" ],
      //"dataFlags" : [ "NotesBorderFontAndColor", "NotesBorderCaption" ],

      //CAPTIONONLY:
      //"flags" : [ "BorderEnable", "NotesOnlyBorder" ],
      //"dataFlags" : [ "NotesBorderFontAndColor", "NotesBorderCaption" ],

      //ARROWSONLY:
      //"flags" : [ "BorderEnable", "NotesOnlyArrows" ],
      //"dataFlags" : [ "NotesBorderFontAndColor", "NotesBorderCaption" ],

      //NONE:
      //"flags" : [ "BorderEnable" ],
      //"dataFlags" : [ "NotesBorderFontAndColor", "NotesBorderCaption" ],

      switch (mode) {
      case Both:
        isNotesOnlyBorder = true;
        isNotesOnlyArrows = true;
        break;
      case CaptionOnly:
        isNotesOnlyBorder = true;
        isNotesOnlyArrows = false;
        break;
      case ArrowsOnly:
        isNotesOnlyBorder = false;
        isNotesOnlyArrows = true;
        break;
      default:
        isNotesOnlyBorder = false;
        isNotesOnlyArrows = false;
        break;
      }

      Set<Flag> flags = record.getFlags();
      if (isNotesOnlyBorder) {
        flags.add(Flag.NotesOnlyBorder);
      }
      else {
        flags.remove(Flag.NotesOnlyBorder);
      }

      if (isNotesOnlyArrows) {
        flags.add(Flag.NotesOnlyArrows);
      }
      else {
        flags.remove(Flag.NotesOnlyArrows);
      }
      record.setFlags(flags);
      layoutDirty = true;
    });
    return this;
  }
  
  @Override
  public Frame setCaptionBorderAlignment(CaptionBorderAlignment align) {
    withFrameRecord((record) -> {
      switch (align) {
      case Left:
        record.setBorderAlignment(BorderAlignment.Left);
        break;
      case Right:
        record.setBorderAlignment(BorderAlignment.Right);
        break;
      case Top:
        record.setBorderAlignment(BorderAlignment.Top);
        break;
      case Bottom:
        record.setBorderAlignment(BorderAlignment.Bottom);
        break;
      }
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public Frame setCaptionTextAlignment(CaptionTextAlignment justify) {
    withFrameRecord((record) -> {
      switch (justify) {
      case Left:
        record.setTextAlignment(TextAlignment.Left);
        break;
      case Right:
        record.setTextAlignment(TextAlignment.Right);
        break;
      case Center:
        record.setTextAlignment(TextAlignment.Center);
        break;
      }
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public Frame setOpen(int amount, OptionUnit unit) {
    withFrameRecord((record) -> {
      record.setOpen(amount);
      
      Set<Flag>flags = record.getFlags();
      
      if (unit == OptionUnit.Percent) {
        flags.add(Flag.NotesOpenPercent);
      }
      else {
        flags.remove(Flag.NotesOpenPercent);
      }
      
      record.setFlags(flags);
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public Frame setCaptionStyle(FontStyle style) {
    withFrameRecord((record) -> {
      record.setFontStyle(style);
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public Frame setCaptionFontName(String fontName) {
    withFrameRecord((record) -> {
      record.setFontName(fontName);
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public Frame setCaptionTextColor(ColorValue color) {
    withFrameRecord((record) -> {
      record.setTextColor(color);
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public Frame setCaptionBackgroundColor(ColorValue color) {
    withFrameRecord((record) -> {
      record.setBackgroundColor(color);
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public Frame setMarginHeight(int amount) {
    withFrameRecord((record) -> {
      record.setMarginHeight(amount);
      Set<Flag> flags = record.getFlags();
      flags.add(Flag.MarginHeight);
      record.setFlags(flags);
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public Frame setMarginHeightDefault() {
    withFrameRecord((record) -> {
      Set<Flag> flags = record.getFlags();
      flags.remove(Flag.MarginHeight);
      record.setFlags(flags);
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public Frame setMarginWidth(int amount) {
    withFrameRecord((record) -> {
      record.setMarginWidth(amount);
      Set<Flag> flags = record.getFlags();
      flags.add(Flag.MarginWidth);
      record.setFlags(flags);
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public Frame setMarginWidthDefault() {
    withFrameRecord((record) -> {
      Set<Flag> flags = record.getFlags();
      flags.remove(Flag.MarginWidth);
      record.setFlags(flags);
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public Frame setHTMLId(String id) {
    withHtmlAttrRecord((record) -> {
      record.setID(id);
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public Frame setHTMLClassName(String c) {
    withHtmlAttrRecord((record) -> {
      record.setClassName(c);
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public Frame setHTMLStyle(String style) {
    withHtmlAttrRecord((record) -> {
      record.setStyle(style);
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public Frame setHTMLTitle(String title) {
    withHtmlAttrRecord((record) -> {
      record.setTitle(title);
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public Frame setHTMLAttributes(String other) {
    withHtmlAttrRecord((record) -> {
      record.setHTMLAttributes(other);
      layoutDirty = true;
    });
    return this;
  }

  @Override
  public String getName() {
    return getFrameRecord().map(CDFrame::getFrameName).orElse(""); //$NON-NLS-1$
  }

  @Override
  public Optional<FrameContentType> getContentType() {
    return getResourceRecord().flatMap((record) -> {
      if (record.getResourceType() == Type.URL) {
        return Optional.of(FrameContentType.URL);
      }
      else if (record.getResourceType() == Type.NOTELINK) {
        return Optional.of(FrameContentType.Link);
      }
      else if (record.getResourceType() == Type.NAMEDELEMENT) {
        return Optional.of(FrameContentType.NamedElement);
      }
      else {
        return Optional.empty();
      }
    });
  }

  @Override
  public Optional<Class<? extends DesignElement>> getNamedElementType() {
    Optional<CDResource> optResourceRecord = getResourceRecord();
    if (optResourceRecord.isPresent()) {
      Optional<ResourceClass> optResourceClass = optResourceRecord.get().getResourceClass();
      if (optResourceClass.isPresent()) {
        ResourceClass resourceClass = optResourceClass.get();
        
        if (resourceClass == ResourceClass.PAGE) {
          return Optional.of(Page.class);
        }
        else if (resourceClass == ResourceClass.FORM) {
          return Optional.of(Form.class);
        }
        else if (resourceClass == ResourceClass.FRAMESET) {
          return Optional.of(Frameset.class);
        }
        else if (resourceClass == ResourceClass.VIEW) {
          return Optional.of(View.class);
        }
        else if (resourceClass == ResourceClass.FOLDER) {
          return Optional.of(Folder.class);
        }
        else if (resourceClass == ResourceClass.NAVIGATOR) {
          return Optional.of(Navigator.class);
        }
        else {
          throw new IllegalArgumentException(MessageFormat.format("Unsupported resource class found: {0}", resourceClass));
        }
      }
    }
    return Optional.empty();
  }

  @Override
  public Optional<String> getNamedElement() {
    return getResourceRecord().flatMap(CDResource::getNamedElement);
  }

  @Override
  public String getTargetName() {
    return getFrameRecord().map(CDFrame::getFrameTarget).orElse(""); //$NON-NLS-1$
  }

  @Override
  public Optional<ScrollType> getScrollType() {
    return getFrameRecord().flatMap((record) -> {
      Set<Flag> flags = record.getFlags();
      if (flags.contains(Flag.Scrolling)) {
        FrameScrollStyle scrollStyle = record.getScrollBarStyle();
        switch (scrollStyle) {
        case ALWAYS:
          return Optional.of(ScrollType.On);
        case NEVER:
          return Optional.of(ScrollType.Off);
        case AUTO:
          return Optional.of(ScrollType.Auto);
        }
      }
      return Optional.empty();
    });
  }

  @Override
  public boolean isAllowResizing() {
    return getFrameRecord()
        .map((record) -> { return record.getNoResize()==0; })
        .orElse(true);
  }

  @Override
  public boolean isInitialFocus() {
    return getFrameRecord()
        .map(CDFrame::getFlags)
        .map((flags) -> { return flags.contains(CDFrame.Flag.NotesInitialFocus); })
        .orElse(false);
  }

  @Override
  public boolean isBorderEnabled() {
    return getFrameRecord()
        .map((record) -> { return record.getBorderEnable()!=0; })
        .orElse(false);
  }

  @Override
  public Optional<String> getCaptionFormula() {
    return getFrameRecord().flatMap((record) -> { return record.getCaptionFormula(); });
  }

  @Override
  public CaptionMode getCaptionMode() {
    Optional<CDFrame> optFrameRecord = getFrameRecord();
    if (optFrameRecord.isPresent()) {
      CDFrame frameRecord = optFrameRecord.get();
      Set<Flag> flags = frameRecord.getFlags();
      Set<DataFlag> dataFlags = frameRecord.getDataFlags();
      
      if (dataFlags.contains(DataFlag.NotesBorderCaption)) {
        boolean isNotesOnlyBorder = flags.contains(Flag.NotesOnlyBorder);
        boolean isNotesOnlyArrows = flags.contains(Flag.NotesOnlyArrows);
        
        //BOTH:
        //"flags" : [ "BorderEnable", "NotesOnlyBorder", "NotesOnlyArrows" ],
        //"dataFlags" : [ "NotesBorderFontAndColor", "NotesBorderCaption" ],
        
        //CAPTIONONLY:
        //"flags" : [ "BorderEnable", "NotesOnlyBorder" ],
        //"dataFlags" : [ "NotesBorderFontAndColor", "NotesBorderCaption" ],
        
        //ARROWSONLY:
        //"flags" : [ "BorderEnable", "NotesOnlyArrows" ],
        //"dataFlags" : [ "NotesBorderFontAndColor", "NotesBorderCaption" ],
        
        //NONE:
        //"flags" : [ "BorderEnable" ],
        //"dataFlags" : [ "NotesBorderFontAndColor", "NotesBorderCaption" ],
        
        if (isNotesOnlyBorder) {
          if (isNotesOnlyArrows) {
            return CaptionMode.Both;
          }
          else {
            return CaptionMode.CaptionOnly;
          }
        }
        else {
          if (isNotesOnlyArrows) {
            return CaptionMode.ArrowsOnly;
          }
          else {
           return CaptionMode.None; 
          }
        }
      }
      else {
        return CaptionMode.None;
      }
    }
    else {
      return CaptionMode.None;
    }
  }

  @Override
  public Optional<CaptionBorderAlignment> getCaptionBorderAlignment() {
    return getFrameRecord()
        .flatMap((record) -> {
          return record.getBorderAlignment();
        })
        .map((alignment) -> {
          switch (alignment) {
          case Left:
            return CaptionBorderAlignment.Left;
          case Right:
            return CaptionBorderAlignment.Right;
          case Bottom:
            return CaptionBorderAlignment.Bottom;
          case Top:
            return CaptionBorderAlignment.Top;
          }
          return null;
        });
  }

  @Override
  public Optional<CaptionTextAlignment> getCaptionTextAlignment() {
    return getFrameRecord()
        .flatMap((record) -> {
          return record.getTextAlignment();
        })
        .map((alignment) -> {
          switch (alignment) {
          case Left:
            return CaptionTextAlignment.Left;
          case Right:
            return CaptionTextAlignment.Right;
          case Center:
            return CaptionTextAlignment.Center;
          }
          return null;
        });
  }

  @Override
  public Optional<OptionUnit> getOpenUnit() {
    Optional<CDFrame> optFrameRecord = getFrameRecord();
    if (optFrameRecord.isPresent()) {
      if (optFrameRecord.get().getFlags().contains(CDFrame.Flag.NotesOpenPercent)) {
        return Optional.of(OptionUnit.Percent);
      }
      else {
        return Optional.of(OptionUnit.Pixels);
      }
    }

    return Optional.empty();
  }

  @Override
  public int getOpen() {
    return getFrameRecord().map(CDFrame::getOpen).orElse(0);
  }

  @Override
  public FontStyle getCaptionStyle() {
    return getFrameRecord().flatMap(CDFrame::getFontStyle).get();
  }

  @Override
  public String getCaptionFontName() {
    return getFrameRecord().flatMap(CDFrame::getFontName).orElse("");
  }

  @Override
  public Optional<ColorValue> getCaptionTextColor() {
    return getFrameRecord().flatMap(CDFrame::getTextColor);
  }

  @Override
  public Optional<ColorValue> getCaptionBackgroundColor() {
    return getFrameRecord().flatMap(CDFrame::getBackgroundColor);
  }

  @Override
  public Optional<Integer> getMarginHeight() {
    Optional<CDFrame> optFrameRecord = getFrameRecord();
    if (optFrameRecord.isPresent()) {
      if (optFrameRecord.get().getFlags().contains(CDFrame.Flag.MarginHeight)) {
        return Optional.of(optFrameRecord.get().getMarginHeight());
      }
    }
    return Optional.empty();
  }

  @Override
  public Optional<Integer> getMarginWidth() {
    Optional<CDFrame> optFrameRecord = getFrameRecord();
    if (optFrameRecord.isPresent()) {
      if (optFrameRecord.get().getFlags().contains(CDFrame.Flag.MarginWidth)) {
        return Optional.of(optFrameRecord.get().getMarginWidth());
      }
    }
    return Optional.empty();
  }

  @Override
  public String getHTMLId() {
    return getHtmlAttrRecord().map(CDIDName::getID).orElse(""); //$NON-NLS-1$
  }

  @Override
  public String getHTMLClassName() {
    return getHtmlAttrRecord().map(CDIDName::getClassName).orElse(""); //$NON-NLS-1$
  }

  @Override
  public String getHTMLStyle() {
    return getHtmlAttrRecord().map(CDIDName::getStyle).orElse(""); //$NON-NLS-1$
  }

  @Override
  public String getHTMLTitle() {
    return getHtmlAttrRecord().map(CDIDName::getTitle).orElse(""); //$NON-NLS-1$
  }

  @Override
  public String getHTMLAttributes() {
    return getHtmlAttrRecord().map(CDIDName::getHTMLAttributes).orElse(""); //$NON-NLS-1$
  }

  @Override
  public boolean isLayoutDirty() {
    return layoutDirty;
  }

  @Override
  public void resetLayoutDirty() {
    layoutDirty = false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getAdapter(Class<T> clazz) {
    if (FramesetStorage.IFramesetRecordAccess.class == clazz) {
      return (T) new IFramesetRecordAccess() {

        @Override
        public List<RichTextRecord<?>> getRecords() {
          List<RichTextRecord<?>> allRecords = new ArrayList<>();

          //make sure we have a CDFrame record; CDResource/CDIDName are in the list if their values have been set
          withFrameRecord((record) -> {
            frameRecords.forEach(allRecords::add);
          });

          return allRecords;
        }
      };
    }
    return null;
  }

  @Override
  public Frame setSize(int amount, FrameSizeUnit unit) {
    this.size = amount;
    this.sizeUnit = Optional.of(unit);
    layoutDirty = true;
    return this;
  }

  @Override
  public int getSize() {
    return size;
  }

  @Override
  public Optional<FrameSizeUnit> getSizeUnit() {
    return sizeUnit;
  }

  @Override
  public Frame removeFrameContents() {
    Optional<CDResource> optResourceRecord = getResourceRecord();
    if (optResourceRecord.isPresent()) {
      CDResource resourceRecord = optResourceRecord.get();
      resourceRecord.setResourceClass(ResourceClass.UNKNOWN);
      resourceRecord.setNamedElement(""); //$NON-NLS-1$
      layoutDirty = true;
    }
    return this;
  }

}
