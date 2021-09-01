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
package com.hcl.domino.commons.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

  /**
   * Same as {@link String#endsWith(String)}, but ignoring case
   * 
   * @param p_sStr    string value
   * @param p_sSubStr sub string
   * @return true, if substring
   */
  public static boolean endsWithIgnoreCase(final String p_sStr, final String p_sSubStr) {
    if (p_sSubStr.length() > p_sStr.length()) {
      return false;
    }
    final int nSubStrLen = p_sSubStr.length();
    final int nStrLen = p_sStr.length();
    int nIdx = 1;
    for (int i = nSubStrLen - 1; i >= 0; i--) {
      final char cSubStr = p_sSubStr.charAt(i);
      final char cStr = p_sStr.charAt(nStrLen - nIdx);
      final char cSubStrLC = Character.toLowerCase(cSubStr);
      final char cStrLC = Character.toLowerCase(cStr);

      if (cSubStrLC != cStrLC) {
        return false;
      }

      nIdx++;
    }

    return true;
  }

  /**
   * Retrieves the first string from the input. If the input object is a
   * {@link Collection},
   * this is a stringified version of the first entry. Otherwise, it's a
   * stringified version
   * of the object itself.
   * 
   * @param val the object from which to extract the first string
   * @return the first string, or {@code null} if the object is null or an empty
   *         collection
   */
  public static String getFirstString(final Object val) {
    if (val instanceof Collection) {
      final Collection<?> c = (Collection<?>) val;
      if (c.isEmpty()) {
        return null;
      } else {
        final Object v = c.iterator().next();
        return v == null ? null : v.toString();
      }
    } else if (val == null) {
      return null;
    } else {
      return val.toString();
    }

  }

  /**
   * Returns the nth value of a string array beginning with position 0.
   * If the array does not have enough entries, the method returns an empty
   * string.
   * 
   * @param strArr array
   * @param index  array index
   * @return value
   */
  public static String getNth(final String[] strArr, final int index) {
    if (index < strArr.length) {
      return strArr[index];
    } else {
      return ""; //$NON-NLS-1$
    }
  }

  /**
   * The method checks of the specified string is <code>null</code> or an empty
   * string
   * 
   * @param strValue string
   * @return <code>true</code> if empty
   */
  public static boolean isEmpty(final String strValue) {
    return strValue == null || strValue.length() == 0;
  }

  /**
   * The method checks whether the specified string value is not null and
   * its size is greater than zero characters
   * 
   * @param str string
   * @return <code>true</code> if not empty
   */
  public static boolean isNotEmpty(final String str) {
    return str != null && str.length() > 0;
  }

  /**
   * The method concatenates a list of strings with the given delimiter
   * 
   * @param p_sStrList   string list
   * @param p_sDelimiter delimiter
   * @return concatenated strings
   */
  public static String join(final List<String> p_sStrList, final String p_sDelimiter) {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < p_sStrList.size(); i++) {
      final String sCurrStr = p_sStrList.get(i);
      if (i > 0) {
        sb.append(p_sDelimiter);
      }
      sb.append(sCurrStr);
    }
    return sb.toString();
  }

  /**
   * Method to add character to a string until it gets the right length
   * 
   * @param str          string
   * @param targetLength length for the result string
   * @param padChar      character to add
   * @param appendChars  true to append the character, false to insert them at the
   *                     beginning of the string
   * @return processed string
   */
  public static String pad(final String str, final int targetLength, final char padChar, final boolean appendChars) {
    if (str.length() >= targetLength) {
      return str;
    }
    final StringBuilder sb = new StringBuilder();
    if (appendChars) {
      sb.append(str);
      while (sb.length() < targetLength) {
        sb.append(padChar);
      }
      return sb.toString();
    } else {
      for (int i = 0; i < targetLength - str.length(); i++) {
        sb.append(padChar);
      }
      sb.append(str);
      return sb.toString();
    }
  }

  /**
   * Repeats a string a number of times
   * 
   * @param c           character
   * @param repetitions nr of repetitions
   * @return resulting string
   */
  public static String repeat(final Character c, final int repetitions) {
    if (repetitions == 0) {
      return ""; //$NON-NLS-1$
    } else if (repetitions == 1) {
      return c.toString();
    }

    final char[] chars = new char[repetitions];
    Arrays.fill(chars, c);
    return new String(chars);
  }

  /**
   * Repeats a string a number of times
   * 
   * @param str         string
   * @param repetitions nr of repetitions
   * @return resulting string
   */
  public static String repeat(final String str, final int repetitions) {
    if (repetitions == 0) {
      return ""; //$NON-NLS-1$
    } else if (repetitions == 1) {
      return str;
    }
    final char[] chars = new char[str.length() * repetitions];
    int index = 0;
    for (int i = 0; i < repetitions; i++) {
      for (int j = 0; j < str.length(); j++) {
        chars[index] = str.charAt(j);
        index++;
      }
    }
    return new String(chars);
  }

  /**
   * Method to change a value in a string array beginning with position 0. If the
   * specified array is not big enough, the method creates a new array and
   * transfers
   * all values first. The newly created array is returned. Otherwise, the method
   * just sets the new value and returns the modified array
   * 
   * @param strArr   array
   * @param index    array index
   * @param newValue new array value
   * @return modified array
   */
  public static String[] setNth(final String[] strArr, final int index, final String newValue) {
    if (index < strArr.length) {
      strArr[index] = newValue;
      return strArr;
    } else {
      final String[] newArr = new String[index + 1];
      for (int i = 0; i < strArr.length; i++) {
        newArr[i] = strArr[i];
      }
      newArr[index] = newValue;
      return newArr;
    }
  }

  /**
   * Same as {@link String#startsWith(String)}, but ignoring case
   * 
   * @param p_sStr    string value
   * @param p_sSubStr sub string
   * @return true, if substring
   */
  public static boolean startsWithIgnoreCase(final String p_sStr, final String p_sSubStr) {
    if (p_sSubStr.length() > p_sStr.length()) {
      return false;
    }
    for (int i = 0; i < p_sSubStr.length(); i++) {
      final char cSubStr = p_sSubStr.charAt(i);
      final char cStr = p_sStr.charAt(i);
      final char cSubStrLC = Character.toLowerCase(cSubStr);
      final char cStrLC = Character.toLowerCase(cStr);

      if (cSubStrLC != cStrLC) {
        return false;
      }
    }
    return true;
  }

  /**
   * Computes the number of bytes a string would allocate if converted to UTF-8
   * 
   * @param str string
   * @return number of bytes
   */
  public static int stringLengthInUTF8(final String str) {
    int retLength = 0;
    final int strLen = str.length();

    for (int i = 0; i < strLen; i++) {
      final char c = str.charAt(i);

      if (c <= 0x7F) {
        retLength++;
      } else if (c <= 0x7FF) {
        retLength += 2;
      } else if (Character.isHighSurrogate(c)) {
        retLength += 4;
        i++;
      } else {
        retLength += 3;
      }
    }
    return retLength;
  }

  /**
   * Converts the provided value to a string using its {@link Object#toString()}
   * implementation if it exists and to {@code ""} if it is {@code null}.
   * 
   * @param value the value to convert
   * @return a string representation of the value
   */
  public static String toString(final Object value) {
    return value == null ? "" : value.toString(); //$NON-NLS-1$
  }

  /**
   * Returns the nth value of a string array. The first array element is at
   * position 1.
   * If the array does not have enough entries, the method returns an empty
   * string.
   * 
   * @param strArr array
   * @param pos    position
   * @return value
   */
  public static String wWord(final String[] strArr, final int pos) {
    return StringUtil.getNth(strArr, pos - 1);
  }

  /**
   * Replaces occurrences of text in a String
   * 
   * @param txt old text
   * @param from old value
   * @param to new value
   * @param caseInsensitive true for case insensitive match
   * @return new text
   */
  public static String replaceAllMatches(String txt, String from, String to, boolean caseInsensitive) {
	  String currTxt = txt;

	  StringBuffer sb = new StringBuffer();
	  String fromQuote = Pattern.quote(from);
	  Pattern fromPattern = caseInsensitive ? Pattern.compile(fromQuote, Pattern.CASE_INSENSITIVE) : Pattern.compile(fromQuote);

	  Matcher m = fromPattern.matcher(currTxt);
	  while (m.find()) {
		  m.appendReplacement(sb, to);
	  }
	  m.appendTail(sb);
	  currTxt = sb.toString();
	  sb.setLength(0);
	  
	  return currTxt;
  }

  /**
   * Replaces occurrences of text in a String
   * 
   * @param txt old text
   * @param replacements map with old and new values
   * @param caseInsensitive true for case insensitive match
   * @return new text
   */
  public static String replaceAllMatches(String txt, Map<String,String> replacements, boolean caseInsensitive) {
	  String currTxt = txt;

	  for (Entry<String,String> currEntry : replacements.entrySet()) {
		  currTxt = replaceAllMatches(currTxt, currEntry.getKey(), currEntry.getValue(), caseInsensitive);
	  }
	  return currTxt;
  }
  
  /**
   * Replaces text matching a {@link Pattern} with text computed by a {@link Function}
   * 
   * @param txt old text
   * @param replacements mapping of {@link Pattern} and {@link Function}
   * @return new text
   */
  public static String replaceAllMatches(String txt, Map<Pattern,Function<Matcher,String>> replacements) {
	  String currTxt = txt;

	  StringBuilder sb = new StringBuilder();

	  for (Entry<Pattern,Function<Matcher,String>> currEntry : replacements.entrySet()) {
		  Pattern pattern = currEntry.getKey();
		  Function<Matcher,String> fct = currEntry.getValue();
		  
		  Matcher matcher = pattern.matcher(txt);
		  int currIdx = 0;

		  while (matcher.find()) {
			  int startIdx = matcher.start();
			  int endIdx = matcher.end();

			  if (startIdx>currIdx) {
				  String preTxt = txt.substring(currIdx, startIdx);
				  if (preTxt.length()>0) {
					  sb.append(preTxt);
				  }
			  }

			  currIdx = endIdx;
			  
			  String newTxt = fct.apply(matcher);
			  sb.append(newTxt);
		  }

		  //write remaining txt until the end
		  if (currIdx < txt.length()) {
			  String postTxt = txt.substring(currIdx, txt.length());
			  if (postTxt.length()>0) {
				  sb.append(postTxt);
			  }
		  }

		  currTxt = sb.toString();
		  sb.setLength(0);
	  }

	  return currTxt;
  }
}
