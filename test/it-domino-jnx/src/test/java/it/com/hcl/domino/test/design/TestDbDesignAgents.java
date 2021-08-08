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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.design.ComputableValue;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.DesignAgent;
import com.hcl.domino.design.DesignAgent.AgentLanguage;
import com.hcl.domino.design.DesignElement;
import com.hcl.domino.design.View;
import com.hcl.domino.design.agent.AgentContent;
import com.hcl.domino.design.agent.AgentInterval;
import com.hcl.domino.design.agent.AgentTrigger;
import com.hcl.domino.design.agent.FormulaAgentContent;
import com.hcl.domino.design.agent.FormulaAgentContent.DocumentAction;
import com.hcl.domino.design.simpleaction.CopyToDatabaseAction;
import com.hcl.domino.design.simpleaction.DeleteDocumentAction;
import com.hcl.domino.design.simpleaction.FolderBasedAction;
import com.hcl.domino.design.simpleaction.ModifyByFormAction;
import com.hcl.domino.design.simpleaction.ModifyFieldAction;
import com.hcl.domino.design.simpleaction.ReadMarksAction;
import com.hcl.domino.design.simpleaction.ReplyAction;
import com.hcl.domino.design.simpleaction.RunAgentAction;
import com.hcl.domino.design.simpleaction.RunFormulaAction;
import com.hcl.domino.design.simpleaction.SendDocumentAction;
import com.hcl.domino.design.simpleaction.SendMailAction;
import com.hcl.domino.design.simpleaction.SendNewsletterAction;
import com.hcl.domino.design.simpleaction.SimpleAction;
import com.hcl.domino.design.agent.ImportedJavaAgentContent;
import com.hcl.domino.design.agent.JavaAgentContent;
import com.hcl.domino.design.agent.LotusScriptAgentContent;
import com.hcl.domino.design.agent.SimpleActionAgentContent;
import com.ibm.commons.util.io.StreamUtil;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDbDesignAgents extends AbstractNotesRuntimeTest {
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
    Assertions.assertEquals(19, agents.size());

    Assertions.assertNull(dbDesign.getAgent("Content").orElse(null));
  }

  @Test
  public void testCreateAgent() throws Exception {
    this.withTempDb(database -> {
      final DbDesign design = database.getDesign();
      final DesignAgent element = design.createAgent("foo bar");
      Assertions.assertNotNull(element);
      Assertions.assertEquals("foo bar", element.getTitle());

      Assertions.assertNull(design.getAgent("foo bar").orElse(null));
      element.save();
      Assertions.assertNotNull(design.getAgent("foo bar").orElse(null));
      element.setTitle("other title");
      element.save();
      Assertions.assertNull(design.getAgent("foo bar").orElse(null));
      Assertions.assertNotNull(design.getAgent("other title").orElse(null));
    });
  }

  @Test
  public void testFormulaAgent() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<DesignAgent> agents = dbDesign.getAgents().collect(Collectors.toList());
    final DesignAgent agent = agents.stream().filter(a -> "formula agent".equals(a.getTitle())).findFirst().orElse(null);
    Assertions.assertNotNull(agent);
    Assertions.assertEquals(AgentLanguage.FORMULA, agent.getAgentLanguage());

    Assertions.assertNotNull(dbDesign.getAgent("formula agent"));
    Assertions.assertEquals(1, dbDesign.getDesignElementsByName(DesignAgent.class, "formula agent").count());
    Assertions.assertNotNull(dbDesign.getDesignElementByName(DesignAgent.class, "formula agent"));

    final String formula = "@StatusBar(\"hey\");\n"
        + " @All";
    final AgentContent content = agent.getAgentContent();
    Assertions.assertInstanceOf(FormulaAgentContent.class, content);
    Assertions.assertEquals(formula, ((FormulaAgentContent) content).getFormula());
    Assertions.assertEquals(DocumentAction.MODIFY, ((FormulaAgentContent) content).getDocumentAction());
  }

  @Test
  public void testFormulaQuery() {
    final DbDesign dbDesign = this.database.getDesign();
    {
      final List<DesignElement> elements = dbDesign.queryDesignElements("$TITLE='formula agent'").collect(Collectors.toList());
      Assertions.assertEquals(1, elements.size());
      Assertions.assertTrue(elements.get(0) instanceof DesignAgent);
      Assertions.assertEquals("formula agent", ((DesignAgent) elements.get(0)).getTitle());
    }
    {
      final List<DesignElement> elements = dbDesign.queryDesignElements(DesignAgent.class, "$TITLE='formula agent'")
          .collect(Collectors.toList());
      Assertions.assertEquals(1, elements.size());
      Assertions.assertTrue(elements.get(0) instanceof DesignAgent);
      Assertions.assertEquals("formula agent", ((DesignAgent) elements.get(0)).getTitle());
    }
    {
      final List<DesignElement> elements = dbDesign.queryDesignElements(View.class, "$TITLE='formula agent'")
          .collect(Collectors.toList());
      Assertions.assertEquals(0, elements.size());
    }
  }

  @Test
  public void testImportedJavaAgent() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<DesignAgent> agents = dbDesign.getAgents().collect(Collectors.toList());
    final DesignAgent agent = agents.stream().filter(a -> "imported java agent".equals(a.getTitle())).findFirst().orElse(null);
    Assertions.assertNotNull(agent);
    Assertions.assertEquals(AgentLanguage.IMPORTED_JAVA, agent.getAgentLanguage());

    Assertions.assertNotNull(dbDesign.getAgent("imported java agent"));
    final AgentContent content = agent.getAgentContent();
    Assertions.assertInstanceOf(ImportedJavaAgentContent.class, content);

    final ImportedJavaAgentContent javaAgent = (ImportedJavaAgentContent) content;
    Assertions.assertEquals("ImportedJavaAgentContent.class", javaAgent.getMainClassName());
    Assertions.assertEquals("H:\\", javaAgent.getCodeFilesystemPath());
    Assertions.assertEquals(Arrays.asList("ImportedJavaAgentContent.class", "JavaAgentContent.class", "bar.txt", "foo.jar"),
        javaAgent.getFileNameList());
  }

  @Test
  public void testJavaAgent() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<DesignAgent> agents = dbDesign.getAgents().collect(Collectors.toList());
    final DesignAgent agent = agents.stream().filter(a -> "java agent".equals(a.getTitle())).findFirst().orElse(null);
    Assertions.assertNotNull(agent);
    Assertions.assertEquals(AgentLanguage.JAVA, agent.getAgentLanguage());

    Assertions.assertNotNull(dbDesign.getAgent("java agent"));
    final AgentContent content = agent.getAgentContent();
    Assertions.assertInstanceOf(JavaAgentContent.class, content);
    final JavaAgentContent javaAgent = (JavaAgentContent) content;
    Assertions.assertEquals("JavaAgent.class", javaAgent.getMainClassName());
    Assertions.assertEquals("c:\\Notes\\Data", javaAgent.getCodeFilesystemPath());
    Assertions.assertEquals("%%source%%.jar", javaAgent.getSourceAttachmentName().get());
    Assertions.assertEquals("%%object%%.jar", javaAgent.getObjectAttachmentName().get());
    Assertions.assertFalse(javaAgent.getResourcesAttachmentName().isPresent());
    Assertions.assertEquals(Collections.emptyList(), javaAgent.getSharedLibraryList());
  }

  @Test
  public void testLargeLotusScriptAgent() throws IOException {
    final DbDesign dbDesign = this.database.getDesign();
    final DesignAgent agent = dbDesign.getAgent("Large LotusScript Agent").orElse(null);
    Assertions.assertNotNull(agent);

    Assertions.assertEquals(AgentLanguage.LS, agent.getAgentLanguage());
    Assertions.assertEquals(AgentTrigger.NEWMAIL, agent.getTrigger());
    Assertions.assertFalse(agent.getStartDate().isPresent());
    Assertions.assertFalse(agent.getEndDate().isPresent());

    String largels;
    try (InputStream is = this.getClass().getResourceAsStream("/text/largels.txt")) {
      largels = StreamUtil.readString(is);
    }
    final AgentContent content = agent.getAgentContent();
    Assertions.assertInstanceOf(LotusScriptAgentContent.class, content);
    Assertions.assertEquals(largels, ((LotusScriptAgentContent) content).getScript());
  }

  @Test
  public void testLSAgent() throws IOException {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<DesignAgent> agents = dbDesign.getAgents().collect(Collectors.toList());
    final DesignAgent agent = agents.stream().filter(a -> "Printer Agent".equals(a.getTitle())).findFirst().orElse(null);
    Assertions.assertNotNull(agent);
    Assertions.assertEquals(AgentLanguage.LS, agent.getAgentLanguage());

    Assertions.assertNotNull(dbDesign.getAgent("Printer Agent"));
    String largels;
    try (InputStream is = this.getClass().getResourceAsStream("/text/printeragent.txt")) {
      largels = StreamUtil.readString(is);
    }
    final AgentContent content = agent.getAgentContent();
    Assertions.assertInstanceOf(LotusScriptAgentContent.class, content);
    Assertions.assertEquals(largels, ((LotusScriptAgentContent) content).getScript());
  }

  @Test
  public void testMultiFileJavaAgent() {
    final DbDesign dbDesign = this.database.getDesign();
    final DesignAgent agent = dbDesign.getAgent("Multi-File Java").orElse(null);
    Assertions.assertNotNull(agent);

    Assertions.assertEquals(AgentLanguage.JAVA, agent.getAgentLanguage());
    Assertions.assertEquals(AgentTrigger.SCHEDULED, agent.getTrigger());
    Assertions.assertEquals(AgentInterval.MINUTES, agent.getIntervalType());
    Assertions.assertTrue(agent.getStartDate().isPresent());
    Assertions.assertEquals(LocalDate.of(2021, 6, 14), agent.getStartDate().get().toLocalDate());
    Assertions.assertFalse(agent.getEndDate().isPresent());
    Assertions.assertEquals(LocalTime.of(10, 0), agent.getRunLocalTime().get());
    Assertions.assertEquals(LocalTime.of(16, 0), agent.getRunEndLocalTime().get());
    Assertions.assertEquals(3 * 60 + 30, agent.getInterval().getAsInt());
    Assertions.assertEquals("CN=Arcturus/O=Frost", agent.getRunLocation());
    Assertions.assertFalse(agent.isRunOnWeekends());

    final AgentContent content = agent.getAgentContent();
    Assertions.assertInstanceOf(JavaAgentContent.class, content);
    final JavaAgentContent javaAgent = (JavaAgentContent) content;
    Assertions.assertEquals("lotus.domino.axis.JavaAgentRenamed.class", javaAgent.getMainClassName());
    Assertions.assertEquals("c:\\Notes\\Data", javaAgent.getCodeFilesystemPath());
    Assertions.assertEquals("%%source%%.jar", javaAgent.getSourceAttachmentName().get());
    Assertions.assertEquals("%%object%%.jar", javaAgent.getObjectAttachmentName().get());
    Assertions.assertEquals("%%resource%%.jar", javaAgent.getResourcesAttachmentName().get());
    Assertions.assertEquals(Arrays.asList("foo.jar", "bar.jar"), javaAgent.getEmbeddedJars());
    Assertions.assertEquals(Arrays.asList("java lib", "java consumer", "java lib 2", "java lib 3", "java lib 4"),
        javaAgent.getSharedLibraryList());
  }

  @Test
  public void testNewDocsFormula() {
    final DbDesign dbDesign = this.database.getDesign();
    final DesignAgent agent = dbDesign.getAgent("new docs formula").orElse(null);
    Assertions.assertNotNull(agent);

    Assertions.assertEquals(AgentLanguage.FORMULA, agent.getAgentLanguage());
    final AgentContent content = agent.getAgentContent();
    Assertions.assertInstanceOf(FormulaAgentContent.class, content);
    Assertions.assertEquals("\"bar\";\n\n @All", ((FormulaAgentContent) content).getFormula());
    Assertions.assertEquals(DocumentAction.CREATE, ((FormulaAgentContent) content).getDocumentAction());
  }

  @Test
  public void testNonformulaSimpleAction() throws IOException {
    final DbDesign dbDesign = this.database.getDesign();
    final DesignAgent agent = dbDesign.getAgent("sa nonformula").orElse(null);
    Assertions.assertNotNull(agent);

    Assertions.assertEquals(AgentLanguage.SIMPLE_ACTION, agent.getAgentLanguage());
  }

  @Test
  public void testScheduledDisabledFormula() {
    final DbDesign dbDesign = this.database.getDesign();
    final DesignAgent agent = dbDesign.getAgent("Scheduled Weekly Disabled").orElse(null);
    Assertions.assertNotNull(agent);

    Assertions.assertEquals(AgentLanguage.FORMULA, agent.getAgentLanguage());
    Assertions.assertEquals(AgentTrigger.SCHEDULED, agent.getTrigger());
    Assertions.assertEquals(AgentInterval.WEEK, agent.getIntervalType());
    Assertions.assertFalse(agent.getStartDate().isPresent());
    Assertions.assertTrue(agent.getEndDate().isPresent());
    Assertions.assertEquals(LocalDate.of(2022, 6, 14), agent.getEndDate().get().toLocalDate());
    Assertions.assertEquals("CN=Arcturus/O=Frost", agent.getRunLocation());
    Assertions.assertEquals(LocalTime.of(2, 0), agent.getRunLocalTime().get());
    Assertions.assertEquals(DayOfWeek.TUESDAY, agent.getRunDayOfWeek().get());
    Assertions.assertFalse(agent.getRunEndLocalTime().isPresent());
  }

  @Test
  public void testSelectDocsFormula() {
    final DbDesign dbDesign = this.database.getDesign();
    final DesignAgent agent = dbDesign.getAgent("select docs formula").orElse(null);
    Assertions.assertNotNull(agent);

    Assertions.assertEquals(AgentLanguage.FORMULA, agent.getAgentLanguage());
    final AgentContent content = agent.getAgentContent();
    Assertions.assertInstanceOf(FormulaAgentContent.class, content);
    Assertions.assertEquals("\"foo\";\n\n @All", ((FormulaAgentContent) content).getFormula());
    Assertions.assertEquals(DocumentAction.SELECT, ((FormulaAgentContent) content).getDocumentAction());
  }

  @Test
  public void testSelectServerFormula() {
    final DbDesign dbDesign = this.database.getDesign();
    final DesignAgent agent = dbDesign.getAgent("Select Server").orElse(null);
    Assertions.assertNotNull(agent);

    Assertions.assertEquals(AgentLanguage.FORMULA, agent.getAgentLanguage());
    Assertions.assertEquals(AgentTrigger.SCHEDULED, agent.getTrigger());
    Assertions.assertEquals(AgentInterval.MONTH, agent.getIntervalType());
    Assertions.assertFalse(agent.getStartDate().isPresent());
    Assertions.assertFalse(agent.getEndDate().isPresent());
    Assertions.assertEquals("", agent.getRunLocation());
    Assertions.assertEquals(12, agent.getRunDayOfMonth().getAsInt());
    Assertions.assertEquals(LocalTime.of(4, 0), agent.getRunLocalTime().get());
    Assertions.assertFalse(agent.getRunEndLocalTime().isPresent());
  }

  @Test
  public void testSimpleActionAgent() {
    final DbDesign dbDesign = this.database.getDesign();
    final Collection<DesignAgent> agents = dbDesign.getAgents().collect(Collectors.toList());
    final DesignAgent agent = agents.stream().filter(a -> "sa agent".equals(a.getTitle())).findFirst().orElse(null);
    Assertions.assertNotNull(agent);
    Assertions.assertEquals(AgentLanguage.SIMPLE_ACTION, agent.getAgentLanguage());

    Assertions.assertNotNull(dbDesign.getAgent("sa agent"));
    final AgentContent content = agent.getAgentContent();
    Assertions.assertInstanceOf(SimpleActionAgentContent.class, content);
    final List<SimpleAction> actions = ((SimpleActionAgentContent) content).getActions();
    Assertions.assertEquals(19, actions.size());
    {
      final RunFormulaAction action = (RunFormulaAction) actions.get(0);
      Assertions.assertEquals(DocumentAction.MODIFY, action.getDocumentAction());
      Assertions.assertEquals("\"foo\"", action.getFormula());
    }
    {
      final ModifyFieldAction action = (ModifyFieldAction) actions.get(1);
      Assertions.assertEquals("Body", action.getFieldName());
      Assertions.assertEquals("dfdf", action.getValue());
    }
    {
      final FolderBasedAction action = (FolderBasedAction) actions.get(2);
      Assertions.assertEquals(FolderBasedAction.Type.REMOVE, action.getType());
    }
    {
      final CopyToDatabaseAction action = (CopyToDatabaseAction) actions.get(3);
      Assertions.assertEquals("CN=Arcturus/O=Frost", action.getServerName());
      Assertions.assertEquals("names.nsf", action.getDatabaseName());
    }
    {
      final FolderBasedAction action = (FolderBasedAction) actions.get(4);
      Assertions.assertEquals("test folder", action.getFolderName());
    }
    {
      final DeleteDocumentAction action = (DeleteDocumentAction) actions.get(5);
      Assertions.assertNotNull(action);
    }
    {
      final ReadMarksAction action = (ReadMarksAction) actions.get(6);
      Assertions.assertEquals(ReadMarksAction.Type.MARK_UNREAD, action.getType());
    }
    {
      final ReadMarksAction action = (ReadMarksAction) actions.get(7);
      Assertions.assertEquals(ReadMarksAction.Type.MARK_READ, action.getType());
    }
    {
      final ModifyByFormAction action = (ModifyByFormAction) actions.get(8);
      Assertions.assertEquals("Alias", action.getFormName());
      final Map<String, List<String>> modifications = action.getModifications();
      Assertions.assertEquals(10, modifications.size()); // 10 total fields on the form
      Assertions.assertEquals(Arrays.asList("foo"), modifications.get("Host"));
      Assertions.assertEquals(Arrays.asList("bar"), modifications.get("From"));
      Assertions.assertEquals(Arrays.asList("baz"), modifications.get("To"));
    }
    {
      final FolderBasedAction action = (FolderBasedAction) actions.get(9);
      Assertions.assertEquals(FolderBasedAction.Type.MOVE, action.getType());
      Assertions.assertEquals("test folder", action.getFolderName());
    }
    {
      final ReplyAction action = (ReplyAction) actions.get(10);
      Assertions.assertFalse(action.isReplyToAll());
      Assertions.assertFalse(action.isReplyOnce());
      Assertions.assertEquals("\"hey sender\"", action.getBody());
      Assertions.assertTrue(action.isIncludeDocument());
    }
    {
      final ReplyAction action = (ReplyAction) actions.get(11);
      Assertions.assertTrue(action.isReplyToAll());
      Assertions.assertTrue(action.isReplyOnce());
      Assertions.assertEquals("\"no copy in this one\"", action.getBody());
      Assertions.assertFalse(action.isIncludeDocument());
    }
    {
      final RunAgentAction action = (RunAgentAction) actions.get(12);
      Assertions.assertEquals("Compiler", action.getAgentName());
    }
    {
      final SendDocumentAction action = (SendDocumentAction) actions.get(13);
      Assertions.assertNotNull(action);
    }
    {
      final SendMailAction action = (SendMailAction) actions.get(14);
      final ComputableValue to = action.getTo();
      Assertions.assertFalse(to.isFormula());
      Assertions.assertEquals("foo@foo.com", to.getValue());
      Assertions.assertEquals("bar@bar.com", action.getCc().getValue());
      final ComputableValue bcc = action.getBcc();
      Assertions.assertTrue(bcc.isFormula());
      Assertions.assertEquals("\"bcc@\" + \"bcc.com\"", bcc.getValue());
      Assertions.assertEquals("Test sending", action.getSubject().getValue());
      Assertions.assertEquals("this is test body", action.getBody());
      Assertions.assertTrue(action.isIncludeDocument());
      Assertions.assertTrue(action.isIncludeDocLink());
    }
    {
      final SendNewsletterAction action = (SendNewsletterAction) actions.get(15);
      Assertions.assertEquals("foo@foo.com", action.getTo());
      Assertions.assertEquals("sdfdf", action.getSubject());
      Assertions.assertEquals("rwViewNameLen  Length, in bytes, of the name of the view used to display the data\r\n"
          + "wSpare		Reserved;  must be 0.", action.getBody());
      Assertions.assertEquals("Aliases", action.getViewName());
      Assertions.assertTrue(action.isGatherDocuments());
      Assertions.assertEquals(2, action.getGatherThreshold());
    }
    {
      final SendNewsletterAction action = (SendNewsletterAction) actions.get(16);
      Assertions.assertEquals("foo@foo.com", action.getTo());
      Assertions.assertEquals("sdfdf", action.getSubject());
      Assertions.assertEquals("this is the newsletter", action.getBody());
      Assertions.assertEquals("Aliases", action.getViewName());
      Assertions.assertTrue(action.isGatherDocuments());
      Assertions.assertEquals(2, action.getGatherThreshold());
    }
    {
      final SendNewsletterAction action = (SendNewsletterAction) actions.get(17);
      Assertions.assertEquals("foo@foo.com", action.getTo());
      Assertions.assertEquals("sdfdf", action.getSubject());
      Assertions.assertEquals("this is the newsletters", action.getBody());
      Assertions.assertEquals("Aliases", action.getViewName());
      Assertions.assertTrue(action.isGatherDocuments());
      Assertions.assertEquals(2, action.getGatherThreshold());
    }
    {
      final SendNewsletterAction action = (SendNewsletterAction) actions.get(18);
      Assertions.assertEquals("foo@foo.com", action.getTo());
      Assertions.assertEquals("sdfdf", action.getSubject());
      Assertions.assertEquals("", action.getBody());
      Assertions.assertFalse(action.isIncludeSummary());
      Assertions.assertTrue(action.isGatherDocuments());
      Assertions.assertEquals(2, action.getGatherThreshold());
    }
  }

  @ParameterizedTest
  @EnumSource(DayOfWeek.class)
  public void testWeekdayAgents(final DayOfWeek dayOfWeek) {
    final DbDesign dbDesign = this.database.getDesign();
    // Also tests "Scheduled Weekly Disabled"'s "Scheduled Weekly Tuesday" alias and
    // case-insensitive lookups
    final DesignAgent agent = dbDesign.getAgent("Scheduled Weekly " + dayOfWeek).orElse(null);
    Assertions.assertNotNull(agent);

    Assertions.assertEquals(AgentTrigger.SCHEDULED, agent.getTrigger());
    Assertions.assertEquals(AgentInterval.WEEK, agent.getIntervalType());
    Assertions.assertFalse(agent.getStartDate().isPresent());
    Assertions.assertTrue(agent.getEndDate().isPresent());
    Assertions.assertEquals(dayOfWeek, agent.getRunDayOfWeek().get());
    Assertions.assertFalse(agent.getRunEndLocalTime().isPresent());
  }
}
