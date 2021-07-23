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

import java.util.List;
import java.util.Set;

/**
 * Computed {@link Acl} access level of a user in a database
 */
public interface AclAccess {
	
	/**
	 * Returns the roles
	 * 
	 * @return roles or empty list
	 */
	List<String> getRoles();
	
	/**
	 * Returns the access level
	 * 
	 * @return access level
	 */
	AclLevel getAclLevel();
	
	/**
	 * Returns the access flags
	 * 
	 * @return flags
	 */
	Set<AclFlag> getAclFlags();

}
