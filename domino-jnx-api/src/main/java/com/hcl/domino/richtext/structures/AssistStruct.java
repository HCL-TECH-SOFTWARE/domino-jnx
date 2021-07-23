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
package com.hcl.domino.richtext.structures;

import java.util.Collection;
import java.util.Set;

import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.design.agent.AgentInterval;
import com.hcl.domino.design.agent.AgentTrigger;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.0.24
 */
@StructureDefinition(
	name="ODS_ASSISTSTRUCT",
	members={
		@StructureMember(name="wVersion", type=short.class, unsigned=true),
		@StructureMember(name="wTriggerType", type=AgentTrigger.class),
		@StructureMember(name="wSearchType", type=AssistStruct.Search.class),
		@StructureMember(name="wIntervalType", type=AgentInterval.class),
		@StructureMember(name="wInterval", type=short.class, unsigned=true),
		@StructureMember(name="dwTime1", type=int.class),
		@StructureMember(name="dwTime2", type=int.class),
		@StructureMember(name="StartTime", type=OpaqueTimeDate.class),
		@StructureMember(name="EndTime", type=OpaqueTimeDate.class),
		@StructureMember(name="dwFlags", type=AssistStruct.Flag.class, bitfield=true),
		@StructureMember(name="dwSpare", type=int[].class, length=16)
	}
)
public interface AssistStruct extends ResizableMemoryStructure {
	enum Search implements INumberEnum<Short> {
		/** Unknown or unavailable  */
		NONE(RichTextConstants.ASSISTSEARCH_TYPE_NONE),
		/** All documents in database  */
		ALL(RichTextConstants.ASSISTSEARCH_TYPE_ALL),
		/** New documents since last run  */
		NEW(RichTextConstants.ASSISTSEARCH_TYPE_NEW),
		/** New or modified docs since last run  */
		MODIFIED(RichTextConstants.ASSISTSEARCH_TYPE_MODIFIED),
		/** Selected documents  */
		SELECTED(RichTextConstants.ASSISTSEARCH_TYPE_SELECTED),
		/** All documents in view  */
		VIEW(RichTextConstants.ASSISTSEARCH_TYPE_VIEW),
		/** All unread documents  */
		UNREAD(RichTextConstants.ASSISTSEARCH_TYPE_UNREAD),
		/** Prompt user  */
		PROMPT(RichTextConstants.ASSISTSEARCH_TYPE_PROMPT),
		/** Works on the selectable object  */
		UI(RichTextConstants.ASSISTSEARCH_TYPE_UI)
		;
		private final short value;
		Search(short value) { this.value = value; }
		@Override
		public Short getValue() {
			return value;
		}
		@Override
		public long getLongValue() {
			return value;
		}
	}
	enum Flag implements INumberEnum<Integer> {
		/** TRUE if manual assistant is hidden  */
		HIDDEN(RichTextConstants.ASSISTODS_FLAG_HIDDEN),
		/** Do not run on weekends  */
		NOWEEKENDS(RichTextConstants.ASSISTODS_FLAG_NOWEEKENDS),
		/** TRUE if storing highlights  */
		STOREHIGHLIGHTS(RichTextConstants.ASSISTODS_FLAG_STOREHIGHLIGHTS),
		/** TRUE if this is the V3-style mail and paste macro  */
		MAILANDPASTE(RichTextConstants.ASSISTODS_FLAG_MAILANDPASTE),
		/** TRUE if server to run on should be chosed when enabled  */
		CHOOSEWHENENABLED(RichTextConstants.ASSISTODS_FLAG_CHOOSEWHENENABLED)
		;
		private final int value;
		Flag(int value) { this.value = value; }
		@Override
		public Integer getValue() {
			return value;
		}
		@Override
		public long getLongValue() {
			return value;
		}
	}
	
	@StructureGetter("wVersion")
	int getVersion();
	@StructureSetter("wVersion")
	AssistStruct setVersion(int version);
	
	@StructureGetter("wTriggerType")
	AgentTrigger getTrigger();
	@StructureSetter("wTriggerType")
	AssistStruct setTrigger(AgentTrigger trigger);
	
	@StructureGetter("wSearchType")
	Search getSearch();
	@StructureSetter("wSearchType")
	AssistStruct setSearch(Search search);
	
	@StructureGetter("wIntervalType")
	AgentInterval getIntervalType();
	@StructureSetter("wIntervalType")
	AssistStruct setIntervalType(AgentInterval interval);
	
	@StructureGetter("wInterval")
	int getInterval();
	@StructureSetter("wInterval")
	AssistStruct setInterval(int interval);
	
	@StructureGetter("dwTime1")
	int getTime1();
	@StructureSetter("dwTime1")
	AssistStruct setTime1(int time1);
	
	@StructureGetter("dwTime2")
	int getTime2();
	@StructureSetter("dwTime2")
	AssistStruct setTime2(int time2);
	
	@StructureGetter("StartTime")
	DominoDateTime getStartDate();
	@StructureSetter("StartTime")
	AssistStruct setStartDate(DominoDateTime startDate);
	
	@StructureGetter("EndTime")
	DominoDateTime getEndDate();
	@StructureSetter("EndTime")
	AssistStruct setEndDate(DominoDateTime endDate);
	
	@StructureGetter("dwFlags")
	Set<Flag> getFlags();
	@StructureSetter("dwFlags")
	AssistStruct setFlags(Collection<Flag> flags);
}
