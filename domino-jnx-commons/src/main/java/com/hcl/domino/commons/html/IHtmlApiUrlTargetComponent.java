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
package com.hcl.domino.commons.html;

/**
 * Returns information about one component of a URL found in the richtext-HTML
 * conversion result.
 *
 * @author Karsten Lehmann
 * @param <T> value type
 */
public interface IHtmlApiUrlTargetComponent<T> {

  /**
   * Returns the type of url component
   * 
   * @return type
   */
  TargetType getType();

  /**
   * Returns the value of the URL component
   * 
   * @return value
   */
  T getValue();

  /**
   * Returns the class of the value returned by {@link #getValue()}, e.g. String
   * for
   * {@link TargetType#FIELD} or {@link TargetType#FIELDOFFSET}
   * 
   * @return value class
   */
  Class<T> getValueClass();

}
