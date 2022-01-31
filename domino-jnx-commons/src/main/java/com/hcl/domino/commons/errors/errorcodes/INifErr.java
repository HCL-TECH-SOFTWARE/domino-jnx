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
package com.hcl.domino.commons.errors.errorcodes;

import com.hcl.domino.commons.errors.ErrorText;

public interface INifErr extends IGlobalErr {

  @ErrorText(text = "Cannot collate using unsortable datatype")
  short ERR_SORT_DATATYPE = IGlobalErr.PKG_NIF + 1;
  @ErrorText(text = "Index corrupted - will be rebuilt automatically when database is closed or server is restarted")
  short ERR_BAD_COLLECTION = IGlobalErr.PKG_NIF + 2;
  @ErrorText(text = "Collection not open")
  short ERR_COLLECTION_HANDLE = IGlobalErr.PKG_NIF + 3;
  @ErrorText(text = "(more to do - call again)")
  short ERR_MORE_TO_DO = IGlobalErr.PKG_NIF + 4;
  @ErrorText(text = "Informational, rebuild view needed - collection object was deleted (reading %s %s note Title:'%s')")
  short ERR_COLLECTION_DELETED = IGlobalErr.PKG_NIF + 5;
  @ErrorText(text = "Unsupported return flag(s)")
  short ERR_UNSUPPORTED_FLAGS = IGlobalErr.PKG_NIF + 6;
  @ErrorText(text = "Error looking up name in Domino Directory")
  short ERR_LOOKUP_ERROR = IGlobalErr.PKG_NIF + 7;
  @ErrorText(text = "Index entry has too many levels")
  short ERR_TOO_MANY_LEVELS = IGlobalErr.PKG_NIF + 8;
  @ErrorText(text = "Domino Directory does not contain a required view")
  short ERR_NO_SUCH_NAMESPACE = IGlobalErr.PKG_NIF + 9;
  @ErrorText(text = "Domino Directory does not exist")
  short ERR_NO_NAMES_FILE = IGlobalErr.PKG_NIF + 10;
  @ErrorText(text = "(Collection does not exist, and was not created)")
  short ERR_COLLECTION_NOT_CREATED = IGlobalErr.PKG_NIF + 11;
  @ErrorText(text = "No document to navigate to")
  short ERR_NAVIGATE_FAILED = IGlobalErr.PKG_NIF + 12;
  @ErrorText(text = "You are not authorized to access the view")
  short ERR_VIEW_NOACCESS = IGlobalErr.PKG_NIF + 13;
  @ErrorText(text = "Error writing %p (%s) index: %e")
  short ERR_WRITE_COLLECTION_ERR = IGlobalErr.PKG_NIF + 14;
  @ErrorText(text = "No more Domino Directory databases")
  short ERR_NO_SUCH_NAMES_BOOK = IGlobalErr.PKG_NIF + 15;
  @ErrorText(text = "Specified item is not present")
  short ERR_NO_SUCH_ITEM = IGlobalErr.PKG_NIF + 16;
  @ErrorText(text = "(No more members in list)")
  short ERR_NO_MORE_MEMBERS = IGlobalErr.PKG_NIF + 17;
  @ErrorText(text = "Unsupported datatype in Domino Directory document")
  short ERR_UNSUPPORTED_TYPE = IGlobalErr.PKG_NIF + 18;
  @ErrorText(text = "(No more matches in table)")
  short ERR_NO_MORE_MATCH = IGlobalErr.PKG_NIF + 19;
  @ErrorText(text = "Index is not to be generated on server.")
  short ERR_NO_REMOTE_INDEX = IGlobalErr.PKG_NIF + 20;
  @ErrorText(text = "Someone else deleted this index while you were updating it.")
  short ERR_UPDATE_INTERRUPTED = IGlobalErr.PKG_NIF + 21;
  @ErrorText(text = "($Container item not found in view note)")
  short ERR_CONTAINER_ITEM_NOT_FOUND = IGlobalErr.PKG_NIF + 22;
  @ErrorText(text = "(Partial match found with TEXT_PARTIALCOMPARE)")
  short ERR_PARTIAL_MATCH = IGlobalErr.PKG_NIF + 23;
  @ErrorText(text = "User or server name not found in Domino Directory")
  short ERR_USER_NOT_FOUND = IGlobalErr.PKG_NIF + 24;
  @ErrorText(text = "ID file not found in Directory")
  short ERR_IDFILE_NOT_FOUND = IGlobalErr.PKG_NIF + 25;
  @ErrorText(text = "Entry had multiple permutations")
  short ERR_WAS_PERMUTED = IGlobalErr.PKG_NIF + 26;
  @ErrorText(text = "View has a bad collation description.  It can't be read.")
  short ERR_BAD_COLLATION = IGlobalErr.PKG_NIF + 27;
  @ErrorText(text = "Invalid collection data was detected.")
  short ERR_BAD_COLLECTION_DATA = IGlobalErr.PKG_NIF + 28;
  @ErrorText(text = "View is damaged.  Please rebuild it (by pressing shift-F9 or running UPDALL -r).")
  short ERR_REBUILD_VIEW = IGlobalErr.PKG_NIF + 29;
  @ErrorText(text = "View cannot be created.  This is a read-only database from a different platform.")
  short ERR_RDONLY_DIFF_PLATFORM = IGlobalErr.PKG_NIF + 30;
  @ErrorText(text = "Informational, rebuilding view - notes have been purged since last update (reading %s %s note Title:'%s')")
  short ERR_NIF_COLLREBUILDPDL = IGlobalErr.PKG_NIF + 31;
  @ErrorText(text = "Insufficient memory - index pool is full.")
  short ERR_NIF_POOLFULL = IGlobalErr.PKG_NIF + 32;
  @ErrorText(text = "Informational, rebuilding view - validation error (reading %s %s note Title:'%s')")
  short ERR_NIF_COLLREBUILD = IGlobalErr.PKG_NIF + 33;
  @ErrorText(text = "Informational, rebuilding view - no container or index (reading %s %s note Title:'%s')")
  short ERR_NIF_COLLREBUILDNCOI = IGlobalErr.PKG_NIF + 34;
  @ErrorText(text = "Informational, rebuilding view - container integrity lost (reading %s %s note Title:'%s')")
  short ERR_NIF_COLLREBUILDCIL = IGlobalErr.PKG_NIF + 35;
  @ErrorText(text = "Informational, rebuilding view - user specified REBUILD (reading %s %s note Title:'%s')")
  short ERR_NIF_COLLREBUILDCS = IGlobalErr.PKG_NIF + 36;
  @ErrorText(text = "Informational, rebuilding view - database ID changed (reading %s %s note Title:'%s')")
  short ERR_NIF_COLLREBUILDDC = IGlobalErr.PKG_NIF + 37;
  @ErrorText(text = "Informational, rebuilding view - database had cutoff-delete performed (reading %s %s note Title:'%s')")
  short ERR_NIF_COLLREBUILDCDP = IGlobalErr.PKG_NIF + 38;
  @ErrorText(text = "Informational, rebuilding view - database cutoff date later than last update (reading %s %s note Title:'%s')")
  short ERR_NIF_COLLREBUILDCDL = IGlobalErr.PKG_NIF + 39;
  @ErrorText(text = "Informational, rebuilding view - user roles changed (reading %s %s note Title:'%s')")
  short ERR_NIF_COLLREBUILDRC = IGlobalErr.PKG_NIF + 40;
  @ErrorText(text = "Informational, rebuilding view - selection or column formula changed (reading %s %s note Title:'%s')")
  short ERR_NIF_COLLREBUILDFC = IGlobalErr.PKG_NIF + 41;
  @ErrorText(text = "Informational, rebuilding view - collation changed (reading %s %s note Title:'%s')")
  short ERR_NIF_COLLREBUILDCC = IGlobalErr.PKG_NIF + 42;
  @ErrorText(text = "Informational, rebuilding view - subtotal codes changed (reading %s %s note '%s')")
  short ERR_NIF_COLLREBUILDSCC = IGlobalErr.PKG_NIF + 43;
  @ErrorText(text = "Domain <%s> was not found in this server's directory domain tables.  Verify this domain with your DA Configuration")
  short ERR_DA_DOMAIN_NOTFOUND = IGlobalErr.PKG_NIF + 44;
  @ErrorText(text = "Collation number specified negative or greater than number of collations in view.")
  short ERR_BAD_COLLATION_NUM = IGlobalErr.PKG_NIF + 45;
  @ErrorText(text = "The wrong entry was deleted from the collection hash table.")
  short ERR_BAD_CHASHTBL_DELETION = IGlobalErr.PKG_NIF + 46;
  @ErrorText(text = "The wrong entry was deleted from the collection user hash table.")
  short ERR_BAD_CUHASHTBL_DELETION = IGlobalErr.PKG_NIF + 47;
  @ErrorText(text = "Too many Items in Key Buffer pass to NIFFindKey.")
  short ERR_NIF_BAD_KEYBUFFER = IGlobalErr.PKG_NIF + 48;
  @ErrorText(text = "Recipient name not unique, too many found in Directory, buffer exceeded 64K byte limit.")
  short ERR_NIF_TOO_MANY_NAMES = IGlobalErr.PKG_NIF + 49;
  @ErrorText(text = "Named folder not found, operation not done.")
  short ERR_NAMED_FOLDER_NOT_FOUND = IGlobalErr.PKG_NIF + 50;
  @ErrorText(text = "NIF collection hash table is full.")
  short ERR_COLLHASH_POOLFULL = IGlobalErr.PKG_NIF + 51;
  @ErrorText(text = "Informational, rebuild view needed - collection object cannot be read (reading %s %s note Title:'%s')")
  short ERR_NIF_COLLOBJ = IGlobalErr.PKG_NIF + 52;
  @ErrorText(text = "Informational, rebuild view needed - invalid collection header (reading %s %s note Title:'%s')")
  short ERR_NIF_COLLHDR = IGlobalErr.PKG_NIF + 53;
  @ErrorText(text = "Informational, rebuild view needed - collation table has changed (reading %s %s note Title:'%s')")
  short ERR_NIF_COLLTBL = IGlobalErr.PKG_NIF + 54;
  @ErrorText(text = "Error attempting to access the Directory %p specified in notes.ini.  Error is ")
  short ERR_NIF_REMOTE_NAB = IGlobalErr.PKG_NIF + 55;
  @ErrorText(text = "Informational, rebuilding view - previous rebuild did not complete (reading %s %s note Title:'%s')")
  short ERR_NIF_FULLSEARCH = IGlobalErr.PKG_NIF + 56;
  @ErrorText(text = "Updating Domino Directory view '%s'")
  short ERR_NIF_UPDATE_NA_VIEW = IGlobalErr.PKG_NIF + 57;
  @ErrorText(text = "Directory Assistance buffer insufficient.")
  short ERR_DA_BUF_INSUF = IGlobalErr.PKG_NIF + 58;
  @ErrorText(text = "Directory Assistance tables failed to load properly.")
  short ERR_DA_INIT_FAILED = IGlobalErr.PKG_NIF + 59;
  @ErrorText(text = "Directory Assistance tables failed to reload properly.")
  short ERR_DA_RELOAD_FAILED = IGlobalErr.PKG_NIF + 60;
  @ErrorText(text = "Directory Assistance database has one or more invalid rules for domain %s.")
  short ERR_DA_INVALID_RULE = IGlobalErr.PKG_NIF + 61;
  @ErrorText(text = "Directory Assistance database has one or more invalid replicas for domain %s.")
  short ERR_DA_INVALID_REPLICA = IGlobalErr.PKG_NIF + 62;
  @ErrorText(text = "Unable to bind to LDAP Server %s.")
  short ERR_NIF_BIND = IGlobalErr.PKG_NIF + 63;
  @ErrorText(text = "Unable to connect to LDAP Server %s.")
  short ERR_NIF_CONNECT = IGlobalErr.PKG_NIF + 64;
  @ErrorText(text = "BAD URL for LDAP Server %s using port %ld.")
  short ERR_NIF_BAD_URL = IGlobalErr.PKG_NIF + 65;
  @ErrorText(text = "Error looking up name on LDAP Server; See server log for further details.")
  short ERR_NIF_LDAP_LOOKUP = IGlobalErr.PKG_NIF + 66;
  @ErrorText(text = "Failed search on LDAP Server %s Reason: %s")
  short ERR_NIF_LDAP_SEARCH = IGlobalErr.PKG_NIF + 67;
  @ErrorText(text = "No more sort results - EOF.")
  short ERR_NIF_SORT_EOF = IGlobalErr.PKG_NIF + 68;
  @ErrorText(text = "Insufficient memory - NAMELookup data cache is full.")
  short ERR_LOOKUP_CACHE_FULL = IGlobalErr.PKG_NIF + 69;
  @ErrorText(text = "Insufficient memory - NAMELookup data hash pool is full.")
  short ERR_LOOKUP_HASH_FULL = IGlobalErr.PKG_NIF + 70;
  @ErrorText(text = "Informational - VIEW_REBUILD_SCRATCH_DIR invalid, using default temp directory instead.")
  short ERR_NIF_VRB_SCRATCH_DIR_INV = IGlobalErr.PKG_NIF + 71;
  @ErrorText(text = "Anti-folder views must be stored in data database")
  short ERR_MUST_BE_SAME_DB = IGlobalErr.PKG_NIF + 72;
  @ErrorText(text = "Sort did not produce a buffer of results due to time constraints. Call again.")
  short ERR_NIF_SORT_NO_BUF = IGlobalErr.PKG_NIF + 73;
  @ErrorText(text = "NIF hasn't been initialized in this process so buffer cannot be written.")
  short ERR_CANTWRITE_NONIF = IGlobalErr.PKG_NIF + 74;
  @ErrorText(text = "Failed search on LDAP Server %s Reason: %s, DN: %s, Attribute: %s")
  short ERR_NIF_LDAP_ATTRNF = IGlobalErr.PKG_NIF + 75;
  @ErrorText(text = "Failed search on LDAP Server %s, DN: %s, Attribute %s has invalid syntax: %d")
  short ERR_NIF_LDAP_INVSYNTAX = IGlobalErr.PKG_NIF + 76;
  @ErrorText(text = "Error converting userCertificate to Notes internal representation for entry %s attribute %s")
  short ERR_NIF_LDAP_CONVERTING_CERT = IGlobalErr.PKG_NIF + 77;
  @ErrorText(text = "Directory Assistance could not access Directory %p, error: ")
  short ERR_DA_CANT_ACCESS_AB = IGlobalErr.PKG_NIF + 78;
  @ErrorText(text = "Directory Assistance could not find an alternate replica for domain %s")
  short ERR_DA_ALTERNATE_REPLICA_UNAVAILABLE = IGlobalErr.PKG_NIF + 79;
  @ErrorText(text = "Directory Assistance found alternate Directory replica %p for domain %s")
  short ERR_DA_ALTERNATE_REPLICA_FOUND = IGlobalErr.PKG_NIF + 80;
  @ErrorText(text = "Error attempting to access the Directory %p (no available alternatives),  error is ")
  short ERR_NIF_REMOTE_NAB2 = IGlobalErr.PKG_NIF + 81;
  @ErrorText(text = "Directory Assistance is reloading internal tables due to change in %p.")
  short ERR_DA_RELOADING = IGlobalErr.PKG_NIF + 82;
  @ErrorText(text = "Data too large for a sort buffer.")
  short ERR_NIF_SORT_DATA_TOO_BIG = IGlobalErr.PKG_NIF + 83;
  @ErrorText(text = "Unable to use optimized view rebuild for view '%s' due to insufficient disk space at %s. Need approximately %ld MB for this view. Using standard rebuild instead.")
  short LOG_NIF_SKIP_REBUILD_OPT = IGlobalErr.PKG_NIF + 84;
  @ErrorText(text = "NAMELookups are limited on this server to a size smaller than what would be returned.  See your Domino Administrator for more information.")
  short ERR_NAMELOOKUP_TOO_BIG = IGlobalErr.PKG_NIF + 85;
  @ErrorText(text = "Unable to access view rebuild directory %s. Using default temp directory instead.")
  short LOG_NIF_ERR_REBUILD_DIR = IGlobalErr.PKG_NIF + 86;
  @ErrorText(text = "Unable to wait for rebuild because collection sem is locked.")
  short ERR_LOCK_ON_WAIT_FOR_REBUILD = IGlobalErr.PKG_NIF + 87;
  @ErrorText(text = "Typeahead not supported on Server.")
  short ERR_TYPEAHEAD_NOT_SUPPORTED = IGlobalErr.PKG_NIF + 88;
  @ErrorText(text = "NIFPOOL [headers]")
  short ERR_CMD_SHOW_NIFPOOL = IGlobalErr.PKG_NIF + 89;
  @ErrorText(text = "* Show the contents of the Server's NIF pool.")
  short ERR_HELP_SHOW_NIFPOOL = IGlobalErr.PKG_NIF + 90;
  @ErrorText(text = "Directory Assistance Initializing.")
  short ERR_DA_INITIALIZING = IGlobalErr.PKG_NIF + 91;
  @ErrorText(text = "LDAPPOOL")
  short ERR_CMD_SHOW_LDAPPOOL = IGlobalErr.PKG_NIF + 92;
  @ErrorText(text = "* Show the contents of the Server's LDAP pool.")
  short ERR_HELP_SHOW_LDAPPOOL = IGlobalErr.PKG_NIF + 93;
  @ErrorText(text = "Directory Catalog is Corrupt, Should be rebuilt.")
  short ERR_DIRECTORY_CATALOG_CORRUPT = IGlobalErr.PKG_NIF + 94;
  @ErrorText(text = "Directory Assistance flushed server's group cache!")
  short ERR_DA_FLUSH_GROUPCACHE = IGlobalErr.PKG_NIF + 95;
  @ErrorText(text = "Cross cert is needed for LDAP Server: %s:%d")
  short ERR_LDAP_NEED_XCERT = IGlobalErr.PKG_NIF + 96;
  @ErrorText(text = "LDAP Schema: Started loading...")
  short MSG_LDAP_LOADSCHEMABEGIN = IGlobalErr.PKG_NIF + 97;
  @ErrorText(text = "LDAP Schema: Finished loading")
  short MSG_LDAP_LOADSCHEMAEND = IGlobalErr.PKG_NIF + 98;
  @ErrorText(text = "LDAP Schema: Failed loading")
  short MSG_LDAP_LOADSCHEMAFAIL = IGlobalErr.PKG_NIF + 99;
  @ErrorText(text = "LDAP Schema: Started exporting...")
  short MSG_LDAP_EXPORTSCHEMABEGIN = IGlobalErr.PKG_NIF + 100;
  @ErrorText(text = "LDAP Schema: Finished exporting")
  short MSG_LDAP_EXPORTSCHEMAEND = IGlobalErr.PKG_NIF + 101;
  @ErrorText(text = "LDAP Schema: Failed exporting")
  short MSG_LDAP_EXPORTSCHEMAFAIL = IGlobalErr.PKG_NIF + 102;
  @ErrorText(text = "Directory Assistance unable to decrypt document for domain %s, skipping this domain.")
  short ERR_DA_INVALID_ENCRYPT_KEY = IGlobalErr.PKG_NIF + 103;
  @ErrorText(text = "Directory Assistance has detected unavailable replica, retry NAMELookup")
  short ERR_DA_RETRY_LOOKUP = IGlobalErr.PKG_NIF + 104;
  @ErrorText(text = "LDAP Schema: This is not a server machine!")
  short ERR_LSCHEMA_NOTSERVERMACHINE = IGlobalErr.PKG_NIF + 105;
  @ErrorText(text = "LDAP Schema: Call LSchemaInit() first!")
  short ERR_LSCHEMA_CALLINITFIRST = IGlobalErr.PKG_NIF + 106;
  @ErrorText(text = "LDAP Schema: Failed to create replica of schema database on LDAP server: %s from admin server: %s!")
  short ERR_LSCHEMA_REPLICATESCHEMA = IGlobalErr.PKG_NIF + 107;
  @ErrorText(text = "LDAP Schema: Failed to export schema since duplicate schema entries with OID: %s found!")
  short ERR_LSCHEMA_EXPORTDUPLICATE = IGlobalErr.PKG_NIF + 108;
  @ErrorText(text = "A view rebuild is in progress")
  short ERR_NIF_REBUILD_IN_PROGRESS = IGlobalErr.PKG_NIF + 109;
  @ErrorText(text = "Directory Assistance: Bad filter for customized authentication filter. The system default filter will be used.")
  short ERR_NIF_AUTHENTICATION_FILTER = IGlobalErr.PKG_NIF + 110;
  @ErrorText(text = "Directory Assistance: Bad filter for customized authorization filter. The system default filter will be used.")
  short ERR_NIF_AUTHORIZATION_FILTER = IGlobalErr.PKG_NIF + 111;
  @ErrorText(text = "Directory Assistance: Bad filter for customized mail filter. The system default filter will be used.")
  short ERR_NIF_MAIL_FILTER = IGlobalErr.PKG_NIF + 112;
  @ErrorText(text = "Roaming User Name & Address book is not local. ")
  short ERR_LOOKUP_RNAB_REMOTE = IGlobalErr.PKG_NIF + 113;
  @ErrorText(text = " (LDAP)")
  short STR_DIRECTORY_LDAP = IGlobalErr.PKG_NIF + 114;
  @ErrorText(text = "Configuration")
  short STR_DIRECTORY_CONFIGNAB = IGlobalErr.PKG_NIF + 115;
  @ErrorText(text = "Directory Catalog '%s' in use")
  short STR_DIRECTORY_DIRCAT = IGlobalErr.PKG_NIF + 116;
  @ErrorText(text = "Remote Primary")
  short STR_DIRECTORY_REMOTE_PRIMARY = IGlobalErr.PKG_NIF + 117;
  @ErrorText(text = "Primary")
  short STR_DIRECTORY_PRIMARY = IGlobalErr.PKG_NIF + 118;
  @ErrorText(text = "Secondary")
  short STR_DIRECTORY_SECONDARY = IGlobalErr.PKG_NIF + 119;
  @ErrorText(text = "Directory Assistance is reloading internal tables due to remote server failure.")
  short ERR_DA_RELOADING2 = IGlobalErr.PKG_NIF + 120;
  @ErrorText(text = "LDAP Schema: %s")
  short ERR_LSCHEMA_LDIFFILE_MISSING = IGlobalErr.PKG_NIF + 121;
  @ErrorText(text = "Cache entry exceeded the maximum size.")
  short CACHE_ENTRY_TOO_LARGE = IGlobalErr.PKG_NIF + 122;
  @ErrorText(text = "Cache entry was stale.")
  short ERR_DATA_STALE = IGlobalErr.PKG_NIF + 123;
  @ErrorText(text = "Too little memory available to initialize the cache.")
  short ERR_MEM_PAINFUL = IGlobalErr.PKG_NIF + 124;
  @ErrorText(text = "Unable to complete operation due to a lock hierarchy conflict.")
  short ERR_LOCK_CONFLICT = IGlobalErr.PKG_NIF + 125;
  @ErrorText(text = "Duplicate entry already added to cache.")
  short ERR_DUPLICATE_ENTRY = IGlobalErr.PKG_NIF + 126;
  @ErrorText(text = "Could not lock NLCache2 object block.")
  short ERR_NLCACHE2_MEM_ERROR = IGlobalErr.PKG_NIF + 127;
  @ErrorText(text = "Could not obtain NLCache2 master semaphore.")
  short ERR_NLCACHE2_SEM_ERROR = IGlobalErr.PKG_NIF2 + 0;
  @ErrorText(text = "NLCache2 already active.")
  short ERR_NLCACHE2_ACTIVE = IGlobalErr.PKG_NIF2 + 1;
  @ErrorText(text = "NLCache2 not active.")
  short ERR_NLCACHE2_NOT_ACTIVE = IGlobalErr.PKG_NIF2 + 2;
  @ErrorText(text = "Warning:  Please define a domain in notes.ini file by setting the DOMAIN= variable")
  short ERR_DA_NODOMAIN = IGlobalErr.PKG_NIF2 + 3;
  @ErrorText(text = "LDAP")
  short STR_LDAP_PROTOCOL = IGlobalErr.PKG_NIF2 + 4;
  @ErrorText(text = "Notes")
  short STR_NRPC_PROTOCOL = IGlobalErr.PKG_NIF2 + 5;
  @ErrorText(text = "UNKNOWN")
  short STR_UNKNOWN_PROTOCOL = IGlobalErr.PKG_NIF2 + 6;
  @ErrorText(text = "   DomainName      DirectoryType         ClientProtocol Replica/LDAP Server\n   --------------- --------------------- -------------- -----------------------")
  short STR_SHOW_DIRECTORY_HEADER = IGlobalErr.PKG_NIF2 + 7;
  @ErrorText(text = "View has been deleted: %p (%s)")
  short ERR_NIF_VIEW_DELETED_MSG = IGlobalErr.PKG_NIF2 + 8;
  @ErrorText(text = "View has been deleted")
  short ERR_NIF_VIEW_DELETED = IGlobalErr.PKG_NIF2 + 9;
  @ErrorText(text = "Shared cache parameter mismatch.")
  short ERR_CACHE_INITIALIZER_MISMATCH = IGlobalErr.PKG_NIF2 + 10;
  @ErrorText(text = "LDAP Server %s is NOT available.")
  short ERR_NIF_LDAP_UNAVAILABLE = IGlobalErr.PKG_NIF2 + 11;
  @ErrorText(text = "ItemTable data is out of range")
  short ERR_BAD_ITEMTABLE_DATA = IGlobalErr.PKG_NIF2 + 12;
  @ErrorText(text = "Collection container is NULL")
  short ERR_NULL_COLLECTION_CIDB = IGlobalErr.PKG_NIF2 + 13;
  @ErrorText(text = "View selection or column formula changed")
  short ERR_VIEW_FORMULA_CHANGED = IGlobalErr.PKG_NIF2 + 14;
  @ErrorText(text = "One or more Directory Assistance Domains used to process directory/NAMELookup requests was unavailable.")
  short ERR_NAMELOOKUP_DOMAIN_UNAVAILABLE = IGlobalErr.PKG_NIF2 + 15;
  @ErrorText(text = "View selection or column formula changed: %p (%s)")
  short ERR_VIEW_FORMULA_CHANGED_MSG = IGlobalErr.PKG_NIF2 + 16;
  @ErrorText(text = "Directory Assistance found alternate Directory Cluster replica %p for domain %s")
  short ERR_DA_ALTERNATE_CLUSTER_REPLICA_FOUND = IGlobalErr.PKG_NIF2 + 17;
  @ErrorText(text = "LDAP Server is NOT available.")
  short ERR_NIF_LDAP_REPLICA_UNAVAILABLE = IGlobalErr.PKG_NIF2 + 18;
  @ErrorText(text = "WARNING: Domain %s is your remote primary domain, and no replicas are available.  Your server cannot process users and groups.  Please contact your system administrator.")
  short ERR_DA_NO_REMOTE_PRIMARY = IGlobalErr.PKG_NIF2 + 19;
  @ErrorText(text = "Informational, rebuilding view - removing deleted documents(reading %s %s note Title:'%s')")
  short ERR_NIF_COLLREBUILDDD = IGlobalErr.PKG_NIF2 + 20;
  @ErrorText(text = "LDAP Schema: Failed to create replica of schema database on LDAP server: %s.  Because the administration server of Domino Directory has not yet been set!")
  short ERR_LSCHEMA_ADMINNOTSET = IGlobalErr.PKG_NIF2 + 21;
  @ErrorText(text = "Informational, collection was not updated to avoid blocking.")
  short ERR_NIF_COLLNOTUPDATED = IGlobalErr.PKG_NIF2 + 22;
  @ErrorText(text = "Sort file is invalid.")
  short ERR_BAD_SORT_FILE = IGlobalErr.PKG_NIF2 + 23;
  @ErrorText(text = "Collections cannot be shared between processes.")
  short ERR_COLLECTION_WRONG_PROCESS = IGlobalErr.PKG_NIF2 + 24;
  @ErrorText(text = "Collection is time-varying and has not been opened.")
  short ERR_VIEW_TIME_VARYING = IGlobalErr.PKG_NIF2 + 25;
  @ErrorText(text = "Warning!!  Remote Primary configured in Directory Assistance is a Configuration Directory.  Ignoring this directory.")
  short ERR_DA_REMOTE_IS_CONFIG = IGlobalErr.PKG_NIF2 + 26;
  @ErrorText(text = "Could not access a directory on server %p specified by a database link in Directory Assistance; error:  ")
  short ERR_DA_CANT_ACCESS_DBLINK = IGlobalErr.PKG_NIF2 + 27;
  @ErrorText(text = "Could not access a directory specified by a database link in Directory Assistance: link contains no server hshort ")
  short ERR_DA_DBLINK_NOHshort = IGlobalErr.PKG_NIF2 + 28;
  @ErrorText(text = "Entry exists on thread-specific Name Collection Queue")
  short ERR_EXISTS_ON_PRIVATEQUEUE = IGlobalErr.PKG_NIF2 + 29;
  @ErrorText(text = "Entry returned from thread-specific Name Collection Queue is an LDAP entry")
  short ERR_NCQUEUE_ENTRY_LDAP = IGlobalErr.PKG_NIF2 + 30;
  @ErrorText(text = "Directory Assistance Database '%s' in use")
  short STR_DIRECTORY_MAB = IGlobalErr.PKG_NIF2 + 31;
  @ErrorText(text = "Directory Assistance not enabled")
  short ERR_DA_NOT_ENABLED = IGlobalErr.PKG_NIF2 + 32;
  @ErrorText(text = "Directory Assistance failed opening Primary Domino Directory %s, error: ")
  short ERR_DA_PRIMARYAB_FAILED = IGlobalErr.PKG_NIF2 + 33;
  @ErrorText(text = "Failed registering Directory Assistance termination routine, error: ")
  short ERR_DA_REG_TERMINATION_FAILED = IGlobalErr.PKG_NIF2 + 34;
  @ErrorText(text = "Error opening Directory Assistance Database %s, error: ")
  short ERR_DA_DB_FAILED = IGlobalErr.PKG_NIF2 + 35;
  @ErrorText(text = "Insufficient memory to initialize Directory Assistance")
  short ERR_DA_INSUFFICIENT_MEMORY = IGlobalErr.PKG_NIF2 + 36;
  @ErrorText(text = "WARNING: NAMELookups from this server to the remote Domino Directory %s when Extended ACLs are enabled is prohibited.  Correct by creating a Directory Assistance document for it on %s.")
  short ERR_LOOKUP_DBNAME_NOT_FOUND = IGlobalErr.PKG_NIF2 + 37;
  @ErrorText(text = "Directory Assistance could not access a Directory in Domain %s, View %s can not be opened, error: ")
  short ERR_NAMELOOKUP_VIEW_ERROR = IGlobalErr.PKG_NIF2 + 38;
  @ErrorText(text = "Error searching a directory in Domain %s for %s, error: ")
  short ERR_NAMELOOKUP_DIRECTORY_ERROR = IGlobalErr.PKG_NIF2 + 39;
  @ErrorText(text = "More than one database with the name %s was found.")
  short ERR_MULTIPLE_DB_MATCHES = IGlobalErr.PKG_NIF2 + 40;
  @ErrorText(text = "NIF DB2 KeyBuffer array is full.")
  short ERR_DB2KEYBUFFERS_FULL = IGlobalErr.PKG_NIF2 + 41;
  @ErrorText(text = "NIF DB2 Unassigned NIFData column.")
  short ERR_DB2_COLUMN_UNDEFINED = IGlobalErr.PKG_NIF2 + 42;
  @ErrorText(text = "NIF DB2 Internal column value error.")
  short ERR_DB2_COLUMN_VALUE_ERROR = IGlobalErr.PKG_NIF2 + 43;
  @ErrorText(text = "NIF DB2 Navigation error.")
  short ERR_DB2_NIF_NAVIGATE_ERROR = IGlobalErr.PKG_NIF2 + 44;
  @ErrorText(text = "NIF DB2 Traverse error.")
  short ERR_DB2_NIF_TRAVERSE_ERROR = IGlobalErr.PKG_NIF2 + 45;
  @ErrorText(text = "An update of NAMES open namespaces as been executed")
  short LOG_NIF_NAMES_UPDATE_OPEN_NAMESPACES = IGlobalErr.PKG_NIF2 + 46;
  @ErrorText(text = "The Domino Directory search response on %a is below configured severity thresholds")
  short ERR_DD_SEARCH_RESPONSE_BELOW_THRESHOLD = IGlobalErr.PKG_NIF2 + 48;
  @ErrorText(text = "The Domino Directory search response (%ld ms) on %a has reached its threshold (%ld ms).")
  short ERR_DD_SEARCH_RESPONSE_THRESHOLD = IGlobalErr.PKG_NIF2 + 49;
  @ErrorText(text = "The secondary LDAP search response (%ld ms) on %a has reached its threshold (%ld ms).")
  short ERR_DA_SEARCH_RESPONSE_THRESHOLD = IGlobalErr.PKG_NIF2 + 50;
  @ErrorText(text = "Insufficient memory - (DB2) index pool is full.")
  short ERR_DB2_NIF_POOLFULL = IGlobalErr.PKG_NIF2 + 53;
  @ErrorText(text = "Database collection error: %s")
  short ERR_NIF_COLLECTION_DDM = IGlobalErr.PKG_NIF2 + 54;
  @ErrorText(text = "Database collection open error: %s")
  short ERR_NIF_COLLECTION_OPEN_DDM = IGlobalErr.PKG_NIF2 + 55;
  @ErrorText(text = "The secondary LDAP search response on %a is below configured severity thresholds")
  short ERR_DA_SEARCH_RESPONSE_BELOW_THRESHOLD = IGlobalErr.PKG_NIF2 + 56;
  @ErrorText(text = "No issues found checking the directory aggregation schedule on %a")
  short ERR_DDM_DIR_AGG_SCHED_OK = IGlobalErr.PKG_NIF2 + 57;
  @ErrorText(text = "Directory assistance is notifying of a MAB reload")
  short LOG_DDM_DA_TABLE_RELOAD = IGlobalErr.PKG_NIF2 + 58;
  @ErrorText(text = "Condensed directory catalogs are not supported in the Directory Assistance database")
  short ERR_DA_NOT_LIGHTWEIGHT_NAB = IGlobalErr.PKG_NIF2 + 59;
  @ErrorText(text = "Collation number is invalid")
  short ERR_DB2_NIF_INVALID_COLLATION = IGlobalErr.PKG_NIF2 + 60;
  @ErrorText(text = "Unable to acquire a NAMELookup thread")
  short ERR_NAMELOOKUP_CANNOT_ACQUIRE_THREAD = IGlobalErr.PKG_NIF2 + 61;
  @ErrorText(text = "Full summary buffer is not valid, entry will not be cached.")
  short ERR_FULLBUFFER_BAD = IGlobalErr.PKG_NIF2 + 62;
  @ErrorText(text = "Full summary buffer has grown too large, entry will not be cached")
  short ERR_FULLBUFFER_TOO_BIG = IGlobalErr.PKG_NIF2 + 63;
  @ErrorText(text = "The id file cannot be located in the specified path.")
  short ERR_ID_PATH_NOT_FOUND = IGlobalErr.PKG_NIF2 + 64;
  @ErrorText(text = "Directory %p for '%s' in use")
  short LOG_NIF_DIRECTORY_SERVING = IGlobalErr.PKG_NIF2 + 65;
  @ErrorText(text = "Past end of DB2 ReadEntries cache")
  short ERR_DB2_NIF_ENDOFCACHE = IGlobalErr.PKG_NIF2 + 66;
  @ErrorText(text = "View is currently marked exclusive.")
  short ERR_COLLECTION_IS_EXCLUSIVE = IGlobalErr.PKG_NIF2 + 67;
  @ErrorText(text = "Subtotals computation failed during update: ")
  short ERR_DB2_NIF_COMP_SUBTOTALS = IGlobalErr.PKG_NIF2 + 68;
  @ErrorText(text = "Recursing in DB2OpenCollection")
  short ERR_DB2_NIF_DB2OPEN_RECURSE = IGlobalErr.PKG_NIF2 + 69;
  @ErrorText(text = "DB2NIF Transaction being rolled back")
  short ERR_DB2_NIF_ROLLBACK = IGlobalErr.PKG_NIF2 + 70;
  @ErrorText(text = "DB2NIF OpenCollection initialization error")
  short ERR_DB2_NIF_OPEN_INIT = IGlobalErr.PKG_NIF2 + 71;
  @ErrorText(text = "Informational, rebuild view needed - missing atom table (reading %s %s note Title:'%s')")
  short ERR_REBUILD_MISSING_ATOM = IGlobalErr.PKG_NIF2 + 72;
  @ErrorText(text = "Directory Assistance is updating server info in internal tables (Server records in primary directory have changed)")
  short ERR_DA_UPDATE_SRVLISTINFO = IGlobalErr.PKG_NIF2 + 73;
  @ErrorText(text = "DB2 NIF Collection - negative level 0 count")
  short ERR_DB2_NIF_BAD_LEVEL0CT = IGlobalErr.PKG_NIF2 + 74;
  @ErrorText(text = "gbiCache container failed consistency check")
  short ERR_GBCACHE_CONTAINER_NOT_VALID = IGlobalErr.PKG_NIF2 + 75;
  @ErrorText(text = "gbiCache container not initialized")
  short ERR_NODE_NOT_INITIALIZED = IGlobalErr.PKG_NIF2 + 76;
  @ErrorText(text = "gbiCache container failed serialization test")
  short ERR_NODE_NOT_LOCKED = IGlobalErr.PKG_NIF2 + 77;
  @ErrorText(text = "nliCache processed an out of sequence update")
  short ERR_UPDATE_OUT_OF_SEQUENCE = IGlobalErr.PKG_NIF2 + 78;
  @ErrorText(text = "nliCache not enough memory available in container")
  short ERR_CONTAINER_TOO_SMALL = IGlobalErr.PKG_NIF2 + 79;
  @ErrorText(text = "nliCache no longer able to update this container")
  short ERR_CONECTION_FAILED = IGlobalErr.PKG_NIF2 + 80;
  @ErrorText(text = "Unknown function requested from the NAMELookup Cache")
  short ERR_NLCACHE_FUNCTION_UNKNOWN = IGlobalErr.PKG_NIF2 + 81;
  @ErrorText(text = "View Registration Corrupt")
  short NIF_VIEW_REG_CORRUPT = IGlobalErr.PKG_NIF2 + 82;
  @ErrorText(text = "Exceeded maximum number of updates (%d) for db: %s, view: %s")
  short NIF_VIEW_REG_OVERFLOW = IGlobalErr.PKG_NIF2 + 83;
  @ErrorText(text = "Failed update on LDAP Server %s, DN: %s, Reason: %s")
  short ERR_NIF_LDAP_UPDATE = IGlobalErr.PKG_NIF2 + 84;
  @ErrorText(text = "Skipping encrypting DA configuration for %s")
  short ERR_NIF_DA_CRED_ENCRYPT = IGlobalErr.PKG_NIF2 + 86;

}
