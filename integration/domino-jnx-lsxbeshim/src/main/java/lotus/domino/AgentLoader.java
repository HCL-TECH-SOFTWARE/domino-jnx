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
