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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * The ConnectController establishes the connection with the server controller
 * port and spawn a new ControllerReader thread to read the console data line by
 * line.
 */
public class ConnectController extends Thread {
  private static final String SB_SUBSCRIBER = "Subscriber"; //$NON-NLS-1$

  private static final String SB_IDENTIFY_TYPE = "ServiceType"; //$NON-NLS-1$
  private static final String SB_IDENTIFY_SVC = "ServiceName"; //$NON-NLS-1$
  private static final String SB_IDENTIFY_USER = "ServiceUser"; //$NON-NLS-1$
  private static final String SB_IDENTIFY_HOST = "ServiceHost"; //$NON-NLS-1$
  private static final String SB_IDENTIFY_OK = "OK"; //$NON-NLS-1$
  private static final String SB_IDENTIFY_FOUND = "Found"; //$NON-NLS-1$
  private static final String SB_IDENTIFY_NOT_FOUND = "NotFound"; //$NON-NLS-1$
  private static final String SB_UNKNOWN_CMD = "UnknownCMD"; //$NON-NLS-1$

  private static final String NS_ALLUSERS = "*"; //$NON-NLS-1$

  private ServerMap serverMapResolved;
  /** registered server map for the server hostname */
  private ServerMap serverMapInDominoConsole = null;
  private final DominoConsoleRunner dc;

  private Socket sokt;
  private LoginSettings loginSettings;
  private String usr = null;
  private String pwd = null;
  private String host = null;
  private String serverName = null;
  private int port = 0;
  private InetAddress bindAddress = null;
  private int passwdCntr = 0;
  private int promptCntr = 0;
  private String serviceName = null;
  private String binderName = null;
  private String binderPort = null;
  private String proxyName = null;
  private String proxyPort = null;
  private boolean advancedLogin = false;
  private final Vector<ServerMap> smap;
  private final ResourceBundle res;
  private BufferedReader inp = null;
  private OutputStreamWriter out = null;

  public ConnectController(final DominoConsoleRunner dominoConsole, final String host, final int port, final String usr,
      final String pwd) {
    this(dominoConsole, null, host, port, usr, pwd);
  }

  public ConnectController(final DominoConsoleRunner dominoConsole, final String srvr, final String host, final int port,
      final String usr, final String pwd) {
    super("ConnectController"); //$NON-NLS-1$

    this.dc = dominoConsole;
    if (srvr != null) {
      this.serverName = srvr;
    }
    if (host != null) {
      this.host = host;
    }
    if (port > 0) {
      this.port = port;
    }
    if (usr != null) {
      this.usr = usr;
    }
    if (pwd != null) {
      this.pwd = pwd;
    }
    this.smap = this.dc.getServerMaps();
    this.res = DominoConsoleRunner.getResourceBundle();
    this.advancedLogin = false;
  }

  public ConnectController(final DominoConsoleRunner dominoConsole, final String usr, final String serviceName,
      final String binderName,
      final String binderPort, final String proxyName, final String proxyPort) {
    super("ConnectController"); //$NON-NLS-1$

    this.dc = dominoConsole;
    this.smap = this.dc.getServerMaps();
    this.res = DominoConsoleRunner.getResourceBundle();
    this.usr = usr;
    this.serviceName = serviceName;
    this.binderName = binderName;
    this.binderPort = binderPort;
    this.proxyName = proxyName;
    this.proxyPort = proxyPort;
    this.advancedLogin = true;
  }

