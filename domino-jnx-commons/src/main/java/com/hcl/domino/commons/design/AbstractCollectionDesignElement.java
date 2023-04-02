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
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.design.view.CollationEncoder;
import com.hcl.domino.commons.design.view.DefaultCalendarSettings;
import com.hcl.domino.commons.design.view.DominoCalendarFormat;
import com.hcl.domino.commons.design.view.DominoCollationInfo;
import com.hcl.domino.commons.design.view.DominoCollectionColumn;
import com.hcl.domino.commons.design.view.DominoViewFormat;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.CollectionColumn;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.data.Formula;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.design.ClassicThemeBehavior;
import com.hcl.domino.design.CollectionDesignElement;
import com.hcl.domino.design.DesignColorsAndFonts;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.DesignElement;
import com.hcl.domino.design.EdgeWidths;
import com.hcl.domino.design.ImageRepeatMode;
import com.hcl.domino.design.SharedColumn;
import com.hcl.domino.design.action.EventId;
import com.hcl.domino.design.format.ViewLineSpacing;
import com.hcl.domino.design.format.ViewTableFormat;
import com.hcl.domino.design.format.ViewTableFormat2;
import com.hcl.domino.design.format.ViewTableFormat3;
import com.hcl.domino.design.format.ViewTableFormat4;
import com.hcl.domino.design.format.ViewTableFormat.Flag;
import com.hcl.domino.formula.FormulaCompiler;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.RichTextRecordList;
import com.hcl.domino.richtext.records.CDLinkColors;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.records.CDTarget;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.util.JNXStringUtil;

/**
 * @param <T> the {@link DesignElement} interface implemented by the class
 * @since 1.0.18
 */
