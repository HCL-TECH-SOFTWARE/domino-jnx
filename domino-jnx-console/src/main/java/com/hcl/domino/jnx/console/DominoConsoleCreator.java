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

import java.io.IOException;
import java.net.InetAddress;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.hcl.domino.jnx.console.IConsoleCallback.DominoStatus;
import com.hcl.domino.jnx.console.internal.ConsoleLine;
import com.hcl.domino.jnx.console.internal.DominoConsoleRunner;
import com.hcl.domino.jnx.console.internal.LoginSettings;
import com.hcl.domino.jnx.console.internal.ServerDetails;
import com.hcl.domino.jnx.console.internal.ServerMap;

/**
 * Implementation of {@link IDominoConsoleCreator} that opens a connection
 * to a local or remote Domino server controller to access the Domino console.
 * The underlying API heavily spawns new threads for various operations, e.g. for
 * reading/writing to the Domino console or displaying prompts. In
 * this class we collect all requests and execute them in the caller thread
 * to simplify the architecture and prevent multithreading issues.
 * 
 * @author Karsten Lehmann
 */
public class DominoConsoleCreator implements IDominoConsoleCreator {

	@Override
	public void openDominoConsole(String serviceName, String binderHostName, int binderPort, String userName,
			String password, boolean viaFirewall, String socksName, int socksPort, IConsoleCallback callback)
			throws Exception {

		ServerMap sm = new ServerMap();
		sm.setServiceName(serviceName);
		sm.setBinderName(binderHostName);
		sm.setBinderPort(Integer.toString(binderPort));
		sm.setUserName(userName);
		sm.setPassword(password);
		sm.setViaFirewall(viaFirewall);
		sm.setProxyName(socksName);
		sm.setProxyPort(Integer.toString(socksPort));

		openDominoConsole(sm, false, callback);
	}

