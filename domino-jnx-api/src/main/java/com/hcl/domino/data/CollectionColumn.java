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

import com.hcl.domino.design.format.ViewColumnFormat;

public interface CollectionColumn {
	enum TotalType {
		None, Total, Average, AveragePerSubcategory,
		PercentOfParentCategory, Percent
	}
	
	interface SortConfiguration {
		boolean isSorted();
		
		boolean isSortedDescending();
		
		boolean isCategory();
		
		boolean isSortPermuted();

		int getSecondResortColumnIndex();
		
		/**
		 * @return an {@link Optional} describing the UNID of the view to switch to when
		 *      resorting, or an empty one if this is unset 
		 */
		Optional<String> getResortToViewUnid();
		
		boolean isDeferResortIndexing();
		
		boolean isResortAscending();
		
		boolean isResortDescending();
		
		boolean isResortToView();
		
		boolean isSecondaryResort();
		
		boolean isSecondaryResortDescending();
	}

	String getItemName();
	
	String getTitle();
	
	int getPosition();
	
	int getColumnValuesIndex();
	
	boolean isConstant();
	
	String getFormula();
	
	/**
	 * @return a {@link SortConfiguration} instance representing the settings of this column
	 * @since 1.0.27
	 */
	SortConfiguration getSortConfiguration();
	
	boolean isHidden();
	
	boolean isResponse();
	
	boolean isIcon();
	
	boolean isResize();
	
	boolean isShowTwistie();
	
	/**
	 * @return the total-row value to compute
	 * @since 1.0.27
	 */
	TotalType getTotalType();
	
	/**
	 * @return the delimiter to use when displaying multiple values
	 * @since 1.0.27
	 */
	ViewColumnFormat.ListDelimiter getListDisplayDelimiter();
	
	boolean isHideDetailRows();
	
	/**
	 * @return {@link true} if the column's hide-when formula should be used; {@code false} otherwise
	 * @since 1.0.27
	 */
	boolean isUseHideWhen();
	
	/**
	 * Retrieves the hide-when formula for the column.
	 * 
	 * <p>Note: this formula may or may not be used; use {@link #isUseHideWhen()} to determine
	 * whether it is enabled.</p>
	 * 
	 * @return the hide-when formula specified for the column.
	 * @since 1.0.27
	 */
	String getHideWhenFormula();
	
	/**
	 * Retrieves the display width of this column.
	 * 
	 * <p>Note: this value reflects the storage mechanism, which differs from the display in Designer.
	 * The stored value is the count of 1/8 average character widths to go by, while Designer adjusts
	 * this by full characters. Accordingly, this value will be about 8 times larger than the value
	 * shown in Designer.</p> 
	 * 
	 * @return the display width of this column, in units of 1/8 of the average character width
	 * @since 1.0.28
	 */
	int getDisplayWidth();
}
