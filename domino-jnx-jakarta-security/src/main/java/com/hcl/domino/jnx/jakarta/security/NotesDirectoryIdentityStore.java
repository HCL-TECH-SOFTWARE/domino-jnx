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
package com.hcl.domino.jnx.jakarta.security;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.NameNotFoundException;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;

@ApplicationScoped
public class NotesDirectoryIdentityStore implements IdentityStore {

  @Override
  public Set<String> getCallerGroups(final CredentialValidationResult validationResult) {
    final String dn = validationResult.getCallerDn();
    return this.getGroups(dn);
  }

  private Set<String> getGroups(final String dn) {
    try (DominoClient client = DominoClientBuilder.newDominoClient().asUser(dn).build()) {
      // TODO filter out non-glob names
      return new LinkedHashSet<>(client.getEffectiveUserNamesList(null).toList());
    }
  }

  @Override
  public int priority() {
    return 70;
  }

  @Override
  public CredentialValidationResult validate(final Credential credential) {
    return IdentityStore.super.validate(credential);
  }

  public CredentialValidationResult validate(final UsernamePasswordCredential credential) {
    try (DominoClient client = DominoClientBuilder.newDominoClient().build()) {
      final String dn = client.validateCredentials(null, credential.getCaller(), credential.getPasswordAsString());
      return new CredentialValidationResult(null, dn, dn, dn, this.getGroups(dn));
    } catch (final NameNotFoundException e) {
      return CredentialValidationResult.NOT_VALIDATED_RESULT;
    } catch (AuthenticationException | AuthenticationNotSupportedException e) {
      return CredentialValidationResult.INVALID_RESULT;
    }
  }

  @Override
  public Set<ValidationType> validationTypes() {
    return IdentityStore.DEFAULT_VALIDATION_TYPES;
  }

}
