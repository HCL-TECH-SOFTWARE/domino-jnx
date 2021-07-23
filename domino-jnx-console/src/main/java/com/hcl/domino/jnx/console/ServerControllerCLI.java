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
package com.hcl.domino.jnx.console;

import java.net.InetAddress;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Simple CLI application that establishes a connection via the server controller port
 * 
 * @author Karsten Lehmann
 */
public class ServerControllerCLI {

	public static void main(String[] args) {

		try {
			String adminUser = null;
			String adminPassword = null;
			String hostName = null;
			String portStr = null;
			String command = null;
			String serverIdPwd = null;

			if (args.length == 0) {
				System.out.println("HCL Domino Server Controller CLI");
				System.out.println("================================");
				System.out.println("Parameters:");
				System.out.println("-host dominoserver1.com  -  Domino server hostname (default: localhost)");
				System.out.println("-port 2050  -  optional Domino Server Controller port (default: 2050)");
				System.out.println("-user \"Admin User\"  -  name of admin user (default: localAdmin)");
				System.out.println("-pass myAdminPassword  -  internet password of admin user (default: localAdmin)");
				System.out.println("-cmd \"show tasks\"  -  optional command to send; console disconnects afterwards");
				System.out.println("-serveridpwd myServerIdPassword  -  pass an optional server.id for server startup");
				System.out.println();
				System.out.println("Use user/password localAdmin to connect locally on the server.");
				System.out.println("To disconnect from the server, type \"close\"");
				System.out.println();
				System.out.println("Special Server Controller commands:");
				System.out.println("#broadcast Hello world  -  sends a broadcast message to other console consumers");
				System.out.println("#start domino  -  starts the Domino server");
				System.out.println("quit  -  stops the Domino server");
				System.out.println("#kill domino  -  kills the Domino server");
				System.out.println("$dir *.nsf  -  executes a shell command in the data directory (full access only)");
				System.out.println("#show processes  -  shows running server processes");
				System.out.println("#show users  -  shows other Server Controller consumers");
				System.exit(0);
			}

			for (int i=0; i<args.length; i++) {
				String currArg = args[i];

				if ("-host".equals(currArg)) {
					if ((i+1) < args.length) {
						hostName = args[++i];
					}
				}
				else if ("-port".equals(currArg)) {
					if ((i+1) < args.length) {
						portStr = args[++i];
					}
				}
				else if ("-user".equals(currArg) || "-username".equals(currArg)) {
					if ((i+1) < args.length) {
						adminUser = args[++i];
					}
				}
				else if ("-pass".equals(currArg) || "-password".equals(currArg)) {
					if ((i+1) < args.length) {
						adminPassword = args[++i];
					}
				}
				else if ("-cmd".equals(currArg)) {
					if ((i+1) < args.length) {
						command = args[++i];
					}
				}
				else if ("-serveridpwd".equals(currArg)) {
					if ((i+1) < args.length) {
						serverIdPwd = args[++i];
					}
				}
			}

			String fCommand = command;
			String fServerIdPwd = serverIdPwd;

			//default to local server if missing hostname/user/password
			
			if (portStr==null) {
				portStr = "2050";
			}
			int portInt = Integer.parseInt(portStr);

			if (hostName==null || hostName.length()==0) {
				hostName = InetAddress.getLocalHost().getHostName();
			}

			if (adminUser==null || adminUser.length()==0) {
				adminUser = "localAdmin";
			}

			if (adminPassword==null || adminPassword.length()==0) {
				adminPassword = "localAdmin";
			}

			IDominoConsoleCreator consoleCreator = new DominoConsoleCreator();

			//console to send commands entered via stdin
			AtomicReference<IDominoServerController> activeConsole = new AtomicReference<>();
			//set to true if user typed "close"
			AtomicBoolean consoleClosed = new AtomicBoolean();
			//temporary consumer of stdin line (to enter server ID password)
			AtomicReference<Consumer<String>> stdInConsumer = new AtomicReference<>();

			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
			
			//spawn a thread to read console commands from stdin
			Thread consoleInputThread = new Thread(() -> {
				while (true) {
					String line = scanner.nextLine();

					//check if we have a registered consumer for the line
					Consumer<String> consumer = stdInConsumer.get();

					if (consumer != null) {
						stdInConsumer.set(null);
						consumer.accept(line);
					}
					else {
						if ("#close".equals(line) || "close".equals(line)) {
							consoleClosed.set(true);
							return;
						}
						else {
							if (line!=null && line.length()>0) {
								if (activeConsole!=null) {
									activeConsole.get().sendCommand(line);
								}
							}
						}
					}
				}
			});
			consoleInputThread.setDaemon(true);
			consoleInputThread.start();

			consoleCreator.openDominoConsole(hostName, portInt,
					adminUser, adminPassword, new BaseConsoleCallback() {

				@Override
				public String passwordRequested(String msg, String title) {
					if (fServerIdPwd!=null && fServerIdPwd.length()>0) {
						return fServerIdPwd;
					}

					System.out.println(title + "\n" + msg);

					//return server.ID password if server is currently starting up
					//and requires one
					AtomicReference<String> returnedLine = new AtomicReference<>();

					stdInConsumer.set((line) -> {
						returnedLine.set(line);

						synchronized (stdInConsumer) {
							stdInConsumer.notify();
						}
					});

					synchronized (stdInConsumer) {
						try {
							stdInConsumer.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					return returnedLine.get();
				}

				@Override
				public boolean shouldDisconnect() {
					if (Boolean.TRUE.equals(consoleClosed.get())) {
						return true;
					}
					else {
						return false;
					}
				}

				@Override
				public void consoleMessageReceived(IConsoleLine line) {
					System.out.println(line.getExecName()+" " + line.getData());
				}

				@Override
				public void setStatusMessage(String msg) {
					System.out.println(msg);
				}

				@Override
				public void showMessageDialog(String msg, String title) {
					System.out.println(title + "\n" + msg);
				}

				@Override
				public void dominoStatusReceived(DominoStatus status) {
					System.out.println("Domino status: " + status);
				}

				@Override
				public void consoleInitialized(IDominoServerController ctrl) {
					System.out.println("Domino Console initialized");

					activeConsole.set(ctrl);
					
					if (fCommand!=null && fCommand.length()>0) {
						//send command and disconnect
						ctrl.sendCommand(fCommand);

						Thread quitThread = new Thread(() -> {
							try {
								Thread.sleep(1500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

							consoleClosed.set(Boolean.TRUE);
						});
						quitThread.start();
					}
					else {
						//fetch some infos about users that are allowed to use the console
						ctrl.requestAdminInfos();
					}
				}

				@Override
				public void serverDetailsReceived(IServerDetails details) {
					System.out.println("Domino and OS details:");
					System.out.println("Name=" + details.getServerName() + ", title=" +
							details.getServerTitle() + ", cluster="
							+ details.getClusterName() + ", hostname=" + details.getHostName() +
							", ip=" + details.getIpAddress() + ", domain=" + details.getDomain()
							+ ", version=" + details.getServerVersion() + ", OS=" + details.getOSName() +
							", adminserver=" + details.isAdminServer()
							+ ", port=" + details.getPort());
				}

				@Override
				public void adminInfosReceived(List<String> serverAdministrators,
						List<String> restrictedAdministrators) {

					System.out.println(
							"Server admins: " + serverAdministrators + "\n"+
									"Restricted admins: " + restrictedAdministrators);
				}
			});

			System.out.println("Domino console closed.");

			System.exit(0);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
