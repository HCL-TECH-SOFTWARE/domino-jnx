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
package com.hcl.domino.jna.internal.gc.allocations;

import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import com.hcl.domino.UserNamesList;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.dbdirectory.DatabaseData;
import com.hcl.domino.jna.admin.JNAServerAdmin;
import com.hcl.domino.jna.data.JNAAcl;
import com.hcl.domino.jna.internal.NotesNamingUtils;
import com.hcl.domino.misc.Pair;
import com.hcl.domino.security.AclAccess;

public class JNAServerAdminAllocations extends APIObjectAllocations<JNAServerAdmin> {
	private boolean m_disposed;
	
	private Map<String,Map<DatabaseData,JNAAcl>> m_aclCache = Collections.synchronizedMap(new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
	private Map<String,ReadWriteLock> m_aclCacheRWLocksByServer = Collections.synchronizedMap(new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
	private Set<String> m_aclCacheFilledForServer = Collections.synchronizedSet(new TreeSet<>(String.CASE_INSENSITIVE_ORDER));
	
	@SuppressWarnings("rawtypes")
	public JNAServerAdminAllocations(IGCDominoClient parentDominoClient, APIObjectAllocations parentAllocations,
			JNAServerAdmin referent, ReferenceQueue<? super IAPIObject> queue) {

		super(parentDominoClient, parentAllocations, referent, queue);
	}

	@Override
	public boolean isDisposed() {
		return m_disposed;
	}

	@Override
	public void dispose() {
		if (isDisposed()) {
			return;
		}

		flushAllAclCaches();

		m_disposed = true;
	}

	/**
	 * Acquires a write lock on the ACL cache for the specified server
	 * 
	 * @param serverAbbr server
	 * @param consumer consumer to access cache map
	 */
	private void writeAccessAclCache(String serverAbbr, Consumer<Map<DatabaseData,JNAAcl>> consumer) {
		Map<DatabaseData,JNAAcl> aclCacheForServer;
		ReadWriteLock rwLock;
		synchronized (m_aclCacheRWLocksByServer) {
			rwLock = m_aclCacheRWLocksByServer.get(serverAbbr);
			if (rwLock==null) {
				rwLock = new ReentrantReadWriteLock();
				m_aclCacheRWLocksByServer.put(serverAbbr, rwLock);
			}
			
			aclCacheForServer = m_aclCache.get(serverAbbr);
			if (aclCacheForServer==null) {
				aclCacheForServer = Collections.synchronizedMap(new HashMap<>());
				m_aclCache.put(serverAbbr, aclCacheForServer);
			}
		}
		
		rwLock.writeLock().lock();
		try {
			consumer.accept(aclCacheForServer);
		}
		finally {
			rwLock.writeLock().unlock();
		}
	}

	/**
	 * Acquires a read lock on the ACL cache for the specified server
	 * 
	 * @param serverAbbr server
	 * @param consumer consumer to access cache map
	 */
	private void readAccessAclCache(String serverAbbr, Consumer<Map<DatabaseData,JNAAcl>> consumer) {
		Map<DatabaseData,JNAAcl> aclCacheForServer;
		ReadWriteLock rwLock;
		synchronized (m_aclCacheRWLocksByServer) {
			rwLock = m_aclCacheRWLocksByServer.get(serverAbbr);
			if (rwLock==null) {
				rwLock = new ReentrantReadWriteLock();
				m_aclCacheRWLocksByServer.put(serverAbbr, rwLock);
			}
			
			aclCacheForServer = m_aclCache.get(serverAbbr);
			if (aclCacheForServer==null) {
				aclCacheForServer = Collections.synchronizedMap(new HashMap<>());
				m_aclCache.put(serverAbbr, aclCacheForServer);
			}
		}
		
		rwLock.readLock().lock();
		try {
			consumer.accept(Collections.unmodifiableMap(aclCacheForServer));
		}
		finally {
			rwLock.readLock().unlock();
		}
	}

	public boolean isACLCacheInitialized(String server) {
		String serverAbbr = NotesNamingUtils.toAbbreviatedName(server);
		return m_aclCacheFilledForServer.contains(serverAbbr);
	}

	public void addACLToCache(DatabaseData dbData, JNAAcl aclClone) {
		String serverAbbr = NotesNamingUtils.toAbbreviatedName(dbData.getServer());
		
		writeAccessAclCache(serverAbbr, (aclCacheForServer) -> {
			JNAAcl oldAcl = aclCacheForServer.put(dbData, aclClone);
			if (oldAcl!=null) {
				oldAcl.dispose();
			}
			m_aclCacheFilledForServer.add(serverAbbr);
		});
	}

	private void flushAllAclCaches() {
		synchronized (m_aclCacheRWLocksByServer) {
			for (String currServer : m_aclCache.keySet()) {
				flushAclCache(currServer);
			}
			m_aclCacheRWLocksByServer.clear();
		}
	}
	
	public void flushAclCache(String server) {
		String serverAbbr = NotesNamingUtils.toAbbreviatedName(server);
		
		writeAccessAclCache(serverAbbr, (aclCacheForServer) -> {
			for (Entry<DatabaseData, JNAAcl> currEntry : aclCacheForServer.entrySet()) {
				JNAAcl acl = currEntry.getValue();
				acl.dispose();
			}
			aclCacheForServer.clear();
		});
	}
	
	public List<Pair<DatabaseData, AclAccess>> computeDatabaseAccess(String server,
			UserNamesList usernamesList) {

		String serverAbbr = NotesNamingUtils.toAbbreviatedName(server);

		List<Pair<DatabaseData, AclAccess>> result = new ArrayList<>();

		readAccessAclCache(serverAbbr, (aclCacheForServer) -> {
			for (Entry<DatabaseData,JNAAcl> currEntry : aclCacheForServer.entrySet()) {
				DatabaseData dbData = currEntry.getKey();
				
				if (NotesNamingUtils.equalNames(server, currEntry.getKey().getServer())) {
					JNAAcl currAcl = currEntry.getValue();
					
					AclAccess aclAccess = currAcl.lookupAccess(usernamesList);
					if (aclAccess!=null) {
						result.add(new Pair<>(dbData, aclAccess));
					}
				}
			}
		});

		return result;
	}
}
