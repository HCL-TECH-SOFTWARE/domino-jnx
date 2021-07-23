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
package com.hcl.domino.data;

public enum Navigate {
	/** Remain at current position (reset position and return data). */
	CURRENT(0),
	/** next category or document */
	NEXT_ENTRY(1),
	/** Up 1 level */
	PARENT_ENTRY(3),
	/** Down 1 level to first child */
	CHILD_ENTRY(4),
	/** First node at our level */
	FIRST_ON_SAME_LEVEL(7),
	/** Last node at our level */
	LAST_ON_SAME_LEVEL(8),
	/** next peer entry (same level) */
	NEXT_ON_SAME_LEVEL(5),
	/** next top level entry */
	NEXT_ON_TOPLEVEL(12),
	/** next parent entry */
	NEXT_PARENT_ENTRY(19),
	/** next unread document */
	NEXT_UNREAD_ENTRY(10),
	/** next unread toplevel entry */
	NEXT_UNREAD_TOPLEVEL_ENTRY(18),
	/** next selected entry */
	NEXT_SELECTED(14),
	/** next selected top level entry */
	NEXT_SELECTED_ON_TOPLEVEL(32),
	/** next expanded category or document */
	NEXT_EXPANDED(15),
	/** next expanded unread document */
	NEXT_EXPANDED_UNREAD(23),
	/** next expanded and selected category or document */
	NEXT_EXPANDED_SELECTED(25),
	/** next expanded category */
	NEXT_EXPANDED_CATEGORY(27),
	/** next expanded document */
	NEXT_EXPANDED_DOCUMENT(39),
//	/** next search hit */
//	NEXT_HIT(29),
//	/** next selected search hit */
//	NEXT_SELECTED_HIT(35),
//	/** next unread search hit */
//	NEXT_UNREAD_HIT(37),
	/** next category */
	NEXT_CATEGORY(41),
	/** next document */
	NEXT_DOCUMENT(43),
	/** previous category or document */
	PREV_ENTRY(9),
	/** previous peer (same level) */
	PREV_ON_SAME_LEVEL(6),
	/** previous top level entry */
	PREV_ON_TOPLEVEL(13),
	/** previous parent entry */
	PREV_PARENT_ENTRY(20),
	/** previous unread entry */
	PREV_UNREAD_ENTRY(21),
	/** previous unread top level entry */
	PREV_UNREAD_TOPLEVEL_ENTRY(34),
	/** previous selected entry */
	PREV_SELECTED(22),
	/** previous selected top level entry */
	PREV_SELECTED_ON_TOPLEVEL(33),
	/** previous expanded category or document */
	PREV_EXPANDED(16),
	/** previous expanded unread document */
	PREV_EXPANDED_UNREAD(24),
	/** previous expanded selected category or document */
	PREV_EXPANDED_SELECTED(26),
	/** previous expanded category */
	PREV_EXPANDED_CATEGORY(28),
	/** previous expanded document */
	PREV_EXPANDED_DOCUMENT(40),
//	/** previous search hit */
//	PREV_HIT(30),
//	/** previous selected search hit */
//	PREV_SELECTED_HIT(36),
//	/** PREV, but only "unread" and FTSearch "hit" entries (in the SAME ORDER as the hit's relevance ranking) */
//	PREV_UNREAD_HIT(38),
	/** previous category entry */
	PREV_CATEGORY(42),
	/** previous document */
	PREV_DOCUMENT(44);
	
	private short m_value;
	
	Navigate(int value) {
		m_value = (short) (value & 0xffff);
	}
	
	public short getValue() {
		return m_value;
	}
}