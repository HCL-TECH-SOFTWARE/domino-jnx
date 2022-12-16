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
package com.hcl.domino.commons.design.view;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.CollectionColumn;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.data.NotesFont;
import com.hcl.domino.design.DesignColorsAndFonts;
import com.hcl.domino.design.format.CalendarType;
import com.hcl.domino.design.format.DateComponentOrder;
import com.hcl.domino.design.format.DateShowFormat;
import com.hcl.domino.design.format.DateShowSpecial;
import com.hcl.domino.design.format.DateTimeFlag;
import com.hcl.domino.design.format.DateTimeFlag2;
import com.hcl.domino.design.format.DayFormat;
import com.hcl.domino.design.format.MonthFormat;
import com.hcl.domino.design.format.NarrowViewPosition;
import com.hcl.domino.design.format.NumberDisplayFormat;
import com.hcl.domino.design.format.NumberPref;
import com.hcl.domino.design.format.TileViewerPosition;
import com.hcl.domino.design.format.TimeShowFormat;
import com.hcl.domino.design.format.TimeZoneFormat;
import com.hcl.domino.design.format.ViewColumnFormat;
import com.hcl.domino.design.format.ViewColumnFormat.ListDelimiter;
import com.hcl.domino.design.format.ViewColumnFormat2;
import com.hcl.domino.design.format.ViewColumnFormat2.Flag3;
import com.hcl.domino.design.format.ViewColumnFormat3;
import com.hcl.domino.design.format.ViewColumnFormat4;
import com.hcl.domino.design.format.ViewColumnFormat5;
import com.hcl.domino.design.format.ViewColumnFormat6;
import com.hcl.domino.design.format.WeekFormat;
import com.hcl.domino.design.format.YearFormat;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.records.CurrencyFlag;
import com.hcl.domino.richtext.records.CurrencyType;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.NFMT;
import com.hcl.domino.richtext.structures.NFMT.Attribute;
import com.hcl.domino.richtext.structures.NFMT.Format;

/**
 * @author Jesse Gallagher
 * @since 1.0.27
 */
public class DominoCollectionColumn implements IAdaptable, CollectionColumn {
  private DominoViewFormat parentViewFormat;
  private ViewColumnFormat format1;
  private ViewColumnFormat2 format2;
  private ViewColumnFormat3 format3;
  private ViewColumnFormat4 format4;
  private ViewColumnFormat5 format5;
  private ViewColumnFormat6 format6;
  private String sharedColumnName;
  private String hiddenTitle;

  // cached expensive fields
  private String cachedFormula;
  private String cachedItemName;
  
  public DominoCollectionColumn(DominoViewFormat parentViewFormat) {
    this.parentViewFormat = parentViewFormat;
    this.format1 = ViewColumnFormat.newInstanceWithDefaults();
    this.format2 = ViewColumnFormat2.newInstanceWithDefaults();
  }

