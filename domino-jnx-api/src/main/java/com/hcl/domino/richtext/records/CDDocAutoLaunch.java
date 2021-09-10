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

import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.forms.AutoLaunchHideWhen;
import com.hcl.domino.design.forms.AutoLaunchType;
import com.hcl.domino.design.forms.AutoLaunchWhen;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.OLE_GUID;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.34
 */
@StructureDefinition(
  name = "CDDOCAUTOLAUNCH",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "ObjectType", type = AutoLaunchType.class),
    @StructureMember(name = "HideWhenFlags", type = AutoLaunchHideWhen.class, bitfield = true),
    @StructureMember(name = "LaunchWhenFlags", type = AutoLaunchWhen.class, bitfield = true),
    @StructureMember(name = "OleFlags", type = CDDocAutoLaunch.OleFlag.class, bitfield = true),
    // NB: this will likely only ever be one value, but is marked bitfield=true because the values
    //     are designed to be multi-value flags
    @StructureMember(name = "CopyToFieldFlags", type = CDDocAutoLaunch.CopyToFieldFlag.class, bitfield = true),
    @StructureMember(name = "Spare1", type = int.class),
    @StructureMember(name = "Spare2", type = int.class),
    @StructureMember(name = "FieldNameLength", type = short.class, unsigned = true),
    @StructureMember(name = "OleObjClass", type = OLE_GUID.class),
  }
)
public interface CDDocAutoLaunch extends RichTextRecord<WSIG> {
  enum OleFlag implements INumberEnum<Integer> {
    EDIT_INPLACE(DesignConstants.OLE_EDIT_INPLACE),
    MODAL_WINDOW(DesignConstants.OLE_MODAL_WINDOW),
    ADV_OPTIONS(DesignConstants.OLE_ADV_OPTIONS);

    private final int value;

    OleFlag(final int value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Integer getValue() {
      return this.value;
    }
  }
  enum CopyToFieldFlag implements INumberEnum<Integer> {
    NONE(DesignConstants.FIELD_COPY_NONE),
    COPY_FIRST(DesignConstants.FIELD_COPY_FIRST),
    COPY_NAMED(DesignConstants.FIELD_COPY_NAMED);

    private final int value;

    CopyToFieldFlag(final int value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Integer getValue() {
      return this.value;
    }
  }

  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("ObjectType")
  AutoLaunchType getObjectType();
  
  @StructureSetter("ObjectType")
  CDDocAutoLaunch setObjectType(AutoLaunchType type);
  
  @StructureGetter("HideWhenFlags")
  Set<AutoLaunchHideWhen> getHideWhenFlags();
  
  @StructureSetter("HideWhenFlags")
  CDDocAutoLaunch setHideWhenFlags(Collection<AutoLaunchHideWhen> flags);
  
  @StructureGetter("LaunchWhenFlags")
  Set<AutoLaunchWhen> getLaunchWhenFlags();
  
  @StructureSetter("LaunchWhenFlags")
  CDDocAutoLaunch setLaunchWhenFlags(Collection<AutoLaunchWhen> flags);
  
  @StructureGetter("OleFlags")
  Set<OleFlag> getOleFlags();
  
  @StructureSetter("OleFlags")
  CDDocAutoLaunch setOleFlags(Collection<OleFlag> flags);
  
  @StructureGetter("CopyToFieldFlags")
  Set<CopyToFieldFlag> getCopyToFieldFlags();
  
  @StructureSetter("CopyToFieldFlags")
  CDDocAutoLaunch setCopyToFieldFlags(Collection<CopyToFieldFlag> flags);
  
  @StructureGetter("FieldNameLength")
  int getFieldNameLength();
  
  @StructureSetter("FieldNameLength")
  CDDocAutoLaunch setFieldNameLength(int len);
  
  @StructureGetter("OleObjClass")
  OLE_GUID getOleObjClass();
  
  default String getFieldName() {
    return StructureSupport.extractStringValue(
      this,
      0,
      getFieldNameLength()
    );
  }
  
  default CDDocAutoLaunch setFieldName(String fieldName) {
    return StructureSupport.writeStringValue(
      this,
      0,
      getFieldNameLength(),
      fieldName,
      this::setFieldNameLength
    );
  }
}
