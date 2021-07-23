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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.jna.test.AbstractJNARuntimeTest;
import com.hcl.domino.mq.MessageQueue;

/**
 * Tests for the Domino message-queue, that run in process, but partially multi-threaded.
 * 
 * TODO: For the {@link #testBlockingOffer()} the queue is filled up to its maximum, since only then
 * the offer-method is supposed to block. It may be worthwhile to mock this blocking, to cut down on the test-duration.
 * 
 * @author Tammo Riedinger
 */
@SuppressWarnings("nls")
public class TestMessageQueue extends AbstractJNARuntimeTest {

	@Test
	public void testAddAndDrainMessages() throws IOException {
		DominoClient client = getClient();
		
		String queueName=UUID.randomUUID().toString();

		MessageQueue queue = client.getMessageQueues().open(queueName, true);
		
		try {
			ArrayList<String> messages=new ArrayList<String>();
			ArrayList<String> removedMessages=new ArrayList<String>();
			
			for (int i=0;i<10;i++) {
				messages.add("msg_" + i);
			}
			
			for (String s:messages.subList(0, 5)) {
				queue.add(s);
			}
			
			for (String s:messages.subList(5, 10)) {
				queue.offer(s);
			}
			
			assertTrue(!queue.isEmpty(), "Message-queue is still empty");
			assertTrue(queue.containsAll(messages), "Message-queue does not contain all messages");
			
			// verify retain
			for (int j=0;j<2;j++) {
				removedMessages.add(messages.remove(0));
			}
			assertTrue(queue.retainAll(messages), "List did not change");
			
			assertTrue(queue.containsAll(messages), "Message-queue does not contain all retained messages");

			// throw in this, in order to test the iterator
			assertIterableEquals(queue, messages, "Message-queue iterator does not return all expected messages");
			
			int remainingCapacity=queue.remainingCapacity();
			
			assertTrue(remainingCapacity==MessageQueue.MAX_MESSAGE_COUNT-messages.size(), "Not enough remaining capacity");
			
			// verify draining
			ArrayList<String> drainedMessages=new ArrayList<String>();
			assertTrue(queue.drainTo(drainedMessages)==messages.size(), "Not all elements have been drained");
			
			assertTrue(queue.isEmpty(), "Queue is not empty");
			
			assertIterableEquals(messages, drainedMessages, "Drained messages do not match");
			
			assertTrue(containsNone(removedMessages, drainedMessages), "Still contained removed messages");
			
			// verify clearing
			queue.clear();
			assertEquals(0, queue.size(), "Message-queue is not empty");
			
			// verify size()
			for (String s:messages) {
				queue.offer(s);
			}
			assertEquals(messages.size(), queue.size(), "Message-queue does not contain enough elements");

			// verify element()
			assertEquals(messages.get(0), queue.element(), "Message-queue element() differs");
			
			// verify close()
			queue.close();
			queue=null;
			
			assertTrue(!client.getMessageQueues().hasQueue(queueName), "Message-queue has not been removed");
		}
		finally {
			if (queue!=null) {
				queue.close();
			}
		}
	}
	
