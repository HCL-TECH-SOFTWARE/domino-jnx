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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoException;
import com.hcl.domino.commons.gc.IGCControl.GCAction;
import com.hcl.domino.commons.util.DominoUtils;
import com.hcl.domino.exception.ObjectDisposedException;

@SuppressWarnings("rawtypes")
public class CAPIGarbageCollector {
	private static Map<IGCDominoClient,ReferenceQueue<? super IAPIObject>> referenceQueues = Collections.synchronizedMap(new HashMap<>());
	private static Map<IGCDominoClient, Map<APIObjectAllocations, List<APIObjectAllocations>>> dominoClientAllocationsByParent = Collections.synchronizedMap(new HashMap<>());
	private static Map<IGCDominoClient,List<ICAPIGarbageCollectorListener>> gcListenerByClient = Collections.synchronizedMap(new HashMap<>());

	
	private static boolean skipDispose = DominoUtils.isDisableGCDispose();
	
	/**
	 * Returns the {@link ReferenceQueue} used to find unreferenced API objects
	 * for a Domino Client
	 * 
	 * @param client Domino client
	 * @return queue
	 */
	public static ReferenceQueue<? super IAPIObject> getReferenceQueueForClient(IGCDominoClient client) {
		ReferenceQueue<? super IAPIObject> queue = referenceQueues.get(client);
		if (queue==null) {
			if (client.isRegisteredForGC()) {
				throw new ObjectDisposedException("Domino Client is already closed");
			}
			else {
				throw new DominoException("Domino Client not registered for GC yet");
			}
		}
		return queue;
	}
	
