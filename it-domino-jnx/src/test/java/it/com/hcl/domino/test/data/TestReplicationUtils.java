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
package it.com.hcl.domino.test.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.admin.replication.GetDocumentsMode;
import com.hcl.domino.admin.replication.ReplicaInfo;
import com.hcl.domino.admin.replication.ReplicaInfo.Priority;
import com.hcl.domino.admin.replication.Replication;
import com.hcl.domino.admin.replication.Replication.Action;
import com.hcl.domino.admin.replication.Replication.IDocumentOpenCallback;
import com.hcl.domino.admin.replication.Replication.IFolderAddCallback;
import com.hcl.domino.admin.replication.Replication.IGetDocumentsCallback;
import com.hcl.domino.admin.replication.Replication.IObjectAllocCallback;
import com.hcl.domino.admin.replication.Replication.IObjectWriteCallback;
import com.hcl.domino.admin.replication.Replication.ReplicationHistoryFlags;
import com.hcl.domino.admin.replication.ReplicationHistorySummary;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Database.OpenDocumentMode;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.Item;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.misc.Pair;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestReplicationUtils extends AbstractNotesRuntimeTest {

	private void debug(Object msg) {
//		System.out.println(msg);
	}

//	@Test
	public void testReplicationHistory() throws Exception {
		DominoClient client = getClient();
		Replication replication = client.getReplication();
		Database db = client.openDatabase("fakenames.nsf");
		List<ReplicationHistorySummary> history = replication.getReplicationHistory(db, EnumSet.of(ReplicationHistoryFlags.SORT_BY_DATE));
		for (ReplicationHistorySummary currEntry : history) {
			System.out.println(currEntry);
		}
	}
	
	@Test
	public void testReplicationInfo() throws Exception {
		withTempDb((database) -> {
			DominoClient client = getClient();
			Replication replication = client.getReplication();
			
			ReplicaInfo replicaInfo = replication.getReplicaInfo(database);
			
			assertEquals(Priority.MEDIUM, replicaInfo.getPriority());

			debug(replicaInfo);

			//check defaults for new database
			assertEquals(Priority.MEDIUM, replicaInfo.getPriority());
			assertNull(replicaInfo.getCutOff().orElse(null));
			assertEquals(90, replicaInfo.getCutOffInterval());
			
			//modify replica info
			
			Priority newPrio = Priority.HIGH;
			Instant newCutOff = Instant.now().minus(1, ChronoUnit.DAYS).with(ChronoField.MILLI_OF_SECOND, 0);
			int newCutOffInterval = 35;

			replicaInfo.setPriority(newPrio);
			replicaInfo.setCutOff(newCutOff);
			replicaInfo.setCutOffInterval(newCutOffInterval);
			
			//compare the changes
			assertEquals(newPrio, replicaInfo.getPriority());
			assertEquals(newCutOffInterval, replicaInfo.getCutOffInterval());
			assertNotNull(replicaInfo.getCutOff().orElse(null));
			assertEquals(newCutOff, replicaInfo.getCutOff().get().toOffsetDateTime().toInstant());
			
			String oldReplId = replicaInfo.getReplicaID();
			assertNotNull(oldReplId);
			assertNotEquals("", oldReplId);
			
			String newReplId = replicaInfo.setNewReplicaId();
			assertEquals(newReplId, replicaInfo.getReplicaID());
			
			replication.setReplicaInfo(database, replicaInfo);
			
			//reload replica info
			ReplicaInfo replicaInfoChanged = replication.getReplicaInfo(database);
			debug(replicaInfoChanged);

			assertEquals(newPrio, replicaInfoChanged.getPriority());
			assertEquals(newCutOffInterval, replicaInfoChanged.getCutOffInterval());
			assertNotNull(replicaInfoChanged.getCutOff().orElse(null));
			assertEquals(newCutOff, replicaInfoChanged.getCutOff().get().toOffsetDateTime().toInstant());
			assertEquals(newReplId, replicaInfo.getReplicaID());
		});
	}
	
	/**
	 * This test uses Replication.getDocuments(...) to get a stream of documents with
	 * changes since a specified sequence numbers.
	 * 
	 * @throws Exception in case of errors
	 */
	@Test
	public void testGetDocuments() throws Exception {
		withTempDb((database) -> {
			DominoClient client = getClient();
			Replication replication = client.getReplication();
			
			int nrOfDocs = 40;
			
			List<Pair<String,Integer>> unidsAndNoteIds = generateNABPersons(database, nrOfDocs);
			
			//add a bogus first entry to check if a load error is returned
			unidsAndNoteIds.add(0, new Pair<>("012345678901234567890123456789012", 1000200));
			
			nrOfDocs = unidsAndNoteIds.size();
			
			List<Integer> noteIds = unidsAndNoteIds.stream().map((pair) -> {return pair.getValue2();}).collect(Collectors.toList());
			int[] noteIdsArr = new int[noteIds.size()];
			for (int i=0; i<nrOfDocs; i++) {
				noteIdsArr[i] = noteIds.get(i).intValue();
			}
			List<String> unids = unidsAndNoteIds.stream().map((pair) -> {return pair.getValue1();}).collect(Collectors.toList());
			
			@SuppressWarnings("unchecked")
			Set<OpenDocumentMode>[] docOpenFlags = new Set[nrOfDocs];
			for (int i=0; i<nrOfDocs; i++) {
				docOpenFlags[i] = EnumSet.noneOf(OpenDocumentMode.class);
			}

			Set<GetDocumentsMode> controlFlags = EnumSet.of(GetDocumentsMode.PRESERVE_ORDER,
					GetDocumentsMode.SEND_OBJECTS,
					GetDocumentsMode.GET_FOLDER_ADDS);

			int[] seqNumAfterCreation = new int[nrOfDocs];
			
			//collect the initial sequence numbers (probably 1)
			for (int i=1; i<nrOfDocs; i++) {
				Document doc = database.getDocumentById(noteIds.get(i)).get();
				seqNumAfterCreation[i] = doc.getSequenceNumber();
			}
			
			{
				// use [0, 0, ...] as sequence numbers to get all doc items
				int[] sinceSeqNumArr = new int[nrOfDocs];

				Database objectDb = null;

				IGetDocumentsCallback getDocumentsCallback = totalSize -> {
					debug("gettingDocuments: "+totalSize);
					
					return Action.CONTINUE;
				};

				AtomicInteger receivedDocCount = new AtomicInteger();
				
				IDocumentOpenCallback docOpenCallback = (doc, noteId, status) -> {
					assertEquals(noteIds.get(receivedDocCount.get()), noteId);
					
					if (receivedDocCount.get() == 0) {
						//check for bogus entry
						assertNull(doc);
						assertEquals(551, status.get().getId());
					}
					else {
						assertNotNull(doc);
						assertFalse(status.isPresent());
						assertEquals(unids.get(receivedDocCount.get()), doc.getUNID());
						
						//check if doc contains all items with type
						Item itm = doc.getFirstItem("Lastname").orElse(null);
						assertEquals(ItemDataType.TYPE_TEXT, itm.getType());
						
						Item itm2 = doc.getFirstItem("NewField").orElse(null);
						assertNull(itm2);
					}
					
					debug("documentOpened: "+doc+", noteId="+noteId+", status="+status);
					
					receivedDocCount.incrementAndGet();
					
					return Action.CONTINUE;
				};
				
				IObjectAllocCallback objectAllocCallback = (doc, oldRRV, status, objectSize) -> {
					debug("objectAllocated: doc="+doc+", oldRRV="+oldRRV+", status="+status+", objectSize="+objectSize);

					return Action.CONTINUE;
				};
				
				IObjectWriteCallback objectWriteCallback = (doc, oldRRV, status, buffer, bufferSize) -> {
					debug("objectChunkWritten: doc="+doc+", oldRRV="+oldRRV+", status="+status+", buffersize="+bufferSize);

					return Action.CONTINUE;
				};
				
				DominoDateTime folderSinceTime = null;
				
				IFolderAddCallback folderAddCallback = unid -> {
					debug("addedToFolder: unid="+unid);
					
					return Action.CONTINUE;
				};
				
				replication.getDocuments(database, noteIdsArr, docOpenFlags, sinceSeqNumArr, controlFlags,
						objectDb, getDocumentsCallback, docOpenCallback, objectAllocCallback,
						objectWriteCallback, folderSinceTime, folderAddCallback);
				
			}

			debug("Modifying all documents");
			
			for (int i=1; i<nrOfDocs; i++) {
				Document doc = database.getDocumentById(noteIds.get(i)).get();
				doc.replaceItemValue("NewField", "123");
				//bumps the doc sequence number
				doc.save();
			}
			
			//now lets run the read operation again but this time we just
			//want to know which items have changed since creating the documents
			
			debug("Fetching diff from creation with sequence numbers: "+Arrays.toString(seqNumAfterCreation));

			{
				//first run, use seqNum = 0 to get full documents
				int[] sinceSeqNumArr = seqNumAfterCreation;

				Database objectDb = null;

				IGetDocumentsCallback getDocumentsCallback = totalSize -> {
					debug("gettingDocuments: "+totalSize);
					
					return Action.CONTINUE;
				};

				AtomicInteger receivedDocCount = new AtomicInteger();
				
				IDocumentOpenCallback docOpenCallback = (doc, noteId, status) -> {
					assertEquals(noteIds.get(receivedDocCount.get()), noteId);
					
					if (receivedDocCount.get() == 0) {
						//check for bogus entry
						assertNull(doc);
						assertEquals(551, status.get().getId());
					}
					else {
						assertNotNull(doc);
						assertFalse(status.isPresent());
						assertEquals(unids.get(receivedDocCount.get()), doc.getUNID());
					
						//the doc is expected to just contain TYPE_UNAVAILABLE typed
						//items for items that have not changed since the specified sequence number
						Item itm = doc.getFirstItem("Lastname").orElse(null);
						assertEquals(ItemDataType.TYPE_UNAVAILABLE, itm.getType());

						Item itm2 = doc.getFirstItem("NewField").orElse(null);
						assertEquals(ItemDataType.TYPE_TEXT, itm2.getType());
					}
					
					debug("documentOpened: "+doc+", noteId="+noteId+", status="+status);
					
					receivedDocCount.incrementAndGet();
					
					return Action.CONTINUE;
				};
				
				IObjectAllocCallback objectAllocCallback = (doc, oldRRV, status, objectSize) -> {
					debug("objectAllocated: doc="+doc+", oldRRV="+oldRRV+", status="+status+", objectSize="+objectSize);

					return Action.CONTINUE;
				};
				
				IObjectWriteCallback objectWriteCallback = (doc, oldRRV, status, buffer, bufferSize) -> {
					debug("objectChunkWritten: doc="+doc+", oldRRV="+oldRRV+", status="+status+", buffersize="+bufferSize);

					return Action.CONTINUE;
				};
				
				DominoDateTime folderSinceTime = null;
				
				IFolderAddCallback folderAddCallback = unid -> {
					debug("addedToFolder: unid="+unid);
					
					return Action.CONTINUE;
				};
				
				replication.getDocuments(database, noteIdsArr, docOpenFlags, sinceSeqNumArr, controlFlags,
						objectDb, getDocumentsCallback, docOpenCallback, objectAllocCallback,
						objectWriteCallback, folderSinceTime, folderAddCallback);

			}
		}
		);
	}
}
