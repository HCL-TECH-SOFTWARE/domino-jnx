package com.hcl.domino.commons.test.richtext.records;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.richtext.structures.ColorValue;

public class TestMemoryStructureProxy {

  @Test
  public void testColorValueRoundTrip() {
    ColorValue color = MemoryStructureUtil.newStructure(ColorValue.class, 0);
    color.setRed((short)255);
    assertEquals(255, color.getRed());
  }

}
