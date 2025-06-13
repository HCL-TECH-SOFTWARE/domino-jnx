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
package com.hcl.domino;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;

import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.NameNotFoundException;

import com.hcl.domino.admin.ServerAdmin;
import com.hcl.domino.admin.ServerStatistics;
import com.hcl.domino.admin.idvault.IdVault;
import com.hcl.domino.admin.replication.Replication;
import com.hcl.domino.calendar.Calendaring;
import com.hcl.domino.data.CompactMode;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Database.Action;
import com.hcl.domino.data.Database.ReplicateOption;
import com.hcl.domino.data.DatabaseChangePathList;
import com.hcl.domino.data.DatabaseClass;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.DominoUniversalNoteId;
import com.hcl.domino.data.Formula;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.data.ModificationTimePair;
import com.hcl.domino.data.UserData;
import com.hcl.domino.dbdirectory.DbDirectory;
import com.hcl.domino.dxl.DxlExporter;
import com.hcl.domino.dxl.DxlImporter;
import com.hcl.domino.exception.FormulaCompilationException;
import com.hcl.domino.exception.ServerNotFoundException;
import com.hcl.domino.exception.ServerRestrictedException;
import com.hcl.domino.exception.ServerUnavailableException;
import com.hcl.domino.freebusy.FreeBusy;
import com.hcl.domino.html.RichTextHTMLConverter;
import com.hcl.domino.mime.MimeReader;
import com.hcl.domino.mime.MimeWriter;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.JNXThread;
import com.hcl.domino.misc.Pair;
import com.hcl.domino.mq.MessageQueues;
import com.hcl.domino.naming.UserDirectory;
import com.hcl.domino.person.Person;
import com.hcl.domino.runtime.DominoRuntime;
import com.hcl.domino.security.Ecl;
import com.hcl.domino.server.ServerInfo;
import com.hcl.domino.server.ServerPingInfo;

/**
 * Used as the entry point and thread management, borrowed from other Java APIs.
 * Handles mapping to Domino server, protocol etc
 *
 * @author t.b.d
 * @since 0.5.0
 */
public interface DominoClient extends IAdaptable, AutoCloseable {
  /** Types of ECL settings */
  public enum ECLType {
    Lotusscript((short) 0), JavaApplets((short) 1), Javascript((short) 2);

    private final short m_type;

    ECLType(final short type) {
      this.m_type = type;
    }

    public short getTypeAsShort() {
      return this.m_type;
    }
  }

  /** Available encryption strengths for database creation */
  public enum Encryption implements INumberEnum<Integer> {
    None(0), Simple(1), Medium(2), Strong(3), AES128(4), AES256(5);

    private final int value;

    Encryption(final int value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Integer getValue() {
      return this.value;
    }
  }

  /**
   * Implement this method to send break signals to long running operations
   */
  @FunctionalInterface
  public interface IBreakHandler {
    /**
     * Make sure that this method does not do any heavy computation as it is called
     * a
     * lot by Domino.
     *
     * @return {@link Action#Stop} to send break signal
     */
    Action shouldInterrupt();
  }

  /**
   * Definition of function that will handle the progress bar signal.<br>
   * <br>
   * The progress signal handler displays a progress bar.<br>
   * The progress position will generally start at 0 and end at Range.<br>
   * The current progress supplied is either absolute ({@link #setPos(long)}) or a
   * delta from the
   * previous progress state ({@link #setDeltaPos(long)}).<br>
   * As the operation which is supplying progress information is performed, the
   * range may
   * change.<br>
   * <br>
   * If it does, an additional {@link #setRange(long)} will be signaled.
   */
  public interface IProgressListener {

    void begin();

    void end();

    void setBytePos(long pos);

    void setByteRange(long range);

    void setDeltaPos(long pos);

    void setPos(long pos);

    void setRange(long range);

    void setText(String str);

  }

