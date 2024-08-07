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
package com.hcl.domino.commons.gc;

import java.lang.ref.ReferenceQueue;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoException;
import com.hcl.domino.commons.gc.IGCControl.GCAction;
import com.hcl.domino.commons.util.DominoUtils;
import com.hcl.domino.exception.ObjectDisposedException;

@SuppressWarnings("rawtypes")
public class CAPIGarbageCollector {
  public static class CAPIGarbageCollectorListenerAdapter implements ICAPIGarbageCollectorListener {

    @Override
    public void endDispose(final DominoClient client, final APIObjectAllocations apiObjectAllocations, final int depth) {
    }

    @Override
    public void endFlushingRefQueue(final DominoClient client) {
    }

    @Override
    public void newAPIObjectCreated(final IAPIObject parent, final IAPIObject obj) {
    }

    @Override
    public void startDispose(final DominoClient client, final APIObjectAllocations apiObjectAllocations, final int depth) {
    }

    @Override
    public void startFlushingRefQueue(final DominoClient client) {
    }

    @Override
    public void unreferencedAPIObjectFound(final DominoClient client, final APIObjectAllocations apiObjectAllocations) {
    }

  }

  /**
   * Listener to get notified about important C API garbage collection
   * events
   * 
   * @author Karsten Lehmann
   */
  public interface ICAPIGarbageCollectorListener {

    /**
     * Called after the child tree structure and the allocations itself have been
     * disposed.
     * 
     * @param client               Domino Client
     * @param apiObjectAllocations disposed allocations
     * @param depth                tree depths starting with 0 (can be used for
     *                             indentation)
     */
    void endDispose(DominoClient client, APIObjectAllocations apiObjectAllocations, int depth);

    /**
     * Method is called after flushing the reference queue
     * of objects to be GCed
     * 
     * @param client Domino client
     */
    void endFlushingRefQueue(DominoClient client);

    /**
     * Called when a new API object has been registered
     * 
     * @param parent parent API object
     * @param obj    new API object
     */
    void newAPIObjectCreated(IAPIObject parent, IAPIObject obj);

    /**
     * Called before diving into the child tree structure to dispose all child
     * allocations
     * (in reverse order) and disposing the allocations itself.
     * 
     * @param client               Domino Client
     * @param apiObjectAllocations allocations about to be disposed
     * @param depth                tree depths starting with 0 (can be used for
     *                             indentation)
     */
    void startDispose(DominoClient client, APIObjectAllocations apiObjectAllocations, int depth);

    /**
     * Method is called before flushing the reference queue
     * of objects to be GCed
     * 
     * @param client Domino client
     */
    void startFlushingRefQueue(DominoClient client);

    /**
     * Called when an unreferenced {@link IAPIObject} has been found
     * for a Domino client. Since the object is already gone, what's left
     * is its {@link APIObjectAllocations} with the C API handles.
     * 
     * @param client               Domino client
     * @param apiObjectAllocations allocations that is about to be disposed
     */
    void unreferencedAPIObjectFound(DominoClient client, APIObjectAllocations apiObjectAllocations);

  }

  private static final Map<IGCDominoClient, ReferenceQueue<? super IAPIObject>> referenceQueues = Collections
      .synchronizedMap(new HashMap<>());
  
  private static final Map<IGCDominoClient, Map<APIObjectAllocations, List<APIObjectAllocations>>> dominoClientAllocationsByParent = Collections
      .synchronizedMap(new HashMap<>());
  
  private static final Map<IGCDominoClient, List<ICAPIGarbageCollectorListener>> gcListenerByClient = Collections
      .synchronizedMap(new HashMap<>());
  
  private static final boolean skipDispose = DominoUtils.isDisableGCDispose();

  /** weak hashmap of r/w locks per DominoClient to prevent parallel GC activity */
  private static final Map<DominoClient, ReadWriteLock> gcLocksByClient = new WeakHashMap<>();
  /** lock to coordinate access on {@link CAPIGarbageCollector#gcLocksByClient} */
  private static final Lock gcLocksByClientLock = new ReentrantLock();
  
