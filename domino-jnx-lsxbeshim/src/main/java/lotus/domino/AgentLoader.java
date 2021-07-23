package lotus.domino;

import java.io.InputStream;
import java.lang.reflect.Method;

public class AgentLoader extends ClassLoader {
	public AgentLoader() {

	}

	@Override
	public synchronized Class<?> loadClass(String className, boolean resolveClass) throws ClassNotFoundException {
		return super.loadClass(className, resolveClass);
	}

	public void addAttachment(String paramString, byte[] paramArrayOfbyte) {
		// NOP
	}

	private native boolean checkECLMQ(long paramLong);
	private native boolean getBreakStatus(int paramInt);
	
	public void runWebService(String paramString, long paramLong1, int paramInt1, boolean paramBoolean1, long paramLong2, boolean paramBoolean2, int paramInt2, Object paramObject) {
		// NOP
	}
	public void runAgent(String paramString, long paramLong1, int paramInt1, boolean paramBoolean1, long paramLong2, boolean paramBoolean2, int paramInt2, Object paramObject) {
		// NOP
	}
	
	@Override
	public InputStream getResourceAsStream(String resName) {
		return super.getResourceAsStream(resName);
	}
	
	@SuppressWarnings("rawtypes")
	public Thread setupAgent(String paramString, Class paramClass, long paramLong1, int paramInt1, boolean paramBoolean1, long paramLong2, boolean paramBoolean2, int paramInt2, Object paramObject) {
		// NOP
		return null;
	}
	public boolean isMacOSX() {
		// NOP
		return false;
	}
	public void runMacAgent(String paramString, int paramInt1, int paramInt2, boolean paramBoolean1, int paramInt3, boolean paramBoolean2) {
		// NOP
	}
	
	public static void MacSyncAgent() {
		// NOP
	}
	private static void MacAgentDone() {
		// NOP
	}
	public Object getServerObject() {
		// NOP
		return null;
	}
	public Object setServerObject(Object paramObject) {
		// NOP
		return null;
	}
	private static Method initializeClearCacheMethod() {
		// NOP
		return null;
	}
	private void resourceBundleClearCache() {
		// NOP
	}
}
