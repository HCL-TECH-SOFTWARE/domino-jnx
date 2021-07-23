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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.jna.test.AbstractJNARuntimeTest;
import com.hcl.domino.mq.MessageQueue;

/**
 * Perform real IPC-tests of the Domino message-queue, by "forking" another
 * JVM-process with similar environment
 * as the JVM currently running the junit-tests.
 * In this forked JVM a producer and consumer of messages to the same
 * message-queue will be fired up to verify
 * the messages get sent/received.
 *
 * @author Tammo Riedinger
 */
@SuppressWarnings("nls")
public class TestMessageQueueIPC extends AbstractJNARuntimeTest {

  /**
   * Interface to a forked process to receive its termination value (as a
   * future-result) and its io output.
   * 
   * @author Tammo Riedinger
   */
  private interface Fork extends Future<Integer> {
    /**
     * Returns the list of lines having been written to the error-stream.
     * 
     * @return list of lines
     */
    List<String> getErrorLines();

    /**
     * Returns the list of lines having been written to the standard-stream.
     * 
     * @return list of lines
     */
    List<String> getMessages();

    /**
     * Checks whether any line has been written to the error-stream.
     * 
     * @return true, if any line was written
     */
    boolean hasErrors();

    /**
     * Checks whether any line has been written to the standard-stream.
     * 
     * @return true, if any line was written
     */
    boolean hasMessages();
  }

  /**
   * Simple class to control a process being launched by a given process-builder,
   * while capturing its
   * io.
   * 
   * @author Tammo Riedinger
   */
  private static class ForkedProcess implements Fork, Callable<Integer> {
    private ProcessMessageCollector m_errorCollector = null;
    private ProcessMessageCollector m_messageCollector = null;
    private final ProcessBuilder m_builder;
    private Future<Integer> m_future = null;

    public ForkedProcess(final String procName, final ProcessBuilder builder) {
      this.m_builder = builder;

      final ExecutorService executor = Executors.newSingleThreadExecutor();
      this.m_future = executor.submit(this);
    }

    @Override
    public Integer call() throws Exception {
      Process process = null;
      try {
        process = this.m_builder.start();

        this.m_errorCollector = new ProcessMessageCollector(process.getErrorStream());
        this.m_messageCollector = new ProcessMessageCollector(process.getInputStream());

        final int exitCode = process.waitFor();

        return Integer.valueOf(exitCode);
      } finally {
        if (process != null) {
          process.destroy();
        }
      }
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
      return this.m_future.cancel(mayInterruptIfRunning);
    }

    @Override
    public Integer get() throws InterruptedException, ExecutionException {
      return this.m_future.get();
    }

    @Override
    public Integer get(final long timeout, final TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
      return this.m_future.get(timeout, unit);
    }

    @Override
    public List<String> getErrorLines() {
      return this.m_errorCollector.getMessages();
    }

    @Override
    public List<String> getMessages() {
      return this.m_messageCollector.getMessages();
    }

    @Override
    public boolean hasErrors() {
      return this.m_errorCollector.hasMessages();
    }

    @Override
    public boolean hasMessages() {
      return this.m_messageCollector.hasMessages();
    }

    @Override
    public boolean isCancelled() {
      return this.m_future.isCancelled();
    }

    @Override
    public boolean isDone() {
      return this.m_future.isDone();
    }
  }

  /**
   * Thread to read lines from an input-stream.
   * This class is used to collect errors and messages sent from a forked jvm.
   * 
   * @author Tammo Riedinger
   */
  private static class ProcessMessageCollector extends Thread {
    private final InputStream m_in;
    private final List<String> m_messages;

    public ProcessMessageCollector(final InputStream in) {
      super("ProcessMessageCollector");

      this.m_in = in;
      this.m_messages = new ArrayList<>();

      this.start();
    }

    public List<String> getMessages() {
      return this.m_messages;
    }

    public boolean hasMessages() {
      return this.m_messages.size() > 0;
    }

