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

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * A CD record of this type specifies the start of a DDE link.
 *
 * @author Jesse Gallagher
 * @since 1.0.2
 */
@StructureDefinition(name = "CDDDEBegin", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "ServerName", type = char[].class, length = RichTextConstants.DDESERVERNAMEMAX),
    @StructureMember(name = "TopicName", type = char[].class, length = 100),
    @StructureMember(name = "ItemName", type = char[].class, length = RichTextConstants.DDEITEMNAMEMAX),
    @StructureMember(name = "Flags", type = CDDDEBegin.DDEFlag.class),
    @StructureMember(name = "PasteEmbedDocName", type = char[].class, length = 80),
    @StructureMember(name = "EmbeddedDocCount", type = short.class, unsigned = true),
    @StructureMember(name = "ClipFormat", type = DDEFormat.class)
})
public interface CDDDEBegin extends RichTextRecord<WSIG> {
  enum DDEFlag implements INumberEnum<Integer> {
    AUTOLINK(0x01),
    MANUALLINK(0x02),
    EMBEDDED(0x04),
    INITIATE(0x08),
    CDP(0x10),
    NOTES_LAUNCHED(0x20),
    CONV_ACTIVE(0x40),
    EMBEDEXTRACTED(0x80),
    NEWOBJECT(0x100);

    private final int value;

    DDEFlag(final int value) {
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
}
