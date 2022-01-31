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
package com.hcl.domino.jnx.console.internal;

import java.util.Collections;
import java.util.Vector;

/**
 * Utility class that parses administrative infos like the Domino server version
 * and its host OS
 */
public class AdminDataParser {
  private static final String sServersBeg = "<servers"; //$NON-NLS-1$
  private static final String sServersEnd = "</servers>"; //$NON-NLS-1$
  private static final String sDomain = "domain=\""; //$NON-NLS-1$
  private static final String sGroupsBeg = "<groups>"; //$NON-NLS-1$
  private static final String sGroupsEnd = "</groups>"; //$NON-NLS-1$
  private static final String sNameBeg = "<name>"; //$NON-NLS-1$
  private static final String sNameEnd = "</name>"; //$NON-NLS-1$
  private static final String sOsBeg = "<os>"; //$NON-NLS-1$
  private static final String sOsEnd = "</os>"; //$NON-NLS-1$
  private static final String sServerInfoBeg = "<serverinfo"; //$NON-NLS-1$
  private static final String sAdmin = "admin=\"1\""; //$NON-NLS-1$
  private static final String sDB2 = "db2=\"1\""; //$NON-NLS-1$
  private static final String sServerInfoEnd = "</serverinfo>"; //$NON-NLS-1$
  private static final String sClusterBeg = "<cluster>"; //$NON-NLS-1$
  private static final String sClusterEnd = "</cluster>"; //$NON-NLS-1$
  private static final String sHostnameBeg = "<hostname>"; //$NON-NLS-1$
  private static final String sHostnameEnd = "</hostname>"; //$NON-NLS-1$
  private static final String sPortBeg = "<port>"; //$NON-NLS-1$
  private static final String sPortEnd = "</port>"; //$NON-NLS-1$
  private static final String sTitleBeg = "<title>"; //$NON-NLS-1$
  private static final String sTitleEnd = "</title>"; //$NON-NLS-1$
  private static final String sVersionBeg = "<version>"; //$NON-NLS-1$
  private static final String sVersionEnd = "</version>"; //$NON-NLS-1$
  private static final String sGroupInfoBeg = "<groupinfo>"; //$NON-NLS-1$
  private static final String sGroupInfoEnd = "</groupinfo>"; //$NON-NLS-1$
  private static final String sMembersBeg = "<members>"; //$NON-NLS-1$
  private static final String sMembersEnd = "</members>"; //$NON-NLS-1$
  private static final String sMemberDataBeg = "<memberdata "; //$NON-NLS-1$
  private static final String sMemberDataEnd = "</memberdata>"; //$NON-NLS-1$
  private static final String xmlAmp = "&amp;"; //$NON-NLS-1$
  private static final String ansiAmp = "&"; //$NON-NLS-1$
  private static final String xmlApos = "&apos;"; //$NON-NLS-1$
  private static final String ansiApos = "'"; //$NON-NLS-1$
  private static final String xmlQuot = "&quot;"; //$NON-NLS-1$
  private static final String ansiQuot = "\""; //$NON-NLS-1$
  private static final String xmlLt = "&lt;"; //$NON-NLS-1$
  private static final String ansiLt = "<"; //$NON-NLS-1$
  private static final String xmlGt = "&gt;"; //$NON-NLS-1$
  private static final String ansiGt = ">"; //$NON-NLS-1$

  private static String parseXmlPreDeclaredEntities(final String val) {
    final StringBuffer stringBuffer = new StringBuffer(val.length() + 1);
    int n = 0;
    int n2 = 0;
    final char c = '&';
    while ((n2 = val.indexOf(c, n)) != -1) {
      stringBuffer.append(val.substring(n, n2));
      n = n2;
      if (val.startsWith(AdminDataParser.xmlAmp, n2)) {
        stringBuffer.append(AdminDataParser.ansiAmp);
        n += AdminDataParser.xmlAmp.length();
        continue;
      }
      if (val.startsWith(AdminDataParser.xmlApos, n2)) {
        stringBuffer.append(AdminDataParser.ansiApos);
        n += AdminDataParser.xmlApos.length();
        continue;
      }
      if (val.startsWith(AdminDataParser.xmlQuot, n2)) {
        stringBuffer.append(AdminDataParser.ansiQuot);
        n += AdminDataParser.xmlQuot.length();
        continue;
      }
      if (val.startsWith(AdminDataParser.xmlLt, n2)) {
        stringBuffer.append(AdminDataParser.ansiLt);
        n += AdminDataParser.xmlLt.length();
        continue;
      }
      if (val.startsWith(AdminDataParser.xmlGt, n2)) {
        stringBuffer.append(AdminDataParser.ansiGt);
        n += AdminDataParser.xmlGt.length();
        continue;
      }
      stringBuffer.append(c);
      ++n;
    }
    if (n < val.length()) {
      stringBuffer.append(val.substring(n));
    }
    return stringBuffer.toString();
  }

