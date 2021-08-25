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

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.hcl.domino.commons.data.AbstractTypedAccess;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.commons.views.ReadMask;
import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.CollectionEntryValueConverter;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Database.DocInfo;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.jna.internal.LMBCSString;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.JNXServiceFinder;
import com.hcl.domino.misc.NotesConstants;

/**
 * Data object that contains all data read from collection entries
 * 
 * @author Karsten Lehmann
 */
public class JNACollectionEntry implements CollectionEntry {
	private JNADominoCollection m_parentCollection;
	private int[] m_pos;
	private String m_posStr;
	private Integer m_noteId;
	private String m_unid;
	private long[] m_unidAsLongs;
	private Integer m_noteClass;
	private Integer m_siblingCount;
	private Integer m_childCount;
	private Integer m_descendantCount;
	private Boolean m_isAnyUnread;
	private Integer m_indentLevels;
	private Integer m_ftScore;
	private Boolean m_isUnread;
	private Object[] m_columnValues;
	private int[] m_columnValueSizes;
	private Map<String, Object> m_summaryData;
	private SoftReference<Map<String, Object>> m_convertedDataRef;
	private String m_singleColumnLookupName;
	private AbstractTypedAccess m_typedAccess;
	private ThreadLocal<Boolean> readingItemType = new ThreadLocal<>();
	
	private Integer m_sequenceNumber;
	private DominoDateTime m_sequenceTime;
	
