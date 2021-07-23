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

import com.hcl.domino.misc.INumberEnum;

/**
 * Access Control Level symbols used to qualify user or server access to a given Domino database.
 * 
 * Note: Currently the values associated with each enum are the same values used by the Notes client. If these
 * values need to be changed, a mapping has to be introduced in the JNA implementation connecting with the Notes C-API.
 * 
 * @author Karsten Lehmann
 */
public enum AclLevel implements INumberEnum<Integer> {
	
	/** User or Server has no access to the database. */
	NOACCESS(0),
	
	/** User or Server can add new data documents to a database, but cannot examine the new document or the database. */
	DEPOSITOR(1),
	
	/** User or Server can only view data documents in the database. */
	READER(2),
	
	/** User or Server can create and/or edit their own data documents and examine existing ones in the database. */
	AUTHOR(3),
	
	/** User or Server can create and/or edit any data document. */
	EDITOR(4),
	
	/** User or Server can create and/or edit any data document and/or design document. */
	DESIGNER(5),
	
	/** User or Server can create and/or maintain any type of database or document, including the ACL. */
	MANAGER(6);

	private int m_val;
	
	AclLevel(int val) {
		m_val = val;
	}
	
	/**
	 * Returns the numeric constant for the access level
	 * 
	 * @return constant
	 */
	@Override
	public Integer getValue() {
		return m_val;
	}
	
	@Override
	public long getLongValue() {
		return m_val;
	}
}
