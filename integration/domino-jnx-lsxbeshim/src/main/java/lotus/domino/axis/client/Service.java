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
package lotus.domino.axis.client;

import java.io.Serializable;
import java.net.URL;
import java.rmi.Remote;
import java.util.Iterator;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.encoding.TypeMappingRegistry;
import javax.xml.rpc.handler.HandlerRegistry;

@SuppressWarnings("serial")
public class Service implements javax.xml.rpc.Service, Serializable, Referenceable {

	@Override
	public Reference getReference() throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Remote getPort(QName portName, Class serviceEndpointInterface) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Remote getPort(Class serviceEndpointInterface) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Call[] getCalls(QName portName) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Call createCall(QName portName) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Call createCall(QName portName, QName operationName) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Call createCall(QName portName, String operationName) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Call createCall() throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QName getServiceName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator getPorts() throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getWSDLDocumentLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeMappingRegistry getTypeMappingRegistry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HandlerRegistry getHandlerRegistry() {
		// TODO Auto-generated method stub
		return null;
	}

}
