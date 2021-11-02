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

import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import com.hcl.domino.commons.data.AbstractCollectionEntry;
import com.hcl.domino.commons.data.AbstractTypedAccess;
import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.TypedAccess;

/**
 * Implementation of {@link CollectionEntry} that has been read
 * from the summary buffer data of a document
 * 
 * @author Karsten Lehmann
 */
public class JNADocSummaryCollectionEntry extends AbstractCollectionEntry {
	private JNADatabase m_parentDb;
	private JNADominoCollection m_parentCollection;
	private int m_noteId;
	private String m_unid;
	private int m_sequenceNumber;
	private DominoDateTime m_sequenceTime;
	private TreeMap<String,Object> m_values;
	private TypedAccess m_typedAccess;
	private JNADocument m_doc;
	
	public JNADocSummaryCollectionEntry(JNADatabase parentDb, JNADominoCollection parentCollection,
			int noteId, String unid, int seq, DominoDateTime seqTime, TreeMap<String,Object> values) {
		
		m_parentDb = parentDb;
		m_parentCollection = parentCollection;
		m_noteId = noteId;
		m_unid = unid;
		m_sequenceNumber = seq;
		m_sequenceTime = seqTime;
		m_values = values;
		
		m_typedAccess = new AbstractTypedAccess() {
			
			@Override
			public boolean hasItem(String itemName) {
				return JNADocSummaryCollectionEntry.this.hasItem(itemName);
			}
			
			@Override
			public List<String> getItemNames() {
				return JNADocSummaryCollectionEntry.this.getItemNames();
			}
			
			@Override
			protected List<?> getItemValue(String itemName) {
				Object obj = m_values.get(itemName);
				if (obj==null) {
					return null;
				}
				else if (obj instanceof List) {
					return (List<?>) obj;
				}
				else {
					return Arrays.asList(obj);
				}
			}
		};
	}
	
	@Override
	public boolean hasItem(String itemName) {
		return m_values.containsKey(itemName);
	}

	@Override
	public List<String> getItemNames() {
		return new ArrayList<>(m_values.keySet());
	}

	@Override
	public <T> T get(String itemName, Class<T> valueType, T defaultValue) {
		return m_typedAccess.get(itemName, valueType, defaultValue);
	}

	@Override
	public <T> List<T> getAsList(String itemName, Class<T> valueType, List<T> defaultValue) {
		return m_typedAccess.getAsList(itemName, valueType, defaultValue);
	}

	@Override
	public int size() {
		return m_values.size();
	}

	@Override
	public boolean isEmpty() {
		return m_values.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return m_values.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return m_values.containsValue(value);
	}

	@Override
	public Object get(Object key) {
		return m_values.get(key);
	}

	@Override
	public Object put(String key, Object value) {
		return m_values.put(key, value);
	}

	@Override
	public Object remove(Object key) {
		return m_values.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		m_values.putAll(m);
	}

	@Override
	public void clear() {
		m_values.clear();
	}

	@Override
	public Set<String> keySet() {
		return m_values.keySet();
	}

	@Override
	public Collection<Object> values() {
		return m_values.values();
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		return m_values.entrySet();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> clazz) {
		if (Document.class == clazz || JNADocument.class == clazz) {
			return (T) openDocument();
		}
		else {
			return null;
		}
	}

	@Override
	public int getNoteID() {
		return m_noteId;
	}

	@Override
	public String getUNID() {
		return m_unid;
	}

	@Override
	public boolean isCategory() {
		return false;
	}

	@Override
	public boolean isDocument() {
		return true;
	}

	@Override
	public Optional<Document> openDocument() {
		if (m_doc==null || m_doc.isDisposed()) {
			m_doc = (JNADocument) m_parentDb.getDocumentById(m_noteId).orElse(null);
		}
		return Optional.ofNullable(m_doc);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getSpecialValue(SpecialValue value, Class<T> clazz, T defaultValue) {
		switch (value) {
		case CHILDCOUNT:
		case DESCENDANTCOUNT:
		case SIBLINGCOUNT:
		case INDEXPOSITION:
			//not available in this context
			return defaultValue;
		case ANYUNREAD:
		case UNREAD:
		{
			Document doc = openDocument().orElse(null);
			if (doc!=null) {
				boolean isUnread = doc.isUnread();
				if (Boolean.class == clazz) {
					return (T) Boolean.valueOf(isUnread);
				}
				else if (String.class == clazz) {
					return (T) Boolean.toString(isUnread);
				}
			}
			return defaultValue;
		}
		case SEQUENCENUMBER:
			return getSequenceNumber(clazz, defaultValue);
		case SEQUENCETIME:
			return getSequenceTime(clazz, defaultValue);
		default:
			return defaultValue;
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T getSequenceTime(Class<T> clazz, T defaultValue) {
		if (m_sequenceTime!=null) {
			if (DominoDateTime.class == clazz) {
				return (T) m_sequenceTime;
			}
			else if (OffsetDateTime.class == clazz) {
				return (T) m_sequenceTime.toOffsetDateTime();
			}
			else if (Temporal.class == clazz) {
				return (T) m_sequenceTime.toTemporal();
			}
		}
		return defaultValue;
	}

	@SuppressWarnings("unchecked")
	private <T> T getSequenceNumber(Class<T> clazz, T defaultValue) {
		if (Integer.class == clazz) {
			return (T) Integer.valueOf(m_sequenceNumber);
		}
		else if (String.class == clazz) {
			return (T) Integer.toString(m_sequenceNumber);
		}
		return defaultValue;
	}
	
	@Override
	public Optional<DocumentClass> getDocumentClass() {
		return Optional.empty();
	}

	@Override
	public String toString() {
		return MessageFormat.format(
			"JNADocSummaryCollectionEntry [parentdb={0}{1}, noteId={2}, unid={3}, sequenceNumber={4}, sequenceTime={5}, values={6}]", //$NON-NLS-1$
			m_parentDb, (m_parentCollection==null ? "" : ", collection=" + m_parentCollection.getName()), m_noteId, m_unid, m_sequenceNumber, m_sequenceTime, m_values //$NON-NLS-1$ //$NON-NLS-2$
		);
	}

	@Override
	public int getIndexedValueCount() {
		return 0;
	}

	@Override
	public <T> T get(int index, Class<T> valueType, T defaultValue) {
		return defaultValue;
	}

	@Override
	public <T> List<T> getAsList(int index, Class<T> valueType, List<T> defaultValue) {
		return defaultValue;
	}

	@Override
	public <T> Optional<T> getOptional(String itemName, Class<T> valueType) {
	  return m_typedAccess.getOptional(itemName, valueType);
	}
	
	@Override
	public <T> Optional<List<T>> getAsListOptional(String itemName, Class<T> valueType) {
	  return m_typedAccess.getAsListOptional(itemName, valueType);
	}
	
}
