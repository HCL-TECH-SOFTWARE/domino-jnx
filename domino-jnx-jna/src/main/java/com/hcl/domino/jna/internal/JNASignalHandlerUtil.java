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
package com.hcl.domino.jna.internal;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import com.hcl.domino.DominoClient.IBreakHandler;
import com.hcl.domino.DominoClient.IProgressListener;
import com.hcl.domino.DominoClient.ReplicationStateListener;
import com.hcl.domino.DominoException;
import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.commons.util.PlatformUtils;
import com.hcl.domino.data.Database.Action;
import com.hcl.domino.jna.internal.callbacks.NotesCallbacks;
import com.hcl.domino.jna.internal.callbacks.NotesCallbacks.OSSIGBREAKPROC;
import com.hcl.domino.jna.internal.callbacks.NotesCallbacks.OSSIGPROGRESSPROC;
import com.hcl.domino.jna.internal.callbacks.NotesCallbacks.OSSIGREPLPROC;
import com.hcl.domino.jna.internal.callbacks.Win32NotesCallbacks;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Pointer;

/**
 * Utility class that uses signal handlers of Domino to stop and get progress information
 * for long running operations.
 * 
 * @author Karsten Lehmann
 */
public class JNASignalHandlerUtil {
	private static final ConcurrentHashMap<Thread, IBreakHandler> m_breakHandlerByThread = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Thread, IProgressListener> m_progressListenerByThread = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Thread, ReplicationStateListener> m_replicationStateListenerByThread = new ConcurrentHashMap<>();

	private static volatile boolean m_breakProcInstalled = false;
	private static volatile NotesCallbacks.OSSIGBREAKPROC prevBreakProc = null;
	private static ThreadLocal<Exception> lastBreakHandlerException = new ThreadLocal<>();

	private static final Win32NotesCallbacks.OSSIGBREAKPROCWin32 breakProcWin32 = () -> {
		try {
			Thread thread = Thread.currentThread();
			IBreakHandler breakHandler = m_breakHandlerByThread.get(thread);
			if (breakHandler!=null) {
				if (breakHandler.shouldInterrupt() == Action.Stop) {
					return INotesErrorConstants.ERR_CANCEL;
				}
			}

			if (prevBreakProc!=null) {
				return prevBreakProc.invoke();
			}
			else {
				return 0;
			}
		}
		catch (Exception e) {
			lastBreakHandlerException.set(e);
			return 0;
		}
	};
	private static final NotesCallbacks.OSSIGBREAKPROC breakProc = () -> {
		try {
			Thread thread = Thread.currentThread();
			IBreakHandler breakHandler = m_breakHandlerByThread.get(thread);
			if (breakHandler!=null) {
				if (breakHandler.shouldInterrupt() == Action.Stop) {
					return INotesErrorConstants.ERR_CANCEL;
				}
			}

			if (prevBreakProc!=null) {
				return prevBreakProc.invoke();
			}
			else {
				return 0;
			}
		}
		catch (Exception e) {
			lastBreakHandlerException.set(e);
			return 0;
		}
	};

	private static volatile boolean m_progressProcInstalled = false;
	private static volatile NotesCallbacks.OSSIGPROGRESSPROC prevProgressProc = null;
	private static ThreadLocal<Exception> lastProgressListenerException = new ThreadLocal<>();

