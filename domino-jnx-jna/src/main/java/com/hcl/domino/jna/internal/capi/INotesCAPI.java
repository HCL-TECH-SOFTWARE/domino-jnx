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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.ByteBuffer;
import com.hcl.domino.data.structures.QuepArgVal;
import com.hcl.domino.data.types.STATUS;
import com.hcl.domino.data.types.WORD;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.Mem.LockedMemory;
import com.hcl.domino.jna.internal.callbacks.NotesCallbacks;
import com.hcl.domino.jna.internal.callbacks.NotesCallbacks.ACLENTRYENUMFUNC;
import com.hcl.domino.jna.internal.callbacks.NotesCallbacks.NSFFORMCMDSPROC;
import com.hcl.domino.jna.internal.callbacks.NotesCallbacks.NSFFORMFUNCPROC;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.HANDLE;
import com.hcl.domino.jna.internal.structs.CreateDAConfigStruct;
import com.hcl.domino.jna.internal.structs.DbOptionsStruct;
import com.hcl.domino.jna.internal.structs.EnableDisableDAStruct;
import com.hcl.domino.jna.internal.structs.HtmlApi_UrlComponentStruct;
import com.hcl.domino.jna.internal.structs.IntlFormatStruct;
import com.hcl.domino.jna.internal.structs.KFM_PASSWORDStruct;
import com.hcl.domino.jna.internal.structs.NIFFindByKeyContextStruct;
import com.hcl.domino.jna.internal.structs.NotesBlockIdStruct;
import com.hcl.domino.jna.internal.structs.NotesBuildVersionStruct;
import com.hcl.domino.jna.internal.structs.NotesCalendarActionDataStruct;
import com.hcl.domino.jna.internal.structs.NotesCollectionPositionStruct;
import com.hcl.domino.jna.internal.structs.NotesCompoundStyleStruct;
import com.hcl.domino.jna.internal.structs.NotesDbReplicaInfoStruct;
import com.hcl.domino.jna.internal.structs.NotesFTIndexStatsStruct;
import com.hcl.domino.jna.internal.structs.NotesItemDefinitionTableExt;
import com.hcl.domino.jna.internal.structs.NotesItemDefinitionTableLock;
import com.hcl.domino.jna.internal.structs.NotesTimeDatePairStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeStruct;
import com.hcl.domino.jna.internal.structs.NotesUniversalNoteIdStruct;
import com.hcl.domino.jna.internal.structs.ReplExtensionsStruct;
import com.hcl.domino.jna.internal.structs.ReplServStatsStruct;
import com.hcl.domino.jna.internal.structs.UpdateDAConfigStruct;
import com.hcl.domino.jna.internal.structs.VerifyLDAPConnectionStruct;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/**
 * C API functions of Domino R9.0.1+ that JNX is using
 * 
 * @author Karsten Lehmann
 */
public interface INotesCAPI extends Library {
  /**
   * Annotation to use a different method name in the C API than here in the interface
   */
	@Target(value = ElementType.METHOD)
	@Retention(value = RetentionPolicy.RUNTIME)
	@interface NativeFunctionName {
	    String name() default "";
	}

  @Target({ElementType.METHOD, ElementType.FIELD})
	@Retention(RetentionPolicy.RUNTIME)
	/**
	 * Annotation for methods that are not yet part of the public C API
	 */
	public @interface UndocumentedAPI {
	}
	
	STATUS NotesInit();
	STATUS NotesInitExtended(int argc, Memory argvPtr);
	void NotesTerm();

	STATUS NotesInitThread();
	void NotesTermThread();

	int OSTranslate32(short translateMode, Memory in, int inLength, Memory out, int outLength);
	int OSTranslate32(short translateMode, Pointer in, int inLength, Memory out, int outLength);

	WORD OSLoadString(int hModule, STATUS StringCode, Memory retBuffer, WORD BufferLength);
	WORD OSLoadString(long hModule, STATUS StringCode, Memory retBuffer, WORD BufferLength);
	STATUS OSPathNetConstruct(Memory PortName,
			Memory ServerName,
			Memory FileName,
			Memory retPathName);
	STATUS OSPathNetParse(Pointer PathName,
			Pointer retPortName,
			Pointer retServerName,
			Pointer retFileName);

	STATUS DNCanonicalize(int Flags, Memory TemplateName, Memory InName, Memory OutName, WORD OutSize, WORD.ByReference OutLength);
	STATUS DNAbbreviate(int Flags, Memory TemplateName, Memory InName, Memory OutName, WORD OutSize, WORD.ByReference OutLength);	

	STATUS NSFBuildNamesList(Memory UserName, int dwFlags, DHANDLE.ByReference rethNamesList);
	
	STATUS NSFGetChangedDBs(
			Memory ServerName,
			NotesTimeDateStruct.ByReference SinceTime,
			LongByReference ChangesSize,
			DHANDLE.ByReference hChanges,
			NotesTimeDateStruct.ByReference NextSinceTime
			);

	/**
	 * @param handle the handle to lock
	 * @return a pointer to the locked value
	 * @deprecated use {@link Mem#OSLockObject(DHANDLE.ByValue)} instead
	 */
	@Deprecated Pointer OSLockObject(DHANDLE.ByValue handle);
	/**
	 * @param handle the handle to unlock
	 * @return whether unlocking was successful
	 * @deprecated use {@link Mem#OSUnlockObject(NotesBlockIdStruct)} instead
	 */
	@Deprecated boolean OSUnlockObject(DHANDLE.ByValue handle);

	/**
	 * @param handle the handle to free
	 * @return the result status
	 * @deprecated use {@link Mem#OSMemFree(DHANDLE.ByValue)} instead
	 */
	@Deprecated STATUS OSMemFree(DHANDLE.ByValue handle);

	/**
	 * @param handle the handle for which to get the size
	 * @param retSize the size return value
	 * @return the result status
	 * @deprecated use {@link Mem#OSMemGetSize(DHANDLE.ByValue, IntByReference)} instead
	 */
	@Deprecated STATUS OSMemGetSize(DHANDLE.ByValue handle, IntByReference retSize);

	/**
	 * @param BlkType the type of memory block to allocate
	 * @param dwSize the count of blocks to allocate
	 * @param retHandle the return value handle
	 * @return the result status
	 * @deprecated use {@link Mem#OSMemAlloc(short, int, com.hcl.domino.jna.internal.gc.handles.DHANDLE.ByReference)} instead
	 */
	@Deprecated STATUS OSMemAlloc(
			WORD  BlkType,
			int  dwSize,
			DHANDLE.ByReference retHandle);
	
	/**
	 * Allocates a block of memory
	 * 
	 * @param dwtype block type
	 * @param size size of memory
	 * @param rethandle returned MEMHANDLE
	 * @return status
	 */
	STATUS OSMemoryAllocate (int dwtype, int size, IntByReference rethandle);

	/**
	 * @param handle the handle to lock
	 * @return a pointer to the locked memory
	 * @deprecated use {@link Mem#OSMemoryLock(int)} instead
	 */
	@Deprecated
	Pointer OSMemoryLock(
			int handle);
	
	/**
	 * @param handle the handle to lock
	 * @return a pointer to the locked memory
	 * @deprecated use {@link Mem#OSMemoryLock(long)} instead
	 */
	@Deprecated
	Pointer OSMemoryLock(
			long handle);
	
	/**
	 * @param handle the handle to unlock
	 * @return whether unlocking was successful
	 * @deprecated use {@link LockedMemory#close} instead
	 */
	@Deprecated
	boolean OSMemoryUnlock(
			int handle);
	
	/**
	 * @param handle the handle to unlock
	 * @return whether unlocking was successful
	 * @deprecated use {@link LockedMemory#close} instead
	 */
	@Deprecated
	boolean OSMemoryUnlock(
			long handle);
	
	/**
	 * @param handle the handle to get the size
	 * @return the size of the handle's data in memory
	 * @deprecated use {@link Mem#OSMemoryGetSize(int)} instead
	 */
	@Deprecated int OSMemoryGetSize(int handle);
	/**
	 * @param handle the handle to get the size
	 * @return the size of the handle's data in memory
	 * @deprecated use {@link Mem#OSMemoryGetSize(long)} instead
	 */
	@Deprecated int OSMemoryGetSize(long handle);
	/**
	 * @param handle the handle to free
	 * @deprecated use {@link Mem#OSMemoryFree(int)} instead
	 */
	@Deprecated void OSMemoryFree(int handle);
	/**
	 * @param handle the handle to free
	 * @deprecated use {@link Mem#OSMemoryFree(long)} instead
	 */
	@Deprecated void OSMemoryFree(long handle);

	/**
	 * @param handle the handle of the memory to realloc
	 * @param newSize new size of memory
	 * @return status
	 * @deprecated use {@link Mem#OSMemRealloc(com.hcl.domino.jna.internal.gc.handles.DHANDLE.ByValue, int)} instead
	 */
	@Deprecated STATUS OSMemRealloc(
			DHANDLE.ByValue handle,
			int newSize);
	
    @UndocumentedAPI
    STATUS CreateNamesListFromSingleName(Memory pszServerName, short fDontLookupAlternateNames,
        Pointer pLookupFlags, Memory pTarget, DHANDLE.ByReference rethNames);

    @UndocumentedAPI
    STATUS CreateNamesListFromNamesExtend(Memory pszServerName, WORD NumNames,
        Pointer ptrArrTargets, DHANDLE.ByReference rethNames);

    @UndocumentedAPI
    STATUS CreateNamesListFromGroupNameExtend(Memory pszServerName, Memory pTarget,
        DHANDLE.ByReference rethNames);

	STATUS ListAllocate(
			WORD ListEntries,
			WORD TextSize,
			int fPrefixDataType,
			DHANDLE.ByReference rethList,
			Memory retpList,
			WORD.ByReference retListSize);

	STATUS ListRemoveAllEntries(
			DHANDLE.ByValue hList,
			int fPrefixDataType,
			WORD.ByReference pListSize);

	STATUS ListAddEntry(
			DHANDLE.ByValue hList,
			int fPrefixDataType,
			WORD.ByReference pListSize,
			WORD EntryNumber,
			Memory Text,
			WORD TextSize);

	STATUS SECKFMGetUserName(Memory retUserName);

	STATUS NSFDbOpen(Memory dbName, HANDLE.ByReference dbHandle);
	STATUS NSFDbClose(HANDLE.ByValue dbHandle);
	STATUS NSFDbOpenExtended (Memory PathName, WORD Options, DHANDLE.ByValue hNames, NotesTimeDateStruct ModifiedTime,
			HANDLE.ByReference rethDB, NotesTimeDateStruct retDataModified, NotesTimeDateStruct retNonDataModified);
	STATUS NSFDbInfoGet(
			HANDLE.ByValue hDB,
			Pointer retBuffer);
	void NSFDbInfoParse(
			Pointer Info,
			WORD What,
			Pointer Buffer,
			WORD Length);
	STATUS NSFDbInfoSet(
			HANDLE.ByValue hDB,
			Pointer Buffer);
	void NSFDbInfoModify(
			Pointer Info,
			WORD What,
			Pointer Buffer);
	STATUS NSFDbCreateExtended(
			Memory pathName,
			WORD DbClass,
			boolean forceCreation,
			WORD options,
			byte encryptStrength,
			long maxFileSize);
	
