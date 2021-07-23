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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.net.SocketException;
import java.util.Vector;

import com.hcl.domino.jnx.console.IConsoleCallback.DominoStatus;

import lotus.domino.console.BinaryMsgFormat;
import lotus.domino.console.MsgFormat;

/**
 * Thread to read console data from the server controller socket
 */
class ControllerReader extends Thread {
	private DominoConsoleRunner dc;
	private int srvType;
	private int indx;
	private boolean stopflag = false;
	private ServerMap sm;
	private Prompt promptThread = null;
	private int promptCntr;
	private int passwdCntr;
	private ParserThread srvrListParser;
	private ParserThread grpsListParser;
	private Vector<String> srvrList = new Vector<>(5, 1);
	private Vector<String> grpsList = new Vector<>(5, 1);

	public void setstop() {
		this.stopflag = true;
	}

	public void setPasswordCounter(int n) {
		this.passwdCntr = n;
	}

	public void setPromptCounter(int n) {
		this.promptCntr = n;
	}

	public ControllerReader(DominoConsoleRunner dominoConsole, ServerMap serverMap) {
		super("ControllerReader");
		this.dc = dominoConsole;
		this.srvType = ConsoleUtils.getOSType(serverMap.getServerType());
		this.sm = serverMap;
		this.indx = serverMap.getIndex();
		this.promptCntr = 0;
		this.passwdCntr = 0;
	}

	@Override
	public void run() {
		try {
			InputStream inputStream = this.sm.getSocket().getInputStream();
			if (inputStream == null) {
				return;
			}
			ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

			while (!this.stopflag && !this.dc.isExitRequested()) {
				MsgFormat msgFormat = null;
				BinaryMsgFormat binaryMsgFormat = null;
				MsgFormat msgFormat2 = (MsgFormat)objectInputStream.readObject();
				
				if (msgFormat2 == null) {
					continue;
				}
				if (msgFormat2 instanceof BinaryMsgFormat) {
					binaryMsgFormat = (BinaryMsgFormat)msgFormat2;
					if (binaryMsgFormat.msgType == 13) {
						this.parseAddOnServiceData(binaryMsgFormat);
						continue;
					}
					if (binaryMsgFormat.msgType != 11) {
						continue;
					}
					this.parseAddOnServiceCmd(binaryMsgFormat);
					continue;
				}
				msgFormat = msgFormat2;
				if (msgFormat.msgType == 0 || msgFormat.msgType == 1) {
					this.splitLines(msgFormat.data);
					continue;
				}
				if (msgFormat.msgType == 3) {
					this.parseServerList(msgFormat.data);
					continue;
				}
				if (msgFormat.msgType == 4) {
					this.parseGroupList(msgFormat.data);
					continue;
				}
				if (msgFormat.msgType == 8) {
					this.parseProcList(msgFormat.data);
					continue;
				}
				if (msgFormat.msgType == 9) {
					this.parseAdminList(msgFormat.data);
					continue;
				}
				if (msgFormat.msgType == 7) {
					this.parseDominoStatus(msgFormat.data);
					continue;
				}
				if (msgFormat.msgType == 10) {
					this.parseServiceCmd(msgFormat.data);
					continue;
				}
				if (msgFormat.msgType == 6) {
					this.showErrorMessage(msgFormat.data);
					continue;
				}
				if (msgFormat.msgType != 14) {
					continue;
				}
				this.showHeartBeat(msgFormat.data);
			}
		}
		catch (SocketException socketException) {
			socketException.printStackTrace();
		}
		catch (EOFException eOFException) {
			// happens when the socket is closed on disconnect
		}
		catch (StreamCorruptedException streamCorruptedException) {
			streamCorruptedException.printStackTrace();
		}
		catch (OptionalDataException optionalDataException) {
			optionalDataException.printStackTrace();
		}
		catch (IOException iOException) {
			iOException.printStackTrace();
		}
		catch (ClassNotFoundException classNotFoundException) {
			classNotFoundException.printStackTrace();
			// empty catch block
		}

		ControllerReader.this.dc.disconnectServer(ControllerReader.this.sm.getServerName(), true);
	}

