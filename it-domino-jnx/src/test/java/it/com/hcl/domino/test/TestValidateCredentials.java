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
package it.com.hcl.domino.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.NameNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.DominoException;
import com.ibm.commons.util.StringUtil;

@SuppressWarnings("nls")
public class TestValidateCredentials extends AbstractNotesRuntimeTest {
	private final Logger log = Logger.getLogger(getClass().getName());

	public static final String VALIDATE_CREDENTIALS_SERVER = "VALIDATE_CREDENTIALS_SERVER";
	public static final String VALIDATE_CREDENTIALS_USER = "VALIDATE_CREDENTIALS_USER";
	public static final String VALIDATE_CREDENTIALS_PASSWORD = "VALIDATE_CREDENTIALS_PASSWORD";
	public static final String VALIDATE_CREDENTIALS_BADPASSWORD = "VALIDATE_CREDENTIALS_BADPASSWORD";
	
	@Test
	@EnabledIfEnvironmentVariable(named = VALIDATE_CREDENTIALS_USER, matches = ".+")
	@EnabledIfEnvironmentVariable(named = VALIDATE_CREDENTIALS_PASSWORD, matches = ".+")
	public void testValidateCredentials() throws AuthenticationException, AuthenticationNotSupportedException, NameNotFoundException {
		String credServer = System.getenv(VALIDATE_CREDENTIALS_SERVER);
		String credUser = System.getenv(VALIDATE_CREDENTIALS_USER);
		String credPassword = System.getenv(VALIDATE_CREDENTIALS_PASSWORD);
		
		DominoClient client = getClient();
		String dn = client.validateCredentials(credServer, credUser, credPassword);
		if(log.isLoggable(Level.INFO)) {
			log.info("got dn " + dn);
		}
		assertFalse(StringUtil.isEmpty(dn), "Result DN should not be empty");
	}
	
	@Test
	@EnabledIfEnvironmentVariable(named = VALIDATE_CREDENTIALS_USER, matches = ".+")
	@EnabledIfEnvironmentVariable(named = VALIDATE_CREDENTIALS_BADPASSWORD, matches = ".+")
	public void testValidateCredentialsBadPassword() throws AuthenticationException, AuthenticationNotSupportedException, NameNotFoundException {
		String credServer = System.getenv(VALIDATE_CREDENTIALS_SERVER);
		String credUser = System.getenv(VALIDATE_CREDENTIALS_USER);
		String credPassword = System.getenv(VALIDATE_CREDENTIALS_BADPASSWORD);
		
		DominoClient client = getClient();
		assertThrows(AuthenticationException.class, () -> client.validateCredentials(credServer, credUser, credPassword));
	}
	
	@Test
	public void testValidateCredentialsFailure() throws AuthenticationException, AuthenticationNotSupportedException, NameNotFoundException {
		DominoClient client = getClient();
		assertThrows(NameNotFoundException.class, () -> client.validateCredentials(null, "It's fair to assume that a user with this name does not exist", "foo"));
	}
	
	@Test
	public void testValidateCredentialsNullFields() throws AuthenticationException, AuthenticationNotSupportedException, NameNotFoundException {
		DominoClient client = getClient();
		assertThrows(IllegalArgumentException.class, () -> client.validateCredentials(null, null, null));
	}
	
	@Test
	public void testValidateCredentialsEmptyFields() throws AuthenticationException, AuthenticationNotSupportedException, NameNotFoundException {
		DominoClient client = getClient();
		assertThrows(IllegalArgumentException.class, () -> client.validateCredentials(null, "", ""));
	}
	
	@Test
	public void testValidateCredentialsNullUser() throws AuthenticationException, AuthenticationNotSupportedException, NameNotFoundException {
		DominoClient client = getClient();
		assertThrows(IllegalArgumentException.class, () -> client.validateCredentials(null, null, "fake password"));
	}
	
	@Test
	public void testValidateCredentialsNullPassword() throws AuthenticationException, AuthenticationNotSupportedException, NameNotFoundException {
		DominoClient client = getClient();
		assertThrows(NameNotFoundException.class, () -> client.validateCredentials(null, "fake user", null));
	}
	
	@Test
	@EnabledIfEnvironmentVariable(named = VALIDATE_CREDENTIALS_USER, matches = ".+")
	@EnabledIfEnvironmentVariable(named = VALIDATE_CREDENTIALS_PASSWORD, matches = ".+")
	public void testValidateCredentialsClient() throws AuthenticationException, AuthenticationNotSupportedException, NameNotFoundException {
		String credServer = System.getenv(VALIDATE_CREDENTIALS_SERVER);
		String credUser = System.getenv(VALIDATE_CREDENTIALS_USER);
		String credPassword = System.getenv(VALIDATE_CREDENTIALS_PASSWORD);
		
		DominoClientBuilder.newDominoClient()
			.authenticateUser(credServer, credUser, credPassword)
			.build();
	}
	
	@Test
	public void testValidateCredentialsClientFailure() throws AuthenticationException, AuthenticationNotSupportedException, NameNotFoundException {
		assertThrows(DominoException.class, () ->
			DominoClientBuilder.newDominoClient()
				.authenticateUser(null, "i expect this user to not exist", "and certainly not with this password")
				.build()
		);
	}
	
	@Test
	public void testValidateCredentialsClientNullFields() throws AuthenticationException, AuthenticationNotSupportedException, NameNotFoundException {
		assertThrows(IllegalArgumentException.class, () ->
			DominoClientBuilder.newDominoClient()
				.authenticateUser(null, null, null)
				.build()
		);
	}
	
	@Test
	public void testValidateCredentialsClientNullPassword() throws AuthenticationException, AuthenticationNotSupportedException, NameNotFoundException {
		assertThrows(DominoException.class, () ->
			DominoClientBuilder.newDominoClient()
				.authenticateUser(null, "i expect this user to not exist", null)
				.build()
		);
	}
	
	@Test
	public void testValidateCredentialsClientNullUser() throws AuthenticationException, AuthenticationNotSupportedException, NameNotFoundException {
		assertThrows(IllegalArgumentException.class, () ->
			DominoClientBuilder.newDominoClient()
				.authenticateUser(null, null, "foo")
				.build()
		);
	}
	
	@Test
	public void testValidateCredentialsClientEmptyFields() throws AuthenticationException, AuthenticationNotSupportedException, NameNotFoundException {
		assertThrows(IllegalArgumentException.class, () ->
			DominoClientBuilder.newDominoClient()
				.authenticateUser(null, "", "")
				.build()
		);
	}
	
	@Test
	public void testValidateCredentialsClientEmptyPassword() throws AuthenticationException, AuthenticationNotSupportedException, NameNotFoundException {
		assertThrows(DominoException.class, () ->
			DominoClientBuilder.newDominoClient()
				.authenticateUser(null, "i expect this user to not exist", "")
				.build()
		);
	}
	
	@Test
	public void testValidateCredentialsClientEmptyUser() throws AuthenticationException, AuthenticationNotSupportedException, NameNotFoundException {
		assertThrows(IllegalArgumentException.class, () ->
			DominoClientBuilder.newDominoClient()
				.authenticateUser(null, "", "foo")
				.build()
		);
	}
}
