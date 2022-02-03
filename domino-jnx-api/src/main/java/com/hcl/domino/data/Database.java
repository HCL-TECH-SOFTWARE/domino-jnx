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
package com.hcl.domino.data;

import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import com.hcl.domino.BuildVersionInfo;
import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClient.Encryption;
import com.hcl.domino.DominoClient.IBreakHandler;
import com.hcl.domino.DominoClient.NotesReplicationStats;
import com.hcl.domino.DominoClient.ReplicationStateListener;
import com.hcl.domino.UserNamesList;
import com.hcl.domino.crypt.DatabaseEncryptionState;
import com.hcl.domino.dbdirectory.DirectorySearchQuery.SearchFlag;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.RichTextBuilder;
import com.hcl.domino.dql.DQL;
import com.hcl.domino.dql.DQL.DQLTerm;
import com.hcl.domino.dql.QueryResultsProcessor;
import com.hcl.domino.exception.CompactionRequiredException;
import com.hcl.domino.exception.DocumentDeletedException;
import com.hcl.domino.exception.SpecialObjectCannotBeLocatedException;
import com.hcl.domino.misc.DominoClientDescendant;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.Loop;
import com.hcl.domino.misc.Ref;
import com.hcl.domino.security.Acl;
import com.hcl.domino.security.AclFlag;
import com.hcl.domino.security.AclLevel;

/**
 * Access to an individual database and its documents, indicies and design
 *
 * @author t.b.d
 */
public interface Database extends IAdaptable, AutoCloseable, DominoClientDescendant {

  /**
   * @since 1.0.18
   */
  interface AccessInfo {
    /**
     * Returns the access flags
     *
     * @return flags
     */
    Set<AclFlag> getAclFlags();

    /**
     * Returns the access level
     *
     * @return access level
     */
    AclLevel getAclLevel();
  }

  public enum Action {
    Continue, Stop
  }

  public enum CollectionType {
    View, Folder, Both
  }

  public enum CreateFlags {
    /** creates a ghost document that does not show up in views/searches */
    HIDE_FROM_VIEWS
  }

  public enum DocFlags {
    /** does not match formula (deleted or updated) */
    NoMatch,
    /** matches formula */
    Match,
    /** document truncated */
    Truncated,
    /** note has been purged. Returned only when SEARCH_INCLUDE_PURGED is used */
    Purged,
    /**
     * note has no purge status. Returned only when SEARCH_FULL_DATACUTOFF is used
     */
    NoPurgeStatus,
    /**
     * if {@link SearchFlag#NOTIFYDELETIONS}: note is soft deleted; NoteClass &amp;
     * {@link DocumentClass#NOTIFYDELETION} also on (off for hard delete)
     */
    SoftDeleted,
    /**
     * if there is reader's field at doc level this is the return value so that we
     * could mark the replication as incomplete
     */
    NoAccess,
    /**
     * note has truncated attachments. Returned only when SEARCH1_ONLY_ABSTRACTS is
     * used
     */
    TruncatedAttachments
  }

  /**
   * Data container that stores the lookup result for document info
   */
  public interface DocInfo {
    /**
     * Returns true if the note currently exists in the database
     *
     * @return true if note exists
     */
    boolean exists();

    /**
     * Returns the note id
     *
     * @return note id or 0 if the note could not be found
     */
    int getNoteId();

    /**
     * Returns the sequence number
     *
     * @return sequence number or 0 if the note could not be found
     */
    int getSequence();

    /**
     * Returns the sequence time ( = "Modified (initially)")
     *
     * @return an {@link Optional} describing the sequence time, or an empty one
     *         if the note could not be found
     */
    Optional<DominoDateTime> getSequenceTime();

    /**
     * Returns the UNID as hex string
     *
     * @return UNID or an empty string if the note could not be found
     */
    String getUnid();

    /**
     * Returns true if the note has already been deleted
     *
     * @return true if deleted
     */
    boolean isDeleted();

  }

  /**
   * Extension of {@link DocInfo} with additional note lookup data
   */
  public interface DocInfoExt extends DocInfo {

    /**
     * Returns the value for "Added in this file"
     *
     * @return date
     */
    Optional<DominoDateTime> getAddedToFile();

    /**
     * Returns the value for "Modified in this file"
     *
     * @return date
     */
    Optional<DominoDateTime> getModified();

    /**
     * Returns the note class
     *
     * @return class
     */
    Set<DocumentClass> getNoteClass();

    /**
     * Returns the note id of the parent note or 0
     *
     * @return parent note id
     */
    int getParentNoteId();

    /**
     * Returns the number of responses
     *
     * @return response count
     */
    short getResponseCount();

  }

  /**
   * Represents the local encryption information for a database.
   *
   * @since 1.0.18
   */
  interface EncryptionInfo {
    /**
     * @return an {@link Optional} describing the current encryption state of the
     *         database, or
     *         an empty one if the information could not be determined
     */
    Optional<DatabaseEncryptionState> getState();

    /**
     * @return an {@link Optional} describing the current encryption strength of the
     *         database, or
     *         an empty one if the information could not be determined
     */
    Optional<Encryption> getStrength();
  }

  public interface FormulaQueryCallback {

    Action deletionFound(Database db, SearchMatch searchMatch, TypedAccess computedValues);

    Action matchFound(Database db, SearchMatch searchMatch, TypedAccess computedValues);

    Action nonMatchFound(Database db, SearchMatch searchMatch, TypedAccess computedValues);
  }

  public interface ModifiedNoteInfo extends IAdaptable {

    int getNoteId();

    DominoDateTime getSeqTime();

    String getThreadRootUNID();

    String getUNID();

    boolean isDeleted();
  }

  public interface ModifiedNoteInfos {

    List<ModifiedNoteInfo> getInfos();

    DominoDateTime getUntil();

  }

