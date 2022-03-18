/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoException;
import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Formula;
import com.hcl.domino.data.Formula.Disallow;
import com.hcl.domino.data.Formula.FormulaExecutionResult;
import com.hcl.domino.data.FormulaAnalyzeResult.FormulaAttributes;
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

  @Test
  public void testFormulaAnalysis() {
    DominoClient client = getClient();

    assertEquals(
        EnumSet.of(FormulaAttributes.CONSTANT),
        client.createFormula("123").analyze().getAttributes()
        );
    assertEquals(
        EnumSet.of(FormulaAttributes.CONSTANT, FormulaAttributes.FUNC_SIBLINGS),
        client.createFormula("@DocSiblings").analyze().getAttributes()
        );

    assertEquals(
        EnumSet.of(FormulaAttributes.CONSTANT, FormulaAttributes.FUNC_DESCENDANTS),
        client.createFormula("@DocDescendants").analyze().getAttributes()
        );

    assertEquals(
        EnumSet.of(FormulaAttributes.TIME_VARIANT),
        client.createFormula("@Now").analyze().getAttributes()
        );

  }
  
  @Test
  public void testFormulaFunctionsAndCommands() {
    FormulaCompiler compiler = FormulaCompiler.get();
    
    List<String> allFunctions = compiler.getAllFormulaFunctions();
    assertTrue(allFunctions.contains("@Left("));
    assertTrue(!compiler.getFunctionParameters("@Left(").isEmpty());
    
    List<String> allCommands = compiler.getAllFormulaCommands();
    assertTrue(allCommands.contains("MailSend"));
    assertTrue(!compiler.getFunctionParameters("ToolsRunMacro").isEmpty());
  }
  
  @Test
  public void testFormulaExecutionResult() throws Exception {
    withTempDb((db) -> {
      Document doc = db.createDocument();

      DominoClient client = getClient();

      int rndNumber = 534;
      
      {
        //use formula to modify note
        Formula formulaUtilModification = client.createFormula("FIELD anumber:= "+rndNumber+"; FIELD curruser:=@UserName; \"XYZ\"");
        FormulaExecutionResult resultModification = formulaUtilModification.evaluateExt(doc);

        //check if note has been marked as modified
        assertEquals( true, resultModification.isDocModified(), "Formula modified note");

        //check number field value
        long noteNumber = doc.get("anumber", Long.class, 0L);
        assertEquals( rndNumber, noteNumber, "Formula wrote number field");

        //check string field value
        String noteUserName = doc.get("curruser", String.class, "");
        assertEquals(client.getEffectiveUserName(), noteUserName, "Formula wrote current username");

        //check string return value
        assertTrue(resultModification.getValue()!=null && resultModification.getValue().size()==1 && "XYZ".equals(resultModification.getValue().get(0)),
            "Formula returned a string value");
      }
      
      {
        //use formula to select note
        Formula formulaUtilMatch = client.createFormula("SELECT anumber="+rndNumber);
        FormulaExecutionResult resultMatch = formulaUtilMatch.evaluateExt(doc);

        assertEquals( true, resultMatch.matchesFormula(), "Note matches formula");
      }
      
      {
        //use formula to mark note to be deleted
        Formula formulaDeletion = client.createFormula("@DeleteDocument");
        FormulaExecutionResult resultDeletion = formulaDeletion.evaluateExt(doc);
        assertEquals( true, resultDeletion.shouldBeDeleted(), "Formula marked doc for deletion");
      }
      
    });
  }
  
  @Test
  public void testDisallowedActions() throws Exception {
    withTempDb((db) -> {
      DominoClient client = getClient();
      Document otherDoc = db.createDocument();
      otherDoc.replaceItemValue("Form", "Testform");
      otherDoc.save();
      String otherDocUnid = otherDoc.getUNID();
      otherDoc.autoClosable().close();
      
      Document doc = db.createDocument();

      //check if we can prevent Notes.ini changes
      {
        client.getDominoRuntime().setProperty("$jnx_test", "");

        Formula formula = client
            .createFormula("@SetEnvironment(\"jnx_test\"; \"123\")");
        formula.evaluate();
        //this is allowed, to the value should have been set
        String val = client.getDominoRuntime().getPropertyString("$jnx_test");
        assertEquals("123", val);
      }
      
      {
        client.getDominoRuntime().setProperty("$jnx_test", "");
        
        DominoException e = null;
        try {
          Formula formula = client
              .createFormula("@SetEnvironment(\"jnx_test\"; \"123\")")
              .disallow(Disallow.SETENVIRONMENT);
          formula.evaluate();
        }
        catch (DominoException e1) {
          e = e1;
        }

        assertNotNull(e);
        assertTrue(e.getId() == INotesErrorConstants.ERR_NSF_COMPUTE_NOENVIRONMENT);
      }

      {
        client.getDominoRuntime().setProperty("$jnx_test", "");
        
        DominoException e = null;
        try {
          Formula formula = client
              .createFormula("@SetEnvironment(\"jnx_test\"; \"123\")")
              .disallow(Disallow.UNSAFE);
          formula.evaluate();
        }
        catch (DominoException e1) {
          e = e1;
        }

        assertNotNull(e);
        assertTrue(e.getId() == INotesErrorConstants.ERR_NSF_COMPUTE_NOENVIRONMENT);
      }

      //check if FIELD fieldname:= can be prevented
      {
        doc.replaceItemValue("field1", "");
        
        Formula formula = client
            .createFormula("FIELD field1:=\"123\"; \"\"");
        formula.evaluate(doc);

        String fldValue = doc.get("field1", String.class, "");
        assertEquals("123", fldValue);
      }
      
      {
        doc.replaceItemValue("field1", "");
        
        Formula formula = client
            .createFormula("FIELD field1:=\"123\"; \"\"")
            .disallow(Disallow.ASSIGN);
        formula.evaluate(doc);

        String fldValue = doc.get("field1", String.class, "");
        assertEquals("", fldValue);
      }

      {
        doc.replaceItemValue("field1", "");
        
        Formula formula = client
            .createFormula("FIELD field1:=\"123\"; \"\"")
            .disallow(Disallow.UNSAFE);
        formula.evaluate(doc);

        String fldValue = doc.get("field1", String.class, "");
        assertEquals("", fldValue);
      }
      
      {
        doc.replaceItemValue("field1", "");
        
        Formula formula = client
            .createFormula("@SetField(\"field1\"; \"123\")")
            .disallow(Disallow.UNSAFE);
        formula.evaluate(doc);

        String fldValue = doc.get("field1", String.class, "");
        assertEquals("", fldValue);
      }
      
      {
        Formula formula = client
            .createFormula("@GetDocField(\""+otherDocUnid+"\";\"form\")");
        List<Object> result = formula.evaluate(doc);
        assertEquals(Arrays.asList("Testform"), result);
      }
      
      //FALLBACK_EXT seems to block retrieval of the doc field
      {
        Formula formula = client
            .createFormula("@GetDocField(\""+otherDocUnid+"\";\"form\")")
            .disallow(Disallow.FALLBACK_EXT);
        List<Object> result = formula.evaluate(doc);
        assertEquals(Arrays.asList(""), result);
      }

      //check if we can prevent @SetDocField
      {
        Formula formula = client
            .createFormula("@SetDocField(\""+otherDocUnid+"\"; \"field1\"; \"123\")");
        formula.evaluate(doc);
        //we tried to load the doc to see if field1 has been set, but that
        //was not the case; not sure why this is not working; but setting one of the
        //Disallow constants below does even more, it throws an exception
      }
      
      {
        DominoException e = null;
        try {
          Formula formula = client
              .createFormula("@SetDocField(\""+otherDocUnid+"\"; \"field1\"; \"123\")")
                      .disallow(Disallow.UNSAFE);
          formula.evaluate(doc);
        } catch (DominoException e1) {
          e = e1;
        }

        assertNotNull(e);
        assertTrue(e.getId() == INotesErrorConstants.ERR_FUNCTION_CONTEXT);
      }

      {
        DominoException e = null;
        try {
          Formula formula = client
              .createFormula("@SetDocField(\""+otherDocUnid+"\"; \"field1\"; \"123\")")
                      .disallow(Disallow.SIDEEFFECTS);
          formula.evaluate(doc);
        } catch (DominoException e1) {
          e = e1;
        }

        assertNotNull(e);
        assertTrue(e.getId() == INotesErrorConstants.ERR_FUNCTION_CONTEXT);
      }

    });
    
  }
}
