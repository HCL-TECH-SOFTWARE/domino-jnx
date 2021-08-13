package it.com.hcl.domino.test.vertx.json;

import org.junit.jupiter.api.Test;

import com.hcl.domino.data.Database;
import com.hcl.domino.jnx.vertx.json.service.VertxJsonSerializer;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

/**
 * @author Jesse Gallagher
 * @since 1.0.33
 */
public class TestVertxDesignJsonSerialization extends AbstractNotesRuntimeTest {

  @Test
  public void testViewSerialization() {
    VertxJsonSerializer serializer = new VertxJsonSerializer();
    
    Database names = getClient().openDatabase("names.nsf");
    names.getDesign()
      .getViews()
      .forEach(view -> {
        serializer.toJson(view);
      });
  }
}