  /**
   * Flags to control the output of
   * {@link Database#getModifiedNotesInfo(Set, Set, TemporalAccessor, boolean, TemporalAccessor)}
   */
  public enum ModifiedNotesInfoFlags implements INumberEnum<Integer> {
    /** Get only new and deleted notes */
    NEW_AND_DELETED_NOTES(0x00000001),
    /** Get named ghost notes as well. */
    NAMED_GHOSTS(0x00000002),
    /** Get new notes only */
    NEW_ONLY(0x00000004),
    /** Get deleted notes only */
    DELETED_ONLY(0x00000008),
    /** Return soft-deleted notes without high bit on */
    NO_SOFT_DELETES(0x00000010),
    /** Return deleted ghost notes as deleted */
    DELETED_GHOSTS(0x00000020),
    /** Return the Thread Root UNID too */
    THREAD_ROOT_UNID(0x00000040),
    /** If TRUE, deleted notes shouldn't have high bit set in ID Table */
    NODELETED_BIT(0x00000080),
    /** return the noteid too */
    NOTEID(0x00000100),
    /** return info for docs that have $TUA[0] == TRU */
    MUST_HAVE_THREAD_ROOT_UNID(0x00000200),
    /** return the $REF as the TRU if no parent */
    RETURN_PARENT_IF_NO_THREAD_ROOT_UNID(0x00000400),
    /** Get ghost notes as well. */
    GHOSTS(0x00000800),
    /** Use the sequence time compared against since time. */
    SEQ_TIME(0x00001000);

    private final Integer m_value;

    ModifiedNotesInfoFlags(final Integer value) {
      this.m_value = value;
    }

    @Override
    public long getLongValue() {
      return this.m_value.longValue();
    }

    @Override
    public Integer getValue() {
      return this.m_value;
    }
  }

  public enum ModifiedTableMode {
    WithDeletions,
    WithoutDeletions
  }

  /**
   * @since 1.0.19
   */
  interface NSFVersionInfo {

    /**
     * The major version number indicates which releases of Domino and Notes
     * software
     * are able to access that database.<br>
     * The major verson number of a database is the same as the ODS version number
     * that
     * is displayed in the Notes Client File/Database/Properties/second information
     * tab.
     *
     * @return major version
     */
    int getMajorVersion();

    /**
     * The minor version number indicates small changes to the internal format of a
     * database,
     * and is generally of little interest to a C API program.
     *
     * @return minor version
     */
    int getMinorVersion();
  }

  /**
   * Flags to control how to open a document.
   */
  public enum OpenDocumentMode {

    /** open only summary info */
    SUMMARY_ONLY,

    /**
     * Mark unread to read if unread list is currently associated (database is a
     * remote database).
     */
    MARK_READ,

    /** Only open an abstract of large documents */
    ABSTRACT_ONLY,

    /**
     * Generate an ID Table of Note IDs for the responses to this note. Use this
     * option in
     * order to access the Note IDs of the immediate responses to a given note in a
     * later
     * call to NSFNoteGetInfo() using _NOTE_RESPONSES as the note header member ID.
     */
    LOAD_RESPONSES,

    /**
     * If specified, the open will check to see if this note had already been read
     * and
     * saved in memory. If not, and the database is server based, we will also check
     * the on-disk cache. If the note is not found, it is cached in memory and at
     * some
     * time in the future commited to a local on disk cache.<br>
     * <br>
     * The notes are guaranteed to be as up to date as the last time
     * NSFValidateNoteCache was called.
     * Minimally, this should be called the 1st time a database is opened prior to
     * specifying
     * this flag.
     */
    CACHE,
    /**
     * Do not read any objects. Objects include file attachments and DDE links.<br>
     * Warning: documents opened with OPEN_NOOBJECTS then subsequently updated loose
     * all
     * objects that were previously attached.
     */
    NOOBJECTS,
    /**
     * Converts items of type {@link ItemDataType#TYPE_RFC822_TEXT} to
     * {@link ItemDataType#TYPE_TEXT}
     * and {@link ItemDataType#TYPE_TIME}. If not set, we leave the items in their
     * native format.
     */
    CONVERT_RFC822_TO_TEXT_AND_TIME,

    /**
     * Converts items of type {@link ItemDataType#TYPE_MIME_PART} to
     * {@link ItemDataType#TYPE_COMPOSITE}
     * (rich text). If not set, we leave the items in their native format.
     */
    CONVERT_MIME_TO_RICHTEXT;
  }

  public enum ReplicateOption implements INumberEnum<Integer> {
    /** Receive notes from server (pull) */
    RECEIVE(0x00000001),

    /** Send notes to server (push) */
    SEND(0x00000002),

    /** Replicate all database files */
    ALL_DBS(0x00000004),

    /** Replicate NTFs as well */
    ALL_NTFS(0x00000400),

    /** Medium &amp; High priority databases only */
    MEDIUM_HIGH_PRIORITY_ONLY(0x00004000),

    /** High priority databases only */
    HIGH_PRIORITY_ONLY(0x00008000);

    private final Integer m_value;

    ReplicateOption(final Integer value) {
      this.m_value = value;
    }

    @Override
    public long getLongValue() {
      return this.m_value;
    }

    @Override
    public Integer getValue() {
      return this.m_value;
    }
  }

  /**
   * Container with information about each document received for an NSF search,
   * containing the document note id / UNID, modified dates, document class
   * and document flags.
   *
   * @author Karsten Lehmann
   */
  public interface SearchMatch {

    /**
     * Returns information about the document's class
     *
     * @return class info
     */
    Set<DocumentClass> getDocumentClass();

    /**
     * Returns information about note flags
     *
     * @return flags
     */
    Set<DocFlags> getFlags();

    /**
     * Returns the modified date of the note as an {@link DominoDateTime}
     *
     * @return modified date
     */
    DominoDateTime getLastModified();

    /**
     * Returns the note id
     *
     * @return note id
     */
    int getNoteID();

    /**
     * Returns the note's sequence number
     *
     * @return sequence number
     */
    int getSequenceNumber();

    /**
     * Returns the sequence time of the note as a {@link DominoDateTime}.
     *
     * @return sequence time
     */
    DominoDateTime getSequenceTime();

    /**
     * Returns the UNID of the note
     *
     * @return UNID
     */
    String getUNID();

  }

  /**
   * Adds note ids to a folder
   *
   * @param folderNoteId note id of folder
   * @param noteIds      note ids to add
   */
  void addToFolder(int folderNoteId, Collection<Integer> noteIds);

  /**
   * Adds note ids to a folder
   *
   * @param folderName name of folder
   * @param noteIds    note ids to add
   */
  void addToFolder(String folderName, Collection<Integer> noteIds);

  /**
   * Closes the database, releasing its handle and allowing it to be
   * deleted.
   * <p>
   * Calling this method will cause all future invocations of methods on this
   * object to fail.
   * </p>
   */
  @Override
  void close();

