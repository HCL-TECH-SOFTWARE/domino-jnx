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
package it.com.hcl.domino.test.richtext;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.hcl.domino.data.Document;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.records.CDActionDBCopy;
import com.hcl.domino.richtext.records.CDActionDelete;
import com.hcl.domino.richtext.records.CDActionFolder;
import com.hcl.domino.richtext.records.CDActionHeader;
import com.hcl.domino.richtext.records.CDActionJavaAgent;
import com.hcl.domino.richtext.records.CDActionLotusScript;
import com.hcl.domino.richtext.records.CDActionModifyField;
import com.hcl.domino.richtext.records.CDActionNewsletter;
import com.hcl.domino.richtext.records.CDActionReadMarks;
import com.hcl.domino.richtext.records.CDActionReply;
import com.hcl.domino.richtext.records.CDActionRunAgent;
import com.hcl.domino.richtext.records.CDActionSendDocument;
import com.hcl.domino.richtext.records.CDActionSendMail;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RecordType.Area;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestRichTextRecordsAgents extends AbstractNotesRuntimeTest {
	
	@Test
	public void testCDActionHeader() throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
				rtWriter.addRichTextRecord(CDActionHeader.class, header -> {
				});
			}

			@SuppressWarnings("unused")
			CDActionHeader header = (CDActionHeader)doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
		});
	}
	
	@Test
	public void testCDActionLotusScript() throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
				rtWriter.addRichTextRecord(CDActionLotusScript.class, script -> {
					script.setScript("Dim foo as NotesBar");
				});
			}

			CDActionLotusScript script = (CDActionLotusScript)doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
			assertEquals("Dim foo as NotesBar", script.getScript());
		});
	}
	
	@Test
	public void testCDActionJavaAgent() throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
				rtWriter.addRichTextRecord(CDActionJavaAgent.class, script -> {
					script.setClassName("foo.Bar");
					script.setCodePath("/foo/Bar.java");
					// TODO investigate file/library storage
				});
			}

			CDActionJavaAgent script = (CDActionJavaAgent)doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
			assertEquals("foo.Bar", script.getClassName());
			assertEquals("/foo/Bar.java", script.getCodePath());
		});
	}
	
	@Test
	public void testCDActionDBCopy() throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
				rtWriter.addRichTextRecord(CDActionDBCopy.class, action -> {
					action.setFlags(EnumSet.of(CDActionDBCopy.Flag.MOVE));
					action.setServerName("CN=foo/O=bar");
					action.setDatabaseName("foo/bar/baz.nsf");
				});
			}

			CDActionDBCopy action = (CDActionDBCopy)doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
			assertEquals(EnumSet.of(CDActionDBCopy.Flag.MOVE), action.getFlags());
			assertEquals("CN=foo/O=bar", action.getServerName());
			assertEquals("foo/bar/baz.nsf", action.getDatabaseName());
		});
	}
	
	@Test
	public void testCDActionDelete() throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
				rtWriter.addRichTextRecord(CDActionDelete.class, action -> {
				});
			}

			@SuppressWarnings("unused")
			CDActionDelete action = (CDActionDelete)doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
		});
	}
	
	@ParameterizedTest
	@EnumSource(value = RecordType.class, names = {"ACTION_MOVETOFOLDER", "ACTION_COPYTOFOLDER", "ACTION_REMOVEFROMFOLDER"})
	public void testCDActionFolder(RecordType recordType) throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
				rtWriter.addRichTextRecord(recordType, (CDActionFolder action) -> {
					action.setFlags(EnumSet.of(CDActionFolder.Flag.NEWFOLDER));
					action.setFolderName("foo bar");
				});
			}

			CDActionFolder action = (CDActionFolder)doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
			assertEquals(recordType.getConstant(), action.getHeader().getSignature());
			assertEquals(EnumSet.of(CDActionFolder.Flag.NEWFOLDER), action.getFlags());
			assertEquals("foo bar", action.getFolderName());
		});
	}
	
	@Test
	public void testCDActionModifyField() throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
				rtWriter.addRichTextRecord(CDActionModifyField.class, action -> {
					action.setFlags(EnumSet.of(CDActionModifyField.Flag.REPLACE));
					action.setFieldName("FirstName");
					action.setValue("Foo");
				});
			}

			CDActionModifyField action = (CDActionModifyField)doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
			assertEquals(EnumSet.of(CDActionModifyField.Flag.REPLACE), action.getFlags());
			assertEquals("FirstName", action.getFieldName());
			assertEquals("Foo", action.getValue());
		});
	}
	
	@Test
	public void testCDActionNewsletter() throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
				rtWriter.addRichTextRecord(CDActionNewsletter.class, action -> {
					action.setFlags(EnumSet.of(CDActionNewsletter.Flag.INCLUDEALL, CDActionNewsletter.Flag.GATHER));
					action.setGatherCount(254);
					action.setSubject("I am the subject");
					action.setViewName("hey view");
					action.setTo("I am to");
					action.setBcc("Secret copy dest");
					action.setCc("Also copy here");
					action.setBody("This is the body");
				});
			}

			CDActionNewsletter action = (CDActionNewsletter)doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
			assertEquals(EnumSet.of(CDActionNewsletter.Flag.INCLUDEALL, CDActionNewsletter.Flag.GATHER), action.getFlags());
			assertEquals(254, action.getGatherCount());
			assertEquals("I am the subject", action.getSubject());
			assertEquals("hey view", action.getViewName());
			assertEquals("I am to", action.getTo());
			assertEquals("Secret copy dest", action.getBcc());
			assertEquals("Also copy here", action.getCc());
			assertEquals("This is the body", action.getBody());
		});
	}
	
	@Test
	public void testCDActionSendMail() throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
				rtWriter.addRichTextRecord(CDActionSendMail.class, action -> {
					action.setFlags(EnumSet.of(CDActionSendMail.Flag.INCLUDEDOC, CDActionSendMail.Flag.INCLUDELINK));
					action.setSubject("I am the subject");
					action.setTo("I am to");
					action.setBcc("Secret copy dest");
					action.setCc("Also copy here");
					action.setBody("This is the body");
				});
			}

			CDActionSendMail action = (CDActionSendMail)doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
			assertEquals(EnumSet.of(CDActionSendMail.Flag.INCLUDEDOC, CDActionSendMail.Flag.INCLUDELINK), action.getFlags());
			assertEquals("I am the subject", action.getSubject());
			assertEquals("I am to", action.getTo());
			assertEquals("Secret copy dest", action.getBcc());
			assertEquals("Also copy here", action.getCc());
			assertEquals("This is the body", action.getBody());
		});
	}
	
	@Test
	public void testCDActionSendMailFormulas() throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
				rtWriter.addRichTextRecord(CDActionSendMail.class, action -> {
					action.setFlags(EnumSet.of(CDActionSendMail.Flag.INCLUDEDOC, CDActionSendMail.Flag.INCLUDELINK));
					action.setSubjectFormula("\"I am the subject\"");
					action.setToFormula("\"I am to\"");
					action.setBcc("\"Secret copy dest\""); // Throw one non-formula in there
					action.setCcFormula("\"Also copy here\"");
					action.setBody("This is the body");
				});
			}

			CDActionSendMail action = (CDActionSendMail)doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
			assertEquals(EnumSet.of(CDActionSendMail.Flag.INCLUDEDOC, CDActionSendMail.Flag.INCLUDELINK, CDActionSendMail.Flag.CCFORMULA, CDActionSendMail.Flag.TOFORMULA, CDActionSendMail.Flag.SUBJECTFORMULA), action.getFlags());
			assertEquals("\"I am the subject\"", action.getSubject());
			assertEquals("\"I am to\"", action.getTo());
			assertEquals("\"Secret copy dest\"", action.getBcc());
			assertEquals("\"Also copy here\"", action.getCc());
			assertEquals("This is the body", action.getBody());
		});
	}
	
	@ParameterizedTest
	@EnumSource(value = RecordType.class, names = {"ACTION_MARKREAD", "ACTION_MARKUNREAD"})
	public void testCDActionReadMarks(RecordType recordType) throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
				rtWriter.addRichTextRecord(recordType, (CDActionReadMarks action) -> {
				});
			}

			CDActionReadMarks action = (CDActionReadMarks)doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
			assertEquals((byte)recordType.getConstant(), action.getHeader().getSignature());
		});
	}
	
	@Test
	public void testCDActionReply() throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
				rtWriter.addRichTextRecord(CDActionReply.class, action -> {
					action.setFlags(EnumSet.of(CDActionReply.Flag.REPLYTOALL));
					action.setBody("I am out of office");
				});
			}

			CDActionReply action = (CDActionReply)doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
			assertEquals(EnumSet.of(CDActionReply.Flag.REPLYTOALL), action.getFlags());
			assertEquals("I am out of office", action.getBody());
		});
	}
	
	@Test
	public void testCDActionRunAgent() throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
				rtWriter.addRichTextRecord(CDActionRunAgent.class, action -> {
					action.setAgentName("Some agent");
				});
			}

			CDActionRunAgent action = (CDActionRunAgent)doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
			assertEquals("Some agent", action.getAgentName());
		});
	}
	
	@Test
	public void testCDActionSendDocument() throws Exception {
		withTempDb(database -> {
			Document doc = database.createDocument();
			try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
				rtWriter.addRichTextRecord(CDActionSendDocument.class, action -> {
				});
			}

			@SuppressWarnings("unused")
			CDActionSendDocument action = (CDActionSendDocument)doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
		});
	}
}
