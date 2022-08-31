package it.com.hcl.domino.test.certs;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.IDTable;

@SuppressWarnings("nls")
public class TestNabCerts extends AbstractCertificateTest {
  
  @Test
  public void testEnumerateAnyCert() {
    Document firstCert = findFirstNabCertDoc();
    
    boolean[] found = new boolean[] { false };
    firstCert.forEachCertificate((cert, loop) -> {
      found[0] = true;
    });
    
    assertTrue(found[0]);
  }
  

  @Test
  public void testEnumerateNullConsumer() throws Exception {
    withTempDb(database -> {
      Document doc = database.createDocument();
      
      assertThrows(NullPointerException.class, () -> doc.forEachCertificate(null));
    });
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
    Document firstCert = findFirstNabCertDoc();
    
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