	@Override
	public void openLocalDominoConsole(IConsoleCallback callback) throws Exception {
		openDominoConsole(InetAddress.getLocalHost().getHostName(), 2050, "localAdmin", "localAdmin", callback); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public void openDominoConsole(String hostName, int port, String user, String password, IConsoleCallback callback)
			throws Exception {
		
		if (hostName==null || hostName.length()==0) {
			throw new IllegalArgumentException("Hostname cannot be empty");
		}

		if (user==null || user.length()==0) {
			user = "localAdmin"; //$NON-NLS-1$
		}
		
		if (password==null || password.length()==0) {
			password = "localAdmin"; //$NON-NLS-1$
		}
		
		if (port==0) {
			port = 2050;
		}
		
		ServerMap sm = new ServerMap();
		sm.setHostname(hostName);
		sm.setPort(port);
		sm.setUserName(user);
		sm.setPassword(password);
		
		openDominoConsole(sm, false, callback);
	}

	private static void openDominoConsole(ServerMap sm, boolean isAdvanced, IConsoleCallback callback) throws Exception {
		//queues for messages to report and code to execute
		LinkedBlockingQueue<ConsoleLine> incomingMessages = new LinkedBlockingQueue<>();
		LinkedBlockingQueue<Runnable> todos = new LinkedBlockingQueue<>();
		
		AtomicReference<Exception> connectException = new AtomicReference<>();
		
		AtomicBoolean consoleExited = new AtomicBoolean();

		DominoConsoleRunner console = new DominoConsoleRunner(sm, isAdvanced) {
			private boolean isInitialized;
			
			@Override
			public void sendCommand(String cmd) {
				if (!isInitialized) {
					throw new IllegalStateException("Console not fully initialized");
				}

				super.sendCommand(cmd);
			}
			
			@Override
			protected void reportDominoStatus(ServerMap sm, DominoStatus status) {
				todos.add(() -> {
					callback.dominoStatusReceived(status);
				});
			}
			
			@Override
			public void reportConsoleInitialized(ServerMap sm) {
				this.isInitialized = true;
				DominoConsoleRunner thisConsole = this;
				
				todos.add(() -> {
					callback.consoleInitialized(new IDominoServerController() {

						@Override
						public void sendCommand(String cmd) {
							thisConsole.sendCommand(cmd);
						}
						
						@Override
						public void sendBroadcastMessage(String msg) {
							thisConsole.sendBroadcastMessage(msg);
						}

						@Override
						public void requestAdminInfos() {
							thisConsole.sendCommand("#update admins"); //$NON-NLS-1$
						}

						@Override
						public void killServer() {
							thisConsole.sendCommand("#kill domino"); //$NON-NLS-1$
						}

						@Override
						public void startServer() {
							thisConsole.sendCommand("#start domino"); //$NON-NLS-1$
						}

						@Override
						public void stopServer() {
							thisConsole.sendCommand("quit"); //$NON-NLS-1$
						}

						@Override
						public void quitServerAndServerController() {
							thisConsole.sendCommand("#quit"); //$NON-NLS-1$
						};
						
						@Override
						public void showProcesses() {
							thisConsole.sendCommand("#show processes"); //$NON-NLS-1$
						}
						
						@Override
						public void showUsers() {
							thisConsole.sendCommand("#show users"); //$NON-NLS-1$
						}
					});
				});
			}

			@Override
			public void reportConsoleConnectFailed(ServerMap sm, String msg, Exception exception) {
				connectException.set(new IOException(msg, exception));
			}
			
			@Override
			protected void adminInfosReceived(Vector<String> serverAdministrators,
					Vector<String> restrictedAdministrators) {
				todos.add(() -> {
					callback.adminInfosReceived(serverAdministrators, restrictedAdministrators);
				});
			}
			
			@Override
			protected void reportServerInfosUpdated(ServerMap existingServer, ServerMap update) {
				ServerDetails details = new ServerDetails();
				details.setAdminServer(update.isAdminServer());
				details.setDb2Server(update.isDB2server());
				details.setHostName(update.getHostname());
				details.setClusterName(update.getClusterName());
				details.setDomain(update.getDomain());
				details.setServerName(update.getServerName());
				details.setOSName(update.getServerType());
				details.setPort(update.getPort());
				details.setServerTitle(update.getTitle());
				details.setServerVersion(update.getVersion());
				
				todos.add(() -> {
					callback.serverDetailsReceived(details);
				});
			}

			@Override
			public void reportStatusMessage(ServerMap sm, String msg) {
				todos.add(() -> {
					callback.setStatusMessage(msg);
				});
			}

			@Override
			public void reportMessageDialog(ServerMap sm, String msg, String title) {
				todos.add(() -> {
					callback.showMessageDialog(msg, title);
				});
			}

			@Override
			public boolean requestLoginSettings(ServerMap sm, LoginSettings loginSettings) {
				AtomicBoolean retValue = new AtomicBoolean();
				Object lock = new Object();
				AtomicReference<Exception> ex = new AtomicReference<>();

				todos.add(() -> {
					try {
						retValue.set(callback.requestLoginSettings(loginSettings));
					}
					catch (Exception e) {
						ex.set(e);
					}
					finally {
						synchronized(lock) {
							lock.notify();
						}
					}
				});
				
				synchronized(lock) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						throw new RuntimeException("Input prompt interrupted", e);
					}
					
					if (ex.get()!=null) {
						throw new RuntimeException("Input prompt error", ex.get());
					}
					
					return retValue.get();
				}
			}

			@Override
			public String requestInputDialog(ServerMap sm, String msg, String title, String[] values,
					String initialSelection) {
				AtomicReference<String> retValue = new AtomicReference<>();
				Object lock = new Object();
				AtomicReference<Exception> ex = new AtomicReference<>();

				todos.add(() -> {
					try {
						retValue.set(callback.showInputDialog(msg, title, values, initialSelection));
					}
					catch (Exception e) {
						ex.set(e);
					}
					finally {
						synchronized(lock) {
							lock.notify();
						}
					}
				});

				synchronized(lock) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						throw new RuntimeException("Input prompt interrupted", e);
					}
					
					if (ex.get()!=null) {
						throw new RuntimeException("Input prompt error", ex.get());
					}
					
					return retValue.get();
				}
			}

			@Override
			public String requestPasswordDialog(ServerMap sm, String msg, String title) {
				AtomicReference<String> retValue = new AtomicReference<>();
				Object lock = new Object();
				AtomicReference<Exception> ex = new AtomicReference<>();
				
				todos.add(() -> {
					try {
						retValue.set(callback.passwordRequested(msg, title));
					}
					catch (Exception e) {
						ex.set(e);
					}
					finally {
						synchronized(lock) {
							lock.notify();
						}
					}
				});

				synchronized(lock) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						throw new RuntimeException("Password prompt interrupted", e);
					}
					
					if (ex.get()!=null) {
						throw new RuntimeException("Password prompt error", ex.get());
					}
					
					return retValue.get();
				}
			}

			@Override
			public void closeOpenPasswordDialog(ServerMap sm) {
				todos.add(() -> {
					callback.closeOpenPasswordDialog();
				});
			}

			@Override
			public void closeOpenPrompt(ServerMap sm) {
				todos.add(() -> {
					callback.closeOpenPrompt();
				});
			}

			@Override
			public <T> T requestPrompt(ServerMap sm, String msg, String title, T[] options) {
				AtomicReference<T> retValue = new AtomicReference<>();
				Object lock = new Object();
				AtomicReference<Exception> ex = new AtomicReference<>();

				todos.add(() -> {
					try {
						retValue.set(callback.showPrompt(msg, title, options));
					}
					catch (Exception e) {
						ex.set(e);
					}
					finally {
						synchronized(lock) {
							lock.notify();
						}
					}
				});
				
				synchronized(lock) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						throw new RuntimeException("Password prompt interrupted", e);
					}
					
					if (ex.get()!=null) {
						throw new RuntimeException("Password prompt error", ex.get());
					}
					
					return retValue.get();
				}
			}

			@Override
			public void consoleMessageReceived(ServerMap sm, ConsoleLine line) {
				incomingMessages.add(line);
			}
			
		};

		AtomicReference<Exception> loopException = new AtomicReference<>();
		
		AtomicBoolean exitRequired = new AtomicBoolean();
		
		while (!consoleExited.get() && loopException.get()==null) {
			//check if connect failed
			Exception ex = connectException.get();
			if (ex!=null) {
				exitRequired.set(false);
				
				//trigger an async exit and dont wait for the result, because
				//the console might not be fully initialized and running
				console.exit(() -> {
				});

				throw ex;
			}
			
			if (callback.shouldDisconnect()) {
				exitRequired.set(false);
				
				console.exit(() -> consoleExited.set(true));
			}
			
			//report all available console messages within the caller thread
			ConsoleLine line;
			while( (line = incomingMessages.poll()) != null) {
				try {
					callback.consoleMessageReceived(line);
				}
				catch (Exception e) {
					loopException.set(e);
				}
			};
			
			//and process all other todos in the caller thread, too
			//e.g. to show UI prompts
			Runnable todo;
			while ( (todo = todos.poll()) != null) {
				try {
					todo.run();
				}
				catch (Exception e) {
					loopException.set(e);
				}
			}
			
			Thread.sleep(300);
		}
		
		if (exitRequired.get()) {
			console.exit(() -> {
				//
			});
		}
		
		if (loopException.get()!=null) {
			throw loopException.get();
		}
	}

}
