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
package com.hcl.domino.admin.replication;

import java.nio.ByteBuffer;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.DominoException;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Database.OpenDocumentMode;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.misc.INumberEnum;

/**
 * @author t.b.d
 */
public interface Replication {

  public enum Action {
    CONTINUE, STOP
  }

  /**
   * This function is called for each document retrieved. If non-NULL, this is
   * called for each document
   * after all objects have been retrieved (if
   * {@link GetDocumentsMode#SEND_OBJECTS} is specified)
   */
  @FunctionalInterface
  public interface IDocumentOpenCallback {

    /**
     * Called for each document retrieved
     *
     * @param doc    document
     * @param noteId note ID
     * @param status if not empty, contains an error that occurred opening the
     *               document
     * @return return {@link Action#STOP} to stop reading document data
     */
    Action documentOpened(Document doc, int noteId, Optional<DominoException> status);

  }

  /**
   * {@link GetDocumentsMode#GET_FOLDER_ADDS} is specified but
   * {@link GetDocumentsMode#APPLY_FOLDER_ADDS} is not,
   * this function is called for each document after the
   * {@link IDocumentOpenCallback} function is called
   */
  @FunctionalInterface
  public interface IFolderAddCallback {

    /**
     * Called for each document after the {@link IDocumentOpenCallback} function is
     * called
     *
     * @param unid UNID
     * @return return {@link Action#STOP} to stop reading document data
     */
    Action addedToFolder(String unid);

  }

  /**
   * Called once before any others but only if going to a server that is R6 or
   * greater.
   * If {@link GetDocumentsMode#ORDER_BY_SIZE} is specified in options the two
   * DWORD parameters,
   * TotalSizeLow and TotalSizeHigh, provide the approximate total size of the
   * bytes to be returned in the documents and objects. These values are intended
   * to be used for progress indication
   */
  @FunctionalInterface
  public interface IGetDocumentsCallback {

    /**
     * Called with the estimated return data size
     *
     * @param totalSize The approximate total number of bytes that are going to be
     *                  returned in the document stream
     * @return return {@link Action#STOP} to stop reading document data
     */
    Action gettingDocuments(long totalSize);

  }

  /**
   * If {@link GetDocumentsMode#SEND_OBJECTS} is specified and
   * <code>objectDb</code> is not NULL,
   * this function is called exactly once for each object to provide the caller
   * with information
   * about the object's size and ObjectID. The intent is to allow for the physical
   * allocation
   * for the object if need be. It is called before the
   * {@link IDocumentOpenCallback} for the corresponding document
   */
  @FunctionalInterface
  public interface IObjectAllocCallback {

    /**
     * Called exactly once for each object to provide the caller with information
     * about the object's size and ObjectID
     *
     * @param doc        document containing the object
     * @param oldRRV     Old Record Relocation Vector
     * @param status     if not empty, contains any error that occurred
     * @param objectSize size of allocated object
     * @return return {@link Action#STOP} to stop reading document data
     */
    Action objectAllocated(Document doc, int oldRRV, Optional<DominoException> status, int objectSize);

  }

  /**
   * This function is called for each "chunk" of each object if
   * {@link GetDocumentsMode#SEND_OBJECTS}
   * is specified and <code>objectDb</code> is not NULL. For each object this will
   * be
   * called one or more times
   */
  @FunctionalInterface
  public interface IObjectWriteCallback {

    /**
     * Called for each "chunk" of each object
     *
     * @param doc        document containing the object
     * @param oldRRV     Old Record Relocation Vector
     * @param status     if not empty, contains any error that occurred
     * @param buffer     buffer containing a "chunk" of the object (as this function
     *                   will be called one or more time for each object)
     * @param bufferSize size of byte buffer
     * @return return {@link Action#STOP} to stop reading document data
     */
    Action objectChunkWritten(Document doc, int oldRRV, Optional<DominoException> status, ByteBuffer buffer,
        int bufferSize);

  }

