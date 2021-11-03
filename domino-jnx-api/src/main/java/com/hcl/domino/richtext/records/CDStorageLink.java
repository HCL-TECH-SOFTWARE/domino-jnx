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

import java.util.Optional;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * CDSTORAGELINK
 * 
 * @author pbugga
 * @since 1.0.46
 */

@StructureDefinition(
  name = "CDSTORAGELINK", 
  members = { 
    @StructureMember(name = "Header", type = WSIG.class),                        
    @StructureMember(name = "StorageType", type = short.class, unsigned = true),  			/* Type of object (Object, Note, URL, etc.) */
    @StructureMember(name = "LoadType", type = short.class, unsigned = true),    				/* How to load (deferred, on demand, etc.) */
    @StructureMember(name = "Flags", type = short.class, unsigned = true),       					/* Currently not used */
    @StructureMember(name = "DataLength", type = short.class, unsigned = true),  			/* Length of data following */
    @StructureMember(name = "Reserved", type = short[].class, unsigned = true, length = 6), 			/* Currently not used */
                                                                                            /* Storage data follows... */
})
public interface CDStorageLink extends RichTextRecord<WSIG> {
  public enum StorageType implements INumberEnum<Short> {
    OBJECT((short)RichTextConstants.STORAGE_LINK_TYPE_OBJECT), 
    NOTE((short)RichTextConstants.STORAGE_LINK_TYPE_NOTE), 
    URL_CONVERTED((short)RichTextConstants.STORAGE_LINK_TYPE_URL_CONVERTED), 
    URL_MIME((short)RichTextConstants.STORAGE_LINK_TYPE_URL_MIME), 
    MIME_PART((short)RichTextConstants.STORAGE_LINK_TYPE_MIME_PART), 
    MIME_OBJECT((short)RichTextConstants.STORAGE_LINK_TYPE_MIME_OBJECT);
    
    private final short value;
    private StorageType(short value) {
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
  
  public enum LoadType implements INumberEnum<Short> {
    LOAD_DEFERRED((short)RichTextConstants.STORAGE_LINK_LOAD_DEFERRED), 
    LOAD_ON_DEMAND((short)RichTextConstants.STORAGE_LINK_LOAD_ON_DEMAND);
    
    private final short value;
    private LoadType(short value) {
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

  @Override
  @StructureGetter("Header")
  WSIG getHeader();

  @StructureGetter("StorageType")
  int getStorageTypeRaw();
  
  @StructureGetter("StorageType")
  StorageType getStorageType();

  @StructureGetter("LoadType")
  int getLoadTypeRaw();
  
  @StructureGetter("LoadType")
  LoadType getLoadType();

  @StructureGetter("Flags")
  int getFlags();

  @StructureGetter("DataLength")
  int getDataLength();

  @StructureSetter("StorageType")
  CDStorageLink setStorageTypeRaw(int storageType);
  
  @StructureSetter("StorageType")
  CDStorageLink setStorageType(StorageType storageType);

  @StructureSetter("LoadType")
  CDStorageLink setLoadTypeRaw(int loadType);
  
  @StructureSetter("LoadType")
  CDStorageLink setLoadType(LoadType loadType);

  @StructureSetter("Flags")
  CDStorageLink setFlags(int flags);

  @StructureSetter("DataLength")
  CDStorageLink setDataLength(int dataLength);

  default Optional<String> getUrl() {
    Optional<String> retVal = Optional.empty();
    if (this.getStorageType() == StorageType.URL_CONVERTED ||
        this.getStorageType() == StorageType.URL_MIME) {
      retVal = Optional.of(StructureSupport.extractStringValue(
          this,
          0,
          this.getDataLength()
        ));
    }
    
    return retVal;
  }
  
  default Optional<String> getObject() {
    Optional<String> retVal = Optional.empty();
    if (this.getStorageType() == StorageType.OBJECT) {
      retVal = Optional.of(StructureSupport.extractStringValue(
          this,
          0,
          this.getDataLength()
        ));
    }
    
    return retVal;
  }
  
  default Optional<byte[]> getNote() {
    Optional<byte[]> retVal = Optional.empty();
    if (this.getStorageType() == StorageType.NOTE) {
      retVal = Optional.of(StructureSupport.extractByteArray(
          this,
          0,
          this.getDataLength()
        ));
    }
    
    return retVal;
  }
  
  default Optional<String> getMime() {
    Optional<String> retVal = Optional.empty();
    if (this.getStorageType() == StorageType.MIME_OBJECT ||
        this.getStorageType() == StorageType.MIME_PART) {
      retVal = Optional.of(StructureSupport.extractStringValue(
          this,
          0,
          this.getDataLength()
        ));
    }
    
    return retVal;
  }
  
  default CDStorageLink setUrl(final String url)  {
    return StructureSupport.writeStringValue(this, 
        0, 
        getDataLength(), 
        url, 
        this::setDataLength);
  }
  
  default CDStorageLink setObject(final String object)  {
    return StructureSupport.writeStringValue(this, 
        0, 
        getDataLength(), 
        object, 
        this::setDataLength);
  }
  
  default CDStorageLink setNote(final byte[] note)  {
    return StructureSupport.writeByteValue(this, 
        0, 
        getDataLength(), 
        note, 
        this::setDataLength);
  }
  
  default CDStorageLink setMime(final String mime)  {
    return StructureSupport.writeStringValue(this, 
        0, 
        getDataLength(), 
        mime, 
        this::setDataLength);
  }
}
