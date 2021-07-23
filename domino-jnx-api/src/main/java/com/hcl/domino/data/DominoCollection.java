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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import com.hcl.domino.misc.DominoClientDescendant;
import com.hcl.domino.misc.Loop;

/**
 * Term borrowed from MongoDB. Covers Domino views/folders
 * Collection Type (static = view)
 * Contains CollectionEntry, Hibernate compliant
 * 
 * 
 * @author t.b.d
 *
 */
public interface DominoCollection extends IAdaptable, DominoClientDescendant {
	/** Available column sort directions */
	public enum Direction {Ascending, Descending};

	Database getParentDatabase();
	
	String getName();

	List<String> getAliases();
	
	String getUNID();
	
	int getNoteId();
	
	String getSelectionFormula();
	
	IDTable getAllIdsAsIDTable(boolean checkRights);
	
	/**
	 * Returns an ordered set of note ids for collection entries that the current user is
	 * allowed to see.
	 * 
	 * <p>Implementations are likely, but not guaranteed, to return a {@link LinkedHashSet}.</p>
	 * 
	 * @param withDocuments true to return document note ids
	 * @param withCategories true to return category note ids
	 * @return sorted set of note ids
	 */
	Set<Integer> getAllIds(boolean withDocuments, boolean withCategories);
	
	/**
	 * Returns an ordered set of note ids for collection entries that match the
	 * specified lookup key
	 * 
	 * <p>Implementations are likely, but not guaranteed, to return a {@link LinkedHashSet}.</p>
	 * 
	 * @param findFlags flags to configure the lookup operation
	 * @param key lookup key (String, Number, DominoDateRange)
	 * @return sorted set of note ids
	 */
	Set<Integer> getAllIdsByKey(Set<Find> findFlags, Object key);

	/**
	 * Returns an ordered set of note ids for collection entries that match the
	 * specified lookup key (supporting multi column lookups)
	 * 
	 * <p>Implementations are likely, but not guaranteed, to return a {@link LinkedHashSet}.</p>
	 * 
	 * @param findFlags flags to configure the lookup operation
	 * @param key lookup key (list of String, Number, DominoDateRange)
	 * @return sorted set of note ids
	 */
	Set<Integer> getAllIdsByKey(Set<Find> findFlags, Collection<Object> key);

	void forEachDocument(int skip, int count, BiConsumer<Document, Loop> consumer);
	
	List<CollectionColumn> getColumns();
	
	void refresh();

	int getTopLevelEntries();
	
	int getDocumentCount();

	void resetViewSortingToDefault();
	
	void resortView(String progColumnName, Direction direction);

	boolean isFolder();
	

	/**
	 * Returns the {@link DominoDateTime} when this view was last accessed
	 * 
	 * @return last access date/time
	 */
	DominoDateTime getLastAccessedTime();
	
	/**
	 * Returns the {@link DominoDateTime} when the view index will be discarded
	 * 
	 * @return discard date/time
	 */
	DominoDateTime getNextDiscardTime();

	CollectionSearchQuery query();
}