  /**
   * Creates a copy of a folder including its content
   *
   * @param sourceFolderNoteId note id of folder to copy
   * @param newFolderName      name of new folder copy
   * @return note id of copy
   */
  int copyFolder(int sourceFolderNoteId, String newFolderName);

  /**
   * Creates a copy of a folder including its content
   *
   * @param sourceFolderName name of folder to copy
   * @param newFolderName    name of new folder copy
   * @return note id of copy
   */
  int copyFolder(String sourceFolderName, String newFolderName);

  Document createDocument();

  Document createDocument(Set<CreateFlags> flags);

  /**
   * Creates a new {@link DocumentSelection} to select data or design documents.
   *
   * @return selection
   */
  DocumentSelection createDocumentSelection();

  /**
   * Creates a new folder in the database with the design of the specified
   * folder/view
   * in another database
   *
   * @param formatDb           database containing the folder design
   * @param formatFolderNoteId name of folder/view to copy design
   * @param newFolderName      name of new folder
   * @return note id of created folder
   */
  int createFolder(Database formatDb, int formatFolderNoteId, String newFolderName);

  /**
   * Creates a new folder in the database with the design of the specified
   * folder/view
   * in another database
   *
   * @param formatDb         database containing the folder design
   * @param formatFolderName name of folder/view to copy design
   * @param newFolderName    name of new folder
   * @return note id of created folder
   */
  int createFolder(Database formatDb, String formatFolderName, String newFolderName);

  /**
   * Creates a new folder in the database with the design of the specified
   * folder/view
   *
   * @param formatFolderNoteId note id of folder/view to copy design
   * @param newFolderName      name of new folder
   * @return note id of created folder
   */
  int createFolder(int formatFolderNoteId, String newFolderName);

  /**
   * Creates a new folder in the database with the default view/folder design
   *
   * @param newFolderName name of new folder
   * @return note id of created folder
   */
  int createFolder(String newFolderName);

  /**
   * Creates a new folder in the database with the design of the specified
   * folder/view
   *
   * @param formatFolderName name of folder/view to copy design
   * @param newFolderName    name of new folder
   * @return note id of created folder
   */
  int createFolder(String formatFolderName, String newFolderName);

  /**
   * Creates a {@link QueryResultsProcessor} to process the results
   * of multi-database queries
   *
   * @return query results processor
   */
  QueryResultsProcessor createQueryResultsProcessor();

  void deleteDocument(int noteId);

  void deleteDocument(String unid);

  void deleteDocuments(Collection<Integer> noteIds);

  void deleteDocumentsByUNID(Collection<String> unids);

  /**
   * Deletes a folder
   *
   * @param folderNoteId note id of folder to delete
   */
  void deleteFolder(int folderNoteId);

  /**
   * Deletes a folder
   *
   * @param folderName name of folder to delete
   */
  void deleteFolder(String folderName);

  /**
   * This function deletes a full text index and disables full text indexing for a
   * database.
   */
  void deleteFTIndex();

  /**
   * Looks up a collection by name in the database design
   *
   * @param name name
   * @param type type of collection to find
   * @return note id of collection or 0 if not found
   */
  int findCollectionId(String name, CollectionType type);

  /**
   * Iterates over all collections in this database
   *
   * @param consumer consumer to receive collection info
   */
  void forEachCollection(BiConsumer<DominoCollectionInfo, Loop> consumer);

  /**
   * Iterates over all profile documents in the database
   *
   * @param consumer consumer to receive documents
   */
  void forEachProfileDocument(BiConsumer<Document, Loop> consumer);

  /**
   * Iterates over all profile documents of the specified type in
   * the database
   *
   * @param profileName profile name
   * @param consumer    consumer to receive documents
   */
  void forEachProfileDocument(String profileName, BiConsumer<Document, Loop> consumer);

  /**
   * Iterates over all profile documents of the specified type/username in
   * the database
   *
   * @param profileName profile name
   * @param userName    username
   * @param consumer    consumer to receive documents
   */
  void forEachProfileDocument(String profileName, String userName, BiConsumer<Document, Loop> consumer);

  /**
   * This function creates a new full text index for a local database.<br>
   * <br>
   * Synchronous full text indexing of a remote database is currently not
   * supported.
   * Use {@link #ftIndexRequest()} to request an index update of a remote
   * database.
   *
   * @param options Indexing options. See {@link FTIndex}
   * @return indexing statistics
   */
  FTIndexStats ftIndex(Set<FTIndex> options);

  /**
   * Requests an asynchronous update of the full text index
   */
  void ftIndexRequest();

  /**
   * This function generates a new Originator ID (OID) used to uniquely identify a
   * document.<br>
   * <br>
   * Use this function when you already have a document open and wish to create a
   * totally new document
   * with the same items as the open document.<br>
   * <br>
   * You do not need this method when creating a new note from scratch using
   * {@link #createDocument()},
   * because the OID is already generated for you.<br>
   * <br>
   * If the database resides on a remote Lotus Domino Server, the current user
   * must to have
   * the appropriate level of access to carry out this operation.
   *
   * @return new OID
   */
  DominoOriginatorId generateOID();

  String generateUNID();

  /**
   * Retrieves the file path of the database.
   * <p>
   * If the database is on a remote server, this path will be relative to the
   * server's data
   * directory.
   * </p>
   * <p>
   * If the database is local, this will be an absolute local file path.
   * </p>
   *
   * @return a data-relative or absolute path to the database, depending on host
   */
  String getAbsoluteFilePath();

  Acl getACL();

  /**
   * Looks up and loads an agent in the database design
   *
   * @param agentName agent name
   * @return an {@link Optional} describing the agent, or an empty one if no
   *         such agent can be found
   */
  Optional<Agent> getAgent(String agentName);

  /**
   * The returned document is created when you save an agent, and it is stored in
   * the same database as the agent.<br>
   * The document replicates, but is not displayed in views.<br>
   * Each time you edit and re-save an agent, its saved data document is deleted
   * and a new, blank one is created. When you delete an agent, its saved data
   * document is deleted.
   *
   * @param agentName agent name
   * @return an {@link Optional} describing the agent's saved data, or an empty
   *         one if no
   *         such agent can be found or if it has no saved data
   */
  Optional<Document> getAgentSavedData(String agentName);

