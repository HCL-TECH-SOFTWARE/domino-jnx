package it.com.hcl.domino.test.design;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.design.DatabaseScriptLibrary;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.jnx.vertx.json.service.VertxJsonSerializer;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

public class TestDatabaseScriptCreation extends AbstractNotesRuntimeTest {

  @SuppressWarnings("nls")
  @Test
  public void testDbScript() throws Exception {
    withTempDb((db) -> {
      DbDesign dbDesign = db.getDesign();
      Optional<DatabaseScriptLibrary> dbScript = dbDesign.getDatabaseScriptLibrary();
      Assertions.assertFalse(dbScript.isPresent());
      
      DatabaseScriptLibrary newDbScript = dbDesign.createDatabaseScriptLibrary();
      Assertions.assertNotNull(newDbScript);
      Assertions.assertEquals("Database Script", newDbScript.getTitle());
      
      String scriptContent = newDbScript.getScript();
      Assertions.assertTrue(scriptContent.contains("LotusScript Development Environment"));
      Assertions.assertTrue(scriptContent.contains("Option Declare"));
      newDbScript.setScript("Option Declare\nREM testcontent\n");
      newDbScript.save();
      
      String dbScriptUnid = newDbScript.getDocument().getUNID();
      
      dbScript = dbDesign.getDatabaseScriptLibrary();
      Assertions.assertTrue(dbScript.isPresent());
      
      Assertions.assertEquals(dbScriptUnid, dbScript.get().getDocument().getUNID());
      
      scriptContent = dbScript.get().getScript();
      Assertions.assertTrue(scriptContent.contains("REM testcontent"));
      
      VertxJsonSerializer serializer = new VertxJsonSerializer();
      String json = serializer.toJson(dbScript.get()).encodePrettily();
      Assertions.assertTrue(json.contains("REM testcontent"));
    });
    
  }
}
