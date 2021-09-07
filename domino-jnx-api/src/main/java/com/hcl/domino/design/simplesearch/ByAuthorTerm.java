package com.hcl.domino.design.simplesearch;

/**
 * Represents a search for documents with a given author.
 * 
 * @author Jesse Gallagher
 * @since 1.0.38
 */
public interface ByAuthorTerm extends SimpleSearchTerm {
  /**
   * Retrieves the author name for the term.
   * 
   * @return the author name to search for
   */
  String getAuthor();
}