  /**
   * Returns a {@link Stream} of {@link DominoCollectionInfo} objects that
   * contain information about the collections in the database.
   *
   * @return collection info stream
   */
  Stream<DominoCollectionInfo> getAllCollections();

  /**
   * Returns the note id of all documents with assigned primary key
   * (via {@link Document#setPrimaryKey(String, String)}, hashed by their category
   * value
   * and object id
   *
   * @return case insensitive lookup result, outer map with category as hash key,
   *         inner map with [objectid,noteid] entries
   */
  Map<String, Map<String, Integer>> getAllDocumentsByPrimaryKey();

  /**
   * Returns the note id of all documents with <code>category</code> in the
   * assigned primary key
   * (via {@link Document#setPrimaryKey(String, String)}, hashed by their category
   * value
   * and object id
   *
   * @param category category
   * @return case insensitive result map with [objectid, noteid] entries
   */
  Map<String, Integer> getAllDocumentsByPrimaryKey(String category);

  /**
   * This function returns an {@link IDTable} of all note IDs in the database
   * with the given set of {@link DocumentClass}.<br>
   * <br>
   * If <code>includeDeletions</code> is set to TRUE, the returned IDTable
   * also contains the note IDs of deleted documents. Thoese note IDs are OR'ed
   * with the special flag {@link IDTable#NOTEID_FLAG_DELETED}.<br>
   * <br>
   * Use the date/time value returned by {@link IDTable#getDateTime()} as
   * <code>since</code>
   * parameter of {@link #getModifiedNoteIds(Set, TemporalAccessor, boolean)} to
   * incrementally
   * read note id changes.<br>
   * <br>
   * This function is just a convenience function and calls
   * {@link #getModifiedNoteIds(Set, TemporalAccessor, boolean)}
   * with a since value of NULL.
   *
   * @param docClasses       the appropriate {@link DocumentClass} mask for the
   *                         documents you wish to select. Symbols can be OR'ed to
   *                         obtain the desired Note classes in the resulting ID
   *                         Table.
   * @param includeDeletions true to include the note IDs of deleted docs
   * @return IDTable
   */
  IDTable getAllNoteIds(Set<DocumentClass> docClasses, boolean includeDeletions);

  /**
   * This function returns a {@link BuildVersionInfo} object which contains all
   * types of
   * information about the level of code running on a machine.<br>
   * <br>
   * See {@link BuildVersionInfo} for more information.
   *
   * @return version
   */
  BuildVersionInfo getBuildVersionInfo();

  String getCategories();

  DbDesign getDesign();

  /**
   * The name of the design template from which a database inherits its
   * design.<br>
   * If the database does not inherit its design from a design template, it
   * returns an empty string ("").
   *
   * @return template name or ""
   */
  String getDesignTemplateName();

  /**
   * Opens a document in the database
   *
   * @param noteId the ID of the document to retrieve
   * @return an {@link Optional} describing the document matching that ID, or
   *         an empty one if there is no such document
   * @throws DocumentDeletedException if the document has been deleted
   */
  Optional<Document> getDocumentById(int noteId);

  /**
   * Opens a document in the database
   *
   * @param noteId the ID of the document to retrieve
   * @param flags  the flags to control the document-opening mode
   * @return an {@link Optional} describing the document matching that ID, or
   *         an empty one if there is no such document
   * @throws DocumentDeletedException if the document has been deleted
   */
  Optional<Document> getDocumentById(int noteId, Set<OpenDocumentMode> flags);

  /**
   * Uses an efficient NSF lookup mechanism to find a document that
   * matches the primary key specified with <code>category</code> and
   * <code>objectKey</code>.
   *
   * @param category category part of primary key
   * @param objectId object id part of primary key
   * @return an {@link Optional} describing the document matching that key, or
   *         an empty one if there is no such document
   */
  Optional<Document> getDocumentByPrimaryKey(String category, String objectId);

  /**
   * Opens a document in the database
   *
   * @param unid UNID of the document to retrieve
   * @return an {@link Optional} describing the the document matching that ID, or
   *         an empty one if there is no such document
   * @throws DocumentDeletedException if the document has been deleted
   */
  Optional<Document> getDocumentByUNID(String unid);

  /**
   * Opens a document in the database
   *
   * @param unid  the UNID of the document to retrieve
   * @param flags the flags to control the document-opening mode
   * @return an {@link Optional} describing the document matching that UNID, or
   *         an empty one if there is no such document
   * @throws DocumentDeletedException if the document has been deleted
   */
  Optional<Document> getDocumentByUNID(String unid, Set<OpenDocumentMode> flags);

  DocInfoExt getDocumentInfo(int noteId);

  /**
   * Retrieves the current effective access level and flags for the open database.
   *
   * @return an {@link AccessInfo} object describing the current access
   */
  AccessInfo getEffectiveAccessInfo();

  /**
   * This function returns the number of entries in the specified folder's
   * index.<br>
   * <br>
   * This is the number of documents plus the number of categories (if any) in the
   * folder.<br>
   * <br>
   * Subfolders and documents in subfolders are not included in the count.
   *
   * @param folderNoteId note id of folder
   * @return count
   */
  int getFolderDocCount(int folderNoteId);

  /**
   * This function returns the number of entries in the specified folder's
   * index.<br>
   * <br>
   * This is the number of documents plus the number of categories (if any) in the
   * folder.<br>
   * <br>
   * Subfolders and documents in subfolders are not included in the count.
   *
   * @param folderName name of folder
   * @return count
   */
  int getFolderDocCount(String folderName);

  /**
   * This function gets the Note IDs of notes in a folder and returns a IDTable.
   *
   * @param folderNoteId note id of folder
   * @param validateIds  If set, return only "validated" noteIDs
   * @return id table
   */
  IDTable getIDTableForFolder(int folderNoteId, boolean validateIds);

  /**
   * This function gets the Note IDs of notes in a folder and returns a IDTable.
   *
   * @param folderName  name of folder
   * @param validateIds If set, return only "validated" noteIDs
   * @return id table
   */
  IDTable getIDTableForFolder(String folderName, boolean validateIds);

  /**
   * The extended version of the Item Definition Table for a database contains
   * the number of items, name and type of all the items defined in that
   * database.<br>
   * <br>
   * Examples are field names, form names, design names, and formula labels.<br>
   * Applications can obtain a copy of the extended version of the Item Definition
   * Table by calling this method.
   *
   * @return item definition table with item names as keys (case-insensitive and
   *         sorted) and item type as value
   */
  NavigableMap<String, ItemDataType> getItemDefinitionTable();

