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
package com.hcl.domino.commons.design.action;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import com.hcl.domino.commons.design.DesignUtil;
import com.hcl.domino.commons.richtext.RichTextUtil;
import com.hcl.domino.design.action.ActionBarAction;
import com.hcl.domino.design.action.ActionContent;
import com.hcl.domino.design.action.FormulaActionContent;
import com.hcl.domino.design.action.JavaScriptActionContent;
import com.hcl.domino.design.action.LotusScriptActionContent;
import com.hcl.domino.design.action.ScriptEvent;
import com.hcl.domino.design.action.SimpleActionActionContent;
import com.hcl.domino.design.action.SystemActionContent;
import com.hcl.domino.design.format.ActionBarControlType;
import com.hcl.domino.design.format.HideFromDevice;
import com.hcl.domino.design.simpleaction.SimpleAction;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.richtext.records.CDAction;
import com.hcl.domino.richtext.records.CDActionExt;
import com.hcl.domino.richtext.records.CDEventEntry;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.records.CDTarget;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * Default implementation of {@link ActionBarAction}s that works based on
 * a {@link List} of composite-data records representing the action's settings.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public class DefaultActionBarAction implements ActionBarAction {
  private final List<RichTextRecord<?>> records;

  public DefaultActionBarAction(List<RichTextRecord<?>> records) {
    this.records = records;
  }

  @Override
  public String getName() {
    return getActionRecord().getTitle();
  }
  
  @Override
  public ActionLanguage getActionLanguage() {
    switch(getActionRecord().getActionType()) {
      case RUN_FORMULA:
        return ActionLanguage.FORMULA;
      case RUN_JAVASCRIPT:
        // Check to see if the JS is common between client and web
        return records.stream()
          .filter(CDEventEntry.class::isInstance)
          .map(CDEventEntry.class::cast)
          .filter(entry -> entry.getActionType() == CDEventEntry.ActionType.JAVASCRIPT_COMMON)
          .findFirst()
          .map(entry -> ActionLanguage.COMMON_JAVASCRIPT)
          .orElse(ActionLanguage.JAVASCRIPT);
      case RUN_SCRIPT:
        return ActionLanguage.LOTUSSCRIPT;
      case PLACEHOLDER:
        // TODO figure out this thing's deal - it seems not used in practice
        return ActionLanguage.FORMULA;
      case SYS_COMMAND:
      case OLDSYS_COMMAND:
        return ActionLanguage.SYSTEM_COMMAND;
      case RUN_AGENT:
      default:
        // More investigation needed
        return ActionLanguage.SIMPLE_ACTION;
    }
  }

  @Override
  public Optional<String> getLabelFormula() {
    return getActionExtRecord()
      .flatMap(actionExt -> {
        String formula = actionExt.getLabelFormula();
        return formula.isEmpty() ? Optional.empty() : Optional.of(formula);
      });
  }
  
  @Override
  public Optional<String> getParentLabelFormula() {
    return getActionExtRecord()
      .flatMap(actionExt -> {
        String formula = actionExt.getParentLabelFormula();
        return formula.isEmpty() ? Optional.empty() : Optional.of(formula);
      });
  }

  @Override
  public Optional<String> getTargetFrame() {
    return getTargetRecord()
      .flatMap(target -> {
        String frame = target.getTargetString().get();
        return frame.isEmpty() ? Optional.empty() : Optional.of(frame);
      });
  }

  @Override
  public ActionBarControlType getDisplayType() {
    return getActionExtRecord()
      .map(CDActionExt::getControlType)
      .orElse(ActionBarControlType.BUTTON);
  }

  @Override
  public Optional<String> getCheckboxFormula() {
    if(getDisplayType() == ActionBarControlType.CHECKBOX) {
      return getActionExtRecord()
        .flatMap(actionExt -> {
          String formula = actionExt.getControlFormula();
          return formula.isEmpty() ? Optional.empty() : Optional.of(formula);
        });
    } else {
      return Optional.empty();
    }
  }

  @Override
  public boolean isIncludeInActionBar() {
    return getActionRecord().getFlags().contains(CDAction.Flag.SHOW_IN_BAR);
  }

  @Override
  public boolean isIconOnlyInActionBar() {
    return getActionRecord().getFlags().contains(CDAction.Flag.TEXT_ONLY_IN_MENU);
  }

  @Override
  public boolean isOppositeAlignedInActionBar() {
    // This meaning is actually just an inverse of the action bar's setting, not
    //   forced right-aligned
    return getActionRecord().getFlags().contains(CDAction.Flag.BUTTON_TO_RIGHT);
  }

  @Override
  public boolean isIncludeInActionMenu() {
    return getActionRecord().getFlags().contains(CDAction.Flag.SHOW_IN_MENU);
  }

  @Override
  public boolean isIncludeInMobileActions() {
    return getActionRecord().getFlags().contains(CDAction.Flag.SHOW_IN_MOBILE_ACTIONS);
  }

  @Override
  public boolean isIncludeInMobileSwipeLeft() {
    return getActionExtRecord()
      .map(CDActionExt::getFlags)
      .map(flags -> flags.contains(CDActionExt.Flag.INCLUDE_IN_SWIPE_LEFT))
      .orElse(false);
  }

  @Override
  public boolean isIncludeInMobileSwipeRight() {
    return getActionExtRecord()
        .map(CDActionExt::getFlags)
        .map(flags -> flags.contains(CDActionExt.Flag.INCLUDE_IN_SWIPE_RIGHT))
        .orElse(false);
  }

  @Override
  public boolean isIncludeInContextMenu() {
    return getActionRecord().getFlags().contains(CDAction.Flag.SHOW_IN_POPUPMENU);
  }

  @Override
  public IconType getIconType() {
    CDAction action = getActionRecord();
    Set<CDAction.Flag> flags = action.getFlags();
    if(flags.contains(CDAction.Flag.IMAGE_RESOURCE_ICON)) {
      return IconType.CUSTOM;
    } else if(action.getIconIndex() != 0) {
      return IconType.NOTES;
    } else {
      return IconType.NONE;
    }
  }

  @Override
  public boolean isDisplayIconOnRight() {
    return getActionRecord().getFlags().contains(CDAction.Flag.ALIGN_ICON_RIGHT);
  }

  @Override
  public int getNotesIconIndex() {
    return getActionRecord().getIconIndex();
  }

  @Override
  public Optional<CDResource> getIconResource() {
    // Should be the only HREF type in the list
    return records.stream()
      .filter(CDResource.class::isInstance)
      .map(CDResource.class::cast)
      .findFirst();
  }

  @Override
  public Set<HideFromDevice> getHideFromDevices() {
    Set<HideFromDevice> result = new HashSet<>();
    Set<CDAction.Flag> flags = getActionRecord().getFlags();
    if(flags.contains(CDAction.Flag.HIDE_FROM_MOBILE)) {
      result.add(HideFromDevice.MOBILE);
    }
    if(flags.contains(CDAction.Flag.HIDE_FROM_NOTES)) {
      result.add(HideFromDevice.NOTES);
    }
    if(flags.contains(CDAction.Flag.HIDE_FROM_WEB)) {
      result.add(HideFromDevice.WEB);
    }
    return result;
  }
  
  @Override
  public boolean isUseHideWhenFormula() {
    return !getActionRecord().getFlags().contains(CDAction.Flag.NO_FORMULA);
  }

  @Override
  public Optional<String> getHideWhenFormula() {
    String hideWhen = getActionRecord().getHideWhenFormula();
    if(hideWhen.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(hideWhen);
    }
  }

  @Override
  public boolean isPublishWithOle() {
    return getActionRecord().getFlags().contains(CDAction.Flag.SHOW_ON_OLE_LAUNCH);
  }

  @Override
  public boolean isCloseOleWhenChosen() {
    return getActionRecord().getFlags().contains(CDAction.Flag.OLE_CLOSE_WHEN_CHOSEN);
  }

  @Override
  public boolean isBringDocumentToFrontInOle() {
    return getActionRecord().getFlags().contains(CDAction.Flag.OLE_DOC_WINDOW_TO_FRONT);
  }

  @Override
  public Optional<String> getCompositeActionName() {
    return getActionExtRecord()
      .flatMap(actionExt -> {
        String compAction = actionExt.getCompActionId();
        return compAction.isEmpty() ? Optional.empty() : Optional.of(compAction);
      });
  }

  @Override
  public String getProgrammaticUseText() {
    return getActionExtRecord()
      .map(CDActionExt::getProgrammaticUseText)
      .orElse(""); //$NON-NLS-1$
  }

  @Override
  public OptionalLong getSharedActionIndex() {
    CDAction action = getActionRecord();
    if(!action.getFlags().contains(CDAction.Flag.SHARED)) {
      return OptionalLong.empty();
    } else {
      return OptionalLong.of(action.getShareId());
    }
  }
  
  @Override
  public boolean isDisplayAsSplitButton() {
    return getActionRecord().getFlags().contains(CDAction.Flag.MAKE_SPLIT_BUTTON);
  }
  
  @Override
  public ActionContent getActionContent() {
    switch(getActionLanguage()) {
      case SIMPLE_ACTION:
        List<RichTextRecord<?>> simpleActionRecords = RichTextUtil.readMemoryRecords(getActionRecord().getActionData(), RecordType.Area.TYPE_ACTION);
        List<SimpleAction> simpleActions = DesignUtil.toSimpleActions(simpleActionRecords);
        return (SimpleActionActionContent)() -> simpleActions;
      case LOTUSSCRIPT:
        // TODO investigate storage of very-large LS
        // The C API docs say this will be stored in separate items, but Designer crashes when trying to store this
        String lotusScript = getActionRecord().getActionLotusScript();
        return (LotusScriptActionContent)() -> lotusScript;
      case COMMON_JAVASCRIPT:
      case JAVASCRIPT: {
        List<ScriptEvent> events = RichTextUtil.readJavaScriptEvents(this.records);
        
        return (JavaScriptActionContent)() -> Collections.unmodifiableList(events);
      }
      case SYSTEM_COMMAND:
        // The content will be two WORDs: one that varies between V4 and V5+ and another that identifies the actual action
        ByteBuffer actionContent = ByteBuffer.wrap(getActionRecord().getActionData()).order(ByteOrder.nativeOrder());
        short val = actionContent.getShort(2);
        SystemActionContent.SystemAction action = DominoEnumUtil.valueOf(SystemActionContent.SystemAction.class, val)
          .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Unable to find System Action value for 0x{0}", Integer.toHexString(val))));
        return (SystemActionContent)() -> action;
      case FORMULA:
      default:
        return (FormulaActionContent)() -> getActionRecord().getActionFormula();
    }
  }

  // *******************************************************************************
  // * Internal implementation methods
  // *******************************************************************************
  
  private CDAction getActionRecord() {
    return records.stream()
      .filter(CDAction.class::isInstance)
      .map(CDAction.class::cast)
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("Unable to find CDACTION record"));
  }
  
  private Optional<CDActionExt> getActionExtRecord() {
    return records.stream()
      .filter(CDActionExt.class::isInstance)
      .map(CDActionExt.class::cast)
      .findFirst();
  }
  
  private Optional<CDTarget> getTargetRecord() {
    return records.stream()
      .filter(CDTarget.class::isInstance)
      .map(CDTarget.class::cast)
      .findFirst();
  }
}
