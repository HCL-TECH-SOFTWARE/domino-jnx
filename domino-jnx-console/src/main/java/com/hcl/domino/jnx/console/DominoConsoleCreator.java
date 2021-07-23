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
package com.hcl.domino.jnx.console;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.hcl.domino.jnx.console.IConsoleCallback.DominoStatus;
import com.hcl.domino.jnx.console.internal.ConsoleLine;
import com.hcl.domino.jnx.console.internal.DominoConsoleRunner;
import com.hcl.domino.jnx.console.internal.LoginSettings;
import com.hcl.domino.jnx.console.internal.ServerDetails;
import com.hcl.domino.jnx.console.internal.ServerMap;

/**
 * Implementation of {@link IDominoConsoleCreator} that opens a connection
 * to a local or remote Domino server controller to access the Domino console.
 * The underlying API heavily spawns new threads for various operations, e.g.
 * for
 * reading/writing to the Domino console or displaying prompts. In
 * this class we collect all requests and execute them in the caller thread
 * to simplify the architecture and prevent multithreading issues.
 *
 * @author Karsten Lehmann
 */
public class DominoConsoleCreator implements IDominoConsoleCreator {

  private static void openDominoConsole(final ServerMap sm, final boolean isAdvanced, final IConsoleCallback callback)
      throws Exception {
    // queues for messages to report and code to execute
    final LinkedBlockingQueue<ConsoleLine> incomingMessages = new LinkedBlockingQueue<>();
    final LinkedBlockingQueue<Runnable> todos = new LinkedBlockingQueue<>();

    final AtomicReference<Exception> connectException = new AtomicReference<>();

    final AtomicBoolean consoleExited = new AtomicBoolean();

    final DominoConsoleRunner console = new DominoConsoleRunner(sm, isAdvanced) {
      private boolean isInitialized;

      @Override
      protected void adminInfosReceived(final Vector<String> serverAdministrators,
          final Vector<String> restrictedAdministrators) {
        todos.add(() -> {
          callback.adminInfosReceived(serverAdministrators, restrictedAdministrators);
        });
      }

      @Override
      public void closeOpenPasswordDialog(final ServerMap sm) {
        todos.add(() -> {
          callback.closeOpenPasswordDialog();
        });
      }

      @Override
      public void closeOpenPrompt(final ServerMap sm) {
        todos.add(() -> {
          callback.closeOpenPrompt();
        });
      }

      @Override
      public void consoleMessageReceived(final ServerMap sm, final ConsoleLine line) {
        incomingMessages.add(line);
      }

      @Override
      public void reportConsoleConnectFailed(final ServerMap sm, final String msg, final Exception exception) {
        connectException.set(new IOException(msg, exception));
      }

      @Override
      public void reportConsoleInitialized(final ServerMap sm) {
        this.isInitialized = true;
        final DominoConsoleRunner thisConsole = this;

        todos.add(() -> {
          callback.consoleInitialized(new IDominoServerController() {

            @Override
            public void killServer() {
              thisConsole.sendCommand("#kill domino"); //$NON-NLS-1$
            }

            @Override
            public void quitServerAndServerController() {
              thisConsole.sendCommand("#quit"); //$NON-NLS-1$
            }

            @Override
            public void requestAdminInfos() {
              thisConsole.sendCommand("#update admins"); //$NON-NLS-1$
            }

            @Override
            public void sendBroadcastMessage(final String msg) {
              thisConsole.sendBroadcastMessage(msg);
            }

            @Override
            public void sendCommand(final String cmd) {
              thisConsole.sendCommand(cmd);
            }

            @Override
            public void showProcesses() {
              thisConsole.sendCommand("#show processes"); //$NON-NLS-1$
            }

            @Override
            public void showUsers() {
              thisConsole.sendCommand("#show users"); //$NON-NLS-1$
            }

            @Override
            public void startServer() {
              thisConsole.sendCommand("#start domino"); //$NON-NLS-1$
            }

            @Override
            public void stopServer() {
              thisConsole.sendCommand("quit"); //$NON-NLS-1$
            }
          });
        });
      }

      @Override
      protected void reportDominoStatus(final ServerMap sm, final DominoStatus status) {
        todos.add(() -> {
          callback.dominoStatusReceived(status);
        });
      }

      @Override
      public void reportMessageDialog(final ServerMap sm, final String msg, final String title) {
        todos.add(() -> {
          callback.showMessageDialog(msg, title);
        });
      }

      @Override
      protected void reportServerInfosUpdated(final ServerMap existingServer, final ServerMap update) {
        final ServerDetails details = new ServerDetails();
        details.setAdminServer(update.isAdminServer());
        details.setDb2Server(update.isDB2server());
        details.setHostName(update.getHostname());
        details.setClusterName(update.getClusterName());
        details.setDomain(update.getDomain());
        details.setServerName(update.getServerName());
        details.setOSName(update.getServerType());
        details.setPort(update.getPort());
        details.setServerTitle(update.getTitle());
        details.setServerVersion(update.getVersion());

        todos.add(() -> {
          callback.serverDetailsReceived(details);
        });
      }

      @Override
      public void reportStatusMessage(final ServerMap sm, final String msg) {
        todos.add(() -> {
          callback.setStatusMessage(msg);
        });
      }

      @Override
      public String requestInputDialog(final ServerMap sm, final String msg, final String title, final String[] values,
          final String initialSelection) {
        final AtomicReference<String> retValue = new AtomicReference<>();
        final Object lock = new Object();
        final AtomicReference<Exception> ex = new AtomicReference<>();

        todos.add(() -> {
          try {
            retValue.set(callback.showInputDialog(msg, title, values, initialSelection));
          } catch (final Exception e) {
            ex.set(e);
          } finally {
            synchronized (lock) {
              lock.notify();
            }
          }
        });

        synchronized (lock) {
          try {
            lock.wait();
          } catch (final InterruptedException e) {
            throw new RuntimeException("Input prompt interrupted", e);
          }

          if (ex.get() != null) {
            throw new RuntimeException("Input prompt error", ex.get());
          }

          return retValue.get();
        }
      }

      @Override
      public boolean requestLoginSettings(final ServerMap sm, final LoginSettings loginSettings) {
        final AtomicBoolean retValue = new AtomicBoolean();
        final Object lock = new Object();
        final AtomicReference<Exception> ex = new AtomicReference<>();

        todos.add(() -> {
          try {
            retValue.set(callback.requestLoginSettings(loginSettings));
          } catch (final Exception e) {
            ex.set(e);
          } finally {
            synchronized (lock) {
              lock.notify();
            }
          }
        });

        synchronized (lock) {
          try {
            lock.wait();
          } catch (final InterruptedException e) {
            throw new RuntimeException("Input prompt interrupted", e);
          }

          if (ex.get() != null) {
            throw new RuntimeException("Input prompt error", ex.get());
          }

          return retValue.get();
        }
      }

      @Override
      public String requestPasswordDialog(final ServerMap sm, final String msg, final String title) {
        final AtomicReference<String> retValue = new AtomicReference<>();
        final Object lock = new Object();
        final AtomicReference<Exception> ex = new AtomicReference<>();

        todos.add(() -> {
          try {
            retValue.set(callback.passwordRequested(msg, title));
          } catch (final Exception e) {
            ex.set(e);
          } finally {
            synchronized (lock) {
              lock.notify();
            }
          }
        });

        synchronized (lock) {
          try {
            lock.wait();
          } catch (final InterruptedException e) {
            throw new RuntimeException("Password prompt interrupted", e);
          }

          if (ex.get() != null) {
            throw new RuntimeException("Password prompt error", ex.get());
          }

          return retValue.get();
        }
      }

      @Override
      public <T> T requestPrompt(final ServerMap sm, final String msg, final String title, final T[] options) {
        final AtomicReference<T> retValue = new AtomicReference<>();
        final Object lock = new Object();
        final AtomicReference<Exception> ex = new AtomicReference<>();

        todos.add(() -> {
          try {
            retValue.set(callback.showPrompt(msg, title, options));
          } catch (final Exception e) {
            ex.set(e);
          } finally {
            synchronized (lock) {
              lock.notify();
            }
          }
        });

        synchronized (lock) {
          try {
            lock.wait();
          } catch (final InterruptedException e) {
            throw new RuntimeException("Password prompt interrupted", e);
          }

          if (ex.get() != null) {
            throw new RuntimeException("Password prompt error", ex.get());
          }

          return retValue.get();
        }
      }

      @Override
      public void sendCommand(final String cmd) {
        if (!this.isInitialized) {
          throw new IllegalStateException("Console not fully initialized");
        }

        super.sendCommand(cmd);
      }

    };

    final AtomicReference<Exception> loopException = new AtomicReference<>();

    final AtomicBoolean exitRequired = new AtomicBoolean();

    while (!consoleExited.get() && loopException.get() == null) {
      // check if connect failed
      final Exception ex = connectException.get();
      if (ex != null) {
        exitRequired.set(false);

        // trigger an async exit and dont wait for the result, because
        // the console might not be fully initialized and running
        console.exit(() -> {
        });

        throw ex;
      }

      if (callback.shouldDisconnect()) {
        exitRequired.set(false);

        console.exit(() -> consoleExited.set(true));
      }

      // report all available console messages within the caller thread
      ConsoleLine line;
      while ((line = incomingMessages.poll()) != null) {
        try {
          callback.consoleMessageReceived(line);
        } catch (final Exception e) {
          loopException.set(e);
        }
      }

      // and process all other todos in the caller thread, too
      // e.g. to show UI prompts
      Runnable todo;
      while ((todo = todos.poll()) != null) {
        try {
          todo.run();
        } catch (final Exception e) {
          loopException.set(e);
        }
      }

      Thread.sleep(300);
    }

    if (exitRequired.get()) {
      console.exit(() -> {
        //
      });
    }

    if (loopException.get() != null) {
      throw loopException.get();
    }
  }

