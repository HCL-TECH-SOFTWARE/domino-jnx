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
package com.hcl.domino.jnx.example.domino.webapp.admin;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.DominoProcess;
import com.hcl.domino.commons.util.DominoUtils;

public class RestEasyServlet extends HttpServletDispatcher {
  private static final long serialVersionUID = 1L;

  public static RestEasyServlet instance;
  public DominoClient dominoClient;
  public ExecutorService executor;

  @Override
  public void destroy() {
    this.executor.shutdownNow();
    try {
      this.executor.awaitTermination(1, TimeUnit.MINUTES);
    } catch (final InterruptedException e) {
    }
    this.dominoClient.close();
  }

  @Override
  public void init(final ServletConfig servletConfig) throws ServletException {
    RestEasyServlet.instance = this;

    DominoUtils.setNoInit(true);
    DominoUtils.setNoTerm(true);
    final ClassLoader cl = Thread.currentThread().getContextClassLoader();
    AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
      Thread.currentThread().setContextClassLoader(RestEasyServlet.class.getClassLoader());
      return null;
    });
    try {
      DominoProcess.get().initializeProcess(new String[0]);
      this.dominoClient = DominoClientBuilder.newDominoClient().build();
      this.executor = Executors.newCachedThreadPool(this.dominoClient.getThreadFactory());

      super.init(servletConfig);
    } finally {
      AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
        Thread.currentThread().setContextClassLoader(cl);
        return null;
      });
    }
  }

  @Override
  protected void service(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse)
      throws ServletException, IOException {
    final ClassLoader cl = Thread.currentThread().getContextClassLoader();
    try {
      AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
        Thread.currentThread().setContextClassLoader(RestEasyServlet.class.getClassLoader());

        return null;
      });

      httpServletResponse.setBufferSize(1);

      super.service(httpServletRequest, httpServletResponse);
    } catch (final Exception e) {
      // Look for a known case of blank XspCmdExceptions
      Throwable t = e;
      while (t != null && t.getCause() != null) {
        t = t.getCause();
      }
      if (t.getClass().getName().equals("com.ibm.domino.xsp.bridge.http.exception.XspCmdException")) { //$NON-NLS-1$
        if ("HTTP: Internal error:".equals(String.valueOf(t.getMessage()).trim())) { //$NON-NLS-1$
          // Ignore
          return;
        }
      }

      throw e;
    } finally {
      AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
        Thread.currentThread().setContextClassLoader(cl);
        return null;
      });
    }
  }
}
