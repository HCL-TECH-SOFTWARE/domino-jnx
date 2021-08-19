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
package com.hcl.domino.data;

import java.util.Optional;
import java.util.Set;

import com.hcl.domino.design.CollectionDesignElement;
import com.hcl.domino.design.format.CalendarType;
import com.hcl.domino.design.format.DateComponentOrder;
import com.hcl.domino.design.format.DateShowFormat;
import com.hcl.domino.design.format.DateShowSpecial;
import com.hcl.domino.design.format.DayFormat;
import com.hcl.domino.design.format.MonthFormat;
import com.hcl.domino.design.format.NarrowViewPosition;
import com.hcl.domino.design.format.NumberDisplayFormat;
import com.hcl.domino.design.format.TileViewerPosition;
import com.hcl.domino.design.format.TimeShowFormat;
import com.hcl.domino.design.format.TimeZoneFormat;
import com.hcl.domino.design.format.ViewColumnFormat;
import com.hcl.domino.design.format.WeekFormat;
import com.hcl.domino.design.format.YearFormat;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.structures.ColorValue;

public interface CollectionColumn {
  interface SortConfiguration {
    /**
     * @return an {@link Optional} describing the UNID of the view to switch to when
     *         resorting, or an empty one if this is unset
     */
    Optional<String> getResortToViewUnid();

    int getSecondResortColumnIndex();

    boolean isCategory();

    boolean isDeferResortIndexing();

    boolean isResortAscending();

    boolean isResortDescending();

    boolean isResortToView();

    boolean isSecondaryResort();

    boolean isSecondaryResortDescending();

    boolean isSorted();

    boolean isSortedDescending();

    boolean isSortPermuted();
  }
  
  /**
   * Represents the formatting options for this column when it contains a numeric
   * value.
   * 
   * @author Jesse Gallagher
   * @since 1.0.32
   */
  interface NumberSettings {
    /**
     * Retrieves the format used to display numerical values.
     * 
     * @return a {@link NumberDisplayFormat} instance
     */
    NumberDisplayFormat getFormat();
    
    /**
     * Determines whether the column should display numbers with varying decimal
     * places instead of using the value from {@link #getFixedDecimalPlaces()}.
     * 
     * @return {@code true} if number values should be displayed with varying decimal
     *         places; {@code false} otherwise
     */
    boolean isVaryingDecimal();
    
    /**
     * Retrieves the number of places to display for decimal values when appropriate
     * and {@link #isVaryingDecimal()} is {@code false}.
     * 
     * @return the fixed decimal length
     */
    int getFixedDecimalPlaces();
    
    /**
     * Determines whether numbers should be displayed with custom symbols defined here
     * instead of what the client has configured.
     * 
     * @return {@code true} if column settings for symbols and separators should
     *         override client settings; {@code false} otherwise
     */
    boolean isOverrideClientLocale();
    
    /**
     * Retrieves the decimal symbol to use when {@link #isOverrideClientLocale()} is
     * {@code true}.
     * 
     * @return the column-specific decimal symbol
     */
    String getDecimalSymbol();
    
    /**
     * Retrieves the thousands separator to use when {@link #isOverrideClientLocale()}
     * is {@code true}.
     * 
     * @return the column-specific thousands separator
     */
    String getThousandsSeparator();
    
    /**
     * Determines whether negative values in this column should be displayed with
     * parentheses when negative instead of with a negation symbol.
     * 
     * @return {@code true} to use parentheses around negative values;
     *         {@code false} to use the default behavior
     */
    boolean isUseParenthesesWhenNegative();
    
    /**
     * Determines whether thousands groups should be punctuated when displayed.
     * 
     * @return {@code true} to add punctuation for displayed thousands;
     *         {@code false} to use the default behavior
     */
    boolean isPunctuateThousands();
    
    /**
     * Retrieves the ISO code for currency values in this column to use when
     * {@link #getFormat()} is {@link NumberDisplayFormat#CURRENCY CURRENCY} and
     * {@link #isOverrideClientLocale()} is {@code true}.
     * 
     * @return an ISO currency code
     */
    long getCurrencyIsoCode();
    