  /**
   * Interface for subscribing to {@code DominoClient} lifecycle events.
   *
   * @since 1.0.2
   */
  public interface LifecycleListener {
    /**
     * Called after the client has completed its {@link DominoClient#close close}
     * method.
     *
     * @param client the closed client object
     */
    void onClose(DominoClient client);
  }

  /**
   * Replication statistics
   */
  public interface NotesReplicationStats {

    long getNumberErrors();

    long getPullFailed();

    long getPullFilesCompleted();

    long getPullNotesAdded();

    long getPullNotesDeleted();

    long getPullNumberErrors();

    long getPullSuccessful();

    long getPullTotalFiles();

    long getPushFailed();

    long getPushFilesCompleted();

    long getPushNotesAdded();

    long getPushNotesDeleted();

    long getPushNumberErrors();

    long getPushSuccessful();

    long getPushTotalFiles();

    long getStubsInitialized();

    long getTotalUnreadExchanges();

  }

  /**
   * Enum for options for opening the database, if the specified database is not
   * currently available
   */
  public enum OpenDatabase implements INumberEnum<Short> {
    /**
     * If open fails, failover to another server in the same cluster that has a
     * replica copy of this
     * database.
     * If the input server is not a member of a cluster or if the database is not
     * replicated on
     * other
     * servers in the cluster, then this flag will have no effect.
     */
    CLUSTER_FAILOVER((short) 0x0080),

    /**
     * Force a database fixup, even if the file was properly closed previously. This
     * flag is not
     * necessary if the database was improperly closed, since Domino and Notes will
     * automatically
     * verify the database contents of improperly closed databases. This process
     * involves three
     * steps:<br>
     * 1) Perform a consistency check that compares the database's header
     * information against the
     * on-disk
     * image of the database and if possible, repair any discrepancies found.<br>
     * 2) Perform a document by document consistency check of the entire database,
     * that compares
     * each
     * note's header information against its on-disk image and if possible, repair
     * any discrepancies
     * found.<br>
     * 3) Delete all bad documents/notes that could not be corrected during the
     * consistency check.
     * NSFDbOpenExtended with {@link #FIXUP} will not succeed if db_name specifies a
     * directory.
     * This flag will prevent the replicator from opening the specified database.
     */
    FIXUP((short) 0x0008),

    /**
     * Scan all notes and all items (not incremental)
     */
    FIXUP_FULL_NOTE_SCAN((short) 0x0010),

    /**
     * Do not delete bad notes during note scan
     */
    FIXUP_NO_NOTE_DELETE((short) 0x0020);

    private final short m_value;

    OpenDatabase(final short value) {
      this.m_value = value;
    }

    @Override
    public long getLongValue() {
      return this.m_value & 0xffff;
    }

    @Override
    public Short getValue() {
      return this.m_value;
    }
  }

  /**
   * Listener to get replication progress
   */
  public interface ReplicationStateListener {

    /**
     * Replication aborted
     */
    void abort();

    /**
     * Signal view is building.
     */
    void buildView();

    /**
     * Starting the connection.
     *
     * @param server remove server
     * @param port   port
     */
    void connecting(String server, String port);

    /**
     * Signal the file is done.
     *
     * @param localFile     local filepath
     * @param replFileStats stats
     */
    void doneFile(String localFile, String replFileStats);

    /**
     * Indicating the connection is done.
     */
    void idle();

    /**
     * Display that it is trying to select a server.
     */
    void pickServer();

    /**
     * A "pull" replication.
     *
     * @param serverFile filepath on server
     * @param localFile  local filepath
     */
    void receiving(String serverFile, String localFile);

    /**
     * Signal found a redirect.
     *
     * @param serverFile server filepath
     * @param localFile  local filepath
     */
    void redirect(String serverFile, String localFile);

    /**
     * Searching for matching replica on the server
     *
     * @param server remove server
     * @param port   port
     */
    void searching(String server, String port);

