/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
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
package com.hcl.domino.jna.internal.views;

import java.util.List;

import com.hcl.domino.commons.views.NotesCollectionStats;
import com.hcl.domino.commons.views.ReadMask;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.jna.data.JNACollectionEntry;
import com.hcl.domino.jna.data.JNADominoCollection;
import com.hcl.domino.misc.NotesConstants;

/**
 * Container class for a lookup result in a collection/view
 * 
 * @author Karsten Lehmann
 */
public class NotesViewLookupResultData {
	private NotesCollectionStats m_stats;
	private List<JNACollectionEntry> m_entries;
	private int m_numEntriesReturned;
	private int m_numEntriesSkipped;
	private short m_signalFlags;
	private String m_pos;
	private int m_indexModifiedSequenceNo;
	private DominoDateTime m_retDiffTime;
	
	/**
	 * Creates a new instance
	 * 
	 * @param stats collection statistics
	 * @param entries entries read from the buffer
	 * @param numEntriesSkipped number of skipped entries
	 * @param numEntriesReturned number of returned entries
	 * @param signalFlags signal flags indicating view index changes and other stuff
	 * @param pos first matching position
	 * @param indexModifiedSequenceNo index modified sequence number
	 * @param retDiffTime only set in {@link JNADominoCollection#readEntriesExt(com.hcl.domino.jna.data.JNADominoCollectionPosition, com.hcl.domino.data.Navigate, boolean, int, com.hcl.domino.data.Navigate, int, java.util.Set, DominoDateTime, com.hcl.domino.jna.data.JNAIDTable, Integer)}
	 */
	public NotesViewLookupResultData(NotesCollectionStats stats, List<JNACollectionEntry> entries, int numEntriesSkipped, int numEntriesReturned, short signalFlags, String pos, int indexModifiedSequenceNo, DominoDateTime retDiffTime) {
		m_stats = stats;
		m_entries = entries;
		m_numEntriesSkipped = numEntriesSkipped;
		m_numEntriesReturned = numEntriesReturned;
		m_signalFlags = signalFlags;
		m_pos = pos;
		m_indexModifiedSequenceNo = indexModifiedSequenceNo;
		m_retDiffTime = retDiffTime;
	}

	/**
	 * For differential view reading via {@link JNADominoCollection#readEntriesExt(com.hcl.domino.jna.data.JNADominoCollectionPosition, com.hcl.domino.data.Navigate, boolean, int, com.hcl.domino.data.Navigate, int, java.util.Set, DominoDateTime, com.hcl.domino.jna.data.JNAIDTable, Integer)},
	 * this method returns the returned diff time that can be passed in subsequent read calls to
	 * get incremental view updates
	 * 
	 * @return diff time or null
	 */
	public DominoDateTime getReturnedDiffTime() {
		return m_retDiffTime;
	}
	
	/**
	 * Returns the index modified sequence number, which is increased on every index change.<br>
	 * 
	 * @return number
	 */
	public int getIndexModifiedSequenceNo() {
		return m_indexModifiedSequenceNo;
	}
	
	/**
	 * If multiple index entries match the specified lookup key (especially if<br>
	 * not enough key items were specified), then the index position of<br>
	 * the FIRST matching entry is returned ("first" is defined by the<br>
	 * entry which collates before all others in the collated index).<br>
	 * 
	 * Will only be set when {@link JNADominoCollection#findByKeyExtended2(java.util.Set, java.util.Set, Object...)}
	 * is called.
	 * 
	 * @return position or null
	 */
	public String getPosition() {
		return m_pos;
	}
	
	/**
	 * Returns view statistics, if they have been requested via the
	 * read mask {@link ReadMask#COLLECTIONSTATS}
	 * 
	 * @return statistics or null
	 */
	public NotesCollectionStats getStats() {
		return m_stats;
	}

	/**
	 * Returns the number of view entries skipped
	 * 
	 * @return skip count
	 */
	public int getSkipCount() {
		return m_numEntriesSkipped;
	}
	
	/**
	 * Returns the number of view entries read
	 * 
	 * @return return count
	 */
	public int getReturnCount() {
		return m_numEntriesReturned;
	}
	
