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

import com.hcl.domino.design.action.EventId;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.15
 */
@StructureDefinition(name = "CDEVENT", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Flags", type = CDEvent.EventFlag.class, bitfield = true),
    @StructureMember(name = "EventType", type = EventId.class),
    @StructureMember(name = "ActionType", type = CDEvent.ActionType.class),
    @StructureMember(name = "ActionLength", type = int.class, unsigned = true),
    @StructureMember(name = "SignatureLength", type = short.class, unsigned = true),
    @StructureMember(name = "Reserved", type = byte[].class, length = 14)
})
public interface CDEvent extends RichTextRecord<WSIG> {
  enum ActionType implements INumberEnum<Short> {
    FORMULA((short) 1),
    CANNED_ACTION((short) 2),
    LOTUS_SCRIPT((short) 3),
    JAVASCRIPT((short) 4);

    private final short value;

    ActionType(final short value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Short getValue() {
      return this.value;
    }
  }

  enum EventFlag implements INumberEnum<Integer> {
    HAS_LIBRARIES(0x00000001);

    private final int value;

    EventFlag(final int value) {
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

  @StructureGetter("ActionLength")
  long getActionLength();

  @StructureGetter("ActionType")
  ActionType getActionType();

  @StructureGetter("EventType")
  EventId getEventType();

  @StructureGetter("Flags")
  Set<EventFlag> getFlags();

  @StructureGetter("Header")
  @Override
  WSIG getHeader();

  @StructureGetter("SignatureLength")
  int getSignatureLength();

  @StructureSetter("ActionLength")
  CDEvent setActionLength(long actionLength);

  @StructureSetter("ActionType")
  CDEvent setActionType(ActionType actionType);

  @StructureSetter("EventType")
  CDEvent setEventType(EventId eventType);

  @StructureSetter("Flags")
  CDEvent setFlags(Collection<EventFlag> flags);

  @StructureSetter("SignatureLength")
  CDEvent setSignatureLength(int signatureLength);
}
