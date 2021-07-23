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

  public static void main(final String[] args) {
    System.setProperty("jnx.noterm", "true"); //$NON-NLS-1$ //$NON-NLS-2$

    final String queueName = args[0];
    final int messageCount = Integer.parseInt(args[1]);

    boolean waitForQuitMsg = false;
    if (args.length > 2) {
      waitForQuitMsg = Boolean.valueOf(args[2]).booleanValue();
    }
    boolean sendQuitMsg = false;
    if (args.length > 3) {
      sendQuitMsg = Boolean.valueOf(args[3]).booleanValue();
    }

    Pattern acceptorPattern = null;
    if (args.length > 4 && args[4].length() > 0) {
      acceptorPattern = Pattern.compile(args[4]);
    }

    long maxUpTime = TimeUnit.MINUTES.toMillis(1);
    if (args.length > 5) {
      maxUpTime = Long.parseLong(args[5]);
    }

    final MessageConsumer consumer = new MessageConsumer(queueName, messageCount, waitForQuitMsg, sendQuitMsg, acceptorPattern);

    if (maxUpTime != -1) {
      final long fMaxUpTime = maxUpTime;

      final Thread killThread = new Thread() {
        @Override
        public final void run() {
          try {
            Thread.sleep(fMaxUpTime);
          } catch (final InterruptedException e) {
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

  private final String m_queueName;
  private final List<String> m_messages = new ArrayList<>();
  private boolean m_waitForQuitMsg = false;
  private boolean m_sendQuitMsg = false;
  private final int m_messageCount;
  private final Pattern m_acceptorPattern;

  public MessageConsumer(final String queueName, final int messageCount, final boolean waitForQuitMsg, final boolean sendQuitMsg) {
    this(queueName, messageCount, waitForQuitMsg, sendQuitMsg, (Pattern) null);
  }

  public MessageConsumer(final String queueName, final int messageCount, final boolean waitForQuitMsg, final boolean sendQuitMsg,
      final Pattern acceptorRegExPattern) {
    this.m_queueName = queueName;
    this.m_waitForQuitMsg = waitForQuitMsg;
    this.m_sendQuitMsg = sendQuitMsg;
    this.m_messageCount = messageCount;

    this.m_acceptorPattern = acceptorRegExPattern;
  }

  public MessageConsumer(final String queueName, final int messageCount, final boolean waitForQuitMsg, final boolean sendQuitMsg,
      final String acceptorRegExPattern) throws PatternSyntaxException {
    this(queueName, messageCount, waitForQuitMsg, sendQuitMsg,
        acceptorRegExPattern != null ? Pattern.compile(acceptorRegExPattern) : (Pattern) null);
  }

  @SuppressWarnings("nls")
  protected boolean acceptsMessage(final String message) throws Exception {
    if (this.m_acceptorPattern != null) {
      if (!this.m_acceptorPattern.matcher(message).matches()) {
        throw new Exception(MessageFormat.format("Consumer: Invalid message received: ''{0}'' does not match ''{1}''", message,
            this.m_acceptorPattern.pattern()));
      }
    }

    return true;
  }

  protected void afterAll(final MessageQueue queue) {
    // nothing
  }

  protected void afterPollAllMessages(final MessageQueue queue, final List<String> messages) {
    // nothing
  }

  protected void afterPollMessage(final MessageQueue queue, final String msg) {
    // nothing
  }

  protected void beforeAll(final MessageQueue queue) {
    // nothing
  }

  protected void beforePollMessage(final MessageQueue queue) {
    // nothing
  }

  @Override
  protected void doRun() throws Exception {
    final DominoClient client = this.getClient();

    final MessageQueue queue = client.getMessageQueues().open(this.m_queueName, false);

    try {
      this.beforeAll(queue);

      while ((!this.m_waitForQuitMsg || !queue.isQuitPending())
          && (this.m_messageCount < 0 || this.m_messages.size() < this.m_messageCount)) {
        this.beforePollMessage(queue);

        final String msg = queue.poll(10, TimeUnit.MILLISECONDS);

        if (msg != null) {
          if (this.acceptsMessage(msg)) {
            this.m_messages.add(msg);
            this.printMessage(msg);
          }
        }

        this.afterPollMessage(queue, msg);

        try {
          Thread.sleep(this.m_messageCount > 0 && this.m_messages.size() < this.m_messageCount ? 50 : 500);
        } catch (final InterruptedException e) {
          // ignored
        }
      }

      this.afterPollAllMessages(queue, this.m_messages);

      if (this.m_sendQuitMsg && !queue.isQuitPending()) {
        queue.putQuitMsg();
      }

      this.afterAll(queue);
    } finally {
      if (queue != null) {
        queue.close();
      }
    }
  }

  public List<String> getMessages() {
    return this.m_messages;
  }

  protected void printMessage(final String message) {
    System.out.println(message);
  }
}
