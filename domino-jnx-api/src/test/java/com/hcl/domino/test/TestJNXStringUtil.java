package com.hcl.domino.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import com.hcl.domino.util.JNXStringUtil;

public class TestJNXStringUtil {
  @ParameterizedTest
  @CsvSource(delimiterString = " - ", value = {
    "00000000000000000000000000000000 - 0 - 0",
    "00000000000000010000000000000001 - 1 - 1"
  })
  public void testToUnid(String expected, long innardsFile, long innardsNote) {
    Assertions.assertEquals(expected, JNXStringUtil.toUNID(innardsFile, innardsNote));
  }
}
