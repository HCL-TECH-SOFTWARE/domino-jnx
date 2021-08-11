package com.hcl.domino.design.action;

import java.util.Collection;

import com.hcl.domino.design.format.HtmlEventId;

/**
 * Represents the contents of a JavaScript-type action.
 *
 * @author Jesse Gallagher
 * @since 1.0.33
 */
public interface JavaScriptActionContent extends ActionContent {
  /**
   * Represents an individual event/code definition.
   * 
   * @author Jesse Gallagher
   * @since 1.0.32
   */
  interface ScriptEvent {
    HtmlEventId getEventId();
    String getScript();
    /**
     * Determines if the event runs in the Notes client or on the web.
     * 
     * @return {@code true} if the event targets Notes;
     *         {@code false} if it targets the web
     */
    boolean isClient();
  }
  /**
   * Retrieves a collections of the action's specified events.
   *  
   * @return a {@link Collection} of {@link ScriptEvent} instances
   */
  Collection<ScriptEvent> getEvents();
}
