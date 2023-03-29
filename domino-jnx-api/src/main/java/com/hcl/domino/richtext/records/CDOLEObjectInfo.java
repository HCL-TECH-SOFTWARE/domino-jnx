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

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.OLE_GUID;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.44
 */
@StructureDefinition(
  name = "CDOLEOBJ_INFO",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "FileObjNameLength", type = short.class, unsigned = true),
    @StructureMember(name = "DescriptionNameLength", type = short.class, unsigned = true),
    @StructureMember(name = "FieldNameLength", type = short.class, unsigned = true),
    @StructureMember(name = "TextIndexObjNameLength", type = short.class, unsigned = true),
    @StructureMember(name = "OleObjClass", type = OLE_GUID.class),
    @StructureMember(name = "StorageFormat", type = CDOLEObjectInfo.StorageFormat.class),
    @StructureMember(name = "DisplayFormat", type = DDEFormat.class),
    @StructureMember(name = "Flags", type = CDOLEObjectInfo.Flag.class, bitfield = true),
    @StructureMember(name = "StorageFormatAppearedIn", type = short.class),
    @StructureMember(name = "HTMLDataLength", type = short.class, unsigned = true),
    @StructureMember(name = "AssociatedFILEsLength", type = short.class, unsigned = true),
    @StructureMember(name = "Reserved3", type = short.class),
    @StructureMember(name = "Reserved4", type = int.class),
  }
)
public interface CDOLEObjectInfo extends RichTextRecord<WSIG> {
  public enum StorageFormat implements INumberEnum<Short> {
    /** OLE "Docfile" structured storage format, RootIStorage/IStorage/IStream (Notes format) */
    STRUCT_STORAGE(NotesConstants.OLE_STG_FMT_STRUCT_STORAGE),
    /** OLE IStorage/IStream structured storage format */
    ISTORAGE_ISTREAM(NotesConstants.OLE_STG_FMT_ISTORAGE_ISTREAM),
    /** OLE RootIStorage/IStream structured storage format */
    STRUCT_STREAM(NotesConstants.OLE_STG_FMT_STRUCT_STREAM);
    
