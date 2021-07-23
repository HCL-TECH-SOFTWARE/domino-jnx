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
	name="CDACTIONNEWSLETTER",
	members={
		@StructureMember(name="Header", type=WSIG.class),
		@StructureMember(name="dwFlags", type=CDActionNewsletter.Flag.class, bitfield=true),
		@StructureMember(name="dwGather", type=int.class, unsigned=true),
		@StructureMember(name="wViewNameLen", type=short.class, unsigned=true),
		@StructureMember(name="wSpare", type=short.class),
		@StructureMember(name="wFieldLen", type=short[].class, length=RichTextConstants.ACTIONSENDMAIL_FIELDCOUNT)
	}
)
public interface CDActionNewsletter extends RichTextRecord<WSIG> {
	enum Flag implements INumberEnum<Integer> {
		/** Summary of docs (with DocLinks)  */
		SUMMARY(RichTextConstants.ACTIONNEWSLETTER_FLAG_SUMMARY),
		/** Gather at least n before mailing  */
		GATHER(RichTextConstants.ACTIONNEWSLETTER_FLAG_GATHER),
		/** Include all notes when mailing out multiple notes  */
		INCLUDEALL(RichTextConstants.ACTIONNEWSLETTER_FLAG_INCLUDEALL)
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
	CDActionNewsletter setFlags(Collection<Flag> flags);
	
	@StructureGetter("dwGather")
	long getGatherCount();
	@StructureSetter("dwGather")
	CDActionNewsletter setGatherCount(long count);
	
	@StructureGetter("wViewNameLen")
	int getViewNameLength();
	@StructureSetter("wViewNameLen")
	CDActionNewsletter setViewNameLength(int len);
	
	@StructureGetter("wFieldLen")
	short[] getFieldLengthsRaw();
	@StructureSetter("wFieldLen")
	CDActionNewsletter setFieldLengthsRaw(short[] lengths);
	
	default int getToLength() {
		return Short.toUnsignedInt(getFieldLengthsRaw()[RichTextConstants.ACTIONSENDMAIL_TOFIELD]);
	}
	default CDActionNewsletter setToLength(int len) {
		short[] lengths = getFieldLengthsRaw();
		lengths[RichTextConstants.ACTIONSENDMAIL_TOFIELD] = (short)len;
		setFieldLengthsRaw(lengths);
		return this;
	}

	default int getCcLength() {
		return Short.toUnsignedInt(getFieldLengthsRaw()[RichTextConstants.ACTIONSENDMAIL_CCFIELD]);
	}
	default CDActionNewsletter setCcLength(int len) {
		short[] lengths = getFieldLengthsRaw();
		lengths[RichTextConstants.ACTIONSENDMAIL_CCFIELD] = (short)len;
		setFieldLengthsRaw(lengths);
		return this;
	}

	default int getBccLength() {
		return Short.toUnsignedInt(getFieldLengthsRaw()[RichTextConstants.ACTIONSENDMAIL_BCCFIELD]);
	}
	default CDActionNewsletter setBccLength(int len) {
		short[] lengths = getFieldLengthsRaw();
		lengths[RichTextConstants.ACTIONSENDMAIL_BCCFIELD] = (short)len;
		setFieldLengthsRaw(lengths);
		return this;
	}

	default int getSubjectLength() {
		return Short.toUnsignedInt(getFieldLengthsRaw()[RichTextConstants.ACTIONSENDMAIL_SUBJECTFIELD]);
	}
	default CDActionNewsletter setSubjectLength(int len) {
		short[] lengths = getFieldLengthsRaw();
		lengths[RichTextConstants.ACTIONSENDMAIL_SUBJECTFIELD] = (short)len;
		setFieldLengthsRaw(lengths);
		return this;
	}

	default int getBodyLength() {
		return Short.toUnsignedInt(getFieldLengthsRaw()[RichTextConstants.ACTIONSENDMAIL_BODYFIELD]);
	}
	default CDActionNewsletter setBodyLength(int len) {
		short[] lengths = getFieldLengthsRaw();
		lengths[RichTextConstants.ACTIONSENDMAIL_BODYFIELD] = (short)len;
		setFieldLengthsRaw(lengths);
		return this;
	}
	
	default String getViewName() {
		return StructureSupport.extractStringValue(this, 0, getViewNameLength());
	}
	default CDActionNewsletter setViewName(String viewName) {
		StructureSupport.writeStringValue(this, 0, getViewNameLength(), viewName, this::setViewNameLength);
		return this;
	}
	
	default String getTo() {
		return StructureSupport.extractStringValueWordPadded(
			this,
			getViewNameLength(),
			getToLength()
		);
	}
	default CDActionNewsletter setTo(String value) {
		return StructureSupport.writeStringValueWordPadded(
			this,
			getViewNameLength(),
			getToLength(),
			value,
			this::setToLength
		);
	}
	
	default String getCc() {
		return StructureSupport.extractStringValueWordPadded(
			this,
			getViewNameLength() + getToLength(),
			getCcLength()
		);
	}
	default CDActionNewsletter setCc(String value) {
		return StructureSupport.writeStringValueWordPadded(
			this,
			getViewNameLength() + getToLength(),
			getCcLength(),
			value,
			this::setCcLength
		);
	}
	
	default String getBcc() {
		return StructureSupport.extractStringValueWordPadded(
			this,
			getViewNameLength() + getToLength() + getCcLength(),
			getBccLength()
		);
	}
	default CDActionNewsletter setBcc(String value) {
		return StructureSupport.writeStringValueWordPadded(
			this,
			getViewNameLength() + getToLength() + getCcLength(),
			getBccLength(),
			value,
			this::setBccLength
		);
	}
	
	default String getSubject() {
		return StructureSupport.extractStringValueWordPadded(
			this,
			getViewNameLength() + getToLength() + getCcLength() + getBccLength(),
			getSubjectLength()
		);
	}
	default CDActionNewsletter setSubject(String value) {
		return StructureSupport.writeStringValueWordPadded(
			this,
			getViewNameLength() + getToLength() + getCcLength() + getBccLength(),
			getSubjectLength(),
			value,
			this::setSubjectLength
		);
	}
	
	default String getBody() {
		return StructureSupport.extractStringValueUnpacked(
			this,
			getViewNameLength() + getToLength() + getCcLength() + getBccLength() + getSubjectLength(),
			getBodyLength()
		);
	}
	default CDActionNewsletter setBody(String value) {
		return StructureSupport.writeStringValueUnpacked(
			this,
			getViewNameLength() + getToLength() + getCcLength() + getBccLength() + getSubjectLength(),
			getBodyLength(),
			value,
			this::setBodyLength
		);
	}
}
