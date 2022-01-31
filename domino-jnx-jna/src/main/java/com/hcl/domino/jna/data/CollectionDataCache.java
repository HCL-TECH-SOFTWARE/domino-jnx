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

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.hcl.domino.commons.views.ReadMask;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.jna.data.JNACollectionEntry.CacheableViewEntryData;
import com.hcl.domino.jna.data.JNADominoCollection.JNACollectionEntryProcessor;

/**
 * LRU cache class to be returned in {@link JNACollectionEntryProcessor#createDataCache()} in order to let NIF
 * improve lookup performance by skipping already known collection data.<br>
 * <br>
 * Please note that according to IBM dev, this optimized view reading (differential view reads) does
 * only work in views that are not permuted (where documents do not appear multiple times, because
 * "Show multiple values as separate entries" has been set on any view column).
 * 
 * @author Karsten Lehmann
 */
public class CollectionDataCache implements Serializable {
	private static final long serialVersionUID = 522152090817358117L;
	
	private int m_maxSize;
	private Map<Integer,CacheableViewEntryData> m_cacheEntries;
	private DominoDateTime m_diffTime;
	private ReentrantReadWriteLock m_rwLock = new ReentrantReadWriteLock();
	private Set<ReadMask> m_readMask;
	private static ThreadLocal<Long> m_cacheUseCounter = new ThreadLocal<>();
	
	/**
	 * Creates a new instance of an unbounded cache
	 */
	public CollectionDataCache() {
		this(Integer.MAX_VALUE);
	}
	
	/**
	 * Creates a new instance
	 * 
	 * @param maxSize maximum number of entries in the LRU cache
	 */
	public CollectionDataCache(final int maxSize) {
		if (maxSize <= 0) {
			throw new IllegalArgumentException(MessageFormat.format("Max size must be greater than 0: {0}", maxSize));
		}
		
		m_maxSize = maxSize;
		
		//set up LRU cache map
		m_cacheEntries = Collections.synchronizedMap(new LinkedHashMap<Integer, CacheableViewEntryData>(16, 0.75f, true) {
			private static final long serialVersionUID = -9095321519005351099L;

			@Override
			protected boolean removeEldestEntry(java.util.Map.Entry<Integer, CacheableViewEntryData> eldest) {
				if (size() > maxSize) {
					return true;
				}
				else {
					return false;
				}
			}
		});
	}
	
	/**
	 * Returns the maximum number of entries in the cache
	 * 
	 * @return maximum number
	 */
	public int getMaxCacheSize() {
		return m_maxSize;
	}
	
	/**
	 * Returns the current number of entries in the cache
	 * 
	 * @return size
	 */
	public int size() {
		m_rwLock.readLock().lock();
		try {
			return m_cacheEntries.size();
		}
		finally {
			m_rwLock.readLock().unlock();
		}
	}
	
	/**
	 * Enables taking cache usage stats for the current thread
	 */
	public void enableUsageStats() {
		Long counter = m_cacheUseCounter.get();
		if (counter==null) {
			m_cacheUseCounter.set((long) 0);
		}
	}

	/**
	 * Disables taking cache usage stats for the current thread
	 */
	public void disableUsageStats() {
		m_cacheUseCounter.set(null);
	}
	
	/**
	 * Method to check whether taking cache usage stats for the
	 * current thread is enabled
	 * 
	 * @return true if enabled
	 */
	public boolean isUsageStatsEnabled() {
		return m_cacheUseCounter.get() != null;
	}
	
	/**
	 * Returns a statistic value with the number of view entries where we could use the cache data
	 * 
	 * @return count or -1 if logging stats are not enabled
	 */
	public long getCacheUsageStats() {
		Long counter = m_cacheUseCounter.get();
		if (counter!=null) {
			return counter;
		}
		return -1;
	}
	
	/**
	 * Sets the cache usage stats to 0
	 */
	public void resetCacheUsageStats() {
		Long counter = m_cacheUseCounter.get();
		if (counter!=null) {
			m_cacheUseCounter.set((long) 0);
		}
	}
	
