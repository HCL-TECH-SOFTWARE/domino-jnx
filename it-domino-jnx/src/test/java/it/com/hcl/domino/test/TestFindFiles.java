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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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


//	@Test
	public void testRecursiveDirectoryScan() {
		DominoClient client = getClient();
		
		String server = "";
		//skip some internal folders
		String formula = "_pathLC:=@LowerCase(@ReplaceSubString($path; \"\\\\\"; \"/\"));"
				+ " !@Begins(@LowerCase(_pathLC); \"domino/\") &"
				+ " !@Begins(@LowerCase(_pathLC); \"ibm_technical_support/\") &"
				+ " !@Begins(@LowerCase(_pathLC); \"expeditor/\")"
				;
		Collection<FileType> fileTypes = EnumSet.noneOf(FileType.class);
		
		// scan in subdirectories
		fileTypes.add(FileType.RECURSE);
		
		//also read the directory structure
		fileTypes.add(FileType.DIRS);
		
		//scanning all file types is default;
		//use FileType.ANYNOTEFILE for example to just read .ns?, .nt? and .box
//		fileTypes.add(FileType.ANYNOTEFILE);
		//use this instead to just read the directory structure:
//		fileTypes.add(FileType.DIRSONLY);
		
		String directory = ""; // start from top level
		DirectorySearchQuery searchQuery = client.openDbDirectory().query()
				.withServer(server)
				.withDirectory(directory)
				.withFormula(formula)
				.withFileTypes(fileTypes);
		
		int skip = 0;
		int limit = -1;
		
		searchQuery.forEach(skip, limit, (entry,loop) -> {
			if (entry instanceof DatabaseData) {
				System.out.println("Database:\t"+entry.getFilePath());
			}
			else if (entry instanceof FolderData) {
				System.out.println("Folder:\t\t"+entry.getFilePath());
			}
			else {
				System.out.println("File:\t\t"+entry.getFilePath());
			}
		});
	}
	
	@Test
	public void testFindFiles() throws IOException {
		DominoClient client = getClient();
		
		List<DirEntry> foundEntriesOnTopLevel = client.openDbDirectory().listFiles("");
		
		assertTrue(foundEntriesOnTopLevel.size()>0, "Read no files in data directory");
		
		assertTrue(
			foundEntriesOnTopLevel.stream()
				.filter(DatabaseData.class::isInstance)
				.anyMatch( entry-> entry instanceof DatabaseData && "names.nsf".equalsIgnoreCase((String)entry.getProperties().get("$TITLE")) ),
			"names.nsf not found in data directory"
		);
		
		assertTrue(
				foundEntriesOnTopLevel.stream()
					.filter(FolderData.class::isInstance)
					.findAny().isPresent(),
				"no subfolder returned for data directory"
			);
		
		String subfolderName = "help";
		
		Optional<FolderData> helpFolderData = foundEntriesOnTopLevel
				.stream()
				.filter((entry) -> { return entry instanceof FolderData && subfolderName.equals(((FolderData)entry).getFileName()); })
				.map(FolderData.class::cast)
				.findFirst();
		assertTrue(helpFolderData.isPresent());
		
		//check if we can read the content of a folder
		
		List<DirEntry> foundEntriesInSubFolder = client.openDbDirectory().listFiles("", subfolderName);
		
		assertTrue(
				foundEntriesInSubFolder.stream()
					.filter(DatabaseData.class::isInstance)
					.anyMatch( entry-> entry instanceof DatabaseData && ( ((DatabaseData)entry).getFilePath().startsWith(subfolderName+"/") || ((DatabaseData)entry).getFilePath().startsWith(subfolderName+"\\")) )  ,
				"no file returned for subfolder"
			);

	}

	@Test
	public void testFindFilesInFolder() throws IOException {
		DominoClient client = getClient();

		String server="";
		String folderName=UUID.randomUUID().toString();
		String dbPath=folderName + File.separator + "test.nsf";
		
		Database tmpDb=client.createDatabase(server, dbPath, true, false, Encryption.None);
		
		try {
			// test if folder can be found
			List<DirEntry> foundEntriesInRoot = client.openDbDirectory().listFiles(server, "");

			assertTrue(foundEntriesInRoot.size()>0, "Read no files in root folder");

			assertTrue(foundEntriesInRoot.stream().filter( entry-> entry instanceof FolderData && folderName.equalsIgnoreCase((String)entry.getProperties().get("$TITLE")) ).count()==1, "not found folder: " + folderName);
			
			
			// test if database found in subfolder
			
			List<DirEntry> foundEntriesInFolder = client.openDbDirectory().listFiles(server, folderName);
			
			assertTrue(foundEntriesInFolder.size()>0, "Read no files in folder " + folderName);
			
			assertTrue(foundEntriesInFolder.stream().filter( entry-> entry instanceof DatabaseData && "test.nsf".equalsIgnoreCase((String)entry.getProperties().get("$TITLE")) ).count()==1, "test.nsf not found in folder " + folderName);
		}
		finally {
			tmpDb.close();
			
			client.deleteDatabase(server, dbPath);
			
			try {
				List<DirEntry> shouldNotReturn = client.openDbDirectory().listFiles(server, folderName);
				
				assertNull(shouldNotReturn, "Checking for folder " + folderName + " should throw an exception indicating it is not found");
			}
			catch (DominoException e) {
				assertEquals(259, e.getId(), "Error 'File does not exist' (259) expected");
			}
		}
	}
	
}
