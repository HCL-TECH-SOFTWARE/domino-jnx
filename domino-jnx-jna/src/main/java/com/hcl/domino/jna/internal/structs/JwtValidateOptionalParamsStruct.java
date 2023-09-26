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
package com.hcl.domino.jna.internal.structs;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;
import com.hcl.domino.jna.internal.capi.INotesCAPI1400;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * @since 1.29.0
 */
public class JwtValidateOptionalParamsStruct extends BaseStructure {
	public Pointer pszCustomClaimName;
	public INotesCAPI1400.ResourceCallback allowedResource;
	public INotesCAPI1400.ClientCallback allowedClientID;
	
	public JwtValidateOptionalParamsStruct() {
		super();
	}

	public static JwtValidateOptionalParamsStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<JwtValidateOptionalParamsStruct>) () -> new JwtValidateOptionalParamsStruct());
	}


	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("pszCustomClaimName", "allowedResource", "allowedClientID"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public JwtValidateOptionalParamsStruct(Pointer pszCustomClaimName, INotesCAPI1400.ResourceCallback allowedResource, INotesCAPI1400.ClientCallback allowedClientID) {
		super();
		this.pszCustomClaimName = pszCustomClaimName;
		this.allowedResource = allowedResource;
		this.allowedClientID = allowedClientID;
	}

	public static JwtValidateOptionalParamsStruct newInstance(Pointer pszCustomClaimName, INotesCAPI1400.ResourceCallback allowedResource, INotesCAPI1400.ClientCallback allowedClientID) {
		return AccessController.doPrivileged((PrivilegedAction<JwtValidateOptionalParamsStruct>) () -> new JwtValidateOptionalParamsStruct(pszCustomClaimName, allowedResource, allowedClientID));
	}

	public JwtValidateOptionalParamsStruct(Pointer peer) {
		super(peer);
	}

	public static JwtValidateOptionalParamsStruct newInstance(final Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<JwtValidateOptionalParamsStruct>) () -> new JwtValidateOptionalParamsStruct(peer));
	}

	public static class ByReference extends JwtValidateOptionalParamsStruct implements Structure.ByReference {

	};

	public static class ByValue extends JwtValidateOptionalParamsStruct implements Structure.ByValue {

	};
}
