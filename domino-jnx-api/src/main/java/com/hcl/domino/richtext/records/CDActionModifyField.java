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
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.0.24
 */
@StructureDefinition(
	name="CDACTIONMODIFYFIELD",
	members={
		@StructureMember(name="Header", type=WSIG.class),
		@StructureMember(name="dwFlags", type=CDActionModifyField.Flag.class, bitfield=true),
		@StructureMember(name="wFieldNameLen", type=short.class, unsigned=true),
		@StructureMember(name="wValueLen", type=short.class, unsigned=true)
	}
)
public interface CDActionModifyField extends RichTextRecord<WSIG> {
	enum Flag implements INumberEnum<Integer> {
		/** Replace field value */
		REPLACE(RichTextConstants.MODIFYFIELD_FLAG_REPLACE),
		/** Append field value */
		APPEND(RichTextConstants.MODIFYFIELD_FLAG_APPEND)
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
	
	@StructureGetter("Header")
	@Override
	WSIG getHeader();
	
	@StructureGetter("dwFlags")
	Set<Flag> getFlags();
	@StructureSetter("dwFlags")
	CDActionModifyField setFlags(Collection<Flag> flags);
	
	@StructureGetter("wFieldNameLen")
	int getFieldNameLength();
	@StructureSetter("wFieldNameLen")
	CDActionModifyField setFieldNameLength(int len);
	
	@StructureGetter("wValueLen")
	int getValueLength();
	@StructureSetter("wValueLen")
	CDActionModifyField setValueLength(int len);
	
	default String getFieldName() {
		return StructureSupport.extractStringValue(this, 0, getFieldNameLength());
	}
	default CDActionModifyField setFieldName(String fieldName) {
		StructureSupport.writeStringValue(this, 0, getFieldNameLength(), fieldName, this::setFieldNameLength);
		return this;
	}
	
	default String getValue() {
		return StructureSupport.extractStringValue(this, getFieldNameLength(), getValueLength());
	}
	default CDActionModifyField setValue(String value) {
		StructureSupport.writeStringValue(this, getFieldNameLength(), getValueLength(), value, this::setValueLength);
		return this;
	}
}