	STATUS NSFDbDelete(
			Memory PathName
			);
	STATUS NSFDbIDGet(HANDLE.ByValue hDB, NotesTimeDateStruct retDbID);
	
	STATUS NSFItemSetText(
			DHANDLE.ByValue hNote,
			Memory ItemName,
			Memory ItemText,
			WORD TextLength);

	void TimeConstant(WORD timeConstantType, NotesTimeDateStruct tdptr);

	STATUS ConvertTIMEDATEToText(
			IntlFormatStruct intlFormat,
			Pointer textFormat,
			NotesTimeDateStruct inputTime,
			Memory retTextBuffer,
			WORD textBufferLength,
			WORD.ByReference retTextLength);
	
	STATUS ConvertTextToTIMEDATE(
			IntlFormatStruct intlFormat,
			Pointer textFormat,
			Memory text,
			WORD maxLength,
			NotesTimeDateStruct retTIMEDATE);

	void OSGetIntlSettings(
			IntlFormatStruct retIntlFormat,
			WORD bufferSize);

	STATUS NSFDbGetSpecialNoteID(
			HANDLE.ByValue hDB,
			WORD Index,
			IntByReference retNoteID);
	STATUS NSFDbPathGet(
			HANDLE.ByValue hDB,
			Memory retCanonicalPathName,
			Memory retExpandedPathName);
	STATUS NSFDbReplicaInfoGet(
			HANDLE.ByValue hDB,
			NotesDbReplicaInfoStruct retReplicationInfo);
	STATUS NSFDbReplicaInfoSet(
			HANDLE.ByValue hDB,
			NotesDbReplicaInfoStruct ReplicationInfo);
	
	STATUS NSFNoteDeleteExtended(HANDLE.ByValue hDB, int noteID, int updateFlags);
	STATUS NSFDbDeleteNotes(HANDLE.ByValue hDB, DHANDLE.ByValue hTable, Memory retUNIDArray);
	WORD ODSLength(WORD type);
	void ODSReadMemory(PointerByReference ppSrc, WORD type, Pointer pDest, WORD iterations);
	STATUS IDCreateTable (int alignment, DHANDLE.ByReference rethTable);
	STATUS IDDestroyTable(DHANDLE.ByValue hTable);
	int IDEntries (DHANDLE.ByValue hTable);
	boolean IDIsPresent (DHANDLE.ByValue hTable, int id);
	STATUS IDInsert (DHANDLE.ByValue hTable, int id, IntByReference retfInserted);
	STATUS IDDelete (DHANDLE.ByValue hTable, int id, IntByReference retfDeleted);
	STATUS IDDeleteAll (DHANDLE.ByValue hTable);
	STATUS IDTableIntersect(DHANDLE.ByValue hSrc1Table, DHANDLE.ByValue hSrc2Table, DHANDLE.ByReference rethDstTable);
	STATUS IDTableCopy (DHANDLE.ByValue hTable, DHANDLE.ByReference rethTable);
	boolean IDScan (DHANDLE.ByValue hTable, boolean fFirst, IntByReference retID);
	boolean IDScanBack (DHANDLE.ByValue hTable, boolean fLast, IntByReference retID);
	STATUS IDInsertTable (DHANDLE.ByValue hTable, DHANDLE.ByValue hIDsToAdd);
    STATUS IDInsertRange(DHANDLE.ByValue hTable, int IDFrom, int IDTo, boolean AddToEnd);
    STATUS IDEnumerate(DHANDLE.ByValue hTable, NotesCallbacks.IdEnumerateProc Routine, Pointer Parameter);
	short IDTableFlags (Pointer pIDTable);
	void IDTableSetFlags (Pointer pIDTable, WORD Flags);
	void IDTableSetTime(Pointer pIDTable, NotesTimeDateStruct Time);
	NotesTimeDateStruct IDTableTime(Pointer pIDTable);
	STATUS IDDeleteTable(DHANDLE.ByValue hTable, DHANDLE.ByValue hIDsToDelete);
	
	STATUS NSFNoteOpenExt(HANDLE.ByValue hDB, int noteId, int flags, DHANDLE.ByReference rethNote);
	STATUS NSFNoteOpenSoftDelete(HANDLE.ByValue hDB, int NoteID, int Reserved, DHANDLE.ByReference rethNote);

	STATUS NSFNoteClose(DHANDLE.ByValue hNote);
	STATUS NSFNoteOpenByUNIDExtended(HANDLE.ByValue hDB, NotesUniversalNoteIdStruct pUNID, int flags, DHANDLE.ByReference rtn); 
	STATUS NSFNoteCreate(HANDLE.ByValue db_handle, DHANDLE.ByReference note_handle);
	void NSFNoteGetInfo(DHANDLE.ByValue hNote, WORD type, Pointer retValue);
	void NSFNoteSetInfo(DHANDLE.ByValue hNote, WORD type, Pointer value);
	STATUS NSFNoteUpdateExtended(DHANDLE.ByValue hNote, int updateFlags);
	STATUS NSFNoteDeleteExtended(DHANDLE.ByValue hDB, int NoteID, int UpdateFlags);
	STATUS NSFNoteCopy(
			DHANDLE.ByValue note_handle_src,
			DHANDLE.ByReference note_handle_dst_ptr);
	STATUS NSFDbGenerateOID(HANDLE.ByValue hDB, Pointer retOID);

	void OSGetExecutableDirectory(Memory retPathName);
	void OSGetDataDirectory(Memory retPathName);
	/**
	 * @param retPathName the destination path-name storage
	 * @return the length of the directory name, as a {@code WORD}
	 * @since 1.0.43
	 */
	short OSGetSharedDataDirectory(Memory retPathName);
	STATUS CreateDAConfiguration(CreateDAConfigStruct ldap);
	STATUS UpdateDAConfiguration(UpdateDAConfigStruct ldap);
	STATUS EnableDisableDADomain(EnableDisableDAStruct daConfig);
	STATUS VerifyLDAPConnection(VerifyLDAPConnectionStruct ldap);
    @UndocumentedAPI
    short OSGetSystemTempDirectory(Memory retPathName, int bufferLength);
    @UndocumentedAPI
    void OSPathAddTrailingPathSep(Memory retPathName);
	short OSGetEnvironmentString(Memory variableName, Memory rethValueBuffer, WORD bufferLength);
	long OSGetEnvironmentLong(Memory variableName);
	void OSSetEnvironmentVariable(Memory variableName, Memory Value);
    @UndocumentedAPI
	void OSSetEnvironmentVariableExt (Memory variableName, Memory Value, short isSoft);
	void OSSetEnvironmentInt(Memory variableName, int Value);

	short OSGetEnvironmentSeqNo();

	@UndocumentedAPI
	short OSRunNSDExt (Memory szServerName, WORD flags);

	void NIFGetViewRebuildDir(Memory retPathName, int BufferLength);

	STATUS NSFFormulaCompile(
			Memory formulaName,
			WORD formulaNameLength,
			Memory formulaText,
			WORD  formulaTextLength,
			DHANDLE.ByReference rethFormula,
			WORD.ByReference retFormulaLength,
			ShortByReference retCompileError,
			WORD.ByReference retCompileErrorLine,
			WORD.ByReference retCompileErrorColumn,
			WORD.ByReference retCompileErrorOffset,
			WORD.ByReference retCompileErrorLength);
	STATUS NSFFormulaDecompile(
			Pointer pFormulaBuffer,
			boolean fSelectionFormula,
			DHANDLE.ByReference rethFormulaText,
			WORD.ByReference retFormulaTextLength);
	STATUS NSFFormulaGetSizeP(Pointer ptr, WORD.ByReference retFormulaLength);

	STATUS NSFFormulaSummaryItem(DHANDLE.ByValue hFormula, Memory ItemName, WORD ItemNameLength);
	STATUS NSFFormulaMerge(
			DHANDLE.ByValue hSrcFormula,
			DHANDLE.ByValue hDestFormula);
    STATUS NSFComputeStart(
        WORD Flags,
        Pointer lpCompiledFormula,
        DHANDLE.ByReference rethCompute);
	STATUS NSFComputeStop(DHANDLE.ByValue hCompute);

    @UndocumentedAPI
    STATUS NSFComputeEvaluateExt(DHANDLE.ByValue hCompute,
        DHANDLE.ByValue hNote,
        DHANDLE.ByReference rethResult,
        IntByReference retResultLength,
        int allowDWordResults,
        IntByReference retNoteMatchesFormula,
        IntByReference retNoteShouldBeDeleted,
        IntByReference retNoteModified);

    @UndocumentedAPI
    void NSFComputeSetDisallowFlags(DHANDLE.ByValue hCompute, int dwFlags);

    @UndocumentedAPI
    STATUS NSFFormulaAnalyze(DHANDLE.ByValue hFormula,
        IntByReference retAttributes,
        WORD.ByReference retSummaryNamesOffset);

    @UndocumentedAPI
    STATUS NSFFormulaFunctions(NSFFORMFUNCPROC callback);

    @UndocumentedAPI
    STATUS NSFFormulaCommands(NSFFORMCMDSPROC callback);

    @UndocumentedAPI
    Pointer NSFFindFormulaParameters(Memory pszString);

    @UndocumentedAPI
    void DEBUGDumpHandleTable(int flags, WORD blkType);

	short ListGetNumEntries(Pointer vList, int noteItem);

	STATUS ListGetText (Pointer pList,
			boolean fPrefixDataType,
			WORD entryNumber,
			PointerByReference retTextPointer,
			WORD.ByReference retTextLength);

    STATUS ListGetText (ByteBuffer pList,
            boolean fPrefixDataType,
            WORD entryNumber,
            ByteBuffer retTextPointer,
            WORD.ByReference retTextLength);

	short ListGetSize(
			Pointer pList,
			int fPrefixDataType);

	void NSFItemQueryEx(
			DHANDLE.ByValue note_handle,
			NotesBlockIdStruct.ByValue item_bid,
			Memory item_name,
			WORD  return_buf_len,
			WORD.ByReference name_len_ptr,
			WORD.ByReference item_flags_ptr,
			WORD.ByReference value_datatype_ptr,
			Pointer value_bid_ptr,
			IntByReference value_len_ptr,
			ByteByReference retSeqByte,
			ByteByReference retDupItemID);

	short NSFNoteHasReadersField(DHANDLE.ByValue note_handle, NotesBlockIdStruct bhFirstReadersItem);

	STATUS NSFItemCopy(DHANDLE.ByValue note_handle, NotesBlockIdStruct.ByValue item_blockid);
	STATUS NSFItemCopyAndRename (DHANDLE.ByValue hNote, NotesBlockIdStruct.ByValue bhItem, Memory pszNewItemName);

	STATUS NSFNoteDetachFile(DHANDLE.ByValue note_handle, NotesBlockIdStruct.ByValue item_blockid);

	STATUS NSFDbReadObject(
			HANDLE.ByValue hDB,
			int objectID,
			int offset,
			int length,
			DHANDLE.ByReference rethBuffer);
	
	@UndocumentedAPI
	STATUS NSFDbAllocObjectExtended2(HANDLE.ByValue cDB,
			int size, WORD noteClass, WORD privs, WORD type, IntByReference rtnRRV);

	STATUS NSFDbWriteObject(
			HANDLE.ByValue hDB,
			int objectID,
			DHANDLE.ByValue hBuffer,
			int offset,
			int length);
	
