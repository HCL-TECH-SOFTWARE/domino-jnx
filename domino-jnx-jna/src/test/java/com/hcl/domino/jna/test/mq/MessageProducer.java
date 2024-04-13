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
package com.hcl.domino.jna.test.mq;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.hcl.domino.DominoClient;
import com.hcl.domino.commons.util.DominoUtils;
import com.hcl.domino.mq.MessageQueue;

public class MessageProducer extends AbstractMessageQueueRunner {

  public static void main(final String[] args) {
    DominoUtils.setNoTerm(true);

    final String queueName = args[0];
    final String msgFormat = args[1];
    final int messageCount = Integer.parseInt(args[2]);

    boolean waitForQuitMsg = false;
    if (args.length > 3) {
      waitForQuitMsg = Boolean.valueOf(args[3]).booleanValue();
    }
    boolean sendQuitMsg = false;
    if (args.length > 4) {
      sendQuitMsg = Boolean.valueOf(args[4]).booleanValue();
    }

    long maxUpTime = TimeUnit.MINUTES.toMillis(1);
    if (args.length > 5) {
      maxUpTime = Long.parseLong(args[5]);
    }

    final MessageProducer producer = new MessageProducer(queueName, msgFormat, messageCount, waitForQuitMsg, sendQuitMsg);

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

    producer.run();

    System.exit(producer.hasError() ? -1 : 0);
  }

  private final String m_queueName;
  private final List<String> m_messages = new ArrayList<>();
  private boolean m_waitForQuitMsg = false;
  private boolean m_sendQuitMsg = false;

  public MessageProducer(final String queueName, final String msgFormat, final int messageCount, final boolean waitForQuitMsg,
      final boolean sendQuitMsg) {
    this.m_queueName = queueName;
    this.m_waitForQuitMsg = waitForQuitMsg;
    this.m_sendQuitMsg = sendQuitMsg;

    for (int i = 1; i <= messageCount; i++) {
      this.m_messages.add(String.format(msgFormat, queueName, i, messageCount));
    }
  }

  protected void afterAll(final MessageQueue queue) {
    // nothing
  }

  protected void afterOfferAllMessages(final MessageQueue queue, final List<String> messages) {
    // nothing
  }

  protected void afterOfferMessage(final MessageQueue queue, final boolean success) {
    // nothing
  }

  protected void beforeAll(final MessageQueue queue) {
    // nothing
  }

  protected void beforeOfferMessage(final MessageQueue queue, final String msg) {
    // nothing
  }

  @Override
  protected void doRun() throws Exception {
    final DominoClient client = this.getClient();

    try (MessageQueue queue = client.getMessageQueues().open(this.m_queueName, false)) {
      this.beforeAll(queue);

      final ArrayList<String> addedMessages = new ArrayList<>();

      for (final String s : this.m_messages) {
        this.beforeOfferMessage(queue, s);

        final boolean result = queue.offer(s);
        if (result) {
          addedMessages.add(s);
          this.printMessage(s);
        }

        this.afterOfferMessage(queue, result);
      }

      if (this.m_waitForQuitMsg) {
        while (!queue.isQuitPending()) {
          try {
            Thread.sleep(500);
          } catch (final InterruptedException e) {
            // ignored
          }
          Thread.yield();
        }
      }

      this.afterOfferAllMessages(queue, addedMessages);

      if (this.m_sendQuitMsg && !queue.isQuitPending()) {
        queue.putQuitMsg();
      }

      this.afterAll(queue);
    }
  }

  public List<String> getMessages() {
    return this.m_messages;
  }

  protected void printMessage(final String message) {
    System.out.println(message);
  }
}
