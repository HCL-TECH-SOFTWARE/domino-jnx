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