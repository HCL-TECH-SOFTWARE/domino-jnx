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
package com.hcl.domino.richtext.structures;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

/**
 * Represents the {@code ACTIVEOBJECT} structure.
 * 
 * @author Jesse Gallagher
 * @since 1.0.44
 */
@StructureDefinition(
  name = "ACTIVEOBJECT",
  members = {
    @StructureMember(name = "Version", type = ActiveObject.Version.class),
    @StructureMember(name = "ObjectType", type = ActiveObject.Type.class),
    @StructureMember(name = "Flags", type = ActiveObject.Flag.class, bitfield = true),
    @StructureMember(name = "WidthUnitType", type = ActiveObject.Unit.class),
    @StructureMember(name = "HeightUnitType", type = ActiveObject.Unit.class),
    @StructureMember(name = "Width", type = int.class, unsigned = true),
    @StructureMember(name = "Height", type = int.class, unsigned = true),
    @StructureMember(name = "SpaceUnitType", type = short.class),
    @StructureMember(name = "HSpace", type = short.class),
    @StructureMember(name = "VSpace", type = short.class),
    @StructureMember(name = "DocURLNameLength", type = short.class, unsigned = true),
    @StructureMember(name = "CodebaseLength", type = short.class, unsigned = true),
    @StructureMember(name = "CodeLength", type = short.class, unsigned = true),
    @StructureMember(name = "ObjectNameLength", type = short.class, unsigned = true),
    @StructureMember(name = "StorageLinkCount", type = short.class, unsigned = true),
    @StructureMember(name = "ParamCount", type = short.class, unsigned = true),
    @StructureMember(name = "Used", type = byte[].class, length = 16)
  }
)
public interface ActiveObject extends ResizableMemoryStructure {
  public enum Version implements INumberEnum<Short> {
    VERSION1(NotesConstants.ACTIVEOBJECT_VERSION1);
    
    private final short value;
    private Version(short value) {
      this.value = value; 
    }
    
    @Override
    public long getLongValue() {
      return value;
    }
    
    @Override
    public Short getValue() {
      return value;
    }
  }

  public enum Type implements INumberEnum<Short> {
    JAVA(NotesConstants.ACTIVEOBJECT_TYPE_JAVA),
    PLUGIN(NotesConstants.ACTIVEOBJECT_TYPE_PLUGIN),
    OBJECT(NotesConstants.ACTIVEOBJECT_TYPE_OBJECT);
    
    private final short value;
    private Type(short value) {
      this.value = value; 
    }
    
    @Override
    public long getLongValue() {
      return value;
    }
    
    @Override
    public Short getValue() {
      return value;
    }
  }

  public enum Unit implements INumberEnum<Byte> {
    PIXELS(NotesConstants.ACTIVEOBJECT_UNIT_PIXELS),
    HIMETRICS(NotesConstants.ACTIVEOBJECT_UNIT_HIMETRICS),
    INCHES(NotesConstants.ACTIVEOBJECT_UNIT_INCHES),
    PERCENT(NotesConstants.ACTIVEOBJECT_UNIT_PERCENT);
    
    private final byte value;
    private Unit(byte value) {
      this.value = value; 
    }
    
    @Override
    public long getLongValue() {
      return value;
    }
    
    @Override
    public Byte getValue() {
      return value;
    }
  }

  public enum Flag implements INumberEnum<Integer> {
    MAYSCRIPT(NotesConstants.ACTIVEOBJECT_FLAG_MAYSCRIPT),
    /** Active object is a Java applet that uses CORBA */
    CORBA_APPLET(NotesConstants.ACTIVEOBJECT_FLAG_CORBA_APPLET),
    /** This is a CORBA applet that uses SSL */
    CORBA_SSL(NotesConstants.ACTIVEOBJECT_FLAG_CORBA_SSL),
    /** This object comes from a mime mail message*/
    MAIL_PLUGIN(NotesConstants.ACTIVEOBJECT_FLAG_MAIL_PLUGIN),
    /** don't automatically download the jar stuff for applets */
    NOCORBADOWNLOAD(NotesConstants.ACTIVEOBJECT_FLAG_NOCORBADOWNLOAD),
    /** Reserved part of ACTIVEOBJECT struct contains applet files digested for signature verification */
    DIGESTAPPLETFILES(NotesConstants.ACTIVEOBJECT_FLAG_DIGESTAPPLETFILES);
    
    private final int value;
    private Flag(int value) {
      this.value = value; 
    }
    
    @Override
    public long getLongValue() {
      return value;
    }
    
    @Override
    public Integer getValue() {
      return value;
    }
  }
  
  @StructureGetter("Version")
  Optional<Version> getVersion();
  
  /**
   * Retrieves the version as a raw {@code short}.
   * 
   * @return the version as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("Version")
  short getVersionRaw();
  
  @StructureSetter("Version")
  ActiveObject setVersion(Version version);
  
  /**
   * Sets the version as a raw {@code short}.
   * 
   * @param version the version as a {@code short}
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("Version")
  ActiveObject setVersionRaw(short version);
  
  @StructureGetter("ObjectType")
  Optional<Type> getObjectType();
  
  /**
   * Retrieves the object type as a raw {@code short}.
   * 
   * @return the object type as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("ObjectType")
  short getObjectTypeRaw();
  
  @StructureSetter("ObjectType")
  ActiveObject setObjectType(Type type);
  
  /**
   * Sets the object type as a raw {@code short}.
   * 
   * @param type the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("ObjectType")
  ActiveObject setObjectTypeRaw(short type);
  
  @StructureGetter("Flags")
  Set<Flag> getFlags();
  
  @StructureSetter("Flags")
  ActiveObject setFlags(Collection<Flag> flags);
  
  @StructureGetter("WidthUnitType")
  Optional<Unit> getWidthUnitType();
  
  /**
   * Retrieves the width unit as a raw {@code byte}.
   * 
   * @return the width unit as a {@code byte}
   * @since 1.24.0
   */
  @StructureGetter("WidthUnitType")
  byte getWidthUnitTypeRaw();
  
