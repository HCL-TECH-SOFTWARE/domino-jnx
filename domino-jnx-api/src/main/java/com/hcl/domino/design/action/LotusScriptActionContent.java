package com.hcl.domino.design.action;

/**
 * Represents the contents of a LotusScript-type action.
 *
 * @author Jesse Gallagher
 * @since 1.0.33
 */
public interface LotusScriptActionContent extends ActionContent {
  String getScript();
}
