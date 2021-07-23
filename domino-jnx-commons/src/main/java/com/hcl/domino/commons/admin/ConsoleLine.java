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
package com.hcl.domino.commons.admin;

import java.text.MessageFormat;

import com.hcl.domino.admin.IConsoleLine;

/**
 * Container for a single Domino console line with the text content and
 * additional properties like the pid, tod, process name, type and priority.
 */
public class ConsoleLine implements IConsoleLine {
    public static final String sSeqNumber = "sq=\""; //$NON-NLS-1$
    public static final String sPassword = "pw=\""; //$NON-NLS-1$
    public static final String sPrompt = "pr=\""; //$NON-NLS-1$
    public static final String sTime = "ti=\""; //$NON-NLS-1$
    public static final String sExecName = "ex=\""; //$NON-NLS-1$
    public static final String sProcId = "pi=\""; //$NON-NLS-1$
    public static final String sThreadId = "tr=\""; //$NON-NLS-1$
    public static final String sStatus = "st=\""; //$NON-NLS-1$
    public static final String sType = "ty=\""; //$NON-NLS-1$
    public static final String sSev = "sv=\""; //$NON-NLS-1$
    public static final String sColor = "co=\""; //$NON-NLS-1$
    public static final String sAddin = "ad=\""; //$NON-NLS-1$
    public static final String sArgsEnd = ">"; //$NON-NLS-1$
    public static final String sConsoleTextStart = "<ct "; //$NON-NLS-1$
    public static final String sConsoleTextEnd = "</ct>"; //$NON-NLS-1$
    public static final String sTokenEnd = "\""; //$NON-NLS-1$
    public static final String sTrue = "t"; //$NON-NLS-1$
    public static final String sServerStatus = "ss=\""; //$NON-NLS-1$

    private int seqNum;
    private String timeStamp;
    private String execName;
    private int pid;
    private long tid;
    private long vTid;
    private int status;
    private int type;
    private int severity;
    private int color;
    private String addName;
    private String data;
    private boolean isPasswdReq;
    private boolean isPrompt;

    private ConsoleLine() {
        this.init();
    }

    private void init() {
        this.setSeqNum(0);
        this.setTimeStamp(""); //$NON-NLS-1$
        this.setExecName(""); //$NON-NLS-1$
        this.setPid(0);
        this.setTid(0L);
        this.setVTid(0L);
        this.setStatus(0);
        this.setType(0);
        this.setSeverity(0);
        this.setColor(-1);
        this.setAddName(""); //$NON-NLS-1$
        this.setData(""); //$NON-NLS-1$
        this.setPasswordString(false);
        this.setPromptString(false);
    }

    private static String getStringToken(String string, String string2, String string3) {
        String string4 = null;
        int n = 0;
        int n2 = 0;
        n = string.indexOf(string2);
        n2 = string.indexOf(string3, n + string2.length());
        if (n > 0 && n2 > 0) {
            string4 = string.substring(n + string2.length(), n2);
        }
        return string4;
    }

    private static int getIntToken(String string, int n, String string2, String string3) {
        String string4 = getStringToken(string, string2, string3);
        if (string4 != null) {
            return Integer.parseInt(string4, n);
        }
        return 0;
    }

    private static boolean getBoolToken(String string, String string2, String string3) {
        String string4 = getStringToken(string, string2, string3);
        if (string4 != null) {
            return string4.equalsIgnoreCase(sTrue);
        }
        return false;
    }

