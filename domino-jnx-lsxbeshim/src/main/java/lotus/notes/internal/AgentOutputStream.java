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
package lotus.notes.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class AgentOutputStream extends OutputStream {
	AgentOutputStream(long javaDoc, int msg) {
		
	}
	AgentOutputStream(long javaDoc, int msg, boolean isUTF8) {
		
	}
	
	private native void writeBytes(long paramLong, int paramInt1, byte[] paramArrayOfbyte, int paramInt2, int paramInt3, boolean paramBoolean) throws IOException;
	
	@Override
	public synchronized void write(int b) throws IOException {
		// NOP
	}
	@Override
	public synchronized void write(byte[] b) throws IOException {
		super.write(b);
	}
	@Override
	public synchronized void write(byte[] b, int off, int len) throws IOException {
		super.write(b, off, len);
	}
	
	public void setUTF8Encoding(boolean isUTF8) {
		// NOP
	}
	
	static void init() {
		// NOP
	}
	
	public static PrintWriter getAgentOutput(boolean paramBoolean, long paramLong) {
		// NOP
		return null;
	}
	private static OutputStream getOutputStream(boolean paramBoolean1, long paramLong, int paramInt, boolean paramBoolean2) {
		// NOP
		return null;
	}

	public static OutputStream getAgentOutputStream(boolean paramBoolean, long paramLong) {
		// NOP
		return null;
	}

	public static OutputStream getServiceOutputStream(boolean paramBoolean1, long paramLong, boolean paramBoolean2) {
		// NOP
		return null;
	}
}
