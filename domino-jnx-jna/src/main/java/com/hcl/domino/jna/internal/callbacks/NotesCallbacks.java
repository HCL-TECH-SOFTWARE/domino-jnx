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
package com.hcl.domino.jna.internal.callbacks;

import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.structs.NIFFindByKeyContextStruct;
import com.hcl.domino.jna.internal.structs.NotesLSCompileErrorInfo;
import com.hcl.domino.jna.internal.structs.NotesUniversalNoteIdStruct;
import com.sun.jna.Callback;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * Callback interface used on non-Windows platforms
 * 
 * @author Karsten Lehmann
 */
public interface NotesCallbacks {

	/**
	 * Callback used by EnumCompositeBuffer
	 */
	interface ActionRoutinePtr extends Callback {
		short invoke(Pointer dataPtr, short signature, int dataLength, Pointer vContext);
	}

	/**
	 * Callback used by NSFDbGetNotes
	 */
	interface NSFGetNotesCallback extends Callback {
		short invoke(Pointer param, int totalSizeLow, int totalSizeHigh);
	}

	/**
	 * Callback used by NSFDbGetNotes
	 */
	interface NSFFolderAddCallback extends Callback {
		short invoke(Pointer param, NotesUniversalNoteIdStruct noteUNID, int opBlock, int opBlockSize);
	}

	/**
	 * Callback used by IDEnumerate
	 */
	interface IdEnumerateProc extends Callback {
		short invoke(Pointer parameter, int noteId);
	}

	/**
	 * Callback used by NSFNoteCipherExtractWithCallback
	 */
	interface NoteExtractCallback extends Callback {
		short invoke(Pointer data, int length, Pointer param);
	}

	/**
	 * Callback used by NSFRecoverDatabases
	 */
	interface LogRestoreCallbackFunction extends Callback {
		short invoke(NotesUniversalNoteIdStruct logID, int logNumber, Memory logSegmentPathName);
	}

	/**
	 * Callback used by NIFFindByKeyExtended3
	 */
	interface NIFFindByKeyProc extends Callback {
		short invoke(NIFFindByKeyContextStruct ctx);
	}

	/**
	 * Callback used by MQScan
	 */
	interface MQScanCallback extends Callback {
		short invoke(Pointer pBuffer, short length, short priority, Pointer ctx);
	}

	/**
	 * Callback used by message signal handlers
	 */
	interface OSSIGMSGPROC extends Callback {
		short invoke(Pointer message, short type);
	}

	/**
	 * Callback used by busy signal handlers
	 */
	interface OSSIGBUSYPROC extends Callback {
		short invoke(short busytype);
	}

	/**
	 * Callback used by break signal handlers
	 */
	interface OSSIGBREAKPROC extends Callback {
		short invoke();
	}

	/**
	 * Callback used to abort design refresh
	 */
	interface ABORTCHECKPROC extends Callback {
		short invoke();
	}
	
	/**
	 * Callback used by progress signal handlers
	 */
	interface OSSIGPROGRESSPROC extends Callback {
		short invoke(short option, Pointer data1, Pointer data2);
	}

	/**
	 * Callback used by replication signal handlers
	 */
	interface OSSIGREPLPROC extends Callback {
		void invoke(short state, Pointer pText1, Pointer pText2);
	}

	/**
	 * Callback used by NSFDbGetNotes
	 */
	interface b32_NSFNoteOpenCallback extends Callback {
		short invoke(Pointer param, int hNote, int noteId, short status);
	}

	/**
	 * Callback used by NSFDbGetNotes
	 */
	interface b32_NSFObjectAllocCallback extends Callback {
		short invoke(Pointer param, int hNote, int oldRRV, short status, int objectSize);
	}

	/**
	 * Callback used by NSFDbGetNotes
	 */
	interface b32_NSFObjectWriteCallback extends Callback {
		short invoke(Pointer param, int hNote, int oldRRV, short status, Pointer buffer, int bufferSize);
	}

	/**
	 * Callback used by NSFNoteComputeWithForm
	 */
	interface b32_CWFErrorProc extends Callback {
		short invoke(Pointer pCDField, short phase, short error, int hErrorText, short wErrorTextSize, Pointer ctx);
	}

