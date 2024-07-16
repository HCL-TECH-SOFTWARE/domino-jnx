package it.com.hcl.domino.test.data;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import com.hcl.domino.data.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

public class TestProfileDocuments extends AbstractNotesRuntimeTest {

  @Test
  public void testProfileDocuments() throws Exception {
    withTempDb((db) -> {
      Set<Integer> allNoteIds = new HashSet<>();
      Set<Integer> testCategoryNoteIds = new HashSet<>();
      Set<Integer> testCategory2NoteIds = new HashSet<>();

      Document doc1;
      {
        doc1 = db.getProfileDocument("testcategory").orElse(null); //$NON-NLS-1$
        Assertions.assertNotNull(doc1);
        Assertions.assertEquals("testcategory", doc1.getProfileName()); //$NON-NLS-1$
        Assertions.assertEquals("", doc1.getProfileUserName()); //$NON-NLS-1$
        doc1.save();
        allNoteIds.add(doc1.getNoteID());
        testCategoryNoteIds.add(doc1.getNoteID());
      }

      Document doc2;
      {
        doc2 = db.getProfileDocument("testcategory", "ausername").orElse(null); //$NON-NLS-1$ //$NON-NLS-2$
        Assertions.assertNotNull(doc2);
        Assertions.assertEquals("testcategory", doc2.getProfileName()); //$NON-NLS-1$
        Assertions.assertEquals("ausername", doc2.getProfileUserName()); //$NON-NLS-1$
        doc2.save();
        allNoteIds.add(doc2.getNoteID());
        testCategoryNoteIds.add(doc2.getNoteID());
      }
      
      Document doc3;
      {
        doc3 = db.getProfileDocument("testcategory", "CN=Mr Tester/O=ACME").orElse(null); //$NON-NLS-1$ //$NON-NLS-2$
        Assertions.assertNotNull(doc3);
        Assertions.assertEquals("testcategory", doc3.getProfileName()); //$NON-NLS-1$
        Assertions.assertEquals("CN=Mr Tester/O=ACME".toLowerCase(), doc3.getProfileUserName()); //$NON-NLS-1$
        doc3.save();
        allNoteIds.add(doc3.getNoteID());
        testCategoryNoteIds.add(doc3.getNoteID());
      }

      Document doc4;
      {
        doc4 = db.getProfileDocument("testcategory2", "CN=Mr Tester2/O=ACME").orElse(null);  //$NON-NLS-1$//$NON-NLS-2$
        Assertions.assertNotNull(doc4);
        Assertions.assertEquals("testcategory2", doc4.getProfileName()); //$NON-NLS-1$
        Assertions.assertEquals("CN=Mr Tester2/O=ACME".toLowerCase(), doc4.getProfileUserName()); //$NON-NLS-1$
        doc4.save();
        allNoteIds.add(doc4.getNoteID());
        testCategory2NoteIds.add(doc4.getNoteID());
      }

      AtomicInteger count = new AtomicInteger(0);
      
      db.forEachProfileDocument((doc, loop) -> {
        count.incrementAndGet();
      });
      Assertions.assertEquals(4, count.get());
      
      count.set(0);
      db.forEachProfileDocument("testcategory", (doc, loop) -> {
        count.incrementAndGet();
      });
      Assertions.assertEquals(3, count.get());
      
      count.set(0);
      db.forEachProfileDocument("testcategory2", (doc, loop) -> {
        count.incrementAndGet();
      });
      Assertions.assertEquals(1, count.get());
      
      count.set(0);
      db.forEachProfileDocument("testcategory", "CN=Mr Tester/O=ACME", (doc, loop) -> {
        count.incrementAndGet();
      });
      Assertions.assertEquals(1, count.get());
      
      count.set(0);
      db.forEachProfileDocument("testcategory2", "CN=Mr Tester2/O=ACME", (doc, loop) -> {
        count.incrementAndGet();
      });
      Assertions.assertEquals(1, count.get());
    });
  }
}
