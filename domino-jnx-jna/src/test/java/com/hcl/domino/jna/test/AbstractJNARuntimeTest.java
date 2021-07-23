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
package com.hcl.domino.jna.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.GZIPInputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClient.Encryption;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.DominoProcess;
import com.hcl.domino.data.Database;
import com.hcl.domino.dxl.DxlImporter;
import com.hcl.domino.dxl.DxlImporter.DXLImportOption;
import com.hcl.domino.dxl.DxlImporter.XMLValidationOption;
import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.StreamUtil;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressWarnings("nls")
@SuppressFBWarnings("UI_INHERITANCE_UNSAFE_GETRESOURCE")
public abstract class AbstractJNARuntimeTest {
  @FunctionalInterface
  protected interface DatabaseConsumer {
    void accept(Database database) throws Exception;
  }

  private static ThreadLocal<DominoClient> threadClient = new ThreadLocal<>();

  private static boolean initialized = false;

  private static <T> T call(final Callable<T> callable) {
    try {
      return callable.call();
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static List<String> getResourceFiles(final String path) throws IOException {
    final List<String> result = new ArrayList<>();
    final File jarFile = new File(AbstractJNARuntimeTest.class.getProtectionDomain().getCodeSource().getLocation().getPath());

    if (jarFile.isFile()) { // Run with JAR file
      final JarFile jar = new JarFile(jarFile);
      final Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries in jar
      while (entries.hasMoreElements()) {
        final String name = entries.nextElement().getName();
        if (name.startsWith(path + "/")) { // filter according to the path
          result.add(name);
        }
      }
      jar.close();
    } else if (jarFile.isDirectory()) {
      final File subDir = new File(jarFile, path);
      if (subDir != null && subDir.exists()) {
        final String[] paths = subDir.list();
        if (paths != null) {
          for (final String name : paths) {
            result.add(path + "/" + name);
          }
        }
      }
    } else { // Run with IDE
      final URL url = AbstractJNARuntimeTest.class.getResource("/" + path);
      if (url != null) {
        try {
          final File apps = new File(url.toURI());
          final File[] files = apps.listFiles();
          if (files != null) {
            for (final File app : files) {
              result.add(app.getPath());
            }
          }
        } catch (final URISyntaxException ex) {
          // never happens
        }
      }
    }
    return result;
  }

  @BeforeAll
  public static void initRuntime() {
    if (!AbstractJNARuntimeTest.initialized) {
      AbstractJNARuntimeTest.initialized = true;
      AbstractJNARuntimeTest.initRuntime(true);
    }
  }

  public static void initRuntime(final boolean addShutdownHook) {
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
      DominoProcess.get().switchToId(StringUtil.isEmpty(idFilePath) ? null : Paths.get(idFilePath), idPassword, true);
    }

    if (addShutdownHook) {
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        DominoProcess.get().terminateProcess();
      }));
    }
  }

  public DominoClient getClient() {
    return AbstractJNARuntimeTest.threadClient.get();
  }

  @BeforeEach
  public void initClient() throws IOException {
    if (AbstractJNARuntimeTest.threadClient.get() == null) {
      DominoProcess.get().initializeThread();
      AbstractJNARuntimeTest.threadClient.set(DominoClientBuilder.newDominoClient().build());
    }
  }

  public DominoClient reloadClient() throws IOException {
    if (AbstractJNARuntimeTest.threadClient.get() != null) {
      AbstractJNARuntimeTest.threadClient.get().close();

      AbstractJNARuntimeTest.threadClient.set(DominoClientBuilder.newDominoClient().build());
    } else {
      AbstractJNARuntimeTest.threadClient.set(DominoClientBuilder.newDominoClient().build());
    }

    return this.getClient();
  }

  @AfterEach
  public void termClient() throws IOException {
    if (AbstractJNARuntimeTest.threadClient.get() != null) {
      AbstractJNARuntimeTest.threadClient.get().close();
      AbstractJNARuntimeTest.threadClient.set(null);
      DominoProcess.get().terminateThread();
    }
  }

  protected void withResourceDxl(final String resDirPath, final DatabaseConsumer c) throws Exception {
    this.withTempDb(database -> {
      final DxlImporter importer = this.getClient().createDxlImporter();
      importer.setInputValidationOption(XMLValidationOption.NEVER);
      importer.setDesignImportOption(DXLImportOption.REPLACE_ELSE_CREATE);
      importer.setReplicaRequiredForReplaceOrUpdate(false);
      AbstractJNARuntimeTest.getResourceFiles(resDirPath).stream()
          .filter(Objects::nonNull)
          .map(name -> PathUtil.concat("/", name, '/'))
          .map(name -> StringUtil.endsWithIgnoreCase(name, ".xml") ? (InputStream) this.getClass().getResourceAsStream(name)
              : StringUtil.endsWithIgnoreCase(name, ".xml.gz")
                  ? AbstractJNARuntimeTest.call(() -> new GZIPInputStream(this.getClass().getResourceAsStream(name)))
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

      c.accept(database);
    });
  }

  protected void withTempDb(final DatabaseConsumer c) throws Exception {
    final DominoClient client = this.getClient();
    final Path tempDest = Files.createTempFile(this.getClass().getName(), ".nsf"); //$NON-NLS-1$
    Files.delete(tempDest);
    final Database database = client.createDatabase(null, tempDest.toString(), false, true, Encryption.None);
    Assertions.assertNotEquals(null, database);
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
