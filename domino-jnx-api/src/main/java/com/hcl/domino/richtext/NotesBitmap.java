package com.hcl.domino.richtext;

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
