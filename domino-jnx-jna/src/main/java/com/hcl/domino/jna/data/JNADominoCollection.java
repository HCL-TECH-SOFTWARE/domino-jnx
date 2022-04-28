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
package com.hcl.domino.jna.data;

import java.lang.ref.ReferenceQueue;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiConsumer;

import com.hcl.domino.BuildVersionInfo;
import com.hcl.domino.DominoException;
import com.hcl.domino.commons.design.view.DominoCollationInfo;
import com.hcl.domino.commons.design.view.DominoCollationInfo.DominoCollateColumn;
import com.hcl.domino.commons.design.view.DominoViewFormat;
import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.PlatformUtils;
import com.hcl.domino.commons.util.StringTokenizerExt;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.commons.views.FindFlag;
import com.hcl.domino.commons.views.ReadMask;
import com.hcl.domino.data.CollectionColumn;
import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.CollectionEntry.SpecialValue;
import com.hcl.domino.data.CollectionSearchQuery;
import com.hcl.domino.data.CollectionSearchQuery.CollectionEntryProcessor;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Database.Action;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.Find;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.data.Navigate;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.exception.ObjectDisposedException;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.JNADominoClient;
import com.hcl.domino.jna.data.CollectionDataCache.CacheState;
import com.hcl.domino.jna.data.JNACollectionEntry.CacheableViewEntryData;
import com.hcl.domino.jna.internal.callbacks.NotesCallbacks;
import com.hcl.domino.jna.internal.callbacks.Win32NotesCallbacks;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNADominoCollectionAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNAIDTableAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE.ByReference;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.structs.NIFFindByKeyContextStruct;
import com.hcl.domino.jna.internal.structs.NotesCollectionPositionStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.hcl.domino.jna.internal.views.NotesLookupResultBufferDecoder;
import com.hcl.domino.jna.internal.views.NotesSearchKeyEncoder;
import com.hcl.domino.jna.internal.views.NotesViewLookupResultData;
import com.hcl.domino.misc.Loop;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.ShortByReference;

public class JNADominoCollection extends BaseJNAAPIObject<JNADominoCollectionAllocations> implements DominoCollection {
	/**
	 * Method to reverse the traversal order, e.g. from {@link Navigate#NEXT_ENTRY} to
	 * {@link Navigate#PREV_ENTRY}.
	 * 
	 * @param nav nav constant
	 * @return reversed constant
	 */
	public static Navigate reverseNav(Navigate nav) {
		switch (nav) {
		case PARENT_ENTRY:
			return Navigate.CHILD_ENTRY;
		case CHILD_ENTRY:
			return Navigate.PARENT_ENTRY;
		case NEXT_ON_SAME_LEVEL:
			return Navigate.PREV_ON_SAME_LEVEL;
		case PREV_ON_SAME_LEVEL:
			return Navigate.NEXT_ON_SAME_LEVEL;
		case FIRST_ON_SAME_LEVEL:
			return Navigate.LAST_ON_SAME_LEVEL;
		case LAST_ON_SAME_LEVEL:
			return Navigate.FIRST_ON_SAME_LEVEL;
		case NEXT_ON_TOPLEVEL:
			return Navigate.PREV_ON_TOPLEVEL;
		case PREV_ON_TOPLEVEL:
			return Navigate.NEXT_ON_TOPLEVEL;
		case NEXT_PARENT_ENTRY:
			return Navigate.PREV_PARENT_ENTRY;
		case PREV_PARENT_ENTRY:
			return Navigate.NEXT_PARENT_ENTRY;
		case NEXT_ENTRY:
			return Navigate.PREV_ENTRY;
		case PREV_ENTRY:
			return Navigate.NEXT_ENTRY;
		case NEXT_UNREAD_ENTRY:
			return Navigate.PREV_UNREAD_ENTRY;
		case NEXT_UNREAD_TOPLEVEL_ENTRY:
			return Navigate.PREV_UNREAD_TOPLEVEL_ENTRY;
		case PREV_UNREAD_TOPLEVEL_ENTRY:
			return Navigate.NEXT_UNREAD_TOPLEVEL_ENTRY;
		case PREV_UNREAD_ENTRY:
			return Navigate.NEXT_UNREAD_ENTRY;
		case NEXT_SELECTED:
			return Navigate.PREV_SELECTED;
		case PREV_SELECTED:
			return Navigate.NEXT_SELECTED;
		case NEXT_SELECTED_ON_TOPLEVEL:
			return Navigate.PREV_SELECTED_ON_TOPLEVEL;
		case PREV_SELECTED_ON_TOPLEVEL:
			return Navigate.NEXT_SELECTED_ON_TOPLEVEL;
		case NEXT_EXPANDED:
			return Navigate.PREV_EXPANDED;
		case PREV_EXPANDED:
			return Navigate.NEXT_EXPANDED;
		case NEXT_EXPANDED_UNREAD:
			return Navigate.PREV_EXPANDED_UNREAD;
		case PREV_EXPANDED_UNREAD:
			return Navigate.NEXT_EXPANDED_UNREAD;
		case NEXT_EXPANDED_SELECTED:
			return Navigate.PREV_EXPANDED_SELECTED;
		case PREV_EXPANDED_SELECTED:
			return Navigate.NEXT_EXPANDED_SELECTED;
		case NEXT_EXPANDED_CATEGORY:
			return Navigate.PREV_EXPANDED_CATEGORY;
		case PREV_EXPANDED_CATEGORY:
			return Navigate.NEXT_EXPANDED_CATEGORY;
		case NEXT_EXPANDED_DOCUMENT:
			return Navigate.PREV_EXPANDED_DOCUMENT;
		case PREV_EXPANDED_DOCUMENT:
			return Navigate.NEXT_EXPANDED_DOCUMENT;
		/*
		case NEXT_HIT:
			return Navigate.PREV_HIT;
		case PREV_HIT:
			return Navigate.NEXT_HIT;
		case NEXT_SELECTED_HIT:
			return Navigate.PREV_SELECTED_HIT;
		case PREV_SELECTED_HIT:
			return Navigate.NEXT_SELECTED_HIT;
		case NEXT_UNREAD_HIT:
			return Navigate.PREV_UNREAD_HIT;
		case PREV_UNREAD_HIT:
			return Navigate.NEXT_UNREAD_HIT;
		*/
		case NEXT_CATEGORY:
			return Navigate.PREV_CATEGORY;
		case PREV_CATEGORY:
			return Navigate.NEXT_CATEGORY;
		case NEXT_DOCUMENT:
			return Navigate.PREV_DOCUMENT;
		case PREV_DOCUMENT:
			return Navigate.NEXT_DOCUMENT;
		default:
			return nav;
		}
	}
	
	/**
	 * Method to check whether a skip or return navigator returns view data from last to first entry
	 * 
	 * @param nav navigator mode
	 * @return true if descending
	 */
	public static boolean isDescendingNav(Navigate nav) {
		boolean descending = nav == Navigate.PREV_ENTRY ||
				nav == Navigate.PREV_CATEGORY ||
				nav == Navigate.PREV_EXPANDED_DOCUMENT ||
				nav == Navigate.PREV_EXPANDED ||
				nav == Navigate.PREV_EXPANDED_CATEGORY ||
				nav == Navigate.PREV_EXPANDED_SELECTED ||
				nav == Navigate.PREV_EXPANDED_UNREAD ||
//				nav == Navigate.PREV_HIT ||
				nav == Navigate.PREV_ON_TOPLEVEL ||
				nav == Navigate.PREV_DOCUMENT ||
				nav == Navigate.PREV_PARENT_ENTRY ||
				nav == Navigate.PREV_ON_SAME_LEVEL ||
				nav == Navigate.PREV_SELECTED ||
//				nav == Navigate.PREV_SELECTED_HIT ||
				nav == Navigate.PREV_SELECTED_ON_TOPLEVEL ||
				nav == Navigate.PREV_UNREAD_ENTRY ||
//				nav == Navigate.PREV_UNREAD_HIT ||
				nav == Navigate.PREV_UNREAD_TOPLEVEL_ENTRY ||
				nav == Navigate.PARENT_ENTRY;
	
		return descending;
	}
	
	private JNADatabase m_parentDbData;
	private int m_viewNoteId;
	private String m_viewUnid;
	private String m_name;
	private List<String> m_aliases;
	private JNADocument m_viewNote;

	private List<String> m_columnItemNames;
	private List<String> m_columnTitles;
	
	private CollationInfo m_collationInfo;

	private Map<String, Integer> m_columnIndicesByItemName;
	private Map<String, Integer> m_columnIndicesByTitle;
	private Map<Integer, String> m_columnNamesByIndex;
	private Map<Integer, Boolean> m_columnIsCategoryByIndex;
	private Map<Integer, String> m_columnTitlesLCByIndex;
	private Map<Integer, String> m_columnTitlesByIndex;
	private DominoViewFormat m_viewFormat;
	private boolean m_autoUpdate = true;

	JNADominoCollection(JNADatabase parentDbView, JNADatabase parentDbData, ByReference rethCollection, int viewNoteId,
			String viewUNID,
			JNAIDTable collapsedList, JNAIDTable selectedList, JNAIDTable unreadTable) {
		super(parentDbView);
		
		//just keep hard reference to data DB so that GC will not purge it as long as the DominoCollection object exists
		m_parentDbData = parentDbData;
		m_viewNoteId = viewNoteId;
		m_viewUnid = viewUNID;
		
		JNADominoCollectionAllocations allocations = getAllocations();
		
		allocations.setCollectionHandle(rethCollection);
		allocations.setCollapsedList(collapsedList);
		allocations.setSelectedList(selectedList);
		allocations.setUnreadTable(unreadTable);
		
		setInitialized();
	}

	@Override
	protected void checkDisposedLocal() {
    super.checkDisposedLocal();
    
	  if (m_parentDbData!=null && m_parentDbData.isDisposed()) {
	    throw new DominoException("Database to read view data is already disposed");
	  }
	}
	
