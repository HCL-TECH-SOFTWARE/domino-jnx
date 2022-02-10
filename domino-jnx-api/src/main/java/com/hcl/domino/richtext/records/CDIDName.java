/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
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
 * @author Jesse Gallagher
 * @since 1.0.24
 */
@StructureDefinition(name = "CDIDNAME", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Length", type = short.class, unsigned = true),
    @StructureMember(name = "wClassLen", type = short.class, unsigned = true),
    @StructureMember(name = "wStyleLen", type = short.class, unsigned = true),
    @StructureMember(name = "wTitleLen", type = short.class, unsigned = true),
    @StructureMember(name = "wExtraLen", type = short.class, unsigned = true),
    @StructureMember(name = "wNameLen", type = short.class, unsigned = true),
    @StructureMember(name = "reserved", type = byte[].class, length = 10)
})
public interface CDIDName extends RichTextRecord<WSIG> {
  default String getClassName() {
    return StructureSupport.extractStringValue(
        this,
        this.getIDLength(),
        this.getClassNameLength());
  }

  @StructureGetter("wClassLen")
  int getClassNameLength();

  @StructureGetter("Header")
  @Override
  WSIG getHeader();

  default String getHTMLAttributes() {
    return StructureSupport.extractStringValue(
        this,
        this.getIDLength() + this.getClassNameLength() + this.getStyleLength() + this.getTitleLength(),
        this.getHTMLAttributesLength());
  }

  @StructureGetter("wExtraLen")
  int getHTMLAttributesLength();

  default String getID() {
    return StructureSupport.extractStringValue(
        this,
        0,
        this.getIDLength());
  }

  @StructureGetter("Length")
  int getIDLength();

  default String getName() {
    return StructureSupport.extractStringValue(
        this,
        this.getIDLength() + this.getClassNameLength() + this.getStyleLength() + this.getTitleLength()
            + this.getHTMLAttributesLength(),
        this.getNameLength());
  }

  @StructureGetter("wNameLen")
  int getNameLength();

  default String getStyle() {
    return StructureSupport.extractStringValue(
        this,
        this.getIDLength() + this.getClassNameLength(),
        this.getStyleLength());
  }

  @StructureGetter("wStyleLen")
  int getStyleLength();

  default String getTitle() {
    return StructureSupport.extractStringValue(
        this,
        this.getIDLength() + this.getClassNameLength() + this.getStyleLength(),
        this.getTitleLength());
  }

  @StructureGetter("wTitleLen")
  int getTitleLength();

  default CDIDName setClassName(final String className) {
    StructureSupport.writeStringValue(
        this,
        this.getIDLength(),
        this.getClassNameLength(),
        className,
        this::setClassNameLength);
    return this;
  }

  @StructureSetter("wClassLen")
  CDIDName setClassNameLength(int length);

  default CDIDName setHTMLAttributes(final String attrs) {
    StructureSupport.writeStringValue(
        this,
        this.getIDLength() + this.getClassNameLength() + this.getStyleLength() + this.getTitleLength(),
        this.getHTMLAttributesLength(),
        attrs,
        this::setHTMLAttributesLength);
    return this;
  }

  @StructureSetter("wExtraLen")
  CDIDName setHTMLAttributesLength(int length);

  default CDIDName setID(final String id) {
    StructureSupport.writeStringValue(
        this,
        0,
        this.getIDLength(),
        id,
        this::setIDLength);
    return this;
  }

  @StructureSetter("Length")
  CDIDName setIDLength(int length);

  default CDIDName setName(final String name) {
    StructureSupport.writeStringValue(
        this,
        this.getIDLength() + this.getClassNameLength() + this.getStyleLength() + this.getTitleLength()
            + this.getHTMLAttributesLength(),
        this.getNameLength(),
        name,
        this::setNameLength);
    return this;
  }

  @StructureSetter("wNameLen")
  CDIDName setNameLength(int length);

  default CDIDName setStyle(final String style) {
    StructureSupport.writeStringValue(
        this,
        this.getIDLength() + this.getClassNameLength(),
        this.getStyleLength(),
        style,
        this::setStyleLength);
    return this;
  }

  @StructureSetter("wStyleLen")
  CDIDName setStyleLength(int length);

  default CDIDName setTitle(final String title) {
    StructureSupport.writeStringValue(
        this,
        this.getIDLength() + this.getClassNameLength() + this.getStyleLength(),
        this.getTitleLength(),
        title,
        this::setTitleLength);
    return this;
  }

  @StructureSetter("wTitleLen")
  CDIDName setTitleLength(int length);
}
