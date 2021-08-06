package com.hcl.domino.commons.design;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import com.hcl.domino.design.ActionBarAction;
import com.hcl.domino.design.format.ActionBarControlType;
import com.hcl.domino.design.format.HideFromDevice;
import com.hcl.domino.richtext.records.CDAction;
import com.hcl.domino.richtext.records.CDActionExt;
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
  public Optional<String> getTargetFrame() {
    return getTargetRecord()
      .flatMap(target -> {
        String frame = target.getTargetString();
        return frame.isEmpty() ? Optional.empty() : Optional.of(frame);
      });
  }

  @Override
  public ActionBarControlType getDisplayType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<String> getCheckboxFormula() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isIncludeInActionBar() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isIconOnlyInActionBar() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isLeftAlignedInActionBar() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isIncludeInActionMenu() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isIncludeInMobileActions() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isIncludeInMobileSwipeLeft() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isIncludeInMobileSwipeRight() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isIncludeInContextMenu() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public IconType getIconType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isDisplayIconOnRight() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public int getNotesIconIndex() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Optional<CDResource> getIconResource() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<HideFromDevice> getHideFromDevices() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<String> getHideWhenFormula() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isPublishWithOle() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isCloseOleWhenChosen() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isBringDocumentToFrontInOle() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Optional<String> getCompositeActionName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getProgrammaticUseText() {
    // TODO Auto-generated method stub
    return null;
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
