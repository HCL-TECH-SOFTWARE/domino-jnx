package com.hcl.domino.commons.design;

import com.hcl.domino.design.ActionBar;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.DesignElement.ActionBarElement;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public interface IDefaultActionBarElement extends ActionBarElement {
  @Override
  default ActionBar getActionBar() {
    // TODO account for pre-V5 actions
    return new DefaultActionBar(getDocument(), DesignConstants.V5ACTION_ITEM);
  }
}