	@Override
	public Database getParentDatabase() {
		return (Database) getParent();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected JNADominoCollectionAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		
		return new JNADominoCollectionAllocations(parentDominoClient, parentAllocations, this, queue);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected <T> T getAdapterLocal(Class<T> clazz) {
		if (clazz == Document.class || clazz == JNADocument.class) {
			return (T) getViewNote();
		}
		
		return null;
	}

	@Override
	public String getUNID() {
		return m_viewUnid;
	}
	
	/**
	 * Returns the index modified sequence number that can be used to track view changes.
	 * The method calls {@link #getLastModifiedTime()} and returns part of the result (Innards[0]).
	 * 
	 * @return index modified sequence number
	 */
	public int getIndexModifiedSequenceNo() {
		JNADominoDateTime ndtModified = (JNADominoDateTime) getLastModifiedTime();
		return ndtModified.getInnards()[0];
	}

	/**
	 * Each time the number of documents in a collection is modified, a sequence number
	 * is incremented.  This function will return the modification sequence number, which
	 * may then be compared to a previous value (also obtained by calling
	 * {@link #getLastModifiedTime}) to determine whether or not the number of documents in the
	 * collection has been changed.<br>
	 * <br>Note that the DominoDateTime value returned by this function is not an actual time.
	 * 
	 * @return time date
	 */
	public DominoDateTime getLastModifiedTime() {
		checkDisposed();
		
		NotesTimeDateStruct retLastModifiedTime = NotesTimeDateStruct.newInstance();
		
		JNADominoCollectionAllocations allocations = getAllocations();
		
		LockUtil.lockHandle(allocations.getCollectionHandle(), (handleByVal) -> {
			NotesCAPI.get().NIFGetLastModifiedTime(handleByVal, retLastModifiedTime);
			return 0;
		});
		
		return new JNADominoDateTime(retLastModifiedTime);
	}
	
	/**
	 * Returns the {@link DominoDateTime} when this view was last accessed
	 * 
	 * @return last access date/time
	 */
	@Override
	public DominoDateTime getLastAccessedTime() {
		checkDisposed();
		
		NotesTimeDateStruct retLastAccessedTime = NotesTimeDateStruct.newInstance();
		
		JNADominoCollectionAllocations allocations = getAllocations();
		
		LockUtil.lockHandle(allocations.getCollectionHandle(), (handleByVal) -> {
			NotesCAPI.get().NIFGetLastAccessedTime(handleByVal, retLastAccessedTime);
			return 0;
		});
		
		return new JNADominoDateTime(retLastAccessedTime);
	}
	
	/**
	 * Returns the {@link DominoDateTime} when the view index will be discarded
	 * 
	 * @return discard date/time
	 */
	@Override
	public DominoDateTime getNextDiscardTime() {
		checkDisposed();
		
		NotesTimeDateStruct retNextDiscardTime = NotesTimeDateStruct.newInstance();
		
		JNADominoCollectionAllocations allocations = getAllocations();
		
		LockUtil.lockHandle(allocations.getCollectionHandle(), (handleByVal) -> {
			NotesCAPI.get().NIFGetNextDiscardTime(handleByVal, retNextDiscardTime);
			return 0;
		});
		
		return new JNADominoDateTime(retNextDiscardTime);
	}

	
	@Override
	public String getName() {
		if (m_name==null) {
			decodeNameAndAliases();
		}
		return m_name;
	}

	private JNADocument getViewNote() {
		checkDisposed();

		if (m_viewNote==null || m_viewNote.isDisposed()) {
			m_viewNote = (JNADocument) ((JNADatabase)getParent()).getDocumentByUNID(m_viewUnid).orElse(null);
		}
		return m_viewNote;
	}
	
	private void decodeNameAndAliases() {
		JNADocument viewNote = getViewNote();
		
		List<String> aliases = new ArrayList<>();
		String name = ""; //$NON-NLS-1$
		
		String title = viewNote.get("$TITLE", String.class, ""); //$NON-NLS-1$ //$NON-NLS-2$
		StringTokenizerExt st = new StringTokenizerExt(title, "|"); //$NON-NLS-1$
		if (st.hasMoreTokens()) {
			name = st.nextToken();
			
			while (st.hasMoreTokens()) {
				aliases.add(st.nextToken());
			}
		}
		m_name = name;
		m_aliases = Collections.unmodifiableList(aliases);
	}
	
	@Override
	public List<String> getAliases() {
		checkDisposed();
		
		if (m_aliases==null) {
			decodeNameAndAliases();
		}
		return m_aliases;
	}

	@Override
	public String getSelectionFormula() {
		checkDisposed();
		
		JNADocument note = getViewNote();
		List<?> formulaObj = note.getItemValue(DesignConstants.VIEW_FORMULA_ITEM);
		String formula = formulaObj!=null && !formulaObj.isEmpty() ? formulaObj.get(0).toString() : ""; //$NON-NLS-1$
		return formula;
	}

	@Override
	public IDTable getAllIdsAsIDTable(boolean checkRights) {
		checkDisposed();
		
		JNAIDTable idTable = new JNAIDTable(getParentDominoClient());
		JNAIDTableAllocations idTableAllocations = (JNAIDTableAllocations) idTable.getAdapter(APIObjectAllocations.class);
		
		JNADominoCollectionAllocations allocations = getAllocations();
		
		short result = LockUtil.lockHandles(allocations.getCollectionHandle(), idTableAllocations.getIdTableHandle(),
				(hCollectionByVal, idTableHandleByVal) -> {
					
			return NotesCAPI.get().NIFGetIDTableExtended(hCollectionByVal, Navigate.NEXT_ENTRY.getValue(),
					(short) (checkRights ? 0 : 1), idTableHandleByVal);

		});
		
		NotesErrorUtils.checkResult(result);

		return idTable;
	}

	@Override
	public void forEachDocument(int skip, int count, BiConsumer<Document, Loop> consumer) {
		query().forEachDocument(skip, count, consumer);
	}
	
	@Override
	public CollectionSearchQuery query() {
		return new JNACollectionSearchQuery(this);
	}

	@Override
	public List<CollectionColumn> getColumns() {
		return Collections.unmodifiableList(getViewFormat().getColumns());
	}

	@Override
	public void refresh() {
		checkDisposed();
		
		JNADominoCollectionAllocations allocations = getAllocations();
		short result = LockUtil.lockHandle(allocations.getCollectionHandle(), (handleByVal) -> {
			return NotesCAPI.get().NIFUpdateCollection(handleByVal);
		});
		NotesErrorUtils.checkResult(result);
	}

	@Override
	public int getTopLevelEntries() {
		NotesViewLookupResultData lkData = readEntries(new JNADominoCollectionPosition("0"), Navigate.CURRENT, false, 0, Navigate.CURRENT, 0, EnumSet.of(ReadMask.COLLECTIONSTATS)); //$NON-NLS-1$
		return lkData.getStats().getTopLevelEntries();
	}

	@Override
	public int getDocumentCount() {
		checkDisposed();
		
		JNADominoCollectionAllocations allocations = getAllocations();
		
		IntByReference retDocCount = new IntByReference();
		
		short result = LockUtil.lockHandle(allocations.getCollectionHandle(), (handleByVal) -> {
			return NotesCAPI.get().NIFGetCollectionDocCountLW(handleByVal, retDocCount);
		});
		NotesErrorUtils.checkResult(result);
		
		return retDocCount.getValue();
	}

	@Override
	public void resetViewSortingToDefault() {
		setCollation((short) 0);
	}

	/**
	 * Sets the active collation (collection column sorting)
	 * 
	 * @param collation collation
	 */
	private void setCollation(short collation) {
		checkDisposed();
		
		JNADominoCollectionAllocations allocations = getAllocations();
		short result = LockUtil.lockHandle(allocations.getCollectionHandle(), (handleByVal) -> {
			return NotesCAPI.get().NIFSetCollation(handleByVal, collation);
		});
		NotesErrorUtils.checkResult(result);
	}
	
	@Override
	public void resortView(String progColumnName, Direction direction) {
		short collation = findCollation(progColumnName, direction);
		if (collation==-1) {
			throw new DominoException(
				MessageFormat.format(
					"Column {0} does not exist in view {1} or is not sortable in {2} direction",
					progColumnName, getName(), direction
				)
			);
		}
		setCollation(collation);
	}

	/**
	 * Finds the matching collation number for the specified sort column and direction
	 * Convenience method that calls {@link #getCollationsInfo()} and {@link CollationInfo#findCollation(String, Direction)}
	 * 
	 * @param columnName sort column name
	 * @param direction sort direction
	 * @return collation number or -1 if not found
	 */
	private short findCollation(String columnName, Direction direction) {
		return getCollationsInfo().findCollation(columnName, direction);
	}

	/**
	 * Returns programmatic names and sorting of sortable columns
	 * 
	 * @return info object with collation info
	 */
	private CollationInfo getCollationsInfo() {
		if (m_collationInfo==null) {
			scanColumns();
		}
		return m_collationInfo;
	}

	/**
	 * Decodes the view format and column information
	 * 
	 * @return view format
	 */
	private DominoViewFormat getViewFormat() {
		if (m_viewFormat==null) {
			scanColumns();
		}
		return m_viewFormat;
	}

	/**
	 * New method to read information about view columns and sortings using C methods
	 */
	private void scanColumns() {
		m_columnItemNames = new ArrayList<>();
		m_columnTitles = new ArrayList<>();
		
		m_columnIndicesByItemName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		m_columnIndicesByTitle = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		
		m_columnNamesByIndex = new TreeMap<>();
		m_columnIsCategoryByIndex = new TreeMap<>();
		m_columnTitlesLCByIndex = new TreeMap<>();
		m_columnTitlesByIndex = new TreeMap<>();

		JNADocument viewNote = getViewNote();
		
		//read collations
		CollationInfo collationInfo = new CollationInfo();

		int colNo = 0;
		boolean readCollations = false;
		
		while (viewNote.hasItem("$Collation"+(colNo==0 ? "" : colNo))) { //$NON-NLS-1$ //$NON-NLS-2$
			List<?> collationInfoList = viewNote.getItemValue("$Collation"+(colNo==0 ? "" : colNo)); //$NON-NLS-1$ //$NON-NLS-2$
			if (collationInfoList!=null && !collationInfoList.isEmpty()) {
				readCollations = true;
				
				DominoCollationInfo colInfo = (DominoCollationInfo) collationInfoList.get(0);
				
				List<DominoCollateColumn> collateColumns = colInfo.getColumns();
				if (!collateColumns.isEmpty()) {
				  DominoCollateColumn firstCollateDesc = collateColumns.get(0);
				  String currItemName = firstCollateDesc.getName();
				  boolean isDescending = firstCollateDesc.isDescending();
				  
          collationInfo.addCollation((short) colNo, currItemName,
              isDescending ? Direction.Descending : Direction.Ascending);
				}
			}
			colNo++;
		}
		
		m_collationInfo = collationInfo;

		if (!readCollations) {
			throw new AssertionError(MessageFormat.format("View note with UNID {0} contains no collations", m_viewUnid));
		}
		
		//read view columns
		List<?> viewFormatList = viewNote.getItemValue("$VIEWFORMAT"); //$NON-NLS-1$
		if (viewFormatList!=null && !viewFormatList.isEmpty()) {
			DominoViewFormat format = (DominoViewFormat) viewFormatList.get(0);
			m_viewFormat = format;
			List<CollectionColumn> columns = format.getColumns();
			
			for (int i=0; i<columns.size(); i++) {
				CollectionColumn currCol = columns.get(i);
				String currItemName = currCol.getItemName();
				String currTitle = currCol.getTitle();
				
				int currColumnValuesIndex = currCol.getColumnValuesIndex();
				
				m_columnItemNames.add(currItemName);
				m_columnTitles.add(currTitle);
				m_columnIndicesByItemName.put(currItemName, currColumnValuesIndex);
				m_columnIndicesByTitle.put(currTitle, currColumnValuesIndex);

				m_columnNamesByIndex.put(currColumnValuesIndex, currItemName);
				m_columnTitlesLCByIndex.put(currColumnValuesIndex, currTitle);
				m_columnTitlesByIndex.put(currColumnValuesIndex, currTitle);

				boolean isCategory = currCol.getSortConfiguration().isCategory();
				m_columnIsCategoryByIndex.put(currColumnValuesIndex, isCategory);
			}
		}
	}
	
	@Override
	public boolean isFolder() {
		Document viewNote = getViewNote();
		String flags = viewNote.get(NotesConstants.DESIGN_FLAGS, String.class, ""); //$NON-NLS-1$
		return flags.contains(NotesConstants.DESIGN_FLAG_FOLDER_VIEW);
	}

	/**
	 * Container object for a collection lookup result
	 * 
	 * @author Karsten Lehmann
	 */
	public static class FindResult {
		private String m_position;
		private int m_entriesFound;
		private boolean m_hasExactNumberOfMatches;
		
		/**
		 * Creates a new instance
		 * 
		 * @param position position of the first match
		 * @param entriesFound number of entries found or 1 if hasExactNumberOfMatches is <code>false</code>
		 * @param hasExactNumberOfMatches true if Notes was able to count the number of matches (e.g. for string key lookups with full or partial matches)
		 */
		public FindResult(String position, int entriesFound, boolean hasExactNumberOfMatches) {
			m_position = position;
			m_entriesFound = entriesFound;
			m_hasExactNumberOfMatches = hasExactNumberOfMatches;
		}

		/**
		 * Returns the number of entries found or 1 if hasExactNumberOfMatches is <code>false</code>
		 * and any matches were found
		 * 
		 * @return count
		 */
		public int getEntriesFound() {
			return m_entriesFound;
		}

		/**
		 * Returns the position of the first match
		 * 
		 * @return position
		 */
		public String getPosition() {
			return m_position;
		}
		
		/**
		 * Use this method to check whether Notes was able to count the number of matches
		 * (e.g. for string key lookups with full or partial matches)
		 * 
		 * @return true if we have an exact match count
		 */
		public boolean hasExactNumberOfMatches() {
			return m_hasExactNumberOfMatches;
		}
	}
	
	public FindResult findByKey(Set<FindFlag> findFlags, Object... keys) {
		checkDisposed();
		
		if (keys==null || keys.length==0) {
			throw new IllegalArgumentException("No search keys specified");
		}
		
		IntByReference retNumMatches = new IntByReference();
		NotesCollectionPositionStruct retIndexPos = NotesCollectionPositionStruct.newInstance();
		short findFlagsBitMask = FindFlag.toBitMask(findFlags);
		short result;
		
		
		Memory keyBuffer;
		try {
			keyBuffer = NotesSearchKeyEncoder.encodeKeys(keys);
		} catch (Throwable e) {
			throw new DominoException(0, "Could not encode search keys", e);
		}
		
		JNADominoCollectionAllocations allocations = getAllocations();
		result = LockUtil.lockHandle(allocations.getCollectionHandle(), (handleByVal) -> {
			
			return NotesCAPI.get().NIFFindByKey(handleByVal, keyBuffer, findFlagsBitMask,
					retIndexPos, retNumMatches);
			
		});
		
		if ((result & NotesConstants.ERR_MASK)==1028) {
			return new FindResult("", 0, canFindExactNumberOfMatches(findFlags)); //$NON-NLS-1$
		}
		
		NotesErrorUtils.checkResult(result);
		
		int nMatchesFound = retNumMatches.getValue();

		int[] retTumbler = retIndexPos.Tumbler;
		short retLevel = retIndexPos.Level;
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<=retLevel; i++) {
			if (sb.length()>0) {
				sb.append("."); //$NON-NLS-1$
			}

			sb.append(retTumbler[i]);
		}

		String firstMatchPos = sb.toString();
		return new FindResult(firstMatchPos, nMatchesFound, canFindExactNumberOfMatches(findFlags));
	}
	