    /**
     * Replicator is in the searching phase.
     *
     * @param srcFile source db filepath
     */
    void searchingDocs(String srcFile);

    /**
     * A "push" replication.
     *
     * @param serverFile filepath on server
     * @param localFile  local filepath
     */
    void sending(String serverFile, String localFile);
  }

  /**
   * Adds a listener to the client that will be called when certain lifecycle
   * events occur.
   *
   * @param listener the listener to add
   * @since 1.0.2
   */
  void addLifecycleListener(LifecycleListener listener);

  @Override
  void close();

  /**
   * This function compresses a local database to remove the space left by
   * deleting documents,
   * freeing up disk space.<br>
   * Deletion stubs however, are left intact in the database.
   *
   * @param pathname path of local database
   * @param mode     compact flags
   * @return the original and compacted size of the NSF
   */
  Pair<Double,Double> compact(String pathname, Set<CompactMode> mode);

  /**
   * Creates a new database on the target server with a given file path.
   *
   * @param serverName    Domino server name to connect to, or an empty string for
   *                      the current server
   * @param filePath      the file path of the destination database
   * @param forceCreation to overwrite an existing database if applicable
   * @param initDesign    true to run a DXL import on the new database which
   *                      creates a view and basic
   *                      structures like the DB icon and the design collection;
   *                      if false, you need to run your
   *                      own initialization since the created database cannot yet
   *                      be opened in the Notes Client
   * @param encryption    encryption level for new database
   * @return a database object for the newly-created database
   * @throws IllegalArgumentException if {@code filePath} is empty
   * @throws DominoException          if the database already exists and
   *                                  {@code forceCreation} is
   *                                  {@code false}
   */
  Database createDatabase(String serverName, String filePath, boolean forceCreation,
      boolean initDesign,
      Encryption encryption);

  /**
   * Creates a new database on the target server with a given file path.
   *
   * @param serverName    Domino server name to connect to, or an empty string for
   *                      the current server
   * @param filePath      the file path of the destination database
   * @param forceCreation to overwrite an existing database if applicable
   * @param initDesign    true to run a DXL import on the new database which
   *                      creates a view and basic
   *                      structures like the DB icon and the design collection;
   *                      if false, you need to run your
   *                      own initialization since the created database cannot yet
   *                      be opened in the Notes Client
   * @param encryption    encryption level for new database
   * @param dbClass       type of database to create
   * @return a database object for the newly-created database
   * @throws IllegalArgumentException if {@code filePath} is empty
   * @throws DominoException          if the database already exists and
   *                                  {@code forceCreation} is
   *                                  {@code false}
   */
  Database createDatabase(String serverName, String filePath, boolean forceCreation,
      boolean initDesign,
      Encryption encryption, DatabaseClass dbClass);

  /**
   * Creates a new database from the specified template DB. Handles copying all
   * design/data
   * documents,
   * setting the inherited template name and creating a ACL based on ACL entries
   * of the template,
   * e.g. a "[Group1]" entry of the template becomes a "Group1" entry in the
   * created DB.
   *
   * @param sourceServerName server name of template database
   * @param sourceFilePath   filepath of template database
   * @param targetServerName Domino server name to connect to, or an empty string
   *                         for the current
   *                         server
   * @param targetFilePath   the file path of the destination database
   * @param encryption       encryption level for new database
   * @return a database object for the newly-created database
   */
  Database createDatabaseFromTemplate(String sourceServerName, String sourceFilePath,
      String targetServerName, String targetFilePath,
      Encryption encryption);

  /**
   * Creates a new database replica, which is a new database with copied
   * design/data documents
   * and the same replica ID as the specified DB.
   *
   * @param sourceServerName server name of database to create the replica for
   * @param sourceFilePath   filepath of database to create the replica for
   * @param targetServerName Domino server name to connect to, or an empty string
   *                         for the current
   *                         server
   * @param targetFilePath   the file path of the destination database
   * @param encryption       encryption level for new database
   * @return a database object for the newly-created replica database
   */
  Database createDatabaseReplica(String sourceServerName, String sourceFilePath,
      String targetServerName, String targetFilePath,
      Encryption encryption);

