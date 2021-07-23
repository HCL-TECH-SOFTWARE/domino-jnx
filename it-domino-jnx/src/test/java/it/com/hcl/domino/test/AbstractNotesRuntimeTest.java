/*
 * ==========================================================================
 * Copyright (C) 2019-2021 HCL America, Inc. ( http://www.hcl.com/ )
 *                            All rights reserved.
 * ==========================================================================
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may
 * not use this file except in compliance with the License.  You may obtain a
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.
 *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the  specific language  governing permissions  and limitations
 * under the License.
 * ==========================================================================
 */
package it.com.hcl.domino.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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

import com.devskiller.jfairy.Fairy;
import com.devskiller.jfairy.producer.person.Person;
import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClient.Encryption;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.DominoProcess;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.dxl.DxlImporter;
import com.hcl.domino.dxl.DxlImporter.DXLImportOption;
import com.hcl.domino.dxl.DxlImporter.XMLValidationOption;
import com.hcl.domino.exception.FileDoesNotExistException;
import com.hcl.domino.misc.Pair;
import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.StreamUtil;

@SuppressWarnings("nls")
public abstract class AbstractNotesRuntimeTest {
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
    final Path tempDest = Files.createTempFile(AbstractNotesRuntimeTest.class.getName(), ".nsf"); //$NON-NLS-1$
    Files.delete(tempDest);
    final Database database = client.createDatabase(null, tempDest.toString(), false, true, Encryption.None);
    Assertions.assertNotNull(database);
    return database;
  }

  protected static List<Pair<String, Integer>> generateNABPersons(final Database db, final int nrOfDocs) {
    final List<Pair<String, Integer>> unidsAndNoteIds = new ArrayList<>();

    final Fairy fairy = Fairy
        .builder()
        .withRandomSeed(5) // return deterministic data
        .withLocale(Locale.ENGLISH)
        .build();

    for (int i = 0; i < nrOfDocs; i++) {
      final Person person = fairy.person();

      final Document doc = db.createDocument();
      doc
          .replaceItemValue("Form", "Person")
          .replaceItemValue("Type", "Person")
          .replaceItemValue("Firstname", person.getFirstName())
          .replaceItemValue("Lastname", person.getLastName())
          .replaceItemValue("InternetAddress", person.getCompanyEmail())
          .replaceItemValue("StreetAddress", person.getAddress().getStreet() + " " + person.getAddress().getStreetNumber())
          .replaceItemValue("City", person.getAddress().getCity())
          .replaceItemValue("Zip", person.getAddress().getPostalCode())
          .replaceItemValue("CompanyName", person.getCompany().getName())
          .replaceItemValue("MailAddress", person.getEmail())
          .replaceItemValue("OfficePhoneNumber", person.getTelephoneNumber())
          .replaceItemValue("Birthday", person.getDateOfBirth())
          .replaceItemValue("WebSite", person.getCompany().getUrl())
          .replaceItemValue("Country", person.getNationality().getCode());

      doc.save();
      unidsAndNoteIds.add(new Pair<>(doc.getUNID(), doc.getNoteID()));

      // System.out.println("created doc with unid "+doc.getUNID()+" note id
      // "+doc.getNoteID()+
      // " ("+Integer.toHexString(doc.getNoteID())+")"+",
      // seq="+doc.getOID().getSequenceTime()+",
      // modified="+doc.getModifiedInThisFile());
    }

    return unidsAndNoteIds;
  }

  protected static List<String> getResourceFiles(final String path) throws IOException, URISyntaxException {
    final List<String> result = new ArrayList<>();
    final Path jarFile = Paths.get(AbstractNotesRuntimeTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());

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
      final URL url = AbstractNotesRuntimeTest.class.getResource(PathUtil.concat("/", path, '/'));
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
    if (!AbstractNotesRuntimeTest.initialized) {
      AbstractNotesRuntimeTest.initialized = true;
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

      if (!"true".equalsIgnoreCase(System.getProperty("no_domino_shutdownhook"))) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
          DominoProcess.get().terminateProcess();
        }));
      }
    }
  }

  protected static void populateResourceDxl(final String resDirPath, final Database database)
      throws IOException, URISyntaxException {
    final DxlImporter importer = database.getParentDominoClient().createDxlImporter();
    importer.setInputValidationOption(XMLValidationOption.NEVER);
    importer.setDesignImportOption(DXLImportOption.REPLACE_ELSE_CREATE);
    importer.setReplicaRequiredForReplaceOrUpdate(false);
    AbstractNotesRuntimeTest.getResourceFiles(resDirPath).stream()
        .filter(Objects::nonNull)
        .map(name -> PathUtil.concat("/", name, '/'))
        .map(name -> StringUtil.endsWithIgnoreCase(name, ".xml")
            ? (InputStream) AbstractNotesRuntimeTest.class.getResourceAsStream(name)
            : StringUtil.endsWithIgnoreCase(name, ".xml.gz")
                ? AbstractNotesRuntimeTest.call(() -> new GZIPInputStream(AbstractNotesRuntimeTest.class.getResourceAsStream(name)))
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
  }

  @AfterAll
  public static void termRuntime() {
    if ("true".equalsIgnoreCase(System.getProperty("no_domino_shutdownhook"))) {
      if (AbstractNotesRuntimeTest.initialized) {
        DominoProcess.get().terminateProcess();
        AbstractNotesRuntimeTest.initialized = false;
      }
    }
  }

  protected final Logger log = Logger.getLogger(this.getClass().getPackage().getName());

  public DominoClient getClient() {
    return AbstractNotesRuntimeTest.threadClient.get();
  }

  @BeforeEach
  public void initClient() throws IOException {
    if (this.isRestrictThreadAccess()) {
      System.setProperty("jnx.allowCrossThreadAccess", "false");
    }
    if (AbstractNotesRuntimeTest.threadClient.get() == null) {
      DominoProcess.get().initializeThread();

      AbstractNotesRuntimeTest.threadClient.set(DominoClientBuilder.newDominoClient().build());
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
    if (AbstractNotesRuntimeTest.threadClient.get() != null) {
      AbstractNotesRuntimeTest.threadClient.get().close();

      AbstractNotesRuntimeTest.threadClient.set(DominoClientBuilder.newDominoClient().build());
    } else {
      AbstractNotesRuntimeTest.threadClient.set(DominoClientBuilder.newDominoClient().build());
    }

    return this.getClient();
  }

  @AfterEach
  public void termClient() throws Exception {
    if (AbstractNotesRuntimeTest.threadClient.get() != null) {
      AbstractNotesRuntimeTest.threadClient.get().close();
      AbstractNotesRuntimeTest.threadClient.set(null);

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
      AbstractNotesRuntimeTest.populateResourceDxl(resDirPath, database);

      c.accept(database);
    });
  }

  protected void withTempDb(final DatabaseConsumer c) throws Exception {
    final DominoClient client = this.getClient();
    this.withTempDb(client, c);
  }

  protected void withTempDb(final DominoClient client, final DatabaseConsumer c) throws Exception {
    final Database database = AbstractNotesRuntimeTest.createTempDb(client);
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
}
