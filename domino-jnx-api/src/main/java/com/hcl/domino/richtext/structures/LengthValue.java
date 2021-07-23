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

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.0.15
 */
@StructureDefinition(
	name="LENGTH_VALUE",
	members={
		@StructureMember(name="Flags", type=LengthValue.Flag.class, bitfield=true),
		@StructureMember(name="Length", type=double.class),
		@StructureMember(name="Units", type=LengthValue.Unit.class),
		@StructureMember(name="Reserved", type=byte.class)
	}
)
public interface LengthValue extends MemoryStructure {
	enum Flag implements INumberEnum<Short> {
		AUTO(0x0001),
		INHERIT(0x0002)
		;
		private final short value;
		Flag(int value) { this.value = (short)value; }
		
		@Override
		public long getLongValue() {
			return value;
		}
		@Override
		public Short getValue() {
			return value;
		}
	}
	enum Unit implements INumberEnum<Byte> {
		UNKNOWN(0),
		TWIPS(1),
		PIXELS(2),
		PERCENT(3),
		EMS(4),
		EXS(5),
		CHARS(6)
		;
		private final byte value;
		Unit(int value) { this.value = (byte)value; }
		
		@Override
		public long getLongValue() {
			return value;
		}
		@Override
		public Byte getValue() {
			return value;
		}
	}
	
	@StructureGetter("Flags")
	Set<Flag> getFlags();
	@StructureSetter("Flags")
	LengthValue setFlags(Collection<Flag> flags);
	
	@StructureGetter("Length")
	double getLength();
	@StructureSetter("Length")
	LengthValue setLength(double length);
	
	@StructureGetter("Units")
	Unit getUnit();
	@StructureSetter("Units")
	LengthValue setUnit(Unit unit);
}
