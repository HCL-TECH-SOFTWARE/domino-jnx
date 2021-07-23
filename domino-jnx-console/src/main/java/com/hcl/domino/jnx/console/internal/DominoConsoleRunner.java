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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

import com.hcl.domino.jnx.console.IConsoleCallback.DominoStatus;

/**
 * This class encapsulates the connection to local or remote server controllers.
 * We currently support one connection per DominoConsoleRunner, but multiple
 * DominoConsoleRunner instances can be created in parallel.
 */
public abstract class DominoConsoleRunner {
	private static final int BADINDEX = -1;
	private static ResourceBundle resourceBundle;

	private String localHostName = null;
	private Vector<ServerMap> smap;
	private InetAddress localInetAddress = null;
	private String localHostAddress = null;
	private Hashtable<String,ServerMap> serversByName;
	private Hashtable<String,GroupMap> grpsList;
	private Hashtable<String,ObjectStack> addOnDataMap;
	private Hashtable<String,ObjectStack> addOnCmdMap;
	private Vector<ControllerInfo> activeServerConnections;
	private Vector<GroupMap> groupMaps;
	private ObjectStack commandMap;
	private int curController;
	private Collator collate = Collator.getInstance(Locale.US);
	int totalActiveConnections = 0;
	private boolean refreshServerListCmd = false;
	private boolean exitInProgress = false;
	private boolean m_exitRequested;
	private ControllerWriter controllerWriter;

	static ResourceBundle getResourceBundle() {
		if (resourceBundle == null) {
			try {
				resourceBundle = ResourceBundle.getBundle(DominoConsoleRunner.class.getName(), Locale.US);
			}
			catch (MissingResourceException e) {
				throw new RuntimeException("Resource bundle not found", e);
			}
		}
		return resourceBundle;
	}

	public DominoConsoleRunner(ServerMap sm, boolean isAdvanced) {
		this.init(sm);

		this.connectServer(sm, isAdvanced);
	}

	private void init(ServerMap sm) {
		SSL.setClassLoader(this.getClass());
		System.setProperty("sun.net.inetaddr.ttl", "0");

		this.serversByName = new Hashtable<>(100, 10.0f);
		this.grpsList = new Hashtable<>(20, 5.0f);
		this.addOnDataMap = new Hashtable<>(3, 1.0f);
		this.addOnCmdMap = new Hashtable<>(3, 1.0f);
		this.activeServerConnections = new Vector<>(100, 10);
		this.smap = new Vector<>(100, 10);
		this.groupMaps = new Vector<>(20, 10);
		this.commandMap = new ObjectStack(20);

		this.curController = BADINDEX;

		try {
			this.localHostName = InetAddress.getLocalHost().getHostName();
			this.getLocalInetAddress();
		}
		catch (UnknownHostException unknownHostException) {
			unknownHostException.printStackTrace();
		}

		this.startIOThreads();
	}

	public void startIOThreads() {
		this.controllerWriter = new ControllerWriter(this);
		this.controllerWriter.setDaemon(true);
		this.controllerWriter.start();
	}

	boolean addServerToMapList(ServerMap serverMap) {
		if (serverMap == null || serverMap.getServerName() == null || serverMap.getServerName().length() == 0) {
			return false;
		}
		if (!this.smap.contains(serverMap)) {
			this.smap.addElement(serverMap);
			this.serversByName.put(serverMap.getServerName(), serverMap);
		}
		return true;
	}

	Hashtable<String,ServerMap> getServersByName() {
		return this.serversByName;
	}

	InetAddress getLocalInetAddress() {
		if (this.localInetAddress==null) {
			try {
				this.localInetAddress = InetAddress.getByName(this.localHostName);
			}
			catch (UnknownHostException unknownHostException) {
				this.localInetAddress = null;
			}
		}
		return this.localInetAddress;
	}

	private String getLocalHostAddress() {
		if (this.localHostAddress==null) {
			InetAddress localInetAddress = getLocalInetAddress();
			if (localInetAddress!=null) {
				this.localHostAddress = localInetAddress.getHostAddress();
			}
			else {
				this.localHostAddress = null;
			}
		}
		return this.localHostAddress;
	}

