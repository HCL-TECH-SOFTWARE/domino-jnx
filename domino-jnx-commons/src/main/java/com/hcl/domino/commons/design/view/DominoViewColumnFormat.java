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

import java.util.Objects;
import java.util.Optional;

import com.hcl.domino.commons.design.DesignColorsAndFonts;
import com.hcl.domino.commons.util.DumpUtil;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.CollectionColumn;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.data.NotesFont;
import com.hcl.domino.design.CollectionDesignElement;
import com.hcl.domino.design.format.NumberDisplayFormat;
import com.hcl.domino.design.format.NumberPref;
import com.hcl.domino.design.format.ViewColumnFormat;
import com.hcl.domino.design.format.ViewColumnFormat2;
import com.hcl.domino.design.format.ViewColumnFormat3;
import com.hcl.domino.design.format.ViewColumnFormat4;
import com.hcl.domino.design.format.ViewColumnFormat5;
import com.hcl.domino.design.format.ViewColumnFormat6;
import com.hcl.domino.formula.FormulaCompiler;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.records.CurrencyFlag;
import com.hcl.domino.richtext.records.CurrencyType;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.NFMT;

/**
 * @author Jesse Gallagher
 * @since 1.0.27
 */
public class DominoViewColumnFormat implements IAdaptable, CollectionColumn {
  private CollectionDesignElement parent;
  private final int index;
  private int columnValuesIndex;
  private ViewColumnFormat format1;
  private ViewColumnFormat2 format2;
  private ViewColumnFormat3 format3;
  private ViewColumnFormat4 format4;
  private ViewColumnFormat5 format5;
  private ViewColumnFormat6 format6;
  private byte[] hideWhenFormula;
  private CDResource twistie;
  private String sharedColumnName;
  private String hiddenTitle;

  public DominoViewColumnFormat(final int index) {
    this.index = index;
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
    return this.columnValuesIndex;
  }

  @Override
  public int getDisplayWidth() {
    return this.getFormat1().getDisplayWidth();
  }

  @Override
  public String getFormula() {
    return this.getFormat1().getFormula();
  }

  @Override
  public String getHideWhenFormula() {
    final byte[] compiled = this.hideWhenFormula;
    if (compiled == null || compiled.length == 0) {
      return ""; //$NON-NLS-1$
    } else {
      return FormulaCompiler.get().decompile(compiled);
    }
  }

  @Override
  public String getItemName() {
    return this.getFormat1().getItemName();
  }

  @Override
  public ViewColumnFormat.ListDelimiter getListDisplayDelimiter() {
    return this.getFormat1().getListDelimiter();
  }

