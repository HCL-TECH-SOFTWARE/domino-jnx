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
package it.com.hcl.domino.test.jakarta.security;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.NameNotFoundException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import com.hcl.domino.jnx.jakarta.security.NotesDirectoryIdentityStore;
import com.ibm.commons.util.StringUtil;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.CredentialValidationResult.Status;

@SuppressWarnings("nls")
public class TestJakartaIdentityStore extends AbstractNotesRuntimeTest {
  public static final String VALIDATE_CREDENTIALS_SERVER = "VALIDATE_CREDENTIALS_SERVER";

  public static final String VALIDATE_CREDENTIALS_USER = "VALIDATE_CREDENTIALS_USER";
  public static final String VALIDATE_CREDENTIALS_PASSWORD = "VALIDATE_CREDENTIALS_PASSWORD";
  public static final String VALIDATE_CREDENTIALS_BADPASSWORD = "VALIDATE_CREDENTIALS_BADPASSWORD";
  private final Logger log = Logger.getLogger(this.getClass().getName());

  @Test
  @EnabledIfEnvironmentVariable(named = TestJakartaIdentityStore.VALIDATE_CREDENTIALS_USER, matches = ".+")
  @EnabledIfEnvironmentVariable(named = TestJakartaIdentityStore.VALIDATE_CREDENTIALS_PASSWORD, matches = ".+")
  public void testValidateCredentials() throws AuthenticationException, AuthenticationNotSupportedException, NameNotFoundException {
    final String credServer = System.getenv(TestJakartaIdentityStore.VALIDATE_CREDENTIALS_SERVER);
    final String credUser = System.getenv(TestJakartaIdentityStore.VALIDATE_CREDENTIALS_USER);
    final String credPassword = System.getenv(TestJakartaIdentityStore.VALIDATE_CREDENTIALS_PASSWORD);
    if (StringUtil.isNotEmpty(credServer)) {
      if (this.log.isLoggable(Level.INFO)) {
        this.log.info(MessageFormat.format("Skipping {0}#testVerifyCredentials due to non-empty remote server",
            this.getClass().getSimpleName()));
      }
      return;
    }

    final NotesDirectoryIdentityStore store = new NotesDirectoryIdentityStore();
    final CredentialValidationResult result = store.validate(new UsernamePasswordCredential(credUser, credPassword));
    Assertions.assertNotNull(result);
    Assertions.assertEquals(Status.VALID, result.getStatus());
    Assertions.assertFalse(StringUtil.isEmpty(result.getCallerDn()), "Result DN should not be empty");
  }

  @Test
  @EnabledIfEnvironmentVariable(named = TestJakartaIdentityStore.VALIDATE_CREDENTIALS_USER, matches = ".+")
  @EnabledIfEnvironmentVariable(named = TestJakartaIdentityStore.VALIDATE_CREDENTIALS_BADPASSWORD, matches = ".+")
  public void testValidateCredentialsBadPassword()
      throws AuthenticationException, AuthenticationNotSupportedException, NameNotFoundException {
    final String credServer = System.getenv(TestJakartaIdentityStore.VALIDATE_CREDENTIALS_SERVER);
    final String credUser = System.getenv(TestJakartaIdentityStore.VALIDATE_CREDENTIALS_USER);
    final String credPassword = System.getenv(TestJakartaIdentityStore.VALIDATE_CREDENTIALS_BADPASSWORD);
    if (StringUtil.isNotEmpty(credServer)) {
      if (this.log.isLoggable(Level.INFO)) {
        this.log.info(MessageFormat.format("Skipping {0}#testValidateCredentialsBadPassword due to non-empty remote server",
            this.getClass().getSimpleName()));
      }
      return;
    }

    final NotesDirectoryIdentityStore store = new NotesDirectoryIdentityStore();
    final CredentialValidationResult result = store.validate(new UsernamePasswordCredential(credUser, credPassword));
    Assertions.assertNotNull(result);
    Assertions.assertEquals(Status.INVALID, result.getStatus());
  }

  @Test
  public void testValidateCredentialsNotValidated()
      throws AuthenticationException, AuthenticationNotSupportedException, NameNotFoundException {
    final NotesDirectoryIdentityStore store = new NotesDirectoryIdentityStore();
    final CredentialValidationResult result = store
        .validate(new UsernamePasswordCredential("It's fair to assume that a user with this name does not exist", "foo"));
    Assertions.assertNotNull(result);
    Assertions.assertEquals(Status.NOT_VALIDATED, result.getStatus());
  }
}
