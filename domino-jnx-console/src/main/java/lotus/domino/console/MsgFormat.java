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
package lotus.domino.console;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Data received from the server controller socket.<br>
 * <br>
 * Please note that it's important that this class keeps its Java package
 * lotus.domino.console
 * and internal structure because its read from the Domino server using
 * Java serialization.
 */
public class MsgFormat implements Serializable {
  /**
  * 
  */
  private static final long serialVersionUID = 1L;
  public static String revStr = "JSC1.0"; //$NON-NLS-1$
  public static String[] MsgFormats = new String[] { "Normal", "Compressed", "Encrypted" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  public static final int MF_DOMINO = 0;
  public static final int MF_CONTROLLER = 1;
  public static final int MF_SHELL = 2;
  public static final int MF_SERVER_LIST = 3;
  public static final int MF_GROUP_LIST = 4;
  public static final int MF_FILE_XFER = 5;
  public static final int MF_ERROR_MSG = 6;
  public static final int MF_DOMINO_STATUS = 7;
  public static final int MF_PROC_LIST = 8;
  public static final int MF_ADMIN_LIST = 9;
  public static final int MF_NORMAL = 0;
  public static final int MF_COMPRESSED = 1;
  public static final int MF_ENCRYPTED = 2;
  public int msgType;
  public String consoleId;
  public String revision = MsgFormat.revStr;
  public int length;
  public String msgFmt;
  public String data;

  public MsgFormat(final String consoleId) {
    this(consoleId, 0, null, 0, MsgFormat.MsgFormats[0]);
  }

  public MsgFormat(final String consoleId, final int length, final String data) {
    this(consoleId, length, data, 0, MsgFormat.MsgFormats[0]);
  }

  public MsgFormat(final String consoleId, final int length, final String data, final int msgType) {
    this(consoleId, length, data, msgType, MsgFormat.MsgFormats[0]);
  }

  public MsgFormat(final String consoleId, final int length, final String data, final int msgType, final String msgFmt) {
    this.msgFmt = msgFmt;
    this.msgType = msgType;
    this.length = length;
    this.consoleId = consoleId;
    this.data = data;
  }

  private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    final String string = (String) in.readObject();
  }

  public void setData(final String data) {
    this.setData(data, data == null ? 0 : data.length());
  }

  public void setData(final String data, final int length) {
    this.data = data;
    this.length = length;
  }

  public void setFormat(final String msgFmt) {
    this.msgFmt = msgFmt;
  }

  public void setType(final int msgType) {
    this.msgType = msgType;
  }

  private void writeObject(final ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeObject("JavaConsole"); //$NON-NLS-1$
  }
}