  @StructureSetter("WidthUnitType")
  ActiveObject setWidthUnitType(Unit unit);
  
  /**
   * Sets the width unit as a raw {@code byte}.
   * 
   * @param unit the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("WidthUnitType")
  ActiveObject setWidthUnitTypeRaw(byte unit);
  
  @StructureGetter("HeightUnitType")
  Optional<Unit> getHeightUnitType();
  
  /**
   * Retrieves the height unit as a raw {@code byte}.
   * 
   * @return the height unity as a {@code byte}
   * @since 1.24.0
   */
  @StructureGetter("HeightUnitType")
  byte getHeightUnitTypeRaw();
  
  @StructureSetter("HeightUnitType")
  ActiveObject setHeightUnitType(Unit unit);
  
  /**
   * Sets the height unit as a raw {@code byte}.
   * 
   * @param unit the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("HeightUnitType")
  ActiveObject setHeightUnitTypeRaw(byte unit);
  
  @StructureGetter("Width")
  long getWidth();
  
  @StructureSetter("Width")
  ActiveObject setWidth(long width);
  
  @StructureGetter("Height")
  long getHeight();
  
  @StructureSetter("Height")
  ActiveObject setHeight(long height);
  
  @StructureGetter("DocURLNameLength")
  int getDocUrlNameLength();
  
  @StructureSetter("DocURLNameLength")
  ActiveObject setDocUrlNameLength(int len);
  
  @StructureGetter("CodeLength")
  int getCodeLength();
  
  @StructureSetter("CodeLength")
  ActiveObject setCodeLength(int len);
  
  @StructureGetter("CodebaseLength")
  int getCodebaseLength();
  
  @StructureSetter("CodebaseLength")
  ActiveObject setCodebaseLength(int len);
  
  @StructureGetter("ObjectNameLength")
  int getObjectNameLength();
  
  @StructureSetter("ObjectNameLength")
  ActiveObject setObjectNameLength(int len);
  
  @StructureGetter("StorageLinkCount")
  int getStorageLinkCount();
  
  @StructureSetter("StorageLinkCount")
  ActiveObject setStorageLinkCount(int count);
  
  @StructureGetter("ParamCount")
  int getParamCount();
  
  @StructureSetter("ParamCount")
  ActiveObject setParamCount(int count);
  
  @StructureGetter("Used")
  byte[] getDataBytes();
  
  @StructureSetter("Used")
  ActiveObject setDataBytes(byte[] bytes);
  
  default String getDocUrlName() {
    return StructureSupport.extractStringValue(
      this,
      0,
      getDocUrlNameLength()
    );
  }
  
  default ActiveObject setDocUrlName(String url) {
    return StructureSupport.writeStringValue(
      this,
      0,
      getDocUrlNameLength(),
      url,
      this::setDocUrlNameLength
    );
  }
  
  default String getCodebase() {
    return StructureSupport.extractStringValue(
      this,
      getDocUrlNameLength(),
      getCodebaseLength()
    );
  }
  
  default ActiveObject setCodebase(String codebase) {
    return StructureSupport.writeStringValue(
      this,
      getDocUrlNameLength(),
      getCodebaseLength(),
      codebase,
      this::setCodebaseLength
    );
  }
  
  default String getCode() {
    return StructureSupport.extractStringValue(
      this,
      getDocUrlNameLength() + getCodebaseLength(),
      getCodeLength()
    );
  }
  
  default ActiveObject setCode(String code) {
    return StructureSupport.writeStringValue(
      this,
      getDocUrlNameLength() + getCodebaseLength(),
      getCodeLength(),
      code,
      this::setCodeLength
    );
  }
  
  default String getObjectName() {
    return StructureSupport.extractStringValue(
      this,
      getDocUrlNameLength() + getCodebaseLength() + getCodeLength(),
      getObjectNameLength()
    );
  }
  
  default ActiveObject setObjectName(String objectName) {
    return StructureSupport.writeStringValue(
      this,
      getDocUrlNameLength() + getCodebaseLength() + getCodeLength(),
      getObjectNameLength(),
      objectName,
      this::setObjectNameLength
    );
  }
  
  default List<ActiveObjectParam> getParams() {
    int size = MemoryStructureWrapperService.get().sizeOf(ActiveObjectParam.class);
    return StructureSupport.extractResizableStructures(
      this,
      getDocUrlNameLength() + getCodebaseLength() + getCodeLength() + getObjectNameLength(),
      ActiveObjectParam.class,
      getParamCount(),
      p -> size + p.getLength() + p.getFormulaLength()
    );
  }
  
  default List<ActiveObjectStorageLink> getStorageLinks() {
    MemoryStructureWrapperService svc = MemoryStructureWrapperService.get();
    int size = svc.sizeOf(ActiveObjectStorageLink.class);
    int paramSize = svc.sizeOf(ActiveObjectParam.class);
    int paramLen = getParams().stream()
      .mapToInt(param -> paramSize + param.getLength() + param.getFormulaLength())
      .sum();
    return StructureSupport.extractResizableStructures(
      this,
      getDocUrlNameLength() + getCodebaseLength() + getCodeLength() + getObjectNameLength() + paramLen,
      ActiveObjectStorageLink.class,
      getStorageLinkCount(),
      p -> size + p.getLength()
    );
  }
}