	/**
	 * Returns true if response document hierarchy is displayed in the view.
	 * 
	 * @return true for response hierarchy, false for flat view
	 */
	public boolean isHierarchical() {
		return getViewFormat().isHierarchical();
	}
	
	/**
	 * Returns true if conflict documents are displayed in the view
	 * 
	 * @return true to show conflicts
	 */
	public boolean isConflict() {
		return getViewFormat().isConflict();
	}
	
	/**
	 * Returns true if view should be collapsed by default
	 * 
	 * @return true if collapsed
	 */
	public boolean isCollapsed() {
		return getViewFormat().isCollapsed();
	}
	
	/**
	 * Position to top when view is opened.
	 * 
	 * @return to go position to top
	 */
	public boolean isGotoTopOnOpen() {
		return getViewFormat().isGotoTopOnOpen();
	}

	/**
	 * Position to top when view is refreshed (as if the user pressed
	 * F9 and Ctrl-Home). When both {@link #isGotoTopOnRefresh()}
	 * and {@link #isGotoBottomOnRefresh()} are set, the view will be
	 * refreshed from the current top row (as if the user pressed F9).
	 * When both flags are clear, automatic refresh of display on update
	 * notification is disabled. In this case, the refresh indicator will be displayed
	 * 
	 * @return true to go to top
	 */
	public boolean isGotoTopOnRefresh() {
		return getViewFormat().isGotoTopOnRefresh();
	}

	/**
	 * Position to bottom when view is opened.
	 * 
	 * @return true to go to bottom
	 */
	public boolean isGotoBottomOnOpen() {
		return getViewFormat().isGotoBottomOnOpen();
	}

	/**
	 *  Position to bottom when view is refreshed (as if the user pressed
	 *  F9 and Ctrl-End). When both {@link #isGotoTopOnRefresh()}
	 *  and {@link #isGotoBottomOnRefresh()} are set, the view will be
	 *  refreshed from the current top row (as if the user pressed F9).
	 *  When both flags are clear, automatic refresh of display on update
	 *  notification is disabled. In this case, the refresh indicator will be displayed.
	 *  
	 * @return true to go to bottom
	 */
	public boolean isGotoBottomOnRefresh() {
		return getViewFormat().isGotoBottomOnRefresh();
	}

	/**
	 * TRUE if last column should be extended to fit the window width.
	 * 
	 * @return extend flag
	 */
	public boolean isExtendLastColumn() {
		return getViewFormat().isExtendLastColumn();
	}
	
	/**
	 * Returns the view column at the specified index
	 * 
	 * @param columnIndex index starting with 0
	 * @return column
	 */
	public CollectionColumn getColumn(int columnIndex) {
		return getViewFormat().getColumns().get(columnIndex);
	}

	/**
	 * Returns the column title for a column
	 * 
	 * @param columnIndex column index
	 * @return title
	 */
	public String getColumnTitle(int columnIndex) {
		return getColumn(columnIndex).getTitle();
	}

	public NotesViewLookupResultData findByKeyExtended2(Set<FindFlag> findFlags, Set<ReadMask> returnMask, Object... keys) {
		checkDisposed();
		
		if (keys==null || keys.length==0) {
			throw new IllegalArgumentException("No search keys specified");
		}
		
		IntByReference retNumMatches = new IntByReference();
		NotesCollectionPositionStruct retIndexPos = NotesCollectionPositionStruct.newInstance();
		int findFlagsBitMask = FindFlag.toBitMaskInt(findFlags);
		short result;
		int returnMaskBitMask = ReadMask.toBitMask(returnMask);
		
		ShortByReference retSignalFlags = new ShortByReference();
		
		Memory keyBuffer;
		try {
			keyBuffer = NotesSearchKeyEncoder.encodeKeys(keys);
		} catch (Throwable e) {
			throw new DominoException(MessageFormat.format("Could not encode search keys: {0}", Arrays.toString(keys)), e);
		}
		
		DHANDLE.ByReference retBuffer = DHANDLE.newInstanceByReference();
		IntByReference retSequence = new IntByReference();
		
		JNADominoCollectionAllocations allocations = getAllocations();
		
		result = LockUtil.lockHandle(allocations.getCollectionHandle(), (handleByVal) -> {
			short localResult = NotesCAPI.get().NIFFindByKeyExtended2(handleByVal, keyBuffer, findFlagsBitMask, 
					returnMaskBitMask, retIndexPos, retNumMatches, retSignalFlags, retBuffer, retSequence);
			return localResult;
		});
		
		if ((result & NotesConstants.ERR_MASK)==1028) {
			return new NotesViewLookupResultData(null, new ArrayList<JNACollectionEntry>(0), 0, 0,
					retSignalFlags.getValue(), null, retSequence.getValue(), null);
		}
		NotesErrorUtils.checkResult(result);

		if (retNumMatches.getValue()==0) {
			return new NotesViewLookupResultData(null, new ArrayList<JNACollectionEntry>(0), 0, 0, retSignalFlags.getValue(), null, retSequence.getValue(), null);
		}
		else {
			if (retBuffer.isNull()) {
				return new NotesViewLookupResultData(null, new ArrayList<JNACollectionEntry>(0), 0, retNumMatches.getValue(), retSignalFlags.getValue(), retIndexPos.toPosString(), retSequence.getValue(), null);
			}
			else {
				boolean convertStringsLazily = true;
				boolean convertNotesTimeDateToCalendar = false;
				
				NotesViewLookupResultData viewData = NotesLookupResultBufferDecoder.decodeCollectionLookupResultBuffer(this, retBuffer,
						0, retNumMatches.getValue(), returnMask, retSignalFlags.getValue(), retIndexPos.toPosString(), retSequence.getValue(), null,
						convertStringsLazily, convertNotesTimeDateToCalendar, null);
				return viewData;
			}
		}
	}
	
	/**
	 * If the specified find flag uses an inequality search like {@link FindFlag#LESS_THAN}
	 * or {@link FindFlag#GREATER_THAN}, this method returns true, meaning that
	 * the Notes API cannot return an exact number of matches.
	 * 
	 * @param findFlags find flags
	 * @return true if exact number of matches can be returned
	 */
	private boolean canFindExactNumberOfMatches(Set<FindFlag> findFlags) {
		if (findFlags.contains(FindFlag.LESS_THAN)) {
			return false;
		}
		else if (findFlags.contains(FindFlag.GREATER_THAN)) {
			return false;
		}
		else {
			return true;
		}
	}
	
	/**
	 * Container class with view collation information (collation index vs. sort item name and sort direction)
	 * 
	 * @author Karsten Lehmann
	 */
	private static class CollationInfo {
		private Map<String,Short> m_ascendingLookup;
		private Map<String,Short> m_descendingLookup;
		private Map<Short,String> m_collationSortItem;
		private Map<Short,Direction> m_collationSorting;
		private int m_nrOfCollations;
		
		/**
		 * Creates a new instance
		 */
		public CollationInfo() {
			m_ascendingLookup = new HashMap<>();
			m_descendingLookup = new HashMap<>();
			m_collationSortItem = new HashMap<>();
			m_collationSorting = new HashMap<>();
		}
		
		/**
		 * Internal method to populate the maps
		 * 
		 * @param collation collation index
		 * @param itemName sort item name
		 * @param direction sort direction
		 */
		void addCollation(short collation, String itemName, Direction direction) {
			String itemNameLC = itemName.toLowerCase();
			if (direction == Direction.Ascending) {
				m_ascendingLookup.put(itemNameLC, collation);
			}
			else if (direction == Direction.Descending) {
				m_descendingLookup.put(itemNameLC, collation);
			}
			m_nrOfCollations = Math.max(m_nrOfCollations, collation);
			m_collationSorting.put(collation, direction);
			m_collationSortItem.put(collation, itemNameLC);
		}
		
		/**
		 * Returns the total number of collations
		 * 
		 * @return number
		 */
		@SuppressWarnings("unused")
		public int getNumberOfCollations() {
			return m_nrOfCollations;
		}
		
		/**
		 * Finds a collation index
		 * 
		 * @param sortItem sort item name
		 * @param direction sort direction
		 * @return collation index or -1 if not found
		 */
		public short findCollation(String sortItem, Direction direction) {
			String itemNameLC = sortItem.toLowerCase();
			if (direction==Direction.Ascending) {
				Short collation = m_ascendingLookup.get(itemNameLC);
				return collation==null ? -1 : collation;
			}
			else {
				Short collation = m_descendingLookup.get(itemNameLC);
				return collation==null ? -1 : collation;
			}
		}
		
		/**
		 * Returns the sort item name of a collation
		 * 
		 * @param collation collation index
		 * @return sort item name
		 */
		@SuppressWarnings("unused")
		public String getSortItem(int collation) {
			if (collation > m_nrOfCollations) {
				throw new IndexOutOfBoundsException(MessageFormat.format("Unknown collation index (max value: {0})", m_nrOfCollations));
			}
			
			String sortItem = m_collationSortItem.get(Short.valueOf((short)collation));
			return sortItem;
		}
		
		/**
		 * Returns the sort direction of a collation
		 * 
		 * @param collation collation index
		 * @return sort direction
		 */
		public Direction getSortDirection(int collation) {
			if (collation > m_nrOfCollations) {
				throw new IndexOutOfBoundsException(MessageFormat.format("Unknown collation index (max value: {0})", m_nrOfCollations));
			}
			
			Direction direction = m_collationSorting.get(Short.valueOf((short)collation));
			return direction;
		}
	}

	@Override
	public int getNoteId() {
		return m_viewNoteId;
	}
	
	/**
	 * Returns an iterator of all available columns for which we can read column values
	 * (e.g. does not return static column names)
	 * 
	 * @return programmatic column names converted to lowercase in the order they appear in the view
	 */
	public List<String> getColumnNames() {
		if (m_columnItemNames==null) {
			scanColumns();
		}
		return Collections.unmodifiableList(m_columnItemNames);
	}

	/**
	 * Returns the number of columns for which we can read column data (e.g. does not count columns
	 * with static values)
	 * 
	 * @return number of columns
	 */
	public int getNumberOfColumns() {
		return getViewFormat().getColumns().size();
	}

	/**
	 * Returns the column values index for the specified programmatic column name
	 * or column title
	 * 
	 * @param columnNameOrTitle programmatic column name or title, case insensitive
	 * @return index or -1 for unknown columns; returns 65535 for static column values that are not returned as column values
	 */
	public int getColumnValuesIndex(String columnNameOrTitle) {
		if (m_columnIndicesByItemName==null) {
			scanColumns();
		}
		Integer idx = m_columnIndicesByItemName.get(columnNameOrTitle);
		if (idx==null) {
			idx = m_columnIndicesByTitle.get(columnNameOrTitle);
		}
		return idx==null ? -1 : idx;
	}
	
	/**
	 * Returns the programmatic column name for a column index
	 * 
	 * @param index index starting with 0
	 * @return column name
	 */
	public String getColumnName(int index) {
		return getColumn(index).getItemName();
	}

