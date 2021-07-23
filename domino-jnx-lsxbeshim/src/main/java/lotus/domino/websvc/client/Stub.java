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
package lotus.domino.websvc.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Iterator;

import javax.xml.rpc.Service;

public class Stub extends lotus.domino.axis.client.Stub implements javax.xml.rpc.Stub/*, lotus.domino.PortTypeBase*/ {
	protected Stub(URL paramURL, Service paramService) /*throws lotus.domino.types.Fault*/ {
		
	}
	public Stub(Service paramService, String paramString) {
		
	}
	Object[] getOperationInfo(String paramString) throws Exception {
		// NOP
		return null;
	}
	Object invoke(String paramString, int paramInt1, int paramInt2) throws RemoteException {
		// NOP
		return null;
	}
	public String getEndpoint() {
		// NOP
		return null;
	}
	public void setEndpoint(String paramString) throws MalformedURLException {
		// NOP
	}
	@Override
	public void _setProperty(String name, Object value) {
		// NOP
	}
	@Override
	public Object _getProperty(String name) {
		// NOP
		return null;
	}
	@SuppressWarnings("rawtypes")
	@Override
	public Iterator _getPropertyNames() {
		// NOP
		return null;
	}
}
