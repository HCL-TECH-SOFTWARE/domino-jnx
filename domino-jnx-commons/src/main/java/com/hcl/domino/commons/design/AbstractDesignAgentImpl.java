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
package com.hcl.domino.commons.design;

import java.nio.ByteBuffer;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import com.hcl.domino.commons.data.DefaultDominoDateTime;
import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.commons.util.InnardsConverter;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.design.DesignAgent;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.NativeDesignSupport;
import com.hcl.domino.design.agent.AgentInterval;
import com.hcl.domino.design.agent.AgentTarget;
import com.hcl.domino.design.agent.AgentTrigger;
import com.hcl.domino.design.simplesearch.SimpleSearchTerm;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.records.CDQueryHeader;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.structures.AssistStruct;

/**
 * @since 1.0.18
 */
public abstract class AbstractDesignAgentImpl<T extends DesignAgent> extends AbstractDesignElement<T> implements DesignAgent, IDefaultReadersRestrictedElement,
  IDefaultNamedDesignElement {

  public AbstractDesignAgentImpl(final Document doc) {
    super(doc);
  }

  @Override
  public DominoDateTime getAgentVersion() {
    Document doc = getDocument();
    return doc
      .getOptional(NotesConstants.ASSIST_VERSION_ITEM, DominoDateTime.class)
      .orElseGet(doc::getLastModified);
  }

  @Override
  public Optional<DominoDateTime> getEndDate() {
    if (this.getTrigger() != AgentTrigger.SCHEDULED) {
      return Optional.empty();
    }
    return this.getAssistInfo()
        .map(AssistStruct::getEndDate)
        .map(dt -> dt.isValid() ? dt : null);
  }

  @Override
  public OptionalInt getInterval() {
    // Ancient data is interpreted very differently
    Optional<AssistStruct> assistInfo = getAssistInfo();
    if(!assistInfo.isPresent()) {
      String periodVal = getDocument().get(NotesConstants.FILTER_PERIOD_ITEM, String.class, ""); //$NON-NLS-1$
      if(StringUtil.isEmpty(periodVal)) {
        return OptionalInt.empty();
      }
      switch(Integer.parseInt(periodVal)) {
      case NotesConstants.PERIOD_HOURLY:
        return OptionalInt.of(60);
      case NotesConstants.PERIOD_DAILY:
        return OptionalInt.of(1);
      case NotesConstants.PERIOD_WEEKLY:
        return OptionalInt.of(1);
      case NotesConstants.PERIOD_DISABLED:
      default:
        return OptionalInt.empty();
      }
    }
    
    switch (this.getIntervalType()) {
      case DAYS:
      case MINUTES:
      case MONTH:
      case WEEK:
        return assistInfo
            .map(info -> OptionalInt.of(info.getInterval()))
            .orElse(OptionalInt.empty());
      case NONE:
      case EVENT:
      default:
        return OptionalInt.empty();
    }
  }

  @Override
  public AgentInterval getIntervalType() {
    return this.getAssistInfo()
      .map(AssistStruct::getIntervalType)
      .orElseGet(() -> {
        // Check for agent data
        if(getTrigger() == AgentTrigger.SCHEDULED) {
          String periodVal = getDocument().get(NotesConstants.FILTER_PERIOD_ITEM, String.class, ""); //$NON-NLS-1$
          if(StringUtil.isEmpty(periodVal)) {
            return AgentInterval.NONE;
          }
          switch(Integer.parseInt(periodVal)) {
          case NotesConstants.PERIOD_HOURLY:
            return AgentInterval.MINUTES;
          case NotesConstants.PERIOD_DAILY:
            return AgentInterval.DAYS;
          case NotesConstants.PERIOD_WEEKLY:
            return AgentInterval.WEEK;
          case NotesConstants.PERIOD_DISABLED:
          default:
            return AgentInterval.NONE;
          }
        } else {
          return AgentInterval.NONE;
        }
      });
  }
  
  @Override
  public Optional<LastRunInfo> getLastRunInfo() {
    return getDocument().getOptional(NotesConstants.ASSIST_RUNINFO_ITEM, LastRunInfo.class);
  }

  @Override
  public OptionalInt getRunDayOfMonth() {
    switch (this.getIntervalType()) {
      case MONTH:
        return this.getAssistInfo()
            .map(AssistStruct::getTime2)
            .map(OptionalInt::of)
            .orElse(OptionalInt.empty());
      case MINUTES:
      case DAYS:
      case WEEK:
      case EVENT:
      case NONE:
      default:
        return OptionalInt.empty();
    }
  }

  @Override
  public Optional<DayOfWeek> getRunDayOfWeek() {
    switch (this.getIntervalType()) {
      case WEEK:
        return this.getAssistInfo()
            .map(AssistStruct::getTime2)
            // Domino's week starts with Saturday=0, while we want 1-based with Monday=1
            .map(val -> DayOfWeek.of((val + 12) % 7 + 1));
      case MINUTES:
      case DAYS:
      case MONTH:
      case EVENT:
      case NONE:
      default:
        return Optional.empty();
    }
  }

  @Override
  public Optional<LocalTime> getRunEndLocalTime() {
    switch (this.getIntervalType()) {
      case MINUTES:
        return this.getAssistInfo()
            .map(AssistStruct::getTime2)
            .map(Integer::toUnsignedLong)
            .flatMap(val -> { 
              // The value 8640000 represents the end of the day, which is what Designer stores for "all day"
              return val == 0 || val == 8640000 ? Optional.empty() : Optional.of(InnardsConverter.ticksToLocalTime(val));
            });
      case DAYS:
      case WEEK:
      case MONTH:
      case EVENT:
      case NONE:
      default:
        return Optional.empty();
    }
  }

  @Override
  public Optional<LocalTime> getRunLocalTime() {
    switch (this.getIntervalType()) {
      case MINUTES:
      case DAYS:
      case WEEK:
      case MONTH:
        return this.getAssistInfo()
            .map(AssistStruct::getTime1)
            .map(Integer::toUnsignedLong)
            .map(InnardsConverter::ticksToLocalTime);
      case EVENT:
      case NONE:
      default:
        return Optional.empty();
    }
  }

  @Override
  public String getRunLocation() {
    return this.getDocument().get(NotesConstants.FILTER_MACHINE_NAME, String.class, ""); //$NON-NLS-1$
  }

  @Override
  public Optional<DominoDateTime> getStartDate() {
    if (this.getTrigger() != AgentTrigger.SCHEDULED) {
      return Optional.empty();
    }
    return this.getAssistInfo()
        .map(AssistStruct::getStartDate)
        .map(dt -> dt.isValid() ? dt : null);
  }

  @Override
  public AgentTrigger getTrigger() {
    return this.getAssistInfo()
      .map(AssistStruct::getTrigger)
      .orElseGet(() -> {
        // Check for ancient data
        String typeVal = getDocument().get(NotesConstants.FILTER_TYPE_ITEM, String.class, "0"); //$NON-NLS-1$
        if(typeVal.isEmpty()) {
          return AgentTrigger.NONE;
        }
        switch(Integer.parseInt(typeVal)) {
        case NotesConstants.FILTER_TYPE_MENU:
          return AgentTrigger.MANUAL;
        case NotesConstants.FILTER_TYPE_MAIL:
          return AgentTrigger.NEWMAIL;
        case NotesConstants.FILTER_TYPE_BACKGROUND:
          return AgentTrigger.SCHEDULED;
        default:
          return AgentTrigger.NONE;
        }
      });
  }

  @Override
  public void initializeNewDesignNote() {
    Document doc = getDocument();
    
    setFlag(NotesConstants.DESIGN_FLAG_HIDE_FROM_V3, true);
    setFlag(NotesConstants.DESIGN_FLAG_V4AGENT, true);
    setFlagsExt(""); //$NON-NLS-1$
    
    doc.replaceItemValue(NotesConstants.ASSIST_DOCCOUNT_ITEM, EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), 0);
    
    AssistStruct assistStruct = createAssistInfoWithDefaults();
    setAssistInfo(assistStruct);
    
    DefaultDominoDateTime wildcardTD = new DefaultDominoDateTime(new int[] {0, 0});
    doc.replaceItemValue(NotesConstants.ASSIST_LASTRUN_ITEM, EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), wildcardTD);

    //write simple search query header
    try (RichTextWriter rtWriter = doc.createRichTextItem(NotesConstants.ASSIST_QUERY_ITEM)) {
      rtWriter.addRichTextRecord(CDQueryHeader.class, (record) -> {
        record
        .getHeader()
        .setSignature((byte) (RichTextConstants.SIG_QUERY_HEADER & 0xff))
        .setLength((short) (MemoryStructureUtil.sizeOf(CDQueryHeader.class) & 0xffff));
      });
    }
    NativeDesignSupport designSupport = NativeDesignSupport.get();
    
    //switch search query item from TYPE_COMPOSITE to TYPE_QUERY
    doc.forEachItem(NotesConstants.ASSIST_QUERY_ITEM, (item,loop) -> {
      item.setSigned(true);
      designSupport.setCDRecordItemType(doc, item, ItemDataType.TYPE_QUERY);
    });

    designSupport.initAgentRunInfo(doc);

    doc.replaceItemValue(DesignConstants.ASSIST_TRIGGER_ITEM, Integer.toString(RichTextConstants.ASSISTTRIGGER_TYPE_MANUAL));
    doc.replaceItemValue(NotesConstants.ASSIST_VERSION_ITEM, new DefaultDominoDateTime());
    
    doc.replaceItemValue(NotesConstants.DESIGNER_VERSION, "8.5.3"); //$NON-NLS-1$
    
    setSecurityLevel(SecurityLevel.RESTRICTED);
    setEnabled(true);
    setRunAsWebUser(false);
    
    doc.replaceItemValue("$Comment", EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), ""); //$NON-NLS-1$ //$NON-NLS-2$
  }

  @Override
  public boolean isRunOnWeekends() {
    return this.getAssistInfo()
        .map(info -> !info.getFlags().contains(AssistStruct.Flag.NOWEEKENDS))
        .orElse(true);
  }

  @Override
  public boolean isRunAsWebUser() {
    return getAssistFlags().contains(DesignConstants.ASSIST_FLAG_AGENT_RUNASWEBUSER);
  }

  @Override
  public DesignAgent setRunAsWebUser(boolean b) {
    if (b) {
     setAssistFlag(DesignConstants.ASSIST_FLAG_AGENT_RUNASWEBUSER, true);
     setAssistFlag(DesignConstants.ASSIST_FLAG_AGENT_RUNASSIGNER, false);
    }
    else {
      setAssistFlag(DesignConstants.ASSIST_FLAG_AGENT_RUNASWEBUSER, false);
      setAssistFlag(DesignConstants.ASSIST_FLAG_AGENT_RUNASSIGNER, true);
    }
    return this;
  }

  @Override
  public Optional<String> getOnBehalfOfUser() {
    return getDocument().getOptional(NotesConstants.ASSIST_ONBEHALFOF, String.class);
  }

  @Override
  public SecurityLevel getSecurityLevel() {
    int restricted = getDocument().get(DesignConstants.ASSIST_RESTRICTED, int.class, 1);
    switch(restricted) {
    case DesignConstants.ASSIST_RESTRICTED_FULLADMIN:
      return SecurityLevel.UNRESTRICTED_FULLADMIN;
    case DesignConstants.ASSIST_RESTRICTED_UNRESTRICTED:
      return SecurityLevel.UNRESTRICTED;
    case DesignConstants.ASSIST_RESTRICTED_RESTRICTED:
    default:
      return SecurityLevel.RESTRICTED;
    }
  }

  @Override
  public DesignAgent setSecurityLevel(SecurityLevel level) {
    Document doc = getDocument();
    switch (level) {
    case UNRESTRICTED_FULLADMIN:
      doc.replaceItemValue(DesignConstants.ASSIST_RESTRICTED, EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), DesignConstants.ASSIST_RESTRICTED_FULLADMIN);
      break;
    case UNRESTRICTED:
      doc.replaceItemValue(DesignConstants.ASSIST_RESTRICTED, EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), DesignConstants.ASSIST_RESTRICTED_UNRESTRICTED);
      break;
    case RESTRICTED:
      doc.replaceItemValue(DesignConstants.ASSIST_RESTRICTED, EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), DesignConstants.ASSIST_RESTRICTED_RESTRICTED);
      break;
    default:
    }
    return this;
  }

  @Override
  public boolean isStoreHighlights() {
    return getAssistInfo()
      .map(AssistStruct::getFlags)
      .map(flags -> flags.contains(AssistStruct.Flag.STOREHIGHLIGHTS))
      .orElse(false);
  }

  @Override
  public boolean isStoreSearch() {
    return getFlags().contains(NotesConstants.DESIGN_FLAG_AGENT_SHOWINSEARCH);
  }

  @Override
  public boolean isProfilingEnabled() {
    return getFlagsExt().contains(DesignConstants.DESIGN_FLAGEXT_PROFILE);
  }

  @Override
  public boolean isAllowRemoteDebugging() {
    return getAssistFlags().contains(DesignConstants.ASSIST_FLAG_ALLOW_REMOTE_DEBUGGING);
  }

  @Override
  public boolean isPrivate() {
    return getAssistFlags().contains(DesignConstants.ASSIST_FLAG_PRIVATE);
  }

  @Override
  public boolean isRunInBackgroundInClient() {
    return getAssistFlags().contains(DesignConstants.ASSIST_FLAG_THREAD);
  }

  @Override
  public boolean isEnabled() {
    return getAssistFlags().contains(DesignConstants.ASSIST_FLAG_ENABLED);
  }
  
  @Override
  public DesignAgent setEnabled(boolean b) {
    setAssistFlag(DesignConstants.ASSIST_FLAG_ENABLED, b);
    return this;
  }
  
  @Override
  public List<? extends SimpleSearchTerm> getDocumentSelection() {
    return DesignUtil.toSimpleSearch(getDocument().getRichTextItem(NotesConstants.ASSIST_QUERY_ITEM, RecordType.Area.TYPE_QUERY));
  }

  @Override
  public AgentTarget getTarget() {
    return getAssistInfo()
      .map(AssistStruct::getSearch)
      .orElseGet(() -> {
        // Could be ancient data
        String scanVal = getDocument().get(NotesConstants.FILTER_SCAN_ITEM, String.class, ""); //$NON-NLS-1$
        if(scanVal.isEmpty()) {
          return AgentTarget.ALL;
        }
        switch(Integer.parseInt(scanVal)) {
        case NotesConstants.FILTER_SCAN_UNREAD:
          return AgentTarget.UNREAD;
        case NotesConstants.FILTER_SCAN_VIEW:
          return AgentTarget.VIEW;
        case NotesConstants.FILTER_SCAN_SELECTED:
          return AgentTarget.SELECTED;
        case NotesConstants.FILTER_SCAN_NEW:
        case NotesConstants.FILTER_SCAN_MAIL:
          return AgentTarget.NEW;
        case NotesConstants.FILTER_SCAN_ALL:
        default:
          return AgentTarget.ALL;
        }
      });
  }

  @Override
  public DesignAgent setTarget(AgentTarget target) {
    AssistStruct assistInfo = getAssistInfo().orElseGet(this::createAssistInfoWithDefaults);
    assistInfo.setSearch(target);
    setAssistInfo(assistInfo);
    return this;
  }
  
  // *******************************************************************************
  // * Implementation utility methods
  // *******************************************************************************

  private String getAssistFlags() {
    return getDocument().get(DesignConstants.ASSIST_FLAGS_ITEM, String.class, ""); //$NON-NLS-1$
  }

  private void setAssistFlags(String flags) {
   getDocument().replaceItemValue(DesignConstants.ASSIST_FLAGS_ITEM, EnumSet.of(ItemFlag.SIGNED, ItemFlag.SUMMARY), flags);
  }

  private void setAssistFlag(final String flagConstant, final boolean value) {
    final String flags = this.getAssistFlags();
    if (value && !flags.contains(flagConstant)) {
      this.setAssistFlags(flags + flagConstant);
    } else if (!value && flags.contains(flagConstant)) {
      this.setAssistFlags(flags.replace(flagConstant, "")); //$NON-NLS-1$
    }
  }
  
  private AssistStruct createAssistInfoWithDefaults() {
    AssistStruct assistStruct = MemoryStructureUtil.newStructure(AssistStruct.class, 0);
    assistStruct.setVersion(1);
    assistStruct.setTrigger(AgentTrigger.MANUAL);
    assistStruct.setSearch(AgentTarget.SELECTED);
    return assistStruct;
  }
  
  private Optional<AssistStruct> getAssistInfo() {
    final Document doc = this.getDocument();
    if (doc.hasItem(NotesConstants.ASSIST_INFO_ITEM)) {
      final AssistStruct assistInfo = doc.get(NotesConstants.ASSIST_INFO_ITEM, AssistStruct.class, null);
      return Optional.ofNullable(assistInfo);
    }
    return Optional.empty();
  }
  
  private void setAssistInfo(AssistStruct info) {
    ByteBuffer infoData = info.getData();
    ByteBuffer infoDataWithType = ByteBuffer.allocate(2 + infoData.limit());
    infoDataWithType.putShort(ItemDataType.TYPE_ASSISTANT_INFO.getValue());
    infoDataWithType.put(infoData);
    infoDataWithType.position(0);
    getDocument().replaceItemValue(NotesConstants.ASSIST_INFO_ITEM, EnumSet.of(ItemFlag.SIGNED), infoDataWithType);
  }
  
}