	STATUS NSFDbFreeObject(
			HANDLE.ByValue hDB,
			int objectID);
	
	STATUS NSFDbReallocObject(
			HANDLE.ByValue hDB,
			int objectID,
			int newSize);

	STATUS NSFItemAppendObject(
			DHANDLE.ByValue hNote,
			WORD ItemFlags,
			Memory Name,
			WORD NameLength,
			NotesBlockIdStruct.ByValue bhValue,
			int ValueLength,
			int fDealloc);
	
	STATUS NSFDbGetObjectSize(
			HANDLE.ByValue hDB,
			int objectID,
			WORD objectType,
			IntByReference retSize,
			ShortByReference retClass,
			ShortByReference retPrivileges);

	STATUS NSFNoteCipherExtractWithCallback (DHANDLE.ByValue hNote, NotesBlockIdStruct.ByValue bhItem,
			int extractFlags, int hDecryptionCipher,
			NotesCallbacks.NoteExtractCallback pNoteExtractCallback, Pointer pParam,
			int Reserved, Pointer pReserved);

	STATUS NSFItemAppendByBLOCKID(
			DHANDLE.ByValue note_handle,
			WORD item_flags,
			Memory item_name,
			WORD name_len,
			NotesBlockIdStruct.ByValue value_bid,
			int value_len,
			NotesBlockIdStruct item_bid_ptr);

	STATUS NSFItemAppend(
	    DHANDLE.ByValue note_handle,
	    WORD item_flags,
	    Memory item_name,
	    WORD name_len,
	    WORD item_type,
	    Pointer item_value,
	    int value_len);

	STATUS NSFItemInfo(
			DHANDLE.ByValue note_handle,
			Memory item_name,
			WORD  name_len,
			Pointer retbhItem,
			WORD.ByReference retDataType,
			Pointer retbhValue,
			IntByReference retValueLength);

	STATUS NSFItemInfoNext(
			DHANDLE.ByValue note_handle,
			NotesBlockIdStruct.ByValue NextItem,
			Memory item_name,
			WORD  name_len,
			Pointer retbhItem,
			WORD.ByReference retDataType,
			Pointer retbhValue,
			IntByReference retValueLength);

	STATUS NSFItemInfoPrev(
			DHANDLE.ByValue note_handle,
			NotesBlockIdStruct.ByValue  CurrItem,
			Memory item_name,
			WORD  name_len,
			NotesBlockIdStruct item_blockid_ptr,
			ShortByReference value_type_ptr,
			NotesBlockIdStruct value_blockid_ptr,
			IntByReference value_len_ptr);

	STATUS NSFItemDelete(
			DHANDLE.ByValue note_handle,
			Memory item_name,
			short name_len);
	
    short NSFItemConvertToText(
        DHANDLE.ByValue note_handle,
        Memory item_name_ptr,
        Memory text_buf_ptr,
        WORD text_buf_len,
        char separator);

    short NSFItemConvertValueToText(
        WORD value_type,
        NotesBlockIdStruct.ByValue value_bid,
        int value_len,
        Memory text_buf_ptr,
        WORD text_buf_len,
        char separator);

	boolean NSFNoteIsSignedOrSealed(DHANDLE.ByValue note_handle, ByteByReference signed_flag_ptr, ByteByReference sealed_flag_ptr);

	@UndocumentedAPI
	STATUS NSFNoteExpand(DHANDLE.ByValue hNote);
	STATUS NSFNoteVerifySignature(
			DHANDLE.ByValue hNote,
			Memory SignatureItemName,
			NotesTimeDateStruct retWhenSigned,
			Memory retSigner,
			Memory retCertifier);
	STATUS NSFNoteContract(DHANDLE.ByValue hNote);

	STATUS NIFFindDesignNoteExt(HANDLE.ByValue hFile, Memory name, WORD noteClass, Memory pszFlagsPattern, IntByReference retNoteID, int options);
	
    STATUS NIFOpenCollection(HANDLE.ByValue hViewDB, HANDLE.ByValue hDataDB, int viewNoteID,
        WORD openFlags, HANDLE.ByValue hUnreadList,
        DHANDLE.ByReference rethCollection, DHANDLE.ByReference rethViewNote, Memory retViewUNID,
        HANDLE.ByReference rethCollapsedList, HANDLE.ByReference rethSelectedList);
	
	STATUS NIFOpenCollectionWithUserNameList (HANDLE.ByValue hViewDB, HANDLE.ByValue hDataDB,
			int viewNoteID, WORD openFlags,
			DHANDLE.ByValue hUnreadList,
			DHANDLE.ByReference rethCollection,
			DHANDLE.ByReference rethViewNote, Memory retViewUNID,
			DHANDLE.ByReference rethCollapsedList,
			DHANDLE.ByReference rethSelectedList,
			DHANDLE.ByValue nameList);

    void NIFGetLastModifiedTime(DHANDLE.ByValue hCollection, NotesTimeDateStruct retLastModifiedTime);
    @UndocumentedAPI
    void NIFGetLastAccessedTime(DHANDLE.ByValue hCollection, NotesTimeDateStruct retLastModifiedTime);

    @UndocumentedAPI
    void NIFGetNextDiscardTime(DHANDLE.ByValue hCollection, NotesTimeDateStruct retLastModifiedTime);
    STATUS NIFGetCollation(DHANDLE.ByValue hCollection, WORD.ByReference retCollationNum);
    STATUS NIFSetCollation(DHANDLE.ByValue hCollection, WORD CollationNum);
	/**
	 * Retrieve detailed information about the collection itself, such as the number of documents
	 * in the collection and the total size of the document entries in the collection.
	 * 
	 * @since Notes/Domino 4.0
	 * @since 1.44.0
	 */
    STATUS NIFGetCollectionData(DHANDLE.ByValue hCollection, DHANDLE.ByReference rethCollData);
    STATUS NIFUpdateCollection(DHANDLE.ByValue hCollection);

	@UndocumentedAPI
	STATUS NIFGetCollectionDocCountLW(DHANDLE.ByValue hCol, IntByReference pDocct);

	STATUS NIFFindByKeyExtended2 (DHANDLE.ByValue hCollection, Memory keyBuffer,
			int findFlags,
			int returnFlags,
			NotesCollectionPositionStruct retIndexPos,
			IntByReference retNumMatches,
			WORD.ByReference retSignalFlags,
			DHANDLE.ByReference rethBuffer,
			IntByReference retSequence);

	STATUS NIFFindByKeyExtended4 (DHANDLE.ByValue hCollection,
            Memory keyBuffer, int findFlags,
            int returnFlags,
            Memory pReserved,
            NotesCollectionPositionStruct retIndexPos,
            IntByReference retNumMatches, WORD.ByReference retSignalFlags,
            DHANDLE.ByReference rethBuffer, IntByReference retSequence,
            NotesCallbacks.NIFFindByKeyProc NIFFindByKeyCallback, NIFFindByKeyContextStruct Ctx, int dwTimeOutSec);
	
	STATUS NIFFindByKey(DHANDLE.ByValue hCollection, Memory keyBuffer, WORD findFlags,
			NotesCollectionPositionStruct retIndexPos, IntByReference retNumMatches);

	STATUS NSFDbModeGet(
			HANDLE.ByValue hDB,
			ShortByReference retMode);

	STATUS NIFReadEntries(DHANDLE.ByValue hCollection, NotesCollectionPositionStruct indexPos, WORD skipNavigator,
			int skipCount, short returnNavigator, int returnCount, int returnMask, DHANDLE.ByReference rethBuffer,
			WORD.ByReference retBufferLength, IntByReference retNumEntriesSkipped, IntByReference retNumEntriesReturned,
			WORD.ByReference retSignalFlags);

	STATUS NIFReadEntriesExt(DHANDLE.ByValue hCollection,
			NotesCollectionPositionStruct collectionPos,
			WORD skipNavigator, int skipCount,
			WORD returnNavigator, int returnCount, int returnMask,
            NotesTimeDateStruct diffTime, DHANDLE.ByValue diffIDTable, int columnNumber, int flags,
            DHANDLE.ByReference rethBuffer, WORD.ByReference retBufferLength,
            IntByReference retNumEntriesSkipped, IntByReference retNumEntriesReturned,
            WORD.ByReference retSignalFlags, NotesTimeDateStruct retDiffTime,
            NotesTimeDateStruct retModifiedTime, IntByReference retSequence);

    @UndocumentedAPI
    STATUS NSFSearchExtended3(HANDLE.ByValue hDB,
        DHANDLE.ByValue hFormula,
        DHANDLE.ByValue hFilter,
        int filterFlags,
        Memory ViewTitle,
        int SearchFlags,
        int SearchFlags1,
        int SearchFlags2,
        int SearchFlags3,
        int SearchFlags4,
        WORD NoteClassMask,
        NotesTimeDateStruct Since,
        NotesCallbacks.NsfSearchProc EnumRoutine,
        Pointer EnumRoutineParameter,
        NotesTimeDateStruct retUntil,
        DHANDLE.ByValue namelist);

    STATUS NIFGetIDTableExtended(DHANDLE.ByValue hCollection, WORD navigator, WORD Flags, DHANDLE.ByValue hIDTable);
	
	short NSFDbIsRemote(HANDLE.ByValue hDb);

	STATUS NSFDbGetBuildVersion(HANDLE.ByValue hDB, WORD.ByReference retVersion);

	STATUS NSFDbLocateByReplicaID(
			HANDLE.ByValue hDB,
			NotesTimeDateStruct ReplicaID,
			Memory retPathName,
			WORD PathMaxLen);
	
	STATUS NSFDbReadACL(
			HANDLE.ByValue hDB,
			DHANDLE.ByReference rethACL);
	
	STATUS NSFDbStoreACL(
			HANDLE.ByValue hDB,
			DHANDLE.ByValue hACL,
			int ObjectID,
			WORD Method);
	
	STATUS ACLLookupAccess(
			DHANDLE.ByValue hACL,
			Pointer pNamesList,
			WORD.ByReference retAccessLevel,
			Memory retPrivileges,
			WORD.ByReference retAccessFlags,
			DHANDLE.ByReference rethPrivNames);
	
	STATUS ACLSetAdminServer(
			DHANDLE.ByValue hACL,
			Memory ServerName);
	
	STATUS ACLGetAdminServer(
			DHANDLE.ByValue hACL,
			Memory retServerName);
	
	STATUS ACLGetPrivName(
			DHANDLE.ByValue hACL,
			WORD PrivNum,
			Memory retPrivName);
	
	STATUS ACLSetPrivName(
			DHANDLE.ByValue hACL,
			WORD PrivNum,
			Memory privName);
	
	STATUS ACLAddEntry(
			DHANDLE.ByValue hACL,
			Memory name,
			WORD AccessLevel,
			Memory privileges,
			WORD AccessFlags);
	
	STATUS ACLDeleteEntry(
			DHANDLE.ByValue hACL,
			Memory name);
	
	STATUS ACLUpdateEntry(
			DHANDLE.ByValue hACL,
			Memory name,
			WORD updateFlags,
			Memory newName,
			WORD newAccessLevel,
			Memory newPrivileges,
			WORD newAccessFlags);
	
	STATUS ACLEnumEntries(
			DHANDLE.ByValue hACL,
			ACLENTRYENUMFUNC EnumFunc,
			Pointer EnumFuncParam);
	
