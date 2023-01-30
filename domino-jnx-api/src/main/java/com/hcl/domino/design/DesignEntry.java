package com.hcl.domino.design;

import java.util.List;

import com.hcl.domino.data.Database;

/**
 * Represents summary information about a design element in a
 * database.
 * 
 * @param <T> the {@link DesignElement} type assumed to be represented
 *            by this entry
 * @since 1.21.0
 */
public interface DesignEntry<T extends DesignElement> {
  /**
   * Retrieves any comment value for the design element.
   * 
   * @return the design-element comment as a string
   */
  String getComment();
  
  /**
   * Retrieves the set of element flags.
   * 
   * @return the design-element flags as a string
   */
  String getFlags();
  
  /**
   * Retrieves the language of the design element, if applicable.
   * 
   * @return the design-element language as a string
   */
  String getLanguage();
  
  /**
   * Retrieves the note ID of the underlying note for the design
   * element.
   * 
   * @return the design-element note ID
   */
  int getNoteID();
  
  /**
   * Retrieves the universal ID of the underlying note for the design
   * element.
   * 
   * @return the design-element UNID
   */
  String getUNID();
  
  /**
   * Retrieves the title and any aliases of the design element.
   * 
   * @return the design-element titles and aliases as a {@link List}
   *         of strings
   */
  List<String> getTitles();
  
  /**
   * Attempts to load the design element from the specified database, which
   * should be the database that was originally queried.
   * 
   * <p>The actual return type will depend on the type of design element.</p>
   * 
   * @param database the database to load the design element from
   * @return the design element as an implementation class of {@link DesignElement}
   */
  T toDesignElement(final Database database);
}
