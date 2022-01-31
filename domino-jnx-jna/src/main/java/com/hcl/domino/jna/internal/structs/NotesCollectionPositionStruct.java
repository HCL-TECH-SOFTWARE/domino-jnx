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
import com.hcl.domino.data.IAdaptable;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * JNA class for the current collection position (COLLECTIONPOSITION type)
 * 
 * @author Karsten Lehmann
 */
public class NotesCollectionPositionStruct extends BaseStructure implements IAdaptable {
	/** # levels -1 in tumbler */
	public short Level;

	/**
	 * MINIMUM level that this position is allowed to be nagivated to. This is
	 * useful to navigate a subtree using all navigator codes. This field is
	 * IGNORED unless the NAVIGATE_MINLEVEL flag is enabled (for backward
	 * compatibility)
	 */
	public byte MinLevel;

	/**
	 * MAXIMUM level that this position is allowed to be nagivated to. This is
	 * useful to navigate a subtree using all navigator codes. This field is
	 * IGNORED unless the NAVIGATE_MAXLEVEL flag is enabled (for backward
	 * compatibility)
	 */
	public byte MaxLevel;
	
	
	/**
	 * Current tumbler (1.2.3, etc)<br>
	 * C type : DWORD[32]
	 */
	public int[] Tumbler = new int[32];
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesCollectionPositionStruct() {
		super();
	}
	
	public static NotesCollectionPositionStruct newInstance() {
		return AccessController.doPrivileged((PrivilegedAction<NotesCollectionPositionStruct>) () -> new NotesCollectionPositionStruct());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> clazz) {
		if (clazz == NotesCollectionPositionStruct.class) {
			return (T) this;
		}
		else if (clazz == Pointer.class) {
			return (T) getPointer();
		}
		return null;
	}
	
	@SuppressWarnings("nls")
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("Level", "MinLevel", "MaxLevel", "Tumbler");
	}
	
	/**
	 * @param Level # levels -1 in tumbler<br>
	 * @param MinLevel MINIMUM level that this position<br>
	 * @param MaxLevel MAXIMUM level that this position<br>
	 * @param Tumbler Current tumbler (1.2.3, etc)<br>
	 * C type : DWORD[32]
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 */
	@Deprecated
	public NotesCollectionPositionStruct(short Level, byte MinLevel, byte MaxLevel, int Tumbler[]) {
		super();
		this.Level = Level;
		this.MinLevel = MinLevel;
		this.MaxLevel = MaxLevel;
		if ((Tumbler.length != this.Tumbler.length)) {
			throw new WrongArraySizeException("Tumbler"); //$NON-NLS-1$
		}
		this.Tumbler = Tumbler;
	}
	
	public static NotesCollectionPositionStruct newInstance(final short Level, final byte MinLevel, final byte MaxLevel, final int Tumbler[]) {
		return AccessController.doPrivileged((PrivilegedAction<NotesCollectionPositionStruct>) () -> new NotesCollectionPositionStruct(Level, MinLevel, MaxLevel, Tumbler));
	}
	
	/**
	 * @deprecated only public to be used by JNA; use static newInstance method instead to run in AccessController.doPrivileged block
	 * 
	 * @param peer pointer
	 */
	@Deprecated
	public NotesCollectionPositionStruct(Pointer peer) {
		super(peer);
	}
	
	public static NotesCollectionPositionStruct newInstance(final Pointer peer) {
		return AccessController.doPrivileged((PrivilegedAction<NotesCollectionPositionStruct>) () -> new NotesCollectionPositionStruct(peer));
	}
	
	/**
	 * Converts the position object to a position string like "1.2.3".<br>
	 * <br>
	 * Please note that we also support an advanced syntax in contrast to IBM's API in order
	 * to specify the min/max level parameters: "1.2.3|0-2" for minlevel=0, maxlevel=2. These
	 * levels can be used to limit reading entries in a categorized view to specified depths.<br>
	 * <br>
	 * This method will returns a string with the advanced syntax if MinLevel or MaxLevel is not 0.
	 * 
	 * @return position string
	 */
	public String toPosString() {
		StringBuilder sb = new StringBuilder();
		
		for (int i=0; i<=this.Level; i++) {
			if (sb.length() > 0) {
				sb.append('.');
			}
			sb.append(this.Tumbler[i]);
		}
		
		if (MinLevel!=0 || MaxLevel!=0) {
			sb.append("|").append(MinLevel).append("-").append(MaxLevel); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return sb.toString();
	}
	
	/**
	 * Converts a position string like "1.2.3" to a {@link NotesCollectionPositionStruct} object.<br>
	 * <br>
	 * Please note that we also support an advanced syntax in contrast to IBM's API in order
	 * to specify the min/max level parameters: "1.2.3|0-2" for minlevel=0, maxlevel=2. These
	 * levels can be used to limit reading entries in a categorized view to specified depths.
	 * 
	 * @param posStr position string
	 * @return position object
	 */
	public static NotesCollectionPositionStruct toPosition(String posStr) {
		short level;
		final int[] tumbler = new int[32];
		byte minLevel = 0;
		byte maxLevel = 0;
		
		int iPos = posStr.indexOf("|"); //$NON-NLS-1$
		if (iPos!=-1) {
			//optional addition to the classic position string: |minlevel-maxlevel
			String minMaxStr = posStr.substring(iPos+1);
			posStr = posStr.substring(0, iPos);
			
			iPos = minMaxStr.indexOf("-"); //$NON-NLS-1$
			if (iPos!=-1) {
				minLevel = Byte.parseByte(minMaxStr.substring(0, iPos));
				maxLevel = Byte.parseByte(minMaxStr.substring(iPos+1));
			}
		}
		
		if (posStr==null || posStr.length()==0 || "0".equals(posStr)) { //$NON-NLS-1$
			level = 0;
			tumbler[0] = 0;
		}
		else {
			String[] parts = posStr.split("\\."); //$NON-NLS-1$
			level = (short) (parts.length-1);
			for (int i=0; i<parts.length; i++) {
				tumbler[i] = Integer.parseInt(parts[i]);
			}
		}
		
		NotesCollectionPositionStruct pos = NotesCollectionPositionStruct.newInstance();
		pos.Level = level;
		pos.MinLevel = minLevel;
		pos.MaxLevel = maxLevel;
		for (int i=0; i<tumbler.length; i++) {
			pos.Tumbler[i] = tumbler[i];
		}
		return pos;
	}

	public static class ByReference extends NotesCollectionPositionStruct implements Structure.ByReference {
		
	};
	public static class ByValue extends NotesCollectionPositionStruct implements Structure.ByValue {
		
	};
}
