package com.hcl.domino.design.simplesearch;

/**
 * Represents a search for documents by a field value.
 * 
 * @author Jesse Gallagher
 * @since 1.0.38
 */
public interface ByFieldTerm extends SimpleSearchTerm {
  enum Type {
    CONTAINS, DOES_NOT_CONTAIN
  }
  
  /**
   * Determines the field query type for this term.
   * 
   * @return a {@link Type} instance
   */
  Type getType();
  
  /**
   * Retrieves the name of the field to query.
   * 
   * @return the name of the field to query
   */
  String getFieldName();

  /**
   * Retrieves the value that must or must not appear in the queried
   * field.
   * 
   * @return a string field value
   */
  String getValue();
}