	/**
	 * Returns the view entry data
	 * 
	 * @return list of view entry data
	 */
	public List<JNACollectionEntry> getEntries() {
		return m_entries;
	}

	/**
	 * End of collection has not been reached because the return buffer is too full.
	 * The NIFReadEntries call should be repeated to continue reading the desired entries.
	 * 
	 * @return true if more to do
	 */
	public boolean hasMoreToDo() {
		return (m_signalFlags & NotesConstants.SIGNAL_MORE_TO_DO) == NotesConstants.SIGNAL_MORE_TO_DO;
	}
	
	/**
	 * Collection is not up to date.
	 *  
	 * @return true if database was modified
	 */
	public boolean isDatabaseModified() {
		return (m_signalFlags & NotesConstants.SIGNAL_DATABASE_MODIFIED) == NotesConstants.SIGNAL_DATABASE_MODIFIED;
	}
	
	/**
	 * At least one of the "definition" view items (Selection formula or sorting rules) has been
	 * modified by another user since the last NIFReadEntries. Upon receipt, you may wish to
	 * re-read the view note if up-to-date copies of these items are needed. You also may wish
	 * to re-synchronize your index position and re-read the rebuilt index.<br>
	 * <br>
	 * This signal is returned only ONCE per detection.
	 * 
	 * @return true if modified
	 */
	public boolean isViewDefiningItemModified() {
		return (m_signalFlags & NotesConstants.SIGNAL_DEFN_ITEM_MODIFIED) == NotesConstants.SIGNAL_DEFN_ITEM_MODIFIED;
	}

	/**
	 * At least one of the non-"definition" view items ($TITLE,etc) has been
	 * modified since last ReadEntries.
	 * Upon receipt, you may wish to re-read the view note if up-to-date copies of these
	 * items are needed.<br>
	 * <br>
	 * Signal returned only ONCE per detection
	 * 
	 * @return true if modified
	 */
	public boolean isViewOtherItemModified() {
		return (m_signalFlags & NotesConstants.SIGNAL_VIEW_ITEM_MODIFIED) == NotesConstants.SIGNAL_VIEW_ITEM_MODIFIED;
	}
	
	/**
	 * The collection index has been modified by another user since the last NIFReadEntries.
	 * Upon receipt, you may wish to re-synchronize your index position and re-read the modified index.
	 * This signal is returned only ONCE per detection.
	 * 
	 * @return true if modified
	 */
	public boolean isViewIndexModified() {
		return (m_signalFlags & NotesConstants.SIGNAL_INDEX_MODIFIED) == NotesConstants.SIGNAL_INDEX_MODIFIED;
	}

	/**
	 * Use this method to tell whether the collection contains a time-relative formula (e.g., @ Now) and
	 * will EVER be up-to-date since time-relative views, by definition, are NEVER up-to-date.
	 * 
	 * @return true if time relative
	 */
	public boolean isViewTimeRelative() {
		return (m_signalFlags & NotesConstants.SIGNAL_VIEW_TIME_RELATIVE) == NotesConstants.SIGNAL_VIEW_TIME_RELATIVE;
	}
	
	/**
	 * Returns whether the view contains documents with reader fields
	 * 
	 * @return true if reader fields
	 */
	public boolean hasDocsWithReaderFields() {
		return (m_signalFlags & NotesConstants.SIGNAL_VIEW_HASPRIVS) == NotesConstants.SIGNAL_VIEW_HASPRIVS;
	}
	
	/**	
	 * Mask that defines all "sharing conflicts" except for {@link #isDatabaseModified()}.
	 * This can be used in combination with {@link #isViewTimeRelative()} to tell if
	 * the database or collection has truly changed out from under the user or if the
	 * view is a time-relative view which will NEVER be up-to-date. {@link #isDatabaseModified()}
	 * is always returned for a time-relative view to indicate that it is never up-to-date.
	 * 
	 *  @return true if we have conflicts
	 */
	public boolean hasAnyNonDataConflicts() {
		return (m_signalFlags & NotesConstants.SIGNAL_ANY_NONDATA_CONFLICT) != 0;
	}
}