  private String serverDomain = null;
  boolean parseServersDone = false;
  boolean parseGroupsDone = false;
  private ServerMap sm = null;
  boolean memberdata = false;

  private GroupMap gm = null;

  private final DominoConsoleRunner dc;

  public AdminDataParser(final DominoConsoleRunner dc) {
    this.dc = dc;
  }

  public String parseCanonicalName(final String val) {
    if (val == null) {
      return null;
    }
    final StringBuffer sb = new StringBuffer();
    int n = 0;
    int n2 = 0;
    while (true) {
      if ((n = val.indexOf('=', n2)) == -1) {
        if (n2 != 0) {
          break;
        }
        sb.append(val);
        break;
      }
      n2 = val.indexOf('/', n);
      if (n2 == -1) {
        sb.append(val.substring(n + 1).trim());
        break;
      }
      sb.append(val.substring(n + 1, n2 + 1).trim());
    }
    return sb.toString();
  }

  public boolean parseGroup(Vector<GroupMap> groups, String line) {
    boolean done = false;
    if ((line = line.trim()).startsWith(AdminDataParser.sGroupsBeg)) {
      if (groups == null) {
        groups = new Vector<>(20, 10);
      }
    } else if (line.startsWith(AdminDataParser.sGroupsEnd)) {
      done = true;
    } else if (!line.startsWith(AdminDataParser.sGroupInfoBeg)) {
      if (line.startsWith(AdminDataParser.sGroupInfoEnd)) {
        this.gm.getMembers().trimToSize();
        Collections.sort(this.gm.getMembers());
        if (groups.contains(this.gm)) {
          final int n = groups.indexOf(this.gm);
          groups.setElementAt(this.gm, n);
        } else {
          groups.addElement(this.gm);
        }
      } else if (!line.startsWith(AdminDataParser.sMembersBeg) && !line.startsWith(AdminDataParser.sMembersEnd)) {
        if (line.startsWith(AdminDataParser.sMemberDataBeg)) {
          this.memberdata = true;
        } else if (line.startsWith(AdminDataParser.sMemberDataEnd)) {
          this.memberdata = false;
        } else if (line.startsWith(AdminDataParser.sNameBeg) && line.endsWith(AdminDataParser.sNameEnd)) {
          final String string2 = line
              .substring(AdminDataParser.sNameBeg.length(), line.length() - AdminDataParser.sNameEnd.length()).trim();
          if (!this.memberdata) {
            final String groupName = AdminDataParser.parseXmlPreDeclaredEntities(string2);
            this.gm = new GroupMap(groupName, GroupMap.SERVER_GROUP);
            this.gm.setDomain(this.serverDomain);
          } else {
            final String string4 = this.parseCanonicalName(string2);
            this.gm.addMember(AdminDataParser.parseXmlPreDeclaredEntities(string4));
          }
        }
      }
    }
    return done;
  }

