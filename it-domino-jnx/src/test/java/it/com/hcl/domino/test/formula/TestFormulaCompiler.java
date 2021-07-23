/*
 * ==========================================================================
 * Copyright (C) 2019-2021 HCL America, Inc. ( http://www.hcl.com/ )
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
package it.com.hcl.domino.test.formula;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.hcl.domino.formula.FormulaCompiler;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestFormulaCompiler extends AbstractNotesRuntimeTest {
  public static class FormulasProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception {
      return Stream.of(
          "Hello",
          "Index>1")
          .map(Arguments::of);
    }
  }

  @ParameterizedTest
  @ArgumentsSource(FormulasProvider.class)
  public void testRoundTrip(final String formula) {
    final FormulaCompiler compiler = FormulaCompiler.get();

    final byte[] compiled = compiler.compile(formula);
    Assertions.assertNotNull(compiled);
    Assertions.assertFalse(compiled.length == 0);

    final String decompiled = compiler.decompile(compiled);
    Assertions.assertNotNull(decompiled);
    Assertions.assertEquals(formula, decompiled);
  }

  @Test
  public void testServiceAvailable() {
    Assertions.assertNotNull(FormulaCompiler.get());
  }
}
