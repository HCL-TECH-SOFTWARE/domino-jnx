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
package com.hcl.domino.jna;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.hcl.domino.DominoException;
import com.hcl.domino.DominoProcess;
import com.hcl.domino.commons.util.DominoUtils;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.exception.DominoInitException;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.INotesCAPI;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Memory;
import com.sun.jna.StringArray;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class JNADominoProcess implements DominoProcess {
	private static boolean processInitialized;
	private static DominoPacemakerThread pacemakerThread;
	private static boolean processTerminated;
	private static Map<Thread, ThreadInfo> threadEnabledForDominoRefCount = Collections.synchronizedMap(new HashMap<>());
	private static final Object pacemakerThreadlock = new Object();
	
	private static final Method notesThreadInit;
	private static final Method notesThreadTerm;
	
	static {
		// If Notes.jar is available and we're on Java 8, prefer those thread init/term methods to account for
		//   in-runtime JNI hooks.
	  // We currently have to exclude Java 9+ due to incompatibilities in the internal JVM locator in
	  //   lsxbe
		Method initMethod;
		Method termMethod;
    if("1.8".equals(DominoUtils.getJavaProperty("java.specification.version", ""))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  		try {
  			Class<?> notesThread = Class.forName("lotus.domino.NotesThread"); //$NON-NLS-1$
  			initMethod = notesThread.getDeclaredMethod("sinitThread"); //$NON-NLS-1$
  			termMethod = notesThread.getDeclaredMethod("stermThread"); //$NON-NLS-1$
  		} catch(Throwable t) {
  			// Then Notes.jar is not present
  			initMethod = null;
  			termMethod = null;
  		}
  	} else {
  	  initMethod = null;
  	  termMethod = null;
  	}
		notesThreadInit = initMethod;
		notesThreadTerm = termMethod;
	}
	
	public static void ensureProcessInitialized() {
		synchronized (pacemakerThreadlock) {
			if (!processInitialized) {
				throw new DominoInitException("DominoProcess.get().initializeProcess(String[]) must be called first to initialize the Domino API for this process.");
			}
		}
	}
	
	public static void ensureProcessNotTerminated() {
		synchronized (pacemakerThreadlock) {
			if (processTerminated) {
				throw new DominoInitException("Domino access for this process has already been terminated.");
			}
		}
	}
	
	@Override
	public void initializeProcess(String[] initArgs) {
		synchronized (pacemakerThreadlock) {
			if (processInitialized) {
				return;
			}

			// Fail early if we can't load the notes shared library. The get() function may throw an unchecked DominoInitException
			NotesCAPI.get(true); // true => do not check if thread is initialized for Domino

			if (initArgs==null) {
				initArgs = new String[0];
			}
			else {
				if (initArgs.length>0) {
					if (StringUtil.isNotEmpty(initArgs[0])) {
						String dominoProgramDirPathStr = initArgs[0];
						Path dominoProgramDirPath = Paths.get(dominoProgramDirPathStr);
						
						if (!Files.exists(dominoProgramDirPath)) {
							throw new DominoInitException(MessageFormat.format("Specified Notes/Domino program dir path does not exist: {0}", dominoProgramDirPath.toString()));
						}
						
						if (!Files.isDirectory(dominoProgramDirPath)) {
							throw new DominoInitException(MessageFormat.format("Specified Notes/Domino program dir path is not a directory: {0}", dominoProgramDirPath.toString()));
						}
					}
					
					if (initArgs.length>1) {
						String notesIniPathStr = initArgs[1];
						if (notesIniPathStr.startsWith("=") ) { //$NON-NLS-1$
							Path notesIniPath = Paths.get(notesIniPathStr.substring(1));
							
							if (!Files.exists(notesIniPath)) {
								throw new DominoInitException(MessageFormat.format("Specified Notes.ini path does not exist: {0}", notesIniPath.toString()));
							}
							
							if (!Files.isRegularFile(notesIniPath)) {
								throw new DominoInitException(MessageFormat.format("Specified Notes.ini path is not a file: {0}", notesIniPath.toString()));
							}
						}
					}
				}
			}
			StringArray strArr = new StringArray(initArgs);
			
			//make sure we have at least one running thread accessing Domino APIs,
			//otherwise the API automatically unloads the native libs when
			//no thread has an active initThread ref count
			pacemakerThread = new DominoPacemakerThread(initArgs.length, strArr);
			pacemakerThread.start();
			try {
				pacemakerThread.waitUntilStarted();
				
				// validate the connection was set up properly
				DominoException e=pacemakerThread.getInitException();
				if (e!=null) {
					// abort if it wasn't
					throw e;
				}
				
				// finally mark the process as initialized
				processInitialized = true;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private String getPropertyString(String propertyName) {
		Memory variableNameMem = NotesStringUtils.toLMBCS(propertyName, true);
		DisposableMemory rethValueBuffer = new DisposableMemory(NotesConstants.MAXENVVALUE);
		try {
			short result = NotesCAPI.get().OSGetEnvironmentString(variableNameMem, rethValueBuffer, NotesConstants.MAXENVVALUE);
			if (result==1) {
				String str = NotesStringUtils.fromLMBCS(rethValueBuffer, -1);
				return str;
			}
			else {
				return ""; //$NON-NLS-1$
			}
		}
		finally {
			rethValueBuffer.dispose();
		}
	}

	@Override
	public String switchToId(Path idPath, String password, boolean dontSetEnvVar) {
		if (idPath==null) {
			idPath = Paths.get(getPropertyString("KeyFileName")); //$NON-NLS-1$
			if (!idPath.isAbsolute()) {
				Path dataDirPath = Paths.get(getPropertyString("Directory")); //$NON-NLS-1$
				idPath = dataDirPath.resolve(idPath);
			}
		}
		Memory idPathMem = NotesStringUtils.toLMBCS(idPath.toString(), true);
		Memory passwordMem = NotesStringUtils.toLMBCS(password, true);
		Memory retUserNameMem = new Memory(NotesConstants.MAXUSERNAME+1);
		
		short result = NotesCAPI.get().SECKFMSwitchToIDFile(idPathMem, passwordMem, retUserNameMem,
				NotesConstants.MAXUSERNAME, dontSetEnvVar ? NotesConstants.fKFM_switchid_DontSetEnvVar : 0, null);
		NotesErrorUtils.checkResult(result);
		
		int userNameLength = 0;
		for (int i=0; i<retUserNameMem.size(); i++) {
			userNameLength = i;
			if (retUserNameMem.getByte(i) == 0) {
				break;
			}
		}
		
		String userName = NotesStringUtils.fromLMBCS(retUserNameMem, userNameLength);
		return userName;
	}
	
	private static boolean isWritePacemakerDebugMessages() {
		return DominoUtils.checkBooleanProperty("jnx.debuginit", "JNX_DEBUGINIT"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public void terminateProcess() {
		synchronized (pacemakerThreadlock) {
			if (!processInitialized) {
				//nothing to do
				return;
			}
			if (pacemakerThread==null) {
				throw new IllegalStateException("Missing Domino pacemaker thread");
			}

			if (!threadEnabledForDominoRefCount.isEmpty()) {
				if (!DominoUtils.isSkipThreadWarning()) {
					AtomicBoolean hasUnmatchedInits = new AtomicBoolean(false);
					AtomicInteger idx = new AtomicInteger(1);
					threadEnabledForDominoRefCount.forEach((thread, threadInfo) -> {
						if (!hasUnmatchedInits.get()) {
							System.out.println("**********************************************************************************************************");
							System.out.println("* WARNING: We found unmatched DominoProcess.get().initializeThread() calls in threads:");
							System.out.println("*");
						}

						System.out.println(
							MessageFormat.format(
								"* #{0} - {1} (started: {2}, ref count: {3})", 
								idx, thread, new Date(threadInfo.getStartTime()), threadInfo.getRefCount()
							)
						);

						StackTraceElement[] callstack = threadInfo.getCallstacks();
						if (callstack.length>1) {
							System.out.println("* Callstack:");

							for (int i=1; i<callstack.length && i<=20; i++) {
								System.out.println("*  "+callstack[i]);
							}
						}

						hasUnmatchedInits.set(true);
						idx.incrementAndGet();

						System.out.println("*");
					});

					if (hasUnmatchedInits.get()) {
						System.out.println("* Please close the DominoThreadContext returned by DominoProcess.get().initializeThread() e.g. with\n"
								+ "* a try-with-resources-block or call DominoProcess.get().terminateThread().\n"
								+ "* Having unmatched initializeThread/terminateThread pairs can cause application crashes.");
						System.out.println("**********************************************************************************************************");
					}
				}
			}
			
			try {
				pacemakerThread.requestShutdown();
				pacemakerThread = null;
			} catch (InterruptedException e) {
				throw new DominoException("Thread has been interrupted", e);
			}
			processInitialized = false;
		}
	}
	
	@Override
	public DominoThreadContext initializeThread() {
		Thread thread = Thread.currentThread();
		ThreadInfo threadInfo = threadEnabledForDominoRefCount.get(thread);
		if (threadInfo==null) {
			if(!DominoUtils.isNoInitTermThread()) {
				if(notesThreadInit != null) {
					try {
						AccessController.doPrivileged((PrivilegedExceptionAction<Void>)() -> {
							notesThreadInit.invoke(null);
							return null;
						});
					} catch (IllegalArgumentException | PrivilegedActionException e) {
						throw new RuntimeException(e);
					}
				} else {
					NotesCAPI.get(true).NotesInitThread(); // true => do not check if thread is initialized for Domino, because we do this
				}
			}
			threadInfo = new ThreadInfo(Thread.currentThread().getStackTrace(), System.currentTimeMillis());
			threadEnabledForDominoRefCount.put(thread, threadInfo);
		}
		else {
			threadInfo.setRefCount(threadInfo.getRefCount()+1);
		}
		
		return new DominoThreadContext() {
			boolean terminated = false;
			
			@Override
			public void close() {
				if (!terminated) {
					terminateThread();
					terminated = true;
				}
			}
		};
	}
	
	@Override
	public void terminateThread() {
		Thread thread = Thread.currentThread();
		ThreadInfo threadInfo = threadEnabledForDominoRefCount.get(thread);
		if (threadInfo==null) {
			throw new DominoException("WARNING: Unmatched notesInitThread detected!");
		}
		else if (threadInfo.getRefCount()==1) {
			if(!DominoUtils.isNoInitTermThread()) {
				if(notesThreadTerm != null) {
					try {
						AccessController.doPrivileged((PrivilegedExceptionAction<Void>)() -> {
							notesThreadTerm.invoke(null);
							return null;
						});
					} catch (IllegalArgumentException | PrivilegedActionException e) {
						throw new RuntimeException(e);
					}
				} else {
					NotesCAPI.get().NotesTermThread();
				}
			}
			threadEnabledForDominoRefCount.remove(thread);
		}
		else {
			threadInfo.setRefCount(threadInfo.getRefCount()-1);
		}
	}
	
	/**
	 * Makes sure that the process and current thread have been initialized for Domino C API access
	 * 
	 * @throws DominoInitException in case of missing initialization
	 */
	public static void checkThreadEnabledForDomino() {
		ensureProcessInitialized();
		ensureProcessNotTerminated();
		
		Thread thread = Thread.currentThread();
		ThreadInfo threadInfo = threadEnabledForDominoRefCount.get(thread);
		if (threadInfo==null || threadInfo.getRefCount()==0) {
			throw new DominoInitException("Please use DominoProcess.get().initializeThread() / terminateThread() to enable Domino access for this thread.");
		}
	}

	private static class DominoPacemakerThread extends Thread {
		private LinkedBlockingQueue<Object> m_quitSignalQueue = new LinkedBlockingQueue<>();
		private InterruptedException m_interruptedEx;
		private LinkedBlockingQueue<Object> m_waitStartedQueue = new LinkedBlockingQueue<>();
		private int argsCount;
		private StringArray args;
		private DominoInitException initException;
		
		public DominoPacemakerThread(int argsCount, StringArray args) {
			super("Domino JNX Pacemaker");
			
			this.argsCount=argsCount;
			this.args=args;
		}

		/**
		 * This method will return an exception, if either
		 * NotesInitExtended or NotesInitThread fail
		 * 
		 * @return		the exception or null, if none occurred
		 */
		public DominoInitException getInitException() {
			return initException;
		}

		@SuppressFBWarnings({ "WA_NOT_IN_LOOP", "JLM_JSR166_UTILCONCURRENT_MONITORENTER" })
		public void requestShutdown() throws InterruptedException {
			synchronized (m_quitSignalQueue) {
				if (processTerminated) {
					return;
				}
				
				//send thread quit signal
				m_quitSignalQueue.add(new Object());

				//wait for thread to finish shutdown
				m_quitSignalQueue.wait();
			}
		}

		public void waitUntilStarted() throws InterruptedException {
			m_waitStartedQueue.take();
			if (this.initException!=null) {
				throw this.initException;
			}
		}
		
		/**
		 * Notifies the code that started this pacemaker thread that execution is
		 * done, either with success or failure.
		 */
		private void notifyCaller() {
		  try {
        m_waitStartedQueue.put(new Object());
      } catch (InterruptedException e1) {
        e1.printStackTrace();
        m_interruptedEx = e1;
      }
		}
		
		private DominoInitException toDominoInitException(short result) {
		  int resultAsInt = Short.toUnsignedInt(result);
      
      //resolve C API init error code without using OSLoadString (using built-in constants) to prevent a crash
      final short statusCode = (short) (result & NotesConstants.ERR_MASK);
      String statusMessage;
      try {
        statusMessage = NotesErrorUtils.errToString(statusCode, true);
      } catch (final Throwable e) {
        statusMessage = MessageFormat.format("Error initializing Notes runtime, ERR 0x{0}", Integer.toHexString(resultAsInt));
      }
      final boolean isRemoteError = (result & NotesConstants.STS_REMOTE) == NotesConstants.STS_REMOTE;

      final String msg = MessageFormat.format(
          "{0} (error code: 0x{1}, raw error with all flags: 0x{2})",
          statusMessage,
          Integer.toHexString(resultAsInt) + (isRemoteError ? ", remote server error" : ""),
          Integer.toHexString(result));
      
      return new DominoInitException(result, msg);
		}
		
		@Override
		public void run() {
			boolean debug = isWritePacemakerDebugMessages();
			short result;
			
			INotesCAPI capi;
			try {
			  //loads Domino shared library and maps it to the JNA INotesCAPI proxy object
			  capi = NotesCAPI.get(true); // true => do not check if thread is initialized for Domino, because we do this
			}
			catch (DominoInitException e) {
			  this.initException = e;
			  notifyCaller();
        return;
			}
			catch (Throwable e) {
			  this.initException = new DominoInitException("Error loading Notes/Domino shared library. Please make sure that the location of nnotes.dll (Windows), libnotes.so (Linux) or libnotes.dylib is added to the PATH.", e);
			  notifyCaller();
			  return;
			}
			
			// initializing the notes connection is performed here, since
			// it appears to be necessary to do this in the same thread as
			// a call to NotesTerm later on
			if (!DominoUtils.isNoInit()) {
			  if (this.argsCount==0) {
			    // DNEXT-12954 Running as an Domino server addin task. Can't reliably know
			    // the notes.ini that the server is using.
			    result = capi.NotesInit();
			  }
			  else {
			    result = capi.NotesInitExtended(this.argsCount, this.args);
			  }
			  
				if(result != 0) {
					// in this case we need to abort and report the exception
					
          int resultAsInt = Short.toUnsignedInt(result);
          
					if (debug) {
						System.out.println(MessageFormat.format("Domino API could not initialize the process. ERR 0x{0}",
						    Integer.toHexString(resultAsInt)));
					}

					this.initException = toDominoInitException(result);
					
					notifyCaller();
					return;
				}
			}
			
			if (debug) {
				System.out.println("Domino API initialized.");
			}
			
			result = capi.NotesInitThread();
			if(result != 0) {
				// here we can also abort only and report the exception
				if (debug) {
					System.out.println(MessageFormat.format("Domino API could not initialize pacemaker thread. ERR 0x{0}", Integer.toHexString(result)));
				}
				
        this.initException = toDominoInitException(result);
				
				// additionally we have to terminate the process, to clean up properly
				if(!DominoUtils.isNoTerm()) {
					capi.NotesTerm();
				}
				
				notifyCaller();
				
				return;
			}
			
			if (debug) {
				System.out.println("Domino pacemaker thread started.");
			}
			
			try {
			  notifyCaller();
				
				//wait for quit signal
				m_quitSignalQueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
				m_interruptedEx = e;
			}
			finally {
				if (debug) {
					System.out.println("Stopping Domino pacemaker thread...");
				}
				capi.NotesTermThread();
				if (debug) {
					System.out.println("Domino pacemaker thread stopped.");
				}
				if(!DominoUtils.isNoTerm()) {
					capi.NotesTerm();
				}
				processTerminated = true;
			}
			
			synchronized(m_quitSignalQueue) {
				m_quitSignalQueue.notify();
			}
			if (debug) {
				System.out.println("Domino API terminated.");
			}
		}
		
	}
	
	private static class ThreadInfo {
		private StackTraceElement[] m_callstacks;
		private int m_refCount = 1;
		private long m_startTime;
		
		public ThreadInfo(StackTraceElement[] callstack, long startTime) {
			m_callstacks = callstack;
			m_startTime = startTime;
		}
		
		public StackTraceElement[] getCallstacks() {
			return m_callstacks;
		}
		
		public int getRefCount() {
			return m_refCount;
		}
		
		public void setRefCount(int count) {
			m_refCount = count;
		}
		
		public long getStartTime() {
			return m_startTime;
		}
	}
}
