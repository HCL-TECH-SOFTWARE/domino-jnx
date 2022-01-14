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

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;

import org.apache.commons.io.IOUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter(value = "/*", asyncSupported = true)
public class AuthenticationFilter implements Filter {

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
      throws IOException, ServletException {
    final HttpServletRequest req = (HttpServletRequest) request;
    final HttpServletResponse resp = (HttpServletResponse) response;

    if ("/j_security_check".equals(req.getServletPath()) || "/j_security_check".equals(req.getPathInfo())) { //$NON-NLS-1$ //$NON-NLS-2$
      chain.doFilter(request, response);
      return;
    }

    final Principal p = req.getUserPrincipal();
    if (p == null || "anonymous".equalsIgnoreCase(p.getName())) { //$NON-NLS-1$
      // No need to re-request login.html for this app
      if ("/index.html".equals(req.getPathInfo())) { //$NON-NLS-1$
        resp.sendRedirect("/"); //$NON-NLS-1$
      }

      resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      resp.setContentType("text/html"); //$NON-NLS-1$
      resp.addCookie(new Cookie("WASReqURL", req.getRequestURI())); //$NON-NLS-1$
      try (InputStream is = this.getClass().getResourceAsStream("/login.html")) { //$NON-NLS-1$
        IOUtils.copy(is, resp.getOutputStream());
      }
    } else {
      chain.doFilter(request, response);
    }
  }

}
