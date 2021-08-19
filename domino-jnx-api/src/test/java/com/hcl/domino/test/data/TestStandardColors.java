package com.hcl.domino.test.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.hcl.domino.data.StandardColors;

public class TestStandardColors {

  @Test
  public void testStandardColorsIndexes() {
    // The enum values of StandardColors are intended to be the integers 0 - 239
    
    // values() should return the values in declaration order
    StandardColors[] colors = StandardColors.values();
    for(int i = 0; i < colors.length; i++) {
      int fi = i;
      assertEquals(i, Byte.toUnsignedInt(colors[i].getValue()), () -> colors[fi] + " should have value " + fi);
    }
  }

}