	/**
	 * Removes all data from the cache
	 */
	public void flush() {
		m_rwLock.writeLock().lock();
		try {
			m_diffTime = null;
			m_readMask = null;
			m_cacheEntries.clear();
		}
		finally {
			m_rwLock.writeLock().unlock();
		}
	}
	
	/**
	 * Method to fill the cache with data read from the collection
	 * 
	 * @param diffTime diff time returned from the read operation
	 * @param entries collection entries read
	 */
	void addCacheValues(Set<ReadMask> readMask, DominoDateTime diffTime, List<JNACollectionEntry> entries) {
		m_rwLock.writeLock().lock();
		try {
			boolean flush = false;
			
			if (m_diffTime!=null && !m_diffTime.equals(diffTime)) {
				flush = true;
			}
			else if (m_readMask!=null && !m_readMask.equals(readMask)) {
				flush = true;
			}
			if (flush) {
				m_cacheEntries.clear();
				cacheFlushed();
			}
			
			m_readMask = readMask;
			m_diffTime = diffTime;
			
			for (JNACollectionEntry currEntry : entries) {
				if (currEntry.hasAnyColumnValues()) {
					CacheableViewEntryData cacheableData = currEntry.getCacheableData();
					m_cacheEntries.put(currEntry.getNoteID(), cacheableData);
				}
			}
		}
		finally {
			m_rwLock.writeLock().unlock();
		}
	}
	
	/**
	 * Called when the cache needed to be flushed because of view index changes.
	 * Method is empty by default, can be overriden, e.g. to write a log entry.
	 */
	protected void cacheFlushed() {
		//
	}
	
	/**
	 * For every {@link NotesViewEntryData} in the specified list, this method checks whether
	 * NIF returned any column data. If not, the entry was skipped by NIF, because it already exists
	 * in the cache. We can then copy the data of our current cache object.
	 * 
	 * @param entries entries to scan
	 */
	void populateEntryStubsWithData(List<JNACollectionEntry> entries) {
		boolean hasAnyMissingData = false;
		for (JNACollectionEntry currEntry : entries) {
			if (!currEntry.hasAnyColumnValues()) {
				hasAnyMissingData = true;
				break;
			}
		}
		
		Long usageStats = m_cacheUseCounter.get();
		long usageStatsPrim = usageStats==null ? -1 : usageStats;
		
		if (hasAnyMissingData) {
			m_rwLock.readLock().lock();
			try {
				for (JNACollectionEntry currEntry : entries) {
					if (!currEntry.hasAnyColumnValues()) {
						CacheableViewEntryData cacheData = m_cacheEntries.get(currEntry.getNoteID());
						if (cacheData!=null) {
							//updating data of stub entry from cache
							currEntry.updateFromCache(cacheData);
							
							if (usageStatsPrim!=-1) {
								usageStatsPrim++;
							}
						}
					}
				}
				
				if (usageStatsPrim!=-1) {
					m_cacheUseCounter.set(usageStatsPrim);
				}
			}
			finally {
				m_rwLock.readLock().unlock();
			}
		}
	}
	
	/**
	 * Copies the current state of the cache
	 * 
	 * @return state
	 */
	CacheState getCacheState() {
		m_rwLock.readLock().lock();
		try {
			return new CacheState(m_readMask, m_diffTime, new HashMap<>(m_cacheEntries));
		}
		finally {
			m_rwLock.readLock().unlock();
		}
	}
	
	/**
	 * Data object with cache state values
	 * 
	 * @author Karsten Lehmann
	 */
	static class CacheState {
		private DominoDateTime m_diffTime;
		private Map<Integer,CacheableViewEntryData> m_cacheEntries;
		private Set<ReadMask> m_readMask;
		
		private CacheState(Set<ReadMask> readMask, DominoDateTime diffTime, Map<Integer,CacheableViewEntryData> cacheEntries) {
			m_readMask = readMask;
			m_diffTime = diffTime;
			m_cacheEntries = cacheEntries;
		}
		
		public Set<ReadMask> getReadMask() {
			return m_readMask;
		}
		
		public DominoDateTime getDiffTime() {
			return m_diffTime;
		}
		
		public Map<Integer,CacheableViewEntryData> getCacheEntries() {
			return m_cacheEntries;
		}
	}
	

}
