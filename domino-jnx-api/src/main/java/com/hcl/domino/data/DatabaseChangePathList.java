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

import java.time.temporal.TemporalAccessor;
import java.util.Collection;

import com.hcl.domino.DominoClient;

/**
 * Represents a collection of databases and the new "since" query time
 * returned from {@link DominoClient#getDatabasePaths(String, TemporalAccessor)}.
 * 
 * @author Jesse Gallagher
 */
public class DatabaseChangePathList {
	private final Collection<String> databasePaths;
	private final TemporalAccessor nextSinceTime;

	public DatabaseChangePathList(Collection<String> databasePaths, TemporalAccessor nextSinceTime) {
		this.databasePaths = databasePaths;
		this.nextSinceTime = nextSinceTime;
	}
	
	/**
	 * @return a {@link Collection} of server-relative database paths
	 */
	public Collection<String> getDatabasePaths() {
		return databasePaths;
	}
	
	/**
	 * @return the next since time to pass to subsequent queries
	 */
	public TemporalAccessor getNextSinceTime() {
		return nextSinceTime;
	}

}
