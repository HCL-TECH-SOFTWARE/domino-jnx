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
package com.hcl.domino.jnx.example;

import java.io.IOException;
import java.security.Principal;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.DominoProcess;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Initializes the request thread and creates a request-specific client
 */
@WebFilter(value = "/*", asyncSupported = true)
public class NotesRequestFilter implements Filter {
  public static final String ATTRIBUTE_CLIENT = "dominoClient"; //$NON-NLS-1$

  private static String ldapNameToDomino(final String value) {
    if (value == null || value.isEmpty()) {
      return ""; //$NON-NLS-1$
    } else if (!value.contains("=")) { //$NON-NLS-1$
      return value;
    } else {
      // Make sure it's actually an LDAP name. We'll assume that an un-escaped slash
      // is indicative of a Domino name
      int slashIndex = value.indexOf('/');
      while (slashIndex > -1) {
        if (slashIndex == 0 || value.charAt(slashIndex - 1) != '\\') {
          // Then it's probably a Domino name
          return value;
        }
        slashIndex = value.indexOf('/', slashIndex + 1);
      }

      try {
        final LdapName dn = new LdapName(value);
        final StringBuilder result = new StringBuilder();
        // LdapName lists components in increasing-specificity order
        for (int i = dn.size() - 1; i >= 0; i--) {
          if (result.length() > 0) {
            result.append("/"); //$NON-NLS-1$
          }

          final String component = dn.get(i);
          // Domino likes the component name capitalized - probably not REQUIRED, but it
          // shouldn't hurt
          final int indexEq = component == null ? -1 : component.indexOf('=');
          if (component != null && indexEq > -1) {
            result.append(component.substring(0, indexEq).toUpperCase());
            result.append('=');
            result.append(component.substring(indexEq + 1));
          } else {
            result.append(component);
          }
        }
        return result.toString();
      } catch (final InvalidNameException e) {
        return value;
      }
    }
  }

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
      throws IOException, ServletException {
    DominoProcess.get().initializeThread();
    try {
      final DominoClient client = DominoClientBuilder.newDominoClient().asUser(this.getUserName(request)).build();
      try {
        request.setAttribute(NotesRequestFilter.ATTRIBUTE_CLIENT, client);
        chain.doFilter(request, response);
      } finally {
        client.close();
      }
    } finally {
      DominoProcess.get().terminateThread();
    }
  }

  private String getUserName(final ServletRequest request) {
    final Principal principal = ((HttpServletRequest) request).getUserPrincipal();
    if (principal == null || principal.getName().equalsIgnoreCase("anonymous")) { //$NON-NLS-1$
      return "Anonymous"; //$NON-NLS-1$
    } else {
      return NotesRequestFilter.ldapNameToDomino(principal.getName());
    }
  }
}
