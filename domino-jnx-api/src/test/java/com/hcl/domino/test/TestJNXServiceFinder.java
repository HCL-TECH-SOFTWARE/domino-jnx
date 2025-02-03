package com.hcl.domino.test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.util.NoSuchElementException;
import com.hcl.domino.misc.JNXServiceFinder;
import org.junit.jupiter.api.Test;

public class TestJNXServiceFinder {
  /**
   * Ensures that the exception message includes the name
   * of the class it attempted to find
   */
  @Test
  public void testNoElement() {
    try {
      JNXServiceFinder.findRequiredService(TestJNXServiceFinder.class, Thread.currentThread().getContextClassLoader());
      fail("Should not have found the dummy service");
    } catch(NoSuchElementException e) {
      assertTrue(String.valueOf(e.getLocalizedMessage()).contains(TestJNXServiceFinder.class.getName()), "Exception should include the class name");
    }
  }
}
