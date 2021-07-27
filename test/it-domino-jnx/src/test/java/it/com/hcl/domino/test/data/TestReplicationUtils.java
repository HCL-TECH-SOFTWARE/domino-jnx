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

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
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

  private void debug(final Object msg) {
    // System.out.println(msg);
  }

  /**
   * This test uses Replication.getDocuments(...) to get a stream of documents
   * with
   * changes since a specified sequence numbers.
   * 
   * @throws Exception in case of errors
   */
  @Test
  public void testGetDocuments() throws Exception {
    this.withTempDb(database -> {
      final DominoClient client = this.getClient();
      final Replication replication = client.getReplication();

      int nrOfDocs = 40;

      final List<Pair<String, Integer>> unidsAndNoteIds = AbstractNotesRuntimeTest.generateNABPersons(database, nrOfDocs);

      // add a bogus first entry to check if a load error is returned
      unidsAndNoteIds.add(0, new Pair<>("012345678901234567890123456789012", 1000200));

      nrOfDocs = unidsAndNoteIds.size();

      final List<Integer> noteIds = unidsAndNoteIds.stream().map((pair) -> {
        return pair.getValue2();
      }).collect(Collectors.toList());
      final int[] noteIdsArr = new int[noteIds.size()];
      for (int i = 0; i < nrOfDocs; i++) {
        noteIdsArr[i] = noteIds.get(i).intValue();
      }
      final List<String> unids = unidsAndNoteIds.stream().map((pair) -> {
        return pair.getValue1();
      }).collect(Collectors.toList());

      @SuppressWarnings("unchecked")
      final Set<OpenDocumentMode>[] docOpenFlags = new Set[nrOfDocs];
      for (int i = 0; i < nrOfDocs; i++) {
        docOpenFlags[i] = EnumSet.noneOf(OpenDocumentMode.class);
      }

      final Set<GetDocumentsMode> controlFlags = EnumSet.of(GetDocumentsMode.PRESERVE_ORDER,
          GetDocumentsMode.SEND_OBJECTS,
          GetDocumentsMode.GET_FOLDER_ADDS);

      final int[] seqNumAfterCreation = new int[nrOfDocs];

      // collect the initial sequence numbers (probably 1)
      for (int i = 1; i < nrOfDocs; i++) {
        final Document doc = database.getDocumentById(noteIds.get(i)).get();
        seqNumAfterCreation[i] = doc.getSequenceNumber();
      }

      {
        // use [0, 0, ...] as sequence numbers to get all doc items
        final int[] sinceSeqNumArr = new int[nrOfDocs];

        final Database objectDb = null;

        final IGetDocumentsCallback getDocumentsCallback = totalSize -> {
          this.debug("gettingDocuments: " + totalSize);

          return Action.CONTINUE;
        };

        final AtomicInteger receivedDocCount = new AtomicInteger();

        final IDocumentOpenCallback docOpenCallback = (doc, noteId, status) -> {
          Assertions.assertEquals(noteIds.get(receivedDocCount.get()), noteId);

          if (receivedDocCount.get() == 0) {
            // check for bogus entry
            Assertions.assertNull(doc);
            Assertions.assertEquals(551, status.get().getId());
          } else {
            Assertions.assertNotNull(doc);
            Assertions.assertFalse(status.isPresent());
            Assertions.assertEquals(unids.get(receivedDocCount.get()), doc.getUNID());

            // check if doc contains all items with type
            final Item itm = doc.getFirstItem("Lastname").orElse(null);
            Assertions.assertEquals(ItemDataType.TYPE_TEXT, itm.getType());

            final Item itm2 = doc.getFirstItem("NewField").orElse(null);
            Assertions.assertNull(itm2);
          }

          this.debug("documentOpened: " + doc + ", noteId=" + noteId + ", status=" + status);

          receivedDocCount.incrementAndGet();

          return Action.CONTINUE;
        };

        final IObjectAllocCallback objectAllocCallback = (doc, oldRRV, status, objectSize) -> {
          this.debug("objectAllocated: doc=" + doc + ", oldRRV=" + oldRRV + ", status=" + status + ", objectSize=" + objectSize);

          return Action.CONTINUE;
        };

        final IObjectWriteCallback objectWriteCallback = (doc, oldRRV, status, buffer, bufferSize) -> {
          this.debug("objectChunkWritten: doc=" + doc + ", oldRRV=" + oldRRV + ", status=" + status + ", buffersize=" + bufferSize);

          return Action.CONTINUE;
        };

        final DominoDateTime folderSinceTime = null;

        final IFolderAddCallback folderAddCallback = unid -> {
          this.debug("addedToFolder: unid=" + unid);

          return Action.CONTINUE;
        };

        replication.getDocuments(database, noteIdsArr, docOpenFlags, sinceSeqNumArr, controlFlags,
            objectDb, getDocumentsCallback, docOpenCallback, objectAllocCallback,
            objectWriteCallback, folderSinceTime, folderAddCallback);

      }

      this.debug("Modifying all documents");

      for (int i = 1; i < nrOfDocs; i++) {
        final Document doc = database.getDocumentById(noteIds.get(i)).get();
        doc.replaceItemValue("NewField", "123");
        // bumps the doc sequence number
        doc.save();
      }

      // now lets run the read operation again but this time we just
      // want to know which items have changed since creating the documents

      this.debug("Fetching diff from creation with sequence numbers: " + Arrays.toString(seqNumAfterCreation));

      {
        // first run, use seqNum = 0 to get full documents
        final int[] sinceSeqNumArr = seqNumAfterCreation;

        final Database objectDb = null;

        final IGetDocumentsCallback getDocumentsCallback = totalSize -> {
          this.debug("gettingDocuments: " + totalSize);

          return Action.CONTINUE;
        };

        final AtomicInteger receivedDocCount = new AtomicInteger();

        final IDocumentOpenCallback docOpenCallback = (doc, noteId, status) -> {
          Assertions.assertEquals(noteIds.get(receivedDocCount.get()), noteId);

          if (receivedDocCount.get() == 0) {
            // check for bogus entry
            Assertions.assertNull(doc);
            Assertions.assertEquals(551, status.get().getId());
          } else {
            Assertions.assertNotNull(doc);
            Assertions.assertFalse(status.isPresent());
            Assertions.assertEquals(unids.get(receivedDocCount.get()), doc.getUNID());

            // the doc is expected to just contain TYPE_UNAVAILABLE typed
            // items for items that have not changed since the specified sequence number
            final Item itm = doc.getFirstItem("Lastname").orElse(null);
            Assertions.assertEquals(ItemDataType.TYPE_UNAVAILABLE, itm.getType());

            final Item itm2 = doc.getFirstItem("NewField").orElse(null);
            Assertions.assertEquals(ItemDataType.TYPE_TEXT, itm2.getType());
          }

          this.debug("documentOpened: " + doc + ", noteId=" + noteId + ", status=" + status);

          receivedDocCount.incrementAndGet();

          return Action.CONTINUE;
        };

        final IObjectAllocCallback objectAllocCallback = (doc, oldRRV, status, objectSize) -> {
          this.debug("objectAllocated: doc=" + doc + ", oldRRV=" + oldRRV + ", status=" + status + ", objectSize=" + objectSize);

          return Action.CONTINUE;
        };

        final IObjectWriteCallback objectWriteCallback = (doc, oldRRV, status, buffer, bufferSize) -> {
          this.debug("objectChunkWritten: doc=" + doc + ", oldRRV=" + oldRRV + ", status=" + status + ", buffersize=" + bufferSize);

          return Action.CONTINUE;
        };

        final DominoDateTime folderSinceTime = null;

        final IFolderAddCallback folderAddCallback = unid -> {
          this.debug("addedToFolder: unid=" + unid);

          return Action.CONTINUE;
        };

        replication.getDocuments(database, noteIdsArr, docOpenFlags, sinceSeqNumArr, controlFlags,
            objectDb, getDocumentsCallback, docOpenCallback, objectAllocCallback,
            objectWriteCallback, folderSinceTime, folderAddCallback);

      }
    });
  }

  // @Test
  public void testReplicationHistory() throws Exception {
    final DominoClient client = this.getClient();
    final Replication replication = client.getReplication();
    final Database db = client.openDatabase("fakenames.nsf");
    final List<ReplicationHistorySummary> history = replication.getReplicationHistory(db,
        EnumSet.of(ReplicationHistoryFlags.SORT_BY_DATE));
    for (final ReplicationHistorySummary currEntry : history) {
      System.out.println(currEntry);
    }
  }

  @Test
  public void testReplicationInfo() throws Exception {
    this.withTempDb(database -> {
      final DominoClient client = this.getClient();
      final Replication replication = client.getReplication();

      final ReplicaInfo replicaInfo = replication.getReplicaInfo(database);

      Assertions.assertEquals(Priority.MEDIUM, replicaInfo.getPriority());

      this.debug(replicaInfo);

      // check defaults for new database
      Assertions.assertEquals(Priority.MEDIUM, replicaInfo.getPriority());
      Assertions.assertNull(replicaInfo.getCutOff().orElse(null));
      Assertions.assertEquals(90, replicaInfo.getCutOffInterval());

      // modify replica info

      final Priority newPrio = Priority.HIGH;
      final Instant newCutOff = Instant.now().minus(1, ChronoUnit.DAYS).with(ChronoField.MILLI_OF_SECOND, 0);
      final int newCutOffInterval = 35;

      replicaInfo.setPriority(newPrio);
      replicaInfo.setCutOff(newCutOff);
      replicaInfo.setCutOffInterval(newCutOffInterval);

      // compare the changes
      Assertions.assertEquals(newPrio, replicaInfo.getPriority());
      Assertions.assertEquals(newCutOffInterval, replicaInfo.getCutOffInterval());
      Assertions.assertNotNull(replicaInfo.getCutOff().orElse(null));
      Assertions.assertEquals(newCutOff, replicaInfo.getCutOff().get().toOffsetDateTime().toInstant());

      final String oldReplId = replicaInfo.getReplicaID();
      Assertions.assertNotNull(oldReplId);
      Assertions.assertNotEquals("", oldReplId);

      final String newReplId = replicaInfo.setNewReplicaId();
      Assertions.assertEquals(newReplId, replicaInfo.getReplicaID());

      replication.setReplicaInfo(database, replicaInfo);

      // reload replica info
      final ReplicaInfo replicaInfoChanged = replication.getReplicaInfo(database);
      this.debug(replicaInfoChanged);

      Assertions.assertEquals(newPrio, replicaInfoChanged.getPriority());
      Assertions.assertEquals(newCutOffInterval, replicaInfoChanged.getCutOffInterval());
      Assertions.assertNotNull(replicaInfoChanged.getCutOff().orElse(null));
      Assertions.assertEquals(newCutOff, replicaInfoChanged.getCutOff().get().toOffsetDateTime().toInstant());
      Assertions.assertEquals(newReplId, replicaInfo.getReplicaID());
    });
  }
}
