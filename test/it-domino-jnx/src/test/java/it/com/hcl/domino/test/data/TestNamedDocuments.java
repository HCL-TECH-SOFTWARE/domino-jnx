package it.com.hcl.domino.test.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Database.NamedObjectInfo;
import com.hcl.domino.data.Document;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

public class TestNamedDocuments extends AbstractNotesRuntimeTest {

  @Test
  public void testNamedDocuments() throws Exception {
    withTempDb((db) -> {
      Assertions.assertThrows(IllegalArgumentException.class, () -> {
        db.getNamedDocument("invalid*name"); //$NON-NLS-1$
      });
      Assertions.assertThrows(IllegalArgumentException.class, () -> {
        db.getNamedDocument("name", "invalid*username"); //$NON-NLS-1$ //$NON-NLS-2$
      });

      Set<Integer> allNoteIds = new HashSet<>();
      Set<Integer> testCategoryNoteIds = new HashSet<>();
      Set<Integer> testCategory2NoteIds = new HashSet<>();

      Document doc1;
      {
        doc1 = db.getNamedDocument("testcategory"); //$NON-NLS-1$
        Assertions.assertNotNull(doc1);
        Assertions.assertTrue(doc1.isNew());
        Assertions.assertEquals("testcategory", doc1.getNameOfDoc()); //$NON-NLS-1$
        Assertions.assertEquals("", doc1.getUserNameOfDoc()); //$NON-NLS-1$
        doc1.save();
        allNoteIds.add(doc1.getNoteID());
        testCategoryNoteIds.add(doc1.getNoteID());
      }

      Document doc2;
      {
        doc2 = db.getNamedDocument("testcategory", "ausername"); //$NON-NLS-1$ //$NON-NLS-2$
        Assertions.assertNotNull(doc2);
        Assertions.assertTrue(doc2.isNew());
        Assertions.assertEquals("testcategory", doc2.getNameOfDoc()); //$NON-NLS-1$
        Assertions.assertEquals("ausername", doc2.getUserNameOfDoc()); //$NON-NLS-1$
        doc2.save();
        allNoteIds.add(doc2.getNoteID());
        testCategoryNoteIds.add(doc2.getNoteID());
      }
      
      Document doc3;
      {
        doc3 = db.getNamedDocument("testcategory", "CN=Mr Tester/O=ACME"); //$NON-NLS-1$ //$NON-NLS-2$
        Assertions.assertNotNull(doc3);
        Assertions.assertTrue(doc3.isNew());
        Assertions.assertEquals("testcategory", doc3.getNameOfDoc()); //$NON-NLS-1$
        Assertions.assertEquals("CN=Mr Tester/O=ACME", doc3.getUserNameOfDoc()); //$NON-NLS-1$
        doc3.save();
        allNoteIds.add(doc3.getNoteID());
        testCategoryNoteIds.add(doc3.getNoteID());
      }

      Document doc4;
      {
        doc4 = db.getNamedDocument("testcategory2", "CN=Mr Tester2/O=ACME");  //$NON-NLS-1$//$NON-NLS-2$
        Assertions.assertNotNull(doc4);
        Assertions.assertTrue(doc4.isNew());
        Assertions.assertEquals("testcategory2", doc4.getNameOfDoc()); //$NON-NLS-1$
        Assertions.assertEquals("CN=Mr Tester2/O=ACME", doc4.getUserNameOfDoc()); //$NON-NLS-1$
        doc4.save();
        allNoteIds.add(doc4.getNoteID());
        testCategory2NoteIds.add(doc4.getNoteID());
      }

      Collection<NamedObjectInfo> allInfos = db.getNamedDocumentInfos();
      Assertions.assertEquals(4, allInfos.size());
      Assertions.assertEquals(allNoteIds, allInfos.stream().map(NamedObjectInfo::getNoteID).collect(Collectors.toSet()));
      
      int doc2NoteId = doc2.getNoteID();
      int doc3NoteId = doc3.getNoteID();
      int doc4NoteId = doc4.getNoteID();
      
      allInfos.forEach((entry) -> {
        Assertions.assertTrue(StringUtil.isNotEmpty(entry.getNameOfDocument()));
        Assertions.assertTrue(entry.getNoteID()>0);
        
        if (entry.getNoteID() == doc2NoteId || entry.getNoteID() == doc3NoteId || entry.getNoteID() == doc4NoteId) {
          Assertions.assertTrue(StringUtil.isNotEmpty(entry.getUserNameOfDocument()));
        }
      });
      
      Collection<NamedObjectInfo> testCategoryInfos = db.getNamedDocumentInfos("testcategory"); //$NON-NLS-1$
      Assertions.assertEquals(3, testCategoryInfos.size());
      Assertions.assertEquals(testCategoryNoteIds, testCategoryInfos.stream().map(NamedObjectInfo::getNoteID).collect(Collectors.toSet()));
      
      Collection<NamedObjectInfo> testCategory2Infos = db.getNamedDocumentInfos("testcategory2"); //$NON-NLS-1$
      Assertions.assertEquals(1, testCategory2Infos.size());
      Assertions.assertEquals(testCategory2NoteIds, testCategory2Infos.stream().map(NamedObjectInfo::getNoteID).collect(Collectors.toSet()));
      
      Document doc1Check = db.getNamedDocument("testcategory"); //$NON-NLS-1$
      Assertions.assertNotNull(doc1Check);
      Assertions.assertEquals(doc1.getNoteID(), doc1Check.getNoteID());
      Assertions.assertFalse(doc1Check.isNew());
      
      doc2.delete();
      
      Document doc2Check = db.getNamedDocument("testcategory", "ausername"); //$NON-NLS-1$ //$NON-NLS-2$
      Assertions.assertNotNull(doc2Check);
      Assertions.assertNotEquals(doc2.getNoteID(), doc2Check.getNoteID());
      Assertions.assertTrue(doc2Check.isNew());
      
    });
  }
}
