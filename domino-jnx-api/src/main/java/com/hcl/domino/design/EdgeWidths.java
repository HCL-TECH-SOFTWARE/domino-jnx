package com.hcl.domino.design;

/**
 * Represents the settings in a design component for margin or border
 * widths around each edge.
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public interface EdgeWidths {
  int getTop();
  int getLeft();
  int getRight();
  int getBottom();
}
