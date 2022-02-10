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
package com.hcl.domino.html;

import java.util.List;

import com.hcl.domino.data.IAdaptable;

/**
 * Container for a rich text &rarr; HTML conversion result, returning the HTML
 * source code and information
 * about contained references to external targets (e.g. links / images)
 *
 * @author Karsten Lehmann
 */
public interface HtmlConversionResult extends IAdaptable {

  /**
   * Returns the HTML code of the conversion result
   *
   * @return html
   */
  String getHtml();

  /**
   * Gives access to all embedded images
   *
   * @return images
   */
  List<EmbeddedImage> getImages();

}
