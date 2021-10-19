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
package com.hcl.domino.jna;

import static com.hcl.domino.commons.util.NotesErrorUtils.checkResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.NameNotFoundException;

import com.hcl.domino.BuildVersionInfo;
import com.hcl.domino.DominoException;
import com.hcl.domino.UserNamesList;
import com.hcl.domino.admin.AdministrationProcess;
import com.hcl.domino.admin.ServerAdmin;
import com.hcl.domino.admin.idvault.IdVault;
import com.hcl.domino.admin.replication.Replication;
import com.hcl.domino.calendar.Calendaring;
import com.hcl.domino.commons.NotYetImplementedException;
import com.hcl.domino.commons.constants.CopyDatabase;
import com.hcl.domino.commons.data.DefaultDominoDateRange;
import com.hcl.domino.commons.data.DefaultModificationTimePair;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.CAPIGarbageCollector;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCControl;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.server.DefaultServerPingInfo;
import com.hcl.domino.commons.util.DominoUtils;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.CompactMode;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Database.ReplicateOption;
import com.hcl.domino.data.DatabaseChangePathList;
import com.hcl.domino.data.DatabaseClass;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.DominoUniversalNoteId;
import com.hcl.domino.data.Formula;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.data.ModificationTimePair;
import com.hcl.domino.dbdirectory.DbDirectory;
import com.hcl.domino.dxl.DxlExporter;
import com.hcl.domino.dxl.DxlImporter;
import com.hcl.domino.dxl.DxlImporter.DXLImportOption;
import com.hcl.domino.exception.BadPasswordException;
import com.hcl.domino.freebusy.FreeBusy;
import com.hcl.domino.html.RichTextHTMLConverter;
import com.hcl.domino.jna.admin.JNAAdministrationProcess;
import com.hcl.domino.jna.admin.JNAIdVault;
import com.hcl.domino.jna.admin.JNAServerAdmin;
import com.hcl.domino.jna.admin.replication.JNAReplication;
import com.hcl.domino.jna.calendaring.JNACalendaring;
import com.hcl.domino.jna.data.JNADatabase;
import com.hcl.domino.jna.data.JNADominoCollection;
import com.hcl.domino.jna.data.JNADominoDateTime;
import com.hcl.domino.jna.data.JNADominoUniversalNoteId;
import com.hcl.domino.jna.data.JNAFormula;
import com.hcl.domino.jna.data.JNAIDTable;
import com.hcl.domino.jna.data.JNAUserNamesList;
import com.hcl.domino.jna.dbdirectory.JNADbDirectory;
import com.hcl.domino.jna.dxl.JNADxlExporter;
import com.hcl.domino.jna.dxl.JNADxlImporter;
import com.hcl.domino.jna.freebusy.JNAFreeBusy;
import com.hcl.domino.jna.html.JNARichtextHTMLConverter;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.ItemDecoder;
import com.hcl.domino.jna.internal.JNANotesConstants;
import com.hcl.domino.jna.internal.JNANotesReplicationStats;
import com.hcl.domino.jna.internal.JNASignalHandlerUtil;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.NotesNamingUtils;
import com.hcl.domino.jna.internal.NotesNamingUtils.Privileges;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNADatabaseAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNADominoClientAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNAUserNamesListAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.HANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.structs.DbOptionsStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.hcl.domino.jna.internal.structs.ReplExtensionsStruct;
import com.hcl.domino.jna.internal.structs.ReplServStatsStruct;
import com.hcl.domino.jna.internal.structs.VerifyLDAPConnectionStruct;
import com.hcl.domino.jna.mime.JNAMimeReader;
import com.hcl.domino.jna.mime.JNAMimeWriter;
import com.hcl.domino.jna.mq.JNAMessageQueues;
import com.hcl.domino.jna.naming.JNAUserDirectory;
import com.hcl.domino.jna.person.JNAPerson;
import com.hcl.domino.jna.runtime.JNADominoRuntime;
import com.hcl.domino.jna.security.JNAEcl;
import com.hcl.domino.jna.utils.JNADominoUtils;
import com.hcl.domino.mime.MimeReader;
import com.hcl.domino.mime.MimeWriter;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.mq.MessageQueues;
import com.hcl.domino.naming.UserDirectory;
import com.hcl.domino.person.Person;
import com.hcl.domino.runtime.DominoRuntime;
import com.hcl.domino.security.Acl;
import com.hcl.domino.security.AclFlag;
import com.hcl.domino.security.AclLevel;
import com.hcl.domino.security.Ecl;
import com.hcl.domino.server.ServerPingInfo;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;

public class JNADominoClient implements IGCDominoClient<JNADominoClientAllocations> {
  public static final String PROP_DEFAULTQUEUEFLUSH = "jnx.gc.defaultqueueflush"; //$NON-NLS-1$
  public static final String ENV_DEFAULTQUEUEFLUSH = "JNX_GC_DEFAULTQUEUEFLUSH"; //$NON-NLS-1$
  public static final String PROP_ALLOWCROSSTHREAD = "jnx.allowCrossThreadAccess"; //$NON-NLS-1$
  public static final String ENV_ALLOWCROSSTHREAD = "JNX_ALLOWCROSSTHREADACCESS"; //$NON-NLS-1$

  private Thread m_parentThread;
  private JNADominoClientBuilder m_builder;
  private Map<String, Object> m_customValues;
  private JNADominoClientAllocations m_allocations;
  private JNADominoRuntime m_dominoRuntime;
  private Boolean m_isOnServer;
  private String m_effectiveUserName;
  private IGCControl m_gcCtrl;
  private final Set<LifecycleListener> listeners = new LinkedHashSet<>();
  private boolean m_allowCrossThreadAccess;
  private boolean m_registeredForGC;
  private BuildVersionInfo localBuildVersionInfo;

