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
package com.hcl.domino.jnx.console.test;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.hcl.domino.jnx.console.BaseConsoleCallback;
import com.hcl.domino.jnx.console.DominoConsoleCreator;
import com.hcl.domino.jnx.console.IConsoleLine;
import com.hcl.domino.jnx.console.IDominoConsoleCreator;
import com.hcl.domino.jnx.console.IDominoServerController;
import com.hcl.domino.jnx.console.IServerDetails;

public class TestServerControllerConsole {

  @Test
  public void testConsole() {
    try {
      final IDominoConsoleCreator consoleCreator = new DominoConsoleCreator();

      final String adminUser = System.getenv("CONSOLE_USER");
      final String adminPassword = System.getenv("CONSOLE_PASSWORD");
      final String hostName = System.getenv("CONSOLE_HOSTNAME");
      String portStr = System.getenv("CONSOLE_PORT");
      if (portStr == null) {
        portStr = "2050";
      }
      final int portInt = Integer.parseInt(portStr);

      if (hostName == null || hostName.length() == 0) {
        // properties not configured, so stop test
        return;
      }

      final long t0 = System.currentTimeMillis();

      consoleCreator.openDominoConsole(hostName, portInt,
          adminUser, adminPassword, new BaseConsoleCallback() {

            @Override
            public void adminInfosReceived(final List<String> serverAdministrators,
                final List<String> restrictedAdministrators) {

              System.out.println(
                  "Server admins: " + serverAdministrators + "\n" +
                      "Restricted admins: " + restrictedAdministrators);
            }

            @Override
            public void consoleInitialized(final IDominoServerController ctrl) {
              System.out.println("Console initialized");

              // ctrl.sendCommand("help");
              // ctrl.requestAdminInfos();
              // ctrl.sendCommand("#show users");
              // ctrl.sendCommand("#show processes");
              // ctrl.sendBroadcastMessage("Hello World");
              // ctrl.sendCommand("$ dir ..\\*.nsf");
              // ctrl.sendCommand("#kill domino");
              // ctrl.sendCommand("#start domino");
              // ctrl.startServer();
              ctrl.sendCommand("help");
            }

            @Override
            public void consoleMessageReceived(final IConsoleLine line) {
              System.out.println(line);
            }

            @Override
            public void dominoStatusReceived(final DominoStatus status) {
              System.out.println("Domino server status: " + status);
            }

            @Override
            public String passwordRequested(final String msg, final String title) {
              // return server.ID password if server is currently starting up
              // and requires one
              // String serverPwd = System.getenv("CONSOLE_SERVERPWD");
              // return serverPwd;
              return null;
            }

            @Override
            public void serverDetailsReceived(final IServerDetails details) {
              System.out.println("Domino and OS details:\n" + details);
            }

            @Override
            public void setStatusMessage(final String msg) {
              System.out.println(msg);
            }

            @Override
            public boolean shouldDisconnect() {
              final long t1 = System.currentTimeMillis();
              // stop console after 5 seconds
              return t1 - t0 > 5 * 1000;
            }

            @Override
            public void showMessageDialog(final String msg, final String title) {
              System.out.println(title + " - " + msg);
            }
          });

      System.out.println("Domino console closed.");

    } catch (final Exception e) {
      e.printStackTrace();
    }
  }
}
