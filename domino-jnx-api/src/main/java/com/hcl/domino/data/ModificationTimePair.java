package com.hcl.domino.data;

/**
 * Represents modification times for a database's data and non-data
 * stores.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public interface ModificationTimePair {
  DominoDateTime getDataModified();
  DominoDateTime getNonDataModified();
}
