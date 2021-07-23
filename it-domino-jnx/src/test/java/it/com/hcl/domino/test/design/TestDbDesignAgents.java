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
import java.util.Collections;
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
import com.hcl.domino.design.ComputableValue;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.DesignAgent;
import com.hcl.domino.design.DesignElement;
import com.hcl.domino.design.View;
import com.hcl.domino.design.action.CopyToDatabaseAction;
import com.hcl.domino.design.action.DeleteDocumentAction;
import com.hcl.domino.design.action.FolderBasedAction;
import com.hcl.domino.design.action.ModifyByFormAction;
import com.hcl.domino.design.action.ModifyFieldAction;
import com.hcl.domino.design.action.ReadMarksAction;
import com.hcl.domino.design.action.ReplyAction;
import com.hcl.domino.design.action.RunAgentAction;
import com.hcl.domino.design.action.RunFormulaAction;
import com.hcl.domino.design.action.SendDocumentAction;
import com.hcl.domino.design.action.SendMailAction;
import com.hcl.domino.design.action.SendNewsletterAction;
import com.hcl.domino.design.action.SimpleAction;
import com.hcl.domino.design.DesignAgent.AgentLanguage;
import com.hcl.domino.design.agent.AgentContent;
import com.hcl.domino.design.agent.AgentInterval;
import com.hcl.domino.design.agent.AgentTrigger;
import com.hcl.domino.design.agent.FormulaAgentContent;
import com.hcl.domino.design.agent.ImportedJavaAgentContent;
import com.hcl.domino.design.agent.JavaAgentContent;
import com.hcl.domino.design.agent.LotusScriptAgentContent;
import com.hcl.domino.design.agent.SimpleActionAgentContent;
import com.hcl.domino.design.agent.FormulaAgentContent.DocumentAction;
import com.ibm.commons.util.io.StreamUtil;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDbDesignAgents extends AbstractNotesRuntimeTest {
	private static String dbPath;
	private Database database;
	
	@BeforeEach
	public void initDesignDb() throws IOException, URISyntaxException {
		if(database == null) {
			DominoClient client = getClient();
			if(dbPath == null) {
				database = createTempDb(client);
				dbPath = database.getAbsoluteFilePath();
				populateResourceDxl("/dxl/testDbDesignAgents", database);
			} else {
				database = client.openDatabase("", dbPath);
			}
		}
	}
	
	@AfterAll
	public static void termDesignDb() {
		try {
			Files.deleteIfExists(Paths.get(dbPath));
		} catch(Throwable t) {
			System.err.println("Unable to delete database " + dbPath + ": " + t);
		}
	}
	
	@Test
	public void testAgentsCount() {
		DbDesign dbDesign = database.getDesign();
		Collection<DesignAgent> agents = dbDesign.getAgents().collect(Collectors.toList());
		assertEquals(19, agents.size());
		
		assertNull(dbDesign.getAgent("Content").orElse(null));
	}
	
	@Test
	public void testFormulaAgent() {
		DbDesign dbDesign = database.getDesign();
		Collection<DesignAgent> agents = dbDesign.getAgents().collect(Collectors.toList());
		DesignAgent agent = agents.stream().filter(a -> "formula agent".equals(a.getTitle())).findFirst().orElse(null);
		assertNotNull(agent);
		assertEquals(AgentLanguage.FORMULA, agent.getAgentLanguage());
		
		assertNotNull(dbDesign.getAgent("formula agent"));
		assertEquals(1, dbDesign.getDesignElementsByName(DesignAgent.class, "formula agent").count());
		assertNotNull(dbDesign.getDesignElementByName(DesignAgent.class, "formula agent"));
		
		String formula = "@StatusBar(\"hey\");\n"
				+ " @All";
		AgentContent content = agent.getAgentContent();
		assertInstanceOf(FormulaAgentContent.class, content);
		assertEquals(formula, ((FormulaAgentContent)content).getFormula());
		assertEquals(DocumentAction.MODIFY, ((FormulaAgentContent)content).getDocumentAction());
	}
	
	@Test
	public void testImportedJavaAgent() {
		DbDesign dbDesign = database.getDesign();
		Collection<DesignAgent> agents = dbDesign.getAgents().collect(Collectors.toList());
		DesignAgent agent = agents.stream().filter(a -> "imported java agent".equals(a.getTitle())).findFirst().orElse(null);
		assertNotNull(agent);
		assertEquals(AgentLanguage.IMPORTED_JAVA, agent.getAgentLanguage());

		assertNotNull(dbDesign.getAgent("imported java agent"));
		AgentContent content = agent.getAgentContent();
		assertInstanceOf(ImportedJavaAgentContent.class, content);
		
		ImportedJavaAgentContent javaAgent = (ImportedJavaAgentContent)content;
		assertEquals("ImportedJavaAgentContent.class", javaAgent.getMainClassName());
		assertEquals("H:\\", javaAgent.getCodeFilesystemPath());
		assertEquals(Arrays.asList("ImportedJavaAgentContent.class", "JavaAgentContent.class", "bar.txt", "foo.jar"), javaAgent.getFileNameList());
	}
	
	@Test
	public void testJavaAgent() {
		DbDesign dbDesign = database.getDesign();
		Collection<DesignAgent> agents = dbDesign.getAgents().collect(Collectors.toList());
		DesignAgent agent = agents.stream().filter(a -> "java agent".equals(a.getTitle())).findFirst().orElse(null);
		assertNotNull(agent);
		assertEquals(AgentLanguage.JAVA, agent.getAgentLanguage());

		assertNotNull(dbDesign.getAgent("java agent"));
		AgentContent content = agent.getAgentContent();
		assertInstanceOf(JavaAgentContent.class, content);
		JavaAgentContent javaAgent = (JavaAgentContent)content;
		assertEquals("JavaAgent.class", javaAgent.getMainClassName());
		assertEquals("c:\\Notes\\Data", javaAgent.getCodeFilesystemPath());
		assertEquals("%%source%%.jar", javaAgent.getSourceAttachmentName().get());
		assertEquals("%%object%%.jar", javaAgent.getObjectAttachmentName().get());
		assertFalse(javaAgent.getResourcesAttachmentName().isPresent());
		assertEquals(Collections.emptyList(), javaAgent.getSharedLibraryList());
	}
	
	@Test
	public void testMultiFileJavaAgent() {
		DbDesign dbDesign = database.getDesign();
		DesignAgent agent = dbDesign.getAgent("Multi-File Java").orElse(null);
		assertNotNull(agent);
		
		assertEquals(AgentLanguage.JAVA, agent.getAgentLanguage());
		assertEquals(AgentTrigger.SCHEDULED, agent.getTrigger());
		assertEquals(AgentInterval.MINUTES, agent.getIntervalType());
		assertTrue(agent.getStartDate().isPresent());
		assertEquals(LocalDate.of(2021, 6, 14), agent.getStartDate().get().toLocalDate());
		assertFalse(agent.getEndDate().isPresent());
		assertEquals(LocalTime.of(10, 0), agent.getRunLocalTime().get());
		assertEquals(LocalTime.of(16, 0), agent.getRunEndLocalTime().get());
		assertEquals(3*60 + 30, agent.getInterval().getAsInt());
		assertEquals("CN=Arcturus/O=Frost", agent.getRunLocation());
		assertFalse(agent.isRunOnWeekends());

		AgentContent content = agent.getAgentContent();
		assertInstanceOf(JavaAgentContent.class, content);
		JavaAgentContent javaAgent = (JavaAgentContent)content;
		assertEquals("lotus.domino.axis.JavaAgentRenamed.class", javaAgent.getMainClassName());
		assertEquals("c:\\Notes\\Data", javaAgent.getCodeFilesystemPath());
		assertEquals("%%source%%.jar", javaAgent.getSourceAttachmentName().get());
		assertEquals("%%object%%.jar", javaAgent.getObjectAttachmentName().get());
		assertEquals("%%resource%%.jar", javaAgent.getResourcesAttachmentName().get());
		assertEquals(Arrays.asList("foo.jar", "bar.jar"), javaAgent.getEmbeddedJars());
		assertEquals(Arrays.asList("java lib", "java consumer", "java lib 2", "java lib 3", "java lib 4"), javaAgent.getSharedLibraryList());
	}
	
	@Test
	public void testLSAgent() throws IOException {
		DbDesign dbDesign = database.getDesign();
		Collection<DesignAgent> agents = dbDesign.getAgents().collect(Collectors.toList());
		DesignAgent agent = agents.stream().filter(a -> "Printer Agent".equals(a.getTitle())).findFirst().orElse(null);
		assertNotNull(agent);
		assertEquals(AgentLanguage.LS, agent.getAgentLanguage());

		assertNotNull(dbDesign.getAgent("Printer Agent"));
		String largels;
		try(InputStream is = getClass().getResourceAsStream("/text/printeragent.txt")) {
			largels = StreamUtil.readString(is);
		}
		AgentContent content = agent.getAgentContent();
		assertInstanceOf(LotusScriptAgentContent.class, content);
		assertEquals(largels, ((LotusScriptAgentContent)content).getScript());
	}
	
	@Test
	public void testSimpleActionAgent() {
		DbDesign dbDesign = database.getDesign();
		Collection<DesignAgent> agents = dbDesign.getAgents().collect(Collectors.toList());
		DesignAgent agent = agents.stream().filter(a -> "sa agent".equals(a.getTitle())).findFirst().orElse(null);
		assertNotNull(agent);
		assertEquals(AgentLanguage.SIMPLE_ACTION, agent.getAgentLanguage());

		assertNotNull(dbDesign.getAgent("sa agent"));
		AgentContent content = agent.getAgentContent();
		assertInstanceOf(SimpleActionAgentContent.class, content);
		List<SimpleAction> actions = ((SimpleActionAgentContent)content).getActions();
		assertEquals(19, actions.size());
		{
			RunFormulaAction action = (RunFormulaAction)actions.get(0);
			assertEquals(DocumentAction.MODIFY, action.getDocumentAction());
			assertEquals("\"foo\"", action.getFormula());
		}
		{
			ModifyFieldAction action = (ModifyFieldAction)actions.get(1);
			assertEquals("Body", action.getFieldName());
			assertEquals("dfdf", action.getValue());
		}
		{
			FolderBasedAction action = (FolderBasedAction)actions.get(2);
			assertEquals(FolderBasedAction.Type.REMOVE, action.getType());
		}
		{
			CopyToDatabaseAction action = (CopyToDatabaseAction)actions.get(3);
			assertEquals("CN=Arcturus/O=Frost", action.getServerName());
			assertEquals("names.nsf", action.getDatabaseName());
		}
		{
			FolderBasedAction action = (FolderBasedAction)actions.get(4);
			assertEquals("test folder", action.getFolderName());
		}
		{
			DeleteDocumentAction action = (DeleteDocumentAction)actions.get(5);
			assertNotNull(action);
		}
		{
			ReadMarksAction action = (ReadMarksAction)actions.get(6);
			assertEquals(ReadMarksAction.Type.MARK_UNREAD, action.getType());
		}
		{
			ReadMarksAction action = (ReadMarksAction)actions.get(7);
			assertEquals(ReadMarksAction.Type.MARK_READ, action.getType());
		}
		{
			ModifyByFormAction action = (ModifyByFormAction)actions.get(8);
			assertEquals("Alias", action.getFormName());
			Map<String, List<String>> modifications = action.getModifications();
			assertEquals(10, modifications.size()); // 10 total fields on the form
			assertEquals(Arrays.asList("foo"), modifications.get("Host"));
			assertEquals(Arrays.asList("bar"), modifications.get("From"));
			assertEquals(Arrays.asList("baz"), modifications.get("To"));
		}
		{
			FolderBasedAction action = (FolderBasedAction)actions.get(9);
			assertEquals(FolderBasedAction.Type.MOVE, action.getType());
			assertEquals("test folder", action.getFolderName());
		}
		{
			ReplyAction action = (ReplyAction)actions.get(10);
			assertFalse(action.isReplyToAll());
			assertFalse(action.isReplyOnce());
			assertEquals("\"hey sender\"", action.getBody());
			assertTrue(action.isIncludeDocument());
		}
		{
			ReplyAction action = (ReplyAction)actions.get(11);
			assertTrue(action.isReplyToAll());
			assertTrue(action.isReplyOnce());
			assertEquals("\"no copy in this one\"", action.getBody());
			assertFalse(action.isIncludeDocument());
		}
		{
			RunAgentAction action = (RunAgentAction)actions.get(12);
			assertEquals("Compiler", action.getAgentName());
		}
		{
			SendDocumentAction action = (SendDocumentAction)actions.get(13);
			assertNotNull(action);
		}
		{
			SendMailAction action = (SendMailAction)actions.get(14);
			ComputableValue to = action.getTo();
			assertFalse(to.isFormula());
			assertEquals("foo@foo.com", to.getValue());
			assertEquals("bar@bar.com", action.getCc().getValue());
			ComputableValue bcc = action.getBcc();
			assertTrue(bcc.isFormula());
			assertEquals("\"bcc@\" + \"bcc.com\"", bcc.getValue());
			assertEquals("Test sending", action.getSubject().getValue());
			assertEquals("this is test body", action.getBody());
			assertTrue(action.isIncludeDocument());
			assertTrue(action.isIncludeDocLink());
		}
		{
			SendNewsletterAction action = (SendNewsletterAction)actions.get(15);
			assertEquals("foo@foo.com", action.getTo());
			assertEquals("sdfdf", action.getSubject());
			assertEquals("rwViewNameLen  Length, in bytes, of the name of the view used to display the data\r\n"
					+ "wSpare		Reserved;  must be 0.", action.getBody());
			assertEquals("Aliases", action.getViewName());
			assertTrue(action.isGatherDocuments());
			assertEquals(2, action.getGatherThreshold());
		}
		{
			SendNewsletterAction action = (SendNewsletterAction)actions.get(16);
			assertEquals("foo@foo.com", action.getTo());
			assertEquals("sdfdf", action.getSubject());
			assertEquals("this is the newsletter", action.getBody());
			assertEquals("Aliases", action.getViewName());
			assertTrue(action.isGatherDocuments());
			assertEquals(2, action.getGatherThreshold());
		}
		{
			SendNewsletterAction action = (SendNewsletterAction)actions.get(17);
			assertEquals("foo@foo.com", action.getTo());
			assertEquals("sdfdf", action.getSubject());
			assertEquals("this is the newsletters", action.getBody());
			assertEquals("Aliases", action.getViewName());
			assertTrue(action.isGatherDocuments());
			assertEquals(2, action.getGatherThreshold());
		}
		{
			SendNewsletterAction action = (SendNewsletterAction)actions.get(18);
			assertEquals("foo@foo.com", action.getTo());
			assertEquals("sdfdf", action.getSubject());
			assertEquals("", action.getBody());
			assertFalse(action.isIncludeSummary());
			assertTrue(action.isGatherDocuments());
			assertEquals(2, action.getGatherThreshold());
		}
	}
	
	@Test
	public void testScheduledDisabledFormula() {
		DbDesign dbDesign = database.getDesign();
		DesignAgent agent = dbDesign.getAgent("Scheduled Weekly Disabled").orElse(null);
		assertNotNull(agent);
		
		assertEquals(AgentLanguage.FORMULA, agent.getAgentLanguage());
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
	
	@ParameterizedTest
	@EnumSource(DayOfWeek.class)
	public void testWeekdayAgents(DayOfWeek dayOfWeek) {
		DbDesign dbDesign = database.getDesign();
		// Also tests "Scheduled Weekly Disabled"'s "Scheduled Weekly Tuesday" alias and case-insensitive lookups
		DesignAgent agent = dbDesign.getAgent("Scheduled Weekly " + dayOfWeek).orElse(null);
		assertNotNull(agent);
		
		assertEquals(AgentTrigger.SCHEDULED, agent.getTrigger());
		assertEquals(AgentInterval.WEEK, agent.getIntervalType());
		assertFalse(agent.getStartDate().isPresent());
		assertTrue(agent.getEndDate().isPresent());
		assertEquals(dayOfWeek, agent.getRunDayOfWeek().get());
		assertFalse(agent.getRunEndLocalTime().isPresent());
	}
	
	@Test
	public void testSelectServerFormula() {
		DbDesign dbDesign = database.getDesign();
		DesignAgent agent = dbDesign.getAgent("Select Server").orElse(null);
		assertNotNull(agent);
		
		assertEquals(AgentLanguage.FORMULA, agent.getAgentLanguage());
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
	public void testSelectDocsFormula() {
		DbDesign dbDesign = database.getDesign();
		DesignAgent agent = dbDesign.getAgent("select docs formula").orElse(null);
		assertNotNull(agent);
		
		assertEquals(AgentLanguage.FORMULA, agent.getAgentLanguage());
		AgentContent content = agent.getAgentContent();
		assertInstanceOf(FormulaAgentContent.class, content);
		assertEquals("\"foo\";\n\n @All", ((FormulaAgentContent)content).getFormula());
		assertEquals(DocumentAction.SELECT, ((FormulaAgentContent)content).getDocumentAction());
	}
	
	@Test
	public void testNewDocsFormula() {
		DbDesign dbDesign = database.getDesign();
		DesignAgent agent = dbDesign.getAgent("new docs formula").orElse(null);
		assertNotNull(agent);
		
		assertEquals(AgentLanguage.FORMULA, agent.getAgentLanguage());
		AgentContent content = agent.getAgentContent();
		assertInstanceOf(FormulaAgentContent.class, content);
		assertEquals("\"bar\";\n\n @All", ((FormulaAgentContent)content).getFormula());
		assertEquals(DocumentAction.CREATE, ((FormulaAgentContent)content).getDocumentAction());
	}
	
	@Test
	public void testLargeLotusScriptAgent() throws IOException {
		DbDesign dbDesign = database.getDesign();
		DesignAgent agent = dbDesign.getAgent("Large LotusScript Agent").orElse(null);
		assertNotNull(agent);
		
		assertEquals(AgentLanguage.LS, agent.getAgentLanguage());
		assertEquals(AgentTrigger.NEWMAIL, agent.getTrigger());
		assertFalse(agent.getStartDate().isPresent());
		assertFalse(agent.getEndDate().isPresent());
		
		String largels;
		try(InputStream is = getClass().getResourceAsStream("/text/largels.txt")) {
			largels = StreamUtil.readString(is);
		}
		AgentContent content = agent.getAgentContent();
		assertInstanceOf(LotusScriptAgentContent.class, content);
		assertEquals(largels, ((LotusScriptAgentContent)content).getScript());
	}
	
	@Test
	public void testNonformulaSimpleAction() throws IOException {
		DbDesign dbDesign = database.getDesign();
		DesignAgent agent = dbDesign.getAgent("sa nonformula").orElse(null);
		assertNotNull(agent);
		
		assertEquals(AgentLanguage.SIMPLE_ACTION, agent.getAgentLanguage());
	}
	
	@Test
	public void testCreateAgent() throws Exception {
		withTempDb(database -> {
			DbDesign design = database.getDesign();
			DesignAgent element = design.createAgent("foo bar");
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
	public void testFormulaQuery() {
		DbDesign dbDesign = database.getDesign();
		{
			List<DesignElement> elements = dbDesign.queryDesignElements("$TITLE='formula agent'").collect(Collectors.toList());
			assertEquals(1, elements.size());
			assertTrue(elements.get(0) instanceof DesignAgent);
			assertEquals("formula agent", ((DesignAgent)elements.get(0)).getTitle());
		}
		{
			List<DesignElement> elements = dbDesign.queryDesignElements(DesignAgent.class, "$TITLE='formula agent'").collect(Collectors.toList());
			assertEquals(1, elements.size());
			assertTrue(elements.get(0) instanceof DesignAgent);
			assertEquals("formula agent", ((DesignAgent)elements.get(0)).getTitle());
		}
		{
			List<DesignElement> elements = dbDesign.queryDesignElements(View.class, "$TITLE='formula agent'").collect(Collectors.toList());
			assertEquals(0, elements.size());
		}
	}
}
