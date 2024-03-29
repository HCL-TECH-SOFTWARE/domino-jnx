/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
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
package com.hcl.domino.jnx.example.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.jnx.jakarta.security.NotesDirectoryIdentityStore;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.CredentialValidationResult.Status;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.HttpHeaders;

@ApplicationScoped
public class AppAuthenticationMechanism implements HttpAuthenticationMechanism {

  @Inject
  NotesDirectoryIdentityStore store;

  @Override
  public AuthenticationStatus validateRequest(final HttpServletRequest request, final HttpServletResponse response,
      final HttpMessageContext httpMessageContext) throws AuthenticationException {

    // Check Basic auth first
    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (StringUtil.isNotEmpty(authHeader)) {
      if (authHeader.startsWith("Basic ")) { //$NON-NLS-1$
        final String pair = new String(Base64.getUrlDecoder().decode(authHeader.substring("Basic ".length())), //$NON-NLS-1$
            StandardCharsets.UTF_8);
        final int colonIndex = pair.indexOf(':');
        if (colonIndex > -1) {
          final String username = pair.substring(0, colonIndex);
          final String password = pair.substring(colonIndex + 1);
          return this.validateUser(username, password, httpMessageContext);
        }
      }
    }

    // Check if we're POSTing to j_security_check
    if ("POST".equals(request.getMethod()) //$NON-NLS-1$
        && ("/j_security_check".equals(request.getServletPath()) || "/j_security_check".equals(request.getPathInfo()))) {  //$NON-NLS-1$ //$NON-NLS-2$
      final String username = request.getParameter("j_username"); //$NON-NLS-1$
      final String password = request.getParameter("j_password"); //$NON-NLS-1$
      return this.validateUser(username, password, httpMessageContext);
    }

    return httpMessageContext.doNothing();
  }

  private AuthenticationStatus validateUser(final String username, final String password,
      final HttpMessageContext httpMessageContext) {
    final CredentialValidationResult result = this.store.validate(new UsernamePasswordCredential(username, password));
    if (result != null && result.getStatus() == Status.VALID) {
      // Set the authentication session whether or not the context says it was
      // requested
      httpMessageContext.setRegisterSession(result.getCallerDn(), result.getCallerGroups());
      return httpMessageContext.notifyContainerAboutLogin(result);
    }
    return AuthenticationStatus.SEND_FAILURE;
  }

}