  /**
   * Creates a new {@link DominoDateRange} of two {@link Temporal} values.
   * Some examples of supported {@link Temporal} value:<br>
   * <ul>
   * <li><code>Instant.now()</code></li>
   * <li><code>LocalDate.now()</code></li>
   * <li><code>LocalTime.now()</code></li>
   * <li><code>ZoneId zone = ZoneId.systemDefault(); Instant now=Instant.now(); OffsetDateTime.ofInstant(now, zone);</code></li>
   * </ul>
   *
   * @param start start temporal
   * @param end   end temporal
   * @return range
   */
  DominoDateRange createDateRange(TemporalAccessor start, TemporalAccessor end);

  /**
   * Creates a {@link DominoDateTime} instance for the provided
   * {@link TemporalAccessor} object.<br>
   * Some examples of supported {@link TemporalAccessor} value:<br>
   * <ul>
   * <li>{@link DominoDateTime}</li>
   * <li><code>Instant.now()</code></li>
   * <li><code>LocalDate.now()</code></li>
   * <li><code>LocalTime.now()</code></li>
   * <li><code>ZoneId zone = ZoneId.systemDefault(); Instant now=Instant.now(); OffsetDateTime.ofInstant(now, zone);</code></li>
   * </ul>
   *
   * @param temporal the temporal implementation to convert; must not be
   *                 {@code null}
   * @return a {@link DominoDateTime} instance that represents the value of
   *         {@code temporal}
   * @throws NullPointerException     if {@code temporal} is {@code null}
   * @throws IllegalArgumentException if {@code temporal} contains a time zone
   *                                  that cannot be
   *                                  expressed
   *                                  in 15-minute increments
   */
  DominoDateTime createDateTime(TemporalAccessor temporal);

  /**
   * @return a newly-created DXL exporter
   */
  DxlExporter createDxlExporter();

  /**
   * @return a newly-created DXL importer
   */
  DxlImporter createDxlImporter();

  /**
   * Compiles a formula
   *
   * @param formula formula to compile
   * @return compiled formula
   * @throws FormulaCompilationException if formula has wrong syntax
   */
  Formula createFormula(String formula) throws FormulaCompilationException;

  /**
   * Creates a new, empty ID table.
   *
   * @return a newly-created ID table
   */
  IDTable createIDTable();

  /**
   * Creates a {@link DominoUniversalNoteId} for a given UNID string
   * that can be used in {@link Document#replaceItemValue(String, Object)}
   * to create an item of type {@link ItemDataType#TYPE_NOTEREF_LIST} to
   * reference another {@link Document}, e.g. in a response hierarchy.
   *
   * @param unidStr UNID String
   * @return Domino universal id
   */
  DominoUniversalNoteId createUNID(String unidStr);
  
  /**
   * Creates a {@link UserData} object for the given format name and byte data
   * that can be used in {@link Document#replaceItemValue(String, Object)}
   * to create an item of type {@link ItemDataType#TYPE_USERDATA}.
   * 
   * @param formatName the programmer-visible format name for the data
   * @param data the data as a byte array
   * @return the newly-created {@link UserData} object
   * @since 1.12.0
   */
  UserData createUserData(String formatName, byte[] data);

  /**
   * Deletes the database at the specified path.
   *
   * @param serverName Domino server name to connect to, empty string for current
   *                   server
   * @param filePath   path of the database relative to Domino's data directory
   */
  void deleteDatabase(String serverName, String filePath);

  /**
   * This function returns a {@link BuildVersionInfo} object which contains all
   * types of
   * information about the level of code running on the specified server.<br>
   * <br>
   * See {@link BuildVersionInfo} for more information.
   *
   * @param server to check the version for, e.g. "" for local environment
   * @return version
   */
  BuildVersionInfo getBuildVersion(String server);