  /**
   * Optional flags that can be used with
   * {@link Replication#getReplicationHistory(Database, Set)}.
   */
  public enum ReplicationHistoryFlags implements INumberEnum<Integer> {
    /** Don't copy wild card entries */
    REMOVE_WILDCARDS(0x00000001),

    SORT_BY_DATE(0x00000002),

    ONLY_COMPLETE(0x00000004);

    private final Integer m_value;

    ReplicationHistoryFlags(final Integer value) {
      this.m_value = value;
    }

    @Override
    public long getLongValue() {
      return this.m_value & 0xffffffff;
    }

    @Override
    public Integer getValue() {
      return this.m_value;
    }
  }

  /** Don't copy wild card entries */
  int REPLHIST_REMOVE_WILDCARDS = 0x00000001;

  /** Sort by date. Default is by server name */
  int REPLHIST_SORT_BY_DATE = 0x00000002;

  /** Only return complete entries */
  int REPLHIST_ONLY_COMPLETE = 0x00000004;

  /**
   * Resets the replication history of a database so enforce a full replication
   *
   * @param db database
   */
  void clearReplicationHistory(Database db);

  /**
   * This function will return a stream of documents to the caller through several
   * callback functions.<br>
   * <br>
   * It can be used to quickly and incrementally read a large number of docs from
   * a database,
   * skipping the transfer of item values where the item's sequence number is
   * lower or equal a specified value
   * (see <code>sinceSeqNum</code> parameter). In that case, items are returns
   * with type {@link ItemDataType#TYPE_UNAVAILABLE}
   *
   * @param db                   database
   * @param noteIds              note ID(s) of doc(s) to be retrieved
   * @param docOpenFlags         flags that control the manner in which the
   *                             document is opened. This, in turn, controls what
   *                             information about the doc is available to you and
   *                             how it is structured. The flags are defined in
   *                             {@link OpenDocumentMode} and may be or'ed
   *                             together to combine functionality.
   * @param sinceSeqNum          since sequence number; controls which fields are
   *                             accessible in the returned documents; e.g. if you
   *                             specify a very high value, items with lower or
   *                             equal sequence number have the type
   *                             {@link ItemDataType#TYPE_UNAVAILABLE}
   * @param controlFlags         Flags that control the actions of the function
   *                             during doc retrieval. The flags are defined in
   *                             {@link GetDocumentsMode}.
   * @param objectDb             If binary objects are being retrieved
   *                             ({@link GetDocumentsMode#SEND_OBJECTS} is used)
   *                             and this value is not NULL, objects will be
   *                             stored in this database and attached to the
   *                             incoming docs prior to
   *                             {@link IDocumentOpenCallback} being called.
   * @param getDocumentsCallback Called once before any others but only if going
   *                             to a server that is R6 or greater. If
   *                             {@link GetDocumentsMode#ORDER_BY_SIZE} is
   *                             specified in options totalSize provides the
   *                             approximate total size of the bytes to be
   *                             returned in the documents and objects. These
   *                             values are intended to be used for progress
   *                             indication
   * @param docOpenCallback      This function is called for each document
   *                             retrieved. If non-NULL, this is called for each
   *                             document after all objects have been retrieved
   *                             (if {@link GetDocumentsMode#SEND_OBJECTS} is
   *                             specified)
   * @param objectAllocCallback  If {@link GetDocumentsMode#SEND_OBJECTS} is
   *                             specified and <code>objectDb</code> is not NULL,
   *                             this function is called exactly once for each
   *                             object to provide the caller with information
   *                             about the object's size and ObjectID. The intent
   *                             is to allow for the physical allocation for the
   *                             object if need be. It is called before the
   *                             {@link IDocumentOpenCallback} for the
   *                             corresponding document
   * @param objectWriteCallback  This function is called for each "chunk" of each
   *                             object if {@link GetDocumentsMode#SEND_OBJECTS}
   *                             is specified and <code>objectDb</code> is not
   *                             NULL. For each object this will be called one or
   *                             more times
   * @param folderSinceTime      {@link TemporalAccessor} containing a time/date
   *                             value specifying the earliest time to retrieve
   *                             documents from the folder. If
   *                             {@link GetDocumentsMode#GET_FOLDER_ADDS} is
   *                             specified this is the time folder operations
   *                             should be retrieved from
   * @param folderAddCallback    If {@link GetDocumentsMode#GET_FOLDER_ADDS} is
   *                             specified but
   *                             {@link GetDocumentsMode#APPLY_FOLDER_ADDS} is
   *                             not, this function is called for each document
   *                             after the {@link IDocumentOpenCallback} function
   *                             is called
   */
  void getDocuments(Database db, final int[] noteIds, Set<OpenDocumentMode>[] docOpenFlags, int[] sinceSeqNum,
      final Set<GetDocumentsMode> controlFlags, final Database objectDb,
      final IGetDocumentsCallback getDocumentsCallback, final IDocumentOpenCallback docOpenCallback,
      final IObjectAllocCallback objectAllocCallback, final IObjectWriteCallback objectWriteCallback,
      TemporalAccessor folderSinceTime, final IFolderAddCallback folderAddCallback);

