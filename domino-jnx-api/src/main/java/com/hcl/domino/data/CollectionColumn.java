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

import com.hcl.domino.design.CollectionDesignElement;
import com.hcl.domino.design.format.ViewColumnFormat;
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

  enum TotalType {
    None, Total, Average, AveragePerSubcategory,
    PercentOfParentCategory, Percent
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

  boolean isHidden();

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
   * Retrieves the color for entry rows in this column.
   * 
   * @return a {@link ColorValue} instance
   * @since 1.0.32
   */
  ColorValue getRowFontColor();
  
  /**
   * Retrieves the font information for the column header.
   * 
   * @return a {@link NotesFont} instance
   * @since 1.0.32
   */
  NotesFont getHeaderFont();
  
  /**
   * Retrieves the font color for the column header.
   * 
   * @return a {@link NotesFont} instance
   * @since 1.0.32
   */
  ColorValue getHeaderFontColor();
  
}
