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
package com.hcl.domino.commons.design.simpleaction;

import java.util.Set;

import com.hcl.domino.design.simpleaction.SendNewsletterAction;
import com.hcl.domino.richtext.records.CDActionNewsletter;
import com.hcl.domino.richtext.records.CDActionNewsletter.Flag;

public class DefaultSendNewsletterAction implements SendNewsletterAction {
  private final String viewName;
  private final String to;
  private final String cc;
  private final String bcc;
  private final String subject;
  private final String body;
  private final long gatherThreshold;
  private final Set<CDActionNewsletter.Flag> flags;

  public DefaultSendNewsletterAction(final String viewName, final String to, final String cc, final String bcc,
      final String subject, final String body, final long gatherThreshold, final Set<Flag> flags) {
    this.viewName = viewName;
    this.to = to;
    this.cc = cc;
    this.bcc = bcc;
    this.subject = subject;
    this.body = body;
    this.gatherThreshold = gatherThreshold;
    this.flags = flags;
  }

  @Override
  public String getBcc() {
    return this.bcc;
  }

  @Override
  public String getBody() {
    return this.body;
  }

  @Override
  public String getCc() {
    return this.cc;
  }

  @Override
  public long getGatherThreshold() {
    return this.gatherThreshold;
  }

  @Override
  public String getSubject() {
    return this.subject;
  }

  @Override
  public String getTo() {
    return this.to;
  }

  @Override
  public String getViewName() {
    return this.viewName;
  }

  @Override
  public boolean isGatherDocuments() {
    return this.flags.contains(Flag.GATHER);
  }

  @Override
  public boolean isIncludeAllNotes() {
    return this.flags.contains(Flag.INCLUDEALL);
  }

  @Override
  public boolean isIncludeSummary() {
    return this.flags.contains(Flag.SUMMARY);
  }

}