	public void showErrorMessage(String string) {
		this.promptThread = new Prompt(this.dc, this.indx, string, Prompt.OK, 0, this.sm);
		this.promptThread.start();
	}

	public void parseDominoStatus(String string) {
		if (string.equalsIgnoreCase("domino-running")) {
			this.sm.setState(2, true);
			this.dc.reportDominoStatus(this.sm, DominoStatus.RUNNING);
		} else {
			this.sm.setState(2, false);
			this.dc.reportDominoStatus(this.sm, DominoStatus.NOT_RUNNING);
		}
	}

	public void parseServerList(String string) {
		if (this.srvrListParser == null || !this.srvrListParser.isAlive()) {
			this.srvrListParser = new ParserThread(this.dc, this.sm, this.srvrList, ParserThread.PARSE_SRVR);
			this.srvrListParser.start();
		}
		this.srvrList.addElement(string);
	}

	public void parseGroupList(String string) {
		if (this.grpsListParser == null || !this.grpsListParser.isAlive()) {
			this.grpsListParser = new ParserThread(this.dc, this.sm, this.grpsList, ParserThread.PARSE_GRPS);
			this.grpsListParser.start();
		}
		this.grpsList.addElement(string);
	}

	public void parseProcList(String string) {
		this.dc.updateProcInfo(this.sm, string);
	}

	public void parseAdminList(String string) {
		this.dc.updateAdminInfo(this.sm, string);
	}

	public void showHeartBeat(String string) {
	}

	public void parseServiceCmd(String string) {
		this.dc.updateServiceInfo(this.sm, string);
	}

	public void parseAddOnServiceCmd(BinaryMsgFormat binaryMsgFormat) {
		ObjectStack objectStack = this.dc.getAddOnCmdMap(binaryMsgFormat.svcname);
		try {
			objectStack.pushObject(binaryMsgFormat.data);
		}
		catch (InterruptedException interruptedException) {
			// empty catch block
		}
	}

	public void parseAddOnServiceData(BinaryMsgFormat binaryMsgFormat) {
		ObjectStack objectStack = this.dc.getAddOnDataMap(binaryMsgFormat.svcname);
		try {
			objectStack.pushObject(binaryMsgFormat);
		}
		catch (InterruptedException interruptedException) {
			// empty catch block
		}
	}

	public void splitLines(String string) {
		String string2 = null;
		int n = string.length();
		int n2 = 0;
		int n3 = 0;
		while ((n3 = string.indexOf(10, n2)) != -1) {
			string2 = n3 > 0 && string.charAt(n3 - 1) == '\r' ? string.substring(n2, n3 - 1) : string.substring(n2, n3);
			this.displayAndLog(string2);
			n2 = n3 + 1;
		}
		if (n > n2) {
			string2 = string.substring(n2);
			this.displayAndLog(string2);
		}
	}

	public void displayAndLog(String consoleLineData) {
		ConsoleLine consoleLine = ConsoleLine.parseConsoleLine(consoleLineData, this.srvType);

		if (this.promptThread != null && this.promptThread.isAlive()) {
			this.promptThread.setStop();
		}
		
		if (consoleLine.isPasswordString()) {
			this.promptThread = new Prompt(this.dc, this.indx, consoleLine.getData(), Prompt.PASSWORD, this.passwdCntr++, this.sm);
			this.promptThread.start();
		}
		else if (consoleLine.isPromptString()) {
			this.promptThread = new Prompt(this.dc, this.indx, consoleLine.getData(), Prompt.YESNO, this.promptCntr++, this.sm);
			this.promptThread.start();
		}
		
		if (this.sm.getFilterCriteria() != 0 && (this.sm.getFilterCriteria() == 1 ? !this.sm.getFilterName().equals(consoleLine.getExecName()) : this.sm.getFilterCriteria() == 2 && this.sm.getFilterPid() != consoleLine.getPid())) {
			return;
		}

		if (this.sm.isThisEventBlocked(consoleLine.getSeverity())) {
			return;
		}

		this.dc.consoleMessageReceived(this.sm, consoleLine);
	}
}