public abstract class AbstractCollectionDesignElement<T extends CollectionDesignElement<?>> extends AbstractDesignElement<T>
    implements CollectionDesignElement<T>, IDefaultAutoFrameElement, IDefaultActionBarElement, IDefaultReadersRestrictedElement,
    IDefaultNamedDesignElement, IAdaptable {
  private DominoViewFormat format;
  private DominoCalendarFormat calendarFormat;
  private boolean calendarFormatDirty;
  
  public AbstractCollectionDesignElement(final Document doc) {
    super(doc);
  }

  @Override
  public void initializeNewDesignNote() {
    Document doc = getDocument();
    
    DominoCollationInfo collationInfo = new DominoCollationInfo();
    doc.replaceItemValue(DesignConstants.VIEW_COLLATION_ITEM,
        EnumSet.of(ItemFlag.SUMMARY), collationInfo);
    doc.replaceItemValue(NotesConstants.VIEW_COMMENT_ITEM,
        EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), ""); //$NON-NLS-1$
    doc.replaceItemValue(DesignConstants.DESIGNER_VERSION, "8.5.3"); //$NON-NLS-1$
    doc.replaceItemValue(DesignConstants.VIEW_INDEX_ITEM,
        EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), ""); //$NON-NLS-1$
    doc.replaceItemValue(DesignConstants.VIEW_CLASSES_ITEM, EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), "1");
    
    
    this.format = new DominoViewFormat(doc);
  }
  
  @Override
  public T copyViewFormatFrom(CollectionDesignElement<?> viewOrFolder) {
    Document docOtherViewOrFolder = viewOrFolder.getDocument();
    DominoViewFormat viewFormat = docOtherViewOrFolder.get(DesignConstants.VIEW_VIEW_FORMAT_ITEM, DominoViewFormat.class, null);
    if (viewFormat==null) {
      throw new IllegalArgumentException("No view format found in provided view or folder design element");
    }
    this.format = viewFormat;
    setViewFormatDirty(true);
    
    return (T) this;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public <T> T getAdapter(Class<T> clazz) {
    if (DominoViewFormat.class.equals(clazz)) {
      return (T) getViewFormat(false).orElse(null);
    }
    else if (DominoCalendarFormat.class.equals(clazz)) {
      return (T) getCalendarFormat(false).orElse(null);
    }

    return null;
  }
  
  @Override
  public T addColumn(int pos, String title, String itemName, Consumer<CollectionColumn> consumer) {
    DominoViewFormat viewFormat = getViewFormat(true)
    .orElseThrow(() -> new IllegalStateException("Unable to read $ViewFormat data"));

    DominoCollectionColumn newCol = (DominoCollectionColumn) viewFormat.addColumn(pos);
    newCol.setTitle(title);
    newCol.setItemName(itemName);
    
    if (consumer!=null) {
      consumer.accept(newCol);
    }
    
    setViewFormatDirty(true);
    
    return (T) this;
  }

  @Override
  public T addColumn(int pos, CollectionColumn templateCol, Consumer<CollectionColumn> consumer) {
    if (!(templateCol instanceof DominoCollectionColumn)) {
      throw new IllegalArgumentException(MessageFormat.format("Template column is not a DominoCollectionColumn: {0}",
          templateCol==null ? "null" : templateCol.getClass().getName())); //$NON-NLS-1$
    }
    DominoCollectionColumn dominoTemplateCol = (DominoCollectionColumn) templateCol;

    DominoViewFormat viewFormat = getViewFormat(true)
    .orElseThrow(() -> new IllegalStateException("Unable to read $ViewFormat data"));
    
    DominoCollectionColumn newCol = (DominoCollectionColumn) viewFormat.addColumn(pos);
    newCol.copyColumnFormatFrom(dominoTemplateCol);
    
    if (consumer!=null) {
      consumer.accept(newCol);
    }
    
    setViewFormatDirty(true);
    
    return (T) this;
  }
  
  @Override
  public T addSharedColumn(int pos, String columnName, Consumer<CollectionColumn> consumer) {
    SharedColumn sharedCol = getDocument()
        .getParentDatabase()
        .getDesign()
        .getSharedColumn(columnName)
        .orElseThrow(() -> new DominoException(MessageFormat.format("Shared column not found: {0}", columnName)));
    
    addColumn(pos, sharedCol.getColumn(), (col) -> {
      col.setSharedColumn(true);
      col.setSharedColumnName(columnName);
    });
    
    return (T) this;
  }
  
  @Override
  public DominoCollection getCollection() {
    Document doc = getDocument();
    if (doc.isNew()) {
      throw new IllegalStateException("Design element has not been saved yet");
    }
    
    return doc.getParentDatabase().openCollectionByUNID(getDocument().getUNID()).get();
  }

  @Override
  public List<CollectionColumn> getColumns() {
    return this.getViewFormat(true)
      .map(DominoViewFormat::getColumns)
      .orElseGet(Collections::emptyList);
  }

  @Override
  public OnOpen getOnOpenUISetting() {
    return getViewFormat(false)
      .map(format -> {
        final ViewTableFormat format1 = format.getAdapter(ViewTableFormat.class);
        final Set<ViewTableFormat.Flag> flags = format1.getFlags();
        if (flags.contains(ViewTableFormat.Flag.GOTO_BOTTOM_ON_OPEN)) {
          return OnOpen.GOTO_BOTTOM;
        } else if (flags.contains(ViewTableFormat.Flag.GOTO_TOP_ON_OPEN)) {
          return OnOpen.GOTO_TOP;
        } else {
          return OnOpen.GOTO_LAST_OPENED;
        }
      })
      .orElse(OnOpen.GOTO_LAST_OPENED);
  }

  @Override
  public T setOnOpenUISetting(OnOpen setting) {
    getViewFormat(true)
    .map(DominoViewFormat::getFormat1)
    .ifPresent((fmt) -> {
      Set<ViewTableFormat.Flag> oldFlags = fmt.getFlags();
      Set<ViewTableFormat.Flag> newFlags = new HashSet<>(oldFlags);
      
      newFlags.remove(ViewTableFormat.Flag.GOTO_BOTTOM_ON_OPEN);
      newFlags.remove(ViewTableFormat.Flag.GOTO_TOP_ON_OPEN);

      if (setting == OnOpen.GOTO_BOTTOM) {
        newFlags.add(ViewTableFormat.Flag.GOTO_BOTTOM_ON_OPEN);
      }
      else if (setting == OnOpen.GOTO_TOP) {
        newFlags.add(ViewTableFormat.Flag.GOTO_TOP_ON_OPEN);
      }
      fmt.setFlags(newFlags);
      setViewFormatDirty(true);
    });
    return (T) this;
  }
  
  @Override
  public OnRefresh getOnRefreshUISetting() {
    return getViewFormat(false)
      .map(format -> {
        final ViewTableFormat format1 = format.getAdapter(ViewTableFormat.class);
        final Set<ViewTableFormat.Flag> flags = format1.getFlags();
        // Auto-refresh is denoted by both flags being present
        if (flags.contains(ViewTableFormat.Flag.GOTO_BOTTOM_ON_REFRESH) && flags.contains(ViewTableFormat.Flag.GOTO_TOP_ON_REFRESH)) {
          return OnRefresh.REFRESH_DISPLAY;
        } else if (flags.contains(ViewTableFormat.Flag.GOTO_BOTTOM_ON_REFRESH)) {
          return OnRefresh.REFRESH_FROM_BOTTOM;
        } else if (flags.contains(ViewTableFormat.Flag.GOTO_TOP_ON_REFRESH)) {
          return OnRefresh.REFRESH_FROM_TOP;
        } else {
          return OnRefresh.DISPLAY_INDICATOR;
        }
      })
      .orElse(OnRefresh.DISPLAY_INDICATOR);
  }

  @Override
  public T setOnRefreshUISetting(OnRefresh onRefreshUISetting) {
    getViewFormat(true)
    .map(DominoViewFormat::getFormat1)
    .ifPresent((fmt) -> {
      Set<ViewTableFormat.Flag> oldFlags = fmt.getFlags();
      Set<ViewTableFormat.Flag> newFlags = new HashSet<>(oldFlags);
      
      newFlags.remove(ViewTableFormat.Flag.GOTO_BOTTOM_ON_REFRESH);
      newFlags.remove(ViewTableFormat.Flag.GOTO_TOP_ON_REFRESH);

      if (onRefreshUISetting == OnRefresh.REFRESH_DISPLAY) {
        newFlags.add(ViewTableFormat.Flag.GOTO_BOTTOM_ON_REFRESH);
        newFlags.add(ViewTableFormat.Flag.GOTO_TOP_ON_REFRESH);
      }
      else if (onRefreshUISetting == OnRefresh.REFRESH_FROM_BOTTOM) {
        newFlags.add(ViewTableFormat.Flag.GOTO_BOTTOM_ON_REFRESH);
      }
      else if (onRefreshUISetting == OnRefresh.REFRESH_FROM_TOP) {
        newFlags.add(ViewTableFormat.Flag.GOTO_TOP_ON_REFRESH);
      }

      fmt.setFlags(newFlags);
      setViewFormatDirty(true);
    });
    return (T) this;
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
  public boolean isAllowCustomizations() {
    return getViewFormat(false)
      .map(format -> {
        final ViewTableFormat3 format3 = format.getAdapter(ViewTableFormat3.class);
        if (format3 == null) {
          return false;
        } else {
          // It appears that this flag is inverted in practice
          return !format3.getFlags().contains(ViewTableFormat3.Flag.AllowCustomizations);
        }
      })
      .orElse(false);
  }

  @Override
  public T setAllowCustomizations(boolean b) {
    getViewFormat(true)
    .flatMap((viewFormat) -> { return viewFormat.getFormat3(true); })
    .ifPresent((fmt3) -> {
      // It appears that this flag is inverted in practice
      fmt3.setFlag(ViewTableFormat3.Flag.AllowCustomizations, !b);
      setViewFormatDirty(true);
    });
    return (T) this;
  }
  
  @Override
  public T removeColumn(final CollectionColumn column) {
    DominoViewFormat viewFormat = getViewFormat(false)
    .orElseThrow(() -> new IllegalStateException("Unable to read $ViewFormat data"));

    viewFormat.removeColumn(column);
    setViewFormatDirty(true);
    return (T) this;
  }

  @Override
  public T swapColumns(final CollectionColumn a, final CollectionColumn b) {
    DominoViewFormat viewFormat = getViewFormat(false)
    .orElseThrow(() -> new IllegalStateException("Unable to read $ViewFormat data"));

    viewFormat.swapColumns(a, b);
    setViewFormatDirty(true);
    return (T) this;
  }

  @Override
  public T swapColumns(final int a, final int b) {
    DominoViewFormat viewFormat = getViewFormat(false)
    .orElseThrow(() -> new IllegalStateException("Unable to read $ViewFormat data"));

    viewFormat.swapColumns(a, b);
    setViewFormatDirty(true);
    return (T) this;
  }
  
  @Override
  public ClassicThemeBehavior getClassicThemeBehavior() {
    return getViewFormat(false)
      .map(format -> {
        final ViewTableFormat3 format3 = format.getAdapter(ViewTableFormat3.class);
        if(format3 == null) {
          return ClassicThemeBehavior.USE_DATABASE_SETTING;
        } else {
          byte themeSetting = (byte)format3.getThemeSetting();
          return DominoEnumUtil.valueOf(ClassicThemeBehavior.class, themeSetting)
            .orElse(ClassicThemeBehavior.USE_DATABASE_SETTING);
        }
      })
      .orElse(ClassicThemeBehavior.USE_DATABASE_SETTING);
  }
  
  @Override
  public Style getStyle() {
    return getCalendarFormat(false)
        .map((calFormat) -> { return Style.CALENDAR; })
        .orElse(Style.STANDARD_OUTLINE);
  }
  
  @Override
  public T setStyle(Style style) {
    if (style == Style.CALENDAR) {
      getCalendarFormat(true).get().getFormat2(true);
      
      getViewFormat(true)
      .get().getFormat1().setFlag(ViewTableFormat.Flag.HIDE_HEADINGS, true)
      .setFlag(ViewTableFormat.Flag.CONFLICT, true)
      .setFlag(ViewTableFormat.Flag.FLATINDEX, true);
      
      setFlag(NotesConstants.DESIGN_FLAG_CALENDAR_VIEW, true);
    }
    else {
      Document doc = getDocument();
      doc.removeItem(NotesConstants.VIEW_CALENDAR_FORMAT_ITEM);
      setFlag(NotesConstants.DESIGN_FLAG_CALENDAR_VIEW, false);
    }
    
    setCalendarFormatDirty(true);
    return (T) this;
  }
  
  @Override
  public boolean isDefaultCollection() {
    return getDocument().getDocumentClass().contains(DocumentClass.DEFAULT);
  }
  
  @Override
  public T setDefaultCollection(boolean b) {
    Document doc = getDocument();
    Set<DocumentClass> oldDocumentClass = doc.getDocumentClass();
    
    if (b) {
      if (!oldDocumentClass.contains(DocumentClass.DEFAULT)) {
        Set<DocumentClass> newDocumentClass = new HashSet<>(oldDocumentClass);
        newDocumentClass.add(DocumentClass.DEFAULT);
        doc.setDocumentClass(newDocumentClass);
      }
    }
    else {
      if (oldDocumentClass.contains(DocumentClass.DEFAULT)) {
        Set<DocumentClass> newDocumentClass = new HashSet<>(oldDocumentClass);
        newDocumentClass.remove(DocumentClass.DEFAULT);
        doc.setDocumentClass(newDocumentClass);
      }
    }
    
    return (T) this;
  }
  
  @Override
  public boolean isDefaultCollectionDesign() {
    return getDocument().getAsText(NotesConstants.DESIGN_FLAGS, ' ').contains(NotesConstants.DESIGN_FLAG_DEFAULT_DESIGN);
  }
  
  @Override
  public T setDefaultCollectionDesign(boolean b) {
    setFlag(NotesConstants.DESIGN_FLAG_DEFAULT_DESIGN, b);
    return (T) this;
  }
  
  @Override
  public void setTitle(final String... title) {
    //$TITLE for views/folders/sharedcolumns are stored differently than for forms (concatenated with |, no string list)
    this.getDocument().replaceItemValue(NotesConstants.FIELD_TITLE,
        EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY),
        Arrays.asList(title).stream().collect(Collectors.joining("|"))); //$NON-NLS-1$
  }

  @Override
  public boolean isCollapseAllOnFirstOpen() {
    return getViewFormat(false)
      .map(format -> 
        format.getAdapter(ViewTableFormat.class)
          .getFlags()
          .contains(ViewTableFormat.Flag.COLLAPSED)
      )
      .orElse(false);
  }
  
  @Override
  public T setCollapseAllOnFirstOpen(boolean b) {
    getViewFormat(true)
    .ifPresent((viewFormat) -> {
      viewFormat.getFormat1().setFlag(ViewTableFormat.Flag.COLLAPSED, b);
      setViewFormatDirty(true);
    });
    
    return (T) this;
  }
  
  @Override
  public boolean isShowResponseDocumentsInHierarchy() {
    // NB: This method reflects Designer's UI, which is inverted from the actual storage
    return getViewFormat(false)
      .map(format ->
        !format.getAdapter(ViewTableFormat.class)
          .getFlags()
          .contains(ViewTableFormat.Flag.FLATINDEX)
      )
      .orElse(true);
  }
  
  @Override
  public T setShowResponseDocumentsInHierarchy(boolean b) {
    getViewFormat(true)
    .ifPresent((viewFormat) -> {
      viewFormat.getFormat1().setFlag(ViewTableFormat.Flag.FLATINDEX, !b);
      setViewFormatDirty(true);
    });
    
    return (T) this;
  }
  
  @Override
  public boolean isShowInViewMenu() {
    return !getDocument().getAsText(NotesConstants.DESIGN_FLAGS , ' ').contains(NotesConstants.DESIGN_FLAG_NO_MENU);
  }
  
  @Override
  public T setShowInViewMenu(boolean b) {
    setFlag(NotesConstants.DESIGN_FLAG_NO_MENU, !b);
    return (T) this;
  }
  
  @Override
  public boolean isEvaluateActionsOnDocumentChange() {
    return getViewFormat(false)
      .map(format -> {
        final ViewTableFormat3 format3 = format.getAdapter(ViewTableFormat3.class);
        if (format3 == null) {
          return false;
        } else {
          return format3.getFlags().contains(ViewTableFormat3.Flag.EvaluateActionsHideWhen);
        }
      })
      .orElse(false);
  }
  
  @Override
  public T setEvaluateActionsOnDocumentChange(boolean b) {
    getViewFormat(true)
    .ifPresent((viewFormat) -> {
      ViewTableFormat3 format3 = viewFormat.getFormat3(true).get();
      format3.setFlag(ViewTableFormat3.Flag.EvaluateActionsHideWhen, b);
      setViewFormatDirty(true);
    });
    return (T) this;
  }
  
  @Override
  public boolean isCreateDocumentsAtViewLevel() {
    return getViewFormat(false)
      .map(format -> {
        final ViewTableFormat3 format3 = format.getAdapter(ViewTableFormat3.class);
        if (format3 == null) {
          return false;
        } else {
          return format3.getFlags().contains(ViewTableFormat3.Flag.AllowCreateNewDoc);
        }
      })
      .orElse(false);
  }
  
  @Override
  public T setCreateDocumentsAtViewLevel(boolean b) {
    getViewFormat(true)
    .ifPresent((viewFormat) -> {
      ViewTableFormat3 format3 = viewFormat.getFormat3(true).get();
      format3.setFlag(ViewTableFormat3.Flag.AllowCreateNewDoc, b);
      setViewFormatDirty(true);
    });
    return (T) this;
  }
  
  @Override
  public CompositeAppSettings getCompositeAppSettings() {
    return new DefaultCompositeAppSettings();
  }
  
  @Override
  public DisplaySettings getDisplaySettings() {
    return new DefaultDisplaySettings();
  }
  
  @Override
  public UnreadMarksMode getUnreadMarksMode() {
    return getTableFormat1(false)
      .map(format1 -> {
        Set<ViewTableFormat.Flag> flags = format1.getFlags();
        if(flags.contains(ViewTableFormat.Flag.DISP_ALLUNREAD)) {
          return UnreadMarksMode.ALL;
        } else if(flags.contains(ViewTableFormat.Flag.DISP_UNREADDOCS)) {
          return UnreadMarksMode.DOCUMENTS_ONLY;
        } else {
          return UnreadMarksMode.NONE;
        }
      })
      .orElse(UnreadMarksMode.NONE);
  }
  
  @Override
  public T setUnreadMarksMode(UnreadMarksMode mode) {
    getViewFormat(true)
    .ifPresent((viewFormat) -> {
      ViewTableFormat tableFormat1 = viewFormat.getFormat1();
      Set<ViewTableFormat.Flag> oldFlags = tableFormat1.getFlags();
      Set<ViewTableFormat.Flag> newFlags = new HashSet<>(oldFlags);
      
      newFlags.remove(ViewTableFormat.Flag.DISP_ALLUNREAD);
      newFlags.remove(ViewTableFormat.Flag.DISP_UNREADDOCS);
      
      if (mode==UnreadMarksMode.ALL) {
        newFlags.add(ViewTableFormat.Flag.DISP_ALLUNREAD);
      }
      else if (mode==UnreadMarksMode.DOCUMENTS_ONLY) {
        newFlags.add(ViewTableFormat.Flag.DISP_UNREADDOCS);
      }
      tableFormat1.setFlags(newFlags);
      setViewFormatDirty(true);
    });
    return (T) this;
  }
  
  @Override
  public IndexSettings getIndexSettings() {
    return new DefaultIndexSettings();
  }
  
  @Override
  public WebRenderingSettings getWebRenderingSettings() {
    return new DefaultWebRenderingSettings();
  }
  
  @Override
  public boolean isAllowDominoDataService() {
    return getWebFlags().contains(DesignConstants.WEBFLAG_NOTE_RESTAPIALLOWED);
  }
  
  @Override
  public T setAllowDominoDataService(boolean b) {
    setWebFlag(DesignConstants.WEBFLAG_NOTE_RESTAPIALLOWED, b);
    return (T) this;
  }
  
  @Override
  public Optional<String> getColumnProfileDocName() {
    String name = getDocument().getAsText(DesignConstants.VIEW_COLUMN_PROFILE_DOC, ' ');
    if(StringUtil.isEmpty(name)) {
      return Optional.empty();
    } else {
      return Optional.of(name);
    }
  }
  
  @Override
  public T setColumnProfileDocName(String name) {
    Document doc = getDocument();
    doc.replaceItemValue(DesignConstants.VIEW_COLUMN_PROFILE_DOC, EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), name);
    return (T) this;
  }
  
  @Override
  public Set<String> getUserDefinableNonFallbackColumns() {
    // TODO see if this is truly defined here, as opposed to this being just derived data from column settings
    return new LinkedHashSet<>(getDocument().getAsList(DesignConstants.VIEW_COLUMN_FORMAT_ITEM, String.class, Collections.emptyList()));
  }
  
  @Override
  public T setUserDefinableNonFallbackColumns(Collection<String> col) {
    Document doc = getDocument();
    doc.replaceItemValue(DesignConstants.VIEW_COLUMN_FORMAT_ITEM, EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), col);
    return (T) this;
  }
  
  @Override
  public String getLotusScript() {
    StringBuilder result = new StringBuilder();
    getDocument().forEachItem(NotesConstants.VIEW_SCRIPT_NAME, (item, loop) -> {
      result.append(item.get(String.class, "")); //$NON-NLS-1$
    });
    return result.toString();
  }
  
  @Override
  public String getLotusScriptGlobals() {
    StringBuilder result = new StringBuilder();
    getDocument().forEachItem(NotesConstants.VIEW_GLOBAL_SCRIPT_NAME, (item, loop) -> {
      result.append(item.get(String.class, "")); //$NON-NLS-1$
    });
    return result.toString();
  }
  
  @Override
  public Optional<String> getSingleClickTargetFrameFormula() {
    return getHtmlCodeItem().stream()
      .filter(CDTarget.class::isInstance)
      .map(CDTarget.class::cast)
      .filter(rec -> rec.getType().contains(RecordType.TARGET))
      .findFirst()
      .flatMap(target -> target.getFlags().contains(CDTarget.Flag.IS_FORMULA) ? target.getTargetFormula() : target.getTargetString());
  }
  
  @Override
  public Optional<String> getDoubleClickTargetFrameFormula() {
    return getHtmlCodeItem().stream()
      .filter(CDTarget.class::isInstance)
      .map(CDTarget.class::cast)
      .filter(rec -> rec.getType().contains(RecordType.TARGET_DBLCLK))
      .findFirst()
      .flatMap(target -> target.getFlags().contains(CDTarget.Flag.IS_FORMULA) ? target.getTargetFormula() : target.getTargetString());
  }
  
  @Override
  public Optional<String> getFormFormula() {
    Document doc = getDocument();
    if(doc.hasItem(NotesConstants.VIEW_FORM_FORMULA_ITEM)) {
      return Optional.of(doc.get(NotesConstants.VIEW_FORM_FORMULA_ITEM, String.class, "")); //$NON-NLS-1$
    } else {
      return Optional.empty();
    }
  }
  
  @Override
  public T setFormFormula(String formulaStr) {
    if (StringUtil.isEmpty(formulaStr)) {
      getDocument().removeItem(NotesConstants.VIEW_FORM_FORMULA_ITEM);
    }
    else {
      Formula formula = getDocument().getParentDatabase().getParentDominoClient().createFormula(formulaStr);
      getDocument().replaceItemValue(NotesConstants.VIEW_FORM_FORMULA_ITEM, EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), formula);
    }
    return (T) this;
  }
  
  @Override
  public Optional<String> getHelpRequestFormula() {
    Document doc = getDocument();
    if(doc.hasItem(DesignConstants.ITEM_NAME_APPHELPFORMULA)) {
      return Optional.of(doc.get(DesignConstants.ITEM_NAME_APPHELPFORMULA, String.class, "")); //$NON-NLS-1$
    } else {
      return Optional.empty();
    }
  }
  
  @Override
  public T setHelpRequestFormula(String formulaStr) {
    if (StringUtil.isEmpty(formulaStr)) {
      getDocument().removeItem(DesignConstants.ITEM_NAME_APPHELPFORMULA);
    }
    else {
      Formula formula = getDocument().getParentDatabase().getParentDominoClient().createFormula(formulaStr);
      getDocument().replaceItemValue(DesignConstants.ITEM_NAME_APPHELPFORMULA, EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), formula);
    }
    return (T) this;
  }
  
  @Override
  public Map<EventId, String> getFormulaEvents() {
    Document doc = getDocument();
    return Arrays.stream(EventId.values())
      .filter(id -> id.getItemName() != null)
      .filter(id -> doc.hasItem(id.getItemName()))
      .collect(Collectors.toMap(
        Function.identity(),
        id -> doc.get(id.getItemName(), String.class, "") //$NON-NLS-1$
      ));
  }
  
  @Override
  public boolean isCalendarFormat() {
    return getFlags().contains(NotesConstants.DESIGN_FLAG_CALENDAR_VIEW);
  }
  
  @Override
  public T setCalendarFormat(boolean b) {
    setFlag(NotesConstants.DESIGN_FLAG_CALENDAR_VIEW, b);
    return (T) this;
  }
  
  @Override
  public Optional<CalendarSettings> getCalendarSettings() {
    return getCalendarFormat(false).map(format -> new DefaultCalendarSettings(this, format));
  }

  // *******************************************************************************
  // * Internal utility methods
  // *******************************************************************************

  private synchronized Optional<DominoViewFormat> getViewFormat(boolean createIfMissing) {
    if (this.format == null) {
      final Document doc = this.getDocument();
      
      if (doc.hasItem(DesignConstants.VIEW_VIEW_FORMAT_ITEM)) {
        this.format = (DominoViewFormat) doc.getItemValue(DesignConstants.VIEW_VIEW_FORMAT_ITEM).get(0);
      }
      
      if (this.format==null) {
        this.format = new DominoViewFormat(doc);
      }
    }
    return Optional.ofNullable(this.format);
  }
  
  private synchronized Optional<DominoCalendarFormat> getCalendarFormat(boolean createIfMissing) {
    if(this.calendarFormat == null) {
      final Document doc = this.getDocument();
      
      if(doc.hasItem(NotesConstants.VIEW_CALENDAR_FORMAT_ITEM)) {
        this.calendarFormat = (DominoCalendarFormat) doc.getItemValue(NotesConstants.VIEW_CALENDAR_FORMAT_ITEM).get(0);
      }
      
      if (this.calendarFormat==null && createIfMissing) {
        this.calendarFormat = new DominoCalendarFormat();
      }
    }
    return Optional.ofNullable(this.calendarFormat);
  }
  
  private Optional<ViewTableFormat> getTableFormat1(boolean createIfMissing) {
    return getViewFormat(createIfMissing).map(DominoViewFormat::getFormat1);
  }

  private Optional<ViewTableFormat2> getTableFormat2(boolean createIfMissing) {
    return getViewFormat(createIfMissing).flatMap((fmt) -> { return fmt.getFormat2(createIfMissing);});
  }

  private Optional<ViewTableFormat3> getTableFormat3(boolean createIfMissing) {
    return getViewFormat(createIfMissing).flatMap((fmt) -> { return fmt.getFormat3(createIfMissing);});
  }

  private Optional<ViewTableFormat4> getTableFormat4(boolean createIfMissing) {
    return getViewFormat(createIfMissing).flatMap((fmt) -> { return fmt.getFormat4(createIfMissing);});
  }

  private Set<String> getIndexDispositionOptions() {
    String index = getDocument().getAsText(DesignConstants.VIEW_INDEX_ITEM, '/');
    return Arrays
        .asList(index.split("/")) //$NON-NLS-1$
        .stream()
        .filter(JNXStringUtil::isNotEmpty)
        .collect(Collectors.toSet());
  }
  
  private T setIndexDispositionOptions(Collection<String> options) {
    String indexStr;
    if (options.isEmpty()) {
      indexStr = ""; //$NON-NLS-1$
    }
    else {
      //prepend delimiter character; order of the options is not relevant
      indexStr = "/" + options //$NON-NLS-1$
          .stream()
          .filter(JNXStringUtil::isNotEmpty)
          .collect(Collectors.joining("/")); //$NON-NLS-1$
    }
    
    getDocument().replaceItemValue(DesignConstants.VIEW_INDEX_ITEM,
        EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), indexStr);
    return (T) this;
  }
  
  protected RichTextRecordList getHtmlCodeItem() {
    return getDocument().getRichTextItem(DesignConstants.ITEM_NAME_HTMLCODE);
  }
  
  public boolean isViewFormatDirty() {
    if (this.format!=null) {
      return this.format.isDirty();
    }
    else {
      return false;
    }
  }
  
  public void setViewFormatDirty(boolean b) {
    if (this.format!=null) {
      this.format.setDirty(b);
    }
  }
  
  public boolean isCalendarFormatDirty() {
    return this.calendarFormatDirty;
  }
  
  public void setCalendarFormatDirty(boolean b) {
    this.calendarFormatDirty = b;
  }
  
  @Override
  public boolean save() {
    if (isViewFormatDirty()) {
      if (this.format!=null) {
        final Document doc = this.getDocument();
        doc.replaceItemValue(DesignConstants.VIEW_VIEW_FORMAT_ITEM,
            EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY),
            this.format);
        
        List<DominoCollationInfo> collations = Objects.requireNonNull(CollationEncoder.newCollationFromViewFormat(this.format));
        if (collations.isEmpty()) {
          throw new IllegalStateException("CollationEncoder returned empty list");
        }
        
        //remove old collations
        doc.removeItem(DesignConstants.VIEW_COLLATION_ITEM);
        int idx = 1;
        while (doc.hasItem(DesignConstants.VIEW_COLLATION_ITEM+Integer.toString(idx))) {
          doc.removeItem(DesignConstants.VIEW_COLLATION_ITEM+Integer.toString(idx));
          idx++;
        }
        
        //write new collations
        doc.replaceItemValue(DesignConstants.VIEW_COLLATION_ITEM,
            EnumSet.of(ItemFlag.SUMMARY), collations.get(0));
        
        if (collations.size()>1) {
          for (int i=1; i<collations.size(); i++) {
            DominoCollationInfo currCollation = collations.get(i);
            doc.replaceItemValue(DesignConstants.VIEW_COLLATION_ITEM+Integer.toString(i),
                EnumSet.of(ItemFlag.SUMMARY), currCollation);
          }
        }

        //rebuild $FORMULA item
        {
          LinkedHashMap<String,String> columnItemNamesAndFormulas = new LinkedHashMap<String, String>();
          for (CollectionColumn currCol : this.format.getColumns()) {
            String currItemName = currCol.getItemName();
            String currFormula = currCol.getFormula();
            columnItemNamesAndFormulas.put(currItemName, currFormula);
          }

          String selectionFormula = getSelectionFormula();
          if (StringUtil.isEmpty(selectionFormula)) {
            selectionFormula = "SELECT @All"; //$NON-NLS-1$
          }
          
          //add additional columns for $Conflict and $REF if required for the collection
          
          //TODO find out why this is not working (Document has invalid structure)
          boolean addConflict = false; //this.format.getFormat1().getFlags().contains(Flag.CONFLICT);
          boolean isShowResponseHierarchy = false; // isShowResponseDocumentsInHierarchy();
          
          byte[] formulaItemData = FormulaCompiler.get().compile(selectionFormula, columnItemNamesAndFormulas, addConflict, isShowResponseHierarchy);
          ByteBuffer formulaItemDataBuf = ByteBuffer.allocate(formulaItemData.length + 2).order(ByteOrder.nativeOrder());
          formulaItemDataBuf.putShort(ItemDataType.TYPE_FORMULA.getValue());
          formulaItemDataBuf.put(formulaItemData);
          
          formulaItemDataBuf.position(0);
          
          doc.replaceItemValue(DesignConstants.VIEW_FORMULA_ITEM, EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), formulaItemDataBuf);
          doc.getFirstItem(DesignConstants.VIEW_FORMULA_ITEM).get().setSummary(true);
        }

        doc.sign();
      }
      
      setViewFormatDirty(false);
    }
    
    if (isCalendarFormatDirty()) {
      if (this.calendarFormat!=null) {
        final Document doc = this.getDocument();
        doc.replaceItemValue(NotesConstants.VIEW_CALENDAR_FORMAT_ITEM,
            EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY),
            this.calendarFormat);
        
      }
      setCalendarFormatDirty(false);
    }
    
    return super.save();
  }
  
  public String getSelectionFormula() {
    return "";
  }
  
  @Override
  public String toString() {
    return MessageFormat.format("{0} [title={1}, columns={2}]", getClass().getSimpleName(), getTitle(), getColumns());
  }
  
  private class DefaultCompositeAppSettings implements CompositeAppSettings {

    @Override
    public boolean isHideColumnHeader() {
      return getTableFormat3(false)
          .map(format3 -> format3.getFlags().contains(ViewTableFormat3.Flag.HideColumnHeader))
          .orElse(false);
    }

    @Override
    public CompositeAppSettings setHideColumnHeader(boolean b) {
      getTableFormat3(true)
      .ifPresent((fmt3) -> {
        fmt3.setFlag(ViewTableFormat3.Flag.HideColumnHeader, b);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public boolean isShowPartialHierarchies() {
      return getTableFormat1(false)
        .map(format -> format.getFlags2().contains(ViewTableFormat.Flag2.SHOW_PARTIAL_THREADS))
        .orElse(false);
    }

    @Override
    public CompositeAppSettings setShowPartialHierarchies(boolean b) {
      getTableFormat1(true)
      .ifPresent((fmt1) -> {
        fmt1.setFlag(ViewTableFormat.Flag2.SHOW_PARTIAL_THREADS, b);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public boolean isShowSwitcher() {
      return getTableFormat3(false)
          .map(format3 -> format3.getFlags().contains(ViewTableFormat3.Flag.ShowVerticalHorizontalSwitcher))
          .orElse(false);
    }

    @Override
    public CompositeAppSettings setShowSwitcher(boolean b) {
      getTableFormat3(true)
      .ifPresent((fmt3) -> {
        fmt3.setFlag(ViewTableFormat3.Flag.ShowVerticalHorizontalSwitcher, b);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public boolean isShowTabNavigator() {
      return getTableFormat3(false)
        .map(format3 -> format3.getFlags().contains(ViewTableFormat3.Flag.ShowTabNavigator))
        .orElse(false);
    }

    @Override
    public CompositeAppSettings setShowTabNavigator(boolean b) {
      getTableFormat3(true)
      .ifPresent((fmt3) -> {
        fmt3.setFlag(ViewTableFormat3.Flag.ShowTabNavigator, b);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public String getViewers() {
      return getDocument().getAsText(DesignConstants.VIEW_VIEWERS_ITEM, ' ');
    }

    @Override
    public CompositeAppSettings setViewers(String s) {
      getDocument().replaceItemValue(DesignConstants.VIEW_VIEWERS_ITEM, s);
      return this;
    }
    
    @Override
    public String getThreadView() {
      return getDocument().getAsText(DesignConstants.VIEW_THREADVIEW_ITEM, ' ');
    }

    @Override
    public CompositeAppSettings setThreadView(String s) {
      getDocument().replaceItemValue(DesignConstants.VIEW_THREADVIEW_ITEM, s);
      return this;
    }
    
    @Override
    public boolean isAllowConversationMode() {
      return getTableFormat1(false)
        .map(format -> format.getFlags2().contains(ViewTableFormat.Flag2.PARTIAL_FLATINDEX))
        .orElse(false);
    }
    
    @Override
    public CompositeAppSettings setAllowConversationMode(boolean b) {
      getTableFormat1(true)
      .ifPresent((fmt1) -> {
        fmt1.setFlag(ViewTableFormat.Flag2.PARTIAL_FLATINDEX, b);
        setViewFormatDirty(true);
      });
      return this;
    }
    
  }
  
  private class DefaultDisplaySettings implements DisplaySettings {

    @Override
    public ColorValue getBackgroundColor() {
      // TODO investigate pre-V5 background colors
      return getTableFormat3(false)
        .map(ViewTableFormat3::getBackgroundColor)
        .orElseGet(DesignColorsAndFonts::whiteColor);
    }

    @Override
    public DisplaySettings setBackgroundColor(ColorValue color) {
      getTableFormat3(true)
      .ifPresent((fmt3) -> {
        fmt3.getBackgroundColor().copyFrom(color);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public ColorValue getAlternateRowColor() {
      // TODO investigate pre-V5 background colors
      return getTableFormat3(false)
        .map(ViewTableFormat3::getAlternateBackgroundColor)
        .orElseGet(DesignColorsAndFonts::noColor);
    }
    
    @Override
    public DisplaySettings setAlternateRowColor(ColorValue color) {
      getTableFormat3(true)
      .ifPresent((fmt3) -> {
        fmt3.getAlternateBackgroundColor().copyFrom(color);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public boolean isUseAlternateRowColor() {
      return getTableFormat1(false)
        .map(format -> format.getFlags().contains(ViewTableFormat.Flag.ALTERNATE_ROW_COLORING))
        .orElse(false);
    }

    @Override
    public DisplaySettings setUseAlternateRowColor(boolean b) {
      getTableFormat1(true)
      .ifPresent((fmt1) -> {
        fmt1.setFlag(ViewTableFormat.Flag.ALTERNATE_ROW_COLORING, b);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public Optional<CDResource> getBackgroundImage() {
      return getViewFormat(false)
        .flatMap(format -> Optional.ofNullable(format.getBackgroundResource()));
    }

    @Override
    public DisplaySettings setBackgroundImage(CDResource resource) {
      getViewFormat(true)
      .ifPresent((viewFormat) -> {
        viewFormat.setBackgroundResource(resource);
        setViewFormatDirty(true);
      });
      return this;
    }

    @Override
    public DisplaySettings setBackgroundImageName(String name) {
      setBackgroundImage(CDResource.newSharedImageByName(name));
      return this;
    }

    @Override
    public DisplaySettings clearBackgroundImage() {
      setBackgroundImage(null);
      return this;
    }
    
    @Override
    public ImageRepeatMode getBackgroundImageRepeatMode() {
      return getTableFormat4(false)
        .map(ViewTableFormat4::getRepeatType)
        .orElse(ImageRepeatMode.ONCE);
    }

    @Override
    public DisplaySettings setBackgroundRepeatMode(ImageRepeatMode mode) {
      getTableFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.setRepeatType(mode);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public GridStyle getGridStyle() {
      Optional<ViewTableFormat3> format3 = getTableFormat3(false);
      if(!format3.isPresent()) {
        return GridStyle.NONE;
      }
      Set<ViewTableFormat3.Flag> flags = format3.get().getFlags();
      if(flags.contains(ViewTableFormat3.Flag.GridStyleSolid)) {
        return GridStyle.SOLID;
      } else if(flags.contains(ViewTableFormat3.Flag.GridStyleDash)) {
        return GridStyle.DASHED;
      } else if(flags.contains(ViewTableFormat3.Flag.GridStyleDot)) {
        return GridStyle.DOTS;
      } else if(flags.contains(ViewTableFormat3.Flag.GridStyleDashDot)) {
        return GridStyle.DASHES_AND_DOTS;
      } else {
        return GridStyle.NONE;
      }
    }

    @Override
    public DisplaySettings setGridStyle(GridStyle style) {
      getTableFormat3(true)
      .ifPresent((fmt3) -> {
        Set<ViewTableFormat3.Flag> oldFlags = fmt3.getFlags();
        Set<ViewTableFormat3.Flag> newFlags = new HashSet<>(oldFlags);
        
        newFlags.remove(ViewTableFormat3.Flag.GridStyleSolid);
        newFlags.remove(ViewTableFormat3.Flag.GridStyleDash);
        newFlags.remove(ViewTableFormat3.Flag.GridStyleDot);
        newFlags.remove(ViewTableFormat3.Flag.GridStyleDashDot);
        
        if (style == GridStyle.SOLID) {
          newFlags.add(ViewTableFormat3.Flag.GridStyleSolid);
        }
        else if (style == GridStyle.DASHED) {
          newFlags.add(ViewTableFormat3.Flag.GridStyleDash);
        }
        else if (style == GridStyle.DOTS) {
          newFlags.add(ViewTableFormat3.Flag.GridStyleDot);
        }
        else if (style == GridStyle.DASHES_AND_DOTS) {
          newFlags.add(ViewTableFormat3.Flag.GridStyleDashDot);
        }
        
        fmt3.setFlags(newFlags);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public ColorValue getGridColor() {
      return getTableFormat3(false)
        .map(ViewTableFormat3::getGridColor)
        .orElseGet(DesignColorsAndFonts::noColor);
    }

    @Override
    public DisplaySettings setGridColor(ColorValue color) {
      getTableFormat3(true)
      .ifPresent((fmt3) -> {
        fmt3.getGridColor().copyFrom(color);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public HeaderStyle getHeaderStyle() {
      return getTableFormat1(false)
        .map(format -> {
          Set<ViewTableFormat.Flag> flags = format.getFlags();
          Set<ViewTableFormat.Flag2> flags2 = format.getFlags2();
          if(flags.contains(ViewTableFormat.Flag.SIMPLE_HEADINGS)) {
            return HeaderStyle.SIMPLE;
          } else if(flags.contains(ViewTableFormat.Flag.HIDE_HEADINGS)) {
            return HeaderStyle.NONE;
          } else if(flags2.contains(ViewTableFormat.Flag2.FLAT_HEADINGS)) {
            return HeaderStyle.FLAT;
          } else {
            return HeaderStyle.BEVELED;
          }
        })
        .orElse(HeaderStyle.BEVELED);
    }

    @Override
    public DisplaySettings setHeaderStyle(HeaderStyle style) {
      getTableFormat1(true)
      .ifPresent((fmt1) -> {
        Set<ViewTableFormat.Flag> oldFlags = fmt1.getFlags();
        Set<ViewTableFormat.Flag> newFlags = new HashSet<>(oldFlags);
        
        Set<ViewTableFormat.Flag2> oldFlags2 = fmt1.getFlags2();
        Set<ViewTableFormat.Flag2> newFlags2 = new HashSet<>(oldFlags2);

        newFlags.remove(ViewTableFormat.Flag.SIMPLE_HEADINGS);
        newFlags.remove(ViewTableFormat.Flag.HIDE_HEADINGS);
        newFlags2.remove(ViewTableFormat.Flag2.FLAT_HEADINGS);
        
        if (style == HeaderStyle.SIMPLE) {
          newFlags.add(ViewTableFormat.Flag.SIMPLE_HEADINGS);
        }
        else if (style == HeaderStyle.NONE) {
          newFlags.add(ViewTableFormat.Flag.HIDE_HEADINGS);
        }
        else if (style == HeaderStyle.FLAT) {
          newFlags2.add(ViewTableFormat.Flag2.FLAT_HEADINGS);
        }
        
        fmt1.setFlags(newFlags);
        fmt1.setFlags2(newFlags2);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public ColorValue getHeaderColor() {
      return getTableFormat3(false)
        .map(ViewTableFormat3::getHeaderBackgroundColor)
        .orElseGet(DesignColorsAndFonts::noColor);
    }

    @Override
    public DisplaySettings setHeaderColor(ColorValue color) {
      getTableFormat3(true)
      .ifPresent((fmt3) -> {
        fmt3.getHeaderBackgroundColor().copyFrom(color);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public int getHeaderLines() {
      return getTableFormat2(false)
        .map(ViewTableFormat2::getHeaderLineCount)
        .orElse((short)1);
    }

    @Override
    public DisplaySettings setHeaderLines(int lines) {
      getTableFormat2(true)
      .ifPresent((fmt2) -> {
        fmt2.setHeaderLineCount((short) lines);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public int getRowLines() {
      return getTableFormat2(false)
        .map(ViewTableFormat2::getLineCount)
        .orElse((short)1);
    }

    @Override
    public DisplaySettings setRowLines(int lines) {
      getTableFormat2(true)
      .ifPresent((fmt2) -> {
        fmt2.setLineCount((short) lines);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public ViewLineSpacing getLineSpacing() {
      return getTableFormat2(false)
        .map(ViewTableFormat2::getSpacing)
        .orElse(ViewLineSpacing.SINGLE_SPACE);
    }

    @Override
    public DisplaySettings setLineSpacing(ViewLineSpacing spacing) {
      getTableFormat2(true)
      .ifPresent((fmt2) -> {
        fmt2.setSpacing(spacing);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public boolean isShrinkRowsToContent() {
      return getTableFormat1(false)
        .map(ViewTableFormat::getFlags)
        .map(flags -> flags.contains(ViewTableFormat.Flag.VARIABLE_LINE_COUNT))
        .orElse(false);
    }

    @Override
    public DisplaySettings setShrinkRowsToContent(boolean b) {
      getTableFormat1(true)
      .ifPresent((fmt1) -> {
        fmt1.setFlag(ViewTableFormat.Flag.VARIABLE_LINE_COUNT, b);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public boolean isHideEmptyCategories() {
      return getIndexDispositionOptions().contains(DesignConstants.INDEXDISPOSITION_HIDEEMPTYCATEGORIES);
    }

    @Override
    public DisplaySettings setHideEmptyCategories(boolean b) {
      Set<String> oldOptions = getIndexDispositionOptions();
      Set<String> newOptions = new HashSet<>(oldOptions);
      
      if (b) {
        if (!newOptions.contains(DesignConstants.INDEXDISPOSITION_HIDEEMPTYCATEGORIES)) {
          newOptions.add(DesignConstants.INDEXDISPOSITION_HIDEEMPTYCATEGORIES);
          setIndexDispositionOptions(newOptions);
        }
      }
      else {
        if (newOptions.contains(DesignConstants.INDEXDISPOSITION_HIDEEMPTYCATEGORIES)) {
          newOptions.remove(DesignConstants.INDEXDISPOSITION_HIDEEMPTYCATEGORIES);
          setIndexDispositionOptions(newOptions);
        }
      }
      return this;
    }
    
    @Override
    public boolean isShowConflicts() {
      return getTableFormat1(false)
          .map(ViewTableFormat::getFlags)
          .map(flags -> flags.contains(ViewTableFormat.Flag.CONFLICT))
          .orElse(false);
    }

    @Override
    public DisplaySettings setShowConflicts(boolean b) {
      getTableFormat1(true)
      .ifPresent((fmt1) -> {
        fmt1.setFlag(ViewTableFormat.Flag.CONFLICT, b);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public boolean isColorizeViewIcons() {
      return getTableFormat1(false)
        .map(ViewTableFormat::getFlags2)
        .map(flags -> flags.contains(ViewTableFormat.Flag2.COLORIZE_ICONS))
        .orElse(false);
    }

    @Override
    public DisplaySettings setColorizeViewIcons(boolean b) {
      getTableFormat1(true)
      .ifPresent((fmt1) -> {
        fmt1.setFlag(ViewTableFormat.Flag2.COLORIZE_ICONS, b);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public ColorValue getUnreadColor() {
      return getTableFormat3(false)
        .map(ViewTableFormat3::getUnreadColor)
        .orElseGet(DesignColorsAndFonts::blackColor);
    }

    @Override
    public DisplaySettings setUnreadColor(ColorValue color) {
      getTableFormat3(true)
      .ifPresent((fmt3) -> {
        fmt3.getUnreadColor().copyFrom(color);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public boolean isUnreadBold() {
      return getTableFormat3(false)
          .map(format -> {
            Set<ViewTableFormat3.Flag> flags = format.getFlags();
            return flags.contains(ViewTableFormat3.Flag.BoldUnreadRows);
          })
          .orElse(false);
    }

    @Override
    public DisplaySettings setUnreadBold(boolean b) {
      getTableFormat3(true)
      .ifPresent((fmt3) -> {
        fmt3.setFlag(ViewTableFormat3.Flag.BoldUnreadRows, b);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public ColorValue getColumnTotalColor() {
      return getTableFormat3(false)
          .map(ViewTableFormat3::getTotalsColor)
          .orElseGet(DesignColorsAndFonts::blackColor);
    }

    @Override
    public DisplaySettings setColumnTotalColor(ColorValue color) {
      getTableFormat3(true)
      .ifPresent((fmt3) -> {
        fmt3.getTotalsColor().copyFrom(color);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public boolean isShowSelectionMargin() {
      return getTableFormat1(false)
        .map(ViewTableFormat::getFlags)
        .map(flags -> !flags.contains(ViewTableFormat.Flag.HIDE_LEFT_MARGIN))
        .orElse(true);
    }

    @Override
    public DisplaySettings setShowSelectionMargin(boolean b) {
      getTableFormat1(true)
      .ifPresent((fmt1) -> {
        fmt1.setFlag(ViewTableFormat.Flag.HIDE_LEFT_MARGIN, !b);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public boolean isHideSelectionMarginBorder() {
      return getTableFormat3(false)
        .map(ViewTableFormat3::getFlags)
        .map(flags -> flags.contains(ViewTableFormat3.Flag.HideLeftMarginBorder))
        .orElse(false);
    }

    @Override
    public DisplaySettings setHideSelectionMarginBorder(boolean b) {
      getTableFormat3(true)
      .ifPresent((fmt3) -> {
        fmt3.setFlag(ViewTableFormat3.Flag.HideLeftMarginBorder, b);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public boolean isExtendLastColumnToWindowWidth() {
      return getTableFormat1(false)
        .map(ViewTableFormat::getFlags)
        .map(flags -> flags.contains(ViewTableFormat.Flag.EXTEND_LAST_COLUMN))
        .orElse(true);
    }

    @Override
    public DisplaySettings setExtendLastColumnToWindowWidth(boolean b) {
      getTableFormat1(true)
      .ifPresent((fmt1) -> {
        fmt1.setFlag(ViewTableFormat.Flag.EXTEND_LAST_COLUMN, b);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public EdgeWidths getMargin() {
      return new LambdaEdgeWidths(
        () -> getTableFormat3(false).map(ViewTableFormat3::getViewMarginTop).orElse(0),
        () -> getTableFormat3(false).map(ViewTableFormat3::getViewMarginLeft).orElse(0),
        () -> getTableFormat3(false).map(ViewTableFormat3::getViewMarginRight).orElse(0),
        () -> getTableFormat3(false).map(ViewTableFormat3::getViewMarginBottom).orElse(0)
      );
    }

    @Override
    public DisplaySettings setMargin(EdgeWidths w) {
      getTableFormat3(true)
      .ifPresent((fmt3) -> {
        fmt3.setViewMarginTop(w.getTop());
        fmt3.setViewMarginBottom(w.getBottom());
        fmt3.setViewMarginLeft(w.getLeft());
        fmt3.setViewMarginRight(w.getRight());
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public int getBelowHeaderMargin() {
      return getTableFormat3(false)
        .map(ViewTableFormat3::getViewMarginTopUnder)
        .orElse(0);
    }

    @Override
    public DisplaySettings setBelowHeaderMargin(int val) {
      getTableFormat3(true)
      .ifPresent((fmt3) -> {
        fmt3.setViewMarginTopUnder(val);
        setViewFormatDirty(true);
      });
      return this;
    }
    
    @Override
    public ColorValue getMarginColor() {
      return getTableFormat3(false)
        .map(ViewTableFormat3::getMarginBackgroundColor)
        .orElseGet(DesignColorsAndFonts::whiteColor);
    }
    
    @Override
    public DisplaySettings setMarginColor(ColorValue color) {
      getTableFormat3(true)
      .ifPresent((fmt3) -> {
        fmt3.getMarginBackgroundColor().copyFrom(color);
        setViewFormatDirty(true);
      });
      return this;
    }
    
  }
  
  private class DefaultIndexSettings implements IndexSettings {
    
    private boolean isNoIndexRefreshOption(String option) {
      return !isIndexRefreshOption(option);
    }
    
    private boolean isIndexRefreshOption(String option) {
      if (DesignConstants.INDEXDISPOSITION_REFRESHMANUAL.equals(option) ||
          DesignConstants.INDEXDISPOSITION_REFRESHAUTO.equals(option) ||
          option.startsWith(DesignConstants.INDEXDISPOSITION_REFRESHAUTOATMOST+"=")) { //$NON-NLS-1$
        return true;
      }
      else {
        return false;
      }
    }

    private boolean isDiscardOption(String option) {
      if (DesignConstants.INDEXDISPOSITION_DISCARDEACHUSE.equals(option) ||
          option.startsWith(DesignConstants.INDEXDISPOSITION_DISCARDINACTIVEFOR+"=")) {
        return true;
      }
      else {
        return false;
      }
    }
    
    private boolean isNoDiscardOption(String option) {
      return !isDiscardOption(option);
    }
    
    @Override
    public IndexRefreshMode getRefreshMode() {
      Set<String> indexOptions = getIndexDispositionOptions();
      if(indexOptions.contains(DesignConstants.INDEXDISPOSITION_REFRESHMANUAL)) {
        return IndexRefreshMode.MANUAL;
      } else if(indexOptions.contains(DesignConstants.INDEXDISPOSITION_REFRESHAUTO)) {
        return IndexRefreshMode.AUTO;
      } else if(indexOptions.stream().anyMatch(o -> o.startsWith(DesignConstants.INDEXDISPOSITION_REFRESHAUTOATMOST))) {
        return IndexRefreshMode.AUTO_AT_MOST_EVERY;
      } else {
        return IndexRefreshMode.AUTO_AFTER_FIRST_USE;
      }
    }

    @Override
    public IndexSettings setRefreshMode(IndexRefreshMode mode) {
      if (mode == IndexRefreshMode.AUTO_AT_MOST_EVERY) {
        return setRefreshMaxIntervalSeconds(12 * 60 * 60); //Designer default: 12 hours
      }
      else {
        Set<String> newOptions = new HashSet<>(
            getIndexDispositionOptions()
            .stream()
            .filter(this::isNoIndexRefreshOption)
            .collect(Collectors.toSet()));
        
        if (mode==IndexRefreshMode.AUTO) {
          newOptions.add(DesignConstants.INDEXDISPOSITION_REFRESHAUTO);
        }
        else if (mode==IndexRefreshMode.MANUAL) {
          newOptions.add(DesignConstants.INDEXDISPOSITION_REFRESHMANUAL);
        }
        
        setIndexDispositionOptions(newOptions);
      }
      return this;
    }
    
    @Override
    public OptionalInt getRefreshMaxIntervalSeconds() {
      Set<String> indexOptions = getIndexDispositionOptions();
      for(String opt : indexOptions) {
        if(opt.startsWith(DesignConstants.INDEXDISPOSITION_REFRESHAUTOATMOST)) {
          return OptionalInt.of(Integer.parseInt(opt.substring(2)));
        }
      }
      return OptionalInt.empty();
    }

    @Override
    public IndexSettings setRefreshMaxIntervalSeconds(int seconds) {
      Set<String> newOptions = new HashSet<>(
          getIndexDispositionOptions()
          .stream()
          .filter(this::isNoIndexRefreshOption)
          .collect(Collectors.toSet()));
      newOptions.add(DesignConstants.INDEXDISPOSITION_REFRESHAUTOATMOST + "=" + Integer.toString(seconds)); //$NON-NLS-1$
      setIndexDispositionOptions(newOptions);
      return this;
    }
    
    @Override
    public IndexDiscardMode getDiscardMode() {
      Set<String> indexOptions = getIndexDispositionOptions();
      if(indexOptions.contains(DesignConstants.INDEXDISPOSITION_DISCARDEACHUSE)) {
        return IndexDiscardMode.AFTER_EACH_USE;
      } else if(indexOptions.stream().anyMatch(o -> o.startsWith(DesignConstants.INDEXDISPOSITION_DISCARDINACTIVEFOR))) {
        return IndexDiscardMode.INACTIVE_FOR;
      } else {
        return IndexDiscardMode.INACTIVE_45_DAYS;
      }
    }

    @Override
    public IndexSettings setDiscardMode(IndexDiscardMode mode) {
      if (mode==IndexDiscardMode.INACTIVE_FOR) {
        return setDiscardAfterHours(7 * 24); //Designer default: 7 days
      }
      else {
        Set<String> newOptions = new HashSet<>(
            getIndexDispositionOptions()
            .stream()
            .filter(this::isNoDiscardOption)
            .collect(Collectors.toSet()));
        
        if (mode == IndexDiscardMode.AFTER_EACH_USE) {
          newOptions.add(DesignConstants.INDEXDISPOSITION_DISCARDEACHUSE);
        }

        setIndexDispositionOptions(newOptions);
      }
      return this;
    }
    
    @Override
    public OptionalInt getDiscardAfterHours() {
      Set<String> indexOptions = getIndexDispositionOptions();
      for(String opt : indexOptions) {
        if(opt.startsWith(DesignConstants.INDEXDISPOSITION_DISCARDINACTIVEFOR)) {
          return OptionalInt.of(Integer.parseInt(opt.substring(2)));
        }
      }
      return OptionalInt.empty();
    }

    @Override
    public IndexSettings setDiscardAfterHours(int hours) {
      Set<String> newOptions = new HashSet<>(
          getIndexDispositionOptions()
          .stream()
          .filter(this::isNoDiscardOption)
          .collect(Collectors.toSet()));
      newOptions.add(DesignConstants.INDEXDISPOSITION_DISCARDINACTIVEFOR + "=" + Integer.toString(hours)); //$NON-NLS-1$
      setIndexDispositionOptions(newOptions);
      return this;
    }
    
    @Override
    public boolean isRestrictInitialBuildToDesigner() {
      return getIndexDispositionOptions().contains(DesignConstants.INDEXDISPOSITION_RESTRICTTODESIGNER);
    }

    @Override
    public IndexSettings setRestrictInitialBuildToDesigner(boolean b) {
      Set<String> oldOptions = getIndexDispositionOptions();
      Set<String> newOptions = new HashSet<>(oldOptions);
      
      if (b) {
        if (!newOptions.contains(DesignConstants.INDEXDISPOSITION_RESTRICTTODESIGNER)) {
          newOptions.add(DesignConstants.INDEXDISPOSITION_RESTRICTTODESIGNER);
          setIndexDispositionOptions(newOptions);
        }
      }
      else {
        if (newOptions.contains(DesignConstants.INDEXDISPOSITION_RESTRICTTODESIGNER)) {
          newOptions.remove(DesignConstants.INDEXDISPOSITION_RESTRICTTODESIGNER);
          setIndexDispositionOptions(newOptions);
        }
      }
      return this;
    }
    
    @Override
    public boolean isGenerateUniqueKeysInIndex() {
      // TODO see if this is actually specified primarily here, as the rest of the item is derived
      // I suspect this is stored in $Collation only, in the COLLATION structure
      List<?> collationValues = getDocument().getItemValue(DesignConstants.VIEW_COLLATION_ITEM);
      if (!collationValues.isEmpty()) {
        DominoCollationInfo collation = (DominoCollationInfo) collationValues.get(0);
        return collation.isUnique();
      }
      
      return false;
    }

    @Override
    public boolean isIncludeUpdatesInTransactionLog() {
      String logUpdates = getDocument().get(DesignConstants.FIELD_LOGVIEWUPDATES, String.class, null);
      return DesignConstants.FIELD_LOGVIEWUPDATES_ENABLED.equals(logUpdates);
    }
    
    @Override
    public IndexSettings setIncludeUpdatesInTransactionLog(boolean b) {
      if (b) {
        getDocument().replaceItemValue(DesignConstants.FIELD_LOGVIEWUPDATES, "1"); //$NON-NLS-1$
      }
      else {
        getDocument().removeItem(DesignConstants.FIELD_LOGVIEWUPDATES);
      }
      return this;
    }
    
  }
  
  private class DefaultWebRenderingSettings implements WebRenderingSettings {

    @Override
    public boolean isTreatAsHtml() {
      return getTableFormat2(false)
        .map(ViewTableFormat2::getFlags)
        .map(flags -> flags.contains(ViewTableFormat2.Flag.HTML_PASSTHRU))
        .orElse(false);
    }

    @Override
    public WebRenderingSettings setTreatAsHtml(boolean b) {
      getTableFormat2(true)
      .ifPresent((fmt2) -> {
        fmt2.setFlag(ViewTableFormat2.Flag.HTML_PASSTHRU, b);
      });
      return this;
    }
    
    @Override
    public boolean isUseJavaApplet() {
      return getWebFlags().contains(DesignConstants.WEBFLAG_NOTE_USEAPPLET_INBROWSER);
    }

    @Override
    public WebRenderingSettings setUseJavaApplet(boolean b) {
      setWebFlag(DesignConstants.WEBFLAG_NOTE_USEAPPLET_INBROWSER, b);
      return this;
    }
    
    @Override
    public boolean isAllowSelection() {
      return getWebFlags().contains(DesignConstants.WEBFLAG_NOTE_ALLOW_DOC_SELECTIONS);
    }

    @Override
    public WebRenderingSettings setAllowSelection(boolean b) {
      setWebFlag(DesignConstants.WEBFLAG_NOTE_ALLOW_DOC_SELECTIONS, b);
      return this;
    }
    
    @Override
    public ColorValue getActiveLinkColor() {
      Document doc = getDocument();
      if(!doc.hasItem(DesignConstants.ITEM_NAME_HTMLCODE)) {
        return DesignColorsAndFonts.defaultActiveLink();
      }
      return getDocument().getRichTextItem(DesignConstants.ITEM_NAME_HTMLCODE)
        .stream()
        .filter(CDLinkColors.class::isInstance)
        .map(CDLinkColors.class::cast)
        .findFirst()
        .map(CDLinkColors::getActiveColor)
        .orElseGet(DesignColorsAndFonts::defaultActiveLink);
    }

    @Override
    public ColorValue getUnvisitedLinkColor() {
      Document doc = getDocument();
      if(!doc.hasItem(DesignConstants.ITEM_NAME_HTMLCODE)) {
        return DesignColorsAndFonts.defaultUnvisitedLink();
      }
      return getDocument().getRichTextItem(DesignConstants.ITEM_NAME_HTMLCODE)
        .stream()
        .filter(CDLinkColors.class::isInstance)
        .map(CDLinkColors.class::cast)
        .findFirst()
        .map(CDLinkColors::getUnvisitedColor)
        .orElseGet(DesignColorsAndFonts::defaultUnvisitedLink);
    }

    @Override
    public ColorValue getVisitedLinkColor() {
      Document doc = getDocument();
      if(!doc.hasItem(DesignConstants.ITEM_NAME_HTMLCODE)) {
        return DesignColorsAndFonts.defaultVisitedLink();
      }
      return getDocument().getRichTextItem(DesignConstants.ITEM_NAME_HTMLCODE)
        .stream()
        .filter(CDLinkColors.class::isInstance)
        .map(CDLinkColors.class::cast)
        .findFirst()
        .map(CDLinkColors::getVisitedColor)
        .orElseGet(DesignColorsAndFonts::defaultVisitedLink);
    }

    @Override
    public boolean isAllowWebCrawlerIndexing() {
      return getWebFlags().contains(DesignConstants.WEBFLAG_NOTE_CRAWLABLE);
    }
    
    @Override
    public WebRenderingSettings setAllowWebCrawlerIndexing(boolean b) {
      setWebFlag(DesignConstants.WEBFLAG_NOTE_CRAWLABLE, b);
      return this;
    }
    
  }
}