  /**
   * Displays a login dialog to let the user enter login settings
   * 
   * @return 0 if login info has been entered, 1 if incomplete
   */
  private int askForLoginInfo() {
    String[] serverNames, serverPorts, serviceNames;
    final Enumeration<ServerMap> serverMapsEnum = this.smap.elements();
    int b1 = 0;
    int b2 = 0;

    int port = 2050;
    String serviceName = null;

    if (this.advancedLogin) {
      serverNames = new String[2];
      serverNames[0] = this.binderName;
      serverNames[1] = this.proxyName;

      serverPorts = new String[2];
      serverPorts[0] = this.binderPort;
      serverPorts[1] = this.proxyPort;

      serviceNames = new String[1];
      serviceNames[0] = this.usr != null ? this.usr : ""; //$NON-NLS-1$
      serviceName = this.serviceName;
      port = this.proxyName == null || this.proxyName.length() == 0 ? Character.MIN_VALUE : '\001';
    } else {
      final String[] tmpServerNames = new String[this.smap.size() + 1];
      final String[] tmpServerPorts = new String[this.smap.size() + 1];
      final String[] tmpServiceNames = new String[this.smap.size() + 1];

      while (serverMapsEnum.hasMoreElements()) {
        final ServerMap serverMap = serverMapsEnum.nextElement();

        if (!serverMap.isActive() && !serverMap.isDeleted()) {
          tmpServerNames[b1] = serverMap.getServerName();
          tmpServerPorts[b1] = String.valueOf(serverMap.getPort());
          tmpServiceNames[b1++] = serverMap.getUserName();
          if (this.host != null && this.host.equalsIgnoreCase(serverMap.getHostname())) {
            serviceName = serverMap.getServerName();
          }
        }
      }
      if (this.smap.size() == 0 || b1 == 0) {
        tmpServerNames[b1] = this.host != null ? this.host : ""; //$NON-NLS-1$
        tmpServerPorts[b1] = String.valueOf(2050);
        tmpServiceNames[b1++] = this.usr != null ? this.usr : ""; //$NON-NLS-1$
      }
      serverNames = new String[b1];
      serverPorts = new String[b1];
      serviceNames = new String[b1];
      while (b2 < b1) {
        serverNames[b2] = tmpServerNames[b2];
        serverPorts[b2] = tmpServerPorts[b2];
        serviceNames[b2] = tmpServiceNames[b2];
        b2++;
      }
    }
    this.loginSettings = new LoginSettings(this.dc, this.serverMapResolved, serverNames, serverPorts, serviceNames, serviceName,
        port, this.advancedLogin);
    this.loginSettings.setLogin(this.usr);

    if (this.dc.requestLoginSettings(this.serverMapResolved, this.loginSettings)) {
      this.usr = this.loginSettings.getLogin();
      if (this.usr == null) {
        this.usr = ""; //$NON-NLS-1$
      }

      this.pwd = this.loginSettings.getPassword();
      if (this.pwd == null) {
        this.pwd = ""; //$NON-NLS-1$
      }

      if (!this.advancedLogin) {
        this.serverName = this.loginSettings.getServer();

        final Hashtable<String, ServerMap> serversByName = this.dc.getServersByName();

        if (serversByName.containsKey(this.serverName)) {
          final ServerMap serverMap = serversByName.get(this.serverName);
          this.host = serverMap.getHostname();
        } else {
          this.host = this.serverName;
        }
        this.port = this.loginSettings.getPortInt();
      }
      return 0;
    }
    return 1;
  }

  private boolean authenticateBinderService() {
    while (true) {
      String string;
      if ((string = this.readFromBinder()) == null) {
        try {
          this.sokt.close();
        } catch (final IOException iOException) {
          // empty catch block
        }
        return false;
      }
      if (string.equalsIgnoreCase(ConnectController.SB_IDENTIFY_TYPE)) {
        this.writeToBinder(ConnectController.SB_SUBSCRIBER);
        continue;
      }
      if (string.equalsIgnoreCase(ConnectController.SB_IDENTIFY_SVC)) {
        this.writeToBinder(this.loginSettings.getServiceName());
        continue;
      }
      if (string.equalsIgnoreCase(ConnectController.SB_IDENTIFY_USER)) {
        this.writeToBinder(ConnectController.NS_ALLUSERS);
        continue;
      }
      if (string.equalsIgnoreCase(ConnectController.SB_IDENTIFY_HOST)) {
        this.writeToBinder(this.sokt.getLocalAddress().getHostAddress() + ":" + this.sokt.getLocalPort()); //$NON-NLS-1$
        continue;
      }
      if (string.equalsIgnoreCase(ConnectController.SB_IDENTIFY_OK)) {
        continue;
      }
      if (string.equalsIgnoreCase(ConnectController.SB_IDENTIFY_NOT_FOUND)) {
        return false;
      }
      if (string.regionMatches(true, 0, ConnectController.SB_IDENTIFY_FOUND, 0, ConnectController.SB_IDENTIFY_FOUND.length())) {
        int n;
        final String string2 = string.substring(ConnectController.SB_IDENTIFY_FOUND.length() + 1);
        if (string2.length() > 0) {
          final int n2 = string2.indexOf(58);
          if (n2 != -1) {
            this.host = string2.substring(0, n2);
            this.port = Integer.parseInt(string2.substring(n2 + 1));
          } else {
            this.host = string2;
            this.port = 2050;
          }
        }
        return (n = this.checkIfExists(this.host)) != 1;
      }
      this.writeToBinder(ConnectController.SB_UNKNOWN_CMD);
    }
  }

