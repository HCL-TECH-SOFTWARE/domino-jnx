package com.hcl.domino.commons.design.action;

import com.hcl.domino.design.action.JavaScriptActionContent;
import com.hcl.domino.design.format.HtmlEventId;

/**
 * Default implementation of {@link JavaScriptActionContent.ScriptEvent}.
 * 
 * @author Jesse Gallagher
 * @since 1.0.33
 */
public class DefaultJavaScriptEvent implements JavaScriptActionContent.ScriptEvent {
  private final HtmlEventId eventId;
  private final boolean client;
  private final String script;

  public DefaultJavaScriptEvent(HtmlEventId eventId, boolean client, String script) {
    this.eventId = eventId;
    this.client = client;
    this.script = script;
  }
  
  
  @Override
  public HtmlEventId getEventId() {
    return eventId;
  }

  @Override
  public String getScript() {
    return script;
  }

  @Override
  public boolean isClient() {
    return client;
  }

  @Override
  public String toString() {
    return String.format("DefaultJavaScriptEvent [eventId=%s, client=%s, script=%s]", eventId, client, script); //$NON-NLS-1$
  }

}
