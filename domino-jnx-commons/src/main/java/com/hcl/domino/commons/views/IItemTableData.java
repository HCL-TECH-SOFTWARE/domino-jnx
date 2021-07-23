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

import java.util.List;
import java.util.Map;

import com.hcl.domino.data.TypedAccess;

/**
 * This is the structure used for item (field) summary buffers, received from
 * NSF searches
 * if {@code Search#SUMMARY} is used and for view read operations with flag
 * {@code ReadMask#SUMMARY}.<br>
 * <br>
 * The table contains a list of item names with their data type and value.
 *
 * @author Karsten Lehmann
 */
public interface IItemTableData extends TypedAccess, IItemValueTableData {

  /**
   * Converts the values to a Java {@link Map}
   * 
   * @return data as map
   */
  Map<String, Object> asMap();

  /**
   * Converts the values to a Java {@link Map}
   * 
   * @param decodeLMBCS true to convert {@code LMBCSString} objects and lists to
   *                    Java Strings
   * @return data as map
   */
  Map<String, Object> asMap(boolean decodeLMBCS);

  /**
   * @deprecated internal method, no need to call this in client code
   */
  @Deprecated
  void free();

  /**
   * Returns the names of the decoded items (programmatic column names in case of
   * collection data)
   * 
   * @return names
   */
  @Override
  List<String> getItemNames();

  /**
   * Method to check whether the table contains the specified item
   * 
   * @param itemName item name
   * @return true if item exists
   */
  @Override
  boolean hasItem(String itemName);

  /**
   * @deprecated internal method, no need to call this in client code
   * @return true if freed
   */
  @Deprecated
  boolean isFreed();

}
