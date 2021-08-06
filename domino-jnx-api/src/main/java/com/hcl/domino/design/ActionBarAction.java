package com.hcl.domino.design;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import com.hcl.domino.design.format.ActionBarControlType;
import com.hcl.domino.design.format.HideFromDevice;
import com.hcl.domino.richtext.records.CDResource;

/**
 * Describes an individual action from the bar on top of collection and form
 * design elements.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public interface ActionBarAction {
  /**
   * Describes the available icon display types for actions.
   * 
   * @author Jesse Gallagher
   * @since 1.0.32
   */
  enum IconType {
    NONE, NOTES, CUSTOM
  }
  
  /**
   * Retrieves the name of the action.
   * 
   * This name may include hierarchical components, separated by {@code '\'}.
   * 
   * @return the action name
   */
  String getName();
  
  /**
   * Retrieves the formula for a displayed label, if specified.
   * 
   * @return an {@link Optional} describing the label formula, or an empty one
   *         if this is not specified
   */
  Optional<String> getLabelFormula();
  
  /**
   * Retrieves the formula for the displayed label of the action's grouping parent
   * button, if specified.
   * 
   * @return an {@link Optional} describing the parent label formula, or an empty one
   *         if this is not specified
   */
  Optional<String> getParentLabelFormula();
  
  /**
   * Retrieves the target frame for applicable action behaviors.
   * 
   * @return an {@link Optional} describing the target frame, or an empty one
   *         if this is not specified
   */
  Optional<String> getTargetFrame();
  
  /**
   * Retrieves the display type to use when rendering this action.
   * 
   * @return a {@link ActionBarControlType}
   */
  ActionBarControlType getDisplayType();
  
  /**
   * Retrieves the formula used for the checkbox state when {@link #getDisplayType()}
   * is {@link ActionBarControlType#CHECKBOX CHECKBOX}.
   * 
   * @return an {@link Optional} describing the checkbox formula, or an empty one
   *         if this is not set
   */
  Optional<String> getCheckboxFormula();
  
  /**
   * Determines whether the action should be displayed in the action bar.
   * 
   * @return {@code true} to display the action in the action bar;
   *         {@code false} otherwise
   */
  boolean isIncludeInActionBar();
  
  /**
   * Determines whether the action should display its button only when
   * {@link #isIncludeInActionBar()} is {@code true}.
   * 
   * @return {@code true} if the action should display only its action when
   *         displayed in the action bar; {@code false} otherwise
   */
  boolean isIconOnlyInActionBar();
  
  /**
   * Determines whether the action should be included in the right-side grouping
   * when displayed in the action bar.
   * 
   * @return {@code true} if the action should be right-aligned when displayed
   *         in the bar; {@code false} otherwise
   */
  boolean isRightAlignedInActionBar();
  
  /**
   * Determines whether the action should be included in the "Actions" menu in
   * the client UI.
   * 
   * @return {@code true} if the action should be included in the "Actions" menu;
   *         {@code false} otherwise
   */
  boolean isIncludeInActionMenu();
  
  /**
   * Determines whether the action should be included in the mobile-specific actions
   * button popout.
   * 
   * @return {@code true} if the action should be in the mobile actions popout;
   *         {@code false} otherwise
   */
  boolean isIncludeInMobileActions();
  
  /**
   * Determines whether the action should be included in the mobile-specific swipe-
   * left behavior list.
   * 
   * @return {@code true} if the action should be included in the swipe-left list;
   *         {@code false} otherwise
   */
  boolean isIncludeInMobileSwipeLeft();
  
  /**
   * Determines whether the action should be included in the mobile-specific swipe-
   * right behavior list.
   * 
   * @return {@code true} if the action should be included in the swipe-right list;
   *         {@code false} otherwise
   */
  boolean isIncludeInMobileSwipeRight();
  
  /**
   * Determines whether the action should be included in the right-click contextual
   * menu.
   * 
   * @return {@code true} if the action should be included in the context menu;
   *         {@code false} otherwise
   */
  boolean isIncludeInContextMenu();
  
  /**
   * Determines the icon display type for the action.
   * 
   * @return a {@link IconType} instance
   */
  IconType getIconType();
  
  /**
   * Determines whether the icon should be displayed on the right side of the action,
   * when applicable.
   * 
   * @return {@code true} to display the icon on the right of the button;
   *         {@code false} to display on the left
   */
  boolean isDisplayIconOnRight();
  
  /**
   * Retrieves the index of the icon to display from the Notes standard icon pool when
   * {@link #getIconType()} is {@link IconType#NOTES NOTES}.
   * 
   * @return the index of the Notes-type icon to display when applicable
   */
  int getNotesIconIndex();
  
  /**
   * Retrieves the image resource to use for the icon when {@link #getIconType()} is
   * {@link IconType#CUSTOM CUSTOM}.
   * 
   * @return an {@link Optional} describing the icon image resource, or an empty one
   *         if this is not set
   */
  Optional<CDResource> getIconResource();
  
  /**
   * Retrieves the device types that this action is marked as hidden from.
   * 
   * @return a {@link Set} of {@link HideFromDevice} instances
   */
  Set<HideFromDevice> getHideFromDevices();
  
  /**
   * Retrieves the hide-when formula for this action, if set.
   * 
   * @return an {@link Optional} describing the hide-when formula for the action, or an
   *         empty one if this is not set
   */
  Optional<String> getHideWhenFormula();
  
  /**
   * Determines whether the action should be published with an associated OLE object.
   * 
   * @return {@code true} if the action should be published with OLE when applicable;
   *         {@code false} otherwise
   */
  boolean isPublishWithOle();
  
  /**
   * Determines whether an associated OLE object should be closed when this action is
   * chosen.
   * 
   * @return {@code true} if an associated OLE object should be closed when this action
   *         is chosen; {@code false} if it should be left open
   */
  boolean isCloseOleWhenChosen();
  
  /**
   * Determines whether the document window should be brought to the front when this action
   * is chosen from the context of an associated OLE object.
   * 
   * @return {@code true} if the document window should come to the front in OLE;
   *         {@code false} otherwise
   */
  boolean isBringDocumentToFrontInOle();
  
  /**
   * Retrieves the name of the associated Composite-Application action.
   * 
   * @return an {@link Optional} describing the name of the associated Composite Application
   *         action, or an empty one if this is not set
   */
  Optional<String> getCompositeActionName();
  
  /**
   * Retrieves the programmatic-use text set for this action, intended for Composite Application
   * use.
   * 
   * @return the programmatic-use text
   */
  String getProgrammaticUseText();
  
  /**
   * Retrieves the index of the action in the shared-actions pool represented by this action.
   * 
   * @return an {@link OptionalLong} describing the shared action index if this is a shared,
   *         or an empty one if it is not
   */
  OptionalLong getSharedActionIndex();
  
  /**
   * Determines whether the drop-down menu containing this action should be displayed as a
   * split button, when this action is the first in the menu.
   * 
   * @return {@code true} to display the containing menu as a split button;
   *         {@code false} otherwise
   */
  boolean isDisplayAsSplitButton();
}
