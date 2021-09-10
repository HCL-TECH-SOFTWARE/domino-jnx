package com.hcl.domino.design.simplesearch;

import java.util.List;
import java.util.Map;

/**
 * Represents a search for documents using an example form.
 * 
 * @author Jesse Gallagher
 * @since 1.0.38
 */
public interface ExampleFormTerm extends SimpleSearchTerm {
  /**
   * Retrieves the name of the form used to provide the example.
   * 
   * @return the string form name
   */
  String getFormName();
  
  /**
   * Retrieves the field/value term queries used by this term.
   * 
   * @return a {@link Map} of field names to matched values
   */
  Map<String, List<String>> getFieldMatches();
}