	private static Win32NotesCallbacks.OSSIGPROGRESSPROCWin32 progressProcWin32 = (option, data1, data2) -> {
		try {
			Thread thread = Thread.currentThread();
			IProgressListener progressListener = m_progressListenerByThread.get(thread);
			if (progressListener!=null) {
				if (option == NotesConstants.PROGRESS_SIGNAL_BEGIN) {
					progressListener.begin();
				}
				else if (option == NotesConstants.PROGRESS_SIGNAL_END) {
					progressListener.end();
				}
				else if (option == NotesConstants.PROGRESS_SIGNAL_SETRANGE) {
					long range = Pointer.nativeValue(data1);
					progressListener.setRange(range);
				}
				else if (option == NotesConstants.PROGRESS_SIGNAL_SETTEXT) {
					String str = NotesStringUtils.fromLMBCS(data1, -1);
					progressListener.setText(str);
				}
				else if (option == NotesConstants.PROGRESS_SIGNAL_SETPOS) {
					long pos = Pointer.nativeValue(data1);
					progressListener.setPos(pos);
				}
				else if (option == NotesConstants.PROGRESS_SIGNAL_DELTAPOS) {
					long deltapos = Pointer.nativeValue(data1);
					progressListener.setDeltaPos(deltapos);
				}
				else if (option == NotesConstants.PROGRESS_SIGNAL_SETBYTERANGE) {
					long totalbytes = Pointer.nativeValue(data1);
					progressListener.setByteRange(totalbytes);
				}
				else if (option == NotesConstants.PROGRESS_SIGNAL_SETBYTEPOS) {
					long bytesDone = Pointer.nativeValue(data1);
					progressListener.setBytePos(bytesDone);
				}
			}

			if (prevProgressProc!=null) {
				return prevProgressProc.invoke(option, data1, data2);
			}
			else {
				return 0;
			}
		}
		catch (Exception e) {
			lastProgressListenerException.set(e);
			return 0;
		}
	};


	private static NotesCallbacks.OSSIGPROGRESSPROC progressProc = (option, data1, data2) -> {
		try {
			Thread thread = Thread.currentThread();
			IProgressListener progressListener = m_progressListenerByThread.get(thread);
			if (progressListener!=null) {

				if (option == NotesConstants.PROGRESS_SIGNAL_BEGIN) {
					progressListener.begin();
				}
				else if (option == NotesConstants.PROGRESS_SIGNAL_END) {
					progressListener.end();
				}
				else if (option == NotesConstants.PROGRESS_SIGNAL_SETRANGE) {
					long range = Pointer.nativeValue(data1);
					progressListener.setRange(range);
				}
				else if (option == NotesConstants.PROGRESS_SIGNAL_SETTEXT) {
					String str = NotesStringUtils.fromLMBCS(data1, -1);
					progressListener.setText(str);
				}
				else if (option == NotesConstants.PROGRESS_SIGNAL_SETPOS) {
					long pos = Pointer.nativeValue(data1);
					progressListener.setPos(pos);
				}
				else if (option == NotesConstants.PROGRESS_SIGNAL_DELTAPOS) {
					long deltapos = Pointer.nativeValue(data1);
					progressListener.setDeltaPos(deltapos);
				}
				else if (option == NotesConstants.PROGRESS_SIGNAL_SETBYTERANGE) {
					long totalbytes = Pointer.nativeValue(data1);
					progressListener.setByteRange(totalbytes);
				}
				else if (option == NotesConstants.PROGRESS_SIGNAL_SETBYTEPOS) {
					long bytesDone = Pointer.nativeValue(data1);
					progressListener.setBytePos(bytesDone);
				}
			}

			if (prevProgressProc!=null) {
				return prevProgressProc.invoke(option, data1, data2);
			}
			else {
				return 0;
			}
		}
		catch (Exception e) {
			lastProgressListenerException.set(e);
			return 0;
		}
	};

	private static volatile boolean m_replProcInstalled = false;
	private static volatile NotesCallbacks.OSSIGREPLPROC prevReplProc = null;
	private static ThreadLocal<Exception> lastReplProcException = new ThreadLocal<>();

