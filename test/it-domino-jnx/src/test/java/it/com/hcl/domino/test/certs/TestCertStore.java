package it.com.hcl.domino.test.certs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.StringReader;
import java.net.ServerSocket;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.junit.jupiter.api.Test;

import com.hcl.domino.data.Document;
import com.hcl.domino.dql.DQL;
import com.hcl.domino.dql.DQL.DQLTerm;
import com.hcl.domino.misc.Ref;

import io.undertow.Undertow;
import io.undertow.server.handlers.ResponseCodeHandler;

@SuppressWarnings("nls")
public class TestCertStore extends AbstractCertificateTest {
  public static final String PASSWORD = "GQd2saCSD47qS7SY57wj";

  @Test
  public void testCertStoreDocument() throws Exception {
    withResourceDxl("/dxl/testCertStore", database -> {
      DQLTerm dql = DQL.item("Form").isEqualTo("KeyFile");
      Document doc = database.queryDQL(dql)
          .getDocuments()
          .findFirst()
          .get();

      X509Certificate expected = loadLocalCert();

      List<X509Certificate> certs = doc.getCertificates();
      assertEquals(expected, certs.get(0));
    });
  }

  @Test
  public void testCertStoreDocumentPrivateKey() throws Exception {
    PrivateKey localKey = loadLocalKey();

    withResourceDxl("/dxl/testCertStore", database -> {
      DQLTerm dql = DQL.item("Form").isEqualTo("KeyFile");
      Document doc = database.queryDQL(dql)
          .getDocuments()
          .findFirst()
          .get();

      String encryptedPem = doc.get("PrivateKeyExportable", String.class, null);
      assertNotNull(encryptedPem);

      Security.addProvider(new BouncyCastleProvider());
      // r is a Reader that reads a PEM file (with header/footer and Base64 data)
      PKCS8EncryptedPrivateKeyInfo pem;
      try (StringReader r = new StringReader(encryptedPem)) {
        PEMParser pemParser = new PEMParser(r);
        pem = (PKCS8EncryptedPrivateKeyInfo) pemParser.readObject();
      }

      JceOpenSSLPKCS8DecryptorProviderBuilder jce = new JceOpenSSLPKCS8DecryptorProviderBuilder();
      jce.setProvider("BC");
      InputDecryptorProvider decProv = jce.build(PASSWORD.toCharArray());
      PrivateKeyInfo info = pem.decryptPrivateKeyInfo(decProv);

      // Convert the BC objects to a byte array and read using standard Java APIs
      byte[] encoded = info.getEncoded();
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
      PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
      assertEquals(localKey, privateKey);
    });
  }

  @Test
  public void testRunServer() throws Exception {
    withResourceDxl("/dxl/testCertStore", database -> {
      DQLTerm dql = DQL.item("Form").isEqualTo("KeyFile");
      Document doc = database.queryDQL(dql)
          .getDocuments()
          .findFirst()
          .get();

      String encryptedPem = doc.get("PrivateKeyExportable", String.class, null);
      assertNotNull(encryptedPem);

      Security.addProvider(new BouncyCastleProvider());
      // r is a Reader that reads a PEM file (with header/footer and Base64 data)
      PKCS8EncryptedPrivateKeyInfo pem;
      try (StringReader r = new StringReader(encryptedPem)) {
        PEMParser pemParser = new PEMParser(r);
        pem = (PKCS8EncryptedPrivateKeyInfo) pemParser.readObject();
      }

      JceOpenSSLPKCS8DecryptorProviderBuilder jce = new JceOpenSSLPKCS8DecryptorProviderBuilder();
      jce.setProvider("BC");
      InputDecryptorProvider decProv = jce.build(PASSWORD.toCharArray());
      PrivateKeyInfo info = pem.decryptPrivateKeyInfo(decProv);

      // Convert the BC objects to a byte array and read using standard Java APIs
      byte[] encoded = info.getEncoded();
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
      RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);

      // Load the certificate from the document
      List<X509Certificate> certs = doc.getCertificates();

      // Build an SSL context
      char[] password = Long.toString(System.currentTimeMillis()).toCharArray();
      KeyStore keystore = KeyStore.getInstance("PKCS12"); //$NON-NLS-1$
      keystore.load(null, password);
      keystore.setKeyEntry("default", privateKey, password, certs.toArray(new Certificate[certs.size()])); //$NON-NLS-1$
      TrustManager[] trustManagers = buildTrustManagers(keystore);
      KeyManager[] keyManagers = buildKeyManagers(keystore, password);

      SSLContext sslContext = SSLContext.getInstance("TLS"); //$NON-NLS-1$
      sslContext.init(keyManagers, trustManagers, null);

      // Run an Undertow server on a free port
      int port;
      try (ServerSocket socket = new ServerSocket(0)) {
        port = socket.getLocalPort();
      }

      Undertow server = Undertow.builder()
          .setHandler(ResponseCodeHandler.HANDLE_200)
          .addHttpsListener(port, "127.0.0.1", sslContext)
          .build();
      server.start();
      
      // Perform an HTTPS exchange with some checks to make sure we're working with the expected certs
      Ref<Certificate[]> peerCerts = new Ref<>();
      HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
          .setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, (hostname, session) -> true))
          .build();
      try (
          CloseableHttpClient httpclient = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .addResponseInterceptorLast((response, entity, context) -> {
              SSLSession sslSession = (SSLSession)context.getAttribute(HttpCoreContext.SSL_SESSION);
              if(sslSession != null) {
                peerCerts.set(sslSession.getPeerCertificates());
              }
            })
            .build()
      ) {
        
        HttpGet httpGet = new HttpGet("https://127.0.0.1:" + port);
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
          assertEquals(200, response.getCode());
          
          assertIterableEquals(certs, Arrays.asList(peerCerts.get()));
        }
      } finally {
        server.stop();
      }
    });
  }

  private static TrustManager[] buildTrustManagers(final KeyStore trustStore) throws IOException {
    TrustManager[] trustManagers = null;
    try {
      TrustManagerFactory trustManagerFactory = TrustManagerFactory
          .getInstance(KeyManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(trustStore);
      trustManagers = trustManagerFactory.getTrustManagers();
    } catch (NoSuchAlgorithmException | KeyStoreException exc) {
      throw new IOException("Unable to initialise TrustManager[]", exc);
    }
    return trustManagers;
  }

  private static KeyManager[] buildKeyManagers(final KeyStore keyStore, char[] storePassword) throws IOException {
    KeyManager[] keyManagers;
    try {
      KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory
          .getDefaultAlgorithm());
      keyManagerFactory.init(keyStore, storePassword);
      keyManagers = keyManagerFactory.getKeyManagers();
    } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException exc) {
      throw new IOException("Unable to initialise KeyManager[]", exc);
    }
    return keyManagers;
  }
}
