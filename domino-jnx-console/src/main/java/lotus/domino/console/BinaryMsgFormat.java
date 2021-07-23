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
 * Extension of {@link MsgFormat} that contains binary data.<br>
 * <br>
 * Please note that it's important that this class keeps its Java package
 * lotus.domino.console
 * and internal structure because its read from the Domino server using
 * Java serialization.
 */
@SuppressWarnings("serial")
public class BinaryMsgFormat extends MsgFormat implements Serializable {
  /**
  * 
  */
  private static final long serialVersionUID = 1L;
  public byte[] bdata;
  public String svcname;

  public BinaryMsgFormat(final String consoleId) {
    this(consoleId, 0, (byte[]) null, 0, MsgFormat.MsgFormats[0]);
  }

  public BinaryMsgFormat(final String consoleId, final int length, final byte[] bdata) {
    this(consoleId, length, bdata, 13, MsgFormat.MsgFormats[0]);
  }

  public BinaryMsgFormat(final String consoleId, final int length, final byte[] bdata, final int msgType) {
    this(consoleId, length, bdata, msgType, MsgFormat.MsgFormats[0]);
  }

  public BinaryMsgFormat(final String consoleId, final int length, final byte[] bdata, final int msgType, final String msgFmt) {
    super(consoleId, length, null, msgType, msgFmt);
    this.bdata = bdata;
    this.svcname = null;
  }

  public byte[] bdata() {
    return this.bdata;
  }

  private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
  }

  public void setBData(final String bdata) {
    this.bdata = null;
    super.setData(bdata);
  }

  public void setData(final byte[] bdata) {
    this.setData(bdata, bdata == null ? 0 : bdata.length);
  }

  public void setData(final byte[] bdata, final int length) {
    this.bdata = bdata;
    this.length = length;
    super.setData(null);
  }

  public String svcname() {
    return this.svcname;
  }

  public void svcname(final String svcname) {
    this.svcname = svcname;
  }

  private void writeObject(final ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
  }
}
