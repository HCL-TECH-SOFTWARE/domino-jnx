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
package lotus.domino.console;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Data received from the server controller socket.<br>
 * <br>
 * Please note that it's important that this class keeps its Java package lotus.domino.console
 * and internal structure because its read from the Domino server using
 * Java serialization.
 */
@SuppressWarnings("serial")
public class MsgFormat implements Serializable {
    public int msgType;
    public String consoleId;
    public String revision = revStr;
    public int length;
    public String msgFmt;
    public String data;
    public static String revStr = "JSC1.0";
    public static String[] MsgFormats = new String[]{"Normal", "Compressed", "Encrypted"};
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

    public MsgFormat(String consoleId) {
        this(consoleId, 0, null, 0, MsgFormats[0]);
    }

    public void setData(String data, int length) {
        this.data = data;
        this.length = length;
    }

    public void setData(String data) {
        this.setData(data, data == null ? 0 : data.length());
    }

    public void setType(int msgType) {
        this.msgType = msgType;
    }

    public void setFormat(String msgFmt) {
        this.msgFmt = msgFmt;
    }

    public MsgFormat(String consoleId, int length, String data) {
        this(consoleId, length, data, 0, MsgFormats[0]);
    }

    public MsgFormat(String consoleId, int length, String data, int msgType) {
        this(consoleId, length, data, msgType, MsgFormats[0]);
    }

    public MsgFormat(String consoleId, int length, String data, int msgType, String msgFmt) {
        this.msgFmt = msgFmt;
        this.msgType = msgType;
        this.length = length;
        this.consoleId = consoleId;
        this.data = data;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject("JavaConsole");
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        String string = (String)in.readObject();
    }
}