  public boolean parseServer(Vector<ServerMap> servers, String line) {
    boolean done = false;
    if ((line = line.trim()).startsWith(AdminDataParser.sServersBeg)) {
      servers = new Vector<>(100, 10);
      final int n = line.indexOf(AdminDataParser.sDomain);
      int n2 = 0;
      if (n != -1) {
        n2 = line.indexOf(AdminDataParser.ansiQuot, n + AdminDataParser.sDomain.length());
        this.serverDomain = n2 > 0 ? line.substring(n + AdminDataParser.sDomain.length(), n2).trim() : null;
      }
    } else if (line.startsWith(AdminDataParser.sServersEnd)) {
      done = true;
    } else if (line.startsWith(AdminDataParser.sServerInfoBeg)) {
      this.sm = new ServerMap();
      this.sm.setDomain(this.serverDomain);
      this.sm.setAdminServer(line.indexOf(AdminDataParser.sAdmin) > 0);
      this.sm.setDB2Server(line.indexOf(AdminDataParser.sDB2) > 0);
    } else if (line.startsWith(AdminDataParser.sServerInfoEnd)) {
      if (servers.contains(this.sm)) {
        final int n = servers.indexOf(this.sm);
        servers.setElementAt(this.sm, n);
      } else {
        servers.addElement(this.sm);
      }
    } else if (line.startsWith(AdminDataParser.sNameBeg) && line.endsWith(AdminDataParser.sNameEnd)) {
      final String string3 = line.substring(AdminDataParser.sNameBeg.length(), line.length() - AdminDataParser.sNameEnd.length())
          .trim();
      this.sm.setServerName(this.parseCanonicalName(string3));
    } else if (line.startsWith(AdminDataParser.sHostnameBeg) && line.endsWith(AdminDataParser.sHostnameEnd)) {
      String string4 = line.substring(AdminDataParser.sHostnameBeg.length(), line.length() - AdminDataParser.sHostnameEnd.length())
          .trim();
      if (string4 != null && string4.length() > 0) {
        final int n = 58;
        final int n3 = string4.indexOf(n);
        if (n3 != -1) {
          string4 = string4.substring(0, n3).trim();
        }
        this.sm.setHostname(string4, true);
      }
    } else if (line.startsWith(AdminDataParser.sPortBeg) && line.endsWith(AdminDataParser.sPortEnd)) {
      int n;
      final String string5 = line.substring(AdminDataParser.sPortBeg.length(), line.length() - AdminDataParser.sPortEnd.length())
          .trim();
      if (string5 != null && string5.length() > 0 && (n = Integer.parseInt(string5)) > 0) {
        this.sm.setPort(n);
      }
    } else if (line.startsWith(AdminDataParser.sClusterBeg) && line.endsWith(AdminDataParser.sClusterEnd)) {
      final String string6 = line
          .substring(AdminDataParser.sClusterBeg.length(), line.length() - AdminDataParser.sClusterEnd.length()).trim();
      if (string6 != null && string6.length() > 0) {
        this.sm.setClusterName(AdminDataParser.parseXmlPreDeclaredEntities(string6));
      }
    } else if (line.startsWith(AdminDataParser.sTitleBeg) && line.endsWith(AdminDataParser.sTitleEnd)) {
      final String string7 = line.substring(AdminDataParser.sTitleBeg.length(), line.length() - AdminDataParser.sTitleEnd.length())
          .trim();
      if (string7 != null && string7.length() > 0) {
        this.sm.setTitle(AdminDataParser.parseXmlPreDeclaredEntities(string7));
      }
    } else if (line.startsWith(AdminDataParser.sVersionBeg) && line.endsWith(AdminDataParser.sVersionEnd)) {
      final String string8 = line
          .substring(AdminDataParser.sVersionBeg.length(), line.length() - AdminDataParser.sVersionEnd.length()).trim();
      if (string8 != null) {
        this.sm.setVersion(AdminDataParser.parseXmlPreDeclaredEntities(string8));
      }
    } else if (line.startsWith(AdminDataParser.sOsBeg) && line.endsWith(AdminDataParser.sOsEnd)) {
      final String osType = line.substring(AdminDataParser.sOsBeg.length(), line.length() - AdminDataParser.sOsEnd.length()).trim();
      this.sm.setServerType(osType);
    }
    return done;
  }

  public boolean parseServersAndGroups(final Vector<ServerMap> vector, final Vector<GroupMap> groups, final String line) {
    if (!this.parseServersDone) {
      this.parseServersDone = this.parseServer(vector, line);
    } else {
      this.parseGroupsDone = this.parseGroup(groups, line);
    }
    return this.parseServersDone && this.parseGroupsDone;
  }

}
