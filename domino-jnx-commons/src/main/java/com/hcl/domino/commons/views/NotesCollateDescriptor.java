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
package com.hcl.domino.commons.views;

import com.hcl.domino.data.DominoCollection.Direction;
import com.hcl.domino.misc.NotesConstants;

/**
 * Data container for a single sort column of a {@link NotesCollationInfo}
 * 
 * @author Karsten Lehmann
 */
public class NotesCollateDescriptor {
	private NotesCollationInfo m_parentCollateInfo;
	private String m_name;
	private CollateType m_keyType;
	private byte m_flags;
	
	public NotesCollateDescriptor(NotesCollationInfo parentCollateInfo, String name, CollateType type, byte flags) {
		m_parentCollateInfo = parentCollateInfo;
		m_name = name;
		m_keyType = type;
		m_flags = flags;
	}
	
	/**
	 * Returns the collation that this descriptor is part of
	 * 
	 * @return collation
	 */
	public NotesCollationInfo getParent() {
		return m_parentCollateInfo;
	}
	
	/**
	 * Returns the sort direction
	 * 
	 * @return sorting
	 */
	public Direction getDirection() {
		return (m_flags & NotesConstants.CDF_M_descending) == NotesConstants.CDF_M_descending ? Direction.Descending : Direction.Ascending;
	}
	
	/**
	 * Returns the collate type
	 * 
	 * @return type
	 */
	public CollateType getType() {
		return m_keyType;
	}
	
	/**
	 * Returns the item name of the column to be sorted
	 * 
	 * @return item name
	 */
	public String getName() {
		return m_name;
	}
	
	/**
	 * If prefix list, then ignore for sorting
	 * 
	 * @return true to ignore
	 */
	public boolean isIgnorePrefixes() {
		return (m_flags & NotesConstants.CDF_M_ignoreprefixes) == NotesConstants.CDF_M_ignoreprefixes;
	}

	/**
	 * If set, text compares are case-sensitive
	 * 
	 * @return true for case-sensitive
	 */
	public boolean isCaseSensitiveSort() {
		return (m_flags & NotesConstants.CDF_M_casesensitive_in_v5) == NotesConstants.CDF_M_casesensitive_in_v5;
	}
	
	/**
	 * If set, text compares are accent-sensitive
	 * 
	 * @return true for accent-sensitive
	 */
	public boolean isAccentSensitiveSort() {
		return (m_flags & NotesConstants.CDF_M_accentsensitive_in_v5) == NotesConstants.CDF_M_accentsensitive_in_v5;
	}
	
	/**
	 * If set, lists are permuted
	 * 
	 * @return true if permuted
	 */
	public boolean isPermuted() {
		return (m_flags & NotesConstants.CDF_M_permuted) == NotesConstants.CDF_M_permuted;
	}
	
	/**
	 * Qualifier if lists are permuted; if set, lists are pairwise permuted, otherwise lists are multiply permuted
	 * 
	 * @return true if pairwise permuted
	 */
	public boolean isPermutedPairwise() {
		return (m_flags & NotesConstants.CDF_M_permuted_pairwise) == NotesConstants.CDF_M_permuted_pairwise;
	}
	
	/**
	 * If set, treat as permuted
	 * 
	 * @return true if permuted
	 */
	public boolean isFlat() {
		return (m_flags & NotesConstants.CDF_M_flat_in_v5) == NotesConstants.CDF_M_flat_in_v5;
	}
}
