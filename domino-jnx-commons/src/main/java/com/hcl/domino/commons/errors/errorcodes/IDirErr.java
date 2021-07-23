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

public interface IDirErr extends IGlobalErr {

  @ErrorText(text = "This Directory Independent API function has not yet been implemented")
  short ERR_DIR_NOT_YET_IMPLEMENTED = IGlobalErr.PKG_DIR + 1;
  @ErrorText(text = "NULL Directory Entry Structure")
  short ERR_DIR_NULL_ENTRY = IGlobalErr.PKG_DIR + 2;
  @ErrorText(text = "NULL Directory Context Structure")
  short ERR_DIR_NULL_CTX = IGlobalErr.PKG_DIR + 3;
  @ErrorText(text = "NULL Directory Argument")
  short ERR_DIR_NULL_ARGUMENT = IGlobalErr.PKG_DIR + 4;
  @ErrorText(text = "Administration Server for domain could not be determined")
  short ERR_DIR_ADMINSERVER_NOT_FOUND = IGlobalErr.PKG_DIR + 5;
  @ErrorText(text = "The requested directory information could not be found")
  short ERR_DIR_GENERIC_NOT_FOUND = IGlobalErr.PKG_DIR + 6;
  @ErrorText(text = "Entry already exists in the directory provider")
  short ERR_DIR_ENTRY_ALREADY_EXISTS = IGlobalErr.PKG_DIR + 7;
  @ErrorText(text = "Entry provided is invalid")
  short ERR_DIR_ENTRY_INVALID = IGlobalErr.PKG_DIR + 8;
  @ErrorText(text = "This Dir API function is not implemented by Service Provider")
  short ERR_DIR_SPI_NOT_IMPLEMENTED = IGlobalErr.PKG_DIR + 9;
  @ErrorText(text = "This DirEntryID has invalid syntax")
  short ERR_DIR_INVALID_DIRENTRYID = IGlobalErr.PKG_DIR + 10;
  @ErrorText(text = "This operation is not valid for the Directory API")
  short ERR_DIR_ILLEGAL_OPERATION = IGlobalErr.PKG_DIR + 11;
  @ErrorText(text = "This Domino server is not capable of servicing the directory operation")
  short ERR_DIR_SERVER_NOT_CAPABLE = IGlobalErr.PKG_DIR + 12;
  @ErrorText(text = "Invalid Dir API argument")
  short ERR_DIR_INVALID_ARGUMENT = IGlobalErr.PKG_DIR + 13;
  @ErrorText(text = "Error updating LDAP directory")
  short ERR_DIR_LDAP_UPDATE = IGlobalErr.PKG_DIR + 14;
  @ErrorText(text = "Error updating LDAP directory, no such attribute")
  short ERR_DIR_LDAP_UPDATE_NO_SUCH_ATTRIBUTE = IGlobalErr.PKG_DIR + 15;
  @ErrorText(text = "Use of the DIR_PRIMARY_ONLY flag is only valid when combined with an empty DirCtx search domain")
  short ERR_DIR_DIRFLAG_MISMATCH = IGlobalErr.PKG_DIR + 16;
  @ErrorText(text = "Bad search filter")
  short ERR_DIR_INVALID_FILTER = IGlobalErr.PKG_DIR + 17;
  @ErrorText(text = "Directory server does not have this type of entry information.")
  short ERR_DIR_NO_SUCH_ENTRY_INFO_TYPE = IGlobalErr.PKG_DIR + 18;
  @ErrorText(text = "NULL Directory Cursor Structure")
  short ERR_DIR_NULL_CURSOR = IGlobalErr.PKG_DIR + 19;
  @ErrorText(text = "Error reading external LDAP server's attributeType descriptions")
  short ERR_DIR_SCHEMA_ATTRIBUTETYPEDESCRIPTIONS_INIT = IGlobalErr.PKG_DIR + 20;
  @ErrorText(text = "Error reading external LDAP server's objectClass descriptions")
  short ERR_DIR_SCHEMA_OBJECTCLASSDESCRIPTIONS_INIT = IGlobalErr.PKG_DIR + 21;
  @ErrorText(text = "Directory entries in the domain may not be created, they may only be promoted")
  short ERR_DIR_MUST_PROMOTE = IGlobalErr.PKG_DIR + 22;
  @ErrorText(text = "LDAP Bind request failed due to invalid credentials: Please verify credentials specified in DA configuration.")
  short ERR_DIR_INVALID_CREDENTIALS = IGlobalErr.PKG_DIR + 23;
  @ErrorText(text = "Incompatible Notes item data type.")
  short ERR_DIR_UNSUPPORTED_DATATYPE = IGlobalErr.PKG_DIR + 24;
  @ErrorText(text = "Error updating LDAP directory, no such object")
  short ERR_DIR_LDAP_UPDATE_NO_SUCH_OBJECT = IGlobalErr.PKG_DIR + 25;
  @ErrorText(text = "Error updating LDAP entry. Non Domino attributes cannot be modified.")
  short ERR_DIR_LDAP_UPDATE_NON_DOMINO_ATTRIBUTE = IGlobalErr.PKG_DIR + 26;
  @ErrorText(text = "The directory entry could not be found.")
  short ERR_DIR_ENTRY_NOT_FOUND = IGlobalErr.PKG_DIR + 27;
  @ErrorText(text = "The number of search entries found exceeded the size limit.")
  short ERR_DIR_SIZELIMIT_EXCEEDED = IGlobalErr.PKG_DIR + 28;
  @ErrorText(text = "The length of the directory search exceeded the time limit.")
  short ERR_DIR_TIMELIMIT_EXCEEDED = IGlobalErr.PKG_DIR + 29;
  @ErrorText(text = "No LDAP attribute mapped from Domino attribute.")
  short ERR_DIR_ATTRIBUTE_NOT_MAPPED = IGlobalErr.PKG_DIR + 30;
  @ErrorText(text = "More than one directory entry match was found.")
  short ERR_DIR_MULTIPLE_MATCHES = IGlobalErr.PKG_DIR + 36;
  @ErrorText(text = "Invalid Entity ID.")
  short ERR_DIR_INVALID_BHENTITYID = IGlobalErr.PKG_DIR + 37;
  @ErrorText(text = "The current LDAP server is not the same as the previously connected LDAP server.  Trigging a DA Failover.")
  short ERR_DIR_SILENTFAILOVER = IGlobalErr.PKG_DIR + 38;
  @ErrorText(text = "TDS is in Configuration Only Mode!  Make sure the Linux password for the TDS dsrdbm01 instance has not expired!")
  short ERR_DIR_CONFIGURATIONMODE = IGlobalErr.PKG_DIR + 39;

}
