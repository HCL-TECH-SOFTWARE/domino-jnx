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
