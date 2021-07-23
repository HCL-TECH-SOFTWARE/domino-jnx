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
package com.hcl.domino.naming;

import com.hcl.domino.DominoClient;
import com.hcl.domino.Name;
import com.hcl.domino.UserNamesList;
import com.hcl.domino.misc.JNXServiceFinder;

/**
 * Utility interface for performing common naming operations with the active API
 * implementation.
 */
public interface Names {
	
	Name _createName(String name);
	String _toAbbreviated(String name);
	String _toCommon(String name);
	String _toCanonical(String name);
	boolean _equalNames(String name1, String name2);
	UserNamesList _buildNamesList(DominoClient client, String name);

	static Name createName(String name) {
		Names names = JNXServiceFinder.findRequiredService(Names.class, Names.class.getClassLoader());
		
		return names._createName(name);
	}
	
	
	static String toAbbreviated(String name) {
		Names names = JNXServiceFinder.findRequiredService(Names.class, Names.class.getClassLoader());

		return names._toAbbreviated(name);
	}


	static String toCommon(String name) {
		Names names = JNXServiceFinder.findRequiredService(Names.class, Names.class.getClassLoader());

		return names._toCommon(name);
	}
	
	static String toCanonical(String name) {
		Names names = JNXServiceFinder.findRequiredService(Names.class, Names.class.getClassLoader());

		return names._toCanonical(name);
	}

	/**
	 * Checks two Domino usernames for equality
	 * 
	 * @param name1 first name
	 * @param name2 second name
	 * @return true if abbreviated names of both are equal, ignoring case
	 */
	static boolean equalNames(String name1, String name2) {
		Names names = JNXServiceFinder.findRequiredService(Names.class, Names.class.getClassLoader());

		return names._equalNames(name1, name2);
	}
	
	/**
	 * Generates a names list for the provided name based on the current environment.
	 * 
	 * @param client a contextual client to use for the lookup
	 * @param name the name to look up
	 * @return a {@link UserNamesList} object for the provided name
	 * @since 1.0.28
	 */
	static UserNamesList buildNamesList(DominoClient client, String name) {
		Names names = JNXServiceFinder.findRequiredService(Names.class, Names.class.getClassLoader());
		
		return names._buildNamesList(client, name);
	}
}
