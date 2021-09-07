package com.hcl.domino.design.simplesearch;

/**
 * Represents a basic text term within a simple search.
 * 
 * @author Jesse Gallagher
 * @since 1.0.38
 */
public interface TextTerm extends SimpleSearchTerm {
  /**
   * Retrieves the text of the term.
   * 
   * @return the text of the term
   */
  String getText();
}
