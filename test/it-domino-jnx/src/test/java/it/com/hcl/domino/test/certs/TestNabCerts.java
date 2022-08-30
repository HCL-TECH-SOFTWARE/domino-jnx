package it.com.hcl.domino.test.certs;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.IDTable;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestNabCerts extends AbstractNotesRuntimeTest {

  @Test
  public void testEnumerateAnyCert() {
    DominoClient client = getClient();
    
    String nabPath = client.openUserDirectory(null).getPrimaryDirectoryPath().get();
    Database names = client.openDatabase(nabPath);
    
    // Find the first certificate in the view - doesn't matter what cert it is
    Document firstCert = names.openCollection("$Certifiers")
        .get()
        .query()
        .collectEntries(0, 1)
        .get(0)
        .openDocument()
        .get();
    
    boolean[] found = new boolean[] { false };
    firstCert.forEachCertificate((cert, loop) -> {
      found[0] = true;
    });
    
    assertTrue(found[0]);
  }

  @Test
  public void testEnumerateNoCertInNewNote() throws Exception {
    withTempDb(database -> {
      Document doc = database.createDocument();
      
      boolean[] found = new boolean[] { false };
      doc.forEachCertificate((cert, loop) -> {
        found[0] = true;
      });
      
      assertFalse(found[0]);
    });
  }

  @Test
  public void testEnumerateParseCert() {
    DominoClient client = getClient();
    
    String nabPath = client.openUserDirectory(null).getPrimaryDirectoryPath().get();
    Database names = client.openDatabase(nabPath);
    
    // Find the first certificate in the view - doesn't matter what cert it is
    Document firstCert = names.openCollection("$Certifiers")
        .get()
        .query()
        .collectEntries(0, 1)
        .get(0)
        .openDocument()
        .get();
    
    Throwable[] ex = new Throwable[] { null };
    firstCert.forEachCertificate((cert, loop) -> {
      try {
        Date dt = cert.getNotAfter();
        assertNotNull(dt);
        assertTrue(dt.after(new Date(0)), () -> "Certificate not-after should be after epoch, but was " + dt);
      } catch(Throwable t) {
        t.printStackTrace();
        ex[0] = t;
      }
    });
    
    assertNull(ex[0]);
  }

  @Test
  public void testEnumerateAllNabCert() {
    DominoClient client = getClient();
    
    String nabPath = client.openUserDirectory(null).getPrimaryDirectoryPath().get();
    Database names = client.openDatabase(nabPath);
    
    IDTable ids = names.openCollection("$Certifiers")
        .get()
        .getAllIdsAsIDTable(false);
    
    for(int id : ids) {
      Document doc = names.getDocumentById(id).get();
      Throwable[] ex = new Throwable[] { null };
      doc.forEachCertificate((cert, loop) -> {
        try {
          Date dt = cert.getNotAfter();
          assertNotNull(dt);
          assertTrue(dt.after(new Date(0)), () -> "Certificate not-after should be after epoch, but was " + dt);
        } catch(Throwable t) {
          t.printStackTrace();
          ex[0] = t;
        }
      });
      
      assertNull(ex[0]);
    }
  }

}
