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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.hcl.domino.design.action.EventId;
import com.hcl.domino.design.action.ScriptEvent;
import com.hcl.domino.richtext.records.CDResource;

/**
 * Represents properties common between Forms, Subforms, and Pages.
 * 
 * @author Jesse Gallagher
 * @since 1.0.34
 */
public interface GenericPageElement<T extends GenericPageElement<T>> extends DesignElement {
  /**
   * This specialization of {@link GenericPageElement} represents elements
   * that can have action bar entries and page scripts.
   * 
   * @author Jesse Gallagher
   * @since 1.0.34
   */
  interface ScriptablePageElement<T extends GenericPageElement<T>> extends GenericPageElement<T>, DesignElement.ActionBarElement {
    /**
     * Retrieves a collection of the JavaScript events associated with the form or page.
     *  
     * @return a {@link Collection} of {@link ScriptEvent} instances
     * @since 1.0.34
     */
    Collection<ScriptEvent> getJavaScriptEvents();
    
    /**
     * Retrieves the element-global LotusScript associated with the form or page, other than
     * the "Globals" portion.
     * 
     * @return a {@link String} representing the IDE-formatted LotusScript for the element
     * @since 1.0.34
     * @see #getLotusScriptGlobals()
     */
    String getLotusScript();
    
    /**
     * Retrieves the "Globals" portion of the LotusScript associated with the form or page.
     * 
     * @return a {@link String} representing the IDE-formatted LotusScript Globals content
     *         for the element
     * @since 1.0.41
     * @see #getLotusScript()
     */
    String getLotusScriptGlobals();
    
    /**
     * Retrieves the window-title formula for the form or page, if specified.
     * 
     * @return an {@link Optional} describing the window-title formula if present,
     *         or an empty one if this has not been specified
     * @since 1.0.34
     */
    Optional<String> getWindowTitleFormula();
    
    /**
     * Retrieves the target-frame formula for the form or page, if specified.
     * 
     * @return an {@link Optional} describing the target-frame formula if present,
     *         or an empty one if this has not been specified
     * @since 1.0.34
     */
    Optional<String> getTargetFrameFormula();
    
    /**
     * Retrieves the HTML head content formula for the form or page, if specified.
     * 
     * @return an {@link Optional} describing the HTML head content formula if present,
     *         or an empty one if this has not been specified
     * @since 1.0.34
     */
    Optional<String> getHtmlHeadContentFormula();
    
    /**
     * Retrieves the HTML body attributes formula for the form or page, if specified.
     * 
     * @return an {@link Optional} describing the HTML body attributes formula if present,
     *         or an empty one if this has not been specified
     * @since 1.0.34
     */
    Optional<String> getHtmlBodyAttributesFormula();
    
    /**
     * Retrieves a list of stylesheet references included in the HTML head.
     * 
     * @return a {@link List} of {@link CDResource} objects
     * @since 1.0.34
     */
    List<CDResource> getIncludedStyleSheets();
    
    /**
     * Retrieves the formulas for UI events that are specified for this form or page.
     * 
     * @return a {@link Map} of {@link EventId} instances to corresponding formulas
     * @since 1.0.34
     */
    Map<EventId, String> getFormulaEvents();
  }
  
  /**
   * Retrieves the body of the form or subform as a list of rich-text entities.
   * 
   * @return a {@link List} of rich-text entities
   * @since 1.0.34
   */
  List<?> getBody();

  /**
   * Determines whether pass-through-HTML text in the form or subform should
   * be rendered in the Notes client.
   * 
   * @return {@code true} to attempt to render pass-through-HTML in the client;
   *         {@code false} to display it as HTML markup
   * @since 1.0.33
   */
  boolean isRenderPassThroughHtmlInClient();
}
