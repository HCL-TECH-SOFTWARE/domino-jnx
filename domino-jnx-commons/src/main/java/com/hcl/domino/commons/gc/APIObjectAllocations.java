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
package com.hcl.domino.commons.gc;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;

import com.hcl.domino.exception.ObjectDisposedException;

/**
 * Base class to collect all C API handles allocated by an
 * {@link IAPIObject}.<br>
 * <br>
 * We decouple the C API handles from the actual API object that application
 * code
 * uses to keep this information even if API objects get garbage collected by
 * Java.<br>
 * <br>
 * {@link APIObjectAllocations} are hashed in a parent/child in
 * {@link CAPIGarbageCollector}
 * and disposed when their linked {@link IAPIObject} is not referenced anymore.
 *
 * @author Karsten Lehmann
 * @param <T> API object type
 */
@SuppressWarnings("rawtypes")
public abstract class APIObjectAllocations<T extends IAPIObject> extends PhantomReference<IAPIObject> {
  private final IGCDominoClient m_parentDominoClient;
  private final APIObjectAllocations m_parentAllocations;
  private final Thread m_ownerThread;
  private final WeakReference<T> m_apiObjectRef;

  /**
   * Creates a new instance
   * 
   * @param parentDominoClient parent Domino client
   * @param parentAllocations  parent {@link APIObjectAllocations} used to dispose
   *                           a whole tree structure
   * @param referent           linked API object
   * @param queue              reference queue from
   *                           {@link CAPIGarbageCollector#getReferenceQueueForClient(IGCDominoClient)}
   */
  public APIObjectAllocations(final IGCDominoClient parentDominoClient, final APIObjectAllocations parentAllocations,
      final T referent, final ReferenceQueue<? super IAPIObject> queue) {

    super(referent, queue);

    this.m_parentDominoClient = parentDominoClient;
    this.m_parentAllocations = parentAllocations;
    this.m_ownerThread = Thread.currentThread();
    this.m_apiObjectRef = new WeakReference<>(referent);
  }

  /**
   * Throws a {@link ObjectDisposedException} when the allocations already
   * have been disposed
   */
  public void checkDisposed() {
    if (!this.m_parentDominoClient.isAllowCrossThreadAccess()) {
      // by default don't allow API object access across thread for safety reasons
      final Thread currentThread = Thread.currentThread();
      if (this.m_ownerThread != currentThread) {
        throw new IllegalStateException(MessageFormat.format(
            "API object has been created in thread {0} and cannot be called from thread {1}.", this.m_ownerThread, currentThread));
      }
    }

    if (this.isDisposed()) {
      throw new ObjectDisposedException(this);
    }
  }

  /**
   * Disposes the allocations. Does nothing if they are already disposed
   */
  public abstract void dispose();

  /**
   * Returns the parent allocations object
   * 
   * @return parent allocations
   */
  public APIObjectAllocations getParentAllocations() {
    return this.m_parentAllocations;
  }

  /**
   * Returns the parent Domino Client that this allocations object
   * belongs to. When the Domino Client gets disposed, all assigned
   * {@link APIObjectAllocations} objects get disposed as well.
   * 
   * @return parent Domino client
   */
  public IGCDominoClient getParentDominoClient() {
    return this.m_parentDominoClient;
  }

  /**
   * Returns a {@link WeakReference} to the API object. This
   * method only returns a value as long as the API object
   * exists (has not been garbage collected)
   * 
   * @return API object or null
   */
  public T getWeaklyReferencedAPIObject() {
    return this.m_apiObjectRef.get();
  }

  /**
   * Method to check if the allocations have been disposed
   * 
   * @return true if disposed
   */
  public abstract boolean isDisposed();

}
