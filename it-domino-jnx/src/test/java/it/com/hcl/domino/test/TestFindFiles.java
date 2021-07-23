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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClient.Encryption;
import com.hcl.domino.DominoException;
import com.hcl.domino.data.Database;
import com.hcl.domino.dbdirectory.DatabaseData;
import com.hcl.domino.dbdirectory.DirEntry;
import com.hcl.domino.dbdirectory.DirectorySearchQuery;
import com.hcl.domino.dbdirectory.FileType;
import com.hcl.domino.dbdirectory.FolderData;

@SuppressWarnings("nls")
public class TestFindFiles extends AbstractNotesRuntimeTest {

  @Test
  public void testFindFiles() throws IOException {
    final DominoClient client = this.getClient();

    final List<DirEntry> foundEntriesOnTopLevel = client.openDbDirectory().listFiles("");

    Assertions.assertTrue(foundEntriesOnTopLevel.size() > 0, "Read no files in data directory");

    Assertions.assertTrue(
        foundEntriesOnTopLevel.stream()
            .filter(DatabaseData.class::isInstance)
            .anyMatch(entry -> entry instanceof DatabaseData
                && "names.nsf".equalsIgnoreCase((String) entry.getProperties().get("$TITLE"))),
        "names.nsf not found in data directory");

    Assertions.assertTrue(
        foundEntriesOnTopLevel.stream()
            .filter(FolderData.class::isInstance)
            .findAny().isPresent(),
        "no subfolder returned for data directory");

    final String subfolderName = "help";

    final Optional<FolderData> helpFolderData = foundEntriesOnTopLevel
        .stream()
        .filter(entry -> (entry instanceof FolderData && subfolderName.equals(((FolderData) entry).getFileName())))
        .map(FolderData.class::cast)
        .findFirst();
    Assertions.assertTrue(helpFolderData.isPresent());

    // check if we can read the content of a folder

    final List<DirEntry> foundEntriesInSubFolder = client.openDbDirectory().listFiles("", subfolderName);

    Assertions.assertTrue(
        foundEntriesInSubFolder.stream()
            .filter(DatabaseData.class::isInstance)
            .anyMatch(
                entry -> entry instanceof DatabaseData && (((DatabaseData) entry).getFilePath().startsWith(subfolderName + "/")
                    || ((DatabaseData) entry).getFilePath().startsWith(subfolderName + "\\"))),
        "no file returned for subfolder");

  }

  @Test
  public void testFindFilesInFolder() throws IOException {
    final DominoClient client = this.getClient();

    final String server = "";
    final String folderName = UUID.randomUUID().toString();
    final String dbPath = folderName + File.separator + "test.nsf";

    final Database tmpDb = client.createDatabase(server, dbPath, true, false, Encryption.None);

    try {
      // test if folder can be found
      final List<DirEntry> foundEntriesInRoot = client.openDbDirectory().listFiles(server, "");

      Assertions.assertTrue(foundEntriesInRoot.size() > 0, "Read no files in root folder");

      Assertions.assertTrue(foundEntriesInRoot.stream()
          .filter(entry -> entry instanceof FolderData && folderName.equalsIgnoreCase((String) entry.getProperties().get("$TITLE")))
          .count() == 1, "not found folder: " + folderName);

      // test if database found in subfolder

      final List<DirEntry> foundEntriesInFolder = client.openDbDirectory().listFiles(server, folderName);

      Assertions.assertTrue(foundEntriesInFolder.size() > 0, "Read no files in folder " + folderName);

      Assertions
          .assertTrue(
              foundEntriesInFolder.stream()
                  .filter(entry -> entry instanceof DatabaseData
                      && "test.nsf".equalsIgnoreCase((String) entry.getProperties().get("$TITLE")))
                  .count() == 1,
              "test.nsf not found in folder " + folderName);
    } finally {
      tmpDb.close();

      client.deleteDatabase(server, dbPath);

      try {
        final List<DirEntry> shouldNotReturn = client.openDbDirectory().listFiles(server, folderName);

        Assertions.assertNull(shouldNotReturn,
            "Checking for folder " + folderName + " should throw an exception indicating it is not found");
      } catch (final DominoException e) {
        Assertions.assertEquals(259, e.getId(), "Error 'File does not exist' (259) expected");
      }
    }
  }

  // @Test
  public void testRecursiveDirectoryScan() {
    final DominoClient client = this.getClient();

    final String server = "";
    // skip some internal folders
    final String formula = "_pathLC:=@LowerCase(@ReplaceSubString($path; \"\\\\\"; \"/\"));"
        + " !@Begins(@LowerCase(_pathLC); \"domino/\") &"
        + " !@Begins(@LowerCase(_pathLC); \"ibm_technical_support/\") &"
        + " !@Begins(@LowerCase(_pathLC); \"expeditor/\")";
    final Collection<FileType> fileTypes = EnumSet.noneOf(FileType.class);

    // scan in subdirectories
    fileTypes.add(FileType.RECURSE);

    // also read the directory structure
    fileTypes.add(FileType.DIRS);

    // scanning all file types is default;
    // use FileType.ANYNOTEFILE for example to just read .ns?, .nt? and .box
    // fileTypes.add(FileType.ANYNOTEFILE);
    // use this instead to just read the directory structure:
    // fileTypes.add(FileType.DIRSONLY);

    final String directory = ""; // start from top level
    final DirectorySearchQuery searchQuery = client.openDbDirectory().query()
        .withServer(server)
        .withDirectory(directory)
        .withFormula(formula)
        .withFileTypes(fileTypes);

    final int skip = 0;
    final int limit = -1;

    searchQuery.forEach(skip, limit, (entry, loop) -> {
      if (entry instanceof DatabaseData) {
        System.out.println("Database:\t" + entry.getFilePath());
      } else if (entry instanceof FolderData) {
        System.out.println("Folder:\t\t" + entry.getFilePath());
      } else {
        System.out.println("File:\t\t" + entry.getFilePath());
      }
    });
  }

}
