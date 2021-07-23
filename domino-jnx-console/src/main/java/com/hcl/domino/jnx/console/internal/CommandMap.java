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

/**
 * Container for a console command
 */
public class CommandMap {
  private String cmd;
  private byte[] data;
  private String dest;
  private boolean group;
  private int type;
  private int indx;

  String getCommand() {
    return this.cmd;
  }

  byte[] getData() {
    return this.data;
  }

  String getDestination() {
    return this.dest;
  }

  int getIndex() {
    return this.indx;
  }

  int getType() {
    return this.type;
  }

  boolean isGroup() {
    return this.group;
  }

  void setCommand(final String cmd) {
    this.cmd = cmd;
  }

  void setData(final byte[] data) {
    this.data = data;
  }

  void setData(final byte[] data, final int len) {
    this.data = new byte[len];
    System.arraycopy(data, 0, this.data, 0, len);
  }

  void setDestination(final String dest) {
    this.dest = dest;
  }

  void setGroup(final boolean b) {
    this.group = b;
  }

  void setIndex(final int n) {
    this.indx = n;
  }

  void setType(final int type) {
    this.type = type;
  }
}
