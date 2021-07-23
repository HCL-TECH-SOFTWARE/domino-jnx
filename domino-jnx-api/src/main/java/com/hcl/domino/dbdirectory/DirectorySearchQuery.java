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

import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.EnumSet;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import com.hcl.domino.DominoException;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.SearchQuery;
import com.hcl.domino.misc.Loop;

/**
 * With this query you can scan for files and subdirectories in a directory (either locally or on the server).<br>
 * <br>
 * Specify a formula argument to improve efficiency when processing a subset of the notes in a database.<br>
 * <br>
 * In addition, the formula argument can be used to return computed "on-the-fly" information.<br>
 * <br>
 * To do this, you specify that a value returned from a formula is to be stored in a
 * temporary field of each note.<br>
 * <br>
 * This temporary field and its value is then accessible in the summary buffer received by
 * the NSFSearch action routine without having to open the note.<br>
 * <br>
 * For example, suppose you want the size of each note found by NSFSearch.<br>
 * Do the following before the call to NSFSearch:<br>
 * Call search with a formula like this:<br>
 * "DEFAULT dLength := @DocLength; @All"<br>
 * and specify {@link SearchFlag#SUMMARY} for the SearchFlags argument.<br>
 * <br>
 * In the action routine of NSFSearch, if you get a search match, look at the summary information.<br>
 * The dLength field will be one of the items in the summary information buffer.<br>
 */
public interface DirectorySearchQuery extends SearchQuery {
	/**
	 * Flags to control result of the {@link Database#queryDQL}.
	 * These values can be bitwise ORed together to combine functionality.
	 * 
	 * Note: The values are directly taken from 
	 * 
	 * @author Karsten Lehmann
	 */
	public enum SearchFlag {
		/** Include deleted and non-matching notes in search (ALWAYS "ON" in partial searches, which are searches using a since date!) */
		ALL_VERSIONS(0x0001),
		/** TRUE to return summary buffer with each match */
		SUMMARY(0x0002),
		/** For directory mode file type filtering. If set, "NoteClassMask" is treated as a FILE_xxx mask for directory filtering */
		FILETYPE(0x0004),
		/** Set NOTE_CLASS_NOTIFYDELETION bit of NoteClass for deleted notes */
		NOTIFYDELETIONS(0x0010),
		/** by using this search option combined with a map of (key=programmatic column name, value=column formula)
		 * entries, the NSF search function just returns these specified column values instead of the
		 * whole note's summary buffer. This speeds up indexing, as Notes has to copy less data and
		 * less data needs to be parsed. */
		NOITEMNAMES(0x0020),
		/** return error if we don't have full privileges */
		ALLPRIVS(0x0040),
		/** Use current session's user name, not server's */
		SESSION_USERNAME(0x0400),
		/** Filter out "Truncated" documents */
		NOABSTRACTS(0x1000),
		/** Search formula applies only to data notes, i.e., others match */
		DATAONLY_FORMULA(0x4000),
		/** INCLUDE notes with non-replicatable OID flag */
		NONREPLICATABLE(0x8000),
		/** Full search (as if Since was "1") but exclude DATA notes prior to passed-in Since time */
		FULL_DATACUTOFF(0x02000000),
		
		/** Allow search to return id's only i.e. no summary buffer */
		NOPRIVCHECK(0x0800),
		
		/** Search includes all children of matching documents. */
		ALLCHILDREN(0x00020000),
		
		/** Search includes all descendants of matching documents. */
		ALLDESCENDANTS(0x00040000),
		
		/**
		 * Include *** ALL *** named ghost notes in the search (profile docs,
		 * xACL's, etc). Note: use SEARCH1_PROFILE_DOCS, etc., introduced in R6, for
		 * finer control
		 */
		NAMED_GHOSTS(0x08000000),
		
		/** Return only docs with protection fields (BS_PROTECTED set in note header) */
		ONLYPROTECTED(0x20000000),
		
