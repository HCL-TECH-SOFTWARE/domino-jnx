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

import java.util.HashMap;
import java.util.Map;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.util.DominoUtils;
import com.hcl.domino.commons.util.PlatformUtils;
import com.hcl.domino.jna.JNADominoProcess;
import com.hcl.domino.jna.internal.capi.NotesCAPI.FunctionNameAnnotationMapper;
import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.Native;

public class NotesCAPI1400 {
	private static volatile INotesCAPI1400 m_instance;
	private static volatile INotesCAPI1400 m_instanceWithStackLogging;
	
	private static DominoException m_initError;

	public static synchronized INotesCAPI1400 get() {
		if (m_instance==null && m_initError==null) {
			try {
				m_instance = createAPI();
			}
			catch (Exception e) {
				m_initError = new DominoException(0, "Error initializing the Domino JNX API", e);
			}
		}
	
		if (m_initError!=null) {
			throw m_initError;
		}
		
    JNADominoProcess.checkThreadEnabledForDomino();

		boolean useCallstackLogging = DominoUtils.checkBooleanProperty("jnx.callstacklog", "JNX_CALLSTACKLOG"); //$NON-NLS-1$ //$NON-NLS-2$
		
		if (useCallstackLogging) {
			if (m_instanceWithStackLogging==null) {
				m_instanceWithStackLogging = NotesCAPI.wrapWithCrashStackLogging(INotesCAPI1400.class, m_instance);
			}
			
			return m_instanceWithStackLogging;
		}
		else {
			return m_instance;
		}
	}
	
	private static INotesCAPI1400 createAPI() {
		Map<String,Object> libraryOptions = new HashMap<>();
		libraryOptions.put(Library.OPTION_CLASSLOADER, NotesCAPI.class.getClassLoader());
		libraryOptions.put(Library.OPTION_FUNCTION_MAPPER, new FunctionNameAnnotationMapper());

		if (PlatformUtils.isWin32()) {
			libraryOptions.put(Library.OPTION_CALLING_CONVENTION, Function.ALT_CONVENTION); // set w32 stdcall convention
		}

		INotesCAPI1400 api;
		if (PlatformUtils.isWindows()) {
			api = Native.load("nnotes", INotesCAPI1400.class, libraryOptions); //$NON-NLS-1$
		}
		else {
			api = Native.load("notes", INotesCAPI1400.class, libraryOptions); //$NON-NLS-1$
		}
		return api;
	}

}
