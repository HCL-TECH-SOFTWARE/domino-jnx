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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.jna.test.AbstractJNARuntimeTest;
import com.hcl.domino.mq.MessageQueue;

/**
 * Tests for the Domino message-queue, that run in process, but partially
 * multi-threaded.
 * TODO: For the {@link #testBlockingOffer()} the queue is filled up to its
 * maximum, since only then
 * the offer-method is supposed to block. It may be worthwhile to mock this
 * blocking, to cut down on the test-duration.
 *
 * @author Tammo Riedinger
 */
@SuppressWarnings("nls")
public class TestMessageQueue extends AbstractJNARuntimeTest {

  /**
   * A consumer that can be run in another thread, be paused and consume a message
   * after a given interval.
   * 
   * @author Tammo Riedinger
   */
  private class ThreadedConsumer extends MessageConsumer {
    private final AtomicLong pausedUntil = new AtomicLong(-1);

    public ThreadedConsumer(final String queueName, final int messageCount, final boolean waitForQuitMsg,
        final boolean sendQuitMsg) {
      super(queueName, messageCount, waitForQuitMsg, sendQuitMsg);
    }

    @Override
    protected void beforePollMessage(final MessageQueue queue) {
      while (this.isPaused()) {
        try {
          Thread.yield();
          Thread.sleep(10);
        } catch (final InterruptedException e) {
          // ignored
        }
      }
      this.pausedUntil.set(-1); // immediately wait for the next delay
    }

    public void consumeAfter(final long ms) {
      this.pausedUntil.set(System.currentTimeMillis() + ms);
    }

    public boolean isPaused() {
      final long restartTime = this.pausedUntil.get();

      return restartTime == -1 || restartTime > System.currentTimeMillis();
    }

    @Override
    protected void printMessage(final String message) {
      // do not print
    }
  }

  /**
   * A producer that can be run in another thread, be paused and produce a message
   * after a given interval.
   * 
   * @author Tammo Riedinger
   */
  private class ThreadedProducer extends MessageProducer {
    private final AtomicLong pausedUntil = new AtomicLong(-1);

    public ThreadedProducer(final String queueName, final String msgFormat, final int messageCount, final boolean waitForQuitMsg,
        final boolean sendQuitMsg) {
      super(queueName, msgFormat, messageCount, waitForQuitMsg, sendQuitMsg);
    }

    @Override
    protected void beforeOfferMessage(final MessageQueue queue, final String msg) {
      while (this.isPaused()) {
        try {
          Thread.yield();
          Thread.sleep(10);
        } catch (final InterruptedException e) {
          // ignored
        }
      }
      this.pausedUntil.set(-1); // immediately wait for the next delay
    }

    public boolean isPaused() {
      final long restartTime = this.pausedUntil.get();

      return restartTime == -1 || restartTime > System.currentTimeMillis();
    }

    @Override
    protected void printMessage(final String message) {
      // do not print
    }

    public void produceAfter(final long ms) {
      this.pausedUntil.set(System.currentTimeMillis() + ms);
    }
  }

  /**
   * Some tool class to measure execution times.
   * 
   * @author Tammo Riedinger
   * @param <R> type to be returned by the callable
   */
  private static abstract class TimedCallable<R> implements Callable<R> {
    private long elapsed = -1;
    private Exception exception = null;
    private long tolerance = 0;

    public TimedCallable(final long tolerance) {
      this.tolerance = tolerance;
    }

    @SuppressWarnings("unused")
    public void assertEqual(final long expected, final String message) {
      this.assertNotFailed();

      if (this.elapsed != expected) {
        if (Math.abs(this.elapsed - expected) > this.tolerance) { // is it within the tolerance?
          Assertions.fail(message + " ==> " + this.elapsed + "ms not equal to " + expected + "ms");
        }
      }
    }

    public void assertGreater(final long expected, final String message) {
      this.assertNotFailed();

      if (this.elapsed <= expected) {
        if (expected - this.elapsed > this.tolerance) { // is it within the tolerance?
          Assertions.fail(message + " ==> " + this.elapsed + "ms not greater than " + expected + "ms");
        }
      }
    }

    public void assertGreaterOrEqual(final long expected, final String message) {
      this.assertNotFailed();

      if (this.elapsed < expected) {
        if (expected - this.elapsed > this.tolerance) { // is it within the tolerance?
          Assertions.fail(message + " ==> " + this.elapsed + "ms not greater than or equal to " + expected + "ms");
        }
      }
    }

