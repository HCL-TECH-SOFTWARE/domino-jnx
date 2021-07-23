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

import java.util.Set;

import com.hcl.domino.commons.design.DefaultComputableValue;
import com.hcl.domino.design.ComputableValue;
import com.hcl.domino.design.action.SendMailAction;
import com.hcl.domino.richtext.records.CDActionSendMail.Flag;

public class DefaultSendMailAction implements SendMailAction {
	private final String to;
	private final String cc;
	private final String bcc;
	private final String subject;
	private final String body;
	private final Set<Flag> flags;

	public DefaultSendMailAction(String to, String cc, String bcc, String subject, String body, Set<Flag> flags) {
		this.to = to;
		this.cc = cc;
		this.bcc = bcc;
		this.subject = subject;
		this.body = body;
		this.flags = flags;
	}
	
	@Override
	public boolean isIncludeDocument() {
		return flags.contains(Flag.INCLUDEDOC);
	}

	@Override
	public boolean isIncludeDocLink() {
		return flags.contains(Flag.INCLUDELINK);
	}

	@Override
	public boolean isSaveMailDocument() {
		return flags.contains(Flag.SAVEMAIL);
	}

	@Override
	public ComputableValue getTo() {
		return new DefaultComputableValue(to, flags.contains(Flag.TOFORMULA));
	}

	@Override
	public ComputableValue getCc() {
		return new DefaultComputableValue(cc, flags.contains(Flag.CCFORMULA));
	}

	@Override
	public ComputableValue getBcc() {
		return new DefaultComputableValue(bcc, flags.contains(Flag.BCCFORMULA));
	}

	@Override
	public ComputableValue getSubject() {
		return new DefaultComputableValue(subject, flags.contains(Flag.SUBJECTFORMULA));
	}

	@Override
	public String getBody() {
		return body;
	}

}
