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
package com.hcl.domino.example.graalvm;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.DominoProcess;
import com.hcl.domino.DominoProcess.DominoThreadContext;

public class Main {
	public static void main(String[] args) {
		DominoProcess.get().initializeProcess(args);
		try {
			try(
				DominoThreadContext ctx = DominoProcess.get().initializeThread();
				DominoClient client = DominoClientBuilder.newDominoClient().build();
			) {
				
				System.out.println("Hello from GraalVM Native, running as " + client.getEffectiveUserName());
			}
		} finally {
			DominoProcess.get().terminateProcess();
		}
	}
}