	interface b32_NSFGetAllFolderChangesCallback extends Callback {
		short invoke(Pointer param, NotesUniversalNoteIdStruct noteUnid, int hAddedNoteTable, int removedNoteTable);
	}

	/**
	 * Callback used by NSFDbGetNotes
	 */
	interface b64_NSFNoteOpenCallback extends Callback {
		short invoke(Pointer param, long hNote, int noteId, short status);
	}

	/**
	 * Callback used by NSFDbGetNotes
	 */
	interface b64_NSFObjectAllocCallback extends Callback {
		short invoke(Pointer param, long hNote, int oldRRV, short status, int objectSize);
	}

	/**
	 * Callback used by NSFDbGetNotes
	 */
	interface b64_NSFObjectWriteCallback extends Callback {
		short invoke(Pointer param, long hNote, int oldRRV, short status, Pointer buffer, int bufferSize);
	}

	/**
	 * Callback used by NSFSearchExtended3
	 */
	interface NsfSearchProc extends Callback {
		short invoke(Pointer enumRoutineParameter, Pointer searchMatch,
				Pointer summaryBuffer);
	}

	/**
	 * Callback used by NSFNoteComputeWithForm
	 */
	interface b64_CWFErrorProc extends Callback {
		short invoke(Pointer pCDField, short phase, short error, long hErrorText, short wErrorTextSize, Pointer ctx);
	}
	
	interface LSCompilerErrorProc extends Callback {
		short invoke(Pointer pInfo, Pointer pCtx);
	}

	interface b64_NSFGetAllFolderChangesCallback extends Callback {
		short invoke(Pointer param, NotesUniversalNoteIdStruct noteUnid, long hAddedNoteTable, long removedNoteTable);
	}

	interface STATTRAVERSEPROC extends Callback {
		short invoke(Pointer ctx, Pointer facility, Pointer statName, short valueType, Pointer value);
	}
	
	interface ACLENTRYENUMFUNC extends Callback {
		void invoke(Pointer enumFuncParam, Pointer name, short accessLevel, Pointer privileges, short accessFlag);
	}
	
	interface XML_READ_FUNCTION extends Callback {
		int invoke(Pointer pBuffer, int length, Pointer pAction);
	}

	interface XML_WRITE_FUNCTION extends Callback {
		void invoke(Pointer bBuffer, int length, Pointer pAction);
	}
	
	interface NSFPROFILEENUMPROC extends Callback {
		short invoke(int hDB, Pointer ctx, Pointer profileName, short profileNameLength,
				Pointer username, short usernameLength, int noteId);
	}

	interface FPMailNoteJitEx2CallBack extends Callback {
		short invoke(DHANDLE.ByValue hdl, Pointer ptr1, Pointer ptr2);
	}

	interface DESIGN_COLL_OPENCLOSE_PROC extends Callback {
		short invoke(int dwFlags, Pointer phColl, Pointer ctx);
	}

	interface LSCOMPILEERRPROC extends Callback {
		short invoke(NotesLSCompileErrorInfo pInfo, Pointer pCtx);
	}
	
	interface IDVLOG_CALLBACK extends Callback {
		short invoke(Pointer pCtx, Pointer pIDVLogInfo); // Pointer to IDV_LOG_INFO structure
	}

	interface REGSIGNALPROC extends Callback {
		void invoke(Pointer message);
	}
	
	interface b64_DESIGNENUMPROC extends Callback {
		short invoke(Pointer routineParameter, long hDB, int NoteID, 
				NotesUniversalNoteIdStruct NoteUNID, short NoteClass, Pointer summary, int designType);
		
	}
	
	interface b32_DESIGNENUMPROC extends Callback {
		short invoke(Pointer routineParameter, int hDB, int NoteID, 
				NotesUniversalNoteIdStruct NoteUNID, short NoteClass, Pointer summary, int designType);
	}

	interface ASYNCNOTIFYPROC extends Callback {
		void invoke(Pointer p1, Pointer p2);
	}

	interface NSFFORMFUNCPROC extends Callback {
	  short invoke(Pointer ptr);
	}

	interface NSFFORMCMDSPROC extends Callback {
	  short invoke(Pointer ptr, short code, IntByReference stopFlag);
	}
	
	interface SECNABENUMPROC extends Callback {
	  boolean invoke(Pointer pCallCtx, Pointer pCert, int certSize, short reserved1, short reserved2);
	}

}
