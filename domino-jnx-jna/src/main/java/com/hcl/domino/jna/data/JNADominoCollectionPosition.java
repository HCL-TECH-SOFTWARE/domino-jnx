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
package com.hcl.domino.jna.data;

import java.text.MessageFormat;
import java.util.Arrays;

import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.jna.internal.structs.NotesCollectionPositionStruct;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * This structure is used to specify the hierarchical, index position of an item (or category)
 * within a View(collection).<br>
 * <br>
 * Level = (number of levels in tumbler - 1)<br>
 * <br>
 * Tumbler is an array of ordinal ranks within the view; with the first (0) entry referring to the top level.<br>
 * <br>
 * For example, consider the following non-Domino Outline Scheme :<br>
 * <br>
 * I.  First Main Category<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;A.  First sub-category under I<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;B.  Second sub-category under I<br>
 * II.  Second Main Category<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;A.  First sub-category under II<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1.  First item under  II.A<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;2.  Second item under II.A<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;B.  Second sub-category under II<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;C.  Third sub-category under II<br>
 * III.  Third Main Category<br>
 * <br>
 * With this example, [2; II.A.2] refers to the "Second item under II.A."<br>
 * Similarly, [0; III] refers to the "Third Main Category."<br>
 * <br>
 * Finally, it should be noted that [2; I.B.1], [1; I.C], and [3; II.A.1] are all NOT valid positions.<br>
 * <br>
 * [2; I.B.1] because the "Second sub-category under I" has no items.<br>
 * [1; I.C] because there is no "Third sub-category under I", and<br>
 * [3; II.A.1] because the value of Level (3) shows four levels should be represented
 * in the Tumbler and there are only three.
 * 
 * @author Karsten Lehmann
 */
public class JNADominoCollectionPosition implements IAdaptable, Comparable<JNADominoCollectionPosition> {
	/** # levels -1 in tumbler */
	private int level;

	/**
	 * MINIMUM level that this position is allowed to be nagivated to. This is
	 * useful to navigate a subtree using all navigator codes. This field is
	 * IGNORED unless the NAVIGATE_MINLEVEL flag is enabled (for backward
	 * compatibility)
	 */
	private int minLevel;

	/**
	 * MAXIMUM level that this position is allowed to be nagivated to. This is
	 * useful to navigate a subtree using all navigator codes. This field is
	 * IGNORED unless the NAVIGATE_MAXLEVEL flag is enabled (for backward
	 * compatibility)
	 */
	private int maxLevel;
	
	/**
	 * Current tumbler (1.2.3, etc)<br>
	 * C type : DWORD[32]
	 */
	private int[] tumbler = new int[32];

	private String toString;
	
	private NotesCollectionPositionStruct struct;
	
	public JNADominoCollectionPosition(IAdaptable adaptable) {
		NotesCollectionPositionStruct struct = adaptable.getAdapter(NotesCollectionPositionStruct.class);
		if (struct!=null) {
			this.struct = struct;
			this.level = struct.Level;
			this.minLevel = struct.MinLevel;
			this.maxLevel = struct.MaxLevel;
			this.tumbler = struct.Tumbler;
			return;
		}
		Pointer p = adaptable.getAdapter(Pointer.class);
		if (p!=null) {
			this.struct = NotesCollectionPositionStruct.newInstance(p);
			this.level = this.struct.Level;
			this.minLevel = this.struct.MinLevel;
			this.maxLevel = this.struct.MaxLevel;
			this.tumbler = this.struct.Tumbler;
			return;
		}
		throw new IllegalArgumentException("Constructor argument cannot provide a supported datatype");
	}
	
	/**
	 * Creates a new instance
	 * 
	 * @param tumbler array with position information [index level0, index level1, ...], e.g. [1,2,3], up to 32 entries
	 */
	public JNADominoCollectionPosition(int[] tumbler) {
		this(computeLevel(tumbler), 0, 0, tumbler);
	}
	
	/**
	 * Computes the level of a tumbler array
	 * 
	 * @param tumbler tumbler array
	 * @return level, e.g. 1 for [1,0,0, ... 0], 2 for [1,3,0, ... 0] and 3 for [1,3,5, ... 0]
	 */
	private static int computeLevel(int[] tumbler) {
		if (tumbler[0]==0) {
			return 0;
		}
		
		for (int i=1; i<tumbler.length; i++) {
			if (tumbler[i]==0) {
				return i-1;
			}
		}
		return tumbler.length-1;
	}
	
