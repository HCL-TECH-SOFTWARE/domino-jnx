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

public interface IConsoleLine {

	@Override String toString();

	int getMsgSeqNum();

	String getTimeStamp();

	String getExecName();

	int getPid();

	long getTid();

	long getVTid();

	int getStatus();

	int getType();

	int getSeverity();

	int getColor();

	String getAddName();

	String getData();

	boolean isPasswordString();

	boolean isPromptString();
	
}
