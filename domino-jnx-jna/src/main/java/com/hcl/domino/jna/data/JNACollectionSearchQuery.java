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
package com.hcl.domino.jna.data;

import java.lang.ref.ReferenceQueue;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.commons.views.FindFlag;
import com.hcl.domino.commons.views.ReadMask;
import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.CollectionEntry.SpecialValue;
import com.hcl.domino.data.CollectionSearchQuery;
import com.hcl.domino.data.Database.Action;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.FTQuery;
import com.hcl.domino.data.Navigate;
import com.hcl.domino.dql.DQL.DQLTerm;
import com.hcl.domino.exception.ObjectDisposedException;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNACollectionSearchQueryAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNADominoCollectionAllocations;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.views.NotesViewLookupResultData;
import com.hcl.domino.misc.Loop;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.ptr.ShortByReference;

public class JNACollectionSearchQuery extends BaseJNAAPIObject<JNACollectionSearchQueryAllocations> implements CollectionSearchQuery {
	//where to start reading view data:
	private boolean m_startAtLastEntry;
	private int m_startAtEntryId;
	private String m_startAtCategory;
	private List<Object> m_startAtCategoryLevels;
	private String m_startAtPosition;

	//IDTables to control what is expanded/selected
	private ExpandedEntries m_expandedEntries;
	private JNAIDTable m_expandedEntriesResolved;
	
	private SelectedEntries m_selectedEntries;
	private JNAIDTable m_selectedEntriesResolved;
	private boolean m_hasSelectionSet;

	//how to traverse the view
	private Set<ReadMask> m_readMask;
	private Navigate m_direction;
	private Integer m_total;
	private boolean m_hasExpandedEntries;
	
	JNACollectionSearchQuery(JNADominoCollection parentCollection) {
		super(parentCollection);
		
		m_direction = Navigate.NEXT_ENTRY;
		m_readMask = new HashSet<>();
		m_readMask.add(ReadMask.NOTEID);
		
		setInitialized();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected JNACollectionSearchQueryAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		
		return new JNACollectionSearchQueryAllocations(parentDominoClient, parentAllocations, this, queue);
	}
	
	@Override
	protected void checkDisposedLocal() {
		JNADominoCollection parentCollection = (JNADominoCollection) getParent();
		
		if (parentCollection.isDisposed()) {
			throw new ObjectDisposedException(this);
		}
	}

	@Override
	public CollectionSearchQuery readSpecialValues(Collection<SpecialValue> values) {
		if (values!=null) {
			values.forEach(this::addSpecialValue);
		}
		return this;
	}
	
	@Override
	public CollectionSearchQuery readSpecialValues(SpecialValue... values) {
		if (values!=null) {
			for (SpecialValue currVal : values) {
				addSpecialValue(currVal);
			}
		}
		return this;
	}
	
	private void addSpecialValue(SpecialValue value) {
		switch (value) {
		case CHILDCOUNT:
			m_readMask.add(ReadMask.INDEXCHILDREN);
			break;
		case DESCENDANTCOUNT:
			m_readMask.add(ReadMask.INDEXDESCENDANTS);
			break;
		case INDEXPOSITION:
			m_readMask.add(ReadMask.INDEXPOSITION);
			break;
		case SIBLINGCOUNT:
			m_readMask.add(ReadMask.INDEXSIBLINGS);
			break;
		case UNREAD:
			m_readMask.add(ReadMask.INDEXUNREAD);
			break;
		case ANYUNREAD:
			m_readMask.add(ReadMask.INDEXANYUNREAD);
			break;
			default:
				// there's no ReadMask entry for SEQUENCENUMBER / SEQUENCETIME,
				// must be read from the DB / doc
		}
	}

	@Override
	public CollectionSearchQuery startAtFirstEntry() {
		//reset all
		m_startAtEntryId = 0;
		m_startAtLastEntry = false;
		m_startAtCategory = null;
		m_startAtCategoryLevels = null;
		m_startAtPosition = null;
		
		m_total = null;
		return this;
	}

	@Override
	public CollectionSearchQuery startAtEntryId(int noteId) {
		m_startAtEntryId = noteId;
		m_startAtLastEntry = false;
		m_startAtCategory = null;
		m_startAtCategoryLevels = null;
		m_startAtPosition = null;
		
		m_total = null;
		return this;
	}

	@Override
	public CollectionSearchQuery startAtLastEntry() {
		m_startAtLastEntry = true;
		
		m_startAtEntryId = 0;
		m_startAtCategory = null;
		m_startAtCategoryLevels = null;
		m_startAtPosition = null;
		
		m_total = null;
		return this;
	}