	/**
	 * Reads collection entries (using NIFReadEntries method).<br>
	 * <br>
	 * This method provides low-level API access. In general, it is safer to use high-level functions like
	 * {@link #getAllEntries} instead because
	 * they handle view index update while reading.
	 * 
	 * @param startPos start position for the scan; will be modified by the method to reflect the current position
	 * @param skipNavigator navigator to use for the skip operation
	 * @param skipNavigatorContinue true to set NAVIGATE_CONTINUE (don't return error when skipping too many entries)
	 * @param skipCount number of entries to skip
	 * @param returnNavigator navigator to use for the read operation
	 * @param returnCount number of entries to read
	 * @param returnMask bitmask of data to read
	 * @return read data
	 */
	public NotesViewLookupResultData readEntries(JNADominoCollectionPosition startPos,
			Navigate skipNavigator, boolean skipNavigatorContinue,
			int skipCount, Navigate returnNavigator, int returnCount, EnumSet<ReadMask> returnMask) {
		checkDisposed();

		IntByReference retNumEntriesSkipped = new IntByReference();
		IntByReference retNumEntriesReturned = new IntByReference();
		ShortByReference retSignalFlags = new ShortByReference();
		ShortByReference retBufferLength = new ShortByReference();

		short skipNavBitMask = skipNavigatorContinue ? (short) ((skipNavigator.getValue() | NotesConstants.NAVIGATE_CONTINUE) & 0xffff) : skipNavigator.getValue();

		short returnNavBitMask = returnNavigator.getValue();
		int readMaskBitMask = ReadMask.toBitMask(returnMask);
		
		NotesCollectionPositionStruct startPosStruct = startPos==null ? null : startPos.getAdapter(NotesCollectionPositionStruct.class);
		
		short result;
		
		JNADominoCollectionAllocations allocations = getAllocations();

		DHANDLE.ByReference retBuffer = DHANDLE.newInstanceByReference();
		
		result = LockUtil.lockHandle(allocations.getCollectionHandle(), (hCollectionByVal) -> {
			return NotesCAPI.get().NIFReadEntries(hCollectionByVal, // hCollection
					startPosStruct, // IndexPos
					skipNavBitMask, // SkipNavigator
					skipCount, // SkipCount
					returnNavBitMask, // ReturnNavigator
					returnCount, // ReturnCount
					readMaskBitMask, // Return mask
					retBuffer, // rethBuffer
					retBufferLength, // retBufferLength
					retNumEntriesSkipped, // retNumEntriesSkipped
					retNumEntriesReturned, // retNumEntriesReturned
					retSignalFlags // retSignalFlags
					);
		});
		
		if ((result & NotesConstants.ERR_MASK)!=1028) {
			NotesErrorUtils.checkResult(result);
		}

		int indexModifiedSequenceNo = getIndexModifiedSequenceNo();

		int iBufLength = retBufferLength.getValue() & 0xffff;
		if (iBufLength==0) {
			return new NotesViewLookupResultData(null, new ArrayList<JNACollectionEntry>(0), retNumEntriesSkipped.getValue(), retNumEntriesReturned.getValue(), retSignalFlags.getValue(), null, indexModifiedSequenceNo, null);
		}
		else {
			boolean convertStringsLazily = true;
			boolean convertNotesTimeDateToCalendar = false;
			
			NotesViewLookupResultData viewData = NotesLookupResultBufferDecoder.decodeCollectionLookupResultBuffer(this, retBuffer,
					retNumEntriesSkipped.getValue(), retNumEntriesReturned.getValue(), returnMask, retSignalFlags.getValue(), null,
					indexModifiedSequenceNo, null, convertStringsLazily, convertNotesTimeDateToCalendar, null);
			return viewData;
		}
	}

	/**
	 * Reads collection entries with extended funcionality (using undocumented NIFReadEntriesExt method).<br>
	 * <br>
	 * This method provides low-level API access. In general, it is safer to use high-level functions like
	 * {@link #getAllEntries} instead because
	 * they handle view index update while reading.
	 * 
	 * @param startPos start position for the scan; will be modified by the method to reflect the current position
	 * @param skipNavigator navigator to use for the skip operation
	 * @param skipNavigatorContinue true to set NAVIGATE_CONTINUE (don't return error when skipping too many entries)
	 * @param skipCount number of entries to skip
	 * @param returnNavigator navigator to use for the read operation
	 * @param returnCount number of entries to read
	 * @param returnMask bitmask of data to read
	 * @param diffTime If non-null, this is a "differential view read" meaning that the caller wants
	 * 				us to optimize things by only returning full information for notes which have
	 * 				changed (or are new) in the view, return just NoteIDs for notes which haven't
	 * 				changed since this time and return a deleted ID table for notes which may be
	 * 				known by the caller and have been deleted since DiffTime. <b>Please note that "differential view reads" do only work in views without permutations (no columns with "show multiple values as separate entries" set) according to IBM. Otherwise, all the view data is always returned.</b>
	 * @param diffIDTable If DiffTime is non-null and DiffIDTable is not null it provides a
	 * 				list of notes which the caller has current information on.  We use this to
	 * 				know which notes we can return shortened information for (i.e., just the NoteID)
	 * 				and what notes we might have to include in the returned DelNoteIDTable.
	 * @param columnNumber If not null, number of single column to return value for (0-based)
	 * @return read data
	 */
	public NotesViewLookupResultData readEntriesExt(JNADominoCollectionPosition startPos,
			Navigate skipNavigator, boolean skipNavigatorContinue,
			int skipCount, Navigate returnNavigator,
			int returnCount, Set<ReadMask> returnMask, DominoDateTime diffTime,
			JNAIDTable diffIDTable,
			Integer columnNumber) {
		
		checkDisposed();

		JNADominoCollectionAllocations allocations = getAllocations();
		
		IntByReference retNumEntriesSkipped = new IntByReference();
		IntByReference retNumEntriesReturned = new IntByReference();
		ShortByReference retSignalFlags = new ShortByReference();
		ShortByReference retBufferLength = new ShortByReference();

		short skipNavBitMask = skipNavigatorContinue ? (short) ((skipNavigator.getValue() | NotesConstants.NAVIGATE_CONTINUE) & 0xffff) : skipNavigator.getValue();
		
		short returnNavBitMask = returnNavigator.getValue();
		int readMaskBitMask = ReadMask.toBitMask(returnMask);
		NotesCollectionPositionStruct startPosStruct = startPos==null ? null : startPos.getAdapter(NotesCollectionPositionStruct.class);
		
		int flags = 0;
		
		NotesTimeDateStruct retDiffTimeStruct = NotesTimeDateStruct.newInstance();
		NotesTimeDateStruct retModifiedTimeStruct = NotesTimeDateStruct.newInstance();
		IntByReference retSequence = new IntByReference();

		String singleColumnLookupName = columnNumber == null ? null : getColumnName(columnNumber);
		
		NotesTimeDateStruct diffTimeStruct = diffTime==null ? null : NotesTimeDateStruct.newInstance(((JNADominoDateTime)diffTime).getInnards());
		
		short result;
		
		DHANDLE.ByReference retBuffer = DHANDLE.newInstanceByReference();
		
		if (diffIDTable!=null && diffIDTable.isDisposed()) {
			throw new ObjectDisposedException(diffIDTable);
		}

		DHANDLE idTableHandle = diffIDTable!=null ? ((JNAIDTableAllocations)diffIDTable.getAdapter(APIObjectAllocations.class)).getIdTableHandle() : null;

		result = LockUtil.lockHandles(
				allocations.getCollectionHandle(),
				idTableHandle,
				(hCollectionByVal, hDiffIdTableByVal) -> {

					return NotesCAPI.get().NIFReadEntriesExt(hCollectionByVal, startPosStruct,
							skipNavBitMask,
							skipCount, returnNavBitMask, returnCount, readMaskBitMask,
							diffTimeStruct, hDiffIdTableByVal,
							columnNumber==null ? NotesConstants.MAXDWORD : columnNumber, flags, retBuffer, retBufferLength,
									retNumEntriesSkipped, retNumEntriesReturned, retSignalFlags,
									retDiffTimeStruct, retModifiedTimeStruct, retSequence);
				}
				);


		if ((result & NotesConstants.ERR_MASK)!=1028) {
			NotesErrorUtils.checkResult(result);
		}
		
		int indexModifiedSequenceNo = retModifiedTimeStruct.Innards[0]; //getIndexModifiedSequenceNo();
		
		DominoDateTime retDiffTimeWrap = new JNADominoDateTime(retDiffTimeStruct);

		int iBufLength = retBufferLength.getValue() & 0xffff;
		if (iBufLength==0 || ((result & NotesConstants.ERR_MASK)==1028)) {
			return new NotesViewLookupResultData(null, new ArrayList<JNACollectionEntry>(0),
					retNumEntriesSkipped.getValue(), retNumEntriesReturned.getValue(),
					retSignalFlags.getValue(), null, indexModifiedSequenceNo, new JNADominoDateTime(retDiffTimeStruct));
		}
		else {
			boolean convertStringsLazily = true;
			boolean convertNotesTimeDateToCalendar = false;
			
			NotesViewLookupResultData viewData = NotesLookupResultBufferDecoder.decodeCollectionLookupResultBuffer(this, retBuffer,
					retNumEntriesSkipped.getValue(), retNumEntriesReturned.getValue(), returnMask, retSignalFlags.getValue(), null,
					indexModifiedSequenceNo, retDiffTimeWrap, convertStringsLazily, convertNotesTimeDateToCalendar, singleColumnLookupName);
			return viewData;
		}
	}	
	
	/**
	 * Callback to dynamically locate the start position of a collection scan, e.g.
	 * the position of a category entry. We use a callback to be able to react on
	 * view index updates. Since a lookup may be repeated when the view index changes,
	 * this callback may be called multiple times to return a fresh starting position
	 * for the lookup.
	 * 
	 * @author Karsten Lehmann
	 */
	private interface IStartPositionRetriever {
		
		/**
		 * Implement this method to find the lookup start position
		 * 
		 * @return start position or null if not found
		 */
		String getStartPosition();
		
	}
	
	/**
	 * The method reads a number of entries from the collection/view. It internally takes care
	 * of view index changes while reading view data and restarts reading if such a change has been
	 * detected.
	 * 
	 * @param startPosStr start position; use "0" or null to start before the first entry; in that case set <code>skipCount</code> to 1 to start reading at the first view row
	 * @param skipCount number entries to skip before reading
	 * @param returnNav navigator to specify how to move in the collection
	 * @param preloadEntryCount amount of entries that is read from the view; if a filter is specified, this should be higher than returnCount
	 * @param returnMask values to extract
	 * @param callback callback that is called for each entry read from the collection, e.g. use {@link EntriesAsListCallback} to read all requested view row data, {@link NoteIdsAsOrderedSetCallback} to collection just the note ids or build your own to build the return objects you need
	 * @return lookup result
	 * 
	 * @param <T> type of lookup result object
	 */
	public <T> T getAllEntries(final String startPosStr, int skipCount, Navigate returnNav,
			int preloadEntryCount,
			Set<ReadMask> returnMask, CollectionEntryProcessor<T> callback) {
		
		return getAllEntries(() -> startPosStr, skipCount, returnNav, preloadEntryCount, returnMask, callback);
	}
	
