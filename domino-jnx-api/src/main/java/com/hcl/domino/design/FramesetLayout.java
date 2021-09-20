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
package com.hcl.domino.design;

import java.util.Optional;
import java.util.stream.Stream;

import com.hcl.domino.richtext.structures.ColorValue;

/**
 * Represents the layout of a frameset
 * 
 * @author Karsten Lehmann
 * @since 1.0.42
 */
public interface FramesetLayout extends FrameContent<FramesetLayout> {

  enum FramesetOrientation {
    LEFT_TO_RIGHT,
    TOP_TO_BOTTOM
  }

  /**
   * Returns the orientation of the frameset
   * 
   * @return orientation
   */
  Optional<FramesetOrientation> getOrientation();

  /**
   * Changes the orientation of the frameset content
   * 
   * @param orientation new orientation
   * @return this frameset
   */
  FramesetLayout setOrientation(FramesetOrientation orientation);

  /**
   * Returns the content of this frameset
   * 
   * @return a {@link Stream} of {@link Frame} and {@link FramesetLayout} elements
   */
  Stream<FrameContent<?>> getContent();

  /**
   * Returns content of the frameset at the specified position
   * 
   * @param pos position
   * @return content
   */
  FrameContent<?> getContent(int pos);

  /**
   * Returns the number of elements in the frame
   * 
   * @return count
   */
  int getCount();

  /**
   * Width/height unit for frame content
   */
  enum FrameSizeUnit {
    /**
     * Attribute is expressed as relative
     */
    RELATIVE,
    /**
     * Attribute is expressed as a percentage
     */
    PERCENTAGE,
    /**
     * Attribute is expressed as pixels
     */
    PIXELS
  }

  /**
   * Creates a new frame object
   * 
   * @return frame
   */
  Frame createFrame();

  /**
   * Creates a new frameset object
   * 
   * @return frameset
   */
  FramesetLayout createFrameset();

  /**
   * Replaces the frameset layout with the specified
   * content and switches the orientation to {@link FramesetOrientation#LEFT_TO_RIGHT}.
   * 
   * @param content frames and framesets
   * @return this frameset
   */
  FramesetLayout initColumns(FrameContent<?>... content);
  
  /**
   * Replaces the frameset layout with the specified
   * content and switches the orientation to {@link FramesetOrientation#TOP_TO_BOTTOM}.
   * 
   * @param content frames and framesets
   * @return this frameset
   */
  FramesetLayout initRows(FrameContent<?>... content);

  /**
   * Splits a frame into two columns
   * 
   * @param frame frame
   * @return if frameset is left-to-right aligned, we return the new {@link Frame}, otherwise we return a newly created {@link FramesetLayout}
   */
  FrameContent<?> splitIntoColumns(Frame frame);

  /**
   * Splits a frame into two rows
   * 
   * @param frame frame
   * @return if frameset is top-to-bottom aligned, we return the new {@link Frame}, otherwise we return a newly created {@link FramesetLayout}
   */
  FrameContent<?> splitIntoRows(Frame frame);

  /**
   * Removes a frame or sub frameset
   * 
   * @param content content to remove
   */
  void deleteFrameContent(FrameContent<?> content);

  /**
   * Replaces frame content
   * 
   * @param oldContent old content
   * @param newContent new content
   * @return this layout
   */
  FramesetLayout replace(FrameContent<?> oldContent, FrameContent<?> newContent);
  
  /**
   * Reverses the order of the content frames/framesets
   * 
   * @return this layout
   */
  FramesetLayout flipHorizontally();

  /**
   * Checks if there border is enabled
   * 
   * @return true if enabled
   */
  boolean isBorderEnabled();
  
  /**
   * Enables/disables the border
   * 
   * @param b true to enable
   * @return this layout
   */
  FramesetLayout setBorderEnabled(boolean b);
  
  /**
   * Returns the frame border default width
   * 
   * @return width
   */
  int getFrameBorderWidth();
  
  /**
   * Sets the frame border width
   * 
   * @param width width
   * @return this layout
   */
  FramesetLayout setFrameBorderWidth(int width);
  
  /**
   * Returns the frame spacing width
   * 
   * @return width
   */
  int getFrameSpacingWidth();
  
  /**
   * Sets the frame spacing width
   * 
   * @param width width
   * @return this layout
   */
  FramesetLayout setFrameSpacingWidth(int width);
  
  /**
   * Returns the frame border color
   * 
   * @return color
   */
  ColorValue getFrameBorderColor();
  
  /**
   * Sets the frame border color
   * 
   * @param color new color
   * @return this layout
   */
  FramesetLayout setFrameBorderColor(ColorValue color);
  
}
