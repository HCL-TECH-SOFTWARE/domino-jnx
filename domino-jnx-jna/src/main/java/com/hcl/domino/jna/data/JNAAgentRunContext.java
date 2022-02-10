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
package com.hcl.domino.jna.data;

import java.io.Writer;
import java.util.Optional;

import com.hcl.domino.data.Agent.AgentRunContext;
import com.hcl.domino.data.Document;

public class JNAAgentRunContext implements AgentRunContext {
	private boolean m_checkSecurity;
	private boolean m_reopenDbAsSigner;
	private Writer m_stdOut;
	private int m_timeoutSeconds;
	private Document m_documentContext;
	private int m_paramDocId;
	private String m_userName;

	@Override
	public boolean isCheckSecurity() {
		return m_checkSecurity;
	}

	@Override
	public AgentRunContext setCheckSecurity(boolean checkSecurity) {
		this.m_checkSecurity = checkSecurity;
		return this;
	}

	@Override
	public boolean isReopenDbAsSigner() {
		return m_reopenDbAsSigner;
	}

	@Override
	public AgentRunContext setReopenDbAsSigner(boolean reopenAsSigner) {
		this.m_reopenDbAsSigner = reopenAsSigner;
		return this;
	}

	@Override
	public Optional<Writer> getOutputWriter() {
		return Optional.ofNullable(m_stdOut);
	}

	@Override
	public AgentRunContext setOutputWriter(Writer writer) {
		this.m_stdOut = writer;
		return this;
	}

	@Override
	public int getTimeoutSeconds() {
		return m_timeoutSeconds;
	}

	@Override
	public AgentRunContext setTimeoutSeconds(int timeoutSeconds) {
		this.m_timeoutSeconds = timeoutSeconds;
		return this;
	}

	@Override
	public Optional<Document> getDocumentContext() {
		return Optional.ofNullable(m_documentContext);
	}

	@Override
	public AgentRunContext setDocumentContext(Document doc) {
		m_documentContext = doc;
		return this;
	}

	@Override
	public int getParamDocId() {
		return m_paramDocId;
	}

	@Override
	public AgentRunContext setParamDocId(int paramDocId) {
		this.m_paramDocId = paramDocId;
		return this;
	}

	@Override
	public String getUsername() {
		return m_userName;
	}

	@Override
	public AgentRunContext setUsername(String sessionEffectiveName) {
		m_userName = sessionEffectiveName;
		return this;
	}

}