    /**
     * Determines whether values in the column should be displayed with a custom
     * currency symbol when {@link #getFormat()} is
     * {@link NumberDisplayFormat#CURRENCY CURRENCY} and
     * {@link #isOverrideClientLocale()} is {@code true}.
     * 
     * @return whether to use a custom currency symbol
     */
    boolean isUseCustomCurrencySymbol();
    
    /**
     * Retrieves the currency symbol to use when {@link #getFormat()} is
     * {@link NumberDisplayFormat#CURRENCY CURRENCY},
     * {@link #isOverrideClientLocale()} is {@code true}, and
     * {@link #isUseCustomCurrencySymbol()} is {@code true}.
     * 
     * @return the column-specific currency symbol
     */
    String getCurrencySymbol();
    
    /**
     * Determines whether the currency symbol specified in {@link #getCurrencySymbol()}
     * should be appended to the end of the number instead of affixed to the start.
     * 
     * @return {@code true} if the currency symbol in use should be postfixed;
     *         {@code false} otherwise
     */
    boolean isCurrencySymbolPostfix();
    
    /**
     * Determines whether the currency display should use a space in between the number
     * and the currency symbol.
     * 
     * @return {@code true} if currency display should include a space next to the number;
     *         {@code false} otherwise
     */
    boolean isUseSpaceNextToNumber();
  }
  
  /**
   * Represents the formatting options for this column when it contains a date/time
   * value.
   * 
   * @author Jesse Gallagher
   * @since 1.0.32
   */
  interface DateTimeSettings {
    /**
     * Determines whether dates and times should be displayed with custom symbols
     * defined here instead of what the client has configured.
     * 
     * @return {@code true} if column settings for dates and times should
     *         override client settings; {@code false} otherwise
     */
    boolean isOverrideClientLocale();
    
    /**
     * Determines whether dates, when shown, should be displayed in abbreviated format.
     * 
     * <p>This setting overrides all other date/time settings.</p>
     * 
     * @return {@code true} if dates should be shown in abbreviated format;
     *         {@code false} otherwise
     */
    boolean isDisplayAbbreviatedDate();
    
    /**
     * Determines whether the date portion of a date/time value should be displayed.
     * 
     * @return {@code true} if date portions of date/time values should be displayed;
     *         {@code false} otherwise
     */
    boolean isDisplayDate();
    
    /**
     * Determines which components of a date value should be shown when {@link #isDisplayDate()}
     * is {@code true}.
     * 
     * @return a {@link DateShowFormat} instance
     */
    DateShowFormat getDateShowFormat();
    
    /**
     * Determines the special behaviors enabled for displaying dates when
     * {@link #isDisplayDate()} is {@code true}.
     * 
     * @return a {@link Set} of {@link DateShowSpecial} instances
     */
    Set<DateShowSpecial> getDateShowBehavior();
    
    /**
     * Determines the calendar type to use when for displaying dates when
     * {@link #isDisplayDate()} is {@code true}.
     * 
     * @return a {@link CalendarType} instance
     */
    CalendarType getCalendarType();
    
    /**
     * Determines the display order of date components when {@link #isDisplayDate()}
     * is {@code true}.
     * 
     * @return a {@link DateComponentOrder} instance
     */
    DateComponentOrder getDateComponentOrder();
    
    /**
     * Retrieves the first separator to use when displaying dates when {@link #isDisplayDate()}
     * and {@link #isOverrideClientLocale()} are {@code true}.
     * 
     * @return the first date separator
     */
    String getCustomDateSeparator1();
    
    /**
     * Retrieves the second separator to use when displaying dates when {@link #isDisplayDate()}
     * and {@link #isOverrideClientLocale()} are {@code true}.
     * 
     * @return the second date separator
     */
    String getCustomDateSeparator2();
    
    /**
     * Retrieves the third separator to use when displaying dates when {@link #isDisplayDate()}
     * and {@link #isOverrideClientLocale()} are {@code true}.
     * 
     * @return the third date separator
     */
    String getCustomDateSeparator3();
    
    /**
     * Retrieves the day format to use when when {@link #isDisplayDate()}
     * and {@link #isOverrideClientLocale()} are {@code true}.
     * 
     * @return a {@link DayFormat} instance
     */
    DayFormat getDayFormat();
    
