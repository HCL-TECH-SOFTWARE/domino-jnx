package com.hcl.domino.jnx.jep454.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClient.Encryption;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.DominoProcess;
import com.hcl.domino.data.AutoCloseableDocument;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.dxl.DxlImporter;
import com.hcl.domino.dxl.DxlImporter.DXLImportOption;
import com.hcl.domino.dxl.DxlImporter.XMLValidationOption;
import com.hcl.domino.exception.FileDoesNotExistException;
import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.StreamUtil;

@SuppressWarnings("nls")
public abstract class AbstractJEPRuntimeTest {
  @FunctionalInterface
  public interface DatabaseConsumer {
    void accept(Database database) throws Exception;
  }

  private static ThreadLocal<DominoClient> threadClient = new ThreadLocal<>();

  private static boolean initialized = false;

  protected static <T> T call(final Callable<T> callable) {
    try {
      return callable.call();
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected static Database createTempDb(final DominoClient client) throws IOException {
    final Path tempDest = Files.createTempFile(AbstractJEPRuntimeTest.class.getName(), ".nsf"); //$NON-NLS-1$
    Files.delete(tempDest);
    final Database database = client.createDatabase(null, tempDest.toString(), false, true, Encryption.None);
    Assertions.assertNotNull(database);
    return database;
  }

  protected static List<String> getResourceFiles(final String path) throws IOException, URISyntaxException {
    final List<String> result = new ArrayList<>();
    final Path jarFile = Paths.get(AbstractJEPRuntimeTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());

    if (Files.isRegularFile(jarFile)) { // Run with JAR file
      try (InputStream is = Files.newInputStream(jarFile)) {
        try (ZipInputStream zis = new ZipInputStream(is)) {
          ZipEntry entry;
          while ((entry = zis.getNextEntry()) != null) {
            final String name = entry.getName();
            if (name.startsWith(path + "/")) { // filter according to the path
              result.add(name);
            }
          }
        }
      }
    } else if (Files.isDirectory(jarFile)) {
      Path subDir;
      if (path.startsWith("/")) {
        subDir = jarFile.resolve(path.substring(1));
      } else {
        subDir = jarFile.resolve(path);
      }
      if (subDir != null && Files.exists(subDir)) {
        Files.list(subDir)
            .forEach(name -> result.add(path + "/" + subDir.relativize(name)));
      }
    } else { // Run with IDE
      final URL url = AbstractJEPRuntimeTest.class.getResource(PathUtil.concat("/", path, '/'));
      if (url != null) {
        try {
          final Path apps = Paths.get(url.toURI());
          Files.list(apps)
              .filter(Files::isRegularFile)
              .forEach(app -> result.add(app.toString()));
        } catch (final URISyntaxException ex) {
          // never happens
        }
      }
    }
    return result;
  }

  @BeforeAll
  public static void initRuntime() {
    if (!AbstractJEPRuntimeTest.initialized) {
      AbstractJEPRuntimeTest.initialized = true;
      final String notesProgramDir = System.getenv("Notes_ExecDirectory");
      final String notesIniPath = System.getenv("NotesINI");
      if (StringUtil.isNotEmpty(notesProgramDir)) {
        final String[] initArgs = new String[] {
            notesProgramDir,
            StringUtil.isEmpty(notesIniPath) ? "" : "=" + notesIniPath //$NON-NLS-1$
        };

        DominoProcess.get().initializeProcess(initArgs);
      } else {
        throw new IllegalStateException("Unable to locate Notes runtime");
      }

      // prevent ID password prompt
      final String idFilePath = System.getenv("Notes_IDPath");
      final String idPassword = System.getenv("Notes_IDPassword");
      if (!StringUtil.isEmpty(idPassword)) {
        DominoProcess.get().initializeThread();
        try {
          DominoProcess.get().switchToId(StringUtil.isEmpty(idFilePath) ? null : Paths.get(idFilePath), idPassword, true);
        } finally {
          DominoProcess.get().terminateThread();
        }
      }
    }
  }

  protected static void populateResourceDxl(final String resDirPath, final Database database)
      throws IOException, URISyntaxException {
    final DxlImporter importer = database.getParentDominoClient().createDxlImporter();
    importer.setInputValidationOption(XMLValidationOption.NEVER);
    importer.setDesignImportOption(DXLImportOption.REPLACE_ELSE_CREATE);
    importer.setReplicaRequiredForReplaceOrUpdate(false);
    AbstractJEPRuntimeTest.getResourceFiles(resDirPath).stream()
        .filter(Objects::nonNull)
        .map(name -> PathUtil.concat("/", name, '/'))
        .map(name -> StringUtil.endsWithIgnoreCase(name, ".xml")
            ? (InputStream) AbstractJEPRuntimeTest.class.getResourceAsStream(name)
            : StringUtil.endsWithIgnoreCase(name, ".xml.gz")
                ? AbstractJEPRuntimeTest.call(() -> new GZIPInputStream(AbstractJEPRuntimeTest.class.getResourceAsStream(name)))
                : null)
        .filter(Objects::nonNull)
        .forEach(is -> {
          try {
            importer.importDxl(is, database);
          } catch (final IOException e) {
            throw new RuntimeException(e);
          } finally {
            StreamUtil.close(is);
          }
        });
    
    Optional<IDTable> importedNoteIds = importer.getImportedNoteIds();

    //sign imported design
    if (importedNoteIds.isPresent()) {
      for (int noteId : importedNoteIds.get()) {
        Optional<Document> docOpt = database.getDocumentById(noteId);
        if (docOpt.isPresent()) {
          try (AutoCloseableDocument doc = docOpt.get().autoClosable()) {
            if (!doc.getDocumentClass().contains(DocumentClass.DATA)) {
              doc.sign();
              doc.save();
            }
          }
        }
      }
    }
  }

  @AfterAll
  public static void termRuntime() {
    if (AbstractJEPRuntimeTest.initialized) {
      DominoProcess.get().terminateProcess();
      AbstractJEPRuntimeTest.initialized = false;
    }
  }

  protected final Logger log = Logger.getLogger(this.getClass().getPackage().getName());

  public DominoClient getClient() {
    return AbstractJEPRuntimeTest.threadClient.get();
  }

  @BeforeEach
  public void initClient() throws IOException {
    if (this.isRestrictThreadAccess()) {
      System.setProperty("jnx.allowCrossThreadAccess", "false");
    }
    if (AbstractJEPRuntimeTest.threadClient.get() == null) {
      DominoProcess.get().initializeThread();

      AbstractJEPRuntimeTest.threadClient.set(DominoClientBuilder.newDominoClient().build());
    } else {
      System.out.println("ThreadClient already set!");
    }
  }

  protected boolean isRestrictThreadAccess() {
    return false;
  }

  protected byte[] produceTestData(final int size) {
    final byte[] data = new byte[size];

    int offset = 0;

    while (offset < size) {
      for (char c = 'A'; c <= 'Z' && offset < size; c++) {
        data[offset++] = (byte) (c & 0xff);
      }
    }

    return data;
  }

  protected void produceTestData(final int size, final OutputStream out) throws IOException {
    int offset = 0;

    while (offset < size) {
      for (char c = 'A'; c <= 'Z' && offset < size; c++) {
        out.write((byte) (c & 0xff));
        offset++;
      }
    }
  }

  protected void produceTestData(final int size, final Writer writer) throws IOException {
    int offset = 0;

    while (offset < size) {
      for (char c = 'A'; c <= 'Z' && offset < size; c++) {
        writer.write(c);
        offset++;
      }
    }
  }

  public DominoClient reloadClient() throws IOException {
    if (AbstractJEPRuntimeTest.threadClient.get() != null) {
      AbstractJEPRuntimeTest.threadClient.get().close();

      AbstractJEPRuntimeTest.threadClient.set(DominoClientBuilder.newDominoClient().build());
    } else {
      AbstractJEPRuntimeTest.threadClient.set(DominoClientBuilder.newDominoClient().build());
    }

    return this.getClient();
  }

  @AfterEach
  public void termClient() throws Exception {
    if (AbstractJEPRuntimeTest.threadClient.get() != null) {
      AbstractJEPRuntimeTest.threadClient.get().close();
      AbstractJEPRuntimeTest.threadClient.set(null);

      DominoProcess.get().terminateThread();
    }
    System.setProperty("jnx.allowCrossThreadAccess", "");
  }

  protected void withResourceDb(final String resDbPath, final DatabaseConsumer c) throws Exception {
    final DominoClient client = this.getClient();
    final Path tempDest = Files.createTempFile(this.getClass().getName(), ".nsf"); //$NON-NLS-1$
    Files.delete(tempDest);
    try (InputStream is = this.getClass().getResourceAsStream(resDbPath)) {
      Files.copy(is, tempDest);
    }
    final Database database = client.openDatabase("", tempDest.toString());
    Assertions.assertNotNull(database);
    try {
      c.accept(database);
    } finally {
      database.close();
      try {
        client.deleteDatabase(null, tempDest.toString());
      } catch (final Throwable t) {
        System.err.println("Unable to delete database " + tempDest + ": " + t);
      }
    }
  }

  protected void withResourceDxl(final String resDirPath, final DatabaseConsumer c) throws Exception {
    this.withTempDb(database -> {
      AbstractJEPRuntimeTest.populateResourceDxl(resDirPath, database);

      c.accept(database);
    });
  }

  protected void withTempDb(final DatabaseConsumer c) throws Exception {
    final DominoClient client = this.getClient();
    this.withTempDb(client, c);
  }

  protected void withTempDb(final DominoClient client, final DatabaseConsumer c) throws Exception {
    final Database database = AbstractJEPRuntimeTest.createTempDb(client);
    final String tempDest = database.getAbsoluteFilePath();
    try {
      c.accept(database);
    } finally {
      database.close();
      try {
        client.deleteDatabase(null, tempDest);
      } catch (final Throwable t) {
        System.err.println("Unable to delete database " + tempDest + ": " + t);
      }
    }
  }

  protected void withTempDbFromTemplate(final String templateServer, final String templatePath, final DatabaseConsumer c)
      throws Exception {
    final DominoClient client = this.getClient();
    final Path tempDest = Files.createTempFile(this.getClass().getName(), ".nsf"); //$NON-NLS-1$
    Files.delete(tempDest);
    Database database;
    try {
      database = client.createDatabaseFromTemplate(templateServer, templatePath, null, tempDest.toString(), Encryption.None);
    } catch (final FileDoesNotExistException e) {
      // Try locally, in case the template is here but not remote
      if (StringUtil.isNotEmpty(templateServer)) {
        database = client.createDatabaseFromTemplate(null, templatePath, null, tempDest.toString(), Encryption.None);
      } else {
        throw e;
      }
    }
    Assertions.assertNotNull(database);
    try {
      c.accept(database);
    } finally {
      database.close();
      try {
        client.deleteDatabase(null, tempDest.toString());
      } catch (final Throwable t) {
        System.err.println("Unable to delete database " + tempDest + ": " + t);
      }
    }
  }
  
  /**
   * Compares the data of two {@link InputStream}s
   * 
   * @param inExpected expected content
   * @param inActual actual content
   * @param msg assert message
   * @throws IOException if there is an exception reading from the streams
   */
  public static void assertEqualStreams(InputStream inExpected, InputStream inActual, String msg) throws IOException {
    try {
      int pos=0;
      int val1;
      int val2;
      
      do {
        val1 = inExpected.read();
        val2 = inActual.read();

        assertEquals(val1, val2, MessageFormat.format("{0} - InputStream value mismatch at position {1}", msg, pos));
        pos++;
      }
      while (val1!=-1);
    }
    finally {
     try {
       inExpected.close();
     } catch(IOException e) { }
      try {
        inActual.close();
      } catch(IOException e) { }
    }
  }

}