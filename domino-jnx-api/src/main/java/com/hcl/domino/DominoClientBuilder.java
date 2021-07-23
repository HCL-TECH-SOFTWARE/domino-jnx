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
package com.hcl.domino;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import com.hcl.domino.misc.JNXServiceFinder;

/**
 * Builder for creating and configuring a DominoClient
 * 
 * @author Karsten Lehmann
 * @author Paul Withers
 * @since 0.5.0
*/
public abstract class DominoClientBuilder {
	private Path m_idFilePath;
	private String m_idPassword;

	private boolean m_asIdUser;
	private String m_userName;
	private List<String> m_userNamesList;
	
	private String m_credServer;
	private String m_credUser;
	private String m_credPassword;
	private Object m_credToken;

	private boolean m_fullAccess;
	private boolean m_maxInternetRights;
	
	/**
	 * Constructor for generating a DominoClient
	 * 
	 * @return new DominoClientBuilder
	 * @throws IllegalStateException if there is no implementation of {@link DominoClientBuilder} available
	 */
	public static DominoClientBuilder newDominoClient() {
		return JNXServiceFinder.findRequiredService(DominoClientBuilder.class, DominoClientBuilder.class.getClassLoader());
	}
	
	/**
	 * Accesses NSFs as the owner of the active user ID file
	 * 
	 * @return DominoClientBuilder this builder
	 */
	public DominoClientBuilder asIDUser() {
		m_asIdUser = true;
		return this;
	}
	
	/** 
	 * Accesses NSFs as a specific user
	 * 
	 * @param userName Domino username to access as
	 * @return DominoClientBuilder this builder
	 */
	public DominoClientBuilder asUser(String userName) {
		m_userName = userName;
		return this;
	}
	
	
	/** 
	 * Accesses NSFs as a specific user
	 * 
	 * @param userNamesList Domino username to access as
	 * @return DominoClientBuilder this builder
	 */
	public DominoClientBuilder asUser(List<String> userNamesList) {
		m_userNamesList = userNamesList;
		return this;
	}
	
	/**
	 * Accesses NSFs as a specific user, validating the password when
	 * the client is constructed.
	 * 
	 * <p>This is distinct from the password assigned to a Notes ID file, but instead
	 * refers to credentials according to an active local runtime or remote server.</p>
	 * 
	 * @param serverName remote Domino server to authenticate against. May be {@code null} to use
	 * 		the local runtime
	 * @param userName Domino username to access as
	 * @param password the user's password
	 * @return this builder
	 * @throws IllegalArgumentException if userName is null or empty
	 */
	public DominoClientBuilder authenticateUser(String serverName, String userName, String password) {
		if(userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("userName cannot be empty");
		}
		m_credServer = serverName;
		m_credUser = userName;
		m_credPassword = password;
		return this;
	}
	
	/**
	 * Accesses NSFs as a specific user, validating the provided credentials token with any registered providers.
	 * 
	 * <p>This is distinct from the password assigned to a Notes ID file, but instead
	 * refers to credentials according to an active local runtime or remote server.</p>
	 * 
	 * @param serverName the name of the server to contact
	 * @param token the token to use to authenticate. The class of the token depends on the available
	 *       provider implementations
	 * @return this builder
	 * @throws NullPointerException if {@code token} is null
	 * @since 1.0.19
	 */
	public DominoClientBuilder authenticateUserWithToken(String serverName, Object token) {
		Objects.requireNonNull(token, "token cannot be null");
		m_credServer = serverName;
		m_credToken = token;
		return this;
	}
	
	
	/** 
	 * Accesses the NSFs and server with full access
	 * 
	 * @return DominoClientBuilder this builder
	 */
	public DominoClientBuilder withFullAccess() {
		m_fullAccess = true;
		return this;
	}
	
	
	/** 
	 * Applies maximum internet access settings from the NSF to the requests
	 * 
	 * @return DominoClientBuilder this builder
	 */
	public DominoClientBuilder withMaxInternetAccess() {
		m_maxInternetRights = true;
		return this;
	}
	
	
	/** 
	 * Builds a DominoClient
	 * 
	 * @return DominoClient for settings requested
	 */
	public abstract DominoClient build();
	
	// *******************************************************************************
	// * Implementation class utilities
	// *******************************************************************************
	
	protected boolean isFullAccess() {
		return m_fullAccess;
	}
	protected Path getIDFilePath() {
		return m_idFilePath;
	}
	protected String getIDPassword() {
		return m_idPassword;
	}
	protected boolean isMaxInternetAccess() {
		return m_maxInternetRights;
	}
	protected boolean isAsIDUser() {
		return m_asIdUser;
	}
	protected String getUserName() {
		return m_userName;
	}
	protected List<String> getUserNamesList() {
		return m_userNamesList;
	}
	protected String getCredServer() {
		return m_credServer;
	}
	protected String getCredUser() {
		return m_credUser;
	}
	protected String getCredPassword() {
		return m_credPassword;
	}
	protected Object getCredToken() {
		return m_credToken;
	}
}
