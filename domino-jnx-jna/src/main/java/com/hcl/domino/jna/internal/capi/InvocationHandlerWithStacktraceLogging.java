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
package com.hcl.domino.jna.internal.capi;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.hcl.domino.commons.util.DominoUtils;
import com.hcl.domino.commons.util.StringUtil;

/**
 * {@link InvocationHandler} that dumps the current callback to disk before
 * invoking the original method in order to identify the cause of JVM crashes.
 * 
 * @author Karsten Lehmann
 */
public class InvocationHandlerWithStacktraceLogging implements InvocationHandler {
	private static final ThreadLocal<DateFormat> fileDtFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyyMMddHHmmss")); //$NON-NLS-1$
	
	private Object m_obj;
	private boolean m_loggedFileLocation;
	
	public InvocationHandlerWithStacktraceLogging(Object obj) {
		m_obj = obj;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Exception e = new Exception();
		e.fillInStackTrace();

		Path stFile = createStackTraceFile(e);
		try {
			return method.invoke(m_obj, args);
		}
		finally {
			if (stFile!=null) {
				deleteStackTraceFile(stFile);
			}
		}
	}

	private void deleteStackTraceFile(final Path stFile) {
		AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
			try {
				Files.deleteIfExists(stFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		});
	}

	private Path createStackTraceFile(final Exception e) {
		return AccessController.doPrivileged((PrivilegedAction<Path>) () -> {
			String outDirPath = DominoUtils.getJavaProperty("jnx.callstacklogdir", null); //$NON-NLS-1$
			
			if (StringUtil.isEmpty(outDirPath)) {
				outDirPath = DominoUtils.getenv("JNX_CALLSTACKLOGDIR"); //$NON-NLS-1$
			}
			
			if (StringUtil.isEmpty(outDirPath)) {
				outDirPath = DominoUtils.getJavaProperty("java.io.tmpdir", null); //$NON-NLS-1$
			}
			
			Path outDir = Paths.get(outDirPath);
			if (!Files.exists(outDir)) {
				try {
					Files.createDirectories(outDir);
				} catch(IOException e1) {
					throw new UncheckedIOException(e1);
				}
			}
			
			if (!m_loggedFileLocation) {
				System.out.println(MessageFormat.format("Writing JNX callstack logs in directory {0}", outDir)); //$NON-NLS-1$
				m_loggedFileLocation = true;
			}

			Path stFile = outDir.resolve("jnx-stack-"+fileDtFormat.get().format(new Date())+"-"+Thread.currentThread().getId()+".txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			try {
				Files.deleteIfExists(stFile);
			} catch(IOException e1) {
				throw new UncheckedIOException(e1);
			}
			try(BufferedWriter fWriter = Files.newBufferedWriter(stFile, StandardCharsets.UTF_8)) {
				try(PrintWriter pWriter = new PrintWriter(fWriter)) {
					e.printStackTrace(pWriter);
				}
			} catch (IOException e1) {
				e.printStackTrace();
				return null;
			}
			
			return stFile;
		});
	}

}
