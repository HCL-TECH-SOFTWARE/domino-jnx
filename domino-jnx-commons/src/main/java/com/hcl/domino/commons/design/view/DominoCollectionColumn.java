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
package com.hcl.domino.commons.design.view;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.commons.design.AbstractCollectionDesignElement;
import com.hcl.domino.commons.design.SharedColumnImpl;
import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.CollectionColumn;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.data.NotesFont;
import com.hcl.domino.design.CollectionDesignElement;
import com.hcl.domino.design.DesignColorsAndFonts;
import com.hcl.domino.design.DesignElement;
import com.hcl.domino.design.SharedColumn;
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
import com.hcl.domino.design.format.ViewColumnFormat2;
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
  private DesignElement parent;
  private ViewColumnFormat format1;
  private ViewColumnFormat2 format2;
  private ViewColumnFormat3 format3;
  private ViewColumnFormat4 format4;
  private ViewColumnFormat5 format5;
  private ViewColumnFormat6 format6;
  private String sharedColumnName;
  private String hiddenTitle;
  
  public DominoCollectionColumn() {
    this.format1 = ViewColumnFormat.newInstance();
    this.format2 = ViewColumnFormat2.newInstance();
  }

  /**
   * Copies all data from the specified column
   * 
   * @param otherCol other column
   * @return this instance
   */
  public DominoCollectionColumn copyDesignFrom(DominoCollectionColumn otherCol) {
    if (otherCol.format1!=null) {
      this.format1 = MemoryStructureUtil.newStructure(ViewColumnFormat.class, otherCol.format1.getVariableData().capacity());
      this.format1.getData().put(otherCol.format1.getData());
    }
    if (otherCol.format2!=null) {
      this.format2 = MemoryStructureUtil.newStructure(ViewColumnFormat2.class, otherCol.format2.getVariableData().capacity());
      this.format2.getData().put(otherCol.format2.getData());
    }
    if (otherCol.format3!=null) {
      this.format3 = MemoryStructureUtil.newStructure(ViewColumnFormat3.class, otherCol.format3.getVariableData().capacity());
      this.format3.getData().put(otherCol.format3.getData());
    }
    if (otherCol.format4!=null) {
      this.format4 = MemoryStructureUtil.newStructure(ViewColumnFormat4.class, otherCol.format4.getVariableData().capacity());
      this.format4.getData().put(otherCol.format4.getData());
    }
    if (otherCol.format5!=null) {
      this.format5 = MemoryStructureUtil.newStructure(ViewColumnFormat5.class, otherCol.format5.getVariableData().capacity());
      this.format5.getData().put(otherCol.format5.getData());
    }
    if (otherCol.format6!=null) {
      this.format6 = MemoryStructureUtil.newStructure(ViewColumnFormat6.class, otherCol.format6.getVariableData().capacity());
      this.format6.getData().put(otherCol.format6.getData());
    }
    
    this.sharedColumnName = otherCol.sharedColumnName;
    this.hiddenTitle = otherCol.hiddenTitle;
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
    if (this.parent instanceof SharedColumn) {
      CollectionColumn column = ((SharedColumn)this.parent).getColumn();
      if (this.equals(column)) {
        int constValLen = getFormat1().getConstantValueLength();
        if (constValLen==0) {
          return 0;
        }
      }
    }
    else if (this.parent instanceof CollectionDesignElement) {
      List<CollectionColumn> columns = ((CollectionDesignElement)this.parent).getColumns();
      int columnValuesIndex = 0;
      
      for (CollectionColumn currCol : columns) {
        int currColValuesIndex = 0xffff;
        
        if (currCol instanceof IAdaptable) {
          ViewColumnFormat currFormat1 = ((IAdaptable)currCol).getAdapter(ViewColumnFormat.class);
          if (currFormat1!=null) {
            int constValLen = currFormat1.getConstantValueLength();
            if (constValLen==0) {
              currColValuesIndex=columnValuesIndex++;
            }
          }
        }
        
        if (this.equals(currCol)) {
          return currColValuesIndex;
        }
      }
      
      int index = columns.indexOf(this);
      return index;
    }
    
    return 0xffff;
  }

  @Override
  public int getDisplayWidth() {
    return this.getFormat1().getDisplayWidth();
  }
  
  @Override
  public String getExtraAttributes() {
    return getFormat6(false)
      .map(ViewColumnFormat6::getAttributes)
      .orElse(""); //$NON-NLS-1$
  }

  @Override
  public String getFormula() {
    return this.getFormat1().getFormula();
  }

  @Override
  public CollectionColumn setFormula(String formula) {
    this.getFormat1().setFormula(formula);
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
    return this.getFormat1().getItemName();
  }

  @Override
  public CollectionColumn setItemName(String itemName) {
    this.getFormat1().setItemName(itemName);
    markViewFormatDirty();
    
    return this;
  }

  @Override
  public ViewColumnFormat.ListDelimiter getListDisplayDelimiter() {
    return this.getFormat1().getListDelimiter();
  }

  @Override
  public int getPosition() {
    if (this.parent instanceof SharedColumn) {
      CollectionColumn column = ((SharedColumn)this.parent).getColumn();
      if (this.equals(column)) {
        return 0;
      }
    }
    else if (this.parent instanceof CollectionDesignElement) {
      List<CollectionColumn> columns = ((CollectionDesignElement)this.parent).getColumns();
      int index = columns.indexOf(this);
      return index;
    }
    
    return -1;
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
  public boolean isHiddenInPreV6() {
    return getFormat2(false)
      .map(ViewColumnFormat2::getFlags)
      .map(flags -> flags.contains(ViewColumnFormat2.Flag3.HideInR5))
      .orElse(false);
  }

  @Override
  public boolean isHideDetailRows() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.HideDetail);
  }

  @Override
  public boolean isIcon() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.Icon);
  }

  @Override
  public boolean isResizable() {
    return !this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.NoResize);
  }

  @Override
  public boolean isResponsesOnly() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.Response);
  }
  
  @Override
  public boolean isSharedColumn() {
    return this.getFormat2(false)
      .map(ViewColumnFormat2::getFlags)
      .map(flags -> flags.contains(ViewColumnFormat2.Flag3.IsSharedColumn))
      .orElse(false);
  }
  
  @Override
  public boolean isShowAsLinks() {
    return this.getFormat1().getFlags2().contains(ViewColumnFormat.Flag2.ShowValuesAsLinks);
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
  public boolean isNameColumn() {
    return getFormat5(false)
      .map(fmt -> fmt.getFlags().contains(ViewColumnFormat5.Flag.IS_NAME))
      .orElse(false);
  }
  
  @Override
  public Optional<String> getOnlinePresenceNameColumn() {
    return getFormat5(false)
      .map(fmt -> fmt.getDnColumnName());
  }
  
  @Override
  public Optional<CDResource> getTwistieImage() {
    return getFormat2(false)
        .flatMap(fmt -> fmt.getTwistieResource());
  }
  
  public CollectionColumn setTwistieImage(CDResource res) {
    getFormat2(true).get().setTwistieResource(res);
    markViewFormatDirty();
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
  public boolean isColor() {
    return getFormat2(false)
      .map(ViewColumnFormat2::getFlags)
      .map(flags -> flags.contains(ViewColumnFormat2.Flag3.Color))
      .orElse(false);
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
    return new TextFontItemNotesFont(this.parent.getDocument(), format1.getFontStyle());
  }
  
  @Override
  public NotesFont getHeaderFont() {
    FontStyle style = getFormat2(false)
      .map(ViewColumnFormat2::getHeaderFontStyle)
      .orElseGet(DesignColorsAndFonts::viewHeaderFont);
    return new TextFontItemNotesFont(this.parent.getDocument(), style);
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
  
  /**
   * Sets the internal parent reference for this column object, as used by
   * some methods. Does not change any value in the actual column definition.
   * 
   * @param parent the {@link DesignElement} to set as the parent
   * @since 1.0.32
   */
  public void setParent(DesignElement parent) {
    this.parent = parent;
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
    }
    return Optional.ofNullable(this.format4);
  }
  
  Optional<ViewColumnFormat5> getFormat5(boolean createIfMissing) {
    if (this.format5==null && createIfMissing) {
      this.format5 = createFormat5();
    }
    return Optional.ofNullable(this.format5);
  }
  
  Optional<ViewColumnFormat6> getFormat6(boolean createIfMissing) {
    if (this.format6==null && createIfMissing) {
      this.format6 = createFormat6();
    }
    return Optional.ofNullable(this.format6);
  }

  ViewColumnFormat2 createFormat2() {
    return ViewColumnFormat2.newInstance();
  }

  ViewColumnFormat3 createFormat3() {
    //make sure predecessor formats exist
    if (this.format2==null) {
      this.format2 = createFormat2();
    }
    
    return ViewColumnFormat3.newInstance();
  }

  ViewColumnFormat4 createFormat4() {
    //make sure predecessor formats exist
    if (this.format2==null) {
      this.format2 = createFormat2();
    }
    if (this.format3==null) {
      this.format3 = createFormat3();
    }
    
    return ViewColumnFormat4.newInstance();
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
    
    return ViewColumnFormat5.newInstance();
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
    
    return ViewColumnFormat6.newInstance();
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

    public NumberSettings setVaryingDecimal(boolean b) {
      getFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.getNumberFormat().setAttribute(NFMT.Attribute.VARYING, b);
      });
      markViewFormatDirty();
      return this;
    };
    
    @Override
    public int getFixedDecimalPlaces() {
      return getFormat4(false)
        .map(format4 -> (int)format4.getNumberFormat().getDigits())
        .orElse(0);
    }

    public NumberSettings setFixedDecimalPlaces(int d) {
      getFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.getNumberFormat().setDigits((short) d);
      });
      return this;
    };
    
    @Override
    public boolean isOverrideClientLocale() {
      return getFormat4(false)
        .map(format4 -> format4.getNumberSymbolPreference() == NumberPref.FIELD)
        .orElse(false);
    }

    public NumberSettings setOverrideClientLocale(boolean b) {
      getFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.setNumberSymbolPreference(b ? NumberPref.FIELD : NumberPref.CLIENT);
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

    public NumberSettings setDecimalSymbol(String s) {
      getFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.setDecimalSymbol(s);
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

    public NumberSettings setThousandsSeparator(String s) {
      getFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.setMilliSeparator(s);
      });
      return this;
    };
    
    @Override
    public boolean isUseParenthesesWhenNegative() {
      return getFormat4(false)
        .map(format4 -> format4.getNumberFormat().getAttributes().contains(NFMT.Attribute.PARENS))
        .orElse(false);
    }

    public NumberSettings setUseParenthesesWhenNegative(boolean b) {
      getFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.getNumberFormat().setAttribute(NFMT.Attribute.PARENS, b);
      });
      return this;
    };
    
    @Override
    public boolean isPunctuateThousands() {
      return getFormat4(false)
        .map(format4 -> format4.getNumberFormat().getAttributes().contains(NFMT.Attribute.PUNCTUATED))
        .orElse(false);
    }

    public NumberSettings setPunctuateThousands(boolean b) {
      getFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.getNumberFormat().setAttribute(NFMT.Attribute.PUNCTUATED, b);
      });
      return this;
    };
    
    @Override
    public long getCurrencyIsoCode() {
      return getFormat4(false)
        .map(format4 -> format4.getISOCountry())
        .orElse(0l);
    }

    public NumberSettings setCurrencyIsoCode(long code) {
      getFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.setISOCountry(code);
      });
      return this;
    };
    
    @Override
    public boolean isUseCustomCurrencySymbol() {
      return getFormat4(false)
        .map(format4 -> format4.getCurrencyType() == CurrencyType.CUSTOM)
        .orElse(false);
    }

    public NumberSettings setUseCustomCurrencySymbol(boolean b) {
      getFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.setCurrencyType(b ? CurrencyType.CUSTOM : CurrencyType.COMMON);
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

    public NumberSettings setCurrencySymbol(String s) {
      getFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.setCurrencySymbol(s);
      });
      return this;
    }
    
    @Override
    public boolean isCurrencySymbolPostfix() {
      return getFormat4(false)
        .map(format4 -> format4.getCurrencyFlags().contains(CurrencyFlag.SYMFOLLOWS))
        .orElse(false);
    }

    public NumberSettings setCurrencySymbolPostfix(boolean b) {
      getFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.setCurrencyFlag(CurrencyFlag.SYMFOLLOWS, b);
      });
      return this;
    };
    
    @Override
    public boolean isUseSpaceNextToNumber() {
      return getFormat4(false)
        .map(format4 -> format4.getCurrencyFlags().contains(CurrencyFlag.USESPACES))
        .orElse(false);
    }
    
    public NumberSettings setUseSpaceNextToNumber(boolean b) {
      getFormat4(true)
      .ifPresent((fmt4) -> {
        fmt4.setCurrencyFlag(CurrencyFlag.USESPACES, b);
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
    if (this.parent instanceof SharedColumnImpl) {
      ((SharedColumnImpl)this.parent).setViewFormatDirty(true);
    }
    else if (this.parent instanceof AbstractCollectionDesignElement) {
      ((AbstractCollectionDesignElement)this.parent).setViewFormatDirty(true);
    }
  }

  @Override
  public String toString() {
    return MessageFormat.format("DominoCollectionColumn [title={0}, itemname={1}, formula={2}]",
        getTitle(), getItemName(), getFormula());
  }

  
}