	STATUS ACLSetFlags(
			DHANDLE.ByValue hACL,
			int Flags);
	
	STATUS ACLGetFlags(
			DHANDLE.ByValue hACL,
			IntByReference retFlags);

	
	// *******************************************************************************
	// * dxl.h
	// *******************************************************************************
	
	STATUS DXLCreateExporter(
			IntByReference prethDXLExport);
	void DXLDeleteExporter(
			int hDXLExport);
	STATUS DXLExportACL(
			int hDXLExport,
			NotesCallbacks.XML_WRITE_FUNCTION pDXLWriteFunc,
			HANDLE.ByValue hDB,
			Pointer pExAction);
	STATUS DXLExportDatabase(
			int hDXLExport,
			NotesCallbacks.XML_WRITE_FUNCTION pDXLWriteFunc,
			HANDLE.ByValue hDB,
			Pointer pExAction);
	STATUS DXLExportIDTable(
			int hDXLExport,
			NotesCallbacks.XML_WRITE_FUNCTION pDXLWriteFunc,
			HANDLE.ByValue hDB,
			DHANDLE.ByValue hIDTable,
			Pointer pExAction);
	STATUS DXLExportNote(
			int hDXLExport,
			NotesCallbacks.XML_WRITE_FUNCTION pDXLWriteFunc,
			DHANDLE.ByValue hNote,
			Pointer pExAction);
	boolean DXLExportWasErrorLogged(
			int hDXLExport);
	STATUS DXLGetExporterProperty(
			int hDXLExport,
			int prop,
			Pointer retPropValue);
	STATUS DXLSetExporterProperty(
			int hDXLExport,
			int prop,
			Pointer propValue);
	
	STATUS DXLCreateImporter(
			IntByReference prethDXLImport);
	void DXLDeleteImporter(
			int hDXLImport);
	void DXLGetImporterProperty(
			int hDxlImporter,
			int prop,
			Pointer retPropValue);
	STATUS DXLImport(
			int hDXLImport,
			NotesCallbacks.XML_READ_FUNCTION pDXLReaderFunc,
			HANDLE.ByValue hDB,
			Pointer pImAction);
	boolean DXLImportWasErrorLogged(
			int hDXLImport);
	STATUS DXLSetImporterProperty(
			int hDXLImport,
			int prop,
			Pointer propValue);

    /**
     * Copies the ACL in hList into hNewList.
     * Unlocks, then locks both handles.
     * Allocates memory for the copied ACL in hNewList
     * 
     * @param hList the source list to copy
     * @param hNewList a handle pointer to house the new copy
     * @return the result status
     */
    @UndocumentedAPI
    STATUS ACLCopy(DHANDLE.ByValue hList, DHANDLE.ByReference hNewList);

    STATUS ACLCreate(DHANDLE.ByReference rethACL);

    STATUS CompoundTextCreate(
        DHANDLE.ByValue hNote,
        Memory pszItemName,
        DHANDLE.ByReference phCompound);

	Pointer OSGetLMBCSCLS();

	void CompoundTextInitStyle(NotesCompoundStyleStruct style);

	STATUS CompoundTextDefineStyle(
			DHANDLE.ByValue hCompound,
			Memory pszStyleName,
			NotesCompoundStyleStruct pDefinition,
			IntByReference pdwStyleID);
	
	void CompoundTextDiscard(
			DHANDLE.ByValue hCompound);

	STATUS CompoundTextAddTextExt(
			DHANDLE.ByValue hCompound,
			int dwStyleID,
			int FontID,
			Memory pchText,
			int dwTextLen,
			Memory pszLineDelim,
			int dwFlags,
			Pointer pInfo);

	STATUS CompoundTextAddCDRecords(
			DHANDLE.ByValue hCompound,
			Pointer pvRecord,
			int dwRecordLength);

	STATUS CompoundTextAddDocLink(
			DHANDLE.ByValue hCompound,
			NotesTimeDateStruct.ByValue DBReplicaID,
			NotesUniversalNoteIdStruct.ByValue ViewUNID,
			NotesUniversalNoteIdStruct.ByValue NoteUNID,
			Memory pszComment,
			int dwFlags);

	STATUS CompoundTextAssimilateItem(
			DHANDLE.ByValue hCompound,
			DHANDLE.ByValue hNote,
			Memory pszItemName,
			int dwFlags);

	STATUS CompoundTextClose(
			DHANDLE.ByValue hCompound,
			DHANDLE.ByReference phReturnBuffer,
			IntByReference pdwReturnBufferSize,
			Memory pchReturnFile,
			WORD wReturnFileNameSize);

	boolean IsRunAsWebUser(DHANDLE.ByValue hAgent);
	STATUS AgentOpen (HANDLE.ByValue hDB, int AgentNoteID, DHANDLE.ByReference rethAgent);
	void AgentClose (DHANDLE.ByValue hAgent);

	STATUS AgentCreateRunContextExt (DHANDLE.ByValue hAgent, Pointer pReserved, int pOldContext, int dwFlags, DHANDLE.ByReference rethContext);
	STATUS AgentSetDocumentContext(DHANDLE.ByValue hAgentCtx, DHANDLE.ByValue hNote);
	STATUS AgentSetTimeExecutionLimit(DHANDLE.ByValue hAgentCtx, int timeLimit);
	boolean AgentIsEnabled(DHANDLE.ByValue hAgent);
	
	void SetParamNoteID(DHANDLE.ByValue hAgentCtx, int noteId);
	
	STATUS AgentRedirectStdout(DHANDLE.ByValue hAgentCtx, WORD redirType);
	void AgentQueryStdoutBuffer(DHANDLE.ByValue hAgentCtx, DHANDLE.ByReference retHdl, IntByReference retSize);
	void AgentDestroyRunContext (DHANDLE.ByValue hAgentCtx);
	STATUS AgentRun (DHANDLE.ByValue hAgent,
			DHANDLE.ByValue hAgentCtx,
			DHANDLE.ByValue hSelection,
			int dwFlags);
	
	STATUS AgentSetHttpStatusCode(DHANDLE.ByValue hAgentCtx, int httpStatus);
	
	STATUS ClientRunServerAgent(HANDLE.ByValue hdb, int nidAgent, int nidParamDoc,
			int bForeignServer, int bSuppressPrintToConsole);

	STATUS MQCreate(Memory queueName, WORD quota, int options);
	STATUS MQOpen(Memory queueName, int options, IntByReference retQueue);
	STATUS MQClose(int queue, int options);
	STATUS MQPut(int queue, WORD priority, Pointer buffer, WORD length, int options);
	STATUS MQGet(int queue, Pointer buffer, WORD bufLength, int options, int timeout, WORD.ByReference retMsgLength);
	void MQPutQuitMsg(int queue);
	boolean MQIsQuitPending(int queue);
	STATUS MQScan(int queue, Pointer buffer, WORD bufLength, int options, NotesCallbacks.MQScanCallback actionRoutine,
			Pointer ctx, WORD.ByReference retMsgLength);
	
	STATUS MMCreateConvControls(
			PointerByReference phCC);

	STATUS MMDestroyConvControls(
			Pointer hCC);
	
	void MMConvDefaults(
			Pointer hCC);

	short MMGetAttachEncoding(
			Pointer hCC);
	
	void MMSetAttachEncoding(
			Pointer hCC,
			WORD wAttachEncoding);
	
	void MMSetDropItems(
			Pointer hCC,
			Memory pszDropItems);
	
	Pointer MMGetDropItems(
			Pointer hCC);
	
	void MMSetKeepTabs(
			Pointer hCC,
			boolean bKeepTabs);
	
	boolean MMGetKeepTabs(
			Pointer hCC);
	
	void MMSetPointSize(
			Pointer hCC,
			WORD wPointSize);

	short MMGetPointSize(
			Pointer hCC);
	
	short MMGetTypeFace(
			Pointer hCC);
	
	void MMSetTypeFace(
			Pointer hCC,
			WORD wTypeFace);

	void MMSetAddItems(
			Pointer hCC,
			Memory pszAddItems);

	Pointer MMGetAddItems(
			Pointer hCC);
	
	void MMSetMessageContentEncoding(
			Pointer hCC,
			WORD wMessageContentEncoding);

	short MMGetMessageContentEncoding(
			Pointer hCC);
	
	void MMSetReadReceipt(
			Pointer hCC,
			WORD wReadReceipt);

	short MMGetReadReceipt(
			Pointer hCC);
	
	void MMSetSkipX(
			Pointer hCC,
			boolean bSkipX);
	
	boolean MMGetSkipX(
			Pointer hCC);
	
	int MIMEStreamPutLine(
			Memory pszLine,
			Pointer hMIMEStream);

	int MIMEStreamRead(
			Memory pchData,
			IntByReference puiDataLen,
			int uiMaxDataLen,
			Pointer hMIMEStream);	
	
	int MIMEStreamRewind(
			Pointer hMIMEStream);

	int MIMEStreamWrite(
			Memory pchData,
			int  uiDataLen,
			Pointer hMIMEStream);

	void MIMEStreamClose(
			Pointer hMIMEStream);

	int MIMEStreamGetLine(
			Memory pszLine,
			int uiMaxLineSize,
			Pointer hMIMEStream);

	STATUS MIMEStreamItemize(
			DHANDLE.ByValue hNote,
			Memory pchItemName,
			WORD wItemNameLen,
			int dwFlags,
			Pointer hMIMEStream);
	
	STATUS MIMEStreamOpen(
			DHANDLE.ByValue hNote,
			Memory pchItemName,
			WORD wItemNameLen,
			int dwOpenFlags,
			PointerByReference rethMIMEStream);

	STATUS MIMEConvertCDParts(
			DHANDLE.ByValue hNote,
			boolean bCanonical,
			boolean bIsMIME,
			Pointer hCC);
	
	STATUS MIMEConvertRFC822TextItems(
	    DHANDLE.ByValue hNote,
	    boolean bCanonical);
	
	STATUS MIMEConvertRFC822TextItemByBLOCKID(
			DHANDLE.ByValue hNote,
			NotesBlockIdStruct.ByValue bhItem,
			NotesBlockIdStruct.ByValue bhValue);

	STATUS NSFProfileOpen(
			HANDLE.ByValue hDB,
			Memory ProfileName,
			WORD ProfileNameLength,
			Memory UserName,
			WORD UserNameLength,
			WORD CopyProfile,
			DHANDLE.ByReference rethProfileNote);
	STATUS NSFProfileUpdate(
			DHANDLE.ByValue hProfile,
			Memory ProfileName,
			WORD ProfileNameLength,
			Memory UserName,
			WORD UserNameLength);
	STATUS NSFProfileSetField(
			HANDLE.ByValue hDB,
			Memory ProfileName,
			WORD ProfileNameLength,
			Memory UserName,
			WORD UserNameLength,
			Memory FieldName,
			WORD FieldNameLength,
			WORD Datatype,
			Pointer Value,
			int ValueLength);
	STATUS NSFProfileDelete(
			HANDLE.ByValue hDB,
			Memory ProfileName,
			WORD ProfileNameLength,
			Memory UserName,
			WORD UserNameLength);

	STATUS NSFProfileEnum(
			HANDLE.ByValue hDB,
			Memory ProfileName,
			WORD ProfileNameLength,
			NotesCallbacks.NSFPROFILEENUMPROC Callback,
			Pointer CallbackCtx,
			int Flags);
	
