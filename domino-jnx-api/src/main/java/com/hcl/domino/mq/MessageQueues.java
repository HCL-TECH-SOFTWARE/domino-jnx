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
package com.hcl.domino.mq;

import com.hcl.domino.DominoException;

/**
 * @author t.b.d
 */
public interface MessageQueues {

  /**
   * Create a message queue with the specified name.
   *
   * @param queueName name to be assigned to the message queue
   * @param quota     Maximum number of messages that the queue may contain. Set
   *                  this to zero for the default maximum. The default maximum
   *                  number of messages that the queue may contain is MAXWORD.
   * @return the message queue
   */
  MessageQueue createAndOpen(String queueName, int quota);

  /**
   * Method to test whether a queue with a specified name exists (tries to open
   * the queue)
   *
   * @param queueName queue name
   * @return true if queue exists
   */
  boolean hasQueue(String queueName);

  /**
   * Open a message queue, get a handle to it, and increment the queue's reference
   * counter.
   * The handle is used by the functions that write to and read from the message
   * queue.
   *
   * @param queueName    name of the queue that is to be opened
   * @param createOnFail true to create the queue if it doesn't exist
   * @return the message queue
   * @throws DominoException if the queue doesn't exist and {@code createOnFail}
   *                         is {@code false}
   */
  MessageQueue open(String queueName, boolean createOnFail);

}
