package com.hcl.domino.richtext;

import com.hcl.domino.richtext.structures.RectSize;

/**
 * Represents a Notes-style bitmap stored in rich-text structures.
 * 
 * @author Jesse Gallagher
 * @since 1.0.34
 */
public interface NotesBitmap {
  RectangleSize getDestinationSize();
  
  RectangleSize getSize();
  
  int getBitsPerPixel();
  
  int getSamplesPerPixel();
  
  int getBitsPerSample();
}
