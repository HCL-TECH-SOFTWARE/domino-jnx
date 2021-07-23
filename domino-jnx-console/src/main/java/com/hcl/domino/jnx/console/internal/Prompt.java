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

import java.text.DateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Thread that triggers user interaction like asking for a server ID password,
 * display yes/no dialogs or simple info dialogs. The result is then send
 * back as console command.
 */
class Prompt extends Thread {
	public static int PASSWORD = 0;
	public static int YESNO = 1;
	public static int OK = 2;
	
	private int promptType;
	private String msg;
	private int index;
	private int cntr;
	private DominoConsoleRunner dc;
	private boolean stopflag = false;
	private ServerMap sm = null;

	public void setStop() {
		if (this.promptType == OK) {
			return;
		}
		this.stopflag = true;
		if (this.promptType == PASSWORD) {
			this.dc.closeOpenPasswordDialog(this.sm);
		} else if (this.promptType == YESNO) {
			this.dc.closeOpenPrompt(this.sm);
		}
	}

	public Prompt(DominoConsoleRunner dominoConsole, int index, String msg, int promptType, int cntr, ServerMap serverMap) {
		this.dc = dominoConsole;
		this.index = index;
		this.msg = msg;
		this.promptType = promptType;
		this.cntr = cntr;
		this.sm = serverMap;
	}

	@Override
	public void run() {
		String serverNamePrefix = this.sm != null ? this.sm.getServerName() + ":" : "";
		
		while (!this.stopflag) {
			String result = null;
			
			if (this.promptType == PASSWORD) {
				String object = serverNamePrefix + DominoConsoleRunner.getResourceBundle().getString("msgPasswdSrvr");
				result = this.dc.requestPasswordDialog(this.sm, this.msg, object);
				if (result==null || result.length()==0) {
					result = ">" + result + "<";
				}
				result = "PasswordCntr" + this.cntr + "=" + result;
			} else if (this.promptType == YESNO) {
				Vector<String> selectionValues = new Vector<>();
				
				int n = this.msg.lastIndexOf("(");
				int n2 = this.msg.lastIndexOf(")");
				
				if (n != -1 && n2 != -1) {
					StringTokenizer st = new StringTokenizer(this.msg.substring(n + 1, n2), "/");
					
					while (st.hasMoreTokens()) {
						selectionValues.add(st.nextToken());
					}

					result = this.dc.requestPrompt(this.sm, this.msg.substring(0, n),
							serverNamePrefix + DominoConsoleRunner.getResourceBundle().getString("msgPromptSrvr"),
							selectionValues.toArray(new String[selectionValues.size()]));

					if (result != null) {
						result = result.substring(0, 1);
					}
				}
				if (result == null) {
					result = "C";
				}
				result = "PromptCntr" + this.cntr + "=" + result;
			} else {
				long l;
				if (this.promptType != OK) {
					break;
				}
				
				DateFormat dtFormat = DateFormat.getDateTimeInstance(3, 0);
				int n = this.msg.indexOf("]");
				long serverDt = this.sm.getDateTime();
				if (n != -1 && this.msg.charAt(0) == '[') {
					l = Long.parseLong(this.msg.substring(1, n));
					this.msg = "[" + dtFormat.format(new Date(l)) + "] " + this.msg.substring(n + 1);
				} else {
					l = serverDt + 1L;
				}
				if (l > serverDt) {
					this.sm.setDateTime(l);

					this.dc.reportMessageDialog(this.sm, this.msg, serverNamePrefix + DominoConsoleRunner.getResourceBundle().getString("msgInfo"));
				}
				result = null;
			}
			
			if (result != null) {
				this.dc.sendCommand(result, this.index, false);
			}
			
			this.stopflag = true;
		}
	}
}

