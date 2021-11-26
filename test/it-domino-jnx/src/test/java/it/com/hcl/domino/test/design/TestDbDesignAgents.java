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
package it.com.hcl.domino.test.design;

import static it.com.hcl.domino.test.util.ITUtil.toLf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.design.ComputableValue;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.DesignAgent;
import com.hcl.domino.design.DesignElement;
import com.hcl.domino.design.View;
import com.hcl.domino.design.agent.AgentInterval;
import com.hcl.domino.design.agent.AgentTarget;
import com.hcl.domino.design.agent.AgentTrigger;
import com.hcl.domino.design.agent.DesignFormulaAgent;
import com.hcl.domino.design.agent.DesignJavaAgent;
import com.hcl.domino.design.agent.DesignLotusScriptAgent;
import com.hcl.domino.design.agent.DesignSimpleActionAgent;
import com.hcl.domino.design.simpleaction.CopyToDatabaseAction;
import com.hcl.domino.design.simpleaction.DeleteDocumentAction;
import com.hcl.domino.design.simpleaction.FolderBasedAction;
import com.hcl.domino.design.simpleaction.ModifyByFormAction;
import com.hcl.domino.design.simpleaction.ModifyFieldAction;
import com.hcl.domino.design.simpleaction.ReadMarksAction;
import com.hcl.domino.design.simpleaction.ReplyAction;
import com.hcl.domino.design.simpleaction.RunAgentAction;
import com.hcl.domino.design.simpleaction.RunFormulaAction;
import com.hcl.domino.design.simpleaction.RunFormulaAction.DocumentAction;
import com.hcl.domino.design.simpleaction.SendDocumentAction;
import com.hcl.domino.design.simpleaction.SendMailAction;
import com.hcl.domino.design.simpleaction.SendNewsletterAction;
import com.hcl.domino.design.simpleaction.SimpleAction;
import com.hcl.domino.design.simplesearch.ByAuthorTerm;
import com.hcl.domino.design.simplesearch.ByDateFieldTerm;
import com.hcl.domino.design.simplesearch.ByFieldTerm;
import com.hcl.domino.design.simplesearch.ByFolderTerm;
import com.hcl.domino.design.simplesearch.ByFormTerm;
import com.hcl.domino.design.simplesearch.ByNumberFieldTerm;
import com.hcl.domino.design.simplesearch.ExampleFormTerm;
import com.hcl.domino.design.simplesearch.SimpleSearchTerm;
import com.hcl.domino.design.simplesearch.TextTerm;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.Pair;
import com.ibm.commons.util.io.StreamUtil;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDbDesignAgents extends AbstractNotesRuntimeTest {
  public static final int EXPECTED_IMPORT_AGENTS = 25;
  private static String dbPath;

  @AfterAll
  public static void termDesignDb() {
    try {
      Files.deleteIfExists(Paths.get(TestDbDesignAgents.dbPath));
    } catch (final Throwable t) {
      System.err.println("Unable to delete database " + TestDbDesignAgents.dbPath + ": " + t);
    }
  }

  private Database database;

  @BeforeEach
  public void initDesignDb() throws IOException, URISyntaxException {
    if (this.database == null) {
      final DominoClient client = this.getClient();
      if (TestDbDesignAgents.dbPath == null) {
        this.database = AbstractNotesRuntimeTest.createTempDb(client);
        TestDbDesignAgents.dbPath = this.database.getAbsoluteFilePath();
        AbstractNotesRuntimeTest.populateResourceDxl("/dxl/testDbDesignAgents", this.database);
      } else {
        this.database = client.openDatabase("", TestDbDesignAgents.dbPath);
      }
    }
  }

  @Test
  public void testAgentsCount() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<DesignAgent> agents = dbDesign.getAgents().collect(Collectors.toList());
    assertEquals(EXPECTED_IMPORT_AGENTS, agents.size());

    assertNull(dbDesign.getAgent("Content").orElse(null));
  }

  @Test
  public void testCreateAgent() throws Exception {
    this.withTempDb(database -> {
      final DbDesign design = database.getDesign();
      final DesignAgent element = design.createAgent(DesignJavaAgent.class, "foo bar");
      assertNotNull(element);
      assertEquals("foo bar", element.getTitle());

      assertNull(design.getAgent("foo bar").orElse(null));
      element.save();
      assertNotNull(design.getAgent("foo bar").orElse(null));
      element.setTitle("other title");
      element.save();
      assertNull(design.getAgent("foo bar").orElse(null));
      assertNotNull(design.getAgent("other title").orElse(null));
    });
  }

  @Test
  public void testFormulaAgent() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<DesignAgent> agents = dbDesign.getAgents().collect(Collectors.toList());
    final DesignAgent agent = agents.stream().filter(a -> "formula agent".equals(a.getTitle())).findFirst().orElse(null);
    assertNotNull(agent);
    
    assertInstanceOf(DesignFormulaAgent.class, agent);
    DesignFormulaAgent formulaAgent = (DesignFormulaAgent) agent;

    assertNotNull(dbDesign.getAgent("formula agent"));
    assertEquals(1, dbDesign.getDesignElementsByName(DesignAgent.class, "formula agent").count());
    assertNotNull(dbDesign.getDesignElementByName(DesignAgent.class, "formula agent"));

    final String formula = "@StatusBar(\"hey\");\n"
        + " @All";
    assertEquals(toLf(formula), toLf(formulaAgent.getFormula().get()));
    assertEquals(DocumentAction.MODIFY, formulaAgent.getDocumentAction().get());
    assertTrue(agent.isRunInBackgroundInClient());
  }

  @Test
  public void testFormulaQuery() {
    final DbDesign dbDesign = this.database.getDesign();
    {
      final List<DesignElement> elements = dbDesign.queryDesignElements("$TITLE='formula agent'").collect(Collectors.toList());
      assertEquals(1, elements.size());
      assertTrue(elements.get(0) instanceof DesignAgent);
      assertEquals("formula agent", ((DesignAgent) elements.get(0)).getTitle());
    }
    {
      final List<DesignElement> elements = dbDesign.queryDesignElements(DesignAgent.class, "$TITLE='formula agent'")
          .collect(Collectors.toList());
      assertEquals(1, elements.size());
      assertTrue(elements.get(0) instanceof DesignAgent);
      assertEquals("formula agent", ((DesignAgent) elements.get(0)).getTitle());
    }
    {
      final List<DesignElement> elements = dbDesign.queryDesignElements(View.class, "$TITLE='formula agent'")
          .collect(Collectors.toList());
      assertEquals(0, elements.size());
    }
  }

  @Test
  public void testLargeLotusScriptAgent() throws IOException {
    final DbDesign dbDesign = this.database.getDesign();
    final DesignAgent agent = dbDesign.getAgent("Large LotusScript Agent").orElse(null);
    assertNotNull(agent);

    assertInstanceOf(DesignLotusScriptAgent.class, agent);
    DesignLotusScriptAgent lsAgent = (DesignLotusScriptAgent) agent;

    assertEquals(AgentTrigger.NEWMAIL, agent.getTrigger());
    assertFalse(agent.getStartDate().isPresent());
    assertFalse(agent.getEndDate().isPresent());

    String largels;
    try (InputStream is = this.getClass().getResourceAsStream("/text/largels.txt")) {
      largels = StreamUtil.readString(is);
    }
    assertEquals(toLf(largels), toLf(lsAgent.getScript()));
  }

  @Test
  public void testLSAgent() throws IOException {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<DesignAgent> agents = dbDesign.getAgents().collect(Collectors.toList());
    final DesignAgent agent = agents.stream().filter(a -> "Printer Agent".equals(a.getTitle())).findFirst().orElse(null);
    assertNotNull(agent);

    assertInstanceOf(DesignLotusScriptAgent.class, agent);
    DesignLotusScriptAgent lsAgent = (DesignLotusScriptAgent) agent;

    assertNotNull(dbDesign.getAgent("Printer Agent"));
    String largels;
    try (InputStream is = this.getClass().getResourceAsStream("/text/printeragent.txt")) {
      largels = StreamUtil.readString(is);
    }
	assertEquals(toLf(largels), toLf(lsAgent.getScript()));
  }

  @Test
  public void testNewDocsFormula() {
    final DbDesign dbDesign = this.database.getDesign();
    final DesignAgent agent = dbDesign.getAgent("new docs formula").orElse(null);
    assertNotNull(agent);

    assertInstanceOf(DesignFormulaAgent.class, agent);
    DesignFormulaAgent formulaAgent = (DesignFormulaAgent) agent;

    assertEquals(toLf("\"bar\";\n\n @All"), toLf(formulaAgent.getFormula().get()));
    assertEquals(DocumentAction.CREATE, formulaAgent.getDocumentAction().get());
  }

  @Test
  public void testNonformulaSimpleAction() throws IOException {
    final DbDesign dbDesign = this.database.getDesign();
    final DesignAgent agent = dbDesign.getAgent("sa nonformula").orElse(null);
    assertNotNull(agent);

    assertInstanceOf(DesignSimpleActionAgent.class, agent);
  }

  @Test
  public void testScheduledDisabledFormula() {
    final DbDesign dbDesign = this.database.getDesign();
    final DesignAgent agent = dbDesign.getAgent("Scheduled Weekly Disabled").orElse(null);
    assertNotNull(agent);

    assertInstanceOf(DesignFormulaAgent.class, agent);

    assertEquals(AgentTrigger.SCHEDULED, agent.getTrigger());
    assertEquals(AgentInterval.WEEK, agent.getIntervalType());
    assertFalse(agent.getStartDate().isPresent());
    assertTrue(agent.getEndDate().isPresent());
    assertEquals(LocalDate.of(2022, 6, 14), agent.getEndDate().get().toLocalDate());
    assertEquals("CN=Arcturus/O=Frost", agent.getRunLocation());
    assertEquals(LocalTime.of(2, 0), agent.getRunLocalTime().get());
    assertEquals(DayOfWeek.TUESDAY, agent.getRunDayOfWeek().get());
    assertFalse(agent.getRunEndLocalTime().isPresent());
  }

  @Test
  public void testSelectDocsFormula() {
    final DbDesign dbDesign = this.database.getDesign();
    final DesignAgent agent = dbDesign.getAgent("select docs formula").orElse(null);
    assertNotNull(agent);

    assertInstanceOf(DesignFormulaAgent.class, agent);
    DesignFormulaAgent formulaAgent = (DesignFormulaAgent) agent;
    
    assertEquals(toLf("\"foo\";\n\n @All"), toLf(formulaAgent.getFormula().get()));
    assertEquals(DocumentAction.SELECT, formulaAgent.getDocumentAction().get());
  }

  @Test
  public void testSelectServerFormula() {
    final DbDesign dbDesign = this.database.getDesign();
    final DesignAgent agent = dbDesign.getAgent("Select Server").orElse(null);
    assertNotNull(agent);

    assertInstanceOf(DesignFormulaAgent.class, agent);

    assertEquals(AgentTrigger.SCHEDULED, agent.getTrigger());
    assertEquals(AgentInterval.MONTH, agent.getIntervalType());
    assertFalse(agent.getStartDate().isPresent());
    assertFalse(agent.getEndDate().isPresent());
    assertEquals("", agent.getRunLocation());
    assertEquals(12, agent.getRunDayOfMonth().getAsInt());
    assertEquals(LocalTime.of(4, 0), agent.getRunLocalTime().get());
    assertFalse(agent.getRunEndLocalTime().isPresent());
  }

  @Test
  public void testSimpleActionAgent() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<DesignAgent> agents = dbDesign.getAgents().collect(Collectors.toList());
    final DesignAgent agent = agents.stream().filter(a -> "sa agent".equals(a.getTitle())).findFirst().orElse(null);
    assertNotNull(agent);

    assertInstanceOf(DesignSimpleActionAgent.class, agent);
    DesignSimpleActionAgent simpleActionAgent = (DesignSimpleActionAgent) agent;
    
    assertNotNull(dbDesign.getAgent("sa agent"));
    final List<SimpleAction> actions = simpleActionAgent.getActions();
    assertEquals(19, actions.size());
    {
      assertInstanceOf(RunFormulaAction.class, actions.get(0));
      final RunFormulaAction action = (RunFormulaAction) actions.get(0);
      assertEquals(DocumentAction.MODIFY, action.getDocumentAction().get());
      assertEquals("\"foo\"", action.getFormula().get());
    }
    {
      assertInstanceOf(ModifyFieldAction.class, actions.get(1));
      final ModifyFieldAction action = (ModifyFieldAction) actions.get(1);
      assertEquals("Body", action.getFieldName());
      assertEquals("dfdf", action.getValue());
    }
    {
      assertInstanceOf(FolderBasedAction.class, actions.get(2));
      final FolderBasedAction action = (FolderBasedAction) actions.get(2);
      assertEquals(FolderBasedAction.Type.REMOVE, action.getType());
    }
    {
      assertInstanceOf(CopyToDatabaseAction.class, actions.get(3));
      final CopyToDatabaseAction action = (CopyToDatabaseAction) actions.get(3);
      assertEquals("CN=Arcturus/O=Frost", action.getServerName());
      assertEquals("names.nsf", action.getDatabaseName());
    }
    {
      assertInstanceOf(FolderBasedAction.class, actions.get(4));
      final FolderBasedAction action = (FolderBasedAction) actions.get(4);
      assertEquals("test folder", action.getFolderName());
    }
    {
      assertInstanceOf(DeleteDocumentAction.class, actions.get(5));
      final DeleteDocumentAction action = (DeleteDocumentAction) actions.get(5);
      assertNotNull(action);
    }
    {
      assertInstanceOf(ReadMarksAction.class, actions.get(6));
      final ReadMarksAction action = (ReadMarksAction) actions.get(6);
      assertEquals(ReadMarksAction.Type.MARK_READ, action.getType());
    }
    {
      assertInstanceOf(ReadMarksAction.class, actions.get(7));
      final ReadMarksAction action = (ReadMarksAction) actions.get(7);
      assertEquals(ReadMarksAction.Type.MARK_UNREAD, action.getType());
    }
    {
      assertInstanceOf(ModifyByFormAction.class, actions.get(8));
      final ModifyByFormAction action = (ModifyByFormAction) actions.get(8);
      assertEquals("Alias", action.getFormName());
      final Map<String, List<String>> modifications = action.getModifications();
      assertEquals(10, modifications.size()); // 10 total fields on the form
      assertEquals(Arrays.asList("foo"), modifications.get("Host"));
      assertEquals(Arrays.asList("bar"), modifications.get("From"));
      assertEquals(Arrays.asList("baz"), modifications.get("To"));
    }
    {
      assertInstanceOf(FolderBasedAction.class, actions.get(9));
      final FolderBasedAction action = (FolderBasedAction) actions.get(9);
      assertEquals(FolderBasedAction.Type.MOVE, action.getType());
      assertEquals("test folder", action.getFolderName());
    }
    {
      assertInstanceOf(ReplyAction.class, actions.get(10));
      final ReplyAction action = (ReplyAction) actions.get(10);
      assertFalse(action.isReplyToAll());
      assertFalse(action.isReplyOnce());
      assertEquals("\"hey sender\"", action.getBody());
      assertTrue(action.isIncludeDocument());
    }
    {
      assertInstanceOf(ReplyAction.class, actions.get(11));
      final ReplyAction action = (ReplyAction) actions.get(11);
      assertTrue(action.isReplyToAll());
      assertTrue(action.isReplyOnce());
      assertEquals("\"no copy in this one\"", action.getBody());
      assertFalse(action.isIncludeDocument());
    }
    {
      assertInstanceOf(RunAgentAction.class, actions.get(12));
      final RunAgentAction action = (RunAgentAction) actions.get(12);
      assertEquals("Compiler", action.getAgentName());
    }
    {
      assertInstanceOf(SendDocumentAction.class, actions.get(13));
      final SendDocumentAction action = (SendDocumentAction) actions.get(13);
      assertNotNull(action);
    }
    {
      assertInstanceOf(SendMailAction.class, actions.get(14));
      final SendMailAction action = (SendMailAction) actions.get(14);
      final ComputableValue to = action.getTo();
      assertFalse(to.isFormula());
      assertEquals("foo@foo.com", to.getValue());
      assertEquals("bar@bar.com", action.getCc().getValue());
      final ComputableValue bcc = action.getBcc();
      assertTrue(bcc.isFormula());
      assertEquals("\"bcc@\" + \"bcc.com\"", bcc.getValue());
      assertEquals("Test sending", action.getSubject().getValue());
      assertEquals("this is test body", action.getBody());
      assertTrue(action.isIncludeDocument());
      assertTrue(action.isIncludeDocLink());
    }
    {
      assertInstanceOf(SendNewsletterAction.class, actions.get(15));
      final SendNewsletterAction action = (SendNewsletterAction) actions.get(15);
      assertEquals("foo@foo.com", action.getTo());
      assertEquals("sdfdf", action.getSubject());
      assertEquals("rwViewNameLen  Length, in bytes, of the name of the view used to display the data\r\n"
          + "wSpare		Reserved;  must be 0.", action.getBody());
      assertEquals("Aliases", action.getViewName());
      assertTrue(action.isGatherDocuments());
      assertEquals(2, action.getGatherThreshold());
    }
    {
      assertInstanceOf(SendNewsletterAction.class, actions.get(16));
      final SendNewsletterAction action = (SendNewsletterAction) actions.get(16);
      assertEquals("foo@foo.com", action.getTo());
      assertEquals("sdfdf", action.getSubject());
      assertEquals("this is the newsletter", action.getBody());
      assertEquals("Aliases", action.getViewName());
      assertTrue(action.isGatherDocuments());
      assertEquals(2, action.getGatherThreshold());
    }
    {
      assertInstanceOf(SendNewsletterAction.class, actions.get(17));
      final SendNewsletterAction action = (SendNewsletterAction) actions.get(17);
      assertEquals("foo@foo.com", action.getTo());
      assertEquals("sdfdf", action.getSubject());
      assertEquals("this is the newsletters", action.getBody());
      assertEquals("Aliases", action.getViewName());
      assertTrue(action.isGatherDocuments());
      assertEquals(2, action.getGatherThreshold());
    }
    {
      assertInstanceOf(SendNewsletterAction.class, actions.get(18));
      final SendNewsletterAction action = (SendNewsletterAction) actions.get(18);
      assertEquals("foo@foo.com", action.getTo());
      assertEquals("sdfdf", action.getSubject());
      assertEquals("", action.getBody());
      assertFalse(action.isIncludeSummary());
      assertTrue(action.isGatherDocuments());
      assertEquals(2, action.getGatherThreshold());
    }
  }

  @ParameterizedTest
  @EnumSource(DayOfWeek.class)
  public void testWeekdayAgents(final DayOfWeek dayOfWeek) {
    final DbDesign dbDesign = this.database.getDesign();
    // Also tests "Scheduled Weekly Disabled"'s "Scheduled Weekly Tuesday" alias and
    // case-insensitive lookups
    final DesignAgent agent = dbDesign.getAgent("Scheduled Weekly " + dayOfWeek).orElse(null);
    assertNotNull(agent);

    assertEquals(AgentTrigger.SCHEDULED, agent.getTrigger());
    assertEquals(AgentInterval.WEEK, agent.getIntervalType());
    assertFalse(agent.getStartDate().isPresent());
    assertTrue(agent.getEndDate().isPresent());
    assertEquals(dayOfWeek, agent.getRunDayOfWeek().get());
    assertFalse(agent.getRunEndLocalTime().isPresent());
  }
  
  @Test
  public void testCreateDocAgent() {
    DbDesign design = database.getDesign();
    
    DesignAgent agent = design.getAgent("Create Doc").get();
    
    assertTrue(agent.isAllowRemoteDebugging());
    assertTrue(agent.isPrivate());
    assertTrue(agent.isStoreSearch());
    assertTrue(agent.isStoreHighlights());
    assertTrue(agent.isProfilingEnabled());
    assertEquals(DesignAgent.SecurityLevel.UNRESTRICTED_FULLADMIN, agent.getSecurityLevel());
  }
  
  @Test
  public void testSelectionQuery() {
    DbDesign design = database.getDesign();
    
    DesignAgent agent = design.getAgent("Test Selection").get();
    
    List<? extends SimpleSearchTerm> search = agent.getDocumentSelection();
    assertEquals(23, search.size());
    
    {
      SimpleSearchTerm term = search.get(0);
      TextTerm text = assertInstanceOf(TextTerm.class, term);
      assertEquals(TextTerm.Type.ACCRUE, text.getType());
      assertEquals(Arrays.asList("dfdf", "b"), text.getValues());
    }
    {
      SimpleSearchTerm term = search.get(1);
      TextTerm text = assertInstanceOf(TextTerm.class, term);
      assertEquals(TextTerm.Type.PLAIN, text.getType());
      assertEquals(Arrays.asList("AND"), text.getValues());
    }
    {
      SimpleSearchTerm term = search.get(2);
      ExampleFormTerm form = assertInstanceOf(ExampleFormTerm.class, term);
      assertEquals("Some Form", form.getFormName());
      Map<String, List<String>> expected = new HashMap<>();
      expected.put("Name", Arrays.asList("fsdf"));
      assertEquals(expected, form.getFieldMatches());
    }
    {
      SimpleSearchTerm term = search.get(3);
      TextTerm text = assertInstanceOf(TextTerm.class, term);
      assertEquals(TextTerm.Type.PLAIN, text.getType());
      assertEquals(Arrays.asList("OR"), text.getValues());
    }
    {
      SimpleSearchTerm term = search.get(4);
      ByFormTerm form = assertInstanceOf(ByFormTerm.class, term);
      assertEquals(new LinkedHashSet<>(Arrays.asList("Some Form", "Some Other Form")), form.getFormNames());
    }
    {
      SimpleSearchTerm term = search.get(5);
      TextTerm text = assertInstanceOf(TextTerm.class, term);
      assertEquals(TextTerm.Type.PLAIN, text.getType());
      assertEquals(Arrays.asList("AND NOT ("), text.getValues());
    }
    {
      SimpleSearchTerm term = search.get(6);
      ByDateFieldTerm date = assertInstanceOf(ByDateFieldTerm.class, term);
      assertEquals(ByDateFieldTerm.DateType.CREATED, date.getDateType());
      assertEquals(LocalDate.of(2021, 9, 7), date.getDate().get().toLocalDate());
      assertFalse(date.getDateRange().isPresent());
    }
    {
      SimpleSearchTerm term = search.get(7);
      TextTerm text = assertInstanceOf(TextTerm.class, term);
      assertEquals(TextTerm.Type.PLAIN, text.getType());
      assertEquals(Arrays.asList("OR"), text.getValues());
    }
    {
      SimpleSearchTerm term = search.get(8);
      ByAuthorTerm author = assertInstanceOf(ByAuthorTerm.class, term);
      assertEquals(ByFieldTerm.TextRule.CONTAINS, author.getTextRule());
      assertEquals(NotesConstants.FIELD_UPDATED_BY, author.getFieldName());
      assertEquals("fsdf", author.getTextValue());
    }
    {
      SimpleSearchTerm term = search.get(9);
      TextTerm text = assertInstanceOf(TextTerm.class, term);
      assertEquals(TextTerm.Type.PLAIN, text.getType());
      assertEquals(Arrays.asList(")"), text.getValues());
    }
    {
      SimpleSearchTerm term = search.get(10);
      ByFieldTerm field = assertInstanceOf(ByFieldTerm.class, term);
      assertEquals(ByFieldTerm.TextRule.CONTAINS, field.getTextRule());
      assertEquals("Name", field.getFieldName());
      assertEquals("dfd", field.getTextValue());
    }
    {
      SimpleSearchTerm term = search.get(11);
      TextTerm text = assertInstanceOf(TextTerm.class, term);
      assertEquals(TextTerm.Type.PLAIN, text.getType());
      assertEquals(Arrays.asList("AND"), text.getValues());
    }
    {
      SimpleSearchTerm term = search.get(12);
      ByFolderTerm folder = assertInstanceOf(ByFolderTerm.class, term);
      assertEquals("Some Folder", folder.getFolderName());
      assertFalse(folder.isPrivate());
    }
    {
      SimpleSearchTerm term = search.get(13);
      TextTerm text = assertInstanceOf(TextTerm.class, term);
      assertEquals(TextTerm.Type.PLAIN, text.getType());
      assertEquals(Arrays.asList("and some arbitrary text"), text.getValues());
    }
    {
      SimpleSearchTerm term = search.get(14);
      TextTerm text = assertInstanceOf(TextTerm.class, term);
      assertEquals(TextTerm.Type.AND, text.getType());
      assertEquals(Arrays.asList("aab", "bbc"), text.getValues());
    }
    {
      SimpleSearchTerm term = search.get(15);
      TextTerm text = assertInstanceOf(TextTerm.class, term);
      assertEquals(TextTerm.Type.PLAIN, text.getType());
      assertEquals(Arrays.asList("AND"), text.getValues());
    }
    {
      SimpleSearchTerm term = search.get(16);
      ByNumberFieldTerm number = assertInstanceOf(ByNumberFieldTerm.class, term);
      assertEquals(ByNumberFieldTerm.NumberRule.LESS_THAN, number.getNumberRule());
      assertEquals("Number", number.getFieldName());
      assertEquals(4d, number.getNumber().getAsDouble());
      assertFalse(number.getNumberRange().isPresent());
    }
    {
      SimpleSearchTerm term = search.get(17);
      TextTerm text = assertInstanceOf(TextTerm.class, term);
      assertEquals(TextTerm.Type.PLAIN, text.getType());
      assertEquals(Arrays.asList("AND"), text.getValues());
    }
    {
      SimpleSearchTerm term = search.get(18);
      ByNumberFieldTerm number = assertInstanceOf(ByNumberFieldTerm.class, term);
      assertEquals(ByNumberFieldTerm.NumberRule.NOT_BETWEEN, number.getNumberRule());
      assertEquals("Number", number.getFieldName());
      assertFalse(number.getNumber().isPresent());
      Pair<Double, Double> range = number.getNumberRange().get();
      assertEquals(3, range.getValue1());
      assertEquals(6, range.getValue2());
    }
    {
      SimpleSearchTerm term = search.get(19);
      TextTerm text = assertInstanceOf(TextTerm.class, term);
      assertEquals(TextTerm.Type.PLAIN, text.getType());
      assertEquals(Arrays.asList("AND"), text.getValues());
    }
    {
      SimpleSearchTerm term = search.get(20);
      ByDateFieldTerm date = assertInstanceOf(ByDateFieldTerm.class, term);
      assertEquals(ByDateFieldTerm.DateType.MODIFIED, date.getDateType());
      assertFalse(date.getDate().isPresent());
      LocalDate start = LocalDate.of(2021, 9, 10);
      LocalDate end = LocalDate.of(2021, 9, 30);
      DominoDateRange range = date.getDateRange().get();
      assertEquals(start, range.getStartDateTime().toLocalDate());
      assertEquals(end, range.getEndDateTime().toLocalDate());
    }
    {
      SimpleSearchTerm term = search.get(21);
      TextTerm text = assertInstanceOf(TextTerm.class, term);
      assertEquals(TextTerm.Type.PLAIN, text.getType());
      assertEquals(Arrays.asList("AND"), text.getValues());
    }
    {
      SimpleSearchTerm term = search.get(22);
      ByDateFieldTerm date = assertInstanceOf(ByDateFieldTerm.class, term);
      assertEquals(ByDateFieldTerm.DateType.FIELD, date.getDateType());
      assertFalse(date.getDate().isPresent());
      assertFalse(date.getDateRange().isPresent());
      assertEquals(7, date.getDayCount().getAsInt());
    }
  }
  
  @Test
  public void testUnlockDocument() {
    DbDesign design = database.getDesign();
    
    DesignAgent agent = design.getAgent("Unlock Document").orElse(null);
    assertNotNull(agent);
    
    assertInstanceOf(DesignFormulaAgent.class, agent);
    DesignFormulaAgent formulaAgent = (DesignFormulaAgent) agent;
    assertEquals(AgentTrigger.MANUAL, agent.getTrigger());
    assertEquals(AgentTarget.SELECTED, agent.getTarget());
    
    assertEquals(toLf("FIELD Locked := @DeleteField;\n"
        + "FIELD DocumentAuthors := @Trim(@Replace(From : CurrentReviewers; \"None\"; \"\"));@All"), toLf(formulaAgent.getFormula().get()));
    assertEquals(DesignFormulaAgent.DocumentAction.MODIFY, formulaAgent.getDocumentAction().get());
    
  }
  
  @Test
  public void testReleaseDeadMessages() {
    DbDesign design = database.getDesign();
    
    DesignAgent agent = design.getAgent("Release Dead Messages").orElse(null);
    assertNotNull(agent);
    
    assertInstanceOf(DesignFormulaAgent.class, agent);
    DesignFormulaAgent formulaAgent = (DesignFormulaAgent) agent;
    
    assertEquals("Release Dead Messages", agent.getTitle());
    assertEquals("This filter releases (and tries to resend) all messages that have been marked DEAD.\n", agent.getComment());
    
    assertEquals(AgentTrigger.MANUAL, agent.getTrigger());
    assertEquals(AgentTarget.ALL, agent.getTarget());

    assertEquals(toLf("RoutingState = \"DEAD\";\n"
        + "FIELD RoutingState := \"\";\n"
        + "FIELD Recipients := IntendedRecipient;\n"
        + "FIELD Form := MailSavedForm;"), toLf(formulaAgent.getFormula().get()));
    assertEquals(DesignFormulaAgent.DocumentAction.MODIFY, formulaAgent.getDocumentAction().get());
  }
  
  @Test
  public void testChangeInformation() {
    DbDesign design = database.getDesign();
    
    DesignAgent agent = design.getAgent("ChangeInformation").orElse(null);
    assertNotNull(agent);
    
    assertEquals("(ChangeInformation)", agent.getTitle());
    assertEquals("Run by Administrator with \"Make Change\" button", agent.getComment());
    
    assertInstanceOf(DesignFormulaAgent.class, agent);
    DesignFormulaAgent formulaAgent = (DesignFormulaAgent) agent;

    assertEquals(AgentTrigger.MANUAL, agent.getTrigger());
    assertEquals(AgentTarget.VIEW, agent.getTarget());

    assertEquals(toLf("(form = \"Person\") & (LastName = @Environment(\"PHADM_OldLastName\")) & (Firstname = @Environment(\"PHADM_OldFirstname\")) & (PhoneExt = @Environment(\"PHADM_OldPhoneExt\"));\n"
        + "FIELD LastName := @Environment(\"PHADM_LastName\");\n"
        + "FIELD FirstName := @Environment(\"PHADM_FirstName\");\n"
        + "FIELD PhoneExt := @Environment(\"PHADM_PhoneExt\");\n"
        + "FIELD WG_ShortName := @Environment(\"PHADM_WG_ShortName\");\n"
        + "FIELD WG_Number := @Environment(\"PHADM_WG_Number\");\n"
        + "FIELD DeptName := @Environment(\"PHADM_DeptName\");\n"
        + "FIELD Site_ShortName := @Environment(\"PHADM_Site_ShortName\");\n"
        + "FIELD OfficeNumber := @Environment(\"PHADM_OfficeNumber\");\n"
        + "FIELD Email := @Environment(\"PHADM_Email\");"), toLf(formulaAgent.getFormula().get()));
    assertEquals(DesignFormulaAgent.DocumentAction.MODIFY, formulaAgent.getDocumentAction().get());
  }
  
  @Test
  public void testSetup() {
    DbDesign design = database.getDesign();
    
    DesignAgent agent = design.getAgent("Setup").orElse(null);
    assertNotNull(agent);
    
    assertEquals("(Setup)", agent.getTitle());
    assertEquals("Copies all Time Slot documents in the (Setup) view and creates a new month's worth of Time Slot documents.", agent.getComment());
    
    assertInstanceOf(DesignFormulaAgent.class, agent);
    DesignFormulaAgent formulaAgent = (DesignFormulaAgent) agent;

    assertEquals(AgentTrigger.MANUAL, agent.getTrigger());
    assertEquals(AgentTarget.VIEW, agent.getTarget());

    assertEquals(toLf("MonthType = @Environment(\"EnvSchMonthType\") & HourSeq >= @Environment(\"EnvDayStart\") & HourSeq <= @Environment(\"EnvDayEnd\");\n"
        + "FIELD Month := @Environment(\"EnvSchMonth\");\n"
        + "FIELD MonthSeq := @Environment(\"EnvSchMonthSeq\");\n"
        + "FIELD ClockType := @Environment(\"EnvClockType\");\n"
        + "FIELD MonthType := @Unavailable;"), toLf(formulaAgent.getFormula().get()));
    assertEquals(DesignFormulaAgent.DocumentAction.CREATE, formulaAgent.getDocumentAction().get());
  }
  
  @Test
  public void testOutgoing() {
    DbDesign design = database.getDesign();
    
    DesignAgent agent = design.getAgent("Outgoing Line Items").orElse(null);
    assertNotNull(agent);
    
    assertEquals("Outgoing Line Items", agent.getTitle());
    assertEquals("This macro runs in the background, hourly.   For each Purchase Requisition which has recently been approved this macro will route the individual line items from that req to the Purchasing Item Tracking database.", agent.getComment());
    
    assertInstanceOf(DesignFormulaAgent.class, agent);
    DesignFormulaAgent formulaAgent = (DesignFormulaAgent) agent;

    assertEquals(AgentTrigger.SCHEDULED, agent.getTrigger());
    assertEquals(AgentTarget.NEW, agent.getTarget());
    
    // This macro is "hourly" in the NSF, but this should translate to a 60-minute interval
    assertEquals(AgentInterval.MINUTES, agent.getIntervalType());
    assertEquals(60, agent.getInterval().getAsInt());

    assertEquals(toLf("REM {Setup the header string (header fields)};\n"
        + "REM;\n"
        + "dlm := \"~~\";\n"
        + "header := RequisitionNumber + dlm + RequisitionDate + dlm + RequisitionedBy + dlm + RequisitionedFor;\n"
        + "REM;\n"
        + "REM {Construct a data string for each line item, put it in the };\n"
        + "REM {subject field of a mail message, and send it off to the Line};\n"
        + "REM {Item Tracking database.};\n"
        + "REM;\n"
        + "data1 := \"1\" + dlm + header + dlm + pn1 + dlm + Name1 + dlm + @Text(Price1) + dlm + @Text(Qty1) + dlm + @Text(Total1) + dlm;\n"
        + "FIELD first := @If(pn1 != \"\" & @IsAvailable(pn1); @MailSend(\"Lineitem\"; \"\"; \"\"; data1); \"\");\n"
        + "FIELD first := @Unavailable;\n"
        + "REM;\n"
        + "data2 := \"2\" + dlm + header + dlm + pn2 + dlm + Name2 + dlm + @Text(Price2) + dlm + @Text(Qty2) + dlm + @Text(Total2) + dlm;\n"
        + "FIELD second := @If(pn2 != \"\" & @IsAvailable(pn2); @MailSend(\"Lineitem\"; \"\"; \"\"; data2); \"\");\n"
        + "FIELD second := @Unavailable;\n"
        + "REM;\n"
        + "data3 := \"3\" + dlm + header + dlm + pn3 + dlm + Name3 + dlm + @Text(Price3) + dlm + @Text(Qty3) + dlm + @Text(Total3) + dlm;\n"
        + "FIELD third := @If(pn3 != \"\" & @IsAvailable(pn3); @MailSend(\"Lineitem\"; \"\"; \"\"; data3); \"\");\n"
        + "FIELD third := @Unavailable;\n"
        + "REM;\n"
        + "REM {Update the status of this Req to Routed=Yes};\n"
        + "REM;\n"
        + "FIELD Routed := \"Yes\";\n"
        + "REM;\n"
        + "REM {Select only Reqs that have been approved, but not yet routed};\n"
        + "REM;\n"
        + "Approved = \"Approved\" & Routed = \"No\""), toLf(formulaAgent.getFormula().get()));
    assertEquals(DesignFormulaAgent.DocumentAction.MODIFY, formulaAgent.getDocumentAction().get());
  }
  
  @Test
  public void testIncoming() {
    DbDesign design = database.getDesign();
    
    DesignAgent agent = design.getAgent("Clean Incoming PO's").orElse(null);
    assertNotNull(agent);
    
    assertEquals("Clean Incoming PO's", agent.getTitle());
    assertEquals("This macro runs on Purchase Requisition documents mailed in from the Product Catalog database.  It sets up certain default field values, and removes unnecessary field values.", agent.getComment());
    
    assertInstanceOf(DesignFormulaAgent.class, agent);
    DesignFormulaAgent formulaAgent = (DesignFormulaAgent) agent;

    assertEquals(AgentTrigger.NEWMAIL, agent.getTrigger());
    assertEquals(AgentTarget.NEW, agent.getTarget());
    assertEquals(AgentInterval.NONE, agent.getIntervalType());

    assertEquals(toLf("@All;\n"
        + "REM;\n"
        + "REM {Set Approval and Email Status fields};\n"
        + "REM;\n"
        + "FIELD Approved := @If(Approvers != \"\"; \"In Process\"; \"Approved\");\n"
        + "initstring := @Explode(@Repeat(\"No \"; @Elements(Approvers) - 1));\n"
        + "FIELD ApproversEmail := @If(Approvers != \"\"; @Trim(\"Yes\" : initstring); \"\");\n"
        + "FIELD ApproversStatus := @If(Approvers != \"\"; @Trim(\"No\" : initstring); \"\");\n"
        + "FIELD Routed := \"No\";\n"
        + "REM;\n"
        + "REM {Remove unnecessary fields from the mailed-in document};\n"
        + "REM;\n"
        + "FIELD PRServer := @Unavailable;\n"
        + "FIELD PRFilename := @Unavailable;\n"
        + "FIELD TestPRNames := @Unavailable;\n"
        + "FIELD Limit := @Unavailable;\n"
        + "FIELD CCApprovers := @Unavailable;\n"
        + "FIELD CCApproversLimit := @Unavailable;\n"
        + "FIELD AssignedApprovers := @Unavailable;\n"
        + "FIELD AdditionalApprovers := @Unavailable;\n"
        + "FIELD AdditionalApproversLimit := @Unavailable;\n"
        + "FIELD MailOptions := @Unavailable;\n"
        + "FIELD InfoMessage := @Unavailable;\n"
        + "FIELD CheckLimit := @Unavailable;\n"
        + "FIELD SendTo := @Unavailable;\n"
        + "FIELD From := @Unavailable;\n"
        + "FIELD PostedDate := @Unavailable;\n"
        + "FIELD Recipients := @Unavailable;\n"
        + "FIELD RouteServers := @Unavailable;\n"
        + "FIELD RouteTimes := @Unavailable;\n"
        + "FIELD DeliveredDate := @Unavailable;\n"
        + "FIELD Categories := @Unavailable;"), toLf(formulaAgent.getFormula().get()));
    assertEquals(DesignFormulaAgent.DocumentAction.MODIFY, formulaAgent.getDocumentAction().get());
  }
}