  JNADominoClient(JNADominoClientBuilder builder) {
    m_builder = builder;

    m_parentThread = Thread.currentThread();
    m_customValues = new HashMap<>();

    // IGCControl implementation to auto-flush the GC reference queue every 50 object allocations by
    // default
    Integer queueFlushThreshold = AccessController.doPrivileged((PrivilegedAction<Integer>) () -> {
      String str = System.getProperty(PROP_DEFAULTQUEUEFLUSH);

      if (StringUtil.isEmpty(str)) {
        str = System.getenv(ENV_DEFAULTQUEUEFLUSH);
      }

      if (!StringUtil.isEmpty(str)) {
        try {
          return Integer.valueOf(str);
        } catch (NumberFormatException e) {
          System.err.println(
              MessageFormat.format("Invalid value for default GC ref queue flush: {0}", str));
        }
      }

      return null;
    });
    if (queueFlushThreshold == null) {
      queueFlushThreshold = 50;
    }
    m_gcCtrl = new DefaultGCControl(queueFlushThreshold);

    CAPIGarbageCollector.registerDominoClient(this);
    m_allocations = new JNADominoClientAllocations(this, null, null,
        CAPIGarbageCollector.getReferenceQueueForClient(this));

    String credUser = builder.getCredUser();
    if (StringUtil.isNotEmpty(credUser)) {
      try {
        m_effectiveUserName = validateCredentials(builder.getCredServer(), builder.getCredUser(),
            builder.getCredPassword());
      } catch (AuthenticationNotSupportedException | AuthenticationException
          | NameNotFoundException e) {
        throw new DominoException("Exception when validating user credentials", e);
      }
    } else {
      Object credToken = builder.getCredToken();
      if (credToken != null) {
        try {
          m_effectiveUserName = validateCredentialsWithToken(builder.getCredServer(), credToken);
        } catch (AuthenticationNotSupportedException | AuthenticationException
            | NameNotFoundException e) {
          throw new DominoException("Exception when validating user credentials", e);
        }
      }
    }

    m_allowCrossThreadAccess = AccessController.doPrivileged((PrivilegedAction<Boolean>) () -> {
      if (!"false".equals(System.getProperty(PROP_ALLOWCROSSTHREAD)) && //$NON-NLS-1$
          !"false".equals(System.getenv(ENV_ALLOWCROSSTHREAD))) { //$NON-NLS-1$
        // cross thread access on by default
        return true;
      } else {
        return false;
      }
    });
  }

  @Override
  public String getEffectiveUserName() {
    if (m_effectiveUserName == null) {
      JNADominoClientBuilder clientBuilder = getBuilder();

      if (clientBuilder.isAsIDUser() || StringUtil.isEmpty(clientBuilder.getUserName())) {
        m_effectiveUserName = getIDUserName();
      } else if (!StringUtil.isEmpty(clientBuilder.getUserName())) {
        m_effectiveUserName = NotesNamingUtils.toCanonicalName(clientBuilder.getUserName());
      } else if (clientBuilder.getUserNamesList() != null
          && !clientBuilder.getUserNamesList().isEmpty()) {
        m_effectiveUserName =
            NotesNamingUtils.toCanonicalName(clientBuilder.getUserNamesList().get(0));
      } else {
        if (isOnServer()) {
          m_effectiveUserName = "Anonymous"; //$NON-NLS-1$
        } else {
          m_effectiveUserName = getIDUserName();
        }
      }
    }
    return m_effectiveUserName;
  }