	STATUS NSFDbGetMultNoteInfoByUNID(
			HANDLE.ByValue hDB,
			WORD Count,
			WORD Options,
			DHANDLE.ByValue hInBuf,
			IntByReference retSize,
			DHANDLE.ByReference rethOutBuf);

	STATUS NSFDbGetMultNoteInfo(
			HANDLE.ByValue  hDb,
			WORD  Count,
			WORD  Options,
			DHANDLE.ByValue hInBuf,
			IntByReference retSize,
			DHANDLE.ByReference rethOutBuf);

	STATUS NSFDbGetNoteInfoExt(
			HANDLE.ByValue hDB,
			int  NoteID,
			Pointer retNoteOID,
			NotesTimeDateStruct retModified,
			WORD.ByReference retNoteClass,
			NotesTimeDateStruct retAddedToFile,
			WORD.ByReference retResponseCount,
			IntByReference retParentNoteID);

    @UndocumentedAPI
    short NSFDbLargeSummaryEnabled(HANDLE.ByValue hDB);

    @UndocumentedAPI
    STATUS NSFDbGetOptionsExt(HANDLE.ByValue hDB, Memory retDbOptions);
    @UndocumentedAPI
    STATUS NSFDbSetOptionsExt(HANDLE.ByValue hDB, Memory dbOptions, Memory mask);

    STATUS NSFDbIsLocallyEncrypted(HANDLE.ByValue hDB, IntByReference retVal);

    STATUS FTGetLastIndexTime(HANDLE.ByValue hDB, NotesTimeDateStruct retTime);
    STATUS FTDeleteIndex(HANDLE.ByValue hDB);

	@UndocumentedAPI
	STATUS NSFDbGetNamedObjectID(HANDLE.ByValue hDB, WORD NameSpace,
            Memory Name, WORD NameLength,
            IntByReference rtnObjectID);

    @UndocumentedAPI
    STATUS NSFGetNamedObjects(HANDLE.ByValue hDb, WORD Namespace, IntByReference rethBuffer,
        IntByReference retBufferLength);

    STATUS NSFNoteAttachFile(
			DHANDLE.ByValue note_handle,
			Memory item_name,
			WORD item_name_length,
			Memory file_name,
			Memory orig_path_name,
			WORD encoding_type);

	STATUS NSFDbNoteLock(
			HANDLE.ByValue hDB,
			int NoteID,
			int Flags,
			Memory pLockers,
			DHANDLE.ByReference rethLockers,
			IntByReference retLength);
	
	STATUS NSFDbNoteUnlock(
			HANDLE.ByValue hDB,
			int NoteID,
			int Flags);

	STATUS NSFNoteOpenWithLock(
			HANDLE.ByValue hDB,
			int NoteID,
			int LockFlags,
			int OpenFlags,
			Memory pLockers,
			DHANDLE.ByReference rethLockers,
			IntByReference retLength,
			DHANDLE.ByReference rethNote);

	short NSFNoteHasComposite(DHANDLE.ByValue hNote);
	short NSFNoteHasMIME(DHANDLE.ByValue hNote);
	short NSFNoteHasMIMEPart(DHANDLE.ByValue hNote);

	STATUS NSFDbCreateAndCopyExtended(
			Memory srcDb,
			Memory dstDb,
			WORD NoteClass,
			WORD limit,
			int flags,
			DHANDLE.ByValue hNames,
			HANDLE.ByReference hNewDb);

	STATUS NIFCloseCollection(DHANDLE.ByValue hCollection);

	STATUS NSFDbOpenTemplateExtended(
			Memory PathName,
			WORD Options,
			DHANDLE hNames,
			NotesTimeDateStruct ModifiedTime,
			HANDLE.ByReference rethDB,
			NotesTimeDateStruct retDataModified,
			NotesTimeDateStruct retNonDataModified);

	STATUS NSFQueryDBExt2(HANDLE.ByValue hDb, Memory query, int qlen, int flags,
			int maxDocsScanned, int maxEntriesScanned, int maxMsecs,
			DHANDLE.ByReference retResults, IntByReference retError, IntByReference retExplain,
			int hQArgList);
	
	/**
	 * Build a list of values to input into a Domino Query Language (DQL) query
	 * 
	 * @param pArg QUEP_ARGVAL* - A value, ordinal, or name for the argument to build
	 * @param phQargList MEMHANDLE*  Set of QUEP_ARGVALs that were built
	 * @return NOERROR - On success. ERR_MISC_INVALID_ARGS - Invalid arguments.
	 *         ERR_MEMORY - Memory failure
	 */
	STATUS NSFQueryDBAddArgs(QuepArgVal pArg, IntByReference phQargList);

	STATUS FTOpenSearch(DHANDLE.ByReference rethSearch);

	STATUS FTSearchExt(
			HANDLE.ByValue hDB,
			DHANDLE.ByReference phSearch,
			DHANDLE.ByValue hColl,
			Memory Query,
			int Options,
			WORD Limit,
			DHANDLE.ByValue hIDTable,
			IntByReference retNumDocs,
			DHANDLE.ByReference Reserved,
			DHANDLE.ByReference rethResults,
			IntByReference retNumHits,
			int Start,
			WORD Count,
			WORD Arg,
			DHANDLE.ByValue hNames);

	STATUS FTCloseSearch(
			DHANDLE.ByValue hSearch);
	
    @UndocumentedAPI
    STATUS NSFDesignHarvest(HANDLE.ByValue hDB, boolean rebuild);

    STATUS FTIndex(HANDLE.ByValue hDB, WORD options, Memory stopFile, NotesFTIndexStatsStruct retStats);

	@UndocumentedAPI
	STATUS ClientFTIndexRequest(HANDLE.ByValue hDB);

	// *******************************************************************************
	// * htmlapi.h
	// *******************************************************************************
	
	STATUS HTMLProcessInitialize();
	void HTMLProcessTerminate();
	
	STATUS NSFDbGetModifiedNoteTable(HANDLE.ByValue hDB, WORD NoteClassMask,
			NotesTimeDateStruct.ByValue Since, NotesTimeDateStruct.ByReference retUntil,
			DHANDLE.ByReference rethTable);

    @UndocumentedAPI
    STATUS NSFDbGetModifiedNotesInfo(HANDLE.ByValue hDB,		/* Database handle */
	    WORD noteClass,	/* 0 == default == NOTE_CLASS_DOCUMENT; */
	    int infoRequestedFlags, /* Additional Info requested. DB_GET_MODIFIED_INFO_TRU for now */	
	    NotesTimeDateStruct.ByValue Since,	/* Return "elements" from this timedate forward, TIMEDATE_WILDCARD */
	    IntByReference arrayCount,		/* total count of "elements" in the array */
	    NotesTimeDateStruct.ByReference retUntil, 		/* oldest modified/created value in this set.  */
	    IntByReference retmhArray	/* Array of DB_MODIFIED_INFO_ELEMENTS */
	    );
	
	STATUS NSFDbModifiedTimeByName(
	  Memory DbName,
	  NotesTimeDateStruct.ByReference retDataModified,
	  NotesTimeDateStruct.ByReference retNonDataModified
	);

	STATUS NIFUpdateFilters (DHANDLE.ByValue hCollection, WORD ModifyFlags);

	STATUS NIFLocateNote (DHANDLE.ByValue hCollection, NotesCollectionPositionStruct indexPos, int noteID);

	STATUS FolderCreate(
			HANDLE.ByValue hDataDB,
			HANDLE.ByValue hFolderDB,
			int FormatNoteID,
			HANDLE.ByValue hFormatDB,
			Memory pszName,
			WORD wNameLen,
			int FolderType,
			int dwFlags,
			IntByReference pNoteID);
	
	STATUS FolderRename(
			HANDLE.ByValue hDataDB,
			HANDLE.ByValue  hFolderDB,
			int FolderNoteID,
			Memory pszName,
			WORD wNameLen,
			int dwFlags);

	STATUS FolderDelete(
			HANDLE.ByValue  hDataDB,
			HANDLE.ByValue  hFolderDB,
			int FolderNoteID,
			int dwFlags);
	
	STATUS FolderCopy(
			HANDLE.ByValue  hDataDB,
			HANDLE.ByValue  hFolderDB,
			int FolderNoteID,
			Memory pszName,
			WORD wNameLen,
			int dwFlags,
			IntByReference pNewNoteID);
	
	STATUS FolderMove(
			HANDLE.ByValue hDataDB,
			HANDLE.ByValue hFolderDB,
			int FolderNoteID,
			HANDLE.ByValue hParentDB,
			int ParentNoteID,
			int dwFlags);

	STATUS FolderDocAdd(
			HANDLE.ByValue  hDataDB,
			HANDLE.ByValue  hFolderDB,
			int  FolderNoteID,
			DHANDLE.ByValue hTable,
			int  dwFlags);
	STATUS FolderDocCount(
			HANDLE.ByValue  hDataDB,
			HANDLE.ByValue  hFolderDB,
			int  FolderNoteID,
			int dwFlags,
			IntByReference pdwNumDocs);
	STATUS FolderDocRemove(
			HANDLE.ByValue  hDataDB,
			HANDLE.ByValue  hFolderDB,
			int  FolderNoteID,
			DHANDLE.ByValue hTable,
			int dwFlags);
	STATUS FolderDocRemoveAll(
			HANDLE.ByValue  hDataDB,
			HANDLE.ByValue  hFolderDB,
			int  FolderNoteID,
			int dwFlags);

	STATUS NSFFolderGetIDTable(
			HANDLE.ByValue  hViewDB,
			HANDLE.ByValue hDataDB,
			int  viewNoteID,
			int  flags,
			DHANDLE.ByReference hTable);

	STATUS NSFNoteSign(DHANDLE.ByValue hNote);
	STATUS NSFNoteSignExt3(DHANDLE.ByValue hNote, 
			Pointer hKFC,
			Memory SignatureItemName,
			WORD ItemCount, DHANDLE.ByReference hItemIDs, 
			int Flags, int Reserved,
			Pointer pReserved);

	STATUS NSFNoteUnsign(DHANDLE.ByValue hNote);

	// *******************************************************************************
	// * free-busy
	// *******************************************************************************
	
	STATUS SchFreeTimeSearch(
			NotesUniversalNoteIdStruct pApptUnid,
			NotesTimeDateStruct pApptOrigDate,
			WORD fFindFirstFit,
			int dwReserved,
			NotesTimeDatePairStruct pInterval,
			WORD Duration,
			Pointer pNames,
			DHANDLE.ByReference rethRange);
	
	void SchContainer_Free(DHANDLE.ByValue hCntnr);
	
	STATUS Schedule_Free(DHANDLE.ByValue hCntnr, int hSched);
	
	STATUS Schedule_ExtractBusyTimeRange(
			DHANDLE.ByValue hCntnr,
			int hSchedObj,
			NotesUniversalNoteIdStruct punidIgnore,
			NotesTimeDatePairStruct pInterval,
			IntByReference retdwSize,
			DHANDLE.ByReference rethRange,
			IntByReference rethMoreCtx);
	
