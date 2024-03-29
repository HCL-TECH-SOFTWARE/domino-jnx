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

import com.hcl.domino.commons.structs.WrongArraySizeException;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class NotesItemDefinitionTableExt extends BaseStructure {
	/** number of items in the table */
	public int Items;
	/**
	 * Memory handle of ITEM_DEFINITION_EXT<br>
	 * structures
	 */
	public int ItemDefArray;
	/**
	 * Number of non-null segments in<br>
	 * ItemNameSegs
	 */
	public int NumSegments;
	/**
	 * Segments of<br>
	 * packed text<br>
	 * C type : DHANDLE[MAX_ITEMDEF_SEGMENTS]
	 */
	public int[] ItemNameSegs = new int[NotesConstants.MAX_ITEMDEF_SEGMENTS];
	/**
	 * Length of<br>
	 * each non-null text segment<br>
	 * C type : DWORD[MAX_ITEMDEF_SEGMENTS]
	 */
	public int[] ItemNameSegLengths = new int[NotesConstants.MAX_ITEMDEF_SEGMENTS];
	
	public NotesItemDefinitionTableExt() {
		super();
	}
	
	public static NotesItemDefinitionTableExt newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesItemDefinitionTableExt>) () -> new NotesItemDefinitionTableExt());
	};
	
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList(
			"Items", //$NON-NLS-1$
			"ItemDefArray", //$NON-NLS-1$
			"NumSegments", //$NON-NLS-1$
			"ItemNameSegs", //$NON-NLS-1$
			"ItemNameSegLengths" //$NON-NLS-1$
		);
	}
	
	/**
	 * @param items number of items in the table<br>
	 * @param itemDefArray Memory handle of ITEM_DEFINITION_EXT<br>
	 * structures<br>
	 * @param numSegments Number of non-null segments in<br>
	 * ItemNameSegs<br>
	 * @param itemNameSegs Segments of<br>
	 * packed text<br>
	 * C type : DHANDLE[MAX_ITEMDEF_SEGMENTS]<br>
	 * @param itemNameSegLengths Length of<br>
	 * each non-null text segment<br>
	 * C type : DWORD[MAX_ITEMDEF_SEGMENTS]
	 */
	public NotesItemDefinitionTableExt(int items, int itemDefArray, int numSegments, int itemNameSegs[], int itemNameSegLengths[]) {
		super();
		this.Items = items;
		this.ItemDefArray = itemDefArray;
		this.NumSegments = numSegments;
		if ((itemNameSegs.length != this.ItemNameSegs.length)) {
			throw new WrongArraySizeException("ItemNameSegs"); //$NON-NLS-1$
		}
		this.ItemNameSegs = itemNameSegs;
		if ((itemNameSegLengths.length != this.ItemNameSegLengths.length)) {
			throw new WrongArraySizeException("ItemNameSegLengths"); //$NON-NLS-1$
		}
		this.ItemNameSegLengths = itemNameSegLengths;
	}
	
	public static NotesItemDefinitionTableExt newInstance(final int items, final int itemDefArray, final int numSegments, final int itemNameSegs[], final int itemNameSegLengths[]) {
		return AccessController.doPrivileged((PrivilegedAction<NotesItemDefinitionTableExt>) () -> new NotesItemDefinitionTableExt(items, itemDefArray, numSegments, itemNameSegs, itemNameSegLengths));
	};
	
	public NotesItemDefinitionTableExt(Pointer peer) {
		super(peer);
	}
	
	public static NotesItemDefinitionTableExt newInstance(final Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<NotesItemDefinitionTableExt>) () -> new NotesItemDefinitionTableExt(peer));
	};

	public static class ByReference extends NotesItemDefinitionTableExt implements Structure.ByReference {
		
	};
	public static class ByValue extends NotesItemDefinitionTableExt implements Structure.ByValue {
		
	};
}