	/**
	 * Adds a garbage collection listener for a Domino client
	 * 
	 * @param client Domino client
	 * @param listener listener to add
	 */
	public static void addListener(IGCDominoClient client, ICAPIGarbageCollectorListener listener) {
		List<ICAPIGarbageCollectorListener> listeners = gcListenerByClient.get(client);
		if (listeners==null) {
			listeners = new ArrayList<>();
			gcListenerByClient.put(client, listeners);
		}
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	/**
	 * Removes a garbage collection listener for a Domino client
	 * 
	 * @param client Domino client
	 * @param listener listener to remove
	 */
	public static void removeListener(IGCDominoClient client, ICAPIGarbageCollectorListener listener) {
		List<ICAPIGarbageCollectorListener> listeners = gcListenerByClient.get(client);
		if (listeners!=null) {
			listeners.remove(listener);
		}
	}
	
	/**
	 * Sets up a {@link ReferenceQueue} to track unreferenced API objects
	 * 
	 * @param client Domino client
	 */
	public static void registerDominoClient(IGCDominoClient client) {
		if (referenceQueues.containsKey(client)) {
			throw new DominoException(0, "Duplicate Domino Client registration");
		}
		referenceQueues.put(client, new ReferenceQueue<>());
		client.markRegisteredForGC();
	}
	
	/**
	 * Cleans up and removes the {@link ReferenceQueue} for a Domino Client
	 * 
	 * @param client Domino client
	 */
	public static void unregisterDominoClient(IGCDominoClient client) {
		if (!referenceQueues.containsKey(client)) {
			throw new DominoException(0, "Domino Client was not registered");
		}
		gc(client);
		
		referenceQueues.remove(client);
		dominoClientAllocationsByParent.remove(client);
		gcListenerByClient.remove(client);
	}
	
	/**
	 * Checks if there are any C resources for garbage collected
	 * Java API objects that can be disposed as well.
	 * 
	 * @param client current Domino client to run the GC on
	 */
	public static void gc(IGCDominoClient client) {
		ReferenceQueue<? super IAPIObject> queue = getReferenceQueueForClient(client);
		APIObjectAllocations currAlloc;
		List<ICAPIGarbageCollectorListener> listeners = gcListenerByClient.get(client);
		
		if (listeners!=null) {
			for (ICAPIGarbageCollectorListener currListener : listeners) {
				currListener.startFlushingRefQueue(client);
			}
		}
		
		while ((currAlloc = (APIObjectAllocations) queue.poll())!=null) {
			if (listeners!=null) {
				for (ICAPIGarbageCollectorListener currListener : listeners) {
					currListener.unreferencedAPIObjectFound(client, currAlloc);
				}
			}

			dispose(client, currAlloc, 0);
		}
		
		if (listeners!=null) {
			for (ICAPIGarbageCollectorListener currListener : listeners) {
				currListener.endFlushingRefQueue(client);
			}
		}
	}
	
	/**
	 * Registers a new API object
	 * 
	 * @param parent parent API object
	 * @param obj new API object to register
	 */
	public static void registerNewAPIObject(IAPIObject parent, IAPIObject obj) {
		DominoClient client = obj.getParentDominoClient();
		if(!(client instanceof IGCDominoClient)) {
			throw new IllegalArgumentException("DominoClient must implement IGCDominoClient");
		}
		
		APIObjectAllocations objectAllocations = obj.getAdapter(APIObjectAllocations.class);
		if (objectAllocations==null) {
			throw new DominoException(0, MessageFormat.format(
				"Object is expected to return an implementation of APIObjectAllocations for resource tracking: {0}",
				obj.getClass().getName()
			));
		}
		APIObjectAllocations parentObjectAllocations = parent.getAdapter(APIObjectAllocations.class);
		if (parentObjectAllocations==null) {
			throw new DominoException(0, MessageFormat.format(
				"Parent object is expected to return an implementation of APIObjectAllocations for resource tracking: {0}",
				parent.getClass().getName()
			));
		}
		
		Map<APIObjectAllocations, List<APIObjectAllocations>> allocationsByParent = dominoClientAllocationsByParent.get(client);
		if (allocationsByParent==null) {
			allocationsByParent = new HashMap<>();
			dominoClientAllocationsByParent.put((IGCDominoClient)client, allocationsByParent);
		}

		List<APIObjectAllocations> allocationsForParent = allocationsByParent.get(parentObjectAllocations);
		if (allocationsForParent==null) {
			allocationsForParent = new ArrayList<>();
			allocationsByParent.put(parentObjectAllocations, allocationsForParent);
		}
		
		if (!allocationsForParent.contains(objectAllocations)) {
			allocationsForParent.add(objectAllocations);
		}
		
		List<ICAPIGarbageCollectorListener> listeners = gcListenerByClient.get(client);
		if (listeners!=null) {
			for (ICAPIGarbageCollectorListener currListener : listeners) {
				try {
					currListener.newAPIObjectCreated(parent, obj);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		IGCControl gcCtrl = client.getAdapter(IGCControl.class);
		if (gcCtrl!=null) {
			GCAction action = gcCtrl.objectAllocated(parent, obj);
			if (action == GCAction.FLUSH_REFQUEUE) {
				gc((IGCDominoClient)client);
			}
		}
	}

	/**
	 * Recursive disposal of an {@link IAPIObject}'s children and of the object itself.
	 * 
	 * @param baseAPIObject api object to dispose
	 */
	public static void dispose(IAPIObject baseAPIObject) {
		APIObjectAllocations objectAllocations = baseAPIObject.getAdapter(APIObjectAllocations.class);
		if (objectAllocations!=null) {
			DominoClient client = baseAPIObject.getParentDominoClient();
			dispose(client, objectAllocations, 0);
		}
	}
	
	/**
	 * Disposable of a {@link DominoClient}'s children and the client itself
	 * 
	 * @param client domino client
	 */
	public static void dispose(IGCDominoClient client) {
		APIObjectAllocations clientAllocations = (APIObjectAllocations)client.getAdapter(APIObjectAllocations.class);
		dispose(client, clientAllocations, 0);
	}
	
	/**
	 * Internal recursive disposal of an {@link APIObjectAllocations} tree structure.
	 * 
	 * @param client Domino client
	 * @param allocations allocations to dispose (we first dispose the child allocations in reverse order)
	 * @param depth contains 0 for the first tree level
	 */
	private static void dispose(DominoClient client, APIObjectAllocations allocations, int depth) {
		if(allocations == null) {
			return;
		}
		List<ICAPIGarbageCollectorListener> listeners = gcListenerByClient.get(client);
		if (listeners!=null) {
			for (ICAPIGarbageCollectorListener currListener : listeners) {
				try {
					currListener.startDispose(client, allocations, depth);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		//dispose children first
		Map<APIObjectAllocations,List<APIObjectAllocations>> allocationsByParent = dominoClientAllocationsByParent.get(client);
		if (allocationsByParent!=null) {
			List<APIObjectAllocations> childAllocations = allocationsByParent.get(allocations);
			if (childAllocations!=null) {
				if (!childAllocations.isEmpty()) {
					APIObjectAllocations[] childAllocationsCopy = childAllocations.toArray(new APIObjectAllocations[childAllocations.size()]);
					
					int childAllocationsSize = childAllocationsCopy.length;
					
					for (int i=childAllocationsSize-1; i>=0; i--) {
						try {
							APIObjectAllocations currChild = childAllocationsCopy[i];
							dispose(client, currChild, depth+1);
						}
						catch (Exception e) {
							throw new DominoException(MessageFormat.format("Error disposing {0}", childAllocationsCopy[i]), e);
						}
					}
					childAllocations.clear();
				}

				allocationsByParent.remove(allocations);
			}
		}
		
		if (!allocations.isDisposed()) {
			APIObjectAllocations parentAllocations = allocations.getParentAllocations();
			
			if(!skipDispose) {
				allocations.dispose();
			}
			
			if (parentAllocations!=null && allocationsByParent!=null) {
				List<APIObjectAllocations> parentsChildAllocations = allocationsByParent.get(parentAllocations);
				if (parentsChildAllocations!=null && parentsChildAllocations.contains(allocations)) {
					parentsChildAllocations.remove(allocations);
					
					if (parentsChildAllocations.isEmpty()) {
						allocationsByParent.remove(parentAllocations);
					}
				}
			}
		}

		if (listeners != null) {
			for (ICAPIGarbageCollectorListener currListener : listeners) {
				try {
					currListener.endDispose(client, allocations, depth);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
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
		 * Called when a new API object has been registered
		 * 
		 * @param parent parent API object
		 * @param obj new API object
		 */
		void newAPIObjectCreated(IAPIObject parent, IAPIObject obj);
		
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
		 * @param client Domino client
		 * @param apiObjectAllocations allocations that is about to be disposed
		 */
		void unreferencedAPIObjectFound(DominoClient client, APIObjectAllocations apiObjectAllocations);
		
		/**
		 * Called before diving into the child tree structure to dispose all child allocations
		 * (in reverse order) and disposing the allocations itself.
		 * 
		 * @param client Domino Client
		 * @param apiObjectAllocations allocations about to be disposed
		 * @param depth tree depths starting with 0 (can be used for indentation)
		 */
		void startDispose(DominoClient client, APIObjectAllocations apiObjectAllocations, int depth);
		
		/**
		 * Called after the child tree structure and the allocations itself have been
		 * disposed.
		 * 
		 * @param client Domino Client
		 * @param apiObjectAllocations disposed allocations
		 * @param depth tree depths starting with 0 (can be used for indentation)
		 */
		void endDispose(DominoClient client, APIObjectAllocations apiObjectAllocations, int depth);
		
		/**
		 * Method is called after flushing the reference queue
		 * of objects to be GCed
		 * 
		 * @param client Domino client
		 */
		void endFlushingRefQueue(DominoClient client);
		
	}
	
	public static class CAPIGarbageCollectorListenerAdapter implements ICAPIGarbageCollectorListener {

		@Override
		public void newAPIObjectCreated(IAPIObject parent, IAPIObject obj) {
		}

		@Override
		public void startFlushingRefQueue(DominoClient client) {
		}

		@Override
		public void unreferencedAPIObjectFound(DominoClient client, APIObjectAllocations apiObjectAllocations) {
		}

		@Override
		public void startDispose(DominoClient client, APIObjectAllocations apiObjectAllocations, int depth) {
		}

		@Override
		public void endDispose(DominoClient client, APIObjectAllocations apiObjectAllocations, int depth) {
		}

		@Override
		public void endFlushingRefQueue(DominoClient client) {
		}
		
	}
}