    /**
     * Retrieves the month format to use when when {@link #isDisplayDate()}
     * and {@link #isOverrideClientLocale()} are {@code true}.
     * 
     * @return a {@link MonthFormat} instance
     */
    MonthFormat getMonthFormat();
    
    /**
     * Retrieves the year format to use when when {@link #isDisplayDate()}
     * and {@link #isOverrideClientLocale()} are {@code true}.
     * 
     * @return a {@link YearFormat} instance
     */
    YearFormat getYearFormat();
    
    /**
     * Retrieves the weekday format to use when when {@link #isDisplayDate()}
     * and {@link #isOverrideClientLocale()} are {@code true}.
     * 
     * @return a {@link WeekFormat} instance
     */
    WeekFormat getWeekdayFormat();
    
    /**
     * Determines whether the time portion of a date/time value should be displayed.
     * 
     * @return {@code true} if time portions of date/time values should be displayed;
     *         {@code false} otherwise
     */
    boolean isDisplayTime();
    
    /**
     * Determines which components of a time value should be shown when {@link #isDisplayTime()}
     * is {@code true}.
     * 
     * @return a {@link TimeShowFormat} instance
     */
    TimeShowFormat getTimeShowFormat();
    
    /**
     * Determines the format for displaying the time zone or adjusting the time to local when
     * {@link #isDisplayTime()} is {@code true}.
     * 
     * @return a {@link TimeZoneFormat} instance
     */
    TimeZoneFormat getTimeZoneFormat();
    
    /**
     * Determines whether time values should be shown in 24-hour format regardless of the user's
     * locale when {@link #isDisplayTime()} and {@link #isOverrideClientLocale()} are {@code true}.
     * 
     * @return {@code true} if times should be shown in 24-hour format;
     *         {@code false} otherwise
     */
    boolean isTime24HourFormat();
    
    /**
     * Retrieves the separator to use when displaying times when {@link #isDisplayTime()} and
     * {@link #isOverrideClientLocale()} are {@code true}.
     * 
     * @return the time separator
     */
    String getCustomTimeSeparator();
  }
  
  /**
   * Represents the formatting options for this column when it contains a name
   * value.
   * 
   * @author Jesse Gallagher
   * @since 1.0.32
   */
  interface NamesSettings {
    /**
     * Determines whether text values in the column should be treated as names.
     * 
     * @return {@code true} if column text values should be considered names;
     *         {@code false} otherwise
     */
    boolean isNamesValue();
    
    /**
     * Determines whether the column should display online-presence information
     * when available.
     * 
     * @return {@code true} if available online-presence information should be displayed;
     *         {@code false} otherwise
     */
    boolean isShowOnlineStatus();
    
    /**
     * Retrieves the item name of the column that contains the online-presence
     * name, if not this one.
     * 
     * @return an {@link Optional} describing the item name of the column containing
     *         the online-presence name, or an empty one if this is not specified
     */
    Optional<String> getNameColumnName();
    
    /**
     * Retrieves the orientation of the online-presence icon when {@link #isShowOnlineStatus()}
     * is {@code true}.
     * 
     * @return an {@link OnlinePresenceOrientation} instance
     */
    OnlinePresenceOrientation getPresenceIconOrientation();
  }
  
  /**
   * Represents display options for this column when it is displayed within a
   * Composite Application.
   * 
   * @author Jesse Gallagher
   * @since 1.0.32
   */
  interface CompositeApplicationSettings {
    /**
     * Retrieves the positioning behavior when the view is considered narrow.
     * 
     * @return a {@link NarrowViewPosition} instance
     */
    NarrowViewPosition getNarrowViewPosition();
    
    /**
     * Determines whether the second row under this column should be justified when
     * {@link #getNarrowViewPosition()} is {@link NarrowViewPosition#KEEP_ON_TOP KEEP_ON_TOP}.
     * 
     * @return {@code true} if the second row should be justified;
     *         {@code false} otherwise
     */
    boolean isJustifySecondRow();
    
    /**
     * Retrieves the value sequence number used when {@link #getNarrowViewPosition()} is
     * {@link NarrowViewPosition#WRAP WRAP} or {@link NarrowViewPosition#HIDE HIDE}.
     * 
     * @return the sequence number for displaying column data in narrow views
     */
    int getSequenceNumber();
    
