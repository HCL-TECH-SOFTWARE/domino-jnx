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

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

/**
 * CDSTORAGELINK
 * 
 * @author 
 * @since 1.0.46
 */

@StructureDefinition(
  name = "CDSTORAGELINK", 
  members = { 
    @StructureMember(name = "Header", type = WSIG.class),                        
    @StructureMember(name = "StorageType", type = short.class, unsigned = true), /* Type of object (Object, Note, URL, etc.) */
    @StructureMember(name = "LoadType", type = short.class, unsigned = true),    /* How to load (deferred, on demand, etc.) */
    @StructureMember(name = "Flags", type = short.class, unsigned = true),       /* Currently not used */
    @StructureMember(name = "DataLength", type = short.class, unsigned = true),  /* Length of data following */
    @StructureMember(name = "Reserved", type = short[].class, unsigned = true, length = 6), /* Currently not used */
FIX ME >>> /* Storage data follows... */
})
public interface CDStorageLink extends RichTextRecord<WSIG> {
  static int STORAGE_LINK_TYPE_OBJECT = 1; 
  static int STORAGE_LINK_TYPE_NOTE = 2; 
  static int STORAGE_LINK_TYPE_URL_CONVERTED = 3; 
  static int STORAGE_LINK_TYPE_URL_MIME = 4; 
  static int STORAGE_LINK_TYPE_MIME_PART = 5; 
  static int STORAGE_LINK_TYPE_MIME_OBJECT = 6; 
  static int STORAGE_LINK_LOAD_DEFERRED = 1; 
  static int STORAGE_LINK_LOAD_ON_DEMAND = 2; 

  @StructureGetter("Header")
  WSIG getHeader();

  @StructureGetter("StorageType")
  int getStorageType();

  @StructureGetter("LoadType")
  int getLoadType();

  @StructureGetter("Flags")
  int getFlags();

  @StructureGetter("DataLength")
  int getDataLength();

  @StructureSetter("StorageType")
  CDStorageLink setStorageType(int storageType);

  @StructureSetter("LoadType")
  CDStorageLink setLoadType(int loadType);

  @StructureSetter("Flags")
  CDStorageLink setFlags(int flags);

  @StructureSetter("DataLength")
  CDStorageLink setDataLength(int dataLength);

}
