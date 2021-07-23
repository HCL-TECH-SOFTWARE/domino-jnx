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
import com.hcl.domino.DominoClient;
import com.hcl.domino.exception.ObjectDisposedException;
import com.hcl.domino.exception.QuitPendingException;
import com.hcl.domino.mq.MessageQueue;
import com.hcl.domino.server.RunJavaAddin;
import com.hcl.domino.server.ServerStatusLine;

/**
 * Example RunJava task using only the JNX API.
 * 
 * @author Jesse Gallagher
 * @since 1.0.11
 */
public class JNXRunJava extends RunJavaAddin {
	
	public static void main(String[] args) {
		Thread t = new JNXRunJava();
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			System.out.println("Interrupted - exiting");
		}
	}
	
	private int messageCount;
	
	public JNXRunJava() {
		super("JNXRunJava");
	}
	
	@Override
	public void runAddin(DominoClient client, ServerStatusLine statusLine, MessageQueue queue) {
		System.out.println("Hello from RunJava?");
		System.out.println("I'm running as " + client.getEffectiveUserName());
		
		String message;
		try {
			while((message = queue.take()) != null) {
				System.out.println("Received message " + message);
				statusLine.setLine("Processed count: " + ++this.messageCount);
			}
		} catch (InterruptedException | ObjectDisposedException | QuitPendingException e) {
			// This occurs during shutdown
		}
	}
}
