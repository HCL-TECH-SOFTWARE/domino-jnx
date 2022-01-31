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

import java.util.Collection;

import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.html.EmbeddedImage.HTMLImageReader;

/**
 * Utility class to convert between rich text and HTML
 */
public interface RichTextHTMLConverter {
  /**
   * @since 1.0.3
   */
  public interface Builder {
    /**
     * Converts the document or item with the configured options.
     *
     * @return the conversion result
     */
    HtmlConversionResult convert();

    /**
     * Sets an HTML conversion option based on the {@link HtmlConvertOption}
     * enumeration.
     *
     * @param option the option to set
     * @param value  the option's value. This is usually {@code "1"}
     * @return this builder
     * @throws NullPointerException if {@code option} is null
     */
    Builder option(HtmlConvertOption option, String value);

    /**
     * Sets an HTML conversion option based on a string name.
     *
     * @param option the option to set
     * @param value  the option's value. This is usually {@code "1"}
     * @return this builder
     * @throws NullPointerException     if {@code option} is null
     * @throws IllegalArgumentException if {@code option} is empty
     */
    Builder option(String option, String value);

    /**
     * Sets multiple HTML conversion option based on the {@link HtmlConvertOption}
     * enumeration.
     *
     * @param options the options to set
     * @param value   the options' value. This is usually {@code "1"}
     * @return this builder
     * @throws NullPointerException if {@code option} is null
     */
    Builder options(Collection<HtmlConvertOption> options, String value);

    /**
     * Sets multiple HTML conversion options.
     * <p>
     * Note: these options must be {@code "key=value"} pairs. To use
     * {@link HtmlConvertOption}
     * values, use the {@link HtmlConvertOption#toOption(String)} method.
     * </p>
     *
     * @param options the key/value option pairs to set.
     * @return this builder
     */
    Builder options(Collection<String> options);

    /**
     * Sets the user agent string to be used during HTML conversion.
     *
     * @param userAgent the user agent string to set
     * @return this builder
     */
    Builder userAgent(String userAgent);
  }

  /**
   * Method to access images embedded in HTML conversion result. Compute index and
   * offset parameters
   * from the img tag path like this: 1.3E =&gt; index=1, offset=63
   *
   * @param doc        document
   * @param itemName   rich text field which is being converted
   * @param options    conversion options in {@code "key=value"} format
   * @param itemIndex  the relative item index -- if there is more than one, Item
   *                   with the same pszItemName, then this indicates which one
   *                   (zero relative)
   * @param itemOffset byte offset in the Item where the element starts
   * @param callback   callback to receive the data
   */
  void readEmbeddedImage(Document doc, String itemName, Collection<String> options,
      int itemIndex, int itemOffset, HTMLImageReader callback);

  /**
   * Renders a database as HTML, which displays its web landing page.
   *
   * @param database the database to convert
   * @return conversion result
   */
  Builder render(Database database);

  /**
   * Renders a document as HTML (including its form) and gives access to embedded
   * images.
   *
   * @param doc the document or design note to convert
   * @return conversion result
   */
  Builder render(Document doc);

  /**
   * Renders a rich text item as HTML and gives access to embedded images
   *
   * @param doc      the document containing the item to convert
   * @param itemName the name of the item to convert
   * @return conversion result
   */
  Builder renderItem(Document doc, String itemName);

}
