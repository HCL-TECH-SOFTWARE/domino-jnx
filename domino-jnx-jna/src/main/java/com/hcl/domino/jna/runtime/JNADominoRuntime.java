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
package com.hcl.domino.jna.runtime;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;

import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.jna.JNADominoClient;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.runtime.DominoRuntime;
import com.hcl.domino.runtime.NSDMode;
import com.sun.jna.Memory;

public class JNADominoRuntime implements DominoRuntime {
	private JNADominoClient m_parent;
	
	public JNADominoRuntime(JNADominoClient parent) {
		m_parent = parent;
	}

	public JNADominoClient getParent() {
		return m_parent;
	}
	
	@Override
	public Optional<Path> getDataDirectory() {
		DisposableMemory retPathName = new DisposableMemory(NotesConstants.MAXPATH);
		try {
			NotesCAPI.get().OSGetDataDirectory(retPathName);
			NotesCAPI.get().OSPathAddTrailingPathSep(retPathName);
			String pathAsStr = NotesStringUtils.fromLMBCS(retPathName, -1);
			if(StringUtil.isNotEmpty(pathAsStr)) {
				return Optional.of(Paths.get(pathAsStr));
			} else {
				return Optional.empty();
			}
		}
		finally {
			retPathName.dispose();
		}
	}

	@Override
	public Optional<Path> getProgramDirectory() {
		DisposableMemory retPathName = new DisposableMemory(NotesConstants.MAXPATH);
		try {
			NotesCAPI.get().OSGetExecutableDirectory(retPathName);
			String pathAsStr = NotesStringUtils.fromLMBCS(retPathName, -1);
			if(StringUtil.isNotEmpty(pathAsStr)) {
				return Optional.of(Paths.get(pathAsStr));
			} else {
				return Optional.empty();
			}
		}
		finally {
			retPathName.dispose();
		}
	}

	@Override
	public Optional<Path> getTempDirectory() {
		DisposableMemory retPathName = new DisposableMemory(NotesConstants.MAXPATH);
		try {
			NotesCAPI.get().OSGetSystemTempDirectory(retPathName, NotesConstants.MAXPATH);
			String pathAsStr = NotesStringUtils.fromLMBCS(retPathName, -1);
			if(StringUtil.isNotEmpty(pathAsStr)) {
				return Optional.of(Paths.get(pathAsStr));
			} else {
				return Optional.empty();
			}
		}
		finally {
			retPathName.dispose();
		}
	}

	@Override
	public Optional<Path> getViewRebuildDirectory() {
		DisposableMemory retPathName = new DisposableMemory(NotesConstants.MAXPATH);
		retPathName.setByte(0, (byte)0);
		try {
			NotesCAPI.get().NIFGetViewRebuildDir(retPathName, NotesConstants.MAXPATH);
			String pathAsStr = NotesStringUtils.fromLMBCS(retPathName, -1);
			if(StringUtil.isNotEmpty(pathAsStr)) {
				return Optional.of(Paths.get(pathAsStr));
			} else {
				return Optional.empty();
			}
		}
		finally {
			retPathName.dispose();
		}
	}
	
	@Override
	public Optional<Path> getSharedDataDirectory() {
	  DisposableMemory retPathName = new DisposableMemory(NotesConstants.MAXPATH);
    try {
      NotesCAPI.get().OSGetSharedDataDirectory(retPathName);
      NotesCAPI.get().OSPathAddTrailingPathSep(retPathName);
      String pathAsStr = NotesStringUtils.fromLMBCS(retPathName, -1);
      if(StringUtil.isNotEmpty(pathAsStr)) {
        return Optional.of(Paths.get(pathAsStr));
      } else {
        return Optional.empty();
      }
    }
    finally {
      retPathName.dispose();
    }
	}

	@Override
	public void setProperty(String propertyName, String value) {
		Memory variableNameMem = NotesStringUtils.toLMBCS(propertyName, true);
		Memory valueMem = NotesStringUtils.toLMBCS(value, true);
		
		boolean isSoft = true; // allow writing OSGI_HTTP_DYNAMIC_BUNDLES
		NotesCAPI.get().OSSetEnvironmentVariableExt(variableNameMem, valueMem, isSoft ? (short) 1 : (short) 0);
	}

	@Override
	public void setProperty(String propertyName, int value) {
		Memory variableNameMem = NotesStringUtils.toLMBCS(propertyName, true);
		NotesCAPI.get().OSSetEnvironmentInt(variableNameMem, value);
	}

	@Override
	public String getPropertyString(String propertyName) {
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
	public int getPropertyInt(String propertyName) {
		String value = getPropertyString(propertyName);
		if (StringUtil.isEmpty(value)) {
			return 0;
		}
		
		try {
			int iVal = (int) Double.parseDouble(value);
			return iVal;
		}
		catch (NumberFormatException e) {
			return 0;
		}
	}

	@Override
	public void invokeNSD(Collection<NSDMode> modes) {
		short modesBitmask = DominoEnumUtil.toBitField(NSDMode.class, modes);

		Memory serverNameMem = NotesStringUtils.toLMBCS("", true); //$NON-NLS-1$
		
		short result = NotesCAPI.get().OSRunNSDExt(serverNameMem, modesBitmask);
		NotesErrorUtils.checkResult(result);
	}
	
}