  /**
   * This routine returns the last time a database was full text indexed.
   * It can also be used to determine if a database is full text indexed.
   *
   * @return an {@link Optional} describing the last index time, or an empty one
   *         if the
   *         database has not been indexed
   */
  Optional<DominoDateTime> getLastFTIndexTime();

  /**
   * @return the current status of the database's local encryption.
   * @since 1.0.18
   */
  EncryptionInfo getLocalEncryptionInfo();

  /**
  * Changes the local encryption level/strength
  * 
  * @param encryption new encryption
  * @param userName user to encrypt the database for; null/empty for current ID user (should be used in the Notes Client and in most cases on the server side as well)
  * @throws CompactionRequiredException to notify the developer that the NSF needs to be compacted next
  */
 public void setLocalEncryptionInfo(Encryption encryption, String userName);
 
  /**
   * This function returns an {@link IDTable} of Note IDs of documents which have
   * been modified in some way
   * from the given starting time until "now".<br>
   * The ending date/time is returned in {@link IDTable#getDateTime()}, so that
   * this
   * function can be performed incrementally.<br>
   * <br>
   * The returned IDTable also contains the note IDs of deleted documents. These
   * note IDs are bitwise OR'ed
   * with the special flag {@link IDTable#NOTEID_FLAG_DELETED}.<br>
   *
   * @param docClasses       the appropriate {@link DocumentClass} mask for the
   *                         documents you wish to select. Symbols can be OR'ed to
   *                         obtain the desired Note classes in the resulting ID
   *                         Table.
   * @param since            A date/time value containing the starting date used
   *                         when selecting notes to be added to the ID Table
   *                         built by this function. Use null to get all note ids.
   * @param includeDeletions true to include the note IDs of deleted docs
   * @return IDTable
   */
  IDTable getModifiedNoteIds(Set<DocumentClass> docClasses, TemporalAccessor since, boolean includeDeletions);

  /**
   * Returns information about documents modified since the specified date/time
   *
   * @param docClasses       document classes to scan, e.g.
   *                         {@link DocumentClass#DATA}
   * @param infoRequested    controls what to return and how to filter
   * @param sinceParam       optional for incremental scans, use
   *                         {@link ModifiedNoteInfos#getUntil()} of a previous
   *                         search
   * @param includeDeletions true to return deletions
   * @param optUntilParam    if not null, optional upper date to return results
   * @return scan result
   */
  ModifiedNoteInfos getModifiedNotesInfo(Set<DocumentClass> docClasses,
      Set<ModifiedNotesInfoFlags> infoRequested,
      TemporalAccessor sinceParam, boolean includeDeletions, TemporalAccessor optUntilParam);

  /**
   * This function obtains the date/time of the last modified data and non-data
   * documents
   * in the specified database.
   *
   * @param retDataModified    will be filled with the last modified date/time of
   *                           data documents
   * @param retNonDataModified will be filled with the last modified date/time of
   *                           non-data (design) documents
   */
  void getModifiedTime(Ref<DominoDateTime> retDataModified, Ref<DominoDateTime> retNonDataModified);

  /**
   * This method can be used to get information for a number documents in a
   * database from their note ids in a single call.<br>
   * The data returned by this method is the note id, the UNID of the document,
   * the sequence number and the sequence time ("Modified initially" time).<br>
   * <br>
   * In addition, the method checks whether a document exists or has been deleted.
   *
   * @param noteIds array of note ids
   * @return lookup results, same size and order as <code>noteIds</code> array
   * @throws IllegalArgumentException if note id array has too many entries (more
   *                                  than 65535)
   */
  DocInfo[] getMultiDocumentInfo(int[] noteIds);

  /**
   * This method can be used to get information for a number documents in a
   * database from their note unids in a single call.<br>
   * The data returned by this method is the note id the UNID of the document, the
   * sequence number
   * and the sequence time ("Modified initially" time).<br>
   * <br>
   * In addition, the method checks whether a document exists or has been
   * deleted.<br>
   *
   * @param noteUNIDs array of note unids
   * @return lookup results, same size and order as <code>noteUNIDs</code> array
   * @throws IllegalArgumentException if note unid array has too many entries
   *                                  (more than 32767)
   */
  DocInfo[] getMultiDocumentInfo(String[] noteUNIDs);

  /**
   * This function returns a {@link UserNamesList} structure. Result is empty
   * when the database was opened as the ID user.
   *
   * @return names list if present
   */
  Optional<UserNamesList> getNamesList();

  /**
   * Reads information about the On-Disk-Structure (ODS) of the database.
   * Each release of Domino or Notes software can access databases that have a
   * major version
   * number that is less than or equal to a particular value associated with that
   * release.<br>
   * <br>
   * A Domino database that has a major version number that is greater than the
   * major version
   * number associated with a particular Domino or Notes release cannot be
   * accessed by that release.<br>
   * <br>
   * The following table shows which major version numbers can be accessed by
   * particular
   * releases of Domino or Notes:<br>
   * <br>
   * <table>
   * <caption>The table ODS levels corresponding to Domino releases</caption>
   * <tr>
   * <th>Domino or Notes Software Releases</th>
   * <th>Major Version Numbers That Can Be Accessed</th>
   * </tr>
   * <tr>
   * <td>1.x</td>
   * <td>16</td>
   * </tr>
   * <tr>
   * <td>2.x</td>
   * <td>16</td>
   * </tr>
   * <tr>
   * <td>3.x</td>
   * <td>17</td>
   * </tr>
   * <tr>
   * <td>4.0, 4.1, 4.5.x, 4.6.x</td>
   * <td>20 or less</td>
   * </tr>
   * <tr>
   * <td>5.0 - 5.0.12, 6 - 6.0.3, 6.5, 7.0</td>
   * <td>43 or less</td>
   * </tr>
   * <tr>
   * <td>9.0</td>
   * <td>52</td>
   * </tr>
   * <tr>
   * <td>10.x - 11.x</td>
   * <td>53</td>
   * </tr>
   * <tr>
   * <td>12.0.0</td>
   * <td>54</td>
   * </tr>
   * </table>
   *
   * @return ODS info
   */
  NSFVersionInfo getNSFVersionInfo();