    public static ConsoleLine parseConsoleLine(String encodedConsoleLine, int srvType) {
    	ConsoleLine line = new ConsoleLine();
    	
        if (!encodedConsoleLine.startsWith(sConsoleTextStart) || !encodedConsoleLine.endsWith(sConsoleTextEnd)) {
        	line.setData(encodedConsoleLine.equals("\n") ? "" : encodedConsoleLine); //$NON-NLS-1$ //$NON-NLS-2$
        	line.setColor(-1);
            return line;
        }
        
        int n2 = srvType == 0 ? 16 : 10;
        int n3 = encodedConsoleLine.indexOf(sArgsEnd);
        String string2 = encodedConsoleLine.substring(0, n3 + 1);
        try {
        	line.setSeqNum(getIntToken(string2, 16, sSeqNumber, sTokenEnd));
        	line.setPasswordString(getBoolToken(string2, sPassword, sTokenEnd));
        	line.setPromptString(getBoolToken(string2, sPrompt, sTokenEnd));
        	line.setTimeStamp(getStringToken(string2, sTime, sTokenEnd));
        	line.setExecName(getStringToken(string2, sExecName, sTokenEnd));
        	line.setPid(getIntToken(string2, n2, sProcId, sTokenEnd));
            int n4 = string2.indexOf(sThreadId);
            int n5 = string2.indexOf("-", n4); //$NON-NLS-1$
            line.setTid(Long.parseLong(string2.substring(n4 + sThreadId.length(), n5), n2));
            n4 = n5;
            n5 = string2.indexOf(sTokenEnd, n4);
            if (n4 > 0 && n5 > 0) {
                String string3 = string2.substring(n4 + 1, n5);
                int n6 = string3.indexOf(":", 0); //$NON-NLS-1$
                if (n6 > 0) {
                    string3 = string3.substring(0, n6);
                }
                n2 = srvType == 1 ? 16 : n2;
                line.setVTid(Long.parseLong(string3, n2));
            }
            line.setStatus(getIntToken(string2, 16, sStatus, sTokenEnd));
            line.setType(getIntToken(string2, 10, sType, sTokenEnd));
            line.setSeverity(getIntToken(string2, 10, sSev, sTokenEnd));
            line.setColor(getIntToken(string2, 10, sColor, sTokenEnd));
            line.setAddName(getStringToken(string2, sAddin, sTokenEnd));
            line.setData(getStringToken(encodedConsoleLine, sArgsEnd, sConsoleTextEnd));
        }
        catch (Exception exception) {
            System.out.println("msg=" + exception.getMessage());
            System.out.println("Exception:parseMS::" + encodedConsoleLine + ":,srvType=" + srvType);
            
            line.setData(encodedConsoleLine);
            line.setColor(-1);
            return line;
        }
        
        return line;
    }

    @Override
	public int getMsgSeqNum() {
		return seqNum;
	}

	private void setSeqNum(int msgSeqNum) {
		this.seqNum = msgSeqNum;
	}

    @Override
	public String getTimeStamp() {
		return timeStamp;
	}

	private void setTimeStamp(String msgTimeStamp) {
		this.timeStamp = msgTimeStamp;
	}

    @Override
	public String getExecName() {
		return execName;
	}

	private void setExecName(String msgExecName) {
		this.execName = msgExecName;
	}

    @Override
	public int getPid() {
		return pid;
	}

	private void setPid(int msgPid) {
		this.pid = msgPid;
	}

    @Override
	public long getTid() {
		return tid;
	}

	private void setTid(long msgTid) {
		this.tid = msgTid;
	}

    @Override
	public long getVTid() {
		return vTid;
	}

	private void setVTid(long msgVTid) {
		this.vTid = msgVTid;
	}

    @Override
	public int getStatus() {
		return status;
	}

	private void setStatus(int msgStatus) {
		this.status = msgStatus;
	}

    @Override
	public int getType() {
		return type;
	}

	private void setType(int msgType) {
		this.type = msgType;
	}

    @Override
	public int getSeverity() {
		return severity;
	}

	private void setSeverity(int msgSeverity) {
		this.severity = msgSeverity;
	}

    @Override
	public int getColor() {
		return color;
	}

	private void setColor(int msgColor) {
		this.color = msgColor;
	}

    @Override
	public String getAddName() {
		return addName;
	}

	private void setAddName(String msgAddName) {
		this.addName = msgAddName;
	}

    @Override
	public String getData() {
		return data;
	}

	private void setData(String msgData) {
		this.data = msgData;
	}

    @Override
	public boolean isPasswordString() {
		return isPasswdReq;
	}

	private void setPasswordString(boolean b) {
		this.isPasswdReq = b;
	}

    @Override
	public boolean isPromptString() {
		return isPrompt;
	}

	private void setPromptString(boolean b) {
		this.isPrompt = b;
	}

	@Override
	public String toString() {
		return MessageFormat.format(
				"ConsoleLine [seqNum={0}, timeStamp={1}, execName={2}, pid={3}, tid={4}, vTid={5}, status={6}, type={7}, severity={8}, color={9}, addName={10}, isPwd={11}, isPrompt={12}, data={13}]", //$NON-NLS-1$
				seqNum, timeStamp, execName, pid, tid, vTid, status, type, severity, color, addName,
				isPasswdReq, isPrompt, data);
	}
	
	
}

