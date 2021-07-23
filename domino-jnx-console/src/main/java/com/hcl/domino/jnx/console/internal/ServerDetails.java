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

import com.hcl.domino.jnx.console.IServerDetails;

/**
 * Implementation of {@link IServerDetails} with information
 * about a Domino server and its host OS.
 */
public class ServerDetails implements IServerDetails {
  private String serverName;
  private String serverTitle;
  private String clusterName;
  private String hostName;
  private String ipAddress;
  private String domain;
  private String serverVersion;
  private String osName;
  private boolean isAdminServer;
  private boolean isDb2Server;
  private int port;

  @Override
  public String getClusterName() {
    return this.clusterName;
  }

  @Override
  public String getDomain() {
    return this.domain;
  }

  @Override
  public String getHostName() {
    return this.hostName;
  }

  @Override
  public String getIpAddress() {
    return this.ipAddress;
  }

  @Override
  public String getOSName() {
    return this.osName;
  }

  @Override
  public int getPort() {
    return this.port;
  }

  @Override
  public String getServerName() {
    return this.serverName;
  }

  @Override
  public String getServerTitle() {
    return this.serverTitle;
  }

  @Override
  public String getServerVersion() {
    return this.serverVersion;
  }

  @Override
  public boolean isAdminServer() {
    return this.isAdminServer;
  }

  @Override
  public boolean isDb2Server() {
    return this.isDb2Server;
  }

  public void setAdminServer(final boolean isAdminServer) {
    this.isAdminServer = isAdminServer;
  }

  public void setClusterName(final String clusterName) {
    this.clusterName = clusterName;
  }

  public void setDb2Server(final boolean isDb2Server) {
    this.isDb2Server = isDb2Server;
  }

  public void setDomain(final String domain) {
    this.domain = domain;
  }

  public void setHostName(final String hostName) {
    this.hostName = hostName;
  }

  public void setIpAddress(final String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public void setOSName(final String osType) {
    this.osName = osType;
  }

  public void setPort(final int port) {
    this.port = port;
  }

  public void setServerName(final String serverName) {
    this.serverName = serverName;
  }

  public void setServerTitle(final String serverTitle) {
    this.serverTitle = serverTitle;
  }

  public void setServerVersion(final String serverVersion) {
    this.serverVersion = serverVersion;
  }

  @Override
  public String toString() {
    return "ServerDetails [serverName=" + this.serverName + ", serverTitle=" + this.serverTitle + ", clusterName="
        + this.clusterName + ", hostName=" + this.hostName + ", ipAddress=" + this.ipAddress + ", domain=" + this.domain
        + ", serverVersion=" + this.serverVersion + ", osName=" + this.osName + ", isAdminServer=" + this.isAdminServer
        + ", isDb2Server=" + this.isDb2Server + ", port=" + this.port + "]";
  }

}