	STATUS Schedule_ExtractMoreBusyTimeRange(
			DHANDLE.ByValue hCntnr,
			int hMoreCtx,
			NotesUniversalNoteIdStruct punidIgnore,
			NotesTimeDatePairStruct pInterval,
			IntByReference retdwSize,
			DHANDLE.ByReference rethRange,
			IntByReference rethMore);
	
	STATUS Schedule_ExtractFreeTimeRange(
			DHANDLE.ByValue hCntnr,
			int hSchedObj,
			NotesUniversalNoteIdStruct punidIgnore,
			WORD fFindFirstFit,
			WORD wDuration,
			NotesTimeDatePairStruct pInterval,
			IntByReference retdwSize,
			DHANDLE.ByReference rethRange);
	
	STATUS SchContainer_GetFirstSchedule(
			DHANDLE.ByValue hCntnr,
			IntByReference rethObj,
			Memory retpSchedule);

	STATUS SchContainer_GetNextSchedule(
			DHANDLE.ByValue hCntnr,
			int hCurSchedule,
			IntByReference rethNextSchedule,
			Memory retpNextSchedule);
	
	STATUS Schedule_ExtractSchedList(
			DHANDLE.ByValue hCntnr,
			int hSchedObj,
			NotesTimeDatePairStruct pInterval,
			IntByReference retdwSize,
			DHANDLE.ByReference rethSchedList,
			IntByReference rethMore);
	
	STATUS Schedule_ExtractMoreSchedList(
			DHANDLE.ByValue hCntnr,
			int hMoreCtx,
			NotesTimeDatePairStruct pInterval,
			IntByReference retdwSize,
			DHANDLE.ByReference rethSchedList,
			IntByReference rethMore);
	
	STATUS SchRetrieve(
			NotesUniversalNoteIdStruct pApptUnid,
			NotesTimeDateStruct pApptOrigDate,
			int dwOptions,
			NotesTimeDatePairStruct pInterval,
			Pointer pNames,
			DHANDLE.ByReference rethCntnr,
			Pointer mustBeNull1,
			Pointer mustBeNull2,
			Pointer mustBeNull3);
	
	STATUS HTMLCreateConverter(IntByReference phHTML);
	STATUS HTMLDestroyConverter(int hHTML);
	STATUS HTMLSetHTMLOptions(int hHTML, StringArray optionList);
	STATUS HTMLConvertItem(
			int hHTML,
			HANDLE.ByValue hDB,
			DHANDLE.ByValue hNote,
			Memory pszItemName);
	STATUS HTMLConvertNote(
			int hHTML,
			HANDLE.ByValue hDB,
			DHANDLE.ByValue hNote,
			int numArgs,
			HtmlApi_UrlComponentStruct pArgs);
	STATUS HTMLGetProperty(
			int hHTML,
			int propertyType,
			Pointer pProperty);
	STATUS HTMLSetProperty(
			int hHTML,
			int propertyType,
			Memory pProperty);
	STATUS HTMLGetText(
			int hHTML,
			int startingOffset,
			IntByReference pTextLength,
			Memory pText);
	STATUS HTMLGetReference(
			int hHTML,
			int index,
			IntByReference phRef);
	STATUS HTMLLockAndFixupReference(
			int hRef,
			Memory ppRef);
	STATUS HTMLConvertElement(
			int hHTML,
			HANDLE.ByValue hDB,
			DHANDLE.ByValue hNote,
			Memory pszItemName,
			int itemIndex,
			int offset);

	@UndocumentedAPI
	STATUS DesignOpenCollection(HANDLE.ByValue hDB,
            boolean bPrivate,
            WORD OpenFlags,
            DHANDLE.ByReference rethCollection,
            IntByReference retCollectionNoteID);
	
	@UndocumentedAPI
	STATUS NSFNoteCreateClone (DHANDLE.ByValue hSrcNote, DHANDLE.ByReference rethDstNote);

	@UndocumentedAPI
	STATUS NSFNoteReplaceItems (DHANDLE.ByValue hSrcNote, DHANDLE.ByValue hDstNote, WORD.ByReference pwRetItemReplaceCount, boolean fAllowDuplicates);

	STATUS StoredFormAddItems (HANDLE.ByValue hSrcDbHandle, DHANDLE.ByValue hSrcNote, DHANDLE.ByValue hDstNote, boolean bDoSubforms, int dwFlags);

	STATUS StoredFormRemoveItems(DHANDLE.ByValue hNote, int dwFlags);

	@UndocumentedAPI
	STATUS MailNoteJitEx2(Pointer vpRunCtx, DHANDLE.ByValue hNote, WORD wMailFlags, IntByReference retdwRecipients,
	    WORD jitflag, WORD wMailNoteFlags, NotesCallbacks.FPMailNoteJitEx2CallBack vCallBack, Pointer vCallBackCtx);

	@UndocumentedAPI
	STATUS MailSetSMTPMessageID(DHANDLE.ByValue hNote, Memory domain, Memory string, WORD stringLength);

    /*
     * This is exactly the same as lookup name BE, except it will use the
     * cache of the design collection as known to the client. This is safe
     * to use outside the client since it will map to the BE code. Using this
     * version will only be as up to date as the last time the design was
     * fetched for the client. This was added explicitly to avoid cases of
     * backend code executing in their world where backend changes to design
     * things was not seen immediately.
     */
    @UndocumentedAPI
    short DesignLookupNameFE (HANDLE.ByValue hDB, WORD wClass, Pointer szFlagsPattern, Pointer szName,
	    WORD wNameLen, int flags,
	    IntByReference retNoteID, IntByReference retbIsPrivate,
	    NotesCallbacks.DESIGN_COLL_OPENCLOSE_PROC OpenCloseRoutine, Pointer Ctx);

	STATUS NSFItemDeleteByBLOCKID(DHANDLE.ByValue note_handle, NotesBlockIdStruct.ByValue item_blockid);

	@UndocumentedAPI
	void DesignGetNameAndAlias(Memory pString, PointerByReference ppName, WORD.ByReference pNameLen, PointerByReference ppAlias, WORD.ByReference pAliasLen);

	@UndocumentedAPI
	boolean StoredFormHasSubformToken(Memory pString);

	STATUS ReplicateWithServerExt(
			Memory portName,
			Memory serverName,
			int options,
			WORD numFiles,
			Memory fileList,
			ReplExtensionsStruct extendedOptions,
			ReplServStatsStruct retStats);

	@NativeFunctionName(name="OSGetSignalHandler")
	NotesCallbacks.OSSIGBREAKPROC OSGetBreakSignalHandler(WORD signalHandlerID);
	@NativeFunctionName(name="OSSetSignalHandler")
	NotesCallbacks.OSSIGBREAKPROC OSSetBreakSignalHandler(WORD signalHandlerID, NotesCallbacks.OSSIGBREAKPROC routine);

	@NativeFunctionName(name="OSGetSignalHandler")
	NotesCallbacks.OSSIGPROGRESSPROC OSGetProgressSignalHandler(WORD signalHandlerID);
	@NativeFunctionName(name="OSSetSignalHandler")
	NotesCallbacks.OSSIGPROGRESSPROC OSSetProgressSignalHandler(WORD signalHandlerID, NotesCallbacks.OSSIGPROGRESSPROC routine);

	@NativeFunctionName(name="OSGetSignalHandler")
	NotesCallbacks.OSSIGREPLPROC OSGetReplicationSignalHandler(WORD signalHandlerID);
	@NativeFunctionName(name="OSSetSignalHandler")
	NotesCallbacks.OSSIGREPLPROC OSSetReplicationSignalHandler(WORD signalHandlerID, NotesCallbacks.OSSIGREPLPROC routine);

	
    STATUS SECKFMSwitchToIDFile(Memory pIDFileName, Memory pPassword, Memory pUserName,
	    WORD  MaxUserNameLength, int Flags, Pointer pReserved);

	STATUS NSFDbMarkInService(Memory dbPath);
	STATUS NSFDbMarkOutOfService(Memory dbPath);

	STATUS NSGetServerClusterMates(
			Memory pServerName,
			int dwFlags,
			DHANDLE.ByReference phList);
	STATUS NSPingServer(
			Memory pServerName,
			IntByReference pdwIndex,
			DHANDLE.ByReference phList);
	STATUS NSGetServerList(
			Memory pPortName,
			DHANDLE.ByReference retServerTextList);

	STATUS NSFRemoteConsole(
			Memory ServerName,
			Memory ConsoleCommand,
			DHANDLE.ByReference hResponseText);

	short NSFDbHasFullAccess(HANDLE.ByValue hDb);
	STATUS NSFDbReopen (HANDLE.ByValue hDb, HANDLE.ByReference retHDb);

	STATUS SECTokenGenerate(
			Memory ServerName,
			Memory OrgName,
			Memory ConfigName,
			Memory UserName,
			NotesTimeDateStruct Creation,
			NotesTimeDateStruct Expiration,
			IntByReference retmhToken,
			int dwReserved,
			Pointer vpReserved);
	void SECTokenFree(IntByReference mhToken);

	// phKFC is actually just a returned pointer; we are using DHANDLE here to use
	// its locking features for concurrency
	STATUS SECidfGet(Memory pUserName, Memory pPassword, Memory pPutIDFileHere,
			PointerByReference phKFC, Memory pServerName, int dwReservedFlags, WORD wReservedType,
			Pointer pReserved);
	STATUS SECidfPut(Memory pUserName, Memory pPassword, Memory pIDFilePath,
			PointerByReference phKFC, Memory pServerName, int dwReservedFlags, WORD wReservedType,
			Pointer pReserved);
	STATUS SECidfSync( Memory pUserName, Memory pPassword, Memory pIDFilePath,
			PointerByReference phKFC, Memory pServerName, int dwReservedFlags, WORD wReservedType,
			Pointer pReserved, IntByReference retdwFlags);

	STATUS SECKFMOpen(PointerByReference phKFC, Memory pIDFileName, Memory pPassword,
			int Flags, int Reserved, Pointer pReserved);

	STATUS SECKFMClose(PointerByReference phKFC, int flags, int reserved, Pointer pReserved);
    STATUS SECidvResetUserPassword(Memory pServer, Memory pUserName, Memory pPassword,
	    WORD wDownloadCount, int ReservedFlags, Pointer pReserved);

	STATUS SECKFMChangePassword(Memory pIDFile, Memory pOldPassword, Memory pNewPassword);
	
	STATUS SECNABEnumerateCertificates(
	    DHANDLE.ByValue hNote,
	    NotesCallbacks.SECNABENUMPROC CallBack,
	    Pointer pCallCtx,
	    int ReservedFlags,
	    Pointer pReserved
	);
	STATUS SECNABAddCertificate(
	    DHANDLE.ByValue hNote,
	    Pointer pCertificate,
	    int CertificateSize,
	    int ReservedFlags,
	    Pointer pReserved
    );

	STATUS SECNABRemoveCertificate(
        DHANDLE.ByValue hNote,
        Pointer pCertificate,
        int CertificateSize,
        int ReservedFlags,
        Pointer pReserved);

	STATUS REGGetIDInfo(
			Memory IDFileName,
			WORD InfoType,
			Memory OutBufr,
			WORD OutBufrLen,
			WORD.ByReference ActualLen);

	@UndocumentedAPI
	STATUS SECidvIsIDInVault(Memory pServer, Memory pUserName);

