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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
	name="ODS_ASSISTFIELDSTRUCT",
	members={
		@StructureMember(name="wTotalLen", type=short.class, unsigned=true),
		@StructureMember(name="wOperator", type=short.class),
		@StructureMember(name="wFieldNameLen", type=short.class, unsigned=true),
		@StructureMember(name="wValueLen", type=short.class, unsigned=true),
		@StructureMember(name="wValueDataType", type=short.class),
		@StructureMember(name="wSpare", type=short.class)
	}
)
public interface AssistFieldStruct extends ResizableMemoryStructure {
	enum ActionByField implements INumberEnum<Short> {
		REPLACE(1),
		APPEND(2),
		REMOVE(3);
		private final short value;
		ActionByField(int value) { this.value = (short)value; }
		@Override
		public Short getValue() {
			return value;
		}
		@Override
		public long getLongValue() {
			return value;
		}
	}
	enum QueryByField implements INumberEnum<Short> {
		GREATER(1),	/*	Greater than value */
		LESS(2),	/*	Less than value */
		NOTEQUAL(3),	/*	Not equal to value */
		BETWEEN(4),	/*	Between Date1 and Date2 */
		NOTWITHIN(5),	/*	Not between Date1 and Date2 */
		EQUAL(6),	/*	Equal to value */
		CONTAINS(7),	/*	Contains value */
		INTHELAST(8),	/*	In the last n days */
		INTHENEXT(9),	/*	In the next n days */
		OLDERTHAN(10),	/*	Older than n days */
		DUEIN(11),	/*	Due more than n days from now */
		DOESNOTCONTAIN(12);	/*	Does not contain */
		private final short value;
		QueryByField(int value) { this.value = (short)value; }
		@Override
		public Short getValue() {
			return value;
		}
		@Override
		public long getLongValue() {
			return value;
		}
	}
	
	@StructureGetter("wTotalLen")
	int getTotalLength();
	@StructureSetter("wTotalLen")
	AssistFieldStruct setTotalLength(int totalLength);
	
	@StructureGetter("wOperator")
	short getOperatorRaw();
	@StructureGetter("wOperator")
	ActionByField getActionOperator();
	@StructureGetter("wOperator")
	QueryByField getQueryOperator();
	@StructureSetter("wOperator")
	AssistFieldStruct setOperatorRaw(short operator);
	@StructureSetter("wOperator")
	AssistFieldStruct setActionOperator(ActionByField actionByField);
	@StructureSetter("wOperator")
	AssistFieldStruct setQueryOperator(QueryByField queryByField);
	
	@StructureGetter("wFieldNameLen")
	int getFieldNameLength();
	@StructureSetter("wFieldNameLen")
	AssistFieldStruct setFieldNameLength(int fieldNameLength);
	
	@StructureGetter("wValueLen")
	int getValueLength();
	@StructureSetter("wValueLen")
	AssistFieldStruct setValueLength(int valueLength);
	
	@StructureGetter("wValueDataType")
	short getValueDataType();
	@StructureSetter("wValueDataType")
	AssistFieldStruct setValueDataType(short valueDataType);
	
	default String getFieldName() {
		ByteBuffer buf = getVariableData();
		int nameLen = getFieldNameLength();
		byte[] nameData = new byte[nameLen];
		buf.get(nameData);
		return new String(nameData, Charset.forName("LMBCS-native")); //$NON-NLS-1$
	}
	default AssistFieldStruct setFieldName(String name) {
		byte[] lmbcs = name == null ? new byte[0] : name.getBytes(Charset.forName("LMBCS-native")); //$NON-NLS-1$
		byte[] valueData = getValueData();
		
		int variableLen = lmbcs.length + valueData.length;
		variableLen += variableLen % 2;
		resizeVariableData(variableLen);
		ByteBuffer buf = getVariableData();
		buf.put(lmbcs);
		buf.put(valueData);
		
		setTotalLength(getData().remaining());
		setFieldNameLength(lmbcs.length);
		
		return this;
	}
	
	default byte[] getValueData() {
		ByteBuffer buf = getVariableData();
		int nameLen = getFieldNameLength();
		int valueLen = getValueLength();
		buf.position(buf.position()+nameLen);
		byte[] valueData = new byte[valueLen];
		buf.get(valueData);
		return valueData;
	}
	
	default List<String> getValues() {
		ByteBuffer buf = getVariableData();
		int nameLen = getFieldNameLength();
		buf.position(buf.position()+nameLen);
		
		// WORD: TYPE_TEXT_LIST
		buf.getShort();
		// USHORT ListEntries
		int listEntries = Short.toUnsignedInt(buf.getShort());
		int[] stringLengths = new int[listEntries];
		for(int i = 0; i < listEntries; i++) {
			stringLengths[i] = Short.toUnsignedInt(buf.getShort());
		}
		Charset lmbcsCharset = Charset.forName("LMBCS-native"); //$NON-NLS-1$
		List<String> result = new ArrayList<>(listEntries);
		for(int i = 0; i < listEntries; i++) {
			byte lmbcs[] = new byte[stringLengths[i]];
			buf.get(lmbcs);
			result.add(new String(lmbcs, lmbcsCharset));
		}
		return result;
	}
	default AssistFieldStruct setValues(Collection<String> values) {
		Collection<String> vals = values == null ? Collections.emptyList() : values;

		int valueLen = 2 // WORD: TYPE_TEXT_LIST
			+ 2 // USHORT ListEntries
			+ (2*vals.size()); // USHORT string lengs
		Charset lmbcsCharset = Charset.forName("LMBCS-native"); //$NON-NLS-1$
		List<byte[]> lmbcs = new ArrayList<>(vals.size());
		for(String val : vals) {
			byte[] valBytes = val.getBytes(lmbcsCharset);
			if(valBytes.length > 0xFFFF) {
				throw new IllegalArgumentException(MessageFormat.format("Unable to store LMBCS value of length {0}", valBytes.length));
			}
			lmbcs.add(valBytes);
		}
		valueLen += lmbcs.stream().mapToInt(b -> b.length).sum();

		int nameLen = getFieldNameLength();
		int variableLen = nameLen + valueLen;
		variableLen += variableLen % 2;
		if(variableLen + 12 > 0xFFFF) {
			throw new IllegalArgumentException("Unable to store total value exceeding 0xFFFF bytes");
		}
		resizeVariableData(variableLen);
		
		ByteBuffer buf = getVariableData();
		buf.position(buf.position()+nameLen);
		buf.putShort((short)(1 + (5 << 8)));
		buf.putShort((short)vals.size());
		for(byte[] val : lmbcs) {
			buf.putShort((short)val.length);
		}
		for(byte[] val : lmbcs) {
			buf.put(val);
		}

		setTotalLength(getData().remaining());
		setValueLength(valueLen);;
		
		return this;
	}
}
