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
package it.com.hcl.domino.test.util;

import com.ibm.commons.util.StringUtil;

public enum ITUtil {
  ;
  
  /**
   * Converts Windows-style CRLF line endings to just LF.
   * 
   * @param value the value to convert
   * @return the converted value
   * @since 1.0.43
   */
  public static String toLf(String value) {
    return StringUtil.toString(value).replace("\r\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
  }
  
  /**
   * Converts Windows- and Unix-style line endings to just CR.
   * 
   * @param value the value to convert
   * @return the converted value
   * @since 1.0.45
   */
  public static String toCr(String value) {
    return StringUtil.toString(value).replaceAll("(\\r)?\\n", "\r"); //$NON-NLS-1$ //$NON-NLS-2$
  }
}
