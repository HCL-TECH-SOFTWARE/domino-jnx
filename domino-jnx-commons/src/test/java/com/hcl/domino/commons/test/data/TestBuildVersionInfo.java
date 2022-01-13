package com.hcl.domino.commons.test.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.hcl.domino.BuildVersionInfo;
import com.hcl.domino.commons.data.BuildVersionInfoImpl;

public class TestBuildVersionInfo {
  public static class BuildsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return Stream.of(
        Arguments.of(
          new BuildVersionInfoImpl(12, 0, 1, 0, 0, 0, 0),
          true,
          12, 0, 0, 0, 0, 0
        ),
        Arguments.of(
          new BuildVersionInfoImpl(12, 0, 0, 0, 0, 0, 0),
          false,
          12, 0, 1, 0, 0, 0
        ),
        Arguments.of(
          new BuildVersionInfoImpl(11, 2, 0, 0, 0, 0, 0),
          false,
          12, 0, 1, 0, 0, 0
        ),
        Arguments.of(
          new BuildVersionInfoImpl(12, 2, 0, 0, 0, 0, 0),
          true,
          12, 0, 1, 0, 0, 0
        ),
        Arguments.of(
          new BuildVersionInfoImpl(12, 2, 0, 0, 0, 0, 0),
          false,
          12, 2, 1, 0, 0, 0
        ),
        Arguments.of(
          new BuildVersionInfoImpl(12, 2, 0, 0, 0, 0, 0),
          true,
          12, 2, 0, 0, 0, 0
        ),
        Arguments.of(
          new BuildVersionInfoImpl(12, 2, 0, 0, 0, 0, 345),
          true,
          12, 2, 0, 0, 0, 0
        )
      );
    }
    
  }

  @ParameterizedTest
  @ArgumentsSource(BuildsProvider.class)
  public void testBuildVersionInfo(BuildVersionInfo info, boolean expected, int majorVersion, int minorVersion, int qmrNumber, int qmuNumber, int hotfixNumber) {
    assertEquals(expected, info.isAtLeast(majorVersion, minorVersion, qmrNumber, qmuNumber, hotfixNumber));
  }

}
