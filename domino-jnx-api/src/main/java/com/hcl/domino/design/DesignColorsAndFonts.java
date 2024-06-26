/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
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

import java.util.EnumSet;

import com.hcl.domino.data.FontAttribute;
import com.hcl.domino.data.StandardColors;
import com.hcl.domino.data.StandardFonts;
import com.hcl.domino.richtext.RectangleSize;
import com.hcl.domino.richtext.records.CDDocument;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;

/**
 * Provides generator methods to create common stock {@link ColorValue} values
 * in memory;
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum DesignColorsAndFonts {
  ;

  /**
   * Returns a new in-memory "no color" color structure.
   * 
   * @return a new {@link ColorValue} structure
   * @since 1.0.32
   */
  public static ColorValue noColor() {
    ColorValue result = MemoryStructureWrapperService.get().newStructure(ColorValue.class, 0);
    result.setFlags(EnumSet.of(ColorValue.Flag.NOCOLOR));
    result.setRed((short)0);
    result.setGreen((short)0);
    result.setBlue((short)0);
    return result;
  }

  /**
   * Returns a new in-memory "system color" color structure.
   * 
   * @return a new {@link ColorValue} structure
   * @since 1.0.32
   */
  public static ColorValue systemColor() {
    ColorValue result = MemoryStructureWrapperService.get().newStructure(ColorValue.class, 0);
    result.setFlags(EnumSet.of(ColorValue.Flag.SYSTEMCOLOR));
    result.setRed((short)0);
    result.setGreen((short)0);
    result.setBlue((short)0);
    return result;
  }

  /**
   * Returns a new in-memory white color structure.
   * 
   * @return a new {@link ColorValue} structure
   * @since 1.0.32
   */
  public static ColorValue whiteColor() {
    ColorValue result = MemoryStructureWrapperService.get().newStructure(ColorValue.class, 0);
    result.setFlags(EnumSet.of(ColorValue.Flag.ISRGB));
    result.setRed((short)255);
    result.setGreen((short)255);
    result.setBlue((short)255);
    return result;
  }

  /**
   * Returns a new in-memory black color structure.
   * 
   * @return a new {@link ColorValue} structure
   * @since 1.0.32
   */
  public static ColorValue blackColor() {
    ColorValue result = MemoryStructureWrapperService.get().newStructure(ColorValue.class, 0);
    result.setFlags(EnumSet.of(ColorValue.Flag.ISRGB));
    result.setRed((short)0);
    result.setGreen((short)0);
    result.setBlue((short)0);
    return result;
  }
  
  /**
   * Returns a new in-memory color structure representing the default color
   * used for active links in classic web rendering.
   * 
   * @return a new {@link ColorValue} structure
   * @since 1.0.32
   */
  public static ColorValue defaultActiveLink() {
    ColorValue result = MemoryStructureWrapperService.get().newStructure(ColorValue.class, 0);
    result.setFlags(EnumSet.of(ColorValue.Flag.ISRGB));
    result.setRed((short)255);
    result.setGreen((short)0);
    result.setBlue((short)0);
    return result;
  }
  
  /**
   * Returns a new in-memory color structure representing the default color
   * used for unvisited links in classic web rendering.
   * 
   * @return a new {@link ColorValue} structure
   * @since 1.0.32
   */
  public static ColorValue defaultUnvisitedLink() {
    ColorValue result = MemoryStructureWrapperService.get().newStructure(ColorValue.class, 0);
    result.setFlags(EnumSet.of(ColorValue.Flag.ISRGB));
    result.setRed((short)0);
    result.setGreen((short)0);
    result.setBlue((short)255);
    return result;
  }
  
  /**
   * Returns a new in-memory color structure representing the default color
   * used for visited links in classic web rendering.
   * 
   * @return a new {@link ColorValue} structure
   * @since 1.0.32
   */
  public static ColorValue defaultVisitedLink() {
    ColorValue result = MemoryStructureWrapperService.get().newStructure(ColorValue.class, 0);
    result.setFlags(EnumSet.of(ColorValue.Flag.ISRGB));
    result.setRed((short)128);
    result.setGreen((short)0);
    result.setBlue((short)128);
    return result;
  }
  
  /**
   * Returns a new in-memory font structure representing the default font settings
   * for a new view/folder column.
   * 
   * @return a new {@link FontStyle} structure
   * @since 1.0.32
   */
  public static FontStyle viewHeaderFont() {
    FontStyle result = MemoryStructureWrapperService.get().newStructure(FontStyle.class, 0);
    result.setStandardFont(StandardFonts.SWISS);
    result.setPointSize(9);
    result.setAttributes(EnumSet.of(FontAttribute.BOLD));
    return result;
  }
  
  /**
   * Returns a new in-memory font structure representing the default font settings
   * for most Notes situations.
   * 
   * @return a new {@link FontStyle} structure
   * @since 1.0.32
   */
  public static FontStyle defaultFont() {
    FontStyle result = MemoryStructureWrapperService.get().newStructure(FontStyle.class, 0);
    result.setStandardFont(StandardFonts.SWISS);
    result.setPointSize(10);
    result.setAttributes(EnumSet.of(FontAttribute.BOLD));
    return result;
  }
  
  /**
   * Return a new, memory-only rectangle set to zero pixel dimentions.
   * 
   * @return a new {@link RectangleSize} object
   * @since 1.0.34
   */
  public static RectangleSize zeroPixelRectangle() {
    return new RectangleSize() {
      private int width = 0;
      private int height;
      
      @Override
      public RectangleSize setWidth(int width) {
        this.width = width;
        return this;
      }
      
      @Override
      public RectangleSize setHeight(int height) {
        this.height = height;
        return this;
      }
      
      @Override
      public int getWidth() {
        return width;
      }
      
      @Override
      public int getHeight() {
        return height;
      }
    };
  }
  
  /**
   * Translates the provided pre-V4 paper color to a {@link StandardColors} instance.
   * 
   * @param oldColor a {@code PreV4PaperColor} value to translate
   * @return the corresponding {@link StandardColors} value, or {@link StandardColors#White}
   *         if {@code oldColor} is {@code null}
   * @since 1.0.42
   */
  public static StandardColors toStandardColor(CDDocument.PreV4PaperColor oldColor) {
    if(oldColor == null) {
      return StandardColors.White;
    }
    switch(oldColor) {
      case BLACK:
        return StandardColors.Black;
      case BLUE:
        return StandardColors.Blue;
      case CYAN:
        return StandardColors.Cyan;
      case CYAN2:
        return StandardColors.Cyan;
      case DKBLUE:
        return StandardColors.DarkBlue;
      case DKCYAN:
        return StandardColors.DarkCyan;
      case DKGREEN:
        return StandardColors.DarkGreen;
      case DKMAGENTA:
        return StandardColors.DarkMagenta;
      case DKRED:
        return StandardColors.DarkRed;
      case DKYELLOW:
        return StandardColors.DarkYellow;
      case GRAY:
        return StandardColors.Gray;
      case GREEN:
        return StandardColors.Green;
      case GREEN2:
        return StandardColors.Green2;
      case LTCYAN:
        // TODO figure out if this translates to something else
        return StandardColors.Cyan;
      case LTGREEN:
        return StandardColors.LightGreen;
      case LTYELLOW:
        return StandardColors.LightYellow;
      case MAGENTA:
        return StandardColors.Magenta;
      case RED:
        return StandardColors.Red;
      case YELLOW:
        return StandardColors.Yellow;
      case YELLOW2:
        return StandardColors.Yellow2;
      case WHITE:
      default:
        return StandardColors.White;
    }
  }
}
