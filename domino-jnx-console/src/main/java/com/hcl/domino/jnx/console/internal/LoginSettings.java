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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Container for login settings to a remote server
 */
public class LoginSettings {
  private static String lastUserName = System.getProperty("user.name");
  private static String lastBinderHostname;

  private static String lastBinderPort;
  private static String lastServiceName;
  private static String lastSocksHostname;
  private static String lastSocksPort;
  private static boolean lastUseProxy;
  private final DominoConsoleRunner dc;
  private final ServerMap sm;
  private String m_userName;
  private String m_password;
  private String m_serverName;

  private String m_serverPort;

  private String m_binderHost;
  private String m_binderPort;
  private String m_serviceName;
  private String m_socksHost;
  private String m_socksPort;
  private boolean m_useSocks;
  private boolean advancedLogin = false;

  public LoginSettings(final DominoConsoleRunner dominoConsole, final ServerMap sm, String[] serverNames, String[] serverPorts,
      String[] serviceNames, String serviceName, final int n, final boolean advancedLogin) {

    String tmpServiceName;
    Object object;

    this.dc = dominoConsole;
    this.sm = sm;
    this.advancedLogin = advancedLogin;

    if (serverNames == null) {
      serverNames = new String[1];
      serverPorts = new String[] { String.valueOf(2050) };
      serverNames[0] = "";
    }
    if (serviceNames == null) {
      serviceNames = new String[] { "" };
    }
    final String[] tmpServerPorts = new String[serverPorts.length];
    for (int i = 0; i < serverPorts.length; ++i) {
      tmpServerPorts[i] = serverPorts[i];
    }
    final String[] tmpServiceNames = new String[serviceNames.length];
    for (int i = 0; i < serviceNames.length; ++i) {
      tmpServiceNames[i] = serviceNames[i];
    }
    if (advancedLogin) {
      object = serverNames[0];
      if (object == null || ((String) object).length() == 0) {
        object = LoginSettings.lastBinderHostname;
      }
      this.m_binderHost = (String) object;

      object = serverPorts[0];
      if (object == null || ((String) object).length() == 0) {
        object = LoginSettings.lastBinderPort;
      }
      this.m_binderPort = (String) object;

      object = serverNames[1];
      if (object == null || ((String) object).length() == 0) {
        object = LoginSettings.lastSocksHostname;
      }
      this.m_socksHost = (String) object;

      object = serverPorts[1];
      if (object == null || ((String) object).length() == 0) {
        object = LoginSettings.lastSocksPort;
      }

      this.m_socksPort = (String) object;

      tmpServiceName = serviceNames[0];
      if (serviceName == null || serviceName.length() == 0) {
        serviceName = LoginSettings.lastServiceName;
      }

      this.m_serviceName = serviceName;

      this.m_useSocks = n == 0 ? LoginSettings.lastUseProxy : true;
      this.setSocksProxy(this.isSocksEnabled());
    } else {
      if (serviceName != null) {
        this.m_serverName = serviceName;
      } else if (serverNames != null && serverNames.length > 0) {
        this.m_serverName = serverNames[0];
      }

      this.m_serverPort = tmpServerPorts[0];
      tmpServiceName = tmpServiceNames[0];
    }
    if (!(tmpServiceName != null && tmpServiceName.length() != 0)) {
      tmpServiceName = LoginSettings.lastUserName;
    }

    this.m_userName = tmpServiceName;
  }

  private boolean advancedLogin() {
    return this.advancedLogin;
  }

  public String getBinderHost() {
    return this.m_binderHost;
  }

  public String getBinderPort() {
    return this.m_binderPort;
  }

  public int getBinderPortInt() {
    return this.getPortInt(this.m_binderPort);
  }

  public String getLogin() {
    return this.m_userName;
  }

  public String getPassword() {
    return this.m_password;
  }

  public String getPort() {
    return this.m_serverPort;
  }

  public int getPortInt() {
    return this.getPortInt(this.m_serverPort);
  }

  private int getPortInt(final String string) {
    int n = -1;
    if (string != null) {
      try {
        n = Integer.parseInt(string);
      } catch (final NumberFormatException numberFormatException) {
        n = -1;
      }
    }
    return n;
  }