    /**
     * Retrieves the positioning behavior when the view is presented in the Tile Viewer.
     * 
     * @return a {@link TileViewerPosition} instance
     */
    TileViewerPosition getTileViewerPosition();
    
    /**
     * Retrieves the column index for displaying this column when the view is presented in
     * the Tile Viewer.
     * 
     * @return the display index for Tile Viewer mode
     */
    int getTileLineNumber();
    
    /**
     * Retrieves the composite property that this field is published as.
     * 
     * @return the composite property name
     */
    String getCompositeProperty();
  }

  enum TotalType {
    None, Total, Average, AveragePerSubcategory,
    PercentOfParentCategory, Percent
  }
  
  /**
   * Represents the orientation of the icon for columns that display online-
   * presence information.
   * 
   * @author Jesse Gallagher
   * @since 1.0.32
   */
  enum OnlinePresenceOrientation {
    TOP, MIDDLE, BOTTOM
  }
  
  int getColumnValuesIndex();

  /**
   * Retrieves the display width of this column.
   * <p>
   * Note: this value reflects the storage mechanism, which differs from the
   * display in Designer.
   * The stored value is the count of 1/8 average character widths to go by, while
   * Designer adjusts
   * this by full characters. Accordingly, this value will be about 8 times larger
   * than the value
   * shown in Designer.
   * </p>
   *
   * @return the display width of this column, in units of 1/8 of the average
   *         character width
   * @since 1.0.28
   */
  int getDisplayWidth();
  
  /**
   * Retrieves the custom attributes set on this column.
   * 
   * @return the attributes string for the column
   * @since 1.0.32
   */
  String getExtraAttributes();

  String getFormula();

  /**
   * Retrieves the hide-when formula for the column.
   * <p>
   * Note: this formula may or may not be used; use {@link #isUseHideWhen()} to
   * determine
   * whether it is enabled.
   * </p>
   *
   * @return the hide-when formula specified for the column.
   * @since 1.0.27
   */
  String getHideWhenFormula();

  String getItemName();

  /**
   * @return the delimiter to use when displaying multiple values
   * @since 1.0.27
   */
  ViewColumnFormat.ListDelimiter getListDisplayDelimiter();

  int getPosition();

  /**
   * @return a {@link SortConfiguration} instance representing the settings of
   *         this column
   * @since 1.0.27
   */
  SortConfiguration getSortConfiguration();

  String getTitle();

  /**
   * @return the total-row value to compute
   * @since 1.0.27
   */
  TotalType getTotalType();

  boolean isConstant();
  
  /**
   * Determines whether the column should be extended to use available
   * window width, regardless of whether it is the last in the view or folder.
   * 
   * @return {@code true} if this column should be extended to the window width;
   *         {@code false} otherwise
   * @since 1.0.32
   */
  boolean isExtendToWindowWidth();

  boolean isHidden();
  
  /**
   * Determines whether the column should be hidden from mobile clients
   * specifically.
   * 
   * @return {@code true} if the column should be hidden from mobile clients;
   *         {@code false} otherwise
   * @since 1.0.32
   */
  boolean isHiddenFromMobile();
  
  /**
   * Determines whether column should be hidden from Notes clients below
   * version 6.
   * 
   * @return {@code true} if the column should be hidden from pre-V6 clients;
   *         {@code false} otherwise
   * @since 1.0.32
   */
  boolean isHiddenInPreV6();

  /**
   * Determines whether a column's non-total rows should be hidden.
   * 
   * @return {@code true} if a column's non-total rows should be hidden;
   *         {@code false} otherwise
   */
  boolean isHideDetailRows();

  /**
   * Determines whether the column's values should be shown as icons, either
   * as indexed values to stock icons or string names of image resources.
   * 
   * @return {@code true} if the column's values represent icons;
   *         {@code false} otherwise
   */
  boolean isIcon();

  /**
   * Determines whether the column should be resizable by the user in the UI.
   * 
   * @return {@code true} if the column is user-resizable;
   *         {@code false} otherwise
   */
  boolean isResizable();

