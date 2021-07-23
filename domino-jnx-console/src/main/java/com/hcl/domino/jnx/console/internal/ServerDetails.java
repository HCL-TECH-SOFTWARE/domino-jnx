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
	public boolean isAdminServer() {
		return isAdminServer;
	}
	public void setAdminServer(boolean isAdminServer) {
		this.isAdminServer = isAdminServer;
	}
	
	@Override
	public boolean isDb2Server() {
		return isDb2Server;
	}
	
	public void setDb2Server(boolean isDb2Server) {
		this.isDb2Server = isDb2Server;
	}
	
	@Override
	public String getHostName() {
		return hostName;
	}
	
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	
	@Override
	public String getIpAddress() {
		return ipAddress;
	}
	
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	@Override
	public String getClusterName() {
		return clusterName;
	}
	
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	
	@Override
	public String getDomain() {
		return domain;
	}
	
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	@Override
	public String getServerName() {
		return serverName;
	}
	
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	
	@Override
	public String getOSName() {
		return osName;
	}
	
	public void setOSName(String osType) {
		this.osName = osType;
	}
	
	@Override
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	@Override
	public String getServerTitle() {
		return serverTitle;
	}
	
	public void setServerTitle(String serverTitle) {
		this.serverTitle = serverTitle;
	}
	
	@Override
	public String getServerVersion() {
		return serverVersion;
	}
	
	public void setServerVersion(String serverVersion) {
		this.serverVersion = serverVersion;
	}

	@Override
	public String toString() {
		return "ServerDetails [serverName=" + serverName + ", serverTitle=" + serverTitle + ", clusterName="
				+ clusterName + ", hostName=" + hostName + ", ipAddress=" + ipAddress + ", domain=" + domain
				+ ", serverVersion=" + serverVersion + ", osName=" + osName + ", isAdminServer=" + isAdminServer
				+ ", isDb2Server=" + isDb2Server + ", port=" + port + "]";
	}
	
	
}
