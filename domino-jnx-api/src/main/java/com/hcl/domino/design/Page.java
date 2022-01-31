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
package com.hcl.domino.design;

import java.util.Optional;

import com.hcl.domino.richtext.structures.ColorValue;

/**
 * Represents a Page design element.
 * 
 * @author Jesse Gallagher
 * @since 1.0.34
 */
public interface Page extends GenericPageElement.ScriptablePageElement<Page>, DesignElement.ThemeableClassicElement, DesignElement.AutoFrameElement,
  DesignElement.NamedDesignElement {
  /**
   * Represents settings related to the rendering of the page when rendered
   * using the classic web renderer.
   * 
   * @author Jesse Gallagher
   * @since 1.0.34
   */
  interface WebRenderingSettings {
    
    /**
     * Determines whether the Domino server should render full rich content (e.g. styled text and editable
     * fields) when this page is displayed on the web.
     * 
     * @return {@code true} to render rich content on the web;
     *         {@code false} otherwise
     */
    boolean isRenderRichContentOnWeb();
    
    /**
     * Retrieves the content type to use when displaying this page on the web when
     * {@link #isRenderRichContentOnWeb()} is {@code false}.
     * 
     * <p>When the page should be rendered as {@code text/html}, this value will be an {@code Optional}
     * containing an empty string.</p>
     * 
     * @return an {@link Optional} describing the content type when rendering this page on the web,
     *         or an empty one if {@link #isRenderRichContentOnWeb()} is {@code true}
     * @since 1.0.33
     */
    Optional<String> getWebMimeType();
    
    /**
     * Retrieves the character set to use when displaying documents using this page on the web, if one is
     * set
     * 
     * @return an {@link Optional} describing the textual name of the character set to use when displaying
     *         on the web, or an empty one if the server should use the default
     * @since 1.0.33
     */
    Optional<String> getWebCharset();
    
    /**
     * Retrieves the color used for active links when using HTML controls.
     * 
     * @return a {@link ColorValue} representing the active link color
     */
    ColorValue getActiveLinkColor();
    
    /**
     * Retrieves the color used for unvisited links when using HTML controls.
     * 
     * @return a {@link ColorValue} representing the unvisited link color
     */
    ColorValue getUnvisitedLinkColor();
    
    /**
     * Retrieves the color used for visited links when using HTML controls.
     * 
     * @return a {@link ColorValue} representing the visited link color
     */
    ColorValue getVisitedLinkColor();
  }
  
  /**
   * Retrieves an object that provides a view onto this page's web rendering
   * settings.
   * 
   * @return a {@link WebRenderingSettings} instance
   * @since 1.0.34
   */
  WebRenderingSettings getWebRenderingSettings();

  
  /**
   * Determines whether the form should choose a field to use for initial focus on open in the UI.
   * 
   * @return {@code true} to use initial focus behavior;
   *         {@code false} otherwise
   * @since 1.0.34
   */
  boolean isUseInitialFocus();
  
  /**
   * Determines whether the form should focus onto a field when the F6 key is pressed in the
   * Notes client UI.
   * 
   * @return {@code true} to focus on F6 press;
   *         {@code false} otherwise
   * @since 1.0.34
   */
  boolean isFocusOnF6();
}