	@Override
	public CollectionSearchQuery startAtCategory(String category) {
		m_startAtCategory = category;
		
		m_startAtEntryId = 0;
		m_startAtLastEntry = false;
		m_startAtCategoryLevels = null;
		m_startAtPosition = null;
		
		m_total = null;
		return this;
	}

	@Override
	public CollectionSearchQuery startAtCategory(List<Object> categoryLevels) {
		m_startAtCategoryLevels = categoryLevels==null ? null : new ArrayList<>(categoryLevels);
		
		m_startAtEntryId = 0;
		m_startAtLastEntry = false;
		m_startAtCategory = null;
		m_startAtPosition = null;
		
		m_total = null;
		return this;
	}

	@Override
	public CollectionSearchQuery startAtPosition(String pos) {
		m_startAtPosition = pos;
		
		m_startAtEntryId = 0;
		m_startAtLastEntry = false;
		m_startAtCategory = null;
		m_startAtCategoryLevels = null;
		
		m_total = null;
		return this;
	}
	
	@Override
	public CollectionSearchQuery expand(ExpandedEntries expandedEntries) {
		m_expandedEntries = expandedEntries;
		m_expandedEntriesResolved = null;
		m_hasExpandedEntries = true;
		m_total = null;
		if (!isDirectionWithExpandCollapse(m_direction)) {
			//automatically select a traversal strategy that makes use of
			//selection / expanded info
			
			if (isDirectionWithSelection(m_direction)) {
				m_direction = Navigate.NEXT_EXPANDED_SELECTED;
			}
			else {
				m_direction = Navigate.NEXT_EXPANDED;
			}
		}
		return this;
	}

	@Override
	public CollectionSearchQuery select(SelectedEntries selectedEntries) {
		m_selectedEntries = selectedEntries;
		m_selectedEntriesResolved = null;
		m_total = null;
		m_hasSelectionSet = true;
		
		if (!isDirectionWithSelection(m_direction)) {
			//automatically select a traversal strategy that makes use of
			//selection / expanded info
			
			if (isDirectionWithExpandCollapse(m_direction)) {
				m_direction = Navigate.NEXT_EXPANDED_SELECTED;
			}
			else {
				m_direction = Navigate.NEXT_SELECTED;
			}
		}
		return this;
	}

	@Override
	public CollectionSearchQuery selectByKey(List<Object> key, boolean exact) {
		if (m_selectedEntries==null || !(m_selectedEntries instanceof AllDeselectedEntries)) {
			select(SelectedEntries.deselectAll().selectByKey(key, exact));
		}
		else {
			((AllDeselectedEntries)m_selectedEntries).selectByKey(key, exact);
		}
		
		return this;
	}
	
	@Override
	public CollectionSearchQuery selectByKey(String key, boolean exact) {
		if (m_selectedEntries==null || !(m_selectedEntries instanceof AllDeselectedEntries)) {
			select(SelectedEntries.deselectAll().selectByKey(key, exact));
		}
		else {
			((AllDeselectedEntries)m_selectedEntries).selectByKey(key, exact);
		}
		
		return this;
	}
	
	@Override
	public CollectionSearchQuery deselectByKey(List<Object> key, boolean exact) {
		if (m_selectedEntries==null || !(m_selectedEntries instanceof AllSelectedEntries)) {
			select(SelectedEntries.selectAll().deselectByKey(key, exact));
		}
		else {
			((AllSelectedEntries)m_selectedEntries).deselectByKey(key, exact);
		}
		
		return this;
	}
	
	@Override
	public CollectionSearchQuery deselectByKey(String key, boolean exact) {
		if (m_selectedEntries==null || !(m_selectedEntries instanceof AllSelectedEntries)) {
			select(SelectedEntries.selectAll().deselectByKey(key, exact));
		}
		else {
			((AllSelectedEntries)m_selectedEntries).deselectByKey(key, exact);
		}
		
		return this;
	}
	
	@Override
	public CollectionSearchQuery direction(Navigate mode) {
		m_direction = mode;
		return this;
	}

	@Override
	public CollectionSearchQuery readColumnValues() {
		m_readMask.add(ReadMask.SUMMARYVALUES);
		return this;
	}
	
	@Override
	public CollectionSearchQuery readUNID() {
		m_readMask.add(ReadMask.NOTEUNID);
		return this;
	}
	
	@Override
	public CollectionSearchQuery readDocumentClass() {
		m_readMask.add(ReadMask.NOTECLASS);
		return this;
	}
	
