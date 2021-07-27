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
package lotus.domino.axis.transport.http;

public class NotesSocket {
	private final boolean m_ssl = false;
	private final String m_host = ""; //$NON-NLS-1$
	private final int m_port = 0;
	private final int m_timeout = 0;
	private final int m_ssloptions = 0;
	private int m_context = 0;
	private boolean m_useFullURL = false;
	private boolean m_useProxyAuth = false;
	private String m_proxyUser;
	private String m_proxyPass;
	
	private native void writeBytes(byte[] paramArrayOfbyte, int paramInt1, int paramInt2) throws NotesSocketException;
	private native int readBytes(byte[] paramArrayOfbyte, int paramInt1, int paramInt2) throws NotesSocketException;
	private native void openConnection() throws NotesSocketException;
	private native void closeConnection() throws NotesSocketException;
}
