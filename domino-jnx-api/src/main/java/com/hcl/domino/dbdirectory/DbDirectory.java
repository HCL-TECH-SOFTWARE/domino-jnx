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
package com.hcl.domino.dbdirectory;

import java.util.List;

/**
 * Very fast directory scanner
 */
public interface DbDirectory {

	/**
	 * Returns the content of the server directory at the top-level.
	 * 
	 * @param server server name, either abbreviated, canonical or common name
	 * @return found file entries (either {@link FolderData} or {@link DatabaseData} instances)
	 */
	List<DirEntry> listFiles(String server);
	
	/**
	 * Returns the content of a single directory on a Domino server
	 * 
	 * @param server server name, either abbreviated, canonical or common name
	 * @param directory directory name or empty string for top-level directory
	 * @return found file entries (either {@link FolderData} or {@link DatabaseData} instances)
	 */
	List<DirEntry> listFiles(String server, String directory);
	
	/**
	 * Returns the content of a server directory, filtered via formula
	 * 
	 * @param server server to scan
	 * @param directory directory name or empty string for top level directory
	 * @param formula optional search formula to filter the returned entries, see {@link DirEntry#getProperties()} for available fields, e.g. $path="mydb.nsf" or @Word($info;@char(10);2)="db category name"
	 * @return search result
	 */
	List<DirEntry> listFiles(String server, String directory, String formula);
	
	/**
	 * With this method you can create a more advanced query for files on any Domino server
	 * 
	 * @return		query object to refine and perform the search
	 */
	DirectorySearchQuery query();
}
