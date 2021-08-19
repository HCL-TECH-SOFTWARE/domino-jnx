package it.com.hcl.domino.test.design;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.hcl.domino.richtext.structures.ColorValue;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

public abstract class AbstractDesignTest extends AbstractNotesRuntimeTest {

  protected static void assertColorEquals(ColorValue color, int red, int green, int blue) {
    assertNotNull(color);
    assertEquals(red, color.getRed());
    assertEquals(green, color.getGreen());
    assertEquals(blue, color.getBlue());
  }

}