  /**
   * Determines whether the column should be evaluated for response documents only.
   * 
   * @return {@code true} if this column should apply to response documents only;
   *         {@code false} otherwise
   */
  boolean isResponsesOnly();
  
  /**
   * Determines whether this column is marked as being displayed as links when rendered
   * on the web.
   * 
   * @return {@code true} if column values should be shown as links on the web;
   *         {@code false} otherwise
   */
  boolean isShowAsLinks();

  boolean isShowTwistie();

  /**
   * @return {@code true} if the column's hide-when formula should be used;
   *         {@code false} otherwise
   * @since 1.0.27
   */
  boolean isUseHideWhen();
  
  /**
   * @return {@code true} if the column is defined by a shared column;
   *         {@code false} otherwise
   * @since 1.0.29
   */
  boolean isSharedColumn();
  
  /**
   * @return an {@link Optional} describing the name of the shared-column
   *         design element that defines this column, or an empty one if
   *         this column is not a shared column
   * @since 1.0.29
   */
  Optional<String> getSharedColumnName();
  
  /**
   * @return {@code true} if this column is marked as containing a name
   *         value; {@code false} otherwise
   * @since 1.0.29
   */
  boolean isNameColumn();
  
  /**
   * @return an {@link Optional} describing the name of the column containing
   *         the distinguished name to use for online presence, or an empty
   *         one if this is not specified
   * @since 1.0.29
   */
  Optional<String> getOnlinePresenceNameColumn();
  
  /**
   * Retrieves the image resource used for the expand/collapse twistie, if
   * configured.
   * 
   * <p>Note: this may return a value even when the twistie is not enabled.
   * Use {@link #isShowTwistie()} to determine whether this value is used.</p>
   * 
   * @return 1.0.32
   */
  Optional<CDResource> getTwistieImage();
  
  /**
   * Determines whether the column is marked as being user-editable.
   * 
   * @return {@code true} if the column is user-editable;
   *         {@code false} otherwise
   * @since 1.0.32
   */
  boolean isUserEditable();
  
  /**
   * Determines whether the column's values should be treated as specifying the
   * color for subsequent columns.
   * 
   * @return {@code true} if this column's values represent color;
   *         {@code false} otherwise
   * @since 1.0.32
   */
  boolean isColor();
  
  /**
   * Determines whether the column represents a color value definable by the user
   * via a profile document.
   * 
   * @return {@code true} if this column is a user-definable color column;
   *         {@code false} otherwise
   * @since 1.0.32
   * @see CollectionDesignElement#getColumnProfileDocName()
   * @see CollectionDesignElement#getUserDefinableNonFallbackColumns()
   */
  boolean isUserDefinableColor();
  
  /**
   * Determines whether the column title should be hidden even when it is specified.
   * 
   * @return {@code true} if the column title should be hidden;
   *         {@code false} otherwise
   * @since 1.0.32
   */
  boolean isHideTitle();
  
  /**
   * Retrieves the font information for entry rows in this column.
   * 
   * @return a {@link NotesFont} instance
   * @since 1.0.32
   */
  NotesFont getRowFont();
  
  /**
   * Retrieves the font information for the column header.
   * 
   * @return a {@link NotesFont} instance
   * @since 1.0.32
   */
  NotesFont getHeaderFont();
  
  /**
   * Retrieves a view of the column's settings to use when displaying number
   * values.
   * 
   * @return a {@link NumberSettings} instance
   * @since 1.0.32
   */
  NumberSettings getNumberSettings();
  
  /**
   * Retrieves a view of the column's settings to use when displaying date/time
   * values.
   * 
   * @return a {@link DateTimeSettings} instance
   * @since 1.0.32
   */
  DateTimeSettings getDateTimeSettings();
  
  /**
   * Retrieves a view of the column's settings to use when displaying names
   * values.
   * 
   * @return a {@link NamesSettings} instance
   * @since 1.0.32
   */
  NamesSettings getNamesSettings();
  
  /**
   * Retrieves a view of the column's settings to use when the view is displayed
   * in a Composite Application.
   * 
   * @return a {@link CompositeApplicationSettings} instance
   * @since 1.0.32
   */
  CompositeApplicationSettings getCompositeApplicationSettings();
}
