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

import com.hcl.domino.misc.StructureSupport;
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
	name="CDIDNAME",
	members={
		@StructureMember(name="Header", type=WSIG.class),
		@StructureMember(name="Length", type=short.class, unsigned=true),
		@StructureMember(name="wClassLen", type=short.class, unsigned=true),
		@StructureMember(name="wStyleLen", type=short.class, unsigned=true),
		@StructureMember(name="wTitleLen", type=short.class, unsigned=true),
		@StructureMember(name="wExtraLen", type=short.class, unsigned=true),
		@StructureMember(name="wNameLen", type=short.class, unsigned=true),
		@StructureMember(name="reserved", type=byte[].class, length=10)
	}
)
public interface CDIDName extends RichTextRecord<WSIG> {
	@StructureGetter("Header")
	@Override
	WSIG getHeader();
	
	@StructureGetter("Length")
	int getIDLength();
	@StructureSetter("Length")
	CDIDName setIDLength(int length);
	
	@StructureGetter("wClassLen")
	int getClassNameLength();
	@StructureSetter("wClassLen")
	CDIDName setClassNameLength(int length);
	
	@StructureGetter("wStyleLen")
	int getStyleLength();
	@StructureSetter("wStyleLen")
	CDIDName setStyleLength(int length);
	
	@StructureGetter("wTitleLen")
	int getTitleLength();
	@StructureSetter("wTitleLen")
	CDIDName setTitleLength(int length);
	
	@StructureGetter("wExtraLen")
	int getHTMLAttributesLength();
	@StructureSetter("wExtraLen")
	CDIDName setHTMLAttributesLength(int length);
	
	@StructureGetter("wNameLen")
	int getNameLength();
	@StructureSetter("wNameLen")
	CDIDName setNameLength(int length);
	
	default String getID() {
		return StructureSupport.extractStringValue(
			this,
			0,
			getIDLength()
		);
	}
	default CDIDName setID(String id) {
		StructureSupport.writeStringValue(
			this,
			0,
			getIDLength(),
			id,
			this::setIDLength
		);
		return this;
	}

	default String getClassName() {
		return StructureSupport.extractStringValue(
			this,
			getIDLength(),
			getClassNameLength()
		);
	}
	default CDIDName setClassName(String className) {
		StructureSupport.writeStringValue(
			this,
			getIDLength(),
			getClassNameLength(),
			className,
			this::setClassNameLength
		);
		return this;
	}

	default String getStyle() {
		return StructureSupport.extractStringValue(
			this,
			getIDLength() + getClassNameLength(),
			getStyleLength()
		);
	}
	default CDIDName setStyle(String style) {
		StructureSupport.writeStringValue(
			this,
			getIDLength() + getClassNameLength(),
			getStyleLength(),
			style,
			this::setStyleLength
		);
		return this;
	}

	default String getTitle() {
		return StructureSupport.extractStringValue(
			this,
			getIDLength() + getClassNameLength() + getStyleLength(),
			getTitleLength()
		);
	}
	default CDIDName setTitle(String title) {
		StructureSupport.writeStringValue(
			this,
			getIDLength() + getClassNameLength() + getStyleLength(),
			getTitleLength(),
			title,
			this::setTitleLength
		);
		return this;
	}

	default String getHTMLAttributes() {
		return StructureSupport.extractStringValue(
			this,
			getIDLength() + getClassNameLength() + getStyleLength() + getTitleLength(),
			getHTMLAttributesLength()
		);
	}
	default CDIDName setHTMLAttributes(String attrs) {
		StructureSupport.writeStringValue(
			this,
			getIDLength() + getClassNameLength() + getStyleLength() + getTitleLength(),
			getHTMLAttributesLength(),
			attrs,
			this::setHTMLAttributesLength
		);
		return this;
	}

	default String getName() {
		return StructureSupport.extractStringValue(
			this,
			getIDLength() + getClassNameLength() + getStyleLength() + getTitleLength() + getHTMLAttributesLength(),
			getNameLength()
		);
	}
	default CDIDName setName(String name) {
		StructureSupport.writeStringValue(
			this,
			getIDLength() + getClassNameLength() + getStyleLength() + getTitleLength() + getHTMLAttributesLength(),
			getNameLength(),
			name,
			this::setNameLength
		);
		return this;
	}
}
