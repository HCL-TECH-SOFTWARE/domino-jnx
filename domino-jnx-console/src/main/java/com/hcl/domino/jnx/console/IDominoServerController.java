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

public interface IDominoServerController {
	
	/**
	 * Use this method to send a command to the server. We currently support sending
	 * standard Domino console commands as well as shell commands.<br>
	 * <br>
	 * Shell commands are executed in the server's data directory under the operating system permissions
	 * of the administrator who started the Domino server. When you enter a shell command in the Command
	 * box, precede the command with "$" to distinguish it from a Domino server command.<br>
	 * <br>
	 * For example:<br>
	 * <code>
	 * $ dir *.nsf<br>
	 * $ dir *.log<br>
	 * <br>
	 * On UNIX:<br>
	 * $ ls -l *.nsf<br>
	 * $ ls *.log<br>
	 * </code>
	 * <br>
	 * The following table describes the access level required for the different command types.<br>
	 * <br>
	 * <table border=1 cellspacing=1 cellpadding=0 width=980 style='width:442.5pt;'>
	 * <tr>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><b><span style='font-size:14.0pt'>Access control field</span></b></span></p>
	 * </td>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><b><span style='font-size:14.0pt'>Domino server commands allowed</span></b></span></p>
	 * </td>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><b><span style='font-size:14.0pt'>Server Controller commands allowed</span></b></span></p>
	 * </td>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><b><span style='font-size:14.0pt'>Shell commands allowed</span></b></span></p>
	 * </td>
	 * </tr>
	 * <tr>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><span style='font-size:14.0pt'>Full Access Administrators</span></span></p>
	 * </td>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><span style='font-size:14.0pt'>All</span></span></p>
	 * </td>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><span style='font-size:14.0pt'>All</span></span></p>
	 * </td>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><span style='font-size:14.0pt'>All</span></span></p>
	 * </td>
	 * </tr>
	 * <tr>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><span style='font-size:14.0pt'>Administrators</span></span></p></td>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><span style='font-size:14.0pt'>All</span></span></p>
	 * </td>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><span style='font-size:14.0pt'>All</span></span></p>
	 * </td>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><span style='font-size:14.0pt'>None</span></span></p>
	 * </td>
	 * </tr>
	 * <tr>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><span style='font-size:14.0pt'>Full Remote Console Administrators</span></span></p>
	 * </td>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><span style='font-size:14.0pt'>All</span></span></p>
	 * </td>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><span style='font-size:14.0pt'>All</span></span></p>
	 * </td>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><span style='font-size:14.0pt'>None</span></span></p>
	 * </td>
	 * </tr>
	 * <tr>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><span style='font-size:14.0pt'>View-only Administrators</span></span></p>
	 * </td>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><span style='font-size:14.0pt'>Commands that show status info only; for example Show Tasks but not Load HTTP</span></span></p>
	 * </td>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><span style='font-size:14.0pt'>All Controller commands except Quit, Enable User, Disable User</span></span></p>
	 * </td>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><span style='font-size:14.0pt'>None</span></span></p>
	 * </td>
	 * </tr>
	 * <tr>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><span style='font-size:14.0pt'>System Administrators</span></span></p>
	 * </td>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><span style='font-size:14.0pt'>None</span></span></p>
	 * </td>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><span style='font-size:14.0pt'>All Controller commands except Quit, Enable User, Disable User</span></span></p>
	 * </td>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><span style='font-size:14.0pt'>All</span></span></p>
	 * </td>
	 * </tr>
	 * <tr>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><span style='font-size:14.0pt'>Restricted System Administrators</span></span></p>
	 * </td>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><span style='font-size:14.0pt'>None</span></span></p>
	 * </td>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><span style='font-size:14.0pt'>All Controller commands except Quit, Enable User, Disable User</span></span></p>
	 * </td>
	 * <td width="25%" valign=top style='width:25.0%;padding:5.25pt 5.25pt 5.25pt 5.25pt'>
	 * <p><span><span style='font-size:14.0pt'>Only shell commands listed in &quot;Restricted System Commands&quot; field</span></span></p>
	 * </td>
	 * </tr>
	 * </table>
	 * 
	 * @param cmd command
	 */
	void sendCommand(String cmd);

	/**
	 * Sends a broadcast message to all users currently connected to the Domino console
	 * 
	 * @param msg message
	 */
	void sendBroadcastMessage(String msg);
	
	/**
	 * Convenience method to send the server controller command "#kill server"
	 * to kill the server process. Use {@link #startServer()} to start it later.
	 * If the server ID is password protected, the password can be specified via
	 * {@link IConsoleCallback#passwordRequested(String, String)}.
	 */
	void killServer();
	
	/**
	 * Convenience method to send the server controller command "#start server"
	 * to start the server process. If the server ID is password protected, the
	 * password can be specified via {@link IConsoleCallback#passwordRequested(String, String)}.
	 */
	void startServer();
	
	/**
	 * Convenience method to send the Domino console command "quit" to
	 * stop the server process. Use {@link #startServer()} to start it later.
	 * If the server ID is password protected, the password can be specified via
	 * {@link IConsoleCallback#passwordRequested(String, String)}.
	 */
	void stopServer();
	
	/**
	 * Convenience method to send the server controller command "#quit" to
	 * stop the server and the server controller. Please note that you will
	 * not be able to reconnect again via this console API.
	 */
	void quitServerAndServerController();

	/**
	 * Convenience method to send the server controller command "#show processes"
	 * to return the names of running processes via the Domino console.
	 */
	void showProcesses();

	/**
	 * Convenience method to send the server controller command "#show users"
	 * to return the names of users that are currently connected to the
	 * server controller.
	 */
	void showUsers();
	
	/**
	 * Requests infos about server administrators and restricted administrators.
	 * Both lists are returned via
	 * {@link IConsoleCallback#adminInfosReceived(java.util.List, java.util.List)}
	 */
	void requestAdminInfos();
	
}