  /**
   * Checks if we have a server map for this hostname
   * 
   * @param hostName server hostname
   * @return 0 if server map could be found, 1 if it's already connected and
   *         focus, 2 if connected and not focused, 3
   */
  private int checkIfExists(final String hostName) {
    int b = 0;

    // try to find a registered ServerMap for the hostname
    if (this.serverName != null) {
      final Hashtable<String, ServerMap> serversByName = this.dc.getServersByName();
      if (serversByName.containsKey(this.serverName)) {
        this.serverMapInDominoConsole = serversByName.get(this.serverName);
      }
    }

    if (this.serverMapInDominoConsole == null || this.serverMapInDominoConsole.getPort() != this.port) {
      final Object[] serverMaps = this.dc.getServerMapsByHostname(hostName, this.port);

      if (serverMaps == null || serverMaps.length == 0) {
        this.serverMapInDominoConsole = null;
      } else if (serverMaps.length == 1) {
        this.serverMapInDominoConsole = (ServerMap) serverMaps[0];
      } else {
        final String[] serversChoices = new String[serverMaps.length];

        for (int b1 = 0; b1 < serverMaps.length; b1++) {
          this.serverMapInDominoConsole = (ServerMap) serverMaps[b1];
          serversChoices[b1] = this.serverMapInDominoConsole.getServerName() + ", " + this.serverMapInDominoConsole.getPort() + ", " //$NON-NLS-1$ //$NON-NLS-2$
              + this.serverMapInDominoConsole.getDomain();
          this.serverMapInDominoConsole = null;
        }

        final Object[] params = { serversChoices[0] };

        final String selectedServer = this.dc.requestInputDialog(
            this.serverMapResolved,
            MessageFormat.format(this.res.getString("msgSelectOneServer"), params), //$NON-NLS-1$
            this.res.getString("msgSelectAServer"), serversChoices, serversChoices[0]); //$NON-NLS-1$

        if (selectedServer == null) {
          this.serverMapInDominoConsole = (ServerMap) serverMaps[0];
        } else {
          for (int b2 = 0; b2 < serverMaps.length; b2++) {
            if (selectedServer.equals(serversChoices[b2])) {
              this.serverMapInDominoConsole = (ServerMap) serverMaps[b2];
              break;
            }
          }
        }
      }
    }
    if (this.serverMapInDominoConsole != null) {
      if (this.serverMapInDominoConsole.isActive()) {
        b = 1;
      } else if (this.serverMapInDominoConsole.getIndex() < 0) {
        b = 3;
      } else {
        b = 2;
      }
    }

    if (b == 0 || this.serverMapInDominoConsole == null) {
      this.serverMapResolved = new ServerMap(hostName, this.port);
    } else {
      // copy all values from registered server map object
      this.serverMapResolved = new ServerMap(this.serverMapInDominoConsole);
      this.serverMapResolved.setPort(this.port);
    }
    this.serverMapResolved.setUserName(this.usr);
    this.serverMapResolved.setPassword(this.pwd);
    return b;
  }

  private ServerMap findServerMap() {
    ServerMap sm = this.serverMapInDominoConsole;
    if (sm == null && this.serverName != null) {
      sm = this.dc.getServerMap(this.serverName);
    }
    if (sm == null && this.host != null) {
      final ServerMap[] serverMaps = this.dc.getServerMapsByHostname(this.host, this.port);
      if (serverMaps != null && serverMaps.length > 0) {
        sm = serverMaps[0];
      }
    }

    if (sm == null) {
      sm = this.serverMapResolved;
    }
    return sm;
  }

