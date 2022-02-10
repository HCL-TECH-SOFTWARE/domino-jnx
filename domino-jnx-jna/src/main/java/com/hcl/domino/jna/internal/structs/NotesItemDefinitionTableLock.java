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
public class NotesItemDefinitionTableLock extends BaseStructure {
	public int Items;
	/** C type : void* */
	public Pointer pItemDefArray;
	/** C type : char*[MAX_ITEMDEF_SEGMENTS] */
	public Pointer[] ItemText = new Pointer[NotesConstants.MAX_ITEMDEF_SEGMENTS];
	public int NumSegments;
	/** C type : DWORD[MAX_ITEMDEF_SEGMENTS] */
	public int[] ItemNameSegLengths = new int[NotesConstants.MAX_ITEMDEF_SEGMENTS];
	
	public NotesItemDefinitionTableLock() {
		super();
	}
	
	public static NotesItemDefinitionTableLock newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesItemDefinitionTableLock>) () -> new NotesItemDefinitionTableLock());
	};

	
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList(
			"Items", //$NON-NLS-1$
			"pItemDefArray", //$NON-NLS-1$
			"ItemText", //$NON-NLS-1$
			"NumSegments", //$NON-NLS-1$
			"ItemNameSegLengths" //$NON-NLS-1$
		);
	}
	
	/**
	 * @param items the count of items
	 * @param pItemDefArray C type : void*<br>
	 * @param itemText C type : char*[MAX_ITEMDEF_SEGMENTS]<br>
	 * @param numSegments the number of segments
	 * @param itemNameSegLengths C type : DWORD[MAX_ITEMDEF_SEGMENTS]
	 */
	public NotesItemDefinitionTableLock(int items, Pointer pItemDefArray, Pointer itemText[], int numSegments, int itemNameSegLengths[]) {
		super();
		this.Items = items;
		this.pItemDefArray = pItemDefArray;
		if ((itemText.length != this.ItemText.length)) {
			throw new WrongArraySizeException("ItemText"); //$NON-NLS-1$
		}
		this.ItemText = itemText;
		this.NumSegments = numSegments;
		if ((itemNameSegLengths.length != this.ItemNameSegLengths.length)) {
			throw new WrongArraySizeException("ItemNameSegLengths"); //$NON-NLS-1$
		}
		this.ItemNameSegLengths = itemNameSegLengths;
	}
	
	public static NotesItemDefinitionTableLock newInstance(final int items, final Pointer pItemDefArray, final Pointer itemText[], final int numSegments, final int itemNameSegLengths[]) {
		return AccessController.doPrivileged((PrivilegedAction<NotesItemDefinitionTableLock>) () -> new NotesItemDefinitionTableLock(items, pItemDefArray, itemText, numSegments, itemNameSegLengths));
	};

	public NotesItemDefinitionTableLock(Pointer peer) {
		super(peer);
	}
	
	public static NotesItemDefinitionTableLock newInstance(final Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<NotesItemDefinitionTableLock>) () -> new NotesItemDefinitionTableLock(peer));
	};

	public static class ByReference extends NotesItemDefinitionTableLock implements Structure.ByReference {
		
	};
	public static class ByValue extends NotesItemDefinitionTableLock implements Structure.ByValue {
		
	};
}
