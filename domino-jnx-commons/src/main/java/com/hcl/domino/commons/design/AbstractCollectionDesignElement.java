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
package com.hcl.domino.commons.design;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import com.hcl.domino.commons.NotYetImplementedException;
import com.hcl.domino.commons.design.view.DominoViewFormat;
import com.hcl.domino.commons.views.NotesCollationInfo;
import com.hcl.domino.data.CollectionColumn;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.design.CollectionDesignElement;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.DesignElement;
import com.hcl.domino.design.EdgeWidths;
import com.hcl.domino.design.ImageRepeatMode;
import com.hcl.domino.design.format.ViewCalendarFormat;
import com.hcl.domino.design.format.ViewLineSpacing;
import com.hcl.domino.design.format.ViewTableFormat;
import com.hcl.domino.design.format.ViewTableFormat2;
import com.hcl.domino.design.format.ViewTableFormat3;
import com.hcl.domino.design.format.ViewTableFormat4;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.structures.ColorValue;

/**
 * @param <T> the {@link DesignElement} interface implemented by the class
 * @since 1.0.18
 */
public abstract class AbstractCollectionDesignElement<T extends CollectionDesignElement> extends AbstractNamedDesignElement<T>
    implements CollectionDesignElement, IDefaultAutoFrameElement {
  private DominoViewFormat format;

  public AbstractCollectionDesignElement(final Document doc) {
    super(doc);
  }

  @Override
  public CollectionDesignElement addColumn() {
    throw new NotYetImplementedException();
  }

  @Override
  public DominoCollection getCollection() {
    throw new NotYetImplementedException();
  }

  @Override
  public List<CollectionColumn> getColumns() {
    return this.readViewFormat().getColumns();
  }

  @Override
  public OnOpen getOnOpenUISetting() {
    final DominoViewFormat format = this.readViewFormat();
    final ViewTableFormat format1 = format.getAdapter(ViewTableFormat.class);
    final Set<ViewTableFormat.Flag> flags = format1.getFlags();
    if (flags.contains(ViewTableFormat.Flag.GOTO_BOTTOM_ON_OPEN)) {
      return OnOpen.GOTO_BOTTOM;
    } else if (flags.contains(ViewTableFormat.Flag.GOTO_TOP_ON_OPEN)) {
      return OnOpen.GOTO_TOP;
    } else {
      return OnOpen.GOTO_LAST_OPENED;
    }
  }

  @Override
  public OnRefresh getOnRefreshUISetting() {
    final DominoViewFormat format = this.readViewFormat();
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
    final DominoViewFormat format = this.readViewFormat();
    final ViewTableFormat3 format3 = format.getAdapter(ViewTableFormat3.class);
    if (format3 == null) {
      return false;
    } else {
      // It appears that this flag is inverted in practice
      return !format3.getFlags().contains(ViewTableFormat3.Flag.AllowCustomizations);
    }
  }

  @Override
  public CollectionDesignElement removeColumn(final CollectionColumn column) {
    throw new NotYetImplementedException();
  }

  @Override
  public CollectionDesignElement setOnRefreshUISetting(final OnRefresh onRefreshUISetting) {
    throw new NotYetImplementedException();
  }

  @Override
  public CollectionDesignElement swapColumns(final CollectionColumn a, final CollectionColumn b) {
    throw new NotYetImplementedException();
  }

  @Override
  public CollectionDesignElement swapColumns(final int a, final int b) {
    throw new NotYetImplementedException();
  }
  
  @Override
  public ClassicThemeBehavior getClassicThemeBehavior() {
    final ViewTableFormat3 format3 = format.getAdapter(ViewTableFormat3.class);
    if(format3 == null) {
      return ClassicThemeBehavior.USE_DATABASE_SETTING;
    } else {
      byte themeSetting = (byte)format3.getThemeSetting();
      return DominoEnumUtil.valueOf(ClassicThemeBehavior.class, themeSetting)
        .orElse(ClassicThemeBehavior.USE_DATABASE_SETTING);
    }
  }
  
  @Override
  public Style getStyle() {
    return readViewFormat().getAdapter(ViewCalendarFormat.class) == null ? Style.STANDARD_OUTLINE : Style.CALENDAR;
  }
  
  @Override
  public boolean isDefaultCollection() {
    return getDocument().getDocumentClass().contains(DocumentClass.DEFAULT);
  }
  
  @Override
  public boolean isDefaultCollectionDesign() {
    return getDocument().getAsText(NotesConstants.DESIGN_FLAGS , ' ').contains(NotesConstants.DESIGN_FLAG_DEFAULT_DESIGN);
  }
  
  @Override
  public boolean isCollapseAllOnFirstOpen() {
    return readViewFormat()
        .getAdapter(ViewTableFormat.class)
        .getFlags()
        .contains(ViewTableFormat.Flag.COLLAPSED);
  }
  
  @Override
  public boolean isShowResponseDocumentsInHierarchy() {
    // NB: This method reflects Designer's UI, which is inverted from the actual storage
    return !readViewFormat()
        .getAdapter(ViewTableFormat.class)
        .getFlags()
        .contains(ViewTableFormat.Flag.FLATINDEX);
  }
  
  @Override
  public boolean isShowInViewMenu() {
    return !getDocument().getAsText(NotesConstants.DESIGN_FLAGS , ' ').contains(NotesConstants.DESIGN_FLAG_NO_MENU);
  }
  
  @Override
  public boolean isEvaluateActionsOnDocumentChange() {
    final DominoViewFormat format = this.readViewFormat();
    final ViewTableFormat3 format3 = format.getAdapter(ViewTableFormat3.class);
    if (format3 == null) {
      return false;
    } else {
      return format3.getFlags().contains(ViewTableFormat3.Flag.EvaluateActionsHideWhen);
    }
  }
  
  @Override
  public boolean isCreateDocumentsAtViewLevel() {
    final DominoViewFormat format = this.readViewFormat();
    final ViewTableFormat3 format3 = format.getAdapter(ViewTableFormat3.class);
    if (format3 == null) {
      return false;
    } else {
      return format3.getFlags().contains(ViewTableFormat3.Flag.AllowCreateNewDoc);
    }
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
    Set<ViewTableFormat.Flag> flags = getFormat1().getFlags();
    if(flags.contains(ViewTableFormat.Flag.DISP_ALLUNREAD)) {
      return UnreadMarksMode.ALL;
    } else if(flags.contains(ViewTableFormat.Flag.DISP_UNREADDOCS)) {
      return UnreadMarksMode.DOCUMENTS_ONLY;
    } else {
      return UnreadMarksMode.NONE;
    }
  }
  
  @Override
  public IndexSettings getIndexSettings() {
    return new DefaultIndexSettings();
  }

  // *******************************************************************************
  // * Internal utility methods
  // *******************************************************************************

  private synchronized DominoViewFormat readViewFormat() {
    if (this.format == null) {
      final Document doc = this.getDocument();
      this.format = (DominoViewFormat) doc.getItemValue(DesignConstants.VIEW_VIEW_FORMAT_ITEM).get(0);
    }
    return this.format;
  }
  
  private ViewTableFormat getFormat1() {
    return readViewFormat().getAdapter(ViewTableFormat.class);
  }
  
  private Optional<ViewTableFormat2> getFormat2() {
    final DominoViewFormat format = this.readViewFormat();
    final ViewTableFormat2 format2 = format.getAdapter(ViewTableFormat2.class);
    if (format2 == null) {
      return Optional.empty();
    } else {
      return Optional.of(format2);
    }
  }
  
  private Optional<ViewTableFormat3> getFormat3() {
    final DominoViewFormat format = this.readViewFormat();
    final ViewTableFormat3 format3 = format.getAdapter(ViewTableFormat3.class);
    if (format3 == null) {
      return Optional.empty();
    } else {
      return Optional.of(format3);
    }
  }
  
  private Optional<ViewTableFormat4> getFormat4() {
    final DominoViewFormat format = this.readViewFormat();
    final ViewTableFormat4 format4 = format.getAdapter(ViewTableFormat4.class);
    if (format4 == null) {
      return Optional.empty();
    } else {
      return Optional.of(format4);
    }
  }
  
  private Set<String> getIndexDispositionOptions() {
    String index = getDocument().getAsText(DesignConstants.VIEW_INDEX_ITEM, '/');
    return new HashSet<>(Arrays.asList(index.split("/"))); //$NON-NLS-1$
  }
  
  private Optional<NotesCollationInfo> getCollationInfo() {
    Document doc = getDocument();
    if(!doc.hasItem(DesignConstants.VIEW_COLLATION_ITEM)) {
      return Optional.empty();
    }
    return Optional.of((NotesCollationInfo)doc.getItemValue(DesignConstants.VIEW_COLLATION_ITEM).get(0));
  }
  
  private class DefaultCompositeAppSettings implements CompositeAppSettings {

    @Override
    public boolean isHideColumnHeader() {
      return getFormat3()
          .map(format3 -> format3.getFlags().contains(ViewTableFormat3.Flag.HideColumnHeader))
          .orElse(false);
    }

    @Override
    public boolean isShowPartialHierarchies() {
      ViewTableFormat format = readViewFormat().getAdapter(ViewTableFormat.class);
      return format.getFlags2().contains(ViewTableFormat.Flag2.SHOW_PARTIAL_THREADS);
    }

    @Override
    public boolean isShowSwitcher() {
      return getFormat3()
          .map(format3 -> format3.getFlags().contains(ViewTableFormat3.Flag.ShowVerticalHorizontalSwitcher))
          .orElse(false);
    }

    @Override
    public boolean isShowTabNavigator() {
      return getFormat3()
        .map(format3 -> format3.getFlags().contains(ViewTableFormat3.Flag.ShowTabNavigator))
        .orElse(false);
    }

    @Override
    public String getViewers() {
      return getDocument().getAsText(DesignConstants.VIEW_VIEWERS_ITEM, ' ');
    }

    @Override
    public String getThreadView() {
      return getDocument().getAsText(DesignConstants.VIEW_THREADVIEW_ITEM, ' ');
    }

    @Override
    public boolean isAllowConversationMode() {
      ViewTableFormat format = readViewFormat().getAdapter(ViewTableFormat.class);
      return format.getFlags2().contains(ViewTableFormat.Flag2.PARTIAL_FLATINDEX);
    }
  }
  
  private class DefaultDisplaySettings implements DisplaySettings {

    @Override
    public ColorValue getBackgroundColor() {
      // TODO investigate pre-V5 background colors
      return getFormat3()
        .map(ViewTableFormat3::getBackgroundColor)
        .orElseGet(DesignUtil::whiteColor);
    }

    @Override
    public ColorValue getAlternateRowColor() {
      // TODO investigate pre-V5 background colors
      return getFormat3()
        .map(ViewTableFormat3::getAlternateBackgroundColor)
        .orElseGet(DesignUtil::noColor);
    }
    
    @Override
    public boolean isUseAlternateRowColor() {
      ViewTableFormat format = readViewFormat().getAdapter(ViewTableFormat.class);
      return format.getFlags().contains(ViewTableFormat.Flag.ALTERNATE_ROW_COLORING);
    }

    @Override
    public Optional<CDResource> getBackgroundImage() {
      return Optional.ofNullable(readViewFormat().getBackgroundResource());
    }

    @Override
    public ImageRepeatMode getBackgroundImageRepeatMode() {
      return getFormat4()
        .map(ViewTableFormat4::getRepeatType)
        .orElse(ImageRepeatMode.ONCE);
    }

    @Override
    public GridStyle getGridStyle() {
      Optional<ViewTableFormat3> format3 = getFormat3();
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
    public ColorValue getGridColor() {
      return getFormat3()
        .map(ViewTableFormat3::getGridColor)
        .orElseGet(DesignUtil::noColor);
    }

    @Override
    public HeaderStyle getHeaderStyle() {
      ViewTableFormat format = readViewFormat().getAdapter(ViewTableFormat.class);
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
    }

    @Override
    public ColorValue getHeaderColor() {
      return getFormat3()
        .map(ViewTableFormat3::getHeaderBackgroundColor)
        .orElseGet(DesignUtil::noColor);
    }

    @Override
    public int getHeaderLines() {
      return getFormat2()
        .map(ViewTableFormat2::getHeaderLineCount)
        .orElse((short)1);
    }

    @Override
    public int getRowLines() {
      return getFormat2()
        .map(ViewTableFormat2::getLineCount)
        .orElse((short)1);
    }

    @Override
    public ViewLineSpacing getLineSpacing() {
      return getFormat2()
        .map(ViewTableFormat2::getSpacing)
        .orElse(ViewLineSpacing.SINGLE_SPACE);
    }

    @Override
    public boolean isShrinkRowsToContent() {
      Set<ViewTableFormat.Flag> flags = readViewFormat().getAdapter(ViewTableFormat.class).getFlags();
      return flags.contains(ViewTableFormat.Flag.VARIABLE_LINE_COUNT);
    }

    @Override
    public boolean isHideEmptyCategories() {
      return getIndexDispositionOptions().contains(DesignConstants.INDEXDISPOSITION_HIDEEMPTYCATEGORIES);
    }

    @Override
    public boolean isColorizeViewIcons() {
      Set<ViewTableFormat.Flag2> flags2 = readViewFormat().getAdapter(ViewTableFormat.class).getFlags2();
      return flags2.contains(ViewTableFormat.Flag2.COLORIZE_ICONS);
    }

    @Override
    public ColorValue getUnreadColor() {
      return getFormat3()
        .map(ViewTableFormat3::getUnreadColor)
        .orElseGet(DesignUtil::blackColor);
    }

    @Override
    public boolean isUnreadBold() {
      return getFormat3()
          .map(format -> {
            Set<ViewTableFormat3.Flag> flags = format.getFlags();
            return flags.contains(ViewTableFormat3.Flag.BoldUnreadRows);
          })
          .orElse(false);
    }

    @Override
    public ColorValue getColumnTotalColor() {
      return getFormat3()
          .map(ViewTableFormat3::getTotalsColor)
          .orElseGet(DesignUtil::blackColor);
    }

    @Override
    public boolean isShowSelectionMargin() {
      ViewTableFormat format = readViewFormat().getAdapter(ViewTableFormat.class);
      return !format.getFlags().contains(ViewTableFormat.Flag.HIDE_LEFT_MARGIN);
    }

    @Override
    public boolean isHideSelectionMarginBorder() {
      return getFormat3()
        .map(ViewTableFormat3::getFlags)
        .map(flags -> flags.contains(ViewTableFormat3.Flag.HideLeftMarginBorder))
        .orElse(false);
    }

    @Override
    public boolean isExtendLastColumnToWindowWidth() {
      ViewTableFormat format = readViewFormat().getAdapter(ViewTableFormat.class);
      return format.getFlags().contains(ViewTableFormat.Flag.EXTEND_LAST_COLUMN);
    }

    @Override
    public EdgeWidths getMargin() {
      return new DefaultEdgeWidths();
    }

    @Override
    public int getBelowHeaderMargin() {
      return getFormat3()
        .map(ViewTableFormat3::getViewMarginTopUnder)
        .orElse(0);
    }

    @Override
    public ColorValue getMarginColor() {
      return getFormat3()
        .map(ViewTableFormat3::getMarginBackgroundColor)
        .orElseGet(DesignUtil::whiteColor);
    }
  }
  
  private class DefaultEdgeWidths implements EdgeWidths {
    @Override
    public int getTop() {
      return getFormat3()
        .map(ViewTableFormat3::getViewMarginTop)
        .orElse(0);
    }

    @Override
    public int getLeft() {
      return getFormat3()
        .map(ViewTableFormat3::getViewMarginLeft)
        .orElse(0);
    }

    @Override
    public int getRight() {
      return getFormat3()
        .map(ViewTableFormat3::getViewMarginRight)
        .orElse(0);
    }

    @Override
    public int getBottom() {
      return getFormat3()
        .map(ViewTableFormat3::getViewMarginBottom)
        .orElse(0);
    }
  }
  
  private class DefaultIndexSettings implements IndexSettings {

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
    public boolean isRestrictInitialBuildToDesigner() {
      return getIndexDispositionOptions().contains(DesignConstants.INDEXDISPOSITION_RESTRICTTODESIGNER);
    }

    @Override
    public boolean isGenerateUniqueKeysInIndex() {
      // TODO see if this is actually specified primarily here, as the rest of the item is derived
      // I suspect this is stored in $Collation only, in the COLLATION structure
      return getCollationInfo()
        .map(NotesCollationInfo::isUnique)
        .orElse(false);
    }

    @Override
    public boolean isIncludeUpdatesInTransactionLog() {
      String logUpdates = getDocument().get(DesignConstants.FIELD_LOGVIEWUPDATES, String.class, null);
      return DesignConstants.FIELD_LOGVIEWUPDATES_ENABLED.equals(logUpdates);
    }
    
  }
}
