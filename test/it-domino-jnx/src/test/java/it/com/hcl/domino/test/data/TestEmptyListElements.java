package it.com.hcl.domino.test.data;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.data.Document;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

public class TestEmptyListElements extends AbstractNotesRuntimeTest {

  @Test
  public void testEmptyStringsInList() throws Exception {
    withTempDb((db) -> {
      Document doc = db.createDocument();
      String testTxt = new String(produceTestData(30000), StandardCharsets.US_ASCII);
      List<String> values = Arrays.asList(testTxt, "val1", "", "val2", "val3");
      doc.replaceItemValue("field", values);
      
      doc.save();
      
      final int noteId = doc.getNoteID();
      doc.autoClosable().close();
      doc = db.getDocumentById(noteId).get();

      List<String> checkValues = doc.getAsList("field", String.class, null);
      Assertions.assertNotNull(checkValues);
      Assertions.assertEquals(values, checkValues);
    });
  }
  
  @Test
  public void testListWithEmptyString() throws Exception {
    withTempDb((db) -> {
      Document doc = db.createDocument();
      List<String> values = Arrays.asList("");
      doc.replaceItemValue("field", values);
      
      doc.save();
      
      final int noteId = doc.getNoteID();
      doc.autoClosable().close();
      doc = db.getDocumentById(noteId).get();

      List<String> checkValues = doc.getAsList("field", String.class, null);
      Assertions.assertNotNull(checkValues);
      Assertions.assertEquals(values, checkValues);
    });
  }
  
}
