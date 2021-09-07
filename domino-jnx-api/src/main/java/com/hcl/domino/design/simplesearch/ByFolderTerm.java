package com.hcl.domino.design.simplesearch;

/**
 * Represents a search for documents that are within a named form or view.
 * 
 * @author Jesse Gallagher
 * @since 1.0.38
 */
public interface ByFolderTerm extends SimpleSearchTerm {
  /**
   * Retrieves the folder or view name queried by this term.
   * 
   * @return the name of folder or view
   */
  String getFolderName();
}
