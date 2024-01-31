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
package com.hcl.domino.jna.mq;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.ReferenceQueue;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.MessageFormat;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.PlatformUtils;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.callbacks.NotesCallbacks;
import com.hcl.domino.jna.internal.callbacks.Win32NotesCallbacks;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNAMessageQueueAllocations;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.mq.MessageQueue;
import com.hcl.domino.mq.MessageQueue.IMQCallback.Action;
import com.hcl.domino.mq.MessageQueues;
import com.sun.jna.Memory;
import com.sun.jna.ptr.ShortByReference;

/**
 * Implementation of the Domino message-queue as a {@link BlockingQueue}.
 * 
 * @author Tammo Riedinger
 */
public class JNAMessageQueue extends BaseJNAAPIObject<JNAMessageQueueAllocations> implements MessageQueue {
	private String m_queueName;
	private BlockingQueue<String> m_blockingQueue=new BlockingMessageQueue();

	/**
	 * Creates a new instance. Use {@link MessageQueues#createAndOpen(String, int)} to create a
	 * new queue or {@link MessageQueues#open(String, boolean)} to open an existing one.
	 * 
	 * @param client	the parent client of the queue
	 * @param queueName name of the message queue
	 * @param queueHandle the handle of the queue
	 */
	public JNAMessageQueue(IGCDominoClient<?> client, String queueName, int queueHandle) {
		super(client);

		m_queueName = queueName;
		getAllocations().setMessageQueueHandle(queueHandle);

		setInitialized();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected JNAMessageQueueAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		return new JNAMessageQueueAllocations(parentDominoClient, parentAllocations, this, queue);
	}

	@Override
	public final String getName() {
		return m_queueName;
	}
	
	@Override
	public String toStringLocal() {
		if (isDisposed()) {
			return "MessageQueue [disposed]"; //$NON-NLS-1$
		}
		else {
			return MessageFormat.format("MessageQueue [handle={0}, name={1}]", getAllocations().getMessageQueueHandle(), getName()); //$NON-NLS-1$
		}
	}

	@Override
	public int scan(byte[] buffer, IMQCallback callback) {
		DisposableMemory outMem=null;

		if (buffer!=null && buffer.length>0) {
			// TODO eventually a cache could be introduced to allocate commonly used memory-buffers
			outMem=new DisposableMemory(buffer.length);
		}

		try {
			int bytesWritten=scan(outMem, callback);

			if (outMem!=null && bytesWritten>0) {
				outMem.read(0, buffer, 0, bytesWritten);
			}

			return bytesWritten;
		}
		finally {
			if (outMem!=null) {
				outMem.close();
			}
		}
	}

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

