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
package com.hcl.domino.jna.internal.structs;

import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;
import com.hcl.domino.commons.structs.WrongArraySizeException;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * JNA class for the VerifyLDAPConnectionStruct type
 * 
 * @author Raghu M R
 */
public class VerifyLDAPConnectionStruct extends BaseStructure implements Serializable, IAdaptable {

  private static final long serialVersionUID = 1L;

  public byte[] szHostName = new byte[NotesConstants.MAXUSERNAME];
  public byte[] szUserName = new byte[NotesConstants.MAXUSERNAME];
  public byte[] szPassword = new byte[NotesConstants.MAXUSERPASSWORD];
  public byte[] szDNSearch = new byte[NotesConstants.MAXLDAPBASE];
  public boolean bUseSSL;
  public short wPort;
  public boolean bAcceptExpiredCertificates;   
  public boolean bVerifyRemoteSrvCert;
 
  
  public VerifyLDAPConnectionStruct(byte hostName[],byte userName[], byte password[], byte dnSearch[], boolean useSSL, short port, boolean acceptExpiredCertificates, boolean verifyRemoteSrvCert) {
    super();
    
    if ((hostName.length > this.szHostName.length)) {
        throw new WrongArraySizeException("hostName");
    }
    this.szHostName = hostName;
    
    if ((userName.length > this.szUserName.length)) {
      throw new WrongArraySizeException("userName");
    }
    this.szUserName = userName;

    if ((password.length > this.szPassword.length)) {
      throw new WrongArraySizeException("Password");
    }
    this.szPassword = password;

    if ((dnSearch.length > this.szDNSearch.length)) {
      throw new WrongArraySizeException("DNSearch");
    }
    this.szDNSearch = dnSearch;
    
    this.wPort = port;
    this.bUseSSL =useSSL;
    this.bAcceptExpiredCertificates = acceptExpiredCertificates;
    this.bVerifyRemoteSrvCert = verifyRemoteSrvCert;
  }

    public VerifyLDAPConnectionStruct() {
      super();
    }


	public static VerifyLDAPConnectionStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<VerifyLDAPConnectionStruct>) () -> new VerifyLDAPConnectionStruct());
	}

	public static VerifyLDAPConnectionStruct.ByValue newInstanceByVal() {
		return AccessController.doPrivileged((PrivilegedAction<ByValue>) () -> new VerifyLDAPConnectionStruct.ByValue());
	}	
	
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("szHostName", "szUserName", "szPassword", "szDNSearch", "bUseSSL", "wPort", "bAcceptExpiredCertificates", "bVerifyRemoteSrvCert");
	}
			
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> clazz) {
		if (clazz == VerifyLDAPConnectionStruct.class) {
			return (T) this;
		}
		else if (clazz == Pointer.class) {
			return (T) getPointer();
		}
		return null;
	}
	
	public static class ByReference extends VerifyLDAPConnectionStruct implements Structure.ByReference {
		private static final long serialVersionUID = -2958581285484373942L;		
	};
	
	public static class ByValue extends VerifyLDAPConnectionStruct implements Structure.ByValue {
		private static final long serialVersionUID = -6538673668884547829L;		
	};
	
}