    public void assertLesser(final long expected, final String message) {
      this.assertNotFailed();

      if (this.elapsed >= expected) {
        if (this.elapsed - expected > this.tolerance) { // is it within the tolerance?
          Assertions.fail(message + " ==> " + this.elapsed + "ms not less than " + expected + "ms");
        }
      }
    }

    @SuppressWarnings("unused")
    public void assertLesserOrEqual(final long expected, final String message) {
      this.assertNotFailed();

      if (this.elapsed > expected) {
        if (this.elapsed - expected > this.tolerance) { // is it within the tolerance?
          Assertions.fail(message + " ==> " + this.elapsed + "ms not less than or equal to " + expected + "ms");
        }
      }
    }

    private void assertNotFailed() {
      if (this.exception != null) {
        Assertions.fail("Callable failed with " + this.exception.getMessage(), this.exception);
      }
    }

    @Override
    public R call() {
      final long start = System.currentTimeMillis();

      try {
        return this.doCall();
      } catch (final Exception e) {
        this.exception = e;
      } finally {
        this.elapsed = System.currentTimeMillis() - start;
      }
      return null;
    }

    public abstract R doCall() throws Exception;
  }

  private boolean containsNone(final List<String> expected, final List<String> actual) {
    final Set<String> allValues = new HashSet<>(actual);

    for (final String element : expected) {
      if (allValues.contains(element)) {
        return false;
      }
    }
    return true;
  }

  @Test
  public void testAddAndDrainMessages() throws IOException {
    final DominoClient client = this.getClient();

    final String queueName = UUID.randomUUID().toString();

    MessageQueue queue = client.getMessageQueues().open(queueName, true);

    try {
      final ArrayList<String> messages = new ArrayList<>();
      final ArrayList<String> removedMessages = new ArrayList<>();

      for (int i = 0; i < 10; i++) {
        messages.add("msg_" + i);
      }

      for (final String s : messages.subList(0, 5)) {
        queue.add(s);
      }

      for (final String s : messages.subList(5, 10)) {
        queue.offer(s);
      }

      Assertions.assertTrue(!queue.isEmpty(), "Message-queue is still empty");
      Assertions.assertTrue(queue.containsAll(messages), "Message-queue does not contain all messages");

      // verify retain
      for (int j = 0; j < 2; j++) {
        removedMessages.add(messages.remove(0));
      }
      Assertions.assertTrue(queue.retainAll(messages), "List did not change");

      Assertions.assertTrue(queue.containsAll(messages), "Message-queue does not contain all retained messages");

      // throw in this, in order to test the iterator
      Assertions.assertIterableEquals(queue, messages, "Message-queue iterator does not return all expected messages");

      final int remainingCapacity = queue.remainingCapacity();

      Assertions.assertTrue(remainingCapacity == MessageQueue.MAX_MESSAGE_COUNT - messages.size(), "Not enough remaining capacity");

      // verify draining
      final ArrayList<String> drainedMessages = new ArrayList<>();
      Assertions.assertTrue(queue.drainTo(drainedMessages) == messages.size(), "Not all elements have been drained");

      Assertions.assertTrue(queue.isEmpty(), "Queue is not empty");

      Assertions.assertIterableEquals(messages, drainedMessages, "Drained messages do not match");

      Assertions.assertTrue(this.containsNone(removedMessages, drainedMessages), "Still contained removed messages");

      // verify clearing
      queue.clear();
      Assertions.assertEquals(0, queue.size(), "Message-queue is not empty");

      // verify size()
      for (final String s : messages) {
        queue.offer(s);
      }
      Assertions.assertEquals(messages.size(), queue.size(), "Message-queue does not contain enough elements");

      // verify element()
      Assertions.assertEquals(messages.get(0), queue.element(), "Message-queue element() differs");

      // verify close()
      queue.close();
      queue = null;

      Assertions.assertTrue(!client.getMessageQueues().hasQueue(queueName), "Message-queue has not been removed");
    } finally {
      if (queue != null) {
        queue.close();
      }
    }
  }