	STATUS NSFNoteCopyAndEncryptExt2(
			DHANDLE.ByValue hSrcNote,
			Pointer hKFC,
			WORD EncryptFlags,
			DHANDLE.ByReference rethDstNote,
			int Reserved,
			Pointer pReserved);
	STATUS NSFNoteCopyAndEncrypt(
			DHANDLE.ByValue hSrcNote,
			WORD EncryptFlags,
			DHANDLE.ByReference rethDstNote);
	STATUS NSFNoteCipherDecrypt(
			DHANDLE.ByValue hNote,
			Pointer hKFC,
			int DecryptFlags,
			IntByReference rethCipherForAttachments,
			int  Reserved,
			Pointer pReserved);

	STATUS OOOStartOperation(
			Pointer pMailOwnerName,
			Pointer pHomeMailServer,
			int bHomeMailServer,
			HANDLE.ByValue hMailFile,
			IntByReference hOOOContext,
			PointerByReference pOOOOContext);

	STATUS OOOEndOperation(int hOOContext, Pointer pOOOContext);

	STATUS OOOInit();
	
	STATUS OOOTerm();

	STATUS OOOEnable(
			Pointer pOOOContext,
			int bState);
	
	STATUS OOOGetAwayPeriod(
			Pointer pOOOContext,
			NotesTimeDateStruct tdStartAway,
			NotesTimeDateStruct tdEndAway);
	
	STATUS OOOGetExcludeInternet(
			Pointer pOOOContext,
			IntByReference bExcludeInternet);
	
	STATUS OOOGetGeneralMessage(
			Pointer pOOOContext,
			Memory pGeneralMessage,
			WORD.ByReference pGeneralMessageLen);
	
	STATUS OOOGetGeneralSubject(
			Pointer pOOOContext,
			Memory pGeneralSubject);
	
	STATUS OOOGetState(
			Pointer pOOOContext,
			WORD.ByReference retVersion,
			WORD.ByReference retState);

	STATUS OOOSetAwayPeriod(
			Pointer pOOOContext,
			NotesTimeDateStruct.ByValue tdStartAway,
			NotesTimeDateStruct.ByValue tdEndAway);
	
	STATUS OOOSetExcludeInternet(
			Pointer pOOOContext,
			int bExcludeInternet);

	STATUS OOOSetGeneralMessage(
			Pointer pOOOContext,
			Memory pGeneralMessage,
			WORD wGeneralMessageLen);
	
	STATUS OOOSetGeneralSubject(
			Pointer pOOOContext,
			Memory pGeneralSubject,
			int bDisplayReturnDate);

	@UndocumentedAPI
	STATUS CESCreateCTXFromNote(DHANDLE.ByValue hNote, IntByReference rethCESCTX);
	@UndocumentedAPI
	STATUS CESGetNoSigCTX(IntByReference rethCESCTX);
	@UndocumentedAPI
	void CESFreeCTX(int hCESCTX);
    @UndocumentedAPI
    STATUS ECLUserTrustSigner(int hCESCtx,
        WORD ECLType,
        WORD bSessionOnly,
        WORD wCapabilities,
        WORD wCapabilities2,
        WORD.ByReference retwCurrentCapabilities,
        WORD.ByReference retwCurrentCapabilities2);

	STATUS NSFDbGetUnreadNoteTable2(
			HANDLE.ByValue hDB,
			Memory userName,
			WORD userNameLength,
			boolean fCreateIfNotAvailable,
			boolean fUpdateUnread,
			DHANDLE.ByReference rethUnreadList);

	STATUS NSFDbSetUnreadNoteTable(
			HANDLE.ByValue hDB,
			Memory UserName,
			WORD UserNameLength,
			boolean fFlushToDisk,
			DHANDLE.ByValue hOriginalUnreadList,
			DHANDLE.ByValue hUnreadUnreadList);
	
	STATUS NSFDbUpdateUnread(
			HANDLE.ByValue hDataDB,
			DHANDLE.ByValue hUnreadList);
	
	STATUS NSFNoteComputeWithForm(
			DHANDLE.ByValue hNote,
			DHANDLE.ByValue hFormNote,
			int dwFlags,
			NotesCallbacks.b64_CWFErrorProc ErrorRoutine,
			Pointer CallersContext);

	STATUS DesignRefresh(
			Memory Server,
			HANDLE.ByValue hDB,
			int dwFlags,
			NotesCallbacks.ABORTCHECKPROC AbortCheck,
			NotesCallbacks.OSSIGMSGPROC MessageProc);

    STATUS SECVerifyPassword(
        WORD wPasswordLen,
        Pointer Password,
        WORD wDigestLen,
        Pointer Digest,
        int ReservedFlags,
        Pointer pReserved);

	STATUS CompoundTextAssimilateFile(
			DHANDLE.ByValue hCompound,
			Memory pszFileName,
			int dwFlags);

	STATUS NSFNoteHardDelete(HANDLE.ByValue hDB, int NoteID, int Reserved);

	STATUS NAMELookup2(
			Memory ServerName,
			int Flags,
			WORD NumNameSpaces,
			Memory NameSpaces,
			WORD NumNames,
			Memory Names,
			WORD NumItems,
			Memory Items,
			DHANDLE.ByReference rethBuffer
	);
	Pointer NAMELocateNextName2(
			Pointer pLookup,
			Pointer pName,
			LongByReference retNumMatches
	);
	Pointer NAMELocateNextMatch2(
			Pointer pLookup,
			Pointer pName,
			Pointer pMatch
	);
	Pointer NAMELocateItem2(
			Pointer pMatch,
			WORD Item,
			WORD.ByReference retDataType,
			WORD.ByReference retSize
	);
	STATUS NAMEGetAddressBooks(
			Memory pszServer,
			WORD wOptions,
			WORD.ByReference pwReturnCount,
			WORD.ByReference pwReturnLength,
			DHANDLE.ByReference phReturn
	);
	
    @UndocumentedAPI
    STATUS ECLGetListCapabilities(Pointer pNamesList, WORD ECLType,
        WORD.ByReference retwCapabilities,
        WORD.ByReference retwCapabilities2, IntByReference retfUserCanModifyECL);

    STATUS NSFDbModifiedTime(
			HANDLE.ByValue hDB,
			NotesTimeDateStruct.ByReference retDataModified,
			NotesTimeDateStruct.ByReference retNonDataModified);
	
    STATUS NSFDbClearReplHistory(HANDLE.ByValue hDb, int dwFlags);

    STATUS NSFDbGetNotes(
			HANDLE.ByValue hDB,
			int NumNotes,
			Memory NoteID, //NOTEID array
			Memory NoteOpenFlags, // DWORD array
			Memory SinceSeqNum, // DWORD array
			int ControlFlags,
			HANDLE.ByValue hObjectDB,
			Pointer CallbackParam,
			NotesCallbacks.NSFGetNotesCallback GetNotesCallback,
			NotesCallbacks.b64_NSFNoteOpenCallback NoteOpenCallback,
			NotesCallbacks.b64_NSFObjectAllocCallback ObjectAllocCallback,
			NotesCallbacks.b64_NSFObjectWriteCallback ObjectWriteCallback,
			NotesTimeDateStruct FolderSinceTime,
			NotesCallbacks.NSFFolderAddCallback FolderAddCallback);

    STATUS NSFDbGetReplHistorySummary(
			HANDLE.ByValue hDb,
			int Flags,
			DHANDLE.ByReference rethSummary,
			IntByReference retNumEntries);

    STATUS NSFNoteLSCompileExt(
			HANDLE.ByValue hDb,
			DHANDLE.ByValue hNote,
			int dwFlags,
			NotesCallbacks.LSCOMPILEERRPROC pfnErrProc,
			Pointer pCtx
			);
	
	@UndocumentedAPI
	STATUS NSFDbCreateExtended4 (Memory pathName, WORD dbClass, 
			 boolean forceCreation, WORD options, int options2,
			 byte encryptStrength, int MaxFileSize,
			 Memory string1, Memory string2, 
			 WORD ReservedListLength, WORD ReservedListCount, 
			 DbOptionsStruct.ByValue dbOptions, DHANDLE.ByValue hNamesList, DHANDLE.ByValue hReservedList);

	// *******************************************************************************
	// * calendaring
	// *******************************************************************************
	
	STATUS CalGetApptunidFromUID(
			Memory pszUID,
			Memory pszApptunid,
			int dwFlags,
			Pointer pCtx);
	
	STATUS CalGetUIDfromNOTEID(
			HANDLE.ByValue hDB,
			int noteid,
			Memory pszUID,
			WORD wLen,
			Pointer pReserved,
			int dwFlags,
			Pointer pCtx);
	
	STATUS CalGetUIDfromUNID(
			HANDLE.ByValue hDB,
			NotesUniversalNoteIdStruct.ByValue unid,
			Memory pszUID,
			WORD wLen,
			Pointer pReserved,
			int dwFlags,
			Pointer pCtx);

	STATUS CalReadRange(
			HANDLE.ByValue hDB,
			NotesTimeDateStruct.ByValue tdStart,
			NotesTimeDateStruct.ByValue  tdEnd,
			int dwViewSkipCount,
			int dwMaxReturnCount,
			int dwReturnMask,
			int dwReturnMaskExt,
			Pointer pFilterInfo,
			DHANDLE.ByReference hRetCalData,
			WORD.ByReference retCalBufferLength,
			DHANDLE.ByReference hRetUIDData,
			IntByReference retNumEntriesProcessed,
			WORD.ByReference retSignalFlags,
			int dwFlags,
			Pointer pCtx);
	
	STATUS CalGetRecurrenceID(
			NotesTimeDateStruct.ByValue tdInput,
			Memory pszRecurID,
			WORD wLenRecurId);
	
	STATUS CalCreateEntry(
			HANDLE.ByValue hDB,
			Memory pszCalEntry,
			int dwFlags,
			DHANDLE.ByReference hRetUID,
			Pointer pCtx);
	
	STATUS CalGetUnappliedNotices(
			HANDLE.ByValue hDB,
			Memory pszUID,
			WORD.ByReference pwNumNotices,
			DHANDLE.ByReference phRetNOTEIDs,
			DHANDLE.ByReference phRetUNIDs,
			Pointer pReserved,
			int dwFlags,
			Pointer pCtx);
	
	STATUS CalGetNewInvitations(
			HANDLE.ByValue hDB,
			NotesTimeDateStruct ptdStart,
			Memory pszUID,
			NotesTimeDateStruct ptdSince,
			NotesTimeDateStruct ptdretUntil,
			WORD.ByReference pwNumInvites,
			DHANDLE.ByReference phRetNOTEIDs,
			DHANDLE.ByReference phRetUNIDs,
			Pointer pReserved,
			int dwFlags,
			Pointer pCtx);
	
	STATUS CalReadNotice(
			HANDLE.ByValue hDB,
			int noteID,
			DHANDLE.ByReference hRetCalData,
			Pointer pReserved,
			int dwFlags,
			Pointer pCtx);
	
	STATUS CalReadNoticeUNID(
			HANDLE.ByValue hDB,
			NotesUniversalNoteIdStruct.ByValue unid,
			DHANDLE.ByReference hRetCalData,
			Pointer pReserved,
			int dwFlags,
			Pointer pCtx);
	
	STATUS CalEntryAction(
			HANDLE.ByValue hDB,
			Memory pszUID,
			Memory pszRecurID,
			int dwAction,
			int dwRange,
			Memory pszComments,
			NotesCalendarActionDataStruct pExtActionInfo,
			int dwFlags,
			Pointer pCtx);
	
