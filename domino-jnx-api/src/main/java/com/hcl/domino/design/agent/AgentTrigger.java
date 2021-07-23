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
package com.hcl.domino.design.agent;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;

public enum AgentTrigger implements INumberEnum<Short> {
  /** Unknown or unavailable */
  NONE(RichTextConstants.ASSISTTRIGGER_TYPE_NONE),
  /** According to time schedule */
  SCHEDULED(RichTextConstants.ASSISTTRIGGER_TYPE_SCHEDULED),
  /** When new mail delivered */
  NEWMAIL(RichTextConstants.ASSISTTRIGGER_TYPE_NEWMAIL),
  /** When documents pasted into database */
  PASTED(RichTextConstants.ASSISTTRIGGER_TYPE_PASTED),
  /** Manually executed */
  MANUAL(RichTextConstants.ASSISTTRIGGER_TYPE_MANUAL),
  /** When doc is updated */
  DOCUPDATE(RichTextConstants.ASSISTTRIGGER_TYPE_DOCUPDATE),
  /** Synchronous new mail agent executed by router */
  SYNCHNEWMAIL(RichTextConstants.ASSISTTRIGGER_TYPE_SYNCHNEWMAIL),
  /** When an server event executes */
  EVENT(RichTextConstants.ASSISTTRIGGER_TYPE_EVENT),
  /** On server start */
  SERVERSTART(RichTextConstants.ASSISTTRIGGER_TYPE_SERVERSTART);

  private final short value;

  AgentTrigger(final int value) {
    this.value = (short) value;
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