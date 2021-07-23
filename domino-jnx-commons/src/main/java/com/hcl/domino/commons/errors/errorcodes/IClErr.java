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
package com.hcl.domino.commons.errors.errorcodes;

import com.hcl.domino.commons.errors.ErrorText;

public interface IClErr extends IGlobalErr {

  @ErrorText(text = "Cannot rename a file to a new server")
  short ERR_RENAME = IGlobalErr.PKG_CLIENT + 1;
  @ErrorText(text = "Server does not support this version of the network protocol")
  short ERR_VERSION = IGlobalErr.PKG_CLIENT + 2;
  @ErrorText(text = "Unable to find path to server. Check that your network connection is working.")
  short ERR_SERVER_NOT_FOUND = IGlobalErr.PKG_CLIENT + 3;
  @ErrorText(text = "Connection denied. The server you connected to has a different name from the one requested.")
  short ERR_SERVER_NAME_CHANGED = IGlobalErr.PKG_CLIENT + 4;
  @ErrorText(text = "This function is not implemented on this version of the server")
  short ERR_NOT_IMPLEMENTED = IGlobalErr.PKG_CLIENT + 5;
  @ErrorText(text = "A database handle to a remote database cannot be used by more than one thread.")
  short ERR_MULTI_THREAD = IGlobalErr.PKG_CLIENT + 6;
  @ErrorText(text = "The server is not responding. The server may be down or you may be experiencing network problems. Contact your system administrator if this problem persists.")
  short ERR_SERVER_NOT_RESPONDING = IGlobalErr.PKG_CLIENT + 7;
  @ErrorText(text = "(Retry NoteUpdate)")
  short ERR_RETRY_NOTE_UPDATE = IGlobalErr.PKG_CLIENT + 8;
  @ErrorText(text = "(internal status; retry the transaction)")
  short ERR_RETRY_TRANSACTION = IGlobalErr.PKG_CLIENT + 9;
  @ErrorText(text = "Network protocol error: message from server is too small")
  short ERR_MSG_TOO_SMALL = IGlobalErr.PKG_CLIENT + 10;
  @ErrorText(text = "Network protocol error: message from server cannot be deciphered")
  short ERR_MSG_FROM_SERVER = IGlobalErr.PKG_CLIENT + 11;
  @ErrorText(text = "Server ")
  short ERR_SERVER_NOT_RESPONDING1 = IGlobalErr.PKG_CLIENT + 12;
  @ErrorText(text = " is not responding")
  short ERR_SERVER_NOT_RESPONDING2 = IGlobalErr.PKG_CLIENT + 13;
  @ErrorText(text = "Remote system's identity is either unknown or fraudulent")
  short ERR_NOT_AUTHENTIC = IGlobalErr.PKG_CLIENT + 14;
  @ErrorText(text = "Session to server lost while database replication or copy in progress")
  short ERR_SCAN_LOCK_LOST = IGlobalErr.PKG_CLIENT + 15;
  @ErrorText(text = "In order to do multiple transactions simultaneously, you cannot use the same DB handle.")
  short ERR_MULTI_TRANS = IGlobalErr.PKG_CLIENT + 16;
  @ErrorText(text = "Name server is not responding")
  short ERR_SERVER_NOT_RESPONDING3 = IGlobalErr.PKG_CLIENT + 17;
  @ErrorText(text = "You are not authorized to use the remote console on this server")
  short ERR_CL_NO_REMOTE_CONSOLE_ACCESS = IGlobalErr.PKG_CLIENT + 18;
  @ErrorText(text = "No response from server for this command")
  short ERR_CL_NO_RESPONSE = IGlobalErr.PKG_CLIENT + 19;
  @ErrorText(text = "Session Closed")
  short ERR_TERMINATE = IGlobalErr.PKG_CLIENT + 20;
  @ErrorText(text = "Server is not responding to remote console commands (try again later)")
  short ERR_CL_NOT_RESPONDING = IGlobalErr.PKG_CLIENT + 21;
  @ErrorText(text = "(Out of received data, need more)")
  short ERR_NEED_RECEIVE_DATA = IGlobalErr.PKG_CLIENT + 22;
  @ErrorText(text = "Multi-Segment ID table length from server is not the length expected")
  short ERR_SRV_LENGTH_MISMATCH = IGlobalErr.PKG_CLIENT + 23;
  @ErrorText(text = "Unable to failover replica ID (%h) from server %A to any other cluster member")
  short ERR_LOG_NOFAILOVER = IGlobalErr.PKG_CLIENT + 24;
  @ErrorText(text = "Looking for replica on server %A")
  short ERR_LOOKFOR_REPLICA = IGlobalErr.PKG_CLIENT + 25;
  @ErrorText(text = "Lookup on server %a")
  short ERR_LOOKUP = IGlobalErr.PKG_CLIENT + 26;
  @ErrorText(text = "Failover on replica ID (%h) from server %A to %A")
  short ERR_FOUND_REPLICA = IGlobalErr.PKG_CLIENT + 27;
  @ErrorText(text = "Looking for replica (%h) on server %A")
  short ERR_LOOKFOR_REPLICA_EXT = IGlobalErr.PKG_CLIENT + 28;
  @ErrorText(text = "(internal status; retry the transaction after dropping the session to the server)")
  short ERR_RETRY_WITH_NEW_SESSION = IGlobalErr.PKG_CLIENT + 29;
  @ErrorText(text = "No cluster mates found")
  short ERR_CLUSTER_NOMATES = IGlobalErr.PKG_CLIENT + 30;
  @ErrorText(text = "The server doesn't support schedule free time lookups.")
  short ERR_NO_CS_SUPPORT = IGlobalErr.PKG_CLIENT + 31;
  @ErrorText(text = "Connection denied. You requested server %A but connected to %A.")
  short ERR_REACHED_WRONG_SERVER = IGlobalErr.PKG_CLIENT + 32;
  @ErrorText(text = "Bad Summary buffer received from server.")
  short ERR_BAD_SUMMARY_BUFFER = IGlobalErr.PKG_CLIENT + 33;
  @ErrorText(text = "Expanded length of data bigger than MAXWORD")
  short ERR_EXPANDED_LENGTH = IGlobalErr.PKG_CLIENT + 34;
  @ErrorText(text = "Unable to find/create Notes Server Object in NDS")
  short ERR_CREATE_SERVER_OBJECT = IGlobalErr.PKG_CLIENT + 35;
  @ErrorText(text = "Port information has changed. Please retry")
  short ERR_PORT_INFO_CHANGED = IGlobalErr.PKG_CLIENT + 36;
  @ErrorText(text = "You cannot administer Enterprise Directories on a pre-R5 server")
  short ERR_NO_EDS = IGlobalErr.PKG_CLIENT + 37;
  @ErrorText(text = "You cannot do a remote queue put to a pre-R5 server")
  short ERR_NO_REMOTE_QUEUE_PUT = IGlobalErr.PKG_CLIENT + 38;
  @ErrorText(text = "Accelerated replica creation cannot be used with an encrypted database")
  short ERR_NO_ACCEL_ON_ENCRYPTED_DB = IGlobalErr.PKG_CLIENT + 39;
  @ErrorText(text = "Network protocol error: message from server is too large")
  short ERR_MSG_TO_LARGE = IGlobalErr.PKG_CLIENT + 40;
  @ErrorText(text = "LookupExtended on server %a")
  short ERR_LOOKUP_EXTENDED = IGlobalErr.PKG_CLIENT + 41;
  @ErrorText(text = "Extended access controls are enabled on this database.  You must modify the database on a version 6 or later Domino server.")
  short ERR_XACL_NOUPDATE = IGlobalErr.PKG_CLIENT + 42;
  @ErrorText(text = "Upgrade to Roaming User error")
  short ERR_ROAMING_UPGRADE = IGlobalErr.PKG_CLIENT + 43;
  @ErrorText(text = "Cannot close a database within an NSFSearchStart - NSFSearchStop loop that was opened outside of the loop")
  short ERR_NSFSEARCHSTART_LOOP = IGlobalErr.PKG_CLIENT + 44;
  @ErrorText(text = "Design or Data Locking is only supported on a server running Domino 6 or later")
  short ERR_NOTE_LOCK_NOT_SUPPORTED = IGlobalErr.PKG_CLIENT + 45;
  @ErrorText(text = "You can not get DB2 information from a pre-R7 server")
  short ERR_NO_UDF_CONN_REC = IGlobalErr.PKG_CLIENT + 47;

}
