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
	private final DominoViewColumnFormat format;
	
	public SortConfigurationImpl(DominoViewColumnFormat format) {
		this.format = format;
	}

	@Override
	public boolean isResortToView() {
		return getFormat1().getFlags().contains(ViewColumnFormat.Flag.ResortToView);
	}

	@Override
	public boolean isSecondaryResort() {
		return getFormat1().getFlags().contains(ViewColumnFormat.Flag.SecondResort);
	}

	@Override
	public boolean isSecondaryResortDescending() {
		return getFormat1().getFlags().contains(ViewColumnFormat.Flag.SecondResortDescending);
	}

	@Override
	public boolean isSortPermuted() {
		return getFormat1().getFlags2().contains(ViewColumnFormat.Flag2.SortPermute);
	}

	@Override
	public boolean isResortAscending() {
		return getFormat1().getFlags().contains(ViewColumnFormat.Flag.ResortAscending);
	}

	@Override
	public boolean isResortDescending() {
		return getFormat1().getFlags().contains(ViewColumnFormat.Flag.ResortDescending);
	}

	@Override
	public Optional<String> getResortToViewUnid() {
		UNID unid = getFormat2().getResortToViewUNID();
		if(unid.isUnset()) {
			return Optional.empty();
		} else {
			return Optional.of(unid.toUnidString());
		}
	}

	@Override
	public int getSecondResortColumnIndex() {
		return getFormat2().getSecondResortColumnIndex();
	}

	@Override
	public boolean isSorted() {
		return getFormat1().getFlags().contains(ViewColumnFormat.Flag.Sort);
	}

	@Override
	public boolean isCategory() {
		return getFormat1().getFlags().contains(ViewColumnFormat.Flag.SortCategorize);
	}

	@Override
	public boolean isSortedDescending() {
		return getFormat1().getFlags().contains(ViewColumnFormat.Flag.SortDescending);
	}

	@Override
	public boolean isDeferResortIndexing() {
		return getFormat6()
			.map(fmt -> fmt.getFlags().contains(ViewColumnFormat6.Flag.BuildCollationOnDemand))
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
