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
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.design.agent.AgentInterval;
import com.hcl.domino.design.agent.AgentTarget;
import com.hcl.domino.design.agent.AgentTrigger;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
@StructureDefinition(name = "ODS_ASSISTSTRUCT", members = {
    @StructureMember(name = "wVersion", type = short.class, unsigned = true),
    @StructureMember(name = "wTriggerType", type = AgentTrigger.class),
    @StructureMember(name = "wSearchType", type = AgentTarget.class),
    @StructureMember(name = "wIntervalType", type = AgentInterval.class),
    @StructureMember(name = "wInterval", type = short.class, unsigned = true),
    @StructureMember(name = "dwTime1", type = int.class),
    @StructureMember(name = "dwTime2", type = int.class),
    @StructureMember(name = "StartTime", type = OpaqueTimeDate.class),
    @StructureMember(name = "EndTime", type = OpaqueTimeDate.class),
    @StructureMember(name = "dwFlags", type = AssistStruct.Flag.class, bitfield = true),
    @StructureMember(name = "dwSpare", type = int[].class, length = 16)
})
public interface AssistStruct extends ResizableMemoryStructure {
  enum Flag implements INumberEnum<Integer> {
    /** TRUE if manual assistant is hidden */
    HIDDEN(RichTextConstants.ASSISTODS_FLAG_HIDDEN),
    /** Do not run on weekends */
    NOWEEKENDS(RichTextConstants.ASSISTODS_FLAG_NOWEEKENDS),
    /** TRUE if storing highlights */
    STOREHIGHLIGHTS(RichTextConstants.ASSISTODS_FLAG_STOREHIGHLIGHTS),
    /** TRUE if this is the V3-style mail and paste macro */
    MAILANDPASTE(RichTextConstants.ASSISTODS_FLAG_MAILANDPASTE),
    /** TRUE if server to run on should be chosed when enabled */
    CHOOSEWHENENABLED(RichTextConstants.ASSISTODS_FLAG_CHOOSEWHENENABLED);

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

  @StructureGetter("EndTime")
  DominoDateTime getEndDate();

  @StructureGetter("dwFlags")
  Set<Flag> getFlags();

  @StructureGetter("wInterval")
  int getInterval();

  @StructureGetter("wIntervalType")
  Optional<AgentInterval> getIntervalType();

  /**
   * Retrieves the interval type as a raw {@code short}.
   * 
   * @return the interval type as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("wIntervalType")
  short getIntervalTypeRaw();

  @StructureGetter("wSearchType")
  Optional<AgentTarget> getSearch();

  /**
   * Retrieves the search type as a raw {@code short}.
   * 
   * @return the search type as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("wSearchType")
  short getSearchRaw();

  @StructureGetter("StartTime")
  DominoDateTime getStartDate();

  @StructureGetter("dwTime1")
  int getTime1();

  @StructureGetter("dwTime2")
  int getTime2();

  @StructureGetter("wTriggerType")
  Optional<AgentTrigger> getTrigger();

  /**
   * Retrieves the trigger type as a raw {@code short}.
   * 
   * @return the trigger type as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("wTriggerType")
  short getTriggerRaw();

  @StructureGetter("wVersion")
  int getVersion();

  @StructureSetter("EndTime")
  AssistStruct setEndDate(DominoDateTime endDate);

  @StructureSetter("dwFlags")
  AssistStruct setFlags(Collection<Flag> flags);

  @StructureSetter("wInterval")
  AssistStruct setInterval(int interval);

  @StructureSetter("wIntervalType")
  AssistStruct setIntervalType(AgentInterval interval);

  /**
   * Sets the interval type as a raw {@code short}.
   * 
   * @param interval the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("wIntervalType")
  AssistStruct setIntervalTypeRaw(short interval);

  @StructureSetter("wSearchType")
  AssistStruct setSearch(AgentTarget search);

  /**
   * Sets the search type as a raw {@code short}.
   * 
   * @param search the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("wSearchType")
  AssistStruct setSearchRaw(short search);

  @StructureSetter("StartTime")
  AssistStruct setStartDate(DominoDateTime startDate);

  @StructureSetter("dwTime1")
  AssistStruct setTime1(int time1);

  @StructureSetter("dwTime2")
  AssistStruct setTime2(int time2);

  @StructureSetter("wTriggerType")
  AssistStruct setTrigger(AgentTrigger trigger);

  /**
   * Sets the trigger type as a raw {@code short}.
   * 
   * @param trigger the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("wTriggerType")
  AssistStruct setTriggerRaw(short trigger);

  @StructureSetter("wVersion")
  AssistStruct setVersion(int version);
}