  /**
   * Method to access the calendaring and scheduling API of domino.
   *
   * @return instance of {@link Calendaring}
   */
  Calendaring getCalendaring();
  
  /**
   * Retrieves the data and non-data modification times for the given database by path,
   * without opening the database.
   * 
   * <p>The path in {@code dbPath} must be a path within the current runtime's data
   * directory.</p>
   * 
   * @param dbPath a data-relative path to a database
   * @return a {@link ModificationTimePair} instance
   * @since 1.0.32
   */
  ModificationTimePair getDatabaseModificationTimes(String dbPath);

  /**
   * Retrieves a list of databases on the target server, optionally restricted to
   * those modified since a given time.
   * 
   * <p>When using a local client runtime, this method is not reliable; use
   * {@link #openDbDirectory()} instead.</p>
   *
   * @param serverName    the server to query, or {@code null} for local
   * @param modifiedSince the start time of the query, or {@code null} for all
   *                      databases
   * @return a {@link DatabaseChangePathList} object containing the path list and
   *         new since time
   */
  DatabaseChangePathList getDatabasePaths(String serverName, TemporalAccessor modifiedSince);

  /**
   * Obtains an accessor object for the Notes/Domino runtime configuration
   *
   * @return a new {@link DominoRuntime} object
   */
  DominoRuntime getDominoRuntime();

  /**
   * Returns the {@link Ecl} for a names list (what the user is allowed to do)
   *
   * @param eclType   ECL type
   * @param namesList usernameslist, e.g. with the same content as
   *                  <code>@UserNamesList</code>
   * @return ECL
   */
  Ecl getEcl(ECLType eclType, List<String> namesList);

  /**
   * Returns the {@link Ecl} for a username (what the user is allowed to do)
   *
   * @param eclType  ECL type
   * @param userName username either abbreviated or canonical
   * @return ECL
   */
  Ecl getEcl(ECLType eclType, String userName);

  /**
   * Returns the current Domino username code will run as
   *
   * @return current "running-as" Domino username
   */
  String getEffectiveUserName();

  /**
   * Computes a list of name variants and groups of the current effective user
   * on the specified server
   *
   * @param server server, use empty string for local environment
   * @return user names list
   */
  UserNamesList getEffectiveUserNamesList(String server);

  /**
   * Method to access the free-busy schedules of a Domino server.
   *
   * @return instance of {@link FreeBusy} to access the free-busy schedules
   */
  FreeBusy getFreeBusy();

  /**
   * Returns the current Domino ID name used to access Domino server / client
   *
   * @return current ID's Domino username
   */
  String getIDUserName();

  /**
   * @return access to the IdVault
   */
  IdVault getIdVault();

  /**
   * Retrieves a list of servers known to the current Domino environment.
   *
   * @param portName the port name to query, or {@code null} for all ports
   * @return a {@link Collection} of distinguished server names
   */
  Collection<String> getKnownServers(String portName);

  /**
   * Method to access the message queues of the Domino API. Message queues provide
   * IPC communication
   * between
   * all processes that access the Domino API.
   *
   * @return an instance of {@link MessageQueues} to querying and creation
   */
  MessageQueues getMessageQueues();

  /**
   * Returns a utility class to read MIME content from a
   * {@link com.hcl.domino.data.Document
   * Document}.
   *
   * @return an instance of {@link MimeReader} to read MIME
   */
  MimeReader getMimeReader();

  /**
   * Returns a utility class to write MIME content to a
   * {@link com.hcl.domino.data.Document
   * Document}
   *
   * @return an instance of {@link MimeWriter} to write MIME
   */
  MimeWriter getMimeWriter();

  /**
   * Returns a utility class that provides information about a user
   *
   * @param username username
   * @return person class
   */
  Person getPerson(String username);

