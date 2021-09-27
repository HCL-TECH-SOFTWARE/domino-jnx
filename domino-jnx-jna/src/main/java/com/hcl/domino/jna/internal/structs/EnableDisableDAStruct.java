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
import com.hcl.domino.jna.internal.structs.DirectoryAssistanceStruct;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * JNA class for the EnableDisableDAStruct type
 * 
 * @author Raghu M R
 */
public class EnableDisableDAStruct extends BaseStructure implements Serializable, IAdaptable {

  private static final long serialVersionUID = 1L;

  public byte[] szServerName = new byte[NotesConstants.MAXUSERNAME];
  public byte[] szDirAssistDBName = new byte[NotesConstants.MAXPATH];
  public byte[] szDocUNID = new byte[NotesConstants.MAX_HOSTNAME];
  public boolean bEnableDomain;
  
 
  public EnableDisableDAStruct(byte serverName[], byte dirAssistDBName[], byte docUNID[], boolean enableDomain) {
    
    super();

    if ((serverName.length > this.szServerName.length)) {
      throw new WrongArraySizeException("serverName");
    }    
    this.szServerName = serverName;

    if ((dirAssistDBName.length > this.szDirAssistDBName.length)) {
      throw new WrongArraySizeException("dirAssistDBName");
    }
    this.szDirAssistDBName = dirAssistDBName;


    if ((docUNID.length > this.szDocUNID.length)) {
        throw new WrongArraySizeException("DocUNID");
    }
    this.szDocUNID = docUNID;
    
    this.bEnableDomain = enableDomain;
  }

    public EnableDisableDAStruct() {
      super();
    }
  
	public static EnableDisableDAStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<EnableDisableDAStruct>) () -> new EnableDisableDAStruct());
	}

	public static EnableDisableDAStruct.ByValue newInstanceByVal() {
		return AccessController.doPrivileged((PrivilegedAction<ByValue>) () -> new EnableDisableDAStruct.ByValue());
	}	
	
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList(
		    "szServerName",
		    "szDirAssistDBName",		    
		    "szDocUNID",
		    "bEnableDomain"
		    );
	}
			
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> clazz) {
		if (clazz == EnableDisableDAStruct.class) {
			return (T) this;
		}
		else if (clazz == Pointer.class) {
			return (T) getPointer();
		}
		return null;
	}
	
	public static class ByReference extends EnableDisableDAStruct implements Structure.ByReference {
		private static final long serialVersionUID = -2958581285484373942L;		
	};
	
	public static class ByValue extends EnableDisableDAStruct implements Structure.ByValue {
		private static final long serialVersionUID = -6538673668884547829L;		
	};
	
}