  private String readFromBinder() {
    try {
      String line = this.inp.readLine();
      if (line != null) {
        line = line.trim();
      }
      return line;
    } catch (final IOException iOException) {
      iOException.printStackTrace();
      return null;
    }
  }

  private String readFromServer() {
    String string = null;
    try {
      string = this.inp.readLine();
    } catch (final IOException iOException) {
      iOException.printStackTrace();
    }
    return string;
  }

  private boolean readUserAndAuthenticate() {
    byte retriesLeft = 3;
    boolean success = false;

    while (true) {
      this.writeToServer("#UI " + this.serverMapResolved.getUserName() + "," + this.serverMapResolved.getPassword() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

      String line = this.readFromServer();
      if (line == null) {
        success = false;
        this.dc.reportMessageDialog(this.serverMapResolved, this.res.getString("msgConnectionTimedOut"), //$NON-NLS-1$
            this.res.getString("msgError")); //$NON-NLS-1$
        this.out = null;
        break;
      }

      if (line.equals("VALID_USER")) { //$NON-NLS-1$
        int i = 0, j = 0;
        this.writeToServer("#ST\n"); //$NON-NLS-1$
        line = this.readFromServer();
        if (line == null) {
          break;
        }
        i = line.indexOf(':');
        if (i == -1) {
          i = line.length();
        }
        this.serverMapResolved.setServerName(line.substring(0, i));
        i++;
        if (i < line.length()) {
          j = line.indexOf(':', i);
          if (j == -1) {
            j = line.length();
          }
          this.serverMapResolved.setServerType(line.substring(i, j));
          i = j + 1;
        }
        if (i < line.length()) {
          j = line.indexOf(':', i);
          if (j == -1) {
            j = line.length();
          }
          this.serverMapResolved.setTitle(ConsoleUtils.changeColonToNative(line.substring(i, j)));
          i = j + 1;
        }
        if (i < line.length()) {
          j = line.indexOf(':', i);
          if (j == -1) {
            j = line.length();
          }
          this.serverMapResolved.setDomain(line.substring(i, j));
        }
        this.writeToServer("#CNTR\n"); //$NON-NLS-1$
        line = this.readFromServer();
        if (line == null) {
          break;
        }
        i = line.indexOf(':');
        this.passwdCntr = Integer.parseInt(line.substring(0, i));
        this.promptCntr = Integer.parseInt(line.substring(i + 1));
        this.writeToServer("#VERSION 2.0\n"); //$NON-NLS-1$
        line = this.readFromServer();
        if (line == null) {
          break;
        }
        if (line.equals("WRONG_PASSWORD")) { //$NON-NLS-1$
          this.serverMapResolved.setControllerVersion("1.0"); //$NON-NLS-1$
        } else {
          this.serverMapResolved.setControllerVersion(line);
        }
        this.writeToServer("#TIMESTAMP\n"); //$NON-NLS-1$
        line = this.readFromServer();
        if (line == null) {
          break;
        }
        if (line.equals("WRONG_PASSWORD") || line //$NON-NLS-1$
            .equals("BAD_COMMAND")) { //$NON-NLS-1$
          this.serverMapResolved.setDateTime(Calendar.getInstance().getTimeInMillis());
        } else {
          this.serverMapResolved.setDateTime(Long.parseLong(line));
        }
        this.writeToServer("#CHKACCESS\n"); //$NON-NLS-1$
        line = this.readFromServer();
        if (line == null) {
          break;
        }
        this.serverMapResolved.setSuperuser(line.equals("FULLACCESS")); //$NON-NLS-1$
        success = true;
        break;
      }
      if (line.equals("WRONG_PASSWORD") || line.equals("MAXED_TRIALS")) { //$NON-NLS-1$ //$NON-NLS-2$
        if (line.equals("MAXED_TRIALS") || retriesLeft-- == 0) { //$NON-NLS-1$
          this.dc.reportMessageDialog(this.serverMapResolved, this.res.getString("msgMaxPasswordTries"), //$NON-NLS-1$
              this.res.getString("msgError"));
          success = false;
          break;
        }
        final Object[] params = { this.serverMapResolved.getUserName() };
        final String msg = MessageFormat.format(this.res.getString("msgWrongPassword"), params); //$NON-NLS-1$
        final String title = this.res.getString("msgPasswdUser"); //$NON-NLS-1$

        this.pwd = this.dc.requestPasswordDialog(this.serverMapResolved, msg, title);
        if (this.pwd == null || this.pwd.length() == 0) {
          success = false;
          break;
        }
        this.serverMapResolved.setPassword(this.pwd);
        continue;
      }
      if (line.equals("NOT_REG_ADMIN")) { //$NON-NLS-1$
        final Object[] params = { this.serverMapResolved.getUserName(), this.serverMapResolved.getServerName() };
        final String msg = MessageFormat.format(this.res.getString("msgUsrNotAdmin"), params); //$NON-NLS-1$

        this.dc.reportMessageDialog(this.serverMapResolved, msg, this.res.getString("msgError")); //$NON-NLS-1$
        success = false;
        break;
      }
      if (line.equals("NOT_LOCAL_ADMIN")) { //$NON-NLS-1$
        this.dc.reportMessageDialog(this.serverMapResolved, this.res.getString("msgNotLocalAdmin"), this.res.getString("msgError")); //$NON-NLS-1$ //$NON-NLS-2$

        success = false;
        break;
      }
      if (line.equals("RESTRICTED_ADMIN")) { //$NON-NLS-1$
        final Object[] params = { this.serverMapResolved.getUserName(), this.serverMapResolved.getServerName() };
        final String msg = MessageFormat.format(this.res.getString("msgAdminRestricted"), params); //$NON-NLS-1$

        this.dc.reportMessageDialog(this.serverMapResolved, msg, this.res.getString("msgError")); //$NON-NLS-1$
        success = false;
        break;
      }
      if (line.equals("MAXED_OUT")) { //$NON-NLS-1$
        this.dc.reportMessageDialog(this.serverMapResolved, this.res.getString("msgMaxConnections"), //$NON-NLS-1$
            this.res.getString("msgError"));  //$NON-NLS-1$
        success = false;
        break;
      }
    }
    if (this.out != null) {
      this.writeToServer("#EXIT\n");  //$NON-NLS-1$
    }
    this.inp = null;
    this.out = null;
    return success;
  }

  @Override
  public void run() {
    int i = 0;

    if ((this.host == null || this.usr == null || this.pwd == null || this.port == 0) &&
        this.askForLoginInfo() > 0) {
      // missing required login infos
      return;
    }

    if (this.advancedLogin) {
      final Object[] params = { this.loginSettings.getBinderHost() };
      this.setStatusMessage(MessageFormat.format(this.res.getString("msgBinderStatus"), params)); //$NON-NLS-1$
    } else {
      final Object[] params = { this.host };
      this.setStatusMessage(MessageFormat.format(this.res.getString("msgConnectStatus"), params)); //$NON-NLS-1$
      i = this.checkIfExists(this.host);
      final Object[] params2 = { this.serverMapResolved.getServerName() };

      if (i == 1) {
        // already connected and focused
        this.setStatusMessage(""); //$NON-NLS-1$
        this.showMessageDialog(MessageFormat.format(this.res.getString("msgSrvrConnected"), params2), //$NON-NLS-1$
            this.res.getString("msgError"));  //$NON-NLS-1$

        this.dc.switchServer(this.serverMapResolved);
        return;
      }
      this.setStatusMessage(MessageFormat.format(this.res.getString("msgConnectStatus"), params2)); //$NON-NLS-1$
    }
    String host = null;
    int port = 0;

    if (this.advancedLogin) {
      host = this.loginSettings.getBinderHost();
      port = this.loginSettings.getBinderPortInt();

      if (this.loginSettings.isSocksEnabled()) {
        ConsoleUtils.enableSocksProxy(this.loginSettings.getSocksHost().trim(),
            this.loginSettings.getSocksPort().trim());
      }
    } else {
      host = this.serverMapResolved.getHostname();
      port = this.serverMapResolved.getPort();
    }

    try {
      this.sokt = SSL.getClientSocket(host, port, this.bindAddress, 0);
    } catch (final Exception exception) {
      String msg;

      if (exception instanceof java.net.BindException) {
        final Object[] params = { this.dc.getLocalInetAddress().toString() };
        msg = MessageFormat.format(this.res.getString("msgCouldNotBind"), params); //$NON-NLS-1$
      } else if (exception instanceof java.net.NoRouteToHostException) {
        final Object[] params = { host };
        msg = MessageFormat.format(this.res.getString("msgNoRouteToHost"), params); //$NON-NLS-1$
      } else if (exception instanceof java.net.UnknownHostException) {
        final Object[] params = { host };
        msg = MessageFormat.format(this.res.getString("msgUnknownHost"), params); //$NON-NLS-1$
      } else {
        final Object[] params = { host, String.valueOf(port), this.advancedLogin ? "Service Binder" : "Server Controller" };
        msg = MessageFormat.format(this.res.getString("msgHostOrPortWrong"), params); //$NON-NLS-1$
      }

      this.dc.reportMessageDialog(this.serverMapResolved, msg, this.res.getString("msgError")); //$NON-NLS-1$
      this.dc.reportStatusMessage(this.serverMapResolved, ""); //$NON-NLS-1$

      this.dc.reportConsoleConnectFailed(this.serverMapResolved, msg, exception);

      if (this.advancedLogin &&
          this.loginSettings.isSocksEnabled()) {
        ConsoleUtils.disableSocksProxy();
      }

      return;
    }

    if (this.advancedLogin &&
        this.loginSettings.isSocksEnabled()) {
      ConsoleUtils.disableSocksProxy();
    }

    if (this.sokt == null) {
      this.dc.reportStatusMessage(this.serverMapResolved, "Connection failed to create socket ...");
      return;
    }
    try {
      this.inp = new BufferedReader(new InputStreamReader(this.sokt.getInputStream(), StandardCharsets.UTF_8));
      this.out = new OutputStreamWriter(this.sokt.getOutputStream(), StandardCharsets.UTF_8);
    } catch (final IOException iOException) {
      iOException.printStackTrace();

      this.dc.reportStatusMessage(this.serverMapResolved, ""); //$NON-NLS-1$
      try {
        this.sokt.close();
      } catch (final IOException iOException1) {
      }
      return;
    }
    if (this.advancedLogin) {
      final Object[] params = { this.loginSettings.getServiceName() };
      this.dc.reportStatusMessage(this.serverMapResolved,
          MessageFormat.format(this.res.getString("msgBinderServiceStatus"), params)); //$NON-NLS-1$

      if (!this.authenticateBinderService()) {
        this.dc.reportStatusMessage(this.serverMapResolved, ""); //$NON-NLS-1$
        this.dc.reportMessageDialog(this.serverMapResolved, this.res.getString("msgNoServiceAtThisTime"), //$NON-NLS-1$
            this.res.getString("msgError"));  //$NON-NLS-1$

        try {
          this.sokt.close();
        } catch (final IOException iOException) {
        }
        return;
      }
      host = this.loginSettings.getBinderHost();
      port = this.loginSettings.getBinderPortInt();
    }
    final Object[] params = { this.serverMapResolved.getUserName() };
    this.dc.reportStatusMessage(this.serverMapResolved, MessageFormat.format(this.res.getString("msgAuthStatus"), params)); //$NON-NLS-1$
    boolean bool = false;
    if (i == 0 || this.serverMapResolved.getServerName().equalsIgnoreCase(this.serverMapResolved.getHostname())) {
      bool = true;
    }
    final boolean success = this.readUserAndAuthenticate();
    this.dc.reportStatusMessage(this.serverMapResolved, ""); //$NON-NLS-1$
    if (!success) {
      try {
        this.sokt.close();
      } catch (final IOException iOException) {
      }
      return;
    }
    if (!bool && this.serverMapInDominoConsole != null && this.serverMapResolved != null && this.serverMapResolved
        .getDomain() != null && this.serverMapInDominoConsole.getDomain() != null &&
        !this.serverMapInDominoConsole.getDomain().equalsIgnoreCase(this.serverMapResolved.getDomain())) {

      boolean bool2 = false;
      if (this.serverMapResolved.getServerName().equalsIgnoreCase(this.serverMapInDominoConsole.getServerName())) {
        if (this.dc.getServerMap(this.serverMapResolved.getUniqueName()) != null) {
          bool2 = true;
        }
      } else if (this.dc.getServerMap(this.serverMapResolved.getUniqueName()) == null) {
        bool2 = true;
      }
      if (bool2) {
        final Object[] params2 = { this.serverMapInDominoConsole.getServerName(), this.serverMapInDominoConsole.getDomain(),
            this.serverMapInDominoConsole.getHostname() };
        final String msg = MessageFormat.format(this.res.getString("msgDomainWrong"), params2); //$NON-NLS-1$

        this.dc.reportMessageDialog(this.serverMapResolved, msg, this.res.getString("msgError")); //$NON-NLS-1$
        this.dc.reportStatusMessage(this.serverMapResolved, ""); //$NON-NLS-1$

        try {
          this.sokt.close();
        } catch (final IOException iOException) {
        }
        return;
      }
    }
    final Vector<ServerMap> vector = new Vector<>(1);
    vector.add(this.serverMapResolved);

    this.dc.serverResolved(this.serverMapResolved);
    this.dc.updateServerList(vector);

    if (this.serverMapInDominoConsole == null) {
      this.serverMapInDominoConsole = this.dc.getServerMap(this.serverMapResolved.getServerName());
    }

    this.serverMapInDominoConsole.setServerName(this.serverMapResolved.getServerName());
    this.serverMapInDominoConsole.setServerName(this.serverMapResolved.getServerName());

    this.serverMapInDominoConsole.setSocket(this.sokt);
    this.serverMapInDominoConsole.setActive(true);
    this.serverMapInDominoConsole.setPort(this.port);
    this.serverMapInDominoConsole.setUserName(this.usr);
    this.serverMapInDominoConsole.setPassword(this.pwd);
    this.serverMapInDominoConsole.setViaFirewall(this.advancedLogin);

    if (this.advancedLogin) {
      this.serverMapInDominoConsole.setServiceName(this.loginSettings.getServiceName());
      this.serverMapInDominoConsole.setBinderName(this.loginSettings.getBinderHost());
      this.serverMapInDominoConsole.setBinderPort(this.loginSettings.getBinderPort());
      this.serverMapInDominoConsole.setProxyName(this.loginSettings.getSocksHost());
      this.serverMapInDominoConsole.setProxyPort(this.loginSettings.getSocksPort());
    }
    this.serverMapInDominoConsole.setDateTime(this.serverMapResolved.getDateTime());

    ConnectController.this.dc.addNewServer(this.serverMapInDominoConsole);

    this.dc.totalActiveConnections++;

    final ControllerReader controllerReader = new ControllerReader(this.dc, this.serverMapInDominoConsole);
    controllerReader.setPasswordCounter(this.passwdCntr);
    controllerReader.setPromptCounter(this.promptCntr);
    controllerReader.start();
  }

  public void setBindIpAddress(final InetAddress bindAddress) {
    this.bindAddress = bindAddress;
  }

  private void setStatusMessage(final String msg) {
    final ServerMap sm = this.findServerMap();

    if (sm != null) {
      this.dc.reportStatusMessage(sm, msg);
    }
  }

  private void showMessageDialog(final String msg, final String title) {
    final ServerMap sm = this.findServerMap();

    if (sm != null) {
      this.dc.reportMessageDialog(sm, msg, title);
    }
  }

  private void writeToBinder(final String line) {
    try {
      this.out.write(line + "\n"); //$NON-NLS-1$
      this.out.flush();
    } catch (final IOException iOException) {
      iOException.printStackTrace();
    }
  }

  private void writeToServer(final String string) {
    try {
      this.out.write(string);
      this.out.flush();
    } catch (final IOException iOException) {
      iOException.printStackTrace();
    }
  }
}
