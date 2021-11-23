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
package com.hcl.domino.jna.internal.capi;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.util.DominoUtils;
import com.hcl.domino.commons.util.PlatformUtils;
import com.hcl.domino.exception.DominoInitException;
import com.hcl.domino.jna.JNADominoProcess;
import com.hcl.domino.jna.internal.capi.INotesCAPI.NativeFunctionName;
import com.sun.jna.Function;
import com.sun.jna.FunctionMapper;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Structure;

public class NotesCAPI {
	private static volatile INotesCAPI m_instance;
	private static volatile INotesCAPI m_instanceWithStackLogging;
	
	@SuppressWarnings("unused")
	private static Class<Native> m_nativeClazz;
	private static DominoException m_initError;
	private static int m_platformAlignment;

	static {
		if (PlatformUtils.isWindows()) {
			if (PlatformUtils.is64Bit()) {
				m_platformAlignment = Structure.ALIGN_DEFAULT;
			}
			else {
				m_platformAlignment = Structure.ALIGN_NONE;
			}
		}
		else if (PlatformUtils.isMac()) {
			if (PlatformUtils.is64Bit()) {
				m_platformAlignment = Structure.ALIGN_NONE;
			}
			else {
				m_platformAlignment = Structure.ALIGN_DEFAULT;
			}
		}
		else if (PlatformUtils.isLinux()) {
			m_platformAlignment = Structure.ALIGN_DEFAULT;
		}
		else {
			m_platformAlignment = -1;
		}
	}

	public static int getPlatformAlignment() {
		return m_platformAlignment;
	}

	 /**
   * Loads the Domino shared library and returns a Java Proxy object to map C functions to Java methods.
   * 
   * @return C API proxy object to call C API methods
   * @throws DominoInitException of Domino shared library cannot be found or the C API init failed
   */
  public static synchronized INotesCAPI get() {
    return get(false);
  }
  
	/**
	 * Loads the Domino shared library and returns a Java Proxy object to map C functions to Java methods.
	 * 
	 * @param skipThreadCheck true to not check if the current thread has been initialized for Domino
	 * @return C API proxy object to call C API methods
	 * @throws DominoInitException of Domino shared library cannot be found or the C API init failed
	 */
	public static synchronized INotesCAPI get(boolean skipThreadCheck) {
		if (m_instance==null && m_initError==null) {
			try {
				m_instance = createAPI();
			}
			catch (Throwable e) {
				m_initError = new DominoInitException("Error loading Notes/Domino shared library. Please make sure that the location of nnotes.dll (Windows), libnotes.so (Linux) or libnotes.dylib is added to the PATH.", e);
			}
		}
	
		if (m_initError!=null) {
			throw m_initError;
		}
		
		if (!skipThreadCheck) {
	    JNADominoProcess.checkThreadEnabledForDomino();
		}
		
		boolean useCallstackLogging = DominoUtils.checkBooleanProperty("jnx.callstacklog", "JNX_CALLSTACKLOG"); //$NON-NLS-1$ //$NON-NLS-2$
		
		if (useCallstackLogging) {
			if (m_instanceWithStackLogging==null) {
				m_instanceWithStackLogging = wrapWithCrashStackLogging(INotesCAPI.class, m_instance);
			}
			
			return m_instanceWithStackLogging;
		}
		else {
			return m_instance;
		}
	}
	
	private static INotesCAPI createAPI() {
		//keep reference to Native as described here: https://github.com/java-native-access/jna/blob/master/www/FrequentlyAskedQuestions.md#why-does-the-vm-sometimes-crash-in-my-shutdown-hook-on-windows
		m_nativeClazz = Native.class;

		//enforce using the extracted JNA .dll/.so file instead of what we find on the PATH
		DominoUtils.setJavaProperty("jna.nosys", "true"); //$NON-NLS-1$ //$NON-NLS-2$

		if (PlatformUtils.isWindows()) {
			if (PlatformUtils.is64Bit()) {
				m_platformAlignment = Structure.ALIGN_DEFAULT;
			}
			else {
				m_platformAlignment = Structure.ALIGN_NONE;
			}
		}
		else if (PlatformUtils.isMac()) {
			if (PlatformUtils.is64Bit()) {
				m_platformAlignment = Structure.ALIGN_NONE;
			}
			else {
				m_platformAlignment = Structure.ALIGN_DEFAULT;
			}
		}
		else if (PlatformUtils.isLinux()) {
			m_platformAlignment = Structure.ALIGN_DEFAULT;
		}
		else {
			String osName = DominoUtils.getJavaProperty("os.name", null); //$NON-NLS-1$
			m_initError = new DominoException(0, MessageFormat.format("Platform is unknown or not supported: {0}", osName));
			return null;
		}

		Map<String,Object> libraryOptions = new HashMap<>();
		libraryOptions.put(Library.OPTION_CLASSLOADER, NotesCAPI.class.getClassLoader());
		libraryOptions.put(Library.OPTION_FUNCTION_MAPPER, new FunctionNameAnnotationMapper());

		if (PlatformUtils.isWin32()) {
			libraryOptions.put(Library.OPTION_CALLING_CONVENTION, Function.ALT_CONVENTION); // set w32 stdcall convention
		}

		INotesCAPI api;
		if (PlatformUtils.isWindows()) {
			api = Native.load("nnotes", INotesCAPI.class, libraryOptions); //$NON-NLS-1$
		}
		else {
			api = Native.load("notes", INotesCAPI.class, libraryOptions); //$NON-NLS-1$
		}
		return api;
	}

	/**
	 * Wraps the specified API object to dump caller stacktraces right before invoking
	 * native methods
	 * 
	 * @param apiClazz API interface
	 * @return api API implementation
	 */
	@SuppressWarnings("unchecked")
	static <T> T wrapWithCrashStackLogging(final Class<T> apiClazz, final T api) {
		try {
			return AccessController.doPrivileged((PrivilegedExceptionAction<T>) () -> {
				InvocationHandlerWithStacktraceLogging invocationHandler = new InvocationHandlerWithStacktraceLogging(api);
				return (T) Proxy.newProxyInstance(api.getClass().getClassLoader(), new Class[] {apiClazz}, invocationHandler);
			});
		} catch (PrivilegedActionException e) {
			e.printStackTrace();
			return api;
		}
	}

	/**
	 * Custom FunctionMapper to use {@link NativeFunctionName} annotations to
	 * define the name of the native methods instead of just using the method name.
	 */
	static class FunctionNameAnnotationMapper implements FunctionMapper {
	    @Override
	    public String getFunctionName(NativeLibrary nativeLibrary, Method method) {
	        NativeFunctionName annotation = method.getAnnotation(NativeFunctionName.class);
	        // just return the function's name if the annotation is not applied
	        if (annotation == null) {
	        	return method.getName();
	        }
	        else {
	        	return annotation.name();
	        }
	    }
	}

}