	private boolean isDirectionWithSelection(Navigate nav) {
		if (nav == Navigate.NEXT_SELECTED || /* m_direction == Navigate.NEXT_SELECTED_HIT || */
				nav == Navigate.NEXT_SELECTED_ON_TOPLEVEL ||
				nav == Navigate.PREV_SELECTED || /* m_direction == Navigate.PREV_SELECTED_HIT || */
				nav == Navigate.PREV_SELECTED_ON_TOPLEVEL) {
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean isDirectionWithExpandCollapse(Navigate nav) {
		if (nav == Navigate.NEXT_EXPANDED || m_direction == Navigate.NEXT_EXPANDED_CATEGORY ||
				nav == Navigate.NEXT_EXPANDED_DOCUMENT ||
				nav == Navigate.NEXT_EXPANDED_SELECTED ||
				nav == Navigate.NEXT_EXPANDED_UNREAD) {
			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	public <T> T build(int skip, int count, CollectionEntryProcessor<T> processor) {
		JNADominoCollection collection = (JNADominoCollection) getParent();
		JNADominoCollectionAllocations collectionAllocations = (JNADominoCollectionAllocations) collection.getAdapter(APIObjectAllocations.class);

		ShortByReference updateFiltersFlags = new ShortByReference();
		Navigate directionToUse = prepareCollectionReadRestrictions(collectionAllocations, updateFiltersFlags);

		if (directionToUse == Navigate.CURRENT && count>1) {
			//prevent reading too many entries if navigation is set to just read the current entry
			count = 1;
		}
		
		final short fUpdateFiltersFlagsVal = updateFiltersFlags.getValue();
		final int fCount = count;
		final Navigate fDirectionToUse = directionToUse;
		
		return LockUtil.lockHandle(collectionAllocations.getCollectionHandle(), (collectionHandleByVal) -> {
			while (true) {
				if (fUpdateFiltersFlagsVal != 0) {
					//for remote databases, push IDTable changes via NRPC

					short result = NotesCAPI.get().NIFUpdateFilters(collectionHandleByVal, fUpdateFiltersFlagsVal);
					NotesErrorUtils.checkResult(result);
				}
				
				JNADominoCollection.JNACollectionEntryProcessor<T> jnaProcessor = new JNADominoCollection.JNACollectionEntryProcessor<T>() {
					int entriesRead = 0;

					@Override
					public T start() {
						entriesRead = 0;
						return processor.start();
					}

					@Override
					public Action entryRead(T result, CollectionEntry entry) {
						Action action = processor.entryRead(result, entry);
						entriesRead++;

						if (entriesRead>=fCount) {
							return Action.Stop;
						}
						else {
							return action;
						}
					}

					@Override
					public T end(T result) {
						return processor.end(result);
					}

				};

				int indexModStart = collection.getIndexModifiedSequenceNo();

				if (m_startAtCategory!=null || m_startAtCategoryLevels!=null) {
					Object[] categoryLevelsAsArr;
					if (m_startAtCategory!=null) {
						categoryLevelsAsArr = new Object[] {m_startAtCategory};
					}
					else {
						categoryLevelsAsArr = m_startAtCategoryLevels.toArray(new Object[m_startAtCategoryLevels.size()]);
					}

					T result = collection.getAllEntriesInCategory(categoryLevelsAsArr, skip, fDirectionToUse, null, null,
							fCount, m_readMask, jnaProcessor);
					return result;
				}
				else {
					//find first entry to read

					String startPos;
					int additionalSkip;

					if (m_startAtLastEntry) {
						startPos = "last"; //$NON-NLS-1$
						additionalSkip = 0;
					}
					else if (m_startAtPosition!=null) {
						startPos = m_startAtPosition;
						additionalSkip = 0;
					}
					else if (m_startAtEntryId!=0) {
						JNAIDTable selectedList = collectionAllocations.getSelectedList();
						JNAIDTable clonedSelectedList = (JNAIDTable) selectedList.clone();
						
						selectedList.setInverted(false);
						selectedList.clear();
						selectedList.add(m_startAtEntryId);
						
						//for remote databases, push IDTable changes via NRPC
						short resultUpdateFilter = NotesCAPI.get().NIFUpdateFilters(collectionHandleByVal, NotesConstants.FILTER_SELECTED);
						NotesErrorUtils.checkResult(resultUpdateFilter);
						
						List<CollectionEntry> selectedEntries = collection.getAllEntries("0", 1, Navigate.NEXT_SELECTED, Integer.MAX_VALUE, //$NON-NLS-1$
								EnumSet.of(ReadMask.INDEXPOSITION, ReadMask.NOTEID),
								new JNADominoCollection.EntriesAsListCallback(Integer.MAX_VALUE));
						
						//reset selection IDTable
						selectedList.clear();
						selectedList.addAll(clonedSelectedList);
						selectedList.setInverted(clonedSelectedList.isInverted());
						clonedSelectedList.dispose();
						
						resultUpdateFilter = NotesCAPI.get().NIFUpdateFilters(collectionHandleByVal, NotesConstants.FILTER_SELECTED);
						NotesErrorUtils.checkResult(resultUpdateFilter);

						if (selectedEntries.isEmpty()) {
							//start entry not found
							T result = processor.start();
							result = processor.end(result);
							return result;
						}
						
						String entryPos = selectedEntries.get(0).getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""); //$NON-NLS-1$
						if (StringUtil.isEmpty(entryPos)) {
							//start entry not found
							T result = processor.start();
							result = processor.end(result);
							return result;
						}

						startPos = entryPos;
						additionalSkip = 0;
					}
					else {
						//default: start at first entry
						startPos = "0"; //$NON-NLS-1$
						additionalSkip = 1;
					}

					T result = collection.getAllEntries(startPos, additionalSkip + skip,
							fDirectionToUse, fCount, m_readMask, jnaProcessor);

					int indexModEnd = collection.getIndexModifiedSequenceNo();
					
					if (indexModStart != indexModEnd) {
						//restart lookup, index changed
						collection.refresh();
						continue;
					}
					return result;
				}
			}
		});
	}

	/**
	 * Makes sure that the navigation rule respects expanded entries
	 * 
	 * @param nav navigation
	 * @return navigation direction respecting selection
	 * @throws UnsupportedOperationException in case the given navigation direction cannot be combined with expanded entries
	 */
	private Navigate addExpandNavigation(Navigate nav) {
		if (isDirectionWithExpandCollapse(nav)) {
			//nothing to do
			return nav;
		}
		
		switch (nav) {
		//these already respect expand states
		case NEXT_EXPANDED:
		case NEXT_EXPANDED_CATEGORY:
		case NEXT_EXPANDED_DOCUMENT:
		case NEXT_EXPANDED_SELECTED:
		case NEXT_EXPANDED_UNREAD:
		case PREV_EXPANDED:
		case PREV_EXPANDED_CATEGORY:
		case PREV_EXPANDED_DOCUMENT:
		case PREV_EXPANDED_SELECTED:
		case PREV_EXPANDED_UNREAD:
			return nav;
			
		//this one is probably ok to ignore the expand states
		case CURRENT:
			return nav;

		//apply expand rule to forward directions
		case NEXT_CATEGORY:
			return Navigate.NEXT_EXPANDED_CATEGORY;
		case NEXT_DOCUMENT:
			return Navigate.NEXT_EXPANDED_DOCUMENT;
		case NEXT_ENTRY:
			return Navigate.NEXT_EXPANDED;
		case NEXT_UNREAD_ENTRY:
			return Navigate.NEXT_EXPANDED_UNREAD;
		case NEXT_SELECTED:
			return Navigate.NEXT_EXPANDED_SELECTED;
			
		//apply expand rule to backward directions
		case PREV_CATEGORY:
			return Navigate.PREV_EXPANDED_CATEGORY;
		case PREV_DOCUMENT:
			return Navigate.PREV_EXPANDED_DOCUMENT;
		case PREV_ENTRY:
			return Navigate.PREV_EXPANDED;
		case PREV_UNREAD_ENTRY:
			return Navigate.PREV_EXPANDED_UNREAD;
		case PREV_SELECTED:
			return Navigate.PREV_EXPANDED_SELECTED;
				
		//cannot yet be combined with expanded states in NIF
		case PARENT_ENTRY:
		case CHILD_ENTRY:
		case FIRST_ON_SAME_LEVEL:
		case LAST_ON_SAME_LEVEL:
			
		case NEXT_ON_SAME_LEVEL:
		case NEXT_ON_TOPLEVEL:
		case NEXT_PARENT_ENTRY:
		case NEXT_SELECTED_ON_TOPLEVEL:
		case NEXT_UNREAD_TOPLEVEL_ENTRY:
			
		case PREV_ON_SAME_LEVEL:
		case PREV_ON_TOPLEVEL:
		case PREV_PARENT_ENTRY:
		case PREV_SELECTED_ON_TOPLEVEL:
		case PREV_UNREAD_TOPLEVEL_ENTRY:
		default:
			throw new UnsupportedOperationException(MessageFormat.format("Combining navigation direction {0} with expanded states is currently unsupported by NIF", nav));
		}
	}

	@Override
	public LinkedHashSet<Integer> collectIds(int skip, int count) {
		return build(skip, count, new CollectionEntryProcessor<LinkedHashSet<Integer>>() {

			@Override
			public LinkedHashSet<Integer> start() {
				return new LinkedHashSet<>();
			}

			@Override
			public Action entryRead(LinkedHashSet<Integer> result, CollectionEntry entry) {
				result.add(entry.getNoteID());
				return Action.Continue;
			}

			@Override
			public LinkedHashSet<Integer> end(LinkedHashSet<Integer> result) {
				return result;
			}
		});
	}

	@Override
	public void collectIds(int skip, int count, Collection<Integer> idTable) {
		LinkedHashSet<Integer> ids = collectIds(skip, count);
		idTable.addAll(ids);
		
		build(skip, count, new CollectionEntryProcessor<Collection<Integer>>() {

			@Override
			public Collection<Integer> start() {
				return idTable;
			}

			@Override
			public Action entryRead(Collection<Integer> result, CollectionEntry entry) {
				result.add(entry.getNoteID());
				return Action.Continue;
			}

			@Override
			public Collection<Integer> end(Collection<Integer> result) {
				return result;
			}
		});
	}

	@Override
	public List<CollectionEntry> collectEntries(int skip, int count) {
		return build(skip, count, new CollectionEntryProcessor<List<CollectionEntry>>() {

			@Override
			public List<CollectionEntry> start() {
				return new ArrayList<>();
			}

			@Override
			public Action entryRead(List<CollectionEntry> result, CollectionEntry entry) {
				result.add(entry);
				return Action.Continue;
			}

			@Override
			public List<CollectionEntry> end(List<CollectionEntry> result) {
				return result;
			}
		});
	}

	@Override
	public void collectEntries(int skip, int count, Collection<CollectionEntry> collection) {
		build(skip, count, new CollectionEntryProcessor<Collection<CollectionEntry>>() {

			@Override
			public Collection<CollectionEntry> start() {
				return collection;
			}

			@Override
			public Action entryRead(Collection<CollectionEntry> result, CollectionEntry entry) {
				result.add(entry);
				return Action.Continue;
			}

			@Override
			public Collection<CollectionEntry> end(Collection<CollectionEntry> result) {
				return result;
			}
		});
	}

	@Override
	public int size() {
		if (m_total==null) {
			JNADominoCollection collection = (JNADominoCollection) getParent();
			JNADominoCollectionAllocations collectionAllocations = (JNADominoCollectionAllocations) collection.getAdapter(APIObjectAllocations.class);

			ShortByReference updateFiltersFlags = new ShortByReference();
			Navigate directionToUse = prepareCollectionReadRestrictions(collectionAllocations, updateFiltersFlags);
			
			final short fUpdateFiltersFlagsVal = updateFiltersFlags.getValue();
			final Navigate fDirectionToUse = directionToUse;
			
			m_total = LockUtil.lockHandle(collectionAllocations.getCollectionHandle(), (collectionHandleByVal) -> {
				if (fUpdateFiltersFlagsVal != 0) {
					//for remote databases, push IDTable changes via NRPC

					short result = NotesCAPI.get().NIFUpdateFilters(collectionHandleByVal, fUpdateFiltersFlagsVal);
					NotesErrorUtils.checkResult(result);
				}
				
				if (m_startAtCategory!=null || m_startAtCategoryLevels!=null) {
					Object[] categoryLevelsAsArr;
					if (m_startAtCategory!=null) {
						categoryLevelsAsArr = new Object[] {m_startAtCategory};
					}
					else {
						categoryLevelsAsArr = m_startAtCategoryLevels.toArray(new Object[m_startAtCategoryLevels.size()]);
					}

					if (JNADominoCollection.isDescendingNav(fDirectionToUse)) {
						return 0;
					}
					
					while (true) {
						int indexModStart = collection.getIndexModifiedSequenceNo();

						//find category entry
						NotesViewLookupResultData catLkResult = collection.findByKeyExtended2(EnumSet.of(FindFlag.MATCH_CATEGORYORLEAF,
								FindFlag.REFRESH_FIRST, FindFlag.RETURN_DWORD, FindFlag.AND_READ_MATCHES, FindFlag.CASE_INSENSITIVE),
								EnumSet.of(ReadMask.NOTEID, ReadMask.SUMMARY), categoryLevelsAsArr);

						if (catLkResult.getReturnCount()==0) {
							//category not found
							return 0;
						}

						final String catPosStr = catLkResult.getPosition();
						if (StringUtil.isEmpty(catPosStr)) {
							//category not found
							return 0;
						}
						
						JNADominoCollectionPosition catPos = new JNADominoCollectionPosition(catPosStr);
						NotesViewLookupResultData skipResult = collection.readEntries(catPos, fDirectionToUse, false,
								Integer.MAX_VALUE, fDirectionToUse, 0, EnumSet.of(ReadMask.NOTEID));
						
						int indexModEnd = collection.getIndexModifiedSequenceNo();

						if (indexModStart != indexModEnd) {
							collection.refresh();
							continue;
						}
						
						return skipResult.getSkipCount();
					}
				}
				else {
					//find first entry to read
					String startPos;

					if (m_startAtLastEntry) {
						NotesViewLookupResultData findLastResult = collection.readEntries(
								new JNADominoCollectionPosition("0"), //$NON-NLS-1$
								Navigate.NEXT_ENTRY, true, Integer.MAX_VALUE, Navigate.CURRENT,
								0, EnumSet.of(ReadMask.INDEXPOSITION));
						
						List<JNACollectionEntry> entries = findLastResult.getEntries();
						if (entries.isEmpty()) {
							return 0;
						}
						
						startPos = entries.get(0).getSpecialValue(SpecialValue.INDEXPOSITION, String.class, ""); //$NON-NLS-1$
					}
					else if (m_startAtEntryId!=0) {
						String entryPos = collection.locateNote(m_startAtEntryId);
						if (StringUtil.isEmpty(entryPos)) {
							//start entry not found
							return 0;
						}

						startPos = entryPos;
					}
					else {
						//default: start at first entry
						startPos = "0"; //$NON-NLS-1$
					}

					NotesViewLookupResultData skipResult = collection.readEntries(
							new JNADominoCollectionPosition(startPos),
							fDirectionToUse, false, Integer.MAX_VALUE, Navigate.CURRENT,
							0, EnumSet.of(ReadMask.NOTEID));
					
					return skipResult.getSkipCount();
				}
			});
		}
		return m_total;
	}

	/**
	 * 
	 * @param collectionAllocations
	 * @param updateFiltersFlags
	 * @return navigate direction for the collection lookup (using m_direction
	 */
	private Navigate prepareCollectionReadRestrictions(JNADominoCollectionAllocations collectionAllocations,
			ShortByReference updateFiltersFlags) {
		
		short updateFiltersFlagsVal = updateFiltersFlags.getValue();
		
		Navigate directionToUse = m_direction;
		if (directionToUse==null) {
			//read all entries by default
			directionToUse = Navigate.NEXT_ENTRY;
		}

		if (m_hasSelectionSet) {
			//make sure that the navigation direction respects the selection; noop if already the case
			directionToUse = addSelectionNavigation(directionToUse);
		}

		if (m_hasExpandedEntries) {
			//make sure that the navigation direction respects expanded entries; noop if already the case
			directionToUse = addExpandNavigation(directionToUse);
		}

		if (isDirectionWithSelection(directionToUse)) {
			//resolve and set the selected entries idtable
			JNAIDTable selectedList = collectionAllocations.getSelectedList();
			JNAIDTable resolvedSelectedList = resolveSelectedEntries(m_selectedEntries);
			selectedList.clear();
			selectedList.addAll(resolvedSelectedList);
			selectedList.setInverted(resolvedSelectedList.isInverted());
			
			updateFiltersFlagsVal |= NotesConstants.FILTER_SELECTED;
		}
		
		if (isDirectionWithExpandCollapse(directionToUse)) {
			//resolve and set the expanded entries idtable
			JNAIDTable collapsedList = collectionAllocations.getCollapsedList();
			JNAIDTable resolvedCollapsedList = resolveExpandedEntries(m_expandedEntries);
			collapsedList.clear();
			collapsedList.addAll(resolvedCollapsedList);
			collapsedList.setInverted(resolvedCollapsedList.isInverted());

			updateFiltersFlagsVal |= NotesConstants.FILTER_COLLAPSED;
		}

		//return update flags to push the changed idtables to remote DBs
		updateFiltersFlags.setValue(updateFiltersFlagsVal);
		
		//return the direction that is used for the actual lookup
		return directionToUse;
	}

	/**
	 * Makes sure that the navigation rule respects an applied selection
	 * 
	 * @param nav navigation
	 * @return navigation direction respecting selection
	 * @throws UnsupportedOperationException in case the given navigation direction cannot be combined with a selection
	 */
	private Navigate addSelectionNavigation(Navigate nav) {
		switch (nav) {
		//these already respect the selection
		case NEXT_SELECTED:
		case NEXT_SELECTED_ON_TOPLEVEL:
		case NEXT_EXPANDED_SELECTED:
		case PREV_SELECTED:
		case PREV_SELECTED_ON_TOPLEVEL:
		case PREV_EXPANDED_SELECTED:
			return nav;

		//here it's easy to add selection navigation
		case NEXT_EXPANDED:
			return Navigate.NEXT_EXPANDED_SELECTED;
		case PREV_EXPANDED:
			return Navigate.PREV_EXPANDED_SELECTED;
		case NEXT_ON_TOPLEVEL:
			return Navigate.NEXT_SELECTED_ON_TOPLEVEL;
		case PREV_ON_TOPLEVEL:
			return Navigate.PREV_SELECTED_ON_TOPLEVEL;
		case NEXT_ENTRY:
		case NEXT_DOCUMENT:
			return Navigate.NEXT_SELECTED;
		case PREV_ENTRY:
		case PREV_DOCUMENT:
			return Navigate.PREV_SELECTED;

		//this one is probably ok to ignore the selection
		case CURRENT:
			return nav;

		case CHILD_ENTRY:
		case PARENT_ENTRY:
		case FIRST_ON_SAME_LEVEL:
		case LAST_ON_SAME_LEVEL:

		case NEXT_CATEGORY:
		case NEXT_EXPANDED_CATEGORY:
		case NEXT_EXPANDED_DOCUMENT:
		case NEXT_EXPANDED_UNREAD:
		case NEXT_ON_SAME_LEVEL:
		case NEXT_PARENT_ENTRY:
		case NEXT_UNREAD_ENTRY:
		case NEXT_UNREAD_TOPLEVEL_ENTRY:

		case PREV_CATEGORY:
		case PREV_EXPANDED_CATEGORY:
		case PREV_EXPANDED_DOCUMENT:
		case PREV_EXPANDED_UNREAD:
		case PREV_UNREAD_TOPLEVEL_ENTRY:
		case PREV_UNREAD_ENTRY:
		case PREV_PARENT_ENTRY:
		case PREV_ON_SAME_LEVEL:
		default:
			throw new UnsupportedOperationException(MessageFormat.format("Combining navigation direction {0} with selecting entries is currently unsupported by NIF", nav));
		}
	}

	/**
	 * Collect all note ids configured in the {@link ExpandedEntries} object
	 * 
	 * @param expandedEntries info about expanded entries
	 * @param retIdTable IDTable to clear and write new note ids
	 */
	private JNAIDTable resolveExpandedEntries(ExpandedEntries expandedEntries) {
		if (m_expandedEntriesResolved==null || m_expandedEntriesResolved.isDisposed()) {
			JNAIDTable idTable = new JNAIDTable(getParentDominoClient());
			idTable.clear();
			
			if (expandedEntries!=null) {
				if (expandedEntries.getMode() == ExpandMode.AllExpanded) {
					idTable.setInverted(false);
				}
				else {
					idTable.setInverted(true);
				}
				//manually set note ids
				Set<Integer> noteIds = expandedEntries.getNoteIds();
				idTable.addAll(noteIds);

				JNADatabase db = (JNADatabase) ((JNADominoCollection)getParent()).getParentDatabase();

				List<DQLTerm> dqlQueries = expandedEntries.getDQLQueries();
				for (DQLTerm currDQLQuery : dqlQueries) {
					db.queryDQL(currDQLQuery).collectIds(0, Integer.MAX_VALUE, idTable);
				}

				List<String> ftQueries = expandedEntries.getFTQueries();
				for (String currFTQuery : ftQueries) {
					db.queryFTIndex(currFTQuery, 0, EnumSet.of(FTQuery.RETURN_IDTABLE), null, 0, 0).collectIds(0, Integer.MAX_VALUE, idTable);
				}
			}
			else {
				idTable.setInverted(false);
			}
			
			m_expandedEntriesResolved = idTable;
		}
		return m_expandedEntriesResolved;
	}
	
	/**
	 * Collect all note ids configured in the {@link ExpandedEntries} object
	 * 
	 * @param selectedEntries info about selected entries
	 */
	private JNAIDTable resolveSelectedEntries(SelectedEntries selectedEntries) {
		if (m_selectedEntriesResolved==null || m_selectedEntriesResolved.isDisposed()) {
			JNAIDTable idTable = new JNAIDTable(getParentDominoClient());
			idTable.clear();

			if (selectedEntries!=null) {
				JNADominoCollection collection = (JNADominoCollection) getParent();
				
				boolean subtractMode;
				
				if (selectedEntries.getMode() == SelectMode.AllSelected) {
					subtractMode = true;
					
					JNAIDTable tableWithAllIds = (JNAIDTable) collection.getAllIdsAsIDTable(false);
					idTable.addAll(tableWithAllIds);
					tableWithAllIds.dispose();
				}
				else {
					subtractMode = false;
				}
				
				//manually set note ids
				Set<Integer> noteIds = selectedEntries.getNoteIds();
				if (subtractMode) {
					idTable.removeAll(noteIds);
				}
				else {
					idTable.addAll(noteIds);
				}

				List<SingleColumnLookupKey> singleColLookups = selectedEntries.getLookupKeysSingleCol();
				List<MultiColumnLookupKey> multiColLookups = selectedEntries.getLookupKeysMultiCol();

				if (!singleColLookups.isEmpty() || !multiColLookups.isEmpty()) {
					
					for (SingleColumnLookupKey currKey : singleColLookups) {
						Set<FindFlag> findFlags = EnumSet.of(FindFlag.EQUAL, FindFlag.RANGE_OVERLAP, FindFlag.CASE_INSENSITIVE);
						if (!currKey.isExact()) {
							findFlags.add(FindFlag.PARTIAL);
						}
						LinkedHashSet<Integer> idsForKey = collection.getAllEntriesByKey(findFlags, EnumSet.of(ReadMask.NOTEID),
								new JNADominoCollection.NoteIdsAsOrderedSetCallback(Integer.MAX_VALUE), currKey.getKey());
						
						if (subtractMode) {
							idTable.removeAll(idsForKey);
						}
						else {
							idTable.addAll(idsForKey);
						}
					}
					
					for (MultiColumnLookupKey currKey : multiColLookups) {
						Set<FindFlag> findFlags = EnumSet.of(FindFlag.EQUAL, FindFlag.RANGE_OVERLAP, FindFlag.CASE_INSENSITIVE);
						if (!currKey.isExact()) {
							findFlags.add(FindFlag.PARTIAL);
						}
						LinkedHashSet<Integer> idsForKey = collection.getAllEntriesByKey(findFlags, EnumSet.of(ReadMask.NOTEID),
								new JNADominoCollection.NoteIdsAsOrderedSetCallback(Integer.MAX_VALUE), currKey.getKey().toArray(new Object[currKey.getKey().size()]));
						
						if (subtractMode) {
							idTable.removeAll(idsForKey);
						}
						else {
							idTable.addAll(idsForKey);
						}
					}
				}
				
				JNADatabase db = (JNADatabase) ((JNADominoCollection)getParent()).getParentDatabase();

				List<DQLTerm> dqlQueries = selectedEntries.getDQLQueries();
				for (DQLTerm currDQLQuery : dqlQueries) {
					if (subtractMode) {
						JNAIDTable tableOfDQLResult = new JNAIDTable(getParentDominoClient());
						db.queryDQL(currDQLQuery).collectIds(0, Integer.MAX_VALUE, tableOfDQLResult);
						idTable.removeAll(tableOfDQLResult);
						tableOfDQLResult.dispose();
					}
					else {
						db.queryDQL(currDQLQuery).collectIds(0, Integer.MAX_VALUE, idTable);
					}
				}
				
				List<String> ftQueries = selectedEntries.getFTQueries();
				for (String currFTQuery : ftQueries) {
					if (subtractMode) {
						JNAIDTable tableOfFTResult = new JNAIDTable(getParentDominoClient());
						db.queryFTIndex(currFTQuery, 0, EnumSet.of(FTQuery.RETURN_IDTABLE), null, 0, 0).collectIds(0, Integer.MAX_VALUE, tableOfFTResult);
						idTable.removeAll(tableOfFTResult);
						tableOfFTResult.dispose();
					}
					else {
						db.queryFTIndex(currFTQuery, 0, EnumSet.of(FTQuery.RETURN_IDTABLE), null, 0, 0).collectIds(0, Integer.MAX_VALUE, idTable);
					}
				}
			}
			
			m_selectedEntriesResolved = idTable;
		}
		return m_selectedEntriesResolved;
	}
	
	@Override
	public void forEachDocument(int skip, int count, BiConsumer<Document, Loop> consumer) {
		JNADominoCollection parentCollection = (JNADominoCollection) getParent();
		JNADatabase parentDb = (JNADatabase) parentCollection.getParentDatabase();
		
		LinkedHashSet<Integer> ids = collectIds(skip, count);
		Stream<Document> docs = ids.stream()
			.map(parentDb::getDocumentById)
			.filter(Optional::isPresent)
			.map(Optional::get);
		
		Iterator<Document> docsIt = docs.iterator();
		
		LoopImpl loop = new LoopImpl();
		
		while (docsIt.hasNext()) {
			Document currDoc = docsIt.next();
			
			if (!docsIt.hasNext()) {
				loop.setIsLast();
			}
			
			consumer.accept(currDoc, loop);
			if (loop.isStopped()) {
				break;
			}
			
			loop.next();
		}
	}

	private static class LoopImpl extends Loop {
		
		public void next() {
			super.setIndex(getIndex()+1);
		}
		
		@Override
		public void setIsLast() {
			super.setIsLast();
		}
	}
}