  @Override
  public void openDominoConsole(final String hostName, int port, String user, String password, final IConsoleCallback callback)
      throws Exception {

    if (hostName == null || hostName.length() == 0) {
      throw new IllegalArgumentException("Hostname cannot be empty");
    }

    if (user == null || user.length() == 0) {
      user = "localAdmin"; //$NON-NLS-1$
    }

    if (password == null || password.length() == 0) {
      password = "localAdmin"; //$NON-NLS-1$
    }

    if (port == 0) {
      port = 2050;
    }

    final ServerMap sm = new ServerMap();
    sm.setHostname(hostName);
    sm.setPort(port);
    sm.setUserName(user);
    sm.setPassword(password);

    DominoConsoleCreator.openDominoConsole(sm, false, callback);
  }

  @Override
  public void openDominoConsole(final String serviceName, final String binderHostName, final int binderPort, final String userName,
      final String password, final boolean viaFirewall, final String socksName, final int socksPort,
      final IConsoleCallback callback)
      throws Exception {

    final ServerMap sm = new ServerMap();
    sm.setServiceName(serviceName);
    sm.setBinderName(binderHostName);
    sm.setBinderPort(Integer.toString(binderPort));
    sm.setUserName(userName);
    sm.setPassword(password);
    sm.setViaFirewall(viaFirewall);
    sm.setProxyName(socksName);
    sm.setProxyPort(Integer.toString(socksPort));

    DominoConsoleCreator.openDominoConsole(sm, false, callback);
  }

  @Override
  public void openLocalDominoConsole(final IConsoleCallback callback) throws Exception {
    this.openDominoConsole(InetAddress.getLocalHost().getHostName(), 2050, "localAdmin", "localAdmin", callback); //$NON-NLS-1$ //$NON-NLS-2$
  }

}