	Vector<ServerMap> getServerMaps() {
		return this.smap;
	}

	Vector<GroupMap> getGroupMaps() {
		return this.groupMaps;
	}

	ObjectStack getCommandMap() {
		return this.commandMap;
	}

	Hashtable<String,GroupMap> getGrpsList() {
		return this.grpsList;
	}

	void connectServer(ServerMap serverMap, boolean isAdvanced) {
		ConnectController connectController;
		String usr = null;

		if (isAdvanced || serverMap != null && serverMap.isViaFirewall()) {
			String serviceName = null;
			String binderName = null;
			String binderPort = null;
			String proxyName = null;
			String proxyPort = null;

			if (serverMap != null) {
				serviceName = serverMap.getServiceName();
				binderName = serverMap.getBinderName();
				binderPort = serverMap.getBinderPort();
				proxyName = serverMap.getProxyName();
				proxyPort = serverMap.getProxyPort();
				usr = serverMap.getUserName();
			}
			connectController = new ConnectController(this, usr, serviceName, binderName, binderPort, proxyName, proxyPort);
		} else {
			String host = this.localHostName;
			String pwd = null;
			int port = 2050;

			if (serverMap != null) {
				port = serverMap.getPort();
				host = serverMap.getHostname();
				usr = serverMap.getUserName();
				pwd = serverMap.getPassword();
			}
			connectController = new ConnectController(this, host, port, usr, pwd);
		}

//		bind socket to local host
//		connectController.setBindIpAddress(this.getLocalInetAddress());

		connectController.start();
	}

	ServerMap[] getServerMapsByHostname(String hostName, int port) {
		String hostAddress = null;
		try {
			hostAddress = InetAddress.getByName(hostName).getHostAddress();
		}
		catch (UnknownHostException unknownHostException) {
			return null;
		}
		return this.getServerMapsByHostAddress(hostAddress, port);
	}

	ServerMap[] getServerMapsByHostAddress(String hostAddress, int port) {
		if (hostAddress == null) {
			return null;
		}

		Vector<ServerMap> matches = new Vector<>(3, 3);
		Enumeration<ServerMap> enumeration = this.smap.elements();

		while (enumeration.hasMoreElements()) {
			ServerMap serverMap = enumeration.nextElement();
			String currIpAddress = serverMap.getIpAddress();

			if (currIpAddress != null && currIpAddress.equals(hostAddress) && serverMap.getPort() == port) {
				matches.add(serverMap);
			}
			serverMap = null;
		}

		if (matches.isEmpty()) {
			return null;
		}
		return matches.toArray(new ServerMap[matches.size()]);
	}

	void addNewServer(ServerMap serverMap) {
		ControllerInfo controllerInfo;
		int indexPos = this.activeServerConnections.size();

		if (serverMap.getIndex() >= 0) {
			controllerInfo = this.activeServerConnections.elementAt(serverMap.getIndex());
		} else {
			controllerInfo = new ControllerInfo();
			this.activeServerConnections.insertElementAt(controllerInfo, indexPos);
			serverMap.setIndex(indexPos);
		}
		controllerInfo.setServerName(serverMap.getServerName());
		controllerInfo.setIndex(this.smap.indexOf(serverMap));
		this.switchServer(serverMap);

		this.reportConsoleInitialized(serverMap);
	}

	void switchServer(ServerMap serverMap) {
		if (serverMap == null) {
			this.curController = BADINDEX;
			return;
		}

		this.curController = serverMap.getIndex();
	}

	public boolean isExitInProgress() {
		return this.exitInProgress;
	}

	public interface IExitCallback {

		void exitDone();

	}

	public boolean isExitRequested() {
		return m_exitRequested;
	}
	
	public synchronized void exit(IExitCallback callback) {
		if (!m_exitRequested) {
			m_exitRequested = true;
			new ExitThread(callback).start();
		}
	}