	STATUS CalNoticeAction(
			HANDLE.ByValue hDB,
			int noteID,
			int dwAction,
			Memory pszComments,
			NotesCalendarActionDataStruct pExtActionInfo,
			int dwFlags,
			Pointer pCtx);
	
	STATUS CalNoticeActionUNID(
			HANDLE.ByValue hDB,
			NotesUniversalNoteIdStruct.ByValue unid,
			int dwAction,
			Memory pszComments,
			NotesCalendarActionDataStruct pExtActionInfo,
			int dwFlags,
			Pointer pCtx);
	
	STATUS CalReadEntry(
			HANDLE.ByValue hDB,
			Memory pszUID,
			Memory pszRecurID,
			DHANDLE.ByReference hRetCalData,
			IntByReference pdwReserved,
			int dwFlags,
			Pointer pCtx);
	
	STATUS CalOpenNoteHandle(
			HANDLE.ByValue hDB,
			Memory pszUID,
			Memory pszRecurID,
			DHANDLE.ByReference rethNote,
			int dwFlags,
			Pointer pCtx);
	
	STATUS CalUpdateEntry(
			HANDLE.ByValue hDB,
			Memory pszCalEntry,
			Memory pszUID,
			Memory pszRecurID,
			Memory pszComments,
			int dwFlags,
			Pointer pCtx);

	STATUS REGCrossCertifyID(
			HANDLE.ByValue  hCertCtx,
			WORD spare1,
			Memory regServer,
			Memory idFileName,
			Memory location,
			Memory comment,
			Memory forwardAddress,
			WORD spare2,
			NotesCallbacks.REGSIGNALPROC  pStatusFunc,
			Memory errorPathName);
	
	STATUS SECKFMGetCertifierCtx(
			Memory pCertFile,
			KFM_PASSWORDStruct.ByReference pKfmPW,
			Memory pLogFile,
			NotesTimeDateStruct.ByReference pExpDate,
			Memory retCertName,
			HANDLE.ByReference rethKfmCertCtx,
			ShortByReference retfIsHierarchical,
			WORD.ByReference retwFileVersion);
	
	void SECKFMCreatePassword(
			Memory pPassword,
			KFM_PASSWORDStruct.ByReference retHashedPassword);
	
	void SECKFMFreeCertifierCtx(
			HANDLE.ByValue hKfmCertCtx);

	@UndocumentedAPI
	STATUS SECKFMMakeSafeCopy(Pointer hKFC, WORD Type, WORD Version, Memory pFileName);
	
	void OSCurrentTIMEDATE(NotesTimeDateStruct retTimeDate);
	
	boolean TimeGMToLocal(NotesTimeStruct time);
	boolean TimeLocalToGM(NotesTimeStruct time);

	STATUS NSFDbItemDefTableExt(
			HANDLE.ByValue hDB,
			HANDLE.ByReference retItemNameTable);

	STATUS NSFItemDefExtLock(
			Pointer pItemDefTable,
			NotesItemDefinitionTableLock itemDefTableLock);

	STATUS NSFItemDefExtEntries(
			NotesItemDefinitionTableLock itemDefTableLock,
			IntByReference numEntries);
	
	STATUS NSFItemDefExtGetEntry(
			NotesItemDefinitionTableLock itemDefTableLock,
			int itemNum,
			WORD.ByReference itemType,
			WORD.ByReference itemLength,
			Pointer itemName);
	
	STATUS NSFItemDefExtUnlock(
			NotesItemDefinitionTableExt itemDefTable,
			NotesItemDefinitionTableLock itemDefTableLock);
	
	STATUS NSFItemDefExtFree(
			NotesItemDefinitionTableExt itemDeftable);

	@UndocumentedAPI
	STATUS AssistantGetLSDataNote (HANDLE.ByValue hDB, int NoteID, NotesUniversalNoteIdStruct.ByReference retUNID);
	
	boolean AddInIdleDelay(int Delay);
	void AddInLogErrorText(Memory string, STATUS AdditionalErrorCode, Memory Arg);
	void AddInLogMessageText(Pointer string, STATUS AdditionalErrorCode);
	boolean AddInSecondsHaveElapsed(int Seconds);
	default boolean AddInMinutesHaveElapsed(int Minutes) {
		return AddInSecondsHaveElapsed(Minutes*60);
	}
	boolean AddInDayHasElapsed();
	boolean AddInShouldTerminate();
	long AddInCreateStatusLine(Memory TaskName);
	void AddInDeleteStatusLine(long hDesc);
	void AddInQueryDefaults(LongByReference rethModule, LongByReference rethDesc);
	void AddInSetDefaults(long hNewModule, long hDesc);
	void AddInSetStatusLine(long hDesc, Memory string);
	void OSPreemptOccasionally();
	
    @UndocumentedAPI
    boolean CmemflagTestMultiple(Pointer s, WORD length, Pointer pattern);

    @UndocumentedAPI
    STATUS QueueCreate(DHANDLE.ByReference rethQueue);
    @UndocumentedAPI
    STATUS QueueDelete(DHANDLE.ByValue hQueue);
    @UndocumentedAPI
    STATUS QueueGet(DHANDLE.ByValue hQueue, DHANDLE.ByReference rethEntry);
    @UndocumentedAPI
    void NSFAsyncNotifyPoll(Pointer actx, IntByReference retMySessions, ShortByReference retFirstError);
    @UndocumentedAPI
    void NSFUpdateAsyncIOStatus(Pointer actx);
    @UndocumentedAPI
    void NSFCancelAsyncIO(Pointer actx);

    @UndocumentedAPI
    STATUS NSFRemoteConsoleAsync(
        Memory serverName, Memory ConsoleCommand, int Flags,
        DHANDLE.ByReference phConsoleText, DHANDLE.ByReference phTasksText,
        DHANDLE.ByReference phUsersText,
        WORD.ByReference pSignals, IntByReference pConsoleBufferID, DHANDLE.ByValue hQueue,
        NotesCallbacks.ASYNCNOTIFYPROC Proc, Pointer param, PointerByReference retactx);

    @UndocumentedAPI
    WORD Cstrlen(Pointer ptr);
	
    @UndocumentedAPI
    STATUS NSFDbLocalSecInfoGetLocal(HANDLE.ByValue hDb, IntByReference state, IntByReference strength);

    @UndocumentedAPI
    STATUS NSFDbLocalSecInfoSet(HANDLE.ByValue hDB, WORD Option, byte EncryptStrength, Memory Username);

	void NSFDbAccessGet(HANDLE.ByValue hDb, WORD.ByReference retAccessLevel, WORD.ByReference retAccessFlag);

	STATUS NSFDbMajorMinorVersionGet (HANDLE.ByValue hDB, WORD.ByReference retMajorVersion, 
			WORD.ByReference retMinorVersion);

	STATUS NSFDbGetMajMinVersion(HANDLE.ByValue hDb, NotesBuildVersionStruct retBuildVersion);

	STATUS NSFDbCompactExtendedExt2(Memory pathname, int options, int options2, DoubleByReference originalSize, DoubleByReference compactedSize);

	@UndocumentedAPI
	void OSLocalizePath(Pointer ptr);
	@UndocumentedAPI
	STATUS OSPathFileType(Pointer ptr, WORD.ByReference retValue);
	@UndocumentedAPI
	Pointer Cstrncat(Pointer to, Pointer from, int tosize);

	short NSFItemAppendTextList(
			DHANDLE.ByValue note_handle,
			Memory item_name,
			Memory entry_text,
			WORD text_len,
			boolean duplicate_flag);
	
	/**
	 * @since 1.31.0
	 */
	int NSFItemGetLong(
        DHANDLE.ByValue note_handle,
        Memory item_name,
        int number_item_default
	);
	
	STATUS NSFDbGetNamesList(HANDLE.ByValue hDB, int Flags, DHANDLE.ByReference rethNamesList);
	
	@UndocumentedAPI
	int OSProcessGroup(int query);
	
	STATUS AgentLSTextFormat(DHANDLE.ByValue hSrc, DHANDLE.ByReference hDest,
	    DHANDLE.ByReference hErrs, int dwFlags, DHANDLE.ByReference phData);

	@UndocumentedAPI
	short NLS_goto_prev_whole_char (
	    PointerByReference ppString, 
	    Pointer pStrStart, 
	    Pointer pInfo);

    STATUS NSFItemModifyValue(DHANDLE.ByValue hNote, NotesBlockIdStruct.ByValue bhItem,
        WORD itemFlags, WORD dataType,
        Pointer value, int valueLength);

	/**
	 * Deletes all occurrences of a statistic.
	 * 
	 * @param Facility Name of facility.  See Symbolic Value, STATPKG_xxx for a list of existing Domino facilities
	 * @param StatName Name of statistic
	 */
	void StatDelete(Memory Facility, Memory StatName);
	
    /**
     * This function will set the value of a numeric-additive statistic (a statistic of type VT_LONG or
     * VT_NUMBER and is additive) to 0.  This function performs the same operation as the Set Stat Domino
     * Server console command.  For more information on this operation, refer to the Administering the
     * Domino System documentation.
     * 
     * @param Facility Null-terminated string containing the name of the facility.
     * @param StatName Null-terminated string containing the name of the statistic.
     * @return Return status from this call:
     * <ul>
     *  <li>NOERROR - Success</li>
     *  <li>ERR_STAT_NOT_SET - Unable to set the specified statistic to 0.</li>
     *  <li>ERR_STAT_NOT_FOUND - Statistic is not in the Lotus Domino Server's statistic table.</li>
     *  <li>ERR_xxx - Errors returned by lower level Notes functions: (memory management, file operations,
     *      network errors, etc.).  There are so many possible causes, that it is best to use the code in
     *      a call to OSLoadString and display/log the error for the user as your default error handling.</li>
     * </ul>
     */
	STATUS StatReset(Memory Facility, Memory StatName);
    
    /**
     * This function updates the value of a statistic for the current server session.  For information
     * regarding the Lotus Domino server statistics see the Administering the Domino System documentation.
     * 
     * @param Facility Name of facility.  See Symbolic Value, STATPKG_xxx for a list of existing Domino
     *                 facilities.  If the facility is not an existing facility monitored by Domino, a
     *                 new facility with the  accompanying statistic will be created for the current
     *                 server session.  
     * @param StatName Name of statistic.  If the facility and statistic combination is not one that
     *                 exists and is monitored by Domino, a new statistic will be created for the current
     *                 server session.
     * @param Flags Flags that modify the behavior of StatUpdate. Specify 0 for default behavior.
     *              Specify ST_UNIQUE if only one instance of statistic should be allowed.
     *              Specify ~ST_UNIQUE if you want to append another instance of this statistic.
     *              Specify ST_ADDITIVE if the value type of the statistic is VT_LONG or VT_NUMBER and
     *              you want the new value added to the current value of the statistic.
     * @param ValueType Value type of the statistic:  VT_LONG, VT_TEXT, VT_TIMEDATE, or VT_NUMBER.
     * @param Value Value of the statistic.
     * @return Return status from this call -- indicates either success (NOERROR) or what the error is.
     */
	STATUS StatUpdate(Memory Facility, Memory StatName, WORD Flags, WORD ValueType, Pointer Value);
}