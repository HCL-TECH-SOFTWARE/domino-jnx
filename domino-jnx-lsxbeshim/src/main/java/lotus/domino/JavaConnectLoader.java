package lotus.domino;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import lotus.notes.AgentSecurityContext;

public class JavaConnectLoader extends ClassLoader {
	private JavaConnectLoader(long paramLong) {
		// NOP
	}

	public AgentSecurityContext getSecurityContext() {
		// NOP
		return null;
	}

	private static JavaConnectLoader currentJCLoader() {
		// NOP
		return null;
	}

	public static AgentSecurityContext currentSecurityContext() {
		// NOP
		return null;
	}

	private static native String getExecutableDirectory();

	private static native boolean getRestrictedAccess(long paramLong);

	private static void setAccessible(AccessibleObject paramAccessibleObject) throws Exception {
		// NOP
	}

	private static ClassLoader setCurrentLoader(ClassLoader paramClassLoader) throws Exception {
		// NOP
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Class findClass(String paramString1, String paramString2) {
		// NOP
		return null;
	}

	public static JavaConnectLoader create(long paramLong) {
		// NOP
		return null;
	}

	@SuppressWarnings("rawtypes")
	public static Field[] getFields(Class paramClass) {
		// NOP
		return null;
	}

	@SuppressWarnings("rawtypes")
	public static Method getMethod(Class paramClass, String paramString, Class[] paramArrayOfClass)
			throws NoSuchMethodException {
		// NOP
		return null;
	}

	@SuppressWarnings("rawtypes")
	public static Method[] getMethods(Class paramClass) {
		// NOP
		return null;
	}

	public Object invoke(Method paramMethod, Object paramObject, Object[] paramArrayOfObject) throws Throwable {
		// NOP
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Object newInstance(Constructor paramConstructor, Object[] paramArrayOfObject) throws Throwable {
		// NOP
		return null;
	}

	public boolean checkECL(int paramInt1, int paramInt2, String paramString) {
		// NOP
		return false;
	}

	private native boolean checkECL(long paramLong, int paramInt1, int paramInt2, String paramString);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class loadClass(String paramString) throws ClassNotFoundException {
		// NOP
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class findClass(String paramString) throws ClassNotFoundException {
		// NOP
		return null;
	}
	
	public native int useLibrary(long paramLong);
	
	public void addAttachment(String paramString, byte[] paramArrayOfbyte) {
		// NOP
	}
	public void handleCleanUp() {
		// NOP
	}
}
