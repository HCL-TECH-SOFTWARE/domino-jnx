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
package com.hcl.domino.jna.internal.structs;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.TypeMapper;

/**
 * Extension of {@link Structure} to make it work in an execution environment
 * that is secured by the Domino SecurityManager.
 * 
 * @author Karsten Lehmann
 */
public abstract class BaseStructure extends Structure {
	private static final WeakHashMap<Class<?>, List<Field>> fieldListCache = new WeakHashMap<>();
	private static final ReadWriteLock fieldListCacheLock = new ReentrantReadWriteLock();

	private static final WeakHashMap<Class<?>, List<Field>> sortedFieldListCache = new WeakHashMap<>();
	private static final ReadWriteLock sortedFieldListCacheLock = new ReentrantReadWriteLock();

  protected BaseStructure() {
		super(NotesCAPI.getPlatformAlignment());
		int overrideAlignment = getOverrideAlignment();
		if (overrideAlignment!=-1) {
			setAlignType(overrideAlignment);
		}
	}
	
	public BaseStructure(int alignType, TypeMapper mapper) {
		super(alignType, mapper);
	}

	public BaseStructure(int alignType) {
		super(alignType);
	}

	public BaseStructure(Pointer p, int alignType, TypeMapper mapper) {
		super(p, alignType, mapper);
	}

	public BaseStructure(Pointer p, int alignType) {
		super(p, alignType);
	}

	public BaseStructure(Pointer p) {
		super(p, NotesCAPI.getPlatformAlignment());
		int overrideAlignment = getOverrideAlignment();
		if (overrideAlignment!=-1) {
			setAlignType(overrideAlignment);
		}
	}

	public BaseStructure(TypeMapper mapper) {
		super(mapper);
		setAlignType(NotesCAPI.getPlatformAlignment());
		int overrideAlignment = getOverrideAlignment();
		if (overrideAlignment!=-1) {
			setAlignType(overrideAlignment);
		}
	}
	
	protected int getOverrideAlignment() {
		return -1;
	}

	@Override
	protected List<Field> getFieldList() {
	  fieldListCacheLock.readLock().lock();
	  try {
	    List<Field> fields = fieldListCache.get(getClass());
	    if (fields!=null) {
	      return fields;
	    }
	  }
	  finally {
	    fieldListCacheLock.readLock().unlock();
	  }
	  
	  fieldListCacheLock.writeLock().lock();
	  try {
	    List<Field> fields = AccessController.doPrivileged(
	        (PrivilegedAction<List<Field>>) () -> BaseStructure.super.getFieldList());
	    fieldListCache.put(getClass(), fields);
	    return fields;
	  }
	  finally {
	    fieldListCacheLock.writeLock().unlock();
	  }
	}
  
	protected List<Field> getFields(boolean force) {
	  //the lists stored in fieldListCache and sortedFieldListCache are actually the same,
	  //because getFields(force) internally calls getFieldList and sorts the (cached) returned list
	  //but getFields(force) calls private functions that we don't want to duplicate in case
	  //they change in a later JNA version
	  //see https://github.com/java-native-access/jna/issues/1478#issuecomment-1312182291 for a discussion about this topic
	  sortedFieldListCacheLock.readLock().lock();
    try {
      List<Field> fields = sortedFieldListCache.get(getClass());
      if (fields!=null) {
        return fields;
      }
    }
    finally {
      sortedFieldListCacheLock.readLock().unlock();
    }
    
    sortedFieldListCacheLock.writeLock().lock();
    try {
      List<Field> fields = AccessController.doPrivileged(
          (PrivilegedAction<List<Field>>) () -> BaseStructure.super.getFields(force));
      if (fields==null) {
        return null;
      }
      sortedFieldListCache.put(getClass(), fields);
      return fields;
    }
    finally {
      sortedFieldListCacheLock.writeLock().unlock();
    }
	}
	
	@Override
	public void read() {
		AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
			BaseStructure.super.read();
			return null;
		});
	}
	
	@Override
	public void write() {
		AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
			BaseStructure.super.write();
			return null;
		});
	}
	
}
