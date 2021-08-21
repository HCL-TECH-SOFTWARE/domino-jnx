package it.com.hcl.domino.test.vertx.json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.hcl.domino.data.Database;
import com.hcl.domino.design.Form;
import com.hcl.domino.jnx.vertx.json.service.VertxJsonSerializer;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

/**
 * @author Jesse Gallagher
 * @since 1.0.33
 */
@SuppressWarnings("nls")
public class TestVertxDesignJsonSerialization extends AbstractNotesRuntimeTest {

  // Run against a handful of common stock NTFs to get a spread of examples
  @ParameterizedTest
  @ValueSource(strings = {
    "pernames.ntf",
    "bookmark.ntf",
    "log.ntf",
    "mailbox.ntf",
    "roamingdata.ntf",
    "autosave.ntf",
    "doclbs7.ntf",
    "headline.ntf",
    "busytime.ntf"
  })
  public void testViewSerialization(String dbName) {
    VertxJsonSerializer serializer = new VertxJsonSerializer();
    
    Database names = getClient().openDatabase(dbName);
    names.getDesign()
      .getViews()
      .forEach(view -> {
        serializer.toJson(view);
      });
  }
  
  @Test
  public void testFormBasicSerialization() throws Exception {
    VertxJsonSerializer serializer = new VertxJsonSerializer();
    
    withResourceDxl("/dxl/testDbDesignForms", database -> {
      Form form = database.getDesign().getForm("Test LS Form").get();
      serializer.toJson(form);
    });
  }
  
  @ParameterizedTest
  @ValueSource(strings = {
    "pernames.ntf",
    "bookmark.ntf",
    "log.ntf",
    "mailbox.ntf",
    "roamingdata.ntf",
    "autosave.ntf",
    "doclbs7.ntf",
    "headline.ntf",
    "busytime.ntf"
  })
  public void testFormSerialization(String dbName) {
    VertxJsonSerializer serializer = new VertxJsonSerializer();
    
    Database names = getClient().openDatabase(dbName);
    names.getDesign()
      .getForms()
      .forEach(form -> {
        serializer.toJson(form);
      });
  }
  
  @ParameterizedTest
  @ValueSource(strings = {
    "pernames.ntf",
    "bookmark.ntf",
    "log.ntf",
    "mailbox.ntf",
    "roamingdata.ntf",
    "autosave.ntf",
    "doclbs7.ntf",
    "headline.ntf",
    "busytime.ntf"
  })
  public void testSubformSerialization(String dbName) {
    VertxJsonSerializer serializer = new VertxJsonSerializer();
    
    Database names = getClient().openDatabase(dbName);
    names.getDesign()
      .getSubforms()
      .forEach(form -> {
        serializer.toJson(form);
      });
  }
}
