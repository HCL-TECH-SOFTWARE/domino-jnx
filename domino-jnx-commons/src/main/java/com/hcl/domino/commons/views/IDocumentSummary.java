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

import com.hcl.domino.data.DominoDateTime;

public interface IDocumentSummary {

  /**
   * Convenience function that converts a summary value to a {@link Calendar}
   * 
   * @param itemName     item name, case insensitive
   * @param defaultValue default value if column is empty or is not a Calendar
   * @return calendar value or null
   */
  Calendar getAsCalendar(String itemName, Calendar defaultValue);

  /**
   * Convenience function that converts a summary value to a {@link Calendar} list
   * 
   * @param itemName     item name, case insensitive
   * @param defaultValue default value if column is empty or is not a Calendar
   * @return calendar list value or null
   */
  List<Calendar> getAsCalendarList(String itemName, List<Calendar> defaultValue);

  /**
   * Convenience function that converts a summary value to a double
   * 
   * @param itemName     item name, case insensitive
   * @param defaultValue default value if column is empty or is not a number
   * @return double
   */
  Double getAsDouble(String itemName, Double defaultValue);

  /**
   * Convenience function that converts a summary value to a double list
   * 
   * @param itemName     item name, case insensitive
   * @param defaultValue default value if column is empty or is not a number
   * @return double list
   */
  List<Double> getAsDoubleList(String itemName, List<Double> defaultValue);

  /**
   * Convenience function that converts a summary value to an integer
   * 
   * @param itemName     item name, case insensitive
   * @param defaultValue default value if column is empty or is not a number
   * @return integer
   */
  Integer getAsInteger(String itemName, Integer defaultValue);

  /**
   * Convenience function that converts a summary value to an integer list
   * 
   * @param itemName     item name, case insensitive
   * @param defaultValue default value if column is empty or is not a number
   * @return integer list
   */
  List<Integer> getAsIntegerList(String itemName, List<Integer> defaultValue);

  /**
   * Convenience function that converts a summary value to a long value
   * 
   * @param itemName     item name, case insensitive
   * @param defaultValue default value if column is empty or is not a number
   * @return long
   */
  Long getAsLong(String itemName, Long defaultValue);

  /**
   * Convenience function that converts a summary value to a list of long values
   * 
   * @param itemName     item name, case insensitive
   * @param defaultValue default value if column is empty or is not a number
   * @return long list
   */
  List<Long> getAsLongList(String itemName, List<Long> defaultValue);

  /**
   * Convenience function that converts a summary value to an abbreviated name
   * 
   * @param itemName item name, case insensitive
   * @return name or null
   */
  String getAsNameAbbreviated(String itemName);

  /**
   * Convenience function that converts a summary value to an abbreviated name
   * 
   * @param itemName     item name, case insensitive
   * @param defaultValue value to be used of item not found
   * @return name or default value
   */
  String getAsNameAbbreviated(String itemName, String defaultValue);

  /**
   * Convenience function that converts a summary value to a list of abbreviated
   * names
   * 
   * @param itemName item name, case insensitive
   * @return names or null
   */
  List<String> getAsNamesListAbbreviated(String itemName);

  /**
   * Convenience function that converts a summary value to a list of abbreviated
   * names
   * 
   * @param itemName     item name, case insensitive
   * @param defaultValue default value if column is empty or is not a string or
   *                     string list
   * @return names or default value if not found
   */
  List<String> getAsNamesListAbbreviated(String itemName, List<String> defaultValue);

  /**
   * Convenience function that converts a summary value to a string
   * 
   * @param itemName     item name, case insensitive
   * @param defaultValue default value if value is empty or is not a string
   * @return string value or null
   */
  String getAsString(String itemName, String defaultValue);

  /**
   * Convenience function that converts a summary value to a string list
   * 
   * @param itemName     item name, case insensitive
   * @param defaultValue default value if column is empty or is not a string or
   *                     string list
   * @return string list value or null
   */
  List<String> getAsStringList(String itemName, List<String> defaultValue);

  /**
   * Convenience function that converts a summary value to a
   * {@link DominoDateTime}
   * 
   * @param itemName     item name, case insensitive
   * @param defaultValue default value if column is empty or is not a
   *                     DominoDateTime
   * @return DominoDateTime value or null
   */
  DominoDateTime getAsTimeDate(String itemName, DominoDateTime defaultValue);

  /**
   * Convenience function that converts a summary value to a
   * {@link DominoDateTime} list
   * 
   * @param itemName     item name, case insensitive
   * @param defaultValue default value if column is empty or is not a
   *                     DominoDateTime list
   * @return DominoDateTime list value or null
   */
  List<DominoDateTime> getAsTimeDateList(String itemName, List<DominoDateTime> defaultValue);

}
