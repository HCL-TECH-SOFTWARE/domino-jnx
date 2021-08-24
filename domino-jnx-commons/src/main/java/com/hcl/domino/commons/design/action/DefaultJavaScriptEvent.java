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
package com.hcl.domino.commons.design.action;

import com.hcl.domino.design.action.ScriptEvent;
import com.hcl.domino.design.format.HtmlEventId;

/**
 * Default implementation of {@link ScriptEvent}.
 * 
 * @author Jesse Gallagher
 * @since 1.0.33
 */
public class DefaultJavaScriptEvent implements ScriptEvent {
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