  /**
   * Returns replication utilities
   *
   * @return replication
   */
  Replication getReplication();

  /**
   * Returns a utility class to render a document or single item as HTML
   *
   * @return converter
   */
  RichTextHTMLConverter getRichTextHtmlConverter();

  /**
   * Returns a utility class with server administration features
   *
   * @return admin utils
   */
  ServerAdmin getServerAdmin();

  /**
   * Get access to Server ACL / ECL according to the provided directory.
   *
   * @param directoryServer the name of a server containing the directory to
   *                        consult, or
   *                        {@code null} to use the local runtime
   * @param serverName      the server to look up
   * @return a {@link ServerInfo} object providing access to the server info
   * @throws IllegalArgumentException if {@code serverName} is empty
   */
  ServerInfo getServerInfo(String directoryServer, final String serverName);
  
  /**
   * Returns a utility class with server-statistic-manipulation features
   * 
   * @return statistic utils
   * @since 1.37.0
   */
  ServerStatistics getServerStatistics();

  /**
   * Obtains a {@link ThreadFactory} implementation that produces
   * Notes-initialized threads.
   *
   * @return a Notes-aware {@link ThreadFactory} instance
   */
  default ThreadFactory getThreadFactory() {
    return JNXThread::new;
  }

  /**
   * This will be used to restrict access to the admin APIs, managed against the
   * Java application's permitted access
   *
   * @return whether or not the session can access admin APIs
   */
  boolean isAdmin();

  /**
   * Returns <code>true</code> if databases are supposed to be opened with full
   * access
   * (if the current user has sufficient rights on the accessed server)
   *
   * @return true if full access
   */
  boolean isFullAccess();

  /**
   * Whether the code is running on a Domino server or client
   *
   * @return true for server, false for client
   */
  boolean isOnServer();

  /**
   * Opens a database by path. This path may be relative to the data directory of
   * the current
   * machine or may contain the name of a remote server. The database path may
   * also be a replica
   * ID.
   * <p>
   * Some examples of legal database paths are:
   * </p>
   * <ul>
   * <li><code>foo/bar.nsf</code></li>
   * <li><code>852584A8:00507284</code></li>
   * <li><code>ServerName/OrgName!!foo/bar.nsf</code></li>
   * <li><code>CN=ServerName/O=OrgName!!foo/bar.nsf</code></li>
   * <li><code>ServerName/OrgName!!852584A8:00507284</code></li>
   * </ul>
   *
   * @param path a path of the database
   * @return database object for data access
   */
  Database openDatabase(String path);

  /**
   * Opens a database by path. This path may be relative to the data directory of
   * the current
   * machine or may contain the name of a remote server. The database path may
   * also be a replica
   * ID.
   * <p>
   * Some examples of legal database paths are:
   * </p>
   * <ul>
   * <li><code>foo/bar.nsf</code></li>
   * <li><code>852584A8:00507284</code></li>
   * <li><code>ServerName/OrgName!!foo/bar.nsf</code></li>
   * <li><code>CN=ServerName/O=OrgName!!foo/bar.nsf</code></li>
   * <li><code>ServerName/OrgName!!852584A8:00507284</code></li>
   * </ul>
   *
   * @param path    a path of the database
   * @param options set of options for if the database is not accessible
   * @return Database database object for data access
   */
  Database openDatabase(String path, Set<OpenDatabase> options);

  /**
   * Opens a database. Data queries against a remote server will be slower
   *
   * @param serverName Domino server name to connect to, or an empty string for
   *                   the current server
   * @param filePath   path of the database relative to Domino's data directory or
   *                   a replica
   *                   ID
   * @return Database database object for data access
   */
  Database openDatabase(String serverName, String filePath);