	class ExitThread extends Thread {
		boolean lsrunning = false;
		private IExitCallback callback;

		public ExitThread(IExitCallback callback) {
			super("ExitThread");
			this.callback = callback;
		}

		@Override
		public void run() {
			Enumeration<ServerMap> enumeration = DominoConsoleRunner.this.smap.elements();

			while (enumeration.hasMoreElements()) {
				ServerMap serverMap = enumeration.nextElement();

				DominoConsoleRunner.this.disconnectServer(serverMap.getServerName(), false);
			}

			while (DominoConsoleRunner.this.totalActiveConnections > 0) {
				try {
					Thread.sleep(100L);
				}
				catch (InterruptedException interruptedException) {}
			}

			DominoConsoleRunner.this.controllerWriter.setStop();

			this.callback.exitDone();
		}
	}

	synchronized void disconnectServer(String serverName, boolean bl) {
		Enumeration<ServerMap> enumeration = this.smap.elements();
		ServerMap serverMap = null;
		ServerMap serverMap2 = null;
		ServerMap serverMap3 = null;
		int n = BADINDEX;
		int n2 = BADINDEX;

		while (enumeration.hasMoreElements()) {
			serverMap = enumeration.nextElement();

			if (!serverMap.isActive()) { 
				continue;
			}

			if (this.collate.equals(serverMap.getServerName(), serverName)) {
				serverMap2 = serverMap;
				n = serverMap.getIndex();
				if (n2 < 0) {
					continue;
				}
				break;
			}
			n2 = serverMap.getIndex();
			serverMap3 = serverMap;
			if (n < 0) {
				continue;
			}
		}
		if (n == BADINDEX) {
			return;
		}

		if (bl) {
			serverMap2.setActive(false);
			try {
				if (serverMap2.getSocket() != null) {
					serverMap2.getSocket().close();
					serverMap2.setSocket(null);
					serverMap2.setObjectOutputStream(null);
				}
			}
			catch (IOException iOException) {
				iOException.printStackTrace();
				// empty catch block
			}
			--this.totalActiveConnections;
			serverMap2.setState(16, false);
			serverMap2.setDateTime(Calendar.getInstance().getTimeInMillis());
		} else {
			this.sendCommand(serverName, "#disconnect");
			serverMap2.setState(16, true);
		}

		if (n == this.curController) {
			this.switchServer(serverMap3);
		}
	}

	private void sendCommand(String serverName, String cmd) {
		int n = BADINDEX;
		if (!this.serversByName.containsKey(serverName)) {
			Object[] params = new Object[]{serverName};
			String msg = MessageFormat.format(getResourceBundle().getString("msgSrvrNotConnect"), params);

			throw new IllegalStateException(msg);
		}
		n = this.serversByName.get(serverName).getIndex();
		this.sendCommand(cmd, n, false, true);
	}

	public void sendCommand(String command) {
		this.sendCommand(command, this.curController, true);
	}

	void sendCommand(String command, int n, boolean bl) {
		this.sendCommand(command, n, false, bl);
	}

	private void sendCommand(String command, int n, boolean bl, boolean bl2) {
		if (n == BADINDEX) {
			throw new IllegalStateException("No active connects");
		}

		CommandMap commandMap = new CommandMap();
		commandMap.setCommand(command + "\n");
		commandMap.setGroup(bl);
		commandMap.setDestination(null); //grpOrSrvrName 
		commandMap.setIndex(this.activeServerConnections.elementAt(n).getIndex());
		commandMap.setType(1);
		try {
			this.commandMap.pushObject(commandMap);
		}
		catch (InterruptedException interruptedException) {
			// empty catch block
		}
	}

	void updateProcInfo(ServerMap sm, String string) {
	}