  /**
   * This function gets the given database's {@link ReplicaInfo} structure.<br>
   * <br>
   * This structure contains information that tells the Domino Replicator how to
   * treat the database.<br>
   * The ".ID" member enables the Replicator to identify "replicas" of
   * databases.<br>
   * <br>
   * The ".CutoffInterval" is the age in days at which deleted document
   * identifiers are purged.<br>
   * Domino divides this interval into thirds, and for each third of the interval
   * carries
   * out what amounts to an incremental purge.<br>
   * <br>
   * These deleted document identifiers are sometimes called deletion stubs.<br>
   * <br>
   * The ".Cutoff" member is a {@link DominoDateTime} value that is calculated by
   * subtracting the Cutoff Interval (also called Purge Interval) from today's
   * date.<br>
   * <br>
   * It prevents notes that are older than that date from being replicated at
   * all.<br>
   * <br>
   * The ".Flags" member is a bit-wise encoded short that stores miscellaneous
   * Replicator flags.<br>
   *
   * @param db database
   * @return replica info
   */
  ReplicaInfo getReplicaInfo(Database db);

  /**
   * Reads the replication history of the database
   *
   * @param db    database
   * @param flags Optional history summary flags enabling you to specify that
   *              wildcard entries are not to be returned and/or that sorting is
   *              to be done by date rather than by the default, server name
   * @return replication history or empty list
   */
  List<ReplicationHistorySummary> getReplicationHistory(Database db, Set<ReplicationHistoryFlags> flags);

  /**
   * Saves a document with additional flags
   *
   * @param doc               document to save
   * @param force             true to force document save even if the document was
   *                          modified in the meantime
   * @param noRevisionHistory true to not update $Revisions
   * @param keepModTime       true to not update the modified time of the document
   */
  void saveDocument(Document doc, boolean force, boolean noRevisionHistory, boolean keepModTime);

  /**
   * Changes the replica id of a database
   *
   * @param db database
   * @return new replica id
   */
  String setNewReplicaID(Database db);

  /**
   * This function sets the given database's {@link ReplicaInfo} structure.<br>
   * <br>
   * Use this function to set specific values, such as the replica ID, in the
   * header
   * data of a database.<br>
   * <br>
   * You may also use {@link #setReplicaInfo(Database, ReplicaInfo)} to set values
   * such as the replication flags in the header of the database.<br>
   *
   * @param db          database
   * @param replicaInfo new replica info
   */
  void setReplicaInfo(Database db, ReplicaInfo replicaInfo);

}
