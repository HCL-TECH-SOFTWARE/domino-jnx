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

/**
 * The Originator ID (OID) for a note identifies all replica copies of the same
 * note and distinguishes
 * between different revisions of that note.<br>
 * <br>
 * The Originator ID is composed of two parts:<br>
 * (1) the Universal Note ID (UNID) and (2) the Sequence Number and Sequence
 * Time.<br>
 * <br>
 * The UNID (the first part of the OID) universally identifies all copies of the
 * same note.
 * If one note in one database has the same UNID as another note in a replica of
 * that database,
 * then the two notes are replica copies of each other. The Sequence Number and
 * the Sequence Time,
 * taken together, distinguish different revisions of the same note from one
 * another.<br>
 * <br>
 * The full Originator ID uniquely identifies one particular version of a note.
 * A modified
 * version of a replica copy of a particular note will have a different OID.<br>
 * This is because Domino or Notes increments the Sequence Number when a note is
 * edited, and also
 * sets the Sequence Time to the timedate when the Sequence Number was
 * incremented.<br>
 * This means that when one replica copy of a note remains unchanged, but
 * another copy is edited
 * and modified, then the UNIDs of the 2 notes will remain the same but the
 * Sequence Number and
 * Sequence Times (hence, the OIDs) will be different.<br>
 * <br>
 * The "File" member of the OID (and UNID), contains a number derived in
 * different ways depending
 * on the release of Domino or Notes. Pre- 2.1 versions of Notes set the "File"
 * member to the
 * creation timedate of the NSF file in which the note is created. Notes 2.1
 * sets the "File"
 * member to a user-unique identifier, derived in part from information in the
 * ID of the user
 * creating the note, and in part from the database where the note is created.
 * Notes 3.0 sets
 * the "File" member to a random number generated at the time the note is
 * created.<br>
 * <br>
 * The "Note" member of the OID (and UNID), contains the date/time when the very
 * first copy of
 * the note was stored into the first NSF (Note: date/time from $CREATED item,
 * if exists, takes precedence).<br>
 * <br>
 * The "Sequence" member is a sequence number used to keep track of the most
 * recent version of the
 * note. The "SequenceTime" member is a sequence number qualifier, that allows
 * the Domino replicator
 * to determine which note is later given identical Sequence numbers.<br>
 * The sequence time qualifies the sequence number by preventing two concurrent
 * updates from looking
 * like no update at all. The sequence time also forces all Domino systems to
 * reach the same
 * decision as to which update is the "latest" version.<br>
 * <br>
 * The sequence time is the value that is returned to the @Modified formula and
 * indicates when
 * the document was last edited and saved.
 */
public interface DominoOriginatorId extends IAdaptable {

  /**
   * Returns the File part of the universal id
   *
   * @return File part of the originator ID
   */
  DominoDateTime getFile();

  /**
   * Returns the Note part of the universal id
   *
   * @return Note part of the originator ID
   */
  DominoDateTime getNote();

  /**
   * Returns the sequence number
   *
   * @return sequence number
   */
  int getSequence();

  /**
   * Returns the sequence time
   *
   * @return sequence time
   */
  DominoDateTime getSequenceTime();

  /**
   * Returns the {@link DominoUniversalNoteId} part formatted as string
   *
   * @return UNID string
   */
  String getUNID();

  /**
   * Extracts the {@link DominoUniversalNoteId} part from the OID data
   *
   * @return UNID
   */
  DominoUniversalNoteId getUNIDAsObj();

}