	void updateAdminInfo(ServerMap sm, String aInfoStr) {
		Vector<String> serverAdministrators = new Vector<>(20, 5);
		Vector<String> restrictedAdministrators = new Vector<>(10, 5);

		if (aInfoStr != null && aInfoStr.length() > 0) {
			int n = aInfoStr.length();
			int n2 = 0;

			while (true) {
				int n3;
				if ((n3 = aInfoStr.indexOf(10, n2)) == -1) {
					n3 = n;
				}
				if (n2 >= n3) {
					break;
				}
				String string2 = aInfoStr.substring(n2, n3).trim();
				n2 = n3 + 1;
				n3 = string2.indexOf(44);
				if (n3 == -1) {
					n3 = string2.length();
				}
				if (string2.regionMatches(n3 + 1, "true", 0, "true".length())) {
					restrictedAdministrators.add(string2.substring(0, n3));
					continue;
				}
				serverAdministrators.add(string2.substring(0, n3));
			}
		}

		adminInfosReceived(serverAdministrators, restrictedAdministrators);
	}

	void updateServiceInfo(ServerMap sm, String string) {
	}

	ObjectStack getAddOnCmdMap(String string) {
		ObjectStack objectStack = this.addOnCmdMap.get(string);
		if (objectStack == null) {
			objectStack = new ObjectStack(50);
			this.addOnCmdMap.put(string, objectStack);
		}
		return objectStack;
	}

	ObjectStack getAddOnDataMap(String string) {
		ObjectStack objectStack = this.addOnDataMap.get(string);
		if (objectStack == null) {
			objectStack = new ObjectStack(50);
			this.addOnDataMap.put(string, objectStack);
		}
		return objectStack;
	}

	private ServerMap getServerMap(String serverName, Hashtable<String,ServerMap> lkMap) {
		ServerMap serverMap = null;
		if (lkMap == null) {
			lkMap = this.serversByName;
		}
		if ((serverMap = lkMap.get(serverName)) == null) {
			Enumeration<String> keys = lkMap.keys();

			while (keys.hasMoreElements()) {
				String currServerName = keys.nextElement();
				if (!this.collate.equals(currServerName, serverName)) {
					continue;
				}
				serverMap = lkMap.get(currServerName);
				break;
			}
		}
		return serverMap;
	}

	ServerMap getServerMap(String serverName) {
		return this.getServerMap(serverName, null);
	}

	/**
	 * Returns an array of server maps where the server name matches the
	 * hostname
	 * 
	 * @return server maps or null if no matches
	 */
	ServerMap[] getMapsWithServerNameAsHostname() {
		Vector<ServerMap> serverMaps = new Vector<>(5, 3);

		Enumeration<ServerMap> enumeration = this.smap.elements();

		while (enumeration.hasMoreElements()) {
			ServerMap currServerMap = enumeration.nextElement();

			if (!currServerMap.getServerName().equals(currServerMap.getHostname())) {
				continue;
			}

			serverMaps.add(currServerMap);
		}
		if (serverMaps.size() == 0) {
			return null;
		}
		return serverMaps.toArray(new ServerMap[serverMaps.size()]);
	}

	void getServerListByDomain(Hashtable<String,ServerMap> retServerMapByName, String domainFilter) {
		if (domainFilter == null || retServerMapByName == null) {
			return;
		}

		Enumeration<String> enumeration = this.serversByName.keys();
		while (enumeration.hasMoreElements()) {
			String currServerName = enumeration.nextElement();
			ServerMap serverMap = this.serversByName.get(currServerName);

			if (serverMap.getDomain() == null || !this.collate.equals(serverMap.getDomain(), domainFilter)) {
				continue;
			}
			retServerMapByName.put(currServerName, serverMap);
		}
	}

	protected abstract void reportServerInfosUpdated(ServerMap existingServer, ServerMap update);

	synchronized void serverResolved(ServerMap sm) {
		this.smap.clear();
		this.smap.add(sm);

		this.serversByName.clear();
		this.serversByName.put(sm.getServerName(), sm);
	}

