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
package com.hcl.domino.jnx.console.internal;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

import lotus.domino.console.BinaryMsgFormat;
import lotus.domino.console.MsgFormat;

/**
 * Thread to write console commands to one or more server controller sockets
 */
class ControllerWriter extends Thread {
  DominoConsoleRunner dc;
  private final ObjectStack cmap;
  private boolean stopflag = false;

  public ControllerWriter(final DominoConsoleRunner dominoConsole) {
    super("ControllerWriter");
    this.dc = dominoConsole;
    this.cmap = dominoConsole.getCommandMap();
  }

  @Override
  public void run() {
    ServerMap[] serverMapsArr = new ServerMap[1];
    int n = 0;
    while (!this.stopflag) {
      CommandMap commandMap;
      try {
        commandMap = (CommandMap) this.cmap.popObject();
      } catch (final InterruptedException interruptedException) {
        continue;
      }
      n = 0;

      final Vector<ServerMap> serverMaps = this.dc.getServerMaps();

      if (commandMap.isGroup()) {
        // send command to a group of servers
        if (this.dc.getGrpsList().containsKey(commandMap.getDestination())) {
          final GroupMap currGroupMap = this.dc.getGrpsList().get(commandMap.getDestination());

          final Vector<String> serverGroupMembers = currGroupMap.getMembers();
          if (serverMapsArr.length < serverGroupMembers.size()) {
            serverMapsArr = new ServerMap[serverGroupMembers.size()];
          }

          for (int i = 0; i < serverGroupMembers.size(); ++i) {
            final String currServerGroupMember = serverGroupMembers.elementAt(i);

            if (!this.dc.getServersByName().containsKey(currServerGroupMember)) {
              continue;
            }
            serverMapsArr[n++] = this.dc.getServersByName().get(currServerGroupMember);
          }

          if (currGroupMap.isTempGroup()) {
            this.dc.getGrpsList().remove(commandMap.getDestination());
          }
        }
      } else {
        // send command to a single server
        serverMapsArr[n++] = serverMaps.elementAt(commandMap.getIndex());
      }

      final BinaryMsgFormat msgFormat = new BinaryMsgFormat("test");

      for (int i = 0; i < n; ++i) {
        synchronized (this) {
          final ServerMap currServerMap = serverMapsArr[i];

          if (currServerMap == null) {
            continue;
          }
          serverMapsArr[i] = null;

          if (!currServerMap.isActive() ||
              currServerMap.isActive() && (currServerMap.getSocket() == null || currServerMap.getControllerVersion() == null)) {
            continue;
          }
          final Socket socket = currServerMap.getSocket();
          if (socket == null) {
            continue;
          }
          try {
            if (currServerMap.getControllerVersion().equalsIgnoreCase("2.0")) {
              if (currServerMap.getObjectOutputStream() == null) {
                currServerMap.setObjectOutputStream(new ObjectOutputStream(socket.getOutputStream()));
              }
              if (commandMap.getType() == 13) {
                msgFormat.svcname(commandMap.getCommand());
                msgFormat.setData(commandMap.getData());
              } else {
                ((MsgFormat) msgFormat).setData(commandMap.getCommand(), commandMap.getCommand().length() - 1);
              }
              ((MsgFormat) msgFormat).setType(commandMap.getType());
              currServerMap.getObjectOutputStream().writeObject(msgFormat);
              currServerMap.getObjectOutputStream().flush();
              currServerMap.getObjectOutputStream().reset();
              msgFormat.setBData(null);
            } else {
              final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream(), "UTF8");
              if (outputStreamWriter != null) {
                outputStreamWriter.write(commandMap.getCommand());
                outputStreamWriter.flush();
              }
            }
          } catch (final IOException iOException) {
            iOException.printStackTrace();
          }

          continue;
        }
      }
    }
  }

  public void setStop() {
    this.stopflag = true;
    this.interrupt();
  }
}