  /**
   * Gets the value of a database option
   *
   * @param option set {@link DatabaseOption}
   * @return true if the option is enabled, false if the option is disabled
   */
  boolean getOption(DatabaseOption option);

  /**
   * Returns the {@link DatabaseOption} values for the database
   *
   * @return options
   */
  Set<DatabaseOption> getOptions();

  /**
   * @param profileName the name of the profile document to return
   * @return an {@link Optional} describing the existing or new profile document,
   *         or an
   *         empty one if the document does not exist and cannot be created
   */
  Optional<Document> getProfileDocument(String profileName);

  /**
   * @param profileName the name of the profile document to return
   * @param userName    the user name key for the profile instance
   * @return an {@link Optional} describing the existing or new profile document,
   *         or an
   *         empty one if the document does not exist and cannot be created
   */
  Optional<Document> getProfileDocument(String profileName, String userName);

  /**
   * Retrieves the file path of the database relative to the containing host's
   * data directory.
   *
   * @return a data-relative path to the database
   */
  String getRelativeFilePath();

  String getReplicaID();

  String getServer();

  /**
   * Opens a soft deleted document in the database
   *
   * @param noteId the ID of the document to retrieve
   * @return an {@link Optional} describing the soft-deleted document matching
   *         that ID, or
   *         an empty one if there is no such document
   */
  Optional<Document> getSoftDeletedDocumentById(int noteId);

  /**
   * Opens a soft deleted document in the database
   *
   * @param unid the UNID of the document to retrieve
   * @return an {@link Optional} describing the soft-deleted document matching
   *         that UNID, or
   *         an empty one if there is no such document
   */
  Optional<Document> getSoftDeletedDocumentByUNID(String unid);

  /**
   * The template name of a database, if the database is a template. If the
   * database is not a template, returns an empty string.
   *
   * @return template name or "" if no template
   */
  String getTemplateName();

  String getTitle();

  /**
   * An ID Table is created containing the list of unread documents in the
   * database for the specified user.<br>
   * <br>
   * The argument {@code createIfNotAvailable} controls what action is to be
   * performed
   * if there is no list of unread notes for the specified user in the
   * database.<br>
   * <br>
   * If no list is found and this flag is set to {@code false}, the method will
   * return {@code null}.<br>
   * if this flag is set to {code true}, the list of unread documents will be
   * created and all
   * documents in the database will be added to the list.<br>
   * <br>
   * No coordination is performed between different users of the same
   * database.<br>
   * <br>
   * If an application obtains a list of unread documents while another user is
   * modifying the
   * list, the changes made may not be visible to the application.<br>
   * <br>
   * Unread marks for each user are stored in the client desktop.dsk file and in
   * the database.<br>
   * <br>
   * When a user closes a database (either through the Notes user interface or
   * through an API program),
   * the unread marks in the desktop.dsk file and in the database are synchronized
   * so that
   * they match.<br>
   * Unread marks are not replicated when a database is replicated.<br>
   * <br>
   * Instead, when a user opens a replica of a database, the unread marks from the
   * desktop.dsk
   * file propagates to the replica database.<br>
   *
   * @param userName             user for which to check unread marks (abbreviated
   *                             or canonical format); use {@code null} for
   *                             current {@link DominoClient} user
   * @param createIfNotAvailable {code true}: If the unread list for this user
   *                             cannot be found on disk, return all note IDs.
   *                             {@code false}: If the list cannot be found,
   *                             return null
   * @param updateUnread         {@code true} to update unread marks,
   *                             {@code false} to not update unread marks.
   * @return an {@link Optional} describing the table of unread documents, or an
   *         empty one if there is no table
   */
  Optional<IDTable> getUnreadDocumentTable(String userName, boolean createIfNotAvailable, boolean updateUnread);

  /**
   * This function permanently deletes the specified "soft deleted" document from
   * the database.<br>
   * The deleted document may be of any {@link DocumentClass}. The active user ID
   * must have
   * sufficient user access in the databases's Access Control List (ACL) to carry
   * out a deletion on the document or the function will throw an error.
   *
   * @param softDelNoteId The note ID of the document that you want to delete
   */
  void hardDeleteDocument(int softDelNoteId);

  /**
   * Checks whether we have full access on a database
   *
   * @return true if full access
   */
  boolean hasFullAccess();

  boolean isDocumentLockingEnabled();

  /**
   * Checks if a document is in the unread table for the specified user.<br>
   * <br>
   * For performance reasons we internally cache the unread table and store the
   * username.
   * This cached table is reused if the username on subsequent calls is the same
   * and disposed if it is different.
   *
   * @param userName name if user in abbreviated or canonical format; if null, we
   *                 use {@link DominoClient#getEffectiveUserName()}
   * @param noteId   note id of document
   * @return true if unread
   */
  boolean isDocumentUnread(String userName, int noteId);

  boolean isFTIndex();

  boolean isLargeSummaryEnabled();

  /**
   * Determines whether the database is locally encrypted.
   * <p>
   * Note: this may be slightly more efficient than
   * {@link #getLocalEncryptionInfo}.
   * </p>
   *
   * @return {@code true} if the database is encrypted on disk; {@code false}
   *         otherwise
   */
  boolean isLocallyEncrypted();

  /**
   * Checks whether a database is located on a remote server
   *
   * @return true if remote
   */
  boolean isRemote();

  /**
   * Moves a folder below another one
   *
   * @param folderNoteId          note id of folder to move
   * @param newParentFolderNoteId new parent folder note id
   */
  void moveFolder(int folderNoteId, int newParentFolderNoteId);

  /**
   * Moves a folder below another one
   *
   * @param folderName          name of folder to move
   * @param newParentFolderName new parent folder
   */
  void moveFolder(String folderName, String newParentFolderName);

  Optional<DominoCollection> openCollection(String collectionName);

  /**
   * @param unid the UNID of a view or folder note to open
   * @return an {@link Optional} describing the requested collection, or an empty
   *         one if
   *         the note does not exist
   */
  Optional<DominoCollection> openCollectionByUNID(String unid);

  Optional<DominoCollection> openDefaultCollection();

  /**
   * Opens and returns the design collection
   *
   * @return design collection
   */
  DominoCollection openDesignCollection();