  /**
   * Returns a R/W lock to ensure there's no concurrent GC on the same DominoClient instance. We currently
   * only use the write lock. The read lock could be used in the future to analyze the GC'ed objects for a client.
   * 
   * @param client client
   * @return R/W lock
   */
  private static ReadWriteLock getClientGCLock(DominoClient client) {
    gcLocksByClientLock.lock();
    try {
      ReadWriteLock clientGCLock = gcLocksByClient.get(client);
      if (clientGCLock==null) {
        clientGCLock = new ReentrantReadWriteLock();
        gcLocksByClient.put(client, clientGCLock);
      }
      return clientGCLock;
    }
    finally {
      gcLocksByClientLock.unlock();
    }
  }
  
  /**
   * Adds a garbage collection listener for a Domino client
   * 
   * @param client   Domino client
   * @param listener listener to add
   */
  public static void addListener(final IGCDominoClient client, final ICAPIGarbageCollectorListener listener) {
    ReadWriteLock gcLock = getClientGCLock(client);
    gcLock.writeLock().lock();
    try {
      List<ICAPIGarbageCollectorListener> listeners = CAPIGarbageCollector.gcListenerByClient.get(client);
      if (listeners == null) {
        listeners = Collections.synchronizedList(new ArrayList<>());
        CAPIGarbageCollector.gcListenerByClient.put(client, listeners);
      }
      if (!listeners.contains(listener)) {
        listeners.add(listener);
      }
    }
    finally {
      gcLock.writeLock().unlock();
    }
  }

  /**
   * Internal recursive disposal of an {@link APIObjectAllocations} tree
   * structure.
   * 
   * @param client      Domino client
   * @param allocations allocations to dispose (we first dispose the child
   *                    allocations in reverse order)
   * @param depth       contains 0 for the first tree level
   */
  private static void dispose(final DominoClient client, final APIObjectAllocations allocations, final int depth) {
    if (allocations == null) {
      return;
    }
    
    ReadWriteLock gcLock = getClientGCLock(client);
    gcLock.writeLock().lock();
    try {
      final List<ICAPIGarbageCollectorListener> listeners = CAPIGarbageCollector.gcListenerByClient.get(client);
      if (listeners != null) {
        for (final ICAPIGarbageCollectorListener currListener : listeners) {
          try {
            currListener.startDispose(client, allocations, depth);
          } catch (final Exception e) {
            e.printStackTrace();
          }
        }
      }

      // dispose children first
      final Map<APIObjectAllocations, List<APIObjectAllocations>> allocationsByParent = CAPIGarbageCollector.dominoClientAllocationsByParent
          .get(client);
      if (allocationsByParent != null) {
        final List<APIObjectAllocations> childAllocations = allocationsByParent.get(allocations);
        if (childAllocations != null) {
          if (!childAllocations.isEmpty()) {
            final APIObjectAllocations[] childAllocationsCopy = childAllocations
                .toArray(new APIObjectAllocations[childAllocations.size()]);

            final int childAllocationsSize = childAllocationsCopy.length;

            for (int i = childAllocationsSize - 1; i >= 0; i--) {
              try {
                final APIObjectAllocations currChild = childAllocationsCopy[i];
                CAPIGarbageCollector.dispose(client, currChild, depth + 1);
              } catch (final Exception e) {
                throw new DominoException(MessageFormat.format("Error disposing {0}", childAllocationsCopy[i]), e);
              }
            }
            childAllocations.clear();
          }

          allocationsByParent.remove(allocations);
        }
      }

      if (!allocations.isDisposed()) {
        final APIObjectAllocations parentAllocations = allocations.getParentAllocations();

        if (!CAPIGarbageCollector.skipDispose) {
          if (!allocations.isDisposed()) {
            allocations.dispose();
          }
        }

        if (parentAllocations != null && allocationsByParent != null) {
          final List<APIObjectAllocations> parentsChildAllocations = allocationsByParent.get(parentAllocations);
          if (parentsChildAllocations!=null) {
            synchronized (parentsChildAllocations) {
              //prevent concurrent modification on parentsChildAllocations in multiple threads
              if (parentsChildAllocations.contains(allocations)) {
                parentsChildAllocations.remove(allocations);

                if (parentsChildAllocations.isEmpty()) {
                  allocationsByParent.remove(parentAllocations);
                }
              }
            }
          }
        }
      }

      if (listeners != null) {
        for (final ICAPIGarbageCollectorListener currListener : listeners) {
          try {
            currListener.endDispose(client, allocations, depth);
          } catch (final Exception e) {
            e.printStackTrace();
          }
        }
      }        
    }
    finally {
      gcLock.writeLock().unlock();
    }
  }

