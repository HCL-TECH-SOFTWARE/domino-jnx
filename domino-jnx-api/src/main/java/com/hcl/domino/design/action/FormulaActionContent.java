package com.hcl.domino.design.action;

/**
 * Represents the content of a formula-language action.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public interface FormulaActionContent extends ActionContent {

  /**
   * @return the action formula script
   */
  String getFormula();
}
