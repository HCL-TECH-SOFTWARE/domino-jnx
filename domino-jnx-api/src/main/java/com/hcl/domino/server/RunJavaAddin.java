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
package com.hcl.domino.server;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.DominoProcess;
import com.hcl.domino.misc.JNXThread;
import com.hcl.domino.mq.MessageQueue;

/**
 * This base class can be extended to implement a server addin compatible
 * with {@code runjava}.
 *
 * @author Jesse Gallagher
 * @since 1.0.11
 */
public abstract class RunJavaAddin extends JNXThread {

  private final String addinName;
  private final String queueName;

  /**
   * Initializes the addin with the provided name, which is also
   * used as the message-queue name.
   *
   * @param addinName the name of the addin to show in the task list
   */
  public RunJavaAddin(final String addinName) {
    this(addinName, addinName);
  }

  /**
   * Initializes the addin with the provided name and message-queue name
   *
   * @param addinName the name of the addin to show in the task list
   * @param queueName the name of the queue to create
   */
  public RunJavaAddin(final String addinName, final String queueName) {
    if (addinName == null || addinName.isEmpty()) {
      throw new IllegalArgumentException("addinName cannot be empty");
    }
    if (queueName == null || queueName.isEmpty()) {
      throw new IllegalArgumentException("queueName cannot be empty");
    }

    this.addinName = addinName;
    this.queueName = queueName;
  }

  @Override
  protected final void doRun() {
    DominoProcess.get().initializeProcess(new String[0]);
    try {
      try (DominoClient client = DominoClientBuilder.newDominoClient().build()) {
        try (ServerStatusLine line = client.getServerAdmin().createServerStatusLine(this.addinName)) {
          line.setLine("Running");
          try (MessageQueue queue = client.getMessageQueues().createAndOpen("MQ$" + this.queueName.toUpperCase(), 0)) { //$NON-NLS-1$
            this.runAddin(client, line, queue);
          } catch (final Exception e) {
            e.printStackTrace();
          }
        }
      }
    } finally {
      DominoProcess.get().terminateProcess();
    }

  }

  protected abstract void runAddin(DominoClient client, ServerStatusLine statusLine, MessageQueue queue);

  public void stopAddin() {

  }

}