	private static Win32NotesCallbacks.OSSIGREPLPROCWin32 replProcWin32 = (state, pText1, pText2) -> {
		try {
			Thread thread = Thread.currentThread();
			ReplicationStateListener progressListener = m_replicationStateListenerByThread.get(thread);
			if (progressListener!=null) {
				if (state == NotesConstants.REPL_SIGNAL_IDLE) {
					progressListener.idle();
				}
				else if (state == NotesConstants.REPL_SIGNAL_PICKSERVER) {
					progressListener.pickServer();
				}
				else if (state == NotesConstants.REPL_SIGNAL_CONNECTING) {
					String server1 = NotesStringUtils.fromLMBCS(pText1, -1);
					String port1 = NotesStringUtils.fromLMBCS(pText2, -1);
					progressListener.connecting(server1, port1);
				}
				else if (state == NotesConstants.REPL_SIGNAL_SEARCHING) {
					String server2 = NotesStringUtils.fromLMBCS(pText1, -1);
					String port2 = NotesStringUtils.fromLMBCS(pText2, -1);
					progressListener.searching(server2, port2);
				}
				else if (state == NotesConstants.REPL_SIGNAL_SENDING) {
					String serverFile1 = NotesStringUtils.fromLMBCS(pText1, -1);
					String localFile1 = NotesStringUtils.fromLMBCS(pText2, -1);
					progressListener.sending(serverFile1, localFile1);
				}
				else if (state == NotesConstants.REPL_SIGNAL_RECEIVING) {
					String serverFile2 = NotesStringUtils.fromLMBCS(pText1, -1);
					String localFile2 = NotesStringUtils.fromLMBCS(pText2, -1);
					progressListener.receiving(serverFile2, localFile2);
				}
				else if (state == NotesConstants.REPL_SIGNAL_SEARCHINGDOCS) {
					String srcFile = NotesStringUtils.fromLMBCS(pText1, -1);
					progressListener.searchingDocs(srcFile);
				}
				else if (state == NotesConstants.REPL_SIGNAL_DONEFILE) {
					String localFile3 = NotesStringUtils.fromLMBCS(pText1, -1);
					String replFileStats = NotesStringUtils.fromLMBCS(pText2, -1);
					progressListener.doneFile(localFile3, replFileStats);
				}
				else if (state == NotesConstants.REPL_SIGNAL_REDIRECT) {
					String serverFile3 = NotesStringUtils.fromLMBCS(pText1, -1);
					String localFile4 = NotesStringUtils.fromLMBCS(pText2, -1);
					progressListener.redirect(serverFile3, localFile4);
				}
				else if (state == NotesConstants.REPL_SIGNAL_BUILDVIEW) {
					progressListener.buildView();
				}
				else if (state == NotesConstants.REPL_SIGNAL_ABORT) {
					progressListener.abort();
				}
			}

			if (prevReplProc!=null) {
				prevReplProc.invoke(state, pText1, pText2);
			}
		}
		catch (Exception e) {
			lastReplProcException.set(e);
		}
	};

	private static NotesCallbacks.OSSIGREPLPROC replProc = (state, pText1, pText2) -> {
		try {
			Thread thread = Thread.currentThread();
			ReplicationStateListener progressListener = m_replicationStateListenerByThread.get(thread);
			if (progressListener!=null) {
				if (state == NotesConstants.REPL_SIGNAL_IDLE) {
					progressListener.idle();
				}
				else if (state == NotesConstants.REPL_SIGNAL_PICKSERVER) {
					progressListener.pickServer();
				}
				else if (state == NotesConstants.REPL_SIGNAL_CONNECTING) {
					String server1 = NotesStringUtils.fromLMBCS(pText1, -1);
					String port1 = NotesStringUtils.fromLMBCS(pText2, -1);
					progressListener.connecting(server1, port1);
				}
				else if (state == NotesConstants.REPL_SIGNAL_SEARCHING) {
					String server2 = NotesStringUtils.fromLMBCS(pText1, -1);
					String port2 = NotesStringUtils.fromLMBCS(pText2, -1);
					progressListener.searching(server2, port2);
				}
				else if (state == NotesConstants.REPL_SIGNAL_SENDING) {
					String serverFile1 = NotesStringUtils.fromLMBCS(pText1, -1);
					String localFile1 = NotesStringUtils.fromLMBCS(pText2, -1);
					progressListener.sending(serverFile1, localFile1);
				}
				else if (state == NotesConstants.REPL_SIGNAL_RECEIVING) {
					String serverFile2 = NotesStringUtils.fromLMBCS(pText1, -1);
					String localFile2 = NotesStringUtils.fromLMBCS(pText2, -1);
					progressListener.receiving(serverFile2, localFile2);
				}
				else if (state == NotesConstants.REPL_SIGNAL_SEARCHINGDOCS) {
					String srcFile = NotesStringUtils.fromLMBCS(pText1, -1);
					progressListener.searchingDocs(srcFile);
				}
				else if (state == NotesConstants.REPL_SIGNAL_DONEFILE) {
					String localFile3 = NotesStringUtils.fromLMBCS(pText1, -1);
					String replFileStats = NotesStringUtils.fromLMBCS(pText2, -1);
					progressListener.doneFile(localFile3, replFileStats);
				}
				else if (state == NotesConstants.REPL_SIGNAL_REDIRECT) {
					String serverFile3 = NotesStringUtils.fromLMBCS(pText1, -1);
					String localFile4 = NotesStringUtils.fromLMBCS(pText2, -1);
					progressListener.redirect(serverFile3, localFile4);
				}
				else if (state == NotesConstants.REPL_SIGNAL_BUILDVIEW) {
					progressListener.buildView();
				}
				else if (state == NotesConstants.REPL_SIGNAL_ABORT) {
					progressListener.abort();
				}
			}

			if (prevReplProc!=null) {
				prevReplProc.invoke(state, pText1, pText2);
			}
		}
		catch (Exception e) {
			lastReplProcException.set(e);
		}
	};

