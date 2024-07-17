package it.com.hcl.domino.test.certs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.hcl.domino.data.Document;

@SuppressWarnings("nls")
public class TestAddRemoveCerts extends AbstractCertificateTest {

  @Test
  public void testAddCertFromNab() throws Exception {
    Document firstCert = findFirstNabCertDoc();

    X509Certificate[] found = new X509Certificate[] {null};
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
    X509Certificate cert = loadLocalCert();

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
    X509Certificate cert = loadLocalCert();

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


  @Test
  public void testAddMultiRemoveCert1() throws Exception {
    Document firstCert = findFirstNabCertDoc();

    X509Certificate[] found = new X509Certificate[] {null, loadLocalCert()};
    firstCert.forEachCertificate((cert, loop) -> {
      found[0] = cert;
      loop.stop();
    });

    assertNotNull(found[0]);

    // Now try adding both to a new doc and then removing one
    withTempDb(database -> {
      Document doc = database.createDocument();
      doc.attachCertificate(found[0]);
      doc.attachCertificate(found[1]);

      List<X509Certificate> fetchedCert = new ArrayList<>();
      doc.forEachCertificate((c, loop) -> {
        fetchedCert.add(c);
        loop.stop();
      });

      assertIterableEquals(Arrays.asList(found), fetchedCert);

      doc.removeCertificate(found[0]);

      fetchedCert.clear();
      doc.forEachCertificate((c, loop) -> {
        fetchedCert.add(c);
        loop.stop();
      });

      assertIterableEquals(Arrays.asList(found[1]), fetchedCert);
    });

  }

  @Test
  public void testAddMultiRemoveCert2() throws Exception {
    Document firstCert = findFirstNabCertDoc();

    X509Certificate[] found = new X509Certificate[] {null, loadLocalCert()};
    firstCert.forEachCertificate((cert, loop) -> {
      found[0] = cert;
      loop.stop();
    });

    assertNotNull(found[0]);

    // Now try adding both to a new doc and then removing one
    withTempDb(database -> {
      Document doc = database.createDocument();
      doc.attachCertificate(found[0]);
      doc.attachCertificate(found[1]);

      List<X509Certificate> fetchedCert = new ArrayList<>();
      doc.forEachCertificate((c, loop) -> {
        fetchedCert.add(c);
        loop.stop();
      });

      assertIterableEquals(Arrays.asList(found), fetchedCert);

      doc.removeCertificate(found[1]);

      fetchedCert.clear();
      doc.forEachCertificate((c, loop) -> {
        fetchedCert.add(c);
        loop.stop();
      });

      assertIterableEquals(Arrays.asList(found[0]), fetchedCert);
    });

  }

}