	/**
	 * Creates a new instance
	 * 
	 * @param level level in the view, use 0 for first level
	 * @param minLevel min level, see {@link #setMinLevel(int)}
	 * @param maxLevel max level, see {@link #setMaxLevel(int)}
	 * @param tumbler array with position information [index level0, index level1, ...], e.g. [1,2,3], up to 32 entries
	 */
	public JNADominoCollectionPosition(int level, int minLevel, int maxLevel, final int tumbler[]) {
		this.level = level;
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
		if (tumbler.length>32) {
			throw new IllegalArgumentException(MessageFormat.format("Tumbler array exceeds the maximum size ({0} > 32)", tumbler.length));
		}
		this.tumbler = new int[32];
		for (int i=0; i<this.tumbler.length; i++) {
			if (i < tumbler.length) {
				this.tumbler[i] = tumbler[i];
			}
			else {
				this.tumbler[i] = 0;
			}
		}
	}
	
	/**
	 * Converts a position string like "1.2.3" to a {@link JNADominoCollectionPosition} object.<br>
	 * <br>
	 * Please note that we also support an advanced syntax in contrast to IBM's API in order
	 * to specify the min/max level parameters: "1.2.3|0-2" for minlevel=0, maxlevel=2. These
	 * levels can be used to limit reading entries in a categorized view to specified depths.
	 * 
	 * @param posStr position string
	 */
	public JNADominoCollectionPosition(String posStr) {
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
		
		tumbler = new int[32];

		if (posStr==null || posStr.length()==0 || "0".equals(posStr)) { //$NON-NLS-1$
			level = 0;
			tumbler[0] = 0;
			this.toString = "0"; //$NON-NLS-1$
		}
		else {
			String[] parts = posStr.split("\\."); //$NON-NLS-1$
			level = (short) (parts.length-1);
			for (int i=0; i<parts.length; i++) {
				tumbler[i] = Integer.parseInt(parts[i]);
			}
			this.toString = posStr;
		}
	}
	
	/**
	 * Checks if this COLLECTIONPOSITION is a descendant of <code>otherPos</code>, e.g. "1.2.3" is a descendant of "1.2" and "1".
	 * 
	 * @param otherPos other position
	 * @return true if descendant
	 */
	public boolean isDescendantOf(JNADominoCollectionPosition otherPos) {
	  int ourLevel = getLevel();
	  int otherLevel = otherPos.getLevel();
	  
	  if (ourLevel > otherLevel) {
	    for (int i=0; i<otherLevel; i++) {
	      if (this.getTumbler(i) != otherPos.getTumbler(i)) {
	        return false;
	      }
	    }
	    return true;
	  }
	  return false;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> clazz) {
		if (clazz == NotesCollectionPositionStruct.class || clazz == Structure.class) {
			if (this.struct==null) {
				this.struct = NotesCollectionPositionStruct.newInstance();
				this.struct.Level = (short) (this.level & 0xffff);
				this.struct.MinLevel = (byte) (this.minLevel & 0xff);
				this.struct.MaxLevel = (byte) (this.maxLevel & 0xff);
				this.struct.Tumbler = this.tumbler.clone();
				this.struct.write();
			}
			return (T) this.struct;
		}
		return null;
	}
	
	/**
	 * # levels -1 in tumbler
	 * 
	 * @return levels
	 */
	public int getLevel() {
		if (this.struct!=null) {
			//get current struct value, is changed by NIFReadEntries
			this.level = this.struct.Level & 0xffff;
		}
		return this.level;
	}
	
	/**
	 * MINIMUM level that this position is allowed to be nagivated to. This is
	 * useful to navigate a subtree using all navigator codes. This field is
	 * IGNORED unless the NAVIGATE_MINLEVEL flag is enabled (for backward
	 * compatibility)
	 * 
	 * @return min level
	 */
	public int getMinLevel() {
		if (this.struct!=null) {
			//get current struct value, is changed by NIFReadEntries
			this.minLevel = (this.struct.MinLevel & 0xffff);
		}
		return this.minLevel;
	}
	
	/**
	 * Sets the MINIMUM level that this position is allowed to be nagivated to. This is
	 * useful to navigate a subtree using all navigator codes. This field is
	 * IGNORED unless the NAVIGATE_MINLEVEL flag is enabled (for backward
	 * compatibility)
	 * 
	 * @param level min level
	 */
	public void setMinLevel(int level) {
		this.minLevel = level;
		if (this.struct!=null) {
			this.struct.MinLevel = (byte) (level & 0xff);
			this.struct.write();
		}
    this.toString=null;
	}
	
