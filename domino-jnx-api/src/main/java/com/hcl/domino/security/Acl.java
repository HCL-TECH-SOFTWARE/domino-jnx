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
package com.hcl.domino.security;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.hcl.domino.DominoException;
import com.hcl.domino.UserNamesList;

/**
 * Access Control List of a database
 * including functions to query access
 * 
 * @author t.b.d
 *
 */
public interface Acl {
	/**
	 * Looks up the access level for a user and his groups
	 * 
	 * @param userName username, either canonical or abbreviated
	 * @return acl access info, with access level, flags and roles
	 */
	AclAccess lookupAccess(String userName);
	
	/**
	 * Looks up the access level for a {@link UserNamesList}
	 * 
	 * @param namesList names list for a user
	 * @return acl access info, with access level, flags and roles
	 */
	AclAccess lookupAccess(UserNamesList namesList);
	
	/**
	 * Returns all roles declared in the ACL
	 * 
	 * @return roles
	 */
	List<String> getRoles();
	
	/**
	 * Returns all ACL entries
	 * 
	 * @return ACL entries hashed by their username in the order they got returned from the C API
	 */
	List<AclEntry> getEntries();
	
	/**
	 * Convenience method that call {@link #getEntries()} and returns a single
	 * value for the specified name
	 * 
	 * @param name name
	 * @return an {@link Optional} describing the ACL entry, or an empty one if not found
	 */
	Optional<AclEntry> getEntry(String name);
	
	/**
	 * This function adds an entry to an access control list.
	 * 
	 * @param name user or group to be added, either in abbreviated or canonical format
	 * @param accessLevel Access level ({@link AclLevel}), of the entry to be added
	 * @param roles roles to be set for this user
	 * @param accessFlags Access level modifier flags ({@link AclFlag}), e.g.: unable to delete documents, unable to create documents, of the entry to be added
	 */
	void addEntry(String name, AclLevel accessLevel, List<String> roles, Collection<AclFlag> accessFlags);
	
	/**
	 * This function deletes an entry from an access control list.
	 * 
	 * @param name user or group to be deleted, in abbreviated or canonical format
	 */
	void removeEntry(String name);
	
	/**
	 * This function updates an entry in an access control list.<br>
	 * <br>
	 * Unless the user's name is specified to be modified, the information that is not specified to be
	 * modified remains intact.<br>
	 * <br>
	 * If the user's name is specified to be modified, the user entry is deleted and a new entry is created.<br>
	 * Unless the other access control information is specified to be modified as well, the other access control
	 * information will be cleared and the user will have No Access to the database.
	 * 
	 * @param name name of the entry to change
	 * @param newName optional new entry name or null
	 * @param newAccessLevel optional new entry access level or null
	 * @param newRoles optional new entry roles or null
	 * @param newFlags optional new acl flags or null
	 */
	void updateEntry(String name, String newName, AclLevel newAccessLevel, List<String> newRoles, Collection<AclFlag> newFlags);
	
	/**
	 * Adds a role to the ACL. If the role is already in the ACL, the method does nothing.
	 * 
	 * @param role role to add
	 */
	void addRole(String role);
	
	/**
	 * Removes a role from the ACL
	 * 
	 * @param role role to remove
	 */
	void removeRole(String role);
	
	/**
	 * Changes the name of a role
	 * 
	 * @param oldName old role name, either enclosed with [] or not
	 * @param newName new role name, either enclosed with [] or not
	 * @throws DominoException if role could not be found
	 */
	void renameRole(String oldName, String newName);
	
	
	/**
	 * Change the name of the administration server for the access control list.
	 * 
	 * @param server server, either in abbreviated or canonical format
	 */
	void setAdminServer(String server);
	
	/**
	 * Reads the name of the administration server for the access control list.
	 * 
	 * @return the server-name
	 */
	String getAdminServer();

	/**
	 * Check if "Enforce consistent ACL" is set
	 * 
	 * @return true if set
	 */
	boolean isUniformAccess();
	
	/**
	 * Changes the value for "Enforce consistent ACL"
	 * 
	 * @param uniformAccess true to set "Enforce consistent ACL" flag
	 */
	void setUniformAccess(boolean uniformAccess);
	
	/**
	 * This function persists the access control list in the parent database.
	 */
	void save();
	
}