  /**
   * Opens a database with options if database is not accessible.
   * Data queries against a remote server will be slower
   *
   * @param serverName Domino server name to connect to, empty string for current
   *                   server
   * @param filePath   path of the database relative to Domino's data directory or
   *                   a replica
   *                   ID
   * @param options    set of options for if the database is not accessible
   * @return Database database object for data access
   */
  Database openDatabase(String serverName, String filePath, Set<OpenDatabase> options);

  /**
   * Opens the db-directory to enumerate database (and other) files as well as
   * subdirectories
   * in the data-directory of a server (or locally)
   *
   * @return the directory
   */
  DbDirectory openDbDirectory();

  /**
   * Opens the mail database if the current user
   *
   * @return an {@link Optional} describing the user's mail database, or an empty
   *         one
   *         if the user has no mail database or it cannot be found
   */
  Optional<Database> openMailDatabase();

  /**
   * Opens the mail database if the current user
   *
   * @param options set of options for if the database is not accessible
   * @return an {@link Optional} describing the user's mail database, or an empty
   *         one
   *         if the user has no mail database or it cannot be found
   */
  Optional<Database> openMailDatabase(Set<OpenDatabase> options);

  /**
   * Opens the effective user directory for the provided server. This allows
   * lookup of
   * user, server, and group names using the server's primary and secondary
   * directories.
   *
   * @param serverName the name of the server to query, or {@code null} to use the
   *                   local
   *                   runtime
   * @return the {@link UserDirectory} for the server
   * @since 1.0.2
   */
  UserDirectory openUserDirectory(String serverName);

  /**
   * Attempts to ping the named server, optionally retrieving additional
   * information.
   *
   * @param serverName          the name of the server to ping
   * @param retrieveLoadIndex   whether to query the server's availability index
   * @param retrieveClusterInfo whether to query the server's cluster name and
   *                            peers
   * @return a {@link ServerPingInfo} instance optionally containing load index
   *         and cluster
   *         information
   * @throws ServerNotFoundException    if the server cannot be resolved
   * @throws ServerUnavailableException if the server is resolved but busy
   * @throws ServerRestrictedException  if the server is resolved but restricted
   * @since 1.0.20
   */
  ServerPingInfo pingServer(String serverName, boolean retrieveLoadIndex,
      boolean retrieveClusterInfo);

  /**
   * This routine replicates Domino database files on the local system with a
   * specified server.<br>
   * <br>
   * Either all common files can be replicated or a specified list of files can be
   * replicated.<br>
   * <br>
   * Replication can be performed in either direction or both directions (push,
   * pull, or both).<br>
   * <br>
   * <b>Please note:<br>
   * Run this method inside {@link #runInterruptable(Callable, IBreakHandler)}
   * to be able to cancel the process and inside
   * {@link #runWithProgress(Callable, IProgressListener)}
   * to get progress info.</b>
   *
   * @param serverName       destination server (either abbreviated or canonical
   *                         format)
   * @param options          replication options
   * @param fileList         list of files to replicate, use server!!filepath
   *                         format to specify databases on
   *                         other servers
   * @param timeLimitMin     If non-zero, number of minutes replication is allowed
   *                         to execute before
   *                         cancellation. If not specified, no limit is imposed
   * @param progressListener replication progress listener or null if not required
   * @return replication stats
   */
  NotesReplicationStats replicateDbsWithServer(String serverName, Set<ReplicateOption> options,
      List<String> fileList, int timeLimitMin, ReplicationStateListener progressListener);

  /**
   * Converts a Domino API error code into a {@link DominoException}.
   *
   * @param code error code
   * @return an {@link Optional} describing a newly-created instance of
   *         {@link DominoException} or a
   *         subclass,
   *         an empty one if {@code result} is 0
   */
  Optional<DominoException> resolveErrorCode(int code);

  /**
   * Runs a block of code asynchronously in a background thread
   *
   * @param <T>      result type
   * @param callable code to run
   * @return FutureTask to get progress/result
   */
  <T> FutureTask<T> runAsync(Callable<T> callable);

