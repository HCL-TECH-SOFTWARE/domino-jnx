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

import com.hcl.domino.jnx.console.IConsoleLine;

/**
 * Container for a single Domino console line with the text content and
 * additional properties like the pid, tod, process name, type and priority.
 */
public class ConsoleLine implements IConsoleLine {
  public static final String sSeqNumber = "sq=\"";
  public static final String sPassword = "pw=\"";
  public static final String sPrompt = "pr=\"";
  public static final String sTime = "ti=\"";
  public static final String sExecName = "ex=\"";
  public static final String sProcId = "pi=\"";
  public static final String sThreadId = "tr=\"";
  public static final String sStatus = "st=\"";
  public static final String sType = "ty=\"";
  public static final String sSev = "sv=\"";
  public static final String sColor = "co=\"";
  public static final String sAddin = "ad=\"";
  public static final String sArgsEnd = ">";
  public static final String sConsoleTextStart = "<ct ";
  public static final String sConsoleTextEnd = "</ct>";
  public static final String sTokenEnd = "\"";
  public static final String sTrue = "t";
  public static final String sServerStatus = "ss=\"";

  private static boolean getBoolToken(final String string, final String string2, final String string3) {
    final String string4 = ConsoleLine.getStringToken(string, string2, string3);
    if (string4 != null) {
      return string4.equalsIgnoreCase(ConsoleLine.sTrue);
    }
    return false;
  }

  private static int getIntToken(final String string, final int n, final String string2, final String string3) {
    final String string4 = ConsoleLine.getStringToken(string, string2, string3);
    if (string4 != null) {
      return Integer.parseInt(string4, n);
    }
    return 0;
  }

