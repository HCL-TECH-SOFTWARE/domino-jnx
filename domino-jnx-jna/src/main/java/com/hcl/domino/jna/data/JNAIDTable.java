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
import java.lang.reflect.Array;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.MessageFormat;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Collectors;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.PlatformUtils;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.JNADominoClient;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.callbacks.NotesCallbacks;
import com.hcl.domino.jna.internal.callbacks.Win32NotesCallbacks;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNAIDTableAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class JNAIDTable extends BaseJNAAPIObject<JNAIDTableAllocations> implements IDTable {
	
	public JNAIDTable(IGCDominoClient<?> parent) {
		this(parent, Collections.emptySet());
	}

	public JNAIDTable(IGCDominoClient<?> parent, Collection<Integer> noteIds) {
		super(parent);
		
		init();
		
		if (!noteIds.isEmpty()) {
			addAll(noteIds);
		}
		
		setInitialized();
	}

	public JNAIDTable(IGCDominoClient<?> parent, DHANDLE handle, boolean noDispose) {
		super(parent);
		
		JNAIDTableAllocations allocations = getAllocations();
		allocations.setIdTableHandle(handle);
		if (noDispose) {
			allocations.setNoDispose();
		}
		
		setInitialized();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected <T> T getAdapterLocal(Class<T> clazz) {
		if (clazz==DHANDLE.class) {
			return (T) getAllocations().getIdTableHandle();
		}
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected JNAIDTableAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {

		return new JNAIDTableAllocations(parentDominoClient, parentAllocations, this, queue);
	}
	
	private void init() {
		DHANDLE.ByReference retHandle = DHANDLE.newInstanceByReference();
		short noteIdLength = NotesCAPI.get().ODSLength((short) 1); //_NOTEID
		
		short result = NotesCAPI.get().IDCreateTable(noteIdLength, retHandle);
		NotesErrorUtils.checkResult(result);
		
		if (retHandle.isNull()) {
			throw new DominoException(0, "Null handle received for id table");
		}
		
		JNAIDTableAllocations allocations = getAllocations();
		allocations.setIdTableHandle(retHandle);
	}

	@Override
	public int size() {
		checkDisposed();
		JNAIDTableAllocations allocations = getAllocations();
		
		int entries = LockUtil.lockHandle(allocations.getIdTableHandle(), (handleByVal) -> {
			return NotesCAPI.get().IDEntries(handleByVal);
		});
		return entries;
	}

	@Override
	public boolean isEmpty() {
		checkDisposed();
		IntByReference retID = new IntByReference();
		boolean first = true;
		boolean hasData = LockUtil.lockHandle(getAllocations().getIdTableHandle(), (handleByVal) -> {
			return NotesCAPI.get().IDScan(handleByVal, first, retID) && retID.getValue()!=0;
		});
		
		return !hasData;
	}

	@Override
	public boolean contains(Object o) {
		if (o instanceof Integer) {
			checkDisposed();
			JNAIDTableAllocations allocations = getAllocations();
			
			return LockUtil.lockHandle(allocations.getIdTableHandle(), (handleByVal) -> {
				return NotesCAPI.get().IDIsPresent(handleByVal, ((Integer)o));
			});
		}

		return false;
	}

	@Override
	public Iterator<Integer> iterator() {
		return new NoteIdIterator(this, false);
	}

	@Override
	public Iterator<Integer> reverseIterator() {
		return new NoteIdIterator(this, true);
	}

	/**
	 * Callback interface for ID table scanning
	 * 
	 * @author Karsten Lehmann
	 */
	private interface IEnumerateCallback {
		public enum Action {Continue, Stop};
		
		/**
		 * Method is called for each ID in the table
		 * 
		 * @param noteId not id
		 * @return either {@link Action#Continue} to go on scanning or {@link Action#Stop}
		 */
		Action noteVisited(int noteId);
		
	}
	
	/**
	 * Traverses the ID table
	 * 
	 * @param callback callback is called for each ID
	 */
	private void enumerate(final IEnumerateCallback callback) {
		checkDisposed();
		JNAIDTableAllocations allocations = getAllocations();
		
		final NotesCallbacks.IdEnumerateProc proc;
		if (PlatformUtils.isWin32()) {
			proc = (Win32NotesCallbacks.IdEnumerateProcWin32) (parameter, noteId) -> {
				IEnumerateCallback.Action result = callback.noteVisited(noteId);
				if (result==IEnumerateCallback.Action.Stop) {
					return INotesErrorConstants.ERR_CANCEL;
				}
				return 0;
			};
		}
		else {
			proc = (parameter, noteId) -> {
				IEnumerateCallback.Action result = callback.noteVisited(noteId);
				if (result==IEnumerateCallback.Action.Stop) {
					return INotesErrorConstants.ERR_CANCEL;
				}
				return 0;
			};
		}
		
		try {
			//AccessController call required to prevent SecurityException when running in XPages
			AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {
				short result = LockUtil.lockHandle(allocations.getIdTableHandle(), (handleByVal) -> {
					return NotesCAPI.get().IDEnumerate(handleByVal, proc, null);
				});
				
				if (result!=INotesErrorConstants.ERR_CANCEL) {
					NotesErrorUtils.checkResult(result);
				}
				return null;
			});
		} catch (PrivilegedActionException e) {
			if (e.getCause() instanceof RuntimeException) {
				throw (RuntimeException) e.getCause();
			} else {
				throw new DominoException(0, "Error enumerating ID table", e);
			}
		}
	}
	
	/**
	 * Converts the content of this id table to a list of Integer
	 * 
	 * @return list
	 */
	private List<Integer> toList(int maxEntries) {
		final List<Integer> idsAsList = new ArrayList<>();

		if (maxEntries > 0) {
			enumerate(noteId -> {
				idsAsList.add(noteId);
				
				if (idsAsList.size() >= maxEntries) {
					return IEnumerateCallback.Action.Stop;
				}
				else {
					return IEnumerateCallback.Action.Continue;
				}
			});
		}

		return idsAsList;
	}
	
	@Override
	public Object[] toArray() {
		List<Integer> idsAsList = toList(Integer.MAX_VALUE);
		Object[] idsArr = new Object[idsAsList.size()];
		
		for (int i=0; i<idsAsList.size(); i++) {
			idsArr[i] = idsAsList.get(i).intValue();
		}
		return idsArr;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		int size = size();
		
		List<Integer> noteIds = toList(Integer.MAX_VALUE);
		
		if (a.length < size) {
			// Make a new array of a's runtime type
			T[] arr = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
			
			for (int i=0; i<noteIds.size(); i++) {
				arr[i] = (T) noteIds.get(i);
			}
			return arr;
		}
		else {
			for (int i=0; i<noteIds.size(); i++) {
				a[i] = (T) noteIds.get(i);
			}
			
			if (a.length > size) {
				a[size] = null;
			}
			
			return a;
		}
	}

	@Override
	public boolean add(Integer noteId) {
		checkDisposed();
		JNAIDTableAllocations allocations = getAllocations();
		
		IntByReference retInserted = new IntByReference();
		short result = LockUtil.lockHandle(allocations.getIdTableHandle(), (handleByVal) -> {
			return NotesCAPI.get().IDInsert(handleByVal, noteId, retInserted);
		});

		NotesErrorUtils.checkResult(result);
		int retInsertedAsInt = retInserted.getValue();
		return retInsertedAsInt != 0;
	}

	@Override
	public boolean remove(Object noteId) {
		if (noteId instanceof Integer) {
			checkDisposed();
			JNAIDTableAllocations allocations = getAllocations();

			IntByReference retDeleted = new IntByReference();
			short result = LockUtil.lockHandle(allocations.getIdTableHandle(), (handleByVal) -> {
				return NotesCAPI.get().IDDelete(handleByVal, (Integer) noteId, retDeleted);
			});
			NotesErrorUtils.checkResult(result);
			
			int retDeletedAsInt = retDeleted.getValue();
			return retDeletedAsInt != 0;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		checkDisposed();
		JNAIDTableAllocations allocations = getAllocations();
		
		JNAIDTable otherIDTable;
		boolean recycleOtherIDTable;
		
		if (c instanceof JNAIDTable) {
			otherIDTable = (JNAIDTable) c;
			otherIDTable.checkDisposed();
			recycleOtherIDTable = false;
		}
		else {
			Collection<Integer> intCol = toIntCollection(c);
			if (intCol.size() != c.size()) {
				return false;
			}
			
			otherIDTable = new JNAIDTable((JNADominoClient) getParent(), intCol);
			recycleOtherIDTable = true;
		}
		
		try {
		JNAIDTableAllocations otherIDTableAllocations = otherIDTable.getAllocations();
		
		//intersect both IDTables and check if the result size matches our current size
		return LockUtil.lockHandles(
				allocations.getIdTableHandle(),
				otherIDTableAllocations.getIdTableHandle(),
				(ourHandleByVal, otherHandleByVal) -> {
					DHANDLE.ByReference rethDstTable = DHANDLE.newInstanceByReference();

					short result = NotesCAPI.get().IDTableIntersect(ourHandleByVal, otherHandleByVal, rethDstTable);
					NotesErrorUtils.checkResult(result);

					int intersectedEntries = LockUtil.lockHandle(rethDstTable, (rethDstTableByVal) -> {
						int numEntries = NotesCAPI.get().IDEntries(rethDstTableByVal);
						NotesCAPI.get().IDDestroyTable(rethDstTableByVal);
						return numEntries;
					});

					return size() == intersectedEntries;
				}
				);

		}
		finally {
			if (recycleOtherIDTable) {
				otherIDTable.dispose();
			}
		}
	}

	@Override
	public boolean addAll(Collection<? extends Integer> c) {
		boolean addToEnd = false;
		if (isEmpty()) {
			addToEnd = true;
		}
		
		return addAll(c, addToEnd);
	}
	
	@SuppressWarnings("unchecked")
	private boolean addAll(Collection<? extends Integer> c, boolean addToEnd) {
		checkDisposed();
		JNAIDTableAllocations allocations = getAllocations();
		
		int oldSize = size();
		
		if (c instanceof JNAIDTable) {
			JNAIDTable otherIDTable = (JNAIDTable) c;
			otherIDTable.checkDisposed();
			
			JNAIDTableAllocations otherIDTableAllocations = otherIDTable.getAllocations();
			if (otherIDTableAllocations==null) {
				throw new DominoException(0, "Unable to read handle of other IDTable");
			}
			short result = LockUtil.lockHandles(
					allocations.getIdTableHandle(),
					otherIDTableAllocations.getIdTableHandle(),
					(ourIDTableHandleByVal, otherIDTableHandleByVal) -> {
						
				return NotesCAPI.get().IDInsertTable(ourIDTableHandleByVal, otherIDTableHandleByVal);
			});
			
			NotesErrorUtils.checkResult(result);
		}
		else {
			//check if Set is already sorted
			boolean isSorted = true;
			if (c instanceof SortedSet && ((SortedSet<Integer>)c).comparator()==null) {
				//set is already sorted by natural ordering, we can skip the following loop to verify ordering
			}
			else {
				Integer lastVal = null;
				Iterator<? extends Integer> idsIt = c.iterator();
				while (idsIt.hasNext()) {
					Integer currVal = idsIt.next();
					if (lastVal!=null && currVal!=null) {
						if (lastVal.intValue() > currVal.intValue()) {
							isSorted = false;
							break;
						}

					}
					lastVal = currVal;
				}
			}
			
			Integer[] noteIdsArr = c.toArray(new Integer[c.size()]);
			if (!isSorted) {
				Arrays.sort(noteIdsArr);
			}
			
			LinkedList<Integer> currIdRange = new LinkedList<>();
			
			LockUtil.lockHandle(
					allocations.getIdTableHandle(),
					(ourIDTableHandleByVal) -> {
						//find consecutive id ranges to reduce number of insert operations (insert ranges)
						for (int i=0; i<noteIdsArr.length; i++) {
							int currNoteId = noteIdsArr[i];
							if (currIdRange.isEmpty()) {
								currIdRange.add(currNoteId);
							}
							else {
								Integer highestRangeId = currIdRange.getLast();
								if (currNoteId == (highestRangeId.intValue() + 4)) {
									currIdRange.add(currNoteId);
								}
								else {
									if (currIdRange.size()==1) {
										add(currIdRange.getFirst());
									}
									else {
										short result = NotesCAPI.get().IDInsertRange(ourIDTableHandleByVal, currIdRange.getFirst(), currIdRange.getLast(), addToEnd);
										NotesErrorUtils.checkResult(result);
									}
									//flush range list
									currIdRange.clear();
									currIdRange.add(currNoteId);
								}
							}
						}
						
						if (!currIdRange.isEmpty()) {
							if (currIdRange.size()==1) {
								add(currIdRange.getFirst());
							}
							else {
								short result = NotesCAPI.get().IDInsertRange(ourIDTableHandleByVal, currIdRange.getFirst(), currIdRange.getLast(), addToEnd);
								NotesErrorUtils.checkResult(result);
							}
						}
						
						return 0;
					});
		
		}

		return oldSize != size();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		Collection<Integer> noteIds = toIntCollection(c);

		int oldSize = size();
		
		if (c.isEmpty()) {
			clear();
			return oldSize > 0;
		}

		JNAIDTable intersectResult = (JNAIDTable) intersect(noteIds);
		clear();
		addAll(intersectResult, true);
		
		return oldSize != size();
	}

	@SuppressWarnings("unchecked")
	private Collection<Integer> toIntCollection(Collection<?> c) {
		if (c.isEmpty() || c instanceof IDTable) {
			return (Collection<Integer>) c;
		}
		
		boolean allInts = true;
		for (Object currObj : c) {
			if (!(currObj instanceof Integer)) {
				allInts = false;
				break;
			}
		}
		
		Collection<Integer> noteIds;
		
		if (!allInts) {
			noteIds = c
			.stream()
			.filter((o) -> { return o instanceof Integer; })
			.mapToInt((o) -> { return (Integer) o; })
			.boxed().collect(Collectors.toList());
			
			if (noteIds.isEmpty()) {
				return new ArrayList<>();
			}
		}
		else {
			noteIds = (Collection<Integer>) c;
		}
		
		return noteIds;
	}
	@Override
	public boolean removeAll(Collection<?> c) {
		checkDisposed();
		JNAIDTableAllocations ourAllocations = getAllocations();
		
		Collection<Integer> noteIds = toIntCollection(c);

		if (c.isEmpty()) {
			return false;
		}

		JNAIDTable otherIDTable;
		boolean disposeOtherIDTable;
		JNAIDTableAllocations otherAllocations;
		
		if (noteIds instanceof JNAIDTable) {
			otherIDTable = (JNAIDTable) noteIds;
			otherIDTable.checkDisposed();
			
			otherAllocations = otherIDTable.getAllocations();
			if (otherAllocations==null) {
				throw new DominoException(0, "Cannot access other IDTable's resources");
			}
			disposeOtherIDTable = false;
		}
		else {
			otherIDTable = new JNAIDTable((JNADominoClient) getParent(), noteIds);
			otherAllocations = (JNAIDTableAllocations) otherIDTable.getAdapter(APIObjectAllocations.class);
			disposeOtherIDTable = true;
		}
		
		int oldSize = size();
		
		try {
			short result = LockUtil.lockHandles(
					ourAllocations.getIdTableHandle(),
					otherAllocations.getIdTableHandle(), (handleByVal, otherAllocationsHandleByVal) -> {
						return NotesCAPI.get().IDDeleteTable(handleByVal, otherAllocationsHandleByVal);
					});
			
			NotesErrorUtils.checkResult(result);

			return oldSize != size();
		}
		finally {
			if (disposeOtherIDTable) {
				otherIDTable.dispose();
			}
		}
	}

	@Override
	public void clear() {
		checkDisposed();
		JNAIDTableAllocations allocations = getAllocations();
		
		short result = LockUtil.lockHandle(allocations.getIdTableHandle(), (handleByVal) -> {
			return NotesCAPI.get().IDDeleteAll(handleByVal);
		});
		NotesErrorUtils.checkResult(result);
	}

	@Override
	public IDTable intersect(Collection<Integer> noteIds) {
		checkDisposed();
		JNAIDTableAllocations ourAllocations = getAllocations();
		
		JNAIDTable otherIDTable;
		boolean disposeOtherIDTable;
		JNAIDTableAllocations otherAllocations;

		if (noteIds instanceof JNAIDTable) {
			otherIDTable = (JNAIDTable) noteIds;
			otherIDTable.checkDisposed();
			otherAllocations = otherIDTable.getAllocations();
			if (otherAllocations==null) {
				throw new DominoException(0, "Cannot access other IDTable's resources");
			}
			disposeOtherIDTable = false;
		}
		else {
			otherIDTable = new JNAIDTable((JNADominoClient) getParent(), noteIds);
			otherAllocations = otherIDTable.getAllocations();
			disposeOtherIDTable = true;
		}
		
		try {
			DHANDLE.ByReference retTableHandle = DHANDLE.newInstanceByReference();

			short result = LockUtil.lockHandles(
					ourAllocations.getIdTableHandle(),
					otherAllocations.getIdTableHandle(),
					(handleByVal, otherAllocationsIDTableHandleByVal) -> {
						
						return NotesCAPI.get().IDTableIntersect(handleByVal,
								otherAllocationsIDTableHandleByVal, retTableHandle);
					});
			
			NotesErrorUtils.checkResult(result);
			
			JNAIDTable retTable = new JNAIDTable((JNADominoClient)getParent(), retTableHandle, false);
			return retTable;
		}
		finally {
			if (disposeOtherIDTable) {
				otherIDTable.dispose();
			}
		}
	}

	@Override
	public boolean isInverted() {
		checkDisposed();
		JNAIDTableAllocations allocations = getAllocations();
		
		return LockUtil.lockHandle(allocations.getIdTableHandle(), (handleByVal) -> {
			Pointer ptr = Mem.OSLockObject(handleByVal);
			
			try {
				short flags = NotesCAPI.get().IDTableFlags(ptr);
				if ((flags & NotesConstants.IDTABLE_INVERTED)==NotesConstants.IDTABLE_INVERTED) {
					return true;
				}
			}
			finally {
				Mem.OSUnlockObject(handleByVal);
			}
			return false;
		});
	}

	@Override
	public void setInverted(boolean inverted) {
		checkDisposed();
		JNAIDTableAllocations allocations = getAllocations();
		
		LockUtil.lockHandle(allocations.getIdTableHandle(), (handleByVal) -> {
			short newFlags = (short) ((isModified() ? NotesConstants.IDTABLE_MODIFIED : 0) + (inverted ? NotesConstants.IDTABLE_INVERTED : 0));
			Pointer ptr = Mem.OSLockObject(handleByVal);

			try {
				NotesCAPI.get().IDTableSetFlags(ptr, newFlags);
			}
			finally {
				Mem.OSUnlockObject(handleByVal);
			}
			return 0;
		});
	}

	@Override
	public boolean isModified() {
		checkDisposed();
		JNAIDTableAllocations allocations = getAllocations();
		
		return LockUtil.lockHandle(allocations.getIdTableHandle(), (handleByVal) -> {
			Pointer ptr = Mem.OSLockObject(handleByVal);
			
			try {
				short flags = NotesCAPI.get().IDTableFlags(ptr);
				if ((flags & NotesConstants.IDTABLE_MODIFIED)==NotesConstants.IDTABLE_MODIFIED) {
					return true;
				}
			}
			finally {
				Mem.OSUnlockObject(handleByVal);
			}

			return false;
		});
	}

	@Override
	public void setModified(boolean modified) {
		checkDisposed();
		JNAIDTableAllocations allocations = getAllocations();

		short newFlags = (short) ((isInverted() ? NotesConstants.IDTABLE_INVERTED : 0) + (modified ? NotesConstants.IDTABLE_MODIFIED : 0));
		
		LockUtil.lockHandle(allocations.getIdTableHandle(), (handleByVal) -> {
			Pointer ptr = Mem.OSLockObject(handleByVal);
			
			try {
				NotesCAPI.get().IDTableSetFlags(ptr, newFlags);
			}
			finally {
				Mem.OSUnlockObject(handleByVal);
			}
			return 0;
		});
	}

	@Override
	public Optional<DominoDateTime> getDateTime() {
		checkDisposed();
		JNAIDTableAllocations allocations = getAllocations();
		
		return LockUtil.lockHandle(allocations.getIdTableHandle(), (handleByVal) -> {
			Pointer ptr = Mem.OSLockObject(handleByVal);
			
			try {
				NotesTimeDateStruct timeStruct = NotesCAPI.get().IDTableTime(ptr);
				return Optional.ofNullable(timeStruct==null ? null : new JNADominoDateTime(timeStruct));
			}
			finally {
				Mem.OSUnlockObject(handleByVal);
			}
		});
	}

	@Override
	public void setDateTime(TemporalAccessor dt) {
		checkDisposed();
		JNAIDTableAllocations allocations = getAllocations();
		
		int[] dtInnards = JNADominoDateTime.from(dt).getInnards();
		if (dtInnards==null) {
			throw new DominoException(0, "Unable to read datetime innards");
		}

		NotesTimeDateStruct timeStruct = dt==null ? null : NotesTimeDateStruct.newInstance(dtInnards);
		
		LockUtil.lockHandle(allocations.getIdTableHandle(), (handleByVal) -> {
			Pointer ptr = Mem.OSLockObject(handleByVal);
			
			try {
				NotesCAPI.get().IDTableSetTime(ptr, timeStruct);
			}
			finally {
				Mem.OSUnlockObject(handleByVal);
			}
			return 0;
		});
	}
	
	private static class NoteIdIterator implements Iterator<Integer> {
		private JNAIDTable m_idTable;
		private IntByReference m_nextIdRef;
		private Integer m_nextId;
		private boolean m_scanBackward;
		
		private NoteIdIterator(JNAIDTable idTable, boolean scanBackward) {
			m_idTable = idTable;
			m_scanBackward = scanBackward;
			
			fetchNext();
		}
		
		private void fetchNext() {
			JNAIDTableAllocations allocations = m_idTable.getAllocations();
			allocations.checkDisposed();
			
			boolean isFirstVal;
			
			if (m_nextIdRef==null) {
				//first id
				isFirstVal = true;
				m_nextIdRef = new IntByReference();
			}
			else if (m_nextId==null) {
				//no more data
				return;
			}
			else {
				isFirstVal = false;
				m_nextIdRef.setValue(m_nextId);
			}
			
			LockUtil.lockHandle(allocations.getIdTableHandle(), (handleByVal) -> {
				if (m_scanBackward) {
					if (NotesCAPI.get().IDScanBack(handleByVal, isFirstVal, m_nextIdRef)) {
						m_nextId = m_nextIdRef.getValue();
					}
					else {
						m_nextId = null;
					}
				}
				else {
					if (NotesCAPI.get().IDScan(handleByVal, isFirstVal, m_nextIdRef)) {
						m_nextId = m_nextIdRef.getValue();
					}
					else {
						m_nextId = null;
					}
				}
				return 0;
			});
		}
		
		@Override
		public boolean hasNext() {
			return m_nextId!=null;
		}

		@Override
		public Integer next() {
			if (m_nextId==null) {
				throw new NoSuchElementException("No more elements");
			}
			
			Integer nextId = m_nextId;
			fetchNext();
			
			return nextId;
		}
		
	}

	/**
	 * Returns the first ID in the table
	 * 
	 * @return ID
	 * @throws DominoException with {@link INotesErrorConstants#ERR_IDTABLE_LENGTH_MISMATCH} if ID table is empty
	 */
	public int getFirstId() {
		checkDisposed();
		
		JNAIDTableAllocations allocations = getAllocations();
		return LockUtil.lockHandle(allocations.getIdTableHandle(), (handleByVal) -> {
			IntByReference retID = new IntByReference();
			
			if (NotesCAPI.get().IDScan(handleByVal, true, retID)) {
				return retID.getValue();
			}
			else {
				throw new DominoException(INotesErrorConstants.ERR_IDTABLE_LENGTH_MISMATCH, "ID table is empty");
			}
		});
	}

	/**
	 * Returns the last ID in the table
	 * 
	 * @return ID
	 * @throws DominoException with {@link INotesErrorConstants#ERR_IDTABLE_LENGTH_MISMATCH} if ID table is empty
	 */
	public int getLastId() {
		checkDisposed();
		
		JNAIDTableAllocations allocations = getAllocations();
		return LockUtil.lockHandle(allocations.getIdTableHandle(), (handleByVal) -> {
			IntByReference retID = new IntByReference();
			
			if (NotesCAPI.get().IDScanBack(handleByVal, true, retID)) {
				return retID.getValue();
			}
			else {
				throw new DominoException(INotesErrorConstants.ERR_IDTABLE_LENGTH_MISMATCH, "ID table is empty");
			}
		});
	}
	
	@Override
	public int[] toIntArray() {
		checkDisposed();
		
		int size = size();
		int[] result = new int[size];
		int[] i = new int[] { 0 };
		enumerate(noteId -> {
			result[i[0]++] = noteId;
			return IEnumerateCallback.Action.Continue;
		});
		
		return result;
	}
	
	/**
	 * Creates a new ID table with the IDs of this table, but with high order
	 * bit set (0x80000000L).
	 * 
	 * @return ID table
	 */
	public JNAIDTable withHighOrderBit() {
		List<Integer> ids = toList(Integer.MAX_VALUE);
		
		for (int i=0; i<ids.size(); i++) {
			long currId = ids.get(i) | NotesConstants.NOTEID_RESERVED;
			ids.set(i, (int) (currId & 0xffffffffL));
		}
		
		return new JNAIDTable(getParentDominoClient(), ids);
	}

	@Override
	public Object clone() {
		checkDisposed();
		
		JNAIDTableAllocations allocations = getAllocations();
		
		return LockUtil.lockHandle(allocations.getIdTableHandle(), (handleByVal) -> {
			DHANDLE.ByReference rethTable = DHANDLE.newInstanceByReference();
			
			short result = NotesCAPI.get().IDTableCopy(handleByVal, rethTable);
			NotesErrorUtils.checkResult(result);
			return new JNAIDTable(getParentDominoClient(), rethTable, false);
		});
	}
	
	@Override
	protected String toStringLocal() {
		if (isDisposed()) {
			return "JNAIDTable [disposed]"; //$NON-NLS-1$
		}
		else {
			return MessageFormat.format(
				"JNAIDTable [handle={0}, inverted={1}, {2} entries]", //$NON-NLS-1$
				getAllocations().getIdTableHandle(), isInverted(), size()
			);
		}
	}
}
