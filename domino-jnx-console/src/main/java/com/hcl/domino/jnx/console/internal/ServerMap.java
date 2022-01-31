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

import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.Collator;
import java.util.Locale;

/**
 * Container for all connection settings, the server socket, Domino version and
 * OS type.
 */
public class ServerMap {
  private static final Collator collate = Collator.getInstance(Locale.US);

  public static String computeUniqueName(final String serverName, final String domain) {
    return (serverName != null ? serverName : "") + (domain != null ? "(" + domain + ")" : "");
  }

  private int filterCriteria = 0;
  private String filterName;
  private Integer filterPid;
  private String sname;
  private String hostname;
  private String ipAddress;
  private String cluster;
  private String domain;
  private String title;
  private String version;
  private String stype;
  private int sport;
  private Socket ss;
  private String uname;
  private String passwd;
  private int indx;
  private boolean isActive;
  private int state;
  private long datetime;
  private int eventTypes;
  private boolean isAdminServer;
  private boolean isDb2Server;
  private boolean isLocal;
  private String controllerVersion;
  private boolean isSuperUser;
  private ObjectOutputStream oos;
  private boolean isFirewall;
  private String binderName;
  private String binderPort;
  private String serviceName;
  private String proxyName;

  private String proxyPort;

  public ServerMap() {
    this.sport = 2050;
    this.eventTypes = 63;
    this.indx = -1;
    this.state = 0;
    this.binderName = null;
    this.binderPort = null;
    this.serviceName = null;
    this.proxyName = null;
    this.proxyPort = null;
    this.isFirewall = false;
    this.oos = null;
    this.controllerVersion = null;
    this.isAdminServer = false;
    this.isDb2Server = false;
  }

  public ServerMap(final ServerMap serverMap) {
    this.sname = serverMap.sname != null ? serverMap.sname : null;
    this.hostname = serverMap.hostname != null ? serverMap.hostname : null;
    this.ipAddress = serverMap.ipAddress != null ? serverMap.ipAddress : null;
    this.cluster = serverMap.cluster != null ? serverMap.cluster : null;
    this.domain = serverMap.domain != null ? serverMap.domain : null;
    this.title = serverMap.title != null ? serverMap.title : null;
    this.version = serverMap.version != null ? serverMap.version : null;
    this.stype = serverMap.stype != null ? serverMap.stype : null;
    this.uname = serverMap.uname != null ? serverMap.uname : null;
    this.passwd = serverMap.passwd != null ? serverMap.passwd : null;
    this.sport = serverMap.sport;
    this.ss = serverMap.ss;
    this.indx = serverMap.indx;
    this.isActive = serverMap.isActive;
    this.state = serverMap.state;
    this.datetime = serverMap.datetime;
    this.eventTypes = serverMap.eventTypes;
    this.isAdminServer = serverMap.isAdminServer;
    this.isDb2Server = serverMap.isDb2Server;
    this.isLocal = serverMap.isLocal;
    this.controllerVersion = serverMap.controllerVersion;
    this.isFirewall = serverMap.isFirewall;
    this.isSuperUser = serverMap.isSuperUser;
    this.oos = serverMap.oos;
    this.serviceName = serverMap.serviceName != null ? serverMap.serviceName : null;
    this.binderName = serverMap.binderName != null ? serverMap.binderName : null;
    this.binderPort = serverMap.binderPort != null ? serverMap.binderPort : null;
    this.proxyName = serverMap.proxyName != null ? serverMap.proxyName : null;
    this.proxyPort = serverMap.proxyPort != null ? serverMap.proxyPort : null;
  }

  ServerMap(final String serverName) {
    this(serverName, 2050);
  }

  ServerMap(final String serverName, final int port) {
    this();
    this.sname = serverName;
    this.setHostname(serverName, true);
    this.sport = port;
  }

