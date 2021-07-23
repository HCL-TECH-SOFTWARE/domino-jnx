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

    ServerMap(String serverName, int port) {
        this();
        this.sname = serverName;
        this.setHostname(serverName, true);
        this.sport = port;
    }

    ServerMap(String serverName) {
        this(serverName, 2050);
    }

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

    void setServerName(String sname) {
        this.sname = sname;
    }

    public String getServerName() {
        return this.sname;
    }

    void setClusterName(String cluster) {
        this.cluster = cluster;
    }

    public String getClusterName() {
        return this.cluster;
    }

    void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return this.domain;
    }

    public String getHostname() {
        return this.hostname;
    }

    public void setHostname(String hostName) {
        this.setHostname(hostName, false);
    }

    void setHostname(String hostName, boolean checkDNS) {
        this.hostname = hostName;
        if (checkDNS) {
            this.checkAddress();
        }
    }

    void checkAddress() {
        try {
            this.ipAddress = InetAddress.getByName(this.hostname).getHostAddress();
        }
        catch (UnknownHostException unknownHostException) {
            this.ipAddress = null;
        }
    }

    void setHostname(String hostName, String ipAddress) {
        this.hostname = hostName;
        this.ipAddress = ipAddress;
    }

    String getIpAddress() {
        if (this.ipAddress == null && this.hostname != null) {
            this.checkAddress();
        }
        return this.ipAddress;
    }

    void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return this.version;
    }

    void setServerType(String stype) {
        this.stype = stype;
    }

    public String getServerType() {
        return this.stype;
    }

    public void setPort(int sport) {
        this.sport = sport;
    }

    public int getPort() {
        return this.sport;
    }

    void setSocket(Socket socket) {
        this.ss = socket;
    }

    Socket getSocket() {
        return this.ss;
    }

    public void setUserName(String uname) {
        this.uname = uname;
    }

    String getUserName() {
        return this.uname;
    }

    public void setPassword(String passwd) {
        this.passwd = passwd;
    }

    String getPassword() {
        return this.passwd;
    }

    void setIndex(int indx) {
        this.indx = indx;
    }

    int getIndex() {
        return this.indx;
    }

    void setActive(boolean active) {
        int n = 1;
        this.isActive = active;
        if (!this.isActive) {
            n |= 6;
        }
        this.setState(n, active);
    }

    boolean isActive() {
        return this.isActive;
    }

    void setAdminServer(boolean b) {
        this.isAdminServer = b;
    }

    public boolean isAdminServer() {
        return this.isAdminServer;
    }

    void setDB2Server(boolean b) {
        this.isDb2Server = b;
    }

    public boolean isDB2server() {
        return this.isDb2Server;
    }

    void setLocal(boolean b) {
        this.isLocal = b;
    }

    boolean isLocal() {
        return this.isLocal;
    }

    int getEventTypes() {
        return this.eventTypes;
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

    void setState(int n, boolean set) {
        this.state = set ? (this.state |= n & 0x1F) : (this.state &= ~(n & 0x1F));
    }

    void setDateTime(long datetime) {
        this.datetime = datetime;
    }

    long getDateTime() {
        return this.datetime;
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

    public void setFilterCriteria(int filterCriteria) {
        this.filterCriteria = filterCriteria;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public void setFilterPid(Integer filterPid) {
        this.filterPid = filterPid;
    }

    public String getUniqueName() {
    	return ServerMap.computeUniqueName(this.sname, this.domain);
    }

    public static String computeUniqueName(String serverName, String domain) {
        return (serverName != null ? serverName : "") + (domain != null ? "(" + domain + ")" : "");
    }

    public void setControllerVersion(String controllerVersion) {
        this.controllerVersion = controllerVersion;
    }

    public String getControllerVersion() {
        return this.controllerVersion;
    }

    public ObjectOutputStream getObjectOutputStream() {
        return this.oos;
    }

    public void setObjectOutputStream(ObjectOutputStream oos) {
        this.oos = oos;
    }

    public boolean isSuperuser() {
        return this.isSuperUser;
    }

    public void setSuperuser(boolean b) {
        this.isSuperUser = b;
    }

    public String getBinderName() {
        return this.binderName;
    }

    public void setBinderName(String binderName) {
        this.binderName = binderName;
    }

    public String getBinderPort() {
        return this.binderPort;
    }

    public void setBinderPort(String binderPort) {
        this.binderPort = binderPort;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getProxyName() {
        return this.proxyName;
    }

    public void setProxyName(String proxyName) {
        this.proxyName = proxyName;
    }

    public String getProxyPort() {
        return this.proxyPort;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public boolean isViaFirewall() {
        return this.isFirewall;
    }

    public void setViaFirewall(boolean b) {
        this.isFirewall = b;
    }

    public void setDominoType(int n) {
        if ((n & 1) == 1) {
            this.setAdminServer(true);
        }
        if ((n & 2) == 2) {
            this.setDB2Server(true);
        }
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

    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        ServerMap serverMap = (ServerMap)object;
        return this.collate.equals(this.sname, serverMap.getServerName());
    }

    public String toString() {
        return this.sname;
    }

    public boolean isThisEventBlocked(int n) {
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

    public boolean isDominoRunning() {
        if (!this.isActive) {
            return false;
        }
        return (this.state & 2) == 2;
    }

    public boolean isDeleted() {
        return (this.state & 8) == 8;
    }

    public boolean isDisconnect() {
        return (this.state & 0x10) == 16;
    }

    public ServerMap(ServerMap serverMap) {
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
}

