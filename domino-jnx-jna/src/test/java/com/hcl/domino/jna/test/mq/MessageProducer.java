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
package com.hcl.domino.jna.test.mq;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.hcl.domino.DominoClient;
import com.hcl.domino.mq.MessageQueue;

public class MessageProducer extends AbstractMessageQueueRunner {

	public static void main(String[] args) {
		System.setProperty("jnx.noterm", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		
		String queueName = args[0];
		String msgFormat = args[1];
		int messageCount = Integer.parseInt(args[2]);
		
		boolean waitForQuitMsg=false;
		if (args.length>3) {
			waitForQuitMsg = Boolean.valueOf(args[3]).booleanValue();
		}
		boolean sendQuitMsg=false;
		if (args.length>4) {
			sendQuitMsg = Boolean.valueOf(args[4]).booleanValue();
		}
		
		long maxUpTime = TimeUnit.MINUTES.toMillis(1);
		if (args.length>5) {
			maxUpTime=Long.parseLong(args[5]);
		}
		
		MessageProducer producer=new MessageProducer(queueName, msgFormat, messageCount, waitForQuitMsg, sendQuitMsg);
		
		if (maxUpTime!=-1) {
			final long fMaxUpTime=maxUpTime;
			
			Thread killThread=new Thread() {
				@Override
				public final void run() {
					try {
						Thread.sleep(fMaxUpTime);
					}
					catch (InterruptedException e) {
						// ignored
					}
					System.exit(2);
				}
			};
			
			killThread.start();
		}
		
		producer.run();

		System.exit(producer.hasError() ? -1 : 0);
	}
	
	private String m_queueName;
	private List<String> m_messages=new ArrayList<String>();
	private boolean m_waitForQuitMsg=false;
	private boolean m_sendQuitMsg=false;
	
	public MessageProducer(String queueName, String msgFormat, int messageCount, boolean waitForQuitMsg, boolean sendQuitMsg) {
		m_queueName = queueName;
		m_waitForQuitMsg = waitForQuitMsg;
		m_sendQuitMsg = sendQuitMsg;
		
		for (int i=1;i<=messageCount;i++) {
			m_messages.add(String.format(msgFormat, queueName, i, messageCount));
		}
	}
	
	public List<String> getMessages() {
		return m_messages;
	}

	@Override
	protected void doRun() throws Exception {
		DominoClient client = getClient();
		
		try(MessageQueue queue = client.getMessageQueues().open(m_queueName, false)) {
			beforeAll(queue);
			
			ArrayList<String> addedMessages=new ArrayList<String>();
			
			for (String s:m_messages) {
				beforeOfferMessage(queue, s);
				
				boolean result=queue.offer(s);
				if (result) {
					addedMessages.add(s);
					printMessage(s);
				}
				
				afterOfferMessage(queue, result);
			}
			
			if (m_waitForQuitMsg) {
				while (!queue.isQuitPending()) {
					try {
						Thread.sleep(500);
					}
					catch (InterruptedException e) {
						// ignored
					}
					Thread.yield();
				}
			}
			
			afterOfferAllMessages(queue, addedMessages);
			
			if (m_sendQuitMsg && !queue.isQuitPending()) {
				queue.putQuitMsg();
			}
			
			afterAll(queue);
		}
	}
	
	protected void beforeAll(MessageQueue queue) {
		// nothing
	}
	
	protected void afterAll(MessageQueue queue) {
		// nothing
	}
	
	protected void beforeOfferMessage(MessageQueue queue, String msg) {
		// nothing
	}
	
	protected void afterOfferMessage(MessageQueue queue, boolean success) {
		// nothing
	}
	
	protected void afterOfferAllMessages(MessageQueue queue, List<String> messages) {
		// nothing
	}
	
	protected void printMessage(String message) {
		System.out.println(message);
	}
}
