package com.hcl.domino.commons.data;

import com.hcl.domino.data.CollectionEntry;

/**
 * Default base implementation of {@link CollectionEntry}.
 * 
 * @author Jesse Gallagher
 * @since 1.0.46
 */
public abstract class AbstractCollectionEntry implements CollectionEntry {
  
  @Override
  public int getIndentLevel() {
    return getSpecialValue(SpecialValue.INDEXPOSITION, int[].class, new int[1]).length;
  }
}
