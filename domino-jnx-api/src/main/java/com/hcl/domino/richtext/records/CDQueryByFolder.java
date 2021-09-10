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

import java.util.Set;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.38
 */
@StructureDefinition(
  name = "CDQUERYBYFOLDER",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "dwFlags", type = CDQueryByFolder.Flag.class, bitfield = true),
    @StructureMember(name = "wFolderNameLen", type = short.class, unsigned = true)
  }
)
public interface CDQueryByFolder extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Integer> {
    /** Folder is private */
    PRIVATE(NotesConstants.QUERYBYFOLDER_FLAG_PRIVATE);

    private final int value;

    Flag(final int value) {
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
  
  @StructureGetter("dwFlags")
  Set<Flag> getFlags();
  
  @StructureGetter("wFolderNameLen")
  int getFolderNameLength();
  
  @StructureSetter("wFolderNameLen")
  CDQueryByFolder setFolderNameLength(int len);
  
  default String getFolderName() {
    return StructureSupport.extractStringValue(
      this,
      0,
      getFolderNameLength()
    );
  }
  
  default CDQueryByFolder setFolderName(String fieldName) {
    return StructureSupport.writeStringValue(
      this,
      0,
      getFolderNameLength(),
      fieldName,
      this::setFolderNameLength
    );
  }
}