		/** Return soft deleted documents */
		SOFTDELETIONS(0x40000000),
		
		// search1 flags
		
		/** flag to let the selection formula be run against profile documents; must
		 * be used together with {@link #PROFILE_DOCS} */
		SELECT_NAMED_GHOSTS(0x00000001 | 0x80000000),
		
		/**
		 * Include profile documents (a specific type of named ghost note) in the
		 * search Note: set {@link #SELECT_NAMED_GHOSTS}, too, if you want the
		 * selection formula to be applied to the profile docs (so as not to get
		 * them all back as matches).
		 */
		PROFILE_DOCS(0X00000002 | 0x80000000);
		
		private static EnumSet<SearchFlag> SEARCH1_FLAGS = EnumSet.of(SELECT_NAMED_GHOSTS, PROFILE_DOCS);
		
		private int m_val;
		
		SearchFlag(int val) {
			m_val = val;
		}
		
		public int getValue() {
			return m_val;
		}
		
		public static short toBitMaskSearch1Flags(Collection<SearchFlag> searchFlagSet) {
			int result = 0;
			if (searchFlagSet!=null) {
				for (SearchFlag currFlag : values()) {
					if (SEARCH1_FLAGS.contains(currFlag) && searchFlagSet.contains(currFlag)) {
						result = result | currFlag.getValue();
					}
				}
			}
			return (short) (result & 0xffff);
		}

		public static int toBitMaskStdFlagsInt(Collection<SearchFlag> searchFlagSet) {
			int result = 0;
			if (searchFlagSet!=null) {
				for (SearchFlag currFlag : values()) {
					if (!SEARCH1_FLAGS.contains(currFlag) && searchFlagSet.contains(currFlag)) {
						result = result | currFlag.getValue();
					}
				}
			}
			return result;
		}
	}
	
	/**
	 * Configures a server for the search.
	 * 
	 * @param server		the server
	 * @return				the refined query
	 */
	DirectorySearchQuery withServer(String server);
	
	/**
	 * Configure the directory to scan in
	 * 
	 * @param directory		directory ("" for top level)
	 * @return				the refined query
	 */
	DirectorySearchQuery withDirectory(String directory);
	
	/**
	 * Configures a formula for the search.
	 * Note: When performing the search, it might throw a FormulaCompilationException
	 * 
	 * @param formula		the formula
	 * @return				the refined query
	 */
	DirectorySearchQuery withFormula(String formula);

	/**
	 * Configures some flags for the query.
	 * 
	 * @param searchFlags	the flags to be used
	 * @return				the refined query
	 */
	DirectorySearchQuery withFlags(Collection<SearchFlag> searchFlags);
	
	/**
	 * Set the types of file to be returned by the query.
	 * 
	 * @param fileTypes		the file-types
	 * @return			the refined query
	 */
	DirectorySearchQuery withFileTypes(Collection<FileType> fileTypes);
	
	/**
	 * The date of the earliest modified file that is matched.
	 * The note's "Modified in this file" date is compared to this date.
	 * 
	 * @param since		the time or null if you do not wish any filtering by date
	 * @return	the refined query
	 */
	DirectorySearchQuery since(TemporalAccessor since);
	
	/**
	 * Iterates over each {@link DirEntry} in the search result
	 * 
	 * @param skip paging offset
	 * @param limit paging count
	 * @param consumer consumer to receive document
	 * @throws DominoException thrown if an error occured, e.g. the formula could not be evaluated
	 */
	void forEach(int skip, int limit, BiConsumer<DirEntry, Loop> consumer) throws DominoException;
	
	/**
	 * Returns a {@link Stream} of {@link DirEntry}, e.g. to map them to your
	 * own objects
	 * 
	 * @return stream
	 * @throws DominoException thrown if an error occured, e.g. the formula could not be evaluated
	 */
	Stream<DirEntry> stream() throws DominoException;
	
}
