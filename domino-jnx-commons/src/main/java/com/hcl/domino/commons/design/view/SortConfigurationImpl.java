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
  private final DominoCollectionColumn format;

  public SortConfigurationImpl(final DominoCollectionColumn format) {
    this.format = format;
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
  public int getSecondResortColumnIndex() {
    return this.getFormat2().getSecondResortColumnIndex();
  }

  @Override
  public boolean isCategory() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.SortCategorize);
  }

  @Override
  public boolean isDeferResortIndexing() {
    return this.getFormat6()
        .map(fmt -> fmt.getFlags().contains(ViewColumnFormat6.Flag.BuildCollationOnDemand))
        .orElse(false);
  }

  @Override
  public boolean isResortAscending() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.ResortAscending);
  }

  @Override
  public boolean isResortDescending() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.ResortDescending);
  }

  @Override
  public boolean isResortToView() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.ResortToView);
  }

  @Override
  public boolean isSecondaryResort() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.SecondResort);
  }

  @Override
  public boolean isSecondaryResortDescending() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.SecondResortDescending);
  }

  @Override
  public boolean isSorted() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.Sort);
  }

  @Override
  public boolean isSortedDescending() {
    return this.getFormat1().getFlags().contains(ViewColumnFormat.Flag.SortDescending);
  }

  @Override
  public boolean isSortPermuted() {
    return this.getFormat1().getFlags2().contains(ViewColumnFormat.Flag2.SortPermute);
  }
  
  @Override
  public boolean isAccentSensitive() {
    return getFormat2().getFlags().contains(ViewColumnFormat2.Flag3.AccentSensitiveSortInV5);
  }
  
  @Override
  public boolean isCaseSensitive() {
    return getFormat2().getFlags().contains(ViewColumnFormat2.Flag3.CaseSensitiveSortInV5);
  }
  
  @Override
  public boolean isCategorizationFlat() {
    return getFormat2().getFlags().contains(ViewColumnFormat2.Flag3.FlatInV5);
  }
  
  @Override
  public boolean isIgnorePrefixes() {
    return getFormat6()
      .map(ViewColumnFormat6::getFlags)
      .map(f -> f.contains(ViewColumnFormat6.Flag.IgnorePrefixes))
      .orElse(false);
  }

  // *******************************************************************************
  // * Internal implementation methods
  // *******************************************************************************

  private ViewColumnFormat getFormat1() {
    return Objects.requireNonNull(this.format.getAdapter(ViewColumnFormat.class), "VIEW_COLUMN_FORMAT not read");
  }

  private ViewColumnFormat2 getFormat2() {
    return Objects.requireNonNull(this.format.getAdapter(ViewColumnFormat2.class), "VIEW_COLUMN_FORMAT2 not read");
  }

  private Optional<ViewColumnFormat6> getFormat6() {
    return Optional.ofNullable(this.format.getAdapter(ViewColumnFormat6.class));
  }
}