	 * @param buffer buffer to be used to read messages, max size is {@link NotesConstants#MQ_MAX_MSGSIZE} (65326 bytes)
	 * @param offset the offset in the buffer where to start writing the message
	 * @param length the max length of the message in the buffer
	 * @param callback callback to be called for each message; if null, we dequeue the a message and return it in the specified buffer
	 * @return The number of bytes written to the buffer (important if <code>callback</code> has been set to null)
	 */
	private int scan(final Memory buffer, final IMQCallback callback) {
		checkDisposed();

		final long length=(buffer!=null) ? buffer.size():0;
		if (buffer!=null && length==0) {
			throw new IllegalArgumentException("Buffer cannot be empty");
		}
		else if (buffer!=null && length > NotesConstants.MQ_MAX_MSGSIZE) {
			throw new IllegalArgumentException(MessageFormat.format("Max size for the buffer is {0} bytes. You specified one with {1} bytes.",
					NotesConstants.MQ_MAX_MSGSIZE, length));
		}

		final NotesCallbacks.MQScanCallback cCallback;
		if (PlatformUtils.isWin32()) {
			cCallback = (Win32NotesCallbacks.MQScanCallbackWin32) (pBuffer, length1, priority, ctx) -> {
				if (callback==null) {
					return INotesErrorConstants.ERR_MQSCAN_DEQUEUE;
				}
				ByteBuffer byteBuf = pBuffer.getByteBuffer(0, length1 & 0xffff);
				ByteBuffer roByteBuf = byteBuf.asReadOnlyBuffer();

				Action action = callback.dataReceived(roByteBuf, priority & 0xffff);
				switch (action) {
				case Continue:
					return 0;
				case Abort:
					return INotesErrorConstants.ERR_MQSCAN_ABORT;
				case Dequeue:
					return INotesErrorConstants.ERR_MQSCAN_DEQUEUE;
				case Delete:
					return INotesErrorConstants.ERR_MQSCAN_DELETE;
				default:
				  //No action taken
				}

				return 0;
			};
		}
		else {
			cCallback = (pBuffer, length1, priority, ctx) -> {
				if (callback==null) {
					return INotesErrorConstants.ERR_MQSCAN_DEQUEUE;
				}
				ByteBuffer byteBuf = pBuffer.getByteBuffer(0, length1 & 0xffff);
				ByteBuffer roByteBuf = byteBuf.asReadOnlyBuffer();

				Action action = callback.dataReceived(roByteBuf, priority & 0xffff);
				switch (action) {
				case Continue:
					return 0;
				case Abort:
					return INotesErrorConstants.ERR_MQSCAN_ABORT;
				case Dequeue:
					return INotesErrorConstants.ERR_MQSCAN_DEQUEUE;
				case Delete:
					return INotesErrorConstants.ERR_MQSCAN_DELETE;
				default:
			      //No action taken
				}

				return 0;
			};
		}

		final ShortByReference retMsgLength = new ShortByReference();
		short result;
		try {
			//AccessController call required to prevent SecurityException when running in XPages
			result = AccessController.doPrivileged((PrivilegedExceptionAction<Short>) () -> NotesCAPI.get().MQScan(getAllocations().getMessageQueueHandle(), buffer, (short) (length & 0xffff), 0, cCallback, null, retMsgLength));
		} catch (PrivilegedActionException e) {
			if (e.getCause() instanceof RuntimeException) {
				throw (RuntimeException) e.getCause();
			} else {
				throw new DominoException(0, "Error scanning message queue", e);
			}
		}
		NotesErrorUtils.checkResult(result);

		return retMsgLength.getValue() & 0xffff;
	}

	@Override
	public void put(byte[] buffer, int priority, int offset, int length) {
		if (length > NotesConstants.MQ_MAX_MSGSIZE) {
			throw new IllegalArgumentException(MessageFormat.format("Max size for the buffer is {0} bytes. You specified one with {1} bytes.",
					NotesConstants.MQ_MAX_MSGSIZE, length));
		}

		// TODO eventually a cache could be introduced to allocate commonly used memory-buffers
		try(DisposableMemory mem = new DisposableMemory(length)) {
			// note: bounds-check should be performed by Memory-class
			mem.write(0, buffer, offset, length);

			put(mem, priority);
		}
	}

	/**
	 * This function adds a message to the message queue.<br>
	 * The message will be placed in the queue according to the value of its priority argument -
	 * higher priority messages will be enqueued ahead of lower priority messages.<br>
	 * <br>
	 * If the queue is full or in a QUIT state, the message will not be put in the queue, and
	 * the function will return an appropriate error code.
	 * 
	 * @param buffer buffer containing the message.  Maximum buffer length is {@link NotesConstants#MQ_MAX_MSGSIZE} (65326 bytes)
	 * @param priority priority
	 */
	private void put(Memory buffer, int priority) {
		checkDisposed();

		if (priority<0 || priority>65535) {
			throw new IllegalArgumentException("Priority must be between 0 and 65535 (WORD datatype in C API)");
		}

		long length = buffer.size();
		if (length > NotesConstants.MQ_MAX_MSGSIZE) {
			throw new IllegalArgumentException(MessageFormat.format("Max size for the buffer is {0} bytes. You specified one with {1} bytes.",
					NotesConstants.MQ_MAX_MSGSIZE, length));
		}

		short result = NotesCAPI.get().MQPut(getAllocations().getMessageQueueHandle(), (short) (priority & 0xffff), buffer, (short) (length & 0xffff), 0);
		NotesErrorUtils.checkResult(result);
	}

	/**
	 * Note: Currently not used, since every read operation currently uses {@link #scan(Memory, com.hcl.domino.mq.MessageQueue.IMQCallback)}
	 * 
	 * Retrieves a message from a message queue, provided the queue is not in a QUIT state.
	 * The message will be stored in the buffer specified in the Buffer argument.<br>
	 * Note: The error code {@link INotesErrorConstants#ERR_MQ_QUITTING} indicates that the
	 * message queue is in the QUIT state, denoting that applications that are reading
	 * the message queue should terminate. For instance, a server addin's message queue
	 * will be placed in the QUIT state when a "tell &lt;addin&gt; quit" command is input at the console.

	 * @param buffer buffer used to read data
	 * @param waitForMessage if the specified message queue is empty, wait for a message to appear in the queue. The timeout argument specifies the amount of time to wait for a message.
	 * @param timeoutMillis if waitForMessage is set to <code>true</code>, the number of milliseconds to wait for a message before timing out. Specify 0 to wait forever. If the message queue goes into a QUIT state before the Timeout expires, MQGet will return immediately.
	 * @param offset the offset in the buffer where to start writing the message
	 * @param length the max length of the message in the buffer
	 * @return Number of bytes written to the buffer
	 */
	@SuppressWarnings("unused")
	private int get(Memory buffer, boolean waitForMessage, int timeoutMillis, int offset, int length) {
		checkDisposed();

		if (length > NotesConstants.MQ_MAX_MSGSIZE) {
			throw new IllegalArgumentException(MessageFormat.format("Max size for the buffer is {0} bytes. You specified one with {1} bytes.",
					NotesConstants.MQ_MAX_MSGSIZE, length));
		}

		ShortByReference retMsgLength = new ShortByReference();

		short result = NotesCAPI.get().MQGet(getAllocations().getMessageQueueHandle(), buffer, (short) (length & 0xffff),
				waitForMessage ? NotesConstants.MQ_WAIT_FOR_MSG : 0,
						timeoutMillis, retMsgLength);
		NotesErrorUtils.checkResult(result);

		return retMsgLength.getValue();
	}
	
    @Override
    public Optional<String> get(long timeout, TimeUnit unit) throws InterruptedException {
      checkDisposed();
      
      try(DisposableMemory buffer = new DisposableMemory(NotesConstants.MQ_MAX_MSGSIZE)) {
        ShortByReference retMsgLength = new ShortByReference();

        long millis = unit.toMillis(timeout);
        if(millis > Integer.MAX_VALUE) {
          throw new IllegalArgumentException(MessageFormat.format("Timeout value {0} is larger than Integer.MAX_VALUE", Long.toString(millis)));
        }
        short result = NotesCAPI.get().MQGet(getAllocations().getMessageQueueHandle(), buffer, (short) (NotesConstants.MQ_MAX_MSGSIZE & 0xffff),
                NotesConstants.MQ_WAIT_FOR_MSG,
                        (int)millis, retMsgLength);
        if(result == INotesErrorConstants.ERR_MQ_EMPTY) {
          return Optional.empty();
        } else if(result == INotesErrorConstants.ERR_MQ_TIMEOUT) {
          return Optional.empty();
        } else if(result == INotesErrorConstants.ERR_MQ_QUITTING) {
          throw new InterruptedException("Received ERR_MQ_QUITTING");
        }
        NotesErrorUtils.checkResult(result);
        
        return Optional.of(NotesStringUtils.fromLMBCS(buffer, Short.toUnsignedInt(retMsgLength.getValue())));
      }
    }

	@Override
	public void putQuitMsg() {
		checkDisposed();

		NotesCAPI.get().MQPutQuitMsg(getAllocations().getMessageQueueHandle());
	}

	@Override
	public boolean isQuitPending() {
		checkDisposed();

		boolean quitPending = NotesCAPI.get().MQIsQuitPending(getAllocations().getMessageQueueHandle());
		return quitPending;
	}

	@Override
	public void close() {
	  getAllocations().dispose();
	}
	
	@Override
	public boolean add(String e) {
		return m_blockingQueue.add(e);
	}

	@Override
	public boolean offer(String e) {
		return m_blockingQueue.offer(e);
	}

	@Override
	public void put(String e) throws InterruptedException {
		m_blockingQueue.put(e);
	}

	@Override
	public boolean offer(String e, long timeout, TimeUnit unit) throws InterruptedException {
		return m_blockingQueue.offer(e, timeout, unit);
	}

	@Override
	public String take() throws InterruptedException {
		return m_blockingQueue.take();
	}

	@Override
	public String poll(long timeout, TimeUnit unit) throws InterruptedException {
		return m_blockingQueue.poll(timeout, unit);
	}

	@Override
	public int remainingCapacity() {
		return m_blockingQueue.remainingCapacity();
	}

	@Override
	public boolean remove(Object o) {
		return m_blockingQueue.remove(o);
	}

	@Override
	public boolean contains(Object o) {
		return m_blockingQueue.contains(o);
	}

	@Override
	public int drainTo(Collection<? super String> c) {
		return m_blockingQueue.drainTo(c);
	}

	@Override
	public int drainTo(Collection<? super String> c, int maxElements) {
		return m_blockingQueue.drainTo(c, maxElements);
	}

	@Override
	public String remove() {
		return m_blockingQueue.remove();
	}

	@Override
	public String poll() {
		return m_blockingQueue.poll();
	}

	@Override
	public String element() {
		return m_blockingQueue.element();
	}

	@Override
	public String peek() {
		return m_blockingQueue.peek();
	}

	@Override
	public int size() {
		return m_blockingQueue.size();
	}

	@Override
	public boolean isEmpty() {
		return m_blockingQueue.isEmpty();
	}

	@Override
	public Iterator<String> iterator() {
		return m_blockingQueue.iterator();
	}

	@Override
	public Object[] toArray() {
		return m_blockingQueue.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return m_blockingQueue.toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return m_blockingQueue.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends String> c) {
		return m_blockingQueue.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return m_blockingQueue.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return m_blockingQueue.retainAll(c);
	}
	@Override
	public boolean removeIf(Predicate<? super String> filter) {
		return m_blockingQueue.removeIf(filter);
	}
	
	@Override
	public void clear() {
		m_blockingQueue.clear();
	}

	/**
	 * Implementation of a blocking messages that uses only {@link JNAMessageQueue#put(byte[], int, int, int)}
	 * and {@link JNAMessageQueue#scan(Memory, com.hcl.domino.mq.MessageQueue.IMQCallback)} to store and retrieve
	 * messages from the Domino message-queue.
	 * 
	 * @author Tammo Riedinger
	 */
	private class BlockingMessageQueue extends AbstractQueue<String> implements BlockingQueue<String> {
		@Override
		public void put(String e) {
			offer(e);
		}

		@Override
		public boolean offer(String e, long timeout, TimeUnit unit) {
			long start=System.nanoTime();
			
			do {
				if (this.offer(e)==true) {
					return true;
				}
				
				try {
					Thread.sleep(Math.min(10, unit.toMillis(Math.round(timeout/2d))));
				}
				catch (InterruptedException ie) {
					// ignore
				}
			} while (timeout > unit.convert(System.nanoTime()-start, TimeUnit.NANOSECONDS));
			
			return false;
		}

		@Override
		public boolean offer(String e) {
			byte[] data=e.getBytes();
			
			try {
				JNAMessageQueue.this.put(data, 0, 0, data.length);
				
				return true;
			}
			catch (DominoException ex) {
				if (ex.getId()==INotesErrorConstants.ERR_MQ_EXCEEDED_QUOTA) {
					return false;
				}
				throw ex;
			}
		}

		@Override
		public String poll(long timeout, TimeUnit unit) throws InterruptedException {
			long start=System.nanoTime();
			
			String[] e=new String[1];
			
			long nanoDelayInUnits=-1;
			do {
				e[0]=null;
				
				try {
					JNAMessageQueue.this.scan((Memory)null, (IMQCallback) (buffer, priority) -> {
						e[0] = JNAMessageQueue.bufferToString(buffer);

						return Action.Dequeue;
					});
					
					break;
				}
				catch (DominoException de) {
					if (de.getId()!=INotesErrorConstants.ERR_MQ_EMPTY) {
						throw de;
					}
				}
					
				if (timeout<0 && (Thread.currentThread().isInterrupted() || !Thread.currentThread().isAlive())) {
					throw new InterruptedException("Thread was interrupted or died");
				}
				
				try {
					// TODO think about this sleep interval
					Thread.sleep(Math.min(10, unit.toMillis(Math.round(timeout/2d))));
				}
				catch (InterruptedException ie) {
					// ignore
				}
				
				nanoDelayInUnits=unit.convert(System.nanoTime()-start, TimeUnit.NANOSECONDS);
			} while (timeout<0 || timeout > nanoDelayInUnits);
			
			return e[0];
		}
		
		@Override
		public String poll() {
			try {
				return poll(0, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// shouldn't happen
				return null;
			}
		}
		
		@Override
		public String peek() {
			String[] e=new String[1];
			
			try {
				JNAMessageQueue.this.scan((Memory)null, (IMQCallback) (buffer, priority) -> {
					e[0] = JNAMessageQueue.bufferToString(buffer);

					return Action.Abort;
				});
			}
			catch (DominoException de) {
				if (de.getId()!=INotesErrorConstants.ERR_MQSCAN_ABORT
						&& de.getId()!=INotesErrorConstants.ERR_MQ_EMPTY) {
					throw de;
				}
			}
			
			return e[0];
		}

		@Override
		public Iterator<String> iterator() {
			// TODO maybe implement an incremental iterator
			final ArrayList<String> allElements=new ArrayList<>();
			
			try {
				JNAMessageQueue.this.scan((Memory)null, (IMQCallback) (buffer, priority) -> {
					allElements.add(JNAMessageQueue.bufferToString(buffer));

					return Action.Continue;
				});
			}
			catch (DominoException e) {
				if (e.getId()!=INotesErrorConstants.ERR_MQ_EMPTY) {
					throw e;
				}
			}
			
			return Collections.unmodifiableList(allElements).iterator();
		}

		@Override
		public int size() {
			final AtomicInteger count=new AtomicInteger();

			try {
				JNAMessageQueue.this.scan((Memory)null, (IMQCallback) (buffer, priority) -> {
					count.incrementAndGet();

					return Action.Continue;
				});
			}
			catch (DominoException e) {
				if (e.getId()!=INotesErrorConstants.ERR_MQ_EMPTY) {
					throw e;
				}
			}

			return count.intValue();
		}

		@Override
		public String take() throws InterruptedException {
			return poll(-1, TimeUnit.MILLISECONDS);
		}

		@Override
		public int remainingCapacity() {
			return MessageQueue.MAX_MESSAGE_COUNT - size();
		}

		@Override
		public int drainTo(Collection<? super String> c) {
			return drainTo(c, -1);
		}

		@Override
		public int drainTo(final Collection<? super String> c, int maxElements) {
			final AtomicInteger counter=new AtomicInteger();
			
			try {
				JNAMessageQueue.this.scan((Memory)null, (IMQCallback) (buffer, priority) -> {
					c.add(JNAMessageQueue.bufferToString(buffer));
					counter.getAndIncrement();

					return Action.Delete;
				});
			}
			catch (DominoException e) {
				if (e.getId()!=INotesErrorConstants.ERR_MQ_EMPTY) {
					throw e;
				}
			}
			
			return counter.intValue();
		}
		
		@Override
		public boolean contains(Object o) {
			return containsAll(Arrays.asList(o));
		}
		
		@Override
		public boolean containsAll(Collection<?> c) {
			final AtomicBoolean contains=new AtomicBoolean();

			try {
				contains.set(true);
				
				JNAMessageQueue.this.scan((Memory)null, (IMQCallback) (buffer, priority) -> {
					if (!c.contains(JNAMessageQueue.bufferToString(buffer))) {
						contains.set(false);
						
						return Action.Abort;
					}
					return Action.Continue;
				});
			}
			catch (DominoException e) {
				if (e.getId()==INotesErrorConstants.ERR_MQSCAN_ABORT) {
					return false;
				}
				else if (e.getId()!=INotesErrorConstants.ERR_MQ_EMPTY) {
					throw e;
				}
			}
			
			return contains.get();
		}
		
		@Override
		public void clear() {
			try {
				JNAMessageQueue.this.scan((Memory)null, (IMQCallback) (buffer, priority) -> Action.Delete);
			}
			catch (DominoException e) {
				if (e.getId()==INotesErrorConstants.ERR_MQ_EMPTY) {
					// this is expected, when queue is empty
					return;
				}
				throw e;
			}
		}
		
		@Override
		public boolean remove(Object o) {
			return removeIf(t -> t.equals(o));
		}
		
		@Override
		public boolean removeAll(Collection<?> c) {
			return removeIf(t -> c.contains(t));
		}
		
		@Override
		public boolean retainAll(final Collection<?> c) {
			return removeIf(t -> !c.contains(t));
		}
		
		@Override
		public boolean removeIf(final Predicate<? super String> filter) {
			Objects.requireNonNull(filter);
			
			final AtomicInteger counter=new AtomicInteger();

			try {
				scan((Memory)null, (IMQCallback) (buffer, priority) -> {
					if (filter.test(JNAMessageQueue.bufferToString(buffer))) {
						counter.getAndIncrement();
						
						return Action.Delete;
					}
					return Action.Continue;
				});
			}
			catch (DominoException e) {
				if (e.getId()!=INotesErrorConstants.ERR_MQ_EMPTY) {
					throw e;
				}	
			}
			
			return counter.intValue()>0;
	    }


		@Override
		public boolean isEmpty() {
			try {
				JNAMessageQueue.this.scan((Memory)null, (IMQCallback) (buffer, priority) -> Action.Abort);
			}
			catch (DominoException e) {
				if (e.getId()==INotesErrorConstants.ERR_MQSCAN_ABORT) {
					return false;
				}
				else if (e.getId()==INotesErrorConstants.ERR_MQ_EMPTY) {
					return true;
				}
				throw e;
			}
			return true;
		}
	}
	
	private static String bufferToString(ByteBuffer buffer) {
		ByteArrayOutputStream bOut=new ByteArrayOutputStream();
		
		while (buffer.hasRemaining()) {
			bOut.write(buffer.get());
		}
		
		try {
			return bOut.toString("UTF-8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			// shouldn't happen
			throw new DominoException(0, MessageFormat.format("Couldn''t decode message: {0}", e.getMessage()));
		}
	}
}
