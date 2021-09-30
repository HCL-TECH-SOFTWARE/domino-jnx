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
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import com.hcl.domino.commons.design.agent.DefaultFormulaAgentContent;
import com.hcl.domino.commons.design.agent.DefaultImportedJavaAgentContent;
import com.hcl.domino.commons.design.agent.DefaultJavaAgentContent;
import com.hcl.domino.commons.design.agent.DefaultSimpleActionAgentContent;
import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.commons.util.DumpUtil;
import com.hcl.domino.commons.util.InnardsConverter;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.Item;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.design.DesignAgent;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.agent.AgentContent;
import com.hcl.domino.design.agent.AgentInterval;
import com.hcl.domino.design.agent.AgentTarget;
import com.hcl.domino.design.agent.AgentTrigger;
import com.hcl.domino.design.agent.FormulaAgentContent;
import com.hcl.domino.design.agent.LotusScriptAgentContent;
import com.hcl.domino.design.simpleaction.SimpleAction;
import com.hcl.domino.design.simplesearch.SimpleSearchTerm;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.Pair;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.RichTextRecordList;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.records.CDActionFormula;
import com.hcl.domino.richtext.records.CDActionHeader;
import com.hcl.domino.richtext.records.CDActionJavaAgent;
import com.hcl.domino.richtext.records.CDActionLotusScript;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RecordType.Area;
import com.hcl.domino.richtext.structures.AssistStruct;

/**
 * @since 1.0.18
 */
