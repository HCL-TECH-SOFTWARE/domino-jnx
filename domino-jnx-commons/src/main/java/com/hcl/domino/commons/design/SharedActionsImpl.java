package com.hcl.domino.commons.design;

import java.util.List;

import com.hcl.domino.data.Document;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.SharedActions;
import com.hcl.domino.design.action.ActionBarAction;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.0.37
 */
public class SharedActionsImpl extends AbstractDesignElement<SharedActions> implements SharedActions {

  public SharedActionsImpl(Document doc) {
    super(doc);
  }

  @Override
  public void initializeNewDesignNote() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public List<ActionBarAction> getActions() {
    // TODO account for pre-V5 actions
    return new DefaultActionBar(getDocument(), DesignConstants.V5ACTION_ITEM).getActions();
  }
}
