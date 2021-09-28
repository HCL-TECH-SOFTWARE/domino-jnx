package com.hcl.domino.commons.test.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.hcl.domino.commons.util.DominoUtils;

public class TestDominoUtils {
  
  private static class ArrayProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return Stream.of(
        new Object[] { new byte[] { 1, 2, 3 }, new byte[] { 4, 5, 6 }, new byte[] { 1, 2, 3 } },
        new Object[] { new byte[] { 1, 2, 3, 4 }, new byte[] { 4, 5, 6 }, new byte[] { 1, 2, 3 } },
        new Object[] { new byte[] { 1, 2 }, new byte[] { 4, 5, 6 }, new byte[] { 1, 2, 0 } }
      ).map(Arguments::of);
    }
  }

  @ParameterizedTest
  @ArgumentsSource(ArrayProvider.class)
  public void testOverwriteArray(byte[] source, byte[] dest, byte[] expected) {
    DominoUtils.overwriteArray(source, dest);
    assertArrayEquals(expected, dest);
  }
}