	synchronized void updateServerList(Vector<ServerMap> serverUpdates) {
		Hashtable<String,ServerMap> serversByNameInDomsin = null;
		Object[] serverMapsWithEqualNameAndHostname = getMapsWithServerNameAsHostname();

		for (int b = 0; b < serverUpdates.size(); b++) {
			ServerMap currServerUpdate = serverUpdates.elementAt(b);

			if (b == 0 && this.refreshServerListCmd) {
				serversByNameInDomsin = new Hashtable<>(20, 5.0F);
				getServerListByDomain(serversByNameInDomsin, currServerUpdate.getDomain());
			} 
			String str = currServerUpdate.getServerName();

			ServerMap existingServer = getServerMap(str, serversByNameInDomsin);
			if (existingServer != null) {
				if (currServerUpdate.getDomain() != null && existingServer.getDomain() != null && 
						!this.collate.equals(existingServer.getDomain(), currServerUpdate.getDomain())) {

					str = currServerUpdate.getUniqueName();
					existingServer = getServerMap(currServerUpdate.getUniqueName(), serversByNameInDomsin);
				}
			} else {
				str = ServerMap.computeUniqueName(currServerUpdate.getServerName(), currServerUpdate.getDomain());

				existingServer = getServerMap(str, serversByNameInDomsin);

				if (existingServer != null) {
					currServerUpdate.getUniqueName();
				}
			}

			if (existingServer != null && this.refreshServerListCmd) {
				serversByNameInDomsin.remove(existingServer.getServerName());
			}

			if (existingServer == null) {
				if (currServerUpdate.getPort() > 0) {
					ServerMap[] serverMaps = getServerMapsByHostAddress(currServerUpdate.getIpAddress(), currServerUpdate.getPort());

					if (serverMaps != null && serverMaps.length > 0) {
						for (int b1 = 0; b1 < serverMaps.length; b1++) {
							existingServer = serverMaps[b1];

							if (existingServer.getHostname().equalsIgnoreCase(existingServer.getServerName())) {
								break; 
							}

							existingServer = null;
						}

						if (existingServer == null) {
							for (int b1 = 0; b1 < serverMaps.length; b1++) {
								existingServer = serverMaps[b1];

								if ((existingServer.getDomain() == null || existingServer.getDomain().length() == 0) && existingServer.getServerName().equalsIgnoreCase(currServerUpdate.getServerName())) {
									break;
								}

								existingServer = null;
							}
						}
					} 
				}

				//try to find the server via ip address
				if (existingServer == null && serverMapsWithEqualNameAndHostname != null) {
					String str1 = null, str2 = null;
					for (int b1 = 0; b1 < serverMapsWithEqualNameAndHostname.length; b1++) {
						existingServer = (ServerMap)serverMapsWithEqualNameAndHostname[b1];
						str1 = currServerUpdate.getIpAddress();
						str2 = existingServer.getIpAddress();
						if (str2.equals(str1)) {
							break;
						} 
						existingServer = null;
					} 
				} 
			}

			if (existingServer != null) {
				this.reportServerInfosUpdated(existingServer, currServerUpdate);

				if (existingServer.isDeleted()) {
					existingServer.setState(8, false);
				}

				String serverName = existingServer.getServerName();
				if (existingServer.getVersion() != null || currServerUpdate.getVersion() != null) {
					if (existingServer.getVersion() == null || currServerUpdate
					.getVersion() == null || 
					!this.collate.equals(existingServer.getVersion(), currServerUpdate.getVersion())) {
						existingServer.setVersion(currServerUpdate.getVersion());

					}
				}  
				if (existingServer.getServerType() != null || currServerUpdate.getServerType() != null) {
					if (existingServer.getServerType() == null || currServerUpdate
					.getServerType() == null || 
					!this.collate.equals(existingServer.getServerType(), currServerUpdate.getServerType())) {
						existingServer.setServerType(currServerUpdate.getServerType());
					}
				}
				if (existingServer.getClusterName() != null || currServerUpdate.getClusterName() != null) {
					if (existingServer.getClusterName() == null || currServerUpdate
					.getClusterName() == null || 
					!this.collate.equals(existingServer.getClusterName(), currServerUpdate.getClusterName())) {
						existingServer.setClusterName(currServerUpdate.getClusterName());
					}
				}

				if (existingServer.getHostname() != null || currServerUpdate.getHostname() != null) {
					if (existingServer.getHostname() == null || currServerUpdate
							.getHostname() == null || 
							!this.collate.equals(existingServer.getHostname(), currServerUpdate.getHostname())) {

						if (!existingServer.isActive()) {
							existingServer.setHostname(currServerUpdate.getHostname(), currServerUpdate.getIpAddress());
						}
					}
				}

				if (existingServer.getPort() > 0 || currServerUpdate.getPort() > 0) {
					if (existingServer.getPort() == 0 || currServerUpdate
							.getPort() == 0 || existingServer
							.getPort() != currServerUpdate.getPort()) {
						if (!existingServer.isActive()) {
							existingServer.setPort(currServerUpdate.getPort());
						}
					}
				}

				if (currServerUpdate.isAdminServer() != existingServer.isAdminServer()) {
					existingServer.setAdminServer(currServerUpdate.isAdminServer());
				}

				if (currServerUpdate.isDB2server() != existingServer.isDB2server()) {
					existingServer.setDB2Server(currServerUpdate.isDB2server());
				} 
				if (currServerUpdate.getDomain() != null) {
					if (existingServer.getDomain() == null) {
						existingServer.setDomain(currServerUpdate.getDomain());
					}
				}
				if (currServerUpdate.getControllerVersion() != null) {
					existingServer.setControllerVersion(currServerUpdate.getControllerVersion());
				}
				if (!this.collate.equals(existingServer.getServerName(), currServerUpdate.getServerName()) || 
						!existingServer.getServerName().equals(currServerUpdate.getServerName())) {
					this.serversByName.put(currServerUpdate.getServerName(), existingServer);

					existingServer.setServerName(currServerUpdate.getServerName());
					this.serversByName.remove(serverName);
				} 
				existingServer.setTitle(currServerUpdate.getTitle());
			} 
		} 
		if (this.refreshServerListCmd) {
			if (serversByNameInDomsin != null && !serversByNameInDomsin.isEmpty()) {
				for (Enumeration<String> keys = serversByNameInDomsin.keys(); keys.hasMoreElements(); ) {
					String currServerName = keys.nextElement();
					serversByNameInDomsin.remove(currServerName);
				}
			}  
			this.refreshServerListCmd = false;
			serversByNameInDomsin = null;
		}
	}

