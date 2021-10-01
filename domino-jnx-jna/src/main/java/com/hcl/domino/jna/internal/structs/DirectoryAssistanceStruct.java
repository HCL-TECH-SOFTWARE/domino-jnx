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
import com.hcl.domino.commons.util.DominoUtils;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * JNA class for the DirectoryAssistanceStruct type
 * 
 * @author Raghu M R
 */
public class DirectoryAssistanceStruct extends BaseStructure implements Serializable, IAdaptable {

  private static final long serialVersionUID = 1L;

  public byte[] szServerName = new byte[NotesConstants.MAXUSERNAME];
  public byte[] szDirAssistDBName = new byte[NotesConstants.MAXPATH];
  
  public byte[] szDomainName = new byte[NotesConstants.MAXDOMAINNAME];
  public byte[] szCompanyName = new byte[NotesConstants.FILETITLEMAX];  

  public byte[] szHostName = new byte [NotesConstants.MAXUSERNAME];
  public short wLDAPVendor;
  public byte[]  szUserName = new byte [NotesConstants.MAXUSERNAME]; 
  public byte[]  szPassword = new byte [NotesConstants.MAXUSERPASSWORD]; 

  public boolean bUseSSL;
  public short wPort;
  
  public DirectoryAssistanceStruct(byte serverName[], byte dirAssistDBName[], byte domainName[], byte companyName[], 
      byte hostName[],short ldapVendor, byte userName[], byte password[], boolean useSSL, short port) {
    
	super();
	    
	if ((serverName.length > this.szServerName.length)) {
	  throw new WrongArraySizeException("serverName");
	}
	DominoUtils.overwriteArray(serverName, this.szServerName);

	if ((dirAssistDBName.length > this.szDirAssistDBName.length)) {
	  throw new WrongArraySizeException("dirAssistDBName");
	}
	DominoUtils.overwriteArray(dirAssistDBName, this.szDirAssistDBName);
	    
	if ((domainName.length > this.szDomainName.length)) {
	  throw new WrongArraySizeException("domainName");
	}
	DominoUtils.overwriteArray(domainName, this.szDomainName);

	if ((companyName.length > this.szCompanyName.length)) {
	  throw new WrongArraySizeException("companyName");
	}
	DominoUtils.overwriteArray(companyName, this.szCompanyName);
	    
	if ((hostName.length > this.szHostName.length)) {
	  throw new WrongArraySizeException("hostName");
	}
	DominoUtils.overwriteArray(hostName, this.szHostName);

	this.wLDAPVendor = ldapVendor; 
	    
	if ((userName.length > this.szUserName.length)) {
	  throw new WrongArraySizeException("userName");
	}
	DominoUtils.overwriteArray(userName, this.szUserName);

	if ((password.length > this.szPassword.length)) {
	  throw new WrongArraySizeException("Password");
	}
	DominoUtils.overwriteArray(password, this.szPassword);
	    
	this.wPort = port;
	    
	this.bUseSSL =useSSL;  
  }

    public DirectoryAssistanceStruct() {
      super();
    }  
  
	public static DirectoryAssistanceStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<DirectoryAssistanceStruct>) () -> new DirectoryAssistanceStruct());
	}

	public static DirectoryAssistanceStruct.ByValue newInstanceByVal() {
		return AccessController.doPrivileged((PrivilegedAction<ByValue>) () -> new DirectoryAssistanceStruct.ByValue());
	}	
	
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("szServerName",
		    "szDirAssistDBName",
		    "szDomainName",
		    "szCompanyName",
		    "szHostName",
		    "wLDAPVendor",
		    "szUserName",
		    "szPassword",
		    "bUseSSL",
		    "wPort");
	}
			
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> clazz) {
		if (clazz == DirectoryAssistanceStruct.class) {
			return (T) this;
		}
		else if (clazz == Pointer.class) {
			return (T) getPointer();
		}
		return null;
	}
	
	public static class ByReference extends DirectoryAssistanceStruct implements Structure.ByReference {
		private static final long serialVersionUID = -2958581285484373942L;		
	};
	
	public static class ByValue extends DirectoryAssistanceStruct implements Structure.ByValue {
		private static final long serialVersionUID = -6538673668884547829L;		
	};
	
}