	/**
	 * The method reads a number of entries from the collection/view. It internally takes care
	 * of view index changes while reading view data and restarts reading if such a change has been
	 * detected.
	 * 
	 * @param startPosRetriever callback to find the start position to read
	 * @param skipCount number entries to skip before reading
	 * @param returnNav navigator to specify how to move in the collection
	 * @param preloadEntryCount amount of entries that is read from the view; if a filter is specified, this should be higher than returnCount
	 * @param returnMask values to extract
	 * @param callback callback that is called for each entry read from the collection
	 * @return lookup result
	 * 
	 * @param <T> type of lookup result object
	 */
	private <T> T getAllEntries(IStartPositionRetriever startPosRetriever, int skipCount, Navigate returnNav,
			int preloadEntryCount,
			Set<ReadMask> returnMask, CollectionEntryProcessor<T> callback) {
		
		Set<ReadMask> useReturnMask = returnMask;

		//decide whether we need to use the undocumented NIFReadEntriesExt
		String readSingleColumnName = callback instanceof JNACollectionEntryProcessor ? ((JNACollectionEntryProcessor<T>)callback).getNameForSingleColumnRead() : null;
		if (readSingleColumnName!=null) {
			//make sure that we actually read any column values
			if (!useReturnMask.contains(ReadMask.SUMMARY) && !useReturnMask.contains(ReadMask.SUMMARYVALUES)) {
				useReturnMask = EnumSet.copyOf(useReturnMask);
				useReturnMask.add(ReadMask.SUMMARYVALUES);
			}
		}
		
		CollectionDataCache dataCache = callback instanceof JNACollectionEntryProcessor ? ((JNACollectionEntryProcessor<T>)callback).getDataCache() : null;
		if (useReturnMask.equals(EnumSet.of(ReadMask.NOTEID))) {
			//disable cache if all we need to read is the note id
			dataCache = null;
		}
		
		Integer readSingleColumnIndex = readSingleColumnName==null ? null : getColumnValuesIndex(readSingleColumnName);
		
		if (readSingleColumnName!=null) {
			//TODO view row caching currently disabled for single column reads, needs more work
			dataCache = null;
		}

		if (dataCache!=null) {
			//if caching is used, make sure that we read the note id, because that's how we hash our data
			if (!useReturnMask.contains(ReadMask.NOTEID)) {
				useReturnMask = EnumSet.copyOf(useReturnMask);
				useReturnMask.add(ReadMask.NOTEID);
			}
		}

		long t0 = System.currentTimeMillis();
		int runs = -1;
		
		while (true) {
			runs++;
			int initialIndexModified = getIndexModifiedSequenceNo();
			
			String startPosStr = startPosRetriever.getStartPosition();
			if (StringUtil.isEmpty(startPosStr)) {
				T result = callback.start();
				result = callback.end(result);
				return result;
			}
			
			int indexModifiedAfterGettingStartPos = getIndexModifiedSequenceNo();

			if (initialIndexModified != indexModifiedAfterGettingStartPos) {
				//view index was changed while reading; restart scan
				if (callback instanceof JNACollectionEntryProcessor) {
					Action retryAction = ((JNACollectionEntryProcessor<T>)callback).retryingReadBecauseViewIndexChanged(runs, System.currentTimeMillis() - t0);
					if (retryAction==Action.Stop) {
						return null;
					}
				}
				refresh();
				continue;
			}
			
			NotesCollectionPositionStruct pos = NotesCollectionPositionStruct.toPosition(("last".equalsIgnoreCase(startPosStr) || startPosStr==null) ? "0" : startPosStr); //$NON-NLS-1$ //$NON-NLS-2$
			JNADominoCollectionPosition posWrap = new JNADominoCollectionPosition(pos);

			T result = callback.start();
			
			if (preloadEntryCount==0) {
				//nothing to do
				result = callback.end(result);
				return result;
			}
			
			boolean viewModified = false;
			boolean firstLoopRun = true;
			
			DominoDateTime retDiffTime = null;
			
			DominoDateTime diffTime = null;
			JNAIDTable diffIDTable = null;
			
			if (dataCache!=null) {
				CacheState cacheState = dataCache.getCacheState();
				
				//only use cache content if read masks are compatible
				Map<Integer,CacheableViewEntryData> cacheEntries = cacheState.getCacheEntries();
				if (cacheEntries!=null && !cacheEntries.isEmpty()) {
					Set<ReadMask> cacheReadMask = cacheState.getReadMask();
					if (useReturnMask.equals(cacheReadMask)) {
						diffTime = cacheState.getDiffTime();

						diffIDTable = new JNAIDTable(getParentDominoClient());
						diffIDTable.addAll(cacheEntries.keySet());
					}
				}
			}
			
			List<JNACollectionEntry> entriesToUpdateCache = dataCache==null ? null : new ArrayList<>();
			
			boolean innerLoopLeftByViewMod = false;

			while (true) {
				int useSkipCount;
				if (firstLoopRun) {
					if ("last".equalsIgnoreCase(startPosStr)) { //$NON-NLS-1$
						//TODO make "last" work when called from getAllEntriesInCategory
						
						//first jump to the end of the view
						useSkipCount = Integer.MAX_VALUE;
					}
					else {
						useSkipCount = skipCount;
					}
				}
				else {
					//just skip the last entry that we returned on the last NIFReadEntries call
					useSkipCount = 1;
				}
				Navigate skipNav = returnNav;
				boolean skipNavContinue = false;
				
				if (firstLoopRun) {
					if ("last".equalsIgnoreCase(startPosStr)) { //$NON-NLS-1$
						if (returnNav == Navigate.CURRENT) {
							//navigate to the last entry of the view and return it
							skipNav = Navigate.NEXT_ENTRY;
						}
						else {
							//compute the skipNav by reversing the returnNav; e.g. for startPos="last"
							//and returnNav=Navigate.PREV_SELECTED, we first jump to the end of the view
							//with skipCount=INTEGER.MAX_VALUE Navigate.NEXT_SELECTED.
							//Then we start reading n entries with Navigate.PREV_SELECTED,
							//effectively returning the last n selected entries of the view
							skipNav = reverseNav(returnNav);
						}
						
						//set NAVIGATE_CONTINUE to stop skipping on the last view element and not return an error
						skipNavContinue = true;
					}
					else {
						skipNav = returnNav;
					}
				}
				else {
					skipNav = returnNav;
				}
				final boolean fSkipNavContinue = skipNavContinue;
				
				NotesViewLookupResultData data = readEntriesExt(posWrap, skipNav, fSkipNavContinue, useSkipCount, returnNav, preloadEntryCount, useReturnMask,
						diffTime, diffIDTable, readSingleColumnIndex);
				
				int indexModifiedAfterDataLookup = getIndexModifiedSequenceNo();

				if (initialIndexModified != indexModifiedAfterDataLookup) {
					//view index was changed while reading; restart scan
					if (callback instanceof JNACollectionEntryProcessor) {
						Action retryAction = ((JNACollectionEntryProcessor<T>)callback).retryingReadBecauseViewIndexChanged(runs, System.currentTimeMillis() - t0);
						if (retryAction==Action.Stop) {
							return null;
						}
					}
					refresh();
					innerLoopLeftByViewMod = true;
					break;
				}

				if (useReturnMask.contains(ReadMask.INIT_POS_NOTEID)) {
					//make sure to only use this flag on the first lookup call
					useReturnMask = EnumSet.copyOf(useReturnMask);
					useReturnMask.remove(ReadMask.INIT_POS_NOTEID);
				}
				
				retDiffTime = data.getReturnedDiffTime();
				
				if (dataCache!=null) {
					//if data cache is used, we fill in missing gaps in cases where NIF skipped producing
					//the summary data, because the corresponding cache entry was already
					//up to date
					List<JNACollectionEntry> entries = data.getEntries();
					dataCache.populateEntryStubsWithData(entries);
					
					entriesToUpdateCache.addAll(entries);
				}

				if (data.getReturnCount()==0) {
					//no more data found
					result = callback.end(result);
					
					if (callback instanceof JNACollectionEntryProcessor && dataCache!=null && retDiffTime!=null) {
						if (!entriesToUpdateCache.isEmpty()) {
							dataCache.addCacheValues(useReturnMask, retDiffTime, entriesToUpdateCache);
						}
						((JNACollectionEntryProcessor<T>)callback).setNewDiffTime(retDiffTime);
					}

					return result;
				}
				
				firstLoopRun = false;
				
				if (isAutoUpdate()) {
					if (data.hasAnyNonDataConflicts()) {
						//refresh the view and restart the lookup
						viewModified=true;
						break;
					}
				}
				
				List<JNACollectionEntry> entries = data.getEntries();
				for (JNACollectionEntry currEntry : entries) {
					Action action = callback.entryRead(result, currEntry);
					if (action==Action.Stop) {
						result = callback.end(result);
						
						if (callback instanceof JNACollectionEntryProcessor && dataCache!=null && retDiffTime!=null) {
							if (!entriesToUpdateCache.isEmpty()) {
								dataCache.addCacheValues(useReturnMask, retDiffTime, entriesToUpdateCache);
							}
							((JNACollectionEntryProcessor<T>)callback).setNewDiffTime(retDiffTime);
						}
						return result;
					}
				}
			}

			if (innerLoopLeftByViewMod) {
				continue;
			}

			if (callback instanceof JNACollectionEntryProcessor && dataCache!=null && retDiffTime!=null) {
				if (!entriesToUpdateCache.isEmpty()) {
					dataCache.addCacheValues(useReturnMask, retDiffTime, entriesToUpdateCache);
				}
				((JNACollectionEntryProcessor<T>)callback).setNewDiffTime(retDiffTime);
			}

			if (diffIDTable!=null) {
				diffIDTable.dispose();
			}
			
			if (viewModified) {
				//view index was changed while reading; restart scan
				if (callback instanceof JNACollectionEntryProcessor) {
				Action retryAction = ((JNACollectionEntryProcessor<T>)callback).retryingReadBecauseViewIndexChanged(runs, System.currentTimeMillis() - t0);
				if (retryAction==Action.Stop) {
					return null;
				}
				}
				refresh();
				continue;
			}
			
			return result;
		}
	}
	
