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

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Thread to parse a list of administrative console infos like the server group
 * or
 * server config.
 */
class ParserThread extends Thread {
  /** parse server information */
  public static int PARSE_SRVR = 0;
  /** parse group information */
  public static int PARSE_GRPS = 1;
  private final int parseType;
  private final Vector<String> cmds;
  private final Vector<ServerMap> srvrsList;
  private final Vector<GroupMap> grpsList;
  private final DominoConsoleRunner dc;
  private final ServerMap sm;
  private final AdminDataParser parser;

  public ParserThread(final DominoConsoleRunner dominoConsole, final ServerMap sm, final Vector<String> cmds, final int parseType) {
    this.dc = dominoConsole;
    this.sm = sm;
    this.cmds = cmds;
    this.parseType = parseType;
    this.srvrsList = new Vector<>(20, 5);
    this.grpsList = new Vector<>(10, 5);
    this.parser = new AdminDataParser(dominoConsole);
  }

  @Override
  public void run() {
    boolean done = false;
    while (!done) {
      if (this.cmds.size() == 0) {
        try {
          Thread.sleep(50L);
        } catch (final InterruptedException interruptedException) {
        }
        continue;
      }
      final String currCmd = this.cmds.elementAt(0);
      this.cmds.removeElementAt(0);

      final StringTokenizer st = new StringTokenizer(currCmd, "\n");
      while (!done && st.hasMoreTokens()) {
        final String currToken = st.nextToken();
        if (this.parseType == ParserThread.PARSE_SRVR) {
          done = this.parser.parseServersAndGroups(this.srvrsList, this.grpsList, currToken);
          continue;
        }
        done = this.parser.parseGroup(this.grpsList, currToken);
      }
      if (!done) {
        continue;
      }

      String msg = null;
      boolean showInfo = false;
      if (this.parseType == ParserThread.PARSE_SRVR) {
        // report collected server infos
        this.dc.updateServerList(this.srvrsList);
        if (this.grpsList.size() > 0) {
          // report collected groups
          this.dc.updateGroupList(this.grpsList);
        }
        if (this.srvrsList.size() == 1) {
          showInfo = false;
        }
        msg = DominoConsoleRunner.getResourceBundle().getString("msgRefreshSrvrs");
      } else if (this.parseType == ParserThread.PARSE_GRPS) {
        if (this.grpsList.size() > 0) {
          // report collected groups
          this.dc.updateGroupList(this.grpsList);
        }
        msg = DominoConsoleRunner.getResourceBundle().getString("msgRefreshGrps");
      }
      if (showInfo) {
        this.dc.reportMessageDialog(this.sm, msg, DominoConsoleRunner.getResourceBundle().getString("msgInfo"));
      }
      if (this.cmds.size() <= 0) {
        continue;
      }
      done = false;
    }
  }
}
