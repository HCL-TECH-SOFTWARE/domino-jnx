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
package com.hcl.domino.commons.html;

import java.util.List;

/**
 * HTMLAPIReference is the complete reference of converted HTML result.
 *
 * @author Karsten Lehmann
 */
public interface IHtmlApiReference {

  /**
   * Returns the web server command associated with the target URL
   * 
   * @return command
   */
  CommandId getCommandId();

  /**
   * The fragment part of the URL
   * 
   * @return fragment or empty string
   */
  String getFragment();

  /**
   * Returns the reference Text, e.g. the URL of anchor or img tags
   * 
   * @return text
   */
  String getReferenceText();

  /**
   * Returns a single target with the specified target type
   * 
   * @param type target type
   * @return target or null if not found
   */
  IHtmlApiUrlTargetComponent<?> getTargetByType(TargetType type);

  /**
   * Returns all parts of the reference text, e.g. tha name of the database in
   * the URL, the name of the view, the UNID of the document etc.
   * 
   * @return url target components
   */
  List<IHtmlApiUrlTargetComponent<?>> getTargets();

  /**
   * Returns how the reference is used in the HTML text (see
   * {@link ReferenceType})
   * 
   * @return type
   */
  ReferenceType getType();

}
