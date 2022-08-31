package it.com.hcl.domino.test.data;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.UserData;
import com.hcl.domino.dql.DQL;
import com.hcl.domino.dql.DQL.DQLTerm;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestUserData extends AbstractNotesRuntimeTest {

  @Test
  public void testCreateUserData() {
    String formatName = "Some Format";
    byte[] someData = "hello".getBytes();
    
    UserData data = getClient().createUserData(formatName, someData);
    assertNotNull(data);
    assertEquals(formatName, data.getFormatName());
    assertArrayEquals(someData, data.getData());
  }
  
  @Test
  public void testUserDataRoundTrip() throws Exception {
    withTempDb(database -> {
      String formatName = "Some Format2";
      byte[] someData = "hello2".getBytes();

      Document doc = database.createDocument();
      {
        UserData data = getClient().createUserData(formatName, someData);
        doc.replaceItemValue("SomeUserData", data);
      }
      {
        UserData data = doc.get("SomeUserData", UserData.class, null);
        assertNotNull(data);
        assertEquals(formatName, data.getFormatName());
        assertArrayEquals(someData, data.getData());
      }
    });
  }
  
  @Test
  public void testExistingUserData() throws Exception {
    withResourceDxl("/dxl/testUserData", database -> {
      Document doc = database.queryDQL(DQL.item("Form").isEqualTo("UserDataForm"))
          .getDocuments()
          .findFirst()
          .get();
      UserData data = doc.get("SomeUserDataItem", UserData.class, null);
      assertNotNull(data);
      assertEquals("Format From Agent", data.getFormatName());
      assertArrayEquals("data from agent".getBytes(), data.getData());
    });
  }
}
