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

import java.text.Collator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Container to define a group of servers, currently unused in this
 * console API implementation
 */
public class GroupMap {
  public static int SERVER_GROUP = 0;
  public static int PRIVATE_GROUP = 1;
  public static int TEMP_GROUP = 2;
  public static String PRIVATE_DOMAIN = "PrivateILI";
  private static final Collator collate = Collator.getInstance(Locale.US);

  private String groupName;
  private String domain;
  private int groupType;
  private final Vector<String> servers;

  public GroupMap() {
    this("");
  }

  public GroupMap(final String grpName) {
    this(grpName, GroupMap.PRIVATE_GROUP);
  }

  public GroupMap(final String grpName, final int groupType) {
    this.groupName = grpName;
    this.groupType = groupType;
    this.servers = new Vector<>(5, 5);
    this.domain = GroupMap.PRIVATE_DOMAIN;
  }

  public void addMember(final String server) {
    if (!this.servers.contains(server)) {
      this.servers.add(server);
    }
  }

  public void addMembers(final Vector<String> servers) {
    for (int i = 0; i < servers.size(); ++i) {
      this.addMember(servers.elementAt(i));
    }
  }

  public void deleteMember(final String server) {
    if (this.servers.contains(server)) {
      this.servers.remove(server);
    }
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    }
    final GroupMap groupMap = (GroupMap) object;
    return GroupMap.collate.equals(this.groupName, groupMap.getGroupName())
        && GroupMap.collate.equals(this.domain, groupMap.getDomain());
  }

  public String getDomain() {
    return this.domain;
  }

  public String getGroupName() {
    return this.groupName;
  }

  public Vector<String> getMembers() {
    return this.servers;
  }

  public Object[] getMembersAsArray() {
    return this.servers.toArray();
  }

  public String getMembersAsString() {
    final String string = this.servers.toString();
    return string.substring(1, string.length() - 1);
  }

  public boolean isPrivateGroup() {
    return this.groupType == GroupMap.PRIVATE_GROUP;
  }

  public boolean isServerGroup() {
    return this.groupType == GroupMap.SERVER_GROUP;
  }

  public boolean isTempGroup() {
    return this.groupType == GroupMap.TEMP_GROUP;
  }

  public String makeDomainGroupName() {
    return this.groupName + "(" + this.domain + ")";
  }

  public void readFromString(final String string) {
    String string2;
    final String string3 = ",";
    final StringTokenizer stringTokenizer = new StringTokenizer(string, string3, true);
    if (stringTokenizer.hasMoreTokens() && !(string2 = stringTokenizer.nextToken(string3).trim()).equals(string3)) {
      this.groupName = string2;
      if (stringTokenizer.hasMoreTokens()) {
        stringTokenizer.nextToken();
      }
    }
    if (stringTokenizer.hasMoreTokens() && !(string2 = stringTokenizer.nextToken(string3).trim()).equals(string3)) {
      this.domain = string2;
      if (stringTokenizer.hasMoreTokens()) {
        stringTokenizer.nextToken();
      }
    }
    if (stringTokenizer.hasMoreTokens() && !(string2 = stringTokenizer.nextToken(string3).trim()).equals(string3)) {
      this.groupType = Integer.parseInt(string2);
      if (stringTokenizer.hasMoreTokens()) {
        stringTokenizer.nextToken();
      }
    }
    while (stringTokenizer.hasMoreTokens()) {
      string2 = stringTokenizer.nextToken(string3).trim();
      if (string2.equals(string3)) {
        continue;
      }
      this.addMember(string2);
    }
  }

  public void setDomain(final String string) {
    this.domain = string;
  }

  public void setGroupTypePrivate() {
    this.groupType = GroupMap.PRIVATE_GROUP;
  }

  public void setGroupTypeServer() {
    this.groupType = GroupMap.SERVER_GROUP;
  }

  public void setGroupTypeTemp() {
    this.groupType = GroupMap.TEMP_GROUP;
  }

  @Override
  public String toString() {
    return this.groupName;
  }

  public String writeToString() {
    final StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append(this.groupName + "," + (this.domain == null ? "" : this.domain) + "," + this.groupType);
    for (int i = 0; i < this.servers.size(); ++i) {
      stringBuffer.append("," + this.servers.elementAt(i));
    }
    return stringBuffer.toString();
  }

}