	/**
	 * Registers our central break handler. Signal handlers are stored in the C API per
	 * process block, unfortunately not per thread. Since any other thread in the
	 * process (e.g. http task) can registers its own handler, there is no safe way
	 * to restore the previous handler, so we have to keep it.
	 */
	public static synchronized void installGlobalBreakHandler() {
		if (!m_breakProcInstalled) {
			try {
				//AccessController call required to prevent SecurityException when running in XPages
				prevBreakProc = AccessController.doPrivileged((PrivilegedExceptionAction<OSSIGBREAKPROC>) () -> {
					if (PlatformUtils.isWin32()) {
						return (NotesCallbacks.OSSIGBREAKPROC) NotesCAPI.get().OSSetBreakSignalHandler(NotesConstants.OS_SIGNAL_CHECK_BREAK, breakProcWin32);
					}
					else {
						return (NotesCallbacks.OSSIGBREAKPROC) NotesCAPI.get().OSSetBreakSignalHandler(NotesConstants.OS_SIGNAL_CHECK_BREAK, breakProc);
					}
				});
			} catch (PrivilegedActionException e) {
				if (e.getCause() instanceof RuntimeException) {
					throw (RuntimeException) e.getCause();
				} else {
					throw new DominoException(0, "Error installing break handler", e);
				}
			}
			m_breakProcInstalled = true;
		}
	}

	/**
	 * Registers our central progress handler. Signal handlers are stored in the C API per
	 * process block, unfortunately not per thread. Since any other thread in the
	 * process (e.g. http task) can registers its own handler, there is no safe way
	 * to restore the previous handler, so we have to keep it.
	 */
	public static synchronized void installGlobalProgressHandler() {
		if (!m_progressProcInstalled) {
			try {
				//AccessController call required to prevent SecurityException when running in XPages
				prevProgressProc = AccessController.doPrivileged((PrivilegedExceptionAction<OSSIGPROGRESSPROC>) () -> {
					if (PlatformUtils.isWin32()) {
						return (NotesCallbacks.OSSIGPROGRESSPROC) NotesCAPI.get().OSSetProgressSignalHandler(NotesConstants.OS_SIGNAL_PROGRESS, progressProcWin32);
					}
					else {
						return (NotesCallbacks.OSSIGPROGRESSPROC) NotesCAPI.get().OSSetProgressSignalHandler(NotesConstants.OS_SIGNAL_PROGRESS, progressProc);
					}
				});
			} catch (PrivilegedActionException e) {
				if (e.getCause() instanceof RuntimeException) {
					throw (RuntimeException) e.getCause();
				} else {
					throw new DominoException(0, "Error installing progress listener", e);
				}
			}
			m_progressProcInstalled = true;
		}
	}

