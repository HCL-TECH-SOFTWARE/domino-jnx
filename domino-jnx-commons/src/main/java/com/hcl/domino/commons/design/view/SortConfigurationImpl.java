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

import java.util.Objects;
import java.util.Optional;

import com.hcl.domino.data.CollectionColumn.SortConfiguration;
import com.hcl.domino.design.format.ViewColumnFormat;
import com.hcl.domino.design.format.ViewColumnFormat2;
import com.hcl.domino.design.format.ViewColumnFormat6;
import com.hcl.domino.richtext.structures.UNID;

/**
 * @author Jesse Gallagher
 * @since 1.0.27
 */
public class SortConfigurationImpl implements SortConfiguration {
  private final DominoCollectionColumn column;

  public SortConfigurationImpl(final DominoCollectionColumn format) {
    this.column = format;
  }

  @Override
  public Optional<String> getResortToViewUnid() {
    final UNID unid = this.getFormat2().getResortToViewUNID();
    if (unid.isUnset()) {
      return Optional.empty();
    } else {
      return Optional.of(unid.toUnidString());
    }
  }

  @Override
  public SortConfiguration setResortToViewUnid(String unid) {
    this.getFormat2().getResortToViewUNID().setUnid(unid);
    this.column.markViewFormatDirty();
    return this;
  }
  
  @Override
  public int getSecondResortColumnIndex() {
    return this.getFormat2().getSecondResortColumnIndex();
  }

  @Override
  public SortConfiguration setSecondResortColumnIndex(int idx) {
    this.getFormat2().setSecondResortColumnIndex(idx);
    this.column.markViewFormatDirty();
    return this;
  }
  
  @Override
  public boolean isCategory() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.SortCategorize);
  }

  @Override
  public SortConfiguration setCategory(boolean b) {
    this.getFormat1().setFlag(ViewColumnFormat.Flag.SortCategorize, b);
    this.column.markViewFormatDirty();
    
    if (b) {
      setSorted(true);
      setSortPermuted(true);
      setAccentSensitive(true);
      this.column.setShowTwistie(true);
    }
    return this;
  }
  
  @Override
  public boolean isDeferResortIndexing() {
    return this.getFormat6(false)
        .map(fmt -> fmt.getFlags().contains(ViewColumnFormat6.Flag.BuildCollationOnDemand))
        .orElse(false);
  }

  @Override
  public SortConfiguration setDeferResortIndexing(boolean b) {
    this.getFormat6(true).get().setFlag(ViewColumnFormat6.Flag.BuildCollationOnDemand, b);
    this.column.markViewFormatDirty();
    return this;
  }
  
  @Override
  public boolean isResortAscending() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.ResortAscending);
  }

  @Override
  public SortConfiguration setResortAscending(boolean b) {
    this.getFormat1().setFlag(ViewColumnFormat.Flag.ResortAscending, b);
    this.column.markViewFormatDirty();
    return this;
  }

  @Override
  public boolean isResortDescending() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.ResortDescending);
  }

  @Override
  public SortConfiguration setResortDescending(boolean b) {
    this.getFormat1().setFlag(ViewColumnFormat.Flag.ResortDescending, b);
    this.column.markViewFormatDirty();
    return this;
  }
  
  @Override
  public boolean isResortToView() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.ResortToView);
  }

  @Override
  public SortConfiguration setResortToView(boolean b) {
    this.getFormat1().setFlag(ViewColumnFormat.Flag.ResortToView, b);
    this.column.markViewFormatDirty();
    return this;
  }
  
  @Override
  public boolean isSecondaryResort() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.SecondResort);
  }

  @Override
  public SortConfiguration setSecondaryResort(boolean b) {
    getFormat1().setFlag(ViewColumnFormat.Flag.SecondResort, b);
    this.column.markViewFormatDirty();
    return this;
  }
  
  @Override
  public boolean isSecondaryResortDescending() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.SecondResortDescending);
  }

  @Override
  public SortConfiguration setSecondaryResortDescending(boolean b) {
    this.getFormat1().setFlag(ViewColumnFormat.Flag.SecondResortDescending, b);
    this.column.markViewFormatDirty();
    return this;
  }
  
  @Override
  public boolean isSorted() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.Sort);
  }

  @Override
  public SortConfiguration setSorted(boolean b) {
    this.getFormat1().setFlag(ViewColumnFormat.Flag.Sort, b);
    this.column.markViewFormatDirty();
    return this;
  }
  
  @Override
  public boolean isSortedDescending() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.SortDescending);
  }

  @Override
  public SortConfiguration setSortedDescending(boolean b) {
    this.getFormat1().setFlag(ViewColumnFormat.Flag.SortDescending, b);
    this.column.markViewFormatDirty();
    return this;
  }
  
  @Override
  public boolean isSortPermuted() {
    return this.getFormat1().getFlags2().contains(ViewColumnFormat.Flag2.SortPermute);
  }
  
  @Override
  public SortConfiguration setSortPermuted(boolean b) {
    this.getFormat1().setFlag(ViewColumnFormat.Flag2.SortPermute, b);
    this.column.markViewFormatDirty();
    return this;
  }
  
  @Override
  public boolean isAccentSensitive() {
    return getFormat2().getFlags().contains(ViewColumnFormat2.Flag3.AccentSensitiveSortInV5);
  }
  
  @Override
  public SortConfiguration setAccentSensitive(boolean b) {
    this.getFormat2().setFlag(ViewColumnFormat2.Flag3.AccentSensitiveSortInV5, b);
    this.column.markViewFormatDirty();
    return  this;
  }
  
  @Override
  public boolean isCaseSensitive() {
    return this.getFormat2().getFlags().contains(ViewColumnFormat2.Flag3.CaseSensitiveSortInV5);
  }
  
  @Override
  public SortConfiguration setCaseSensitive(boolean b) {
    this.getFormat2().setFlag(ViewColumnFormat2.Flag3.CaseSensitiveSortInV5, b);
    this.column.markViewFormatDirty();
    return this;
  }
  
  @Override
  public boolean isCategorizationFlat() {
    return getFormat2().getFlags().contains(ViewColumnFormat2.Flag3.FlatInV5);
  }
  
  @Override
  public SortConfiguration setCategorizationFlat(boolean b) {
    this.getFormat2().setFlag(ViewColumnFormat2.Flag3.FlatInV5, b);
    this.column.markViewFormatDirty();
    return this;
  }
  
  @Override
  public boolean isIgnorePrefixes() {
    return getFormat6(false)
      .map(ViewColumnFormat6::getFlags)
      .map(f -> f.contains(ViewColumnFormat6.Flag.IgnorePrefixes))
      .orElse(false);
  }

  @Override
  public SortConfiguration setIgnorePrefixes(boolean b) {
    this.getFormat6(true).get().setFlag(ViewColumnFormat6.Flag.IgnorePrefixes, b);
    this.column.markViewFormatDirty();
    return this;
  }
  
  // *******************************************************************************
  // * Internal implementation methods
  // *******************************************************************************

  private ViewColumnFormat getFormat1() {
    return Objects.requireNonNull(this.column.getAdapter(ViewColumnFormat.class), "VIEW_COLUMN_FORMAT not read");
  }

  private ViewColumnFormat2 getFormat2() {
    return Objects.requireNonNull(this.column.getAdapter(ViewColumnFormat2.class), "VIEW_COLUMN_FORMAT2 not read");
  }

  private Optional<ViewColumnFormat6> getFormat6(boolean createIfMissing) {
    return this.column.getFormat6(createIfMissing);
  }
}
