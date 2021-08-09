package com.hcl.domino.design.action;

import java.util.List;

import com.hcl.domino.design.simpleaction.SimpleAction;

/**
 * Represents the contents of a Simple Action action.
 *
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public interface SimpleActionActionContent extends ActionContent {
  List<SimpleAction> getActions();
}
