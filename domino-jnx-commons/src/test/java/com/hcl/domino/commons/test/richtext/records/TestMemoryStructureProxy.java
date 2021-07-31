package com.hcl.domino.commons.test.richtext.records;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.hcl.domino.richtext.structures.ColorValue;

import com.hcl.domino.commons.richtext.records.MemoryStructureProxy;

public class TestMemoryStructureProxy {

  @Test
  public void testColorValueRoundTrip() {
    ColorValue color = MemoryStructureProxy.newStructure(ColorValue.class, 0);
    color.setRed((short)255);
    assertEquals(255, color.getRed());
  }

}