	synchronized void updateGroupList(Vector<GroupMap> groups) {
	}

	public void refreshServers() {
		DominoConsoleRunner.this.refreshServerListCmd = true;
		DominoConsoleRunner.this.sendCommand("#refresh servers");
	}

	public void sendBroadcastMessage(String msg) {
		if (msg != null && msg.length() > 0) {
			String cmd = "#broadcast " + msg.toString();
			this.sendCommand(cmd);
		}
	}

	protected abstract void reportConsoleInitialized(ServerMap sm);

	protected abstract void reportConsoleConnectFailed(ServerMap sm, String msg, Exception exception);

	protected abstract void reportStatusMessage(ServerMap sm, String msg);

	protected abstract void reportMessageDialog(ServerMap sm, String msg, String title);

	protected abstract boolean requestLoginSettings(ServerMap sm, LoginSettings loginSettings);

	protected abstract String requestInputDialog(ServerMap sm, String msg, String title, String[] values, String initialSelection);

	protected abstract String requestPasswordDialog(ServerMap sm, String msg, String title);

	protected abstract void closeOpenPasswordDialog(ServerMap sm);

	protected abstract void closeOpenPrompt(ServerMap sm);

	protected abstract <T> T requestPrompt(ServerMap sm, String msg, String title, T[] options);

	protected abstract void consoleMessageReceived(ServerMap sm, ConsoleLine line);

	protected abstract void adminInfosReceived(Vector<String> serverAdministrators,
			Vector<String> restrictedAdministrators);

	protected abstract void reportDominoStatus(ServerMap sm, DominoStatus status);

}