  void checkAddress() {
    try {
      this.ipAddress = InetAddress.getByName(this.hostname).getHostAddress();
    } catch (final UnknownHostException unknownHostException) {
      this.ipAddress = null;
    }
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    }
    final ServerMap serverMap = (ServerMap) object;
    return ServerMap.collate.equals(this.sname, serverMap.getServerName());
  }

  public String getBinderName() {
    return this.binderName;
  }

  public String getBinderPort() {
    return this.binderPort;
  }

  public String getClusterName() {
    return this.cluster;
  }

  public String getControllerVersion() {
    return this.controllerVersion;
  }

  long getDateTime() {
    return this.datetime;
  }

  public String getDomain() {
    return this.domain;
  }

  public int getDominoType() {
    int n = 0;
    if (this.isAdminServer) {
      n |= 1;
    }
    if (this.isDb2Server) {
      n |= 2;
    }
    return n;
  }

  int getEventTypes() {
    return this.eventTypes;
  }

  public int getFilterCriteria() {
    return this.filterCriteria;
  }

  public String getFilterName() {
    return this.filterName;
  }

  public Integer getFilterPid() {
    return this.filterPid;
  }

  public String getHostname() {
    return this.hostname;
  }

  int getIndex() {
    return this.indx;
  }

  String getIpAddress() {
    if (this.ipAddress == null && this.hostname != null) {
      this.checkAddress();
    }
    return this.ipAddress;
  }

  public ObjectOutputStream getObjectOutputStream() {
    return this.oos;
  }

  String getPassword() {
    return this.passwd;
  }

  public int getPort() {
    return this.sport;
  }

  public String getProxyName() {
    return this.proxyName;
  }

  public String getProxyPort() {
    return this.proxyPort;
  }

  public String getServerName() {
    return this.sname;
  }

  public String getServerType() {
    return this.stype;
  }

  public String getServiceName() {
    return this.serviceName;
  }

  Socket getSocket() {
    return this.ss;
  }

  public String getTitle() {
    return this.title;
  }

  public String getUniqueName() {
    return ServerMap.computeUniqueName(this.sname, this.domain);
  }

  String getUserName() {
    return this.uname;
  }

  public String getVersion() {
    return this.version;
  }

  boolean isActive() {
    return this.isActive;
  }

  public boolean isAdminServer() {
    return this.isAdminServer;
  }

  public boolean isDB2server() {
    return this.isDb2Server;
  }

  public boolean isDeleted() {
    return (this.state & 8) == 8;
  }

  public boolean isDisconnect() {
    return (this.state & 0x10) == 16;
  }

  public boolean isDominoRunning() {
    if (!this.isActive) {
      return false;
    }
    return (this.state & 2) == 2;
  }

  boolean isLocal() {
    return this.isLocal;
  }

  public boolean isSuperuser() {
    return this.isSuperUser;
  }

  public boolean isThisEventBlocked(final int n) {
    int n2 = 0;
    switch (n) {
      case 0: {
        n2 = 1;
        break;
      }
      case 1: {
        n2 = 2;
        break;
      }
      case 2: {
        n2 = 4;
        break;
      }
      case 3: {
        n2 = 8;
        break;
      }
      case 4: {
        n2 = 16;
        break;
      }
      case 5: {
        n2 = 32;
        break;
      }
      default: {
        return false;
      }
    }
    return (this.eventTypes & n2) <= 0;
  }

  public boolean isViaFirewall() {
    return this.isFirewall;
  }

  void setActive(final boolean active) {
    int n = 1;
    this.isActive = active;
    if (!this.isActive) {
      n |= 6;
    }
    this.setState(n, active);
  }

  void setAdminServer(final boolean b) {
    this.isAdminServer = b;
  }

  public void setBinderName(final String binderName) {
    this.binderName = binderName;
  }

  public void setBinderPort(final String binderPort) {
    this.binderPort = binderPort;
  }

  void setClusterName(final String cluster) {
    this.cluster = cluster;
  }

  public void setControllerVersion(final String controllerVersion) {
    this.controllerVersion = controllerVersion;
  }

  void setDateTime(final long datetime) {
    this.datetime = datetime;
  }

  void setDB2Server(final boolean b) {
    this.isDb2Server = b;
  }

  void setDomain(final String domain) {
    this.domain = domain;
  }

  public void setDominoType(final int n) {
    if ((n & 1) == 1) {
      this.setAdminServer(true);
    }
    if ((n & 2) == 2) {
      this.setDB2Server(true);
    }
  }

  void setEventTypes(int n) {
    if (n > 63) {
      int n2 = 0;
      if (((n &= 0x111111) & 1) == 1) {
        n2 |= 1;
      }
      if ((n & 0x10) == 16) {
        n2 |= 2;
      }
      if ((n & 0x100) == 256) {
        n2 |= 4;
      }
      if ((n & 0x1000) == 4096) {
        n2 |= 8;
      }
      if ((n & 0x10000) == 65536) {
        n2 |= 0x10;
      }
      if ((n & 0x100000) == 0x100000) {
        n2 |= 0x20;
      }
      n = n2;
    }
    this.eventTypes = n & 0x3F;
  }

  public void setFilterCriteria(final int filterCriteria) {
    this.filterCriteria = filterCriteria;
  }

  public void setFilterName(final String filterName) {
    this.filterName = filterName;
  }

  public void setFilterPid(final Integer filterPid) {
    this.filterPid = filterPid;
  }

  public void setHostname(final String hostName) {
    this.setHostname(hostName, false);
  }

  void setHostname(final String hostName, final boolean checkDNS) {
    this.hostname = hostName;
    if (checkDNS) {
      this.checkAddress();
    }
  }

  void setHostname(final String hostName, final String ipAddress) {
    this.hostname = hostName;
    this.ipAddress = ipAddress;
  }

  void setIndex(final int indx) {
    this.indx = indx;
  }

  void setIpAddress(final String ipAddress) {
    this.ipAddress = ipAddress;
  }

  void setLocal(final boolean b) {
    this.isLocal = b;
  }

  public void setObjectOutputStream(final ObjectOutputStream oos) {
    this.oos = oos;
  }

  public void setPassword(final String passwd) {
    this.passwd = passwd;
  }

  public void setPort(final int sport) {
    this.sport = sport;
  }

  public void setProxyName(final String proxyName) {
    this.proxyName = proxyName;
  }

  public void setProxyPort(final String proxyPort) {
    this.proxyPort = proxyPort;
  }

  void setServerName(final String sname) {
    this.sname = sname;
  }

  void setServerType(final String stype) {
    this.stype = stype;
  }

  public void setServiceName(final String serviceName) {
    this.serviceName = serviceName;
  }

  void setSocket(final Socket socket) {
    this.ss = socket;
  }

  void setState(final int n, final boolean set) {
    this.state = set ? (this.state |= n & 0x1F) : (this.state &= ~(n & 0x1F));
  }

  public void setSuperuser(final boolean b) {
    this.isSuperUser = b;
  }

  void setTitle(final String title) {
    this.title = title;
  }

  public void setUserName(final String uname) {
    this.uname = uname;
  }

  void setVersion(final String version) {
    this.version = version;
  }

  public void setViaFirewall(final boolean b) {
    this.isFirewall = b;
  }

  @Override
  public String toString() {
    return this.sname;
  }
}
