package com.hcl.domino.commons.design;

import java.util.EnumSet;

import com.hcl.domino.commons.richtext.records.MemoryStructureProxy;
import com.hcl.domino.data.FontAttribute;
import com.hcl.domino.data.StandardFonts;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.FontStyle;

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
    ColorValue result = MemoryStructureProxy.newStructure(ColorValue.class, 0);
    result.setFlags(EnumSet.of(ColorValue.Flag.NOCOLOR));
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
    ColorValue result = MemoryStructureProxy.newStructure(ColorValue.class, 0);
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
    ColorValue result = MemoryStructureProxy.newStructure(ColorValue.class, 0);
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
    ColorValue result = MemoryStructureProxy.newStructure(ColorValue.class, 0);
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
    ColorValue result = MemoryStructureProxy.newStructure(ColorValue.class, 0);
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
    ColorValue result = MemoryStructureProxy.newStructure(ColorValue.class, 0);
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
    FontStyle result = MemoryStructureProxy.newStructure(FontStyle.class, 0);
    result.setStandardFont(StandardFonts.SWISS);
    result.setPointSize(9);
    result.setAttributes(EnumSet.of(FontAttribute.BOLD));
    return result;
  }
}