	/**
	 * MAXIMUM level that this position is allowed to be navigated to. This is
	 * useful to navigate a subtree using all navigator codes.
	 * 
	 * @return max level
	 */
	public int getMaxLevel() {
		if (this.struct!=null) {
			//get current struct value, is changed by NIFReadEntries
			this.maxLevel = (this.struct.MaxLevel & 0xffff);
		}
		return this.maxLevel;
	}
	
	/**
	 * Sets the MAXIMUM level that this position is allowed to be navigated to. This is
	 * useful to navigate a subtree using all navigator codes.
	 * 
	 * @param level max level
	 */
	public void setMaxLevel(int level) {
		this.maxLevel = level;
		if (this.struct!=null) {
			this.struct.MaxLevel = (byte) (level & 0xff);
			this.struct.write();
		}
		this.toString=null;
	}
	
	/**
	 * Returns the index position at each view level
	 * 
	 * @param level 0 for first level
	 * @return position starting with 1 if not restricted by reader fields
	 */
	public int getTumbler(int level) {
		if (this.struct!=null) {
			//get current struct value, is changed by NIFReadEntries
			this.tumbler = this.struct.Tumbler;
		}
		return this.tumbler[level];
	}

	/**
	 * Returns the tumbler array
	 * 
	 * @return array, length == level
	 */
	public int[] toTumblerArray() {
	  if (this.struct!=null) {
      //get current struct value, is changed by NIFReadEntries
      this.tumbler = this.struct.Tumbler;
    }
	  int level = getLevel();
	  int[] arr = new int[level+1];
	  System.arraycopy(this.tumbler, 0, arr, 0, level+1);
	  return arr;
	}
	
	void resetToStringVal() {
	  toString=null;
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
	@Override
	public String toString() {
		boolean recalc;
		//cache if cached value needs to be recalculated
		if (this.toString==null) {
			recalc = true;
		}
		else if (this.struct!=null && (this.level != this.struct.Level ||
				this.minLevel != this.struct.MinLevel ||
				this.maxLevel != this.struct.MaxLevel ||
				!Arrays.equals(this.tumbler, this.struct.Tumbler))) {
			recalc = true;
		}
		else {
			recalc = false;
		}
		
		if (recalc) {
			int level = this.getLevel();
			int minLevel = this.getMinLevel();
			int maxLevel = this.getMaxLevel();
			
			StringBuilder sb = new StringBuilder();
			
			for (int i=0; i<=level; i++) {
				if (sb.length() > 0) {
					sb.append('.');
				}
				sb.append(this.getTumbler(i));
			}
			
			if (minLevel!=0 || maxLevel!=0) {
				sb.append("|").append(minLevel).append("-").append(maxLevel); //$NON-NLS-1$ //$NON-NLS-2$
			}
			toString = sb.toString();
		}
		return toString;
	}

  @Override
  public int compareTo(JNADominoCollectionPosition o) {
    int[] ourTumbler = tumbler;
    int[] otherTumbler = o.tumbler;
    int level = Math.min(ourTumbler.length, otherTumbler.length);
    
    for (int i=0; i<level; i++) {
      if (ourTumbler[i] < otherTumbler[i]) {
        return -1;
      }
      else if (ourTumbler[i] > otherTumbler[i]) {
        return 1;
      }
    }
    
    if (minLevel < o.minLevel) {
      return -1;
    }
    else if (minLevel > o.minLevel) {
      return 1;
    }
    
    if (maxLevel < o.maxLevel) {
      return -1;
    }
    else if (maxLevel > o.maxLevel) {
      return 1;
    }
    
    return 0;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof JNADominoCollectionPosition) {
      int[] ourTumbler = tumbler;
      int[] otherTumbler = ((JNADominoCollectionPosition)o).tumbler;
      int otherMinLevel = ((JNADominoCollectionPosition)o).minLevel;
      int otherMaxLevel = ((JNADominoCollectionPosition)o).maxLevel;
      
      return Arrays.equals(ourTumbler, otherTumbler) &&
          minLevel==otherMinLevel &&
          maxLevel==otherMaxLevel;
    }
    return false;
  }

  @Override
  protected Object clone() {
    JNADominoCollectionPosition clonedPos = new JNADominoCollectionPosition(tumbler.clone());
    clonedPos.minLevel = minLevel;
    clonedPos.maxLevel = maxLevel;
    return clonedPos;
  }
  
}