  /**
   * Recursive disposal of an {@link IAPIObject}'s children and of the object
   * itself.
   * 
   * @param baseAPIObject api object to dispose
   */
  public static void dispose(final IAPIObject baseAPIObject) {
    DominoClient client = baseAPIObject.getParentDominoClient();
    ReadWriteLock gcLock = getClientGCLock(client);
    gcLock.writeLock().lock();
    try {
      final APIObjectAllocations objectAllocations = baseAPIObject.getAdapter(APIObjectAllocations.class);
      if (objectAllocations != null) {
        CAPIGarbageCollector.dispose(client, objectAllocations, 0);
      }
    }
    finally {
      gcLock.writeLock().unlock();
    }
  }

  /**
   * Disposable of a {@link DominoClient}'s children and the client itself
   * 
   * @param client domino client
   */
  public static void dispose(final IGCDominoClient<?> client) {
    ReadWriteLock gcLock = getClientGCLock(client);
    gcLock.writeLock().lock();
    try {
      final APIObjectAllocations clientAllocations = client.getAdapter(APIObjectAllocations.class);
      CAPIGarbageCollector.dispose(client, clientAllocations, 0);
    }
    finally {
      gcLock.writeLock().unlock();
    }
  }

  /**
   * Checks if there are any C resources for garbage collected
   * Java API objects that can be disposed as well.
   * 
   * @param client current Domino client to run the GC on
   */
  public static void gc(final IGCDominoClient client) {
    ReadWriteLock gcLock = getClientGCLock(client);
    gcLock.writeLock().lock();
    try {
      final ReferenceQueue<? super IAPIObject> queue = CAPIGarbageCollector.getReferenceQueueForClient(client);
      final List<ICAPIGarbageCollectorListener> listeners = CAPIGarbageCollector.gcListenerByClient.get(client);

      if (listeners != null) {
        for (final ICAPIGarbageCollectorListener currListener : listeners) {
          currListener.startFlushingRefQueue(client);
        }
      }

      APIObjectAllocations currAlloc;
      
      while ((currAlloc = (APIObjectAllocations) queue.poll()) != null) {
        if (listeners != null) {
          for (final ICAPIGarbageCollectorListener currListener : listeners) {
            currListener.unreferencedAPIObjectFound(client, currAlloc);
          }
        }

        CAPIGarbageCollector.dispose(client, currAlloc, 0);
      }

      if (listeners != null) {
        for (final ICAPIGarbageCollectorListener currListener : listeners) {
          currListener.endFlushingRefQueue(client);
        }
      }
    }
    finally {
      gcLock.writeLock().unlock();
    }
  }

  /**
   * Returns the {@link ReferenceQueue} used to find unreferenced API objects
   * for a Domino Client
   * 
   * @param client Domino client
   * @return queue
   */
  public static ReferenceQueue<? super IAPIObject> getReferenceQueueForClient(final IGCDominoClient client) {
    ReadWriteLock gcLock = getClientGCLock(client);
    gcLock.writeLock().lock();
    try {
      final ReferenceQueue<? super IAPIObject> queue = CAPIGarbageCollector.referenceQueues.get(client);
      if (queue == null) {
        if (client.isRegisteredForGC()) {
          throw new ObjectDisposedException("Domino Client is already closed");
        } else {
          throw new DominoException("Domino Client not registered for GC yet");
        }
      }
      return queue;
    }
    finally {
      gcLock.writeLock().unlock();
    }
  }

  /**
   * Sets up a {@link ReferenceQueue} to track unreferenced API objects
   * 
   * @param client Domino client
   */
  public static void registerDominoClient(final IGCDominoClient client) {
    ReadWriteLock gcLock = getClientGCLock(client);
    gcLock.writeLock().lock();
    try {
      if (CAPIGarbageCollector.referenceQueues.containsKey(client)) {
        throw new DominoException(0, "Duplicate Domino Client registration");
      }
      CAPIGarbageCollector.referenceQueues.put(client, new ReferenceQueue<>());
      client.markRegisteredForGC();
    }
    finally {
      gcLock.writeLock().unlock();
    }
  }

