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
package com.hcl.domino.data;

import java.time.temporal.TemporalAccessor;
import java.util.Optional;

/**
 * The Universal Note ID (UNID) identifies all copies of the same document in
 * different replicas of the same
 * database universally (across all servers).<br>
 * <br>
 * If one document in one database has the same UNID as another document in a
 * replica database, then the
 * two documents are replicas of each other.<br>
 * <br>
 * The UNID is used to reference a specific document from another document.
 * Specifically, the $REF
 * field of a response document contains the UNID of it's parent.<br>
 * <br>
 * Similarly, Doc Links contains the UNID of the linked-to document plus the
 * database ID
 * where the linked-to document can be found. The important characteristic of
 * the UNID is that it
 * continues to reference a specific document even if the document being
 * referenced is updated.<br>
 * <br>
 * The Domino replicator uses the Universal Note ID to match the documents in
 * one database with
 * their respective copies in replica databases. For example, if database A is a
 * replica copy
 * of database B, database A contains a document with a particular UNID, and
 * database B contains
 * a document with the same UNID, then the replicator concludes that these two
 * documents are replica
 * copies of one another. On the other hand, if database A contains a document
 * with a particular
 * UNID but database B does not, then the replicator will create a copy of that
 * document and
 * add it to database B.<br>
 * <br>
 * One database must never contain two documents with the same UNID. If the
 * replicator finds two
 * documents with the same UNID in the same database, it generates an error
 * message in the log
 * and does not replicate the document.<br>
 * <br>
 * The "File" member ({@link #getFile()}) of the UNID contains a number derived
 * in different ways depending on
 * the release of Domino or Notes.<br>
 * Pre- 2.1 versions of Notes set the "File" member to the creation date/time of
 * the NSF file
 * in which the document is created. Notes 2.1 sets the "File" member to a
 * user-unique identifier,
 * derived in part from information in the ID of the user creating the document,
 * and in part
 * from the database where the document is created. Notes 3.0 sets the "File"
 * member to a
 * random number generated at the time the document is created.<br>
 * <br>
 * The "Note" ({@link #getNote()}) member of the UNID contains the date/time
 * when the very first copy of the document
 * was stored into the first NSF (Note: date/time from $CREATED item, if exists,
 * takes precedence).
 */
public interface DominoUniversalNoteId extends IAdaptable {
  /**
   * Returns the File part of the universal id
   *
   * @return an {@link Optional} describing the File part, or an empty one if this
   *         has not yet been set
   */
  Optional<DominoDateTime> getFile();

  /**
   * Returns the Note part of the universal id
   *
   * @return an {@link Optional} describing the Note part, or an empty one if this
   *         has not yet been set
   */
  Optional<DominoDateTime> getNote();

  /**
   * Changes the File part of the universal id
   *
   * @param td new value for File
   */
  void setFile(TemporalAccessor td);

  /**
   * Changes the Note part of the universal id
   *
   * @param td new value for Note
   */
  void setNote(TemporalAccessor td);

  /**
   * Returns the string representation of the universal id (32 character hex
   * string)
   *
   * @return UNID string
   */
  @Override
  String toString();

}