public class AgentImpl extends AbstractDesignElement<DesignAgent> implements DesignAgent, IDefaultReadersRestrictedElement,
  IDefaultNamedDesignElement {

  public AgentImpl(final Document doc) {
    super(doc);
  }

  @Override
  public AgentContent getAgentContent() {
    switch (this.getAgentLanguage()) {
      case FORMULA: {
        Document doc = this.getDocument();
        if(doc.hasItem(NotesConstants.ASSIST_ACTION_ITEM)) {
          // Find the first CDACTIONFORMULA and read the contents
          return this.getDocument().getRichTextItem(NotesConstants.ASSIST_ACTION_ITEM, Area.TYPE_ACTION)
              .stream()
              .filter(CDActionFormula.class::isInstance)
              .map(CDActionFormula.class::cast)
              .findFirst()
              .map(action -> {
                FormulaAgentContent.DocumentAction docAction;
                final Set<CDActionFormula.Flag> flags = action.getFlags();
                if (flags.contains(CDActionFormula.Flag.NEWCOPY)) {
                  docAction = FormulaAgentContent.DocumentAction.CREATE;
                } else if (flags.contains(CDActionFormula.Flag.SELECTDOCS)) {
                  docAction = FormulaAgentContent.DocumentAction.SELECT;
                } else {
                  docAction = FormulaAgentContent.DocumentAction.MODIFY;
                }
                final String formula = action.getAction();
                return new DefaultFormulaAgentContent(docAction, formula);
              })
              .orElseThrow(() -> new IllegalStateException("Unable to find formula action data"));
        } else {
          // Ancient agents use "$Formula" and related fields
          String formula = doc.get(NotesConstants.FILTER_FORMULA_ITEM, String.class, ""); //$NON-NLS-1$
          
          int action = doc.get(NotesConstants.FILTER_OPERATION_ITEM, int.class, 0);
          FormulaAgentContent.DocumentAction docAction;
          switch(action) {
            case NotesConstants.FILTER_OP_SELECT:
              docAction = FormulaAgentContent.DocumentAction.SELECT;
              break;
            case NotesConstants.FILTER_OP_NEW_COPY:
              docAction = FormulaAgentContent.DocumentAction.CREATE;
              break;
            case NotesConstants.FILTER_OP_UPDATE:
            default:
              docAction = FormulaAgentContent.DocumentAction.MODIFY;
              break;
          }
          
          return new DefaultFormulaAgentContent(docAction, formula);
        }
      }
      case LS: {
        // Could be represented two ways: either as a CDACTIONLOTUSSCRIPT or as multiple
        // $AgentHScript text items
        final CDActionLotusScript action = this.getDocument().getRichTextItem(NotesConstants.ASSIST_ACTION_ITEM, Area.TYPE_ACTION)
            .stream()
            .filter(CDActionLotusScript.class::isInstance)
            .map(CDActionLotusScript.class::cast)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Unable to find LotusScript action data"));
        if (action.getScriptLength() == 0) {
          // This must be stored in $AgentHScript items
          return (LotusScriptAgentContent) () -> this.getDocument().allItems()
              .filter(item -> NotesConstants.AGENT_HSCRIPT_ITEM.equalsIgnoreCase(item.getName()))
              .map(item -> item.getValue().get(0))
              .map(String::valueOf)
              .collect(Collectors.joining());
        } else {
          return (LotusScriptAgentContent) () -> action.getScript();
        }
      }
      case SIMPLE_ACTION: {
        RichTextRecordList records = this.getDocument().getRichTextItem(NotesConstants.ASSIST_ACTION_ITEM, Area.TYPE_ACTION);
        final List<SimpleAction> actions = DesignUtil.toSimpleActions(records);
        return new DefaultSimpleActionAgentContent(actions);
      }
      case JAVA: {
        final CDActionJavaAgent action = this.getDocument().getRichTextItem(NotesConstants.ASSIST_ACTION_ITEM, Area.TYPE_ACTION)
            .stream()
            .filter(CDActionJavaAgent.class::isInstance)
            .map(CDActionJavaAgent.class::cast)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Unable to find Java action data"));
        return new DefaultJavaAgentContent(
            action.getClassName(),
            action.getCodePath(),
            Arrays.stream(action.getFileList().split("\\n")) //$NON-NLS-1$
                .filter(StringUtil::isNotEmpty)
                .collect(Collectors.toList()),
            Arrays.stream(action.getLibraryList().split("\\n")) //$NON-NLS-1$
                .filter(StringUtil::isNotEmpty)
                .collect(Collectors.toList()));
      }
      case IMPORTED_JAVA: {
        // Similar to above, but the file list is a flat collection of imported files
        // and the library list isn't used
        final CDActionJavaAgent action = this.getDocument().getRichTextItem(NotesConstants.ASSIST_ACTION_ITEM, Area.TYPE_ACTION)
            .stream()
            .filter(CDActionJavaAgent.class::isInstance)
            .map(CDActionJavaAgent.class::cast)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Unable to find Java action data"));
        return new DefaultImportedJavaAgentContent(
            action.getClassName(),
            action.getCodePath(),
            Arrays.stream(action.getFileList().split("\\n")) //$NON-NLS-1$
                .filter(StringUtil::isNotEmpty)
                .collect(Collectors.toList()));
      }
      default:
        throw new UnsupportedOperationException(MessageFormat.format("Unsupported agent type {0}", this.getAgentLanguage()));
    }
  }

  @Override
  public AgentLanguage getAgentLanguage() {
    short lang = this.getDocument().get(NotesConstants.ASSIST_TYPE_ITEM, short.class, (short) 0);
    if (lang == RichTextConstants.SIG_ACTION_FORMULAONLY) {
      lang = RichTextConstants.SIG_ACTION_FORMULA;
    } else if (lang == 0) {
      // Seen in ancient data
      lang = RichTextConstants.SIG_ACTION_FORMULA;
    }
    switch (lang) {
      case RichTextConstants.SIG_ACTION_JAVAAGENT:
        // Imported Java agents are distinguished by a $Flags value
        if (this.getFlags().contains(NotesConstants.DESIGN_FLAG_JAVA_AGENT_WITH_SOURCE)) {
          return AgentLanguage.JAVA;
        } else {
          return AgentLanguage.IMPORTED_JAVA;
        }
      case RichTextConstants.SIG_ACTION_LOTUSSCRIPT:
        return AgentLanguage.LS;
      case RichTextConstants.SIG_ACTION_FORMULAONLY:
        return AgentLanguage.FORMULA;
      case RichTextConstants.SIG_ACTION_FORMULA: {
        // This gets weird. Both FORMULA and FORMULAONLY have been observed to represent
        // formula agents... but that's not all. If a Simple Action agent _starts_ with a
        // @Function Formula action, then the $AssistType field is written as
        // SIG_ACTION_FORMULA. Thus, we need to open the agent to check.
        // Currently, the best check is to see if it has more than just the two opening
        // records that it shared with Formula agents
        final RichTextRecordList list = this.getDocument().getRichTextItem(NotesConstants.ASSIST_ACTION_ITEM, Area.TYPE_ACTION);
        if (list.size() > 2) {
          return AgentLanguage.SIMPLE_ACTION;
        }
        return AgentLanguage.FORMULA;
      }
      case RichTextConstants.SIG_ACTION_MODIFYFIELD:
      case RichTextConstants.SIG_ACTION_REPLY:
      case RichTextConstants.SIG_ACTION_SENDMAIL:
      case RichTextConstants.SIG_ACTION_DBCOPY:
      case RichTextConstants.SIG_ACTION_DELETE:
      case RichTextConstants.SIG_ACTION_BYFORM:
      case RichTextConstants.SIG_ACTION_MARKREAD:
      case RichTextConstants.SIG_ACTION_MARKUNREAD:
      case RichTextConstants.SIG_ACTION_MOVETOFOLDER:
      case RichTextConstants.SIG_ACTION_COPYTOFOLDER:
      case RichTextConstants.SIG_ACTION_REMOVEFROMFOLDER:
      case RichTextConstants.SIG_ACTION_NEWSLETTER:
      case RichTextConstants.SIG_ACTION_RUNAGENT:
      case RichTextConstants.SIG_ACTION_SENDDOCUMENT:
      case -1: // Set when there are no actions
        return AgentLanguage.SIMPLE_ACTION;
      default:
        throw new IllegalStateException(MessageFormat.format("Unknown language value {0} of agent {1} (UNID: {2})", Short.toUnsignedInt(lang), getTitle(), getDocument().getUNID()));
    }
  }
  
  @Override
  public DesignAgent setAgentContent(AgentLanguage lang, String content) {
    if (lang != AgentLanguage.LS) {
      throw new IllegalArgumentException(MessageFormat.format("Setting agent content for {0} is currently unsupported", lang));
    }
    initializeAgentLanguage(lang);
    
    setAgentContentLS(content);
    
    return this;
  }

  private DesignAgent setAgentContentLS(String content) {
    this.setFlag(NotesConstants.DESIGN_FLAG_HIDE_FROM_V3, true);
    this.setFlag(NotesConstants.DESIGN_FLAG_V4AGENT, true);
    this.setFlag(NotesConstants.DESIGN_FLAG_LOTUSSCRIPT_AGENT, true);

    this.setFlag(NotesConstants.DESIGN_FLAG_JAVA_AGENT_WITH_SOURCE, false);
    this.setFlag(NotesConstants.DESIGN_FLAG_JAVA_AGENT, false);

    //format LS code to be Designer compatible
    NativeDesignSupport designSupport = NativeDesignSupport.get();
    Pair<String,String> formattedCodeAndErrors = designSupport.formatLSForDesigner(content, ""); //$NON-NLS-1$
    String formattedCode = formattedCodeAndErrors.getValue1();

    Document doc = getDocument();
    Charset lmbcsCharset = Charset.forName("LMBCS"); //$NON-NLS-1$

    //remove old items with source and compiled code
    doc.removeItem(NotesConstants.ASSIST_ACTION_ITEM);
    doc.removeItem(NotesConstants.AGENT_HSCRIPT_ITEM);
    doc.removeItem(NotesConstants.ASSIST_EXACTION_ITEM);

    //split code into LMBCS 
    List<ByteBuffer> chunks = designSupport.splitAsLMBCS(formattedCode, true, false, 61310); // 61310 -> retrieved by inspecting db design
    if (chunks.size() == 1) {
      //code fits into a single item $AssistAction stored as CDACTIONLOTUSSCRIPT record
      ByteBuffer chunk = chunks.get(0);
      byte[] data = new byte[chunk.limit()];
      chunk.get(data);
      String chunkStr = new String(data, lmbcsCharset);

      try (RichTextWriter rtWriter = doc.createRichTextItem(NotesConstants.ASSIST_ACTION_ITEM);) {
        rtWriter.addRichTextRecord(CDActionHeader.class, (record) -> {
          record.getHeader().setSignature((byte) (RecordType.ACTION_HEADER.getConstant() & 0xff));
          record.getHeader().setLength((short) (MemoryStructureUtil.sizeOf(CDActionHeader.class) & 0xffff));
        });

        rtWriter.addRichTextRecord(CDActionLotusScript.class, (record) -> {
          record.getHeader().setSignature(RecordType.ACTION_LOTUSSCRIPT.getConstant());
          record.getHeader().setLength(MemoryStructureUtil.sizeOf(CDActionLotusScript.class));
          record.setScript(chunkStr);
        });
      }
    }
    else {
      //split up code across multiple $AgentHScript items
      chunks.forEach((chunk) -> {
        byte[] data = new byte[chunk.limit()];
        chunk.get(data);
        String chunkStr = new String(data, lmbcsCharset);

        doc.appendItemValue(NotesConstants.AGENT_HSCRIPT_ITEM, EnumSet.of(ItemFlag.SIGNED, ItemFlag.KEEPLINEBREAKS), chunkStr);
      });

      try (RichTextWriter rtWriter = doc.createRichTextItem(NotesConstants.ASSIST_ACTION_ITEM);) {
        rtWriter.addRichTextRecord(CDActionHeader.class, (record) -> {
          record.getHeader().setSignature((byte) (RecordType.ACTION_HEADER.getConstant() & 0xff));
          record.getHeader().setLength((short) (MemoryStructureUtil.sizeOf(CDActionHeader.class) & 0xffff));
        });

        rtWriter.addRichTextRecord(CDActionLotusScript.class, (record) -> {
          record.getHeader().setSignature(RecordType.ACTION_LOTUSSCRIPT.getConstant());
          record.getHeader().setLength(MemoryStructureUtil.sizeOf(CDActionLotusScript.class));
          record.setScriptLength(0);
        });
      }
    }

    //switch action items from TYPE_COMPOSITE to TYPE_ACTION
    doc.forEachItem(NotesConstants.ASSIST_ACTION_ITEM, (item,loop) -> {
      item.setSigned(true);
      designSupport.setCDRecordItemType(doc, item, ItemDataType.TYPE_ACTION);
    });

    //compile and sign
    doc.compileLotusScript();
    doc.sign();

    return this;
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
    switch (this.getIntervalType()) {
      case DAYS:
      case MINUTES:
      case MONTH:
      case WEEK:
        return this.getAssistInfo()
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
        .orElse(AgentInterval.NONE);
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
        String typeVal = getDocument().get(NotesConstants.FILTER_TYPE_ITEM, String.class, "");
        if(typeVal.isEmpty()) {
          return AgentTrigger.NONE;
        }
        switch(Integer.parseInt(typeVal)) {
        case NotesConstants.FILTER_TYPE_MENU:
          return AgentTrigger.MANUAL;
        case NotesConstants.FILTER_TYPE_MAIL:
          return AgentTrigger.NEWMAIL;
        default:
          return AgentTrigger.NONE;
        }
      });
  }

  @Override
  public DesignAgent initializeAgentLanguage(final AgentLanguage lang) {
    int val;
    switch (Objects.requireNonNull(lang)) {
      case LS:
        val = RichTextConstants.SIG_ACTION_LOTUSSCRIPT;
        break;
      case JAVA:
      case IMPORTED_JAVA:
        val = RichTextConstants.SIG_ACTION_JAVA;
        break;
      case FORMULA:
        val = RichTextConstants.SIG_ACTION_FORMULAONLY;
        break;
      default:
        val = -1;
    }
    this.getDocument().replaceItemValue(NotesConstants.ASSIST_TYPE_ITEM, val);
    return this;
  }

  @Override
  public void initializeNewDesignNote() {
//    this.setFlags(NotesConstants.DESIGN_FLAG_JAVA_AGENT_WITH_SOURCE + NotesConstants.DESIGN_FLAG_HIDE_FROM_V3); //$NON-NLS-1$
    this.setFlags(NotesConstants.DESIGN_FLAG_HIDE_FROM_V3); //$NON-NLS-1$
    
    Document doc = getDocument();
    doc.replaceItemValue(NotesConstants.DESIGNER_VERSION, "8.5.3"); //$NON-NLS-1$
    
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
      default:
    }
    return this;
  }

  @Override
  public boolean isStoreHighlights() {
    return getAssistInfo()
      .map(AssistStruct::getFlags)
      .map(flags -> flags.contains(AssistStruct.Flag.STOREHIGHLIGHTS))
      .orElse(null);
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
          // TODO determine whether MAIL actually translates to this
          return AgentTarget.NEW;
        case NotesConstants.FILTER_SCAN_ALL:
        default:
          return AgentTarget.ALL;
        }
      });
  }

  // *******************************************************************************
  // * Implementation utility methods
  // *******************************************************************************

  private String getAssistFlags() {
    return getDocument().get(DesignConstants.ASSIST_FLAGS_ITEM, String.class, ""); //$NON-NLS-1$
  }

  private Optional<AssistStruct> getAssistInfo() {
    final Document doc = this.getDocument();
    if (doc.hasItem(NotesConstants.ASSIST_INFO_ITEM)) {
      final AssistStruct assistInfo = doc.get(NotesConstants.ASSIST_INFO_ITEM, AssistStruct.class, null);
      return Optional.ofNullable(assistInfo);
    }
    return Optional.empty();
  }
}
