package com.hcl.domino.design;

import com.hcl.domino.design.DesignElement.NamedDesignElement;

/**
 * Frameset design element
 * 
 * @author Karsten Lehmann
 * @since 1.0.40
 */
public interface Frameset extends NamedDesignElement {

  /**
   * Returns the layout of the frameset. Use it to add/remove
   * frames and embedded framesets.
   * 
   * @return layout
   */
  public FramesetLayout getLayout();
  
}
