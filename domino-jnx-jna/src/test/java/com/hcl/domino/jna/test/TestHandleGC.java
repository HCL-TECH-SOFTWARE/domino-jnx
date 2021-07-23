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
package com.hcl.domino.jna.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.CAPIGarbageCollector;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCControl;
import com.hcl.domino.commons.gc.CAPIGarbageCollector.CAPIGarbageCollectorListenerAdapter;
import com.hcl.domino.commons.gc.CAPIGarbageCollector.ICAPIGarbageCollectorListener;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.jna.JNADominoClient;
import com.hcl.domino.jna.data.JNADatabase;
import com.hcl.domino.jna.data.JNADocument;

@SuppressWarnings({ "nls", "unused", "rawtypes" })
public class TestHandleGC extends AbstractJNARuntimeTest {
	
	@Test
	public void testAutoQueueFlush() throws Exception {
		withTempDb((database) -> {
			DominoClient client = getClient();
			
			AtomicBoolean flushQueueDetected = new AtomicBoolean();
			
			CAPIGarbageCollectorListenerAdapter listener = new CAPIGarbageCollectorListenerAdapter() {
				@Override
				public void startFlushingRefQueue(DominoClient client) {
					flushQueueDetected.set(Boolean.TRUE);
				}
			};
			CAPIGarbageCollector.addListener((JNADominoClient) client, listener);
			
			//queue flush is expected to run after 50 object allocations by default
			for (int i=0; i<50; i++) {
				Document doc = database.createDocument();
			}
			
			assertTrue(flushQueueDetected.get());
		});
	}
	
	@Test
	public void testCAPIGarbageCollection() throws IOException {
		try(DominoClient client =  DominoClientBuilder.newDominoClient().build()) {
			//disable automatic ref queue flushing for this client
			IGCControl gcCtrl = client.getAdapter(IGCControl.class);
			assertNotNull(gcCtrl);
			gcCtrl.setThreshold(0);
			
			AtomicInteger createdObjects = new AtomicInteger();
			AtomicInteger detectedOrphans = new AtomicInteger();
			AtomicInteger disposedObjects = new AtomicInteger();

			ICAPIGarbageCollectorListener listener = new ICAPIGarbageCollectorListener() {

				@Override
				public void startFlushingRefQueue(DominoClient client) {
					System.out.println("Flushing GC ref queue");
				}
				
				@Override
				public void newAPIObjectCreated(IAPIObject parent, IAPIObject obj) {
					System.out.println("Registered new API object, parent="+parent+", child="+obj);
					createdObjects.incrementAndGet();
				}
				
				@Override
				public void unreferencedAPIObjectFound(DominoClient client, APIObjectAllocations apiObjectAllocations) {
					System.out.println(" Orphaned API object detected, allocations: "+apiObjectAllocations);
					detectedOrphans.incrementAndGet();
				}

				@Override
				public void startDispose(DominoClient client, APIObjectAllocations apiObjectAllocations, int depth) {
					System.out.println(StringUtil.repeat(' ', depth) + " Start disposing "+apiObjectAllocations);
				}

				@Override
				public void endDispose(DominoClient client, APIObjectAllocations apiObjectAllocations, int depth) {
					System.out.println(StringUtil.repeat(' ', depth) + " End disposing "+apiObjectAllocations);
					disposedObjects.incrementAndGet();	
				}
				
				@Override
				public void endFlushingRefQueue(DominoClient client) {
					System.out.println("Done flushing GC ref queue");
				}
				
			};
			CAPIGarbageCollector.addListener((JNADominoClient) client, listener);
			
			Database db = client.openDatabase("", "names.nsf");
			assertNotNull(db);
			assertEquals(2, createdObjects.get(), "1 API object created (db + nameslist)");
			
			String dbTitle = db.getTitle();
			assertNotNull(dbTitle);
			
			createdObjects.set(0);
			
			Document doc1 = db.createDocument();
			Document doc2 = db.createDocument();
			
			assertEquals(2, createdObjects.get(), "2 API objects created");

			detectedOrphans.set(0);
			
			System.out.println("Setting doc1 to null");
			doc1 = null;
			
			System.gc();
			//give background GC thread some time
			Thread.sleep(3000);
			
			System.out.println("Running CAPI GC");
			CAPIGarbageCollector.gc((JNADominoClient) client);
			
			assertEquals(1, detectedOrphans.get(), "1 orphaned API object detected");

			//we need to access doc2/db here, otherwise the JDK seems to prematurely dispose
			//them and the test above fails with 3 detected orphans (?!)
			assertEquals(false, ((JNADocument)doc2).isDisposed(), "doc2 not disposed yet");
			assertEquals(false, ((JNADatabase)db).isDisposed(), "DB not disposed yet");
			
			detectedOrphans.set(0);

			System.out.println("Setting doc2 to null");
			doc2 = null;
			System.out.println("Setting db to null");
			db = null;
			
			System.out.println("Running CAPI GC");
			System.gc();
			//give background GC thread some time
			Thread.sleep(3000);
			CAPIGarbageCollector.gc((JNADominoClient) client);

			assertEquals(2, detectedOrphans.get(), "2 orphaned API object detected");

			System.out.println("done");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