  @Override
  public UserNamesList getEffectiveUserNamesList(String server) {
    JNAUserNamesList namesList;

    JNADominoClientBuilder clientBuilder = getBuilder();

    if (clientBuilder.getUserNamesList() != null && !clientBuilder.getUserNamesList().isEmpty()) {
      // special case where the full usernameslist is already provided
      namesList = NotesNamingUtils.writeNewNamesList(this, clientBuilder.getUserNamesList());
    } else {
      namesList = NotesNamingUtils.buildNamesList(this, server, getEffectiveUserName());
    }

    // setting authenticated flag for the user is required when running on the server
    if (isFullAccess()) {
      NotesNamingUtils.setPrivileges(namesList, EnumSet.of(Privileges.FullAdminAccess,
          Privileges.Authenticated));
    } else {
      if (clientBuilder.isMaxInternetAccess()) {
        NotesNamingUtils.setPrivileges(namesList,
            EnumSet.of(Privileges.Authenticated, Privileges.PasswordAuthenticated));
      } else {
        NotesNamingUtils.setPrivileges(namesList, EnumSet.of(Privileges.Authenticated));
      }
    }

    return namesList;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getAdapter(Class<T> clazz) {
    if (clazz == JNADominoClientBuilder.class) {
      return (T) m_builder;
    } else if (clazz == APIObjectAllocations.class) {
      return (T) m_allocations;
    } else if (clazz == Thread.class) {
      return (T) m_parentThread;
    } else if (clazz == IGCControl.class) {
      return (T) m_gcCtrl;
    }

    return null;
  }

  public JNADominoClientBuilder getBuilder() {
    return m_builder;
  }

  @Override
  public String getIDUserName() {
    Memory retUserNameMem = new Memory(NotesConstants.MAXUSERNAME + 1);

    short result = NotesCAPI.get().SECKFMGetUserName(retUserNameMem);
    checkResult(result);

    int userNameLength = 0;
    for (int i = 0; i < retUserNameMem.size(); i++) {
      userNameLength = i;
      if (retUserNameMem.getByte(i) == 0) {
        break;
      }
    }

    String userName = NotesStringUtils.fromLMBCS(retUserNameMem, userNameLength);
    return userName;
  }

  @Override
  public boolean isOnServer() {
    if (m_isOnServer == null) {
      m_isOnServer = NotesConstants.PROCESS_GROUP_NOTES_SERVER == NotesCAPI.get()
          .OSProcessGroup(NotesConstants.PROCESS_GROUP_QUERY);
    }
    return m_isOnServer;
  }

  private String[] resolveDbPath(String path) {
    int iPos = path.indexOf("!!"); //$NON-NLS-1$
    if (iPos != -1) {
      String server = path.substring(0, iPos);
      String filePath = path.substring(iPos + 2);
      return new String[] {server, filePath};
    } else {
      return new String[] {"", path}; //$NON-NLS-1$
    }
  }

  @Override
  public Database createDatabase(String paramServerName, String filePath, boolean forceCreation,
      boolean initDesign,
      Encryption encryption, DatabaseClass dbClass) {

    if (StringUtil.isEmpty(filePath)) {
      throw new IllegalArgumentException("filePath cannot be empty");
    }
    String serverName = paramServerName;
    if (serverName == null) {
      serverName = ""; //$NON-NLS-1$
    }

    serverName = NotesNamingUtils.toCanonicalName(serverName);

    boolean isOnServer = isOnServer();

    if (!"".equals(serverName)) { //$NON-NLS-1$
      if (isOnServer) {
        String serverCN = NotesNamingUtils.toCommonName(serverName);
        String currServerCanonical = getIDUserName();
        String currServerCN = NotesNamingUtils.toCommonName(currServerCanonical);

        if (serverCN.equalsIgnoreCase(currServerCN)) {
          // switch to "" as servername if server points to the server the API is running on
          serverName = ""; //$NON-NLS-1$
        }
      }
    }

    JNAUserNamesList userNamesList = (JNAUserNamesList) getEffectiveUserNamesList(serverName);

    byte encryptStrengthByte = NotesConstants.DBCREATE_ENCRYPT_NONE;
    switch (encryption) {
      case Simple:
        encryptStrengthByte = NotesConstants.DBCREATE_ENCRYPT_SIMPLE;
        break;
      case Medium:
        encryptStrengthByte = NotesConstants.DBCREATE_ENCRYPT_MEDIUM;
        break;
      case Strong:
        encryptStrengthByte = NotesConstants.DBCREATE_ENCRYPT_STRONG;
        break;
      case AES128:
        encryptStrengthByte = NotesConstants.DBCREATE_ENCRYPT_AES128;
        break;
      case AES256:
        encryptStrengthByte = NotesConstants.DBCREATE_ENCRYPT_AES256;
        break;
      case None:
      default:
        encryptStrengthByte = NotesConstants.DBCREATE_ENCRYPT_NONE;
        break;
    }
    final byte fEncryptStrengthByte = encryptStrengthByte;

    short dbClassShort = dbClass.getValue();
    short optionsShort =
        NotesConstants.DBCREATE_FORM_BUCKET_OPT | NotesConstants.DBCREATE_LARGE_UNKTABLE;
    if (encryptStrengthByte != 0) {
      optionsShort |= NotesConstants.DBCREATE_LOCALSECURITY;
    }
    final short fOptionsShort = optionsShort;

    DisposableMemory path = JNADominoUtils.constructNetPath(this, serverName, filePath);

    JNAUserNamesListAllocations namesListAllocations = userNamesList != null
        ? (JNAUserNamesListAllocations) userNamesList.getAdapter(APIObjectAllocations.class)
        : null;

    int options2 = 0;

    DbOptionsStruct.ByValue defaultDbOptionsByVal = DbOptionsStruct.newInstanceByVal();

    checkResult(LockUtil.lockHandle(
        namesListAllocations == null ? null : namesListAllocations.getHandle(), (hNamesList) -> {
          return NotesCAPI.get().NSFDbCreateExtended4(path, dbClassShort, forceCreation,
              fOptionsShort,
              options2, fEncryptStrengthByte, 0, (Memory) null, (Memory) null,
              (short) 0, (short) 0, defaultDbOptionsByVal, hNamesList, (DHANDLE.ByValue) null);
        }));

    // create ACL
    HANDLE.ByReference rethDb = HANDLE.newInstanceByReference();
    checkResult(NotesCAPI.get().NSFDbOpen(path, rethDb));

    LockUtil.lockHandle(rethDb, (rethDbByVal) -> {
      try {
        DHANDLE.ByReference rethACL = DHANDLE.newInstanceByReference();
        checkResult(NotesCAPI.get().ACLCreate(rethACL));

        checkResult(LockUtil.lockHandle(rethACL, (rethACLByVal) -> {
          return NotesCAPI.get().NSFDbStoreACL(rethDbByVal, rethACLByVal, 0, (short) 1);
        }));

        return 0;
      } finally {
        checkResult(NotesCAPI.get().NSFDbClose(rethDbByVal));
      }
    });

    Database db = openDatabase(serverName, filePath);

    // the method creates the design collection which is missing after a NSFDbCreate
    JNADominoCollection designCollection = (JNADominoCollection) db.openDesignCollection();
    if (designCollection != null) {
      designCollection.dispose();
    }

    if (initDesign) {
      InputStream in = null;
      try {
        // use DXL importer to create basic structures like the icon note
        // and the design collection
        in = JNANotesConstants.class.getResourceAsStream("blank_dxl.xml"); //$NON-NLS-1$
        if (in == null) {
          throw new IllegalStateException("File blank_dxl.xml not found");
        }

        JNADxlImporter importer = (JNADxlImporter) createDxlImporter();
        importer.setDesignImportOption(DXLImportOption.REPLACE_ELSE_CREATE);
        importer.setReplaceDbProperties(true);
        importer.importDxl(in, db);
        importer.dispose();
      } catch (IOException e) {
        throw new DominoException(MessageFormat
            .format("Error initializing design of new database {0}!!{1}", serverName, filePath), e);
      } finally {
        if (in != null) {
          try {
            in.close();
          } catch (IOException e) {
          }
        }
      }
    }

    // write default acl entries, might lock us out
    Acl acl = db.getACL();

    AclLevel defaultAccessLevel = AclLevel.NOACCESS;
    String manager = getEffectiveUserName();

    acl.updateEntry("-Default-", "-Default-", defaultAccessLevel, Collections.emptyList(), //$NON-NLS-1$ //$NON-NLS-2$
        EnumSet.noneOf(AclFlag.class));
    acl.updateEntry("OtherDomainServers", null, AclLevel.NOACCESS, Collections.emptyList(), //$NON-NLS-1$
        EnumSet.of(AclFlag.GROUP, AclFlag.SERVER));
    acl.updateEntry(manager, null, AclLevel.MANAGER, Collections.emptyList(),
        EnumSet.noneOf(AclFlag.class));

    if (db.isRemote()) {
      acl.updateEntry(db.getServer(), null, AclLevel.MANAGER, Collections.emptyList(),
          EnumSet.of(AclFlag.SERVER, AclFlag.ADMIN_SERVER));
      acl.setAdminServer(db.getServer());
    }

    acl.updateEntry("LocalDomainServers", null, AclLevel.MANAGER, Collections.emptyList(), //$NON-NLS-1$
        EnumSet.of(AclFlag.GROUP, AclFlag.SERVER));

    acl.save();

    return db;
  }

  @Override
  public Database createDatabaseFromTemplate(String sourceServerName, String sourceFilePath,
      String targetServerName, String targetFilePath,
      Encryption encryption) {

    EnumSet<CopyDatabase> copyFlags = EnumSet.noneOf(CopyDatabase.class);
    if (encryption == Encryption.Simple) {
      copyFlags.add(CopyDatabase.ENCRYPT_SIMPLE);
    } else if (encryption == Encryption.Medium) {
      copyFlags.add(CopyDatabase.ENCRYPT_MEDIUM);
    } else if (encryption == Encryption.Strong) {
      copyFlags.add(CopyDatabase.ENCRYPT_STRONG);
    }

    Database newDb = createAndCopyDatabase(sourceServerName, sourceFilePath, targetServerName,
        targetFilePath, null, 0,
        (Set<CopyDatabase>) null, null);
    return newDb;
  }

  @Override
  public Database createDatabaseReplica(String sourceServerName, String sourceFilePath,
      String targetServerName, String targetFilePath,
      Encryption encryption) {

    EnumSet<CopyDatabase> copyFlags =
        EnumSet.of(CopyDatabase.REPLICA, CopyDatabase.REPLICA_NAMELIST);
    if (encryption == Encryption.Simple) {
      copyFlags.add(CopyDatabase.ENCRYPT_SIMPLE);
    } else if (encryption == Encryption.Medium) {
      copyFlags.add(CopyDatabase.ENCRYPT_MEDIUM);
    } else if (encryption == Encryption.Strong) {
      copyFlags.add(CopyDatabase.ENCRYPT_STRONG);
    }

    return createAndCopyDatabase(sourceServerName, sourceFilePath, targetServerName, targetFilePath,
        null, 0, copyFlags, null);
  }

  /**
   * This function creates a new copy of a Domino database based on database at
   * <code>sourceServerName / sourceFilePath</code>
   * and allows for a {@link JNAUserNamesList} structure UserName to provide authentication for
   * trusted servers.<br>
   * <br>
   * The database class of the new database is based on the file extension specified by
   * <code>sourceServerName / sourceFilePath</code>.<br>
   * <br>
   * Specifically, the new copy will contain the replication settings, database options, Access
   * Control List,
   * Full Text Index (if any), as well as data and non-data notes (dependent on the DocumentClass
   * argument) of
   * the original database.<br>
   * <br>
   * You may specify the types of notes that you want copied to the new database with the
   * <code>docClassesToCopy</code> argument.<br>
   * <br>
   * You may also specify the maximum size (database quota) that the database can grow to with the
   * <code>maxFileSize</code> argument.<br>
   * <br>
   * Additionally, you may specify that the new database is to be a replica copy of the original
   * database,
   * meaning that it will share the same replica ID.<br>
   *
   * @param sourceDb database to copy
   * @param targetServerName server name of new database to be created
   * @param targetFilePath filepath of new database to be created
   * @param docClassesToCopy type of notes to copy or <code>null</code> to copy all content
   * @param maxFileSize Size limit for new database in bytes, will be rounded to full megabytes.
   *        This argument will control how large the new copy can grow to. Specify a value of zero
   *        if you do not wish to place a size limit on the newly copied database.
   * @param copyFlags Option flags determining type of copy. Currently, the only supported flags are
   *        {@link CopyDatabase#REPLICA}, {@link CopyDatabase#ENCRYPT_SIMPLE},
   *        {@link CopyDatabase#ENCRYPT_MEDIUM}, {@link CopyDatabase#ENCRYPT_STRONG},
   *        {@link CopyDatabase#REPLICA_NAMELIST}, {@link CopyDatabase#OVERRIDE_DEST}.
   * @param namesList may be null or a UserName that is used to provide authentication for trusted
   *        servers. This causes the UserName's ACL permissions in the database to be enforced.
   *        Please see {@link NotesNamingUtils#buildNamesList(String)} NSFBuildNamesList for more
   *        information on building a NAMES_LIST structure.
   * @return database copy
   */
  private Database createAndCopyDatabase(String sourceServerName, String sourceFilePath,
      String targetServerName, String targetFilePath,
      Set<DocumentClass> docClassesToCopy,
      long maxFileSize, Set<CopyDatabase> copyFlags,
      JNAUserNamesList namesList) {

    // Open the source database to account for odd cases (e.g. shared data directories)
    Database sourceDb = getParentDominoClient().openDatabase(sourceServerName, sourceFilePath);

    DisposableMemory fullPathTargetMem =
        JNADominoUtils.constructNetPath(this, targetServerName, targetFilePath);
    DisposableMemory fullPathSourceMem =
        JNADominoUtils.constructNetPath(this, sourceDb.getServer(), sourceDb.getRelativeFilePath());

    short noteClassToCopy = docClassesToCopy == null ? NotesConstants.NOTE_CLASS_ALL
        : DominoEnumUtil.toBitField(DocumentClass.class, docClassesToCopy);

    int createCopyFlags = CopyDatabase.toBitMask(copyFlags);
    createCopyFlags |= NotesConstants.DBCOPY_DEST_IS_NSF;

    short result;

    JNAUserNamesList namesListForDbCreate;
    if (namesList == null) {
      namesListForDbCreate = (JNAUserNamesList) getEffectiveUserNamesList(targetServerName);
    } else {
      namesListForDbCreate = namesList;
    }
    JNAUserNamesList fNamesListForDbCreate = namesListForDbCreate;

    short maxFileSizeInMB = (short) ((maxFileSize / (1024 * 1024)) & 0xffff);

    HANDLE.ByReference rethNewDb = HANDLE.newInstanceByReference();

    result = NotesCAPI.get().NSFDbCreateAndCopyExtended(fullPathSourceMem, fullPathTargetMem,
        noteClassToCopy, maxFileSizeInMB, createCopyFlags, null, rethNewDb);
    checkResult(result);

    return new JNADatabase(this, new IAdaptable() {

      @Override
      @SuppressWarnings("unchecked")
      public <T> T getAdapter(Class<T> clazz) {
        if (HANDLE.class.equals(clazz)) {
          return (T) rethNewDb;
        } else if (JNAUserNamesList.class.equals(clazz)) {
          return (T) fNamesListForDbCreate;
        } else {
          return null;
        }
      }
    });
  }

  @Override
  public Database openDatabase(String path, Set<OpenDatabase> options) {
    String[] resolvedServerAndPath = resolveDbPath(path);
    if (StringUtil.isEmpty(resolvedServerAndPath[1])) {
      throw new DominoException(0,
          MessageFormat.format("Unable to find database for path {0}", path));
    }

    return openDatabase(resolvedServerAndPath[0], resolvedServerAndPath[1], options);
  }

  @Override
  public Database openDatabase(String serverName, String filePath, Set<OpenDatabase> options) {
    if (serverName == null) {
      serverName = ""; //$NON-NLS-1$
    }
    serverName = NotesNamingUtils.toCanonicalName(serverName);

    if (DominoUtils.isReplicaId(filePath)) {
      // scan directory for db with replica id

      try (JNADatabase dir =
          new JNADatabase(this, serverName, "", EnumSet.noneOf(OpenDatabase.class));) { //$NON-NLS-1$
        int[] innards = NotesStringUtils.replicaIdToInnards(filePath);

        NotesTimeDateStruct replicaIdStruct = NotesTimeDateStruct.newInstance(innards);

        DisposableMemory retPathNameMem = new DisposableMemory(NotesConstants.MAXPATH);
        try {
          JNADatabaseAllocations dbAllocations =
              (JNADatabaseAllocations) dir.getAdapter(APIObjectAllocations.class);
          short result = LockUtil.lockHandle(dbAllocations.getDBHandle(), (hDirByVal) -> {
            return NotesCAPI.get().NSFDbLocateByReplicaID(hDirByVal, replicaIdStruct,
                retPathNameMem, (short) (NotesConstants.MAXPATH & 0xffff));
          });

          if (result == 259) {
            throw new DominoException(result, MessageFormat.format(
                "No database found on server ''{0}'' for replica id {1}", serverName, filePath));
          }

          checkResult(result);

          String retPathName = NotesStringUtils.fromLMBCS(retPathNameMem, -1);
          if (retPathName == null || retPathName.length() == 0) {
            throw new DominoException(result, MessageFormat.format(
                "No database found on server ''{0}'' for replica id {1}", serverName, filePath));
          } else {
            return new JNADatabase(this, serverName, retPathName, options);
          }

        } finally {
          retPathNameMem.dispose();
        }
      }
    } else {
      return new JNADatabase(this, serverName, filePath, options);
    }
  }

  @Override
  public Optional<Database> openMailDatabase(Set<OpenDatabase> options) {
    String mailServer;
    String mailFile;

    DominoRuntime runtime = getDominoRuntime();

    if (!isOnServer()) {
      mailServer = runtime.getPropertyString("MailServer"); //$NON-NLS-1$
      mailFile = runtime.getPropertyString("MailFile"); //$NON-NLS-1$
    } else {
      String effectiveUsername = getEffectiveUserName();

      JNAFormula serverFormula = (JNAFormula) createFormula(
          "@NameLookup( [NoUpdate] ; \"" + effectiveUsername + "\"; \"MailServer\"  )"); //$NON-NLS-1$ //$NON-NLS-2$
      mailServer = serverFormula.evaluateAsString();
      serverFormula.dispose();

      JNAFormula mailFileFormula = (JNAFormula) createFormula(
          "@NameLookup( [NoUpdate] ; \"" + effectiveUsername + "\"; \"MailFile\"  )"); //$NON-NLS-1$ //$NON-NLS-2$
      mailFile = mailFileFormula.evaluateAsString();
      mailFileFormula.dispose();
    }

    if (StringUtil.isEmpty(mailFile)) {
      return Optional.empty();
    }

    return Optional.of(openDatabase(mailServer, mailFile, options));
  }

  @Override
  public void deleteDatabase(final String paramServerName, final String filePath) {
    if (StringUtil.isEmpty(filePath)) {
      throw new IllegalArgumentException("filePath cannot be empty");
    }
    String serverName = paramServerName;
    if (serverName == null) {
      serverName = ""; //$NON-NLS-1$
    }
    serverName = NotesNamingUtils.toCanonicalName(serverName);

    DisposableMemory path = JNADominoUtils.constructNetPath(this, serverName, filePath);
    checkResult(NotesCAPI.get().NSFDbDelete(path));
  }

  @Override
  public boolean isAdmin() {
    // TODO Auto-generated method stub
    throw new NotYetImplementedException();
  }

  @Override
  public boolean isFullAccess() {
    return m_builder.isFullAccess();
  }

  @Override
  public DominoRuntime getDominoRuntime() {
    if (m_dominoRuntime == null) {
      m_dominoRuntime = new JNADominoRuntime(this);
    }
    return m_dominoRuntime;
  }

  /**
   * Frees all memory and internal handles held by this Domino client session
   */
  public void dispose() {
    if (m_allocations != null && !m_allocations.isDisposed()) {
      CAPIGarbageCollector.dispose(this);

      CAPIGarbageCollector.unregisterDominoClient(this);
      m_allocations = null;
      // CAPIGarbageCollector.decRefCountForDominoEnabledThread();
    }
  }

  /**
   * Checks if this Domino client session object is already recycled
   *
   * @return true if recycled
   */
  public boolean isDisposed() {
    return m_allocations == null || m_allocations.isDisposed();
  }

  @Override
  public void close() {
    if (m_allocations != null && !m_allocations.isDisposed()) {
      dispose();

      this.listeners.forEach(l -> l.onClose(this));
    }
  }

  /**
   * Use this method to store your own custom values for this Domino client.
   *
   * @param key key
   * @param value value, implement interface {@link IDisposableCustomValue} to get called for
   *        disposal
   * @return previous value
   */
  public Object setCustomValue(String key, Object value) {
    return m_customValues.put(key, value);
  }

  /**
   * Reads a custom value stored via {@link #setCustomValue(String, Object)}
   * execution block.
   *
   * @param key the key of the stored custom value
   * @return value or null if not set
   */
  public Object getCustomValue(String key) {
    return m_customValues.get(key);
  }

  /**
   * Tests if a custom value has been set via {@link #setCustomValue(String, Object)}.
   *
   * @param key key
   * @return true if value is set
   */
  public boolean hasCustomValue(String key) {
    return m_customValues.containsKey(key);
  }

  /**
   * When using {@code NotesGC#setCustomValue(String, Object)} to store your own
   * values, use this
   * interface for your value to get called for disposal when the
   * {@code NotesGC#runWithAutoGC(Callable)}
   * block is finished. Otherwise the value is just removed from the intermap map.
   *
   * @author Karsten Lehmann
   */
  public interface IDisposableCustomValue {
    void dispose();
  }

  @Override
  public IAPIObject<?> getParent() {
    return null;
  }

  @Override
  public JNADominoClient getParentDominoClient() {
    return this;
  }

  @Override
  public DominoDateTime createDateTime(TemporalAccessor temporal) {
    return JNADominoDateTime.from(temporal);
  }

  @Override
  public DominoDateRange createDateRange(TemporalAccessor start, TemporalAccessor end) {
    DominoDateTime startDT = createDateTime(start);
    DominoDateTime endDT = createDateTime(end);

    return new DefaultDominoDateRange(startDT, endDT);
  }

  @Override
  public DxlExporter createDxlExporter() {
    IntByReference rethDxlExporter = new IntByReference();
    NotesCAPI.get().DXLCreateExporter(rethDxlExporter);
    return new JNADxlExporter(this, rethDxlExporter.getValue());
  }

  @Override
  public DxlImporter createDxlImporter() {
    IntByReference rethDxlImporter = new IntByReference();
    NotesCAPI.get().DXLCreateImporter(rethDxlImporter);
    return new JNADxlImporter(this, rethDxlImporter.getValue());
  }

  @Override
  public DbDirectory openDbDirectory() {
    return new JNADbDirectory(this);
  }

  @Override
  public FreeBusy getFreeBusy() {
    return new JNAFreeBusy(this);
  }

  @Override
  public Calendaring getCalendaring() {
    return new JNACalendaring(this);
  }

  @Override
  public MessageQueues getMessageQueues() {
    return new JNAMessageQueues(this);
  }

  @Override
  public MimeReader getMimeReader() {
    return new JNAMimeReader(this);
  }

  @Override
  public MimeWriter getMimeWriter() {
    return new JNAMimeWriter(this);
  }

  @Override
  public JNAIDTable createIDTable() {
    return new JNAIDTable(this);
  }

  @Override
  public Formula createFormula(String formula) {
    return new JNAFormula(this, formula);
  }

  @Override
  public void verifyLdapConnection(
      String hostName, String userName, String password, String dnSearch, boolean useSSL,
      short port, boolean acceptExpiredCerts, boolean verifyRemoteServerCert) {

    VerifyLDAPConnectionStruct ldap = new VerifyLDAPConnectionStruct();

    NotesStringUtils.toLMBCS(hostName, true, ldap.szHostName);
    NotesStringUtils.toLMBCS(userName, true, ldap.szUserName);
    NotesStringUtils.toLMBCS(password, true, ldap.szPassword);
    NotesStringUtils.toLMBCS(dnSearch, true, ldap.szDNSearch);

    ldap.bAcceptExpiredCertificates = acceptExpiredCerts;
    ldap.bVerifyRemoteSrvCert = verifyRemoteServerCert;
    ldap.wPort = port;
    ldap.write();
    NotesErrorUtils.checkResult(NotesCAPI.get().VerifyLDAPConnection(ldap));
  }

  @Override
  public DominoUniversalNoteId createUNID(String unidStr) {
    return new JNADominoUniversalNoteId(unidStr);
  }

  @Override
  public RichTextHTMLConverter getRichTextHtmlConverter() {
    return new JNARichtextHTMLConverter(this);
  }


  @Override
  public NotesReplicationStats replicateDbsWithServer(String serverName,
      Set<ReplicateOption> options,
      List<String> fileList, int timeLimitMin, ReplicationStateListener progressListener) {

    if (timeLimitMin > 65535) {
      throw new IllegalArgumentException("Time limit cannot be greater than 65535 minutes.");
    }

    Memory serverNameMem =
        NotesStringUtils.toLMBCS(NotesNamingUtils.toAbbreviatedName(serverName), true);

    ReplServStatsStruct retStatsStruct = ReplServStatsStruct.newInstance();
    ReplExtensionsStruct extendedOptions = ReplExtensionsStruct.newInstance();
    extendedOptions.Size = 2 + 2;
    extendedOptions.TimeLimit = (short) (timeLimitMin & 0xffff);
    extendedOptions.write();

    int numFiles = 0;
    DisposableMemory fileListMem = null;
    if (fileList != null && !fileList.isEmpty()) {
      if (fileList.size() > 65535) {
        throw new IllegalArgumentException("Number of files cannot be greater than 65535.");
      }
      numFiles = (short) (fileList.size() & 0xffff);
      ByteArrayOutputStream bOut = new ByteArrayOutputStream();

      for (String currFileName : fileList) {
        if (currFileName.length() > 0) {
          Memory currFileNameMem = NotesStringUtils.toLMBCS(currFileName, true);
          try {
            bOut.write(currFileNameMem.getByteArray(0, (int) currFileNameMem.size()));
          } catch (IOException e) {
            throw new DominoException(0, "Error writing file list to memory", e);
          }
        }
      }
      fileListMem = new DisposableMemory(bOut.size());
      byte[] bOutArr = bOut.toByteArray();
      fileListMem.write(0, bOutArr, 0, bOutArr.length);
    }

    int optionsInt = DominoEnumUtil.toBitField(ReplicateOption.class, options);

    short result;
    if (progressListener != null) {
      final int fNumFiles = numFiles;
      final DisposableMemory fFileListMem = fileListMem;

      try {
        JNASignalHandlerUtil.runWithReplicationStateTracking(() -> {
          short result1 = NotesCAPI.get().ReplicateWithServerExt(null, serverNameMem, optionsInt,
              (short) (fNumFiles & 0xffff),
              fFileListMem, extendedOptions, retStatsStruct);
          checkResult(result1);
          return null;
        }, progressListener);
      } catch (Exception e) {
        throw new DominoException("Error replicating with state tracking", e);
      }
    } else {
      result = NotesCAPI.get().ReplicateWithServerExt(null, serverNameMem, optionsInt,
          (short) (numFiles & 0xffff),
          fileListMem, extendedOptions, retStatsStruct);
      checkResult(result);
    }

    retStatsStruct.read();

    if (fileListMem != null) {
      fileListMem.dispose();
    }

    NotesReplicationStats retStats = new JNANotesReplicationStats(retStatsStruct);
    return retStats;
  }

  @Override
  public <T> T runInterruptable(Callable<T> callable, IBreakHandler breakHandler) {
    return JNASignalHandlerUtil.runInterruptable(callable, breakHandler);
  }

  @Override
  public <T> T runWithProgress(Callable<T> callable, IProgressListener progressHandler) {
    return JNASignalHandlerUtil.runWithProgress(callable, progressHandler);
  }

  @Override
  public ServerAdmin getServerAdmin() {
    return new JNAServerAdmin(this);
  }

  @Override
  public Collection<String> getKnownServers(String portName) {
    Memory pPortName = NotesStringUtils.toLMBCS(portName, true);
    DHANDLE.ByReference hServerTextList = DHANDLE.newInstanceByReference();

    checkResult(NotesCAPI.get().NSGetServerList(pPortName, hServerTextList));
    if (hServerTextList.isNull()) {
      return Collections.emptyList();
    }

    return LockUtil.lockHandle(hServerTextList, hServerTextListVal -> {
      return Mem.OSLockObject(hServerTextListVal, ptr -> {
        int serverCount = Short.toUnsignedInt(ptr.getShort(0));

        Pointer pNameLengths = ptr.share(2); // WORD
        Pointer pNames = pNameLengths.share(serverCount * 2); // WORD

        int[] nameLengths = IntStream.range(0, serverCount)
            .map(i -> Short.toUnsignedInt(pNameLengths.getShort(i * 2)))
            .toArray();

        Pointer pCurrentName = pNames;
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        for (int i = 0; i < serverCount; i++) {
          result.add(NotesStringUtils.fromLMBCS(pCurrentName, nameLengths[i]));
          pCurrentName = pCurrentName.share(nameLengths[i]);
        }
        return result;
      });
    });
  }

  @Override
  public DatabaseChangePathList getDatabasePaths(String serverName, TemporalAccessor since) {
    Memory pServerName = NotesStringUtils.toLMBCS(serverName, true);
    JNADominoDateTime sinceDt =
        new JNADominoDateTime(since == null ? JNADominoDateTime.createMinimumDateTime() : since);
    NotesTimeDateStruct.ByReference sinceStruct = NotesTimeDateStruct.newInstanceByReference(sinceDt.getInnards());

    LongByReference changesSize = new LongByReference();
    DHANDLE.ByReference hChanges = DHANDLE.newInstanceByReference();
    NotesTimeDateStruct.ByReference nextSinceTime = NotesTimeDateStruct.newInstanceByReference();

    checkResult(NotesCAPI.get().NSFGetChangedDBs(pServerName, sinceStruct, changesSize, hChanges,
        nextSinceTime));
    nextSinceTime.read();

    return LockUtil.lockHandle(hChanges, hChangesVal -> {
      Collection<String> paths = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      
      if (hChangesVal!=null && !hChangesVal.isNull()) {
        Mem.OSLockObject(hChangesVal, ptr -> {
          long remaining = changesSize.getValue();
          Pointer pPath = ptr;
          while (remaining > 0) {
            int strlen = NotesStringUtils.getNullTerminatedLength(pPath);
            paths.add(NotesStringUtils.fromLMBCS(pPath, strlen));

            pPath = pPath.share(strlen + 1);
            remaining -= strlen + 1;
          }
          return null;
        });
      }
      
      return new DatabaseChangePathList(paths, new JNADominoDateTime(nextSinceTime.Innards));
    });
  }

  @Override
  public IdVault getIdVault() {
    return new JNAIdVault(this);
  }

  @Override
  public Person getPerson(String username) {
    return new JNAPerson(this, username);
  }

  @Override
  public String validateCredentials(String serverName, String userName, String password)
      throws NameNotFoundException, AuthenticationNotSupportedException, AuthenticationException {
    // TODO investigate LDAP in DirAssist
    // TODO investigate enforcing "fewer names" rule from server doc
    // TODO verify that behavior matches HTTP login when there are multiple users matching the name

    if (StringUtil.isEmpty(userName)) {
      throw new IllegalArgumentException("userName cannot be empty");
    }

    UserDirectory dir = openUserDirectory(serverName);
    List<Map<String, List<Object>>> result = dir.query()
        .namespaces(Collections.singleton(NotesConstants.USERNAMESSPACE))
        .names(Collections.singleton(userName))
        .items(
            Arrays.asList(NotesConstants.MAIL_FULLNAME_ITEM, NotesConstants.MAIL_HTTPPASSWORD_ITEM))
        .stream()
        .findFirst()
        .orElseThrow(() -> new NameNotFoundException(
            MessageFormat.format("Unable to locate name \"{0}\"", userName)));
    if (result.isEmpty()) {
      throw new NameNotFoundException(
          MessageFormat.format("Unable to locate name \"{0}\"", userName));
    }

    String fullName =
        StringUtil.getFirstString(result.get(0).get(NotesConstants.MAIL_FULLNAME_ITEM));
    String httpPassword =
        StringUtil.getFirstString(result.get(0).get(NotesConstants.MAIL_HTTPPASSWORD_ITEM));
    if (StringUtil.isEmpty(httpPassword)) {
      throw new AuthenticationNotSupportedException(
          MessageFormat.format("User \"{0}\" has no specified password", userName));
    }

    Memory passwordPtr = NotesStringUtils.toLMBCS(password, false);
    Memory digestPtr = NotesStringUtils.toLMBCS(httpPassword, false);
    try {
      checkResult(NotesCAPI.get().SECVerifyPassword(
          (short) passwordPtr.size(),
          passwordPtr,
          (short) digestPtr.size(),
          digestPtr,
          0,
          null));
    } catch (BadPasswordException e) {
      throw new AuthenticationException("Passwords do not match");
    }

    return StringUtil.isEmpty(fullName) ? userName : fullName;
  }

  @Override
  public Ecl getEcl(ECLType eclType, List<String> namesList) {
    return new JNAEcl(this, eclType, namesList);
  }

  @Override
  public Ecl getEcl(ECLType eclType, String userName) {
    return new JNAEcl(this, eclType, userName);
  }

  @Override
  public Replication getReplication() {
    return new JNAReplication(this);
  }

  @Override
  public UserDirectory openUserDirectory(String serverName) {
    return new JNAUserDirectory(this, serverName);
  }

  @Override
  public void addLifecycleListener(LifecycleListener listener) {
    if (listener != null) {
      this.listeners.add(listener);
    }
  }

  private static class DefaultGCControl implements IGCControl {
    private AtomicLong m_objCount;
    private int m_threshold;

    public DefaultGCControl(int defaultThreshold) {
      m_threshold = defaultThreshold;
      m_objCount = new AtomicLong();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public GCAction objectAllocated(IAPIObject parent, IAPIObject obj) {
      if (m_threshold != 0) {
        long count = m_objCount.incrementAndGet();
        if ((count & m_threshold) == 0) {
          return GCAction.FLUSH_REFQUEUE;
        }
      }

      return GCAction.NOOP;
    }

    @Override
    public void setThreshold(int threshold) {
      m_threshold = threshold;
    }

  }

  @Override
  protected void finalize() {
    close();
  }

  @Override
  public boolean isAllowCrossThreadAccess() {
    return m_allowCrossThreadAccess;
  }

  public void setAllowCrossThreadAccess(boolean b) {
    this.m_allowCrossThreadAccess = b;
  }

  @Override
  public void markRegisteredForGC() {
    m_registeredForGC = true;
  }

  @Override
  public boolean isRegisteredForGC() {
    return m_registeredForGC;
  }

  @Override
  public AdministrationProcess getAdministrationProcess(String serverName) {
    return new JNAAdministrationProcess(this, serverName);
  }


  @Override
  public void compact(String pathname, Set<CompactMode> mode) {
    if (StringUtil.isEmpty(pathname)) {
      throw new IllegalArgumentException("Pathname cannot be empty");
    }

    int options1 = 0;
    int options2 = 0;

    if (mode != null) {
      for (CompactMode currMode : mode) {
        if (currMode.getSetIndex() == 1) {
          options1 |= currMode.getValue();
        } else if (currMode.getSetIndex() == 2) {
          options1 |= currMode.getValue();
        }
      }
    }

    int timeLimit = 0;

    Memory pathnameMem = NotesStringUtils.toLMBCS(pathname, true);

    short result = NotesCAPI.get().NSFDbCompactExtended4(pathnameMem, options1,
        options2, timeLimit, null, null, null);

    checkResult(result);
  }

  @Override
  public ServerPingInfo pingServer(String serverName, boolean retrieveLoadIndex,
      boolean retrieveClusterInfo) {
    if (StringUtil.isEmpty(serverName)) {
      throw new IllegalArgumentException("serverName cannot be empty");
    }

    Memory serverNameLmbcs = NotesStringUtils.toLMBCS(serverName, true);
    IntByReference pLoadIndex = retrieveLoadIndex ? new IntByReference() : null;
    DHANDLE.ByReference phList = retrieveClusterInfo ? DHANDLE.newInstanceByReference() : null;

    checkResult(NotesCAPI.get().NSPingServer(serverNameLmbcs, pLoadIndex, phList));

    Optional<Integer> loadIndex =
        retrieveLoadIndex ? Optional.of(pLoadIndex.getValue()) : Optional.empty();
    Optional<String> clusterName;
    Optional<List<String>> clusterMembers;
    if (retrieveClusterInfo) {
      // If requested but the server is not in a cluster, the handle will remain null
      if (!phList.isNull()) {
        List<String> clusterInfo =
            LockUtil.lockHandle(phList, hList -> Mem.OSLockObject(hList, ptr -> {
              List<Object> decoded = ItemDecoder.decodeTextListValue(ptr, false);
              return decoded.stream().map(String::valueOf).collect(Collectors.toList());
            }));
        clusterName = Optional.of(clusterInfo.get(0));
        clusterMembers = Optional.of(clusterInfo.subList(1, clusterInfo.size()));
      } else {
        clusterName = Optional.of(""); //$NON-NLS-1$
        clusterMembers = Optional.of(Collections.emptyList());
      }
    } else {
      clusterName = Optional.empty();
      clusterMembers = Optional.empty();
    }

    return new DefaultServerPingInfo(loadIndex, clusterName, clusterMembers);
  }

  @Override
  public BuildVersionInfo getBuildVersion(String server) {
    if (StringUtil.isEmpty(server) && localBuildVersionInfo != null) {
      // use cached local api version
      return localBuildVersionInfo;
    }

    BuildVersionInfo buildVersionInfo;
    try (JNADatabase db = new JNADatabase(this, server, "", //$NON-NLS-1$
        Collections.emptySet());) {
      buildVersionInfo = db.getBuildVersionInfo();
    }

    if (StringUtil.isEmpty(server)) {
      localBuildVersionInfo = buildVersionInfo;
    }

    return buildVersionInfo;
  }

  @Override
  public Optional<DominoException> resolveErrorCode(int code) {
    return NotesErrorUtils.toNotesError((short) (code & 0xffff));
  }
  
  @Override
  public ModificationTimePair getDatabaseModificationTimes(String dbPath) {
    Memory pDbPath = NotesStringUtils.toLMBCS(dbPath, true);
    NotesTimeDateStruct.ByReference retDataModifiedStruct = NotesTimeDateStruct.newInstanceByReference();
    NotesTimeDateStruct.ByReference retNonDataModifiedStruct = NotesTimeDateStruct.newInstanceByReference();
    
    checkResult(NotesCAPI.get().NSFDbModifiedTimeByName(pDbPath, retDataModifiedStruct, retNonDataModifiedStruct));
    
    DominoDateTime dataModified = new JNADominoDateTime(retDataModifiedStruct.Innards);
    DominoDateTime nonDataModified = new JNADominoDateTime(retNonDataModifiedStruct.Innards);
    
    return new DefaultModificationTimePair(dataModified, nonDataModified);
  }
}