  /**
   * Runs a block of code asynchronously in a background thread
   *
   * @param <T>      result type
   * @param service  {@link ExecutorService} to select the background thread
   * @param callable code to run
   * @return FutureTask to get progress/result and cancel the operation
   */
  <T> FutureTask<T> runAsync(ExecutorService service, Callable<T> callable);

  /**
   * The method registers a break signal handler for the execution time of the
   * specified
   * {@link Callable}. The break signal handler can be used to send a break signal
   * to Domino
   * so that the current (probably long running) operation, e.g. a fulltext on a
   * remote
   * database, can be interrupted.
   *
   * @param callable     callable to execute
   * @param breakHandler break handler to interrupt the current operation
   * @return optional result
   * @param <T> result type
   */
  <T> T runInterruptable(Callable<T> callable, final IBreakHandler breakHandler);

  /**
   * The method registers a progress signal handler for the execution time of the
   * specified
   * {@link Callable}. The progress signal handler can be used to get notified
   * about the
   * progress of method execution, e.g. replication or copy operations.
   *
   * @param callable        callable to execute
   * @param progressHandler progress handler to get notified about progress
   *                        changes
   * @return optional result
   * @param <T> result type
   */
  <T> T runWithProgress(Callable<T> callable, final IProgressListener progressHandler);

  /**
   * Attempts to validate the provider username and password credentials using the
   * named server.
   *
   * @param serverName the name of the server to validate against, or {@code null}
   *                   to use the local
   *                   runtime
   * @param userName   a name for the user; may be a short name or other user ID
   * @param password   the password to check for the user
   * @return the user's canonical name in Notes format
   * @throws IllegalArgumentException            if userName is empty
   * @throws NameNotFoundException               if the provided name cannot be
   *                                             found in any directory
   * @throws AuthenticationException             if the provided password does not
   *                                             match the user's password
   * @throws AuthenticationNotSupportedException if the user exists but does not
   *                                             have a specified
   *                                             password
   */
  String validateCredentials(String serverName, String userName, String password)
      throws NameNotFoundException, AuthenticationException, AuthenticationNotSupportedException;

  /**
   * Attempts to validate the provided credentials token with any registered
   * providers.
   *
   * @param token      the token to use to authenticate. The class of the token
   *                   depends on the available
   *                   provider implementations
   * @param serverName the name of the server to contact
   * @return the in-memory user ID
   * @throws IllegalArgumentException when no provider can be found to handle
   *                                  {@code token}
   * @since 1.0.19
   * @throws NameNotFoundException               if the implementation finds that
   *                                             the name cannot be found
   * @throws AuthenticationException             if the implementation finds that
   *                                             the credentials cannot be
   *                                             validated
   * @throws AuthenticationNotSupportedException if the implementation finds that
   *                                             the user exists
   *                                             but cannot be validated with that
   *                                             token
   * @throws UnsupportedOperationException       if no implementation is found
   *                                             that can handle the
   *                                             provided token
   */
  String validateCredentialsWithToken(String serverName, Object token)
      throws NameNotFoundException, AuthenticationException, AuthenticationNotSupportedException;

  /**
   * Verifies that the provided LDAP connection information is valid and usable.
   * 
   * @param hostName the name of the LDAP host
   * @param userName the user name to use to connect for simple bindings
   * @param password the password to use to connect
   * @param dnSearch a DN search base
   * @param port the port to use to connect to the server
   * @param useSSL whether the connection should use TLS/SSL
   * @param acceptExpiredCerts whether to allow expired TLS certificates
   * @param verifyRemoteServerCert whether to verify the validity of the remote TLS certificate
   * @throws DominoException if the connection cannot be validated. The specific exception details
   *         will include the reason for failure
   * @since 1.0.39
   */
  void verifyLdapConnection(String hostName, String userName, String password, String dnSearch,
      boolean useSSL,short port,  boolean acceptExpiredCerts, boolean verifyRemoteServerCert);

}
