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
package com.hcl.domino.calendar;

/**
 * Flags that control behavior of the calendar APIs that return iCalendar data for an entry or notice
 * 
 * Note: The values of these constants are the very same constants used by the C-API.
 * 
 * @author Karsten Lehmann
 */
public enum CalendarRangeRepeat {
	/** Modifying just this instance */
	CURRENT(0),
	/** Modifying all instances */
	ALL(1),
	/** Modifying current + previous */
	PREV(2),
	/** Modifying current + future */
	FUTURE(3);
	
	private int m_val;
	
	CalendarRangeRepeat(int val) {
		m_val = val;
	}
	
	public int getValue() {
		return m_val;
	}

}
