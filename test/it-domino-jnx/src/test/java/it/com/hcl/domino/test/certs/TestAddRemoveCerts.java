package it.com.hcl.domino.test.certs;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestAddRemoveCerts extends AbstractNotesRuntimeTest {

  @Test
  public void testAddCertFromNab() throws Exception {
    // Find the first cert from the NAB certificates list
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
    
    X509Certificate[] found = new X509Certificate[] { null };
    firstCert.forEachCertificate((cert, loop) -> {
      found[0] = cert;
    });
    
    assertNotNull(found[0]);
    
    // Now try adding to a new doc and comparing
    X509Certificate[] fetchedCert = new X509Certificate[1];
    withTempDb(database -> {
      Document doc = database.createDocument();
      doc.attachCertificate(found[0]);
      doc.forEachCertificate((c, loop) -> {
        fetchedCert[0] = c;
        loop.stop();
      });
    });
    
    assertEquals(found[0], fetchedCert[0]);
  }
  
  @Test
  public void testAddLocalCert() throws Exception {
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    X509Certificate cert;
    try(InputStream is = getClass().getResourceAsStream("/text/testAddRemoveCerts/certificate.crt")) {
      cert = (X509Certificate)cf.generateCertificate(is);
    }
    assertNotNull(cert);
    String dn = "EMAILADDRESS=foo@somecompany.com, CN=foo.somecompany.com, OU=IT, O=Some Co., L=Villetown, ST=Test State, C=US";
    assertEquals(dn, cert.getSubjectDN().getName());
    
    X509Certificate[] fetchedCert = new X509Certificate[1];
    withTempDb(database -> {
      Document doc = database.createDocument();
      doc.attachCertificate(cert);
      doc.forEachCertificate((c, loop) -> {
        fetchedCert[0] = c;
        loop.stop();
      });
    });
    
    assertEquals(cert, fetchedCert[0]);
  }
  
  @Test
  public void testAddNullCert() throws Exception {
    withTempDb(database -> {
      Document doc = database.createDocument();
      assertThrows(NullPointerException.class, () -> doc.attachCertificate(null));
    });
  }
  
  @Test
  public void testAddRemoveLocalCert() throws Exception {
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    X509Certificate cert;
    try(InputStream is = getClass().getResourceAsStream("/text/testAddRemoveCerts/certificate.crt")) {
      cert = (X509Certificate)cf.generateCertificate(is);
    }
    assertNotNull(cert);
    String dn = "EMAILADDRESS=foo@somecompany.com, CN=foo.somecompany.com, OU=IT, O=Some Co., L=Villetown, ST=Test State, C=US";
    assertEquals(dn, cert.getSubjectDN().getName());
    
    X509Certificate[] fetchedCert = new X509Certificate[1];
    withTempDb(database -> {
      Document doc = database.createDocument();
      doc.attachCertificate(cert);
      doc.forEachCertificate((c, loop) -> {
        fetchedCert[0] = c;
        loop.stop();
      });
      
      assertEquals(cert, fetchedCert[0]);
      
      // Now try to remove it
      
      doc.removeCertificate(cert);
      boolean[] found = new boolean[1];
      doc.forEachCertificate((c, loop) -> {
        found[0] = true;
      });
      assertFalse(found[0], "Document should have no certificates");
    });
    
  }

}
