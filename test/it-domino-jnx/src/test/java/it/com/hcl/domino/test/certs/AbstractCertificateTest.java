package it.com.hcl.domino.test.certs;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

import com.drew.lang.StreamUtil;
import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public abstract class AbstractCertificateTest extends AbstractNotesRuntimeTest {

  public Document findFirstNabCertDoc() {
    DominoClient client = getClient();
    
    String nabPath = client.openUserDirectory(null).getPrimaryDirectoryPath().get();
    Database names = client.openDatabase(nabPath);
    
    // Find the first certificate in the view - doesn't matter what cert it is
    return names.openCollection("$Certifiers")
        .get()
        .query()
        .collectEntries(0, 1)
        .get(0)
        .openDocument()
        .get();
  }
  
  public X509Certificate loadLocalCert() throws CertificateException, IOException {
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    X509Certificate cert;
    try(InputStream is = getClass().getResourceAsStream("/text/testAddRemoveCerts/certificate.crt")) {
      cert = (X509Certificate)cf.generateCertificate(is);
    }
    assertNotNull(cert);
    String dn = "EMAILADDRESS=foo@somecompany.com, CN=foo.somecompany.com, OU=IT, O=Some Co., L=Villetown, ST=Test State, C=US";
    assertEquals(dn, cert.getSubjectDN().getName());
    
    return cert;
  }
  
  public PrivateKey loadLocalKey() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
    byte[] keyData;
    try(InputStream is = getClass().getResourceAsStream("/text/testAddRemoveCerts/privateKey.der")) {
      keyData = StreamUtil.readAllBytes(is);
    }
    return parsePrivateKey(keyData);
  }
  
  public PrivateKey parsePrivateKey(byte[] keyData) throws InvalidKeySpecException, NoSuchAlgorithmException {
    KeyFactory kr = KeyFactory.getInstance("RSA");
    KeySpec keySpec = new PKCS8EncodedKeySpec(keyData);
    return kr.generatePrivate(keySpec);
  }

}
