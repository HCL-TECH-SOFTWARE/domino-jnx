package com.hcl.domino.commons.data;

import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.ModificationTimePair;

/**
 * Basic implementation of {@link ModificationTimePair}.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public class DefaultModificationTimePair implements ModificationTimePair {
  private final DominoDateTime dataModified;
  private final DominoDateTime nonDataModified;

  public DefaultModificationTimePair(DominoDateTime dataModified, DominoDateTime nonDataModified) {
    this.dataModified = dataModified;
    this.nonDataModified = nonDataModified;
  }

  @Override
  public DominoDateTime getDataModified() {
    return this.dataModified;
  }

  @Override
  public DominoDateTime getNonDataModified() {
    return this.nonDataModified;
  }
}
