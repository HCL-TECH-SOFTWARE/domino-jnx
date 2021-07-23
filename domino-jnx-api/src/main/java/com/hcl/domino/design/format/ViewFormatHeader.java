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

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.ViewFormatConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.MemoryStructure;

@StructureDefinition(
	name="VIEW_FORMAT_HEADER",
	members={
		@StructureMember(name="Version", type=ViewFormatHeader.Version.class),
		@StructureMember(name="ViewStyle", type=ViewFormatHeader.ViewStyle.class)
	}
)
public interface ViewFormatHeader extends MemoryStructure {
	enum Version implements INumberEnum<Byte> {
		VERSION1(ViewFormatConstants.VIEW_FORMAT_VERSION);
		private final byte value;
		private Version(byte value) { this.value = value; }
		@Override
		public Byte getValue() {
			return value;
		}
		@Override
		public long getLongValue() {
			return value;
		}
	}
	enum ViewStyle implements INumberEnum<Byte> {
		TABLE(ViewFormatConstants.VIEW_STYLE_TABLE),
		DAY(ViewFormatConstants.VIEW_STYLE_DAY),
		WEEK(ViewFormatConstants.VIEW_STYLE_WEEK),
		MONTH(ViewFormatConstants.VIEW_STYLE_MONTH);
		private final byte value;
		private ViewStyle(byte value) { this.value = value; }
		@Override
		public Byte getValue() {
			return value;
		}
		@Override
		public long getLongValue() {
			return value;
		}
	}
	
	@StructureGetter("Version")
	Version getVersion();
	@StructureSetter("Version")
	ViewFormatHeader setVersion(Version version);
	
	@StructureGetter("ViewStyle")
	ViewStyle getViewStyle();
	@StructureSetter("ViewStyle")
	ViewFormatHeader setStyle(ViewStyle style);
}
