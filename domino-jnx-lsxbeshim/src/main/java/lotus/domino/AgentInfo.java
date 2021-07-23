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
package lotus.domino;

import lotus.notes.AgentSecurityContext;

public class AgentInfo {
	@SuppressWarnings("rawtypes")
	AgentInfo(String name, Class agentClass, long timeout, int runctx, boolean restricted, long docID, boolean redirectAgentOutput, int httpTID, ClassLoader classLoader, Object serviceContext) {
		
	}
	String getAgentName() {
		// NOP
		return null;
	}
	@SuppressWarnings("rawtypes")
	Class getAgentClass() {
		// NOP
		return null;
	}
	long getTimeout() {
		// NOP
		return 0;
	}
	int getRunContext() {
		// NOP
		return 0;
	}
	boolean isRestricted() {
		// NOP
		return false;
	}
	long getDocID() {
		// NOP
		return 0;
	}
	boolean redirectAgentOutput() {
		// NOP
		return false;
	}
	AgentSecurityContext getSecurityContext() {
		// NOP
		return null;
	}
	void newSecurityContext(ThreadGroup paramThreadGroup) {
		// NOP
	}
	Object newInstance() throws InstantiationException, IllegalAccessException {
		// NOP
		return null;
	}
	
	// void setSession(Session)
	// Session getSession()
	
	public boolean checkECL(int paramInt1, int paramInt2, String paramString) {
		// NOP
		return false;
	}
	int getHttpTID() {
		// NOP
		return 0;
	}
	
	private native boolean checkECL(long paramLong, int paramInt1, int paramInt2, String paramString);
	
	ClassLoader getClassLoader() {
		// NOP
		return null;
	}
	
	void setHttpStatusCode(int paramInt) {
		// NOP
	}
	private native void setHttpStatusCode(long paramLong, int paramInt);
	Object getServiceContext() {
		// NOP
		return null;
	}
}
