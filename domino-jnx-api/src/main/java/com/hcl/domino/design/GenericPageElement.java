package com.hcl.domino.design;

import java.util.Collection;
import java.util.List;

import com.hcl.domino.design.action.ScriptEvent;

/**
 * Represents properties common between Forms, Subforms, and Pages.
 * 
 * @author Jesse Gallagher
 * @since 1.0.34
 */
public interface GenericPageElement<T extends GenericPageElement<T>> extends DesignElement.NamedDesignElement, DesignElement.ActionBarElement {
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
  
  /**
   * Retrieves a collection of the JavaScript events associated with the form or page.
   *  
   * @return a {@link Collection} of {@link ScriptEvent} instances
   * @since 1.0.34
   */
  Collection<ScriptEvent> getJavaScriptEvents();
}
