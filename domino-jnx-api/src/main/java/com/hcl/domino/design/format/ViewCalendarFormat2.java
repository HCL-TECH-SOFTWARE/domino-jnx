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
package com.hcl.domino.design.format;

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.MemoryStructure;

/**
 * @since 1.0.27
 */
@StructureDefinition(
	name="VIEW_CALENDAR_FORMAT2",
	members={
		@StructureMember(name="Signature", type=short.class),
		@StructureMember(name="DayDateBkColor", type=ColorValue.class),
		@StructureMember(name="NonMonthBkColor", type=ColorValue.class),
		@StructureMember(name="NonMonthTextColor", type=ColorValue.class),
		@StructureMember(name="DayDateColor", type=ColorValue.class),
		@StructureMember(name="TimeSlotColor", type=ColorValue.class),
		@StructureMember(name="HeaderColor", type=ColorValue.class),
		@StructureMember(name="TodayRGBColor", type=ColorValue.class),
		@StructureMember(name="WeekDayMonthFont", type=FontStyle.class),
		@StructureMember(name="Spare", type=int[].class, length=3)
	}
)
public interface ViewCalendarFormat2 extends MemoryStructure {
	@StructureGetter("Signature")
	short getSignature();
	@StructureSetter("Signature")
	ViewCalendarFormat2 setSignature(short signature);
	
	@StructureGetter("DayDateBkColor")
	ColorValue getDayDateBackgroundColor();
	
	@StructureGetter("NonMonthBkColor")
	ColorValue getNonMonthBackgroundColor();
	
	@StructureGetter("DayDateColor")
	ColorValue getDayDateColor();
	
	@StructureGetter("TimeSlotColor")
	ColorValue getTimeSlotColor();
	
	@StructureGetter("HeaderColor")
	ColorValue getHeaderColor();
	
	@StructureGetter("TodayRGBColor")
	ColorValue getTodayColor();
	
	@StructureGetter("WeekDayMonthFont")
	FontStyle getWeekDayMonthFont();
}
