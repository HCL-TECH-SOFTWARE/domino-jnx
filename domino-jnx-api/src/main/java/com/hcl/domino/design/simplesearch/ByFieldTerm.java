package com.hcl.domino.design.simplesearch;

/**
 * Represents a search for documents by a field value.
 * 
 * <p>Terms of this type contain values when searching against text
 * items, but the specialized types {@link ByDateFieldTerm} and
 * {@link ByNumberFieldTerm} contain additional rules for working
 * against date and number items, respectively.</p>
 * 
 * @author Jesse Gallagher
 * @since 1.0.38
 */
public interface ByFieldTerm extends SimpleSearchTerm {
  enum TextRule {
    /**
     * Indicates that a text item contains the associated text
     */
    CONTAINS,
    /**
     * Indicates that a text item does not contain the associated
     * text
     */
    DOES_NOT_CONTAIN
  }
  
  /**
   * Determines the field query type for this term when used for text
   * comparison.
   * 
   * @return a {@link TextRule} instance
   */
  TextRule getTextRule();
  
  /**
   * Retrieves the name of the field to query.
   * 
   * @return the name of the field to query
   */
  String getFieldName();

  /**
   * Retrieves the text value that is used when this term is applied
   * to text items.
   * 
   * @return a string field value
   */
  String getTextValue();
}
