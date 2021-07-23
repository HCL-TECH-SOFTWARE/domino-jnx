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

import java.util.Optional;

public abstract class DQLQueryResult implements DbQueryResult<DQLQueryResult> {

	/**
	 * Returns the DQL query string
	 * 
	 * @return DQL query
	 */
	public abstract String getQuery();
	
	@Override
	public abstract Optional<IDTable> getNoteIds();
	
	/**
	 * Returns the explain text if {@link DBQuery#EXPLAIN} was specified as
	 * query option
	 * 
	 * @return explain text or empty string
	 */
	public abstract String getExplainText();
	
	/**
	 * Returns the number of milliseconds it took to compute the result
	 * 
	 * @return duration
	 */
	public abstract long getDurationInMillis();
	
}
