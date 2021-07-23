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

import java.util.Optional;

import com.hcl.domino.data.DominoDateTime;

/**
 * Subclass of {@link DirEntry} that is used to return
 * parsed data of databases.
 */
public interface DatabaseData extends DirEntry {
	
	/**
	 * Returns the database title
	 * 
	 * @return title
	 */
	String getTitle();

	/**
	 * Returns the filename of the database
	 * 
	 * @return filename
	 */
	String getFileName();

	/**
	 * Returns the complete relative path of the database in the data directory
	 * 
	 * @return path
	 */
	String getFilePath();

	/**
	 * Returns the database creation date
	 * 
	 * @return creation date
	 */
	DominoDateTime getCreated();

	/**
	 * Returns the database modification date
	 * 
	 * @return modification date
	 */
	@Override
	DominoDateTime getModified();

	/**
	 * Returns the date of the last fixup
	 * 
	 * @return an {@link Optional} describing the last fixup time, or an empty one if
	 *      no history is present
	 */
	Optional<DominoDateTime> getLastFixup();
	
	/**
	 * Returns the date of the last compact
	 * 
	 * @return an {@link Optional} describing the last compact time, or an empty one if
	 *      no history is present
	 */
	Optional<DominoDateTime> getLastCompact();
	
	/**
	 * Returns the date of the last design change
	 * 
	 * @return design modified date
	 */
	DominoDateTime getDesignModifiedDate();
	
	/**
	 * Returns the database category
	 * 
	 * @return category or empty string
	 */
	String getCategory();
	
	/**
	 * Returns the template name
	 * 
	 * @return template name if this database is a template, empty string otherwise
	 */
	String getTemplateName();
	
	/**
	 * Returns the name of the template that this database inherits its design from
	 * 
	 * @return inherit template name or empty string
	 */
	String getInheritTemplateName();
	
}
