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
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;
/**
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class NotesFieldFormulaStruct extends BaseStructure {
	/**
	 * the query results name<br>
	 * C type : char[MAX_CMD_VALLEN]
	 */
	public byte[] resultsname = new byte[NotesConstants.MAX_CMD_VALLEN];
	/**
	 * columnname \ufffd refers to sort column name - programmatic name<br>
	 * C type : char[MAX_CMD_VALLEN]
	 */
	public byte[] columnname = new byte[NotesConstants.MAX_CMD_VALLEN];
	/**
	 * how to build result data from documents<br>
	 * C type : char[MAX_CMD_VALLEN]
	 */
	public byte[] formula = new byte[NotesConstants.MAX_CMD_VALLEN];
	/** We will flag any non-matched formula entry misspelling, etc */
	public boolean bMatched;
	
	public NotesFieldFormulaStruct() {
		super();
	}
	
	@Override
	protected int getOverrideAlignment() {
		return Structure.ALIGN_DEFAULT;
	}

	public static NotesFieldFormulaStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesFieldFormulaStruct>) () -> {
			return new NotesFieldFormulaStruct();
		});
	}

	protected List<String> getFieldOrder() {
		return Arrays.asList("resultsname", "columnname", "formula", "bMatched");
	}
	
	/**
	 * @param resultsname the query results name<br>
	 * C type : char[MAX_CMD_VALLEN]<br>
	 * @param columnname columnname \ufffd refers to sort column name - programmatic name<br>
	 * C type : char[MAX_CMD_VALLEN]<br>
	 * @param formula how to build result data from documents<br>
	 * C type : char[MAX_CMD_VALLEN]<br>
	 * @param bMatched We will flag any non-matched formula entry misspelling, etc
	 */
	public NotesFieldFormulaStruct(byte resultsname[], byte columnname[], byte formula[], boolean bMatched) {
		super();
		if ((resultsname.length != this.resultsname.length)) 
			throw new IllegalArgumentException("Wrong array size !");
		this.resultsname = resultsname;
		if ((columnname.length != this.columnname.length)) 
			throw new IllegalArgumentException("Wrong array size !");
		this.columnname = columnname;
		if ((formula.length != this.formula.length)) 
			throw new IllegalArgumentException("Wrong array size !");
		this.formula = formula;
		this.bMatched = bMatched;
	}
	
	public NotesFieldFormulaStruct(Pointer peer) {
		super(peer);
	}
	
	public static NotesFieldFormulaStruct newInstance(Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<NotesFieldFormulaStruct>) () -> {
			return new NotesFieldFormulaStruct(peer);
		});
	}

	public static class ByReference extends NotesFieldFormulaStruct implements Structure.ByReference {
		
	};
	
	public static NotesFieldFormulaStruct.ByReference newInstanceByReference() {
		return AccessController.doPrivileged((PrivilegedAction<NotesFieldFormulaStruct.ByReference>) () -> {
			return new NotesFieldFormulaStruct.ByReference();
		});
	}

	public static class ByValue extends NotesFieldFormulaStruct implements Structure.ByValue {
		
	};
}