  @Override
  public CollectionColumn copyColumnFormatFrom(CollectionColumn otherCol) {
    if (!(otherCol instanceof DominoCollectionColumn)) {
      throw new IllegalArgumentException(MessageFormat.format("Column is not a DominoCollectionColumn: {0}",
          otherCol==null ? "null" : otherCol.getClass().getName())); //$NON-NLS-1$
    }

    DominoCollectionColumn otherDominoCol = (DominoCollectionColumn) otherCol;
    
    if (otherDominoCol.format1!=null) {
      this.format1 = MemoryStructureUtil.newStructure(ViewColumnFormat.class, otherDominoCol.format1.getVariableData().capacity());
      this.format1.getData().put(otherDominoCol.format1.getData());
    }
    if (otherDominoCol.format2!=null) {
      this.format2 = MemoryStructureUtil.newStructure(ViewColumnFormat2.class, otherDominoCol.format2.getVariableData().capacity());
      this.format2.getData().put(otherDominoCol.format2.getData());
    }
    if (otherDominoCol.format3!=null) {
      this.format3 = MemoryStructureUtil.newStructure(ViewColumnFormat3.class, otherDominoCol.format3.getVariableData().capacity());
      this.format3.getData().put(otherDominoCol.format3.getData());
    }
    if (otherDominoCol.format4!=null) {
      this.format4 = MemoryStructureUtil.newStructure(ViewColumnFormat4.class, otherDominoCol.format4.getVariableData().capacity());
      this.format4.getData().put(otherDominoCol.format4.getData());
    }
    if (otherDominoCol.format5!=null) {
      this.format5 = MemoryStructureUtil.newStructure(ViewColumnFormat5.class, otherDominoCol.format5.getVariableData().capacity());
      this.format5.getData().put(otherDominoCol.format5.getData());
    }
    if (otherDominoCol.format6!=null) {
      this.format6 = MemoryStructureUtil.newStructure(ViewColumnFormat6.class, otherDominoCol.format6.getVariableData().capacity());
      this.format6.getData().put(otherDominoCol.format6.getData());
    }
    
    this.sharedColumnName = otherDominoCol.sharedColumnName;
    this.hiddenTitle = otherDominoCol.hiddenTitle;
    markViewFormatDirty();
    
    return this;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public <T> T getAdapter(final Class<T> clazz) {
    if (ViewColumnFormat.class == clazz) {
      return (T) this.format1;
    } else if (ViewColumnFormat2.class == clazz) {
      return (T) this.format2;
    } else if (ViewColumnFormat3.class == clazz) {
      return (T) this.format3;
    } else if (ViewColumnFormat4.class == clazz) {
      return (T) this.format4;
    } else if (ViewColumnFormat5.class == clazz) {
      return (T) this.format5;
    } else if (ViewColumnFormat6.class == clazz) {
      return (T) this.format6;
    }
    return null;
  }

  @Override
  public int getColumnValuesIndex() {
    return this.parentViewFormat.getColumnValuesIndex(this);
  }

  @Override
  public int getDisplayWidth() {
    return this.getFormat1().getDisplayWidth();
  }
  
  @Override
  public CollectionColumn setDisplayWidth(int width) {
    this.getFormat1().setDisplayWidth(width);
    markViewFormatDirty();
    return this;
  }
  
  @Override
  public String getExtraAttributes() {
    return getFormat6(false)
      .map(ViewColumnFormat6::getAttributes)
      .orElse(""); //$NON-NLS-1$
  }

  @Override
  public CollectionColumn setExtraAttributes(String attr) {
    getFormat6(true)
    .ifPresent((fmt6) -> {
      fmt6.setAttributes(attr);
      markViewFormatDirty();
    });
    return this;
  }
  
  @Override
  public String getFormula() {
    if (this.cachedFormula == null)
        this.cachedFormula = this.getFormat1().getFormula();
    return this.cachedFormula;
  }

  @Override
  public CollectionColumn setFormula(String formula) {
    this.getFormat1().setFormula(formula);
    this.cachedFormula = formula;
    markViewFormatDirty();
    return this;
  }
  
  @Override
  public String getHideWhenFormula() {
    return getFormat2(false)
        .map(fmt -> fmt.getHideWhenFormula())
        .orElse(""); //$NON-NLS-1$
  }

  @Override
  public CollectionColumn setHideWhenFormula(String formula) {
    getFormat2(true).get().setHideWhenFormula(formula);
    markViewFormatDirty();
    return this;
  }

  @Override
  public String getItemName() {
	if (this.cachedItemName == null) {
		this.cachedItemName = this.getFormat1().getItemName();
	}
	return this.cachedItemName;
  }

  @Override
  public CollectionColumn setItemName(String itemName) {
    this.getFormat1().setItemName(itemName);
    this.cachedItemName = itemName;
    markViewFormatDirty();
    return this;
  }

  @Override
  public ViewColumnFormat.ListDelimiter getListDisplayDelimiter() {
    return this.getFormat1().getListDelimiter();
  }

  @Override
  public CollectionColumn setListDisplayDelimiter(ListDelimiter delimiter) {
    this.getFormat1().setListDelimiter(delimiter);
    markViewFormatDirty();
    return this;
  }
  
  @Override
  public int getPosition() {
    return this.parentViewFormat.getPosition(this);
  }
  
  @Override
  public SortConfiguration getSortConfiguration() {
    return new SortConfigurationImpl(this);
  }

  @Override
  public String getTitle() {
    if(this.isHideTitle()) {
      return StringUtil.toString(this.hiddenTitle);
    } else {
      return this.getFormat1().getTitle();
    }
  }

  @Override
  public CollectionColumn setTitle(String title) {
    if (this.isHideTitle()) {
      this.hiddenTitle = title;
    }
    else {
      this.getFormat1().setTitle(title);
    }
    markViewFormatDirty();
    return this;
  }
  
  @Override
  public TotalType getTotalType() {
    switch (this.getFormat1().getTotalType()) {
      case AVG_PER_CHILD:
        return TotalType.AveragePerSubcategory;
      case AVG_PER_ENTRY:
        return TotalType.Average;
      case PCT_OVERALL:
        return TotalType.Percent;
      case PCT_PARENT:
        return TotalType.PercentOfParentCategory;
      case TOTAL:
        return TotalType.Total;
      case NONE:
      default:
        return TotalType.None;
    }
  }

  @Override
  public CollectionColumn setTotalType(TotalType type) {
    ViewColumnFormat.StatType newStatType;
    
    switch (type) {
    case AveragePerSubcategory:
      newStatType = ViewColumnFormat.StatType.AVG_PER_CHILD;
      break;
    case Average:
      newStatType = ViewColumnFormat.StatType.AVG_PER_ENTRY;
      break;
    case Percent:
      newStatType = ViewColumnFormat.StatType.PCT_OVERALL;
      break;
    case PercentOfParentCategory:
      newStatType = ViewColumnFormat.StatType.PCT_PARENT;
      break;
    case Total:
      newStatType = ViewColumnFormat.StatType.TOTAL;
      break;
    case None:
      newStatType = ViewColumnFormat.StatType.NONE;
      break;
      default:
        throw new IllegalArgumentException(MessageFormat.format("Unknown TotalType: {0}", type));
    }
    
    this.getFormat1().setTotalType(newStatType);
    markViewFormatDirty();
    return this;
  }
  
  @Override
  public boolean isConstant() {
    return this.getFormat1().getConstantValueLength() > 0;
  }
  
  @Override
  public boolean isExtendToWindowWidth() {
    return getFormat6(false)
      .map(ViewColumnFormat6::getFlags)
      .map(flags -> flags.contains(ViewColumnFormat6.Flag.ExtendColWidthToAvailWindowWidth))
      .orElse(false);
  }

  @Override
  public CollectionColumn setExtendToWindowWidth(boolean b) {
    getFormat6(true)
    .ifPresent((fmt6) -> {
      fmt6.setFlag(ViewColumnFormat6.Flag.ExtendColWidthToAvailWindowWidth, b);
      markViewFormatDirty();
    });
    return this;
  }
  
  @Override
  public boolean isHidden() {
    if(this.format1.getFlags().contains(ViewColumnFormat.Flag.Hidden)) {
      // Then we need to look for further details
      return this.getFormat2(false)
        .map(format2 -> {
          Set<ViewColumnFormat2.HiddenFlag> hiddenFlags = format2.getCustomHiddenFlags();
          if(hiddenFlags.contains(ViewColumnFormat2.HiddenFlag.NormalView)) {
            // Then it's asserted as hidden here
            return true;
          } else if(format2.getFlags().contains(ViewColumnFormat2.Flag3.HideWhenFormula)) {
            // Then it's marked as hidden but only by hide-when
            return false;
          } else if(format2.getFlags().contains(ViewColumnFormat2.Flag3.HideInR5)) {
            // Then it's specially marked as only being hidden in older releases
            return false;
          }
          // If there's no special indicator, then the original Hidden flag holds sway
          return true;
        })
        // If there's no VCF2, it's outright hidden
        .orElse(true);
    } else {
      return false;
    }
  }
  
  @Override
  public boolean isHiddenFromMobile() {
    return getFormat2(false)
      .map(ViewColumnFormat2::getCustomHiddenFlags)
      .map(flags -> flags.contains(ViewColumnFormat2.HiddenFlag.MOBILE))
      .orElse(false);
  }
  
  @Override
  public CollectionColumn setHiddenFromMobile(boolean b) {
    getFormat2(true)
    .ifPresent((fmt2) -> {
      fmt2.setCustomHiddenFlag(ViewColumnFormat2.HiddenFlag.MOBILE, b);
      markViewFormatDirty();
    });
    return this;
  }
  
  @Override
  public boolean isHiddenInPreV6() {
    return getFormat2(false)
      .map(ViewColumnFormat2::getFlags)
      .map(flags -> flags.contains(ViewColumnFormat2.Flag3.HideInR5))
      .orElse(false);
  }

  @Override
  public CollectionColumn setHiddenInPreV6(boolean b) {
    getFormat2(true)
    .ifPresent((fmt2) -> {
      fmt2.setFlag(ViewColumnFormat2.Flag3.HideInR5, b);
      markViewFormatDirty();
    });
    return this;
  }
  
  @Override
  public boolean isHideDetailRows() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.HideDetail);
  }

  @Override
  public CollectionColumn setHideDetailRows(boolean b) {
    this.getFormat1().setFlag(ViewColumnFormat.Flag.HideDetail, b);
    markViewFormatDirty();
    return this;
  }
  
  @Override
  public boolean isIcon() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.Icon);
  }

  @Override
  public CollectionColumn setIcon(boolean b) {
    this.getFormat1().setFlag(ViewColumnFormat.Flag.Icon, b);
    markViewFormatDirty();
    return this;
  }
  
  @Override
  public boolean isResizable() {
    return !this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.NoResize);
  }

  @Override
  public CollectionColumn setResizable(boolean b) {
    getFormat1().setFlag(ViewColumnFormat.Flag.NoResize, !b);
    markViewFormatDirty();
    return this;
  }
  
  @Override
  public boolean isResponsesOnly() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.Response);
  }
  
  @Override
  public CollectionColumn setResponsesOnly(boolean b) {
    this.getFormat1().setFlag(ViewColumnFormat.Flag.Response, b);
    markViewFormatDirty();
    return this;
  }
  
  @Override
  public boolean isSharedColumn() {
    return this.getFormat2(false)
      .map(ViewColumnFormat2::getFlags)
      .map(flags -> flags.contains(ViewColumnFormat2.Flag3.IsSharedColumn))
      .orElse(false);
  }
  
  @Override
  public CollectionColumn setSharedColumn(boolean b) {
    getFormat2(true)
    .ifPresent((fmt2) -> {
      fmt2.setFlag(ViewColumnFormat2.Flag3.IsSharedColumn, b);
      markViewFormatDirty();
    });
    return this;
  }
  
  @Override
  public boolean isShowAsLinks() {
    return this.getFormat1().getFlags2().contains(ViewColumnFormat.Flag2.ShowValuesAsLinks);
  }

  @Override
  public CollectionColumn setShowAsLinks(boolean b) {
    getFormat1().setFlag(ViewColumnFormat.Flag2.ShowValuesAsLinks, b);
    markViewFormatDirty();
    return this;
  }
  
  @Override
  public boolean isShowTwistie() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.Twistie);
  }

  @Override
  public CollectionColumn setShowTwistie(boolean b) {
    this.getFormat1().setFlag(ViewColumnFormat.Flag.Twistie, b);
    markViewFormatDirty();
    return this;
  }
  
  @Override
  public boolean isUseHideWhen() {
    return this.getFormat2(false)
      .map(ViewColumnFormat2::getFlags)
      .map(flags -> flags.contains(ViewColumnFormat2.Flag3.HideWhenFormula))
      .orElse(false);
  }
  
  @Override
  public CollectionColumn setUseHideWhen(boolean b) {
    getFormat2(true).get().setFlag(ViewColumnFormat2.Flag3.HideWhenFormula, b);
    markViewFormatDirty();
    return this;
  }
  
  @Override
  public Optional<String> getSharedColumnName() {
    return Optional.ofNullable(this.sharedColumnName);
  }
  
  @Override
  public CollectionColumn setSharedColumnName(String name) {
    this.sharedColumnName = name;
    return this;
  }
  
  @Override
  public boolean isNameColumn() {
    return getFormat5(false)
      .map(fmt -> fmt.getFlags().contains(ViewColumnFormat5.Flag.IS_NAME))
      .orElse(false);
  }
  
  @Override
  public CollectionColumn setNameColumn(boolean b) {
    getFormat5(true)
    .ifPresent((fmt5) -> {
      fmt5.setFlag(ViewColumnFormat5.Flag.IS_NAME, b);
      markViewFormatDirty();
    });
    return this;
  }
  
  @Override
  public Optional<String> getOnlinePresenceNameColumn() {
    return getFormat5(false)
      .map(fmt -> fmt.getDnColumnName());
  }
  
  @Override
  public CollectionColumn setOnlinePresenceNameColumn(String name) {
    getFormat5(true)
    .ifPresent((fmt5) -> {
      fmt5.setDnColumnName(name);
      markViewFormatDirty();
    });
    return this;
  }
  
  @Override
  public Optional<CDResource> getTwistieImage() {
    return getFormat2(false)
        .flatMap(fmt -> fmt.getTwistieResource());
  }
  
  @Override
  public CollectionColumn clearTwistieImage() {
    getFormat2(true).get().setTwistieResource(null).setFlag(Flag3.TwistieResource, false);
    markViewFormatDirty();
    return this;
  }

  @Override
  public CollectionColumn setTwistieImage(CDResource res) {
    getFormat2(true).get().setTwistieResource(res).setFlag(Flag3.TwistieResource, true);
    markViewFormatDirty();
    return this;
  }

  @Override
  public CollectionColumn setTwistieImageName(String name) {
    setTwistieImage(CDResource.newSharedImageByName(name));
    return this;
  }
  
  @Override
  public boolean isUserEditable() {
    return getFormat2(false)
      .map(ViewColumnFormat2::getFlags)
      .map(flags -> flags.contains(ViewColumnFormat2.Flag3.IsColumnEditable))
      .orElse(false);
  }
  
  @Override
  public CollectionColumn setUserEditable(boolean b) {
    getFormat2(true)
    .ifPresent((fmt2) -> {
      fmt2.setFlag(ViewColumnFormat2.Flag3.IsColumnEditable, b);
      markViewFormatDirty();
    });
    return this;
  }
  
  @Override
  public boolean isColor() {
    return getFormat2(false)
      .map(ViewColumnFormat2::getFlags)
      .map(flags -> flags.contains(ViewColumnFormat2.Flag3.Color))
      .orElse(false);
  }
  
  @Override
  public CollectionColumn setColor(boolean b) {
    getFormat2(true)
    .ifPresent((fmt2) -> {
      fmt2.setFlag(ViewColumnFormat2.Flag3.Color, b);
      markViewFormatDirty();
    });
    return this;
  }
  
  @Override
  public boolean isUserDefinableColor() {
    boolean setInVcf2 = getFormat2(false)
        .map(ViewColumnFormat2::getFlags)
        .map(flags -> {
          return flags;
        })
        .map(flags -> flags.contains(ViewColumnFormat2.Flag3.UserDefinableColor))
        .orElse(false);
    if(setInVcf2) {
      return true;
    }
    return getFormat6(false)
      .map(ViewColumnFormat6::getFlags)
      .map(flags -> flags.contains(ViewColumnFormat6.Flag.UserDefinableExtended))
      .orElse(false);
  }
  
  @Override
  public boolean isHideTitle() {
    return getFormat2(false)
      .map(ViewColumnFormat2::getFlags)
      .map(flags -> flags.contains(ViewColumnFormat2.Flag3.HideColumnTitle))
      .orElse(false);
  }
  
  @Override
  public CollectionColumn setHideTitle(boolean b) {
    if (this.format2==null) {
      this.format2 = createFormat2();
    }
    getFormat2(true).get().setFlag(ViewColumnFormat2.Flag3.HideColumnTitle, b);
    markViewFormatDirty();
    return this;
  }
  
  @Override
  public NotesFont getRowFont() {
    Document doc = Objects.requireNonNull(this.parentViewFormat.getAdapter(Document.class));
    return new TextFontItemNotesFont(doc, format1.getFontStyle());
  }
  
  @Override
  public NotesFont getHeaderFont() {
    FontStyle style = getFormat2(false)
      .map(ViewColumnFormat2::getHeaderFontStyle)
      .orElseGet(DesignColorsAndFonts::viewHeaderFont);
    Document doc = Objects.requireNonNull(this.parentViewFormat.getAdapter(Document.class));
    return new TextFontItemNotesFont(doc, style);
  }
  
  /**
   * @return a {@link ColorValue} for the row font color
   * @deprecated through this reflects a value stored in this structure, it appears
   *             to be always 0 in practice
   */
  public ColorValue getRowFontColor() {
    return getFormat2(false)
      .map(ViewColumnFormat2::getColumnColor)
      .orElseGet(DesignColorsAndFonts::blackColor);
  }
  
  /**
   * @return a {@link ColorValue} for the header font color
   * @deprecated through this reflects a value stored in this structure, it appears
   *             to be always 0 in practice
   */
  @Deprecated
  public ColorValue getHeaderFontColor() {
    return getFormat2(false)
      .map(ViewColumnFormat2::getHeaderFontColor)
      .orElseGet(DesignColorsAndFonts::blackColor);
  }
  
  @Override
  public NumberSettings getNumberSettings() {
    return new DefaultNumberSettings();
  }
  
  @Override
  public DateTimeSettings getDateTimeSettings() {
    return new DefaultDateTimeSettings();
  }
  
  @Override
  public NamesSettings getNamesSettings() {
    return new DefaultNamesSettings();
  }
  
  @Override
  public CompositeApplicationSettings getCompositeApplicationSettings() {
    return new DefaultCompositeApplicationSettings();
  }

  // *******************************************************************************
  // * Format-reader hooks
  // *******************************************************************************

  public void read(final ViewColumnFormat format1) {
    this.format1 = format1;
  }

  public void read(final ViewColumnFormat2 format2) {
    this.format2 = format2;
  }

  public void read(final ViewColumnFormat3 format3) {
    this.format3 = format3;
  }

  public void read(final ViewColumnFormat4 format4) {
    this.format4 = format4;
  }

  public void read(final ViewColumnFormat5 format5) {
    this.format5 = format5;
  }

  public void read(final ViewColumnFormat6 format6) {
    this.format6 = format6;
  }
  
  public void readSharedColumnName(final String name) {
    this.sharedColumnName = name;
  }
  
  public void readHiddenTitle(String title) {
    this.hiddenTitle = title;
  }
  
  // *******************************************************************************
  // * Internal implementation utilities
  // *******************************************************************************

  ViewColumnFormat getFormat1() {
    return Objects.requireNonNull(this.format1, "VIEW_COLUMN_FORMAT not read");
  }

  Optional<ViewColumnFormat2> getFormat2(boolean createIfMissing) {
    if (this.format2==null && createIfMissing) {
      this.format2 = createFormat2();
    }
    return Optional.ofNullable(this.format2);
  }
  
  Optional<ViewColumnFormat3> getFormat3(boolean createIfMissing) {
    if (this.format3==null && createIfMissing) {
      this.format3 = createFormat3();
    }
    return Optional.ofNullable(this.format3);
  }
  
  Optional<ViewColumnFormat4> getFormat4(boolean createIfMissing) {
    if (this.format4==null && createIfMissing) {
      this.format4 = createFormat4();
      
      //set flag to add ViewColumnFormat4 when encoding $ViewFormat
      getFormat2(true).get().setFlag(ViewColumnFormat2.Flag3.NumberFormat, true);
    }
    return Optional.ofNullable(this.format4);
  }
  
  Optional<ViewColumnFormat5> getFormat5(boolean createIfMissing) {
    if (this.format5==null && createIfMissing) {
      this.format5 = createFormat5();
      
      //set flag to add ViewColumnFormat5 when encoding $ViewFormat
      getFormat2(true).get().setFlag(ViewColumnFormat2.Flag3.NamesFormat, true);
    }
    return Optional.ofNullable(this.format5);
  }
  
  Optional<ViewColumnFormat6> getFormat6(boolean createIfMissing) {
    if (this.format6==null && createIfMissing) {
      this.format6 = createFormat6();
      
      //set flag to add ViewColumnFormat6 when encoding $ViewFormat
      getFormat2(true).get().setFlag(ViewColumnFormat2.Flag3.ExtendedViewColFmt6, true);
    }
    return Optional.ofNullable(this.format6);
  }

  ViewColumnFormat2 createFormat2() {
    return ViewColumnFormat2.newInstanceWithDefaults();
  }

  ViewColumnFormat3 createFormat3() {
    //make sure predecessor formats exist
    if (this.format2==null) {
      this.format2 = createFormat2();
    }
    
    return ViewColumnFormat3.newInstanceWithDefaults();
  }

  ViewColumnFormat4 createFormat4() {
    //make sure predecessor formats exist
    if (this.format2==null) {
      this.format2 = createFormat2();
    }
    if (this.format3==null) {
      this.format3 = createFormat3();
    }
    
    return ViewColumnFormat4.newInstanceWithDefaults();
  }

  ViewColumnFormat5 createFormat5() {
    //make sure predecessor formats exist
    if (this.format2==null) {
      this.format2 = createFormat2();
    }
    if (this.format3==null) {
      this.format3 = createFormat3();
    }
    if (this.format4==null) {
      this.format4 = createFormat4();
    }
    
    return ViewColumnFormat5.newInstanceWithDefaults();
  }

  ViewColumnFormat6 createFormat6() {
    //make sure predecessor formats exist
    if (this.format2==null) {
      this.format2 = createFormat2();
    }
    if (this.format3==null) {
      this.format3 = createFormat3();
    }
    if (this.format4==null) {
      this.format4 = createFormat4();
    }
    if (this.format5==null) {
      this.format5 = createFormat5();
    }
    
    return ViewColumnFormat6.newInstanceWithDefaults();
  }

  private class DefaultNumberSettings implements NumberSettings {

    @Override
    public NumberDisplayFormat getFormat() {
      return getFormat4(false)
        .map(format4 -> {
          switch(format4.getNumberFormat().getFormat()) {
            case BYTES:
              // In practice, this is identified by attributes below
              return NumberDisplayFormat.BYTES;
            case CURRENCY:
              return NumberDisplayFormat.CURRENCY;
            case SCIENTIFIC:
              return NumberDisplayFormat.SCIENTIFIC;
            case FIXED:
            case GENERAL:
            default:
              if(format4.getNumberFormat().getAttributes().contains(NFMT.Attribute.BYTES)) {
                return NumberDisplayFormat.BYTES;
              } else if(format4.getNumberFormat().getAttributes().contains(NFMT.Attribute.PERCENT)) {
                return NumberDisplayFormat.PERCENT;
              } else {
                return NumberDisplayFormat.DECIMAL;
              }
          }
        })
        .orElse(NumberDisplayFormat.DECIMAL);
    }

    @Override
    public NumberSettings setFormat(NumberDisplayFormat format) {
      NFMT nfmt = getFormat4(true).get().getNumberFormat();
      
      switch (format) {
      case BYTES:
      case PERCENT:
      case DECIMAL:
      {
        nfmt.setFormat(Format.GENERAL);
        
        Set<Attribute> oldAttributes = nfmt.getAttributes();
        Set<Attribute> newAttributes = new HashSet<>(oldAttributes);
        
        if (format == NumberDisplayFormat.BYTES) {
          newAttributes.remove(NFMT.Attribute.PERCENT);
          
        }
        else if (format == NumberDisplayFormat.PERCENT) {
          newAttributes.remove(NFMT.Attribute.BYTES);
          
        }
        else if (format == NumberDisplayFormat.DECIMAL) {
          newAttributes.remove(NFMT.Attribute.PERCENT);
          newAttributes.remove(NFMT.Attribute.BYTES);
          
        }
        
        nfmt.setAttributes(newAttributes);
        markViewFormatDirty();
      }
        break;
      case CURRENCY:
        nfmt.setFormat(Format.CURRENCY);
        break;
      case SCIENTIFIC:
        nfmt.setFormat(Format.SCIENTIFIC);
        break;
      default:
          throw new IllegalArgumentException(MessageFormat.format("Unknown format: {0}", format));
      }
      
      markViewFormatDirty();
      return this;
    }

    @Override
    public boolean isVaryingDecimal() {
      return getFormat4(false)
        .map(format4 -> format4.getNumberFormat().getAttributes().contains(NFMT.Attribute.VARYING))
        .orElse(true);
    }

    @Override
    public NumberSettings setVaryingDecimal(boolean b) {
      getFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.getNumberFormat().setAttribute(NFMT.Attribute.VARYING, b);
        markViewFormatDirty();
      });
      return this;
    };
    
    @Override
    public int getFixedDecimalPlaces() {
      return getFormat4(false)
        .map(format4 -> (int)format4.getNumberFormat().getDigits())
        .orElse(0);
    }

    @Override
    public NumberSettings setFixedDecimalPlaces(int d) {
      getFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.getNumberFormat().setDigits((short) d);
        markViewFormatDirty();
      });
      return this;
    };
    
    @Override
    public boolean isOverrideClientLocale() {
      return getFormat4(false)
        .map(format4 -> format4.getNumberSymbolPreference() == NumberPref.FIELD)
        .orElse(false);
    }

    @Override
    public NumberSettings setOverrideClientLocale(boolean b) {
      getFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.setNumberSymbolPreference(b ? NumberPref.FIELD : NumberPref.CLIENT);
        markViewFormatDirty();
      });
      return this;
    };
    
    @Override
    public String getDecimalSymbol() {
      // TODO determine whether the default here should change for non-US locales
      return getFormat4(false)
        .map(format4 -> format4.getDecimalSymbol())
        .orElse("."); //$NON-NLS-1$
    }

    @Override
    public NumberSettings setDecimalSymbol(String s) {
      getFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.setDecimalSymbol(s);
        markViewFormatDirty();
      });
      return this;
    };
    
    @Override
    public String getThousandsSeparator() {
      // TODO determine whether the default here should change for non-US locales
      return getFormat4(false)
        .map(format4 -> format4.getMilliSeparator())
        .orElse(","); //$NON-NLS-1$
    }

    @Override
    public NumberSettings setThousandsSeparator(String s) {
      getFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.setMilliSeparator(s);
        markViewFormatDirty();
      });
      return this;
    };
    
    @Override
    public boolean isUseParenthesesWhenNegative() {
      return getFormat4(false)
        .map(format4 -> format4.getNumberFormat().getAttributes().contains(NFMT.Attribute.PARENS))
        .orElse(false);
    }

    @Override
    public NumberSettings setUseParenthesesWhenNegative(boolean b) {
      getFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.getNumberFormat().setAttribute(NFMT.Attribute.PARENS, b);
        markViewFormatDirty();
      });
      return this;
    };
    
    @Override
    public boolean isPunctuateThousands() {
      return getFormat4(false)
        .map(format4 -> format4.getNumberFormat().getAttributes().contains(NFMT.Attribute.PUNCTUATED))
        .orElse(false);
    }

    @Override
    public NumberSettings setPunctuateThousands(boolean b) {
      getFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.getNumberFormat().setAttribute(NFMT.Attribute.PUNCTUATED, b);
        markViewFormatDirty();
      });
      return this;
    };
    
    @Override
    public long getCurrencyIsoCode() {
      return getFormat4(false)
        .map(format4 -> format4.getISOCountry())
        .orElse(0l);
    }

    @Override
    public NumberSettings setCurrencyIsoCode(long code) {
      getFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.setISOCountry(code);
        markViewFormatDirty();
      });
      return this;
    };
    
    @Override
    public boolean isUseCustomCurrencySymbol() {
      return getFormat4(false)
        .map(format4 -> format4.getCurrencyType() == CurrencyType.CUSTOM)
        .orElse(false);
    }

    @Override
    public NumberSettings setUseCustomCurrencySymbol(boolean b) {
      getFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.setCurrencyType(b ? CurrencyType.CUSTOM : CurrencyType.COMMON);
        markViewFormatDirty();
      });
      return this;
    };
    
    @Override
    public String getCurrencySymbol() {
      // TODO determine whether the default here should change for non-US locales
      return getFormat4(false)
        .map(format4 -> format4.getCurrencySymbol())
        .orElse("$"); //$NON-NLS-1$
    }

    @Override
    public NumberSettings setCurrencySymbol(String s) {
      getFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.setCurrencySymbol(s);
        markViewFormatDirty();
      });
      return this;
    }
    
    @Override
    public boolean isCurrencySymbolPostfix() {
      return getFormat4(false)
        .map(format4 -> format4.getCurrencyFlags().contains(CurrencyFlag.SYMFOLLOWS))
        .orElse(false);
    }

    @Override
    public NumberSettings setCurrencySymbolPostfix(boolean b) {
      getFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.setCurrencyFlag(CurrencyFlag.SYMFOLLOWS, b);
        markViewFormatDirty();
      });
      return this;
    };
    
    @Override
    public boolean isUseSpaceNextToNumber() {
      return getFormat4(false)
        .map(format4 -> format4.getCurrencyFlags().contains(CurrencyFlag.USESPACES))
        .orElse(false);
    }

    @Override
    public NumberSettings setUseSpaceNextToNumber(boolean b) {
      getFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.setCurrencyFlag(CurrencyFlag.USESPACES, b);
        markViewFormatDirty();
      });
      return this;
    };
    
  }
  
  private class DefaultDateTimeSettings implements DateTimeSettings {
    @Override
    public boolean isOverrideClientLocale() {
      return getFormat3(false)
        .map(format3 -> format3.getDateTimePreference() == NumberPref.FIELD)
        .orElse(false);
    }

    @Override
    public boolean isDisplayAbbreviatedDate() {
      return getFormat3(false)
        .map(format3 -> format3.getDateTimeFlags().contains(DateTimeFlag.SHOWABBREV))
        .orElse(false);
    }

    @Override
    public boolean isDisplayDate() {
      return getFormat3(false)
        .map(format3 -> format3.getDateTimeFlags().contains(DateTimeFlag.SHOWDATE))
        .orElse(true);
    }

    @Override
    public DateShowFormat getDateShowFormat() {
      return getFormat3(false)
        .map(format3 -> format3.getDateShowFormat())
        .orElse(DateShowFormat.MDY);
    }

    @Override
    public Set<DateShowSpecial> getDateShowBehavior() {
      return getFormat3(false)
        .map(format3 -> format3.getDateShowSpecial())
        .orElseGet(() -> EnumSet.of(DateShowSpecial.SHOW_21ST_4DIGIT));
    }

    @Override
    public CalendarType getCalendarType() {
      return getFormat3(false)
        .map(ViewColumnFormat3::getDateTimeFlags2)
        .map(flags -> flags.contains(DateTimeFlag2.USE_HIJRI_CALENDAR) ? CalendarType.HIJRI : CalendarType.GREGORIAN)
        .orElse(CalendarType.GREGORIAN);
    }

    @Override
    public DateComponentOrder getDateComponentOrder() {
      return getFormat3(false)
        .map(ViewColumnFormat3::getDateComponentOrder)
        .orElse(DateComponentOrder.WMDY);
    }

    @Override
    public String getCustomDateSeparator1() {
      // TODO determine whether the default here should change for non-US locales
      return getFormat3(false)
        .map(ViewColumnFormat3::getDateSeparator1)
        .orElse(" "); //$NON-NLS-1$
    }

    @Override
    public String getCustomDateSeparator2() {
      // TODO determine whether the default here should change for non-US locales
      return getFormat3(false)
        .map(ViewColumnFormat3::getDateSeparator2)
        .orElse("/"); //$NON-NLS-1$
    }

    @Override
    public String getCustomDateSeparator3() {
      // TODO determine whether the default here should change for non-US locales
      return getFormat3(false)
        .map(ViewColumnFormat3::getDateSeparator3)
        .orElse("/"); //$NON-NLS-1$
    }

    @Override
    public DayFormat getDayFormat() {
      return getFormat3(false)
        .map(ViewColumnFormat3::getDayFormat)
        .orElse(DayFormat.DD);
    }

    @Override
    public MonthFormat getMonthFormat() {
      return getFormat3(false)
        .map(ViewColumnFormat3::getMonthFormat)
        .orElse(MonthFormat.MM);
    }

    @Override
    public YearFormat getYearFormat() {
      return getFormat3(false)
        .map(ViewColumnFormat3::getYearFormat)
        .orElse(YearFormat.YYYY);
    }

    @Override
    public WeekFormat getWeekdayFormat() {
      return getFormat3(false)
        .map(ViewColumnFormat3::getDayOfWeekFormat)
        .orElse(WeekFormat.WWW);
    }

    @Override
    public boolean isDisplayTime() {
      return getFormat3(false)
        .map(format3 -> format3.getDateTimeFlags().contains(DateTimeFlag.SHOWTIME))
        .orElse(true);
    }

    @Override
    public TimeShowFormat getTimeShowFormat() {
      return getFormat3(false)
        .flatMap(ViewColumnFormat3::getTimeShowFormat)
        .orElse(TimeShowFormat.HMS);
    }

    @Override
    public TimeZoneFormat getTimeZoneFormat() {
      return getFormat3(false)
        .map(ViewColumnFormat3::getTimeZoneFormat)
        .orElse(TimeZoneFormat.NEVER);
    }

    @Override
    public boolean isTime24HourFormat() {
      return getFormat3(false)
        .map(format3 -> format3.getDateTimeFlags().contains(DateTimeFlag.TWENTYFOURHOUR))
        .orElse(true);
    }

    @Override
    public String getCustomTimeSeparator() {
      // TODO determine whether the default here should change for non-US locales
      return getFormat3(false)
        .map(format3 -> format3.getTimeSeparator())
        .orElse(":"); //$NON-NLS-1$
    }
  }
  
  private class DefaultNamesSettings implements NamesSettings {

    @Override
    public boolean isNamesValue() {
      return getFormat5(false)
        .map(ViewColumnFormat5::getFlags)
        .map(flags -> flags.contains(ViewColumnFormat5.Flag.IS_NAME))
        .orElse(false);
    }

    @Override
    public boolean isShowOnlineStatus() {
      return getFormat5(false)
        .map(ViewColumnFormat5::getFlags)
        .map(flags -> flags.contains(ViewColumnFormat5.Flag.SHOW_IM_STATUS))
        .orElse(false);
    }

    @Override
    public Optional<String> getNameColumnName() {
      return getFormat5(false)
        .map(ViewColumnFormat5::getDnColumnName)
        .flatMap(name -> name.isEmpty() ? Optional.empty() : Optional.of(name));
    }

    @Override
    public OnlinePresenceOrientation getPresenceIconOrientation() {
      return getFormat5(false)
        .map(ViewColumnFormat5::getFlags)
        .map(flags -> {
          if(flags.contains(ViewColumnFormat5.Flag.VERT_ORIENT_BOTTOM)) {
            return OnlinePresenceOrientation.BOTTOM;
          } else if(flags.contains(ViewColumnFormat5.Flag.VERT_ORIENT_MID)) {
            return OnlinePresenceOrientation.MIDDLE;
          } else {
            return OnlinePresenceOrientation.TOP;
          }
        })
        .orElse(OnlinePresenceOrientation.TOP);
    }
  }
  
  private class DefaultCompositeApplicationSettings implements CompositeApplicationSettings {

    @Override
    public NarrowViewPosition getNarrowViewPosition() {
      return getFormat6(false)
        .map(ViewColumnFormat6::getIfViewIsNarrowDo)
        .orElse(NarrowViewPosition.KEEP_ON_TOP);
    }

    @Override
    public boolean isJustifySecondRow() {
      return getFormat6(false)
        .map(ViewColumnFormat6::getFlags)
        .map(flags -> flags.contains(ViewColumnFormat6.Flag.BeginWrapUnder))
        .orElse(false);
    }

    @Override
    public int getSequenceNumber() {
      return getFormat6(false)
        .map(ViewColumnFormat6::getSequenceNumber)
        .orElse(0);
    }

    @Override
    public TileViewerPosition getTileViewerPosition() {
      return getFormat6(false)
        .map(ViewColumnFormat6::getTileViewer)
        .orElse(TileViewerPosition.TOP);
    }

    @Override
    public int getTileLineNumber() {
      return getFormat6(false)
        .map(ViewColumnFormat6::getLineNumber)
        .map(index -> index == 0 ? 1 : index)
        .orElse(1);
    }

    @Override
    public String getCompositeProperty() {
      return getFormat6(false)
        .map(ViewColumnFormat6::getPublishFieldName)
        .orElse(""); //$NON-NLS-1$
    }
  }
  
  void markViewFormatDirty() {
    this.parentViewFormat.setDirty(true);
  }

  @Override
  public String toString() {
    return MessageFormat.format("DominoCollectionColumn [title={0}, itemname={1}, formula={2}]",
        getTitle(), getItemName(), getFormula());
  }

  
}
