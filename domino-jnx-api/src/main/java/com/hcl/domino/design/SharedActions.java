package com.hcl.domino.design;

import java.util.List;

import com.hcl.domino.design.action.ActionBarAction;

/**
 * Represents the shared-actions note for a database.
 * 
 * @author Jesse Gallagher
 * @since 1.0.37
 */
public interface SharedActions extends DesignElement {
  /**
   * Retrieves the actions from the shared-actions note, in declaration order.
   *  
   * @return a {@link List} of the actions in the note
   */
  List<ActionBarAction> getActions();
}