  private static String getStringToken(final String string, final String string2, final String string3) {
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

  public static ConsoleLine parseConsoleLine(final String encodedConsoleLine, final int srvType) {
    final ConsoleLine line = new ConsoleLine();

    if (!encodedConsoleLine.startsWith(ConsoleLine.sConsoleTextStart)
        || !encodedConsoleLine.endsWith(ConsoleLine.sConsoleTextEnd)) {
      line.setData(encodedConsoleLine.equals("\n") ? "" : encodedConsoleLine);
      line.setColor(-1);
      return line;
    }

    int n2 = srvType == 0 ? 16 : 10;
    final int n3 = encodedConsoleLine.indexOf(ConsoleLine.sArgsEnd);
    final String string2 = encodedConsoleLine.substring(0, n3 + 1);
    try {
      line.setSeqNum(ConsoleLine.getIntToken(string2, 16, ConsoleLine.sSeqNumber, ConsoleLine.sTokenEnd));
      line.setPasswordString(ConsoleLine.getBoolToken(string2, ConsoleLine.sPassword, ConsoleLine.sTokenEnd));
      line.setPromptString(ConsoleLine.getBoolToken(string2, ConsoleLine.sPrompt, ConsoleLine.sTokenEnd));
      line.setTimeStamp(ConsoleLine.getStringToken(string2, ConsoleLine.sTime, ConsoleLine.sTokenEnd));
      line.setExecName(ConsoleLine.getStringToken(string2, ConsoleLine.sExecName, ConsoleLine.sTokenEnd));
      line.setPid(ConsoleLine.getIntToken(string2, n2, ConsoleLine.sProcId, ConsoleLine.sTokenEnd));
      int n4 = string2.indexOf(ConsoleLine.sThreadId);
      int n5 = string2.indexOf("-", n4);
      line.setTid(Long.parseLong(string2.substring(n4 + ConsoleLine.sThreadId.length(), n5), n2));
      n4 = n5;
      n5 = string2.indexOf(ConsoleLine.sTokenEnd, n4);
      if (n4 > 0 && n5 > 0) {
        String string3 = string2.substring(n4 + 1, n5);
        final int n6 = string3.indexOf(":", 0);
        if (n6 > 0) {
          string3 = string3.substring(0, n6);
        }
        n2 = srvType == 1 ? 16 : n2;
        line.setVTid(Long.parseLong(string3, n2));
      }
      line.setStatus(ConsoleLine.getIntToken(string2, 16, ConsoleLine.sStatus, ConsoleLine.sTokenEnd));
      line.setType(ConsoleLine.getIntToken(string2, 10, ConsoleLine.sType, ConsoleLine.sTokenEnd));
      line.setSeverity(ConsoleLine.getIntToken(string2, 10, ConsoleLine.sSev, ConsoleLine.sTokenEnd));
      line.setColor(ConsoleLine.getIntToken(string2, 10, ConsoleLine.sColor, ConsoleLine.sTokenEnd));
      line.setAddName(ConsoleLine.getStringToken(string2, ConsoleLine.sAddin, ConsoleLine.sTokenEnd));
      line.setData(ConsoleLine.getStringToken(encodedConsoleLine, ConsoleLine.sArgsEnd, ConsoleLine.sConsoleTextEnd));
    } catch (final Exception exception) {
      System.out.println("msg=" + exception.getMessage());
      System.out.println("Exception:parseMS::" + encodedConsoleLine + ":,srvType=" + srvType);

      line.setData(encodedConsoleLine);
      line.setColor(-1);
      return line;
    }

    return line;
  }

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

  @Override
  public String getAddName() {
    return this.addName;
  }

  @Override
  public int getColor() {
    return this.color;
  }

  @Override
  public String getData() {
    return this.data;
  }

  @Override
  public String getExecName() {
    return this.execName;
  }

  @Override
  public int getMsgSeqNum() {
    return this.seqNum;
  }

  @Override
  public int getPid() {
    return this.pid;
  }

  @Override
  public int getSeverity() {
    return this.severity;
  }

  @Override
  public int getStatus() {
    return this.status;
  }

  @Override
  public long getTid() {
    return this.tid;
  }

  @Override
  public String getTimeStamp() {
    return this.timeStamp;
  }

  @Override
  public int getType() {
    return this.type;
  }

  @Override
  public long getVTid() {
    return this.vTid;
  }

  private void init() {
    this.setSeqNum(0);
    this.setTimeStamp("");
    this.setExecName("");
    this.setPid(0);
    this.setTid(0L);
    this.setVTid(0L);
    this.setStatus(0);
    this.setType(0);
    this.setSeverity(0);
    this.setColor(-1);
    this.setAddName("");
    this.setData("");
    this.setPasswordString(false);
    this.setPromptString(false);
  }

  @Override
  public boolean isPasswordString() {
    return this.isPasswdReq;
  }

  @Override
  public boolean isPromptString() {
    return this.isPrompt;
  }

  private void setAddName(final String msgAddName) {
    this.addName = msgAddName;
  }

  private void setColor(final int msgColor) {
    this.color = msgColor;
  }

  private void setData(final String msgData) {
    this.data = msgData;
  }

  private void setExecName(final String msgExecName) {
    this.execName = msgExecName;
  }

  private void setPasswordString(final boolean b) {
    this.isPasswdReq = b;
  }

  private void setPid(final int msgPid) {
    this.pid = msgPid;
  }

  private void setPromptString(final boolean b) {
    this.isPrompt = b;
  }

  private void setSeqNum(final int msgSeqNum) {
    this.seqNum = msgSeqNum;
  }

  private void setSeverity(final int msgSeverity) {
    this.severity = msgSeverity;
  }

  private void setStatus(final int msgStatus) {
    this.status = msgStatus;
  }

  private void setTid(final long msgTid) {
    this.tid = msgTid;
  }

  private void setTimeStamp(final String msgTimeStamp) {
    this.timeStamp = msgTimeStamp;
  }

  private void setType(final int msgType) {
    this.type = msgType;
  }

  private void setVTid(final long msgVTid) {
    this.vTid = msgVTid;
  }

  @Override
  public String toString() {
    return "ConsoleLine [seqNum=" + this.seqNum + ", timeStamp=" + this.timeStamp + ", execName=" + this.execName + ", pid="
        + this.pid
        + ", tid=" + this.tid + ", vTid=" + this.vTid + ", status=" + this.status + ", type=" + this.type + ", severity="
        + this.severity
        + ", color=" + this.color + ", addName=" + this.addName + ", isPwd=" + this.isPasswdReq + ", isPrompt="
        + this.isPrompt + ", data=" + this.data + "]";
  }

}
