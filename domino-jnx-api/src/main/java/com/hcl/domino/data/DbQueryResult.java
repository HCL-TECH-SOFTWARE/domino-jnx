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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import com.hcl.domino.data.CollectionSearchQuery.CollectionEntryProcessor;
import com.hcl.domino.misc.Loop;

public interface DbQueryResult<CHAINTYPE extends DbQueryResult<?>> {

	/**
	 * Returns the {@link Database} that was used to run the query
	 * 
	 * @return database
	 */
	Database getParentDatabase();
	
	/**
	 * Returns an {@link IDTable} of documents matching the search.
	 * 
	 * @return an {@link Optional} describing the documents matched by the search, or
	 *      an empty one if the search did not request a document collection
	 */
	Optional<IDTable> getNoteIds();
	
	Stream<Document> getDocuments();
	
	/**
	 * Build a result out of the collection entries
	 * 
	 * @param <T> result type
	 * @param skip paging offset
	 * @param count paging count
	 * @param processor builder code to produce the result
	 * @return result
	 */
	<T> T build(int skip, int count, CollectionEntryProcessor<T> processor);
	
	/**
	 * Return the note ids of the search result as an ordered {@link Set}.
	 * 
	 * <p>Implementations are likely, but not guaranteed, to return a {@link LinkedHashSet}.</p>
	 * 
	 * @param skip paging offset
	 * @param count paging count
	 * @return set of note ids
	 */
	Set<Integer> collectIds(int skip, int count);
	
	/**
	 * Adds all note ids of the search result to a note ID collection, with special support
	 * for {@link IDTable}s
	 * 
	 * @param skip paging offset
	 * @param count paging count
	 * @param idTable note ID collection
	 */
	void collectIds(int skip, int count, Collection<Integer> idTable);

	/**
	 * Dynamically computes virtual item values from the summary buffer data
	 * 
	 * @param itemsAndFormulas tuples with item name/formula; leave formula empty to read an existing document item, e.g. ["_created", "@Created", "Form", ""]
	 * @return this search query
	 */
	CHAINTYPE computeValues(String... itemsAndFormulas);

	/**
	 * Dynamically computes virtual item values from the summary buffer data
	 * 
	 * @param itemsAndFormulas map of item/formula pairs
	 * @return this search query
	 */
	CHAINTYPE computeValues(Map<String,String> itemsAndFormulas);

	/**
	 * Sorts/filters the note ids of the search result like the specified {@link DominoCollection}.
	 * Note ids that are not part of the collection will be ignored.
	 * 
	 * @param collection collection
	 * @return this search query
	 */
	CHAINTYPE sort(DominoCollection collection);

	/**
	 * Collect all {@link CollectionEntry} objects as list
	 * 
	 * @return all entries
	 */
	default List<CollectionEntry> collectEntries() {
		return collectEntries(0, Integer.MAX_VALUE);
	}
	
	/**
	 * Collect all {@link CollectionEntry} objects as list
	 * 
	 * @param skip paging offset
	 * @param count paging count
	 * @return list of collection entries
	 */
	List<CollectionEntry> collectEntries(int skip, int count);

	/**
	 * Collect all {@link CollectionEntry} objects in a {@link Collection}
	 * 
	 * @param skip paging offset
	 * @param count paging count
	 * @param collection collection to add entries
	 */
	void collectEntries(int skip, int count, Collection<CollectionEntry> collection);

	/**
	 * Iterates over each document in the search result
	 * 
	 * @param skip paging offset
	 * @param count paging count
	 * @param consumer consumer to receive document
	 */
	void forEachDocument(int skip, int count, BiConsumer<Document, Loop> consumer);

	/**
	 * Returns the total number of results
	 * 
	 * @return total
	 */
	int size();
	
}
