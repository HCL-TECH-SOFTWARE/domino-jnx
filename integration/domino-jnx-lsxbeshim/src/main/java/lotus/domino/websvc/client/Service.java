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

import java.io.Serializable;

import javax.naming.Referenceable;
import javax.xml.rpc.Call;
import javax.xml.rpc.ServiceException;

@SuppressWarnings("serial")
public class Service extends lotus.domino.axis.client.Service implements javax.xml.rpc.Service, Serializable, Referenceable {
	protected Service(String paramString) {
		// NOP
	}
	Service(String paramString, byte[] paramArrayOfbyte) {
		// NOP
	}
	
	@Override
	public Call createCall() throws ServiceException {
		return super.createCall();
	}
}