	/**
	 * Method to check whether an optimized view lookup method can be used for
	 * a set of find/return flags and the current Domino version
	 * 
	 * @param findFlags find flags
	 * @param returnMask return flags
	 * @param keys lookup keys
	 * @return true if method can be used
	 */
	private boolean canUseOptimizedLookupForKeyLookup(Set<FindFlag> findFlags, Set<ReadMask> returnMask, Object... keys) {
		if (findFlags.contains(FindFlag.GREATER_THAN) || findFlags.contains(FindFlag.LESS_THAN)) {
			//TODO check this with IBM dev; we had crashes like "[0A0F:0002-21A00] PANIC: LookupHandle: null handle" using NIFFindByKeyExtended2
			return false;
		}
		{
			//we had "ERR 774: Unsupported return flag(s)" errors when using the optimized lookup
			//method with other return values other than note id
			boolean unsupportedValuesFound = false;
			for (ReadMask currReadMaskValues: returnMask) {
				//commented out ReadMask.SUMMARY because we found truncated ITEM_VALUE_TABLE's returned by NIFFindByKeyExtended2/3
				if ((currReadMaskValues != ReadMask.NOTEID) /* && (currReadMaskValues != ReadMask.SUMMARY) */) {
					unsupportedValuesFound = true;
					break;
				}
			}

			if (unsupportedValuesFound) {
				return false;
			}
		}
		
		{
			//check for R9 and flag compatibility
		  BuildVersionInfo buildVersion = ((JNADatabase) getParent()).getBuildVersionInfo();
		  
			if (buildVersion.getBuildNumber() < 400) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks whether we can use an optimized lookup that locks the view index against concurrent
	 * modifications while we read it. This mode needs more testing and is disabled by default.
	 * It can be enabled by calling {@link JNADominoClient#setCustomValue(String, Object)} with
	 * key "collection_optimizedlookup" and value <code>Boolean.TRUE</code>. The optimization also only
	 * works for pure note id lookups and local databases.
	 * 
	 * @param findFlags find flags, see {@link FindFlag}
	 * @param returnMask values to be returned
	 * @param keys lookup keys
	 * @return true if supported
	 */
	private boolean canUseOptimizedLocalKeyLookup(Set<FindFlag> findFlags, Set<ReadMask> returnMask, Object... keys) {
		JNADominoClient client = (JNADominoClient)getParentDominoClient();
		if (Boolean.TRUE.equals(client.getCustomValue("collection_optimizedlookup"))) { // disabled by default //$NON-NLS-1$
			JNADatabase db = (JNADatabase) getParent();
			if (!db.isRemote()) { // only working on local dbs
				return canUseOptimizedLookupForKeyLookup(findFlags, returnMask, keys); // only working properly for pure note id lookups
			}
		}
		return false;
	}
	
	/**
	 * Returns whether the view automatically handles view index updates while reading from the view.<br>
	 * <br>
	 * This flag is used by the methods<br>
	 * <br>
	 * <ul>
	 * <li>{@link #getAllEntries}</li>
	 * <li>{@link #getAllEntriesByKey(Set, Set, JNACollectionEntryProcessor, Object...)}</li>
	 * <li>{@link #getAllIdsAsIDTable(boolean)}</li>
	 * <li>{@link #getAllIdsInCategory(String, Navigate)}</li>
	 * </ul>
	 * @return true if auto update
	 */
	public boolean isAutoUpdate() {
		return m_autoUpdate;
	}
	
	/**
	 * Changes the auto update flag, which indicates whether the view automatically handles view index
	 * updates while reading from the view.<br>
	 * <br>
	 * This flag is used by the methods<br>
	 * <br>
	 * <ul>
	 * <li>{@link #getAllEntries}</li>
	 * <li>{@link #getAllEntriesByKey(Set, Set, JNACollectionEntryProcessor, Object...)}</li>
	 * <li>{@link #getAllIdsAsIDTable(boolean)}</li>
	 * <li>{@link #getAllIdsInCategory(String, Navigate)}</li>
	 * </ul>
	 * @param update true to activate auto update
	 */
	public void setAutoUpdate(boolean update) {
		m_autoUpdate = update;
	}
	
	/**
	 * Returns the sort direction that has last been used to resort the view
	 * 
	 * @return an {@link Optional} describing the current sort direction or an empty
	 *      one if it has not been resorted
	 */
	public Optional<Direction> getCurrentSortDirection() {
		short collation = getCollation();
		if (collation==0) {
			return Optional.empty();
		}
		CollationInfo colInfo = getCollationsInfo();
		return Optional.ofNullable(colInfo.getSortDirection(collation));
	}
	
	/**
	 * Returns the currently active collation
	 * 
	 * @return collation
	 */
	private short getCollation() {
		checkDisposed();
		
		ShortByReference retCollationNum = new ShortByReference();
		
		short result = LockUtil.lockHandle(getAllocations().getCollectionHandle(), (hCollectionByVal) -> {
			return NotesCAPI.get().NIFGetCollation(hCollectionByVal, retCollationNum);
		});

		NotesErrorUtils.checkResult(result);
		return retCollationNum.getValue();
	}
	
	@Override
	public LinkedHashSet<Integer> getAllIds(boolean withDocuments, boolean withCategories) {
		if (!withDocuments && !withCategories) {
			return new LinkedHashSet<>();
		}
		
		Navigate nav;
		if (withDocuments) {
			if (withCategories) {
				nav = Navigate.NEXT_ENTRY;
			}
			else {
				nav = Navigate.NEXT_DOCUMENT;
			}
			
		}
		else {
			nav = Navigate.NEXT_CATEGORY;
		}
		
		return getAllEntries("0", 1, nav, //$NON-NLS-1$
				Integer.MAX_VALUE, EnumSet.of(ReadMask.NOTEID), 
				new NoteIdsAsOrderedSetCallback(Integer.MAX_VALUE));
	}
	
	
	@Override
	public LinkedHashSet<Integer> getAllIdsByKey(Set<Find> findFlags, Object key) {
		Objects.requireNonNull(key, "Key cannot be null");
		return getAllIdsByKey(findFlags, Arrays.asList(key));
	}

	@Override
	public LinkedHashSet<Integer> getAllIdsByKey(Set<Find> findFlags, Collection<Object> key) {
		Objects.requireNonNull(key, "Key cannot be null");
		if (key.isEmpty()) {
			throw new IllegalArgumentException("Key cannot be empty");
		}
		
		Object[] keysArr = key.toArray(new Object[key.size()]);
		Set<FindFlag> jnaFindFlags = toJNAFind(findFlags);
		
		return getAllEntriesByKey(jnaFindFlags, EnumSet.of(ReadMask.NOTEID),
				new NoteIdsAsOrderedSetCallback(Integer.MAX_VALUE), keysArr);
	}
	
	/**
	 * Maps publicly available find flags to the full list
	 * 
	 * @param findFlags find flags
	 * @return internal find flags
	 */
	private Set<FindFlag> toJNAFind(Set<Find> findFlags) {
		Set<FindFlag> jnaFindFlags = new HashSet<>();
		for (Find currFind : findFlags) {
			jnaFindFlags.add(toJNAFind(currFind));
		}
		return jnaFindFlags;
	}
	
	/**
	 * Maps a publicly available find flag to an internal find flag
	 * 
	 * @param findFlag find flag
	 * @return internal fing flag
	 */
	private FindFlag toJNAFind(Find findFlag) {
		int val = findFlag.getValue();
		for (FindFlag currJNAFind : FindFlag.values()) {
			if (val == currJNAFind.getValue()) {
				return currJNAFind;
			}
		}
		throw new IllegalArgumentException(MessageFormat.format("Unknown find flag: {0}", findFlag));
	}
	
	/**
	 * Returns all view entries matching the specified search key(s) in the collection.
	 * It internally takes care of view index changes while reading view data and restarts
	 * reading if such a change has been detected.
	 * 
	 * @param findFlags find flags, see {@link FindFlag}
	 * @param returnMask values to be returned
	 * @param callback callback that is called for each entry read from the collection, e.g. use {@link EntriesAsListCallback} to read all requested view row data, {@link NoteIdsAsOrderedSetCallback} to collection just the note ids or build your own to build the return objects you need
	 * @param keys lookup keys
	 * @return lookup result
	 * 
	 * @param <T> type of lookup result object
	 */
	public <T> T getAllEntriesByKey(Set<FindFlag> findFlags, Set<ReadMask> returnMask, JNACollectionEntryProcessor<T> callback, Object... keys) {
		checkDisposed();
		
		//for local databases, we can use an optimized lookup that locks the view during the lookup against index updates so that
		//we don't have to rerun the lookup loop
		if (canUseOptimizedLocalKeyLookup(findFlags, returnMask, callback, keys)) {
			T result = getAllEntriesByKeyLocally(findFlags, returnMask, callback, keys);
			return result;
		}
		
		Set<ReadMask> useReturnMask = returnMask;

		//decide whether we need to use the undocumented NIFReadEntriesExt
		String readSingleColumnName = callback.getNameForSingleColumnRead();
		if (readSingleColumnName!=null) {
			//make sure that we actually read any column values
			if (!useReturnMask.contains(ReadMask.SUMMARY) && !useReturnMask.contains(ReadMask.SUMMARYVALUES)) {
				useReturnMask = EnumSet.copyOf(useReturnMask);
				useReturnMask.add(ReadMask.SUMMARYVALUES);
			}
		}
		
		Integer readSingleColumnIndex = readSingleColumnName==null ? null : getColumnValuesIndex(readSingleColumnName);

		//we are leaving the loop when there is no more data to be read;
		//while(true) is here to rerun the query in case of view index changes while reading

		long t0=System.currentTimeMillis();
		
		int runs = -1;
		
		while (true) {
			runs++;
			
			T result = callback.start();

			NotesViewLookupResultData data;
			//position of first match
			String firstMatchPosStr;
			int remainingEntries;
			
			int entriesToSkipOnFirstLoopRun = 0;

			if (canUseOptimizedLookupForKeyLookup(findFlags, returnMask, keys)) {
				//do the first lookup and read operation atomically; uses a large buffer for local calls
				EnumSet<FindFlag> findFlagsWithExtraBits = EnumSet.copyOf(findFlags);
				findFlagsWithExtraBits.add(FindFlag.AND_READ_MATCHES);
				findFlagsWithExtraBits.add(FindFlag.RETURN_DWORD);
				
				data = findByKeyExtended2(findFlagsWithExtraBits, returnMask, keys);
				
				int numEntriesFound = data.getReturnCount();
				if (numEntriesFound!=-1) {
					if (isAutoUpdate()) {
						//check for view index or design change
						if (data.hasAnyNonDataConflicts()) {
							//refresh the view and restart the lookup
							Action retryAction = callback.retryingReadBecauseViewIndexChanged(runs, System.currentTimeMillis() - t0);
							if (retryAction==Action.Stop) {
								return null;
							}
							refresh();
							continue;
						}
					}
					
					//copy the data we have read
					List<JNACollectionEntry> entries = data.getEntries();
					for (JNACollectionEntry currEntryData : entries) {
						Action action = callback.entryRead(result, currEntryData);
						if (action==Action.Stop) {
							result = callback.end(result);
							return result;
						}
					}
					entriesToSkipOnFirstLoopRun = entries.size();
					
					if (!data.hasMoreToDo()) {
						//we are done
						result = callback.end(result);
						return result;
					}

					//compute what we have left
					int entriesReadOnFirstLookup = entries.size();
					remainingEntries = numEntriesFound - entriesReadOnFirstLookup;
					firstMatchPosStr = data.getPosition();
				}
				else {
					//workaround for a bug where the method NIFFindByKeyExtended2 returns -1 as numEntriesFound
					//and no buffer data
					//
					//fallback to classic lookup until this is fixed/commented by IBM dev:
					FindResult findResult = findByKey(findFlags, keys);
					remainingEntries = findResult.getEntriesFound();
					if (remainingEntries==0) {
						return result;
					}
					firstMatchPosStr = findResult.getPosition();
				}
			}
			else {
				//first find the start position to read data
				FindResult findResult = findByKey(findFlags, keys);
				remainingEntries = findResult.getEntriesFound();
				if (remainingEntries==0) {
					return result;
				}
				firstMatchPosStr = findResult.getPosition();
			}

			if (!canFindExactNumberOfMatches(findFlags)) {
				Direction currSortDirection = getCurrentSortDirection().orElse(null);
				if (currSortDirection!=null) {
					//handle special case for inquality search where column sort order matches the find flag,
					//so we can read all view entries after findResult.getPosition()
					
					if (currSortDirection==Direction.Ascending && findFlags.contains(FindFlag.GREATER_THAN)) {
						//read all entries after findResult.getPosition()
						remainingEntries = Integer.MAX_VALUE;
					}
					else if (currSortDirection==Direction.Descending && findFlags.contains(FindFlag.LESS_THAN)) {
						//read all entries after findResult.getPosition()
						remainingEntries = Integer.MAX_VALUE;
					}
				}
			}

			if (firstMatchPosStr!=null) {
				//position of the first match; we skip (entries.size()) to read the remaining entries
				boolean isFirstLookup = true;
				
				NotesCollectionPositionStruct lookupPos = NotesCollectionPositionStruct.toPosition(firstMatchPosStr);
				JNADominoCollectionPosition lookupPosWrap = new JNADominoCollectionPosition(lookupPos);
				
				boolean viewModified = false;
				
				while (remainingEntries>0) {
					//on first lookup, start at "posStr" and skip the amount of already read entries
					data = readEntriesExt(lookupPosWrap, Navigate.NEXT_DOCUMENT, false, isFirstLookup ? entriesToSkipOnFirstLoopRun : 1,
							Navigate.NEXT_DOCUMENT, remainingEntries, useReturnMask, null, null, readSingleColumnIndex);
					
					if (isFirstLookup || isAutoUpdate()) {
						//for the first lookup, make sure we start at the right position
						if (data.hasAnyNonDataConflicts()) {
							//set viewModified to true and leave the inner loop; we will refresh the view and restart the lookup
							viewModified=true;
							break;
						}
					}
					isFirstLookup=false;
					
					List<JNACollectionEntry> entries = data.getEntries();
					if (entries.isEmpty()) {
						//looks like we don't have any more data in the view
						break;
					}
					
					for (JNACollectionEntry currEntryData : entries) {
						Action action = callback.entryRead(result, currEntryData);
						if (action==Action.Stop) {
							result = callback.end(result);
							return result;
						}
					}
					remainingEntries = remainingEntries - entries.size();
				}
				
				if (viewModified) {
					//refresh view and redo the whole lookup
					Action retryAction = callback.retryingReadBecauseViewIndexChanged(runs, System.currentTimeMillis() - t0);
					if (retryAction==Action.Stop) {
						return null;
					}
					refresh();
					continue;
				}
			}
			
			result = callback.end(result);
			return result;
		}
	}
	
	/**
	 * Returns all view entries matching the specified search key(s) in the collection.
	 * It internally takes care of view index changes while reading view data and restarts
	 * reading if such a change has been detected.
	 * 
	 * @param findFlags find flags, see {@link FindFlag}
	 * @param returnMask values to be returned
	 * @param callback callback that is called for each entry read from the collection, e.g. use {@link EntriesAsListCallback} to read all requested view row data, {@link NoteIdsAsOrderedSetCallback} to collection just the note ids or build your own to build the return objects you need
	 * @param keys lookup keys
	 * @return lookup result
	 * 
	 * @param <T> type of lookup result object
	 */
	private <T> T getAllEntriesByKeyLocally(Set<FindFlag> findFlags, Set<ReadMask> returnMask, final JNACollectionEntryProcessor<T> callback, Object... keys) {
		final NIFFindByKeyContextStruct ctx = NIFFindByKeyContextStruct.newInstance();

		final boolean convertStringsLazily = true;
		final boolean convertNotesTimeDateToCalendar = false;

		final Set<ReadMask> returnMaskToUse = EnumSet.copyOf(returnMask);
		//make sure every read entry looks the same (collectionstats might otherwise add data at
		//the beginning of the buffer)
		final int returnMaskToUseAsInt = ReadMask.toBitMask(returnMaskToUse);
		
		final T viewCallbackObj = callback.start();

		final Throwable invocationEx[] = new Throwable[1];

		final NotesCallbacks.NIFFindByKeyProc nifCallback;
		
		if (PlatformUtils.isWin32()) {
			nifCallback = (Win32NotesCallbacks.NIFFindByKeyProcWin32) ctx1 -> {
				try {
					short wSizeOFChunk = ctx1.wSizeOfChunk;
					Pointer summaryBuffer = ctx1.SummaryBuffer;

					ctx1.TotalDataInBuffer += wSizeOFChunk & 0xffff;

					if (summaryBuffer!=null && Pointer.nativeValue(summaryBuffer)!=0) {
						NotesViewLookupResultData viewData = NotesLookupResultBufferDecoder.decodeCollectionLookupResultBuffer(JNADominoCollection.this,
								summaryBuffer,
								0, ctx1.EntriesThisChunk & 0xffff, returnMaskToUse, (short) 0, null,
								0, null, convertStringsLazily, convertNotesTimeDateToCalendar, null);

						for (JNACollectionEntry currEntry : viewData.getEntries()) {
							Action action = callback.entryRead(viewCallbackObj, currEntry);
							if (action==Action.Stop) {
								return INotesErrorConstants.ERR_CANCEL;
							}
						}
					}

					return 0;
				}
				catch (Throwable t) {
					invocationEx[0] = t;
					return INotesErrorConstants.ERR_CANCEL;
				}
			};
		}
		else if (PlatformUtils.is32Bit()) {
			nifCallback = ctx1 -> {
				try {
					short wSizeOFChunk = ctx1.wSizeOfChunk;
					Pointer summaryBuffer = ctx1.SummaryBuffer;

					ctx1.TotalDataInBuffer += wSizeOFChunk & 0xffff;

					if (summaryBuffer!=null && Pointer.nativeValue(summaryBuffer)!=0) {
						NotesViewLookupResultData viewData = NotesLookupResultBufferDecoder.decodeCollectionLookupResultBuffer(JNADominoCollection.this,
								summaryBuffer,
								0, ctx1.EntriesThisChunk & 0xffff, returnMaskToUse, (short) 0, null,
								0, null, convertStringsLazily, convertNotesTimeDateToCalendar, null);

						for (JNACollectionEntry currEntry : viewData.getEntries()) {
							Action action = callback.entryRead(viewCallbackObj, currEntry);
							if (action==Action.Stop) {
								return INotesErrorConstants.ERR_CANCEL;
							}
						}
					}

					return 0;
				}
				catch (Throwable t) {
					invocationEx[0] = t;
					return INotesErrorConstants.ERR_CANCEL;
				}
			};
		}
		else {
			nifCallback = ctx1 -> {
				try {
					short wSizeOFChunk = ctx1.wSizeOfChunk;
					Pointer summaryBuffer = ctx1.SummaryBuffer;
					
					ctx1.TotalDataInBuffer += wSizeOFChunk & 0xffff;

					if (summaryBuffer!=null && Pointer.nativeValue(summaryBuffer)!=0) {
						NotesViewLookupResultData viewData = NotesLookupResultBufferDecoder.decodeCollectionLookupResultBuffer(JNADominoCollection.this,
								summaryBuffer,
								0, ctx1.EntriesThisChunk & 0xffff, returnMaskToUse, (short) 0, null,
								0, null, convertStringsLazily, convertNotesTimeDateToCalendar, null);

						for (JNACollectionEntry currEntry : viewData.getEntries()) {
							Action action = callback.entryRead(viewCallbackObj, currEntry);
							if (action==Action.Stop) {
								return INotesErrorConstants.ERR_CANCEL;
							}
						}
					}

					return 0;
				}
				catch (Throwable t) {
					invocationEx[0] = t;
					return INotesErrorConstants.ERR_CANCEL;
				}
			};
		}

		final Memory keyBuffer;
		try {
			keyBuffer = NotesSearchKeyEncoder.encodeKeys(keys);
		} catch (Throwable e) {
			throw new DominoException(0, MessageFormat.format("Could not encode search keys: {0}", Arrays.toString(keys)), e);
		}
		
		final NotesCollectionPositionStruct retIndexPos = NotesCollectionPositionStruct.newInstance(); //null; // NotesCollectionPositionStruct.newInstance();
		final IntByReference retNumMatches = new IntByReference();
		final ShortByReference retSignalFlags = new ShortByReference();
		final IntByReference retSequence = new IntByReference();
		
		final int findFlagsAsInt = FindFlag.toBitMaskInt(findFlags) | 0x2000; // => AND_READ_MATCHES

		final DHANDLE.ByReference rethBuffer = DHANDLE.newInstanceByReference();
		
		short result = LockUtil.lockHandle(getAllocations().getCollectionHandle(), (hCollectionByVal) -> {
			return NotesCAPI.get().NIFFindByKeyExtended3(hCollectionByVal,
					keyBuffer, findFlagsAsInt,
					returnMaskToUseAsInt, retIndexPos, retNumMatches, retSignalFlags, 
					rethBuffer,
					retSequence, nifCallback, ctx);
		});

		if (invocationEx[0]!=null) {
			//special case for JUnit testcases
			if (invocationEx[0] instanceof AssertionError) {
				throw (AssertionError) invocationEx[0];
			}
			throw new DominoException(0, "Error in view lookup", invocationEx[0]);
		}
		
		if (result!=INotesErrorConstants.ERR_CANCEL) {
			if ((result & NotesConstants.ERR_MASK)!=1028) { // no data found
				NotesErrorUtils.checkResult(result);
			}
		}
		else {
			return null;
		}

		T viewCallbackObjToReturn = callback.end(viewCallbackObj);
		
		return viewCallbackObjToReturn;
	}

	/**
	 * The method reads a number of entries located under a specified category from the collection/view.
	 * We internally takes care of view index changes while reading view data and restarts reading
	 * if such a change has been detected.
	 * 
	 * @param <T> result data type
	 * 
	 * @param categoryLevels array with category structure lookup key, e.g. ["level1\level2"] if a category column value is a string or [2019,5] if there are multiple category columns containining numbers like year and cw
	 * @param skipCount number of entries to skip
	 * @param returnNav navigator to specify how to move in the collection
	 * @param diffTime If non-null, this is a "differential view read" meaning that the caller wants
	 * 				us to optimize things by only returning full information for notes which have
	 * 				changed (or are new) in the view, return just NoteIDs for notes which haven't
	 * 				changed since this time and return a deleted ID table for notes which may be
	 * 				known by the caller and have been deleted since DiffTime.
	 * 				<b>Please note that "differential view reads" do only work in views without permutations (no columns with "show multiple values as separate entries" set) according to IBM. Otherwise, all the view data is always returned.</b>
	 * @param diffIDTable If DiffTime is non-null and DiffIDTable is not null it provides a
	 * 				list of notes which the caller has current information on.  We use this to
	 * 				know which notes we can return shortened information for (i.e., just the NoteID)
	 * 				and what notes we might have to include in the returned DelNoteIDTable.
	 * @param preloadEntryCount amount of entries that is read from the view; if a filter is specified, this should be higher than returnCount
	 * @param returnMask values to extract
	 * @param callback callback that is called for each entry read from the collection, e.g. use {@link EntriesAsListCallback} to read all requested view row data, {@link NoteIdsAsOrderedSetCallback} to collection just the note ids or build your own to build the return objects you need
	 * @return lookup result
	 */
	public <T> T getAllEntriesInCategory(final Object[] categoryLevels, int skipCount, Navigate returnNav,
			DominoDateTime diffTime, JNAIDTable diffIDTable, int preloadEntryCount, Set<ReadMask> returnMask,
			final JNACollectionEntryProcessor<T> callback) {
		
		checkDisposed();

		Navigate useReturnNav = returnNav;
//		if (useReturnNav == Navigate.ALL_DESCENDANTS) {
//			//replace with NEXT to get proper results when the data to be read does not
//			//fit into the buffer and we need a second NIFReadEntries call; we make
//			//sure not to leave the category in our own code
//			useReturnNav = Navigate.NEXT_ENTRY;
//		}

		while (true) {
			int initialIndexMod = getIndexModifiedSequenceNo();
			
			//find category entry
			NotesViewLookupResultData catLkResult = findByKeyExtended2(EnumSet.of(FindFlag.MATCH_CATEGORYORLEAF,
					FindFlag.REFRESH_FIRST, FindFlag.RETURN_DWORD, FindFlag.AND_READ_MATCHES, FindFlag.CASE_INSENSITIVE),
					EnumSet.of(ReadMask.NOTEID, ReadMask.SUMMARY), categoryLevels);
			
			if (catLkResult.getReturnCount()==0) {
				//category not found
				T result = callback.start();
				result = callback.end(result);
				return result;
			}
			
			final String catPos = catLkResult.getPosition();
			if (StringUtil.isEmpty(catPos)) {
				//category not found
				T result = callback.start();
				result = callback.end(result);
				return result;
			}
			
			boolean shouldGotoChild = false;
			if (useReturnNav == Navigate.NEXT_ON_SAME_LEVEL) {
				shouldGotoChild = true;
			}
			
			String startReadingPos;
			if (shouldGotoChild) {
				//goto first child
				List<CollectionEntry> childAsList = getAllEntries(catPos, 1, Navigate.CHILD_ENTRY, 1,
						EnumSet.of(ReadMask.NOTEID, ReadMask.SUMMARY, ReadMask.INDEXPOSITION), new EntriesAsListCallback(1));
				
				int modCountAfterFindChild = getIndexModifiedSequenceNo();
				
				if (initialIndexMod != modCountAfterFindChild) {
					//retry, index changed
					continue;
				}
				
				if (childAsList.isEmpty()) {
					//empty category entry, all hidden
					T result = callback.start();
					result = callback.end(result);
					return result;
				}
				
				CollectionEntry childEntry = childAsList.get(0);
				startReadingPos = childEntry.getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""); //$NON-NLS-1$
			}
			else {
				startReadingPos = catPos;
			}
			
			EnumSet<ReadMask> useReturnMask = EnumSet.copyOf(returnMask);
			//make sure that we get the entry position for the range check
			useReturnMask.add(ReadMask.INDEXPOSITION);

			
			T data = getAllEntries(startReadingPos, skipCount, useReturnNav,
					preloadEntryCount, useReturnMask, new JNACollectionEntryProcessorWrapper<T>(callback) {
				
				@Override
				public Action entryRead(T result,
						CollectionEntry entryData) {
					
					//check if this entry is still one of the descendants of the category entry
					String entryPos = entryData.getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""); //$NON-NLS-1$
					if (entryPos.equals(catPos) && returnNav != Navigate.CURRENT) {
						//skip category entry
						return Action.Continue;
					}
					if (entryPos.startsWith(catPos)) {
						return super.entryRead(result, entryData);
					}
					else {
						return Action.Stop;
					}
				}
			});
			
			int modCountAfterDataRead = getIndexModifiedSequenceNo();
			if (initialIndexMod != modCountAfterDataRead) {
				//retry, index changed
				continue;
			}
			
			return data;
		}
	}
	
	/**
	 * The method reads a number of entries located under a specified category from the collection/view.
	 * It internally takes care of view index changes while reading view data and restarts reading
	 * if such a change has been detected.
	 * 
	 * @param <T> result data type
	 * 
	 * @param category category or catlevel1\catlevel2 structure
	 * @param skipCount number of entries to skip
	 * @param returnNav navigator to specify how to move in the collection
	 * @param preloadEntryCount amount of entries that is read from the view; if a filter is specified, this should be higher than returnCount
	 * @param returnMask values to extract
	 * @param callback callback that is called for each entry read from the collection, e.g. use {@link EntriesAsListCallback} to read all requested view row data, {@link NoteIdsAsOrderedSetCallback} to collection just the note ids or build your own to build the return objects you need
	 * @return lookup result
	 */
	
	public <T> T getAllEntriesInCategory(String category, int skipCount, Navigate returnNav,
			int preloadEntryCount, EnumSet<ReadMask> returnMask,
			final JNACollectionEntryProcessor<T> callback) {
		
		return getAllEntriesInCategory(category, skipCount, returnNav, null, null, preloadEntryCount, returnMask, callback);
	}

	/**
	 * The method reads a number of entries located under a specified category from the collection/view.
	 * It internally takes care of view index changes while reading view data and restarts reading
	 * if such a change has been detected.
	 * 
	 * @param <T> result data type
	 * 
	 * @param category category or catlevel1\catlevel2 structure
	 * @param skipCount number of entries to skip
	 * @param returnNav navigator to specify how to move in the collection
	 * @param diffTime If non-null, this is a "differential view read" meaning that the caller wants
	 * 				us to optimize things by only returning full information for notes which have
	 * 				changed (or are new) in the view, return just NoteIDs for notes which haven't
	 * 				changed since this time and return a deleted ID table for notes which may be
	 * 				known by the caller and have been deleted since DiffTime.
	 * 				<b>Please note that "differential view reads" do only work in views without permutations (no columns with "show multiple values as separate entries" set) according to IBM. Otherwise, all the view data is always returned.</b>
	 * @param diffIDTable If DiffTime is non-null and DiffIDTable is not null it provides a
	 * 				list of notes which the caller has current information on.  We use this to
	 * 				know which notes we can return shortened information for (i.e., just the NoteID)
	 * 				and what notes we might have to include in the returned DelNoteIDTable.
	 * @param preloadEntryCount amount of entries that is read from the view; if a filter is specified, this should be higher than returnCount
	 * @param returnMask values to extract
	 * @param callback callback that is called for each entry read from the collection, e.g. use {@link EntriesAsListCallback} to read all requested view row data, {@link NoteIdsAsOrderedSetCallback} to collection just the note ids or build your own to build the return objects you need
	 * @return lookup result
	 */
	public <T> T getAllEntriesInCategory(final String category, int skipCount, Navigate returnNav,
			DominoDateTime diffTime, JNAIDTable diffIDTable, int preloadEntryCount, EnumSet<ReadMask> returnMask,
			final JNACollectionEntryProcessor<T> callback) {
		
		return getAllEntriesInCategory(new Object[] {category}, skipCount, returnNav, diffTime,
				diffIDTable, preloadEntryCount, returnMask, callback);
	}
	
	/**
	 * Convenience method that reads all note ids located under a category
	 * 
	 * @param category category
	 * @param returnNav navigator to be used to scan for collection entries
	 * @return ids in view order
	 */
	public LinkedHashSet<Integer> getAllIdsInCategory(String category, Navigate returnNav) {
		return getAllEntriesInCategory(category, 0, returnNav, Integer.MAX_VALUE, EnumSet.of(ReadMask.NOTEID),
				new NoteIdsAsOrderedSetCallback(Integer.MAX_VALUE));
	}
	
	/**
	 * Locates a note in the collection
	 * 
	 * @param noteId note id
	 * @return collection position or empty string if not found
	 */
	public String locateNote(int noteId) {
		checkDisposed();

		NotesCollectionPositionStruct foundPos = NotesCollectionPositionStruct.newInstance();
		short result = LockUtil.lockHandle(getAllocations().getCollectionHandle(), (hColByVal) -> {
			return NotesCAPI.get().NIFLocateNote(hColByVal, foundPos, noteId);
		});
		if ((result & NotesConstants.ERR_MASK)==1028) {
			return ""; //$NON-NLS-1$
		}
		NotesErrorUtils.checkResult(result);
		return foundPos.toPosString();
	}

	
	/**
	 * Callback base class used to process collection lookup results
	 * 
	 * @param <T> the destination type that subclasses will convert to
	 * @author Karsten Lehmann
	 */
	public static abstract class JNACollectionEntryProcessor<T> implements CollectionEntryProcessor<T> {
		private DominoDateTime m_newDiffTime;
		
		/**
		 * Override this method to return the programmatic name of a collection column. If
		 * a non-null value is returned, we use an optimized lookup method to read the data,
		 * resulting in much better performance (working like the formula @DbColumn)
		 * 
		 * @return programmatic column name or null
		 */
		public String getNameForSingleColumnRead() {
			return null;
		}
		
		/**
		 * This method gets called when a view index change has been detected
		 * during a view read operation which would cause the operation to be restarted.
		 * Add your own code to log these retries or decide to stop reading when too
		 * much time has passed
		 * 
		 * @param nrOfRetries number of retries already made (0 = first retry is about to begin)
		 * @param durationSinceStart number of milliseconds elapsed since starting the lookup
		 * @return action, whether to continue (default) or stop the lookup; if stop, the lookup method returns null; as an alternative, throw a {@link RuntimeException} here to jump out of the lookup function without return value
		 */
		public Action retryingReadBecauseViewIndexChanged(int nrOfRetries, long durationSinceStart) {
			return Action.Continue;
		}
		
		/**
		 * The method is called when differential view reading is used to return the {@link DominoDateTime}
		 * to be used for the next lookups
		 * 
		 * @param newDiffTime new diff time
		 */
		public void setNewDiffTime(DominoDateTime newDiffTime) {
			m_newDiffTime = newDiffTime;
		}
		
		/**
		 * Use this method to read the {@link DominoDateTime} to be used for the next lookups when using differential view
		 * reads
		 * 
		 * @return diff time or null
		 */
		public DominoDateTime getNewDiffTime() {
			return m_newDiffTime;
		}
		
		/**
		 * Override this method to return an optional {@link CollectionDataCache} to speed up view reading.
		 * The returned cache instance is shared for all calls done with this callback implementation.<br>
		 * <br>
		 * Please note that according to IBM dev, this optimized view reading (differential view reads) does
		 * only work in views that are not permuted (where documents do not appear multiple times, because
		 * "Show multiple values as separate entries" has been set on any view column).
		 * 
		 * @return cache or null (default value)
		 */
		public CollectionDataCache createDataCache() {
			return null;
		}
		
		private CollectionDataCache m_cacheInstance;
		
		/**
		 * Standard implementation of this method calls {@link #createDataCache()} once
		 * and stores the object instance in a member variable for later reuse.<br>
		 * Can be overridden in case you need to store the cache somewhere else,
		 * e.g. to reuse it later on.
		 * 
		 * @return cache
		 */
		public CollectionDataCache getDataCache() {
			if (m_cacheInstance==null) {
				m_cacheInstance = createDataCache();
			}
			return m_cacheInstance;
		}
		
	}

	/**
	 * Subclass of {@link JNACollectionEntryProcessor} that wraps any methods and forwards all calls
	 * the another {@link JNACollectionEntryProcessor}.
	 * 
	 * @param <T> the destination type that subclasses will convert to
	 * @author Karsten Lehmann
	 */
	public static class JNACollectionEntryProcessorWrapper<T> extends JNACollectionEntryProcessor<T> {
		private JNACollectionEntryProcessor<T> m_innerCallback;
		
		public JNACollectionEntryProcessorWrapper(JNACollectionEntryProcessor<T> innerCallback) {
			m_innerCallback = innerCallback;
		}

		@Override
		public String getNameForSingleColumnRead() {
			return m_innerCallback.getNameForSingleColumnRead();
		}
		
		@Override
		public T start() {
			return m_innerCallback.start();
		}

		@Override
		public Action entryRead(T result,
				CollectionEntry entryData) {
			return m_innerCallback.entryRead(result, entryData);
		}

		@Override
		public CollectionDataCache createDataCache() {
			return m_innerCallback.createDataCache();
		}
		
		@Override
		public DominoDateTime getNewDiffTime() {
			return m_innerCallback.getNewDiffTime();
		}
		
		@Override
		public void setNewDiffTime(DominoDateTime newDiffTime) {
			m_innerCallback.setNewDiffTime(newDiffTime);
		}
		
		@Override
		public T end(T result) {
			return m_innerCallback.end(result);
		}
		
		@Override
		public Action retryingReadBecauseViewIndexChanged(int nrOfRetries, long durationSinceStart) {
			return m_innerCallback.retryingReadBecauseViewIndexChanged(nrOfRetries, durationSinceStart);
		}
	}
	
	/**
	 * Subclass of {@link JNACollectionEntryProcessor} that uses an optimized view lookup to
	 * only read the value of a single collection column. This results in much
	 * better performance, because the 64K summary buffer is not polluted with irrelevant data.<br>
	 * <br>
	 * Please make sure to pass either {@link ReadMask#SUMMARYVALUES} or {@link ReadMask#SUMMARY},
	 * preferably {@link ReadMask#SUMMARYVALUES}.
	 * 
	 * @author Karsten Lehmann
	 */
	public static class ReadSingleColumnValues extends JNACollectionEntryProcessor<Set<String>> {
		private String m_columnName;
		private Locale m_sortLocale;
		
		/**
		 * Creates a new instance
		 * 
		 * @param columnName programmatic column name
		 * @param sortLocale optional sort locale used to sort the result
		 */
		public ReadSingleColumnValues(String columnName, Locale sortLocale) {
			m_columnName = columnName;
			m_sortLocale = sortLocale;
		}

		@Override
		public String getNameForSingleColumnRead() {
			return m_columnName;
		}
		
		@Override
		public Set<String> start() {
			Collator collator = Collator.getInstance(m_sortLocale==null ? Locale.getDefault() : m_sortLocale);
			return new TreeSet<>(collator);
		}

		@Override
		public Action entryRead(Set<String> result,
				CollectionEntry entryData) {
			String colValue = entryData.get(m_columnName, String.class, ""); //$NON-NLS-1$
			if (!StringUtil.isEmpty(colValue)) {
				result.add(colValue);
			}
			return Action.Continue;
		}

		@Override
		public Set<String> end(Set<String> result) {
			return result;
		}
	}
	
	/**
	 * Subclass of {@link JNACollectionEntryProcessor} that stores the data of read collection entries
	 * in a {@link List}.
	 * 
	 * @author Karsten Lehmann
	 */
	public static class EntriesAsListCallback extends JNACollectionEntryProcessor<List<CollectionEntry>> {
		private int m_maxEntries;
		
		/**
		 * Creates a new instance
		 * 
		 * @param maxEntries maximum entries to return
		 */
		public EntriesAsListCallback(int maxEntries) {
			m_maxEntries = maxEntries;
		}
		
		@Override
		public List<CollectionEntry> start() {
			return new ArrayList<>();
		}

		@Override
		public Action entryRead(
				List<CollectionEntry> result, CollectionEntry entryData) {
			
			if (m_maxEntries==0) {
				return Action.Stop;
			}

			if (!isAccepted(entryData)) {
				//ignore this entry
				return Action.Continue;
			}

			//add entry to result list
			result.add(entryData);
			
			if (result.size() >= m_maxEntries) {
				//stop the lookup, we have enough data
				return Action.Stop;
			}
			else {
				//go on reading the view
				return Action.Continue;
			}
		}

		/**
		 * Override this method to filter entries
		 * 
		 * @param entryData current entry
		 * @return true if entry should be added to the result
		 */
		protected boolean isAccepted(CollectionEntry entryData) {
			return true;
		}
		
		@Override
		public List<CollectionEntry> end(List<CollectionEntry> result) {
			return result;
		}
	}

	/**
	 * Subclass of {@link JNACollectionEntryProcessor} that stores the the note ids of read collection entries
	 * in a {@link LinkedHashSet}, a {@link Set} that keeps the insertion order.
	 * 
	 * @author Karsten Lehmann
	 */
	public static class NoteIdsAsOrderedSetCallback extends JNACollectionEntryProcessor<LinkedHashSet<Integer>> {
		private int m_maxEntries;

		public NoteIdsAsOrderedSetCallback(int maxEntries) {
			m_maxEntries = maxEntries;
		}

		@Override
		public LinkedHashSet<Integer> start() {
			return new LinkedHashSet<>();
		}

		@Override
		public Action entryRead(LinkedHashSet<Integer> result, CollectionEntry entryData) {
			if (m_maxEntries==0) {
				return Action.Stop;
			}

			int noteId = entryData.getNoteID();
			if (noteId != 0) {
				if (!isAccepted(noteId)) {
					//ignore this note id
					return Action.Continue;
				}

				//add note id to result list
				result.add(noteId);
				
				if (result.size() >= m_maxEntries) {
					//stop the lookup, we have enough data
					return Action.Stop;
				}
				else {
					//go on reading the view
					return Action.Continue;
				}
			}
			else {
				return Action.Continue;
			}
		}

		/**
		 * Override this method to filter note ids
		 * 
		 * @param noteId current note id
		 * @return true if note id should be added to the result
		 */
		protected boolean isAccepted(int noteId) {
			return true;
		}
		
		@Override
		public LinkedHashSet<Integer> end(LinkedHashSet<Integer> result) {
			return result;
		}
	}

	@Override
	public String toStringLocal() {
		if (isDisposed()) {
			return "JNADominoCollection [disposed]"; //$NON-NLS-1$
		}
		else {
			return MessageFormat.format(
				"JNADominoCollection [handle={0}, name={1}, aliases={2}, noteid={3}, columns={4}]", //$NON-NLS-1$
				getAllocations().getCollectionHandle(), getName(), getAliases(), getNoteId(), getColumnNames()
			);
		}
	}
}