	@Test
	public void testBlockingPoll() throws IOException {
		DominoClient client = getClient();
		
		String queueName=UUID.randomUUID().toString();

		int msgCount=10;
		final MessageQueue queue=client.getMessageQueues().open(queueName, true);

		// test blocking features by a MessageProducer from another thread, that will created new messages only when it is instructed
		ThreadedProducer producer = new ThreadedProducer(queueName, "Message to %s: %d", 1000, false, false);
		Thread producerThread=new Thread(producer, "Delayed Producer");
		producerThread.start();

		try {
			// send a message after 1 second
			// and wait for take to fetch it
			long waitTime=1000;
			
			TimedCallable<String> takeCallable=new TimedCallable<String>(15) {
				@Override
				public String doCall() throws Exception {
					return queue.take();
				}
			};
			
			// verify blocking poll for a couple of times
			for (int i=0;i<Math.floor(msgCount/2);i++) {
				assertTrue(queue.isEmpty(), "Message-queue is not empty");
			
				producer.produceAfter(waitTime);
			
				String delayedMsg=takeCallable.call();
				
				takeCallable.assertGreaterOrEqual(waitTime, "Take did not block for the expected time");
				assertNotNull(delayedMsg, "A message was expected to be taken from the queue");
				
				// send another message after 1 second
				// and verify that poll times out before 1 second
				TimedCallable<String> pollCallable=new TimedCallable<String>(15) {
					@Override
					public String doCall() throws Exception {
						return queue.poll(waitTime/2, TimeUnit.MILLISECONDS);
					}
				};
				producer.produceAfter(waitTime);
				
				delayedMsg=pollCallable.call();
				
				pollCallable.assertLesser(waitTime, "Poll blocked longer than the expected time");
				assertNull(delayedMsg, "A message was not expected to be taken from the queue");
				pollCallable.assertGreater((long)Math.floor(waitTime/2.5), "Poll blocked less than the expected time");
				
				
				while (delayedMsg==null) {
					// poll another time to verify that it blocked long enough
					delayedMsg=pollCallable.call();
					
					pollCallable.assertLesser(waitTime/2, "Poll blocked longer than the expected time");
					if (delayedMsg==null) {
						pollCallable.assertGreater((long)Math.floor(waitTime/2.5), "Poll blocked less than the expected time");
					}
				}
			}
			
			assertNull(queue.peek(), "Queue should be empty");
			
			// verify blocking peek
			long counter=0;
			String topMsg=null;
			
			producer.produceAfter(waitTime);

			do {
				topMsg=queue.peek();
				
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// ignored
				}

				counter++;
			} while(topMsg==null);
			assertTrue(counter>0, "Peek should have taken some time to succeed");
			
			assertEquals(topMsg, queue.poll(), "The last peek should return the same element as poll");
			assertTrue(queue.isEmpty(), "Message-queue is not empty");
		}
		finally {
			if (queue!=null) {
				queue.close();
			}
		}
	}
	
	@Test
	public void testBlockingOffer() throws IOException {
		DominoClient client = getClient();
		
		String queueName=UUID.randomUUID().toString();
		final MessageQueue queue=client.getMessageQueues().open(queueName, true);

		// test blocking features by a MessageConsumer from another thread, that will created new messages only when it is instructed
		ThreadedConsumer consumer = new ThreadedConsumer(queueName, 100, false, false);
		Thread consumerThread=new Thread(consumer, "Delayed Consumer");
		consumerThread.start();
		
		try {
			// send a message after 1 second
			// and wait for take to fetch it
			long waitTime=50;
			

			// fill up the queue to maximum
			long counter=0;
			do  {
				if (counter>MessageQueue.MAX_MESSAGE_COUNT) {
					fail("Too many values in queue ==> " + counter + " > " + MessageQueue.MAX_MESSAGE_COUNT);
				}
			} while (queue.offer("Message " + (counter++)));
			
			assertEquals(0, queue.remainingCapacity(), "There should be no capacity available");
			
			// and see if it blocks until we removed an element
			TimedCallable<Boolean> offerCallable=new TimedCallable<Boolean>(15) {
				@Override
				public Boolean doCall() throws Exception {
					return queue.offer("Message x", waitTime*2, TimeUnit.MILLISECONDS);
				}
			};
			
			// verify blocking while appending a message to a full queue repeatedly
			for (int i=0;i<10;i++) {
				// removal  has to be performed in another thread
				consumer.consumeAfter(waitTime);
				Boolean result=offerCallable.call();
				
				assertEquals(result, Boolean.valueOf(true), "The message was not added to the queue");
				int size=queue.size();
				if (size>MessageQueue.MAX_MESSAGE_COUNT) {
					fail("Too many values in queue ==> " + size + " > " + MessageQueue.MAX_MESSAGE_COUNT);
				}
				offerCallable.assertGreaterOrEqual(waitTime, "Offer did not block for the expected time");
				offerCallable.assertLesser(waitTime*2, "Offer took longer than the expected time");
			}
		}
		finally {
			if (queue!=null) {
				queue.close();
			}
		}
	}
	
	private boolean containsNone(List<String> expected, List<String> actual) {
		Set<String> allValues=new HashSet<String>(actual);
		
		for (int i=0;i<expected.size();i++) {
			if (allValues.contains(expected.get(i))) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * A producer that can be run in another thread, be paused and produce a message after a given interval.
	 * 
	 * @author Tammo Riedinger
	 */
	private class ThreadedProducer extends MessageProducer {
		private AtomicLong pausedUntil=new AtomicLong(-1);
		
		public ThreadedProducer(String queueName, String msgFormat, int messageCount, boolean waitForQuitMsg,
				boolean sendQuitMsg) {
			super(queueName, msgFormat, messageCount, waitForQuitMsg, sendQuitMsg);
		}

		@Override
		protected void beforeOfferMessage(MessageQueue queue, String msg) {
			while (isPaused()) {
				try {
					Thread.yield();
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// ignored
				}
			}
			pausedUntil.set(-1); // immediately wait for the next delay
		}
		
		public boolean isPaused() {
			long restartTime=pausedUntil.get();
			
			return restartTime==-1 || restartTime>System.currentTimeMillis();
		}
		
		public void produceAfter(long ms) {
			pausedUntil.set(System.currentTimeMillis()+ms);
		}	
		
		protected void printMessage(String message) {
			// do not print
		}
	}
	
	/**
	 * A consumer that can be run in another thread, be paused and consume a message after a given interval.
	 * 
	 * @author Tammo Riedinger
	 */
	private class ThreadedConsumer extends MessageConsumer {
		private AtomicLong pausedUntil=new AtomicLong(-1);
		
		public ThreadedConsumer(String queueName, int messageCount, boolean waitForQuitMsg,
				boolean sendQuitMsg) {
			super(queueName, messageCount, waitForQuitMsg, sendQuitMsg);
		}

		@Override
		protected void beforePollMessage(MessageQueue queue) {
			while (isPaused()) {
				try {
					Thread.yield();
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// ignored
				}
			}
			pausedUntil.set(-1); // immediately wait for the next delay
		}
		
		public boolean isPaused() {
			long restartTime=pausedUntil.get();
			
			return restartTime==-1 || restartTime>System.currentTimeMillis();
		}
		
		public void consumeAfter(long ms) {
			pausedUntil.set(System.currentTimeMillis()+ms);
		}
		
		protected void printMessage(String message) {
			// do not print
		}
	}
	
	/**
	 * Some tool class to measure execution times.
	 * 
	 * @author Tammo Riedinger
	 *
	 * @param <R>		type to be returned by the callable
	 */
	private static abstract class TimedCallable<R> implements Callable<R> {
		private long elapsed=-1;
		private Exception exception=null;
		private long tolerance=0;
		
		public TimedCallable(long tolerance) {
			this.tolerance=tolerance;
		}
		
		@Override
		public R call() {
			long start=System.currentTimeMillis();
			
			try {
				return doCall();
			}
			catch (Exception e) {
				exception=e;
			}
			finally {
				elapsed=System.currentTimeMillis()-start;
			}
			return null;
		}

		private void assertNotFailed() {
			if (exception!=null) {
				fail("Callable failed with " + exception.getMessage(), exception);
			}
		}
		
		public void assertGreater(long expected, String message) {
			assertNotFailed();
			
			if (elapsed<=expected) {
				if ((expected-elapsed) > tolerance) { // is it within the tolerance?
					fail(message + " ==> " + elapsed + "ms not greater than " + expected + "ms");
				}
			}
		}
		
		public void assertGreaterOrEqual(long expected, String message) {
			assertNotFailed();
			
			if (elapsed<expected) {
				if ((expected-elapsed) > tolerance) { // is it within the tolerance?
					fail(message + " ==> " + elapsed + "ms not greater than or equal to " + expected + "ms");
				}
			}
		}
		
		public void assertLesser(long expected, String message) {
			assertNotFailed();
			
			if (elapsed>=expected) {
				if ((elapsed-expected) > tolerance) { // is it within the tolerance?
					fail(message + " ==> " + elapsed + "ms not less than " + expected + "ms");
				}
			}
		}
		
		@SuppressWarnings("unused")
		public void assertLesserOrEqual(long expected, String message) {
			assertNotFailed();
			
			if (elapsed>expected) {
				if ((elapsed-expected) > tolerance) { // is it within the tolerance?
					fail(message + " ==> " + elapsed + "ms not less than or equal to " + expected + "ms");
				}
			}
		}
		
		@SuppressWarnings("unused")
		public void assertEqual(long expected, String message) {
			assertNotFailed();
			
			if (elapsed!=expected) {
				if (Math.abs(elapsed-expected) > tolerance) { // is it within the tolerance?
					fail(message + " ==> " + elapsed + "ms not equal to " + expected + "ms");
				}
			}
		}
		
		public abstract R doCall() throws Exception;
	}
}