  @Test
  public void testBlockingOffer() throws IOException {
    final DominoClient client = this.getClient();

    final String queueName = UUID.randomUUID().toString();
    final MessageQueue queue = client.getMessageQueues().open(queueName, true);

    // test blocking features by a MessageConsumer from another thread, that will
    // created new messages only when it is instructed
    final ThreadedConsumer consumer = new ThreadedConsumer(queueName, 100, false, false);
    final Thread consumerThread = new Thread(consumer, "Delayed Consumer");
    consumerThread.start();

    try {
      // send a message after 1 second
      // and wait for take to fetch it
      final long waitTime = 50;

      // fill up the queue to maximum
      long counter = 0;
      do {
        if (counter > MessageQueue.MAX_MESSAGE_COUNT) {
          Assertions.fail("Too many values in queue ==> " + counter + " > " + MessageQueue.MAX_MESSAGE_COUNT);
        }
      } while (queue.offer("Message " + counter++));

      Assertions.assertEquals(0, queue.remainingCapacity(), "There should be no capacity available");

      // and see if it blocks until we removed an element
      final TimedCallable<Boolean> offerCallable = new TimedCallable<Boolean>(15) {
        @Override
        public Boolean doCall() throws Exception {
          return queue.offer("Message x", waitTime * 2, TimeUnit.MILLISECONDS);
        }
      };

      // verify blocking while appending a message to a full queue repeatedly
      for (int i = 0; i < 10; i++) {
        // removal has to be performed in another thread
        consumer.consumeAfter(waitTime);
        final Boolean result = offerCallable.call();

        Assertions.assertEquals(result, Boolean.valueOf(true), "The message was not added to the queue");
        final int size = queue.size();
        if (size > MessageQueue.MAX_MESSAGE_COUNT) {
          Assertions.fail("Too many values in queue ==> " + size + " > " + MessageQueue.MAX_MESSAGE_COUNT);
        }
        offerCallable.assertGreaterOrEqual(waitTime, "Offer did not block for the expected time");
        offerCallable.assertLesser(waitTime * 2, "Offer took longer than the expected time");
      }
    } finally {
      if (queue != null) {
        queue.close();
      }
    }
  }

  @Test
  public void testBlockingPoll() throws IOException {
    final DominoClient client = this.getClient();

    final String queueName = UUID.randomUUID().toString();

    final int msgCount = 10;
    final MessageQueue queue = client.getMessageQueues().open(queueName, true);

    // test blocking features by a MessageProducer from another thread, that will
    // created new messages only when it is instructed
    final ThreadedProducer producer = new ThreadedProducer(queueName, "Message to %s: %d", 1000, false, false);
    final Thread producerThread = new Thread(producer, "Delayed Producer");
    producerThread.start();

    try {
      // send a message after 1 second
      // and wait for take to fetch it
      final long waitTime = 1000;

      final TimedCallable<String> takeCallable = new TimedCallable<String>(15) {
        @Override
        public String doCall() throws Exception {
          return queue.take();
        }
      };

      // verify blocking poll for a couple of times
      for (int i = 0; i < Math.floor(msgCount / 2); i++) {
        Assertions.assertTrue(queue.isEmpty(), "Message-queue is not empty");

        producer.produceAfter(waitTime);

        String delayedMsg = takeCallable.call();

        takeCallable.assertGreaterOrEqual(waitTime, "Take did not block for the expected time");
        Assertions.assertNotNull(delayedMsg, "A message was expected to be taken from the queue");

        // send another message after 1 second
        // and verify that poll times out before 1 second
        final TimedCallable<String> pollCallable = new TimedCallable<String>(15) {
          @Override
          public String doCall() throws Exception {
            return queue.poll(waitTime / 2, TimeUnit.MILLISECONDS);
          }
        };
        producer.produceAfter(waitTime);

        delayedMsg = pollCallable.call();

        pollCallable.assertLesser(waitTime, "Poll blocked longer than the expected time");
        Assertions.assertNull(delayedMsg, "A message was not expected to be taken from the queue");
        pollCallable.assertGreater((long) Math.floor(waitTime / 2.5), "Poll blocked less than the expected time");

        while (delayedMsg == null) {
          // poll another time to verify that it blocked long enough
          delayedMsg = pollCallable.call();

          pollCallable.assertLesser(waitTime / 2, "Poll blocked longer than the expected time");
          if (delayedMsg == null) {
            pollCallable.assertGreater((long) Math.floor(waitTime / 2.5), "Poll blocked less than the expected time");
          }
        }
      }

      Assertions.assertNull(queue.peek(), "Queue should be empty");

      // verify blocking peek
      long counter = 0;
      String topMsg = null;

      producer.produceAfter(waitTime);

      do {
        topMsg = queue.peek();

        try {
          Thread.sleep(10);
        } catch (final InterruptedException e) {
          // ignored
        }

        counter++;
      } while (topMsg == null);
      Assertions.assertTrue(counter > 0, "Peek should have taken some time to succeed");

      Assertions.assertEquals(topMsg, queue.poll(), "The last peek should return the same element as poll");
      Assertions.assertTrue(queue.isEmpty(), "Message-queue is not empty");
    } finally {
      if (queue != null) {
        queue.close();
      }
    }
  }
}
