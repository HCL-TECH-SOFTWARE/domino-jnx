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
package it.com.hcl.domino.test.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Database.OpenDocumentMode;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.data.Item;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.dbdirectory.DirectorySearchQuery.SearchFlag;
import com.hcl.domino.exception.LotusScriptCompilationException;
import com.hcl.domino.exception.MismatchedPublicKeyException;
import com.hcl.domino.exception.NoCrossCertificateException;
import com.hcl.domino.exception.NotAuthorizedException;
import com.hcl.domino.security.Acl;
import com.hcl.domino.security.AclLevel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDocuments extends AbstractNotesRuntimeTest {

  @Test
  public void testAppendItemValueVector() throws Exception {
    this.withTempDb(database -> {
      final Document testdoc = database.createDocument();
      final Vector<String> someobject = new Vector<>();
      someobject.add("cat1");
      someobject.add("cat2");
      testdoc.appendItemValue("categories", someobject);
      Assertions.assertEquals(someobject, testdoc.getAsList("categories", String.class, null));
    });
  }

  @Test
  public void testAppendToTextList() throws Exception {
    this.withTempDb(database -> {
      final Document testdoc = database.createDocument();

      final List<String> values = new ArrayList<>();
      values.add("A");
      values.add("B");
      testdoc.replaceItemValue("list", values);
      Assertions.assertEquals(values, testdoc.getAsList("list", String.class, null));

      testdoc.appendToTextList("list", "C", true);
      testdoc.appendToTextList("list", "D", true);
      testdoc.appendToTextList("list", "A", false);

      values.add("C");
      values.add("D");
      Assertions.assertEquals(values, testdoc.getAsList("list", String.class, null));
    });
  }

  @Test
  public void testCompileInvalidLotusScript() throws Exception {
    this.withResourceDxl("/dxl/testCompileLotusScript", database -> {
      final Document doc = database
          .queryFormula("$TITLE='Test LS'", null, EnumSet.noneOf(SearchFlag.class), null, EnumSet.of(DocumentClass.ALLNONDATA))
          .getDocuments()
          .findFirst()
          .orElseThrow(() -> new IllegalStateException("Unable to find Test LS Library"));
      Assertions.assertFalse(doc.hasItem("$ScriptLib_O"), "Library should not yet have compiled LotusScript");
      Assertions.assertThrows(LotusScriptCompilationException.class, () -> doc.compileLotusScript());
      Assertions.assertFalse(doc.hasItem("$ScriptLib_O"), "Library should still not have compiled LotusScript");
    });
  }

  @Test
  public void testCompileLotusScript() throws Exception {
    this.withResourceDxl("/dxl/testCompileLotusScript", database -> {
      final Document doc = database
          .queryFormula("$TITLE='Test LS Form'", null, EnumSet.noneOf(SearchFlag.class), null, EnumSet.of(DocumentClass.ALLNONDATA))
          .getDocuments()
          .findFirst()
          .orElseThrow(() -> new IllegalStateException("Unable to find Test LS Form"));
      Assertions.assertFalse(doc.hasItem("$SCRIPTOBJ_0"), "Form should not yet have compiled LotusScript");
      doc.compileLotusScript();
      Assertions.assertTrue(doc.hasItem("$SCRIPTOBJ_0"), "Form should now have compiled LotusScript");
    });
  }

  @Test
  public void testDocumentInvalidCreationDate() throws Exception {
    this.withResourceDb("/nsf/invalidtimedate.nsf", database -> {
      final CollectionEntry entry = database.queryDocuments()
          .computeValues("Created", "@Created")
          .collectEntries()
          .get(0);

      DominoDateTime dt = entry.get("Created", DominoDateTime.class, null);
      Assertions.assertNotNull(dt);
      Assertions.assertFalse(dt.isValid());

      final Document doc = database
          .queryFormula(" Subject='**** Calendaring and Scheduling Meta Data Doc - DO NOT MODIFY ****' ", null,
              Collections.emptySet(), null, EnumSet.of(DocumentClass.DOCUMENT))
          .getDocuments()
          .findFirst()
          .get();
      dt = doc.getCreated();
      Assertions.assertNotNull(dt);
      Assertions.assertFalse(dt.isValid());
    });
  }

  @Test
  public void testForEachItemName() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("foo", "bar");
      doc.replaceItemValue("bar", "baz");
      doc.forEachItem("foo", (item, loop) -> {
        Assertions.assertEquals("foo", item.getName());
      });
    });
  }

  @Test
  public void testNamesNsfAclNote() {
    final DominoClient client = this.getClient();
    final Database names = client.openDatabase("names.nsf"); //$NON-NLS-1$

    final Document acl = names.getDocumentById(0xFFFF0000 | 0x0040).get(); // ACL note by Special ID
    Assertions.assertNotEquals(null, acl);

    final Set<DocumentClass> classes = acl.getDocumentClass();
    Assertions.assertNotEquals(null, classes);
    Assertions.assertTrue(classes.contains(DocumentClass.ACL));
    Assertions.assertFalse(classes.contains(DocumentClass.DATA));
  }

  @Test
  public void testNamesNsfNewNote() {
    final DominoClient client = this.getClient();
    final Database names = client.openDatabase("names.nsf"); //$NON-NLS-1$

    final Document note = names.createDocument();
    Assertions.assertNotEquals(null, note);

    note.replaceItemValue("foo", "bar");
    Assertions.assertEquals("bar", note.get("foo", String.class, ""));

    Assertions.assertEquals("", note.get("does-not-exist", String.class, ""));

    note.setUNID("12345678901234567890123456789012");
    Assertions.assertEquals("12345678901234567890123456789012", note.getUNID());
  }

  /**
   * Ensures that doc.removeItem can be called directly without performing a
   * hasItem test
   *
   * @throws Exception if a test problem occurs
   */
  @Test
  public void testRemoveFakeItem() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      doc.replaceItemValue("Heh", "Hey");
      doc.removeItem("FooBarBaz");
      Assertions.assertEquals("Hey", doc.getAsText("Heh", ' '));
    });
  }

  @Test
  public void testRemoveItem() throws Exception {
    this.withTempDb(database -> {
      final Document doc = database.createDocument();
      //create three items
      doc.appendItemValue("itemname", "item1value");
      doc.appendItemValue("itemname", "item2value");
      doc.appendItemValue("itemname", "item3value");
      
      long itemCountBefore = doc.allItems()
      .filter((item) -> { return "itemname".equalsIgnoreCase(item.getName()); })
      .count();
      assertEquals(3, itemCountBefore);
      
      doc.removeItem("itemname");

      //check if all three got removed
      long itemCountAfter = doc.allItems()
      .filter((item) -> { return "itemname".equalsIgnoreCase(item.getName()); })
      .count();
      assertEquals(0, itemCountAfter);
    });
  }
  
  @Test
  public void testResponseCount() throws Exception {
    this.withTempDb(database -> {
      String parentUnid;
      {
        final Document parent = database.createDocument();
        parent.replaceItemValue("Form", "Parent");
        parent.save();
        Assertions.assertEquals("", parent.getParentDocumentUNID());
        parentUnid = parent.getUNID();
      }

      for (int i = 0; i < 10; i++) {
        final Document child = database.createDocument();
        child.replaceItemValue("Form", "Child");
        child.makeResponse(parentUnid);
        child.save();
      }

      final Document parent = database.getDocumentByUNID(parentUnid).get();
      Assertions.assertEquals(10, parent.getResponseCount());
    });
  }

  @Test
  public void testResponseParent() throws Exception {
    this.withTempDb(database -> {
      String parentUnid;
      {
        final Document parent = database.createDocument();
        parent.replaceItemValue("Form", "Parent");
        parent.save();
        Assertions.assertEquals("", parent.getParentDocumentUNID());
        parentUnid = parent.getUNID();
      }

      final Document child = database.createDocument();
      child.replaceItemValue("Form", "Child");
      child.makeResponse(parentUnid);
      child.save();

      final String unid = child.getParentDocumentUNID();
      Assertions.assertEquals(parentUnid, unid);
      final Document parent = database.getDocumentByUNID(unid).get();
      Assertions.assertEquals("Parent", parent.get("Form", String.class, null));
    });
  }

  @Test
  public void testResponseTable() throws Exception {
    this.withTempDb(database -> {
      String parentUnid;
      {
        final Document parent = database.createDocument();
        parent.replaceItemValue("Form", "Parent");
        parent.save();
        Assertions.assertEquals("", parent.getParentDocumentUNID());
        parentUnid = parent.getUNID();
      }

      final Set<String> childIds = new HashSet<>();

      for (int i = 0; i < 10; i++) {
        final Document child = database.createDocument();
        child.replaceItemValue("Form", "Child");
        child.makeResponse(parentUnid);
        child.save();
        childIds.add(child.getUNID());
      }

      final Document parent = database.getDocumentByUNID(parentUnid, EnumSet.of(OpenDocumentMode.LOAD_RESPONSES)).get();
      Assertions.assertEquals(10, parent.getResponseCount());
      final IDTable children = parent.getResponses();
      final Set<String> foundChildIds = children.stream()
          .map(database::getDocumentById)
          .map(Optional::get)
          .map(Document::getUNID)
          .collect(Collectors.toSet());
      Assertions.assertEquals(childIds, foundChildIds);
    });
  }

  @Test
  public void testSetGetBasicItemTypes() throws Exception {
    this.withTempDb(database -> {
      final Document testdoc = database.createDocument();

      {
        final String str = "testval";
        testdoc.replaceItemValue("strvalue", str);
        Assertions.assertEquals(str, testdoc.get("strvalue", String.class, null));
      }
      {
        final DominoDateTime dt = this.getClient().createDateTime(
            Instant.now().with(ChronoField.MILLI_OF_SECOND, 50) // Domino can only store 1/100 seconds
        );
        testdoc.replaceItemValue("datevalue", dt);
        Assertions.assertEquals(dt, testdoc.get("datevalue", DominoDateTime.class, null));
      }
      {
        final int nr = 1352;
        testdoc.replaceItemValue("intvalue", nr);
        Assertions.assertEquals(nr, testdoc.get("intvalue", Integer.class, null));
      }
      {
        final double nr = 13.52;
        testdoc.replaceItemValue("doublevalue", nr);
        Assertions.assertEquals(nr, testdoc.get("doublevalue", Double.class, null));
      }

      {
        final List<String> strValues = Arrays.asList("A", "B");
        testdoc.replaceItemValue("strvalues", strValues);
        Assertions.assertEquals(strValues, testdoc.getAsList("strvalues", String.class, null));
        Assertions.assertEquals(strValues.get(0), testdoc.get("strvalues", String.class, null));
      }
      {
        final List<Integer> intValues = Arrays.asList(1, 3, 5, 2);
        testdoc.replaceItemValue("intvalues", intValues);
        Assertions.assertEquals(intValues, testdoc.getAsList("intvalues", Integer.class, null));
        Assertions.assertEquals(intValues.get(0), testdoc.get("intvalues", Integer.class, null));
      }
      {
        final List<Double> doubleValues = Arrays.asList(1.5, 3.4, 5.3, 2.1);
        testdoc.replaceItemValue("doublevalues", doubleValues);
        Assertions.assertEquals(doubleValues, testdoc.getAsList("doublevalues", Double.class, null));
        Assertions.assertEquals(doubleValues.get(0), testdoc.get("doublevalues", Double.class, null));
      }
      {
        final DominoDateTime dt1 = this.getClient().createDateTime(
            Instant.now().with(ChronoField.MILLI_OF_SECOND, 40) // Domino can only store 1/100 seconds
        );
        final DominoDateTime dt2 = this.getClient().createDateTime(
            Instant.now().plusSeconds(10000).with(ChronoField.MILLI_OF_SECOND, 50) // Domino can only store 1/100 seconds
        );
        final List<DominoDateTime> dtValues = Arrays.asList(dt1, dt2);
        testdoc.replaceItemValue("datevalues", dtValues);
        Assertions.assertEquals(dtValues, testdoc.getAsList("datevalues", DominoDateTime.class, null));
        Assertions.assertEquals(dtValues.get(0), testdoc.get("datevalues", DominoDateTime.class, null));
      }
    });
  }

  @Test
  public void writeDesignNote() throws Exception {
    this.withTempDb(database -> {
      String docId;
      {
        final Document doc = database.createDocument();
        Assertions.assertFalse(doc.getDocumentClass().contains(DocumentClass.FORM));
        Assertions.assertTrue(doc.getDocumentClass().contains(DocumentClass.DATA));
        doc.setDocumentClass(DocumentClass.FORM);
        Assertions.assertTrue(doc.getDocumentClass().contains(DocumentClass.FORM));
        Assertions.assertFalse(doc.getDocumentClass().contains(DocumentClass.DATA));
        doc.save();
        docId = doc.getUNID();
      }
      {
        final Document doc = database.getDocumentByUNID(docId).get();
        Assertions.assertTrue(doc.getDocumentClass().contains(DocumentClass.FORM));
        Assertions.assertFalse(doc.getDocumentClass().contains(DocumentClass.DATA));
      }
    });
  }
  
  @Test
  public void testWriteSet() throws Exception {
    withTempDb(database -> {
      Document doc = database.createDocument();
      doc.replaceItemValue("foo", Collections.singleton("hey"));
      assertEquals("hey", doc.get("foo", String.class, null));
    });
  }
  
  @Test
  public void testWriteIterableString() throws Exception {
    SomeIterable<String> someIterable = new SomeIterable<String>("foo", "bar");

    withTempDb(database -> {
      Document doc = database.createDocument();
      doc.replaceItemValue("foo", someIterable);
      assertIterableEquals(someIterable.list, doc.getAsList("foo", String.class, null));
    });
  }
  
  @Test
  public void testWriteIterableNumbers() throws Exception {
    SomeIterable<Double> someIterable = new SomeIterable<Double>(1d, 4d);

    withTempDb(database -> {
      Document doc = database.createDocument();
      doc.replaceItemValue("foo", someIterable);
      assertIterableEquals(someIterable.list, doc.getAsList("foo", Double.class, null));
    });
  }
  
  @Test
  public void testWriteIterableNumbers2() throws Exception {
    SomeIterable<Number> someIterable = new SomeIterable<Number>(1d, 4d, 8);

    withTempDb(database -> {
      Document doc = database.createDocument();
      doc.replaceItemValue("foo", someIterable);
      assertIterableEquals(
          someIterable.list.stream()
            .map(Number::doubleValue)
            .collect(Collectors.toList()),
          doc.getAsList("foo", Double.class, null)
      );
    });
  }
  
  @Test
  public void testWriteIterableDates() throws Exception {
    SomeIterable<Temporal> someIterable = new SomeIterable<Temporal>(LocalDate.of(2022, 1, 31), LocalDate.of(2040, 2, 2));

    withTempDb(database -> {
      Document doc = database.createDocument();
      doc.replaceItemValue("foo", someIterable);
      assertIterableEquals(
          someIterable.list,
          doc.getAsList("foo", DominoDateTime.class, null)
            .stream()
            .map(DominoDateTime::toLocalDate)
            .collect(Collectors.toList())
      );
    });
  }
  
  private class SomeIterable<T> implements Iterable<T> {
    List<T> list;
    
    public SomeIterable(@SuppressWarnings("unchecked") T... vals) {
      list = Arrays.asList(vals);
    }
    
    @Override
    public Iterator<T> iterator() {
      return list.iterator();
    }
  }
  
  @Test
  public void testAccessReaderFieldDocument() throws Exception {
    withTempDb(database -> {
      String otherUser = "CN=Some User/O=SomeOrg";
      Acl acl = database.getACL();
      acl.addEntry(otherUser, AclLevel.EDITOR, null, null);
      acl.save();
      
      Document doc = database.createDocument();
      String userName = database.getParentDominoClient().getEffectiveUserName();
      doc.replaceItemValue("Readers", EnumSet.of(ItemFlag.READERS), userName);
      String expected = "hello" + System.nanoTime();
      doc.replaceItemValue("TextField", expected);
      doc.save();
      
      int id = doc.getNoteID();
      String dbPath = database.getAbsoluteFilePath();
      
      try(DominoClient sameUserClient = DominoClientBuilder.newDominoClient().asUser(userName).build()) {
        Database sameUserDb = sameUserClient.openDatabase(dbPath);
        Document sameUserDoc = sameUserDb.getDocumentById(id).get();
        Assertions.assertEquals(expected, sameUserDoc.get("TextField", String.class, null));
      }
      
      try(DominoClient otherUserClient = DominoClientBuilder.newDominoClient().asUser(otherUser).build()) {
        Database otherUserDb = otherUserClient.openDatabase(dbPath);
        Assertions.assertThrows(NotAuthorizedException.class, () -> otherUserDb.getDocumentById(id));
      }
    });
  }
  
  @Test
  public void testAccessReaderFieldDocumentThreads() throws Exception {
    withTempDb(database -> {
      String otherUser = "CN=Some User/O=SomeOrg";
      Acl acl = database.getACL();
      acl.addEntry(otherUser, AclLevel.EDITOR, null, null);
      acl.save();
      
      Document doc = database.createDocument();
      String userName = database.getParentDominoClient().getEffectiveUserName();
      doc.replaceItemValue("Readers", EnumSet.of(ItemFlag.READERS), userName);
      String expected = "hello" + System.nanoTime();
      doc.replaceItemValue("TextField", expected);
      doc.save();
      
      int id = doc.getNoteID();
      String dbPath = database.getAbsoluteFilePath();
      
      try(DominoClient sameUserClient = DominoClientBuilder.newDominoClient().asUser(userName).build()) {
        Database sameUserDb = sameUserClient.openDatabase(dbPath);
        Document sameUserDoc = sameUserDb.getDocumentById(id).get();
        Assertions.assertEquals(expected, sameUserDoc.get("TextField", String.class, null));
      }

      int runCount = 200;
      ExecutorService exec = Executors.newFixedThreadPool(runCount, database.getParentDominoClient().getThreadFactory());

      AtomicInteger throwCount = new AtomicInteger(0);
      
      for(int i = 0; i < runCount; i++) {
        exec.submit(() -> {
          try(DominoClient sameUserClient = DominoClientBuilder.newDominoClient().asUser(userName).build()) {
            Database sameUserDb = sameUserClient.openDatabase(dbPath);
            try {
              sameUserDb.getDocumentById(id);
            } catch(NotAuthorizedException e) {
              throwCount.incrementAndGet();
            }
          }
        });
      }

      exec.shutdown();
      exec.awaitTermination(2, TimeUnit.MINUTES);
      
      assertEquals(0, throwCount.get());
      
      exec = Executors.newFixedThreadPool(runCount, database.getParentDominoClient().getThreadFactory());
      for(int i = 0; i < runCount; i++) {
        exec.submit(() -> {
          try(DominoClient otherUserClient = DominoClientBuilder.newDominoClient().asUser(otherUser).build()) {
            Database otherUserDb = otherUserClient.openDatabase(dbPath);
            try {
              otherUserDb.getDocumentById(id);
            } catch(NotAuthorizedException e) {
              throwCount.incrementAndGet();
            }
          }
        });
      }
      
      exec.shutdown();
      exec.awaitTermination(2, TimeUnit.MINUTES);
      assertEquals(runCount, throwCount.get());
    });
  }
  
  @Test
  public void testPlaceholder() throws Exception {
    withTempDb(database -> {
      Document doc = database.createDocument();
      doc.replaceItemValuePlaceholder("Foo");
      Item item = doc.getFirstItem("Foo").get();
      assertEquals(ItemDataType.TYPE_INVALID_OR_UNKNOWN, item.getType());
      assertIterableEquals(EnumSet.of(ItemFlag.PLACEHOLDER), item.getFlags());
    });
  }
  
  @Test
  public void testItemGetAsText() throws Exception {
    withTempDb(database -> {
      Document doc = database.createDocument();
      doc.replaceItemValue("Foo", "Bar");
      Item item = doc.getFirstItem("Foo").get();
      assertEquals("Bar", item.getAsText(' '));
    });
  }
  
  @Test
  public void testItemGetAsTextNumberList() throws Exception {
    withTempDb(database -> {
      Document doc = database.createDocument();
      doc.replaceItemValue("Foo", Arrays.asList(1, 2, 3));
      Item item = doc.getFirstItem("Foo").get();
      assertEquals("1 2 3", item.getAsText(' '));
    });
  }
  
  @Test
  public void testItemGetAsTextNumberMultiItem() throws Exception {
    withTempDb(database -> {
      Document doc = database.createDocument();
      doc.replaceItemValue("Foo", Arrays.asList(1, 2, 3));
      doc.appendItemValue("Foo", Arrays.asList(4, 5, 6));
      int[] i = new int[1];
      doc.forEachItem("Foo", (item, loop) -> {
        if(i[0]++ == 0) {
          assertEquals("1 2 3", item.getAsText(' '));
        } else {
          assertEquals("4 5 6", item.getAsText(' '));
        }
      });
    });
  }
  
  @Test
  public void testItemGetAsInt() throws Exception {
    withTempDb(database -> {
      Document doc = database.createDocument();
      doc.replaceItemValue("Foo", "dsf");
      assertEquals(21, doc.getAsInt("Foo", 21));
      doc.replaceItemValue("Bar", 43);
      assertEquals(43, doc.getAsInt("Bar", 0));
      doc.replaceItemValue("Baz", 82.1);
      assertEquals(82, doc.getAsInt("Baz", 1));
      assertEquals(-2, doc.getAsInt("Fake", -2));
    });
  }
  
  @Test
  public void testReadForeignSigner() throws Exception {
    withResourceDb("/nsf/signeddoc.nsf", database -> {
      int noteId = database.openDefaultCollection().get().getAllIds(true, false).iterator().next();
      Document doc = database.getDocumentById(noteId).get();
      try {
        String signer = doc.getSigner();
        assertNotNull(signer);
        assertFalse(signer.isEmpty());
      } catch(NoCrossCertificateException e) {
        // Presumably legit signature that we don't know - fine
      } catch(MismatchedPublicKeyException e) {
        // Presumably legit signature from a dev environment that changed - fine
      }
    });
  }
}
