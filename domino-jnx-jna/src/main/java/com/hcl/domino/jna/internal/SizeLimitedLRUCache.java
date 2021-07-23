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
package com.hcl.domino.jna.internal;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.EntryWeigher;

/**
 * Abstract cache class that implements an LRU algorithm. Uses RW lock
 * internally to support concurrent access across threads.
 * 
 * @author Karsten Lehmann
 *
 * @param <K> key type
 * @param <V> value type
 */
public abstract class SizeLimitedLRUCache<K,V> {
	private ConcurrentLinkedHashMap<K, V> m_cache;
	
	public SizeLimitedLRUCache(int maxSizeUnits) {
		EntryWeigher<K, V> customWeigher = (key, value) -> computeSize(key, value);
		m_cache = new ConcurrentLinkedHashMap.Builder<K, V>()
			    .maximumWeightedCapacity(maxSizeUnits)
			    .weigher(customWeigher)
			    .build();
	}
	
	public List<K> getKeys() {
		return new ArrayList<>(m_cache.keySet());
	}
	
	public void clear() {
		m_cache.clear();
	}
	
	public final long getCurrentCacheSizeInUnits() {
		return m_cache.weightedSize();
	}

	/**
	 * Implement this method to compute a size for the cache entry
	 * 
	 * @param key key
	 * @param value value
	 * @return size in units
	 */
	protected abstract int computeSize(K key, V value);
	
	/**
	 * Method to look up a cache entry
	 * 
	 * @param key key
	 * @return value or null if not found
	 */
	public V get(K key) {
		return m_cache.get(key);
	}
	
	/**
	 * Method to check whether the cache contains a key
	 * 
	 * @param key key
	 * @return true if value exists
	 */
	public boolean containsKey(K key) {
		return m_cache.containsKey(key);
	}
	
	/**
	 * Removes a key from the LRU cache
	 * 
	 * @param key key
	 * @return previously stored value or null
	 */
	public V remove(K key) {
		return m_cache.remove(key);
	}
	
	/**
	 * Adds an entry to the LRU cache
	 * 
	 * @param key key
	 * @param newValue value, if null we remove the cache entry
	 * @return previously stored value or null
	 */
	public V put(K key, V newValue) {
		if (newValue==null) {
			return remove(key);
		}
		else {
			return m_cache.put(key, newValue);
		}
	}
	
}
