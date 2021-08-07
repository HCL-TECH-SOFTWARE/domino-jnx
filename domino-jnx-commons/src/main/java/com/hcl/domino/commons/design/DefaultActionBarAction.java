package com.hcl.domino.commons.design;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import com.hcl.domino.design.ActionBarAction;
import com.hcl.domino.design.format.ActionBarControlType;
import com.hcl.domino.design.format.HideFromDevice;
import com.hcl.domino.richtext.records.CDAction;
import com.hcl.domino.richtext.records.CDActionExt;
import com.hcl.domino.richtext.records.CDEventEntry;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.records.CDTarget;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * Default implementation of {@link ActionBarActions} that works based on
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
        String frame = target.getTargetString();
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
  public boolean isLeftAlignedInActionBar() {
    // This appears to be flipped in meaning from the constant's name
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
  
  private Optional<CDEventEntry> getEventEntryRecord() {
    return records.stream()
      .filter(CDEventEntry.class::isInstance)
      .map(CDEventEntry.class::cast)
      .findFirst();
  }
}
