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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.hcl.domino.DominoClient;
import com.hcl.domino.mq.MessageQueue;

public class MessageConsumer extends AbstractMessageQueueRunner {

	public static void main(String[] args) {
		System.setProperty("jnx.noterm", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		
		String queueName = args[0];
		int messageCount = Integer.parseInt(args[1]);
		
		boolean waitForQuitMsg=false;
		if (args.length>2) {
			waitForQuitMsg = Boolean.valueOf(args[2]).booleanValue();
		}
		boolean sendQuitMsg=false;
		if (args.length>3) {
			sendQuitMsg = Boolean.valueOf(args[3]).booleanValue();
		}

		Pattern acceptorPattern=null;
		if (args.length>4 && args[4].length()>0) {
			acceptorPattern=Pattern.compile(args[4]);
		}
		
		long maxUpTime = TimeUnit.MINUTES.toMillis(1);
		if (args.length>5) {
			maxUpTime=Long.parseLong(args[5]);
		}
		
		final MessageConsumer consumer=new MessageConsumer(queueName, messageCount, waitForQuitMsg, sendQuitMsg, acceptorPattern);
		
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
		consumer.run();

		System.exit(consumer.hasError() ? -1 : 0);
	}
	
	private String m_queueName;
	private List<String> m_messages=new ArrayList<String>();
	private boolean m_waitForQuitMsg=false;
	private boolean m_sendQuitMsg=false;
	private int m_messageCount;
	private Pattern m_acceptorPattern;
	
	public MessageConsumer(String queueName, int messageCount, boolean waitForQuitMsg, boolean sendQuitMsg) {
		this(queueName, messageCount, waitForQuitMsg, sendQuitMsg, (Pattern)null);
	}

	public MessageConsumer(String queueName, int messageCount, boolean waitForQuitMsg, boolean sendQuitMsg, String acceptorRegExPattern) throws PatternSyntaxException {
		this(queueName, messageCount, waitForQuitMsg, sendQuitMsg, (acceptorRegExPattern!=null) ? Pattern.compile(acceptorRegExPattern) : (Pattern)null);
	}
	
	public MessageConsumer(String queueName, int messageCount, boolean waitForQuitMsg, boolean sendQuitMsg, Pattern acceptorRegExPattern) {
		m_queueName = queueName;
		m_waitForQuitMsg = waitForQuitMsg;
		m_sendQuitMsg = sendQuitMsg;
		m_messageCount = messageCount;
		
		m_acceptorPattern = acceptorRegExPattern;
	}

	public List<String> getMessages() {
		return m_messages;
	}

	@Override
	protected void doRun() throws Exception {
		DominoClient client = getClient();
		
		MessageQueue queue = client.getMessageQueues().open(m_queueName, false);
		
		try {
			beforeAll(queue);
			
			while ((!m_waitForQuitMsg || !queue.isQuitPending()) && (m_messageCount<0 || m_messages.size()<m_messageCount)) {
				beforePollMessage(queue);
				
				String msg=queue.poll(10, TimeUnit.MILLISECONDS);
				
				if (msg!=null) {
					if (acceptsMessage(msg)) {
						m_messages.add(msg);
						printMessage(msg);
					}
				}
				
				afterPollMessage(queue, msg);
				
				try {
					Thread.sleep((m_messageCount>0 && m_messages.size()<m_messageCount) ? 50 : 500);
				}
				catch (InterruptedException e) {
					// ignored
				}
			}
			
			afterPollAllMessages(queue, m_messages);
			
			if (m_sendQuitMsg && !queue.isQuitPending()) {
				queue.putQuitMsg();
			}
			
			afterAll(queue);
		}
		finally {
			if (queue!=null) {
				queue.close();
			}
		}
	}
	
	protected void printMessage(String message) {
		System.out.println(message);
	}
	
	protected void beforeAll(MessageQueue queue) {
		// nothing
	}
	
	protected void afterAll(MessageQueue queue) {
		// nothing
	}
	
	protected void beforePollMessage(MessageQueue queue) {
		// nothing
	}
	
	protected void afterPollMessage(MessageQueue queue, String msg) {
		// nothing
	}
	
	protected void afterPollAllMessages(MessageQueue queue, List<String> messages) {
		// nothing
	}
	
	@SuppressWarnings("nls")
	protected boolean acceptsMessage(String message) throws Exception {
		if  (m_acceptorPattern!=null) {
			if (!m_acceptorPattern.matcher(message).matches()) {
				throw new Exception(MessageFormat.format("Consumer: Invalid message received: ''{0}'' does not match ''{1}''", message,
						m_acceptorPattern.pattern()));
			}
		}
		
		return true;
	}
}