	/**
	 * Registers our central replication state handler. Signal handlers are stored in the C API per
	 * process block, unfortunately not per thread. Since any other thread in the
	 * process (e.g. http task) can registers its own handler, there is no safe way
	 * to restore the previous handler, so we have to keep it.
	 */
	public static synchronized void installGlobalReplicationStateHandler() {
		if (!m_replProcInstalled) {
			try {
				//AccessController call required to prevent SecurityException when running in XPages
				prevReplProc = AccessController.doPrivileged((PrivilegedExceptionAction<OSSIGREPLPROC>) () -> {
					if (PlatformUtils.isWin32()) {
						return (NotesCallbacks.OSSIGREPLPROC) NotesCAPI.get().OSSetReplicationSignalHandler(NotesConstants.OS_SIGNAL_REPL, replProcWin32);
					}
					else {
						return (NotesCallbacks.OSSIGREPLPROC) NotesCAPI.get().OSSetReplicationSignalHandler(NotesConstants.OS_SIGNAL_REPL, replProc);
					}
				});
			} catch (PrivilegedActionException e) {
				if (e.getCause() instanceof RuntimeException) {
					throw (RuntimeException) e.getCause();
				} else {
					throw new DominoException(0, "Error installing replication state listener", e);
				}
			}
			m_replProcInstalled = true;
		}
	}

	/**
	 * The break signal handler can be used to send a break signal to Domino
	 * so that the current (probably long running) operation, e.g. a fulltext on a remote
	 * database, can be interrupted.
	 * 
	 * @param callable callable to execute
	 * @param breakHandler break handler to interrupt the current operation
	 * @return optional result
	 * @throws DominoException if {@code callable} throws an exception
	 * 
	 * @param <T> result type
	 */
	public static <T> T runInterruptable(Callable<T> callable, final IBreakHandler breakHandler) {
		installGlobalBreakHandler();

		Thread thread = Thread.currentThread();
		
		lastBreakHandlerException.set(null);
		m_breakHandlerByThread.put(thread, breakHandler);
		try {
			T result = callable.call();
			
			Exception e = lastBreakHandlerException.get();
			if (e!=null) {
				throw new DominoException("Error occurred in break handler", e);
			}
			return result;
		} catch (DominoException e) {
			throw e;
		} catch (Exception e1) {
			throw new DominoException("Error running operation with break handler", e1);
		}
		finally {
			lastBreakHandlerException.set(null);
			m_breakHandlerByThread.remove(thread);
		}
	}

	/**
	 * The progress signal handler can be used to get notified about the
	 * progress of method execution, e.g. replication or copy operations.
	 * 
	 * @param callable {@link Callable} to execute
	 * @param progressListener progress handler to get notified about progress changes
	 * @return optional result
	 * @throws DominoException if {@code callable} throws an error
	 * 
	 * @param <T> result type
	 */
	public static <T> T runWithProgress(Callable<T> callable, final IProgressListener progressListener) {
		installGlobalProgressHandler();

		Thread thread = Thread.currentThread();
		
		lastProgressListenerException.set(null);
		m_progressListenerByThread.put(thread, progressListener);
		try {
			T result = callable.call();
			Exception e = lastProgressListenerException.get();
			if (e!=null) {
				throw new DominoException("Error occurred in progress listener", e);
			}
			return result;
		} catch (DominoException e) {
			throw e;
		} catch (Exception e1) {
			throw new DominoException("Error running operation with progress listener", e1);
		}
		finally {
			lastProgressListenerException.set(null);
			m_progressListenerByThread.remove(thread);
		}
	}

	/**
	 * The replication state handler can be used to get notified about the
	 * replication progress.
	 * 
	 * @param callable callable to execute
	 * @param replStateListener replication state handler to get notified about replication state changes
	 * @return optional result
	 * @throws Exception of callable throws an error
	 * 
	 * @param <T> result type
	 */
	public static <T> T runWithReplicationStateTracking(Callable<T> callable,
			final ReplicationStateListener replStateListener) throws Exception {

		installGlobalReplicationStateHandler();

		Thread thread = Thread.currentThread();
		lastReplProcException.set(null);
		m_replicationStateListenerByThread.put(thread, replStateListener);
		try {
			T result = callable.call();
			Exception e = lastReplProcException.get();
			if (e!=null) {
				throw new DominoException("Error occurred in replication state listener", e);
			}
			return result;
		}
		finally {
			lastReplProcException.set(null);
			m_replicationStateListenerByThread.remove(thread);
		}
	}

}