  @Override
  public int getPosition() {
    return this.index;
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
  public boolean isHidden() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.Hidden);
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
    return this.getFormat2()
      .map(ViewColumnFormat2::getFlags)
      .map(flags -> flags.contains(ViewColumnFormat2.Flag3.IsSharedColumn))
      .orElse(false);
  }

  @Override
  public boolean isShowTwistie() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.Twistie);
  }

  @Override
  public boolean isUseHideWhen() {
    return this.getFormat2()
      .map(ViewColumnFormat2::getFlags)
      .map(flags -> flags.contains(ViewColumnFormat2.Flag3.HideWhenFormula))
      .orElse(false);
  }
  
  @Override
  public Optional<String> getSharedColumnName() {
    return Optional.ofNullable(this.sharedColumnName);
  }
  
  @Override
  public boolean isNameColumn() {
    return getFormat5()
      .map(fmt -> fmt.getFlags().contains(ViewColumnFormat5.Flag.IS_NAME))
      .orElse(false);
  }
  
  @Override
  public Optional<String> getOnlinePresenceNameColumn() {
    return getFormat5()
      .map(fmt -> fmt.getDnColumnName());
  }
  
  @Override
  public Optional<CDResource> getTwistieImage() {
    return Optional.ofNullable(this.twistie);
  }
  
  @Override
  public boolean isUserEditable() {
    return getFormat2()
      .map(ViewColumnFormat2::getFlags)
      .map(flags -> flags.contains(ViewColumnFormat2.Flag3.IsColumnEditable))
      .orElse(false);
  }
  
  @Override
  public boolean isColor() {
    return getFormat2()
      .map(ViewColumnFormat2::getFlags)
      .map(flags -> flags.contains(ViewColumnFormat2.Flag3.Color))
      .orElse(false);
  }
  
  @Override
  public boolean isUserDefinableColor() {
    boolean setInVcf2 = getFormat2()
        .map(ViewColumnFormat2::getFlags)
        .map(flags -> {
          return flags;
        })
        .map(flags -> flags.contains(ViewColumnFormat2.Flag3.UserDefinableColor))
        .orElse(false);
    if(setInVcf2) {
      return true;
    }
    return getFormat6()
      .map(ViewColumnFormat6::getFlags)
      .map(flags -> flags.contains(ViewColumnFormat6.Flag.UserDefinableExtended))
      .orElse(false);
  }
  
  @Override
  public boolean isHideTitle() {
    return getFormat2()
      .map(ViewColumnFormat2::getFlags)
      .map(flags -> flags.contains(ViewColumnFormat2.Flag3.HideColumnTitle))
      .orElse(false);
  }
  
  @Override
  public NotesFont getRowFont() {
    return new TextFontItemNotesFont(this.parent.getDocument(), format1.getFontStyle());
  }
  
  @Override
  public NotesFont getHeaderFont() {
    FontStyle style = getFormat2()
      .map(ViewColumnFormat2::getHeaderFontStyle)
      .orElseGet(DesignColorsAndFonts::viewHeaderFont);
    return new TextFontItemNotesFont(this.parent.getDocument(), style);
  }
  
  @Override
  public ColorValue getRowFontColor() {
    return getFormat2()
      .map(ViewColumnFormat2::getColumnColor)
      .orElseGet(DesignColorsAndFonts::blackColor);
  }
  
  @Override
  public ColorValue getHeaderFontColor() {
    return getFormat2()
      .map(ViewColumnFormat2::getHeaderFontColor)
      .orElseGet(DesignColorsAndFonts::blackColor);
  }
  
  @Override
  public NumberSettings getNumberSettings() {
    return new DefaultNumberSettings();
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

  public void readColumnValuesIndex(final int columnValuesIndex) {
    this.columnValuesIndex = columnValuesIndex;
  }

  public void readHideWhenFormula(final byte[] formula) {
    this.hideWhenFormula = formula;
  }

  public void readSharedColumnName(final String name) {
    this.sharedColumnName = name;
  }
  
  public void readTwistie(final CDResource resource) {
    this.twistie = resource;
  }
  
  public void readHiddenTitle(String title) {
    this.hiddenTitle = title;
  }
  
  /**
   * Sets the internal parent reference for this column object, as used by
   * some methods. Does not change any value in the actual column definition.
   * 
   * @param parent the {@link CollectionDesignElement} to set as the parent
   * @since 1.0.32
   */
  public void setParent(CollectionDesignElement parent) {
    this.parent = parent;
  }

  // *******************************************************************************
  // * Internal implementation utilities
  // *******************************************************************************

  private ViewColumnFormat getFormat1() {
    return Objects.requireNonNull(this.format1, "VIEW_COLUMN_FORMAT not read");
  }

  private Optional<ViewColumnFormat2> getFormat2() {
    return Optional.ofNullable(this.format2);
  }
  
  private Optional<ViewColumnFormat4> getFormat4() {
    return Optional.ofNullable(this.format4);
  }
  
  private Optional<ViewColumnFormat5> getFormat5() {
    return Optional.ofNullable(this.format5);
  }
  
  private Optional<ViewColumnFormat6> getFormat6() {
    return Optional.ofNullable(this.format6);
  }
  
  private class DefaultNumberSettings implements NumberSettings {

    @Override
    public NumberDisplayFormat getFormat() {
      return getFormat4()
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
    public boolean isVaryingDecimal() {
      return getFormat4()
        .map(format4 -> format4.getNumberFormat().getAttributes().contains(NFMT.Attribute.VARYING))
        .orElse(false);
    }

    @Override
    public int getFixedDecimalPlaces() {
      return getFormat4()
        .map(format4 -> (int)format4.getNumberFormat().getDigits())
        .orElse(0);
    }

    @Override
    public boolean isOverrideClientLocale() {
      return getFormat4()
        .map(format4 -> format4.getNumberSymbolPreference() == NumberPref.FIELD)
        .orElse(false);
    }

    @Override
    public String getDecimalSymbol() {
      // TODO determine whether the default here should change for non-US locales
      return getFormat4()
        .map(format4 -> format4.getDecimalSymbol())
        .orElse("."); //$NON-NLS-1$
    }

    @Override
    public String getThousandsSeparator() {
      // TODO determine whether the default here should change for non-US locales
      return getFormat4()
        .map(format4 -> format4.getMilliSeparator())
        .orElse(","); //$NON-NLS-1$
    }

    @Override
    public boolean isUseParenthesesWhenNegative() {
      return getFormat4()
        .map(format4 -> format4.getNumberFormat().getAttributes().contains(NFMT.Attribute.PARENS))
        .orElse(false);
    }

    @Override
    public boolean isPunctuateThousands() {
      return getFormat4()
        .map(format4 -> format4.getNumberFormat().getAttributes().contains(NFMT.Attribute.PUNCTUATED))
        .orElse(false);
    }

    @Override
    public long getCurrencyIsoCode() {
      return getFormat4()
        .map(format4 -> format4.getISOCountry())
        .orElse(0l);
    }

    @Override
    public boolean isUseCustomCurrencySymbol() {
      return getFormat4()
        .map(format4 -> format4.getCurrencyType() == CurrencyType.CUSTOM)
        .orElse(false);
    }

    @Override
    public String getCurrencySymbol() {
      // TODO determine whether the default here should change for non-US locales
      return getFormat4()
        .map(format4 -> format4.getCurrencySymbol())
        .orElse("$"); //$NON-NLS-1$
    }

    @Override
    public boolean isCurrencySymbolPostfix() {
      return getFormat4()
        .map(format4 -> format4.getCurrencyFlags().contains(CurrencyFlag.SYMFOLLOWS))
        .orElse(false);
    }

    @Override
    public boolean isUseSpaceNextToNumber() {
      return getFormat4()
        .map(format4 -> format4.getCurrencyFlags().contains(CurrencyFlag.USESPACES))
        .orElse(false);
    }
    
  }
}
