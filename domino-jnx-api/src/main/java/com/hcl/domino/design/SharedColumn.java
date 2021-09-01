package com.hcl.domino.design;

import com.hcl.domino.data.CollectionColumn;

/**
 * Represents a shared column within a database.
 * 
 * @author Jesse Gallagher
 * @since 1.0.37
 */
public interface SharedColumn extends DesignElement.NamedDesignElement {
  CollectionColumn getColumn();
}
