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

import java.lang.ref.ReferenceQueue;

@SuppressWarnings("rawtypes")
public abstract class BaseAPIObject<AT extends APIObjectAllocations> implements IAPIObject<AT> {
  private final IAPIObject<?> m_parent;
  private final AT m_allocations;

  public BaseAPIObject(final IAPIObject<?> parent) {
    this.m_parent = parent;
    this.m_allocations = this.createAllocations(this.getParentDominoClient(), this.m_parent.getAdapter(APIObjectAllocations.class),
        CAPIGarbageCollector.getReferenceQueueForClient(this.getParentDominoClient()));

    CAPIGarbageCollector.registerNewAPIObject(this.m_parent, this);
  }

  protected abstract void checkDisposed();

  /**
   * Implement this method to return a subclass of {@link APIObjectAllocations}
   * that collects all C API handles for this API object
   * 
   * @param parentDominoClient parent Domino client to be passed in the
   *                           constructor
   * @param parentAllocations  parent allocations to build a tree structure for
   *                           recursive disposal
   * @param queue              reference queue
   * @return allocations object
   */
  protected abstract AT createAllocations(IGCDominoClient<?> parentDominoClient, APIObjectAllocations parentAllocations,
      ReferenceQueue<? super IAPIObject> queue);

  /**
   * Disposes this API object and all children. Does nothing if
   * already disposed.
   */
  public final void dispose() {
    if (this.isDisposed()) {
      return;
    }

    CAPIGarbageCollector.dispose(this);
  }

  @SuppressWarnings("unchecked")
  @Override
  public final <T> T getAdapter(final Class<T> clazz) {
    this.checkDisposed();

    if (clazz == APIObjectAllocations.class) {
      return (T) this.m_allocations;
    }

    return this.getAdapterLocal(clazz);
  }

  /**
   * Return any adapter implementations here. Method is called by
   * {@link #getAdapter(Class)}.
   * 
   * @param <T>   adapter type
   * @param clazz class of adapter type
   * @return adapter or null
   */
  protected <T> T getAdapterLocal(final Class<T> clazz) {
    return null;
  }

  /**
   * Returns the allocations object created in
   * {@link #createAllocations(IGCDominoClient, APIObjectAllocations, ReferenceQueue)}
   * 
   * @return allocations
   */
  protected AT getAllocations() {
    return this.m_allocations;
  }

  @Override
  public IAPIObject getParent() {
    return this.m_parent;
  }

  @Override
  public IGCDominoClient getParentDominoClient() {
    IAPIObject currParent = this.m_parent;
    while (currParent != null && !(currParent instanceof IGCDominoClient)) {
      currParent = currParent.getParent();
    }
    return currParent == null ? null : (IGCDominoClient) currParent;
  }

  /**
   * Method to check if the C API allocations of this API object
   * have been disposed
   * 
   * @return true if disposed
   */
  public final boolean isDisposed() {
    return this.m_allocations.isDisposed();
  }

}
