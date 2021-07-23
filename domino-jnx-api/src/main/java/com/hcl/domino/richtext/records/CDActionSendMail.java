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
	name="CDACTIONSENDMAIL",
	members={
		@StructureMember(name="Header", type=WSIG.class),
		@StructureMember(name="dwFlags", type=CDActionSendMail.Flag.class, bitfield=true),
		@StructureMember(name="wFieldLen", type=short[].class, length=RichTextConstants.ACTIONSENDMAIL_FIELDCOUNT)
	}
)
public interface CDActionSendMail extends RichTextRecord<WSIG> {
	enum Flag implements INumberEnum<Integer> {
		/** Include matching document  */
		INCLUDEDOC(RichTextConstants.ACTIONSENDMAIL_FLAG_INCLUDEDOC),
		/** Include doclink to document  */
		INCLUDELINK(RichTextConstants.ACTIONSENDMAIL_FLAG_INCLUDELINK),
		/** save copy  */
		SAVEMAIL(RichTextConstants.ACTIONSENDMAIL_FLAG_SAVEMAIL),
		/** To field is a formula  */
		TOFORMULA(RichTextConstants.ACTIONSENDMAIL_FLAG_TOFORMULA),
		/** cc field is a formula  */
		CCFORMULA(RichTextConstants.ACTIONSENDMAIL_FLAG_CCFORMULA),
		/** bcc field is a formula  */
		BCCFORMULA(RichTextConstants.ACTIONSENDMAIL_FLAG_BCCFORMULA),
		/** Subject field is a formula  */
		SUBJECTFORMULA(RichTextConstants.ACTIONSENDMAIL_FLAG_SUBJECTFORMULA),
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
	CDActionSendMail setFlags(Collection<Flag> flags);
	
	@StructureGetter("wFieldLen")
	short[] getFieldLengthsRaw();
	@StructureSetter("wFieldLen")
	CDActionSendMail setFieldLengthsRaw(short[] lengths);
	
	default int getToLength() {
		return Short.toUnsignedInt(getFieldLengthsRaw()[RichTextConstants.ACTIONSENDMAIL_TOFIELD]);
	}
	default CDActionSendMail setToLength(int len) {
		short[] lengths = getFieldLengthsRaw();
		lengths[RichTextConstants.ACTIONSENDMAIL_TOFIELD] = (short)len;
		setFieldLengthsRaw(lengths);
		return this;
	}

	default int getCcLength() {
		return Short.toUnsignedInt(getFieldLengthsRaw()[RichTextConstants.ACTIONSENDMAIL_CCFIELD]);
	}
	default CDActionSendMail setCcLength(int len) {
		short[] lengths = getFieldLengthsRaw();
		lengths[RichTextConstants.ACTIONSENDMAIL_CCFIELD] = (short)len;
		setFieldLengthsRaw(lengths);
		return this;
	}

	default int getBccLength() {
		return Short.toUnsignedInt(getFieldLengthsRaw()[RichTextConstants.ACTIONSENDMAIL_BCCFIELD]);
	}
	default CDActionSendMail setBccLength(int len) {
		short[] lengths = getFieldLengthsRaw();
		lengths[RichTextConstants.ACTIONSENDMAIL_BCCFIELD] = (short)len;
		setFieldLengthsRaw(lengths);
		return this;
	}

	default int getSubjectLength() {
		return Short.toUnsignedInt(getFieldLengthsRaw()[RichTextConstants.ACTIONSENDMAIL_SUBJECTFIELD]);
	}
	default CDActionSendMail setSubjectLength(int len) {
		short[] lengths = getFieldLengthsRaw();
		lengths[RichTextConstants.ACTIONSENDMAIL_SUBJECTFIELD] = (short)len;
		setFieldLengthsRaw(lengths);
		return this;
	}

	default int getBodyLength() {
		return Short.toUnsignedInt(getFieldLengthsRaw()[RichTextConstants.ACTIONSENDMAIL_BODYFIELD]);
	}
	default CDActionSendMail setBodyLength(int len) {
		short[] lengths = getFieldLengthsRaw();
		lengths[RichTextConstants.ACTIONSENDMAIL_BODYFIELD] = (short)len;
		setFieldLengthsRaw(lengths);
		return this;
	}
	
