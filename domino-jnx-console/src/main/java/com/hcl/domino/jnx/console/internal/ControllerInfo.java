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

/**
 * Container for an open server controller connection and its index
 * in the list of opened connections (we currently only support
 * one connection per {@link DominoConsoleRunner}.
 */
public class ControllerInfo {
  private int index;
  private String serverName;

  int getIndex() {
    return this.index;
  }

  String getServerName() {
    return this.serverName;
  }

  void setIndex(final int n) {
    this.index = n;
  }

  void setServerName(final String serverName) {
    this.serverName = serverName;
  }

}