	/**
	 * Creates a new instance
	 * 
	 * @param parentCollection parent notes collection
	 */
	public JNACollectionEntry(JNADominoCollection parentCollection) {
		m_parentCollection = parentCollection;
		
		m_typedAccess = new AbstractTypedAccess() {
			
			@Override
			public boolean hasItem(String itemName) {
				return JNACollectionEntry.this.hasItem(itemName);
			}

			@Override
			public List<String> getItemNames() {
				return JNACollectionEntry.this.getItemNames();
			}
			
			@Override
			protected List<?> getItemValue(String itemName) {
				return JNACollectionEntry.this.getItemValue(itemName);
			}
			
			@Override
			public int getIndexedValueCount() {
				return m_columnValues == null ? 0 : m_columnValues.length;
			}
			
			@Override
			protected <T> T getViaValueConverter(String itemName, Class<T> valueType, T defaultValue) {
				return tryWithConverters(itemName, valueType, defaultValue,
					converter -> converter.getValue(JNACollectionEntry.this, itemName, valueType, defaultValue)
				);
			}
			
			@Override
			protected <T> List<T> getAsListViaValueConverter(String itemName, Class<T> valueType, List<T> defaultValue) {
				return tryWithConverters(itemName, valueType, defaultValue,
					converter -> converter.getValueAsList(JNACollectionEntry.this, itemName, valueType, defaultValue)
				);
			}
			
			@Override
			protected List<?> getItemValue(int index) {
				if(m_columnValues == null) {
					return null;
				} else {
					return cleanValue(m_columnValues[index]);
				}
			}
			
			@Override
			protected <T> T getViaValueConverter(int index, Class<T> valueType, T defaultValue) {
				return tryWithConverters(index, valueType, defaultValue,
					converter -> converter.getValue(JNACollectionEntry.this, index, valueType, defaultValue)
				);
			}
			
			@Override
			protected <T> List<T> getAsListViaValueConverter(int index, Class<T> valueType, List<T> defaultValue) {
				return tryWithConverters(index, valueType, defaultValue,
					converter -> converter.getValueAsList(JNACollectionEntry.this, index, valueType, defaultValue)
				);
			}
			
			private <IDENT, TYPE, RESULT> RESULT tryWithConverters(IDENT itemIdentifier, Class<TYPE> valueType, RESULT defaultValue, Function<CollectionEntryValueConverter, RESULT> supplier) {
				CollectionEntryValueConverter converter = JNXServiceFinder.findServices(CollectionEntryValueConverter.class)
					.filter(c -> c.supportsRead(valueType))
					.sorted(Comparator.comparing(CollectionEntryValueConverter::getPriority).reversed())
					.findFirst()
					.orElse(null);

				if (converter!=null) {
					if (Boolean.TRUE.equals(readingItemType.get())) {
						throw new IllegalStateException(
							MessageFormat.format(
								"Infinite loop detected reading the value of item {0} as type {1}",
								itemIdentifier, valueType.getName()
							)
						);
					}
					readingItemType.set(Boolean.TRUE);
					try {
						return supplier.apply(converter);
					}
					finally {
						readingItemType.set(null);
					}
				}
				else {
					throw new IllegalArgumentException(MessageFormat.format("Unsupported return value type: {0}", valueType.getName()));
				}
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> clazz) {
		if (JNADocument.class.equals(clazz) || Document.class.equals(clazz)) {
			return (T) openDocument();
		}

		return null;
	}

	class CacheableViewEntryData implements Serializable {
		private static final long serialVersionUID = -6919729244434994355L;
		
		private int[] m_pos;
		private String m_posStr;
		private Integer m_noteId;
		private String m_unid;
		private long[] m_unidAsLongs;
		private Integer m_noteClass;
		private Integer m_siblingCount;
		private Integer m_childCount;
		private Integer m_descendantCount;
		private Boolean m_isAnyUnread;
		private Integer m_indentLevels;
		private Integer m_ftScore;
		private Boolean m_isUnread;
		private Object[] m_columnValues;
		private int[] m_columnValueSizes;
		private Map<String, Object> m_summaryData;
		private transient SoftReference<Map<String, Object>> m_convertedDataRef;
		private String m_singleColumnLookupName;
	}
	
	/**
	 * Method to read the cacheable and serializable data from this object
	 * 
	 * @return data
	 */
	CacheableViewEntryData getCacheableData() {
		CacheableViewEntryData data = new CacheableViewEntryData();
		data.m_pos = m_pos;
		data.m_posStr = m_posStr;
		data.m_noteId = m_noteId;
		data.m_unid = m_unid;
		data.m_unidAsLongs = m_unidAsLongs;
		data.m_noteClass = m_noteClass;
		data.m_siblingCount = m_siblingCount;
		data.m_childCount = m_childCount;
		data.m_descendantCount = m_descendantCount;
		data.m_isAnyUnread = m_isAnyUnread;
		data.m_indentLevels = m_indentLevels;
		data.m_ftScore = m_ftScore;
		data.m_isUnread = m_isUnread;
		data.m_columnValues = m_columnValues;
		data.m_columnValueSizes = m_columnValueSizes;
		data.m_summaryData = m_summaryData;
		data.m_convertedDataRef = m_convertedDataRef;
		data.m_singleColumnLookupName = m_singleColumnLookupName;
		return data;
	}
	
	/**
	 * Method to update the internal state from a cache entry
	 * 
	 * @param data cache entry data
	 */
	void updateFromCache(CacheableViewEntryData data) {
		if (m_noteId.intValue()!=data.m_noteId.intValue()) {
			throw new IllegalArgumentException(MessageFormat.format("Note ids do not match: {0} != {1}", m_noteId, data.m_noteId));
		}
		
		m_pos = data.m_pos;
		m_posStr = data.m_posStr;
		m_noteId = data.m_noteId;
		m_unid = data.m_unid;
		m_unidAsLongs = data.m_unidAsLongs;
		m_noteClass = data.m_noteClass;
		m_siblingCount = data.m_siblingCount;
		m_childCount = data.m_childCount;
		m_descendantCount = data.m_descendantCount;
		m_isAnyUnread = data.m_isAnyUnread;
		m_indentLevels = data.m_indentLevels;
		m_ftScore = data.m_ftScore;
		m_isUnread = data.m_isUnread;
		m_columnValues = data.m_columnValues;
		m_columnValueSizes = data.m_columnValueSizes;
		m_summaryData = data.m_summaryData;
		m_convertedDataRef = data.m_convertedDataRef;
		m_singleColumnLookupName = data.m_singleColumnLookupName;
	}
	
	/**
	 * Returns the parent collection
	 * 
	 * @return parent collection
	 */
	public JNADominoCollection getParent() {
		return m_parentCollection;
	}
	
	/**
	 * Method to check whether an entry is a conflict document. Can only returns a true value
	 * if {@link ReadMask#SUMMARYVALUES} or {@link ReadMask#SUMMARY} is used for the lookup.
	 * 
	 * @return true if conflict
	 */
	public boolean isConflict() {
		//C API documentation regarding conflict flags in views
		//VIEW_TABLE_FLAG_CONFLICT	  -  Replication conflicts will be flagged. If TRUE, the '$Conflict' item must be SECOND-TO-LAST in the list of summary items for this view.
		if (m_columnValues!=null) {
			if (!m_parentCollection.isConflict()) {
				return false;
			}
			else if (m_parentCollection.isHierarchical()) {
				return m_columnValues.length>=2 && m_columnValues[m_columnValues.length-2] != null;
			}
			else {
				//special case for views which have "show response hierarchy" = false:
				//here the response column value is missing
				return m_columnValues.length>=1 && m_columnValues[m_columnValues.length-1] != null;
			}
		}
		else if (m_summaryData!=null) {
			return m_summaryData.get("$Conflict") != null; //$NON-NLS-1$
		}
		return false;
	}

	/**
	 * Method to check whether an entry is a conflict document. Can only returns a true value
	 * if {@link ReadMask#SUMMARYVALUES} or {@link ReadMask#SUMMARY} is used for the lookup.
	 * 
	 * @return true if response
	 */
	public boolean isResponse() {
		//C API documentation regarding response flags in views
		//VIEW_TABLE_FLAG_FLATINDEX	  -  Do not index hierarchically If FALSE, the '$REF' item must be LAST in the list of summary items for this view.
		if (m_columnValues!=null) {
			if (m_parentCollection.isHierarchical()) {
				return m_columnValues.length>=1 && m_columnValues[m_columnValues.length-1] != null;
			}
			else {
				//fallback to isConflict as this is the only info we have
				return isConflict();
			}
		}
		else if (m_summaryData!=null) {
			return m_summaryData.get("$Ref") != null; //$NON-NLS-1$
		}
		return false;
	}

	/**
	 * Returns the level of the entry in the view (position 1 = level 0, position 1.1 = level 1)
	 * 
	 * @return level, only available when position is loaded, otherwise the method returns -1
	 */
	public int getLevel() {
		return m_pos!=null ? (m_pos.length-1) : -1;
	}
	
	/**
	 * Returns the entry position in the view as an int[] array (e.g. [1,2,3])
	 * or a string (e.g. "1.2.3"). Only returns a non-null value if
	 * {@link ReadMask#INDEXPOSITION} is used for the lookup.
	 * 
	 * @param clazz requested return value class
	 * @param defaultValue default value is returned if index position has not been read or class is unsupported
	 * @return position or default value
	 */
	@SuppressWarnings("unchecked")
	private <T> T getPosition(Class<T> clazz, T defaultValue) {
		if (int[].class == clazz) {
			if (m_pos!=null) {
				return (T) m_pos;
			}
			else {
				return defaultValue;
			}
		}
		else if (String.class == clazz) {
			if (m_pos!=null && m_pos.length>0) {
				if (m_posStr==null) {
					StringBuilder sb = new StringBuilder();
					for (int i=0; i<m_pos.length; i++) {
						if (i>0) {
							sb.append("."); //$NON-NLS-1$
						}
						sb.append(m_pos[i]);
					}
					m_posStr = sb.toString();
				}
				return (T) m_posStr;
			}
			else {
				return defaultValue;
			}
		}
		
		return null;
	}
	
	/**
	 * Sets the position
	 * 
	 * @param pos new position
	 */
	public void setPosition(int[] pos) {
		m_pos = pos;
	}
	
	/**
	 * Sets the note id
	 * 
	 * @param noteId note id
	 */
	public void setNoteID(int noteId) {
		m_noteId = noteId;
	}

	/**
	 * Method to check whether the entry is a document. Only returns a value if {@link ReadMask#NOTEID}
	 * is used for the lookup
	 * 
	 * @return true if document
	 */
	@Override
	public boolean isDocument() {
		return !isCategory() && !isTotal();
	}

	/**
	 * Method to check whether the entry is a category. Only returns a value if {@link ReadMask#NOTEID}
	 * is used for the lookup
	 * 
	 * @return true if category
	 */
	@Override
	public boolean isCategory()  {
		if (m_noteId!=null) {
			return (m_noteId.intValue() & NotesConstants.NOTEID_CATEGORY) == NotesConstants.NOTEID_CATEGORY;
		}
		return false;
	}

	/**
	 * Method to check whether the entry is a total value. Only returns a value if {@link ReadMask#NOTEID}
	 * is used for the lookup
	 * 
	 * @return true if total
	 */
	public boolean isTotal() {
		if (m_noteId!=null) {
			return (m_noteId.intValue() & NotesConstants.NOTEID_CATEGORY_TOTAL) == NotesConstants.NOTEID_CATEGORY_TOTAL;
		}
		return false;
	}

	/**
	 * Returns the UNID of the entry. Only returns a value if {@link ReadMask#NOTEUNID}
	 * is used for the lookup
	 * 
	 * @return UNID or null
	 */
	@Override
	public String getUNID() {
		if (m_unid==null) {
			if (m_unidAsLongs!=null) {
				m_unid = NotesStringUtils.toUNID(m_unidAsLongs[0], m_unidAsLongs[1]);
			}
		}
		return m_unid;
	}

	/**
	 * Sets the UNID
	 * 
	 * @param unid UNID
	 */
	public void setUNID(String unid) {
		m_unid = unid;
	}

	/**
	 * Sets the UNID as a long array
	 * 
	 * @param unidAsLongs long array with file / note timedates
	 */
	public void setUNID(long[] unidAsLongs) {
		m_unidAsLongs = unidAsLongs;
	}
	
	/**
	 * Returns the entry's note class. Only returns a value if {@link ReadMask#NOTECLASS}
	 * is used for the lookup
	 * 
	 * @return the document class
	 */
	@Override
	public Optional<DocumentClass> getDocumentClass() {
		return Optional.ofNullable(m_noteClass)
			.map(c -> DominoEnumUtil.valueOf(DocumentClass.class, c))
			.orElse(Optional.empty());
	}

	/**
	 * Sets the note class
	 * 
	 * @param noteClass note class
	 */
	public void setNoteClass(int noteClass) {
		m_noteClass = noteClass;
	}

	/**
	 * Returns the sibling count. Only returns a value if {@link ReadMask#INDEXSIBLINGS}
	 * is used for the lookup
	 * 
	 * @param clazz requested return value class
	 * @param defaultValue default value is returned if count has not been read or class is unsupported
	 * @return count or 0
	 */
	@SuppressWarnings("unchecked")
	private <T> T getSiblingCount(Class<T> clazz, T defaultValue) {
		if (Integer.class == clazz) {
			if (m_siblingCount!=null) {
				return (T) m_siblingCount;
			}
			else {
				return defaultValue;
			}
		}
		else if (String.class == clazz) {
			if (m_siblingCount!=null) {
				return (T) m_siblingCount.toString();
			}
			else {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	/**
	 * Sets the sibling count
	 * 
	 * @param siblingCount count
	 */
	public void setSiblingCount(int siblingCount) {
		m_siblingCount = siblingCount;
	}

	/**
	 * Returns the sibling count. Only returns a value if {@link ReadMask#INDEXCHILDREN}
	 * is used for the lookup
	 * 
	 * @param clazz requested return value class
	 * @param defaultValue default value is returned if count has not been read or class is unsupported
	 * @return count or default value
	 */
	@SuppressWarnings("unchecked")
	private <T> T getChildCount(Class<T> clazz, T defaultValue) {
		if (Integer.class == clazz) {
			if (m_childCount!=null) {
				return (T) m_childCount;
			}
			else {
				return defaultValue;
			}
		}
		else if (String.class == clazz) {
			if (m_childCount!=null) {
				return (T) m_childCount.toString();
			}
			else {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	/**
	 * Sets the child count
	 * 
	 * @param childCount count
	 */
	public void setChildCount(int childCount) {
		m_childCount = childCount;
	}

	/**
	 * Returns the descendant count. Only returns a value if {@link ReadMask#INDEXDESCENDANTS}
	 * is used for the lookup
	 * 
	 * @param clazz requested return value class
	 * @param defaultValue default value is returned if count has not been read or class is unsupported
	 * @return count or default value
	 */
	@SuppressWarnings("unchecked")
	private <T> T getDescendantCount(Class<T> clazz, T defaultValue) {
		if (Integer.class == clazz) {
			if (m_descendantCount!=null) {
				return (T) m_descendantCount;
			}
			else {
				return defaultValue;
			}
		}
		else if (String.class == clazz) {
			if (m_descendantCount!=null) {
				return (T) m_descendantCount.toString();
			}
			else {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	/**
	 * Sets the descendant count
	 * 
	 * @param descendantCount count
	 */
	public void setDescendantCount(int descendantCount) {
		m_descendantCount = descendantCount;
	}

	/**
	 * Returns true if the entry is unread. Only returns a value if {@link ReadMask#INDEXUNREAD}
	 * is used for the lookup
	 * 
	 * @param clazz requested return value class
	 * @param defaultValue default value is returned if unread state has not been read or class is unsupported
	 * @return unread
	 */
	@SuppressWarnings("unchecked")
	private <T> T isUnread(Class<T> clazz, T defaultValue) {
		if (m_isUnread!=null) {
			if (Boolean.class == clazz) {
				return (T) m_isUnread;
			}
			else if (String.class == clazz) {
				return (T) m_isUnread.toString();
			}
		}
		
		return defaultValue;
	}

	/**
	 * Sets the unread flag
	 * 
	 * @param isUnread flag
	 */
	public void setUnread(boolean isUnread) {
		m_isUnread = isUnread;
	}

	/**
	 * Returns the indent levels in the view. Only returns a value if {@link ReadMask#INDENTLEVELS}
	 * is used for the lookup
	 * 
	 * @return levels or 0
	 */
	public int getIndentLevels() {
		return m_indentLevels!=null ? m_indentLevels : 0;
	}

	/**
	 * Sets the indent levels
	 * 
	 * @param indentLevels levels
	 */
	public void setIndentLevels(int indentLevels) {
		m_indentLevels = indentLevels;
	}

	/**
	 * Returns the fulltext search score. Only returns a value if {@link ReadMask#SCORE}
	 * is used for the lookup
	 * 
	 * @return score or 0
	 */
	public int getFTScore() {
		return m_ftScore!=null ? m_ftScore : 0;
	}

	/**
	 * Sets the fulltext search score
	 * 
	 * @param ftScore score
	 */
	public void setFTScore(int ftScore) {
		m_ftScore = ftScore;
	}

	/**
	 * Returns a flag whether the entry or any descendents are unread. Only returns a value if {@link ReadMask#INDEXANYUNREAD}
	 * is used for the lookup
	 * 
	 * @param clazz requested return value class
	 * @param defaultValue default value is returned if unread state has not been read or class is unsupported
	 * @return unread
	 */
	@SuppressWarnings("unchecked")
	private <T> T isAnyUnread(Class<T> clazz, T defaultValue) {
		if (m_isAnyUnread!=null) {
			if (Boolean.class == clazz) {
				return (T) m_isAnyUnread;
			}
			else if (String.class == clazz) {
				return (T) m_isAnyUnread.toString();
			}
		}
		
		return defaultValue;
	}

	/**
	 * Sets the any unread flag
	 * 
	 * @param isAnyUnread flag
	 */
	public void setAnyUnread(boolean isAnyUnread) {
		m_isAnyUnread = isAnyUnread;
	}

	/**
	 * Sets the collection entry column values.
	 * 
	 * @param itemValues new values
	 */
	public void setColumnValues(Object[] itemValues) {
		m_columnValues = itemValues;
	}
	
	public Object[] getColumnValues() {
		return m_columnValues;
	}
	
	/**
	 * Returns an iterator of all available columns for which we can read column values
	 * (e.g. does not return static column names).
	 * 
	 * @return programmatic column names converted to lowercase
	 */
	public List<String> getColumnNames() {
		if ((m_parentCollection.getNoteId() & NotesConstants.NOTE_ID_SPECIAL) == NotesConstants.NOTE_ID_SPECIAL) {
			//special collection (e.g. design collection) where we cannot read the column names from the design element
			if (m_summaryData!=null) {
				//if we have used ReadMask.SUMMARY to read the data, we can take the summary map keys
				return new ArrayList<>(m_summaryData.keySet());
			}
			else {
				return Collections.emptyList();
			}
		}
		else {
			return m_parentCollection.getColumnNames();
		}
	}

	@Override
	public List<String> getItemNames() {
		return getColumnNames();
	}
	
	/**
	 * Returns the number of columns for which we can read column data (e.g. does not count columns
	 * with static values)
	 * 
	 * @return number of columns
	 */
	public int getNumberOfColumnsWithValues() {
		return m_parentCollection.getNumberOfColumns();
	}
	
	/**
	 * Converts the column values to a map. If you are only interested in specific columns,
	 * you get way better performance calling {@link #get(String)} for those columns
	 * directly, because we lazily convert text/text list column data from LMBCS format to Java String format.<br>
	 * Calling this method converts all string columns at once.
	 * 
	 * @return map with programmatic column names as keys
	 */
	private Map<String,Object> getColumnDataAsMap() {
		Map<String,Object> data = m_convertedDataRef==null ? null : m_convertedDataRef.get();
		if (data==null) {
			data = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			
			List<String> colNames = getColumnNames();
			
			for (String currColName : colNames) {
				List<Object> currColValue = getItemValue(currColName);
				if(currColValue != null) {
					currColValue = currColValue.stream()
						.map(v -> {
							if (v instanceof GregorianCalendar) {
								return new JNADominoDateTime(((GregorianCalendar) v).toZonedDateTime());
							} else if (v instanceof Date) {
								return new JNADominoDateTime(((Date) v).getTime());
							} else {
								return v;
							}
						})
						.collect(Collectors.toList());
				}
				
				if(currColValue != null && currColValue.size() == 1) {
					data.put(currColName, currColValue.get(0));
				} else {
					data.put(currColName, currColValue);
				}
			}
			m_convertedDataRef = new SoftReference<>(data);
		}
		return data;
	}
	
	/**
	 * Returns a list of reader that are allowed to see this view entry.
	 * This data is only retrieved when {@link ReadMask#SUMMARY} and
	 * {@link ReadMask#RETURN_READERSLIST} are both used to read the
	 * collection data.
	 * 
	 * @return an {@link Optional} representing the readers or an empty one
	 *      if this was not read
	 */
	@SuppressWarnings("unchecked")
	public Optional<List<String>> getReadersList() {
		Object readersList = get("$C1$"); //$NON-NLS-1$
		if (readersList instanceof List) {
			return Optional.of((List<String>) readersList);
		}
		else if (readersList instanceof String) {
			return Optional.of(Arrays.asList((String) readersList));
		}
		else {
			return Optional.empty();
		}
	}
	
	/**
	 * Method to check whether the the view entry contains a non-null value for a programmatic column
	 * name
	 * 
	 * @param columnName column name
	 * @return true if exists
	 */
	@Override
	public boolean hasItem(String columnName) {
		if (m_summaryData!=null) {
			return m_summaryData.containsKey(columnName);
		}
		else if (m_columnValues!=null) {
			int colIdx = m_parentCollection.getColumnValuesIndex(columnName);
			if (colIdx==-1) {
				return false;
			}
			else {
				return m_columnValues[colIdx] != null;
			}
		}
		else {
			return false;
		}
	}

	/**
	 * Returns a column value. Only returns data of either {@link ReadMask#SUMMARY} or {@link ReadMask#SUMMARYVALUES}
	 * was used to read the collection data
	 * <br>
	 * The following data types are returned for the different column data types:<br>
	 * <ul>
	 * <li>{@link NotesItem#TYPE_TEXT} - {@link String}</li>
	 * <li>{@link NotesItem#TYPE_TEXT_LIST} - {@link List} of {@link String}</li>
	 * <li>{@link NotesItem#TYPE_NUMBER} - {@link Double}</li>
	 * <li>{@link NotesItem#TYPE_NUMBER_RANGE} - {@link List} with {@link Double} values for number lists or double[] values for number ranges (not sure if Notes views really supports them)</li>
	 * <li>{@link NotesItem#TYPE_TIME} - {@link Calendar} or {@link DominoDateTime} if {@link #setPreferNotesTimeDates(boolean)} has been called</li>
	 * <li>{@link NotesItem#TYPE_TIME_RANGE} - {@link List} with {@link Calendar} values for datetime lists or Calendar[] values for datetime ranges {@link DominoDateTime} if {@link #setPreferNotesTimeDates(boolean)} has been called</li>
	 * </ul>
	 * 
	 * @param columnNameOrTitle programatic column name or column title
	 * @return column value or null
	 */
	private List<Object> getItemValue(String columnNameOrTitle) {
		Object val = null;
		
		if (m_summaryData!=null) {
			if (m_summaryData.containsKey(columnNameOrTitle)) {
				val = m_summaryData.get(columnNameOrTitle);
			}
			else {
				//try to find the programmatic column name if columnNameOrTitle contains the column title
				int colIdx = m_parentCollection.getColumnValuesIndex(columnNameOrTitle);
				if (colIdx!=-1 && colIdx!=65535) {
					String progColName = m_parentCollection.getColumnName(colIdx);
					if (progColName!=null) {
						val = m_summaryData.get(progColName);
					}
				}
				
			}
		}
		else if (m_columnValues!=null) {
			int colIdx = m_parentCollection.getColumnValuesIndex(columnNameOrTitle);
			if (colIdx!=-1 && colIdx!=65535) {
				if (colIdx < m_columnValues.length) {
					val = m_columnValues[colIdx];
				}
				else {
					val = null;
				}
			}
		}
		
		return cleanValue(val);
	}

	@SuppressWarnings("unchecked")
	private List<Object> cleanValue(Object val) {
		if (val instanceof List) {
			List<Object> valAsList = (List<Object>) val;
			for (int i=0; i<valAsList.size(); i++) {
				Object currListValue = valAsList.get(i);
				
				if (currListValue instanceof LMBCSString) {
					valAsList.set(i, ((LMBCSString)currListValue).getValue());
				}
			}
		}
		else if (val instanceof LMBCSString) {
			val = ((LMBCSString)val).getValue();
		}
		
		if (val instanceof List) {
			return (List<Object>) val;
		}
		else {
			return Arrays.asList(val);
		}
	}
	
	/**
	 * Convenience method to check whether there are any column values stored in this entry
	 * 
	 * @return true if we have column values
	 */
	public boolean hasAnyColumnValues() {
		if (m_summaryData!=null) {
			if (!m_summaryData.isEmpty()) {
				return true;
			}
		}
		if (m_columnValues!=null) {
			for (int i=0; i<m_columnValues.length; i++) {
				if (m_columnValues[i]!=null) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Sets the sizes in bytes of the collection entry column values
	 * 
	 * @param sizes sizes
	 */
	public void setColumnValueSizesInBytes(int[] sizes) {
		m_columnValueSizes = sizes;
	}
	
	/**
	 * Returns the sizes in bytes of collection entry column values. Can be used for performance optimization
	 * to analyze which columns of a view fill the summary buffer the most. Only returns a non-null value if
	 * {@link ReadMask#SUMMARYVALUES} is used for the lookup.
	 * 
	 * @return sizes or null
	 */
	public int[] getColumnValueSizesInBytes() {
		return m_columnValueSizes;
	}
	
	/**
	 * Sets the summary map data
	 * 
	 * @param summaryData new data
	 */
	public void setSummaryData(Map<String,Object> summaryData) {
		m_summaryData = summaryData;
	}
	
	/**
	 * If this view entry data was received by an optimized lookup that read only one column, you
	 * can use this method to get the programmatic name of the collection column. The method
	 * is mainly used for collection data caching purposes.
	 * 
	 * @return column name or null
	 */
	public String getSingleColumnLookupName() {
		return m_singleColumnLookupName;
	}
	
	/**
	 * If this view entry data was received by an optimized lookup that read only one column,
	 * this method is used to set the programmatic name of that column.
	 * 
	 * @param colName column name or null
	 */
	public void setSingleColumnLookupName(String colName) {
		m_singleColumnLookupName = colName;
	}

	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (m_noteId!=null) {
			sb.append(",noteid="+m_noteId); //$NON-NLS-1$
		}
		
		String unid = getUNID();
		if (unid!=null) {
			sb.append(",unid="+unid); //$NON-NLS-1$
		}
		
		String posStr = getPosition(String.class, ""); //$NON-NLS-1$
		if (posStr.length()>0) {
			sb.append(",pos="+posStr); //$NON-NLS-1$
		}
		
		if (m_noteClass!=null) {
			sb.append(",class="+m_noteClass.intValue()); //$NON-NLS-1$
		}
		
		if (isDocument()) {
			sb.append(",type=document"); //$NON-NLS-1$
		}
		else if (isCategory()) {
			sb.append(",type=category"); //$NON-NLS-1$
		}
		else if (isTotal()) {
			sb.append(",type=total"); //$NON-NLS-1$
		}
		
		if (m_indentLevels!=null) {
			sb.append(",indentlevel="+m_indentLevels.intValue()); //$NON-NLS-1$
		}
		
		if (m_childCount!=null) {
			sb.append(",childcount="+m_childCount.intValue()); //$NON-NLS-1$
		}
		
		if (m_descendantCount!=null) {
			sb.append(",descendantcount="+m_descendantCount.intValue()); //$NON-NLS-1$
		}
		
		if (m_siblingCount!=null) {
			sb.append(",siblingcount="+m_siblingCount.intValue()); //$NON-NLS-1$
		}
		
		if (m_summaryData!=null) {
			sb.append(",summary="+m_summaryData.toString()); //$NON-NLS-1$
		}
		
		if (m_columnValues!=null) {
			sb.append(",columns=["); //$NON-NLS-1$
			for (int i=0; i<m_columnValues.length; i++) {
				if (i>0) {
					sb.append(","); //$NON-NLS-1$
				}
				sb.append(colValueToString(m_columnValues[i]));
			}
			sb.append("]"); //$NON-NLS-1$
		}
		
		if (m_ftScore!=null) {
			sb.append(",score="+m_ftScore.intValue()); //$NON-NLS-1$
		}
		
		if (m_isUnread!=null) {
			sb.append(",unread="+m_isUnread.booleanValue()); //$NON-NLS-1$
		}
		
		if (m_isAnyUnread!=null) {
			sb.append(",anyunread="+m_isAnyUnread.booleanValue()); //$NON-NLS-1$
		}
		
		if (sb.length()>0) {
			//remove first ","
			sb.delete(0, 1);
		}

		sb.insert(0, "ViewEntry["); //$NON-NLS-1$
		sb.append("]"); //$NON-NLS-1$
		
		return sb.toString();
	}
	
	/**
	 * Converts a column value to a string, used for debugging values
	 * 
	 * @param val column value
	 * @return value as string
	 */
	private String colValueToString(Object val) {
		if (val==null) {
			return "null"; //$NON-NLS-1$
		}
		
		if (val instanceof Calendar) {
			return ((Calendar)val).getTime().toString();
		}
		else {
			return val.toString();
		}
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
		return getColumnDataAsMap().size();
	}

	@Override
	public boolean isEmpty() {
		return getColumnDataAsMap().isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		if (key instanceof String) {
			return hasItem((String) key);
		}
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		return getColumnDataAsMap().containsValue(value);
	}

	@Override
	public Object get(Object key) {
		return getColumnDataAsMap().get(key);
	}

	@Override
	public Object put(String key, Object value) {
		return getColumnDataAsMap().put(key, value);
	}

	@Override
	public Object remove(Object key) {
		return getColumnDataAsMap().remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		getColumnDataAsMap().putAll(m);
	}

	@Override
	public void clear() {
		getColumnDataAsMap().clear();
	}

	@Override
	public Set<String> keySet() {
		return getColumnDataAsMap().keySet();
	}

	@Override
	public Collection<Object> values() {
		return getColumnDataAsMap().values();
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		return getColumnDataAsMap().entrySet();
	}

	@Override
	public int getNoteID() {
		return m_noteId!=null ? m_noteId : 0;
	}

	@Override
	public Optional<Document> openDocument() {
		int noteId = getNoteID();
		if (noteId!=0) {
			return ((Database) getParent().getParent()).getDocumentById(noteId);
		}
		String unid = getUNID();
		if (!StringUtil.isEmpty(unid)) {
			return ((Database) getParent().getParent()).getDocumentByUNID(unid);
		}
		return Optional.empty();
	}

	private void loadSequenceNumberAndTime() {
		if (m_sequenceNumber==null && m_sequenceTime==null && isDocument()) {
			DocInfo[] docInfos = getParent().getParentDatabase().getMultiDocumentInfo(new int[] {getNoteID()});
			if (docInfos!=null && docInfos.length>0 && docInfos[0]!=null) {
				m_sequenceNumber = docInfos[0].getSequence();
				m_sequenceTime = docInfos[0].getSequenceTime().orElse(null);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T getSequenceNumber(Class<T> clazz, T defaultValue) {
		loadSequenceNumberAndTime();
		
		if (m_sequenceNumber!=null) {
			if (Integer.class == clazz) {
				return (T) m_sequenceNumber;
			}
			else if (String.class == clazz) {
				return (T) m_sequenceNumber.toString();
			}
		}
		
		return defaultValue;
	}

	@SuppressWarnings("unchecked")
	private <T> T getSequenceTime(Class<T> clazz, T defaultValue) {
		loadSequenceNumberAndTime();
		
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
	
	@Override
	public <T> T getSpecialValue(SpecialValue value, Class<T> clazz, T defaultValue) {
		switch (value) {
		case CHILDCOUNT:
			return getChildCount(clazz, defaultValue);
		case DESCENDANTCOUNT:
			return getDescendantCount(clazz, defaultValue);
		case SIBLINGCOUNT:
			return getSiblingCount(clazz, defaultValue);
		case INDEXPOSITION:
			return getPosition(clazz, defaultValue);
		case SEQUENCENUMBER:
			return getSequenceNumber(clazz, defaultValue);
		case SEQUENCETIME:
			return getSequenceTime(clazz, defaultValue);
		case UNREAD:
			return isUnread(clazz, defaultValue);
		case ANYUNREAD:
			return isAnyUnread(clazz, defaultValue);
			default:
				return defaultValue;
		}
	}

	@Override
	public int getIndexedValueCount() {
		return m_typedAccess.getIndexedValueCount();
	}

	@Override
	public <T> T get(int index, Class<T> valueType, T defaultValue) {
		return m_typedAccess.get(index, valueType, defaultValue);
	}

	@Override
	public <T> List<T> getAsList(int index, Class<T> valueType, List<T> defaultValue) {
		return m_typedAccess.getAsList(index, valueType, defaultValue);
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
