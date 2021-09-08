package com.hcl.domino.design.simplesearch;

import java.util.List;

/**
 * Represents a basic or multi-value text term within a simple search.
 * 
 * @author Jesse Gallagher
 * @since 1.0.38
 */
public interface TextTerm extends SimpleSearchTerm {
  enum Type {
    PLAIN, AND, ACCRUE, NEAR
  }
  
  /**
   * Retrieves the type of operation for multi-value terms.
   * 
   * @return a {@link Type} instance
   */
  Type getType();
  
  /**
   * Retrieves the text values of the term.
   * 
   * @return the text values of the term
   */
  List<String> getValues();
}