  /**
   * Registers a new API object
   * 
   * @param parent parent API object
   * @param obj    new API object to register
   */
  public static void registerNewAPIObject(final IAPIObject parent, final IAPIObject obj) {
    final DominoClient client = obj.getParentDominoClient();
    ReadWriteLock gcLock = getClientGCLock(client);
    gcLock.writeLock().lock();
    try {
      if (!(client instanceof IGCDominoClient)) {
        throw new IllegalArgumentException("DominoClient must implement IGCDominoClient");
      }

      final APIObjectAllocations objectAllocations = obj.getAdapter(APIObjectAllocations.class);
      if (objectAllocations == null) {
        throw new DominoException(0, MessageFormat.format(
            "Object is expected to return an implementation of APIObjectAllocations for resource tracking: {0}",
            obj.getClass().getName()));
      }
      final APIObjectAllocations parentObjectAllocations = parent.getAdapter(APIObjectAllocations.class);
      if (parentObjectAllocations == null) {
        throw new DominoException(0, MessageFormat.format(
            "Parent object is expected to return an implementation of APIObjectAllocations for resource tracking: {0}",
            parent.getClass().getName()));
      }

      Map<APIObjectAllocations, List<APIObjectAllocations>> allocationsByParent = CAPIGarbageCollector.dominoClientAllocationsByParent.get(client);
      if (allocationsByParent == null) {
        allocationsByParent = Collections.synchronizedMap(new HashMap<>());
        CAPIGarbageCollector.dominoClientAllocationsByParent.put((IGCDominoClient) client, allocationsByParent);
      }

      List<APIObjectAllocations> allocationsForParent = allocationsByParent.get(parentObjectAllocations);
      if (allocationsForParent == null) {
        allocationsForParent = new ArrayList<>();
        allocationsByParent.put(parentObjectAllocations, allocationsForParent);
      }

      if (!allocationsForParent.contains(objectAllocations)) {
        allocationsForParent.add(objectAllocations);
      }

      final List<ICAPIGarbageCollectorListener> listeners = CAPIGarbageCollector.gcListenerByClient.get(client);
      if (listeners != null) {
        for (final ICAPIGarbageCollectorListener currListener : listeners) {
          try {
            currListener.newAPIObjectCreated(parent, obj);
          } catch (final Exception e) {
            e.printStackTrace();
          }
        }
      }

      final IGCControl gcCtrl = client.getAdapter(IGCControl.class);
      if (gcCtrl != null) {
        final GCAction action = gcCtrl.objectAllocated(parent, obj);
        if (action == GCAction.FLUSH_REFQUEUE) {
          CAPIGarbageCollector.gc((IGCDominoClient) client);
        }
      }
    }
    finally {
      gcLock.writeLock().unlock();
    }
  }

  /**
   * Removes a garbage collection listener for a Domino client
   * 
   * @param client   Domino client
   * @param listener listener to remove
   */
  public static void removeListener(final IGCDominoClient client, final ICAPIGarbageCollectorListener listener) {
    ReadWriteLock gcLock = getClientGCLock(client);
    gcLock.writeLock().lock();
    try {
      final List<ICAPIGarbageCollectorListener> listeners = CAPIGarbageCollector.gcListenerByClient.get(client);
      if (listeners != null) {
        listeners.remove(listener);
      }
    }
    finally {
      gcLock.writeLock().unlock();
    }
  }

  /**
   * Cleans up and removes the {@link ReferenceQueue} for a Domino Client
   * 
   * @param client Domino client
   */
  public static void unregisterDominoClient(final IGCDominoClient client) {
    ReadWriteLock gcLock = getClientGCLock(client);
    gcLock.writeLock().lock();
    try {
      if (!CAPIGarbageCollector.referenceQueues.containsKey(client)) {
        throw new DominoException(0, "Domino Client was not registered");
      }
      CAPIGarbageCollector.gc(client);

      CAPIGarbageCollector.referenceQueues.remove(client);
      CAPIGarbageCollector.dominoClientAllocationsByParent.remove(client);
      CAPIGarbageCollector.gcListenerByClient.remove(client);
    }
    finally {
      gcLock.writeLock().unlock();
    }
  }
  
  public static Collection<IGCDominoClient> getAllClients() {
    return new ArrayList<>(referenceQueues.keySet());
  }
}
