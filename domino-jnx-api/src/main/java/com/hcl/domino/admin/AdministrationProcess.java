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
package com.hcl.domino.admin;

import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Optional;

import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.misc.DominoClientDescendant;

/**
 * Represents the administration process
 */
public interface AdministrationProcess extends DominoClientDescendant {
  int MAILFILE_DELETE_NONE = 0;
  int MAILFILE_DELETE_HOME = 1;
  int MAILFILE_DELETE_ALL = 2;

  int PWD_CHK_DONTCHECKPASSWORD = 0;
  int PWD_CHK_CHECKPASSWORD = 1;
  int PWD_CHK_LOCKOUT = 2;

  int IDVAULT_MARK_AS_INACTIVE = 0;
  int IDVAULT_DELETE = 1;
  int IDVAULT_RETAIN = 2;

  /**
   * Enters a request in the Administration Requests database to add members to a
   * new or existing a group.
   *
   * @param group   The name of the group
   * @param members The name of the group
   * @return The note ID of an entry created in the Administration Requests
   *         database
   */
  Optional<Integer> addGroupMembers(String group, Collection<String> members);

  /**
   * Enters a request in the Administration Requests database to add a server to a
   * cluster.
   *
   * @param server  The full hierarchical name (can be abbreviated) of the server.
   * @param cluster The name of the cluster.
   * @return The note ID of an entry created in the Administration Requests
   *         database.
   */
  int addServerToCluster(String server, String cluster);

  /**
   * Enters a request in the Administration Requests database to change a user's
   * Internet password.
   *
   * @param username    The full hierarchical name (can be abbreviated) of the
   *                    user.
   * @param oldpassword The existing password
   * @param newpassword The new password
   * @return note id The note ID of an entry created in the Administration
   *         Requests database
   */
  Optional<Integer> changeHTTPPassword(String username, String oldpassword, String newpassword);

  String getCertificateAuthorityOrg();

  DominoDateTime getCertificateExpiration();

  String getCertifierFile();

  String getCertifierPassword();

  String getServer();

  boolean isCertificateAuthorityAvailable();

  boolean isUseCertificateAuthority();

  /**
   * Enters a request in the Administration Requests database to remove a server
   * from a cluster.
   *
   * @param server The name of the server.
   * @return The note ID of an entry created in the Administration Requests
   *         database.
   */
  int removeServerFromCluster(String server);

  /**
   * Enters a request in the Administration Requests database to rename a group.
   *
   * @param group    The name of the group.
   * @param newgroup The new name of the group.
   * @return The note ID of an entry created in the Administration Requests
   *         database.
   */
  int renameGroup(String group, String newgroup);

  void setCertificateAuthorityOrg(String caa);

  void setCertificateExpiration(Temporal ce);

  void setCertifierFile(String cf);

  void setCertifierPassword(String cf);

  void setUseCertificateAuthority(boolean uca);

  // to be done, methods from Notes.jar:

