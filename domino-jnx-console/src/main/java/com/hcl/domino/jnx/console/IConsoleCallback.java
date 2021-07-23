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

import java.util.List;

import com.hcl.domino.jnx.console.internal.LoginSettings;

public interface IConsoleCallback {

	/**
	 * Implement this method to request a disconnect
	 * 
	 * @return true to disconnect
	 */
	boolean shouldDisconnect();
	
	/**
	 * After this method has been invoked, it's safe to send commands
	 * 
	 * @param console console
	 */
	void consoleInitialized(IDominoServerController console);
	
    /**
     * This method is called when the server controller requires a server ID
     * password to launch the Domino server.
     * 
     * @param msg dialog message
     * @param title dialog title
     * @return password or null if aborted
     */
    String passwordRequested(String msg, String title);
    
    /**
     * Displays a dialog to let the user select a value, e.g. for yes/no questions
     * 
     * @param msg dialog message
     * @param title dialog title
     * @param options selection
     * @return selected value or null if aborted
     */
    <T> T showPrompt(String msg, String title, T[] options);

	void showMessageDialog(String msg, String title);

	/**
	 * This method is called before a password is requested via {@link #passwordRequested(String, String)} to close
	 * any already open password dialogs.
	 */
	void closeOpenPasswordDialog();

	/**
	 * This method is called before a prompt is requested via {@link #showPrompt(String, String, Object[])}
	 * to close any already open prompt dialogs.
	 */
 	void closeOpenPrompt();

 	/**
 	 * This method is called to display an informational message
 	 * 
 	 * @param msg message
 	 */
	void setStatusMessage(String msg);
	
	<T> T showInputDialog(String msg, String title, T[] values, T initialSelection);

	/**
	 * Implement this method and display a dialog to let the user enter
	 * login settings
	 * 
	 * @param loginSettings current settings, to be changed
	 * @return true if successful, false if aborted
	 */
	boolean requestLoginSettings(LoginSettings loginSettings);

	public enum DominoStatus { RUNNING, NOT_RUNNING }
	
	void dominoStatusReceived(DominoStatus status);
	
	void serverDetailsReceived(IServerDetails details);
	
	void adminInfosReceived(List<String> serverAdministrators, List<String> restrictedAdministrators);
	
	void consoleMessageReceived(IConsoleLine line);

}
