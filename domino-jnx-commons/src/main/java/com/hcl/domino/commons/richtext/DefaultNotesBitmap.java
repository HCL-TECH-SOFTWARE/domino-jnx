/*
 * ==========================================================================
 * Copyright (C) 2019-2021 HCL America, Inc. ( http://www.hcl.com/ )
 *                            All rights reserved.
 * ==========================================================================
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may
 * not use this file except in compliance with the License.  You may obtain a
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.
 *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the  specific language  governing permissions  and limitations
 * under the License.
 * ==========================================================================
 */
package com.hcl.domino.commons.richtext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.hcl.domino.design.DesignColorsAndFonts;
import com.hcl.domino.richtext.NotesBitmap;
import com.hcl.domino.richtext.RectangleSize;
import com.hcl.domino.richtext.records.CDBitmapHeader;
import com.hcl.domino.richtext.records.CDGraphic;
import com.hcl.domino.richtext.records.RichTextRecord;

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
  public RectangleSize getDestinationSize() {
    return getBitmapHeader()
      .map(CDBitmapHeader::getDest)
      .map(RectangleSize.class::cast)
      .orElseGet(() ->
        getGraphic()
          .map(CDGraphic::getDestSize)
          .map(RectangleSize.class::cast)
          .orElseGet(DesignColorsAndFonts::zeroPixelRectangle)
      );
  }

  @Override
  public RectangleSize getSize() {
    return new RectangleSize() {
      
      @Override
      public RectangleSize setWidth(int width) {
        Optional<CDBitmapHeader> header = getBitmapHeader();
        if(header.isPresent()) {
          header.get().setWidth(width);
        }
        return this;
      }
      
      @Override
      public RectangleSize setHeight(int height) {
        Optional<CDBitmapHeader> header = getBitmapHeader();
        if(header.isPresent()) {
          header.get().setHeight(height);
        }
        return this;
      }
      
      @Override
      public int getWidth() {
        return getBitmapHeader()
          .map(CDBitmapHeader::getWidth)
          .orElseGet(() -> 
            getGraphic()
              .map(graphic -> graphic.getDestSize().getWidth())
              .orElse(0)
          );
      }
      
      @Override
      public int getHeight() {
        return getBitmapHeader()
          .map(CDBitmapHeader::getHeight)
          .orElseGet(() -> 
            getGraphic()
              .map(graphic -> graphic.getDestSize().getHeight())
              .orElse(0)
          );
      }
    };
  }

  @Override
  public int getBitsPerPixel() {
    return getBitmapHeader()
      .map(CDBitmapHeader::getBitsPerPixel)
      .orElse(0);
  }

  @Override
  public int getSamplesPerPixel() {
    return getBitmapHeader()
      .map(CDBitmapHeader::getSamplesPerPixel)
      .orElse(0);
  }

  @Override
  public int getBitsPerSample() {
    return getBitmapHeader()
      .map(CDBitmapHeader::getBitsPerSample)
      .orElse(0);
  }

  // *******************************************************************************
  // * Internal implementation utilities
  // *******************************************************************************
  
  private Optional<CDBitmapHeader> getBitmapHeader() {
    return records.stream()
      .filter(CDBitmapHeader.class::isInstance)
      .map(CDBitmapHeader.class::cast)
      .findFirst();
  }
  
  private Optional<CDGraphic> getGraphic() {
    return records.stream()
      .filter(CDGraphic.class::isInstance)
      .map(CDGraphic.class::cast)
      .findFirst();
  }
}
