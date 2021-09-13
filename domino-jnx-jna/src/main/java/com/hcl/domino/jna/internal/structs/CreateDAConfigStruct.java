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
 * JNA class for the CreateDAConfigStruct type
 * 
 * @author Raghu M R
 */
public class CreateDAConfigStruct extends BaseStructure implements Serializable, IAdaptable {

  private static final long serialVersionUID = 1L;

  public byte[] szServerName = new byte[NotesConstants.MAX_HOSTNAME];
  public byte[] szDirAssistDBName = new byte[NotesConstants.MAXUSERNAME];
  public boolean bUpdateServerDoc; 
  public byte[] szDomainName = new byte[NotesConstants.MAX_HOSTNAME];
  public byte[] szCompanyName = new byte[NotesConstants.MAXUSERNAME];
  public short wSearchOrder;
  public byte[] szHostName = new byte[NotesConstants.MAX_HOSTNAME];
  public short wLDAPVendor;
  public byte[] szUserName = new byte[NotesConstants.MAXUSERNAME];
  public byte[] szPassword = new byte[NotesConstants.MAXUSERNAME];
  public byte[] szDNSearch = new byte[NotesConstants.MAXPATH];
  public boolean bUseSSL;
  public short wPort;      
  public boolean bAcceptExpiredCertificates;   
  public boolean bVerifyRemoteSrvCert;
  public short wTimeout; 
  public short wMaxEntriesReturned;
  
  public CreateDAConfigStruct( byte serverName[], byte dirAssistDBName[], boolean updateServerDoc, byte domainName[], byte companyName[], short searchOrder, 
      byte  hostName[], short ldapVendor, byte userName[], byte password[], byte dnSearch[], boolean useSSL, short port,  boolean acceptExpiredCertificates, 
      boolean verifyRemoteSrvCert,  short timeout, short maxEntriesReturned) {
    
    super();
    
    if ((serverName.length != this.szServerName.length)) {
        throw new WrongArraySizeException("serverName");
    }
    this.szServerName = serverName;
    
    if ((dirAssistDBName.length != this.szDirAssistDBName.length)) {
      throw new WrongArraySizeException("dirAssistDBName");
    }
    this.szDirAssistDBName = dirAssistDBName;
  
    this.bUpdateServerDoc = updateServerDoc;
  
    if ((domainName.length != this.szDomainName.length)) {
      throw new WrongArraySizeException("domainName");
    }
    this.szDomainName = domainName;
  
    
    if ((companyName.length != this.szCompanyName.length)) {
      throw new WrongArraySizeException("companyName");
    }
    this.szCompanyName = companyName;
    
    this.wSearchOrder = searchOrder;
    
    if ((hostName.length != this.szHostName.length)) {
      throw new WrongArraySizeException("hostName");
    }
    this.szHostName = hostName;
    
    this.wLDAPVendor=ldapVendor;
  
    if ((userName.length != this.szUserName.length)) {
      throw new WrongArraySizeException("userName");
    }
    this.szUserName = userName;

    if ((password.length != this.szPassword.length)) {
      throw new WrongArraySizeException("Password");
    }
    this.szPassword = password;

    if ((dnSearch.length != this.szDNSearch.length)) {
      throw new WrongArraySizeException("DNSearch");
    }    
    this.szDNSearch = dnSearch;
    
    this.wPort = port;
    
    this.bUseSSL =useSSL;
    
    this.bAcceptExpiredCertificates = acceptExpiredCertificates;
    
    this.bVerifyRemoteSrvCert = verifyRemoteSrvCert;
    
    this.wTimeout = timeout; 
    
    this.wMaxEntriesReturned = maxEntriesReturned;      
  }

    public CreateDAConfigStruct() {
      super();
    }
  
	public static CreateDAConfigStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<CreateDAConfigStruct>) () -> new CreateDAConfigStruct());
	}

	public static CreateDAConfigStruct.ByValue newInstanceByVal() {
		return AccessController.doPrivileged((PrivilegedAction<ByValue>) () -> new CreateDAConfigStruct.ByValue());
	}	
	
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList(  "szServerName",
		    "szDirAssistDBName",
		    "bUpdateServerDoc", 
		    "szDomainName",
		    "szCompanyName",
		    "wSearchOrder",
		    "szHostName",
		    "wLDAPVendor",
		    "szUserName",
		    "szPassword",
		    "szDNSearch",
		    "bUseSSL",
		    "wPort",      
		    "bAcceptExpiredCertificates",
		    "bVerifyRemoteSrvCert",
		    "wTimeout", 
		    "wMaxEntriesReturned"
		    );
	}
			
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> clazz) {
		if (clazz == CreateDAConfigStruct.class) {
			return (T) this;
		}
		else if (clazz == Pointer.class) {
			return (T) getPointer();
		}
		return null;
	}
	
	public static class ByReference extends CreateDAConfigStruct implements Structure.ByReference {
		private static final long serialVersionUID = -2958581285484373942L;		
	};
	
	public static class ByValue extends CreateDAConfigStruct implements Structure.ByValue {
		private static final long serialVersionUID = -6538673668884547829L;		
	};
	
}