  public String getServer() {
    return this.m_serverName;
  }

  public String getServiceName() {
    return this.m_serviceName;
  }

  public String getSocksHost() {
    return this.m_socksHost;
  }

  public String getSocksPort() {
    return this.m_socksPort;
  }

  public int getSocksPortInt() {
    return this.getPortInt(this.m_socksPort);
  }

  public boolean isDataMissing() {
    boolean bl = false;
    if (this.getLogin() == null || this.getLogin().length() == 0 || this.getPassword() == null
        || this.getPassword().length() == 0) {
      bl = true;
    }
    if (!bl) {
      if (!this.advancedLogin) {
        if (this.getServer() == null || this.getServer().length() == 0 || this.getPortInt() == -1) {
          bl = true;
        }
      } else if (this.getServiceName() == null || this.getServiceName().length() == 0 || this.getBinderHost() == null
          || this.getBinderHost().length() == 0 || this.getBinderPortInt() == -1) {
        bl = true;
      } else if (this.isSocksEnabled()
          && (this.getSocksHost() == null || this.getSocksHost().length() == 0 || this.getSocksPortInt() == -1)) {
        bl = true;
      }
    }
    if (bl) {
      final ResourceBundle resourceBundle = DominoConsoleRunner.getResourceBundle();
      this.dc.reportMessageDialog(this.sm, resourceBundle.getString("labelIncorrectMsg"),
          resourceBundle.getString("labelIncorrectHeader"));
    }
    return bl;
  }

  public boolean isSocksEnabled() {
    return this.m_useSocks;
  }

  private void saveOldValues() {
    LoginSettings.lastUserName = this.getLogin();
    if (!this.advancedLogin) {
      return;
    }
    LoginSettings.lastBinderHostname = this.getBinderHost();
    LoginSettings.lastBinderPort = this.getBinderPort();
    LoginSettings.lastServiceName = this.getServiceName();
    LoginSettings.lastSocksHostname = this.getSocksHost();
    LoginSettings.lastSocksPort = this.getSocksPort();
    LoginSettings.lastUseProxy = this.isSocksEnabled();
  }

  public void setLogin(final String userName) {
    if (userName != null && userName.length() > 0) {
      this.m_userName = userName;
    }
  }

  public void setPassword(final String password) {
    this.m_password = password;
  }

  private void setSocksProxy(final boolean on) {
  }

  /**
   * Validates the login settings
   * 
   * @return {@link Future} with true if successful, false if invalid
   */
  public Future<Boolean> validateValues() {
    final FutureTask<Boolean> validateTask = new FutureTask<>(() -> {
      if (!LoginSettings.this.isDataMissing()) {
        if (LoginSettings.this.advancedLogin()) {
          this.dc.reportStatusMessage(this.sm, DominoConsoleRunner.getResourceBundle().getString("msgValidate"));

          String binderHost = null;
          try {
            binderHost = LoginSettings.this.getBinderHost();
            InetAddress inetAddress = InetAddress.getByName(binderHost);
            if (LoginSettings.this.isSocksEnabled()) {
              binderHost = LoginSettings.this.getSocksHost();
              inetAddress = InetAddress.getByName(binderHost);
            }
            LoginSettings.this.dc.reportStatusMessage(this.sm, "");
            LoginSettings.this.saveOldValues();

            return true;
          } catch (final Exception e) {
            final ResourceBundle resourceBundle = DominoConsoleRunner.getResourceBundle();
            final Object[] params = new Object[] { binderHost };
            final String string2 = e instanceof UnknownHostException
                ? MessageFormat.format(resourceBundle.getString("msgUnknownHost"), params)
                : e instanceof SecurityException ? MessageFormat.format(resourceBundle.getString("msgSecurityError"), params)
                    : e.getMessage();
            LoginSettings.this.dc.reportStatusMessage(this.sm, "ERROR: " + string2);

            return false;
          }

        } else {
          return true;
        }
      } else {
        return false;
      }
    });
    new Thread(validateTask).start();
    return validateTask;
  }

}
