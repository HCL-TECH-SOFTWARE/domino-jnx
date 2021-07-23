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
import java.util.Arrays;
import java.util.List;

import com.hcl.domino.jna.internal.gc.handles.HANDLE;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class NotesAdminReqParams extends BaseStructure {
	/** Reserved */
	public int Flags;
	/** DELETE_xxxx_IN_NAB defined above */
	public int dwDeleteInNABType;
	/**
	 * if dwDeleteInNABType equals<br>
	 * DELETE_PERSON_IN_NAB a pointer to a<br>
	 * group (termination perhaps) to have<br>
	 * the name added<br>
	 * C type : char*
	 */
	public Pointer chGroupName;
	/**
	 * if dwDeleteInNABType equals<br>
	 * DELETE_PERSON_IN_NAB a pointer to<br>
	 * the person's Alternate Name<br>
	 * C type : char*
	 */
	public Pointer chAltName;
	/**
	 * for ADMINReqMoveComplete, a pointer<br>
	 * to a new first name for the<br>
	 * person<br>
	 * C type : char*
	 */
	public Pointer chFirstName;
	/**
	 * for ADMINReqMoveComplete, a<br>
	 * pointer to a new middle initial for<br>
	 * the person<br>
	 * C type : char*
	 */
	public Pointer chMiddleInitial;
	/**
	 * for ADMINReqMoveComplete, a pointer<br>
	 * to a new last name for the person<br>
	 * C type : char*
	 */
	public Pointer chLastName;
	/**
	 * for ADMINReqRename,<br>
	 * ADMINReqRecertify, and<br>
	 * ADMINReqMoveComplete, a pointer to a<br>
	 * new alternate common name for the<br>
	 * person<br>
	 * C type : char*
	 */
	public Pointer chAltCommonName;
	/**
	 * for ADMINReqRename,<br>
	 * ADMINReqRecertify, and<br>
	 * ADMINReqMoveComplete, a pointer to a<br>
	 * new alternate org unit for the<br>
	 * person<br>
	 * C type : char*
	 */
	public Pointer chAltOrgUnitName;
	/**
	 * for ADMINReqRename,<br>
	 * ADMINReqRecertify, and<br>
	 * ADMINReqMoveComplete, a pointer to a<br>
	 * new alternate language for the<br>
	 * person<br>
	 * C type : char*
	 */
	public Pointer chAltLanguage;
	/**
	 * for ADMINReqMoveUserInHier,<br>
	 * TRUE indicates that support for a<br>
	 * simultaneous hierarchy move and name<br>
	 * change.  Recognized only by v5.02<br>
	 * and above clients and servers
	 */
	public boolean fDontUseV1ChangeRequest;
	/**
	 * 5.x structure ended here.  The following fields were added for Rnext<br>
	 * for ADMINReqDeleteInNAB, a handle to <br>
	 * the directory from which the Person, <br>
	 * Server, or Group is to be deleted if <br>
	 * the target directory is not names.nsf<br>
	 * C type : DBHANDLE
	 */
	public HANDLE dbhDirectory;
	public NotesAdminReqParams() {
		super();
	}
	protected List<String> getFieldOrder() {
		return Arrays.asList("Flags", "dwDeleteInNABType", "chGroupName", "chAltName", "chFirstName", "chMiddleInitial", "chLastName", "chAltCommonName", "chAltOrgUnitName", "chAltLanguage", "fDontUseV1ChangeRequest", "dbhDirectory");
	}
	
	public NotesAdminReqParams(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends NotesAdminReqParams implements Structure.ByReference {
		
	};
	public static class ByValue extends NotesAdminReqParams implements Structure.ByValue {
		
	};
}