    @Override
    public void run() {
      try {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(this.m_in))) {
          String line;
          while ((line = reader.readLine()) != null) {
            this.m_messages.add(line);
          }
        }
      } catch (final Exception ex) {

      }
    }
  }

  /**
   * Forks a new JVM-instance and runs the main-method in the given class.
   * 
   * @param mainClass the main-class to be run in the jvm
   * @param args      the arguments to be passed to the static main method
   * @return the forked instance, that is basically a future to the termination
   *         value of the process
   * @throws Exception
   */
  private Fork fork(final Class<?> mainClass, final String... args) throws Exception {
    return this.fork(-1, mainClass, args);
  }

  /**
   * Forks a new JVM-instance and runs the main-method in the given class.
   * 
   * @param debugPort optional debug-port for the forked jvm (useful when writing
   *                  new tests)
   * @param mainClass the main-class to be run in the jvm
   * @param args      the arguments to be passed to the static main method
   * @return the forked instance, that is basically a future to the termination
   *         value of the process
   * @throws Exception
   */
  private Fork fork(final int debugPort, final Class<?> mainClass, final String... args) throws Exception {
    String classpath;
    // Try to find a URLClassLoader
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    while (cl != null && !(cl instanceof URLClassLoader)) {
      cl = cl.getParent();
    }
    if (cl != null) {
      classpath = Arrays.stream(((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs())
          .map(URL::getFile)
          .collect(Collectors.joining(File.pathSeparator));
    } else {
      classpath = System.getProperty("java.class.path");
    }

    final ArrayList<String> conf = new ArrayList<>();

    conf.add(System.getProperty("java.home") + "/bin/java");
    if (debugPort > 0) {
      conf.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=" + debugPort);
    }
    conf.add("-classpath");
    conf.add(classpath);
    conf.add(mainClass.getName());
    if (args != null && args.length > 0) {
      conf.addAll(Arrays.asList(args));
    }

    return new ForkedProcess(mainClass.getCanonicalName(), new ProcessBuilder(conf.toArray(new String[conf.size()])));
  }

  @Test
  public void testOtherProcessConsumer() throws Exception {
    final DominoClient client = this.getClient();

    final String queueName = UUID.randomUUID().toString();

    final AtomicReference<MessageQueue> queue = new AtomicReference<>(client.getMessageQueues().open(queueName, true));

    Fork consumer = null;
    try {
      final int messageCount = 10;
      final String msgFormat = "Message to %s: %d/%d";
      // simple validation pattern for consumer
      final Pattern msgAcceptorPattern = Pattern
          .compile("Message to ([\\p{XDigit}]{8}-[\\w]{4}-[\\w]{4}-[\\w]{4}-[\\w]{12}): (\\d{1,2})/(\\d{1,2})");

      // fork a consumer, that will send a quit-message after consuming the given
      // amount of messages
      consumer = this.fork(MessageConsumer.class, queue.get().getName(), "" + messageCount, "false", "true",
          msgAcceptorPattern.pattern(), "" + TimeUnit.SECONDS.toMillis(20));

      // launch a thread to produce the messages and send quit
      final Fork fConsumer = consumer;
      final MessageProducer testProducer = new MessageProducer(queue.get().getName(), msgFormat, messageCount, true, false) {
        @Override
        protected void afterAll(final MessageQueue q) {
          // wait for the consumer
          final long start = System.currentTimeMillis();

          while (!fConsumer.isDone()) {
            try {
              Thread.sleep(10);
            } catch (final InterruptedException e) {
              // ignored
            }

            if (System.currentTimeMillis() - start > 10000) {
              Assertions.fail("Consumer-thread did not finish after 10 seconds");
            }
          }
        }
      };

      final Thread producerThread = new Thread(testProducer, "TestProducer");
      producerThread.start();

      final Integer result = consumer.get();

      if (consumer.hasErrors()) {
        Assertions.fail(consumer.getErrorLines().toString());
      }
      // ignore messages that do not match the pattern bounced by consumer (e.g.
      // diagnostic output to std-out)
      final List<String> filteredMessages = consumer.getMessages().stream().filter(msg -> msgAcceptorPattern.matcher(msg).matches())
          .collect(Collectors.toList());
      Assertions.assertEquals(messageCount, filteredMessages.size(), "Not enough messages consumed");
      Assertions.assertTrue(filteredMessages.containsAll(testProducer.getMessages()), "Consumer did not receive all sent messages");

      consumer = null;

      Assertions.assertEquals(Integer.valueOf(0), result, "Process did not terminate properly: " + result.intValue());
    } finally {
      if (queue.get() != null) {
        queue.get().close();
      }

      if (consumer != null) {
        consumer.cancel(true);
      }
    }
  }

  @Test
  public void testOtherProcessProducer() throws Exception {
    final DominoClient client = this.getClient();

    final String queueName = UUID.randomUUID().toString();

    final AtomicReference<MessageQueue> queue = new AtomicReference<>(client.getMessageQueues().open(queueName, true));

    Fork producer = null;
    try {
      final int messageCount = 10;
      final String msgFormat = "Message from %s: %d/%d";
      final Pattern msgAcceptorPattern = Pattern
          .compile("Message from ([\\p{XDigit}]{8}-[\\w]{4}-[\\w]{4}-[\\w]{4}-[\\w]{12}): (\\d{1,2})/(\\d{1,2})");

      final List<String> expectedMessages = new ArrayList<>();
      for (int i = 1; i < messageCount; i++) {
        expectedMessages.add(String.format(msgFormat, queueName, i, messageCount));
      }

      // fork a producer, that will wait for a quit-message after producing all
      // messages
      producer = this.fork(MessageProducer.class, queue.get().getName(), msgFormat, "" + messageCount, "true", "false",
          "" + TimeUnit.SECONDS.toMillis(20));

      // launch a thread to consume the messages and send quit
      final AtomicReference<Exception> consumerException = new AtomicReference<>();

      final MessageConsumer testConsumer = new MessageConsumer(queue.get().getName(), messageCount, false, true,
          msgAcceptorPattern) {
        @Override
        protected boolean acceptsMessage(final String message) throws Exception {
          try {
            return super.acceptsMessage(message);
          } catch (final Exception e) {
            consumerException.set(e);

            return false;
          }
        }

        @Override
        protected void afterPollAllMessages(final MessageQueue q, final List<String> messages) {
          Assertions.assertTrue(q.isEmpty(), "Queue should be empty");
        }
      };

      final Thread consumerThread = new Thread(testConsumer, "TestConsumer");
      consumerThread.start();

      final Integer result = producer.get();
      if (producer.hasErrors()) {
        Assertions.fail(producer.getErrorLines().toString());
      }

      producer = null;

      if (consumerException.get() != null) {
        Assertions.fail(consumerException.get());
      }

      Assertions.assertEquals(messageCount, testConsumer.getMessages().size(), "Not enough messages produced");
      Assertions.assertTrue(testConsumer.getMessages().containsAll(expectedMessages), "Consumer did not receive all messages");
      Assertions.assertEquals(Integer.valueOf(0), result, "Process did not terminate properly: " + result.intValue());

      queue.get().close();
      queue.set(null);

      Assertions.assertTrue(!client.getMessageQueues().hasQueue(queueName), "Message-queue has not been removed");
    } finally {
      if (queue.get() != null) {
        queue.get().close();
      }

      if (producer != null) {
        producer.cancel(true);
      }
    }
  }
}
