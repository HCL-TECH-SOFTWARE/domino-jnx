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
package com.hcl.domino.design.agent;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;

/**
 * Represents the broad document selection for running an agent.
 * 
 * @author Jesse Gallagher
 * @since 1.0.42
 */
public enum AgentTarget implements INumberEnum<Short> {
  /** Unknown or unavailable */
  NONE(RichTextConstants.ASSISTSEARCH_TYPE_NONE),
  /** All documents in database */
  ALL(RichTextConstants.ASSISTSEARCH_TYPE_ALL),
  /** New documents since last run */
  NEW(RichTextConstants.ASSISTSEARCH_TYPE_NEW),
  /** New or modified docs since last run */
  MODIFIED(RichTextConstants.ASSISTSEARCH_TYPE_MODIFIED),
  /** Selected documents */
  SELECTED(RichTextConstants.ASSISTSEARCH_TYPE_SELECTED),
  /** All documents in view */
  VIEW(RichTextConstants.ASSISTSEARCH_TYPE_VIEW),
  /** All unread documents */
  UNREAD(RichTextConstants.ASSISTSEARCH_TYPE_UNREAD),
  /** Prompt user */
  PROMPT(RichTextConstants.ASSISTSEARCH_TYPE_PROMPT),
  /** Works on the selectable object */
  UI(RichTextConstants.ASSISTSEARCH_TYPE_UI);

  private final short value;

  AgentTarget(final short value) {
    this.value = value;
  }

  @Override
  public long getLongValue() {
    return this.value;
  }

  @Override
  public Short getValue() {
    return this.value;
  }
}