    private final short value;
    private StorageFormat(short value) {
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
  
  public enum Flag implements INumberEnum<Integer> {
    /** Object is scripted */
    SCRIPTED(NotesConstants.OBJINFO_FLAGS_SCRIPTED),
    /** Object is run in read-only mode */
    RUNREADONLY(NotesConstants.OBJINFO_FLAGS_RUNREADONLY),
    /** Object is a control */
    CONTROL(NotesConstants.OBJINFO_FLAGS_CONTROL),
    /** Object is sized to fit to window */
    FITTOWINDOW(NotesConstants.OBJINFO_FLAGS_FITTOWINDOW),
    /** Object is sized to fit below fields */
    FITBELOWFIELDS(NotesConstants.OBJINFO_FLAGS_FITBELOWFIELDS),
    /** Object is to be updated from document */
    UPDATEFROMDOCUMENT(NotesConstants.OBJINFO_FLAGS_UPDATEFROMDOCUMENT),
    /** Object is to be updated from document */
    INCLUDERICHTEXT(NotesConstants.OBJINFO_FLAGS_INCLUDERICHTEXT),
    /** Object is stored in IStorage/IStream format rather than RootIStorage/IStorage/IStream */
    ISTREAM(NotesConstants.OBJINFO_FLAGS_ISTORAGE_ISTREAM),
    /** Object has HTML data */
    HTMLDATA(NotesConstants.OBJINFO_FLAGS_HTMLDATA);
    
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
  
  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("FileObjNameLength")
  int getFileObjectNameLength();
  
  @StructureSetter("FileObjNameLength")
  CDOLEObjectInfo setFileObjectNameLength(int len);
  
  @StructureGetter("DescriptionNameLength")
  int getDescriptionLength();
  
  @StructureSetter("DescriptionNameLength")
  CDOLEObjectInfo setDescriptionLength(int len);
  
  @StructureGetter("FieldNameLength")
  int getFieldNameLength();
  
  @StructureSetter("FieldNameLength")
  CDOLEObjectInfo setFieldNameLength(int len);
  
  @StructureGetter("TextIndexObjNameLength")
  int getTextIndexObjectNameLength();
  
  @StructureSetter("TextIndexObjNameLength")
  CDOLEObjectInfo setTextIndexObjectNameLength(int len);
  
  @StructureGetter("OleObjClass")
  OLE_GUID getOleObjectClass();
  
  @StructureGetter("StorageFormat")
  Optional<StorageFormat> getStorageFormat();
  
  /**
   * Retrieves the storage format as a raw {@code short}.
   * 
   * @return the storage format as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("StorageFormat")
  short getStorageFormatRaw();
  
  @StructureSetter("StorageFormat")
  CDOLEObjectInfo setStorageFormat(StorageFormat format);
  
  /**
   * Sets the storage format as a raw {@code short}.
   * 
   * @param format the format to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("StorageFormat")
  CDOLEObjectInfo setStorageFormatRaw(short format);
  
  @StructureGetter("DisplayFormat")
  Optional<DDEFormat> getDisplayFormat();
  
  /**
   * Retrieves the display format as a raw {@code short}.
   * 
   * @return the display format as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("DisplayFormat")
  short getDisplayFormatRaw();
  
  @StructureSetter("DisplayFormat")
  CDOLEObjectInfo setDisplayFormat(DDEFormat format);
  
  /**
   * Sets the display format as a raw {@code short}.
   * 
   * @param format the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("DisplayFormat")
  CDOLEObjectInfo setDisplayFormatRaw(short format);
  
  @StructureGetter("Flags")
  Set<Flag> getFlags();
  
  @StructureSetter("Flags")
  CDOLEObjectInfo setFlags(Collection<Flag> flags);
  
  @StructureGetter("StorageFormatAppearedIn")
  short getStorageFormatAppearedIn();
  
  @StructureSetter("StorageFormatAppearedIn")
  CDOLEObjectInfo setStorageFormatAppearedIn(short appeared);
  
  @StructureGetter("HTMLDataLength")
  int getHtmlDataLength();
  
  @StructureSetter("HTMLDataLength")
  CDOLEObjectInfo setHtmlDataLength(int len);
  
  @StructureGetter("AssociatedFILEsLength")
  int getAssociatedFileLength();
  
  @StructureSetter("AssociatedFILEsLength")
  CDOLEObjectInfo setAssociatedFileLength(int len);
  
  default String getFileObjectName() {
    return StructureSupport.extractStringValue(
      this,
      0,
      getFileObjectNameLength()
    );
  }
  
  default CDOLEObjectInfo setFileObjectName(String name) {
    return StructureSupport.writeStringValue(
      this,
      0,
      getFileObjectNameLength(),
      name,
      this::setFileObjectNameLength
    );
  }
  
  default String getDescription() {
    return StructureSupport.extractStringValue(
      this,
      getFileObjectNameLength(),
      getDescriptionLength()
    );
  }
  
  default CDOLEObjectInfo setDescription(String name) {
    return StructureSupport.writeStringValue(
      this,
      getFileObjectNameLength(),
      getDescriptionLength(),
      name,
      this::setDescriptionLength
    );
  }
  
  default String getFieldName() {
    return StructureSupport.extractStringValue(
      this,
      getFileObjectNameLength() + getDescriptionLength(),
      getFieldNameLength()
    );
  }
  
  default CDOLEObjectInfo setFieldName(String name) {
    return StructureSupport.writeStringValue(
      this,
      getFileObjectNameLength() + getDescriptionLength(),
      getFieldNameLength(),
      name,
      this::setFieldNameLength
    );
  }
  
  default String getTextIndexObjectName() {
    return StructureSupport.extractStringValue(
      this,
      getFileObjectNameLength() + getDescriptionLength() + getFieldNameLength(),
      getTextIndexObjectNameLength()
    );
  }
  
  default CDOLEObjectInfo setTextIndexObjectName(String name) {
    return StructureSupport.writeStringValue(
      this,
      getFileObjectNameLength() + getDescriptionLength() + getFieldNameLength(),
      getTextIndexObjectNameLength(),
      name,
      this::setTextIndexObjectNameLength
    );
  }
  
  default String getHtmlData() {
    return StructureSupport.extractStringValue(
      this,
      getFileObjectNameLength() + getDescriptionLength() + getFieldNameLength() + getTextIndexObjectNameLength(),
      getHtmlDataLength()
    );
  }
  
  default CDOLEObjectInfo setHtmlData(String data) {
    return StructureSupport.writeStringValue(
      this,
      getFileObjectNameLength() + getDescriptionLength() + getFieldNameLength() + getTextIndexObjectNameLength(),
      getHtmlDataLength(),
      data,
      this::setHtmlDataLength
    );
  }
  
  default byte[] getAssociatedFileData() {
    return StructureSupport.extractByteArray(
      this,
      getFileObjectNameLength() + getDescriptionLength() + getFieldNameLength() + getTextIndexObjectNameLength() + getHtmlDataLength(),
      getAssociatedFileLength()
    );
  }
  
  default CDOLEObjectInfo setAssociatedFileData(byte[] data) {
    return StructureSupport.writeByteValue(
      this,
      getFileObjectNameLength() + getDescriptionLength() + getFieldNameLength() + getTextIndexObjectNameLength() + getHtmlDataLength(),
      getAssociatedFileLength(),
      data,
      this::setAssociatedFileLength
    );
  }
}
