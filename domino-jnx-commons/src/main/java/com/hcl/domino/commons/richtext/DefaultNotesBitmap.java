package com.hcl.domino.commons.richtext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hcl.domino.richtext.NotesBitmap;
import com.hcl.domino.richtext.RectangleSize;
import com.hcl.domino.richtext.records.CDBitmapHeader;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.structures.RectSize;

/**
 * Default implementation of {@link NotesBitmap} that derives its data
 * from a collection of {@link RichTextRecord} instances.
 * 
 * @author Jesse Gallagher
 * @since 1.0.34
 */
public class DefaultNotesBitmap implements NotesBitmap {
  private final List<RichTextRecord<?>> records;

  public DefaultNotesBitmap(Collection<RichTextRecord<?>> records) {
    this.records = new ArrayList<>(records);
  }

  @Override
  public RectSize getDestinationSize() {
    return getBitmapHeader().getDest();
  }

  @Override
  public RectangleSize getSize() {
    return new RectangleSize() {
      
      @Override
      public RectangleSize setWidth(int width) {
        getBitmapHeader().setWidth(width);
        return this;
      }
      
      @Override
      public RectangleSize setHeight(int height) {
        getBitmapHeader().setHeight(height);
        return this;
      }
      
      @Override
      public int getWidth() {
        return getBitmapHeader().getWidth();
      }
      
      @Override
      public int getHeight() {
        return getBitmapHeader().getHeight();
      }
    };
  }

  @Override
  public int getBitsPerPixel() {
    return getBitmapHeader().getBitsPerPixel();
  }

  @Override
  public int getSamplesPerPixel() {
    return getBitmapHeader().getSamplesPerPixel();
  }

  @Override
  public int getBitsPerSample() {
    return getBitmapHeader().getBitsPerSample();
  }

  // *******************************************************************************
  // * Internal implementation utilities
  // *******************************************************************************
  
  private CDBitmapHeader getBitmapHeader() {
    return records.stream()
      .filter(CDBitmapHeader.class::isInstance)
      .map(CDBitmapHeader.class::cast)
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("Cannot find CDBITMAPHEADER"));
  }
}