	default String getTo() {
		if(getFlags().contains(Flag.TOFORMULA)) {
			return StructureSupport.extractCompiledFormula(
				this,
				0,
				getToLength()
			);
		} else {
			return StructureSupport.extractStringValueUnpacked(
				this,
				0,
				getToLength()
			);
		}
	}
	default CDActionSendMail setTo(String value) {
		Set<Flag> flags = getFlags();
		flags.remove(Flag.TOFORMULA);
		setFlags(flags);
		return StructureSupport.writeStringValueUnpacked(
			this,
			0,
			getToLength(),
			value,
			this::setToLength
		);
	}
	default CDActionSendMail setToFormula(String value) {
		Set<Flag> flags = getFlags();
		flags.add(Flag.TOFORMULA);
		setFlags(flags);
		return StructureSupport.writeCompiledFormula(
			this,
			0,
			getToLength(),
			value,
			this::setToLength
		);
	}
	
	default String getCc() {
		if(getFlags().contains(Flag.CCFORMULA)) {
			return StructureSupport.extractCompiledFormula(
				this,
				getToLength(),
				getCcLength()
			);
		} else {
			return StructureSupport.extractStringValueUnpacked(
				this,
				getToLength(),
				getCcLength()
			);
		}
	}
	default CDActionSendMail setCc(String value) {
		Set<Flag> flags = getFlags();
		flags.remove(Flag.CCFORMULA);
		setFlags(flags);
		return StructureSupport.writeStringValueUnpacked(
			this,
			getToLength(),
			getCcLength(),
			value,
			this::setCcLength
		);
	}
	default CDActionSendMail setCcFormula(String value) {
		Set<Flag> flags = getFlags();
		flags.add(Flag.CCFORMULA);
		setFlags(flags);
		return StructureSupport.writeCompiledFormula(
			this,
			getToLength(),
			getCcLength(),
			value,
			this::setCcLength
		);
	}
	
	default String getBcc() {
		if(getFlags().contains(Flag.BCCFORMULA)) {
			return StructureSupport.extractCompiledFormula(
				this,
				getToLength() + getCcLength(),
				getBccLength()
			);
		} else {
			return StructureSupport.extractStringValueUnpacked(
				this,
				getToLength() + getCcLength(),
				getBccLength()
			);
		}
	}
	default CDActionSendMail setBcc(String value) {
		Set<Flag> flags = getFlags();
		flags.remove(Flag.BCCFORMULA);
		setFlags(flags);
		return StructureSupport.writeStringValueUnpacked(
			this,
			getToLength() + getCcLength(),
			getBccLength(),
			value,
			this::setBccLength
		);
	}
	default CDActionSendMail setBccFormula(String value) {
		Set<Flag> flags = getFlags();
		flags.remove(Flag.BCCFORMULA);
		setFlags(flags);
		return StructureSupport.writeCompiledFormula(
			this,
			getToLength() + getCcLength(),
			getBccLength(),
			value,
			this::setBccLength
		);
	}
	
	default String getSubject() {
		if(getFlags().contains(Flag.SUBJECTFORMULA)) {
			return StructureSupport.extractCompiledFormula(
				this,
				getToLength() + getCcLength() + getBccLength(),
				getSubjectLength()
			);
		} else {
			return StructureSupport.extractStringValueUnpacked(
				this,
				getToLength() + getCcLength() + getBccLength(),
				getSubjectLength()
			);
		}
	}
	default CDActionSendMail setSubject(String value) {
		Set<Flag> flags = getFlags();
		flags.remove(Flag.SUBJECTFORMULA);
		setFlags(flags);
		return StructureSupport.writeStringValueUnpacked(
			this,
			getToLength() + getCcLength() + getBccLength(),
			getSubjectLength(),
			value,
			this::setSubjectLength
		);
	}
	default CDActionSendMail setSubjectFormula(String value) {
		Set<Flag> flags = getFlags();
		flags.add(Flag.SUBJECTFORMULA);
		setFlags(flags);
		return StructureSupport.writeCompiledFormula(
			this,
			getToLength() + getCcLength() + getBccLength(),
			getSubjectLength(),
			value,
			this::setSubjectLength
		);
	}
	
	default String getBody() {
		return StructureSupport.extractStringValueUnpacked(
			this,
			getToLength() + getCcLength() + getBccLength() + getSubjectLength(),
			getBodyLength()
		);
	}
	default CDActionSendMail setBody(String value) {
		return StructureSupport.writeStringValueUnpacked(
			this,
			getToLength() + getCcLength() + getBccLength() + getSubjectLength(),
			getBodyLength(),
			value,
			this::setBodyLength
		);
	}
}
