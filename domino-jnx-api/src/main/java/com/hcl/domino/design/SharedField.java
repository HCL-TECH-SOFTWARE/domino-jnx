package com.hcl.domino.design;

import java.util.List;

import com.hcl.domino.design.DesignElement.NamedDesignElement;

/**
 * Represents a Shared Field design element.
 * 
 * @author Jesse Gallagher
 * @since 1.0.34
 */
public interface SharedField extends NamedDesignElement {
  /**
   * Retrieves the field components as a list of rich-text entities.
   * 
   * @return a {@link List} of rich-text entities
   * @since 1.0.34
   */
  List<?> getFieldBody();
  
  /**
   * Retrieves the element-global LotusScript associated with the field.
   * 
   * @return a {@link String} representing the IDE-formatted LotusScript for the element
   * @since 1.0.34
   */
  String getLotusScript();
}
