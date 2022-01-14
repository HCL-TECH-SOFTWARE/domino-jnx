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
package it.com.hcl.domino.test.richtext;

import java.util.EnumSet;

import org.junit.jupiter.api.Assertions;
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
  public void testCDActionDBCopy() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDActionDBCopy.class, action -> {
          action.setFlags(EnumSet.of(CDActionDBCopy.Flag.MOVE));
          action.setServerName("CN=foo/O=bar");
          action.setDatabaseName("foo/bar/baz.nsf");
        });
      }

      final CDActionDBCopy action = (CDActionDBCopy) doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
      Assertions.assertEquals(EnumSet.of(CDActionDBCopy.Flag.MOVE), action.getFlags());
      Assertions.assertEquals("CN=foo/O=bar", action.getServerName());
      Assertions.assertEquals("foo/bar/baz.nsf", action.getDatabaseName());
    });
  }

  @Test
  public void testCDActionDelete() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDActionDelete.class, action -> {
        });
      }

      @SuppressWarnings("unused")
      final CDActionDelete action = (CDActionDelete) doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
    });
  }

  @ParameterizedTest
  @EnumSource(value = RecordType.class, names = { "ACTION_MOVETOFOLDER", "ACTION_COPYTOFOLDER", "ACTION_REMOVEFROMFOLDER" })
  public void testCDActionFolder(final RecordType recordType) throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(recordType, (final CDActionFolder action) -> {
          action.setFlags(EnumSet.of(CDActionFolder.Flag.NEWFOLDER));
          action.setFolderName("foo bar");
        });
      }

      final CDActionFolder action = (CDActionFolder) doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
      Assertions.assertEquals(recordType.getConstant(), action.getHeader().getSignature());
      Assertions.assertEquals(EnumSet.of(CDActionFolder.Flag.NEWFOLDER), action.getFlags());
      Assertions.assertEquals("foo bar", action.getFolderName());
    });
  }

  @Test
  public void testCDActionHeader() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDActionHeader.class, header -> {
        });
      }

      @SuppressWarnings("unused")
      final CDActionHeader header = (CDActionHeader) doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
    });
  }

  @Test
  public void testCDActionJavaAgent() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDActionJavaAgent.class, script -> {
          script.setClassName("foo.Bar");
          script.setCodePath("/foo/Bar.java");
          // TODO investigate file/library storage
        });
      }

      final CDActionJavaAgent script = (CDActionJavaAgent) doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
      Assertions.assertEquals("foo.Bar", script.getClassName());
      Assertions.assertEquals("/foo/Bar.java", script.getCodePath());
    });
  }

  @Test
  public void testCDActionLotusScript() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDActionLotusScript.class, script -> {
          script.setScript("Dim foo as NotesBar");
        });
      }

      final CDActionLotusScript script = (CDActionLotusScript) doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
      Assertions.assertEquals("Dim foo as NotesBar", script.getScript());
    });
  }

  @Test
  public void testCDActionModifyField() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDActionModifyField.class, action -> {
          action.setFlags(EnumSet.of(CDActionModifyField.Flag.REPLACE));
          action.setFieldName("FirstName");
          action.setValue("Foo");
        });
      }

      final CDActionModifyField action = (CDActionModifyField) doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
      Assertions.assertEquals(EnumSet.of(CDActionModifyField.Flag.REPLACE), action.getFlags());
      Assertions.assertEquals("FirstName", action.getFieldName());
      Assertions.assertEquals("Foo", action.getValue());
    });
  }

  @Test
  public void testCDActionNewsletter() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
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

      final CDActionNewsletter action = (CDActionNewsletter) doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
      Assertions.assertEquals(EnumSet.of(CDActionNewsletter.Flag.INCLUDEALL, CDActionNewsletter.Flag.GATHER), action.getFlags());
      Assertions.assertEquals(254, action.getGatherCount());
      Assertions.assertEquals("I am the subject", action.getSubject());
      Assertions.assertEquals("hey view", action.getViewName());
      Assertions.assertEquals("I am to", action.getTo());
      Assertions.assertEquals("Secret copy dest", action.getBcc());
      Assertions.assertEquals("Also copy here", action.getCc());
      Assertions.assertEquals("This is the body", action.getBody());
    });
  }

  @ParameterizedTest
  @EnumSource(value = RecordType.class, names = { "ACTION_MARKREAD", "ACTION_MARKUNREAD" })
  public void testCDActionReadMarks(final RecordType recordType) throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(recordType, (final CDActionReadMarks action) -> {
        });
      }

      final CDActionReadMarks action = (CDActionReadMarks) doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
      Assertions.assertEquals((byte) recordType.getConstant(), action.getHeader().getSignature());
    });
  }

  @Test
  public void testCDActionReply() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDActionReply.class, action -> {
          action.setFlags(EnumSet.of(CDActionReply.Flag.REPLYTOALL));
          action.setBody("I am out of office");
        });
      }

      final CDActionReply action = (CDActionReply) doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
      Assertions.assertEquals(EnumSet.of(CDActionReply.Flag.REPLYTOALL), action.getFlags());
      Assertions.assertEquals("I am out of office", action.getBody());
    });
  }

  @Test
  public void testCDActionRunAgent() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDActionRunAgent.class, action -> {
          action.setAgentName("Some agent");
        });
      }

      final CDActionRunAgent action = (CDActionRunAgent) doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
      Assertions.assertEquals("Some agent", action.getAgentName());
    });
  }

  @Test
  public void testCDActionSendDocument() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      try (RichTextWriter rtWriter = doc.createRichTextItem("Body")) {
        rtWriter.addRichTextRecord(CDActionSendDocument.class, action -> {
        });
      }

      @SuppressWarnings("unused")
      final CDActionSendDocument action = (CDActionSendDocument) doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
    });
  }

  @Test
  public void testCDActionSendMail() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
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

      final CDActionSendMail action = (CDActionSendMail) doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
      Assertions.assertEquals(EnumSet.of(CDActionSendMail.Flag.INCLUDEDOC, CDActionSendMail.Flag.INCLUDELINK), action.getFlags());
      Assertions.assertEquals("I am the subject", action.getSubject());
      Assertions.assertEquals("I am to", action.getTo());
      Assertions.assertEquals("Secret copy dest", action.getBcc());
      Assertions.assertEquals("Also copy here", action.getCc());
      Assertions.assertEquals("This is the body", action.getBody());
    });
  }

  @Test
  public void testCDActionSendMailFormulas() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
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

      final CDActionSendMail action = (CDActionSendMail) doc.getRichTextItem("Body", Area.TYPE_ACTION).get(0);
      Assertions.assertEquals(EnumSet.of(CDActionSendMail.Flag.INCLUDEDOC, CDActionSendMail.Flag.INCLUDELINK,
          CDActionSendMail.Flag.CCFORMULA, CDActionSendMail.Flag.TOFORMULA, CDActionSendMail.Flag.SUBJECTFORMULA),
          action.getFlags());
      Assertions.assertEquals("\"I am the subject\"", action.getSubject());
      Assertions.assertEquals("\"I am to\"", action.getTo());
      Assertions.assertEquals("\"Secret copy dest\"", action.getBcc());
      Assertions.assertEquals("\"Also copy here\"", action.getCc());
      Assertions.assertEquals("This is the body", action.getBody());
    });
  }
}
