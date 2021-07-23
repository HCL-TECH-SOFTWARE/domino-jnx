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
package com.hcl.domino.mq;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

/**
 * 
 * <p>Implementation note: due to the underlying MQ capabilities, this implementation of
 * {@link BlockingQueue} is not expected to implement the following methods:</p>
 * 
 * <ul>
 *   <li>{@link java.util.Collection#containsAll containsAll}</li>
 *   <li>{@link java.util.Queue#element element}</li>
 *   <li>{@link java.util.Queue#peek() peek}</li>
 *   <li>{@link java.util.Collection#removeAll removeAll}</li>
 *   <li>{@link java.util.Collection#retainAll retainAll}</li>
 *   <li>{@link java.util.Collection#contains contains}</li>
 *   <li>{@link java.util.Collection#remove remove}</li>
 * </ul>
 * 
 * @author t.b.d
 *
 */
public interface MessageQueue extends BlockingQueue<String>, Closeable {
	/**
	 * Maximum allowed message-count per queue.
	 * 
	 * TODO: verify if this is actually the case
	 */
	int MAX_MESSAGE_COUNT=65535;
	
	/**
	 * Returns the name of the queue as specified via {@link MessageQueues#createAndOpen(String, int)} or
	 * {@link MessageQueues#open(String, boolean)} respectively.
	 * 	
	 * @return		the name of the queue
	 */
	String getName();
	
	/**
	 * This function parses a message queue, calling an action routine for each message in the queue.<br>
	 * If the message queue is empty, or if all messages in the queue have been enumerated without
	 * returning an error code, MQScan returns ERR_MQ_EMPTY.<br>
	 * <br>
	 * In the simple case, MQScan() does not modify the contents of the queue;
	 * by returning the appropriate error codes to MQScan, the action routine can specify that
	 * messages are to be removed from the queue or skipped, or that enumeration is to be
	 * terminated immediately.  See the reference entry for {@link IMQCallback} for more details.<br>
	 * <br>
	 * Note: MQScan locks out all other message queue function calls until it completes.

	 * @param buffer buffer to be used to read messages, max size is 65326 bytes
	 * @param callback callback to be called for each message; if null, we dequeue the a message and return it in the specified buffer
	 * @return The number of bytes written to the buffer (important if <code>callback</code> has been set to null)
	 */
	int scan(byte[] buffer, final IMQCallback callback);
	
	/**
	 * This function puts the message queue in a QUIT state, which indicates to
	 * applications that read the message queue that they should terminate.
	 */
	void putQuitMsg();
	
	/**
	 * This function adds a message to the message queue.<br>
	 * The message will be placed in the queue according to the value of its priority argument -
	 * higher priority messages will be enqueued ahead of lower priority messages.<br>
	 * <br>
	 * If the queue is full or in a QUIT state, the message will not be put in the queue, and
	 * the function will return an appropriate error code.
	 * 
	 * @param buffer buffer containing the message.  Maximum buffer length is 65326 bytes
	 * @param priority priority
	 * @param offset offset in the buffer where the message starts
	 * @param length lengths of the message in the buffer
	 */
	void put(byte[] buffer, int priority, int offset, int length);
	
	/**
	 * This function tests whether the specified message queue is in a QUIT state, and returns TRUE if so. Otherwise, it returns FALSE.

	 * @return true if in quite state
	 */
	boolean isQuitPending();
	
	/**
	 * Callback interface to scan a message queue for new messages
	 */
	@FunctionalInterface
	public interface IMQCallback {
		public enum Action {
			/** Process the next message */
			Continue,
			/** Return from MQScan immediately without dequeueing a message.<br>
			 * Note that MQScan returns ERR_MQSCAN_ABORT */
			Abort,
			/** Remove the message from the queue, terminate the enumeration, and return
			 * the current message to the caller of MQScan.<br>
			 * If the Buffer is smaller than the message, MQScan can return ERR_MQ_BFR_TOO_SMALL. */
			Dequeue,
			/** Remove the current message from the message queue and continue the enumeration */
			Delete}

		/**
		 * Implement this method to read 
		 * 
		 * @param buffer read only byte buffer with message data
		 * @param priority priority
		 * @return what to do next
		 */
		Action dataReceived(ByteBuffer buffer, int priority);
	}
	
	@Override
	void close();
}
