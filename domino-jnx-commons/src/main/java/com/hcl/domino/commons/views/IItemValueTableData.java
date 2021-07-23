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
package com.hcl.domino.commons.views;

import java.util.Calendar;
import java.util.List;

import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.ItemDataType;

/**
 * This is the structure received from NSF searches
 * if both {@link Search#NOITEMNAMES} and {@link Search#SUMMARY} are set 
 * and for view read operations with flag
 * {@link ReadMask#SUMMARYVALUES}.<br>
 * <br>
 * The table contains a list of item data types and value. In contrast to
 * {@link IItemTableData}, the {@link IItemValueTableData} does not contain
 * column item names.
 * 
 * @author Karsten Lehmann
 */
public interface IItemValueTableData {

	/**
	 * Returns the decoded item value, with the following types:<br>
	 * <ul>
	 * <li>{@link ItemDataType#TYPE_TEXT} - {@link String}</li>
	 * <li>{@link ItemDataType#TYPE_TEXT_LIST} - {@link List} of {@link String}</li>
	 * <li>{@link ItemDataType#TYPE_NUMBER} - {@link Double}</li>
	 * <li>{@link ItemDataType#TYPE_NUMBER_RANGE} - {@link List} with {@link Double} values for number lists or double[] values for number ranges (not sure if Notes views really supports them)</li>
	 * <li>{@link ItemDataType#TYPE_TIME} - {@link Calendar}; if {@link #setPreferNotesTimeDates(boolean)} is called, we return {@link DominoDateTime} instead</li>
	 * <li>{@link ItemDataType#TYPE_TIME_RANGE} - {@link List} with {@link Calendar} values for datetime lists or Calendar[] values for datetime ranges; if {@link #setPreferNotesTimeDates(boolean)} is called, we return {@link DominoDateTime} and {@link DominoDateRange} instead</li>
	 * </ul>
	 * 
	 * @param index item index between 0 and {@link #getItemsCount()}
	 * @return value or null if unknown type
	 */
	Object getItemValue(int index);
	
	/**
	 * Returns the data type of an item value by its index, e.g. {@link ItemDataType#TYPE_TEXT},
	 * {@link ItemDataType#TYPE_TEXT_LIST}, {@link ItemDataType#TYPE_NUMBER},
	 * {@link ItemDataType#TYPE_NUMBER_RANGE}
	 * 
	 * @param index item index between 0 and {@link #getItemsCount()}
	 * @return data type
	 */
	int getItemDataType(int index);
	
	/**
	 * Returns the number of decoded items
	 * 
	 * @return number
	 */
	int getItemsCount();
	
	/**
	 * Sets whether methods like {@link #getItemValue(int)} should return {@link DominoDateTime}
	 * instead of {@link Calendar} and {@link DominoDateRange} instead of Calendar[].
	 * 
	 * @param b true to prefer NotesTimeDate (false by default)
	 */
	void setPreferNotesTimeDates(boolean b);
	
	/**
	 * Returns whether methods like {@link #getItemValue(int)} should return {@link DominoDateTime}
	 * instead of {@link Calendar} and {@link DominoDateRange} instead of Calendar[].
	 * 
	 * @return true to prefer NotesTimeDate
	 */
	boolean isPreferNotesTimeDates();
}
