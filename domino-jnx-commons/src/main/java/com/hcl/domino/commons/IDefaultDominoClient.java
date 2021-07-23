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
package com.hcl.domino.commons;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.NameNotFoundException;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoProcess;
import com.hcl.domino.commons.server.DefaultServerInfo;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.DatabaseClass;
import com.hcl.domino.misc.JNXServiceFinder;
import com.hcl.domino.security.CredentialValidationTokenHandler;
import com.hcl.domino.server.ServerInfo;

/**
 * Contains implementation-neutral versions of some DominoClient methods.
 *
 * @author Jesse Gallagher
 * @since 1.0.19
 */
public interface IDefaultDominoClient extends DominoClient {
  ExecutorService defaultExecutorService = Executors.newCachedThreadPool();

  @Override
  default Database createDatabase(final String paramServerName, final String filePath, final boolean forceCreation,
      final boolean initDesign,
      final Encryption encryption) {
    return this.createDatabase(paramServerName, filePath, forceCreation, initDesign, encryption, DatabaseClass.BY_EXTENSION);
  }

  @Override
  default ServerInfo getServerInfo(final String directoryServer, final String serverName) {
    if (serverName == null || serverName.isEmpty()) {
      throw new IllegalArgumentException("serverName cannot be empty");
    }
    return new DefaultServerInfo(this, directoryServer, serverName);
  }

  @Override
  default Database openDatabase(final String path) {
    return this.openDatabase(path, Collections.emptySet());
  }

  @Override
  default Database openDatabase(final String serverName, final String filePath) {
    return this.openDatabase(serverName, filePath, Collections.emptySet());
  }

  @Override
  default Optional<Database> openMailDatabase() {
    return this.openMailDatabase(Collections.emptySet());
  }

  @Override
  default <T> FutureTask<T> runAsync(final Callable<T> callable) {
    return this.runAsync(null, callable);
  }

  @Override
  default <T> FutureTask<T> runAsync(final ExecutorService service, final Callable<T> callable) {
    final FutureTask<T> task = new FutureTask<>(() -> {
      DominoProcess.get().initializeThread();

      try {
        final T result = callable.call();
        return result;
      } finally {
        DominoProcess.get().terminateThread();
      }
    });

    if (service == null) {
      IDefaultDominoClient.defaultExecutorService.execute(task);
    } else {
      service.execute(task);

    }

    return task;
  }

  @SuppressWarnings("unchecked")
  @Override
  default String validateCredentialsWithToken(final String serverName, final Object token)
      throws NameNotFoundException, AuthenticationException, AuthenticationNotSupportedException {
    try {
      return JNXServiceFinder.findServices(CredentialValidationTokenHandler.class)
          .map(h -> (CredentialValidationTokenHandler<Object>) h)
          .filter(h -> h.canProcess(token))
          .map(h -> {
            try {
              return h.getUserDn(token, serverName, this);
            } catch (AuthenticationException | AuthenticationNotSupportedException | NameNotFoundException e) {
              throw new RuntimeException(e);
            }
          })
          .filter(Optional::isPresent)
          .findFirst()
          .map(Optional::get)
          .orElseThrow(() -> new UnsupportedOperationException(
              MessageFormat.format("No {0} implementation found to handle token of type {1}",
                  CredentialValidationTokenHandler.class.getSimpleName(), token == null ? "null" : token.getClass().getName()) //$NON-NLS-1$
          ));
    } catch (final RuntimeException e) {
      // Unwrap inner security exceptions
      final Throwable t = e.getCause();
      if (t instanceof AuthenticationException) {
        throw (AuthenticationException) t;
      } else if (t instanceof NameNotFoundException) {
        throw (NameNotFoundException) t;
      } else if (t instanceof AuthenticationNotSupportedException) {
        throw (AuthenticationNotSupportedException) t;
      }
      throw e;
    }
  }
}
