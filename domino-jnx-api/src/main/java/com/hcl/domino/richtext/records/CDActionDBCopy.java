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
 * @author Jesse Gallagher
 * @since 1.0.24
 */
@StructureDefinition(name = "CDACTIONDBCOPY", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "dwFlags", type = CDActionDBCopy.Flag.class, bitfield = true),
    @StructureMember(name = "wServerLen", type = short.class, unsigned = true),
    @StructureMember(name = "wDatabaseLen", type = short.class, unsigned = true)
})
public interface CDActionDBCopy extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Integer> {
    /** Remove document from original database */
    MOVE(RichTextConstants.ACTIONDBCOPY_FLAG_MOVE);

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

  default String getDatabaseName() {
    return StructureSupport.extractStringValue(this, this.getServerNameLength(), this.getDatabaseNameLength());
  }

  @StructureGetter("wDatabaseLen")
  int getDatabaseNameLength();

  @StructureGetter("dwFlags")
  Set<Flag> getFlags();

  @StructureGetter("Header")
  @Override
  WSIG getHeader();

  default String getServerName() {
    return StructureSupport.extractStringValue(this, 0, this.getServerNameLength());
  }

  @StructureGetter("wServerLen")
  int getServerNameLength();

  default CDActionDBCopy setDatabaseName(final String databaseName) {
    StructureSupport.writeStringValue(this, this.getServerNameLength(), this.getDatabaseNameLength(), databaseName,
        this::setDatabaseNameLength);
    return this;
  }

  @StructureSetter("wDatabaseLen")
  CDActionDBCopy setDatabaseNameLength(int len);

  @StructureSetter("dwFlags")
  CDActionDBCopy setFlags(Collection<Flag> flags);

  default CDActionDBCopy setServerName(final String serverName) {
    StructureSupport.writeStringValue(this, 0, this.getServerNameLength(), serverName, this::setServerNameLength);
    return this;
  }

  @StructureSetter("wServerLen")
  CDActionDBCopy setServerNameLength(int len);
}
