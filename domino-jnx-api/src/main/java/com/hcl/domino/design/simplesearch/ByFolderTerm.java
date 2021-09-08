package com.hcl.domino.design.simplesearch;

/**
 * Represents a search for documents that are within a named form or view.
 * 
 * @author Jesse Gallagher
 * @since 1.0.38
 */
public interface ByFolderTerm extends SimpleSearchTerm {
  /**
   * Determines whether the view or folder referenced by this term
   * is expected to be private.
   * 
   * @return {@code true} if the referenced collection is expected
   *         to be private; {@code false} if it is shared 
   */
  boolean isPrivate();
  
  /**
   * Retrieves the folder or view name queried by this term.
   * 
   * @return the name of folder or view
   */
  String getFolderName();
}
