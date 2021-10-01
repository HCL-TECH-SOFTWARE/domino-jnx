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
package com.hcl.domino.jna.naming;

import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.internal.ItemDecoder;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNAUserDirectoryQueryIteratorAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.richtext.records.RecordType;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.ShortByReference;

public class JNAUserDirectoryQueryIterator extends BaseJNAAPIObject<JNAUserDirectoryQueryIteratorAllocations> implements Iterator<List<Map<String, List<Object>>>> {
	
	private final List<String> items;
	private Pointer current;
	private Pointer next;
	private long nextMatches;

	public JNAUserDirectoryQueryIterator(IGCDominoClient<?> client, DHANDLE.ByReference phBuffer, List<String> items) {
		super(client);
		this.items = items;
		
		getAllocations().setHBuffer(phBuffer);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected JNAUserDirectoryQueryIteratorAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		return new JNAUserDirectoryQueryIteratorAllocations(parentDominoClient, parentAllocations, this, queue);
	}

	@Override
	public boolean hasNext() {
		LongByReference numMatches = new LongByReference();
		this.next = NotesCAPI.get().NAMELocateNextName2(getAllocations().getValuePointer(), current, numMatches);
		this.nextMatches = numMatches.getValue();
		return this.next != null;
	}

	@Override
	public List<Map<String, List<Object>>> next() {
		Pointer pName;
		LongByReference numMatches = new LongByReference();
		
		if(this.next != null) {
			// Use any hasNext() lookup results first
			pName = this.next;
			numMatches.setValue(nextMatches);
			this.next = null;
		} else {
			pName = NotesCAPI.get().NAMELocateNextName2(getAllocations().getValuePointer(), current, numMatches);
		}
		this.current = pName;
		
		if(pName == null) {
			throw new NoSuchElementException();
		}
		
		// Read in the matches
		List<Map<String, List<Object>>> result = new ArrayList<>();
		Pointer pMatch = null;
		for(long i = 0; i < numMatches.getValue(); i++) {
			pMatch = NotesCAPI.get().NAMELocateNextMatch2(getAllocations().getValuePointer(), pName, pMatch);
			
			// Seek out each value
			Map<String, List<Object>> values = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			for(int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
				String key = items.get(itemIndex);
				ShortByReference retDataType = new ShortByReference();
				ShortByReference retSize = new ShortByReference();
				Pointer data = NotesCAPI.get().NAMELocateItem2(pMatch, (short)itemIndex, retDataType, retSize);
				if(data == null) {
					values.put(key, null);
				} else {
					// Read the data based on its type
					Pointer dataPtr = data.share(2); // Skip the data type word
					int dataLen = Short.toUnsignedInt(retSize.getValue())-2;
					Object val = ItemDecoder.readItemValue(dataPtr, retDataType.getValue(), dataLen, RecordType.Area.TYPE_COMPOSITE);
					if(val instanceof Collection) {
						values.put(key, new ArrayList<>((Collection<?>)val));
					} else {
						values.put(key, new ArrayList<>(Arrays.asList(val)));
					}
				}
			}
			
			result.add(values);
		}
		
		
		return result;
	}

}