  /**
   * Convenience method to look up the roles for a user from the database
   * {@link Acl}.<br>
   * Use {@link Acl#lookupAccess(UserNamesList)} if you need other access
   * information
   * for the same user as well.
   *
   * @param userName username, either canonical or abbreviated
   * @return list of roles, not null
   */
  List<String> queryAccessRoles(String userName);

  /**
   * Runs an operation on all documents in the database
   *
   * @return query result, use
   *         {@link DocumentSummaryQueryResult#computeValues(Map)} to compute
   *         document values on the fly and
   *         {@link DocumentSummaryQueryResult#sort(DominoCollection)} to project
   *         the document note ids onto a {@link DominoCollection} to get them
   *         back sorted via
   *         {@link DocumentSummaryQueryResult#collectEntries(int, int)}
   */
  DocumentSummaryQueryResult queryDocuments();

  /**
   * Runs an operation on a number of documents in the database
   *
   * @param ids note ids of documents to process
   * @return query result, use
   *         {@link DocumentSummaryQueryResult#computeValues(Map)} to compute
   *         document values on the fly and
   *         {@link DocumentSummaryQueryResult#sort(DominoCollection)} to project
   *         the document note ids onto a {@link DominoCollection} to get them
   *         back sorted via
   *         {@link DocumentSummaryQueryResult#collectEntries(int, int)}
   */
  DocumentSummaryQueryResult queryDocuments(Collection<Integer> ids);

  /**
   * Runs a DQL query against the documents in the database.<br>
   *
   * @param query Domino query (DQL) generated via {@link DQL} factory class
   * @return query result
   */
  DQLQueryResult queryDQL(DQLTerm query);

  /**
   * Runs a DQL query against the documents in the database.<br>
   *
   * @param query Domino query (DQL) generated via {@link DQL} factory class
   * @param flags controlling execution, see {@link DBQuery}
   * @return query result
   */
  DQLQueryResult queryDQL(DQLTerm query, Set<DBQuery> flags);

  /**
   * Runs a DQL query against the documents in the database.<br>
   *
   * @param query             Domino query (DQL) generated via {@link DQL} factory
   *                          class
   * @param flags             controlling execution, see {@link DBQuery}
   * @param maxDocsScanned    maximum number of document scans allowed
   * @param maxEntriesScanned maximum number of view entries processed allows
   * @param maxMsecs          max milliseconds of executiion allow
   * @return query result
   */
  DQLQueryResult queryDQL(DQLTerm query, Set<DBQuery> flags,
      int maxDocsScanned, int maxEntriesScanned, int maxMsecs);

  /**
   * Runs a DQL query against the documents in the database.<br>
   *
   * @param query Domino query (DQL) as a single string (max 64K in length)
   * @return query result
   */
  DQLQueryResult queryDQL(String query);

  /**
   * Runs a DQL query against the documents in the database.<br>
   *
   * @param query Domino query (DQL) as a single string (max 64K in length)
   * @param flags controlling execution, see {@link DBQuery}
   * @return query result
   */
  DQLQueryResult queryDQL(String query, Set<DBQuery> flags);

  /**
   * Runs a DQL query against the documents in the database.<br>
   *
   * @param query             Domino query (DQL) as a single string (max 64K in
   *                          length)
   * @param flags             controlling execution, see {@link DBQuery}
   * @param maxDocsScanned    maximum number of document scans allowed
   * @param maxEntriesScanned maximum number of view entries processed allows
   * @param maxMsecs          max milliseconds of execution allow
   * @return query result
   */
  DQLQueryResult queryDQL(String query, Set<DBQuery> flags,
      int maxDocsScanned, int maxEntriesScanned, int maxMsecs);

  /**
   * Evaluates a formula on a set of documents or the whole database
   *
   * @param selectionFormula selection formula
   * @param filter           optional filter IDTable to evaluate the formula only
   *                         against selected documents
   * @param searchFlags      flags to control the search behavior
   * @param since            optional start date/time to run incremental searches,
   *                         e.g. the value {@link IDTable#getDateTime()} of the
   *                         returned {@link FormulaQueryResult#getNoteIds()}
   * @param docClass         class of documents to search
   * @return query result
   */
  FormulaQueryResult queryFormula(String selectionFormula, IDTable filter, Set<SearchFlag> searchFlags,
      TemporalAccessor since, Set<DocumentClass> docClass);

  /**
   * Evaluates a formula on a set of documents or the whole database
   * 
   * @param selectionFormula selection formula
   * @param filter           optional filter IDTable to evaluate the formula only
   *                         against selected documents
   * @param searchFlags      flags to control the search behavior
   * @param since            optional start date/time to run incremental searches,
   *                         e.g. the return value of previous queryFormula method calls.
   * @param docClass         class of documents to search
   * @param computeValues    map to compute summary values for the search matches, returned via 
   *                         the <code>callback</code>.
   *                         For example use (key="item1", value="") to return values
   *                         of an existing item "item1"
   *                         or (key="_computeditem2", value="@Text(@Created)") to compute a value.
   * @param callback         callback to receive matches, non-matches and deletions
   *                         since the given date/time
   * @return                 new date/time to be passed as "since" value for the next search for incremental search results
   */
  DominoDateTime queryFormula(String selectionFormula, Set<Integer> filter, Set<SearchFlag> searchFlags,
      TemporalAccessor since, Set<DocumentClass> docClass, Map<String, String> computeValues,
      FormulaQueryCallback callback);

  /**
   * Performs a fulltext search in the database with advanced options.
   *
   * @param query      fulltext query
   * @param maxResults Maximum number of documents to return (max. 65535). Use 0
   *                   to return the maximum number of results for the search
   * @param options    search options
   * @param filterIds  optional ID table to further refine the search. Use null if
   *                   this is not required.
   * @param start      the starting document number for the paged result. For the
   *                   non-paged result, set this item to 0. For the paged result,
   *                   set this item to a non-zero number.
   * @param count      number of documents to return for the paged result (max.
   *                   65535), set to 0 to return all results
   * @return search result
   */
  FTQueryResult queryFTIndex(String query, int maxResults, Set<FTQuery> options,
      Set<Integer> filterIds, int start, int count);