  // String addInternetCertificateToUser(
  // String user,
  // String keyringfile, String keyringpassword);
  // String addInternetCertificateToUser(
  // String user,
  // String keyringfile, String keyringpassword, Temporal expiration);
  //
  //
  // String approveDeletePersonInDirectory( String noteid);
  // String approveDeleteServerInDirectory( String noteid);
  // String approveDesignElementDeletion( String noteid);
  // String approveMailFileDeletion( String noteid);
  // String approveMovedReplicaDeletion( String noteid);
  // String approveNameChangeRetraction( String noteid);
  // String approveRenamePersonInDirectory( String noteid);
  // String approveRenamePersonCommonNameInDirectory(String noteid);
  // String approveRenameServerInDirectory( String noteid);
  // String approveReplicaDeletion( String noteid);
  // String approveResourceDeletion( String noteid);
  // String approveHostedOrgStorageDeletion( String noteid);
  //
  //
  // String configureMailAgent(String username, String agentname);
  // String configureMailAgent(
  // String username, String agentname, boolean activatable, boolean enable);
  //
  // String createReplica(String sourceserver, String sourcedbfile, String
  // destserver);
  // String createReplica(
  // String sourceserver, String sourcedbfile,
  // String destserver, String destdbfile,
  // boolean copyacl, boolean createftindex);
  //
  // String deleteGroup(String groupname, boolean immediate);
  // String deleteGroup(
  // String groupname, boolean immediate, boolean deletewindowsgroup);
  //
  // String deleteReplicas(String server, String dbfile);
  //
  // String deleteServer(String servername , boolean immediate);
  //
  // String deleteUser(
  // String username, boolean immediate,
  // int mailfileaction, String denygroup);
  // String deleteUser(
  // String username, boolean immediate,
  // int mailfileaction, String denygroup, boolean deletewindowsuser);
  //
  // String deleteUser(
  // String username, boolean immediate,
  // int mailfileaction, String denygroup, boolean deletewindowsuser, int
  // idvaultaction);
  //
  //
  // String findGroupInDomain(String group);
  // String findServerInDomain(String server);
  // String findUserInDomain(String user);
  //
  // String moveMailUser(
  // String username, String newhomeserver, String newhomeservermailpath);
  // String moveMailUser(
  // String username, String newhomeserver, String newhomeservermailpath,
  // boolean usescos, Collection<String> newclusterreplicaarray,
  // boolean deleteoldclusterreplicas);
  //
  // String moveReplica( String sourceserver, String sourcedbfile, String
  // destserver);
  // String moveReplica(
  // String sourceserver, String sourcedbfile,
  // String destserver, String destdbfile,
  // boolean copyacl, boolean createftindex);
  //
  // String moveRoamingUser(String username, String destserver, String
  // destserverpath);
  //
  // String moveUserInHierarchyComplete(String requestnoteid);
  // String moveUserInHierarchyComplete(
  // String requestnoteid,
  // String lastname, String firstname, String middleinitial,
  // String orgunit,
  // String altcommonname, String altorgunit, String altlanguage,
  // boolean renamewindowsuser);
  //
  // String moveUserInHierarchyRequest(String username, String targetcertifier);
  // String moveUserInHierarchyRequest(
  // String username, String targetcertifier, boolean allowprimarynamechange);
  //
  // String recertifyServer(String server);
  //
  // String recertifyUser(String username);
  //
  //
  //
  // String renameNotesUser(
  // String username,
  // String lastname, String firstname, String middleinitial,
  // String orgunit);
  // String renameNotesUser(
  // String username,
  // String lastname, String firstname, String middleinitial,
  // String orgunit,
  // String altcommonname, String altorgunit, String altlanguage,
  // boolean renamewindowsuser);
  //
  // String renameWebUser(
  // String username, String newusername,
  // String newlastname, String newfirstname, String newmiddleinitial,
  // String newshortname, String newinternetaddress);
  //
  // String setServerDirectoryAssistanceSettings(String server, String dbfile);
  //
  // String setUserPasswordSettings(
  // String username,
  // java.lang.Integer notespasswordchecksetting,
  // java.lang.Integer notespasswordchangeinterval,
  // java.lang.Integer notespasswordgraceperiod,
  // java.lang.Boolean internetpasswordforcechange);
  //
  // String signDatabaseWithServerID( String server, String dbfile);
  // String signDatabaseWithServerID( String server, String dbfile, boolean
  // updateonly);
  //
  // String upgradeUserToHierarchical( String username);
  // String upgradeUserToHierarchical(
  // String username,
  // String orgunit,
  // String altcommonname, String altorgunit, String altlanguage);
  //
  // String setEnableOutlookSupport(String server, boolean enable);
  // String delegateMailFile(String mailfileowner, Collection<String>
  // publicreaders, Collection<String> publicwriters,
  // Collection<String> otherreaders, Collection<String> otherwriters,
  // Collection<String> othereditors,
  // Collection<String> otherdeletors,
  // Collection<String> removefromacl,
  // String mailfilename, String mailserver);

}
