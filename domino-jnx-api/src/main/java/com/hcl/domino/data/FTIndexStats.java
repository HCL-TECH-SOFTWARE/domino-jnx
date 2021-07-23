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
package com.hcl.domino.data;

/**
 * This class contains statistics information that is returned when indexing a database for full
 * text searching capabilities with {@link Database#ftIndex(java.util.Set)}
 * 
 * @author Karsten Lehmann
 */
public interface FTIndexStats {
	/**
	 * Returns the database server
	 * 
	 * @return server
	 */
	String getServer();
	
	/**
	 * Returns the database filepath
	 * 
	 * @return filepath
	 */
	String getFilePath();
	
	/**
	 * # of new documents
	 * 
	 * @return count
	 */
	int getDocsAdded();
	
	/**
	 * # of revised documents
	 * 
	 * @return count
	 */
	int getDocsUpdated();
	
	/**
	 * # of deleted documents
	 * 
	 * @return count
	 */
	int getDocsDeleted();
	
	/**
	 * # of bytes indexed
	 * 
	 * @return bytes
	 */
	int getBytesIndexed();
	
}