  /**
   * This function will refresh the database design, as allowed by the
   * database/design properties and
   * access control of Domino, from a server's templates.<br>
   * <br>
   * The refreshed database, if open in Domino or Notes at the time of refresh,
   * must be closed and
   * reopened to view any changes.<br>
   * <br>
   * Convenience function that calls
   * {@link #refreshDesign(String, boolean, boolean, DominoClient.IBreakHandler)} with
   * force and errIfTemplateNotFound set to true and without break handler.
   *
   * @param server name of the Lotus Domino Server on which the database template
   *               resides, If you want to specify "no server" (the local
   *               machine), use ""
   */
  void refreshDesign(String server);

  /**
   * This function will refresh the database design, as allowed by the
   * database/design properties and
   * access control of Domino, from a server's templates.<br>
   * <br>
   * The refreshed database, if open in Domino or Notes at the time of refresh,
   * must be closed and
   * reopened to view any changes.
   *
   * @param server                name of the Lotus Domino Server on which the
   *                              database template resides, If you want to
   *                              specify "no server" (the local machine), use ""
   * @param force                 true to force operation, even if destination "up
   *                              to date"
   * @param errIfTemplateNotFound true to return an error if the template is not
   *                              found
   * @param abortHandler          optional break handler to abort the operation or
   *                              null
   */
  void refreshDesign(String server, boolean force, boolean errIfTemplateNotFound, IBreakHandler abortHandler);

  /**
   * Removes all note ids from a folder
   *
   * @param folderNoteId note id of folder
   */
  void removeAllFromFolder(int folderNoteId);

  /**
   * Removes all note ids from a folder
   *
   * @param folderName name of folder
   */
  void removeAllFromFolder(String folderName);

  /**
   * Removes note ids from a folder
   *
   * @param folderNoteId note id of folder
   * @param noteIds      note ids to remove
   */
  void removeFromFolder(int folderNoteId, Collection<Integer> noteIds);

  /**
   * Removes note ids from a folder
   *
   * @param folderName name of folder
   * @param noteIds    note ids to remove
   */
  void removeFromFolder(String folderName, Collection<Integer> noteIds);

  /**
   * Renames a folder
   *
   * @param oldFolderNoteId note id of folder to rename
   * @param newFolderName   new folder name
   */
  void renameFolder(int oldFolderNoteId, String newFolderName);

  /**
   * Renames a folder
   *
   * @param oldFolderName old folder name
   * @param newFolderName new folder name
   */
  void renameFolder(String oldFolderName, String newFolderName);

  /**
   * Re-opens the database with a new handle.
   * <p>
   * This can be useful for additional safely when working with the same database
   * across
   * multiple threads.
   * </p>
   *
   * @return a new {@link Database} instance for the current database
   * @since 1.0.9
   */
  Database reopen();

  /**
   * Bidirectional replication of this database with another server
   *
   * @param serverName server to replicate with
   * @return replication results
   */
  NotesReplicationStats replicate(String serverName);

  /**
   * Replicate database with additional options
   *
   * @param serverName   server to replicate with
   * @param options      replication options
   * @param timeLimitMin If non-zero, number of minutes replication is allowed to
   *                     execute before cancellation. If not specified, no limit
   *                     is imposed
   * @return replication results
   * @param progressListener optional listener to get notified about replication
   *                         progress
   */
  NotesReplicationStats replicate(String serverName, Set<ReplicateOption> options, int timeLimitMin,
      ReplicationStateListener progressListener);

  void setCategories(String categories);

  /**
   * Changes the name of the design template from which a database inherits its
   * design
   *
   * @param newDesignTemplateName new design template name
   */
  void setDesignTemplateName(String newDesignTemplateName);

  void setDocumentLockingEnabled(boolean b);

  /**
   * Sets the value of a database option
   *
   * @param option see {@link DatabaseOption}
   * @param flag   true to set the option
   */
  void setOption(DatabaseOption option, boolean flag);

  /**
   * Changes the template name of a template database.
   *
   * @param newTemplateName new template name
   */
  void setTemplateName(String newTemplateName);

  void setTitle(String title);

  /**
   * Converts a single UNID to a note id
   *
   * @param unid UNID
   * @return note id or 0 if not found
   */
  int toNoteId(String unid);

  /**
   * Bulk conversion of UNIDs to note ids
   *
   * @param unids                 UNIDs to convert
   * @param resolvedNoteIDsByUNID Note ids for UNIDs that could be resolved
   * @param unresolvedUNIDs       set of UNIDs that could not be resolved
   */
  void toNoteIds(Collection<String> unids, Map<String, Integer> resolvedNoteIDsByUNID, Set<String> unresolvedUNIDs);

  /**
   * Converts a single note ID to a UNID
   *
   * @param noteId note ID
   * @return UNID or empty string if not found
   */
  String toUNID(int noteId);

  /**
   * Bulk conversion of note ids to UNIDs
   *
   * @param noteIds               note ids to convert
   * @param resolvedUNIDsByNoteId UNIDs for note ids that could be resolved
   * @param unresolvedNoteIds     set of note ids that could not be resolved
   */
  void toUNIDs(Collection<Integer> noteIds, Map<Integer, String> resolvedUNIDsByNoteId, Set<Integer> unresolvedNoteIds);

  /**
   * Harvest view design elements for optimized DQL performance.
   *
   * @param rebuild true to rebuild the design catalog
   */
  void updateDQLDesignCatalog(boolean rebuild);

  /**
   * Method to apply changes to the unread document table
   *
   * @param userName            user for which to update unread marks (abbreviated
   *                            or canonical format); use {@code null} for current
   *                            {@link DominoClient} user
   * @param noteIdToMarkRead    note ids to mark read (=remove from the unread
   *                            table)
   * @param noteIdsToMarkUnread note ids to mark unread (=add to the unread table)
   */
  void updateUnreadDocumentTable(String userName, Set<Integer> noteIdToMarkRead,
      Set<Integer> noteIdsToMarkUnread);

  /**
   * Looks up the note ID of the special note of the provided type. The meaning of
   * "special" depends on the {@link DocumentClass} value provided.
   * 
   * @param documentClass the type of note to look up
   * @return the ID of the special note
   * @since 1.0.32
   * @throws NullPointerException if {@code documentClass} is {@code null}
   * @throws SpecialObjectCannotBeLocatedException if there is no special ID
   *         corresponding to {@code documentClass}
   */
  int getSpecialNoteId(DocumentClass documentClass);

  /**
   * Returns a builder to compose richtext (both normal and design richtext) by combining existing pieces
   * 
   * @return richtext builder
   */
  RichTextBuilder getRichTextBuilder();

}
