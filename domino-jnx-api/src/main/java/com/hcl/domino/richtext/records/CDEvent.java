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
package com.hcl.domino.richtext.records;

import java.util.Collection;
import java.util.Set;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.0.15
 */
@StructureDefinition(
	name="CDEVENT",
	members={
		@StructureMember(name="Header", type=WSIG.class),
		@StructureMember(name="Flags", type=CDEvent.EventFlag.class, bitfield=true),
		@StructureMember(name="EventType", type=CDEvent.EventType.class),
		@StructureMember(name="ActionType", type=CDEvent.ActionType.class),
		@StructureMember(name="ActionLength", type=int.class, unsigned=true),
		@StructureMember(name="SignatureLength", type=short.class, unsigned=true),
		@StructureMember(name="Reserved", type=byte[].class, length=14)
	}
)
public interface CDEvent extends RichTextRecord<WSIG> {
	enum EventFlag implements INumberEnum<Integer> {
		HAS_LIBRARIES(0x00000001);
		
		private final int value;
		EventFlag(int value) { this.value = value; }
		@Override
		public long getLongValue() {
			return value;
		}
		@Override
		public Integer getValue() {
			return value;
		}
	}
	enum EventType implements INumberEnum<Short> {
		ONCLICK((short)1),
		ONDBLCLICK((short)2),
		ONMOUSEDOWN((short)3),
		ONMOUSEUP((short)4),
		ONMOUSEOVER((short)5),
		ONMOUSEMOVE((short)6),
		ONMOUSEOUT((short)7),
		ONKEYPRESS((short)8),
		ONKEYDOWN((short)9),
		ONKEYUP((short)10),
		ONFOCUS((short)11),
		ONBLUR((short)12),
		ONLOAD((short)13),
		ONUNLOAD((short)14),
		HEADER((short)15),
		ONSUBMIT((short)16),
		ONRESET((short)17),
		ONCHANGE((short)18),
		ONERROR((short)19),
		ONHELP((short)20),
		ONSELECT((short)21),
		/** This isn't really an event */
		LIBRARY((short)22);
		private final short value;
		EventType(short value) { this.value = value; }
		@Override
		public long getLongValue() {
			return value;
		}
		@Override
		public Short getValue() {
			return value;
		}
	}
	enum ActionType implements INumberEnum<Short> {
		FORMULA((short)1),
		CANNED_ACTION((short)2),
		LOTUS_SCRIPT((short)3),
		JAVASCRIPT((short)4);
		private final short value;
		ActionType(short value) { this.value = value; }
		@Override
		public long getLongValue() {
			return value;
		}
		@Override
		public Short getValue() {
			return value;
		}
	}
	
	@StructureGetter("Header")
	@Override
	WSIG getHeader();
	
	@StructureGetter("Flags")
	Set<EventFlag> getFlags();
	@StructureSetter("Flags")
	CDEvent setFlags(Collection<EventFlag> flags);
	
	@StructureGetter("EventType")
	EventType getEventType();
	@StructureSetter("EventType")
	CDEvent setEventType(EventType eventType);
	
	@StructureGetter("ActionType")
	ActionType getActionType();
	@StructureSetter("ActionType")
	CDEvent setActionType(ActionType actionType);
	
	@StructureGetter("ActionLength")
	long getActionLength();
	@StructureSetter("ActionLength")
	CDEvent setActionLength(long actionLength);
	
	@StructureGetter("SignatureLength")
	int getSignatureLength();
	@StructureSetter("SignatureLength")
	CDEvent setSignatureLength(int signatureLength);
}
