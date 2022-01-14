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
 * @author Jesse Gallagher
 * @since 1.0.24
 */
@StructureDefinition(name = "CDACTIONFOLDER", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "dwFlags", type = CDActionFolder.Flag.class, bitfield = true),
    @StructureMember(name = "wFolderNameLen", type = short.class, unsigned = true),
    @StructureMember(name = "wSpare", type = short.class)
})
public interface CDActionFolder extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Integer> {
    /** Create new folder */
    NEWFOLDER(RichTextConstants.ACTIONFOLDER_FLAG_NEWFOLDER),
    /** Folder is private */
    PRIVATEFOLDER(RichTextConstants.ACTIONFOLDER_FLAG_PRIVATEFOLDER);

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

  @StructureGetter("dwFlags")
  Set<Flag> getFlags();

  default String getFolderName() {
    return StructureSupport.extractStringValue(this, 0, this.getFolderNameLength());
  }

  @StructureGetter("wFolderNameLen")
  int getFolderNameLength();

  @StructureGetter("Header")
  @Override
  WSIG getHeader();

  @StructureSetter("dwFlags")
  CDActionFolder setFlags(Collection<Flag> flags);

  default CDActionFolder setFolderName(final String folderName) {
    return StructureSupport.writeStringValue(this, 0, this.getFolderNameLength(), folderName, this::setFolderNameLength);
  }

  @StructureSetter("wFolderNameLen")
  CDActionFolder setFolderNameLength(int len);
}
