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
@StructureDefinition(name = "CDACTIONREPLY", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "dwFlags", type = CDActionReply.Flag.class, bitfield = true),
    @StructureMember(name = "wBodyLen", type = short.class, unsigned = true)
})
public interface CDActionReply extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Integer> {
    /** Reply to all (otherwise, just to sender) */
    REPLYTOALL(RichTextConstants.ACTIONREPLY_FLAG_REPLYTOALL),
    /** Include copy of document */
    INCLUDEDOC(RichTextConstants.ACTIONREPLY_FLAG_INCLUDEDOC),
    /** Save copy */
    SAVEMAIL(RichTextConstants.ACTIONREPLY_FLAG_SAVEMAIL),
    /** Do not reply to agent-generated mail */
    NOAGENTREPLY(RichTextConstants.ACTIONREPLY_FLAG_NOAGENTREPLY),
    /** Only reply once per sender */
    REPLYONCE(RichTextConstants.ACTIONREPLY_FLAG_REPLYONCE),
    ;

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

  default String getBody() {
    return StructureSupport.extractStringValue(this, 0, this.getBodyLength());
  }

  @StructureGetter("wBodyLen")
  int getBodyLength();

  @StructureGetter("dwFlags")
  Set<Flag> getFlags();

  @StructureGetter("Header")
  @Override
  WSIG getHeader();

  default CDActionReply setBody(final String body) {
    return StructureSupport.writeStringValue(this, 0, this.getBodyLength(), body, this::setBodyLength);
  }

  @StructureSetter("wBodyLen")
  CDActionReply setBodyLength(int len);

  @StructureSetter("dwFlags")
  CDActionReply setFlags(Collection<Flag> flags);
}
