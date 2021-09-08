package com.hcl.domino.design.simplesearch;

import java.util.Set;

/**
 * Represents a search for documents that use one of a set of forms.
 * 
 * @author Jesse Gallagher
 * @since 1.0.38
 */
public interface ByFormTerm extends SimpleSearchTerm {
  /**
   * Retrieves the names of the forms queried by this term.
   * 
   * @return a {@link Set} of form names
   */
  Set<String> getFormNames();
}
