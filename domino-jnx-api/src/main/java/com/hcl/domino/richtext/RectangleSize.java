package com.hcl.domino.richtext;

/**
 * Represents rectangle metrics as implemented by several mechanisms.
 * 
 * @author Jesse Gallagher
 * @since 1.0.34
 */
public interface RectangleSize {
  int getHeight();

  int getWidth();

  RectangleSize setHeight(int height);

  RectangleSize setWidth(int width);
}
