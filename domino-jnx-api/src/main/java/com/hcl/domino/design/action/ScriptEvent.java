package com.hcl.domino.design.action;

/**
 * Represents an individual event/code definition.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public interface ScriptEvent {
  EventId getEventId();
  String getScript();
  /**
   * Determines if the event runs in the Notes client or on the web.
   * 
   * @return {@code true} if the event targets Notes;
   *         {@code false} if it targets the web
   */
  boolean isClient();
}