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
package com.hcl.domino.naming;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.hcl.domino.data.SearchQuery;

/**
 * Represents a query into the names directory system of the local runtime or
 * a remote server.
 * 
 * @author Jesse Gallagher
 * @since 1.0.2
 */
public interface UserDirectoryQuery extends SearchQuery {
	/**
	 * Sets the query to search through all available directories,
	 * rather than stopping with the first directory to contain a match.
	 * 
	 * @return this query builder
	 */
	UserDirectoryQuery exhaustive();
	/**
	 * Instructs the query to update the back-end views before performing
	 * the search.
	 * 
	 * @return this query builder
	 */
	UserDirectoryQuery forceUpdate();
	
	/**
	 * Instructs the query to search the provided namespaces, which in practice
	 * correspond to Domino view names. The default for this setting is to
	 * search "$Users".
	 * 
	 * <p>If specified, this must be non-null and non-empty.</p>
	 * 
	 * @param namespaces a {@link Collection} of namespace names to search
	 * @return this query builder
	 */
	UserDirectoryQuery namespaces(Collection<String> namespaces);
	
	/**
	 * Instructs the query to search the provided namespaces, which in practice
	 * correspond to Domino view names. The default for this setting is to
	 * search "$Users".
	 * 
	 * @param namespaces the namespaces names to search
	 * @return this query builder
	 * @since 1.0.17
	 */
	default UserDirectoryQuery namespaces(String... namespaces) {
		return namespaces(Arrays.asList(namespaces));
	}
	
	/**
	 * Specifies the names to look up. If unspecified, the query will return all
	 * entries in the specified namespaces.
	 * 
	 * @param names a {@link Collection} of string names to look up
	 * @return this query builder
	 */
	UserDirectoryQuery names(Collection<String> names);
	
	/**
	 * Specifies the names to look up. If unspecified, the query will return all
	 * entries in the specified namespaces.
	 * 
	 * @param names the names to look up
	 * @return this query builder
	 * @since 1.0.17
	 */
	default UserDirectoryQuery names(String... names) {
		return names(Arrays.asList(names));
	}
	
	/**
	 * Specifies the items to extract from found directory entries.
	 * 
	 * <p>This must be specified before a call to {@link #stream()}</p>
	 * 
	 * @param items a {@link Collection} of item names
	 * @return this query builder
	 */
	UserDirectoryQuery items(Collection<String> items);
	
	/**
	 * Specifies the items to extract from found directory entries.
	 * 
	 * <p>This must be specified before a call to {@link #stream()}</p>
	 * 
	 * @param items a {@link Collection} of item names
	 * @return this query builder
	 * @since 1.0.17
	 */
	default UserDirectoryQuery items(String... items) {
		return items(Arrays.asList(items));
	}
	
	/**
	 * Executes and retrieves a stream of results from the query.
	 * 
	 * <p>The result stream will contain one entry per queried name per queried namespace. Each of
	 * those entries will contain a list of matches for the name+namespace pair.</p>
	 * 
	 * @return a {@link Stream} of {@code List<Map<String, Object>>} lookup results for this query
	 * @throws IllegalArgumentException if {@link #items(Collection)} has not been specified or if
	 * 		either it or {@link #namespaces(Collection)} is set to {@code null} or empty
	 */
	Stream<List<Map<String, List<Object>>>> stream();
}
