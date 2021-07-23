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

public interface IDominoConsoleCreator {

	void openDominoConsole(String serviceName, String binderHostName,
			int binderPort, String userName, String password, boolean viaFirewall, String socksName, int socksPort,
			IConsoleCallback callback) throws Exception;
	
	/**
	 * Opens the Domino console on the local machine
	 * 
	 * @param callback console callback to send/receive messages
	 * @throws Exception in case of connection errors
	 */
	void openLocalDominoConsole(IConsoleCallback callback) throws Exception;
	
	void openDominoConsole(String hostName, int port, String user, String password,
			IConsoleCallback callback) throws Exception;

}
