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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.Mem.LockedMemory;
import com.hcl.domino.jna.internal.callbacks.NotesCallbacks;
import com.hcl.domino.jna.internal.callbacks.NotesCallbacks.ACLENTRYENUMFUNC;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.HANDLE;
import com.hcl.domino.jna.internal.structs.CreateDAConfigStruct;
import com.hcl.domino.jna.internal.structs.UpdateDAConfigStruct;
import com.hcl.domino.jna.internal.structs.EnableDisableDAStruct;
import com.hcl.domino.jna.internal.structs.DbOptionsStruct;
import com.hcl.domino.jna.internal.structs.VerifyLDAPConnectionStruct;
import com.hcl.domino.jna.internal.structs.HtmlApi_UrlComponentStruct;
import com.hcl.domino.jna.internal.structs.IntlFormatStruct;
import com.hcl.domino.jna.internal.structs.KFM_PASSWORDStruct;
import com.hcl.domino.jna.internal.structs.NIFFindByKeyContextStruct;
import com.hcl.domino.jna.internal.structs.NotesAdminReqParams;
import com.hcl.domino.jna.internal.structs.NotesBlockIdStruct;
import com.hcl.domino.jna.internal.structs.NotesBuildVersionStruct;
import com.hcl.domino.jna.internal.structs.NotesCalendarActionDataStruct;
import com.hcl.domino.jna.internal.structs.NotesCollectionPositionStruct;
import com.hcl.domino.jna.internal.structs.NotesCompoundStyleStruct;
import com.hcl.domino.jna.internal.structs.NotesDbReplicaInfo;
import com.hcl.domino.jna.internal.structs.NotesDbReplicaInfoStruct;
import com.hcl.domino.jna.internal.structs.NotesFTIndexStatsStruct;
import com.hcl.domino.jna.internal.structs.NotesItemDefinitionTableExt;
import com.hcl.domino.jna.internal.structs.NotesItemDefinitionTableLock;
import com.hcl.domino.jna.internal.structs.NotesOriginatorIdStruct;
import com.hcl.domino.jna.internal.structs.NotesStringDescStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDatePairStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.hcl.domino.jna.internal.structs.NotesUniversalNoteIdStruct;
import com.hcl.domino.jna.internal.structs.ReplExtensionsStruct;
import com.hcl.domino.jna.internal.structs.ReplServStatsStruct;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

public interface INotesCAPI extends Library {
	@Target(value = ElementType.METHOD)
	@Retention(value = RetentionPolicy.RUNTIME)
	@interface NativeFunctionName {
	    String name() default "";
	}

	short NotesInit();
	short NotesInitExtended(int argc, Memory argvPtr);
	void NotesTerm();

	short NotesInitThread();
	void NotesTermThread();

	short OSTranslate(short translateMode, Memory in, short inLength, Memory out, short outLength);
	short OSTranslate(short translateMode, Pointer in, short inLength, Memory out, short outLength);
	int OSTranslate32(short translateMode, Memory in, int inLength, Memory out, int outLength);
	int OSTranslate32(short translateMode, Pointer in, int inLength, Memory out, int outLength);

	short OSLoadString(int hModule, short StringCode, Memory retBuffer, short BufferLength);
	short OSLoadString(long hModule, short StringCode, Memory retBuffer, short BufferLength);
	short OSPathNetConstruct(Memory PortName,
			Memory ServerName,
			Memory FileName,
			Memory retPathName);
	short OSPathNetParse(Pointer PathName,
			Pointer retPortName,
			Pointer retServerName,
			Pointer retFileName);

	short DNCanonicalize(int Flags, Memory TemplateName, Memory InName, Memory OutName, short OutSize, ShortByReference OutLength);
	short DNAbbreviate(int Flags, Memory TemplateName, Memory InName, Memory OutName, short OutSize, ShortByReference OutLength);	

	short NSFBuildNamesList(Memory UserName, int dwFlags, DHANDLE.ByReference rethNamesList);
	
	short NSFGetChangedDBs(
			Memory ServerName,
			NotesTimeDateStruct SinceTime,
			LongByReference ChangesSize,
			DHANDLE.ByReference hChanges,
			NotesTimeDateStruct NextSinceTime
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
	@Deprecated short OSMemFree(DHANDLE.ByValue handle);

	/**
	 * @param handle the handle for which to get the size
	 * @param retSize the size return value
	 * @return the result status
	 * @deprecated use {@link Mem#OSMemGetSize(DHANDLE.ByValue, IntByReference)} instead
	 */
	@Deprecated short OSMemGetSize(DHANDLE.ByValue handle, IntByReference retSize);

	/**
	 * @param BlkType the type of memory block to allocate
	 * @param dwSize the count of blocks to allocate
	 * @param retHandle the return value handle
	 * @return the result status
	 * @deprecated use {@link Mem#OSMemAlloc(short, int, com.hcl.domino.jna.internal.gc.handles.DHANDLE.ByReference)} instead
	 */
	@Deprecated short OSMemAlloc(
			short  BlkType,
			int  dwSize,
			DHANDLE.ByReference retHandle);
	
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
	@Deprecated short OSMemRealloc(
			DHANDLE.ByValue handle,
			int newSize);
	
	short CreateNamesListFromSingleName(Memory pszServerName, short fDontLookupAlternateNames,
			Pointer pLookupFlags, Memory pTarget, DHANDLE.ByReference rethNames);
	short CreateNamesListFromNamesExtend(Memory pszServerName, short cTargets, Pointer ptrArrTargets, DHANDLE.ByReference rethNames);
	short CreateNamesListFromGroupNameExtend(Memory pszServerName, Memory pTarget, DHANDLE.ByReference rethNames);

	short ListAllocate(
			short ListEntries,
			short TextSize,
			int fPrefixDataType,
			DHANDLE.ByReference rethList,
			Memory retpList,
			ShortByReference retListSize);

	short ListRemoveAllEntries(
			DHANDLE.ByValue hList,
			int fPrefixDataType,
			ShortByReference pListSize);

	short ListAddEntry(
			DHANDLE.ByValue hList,
			int fPrefixDataType,
			ShortByReference pListSize,
			short EntryNumber,
			Memory Text,
			short TextSize);

	short ListDuplicateDissimilar(Pointer pInList,
			boolean fNoteItemIn,
			boolean fNoteItemOut,
			DHANDLE.ByReference	phOutList);
	
	short SECKFMGetUserName(Memory retUserName);

	short NSFDbOpen(Memory dbName, HANDLE.ByReference dbHandle);
	short NSFDbClose(HANDLE.ByValue dbHandle);
	short NSFDbOpenExtended (Memory PathName, short Options, DHANDLE.ByValue hNames, NotesTimeDateStruct ModifiedTime,
			HANDLE.ByReference rethDB, NotesTimeDateStruct retDataModified, NotesTimeDateStruct retNonDataModified);
	short NSFDbInfoGet(
			HANDLE.ByValue hDB,
			Pointer retBuffer);
	void NSFDbInfoParse(
			Pointer Info,
			short What,
			Pointer Buffer,
			short Length);
	short NSFDbInfoSet(
			HANDLE.ByValue hDB,
			Pointer Buffer);
	void NSFDbInfoModify(
			Pointer Info,
			short What,
			Pointer Buffer);
	short NSFDbCreate(
			Memory pathName,
			short DbClass,
			boolean ForceCreation
			);
	short NSFDbCreateExtended(
			Memory pathName,
			short DbClass,
			boolean forceCreation,
			short options,
			byte encryptStrength,
			long maxFileSize);
	
	short NSFDbDelete(
			Memory PathName
			);
	
	short NSFItemSetText(
			DHANDLE.ByValue hNote,
			Memory ItemName,
			Memory ItemText,
			short TextLength);

	void TimeConstant(short timeConstantType, NotesTimeDateStruct tdptr);

	short ConvertTIMEDATEToText(
			IntlFormatStruct intlFormat,
			Pointer textFormat,
			NotesTimeDateStruct inputTime,
			Memory retTextBuffer,
			short textBufferLength,
			ShortByReference retTextLength);
	
	short ConvertTextToTIMEDATE(
			IntlFormatStruct intlFormat,
			Pointer textFormat,
			Memory text,
			short maxLength,
			NotesTimeDateStruct retTIMEDATE);

	void OSGetIntlSettings(
			IntlFormatStruct retIntlFormat,
			short bufferSize);

	short NSFDbGetSpecialNoteID(
			HANDLE.ByValue hDB,
			short Index,
			IntByReference retNoteID);
	short NSFDbPathGet(
			HANDLE.ByValue hDB,
			Memory retCanonicalPathName,
			Memory retExpandedPathName);
	short NSFDbReplicaInfoGet(
			HANDLE.ByValue hDB,
			NotesDbReplicaInfoStruct retReplicationInfo);
	short NSFDbReplicaInfoSet(
			HANDLE.ByValue hDB,
			NotesDbReplicaInfoStruct ReplicationInfo);
	
	short NSFNoteDeleteExtended(HANDLE.ByValue hDB, int noteID, int updateFlags);
	short NSFDbDeleteNotes(HANDLE.ByValue hDB, DHANDLE.ByValue hTable, Memory retUNIDArray);
	short ODSLength(short type);
	short ODSReadMemory(PointerByReference ppSrc, short type, Pointer pDest, short iterations);
	short IDCreateTable (int alignment, DHANDLE.ByReference rethTable);
	short IDDestroyTable(DHANDLE.ByValue hTable);
	int IDEntries (DHANDLE.ByValue hTable);
	boolean IDIsPresent (DHANDLE.ByValue hTable, int id);
	short IDInsert (DHANDLE.ByValue hTable, int id, IntByReference retfInserted);
	short IDDelete (DHANDLE.ByValue hTable, int id, IntByReference retfDeleted);
	short IDDeleteAll (DHANDLE.ByValue hTable);
	short IDTableIntersect(DHANDLE.ByValue hSrc1Table, DHANDLE.ByValue hSrc2Table, DHANDLE.ByReference rethDstTable);
	short IDTableCopy (DHANDLE.ByValue hTable, DHANDLE.ByReference rethTable);
	boolean IDScan (DHANDLE.ByValue hTable, boolean fFirst, IntByReference retID);
	boolean IDScanBack (DHANDLE.ByValue hTable, boolean fLast, IntByReference retID);
	short IDInsertTable (DHANDLE.ByValue hTable, DHANDLE.ByValue hIDsToAdd);
	short IDInsertRange(DHANDLE.ByValue hTable, int IDFrom, int IDTo, boolean AddToEnd);
	short IDEnumerate(DHANDLE.ByValue hTable, NotesCallbacks.IdEnumerateProc Routine, Pointer Parameter);
	short IDTableFlags (Pointer pIDTable);
	void IDTableSetFlags (Pointer pIDTable, short Flags);
	void IDTableSetTime(Pointer pIDTable, NotesTimeDateStruct Time);
	NotesTimeDateStruct IDTableTime(Pointer pIDTable);
	short IDDeleteTable(DHANDLE.ByValue hTable, DHANDLE.ByValue hIDsToDelete);
	
	short NSFNoteOpen(HANDLE.ByValue db_handle, int note_id, short open_flags, DHANDLE.ByReference  note_handle);
	short NSFNoteOpenExt(HANDLE.ByValue hDB, int noteId, int flags, DHANDLE.ByReference rethNote);
	short NSFNoteOpenSoftDelete(HANDLE.ByValue hDB, int NoteID, int Reserved, DHANDLE.ByReference rethNote);

	short NSFNoteClose(DHANDLE.ByValue hNote);
	short NSFNoteOpenByUNIDExtended(HANDLE.ByValue hDB, NotesUniversalNoteIdStruct pUNID, int flags, DHANDLE.ByReference rtn); 
	short NSFNoteCreate(HANDLE.ByValue db_handle, DHANDLE.ByReference note_handle);
	void NSFNoteGetInfo(DHANDLE.ByValue hNote, short type, Pointer retValue);
	void NSFNoteSetInfo(DHANDLE.ByValue hNote, short type, Pointer value);
	short NSFNoteUpdateExtended(DHANDLE.ByValue hNote, int updateFlags);
	short NSFNoteDeleteExtended(DHANDLE.ByValue hDB, int NoteID, int UpdateFlags);
	short NSFNoteCopy(
			DHANDLE.ByValue note_handle_src,
			DHANDLE.ByReference note_handle_dst_ptr);
	short NSFDbGenerateOID(HANDLE.ByValue hDB, NotesOriginatorIdStruct retOID);

	void OSGetExecutableDirectory(Memory retPathName);
	void OSGetDataDirectory(Memory retPathName);
	short CreateDAConfig(CreateDAConfigStruct ldap);
	short UpdateDAConfig(UpdateDAConfigStruct ldap);
	short EnableDisableDA(EnableDisableDAStruct daConfig);
	short VerifyLDAPConnection(VerifyLDAPConnectionStruct ldap);
	short OSGetSystemTempDirectory(Memory retPathName, int bufferLength);
	void OSPathAddTrailingPathSep(Memory retPathName);
	short OSGetEnvironmentString(Memory variableName, Memory rethValueBuffer, short bufferLength);
	long OSGetEnvironmentLong(Memory variableName);
	short OSGetEnvironmentTIMEDATE(Memory envVariable, NotesTimeDateStruct retTd); 
	void OSSetEnvironmentVariable(Memory variableName, Memory Value);
	void OSSetEnvironmentVariableExt (Memory variableName, Memory Value, short isSoft);
	void OSSetEnvironmentInt(Memory variableName, int Value);
	void OSSetEnvironmentTIMEDATE(Memory envVariable, NotesTimeDateStruct td);
	short OSGetEnvironmentSeqNo();

	short OSRunNSDExt (Memory szServerName, short flags);

	void NIFGetViewRebuildDir(Memory retPathName, int BufferLength);

	short NSFFormulaCompile(
			Memory formulaName,
			short formulaNameLength,
			Memory formulaText,
			short  formulaTextLength,
			HANDLE.ByReference rethFormula,
			ShortByReference retFormulaLength,
			ShortByReference retCompileError,
			ShortByReference retCompileErrorLine,
			ShortByReference retCompileErrorColumn,
			ShortByReference retCompileErrorOffset,
			ShortByReference retCompileErrorLength);
	short NSFFormulaDecompile(
			Pointer pFormulaBuffer,
			boolean fSelectionFormula,
			DHANDLE.ByReference rethFormulaText,
			ShortByReference retFormulaTextLength);
	short NSFFormulaSummaryItem(HANDLE.ByValue hFormula, Memory ItemName, short ItemNameLength);
	short NSFFormulaMerge(
			HANDLE.ByValue hSrcFormula,
			HANDLE.ByValue hDestFormula);
	short NSFComputeStart(
			short Flags,
			Pointer lpCompiledFormula,
			DHANDLE.ByReference rethCompute);
	short NSFComputeStop(DHANDLE.ByValue hCompute);
	short NSFComputeEvaluate(
			DHANDLE.ByValue hCompute,
			DHANDLE.ByValue hNote,
			DHANDLE.ByReference rethResult,
			ShortByReference retResultLength,
			IntByReference retNoteMatchesFormula,
			IntByReference retNoteShouldBeDeleted,
			IntByReference retNoteModified);

	void DEBUGDumpHandleTable(int flags, short blkType);

	short ListGetNumEntries(Pointer vList, int noteItem);

	short ListGetText (Pointer pList,
			boolean fPrefixDataType,
			short entryNumber,
			Memory retTextPointer,
			ShortByReference retTextLength);

	short ListGetSize(
			Pointer pList,
			int fPrefixDataType);

	void NSFItemQueryEx(
			DHANDLE.ByValue note_handle,
			NotesBlockIdStruct.ByValue item_bid,
			Memory item_name,
			short  return_buf_len,
			ShortByReference name_len_ptr,
			ShortByReference item_flags_ptr,
			ShortByReference value_datatype_ptr,
			NotesBlockIdStruct value_bid_ptr,
			IntByReference value_len_ptr,
			ByteByReference retSeqByte,
			ByteByReference retDupItemID);
	
	short NSFItemGetTextListEntry(
			DHANDLE.ByValue note_handle,
			Memory item_name,
			short entry_position,
			Memory entry_text,
			short text_len);

	short NSFNoteHasReadersField(DHANDLE.ByValue note_handle, NotesBlockIdStruct bhFirstReadersItem);


	short NSFItemCopy(DHANDLE.ByValue note_handle, NotesBlockIdStruct.ByValue item_blockid);
	short NSFItemCopyAndRename (DHANDLE.ByValue hNote, NotesBlockIdStruct.ByValue bhItem, Memory pszNewItemName);

	short NSFNoteDetachFile(DHANDLE.ByValue note_handle, NotesBlockIdStruct.ByValue item_blockid);

	short NSFDbReadObject(
			HANDLE.ByValue hDB,
			int objectID,
			int offset,
			int length,
			DHANDLE.ByReference rethBuffer);

	short NSFDbAllocObject(
			HANDLE.ByValue hDB,
			int dwSize,
			short Class,
			short Privileges,
			IntByReference retObjectID);
	
	short NSFDbAllocObjectExtended2(HANDLE.ByValue cDB,
			int size, short noteClass, short privs, short type, IntByReference rtnRRV);

	short NSFDbWriteObject(
			HANDLE.ByValue hDB,
			int objectID,
			DHANDLE.ByValue hBuffer,
			int offset,
			int length);
	
	short NSFDbFreeObject(
			HANDLE.ByValue hDB,
			int objectID);
	
	short NSFDbReallocObject(
			HANDLE.ByValue hDB,
			int objectID,
			int newSize);

	short NSFItemAppendObject(
			DHANDLE.ByValue hNote,
			short ItemFlags,
			Memory Name,
			short NameLength,
			NotesBlockIdStruct.ByValue bhValue,
			int ValueLength,
			int fDealloc);
	
	short NSFDbGetObjectSize(
			HANDLE.ByValue hDB,
			int objectID,
			short objectType,
			IntByReference retSize,
			ShortByReference retClass,
			ShortByReference retPrivileges);

	short NSFNoteCipherExtractWithCallback (DHANDLE.ByValue hNote, NotesBlockIdStruct.ByValue bhItem,
			int extractFlags, int hDecryptionCipher,
			NotesCallbacks.NoteExtractCallback pNoteExtractCallback, Pointer pParam,
			int Reserved, Pointer pReserved);

	short NSFItemAppendByBLOCKID(
			DHANDLE.ByValue note_handle,
			short item_flags,
			Memory item_name,
			short name_len,
			NotesBlockIdStruct.ByValue value_bid,
			int value_len,
			NotesBlockIdStruct item_bid_ptr);

	short NSFItemInfo(
			DHANDLE.ByValue note_handle,
			Memory item_name,
			short  name_len,
			NotesBlockIdStruct retbhItem,
			ShortByReference retDataType,
			NotesBlockIdStruct retbhValue,
			IntByReference retValueLength);

	short NSFItemInfoNext(
			DHANDLE.ByValue note_handle,
			NotesBlockIdStruct.ByValue NextItem,
			Memory item_name,
			short  name_len,
			NotesBlockIdStruct retbhItem,
			ShortByReference retDataType,
			NotesBlockIdStruct retbhValue,
			IntByReference retValueLength);

	short NSFItemInfoPrev(
			DHANDLE.ByValue note_handle,
			NotesBlockIdStruct.ByValue  CurrItem,
			Memory item_name,
			short  name_len,
			NotesBlockIdStruct item_blockid_ptr,
			ShortByReference value_type_ptr,
			NotesBlockIdStruct value_blockid_ptr,
			IntByReference value_len_ptr);

	short NSFItemDelete(
			DHANDLE.ByValue note_handle,
			Memory item_name,
			short name_len);
	
	short NSFItemConvertToText(
			DHANDLE.ByValue note_handle,
			Memory item_name_ptr,
			Memory text_buf_ptr,
			short text_buf_len,
			char separator);
	
	short NSFItemConvertValueToText(
			short value_type,
			NotesBlockIdStruct.ByValue value_bid,
			int value_len,
			Memory text_buf_ptr,
			short text_buf_len,
			char separator);

	boolean NSFNoteIsSignedOrSealed(DHANDLE.ByValue note_handle, ByteByReference signed_flag_ptr, ByteByReference sealed_flag_ptr);

	short NSFNoteExpand(DHANDLE.ByValue hNote);
	short NSFNoteVerifySignature(
			DHANDLE.ByValue hNote,
			Memory SignatureItemName,
			NotesTimeDateStruct retWhenSigned,
			Memory retSigner,
			Memory retCertifier);
	short NSFNoteContract(DHANDLE.ByValue hNote);

	short NIFFindDesignNoteExt(HANDLE.ByValue hFile, Memory name, short noteClass, Memory pszFlagsPattern, IntByReference retNoteID, int options);
	
	short NIFOpenCollection(HANDLE.ByValue hViewDB, HANDLE.ByValue hDataDB, int viewNoteID,
			short openFlags, HANDLE.ByValue hUnreadList,
			DHANDLE.ByReference rethCollection, DHANDLE.ByReference rethViewNote, Memory retViewUNID,
			HANDLE.ByReference rethCollapsedList, HANDLE.ByReference rethSelectedList);
	
	short NIFOpenCollectionWithUserNameList (HANDLE.ByValue hViewDB, HANDLE.ByValue hDataDB,
			int viewNoteID, short openFlags,
			DHANDLE.ByValue hUnreadList,
			DHANDLE.ByReference rethCollection,
			DHANDLE.ByReference rethViewNote, Memory retViewUNID,
			DHANDLE.ByReference rethCollapsedList,
			DHANDLE.ByReference rethSelectedList,
			DHANDLE.ByValue nameList);

	void NIFGetLastModifiedTime(DHANDLE.ByValue hCollection, NotesTimeDateStruct retLastModifiedTime);
	void NIFGetLastAccessedTime(DHANDLE.ByValue hCollection, NotesTimeDateStruct retLastModifiedTime);
	void NIFGetNextDiscardTime(DHANDLE.ByValue hCollection, NotesTimeDateStruct retLastModifiedTime);

	short NIFGetCollation(DHANDLE.ByValue hCollection, ShortByReference retCollationNum);
	short NIFSetCollation(DHANDLE.ByValue hCollection, short CollationNum);
	short NIFUpdateCollection(DHANDLE.ByValue hCollection);

	short NIFGetCollectionDocCountLW(DHANDLE.ByValue hCol, IntByReference pDocct);

	short NIFFindByKeyExtended2 (DHANDLE.ByValue hCollection, Memory keyBuffer,
			int findFlags,
			int returnFlags,
			NotesCollectionPositionStruct retIndexPos,
			IntByReference retNumMatches,
			ShortByReference retSignalFlags,
			DHANDLE.ByReference rethBuffer,
			IntByReference retSequence);

	short NIFFindByKeyExtended3 (DHANDLE.ByValue hCollection,
			Memory keyBuffer, int findFlags,
			int returnFlags,
			NotesCollectionPositionStruct retIndexPos,
			IntByReference retNumMatches, ShortByReference retSignalFlags,
			DHANDLE.ByReference rethBuffer, IntByReference retSequence,
			NotesCallbacks.NIFFindByKeyProc NIFFindByKeyCallback, NIFFindByKeyContextStruct Ctx);
	
	short NIFFindByKey(DHANDLE.ByValue hCollection, Memory keyBuffer, short findFlags,
			NotesCollectionPositionStruct retIndexPos, IntByReference retNumMatches);
	short NIFFindByName(DHANDLE.ByValue hCollection, Memory name, short findFlags,
			NotesCollectionPositionStruct retIndexPos, IntByReference retNumMatches);

	short NSFDbModeGet(
			HANDLE.ByValue hDB,
			ShortByReference retMode);

	short NIFReadEntries(DHANDLE.ByValue hCollection, NotesCollectionPositionStruct indexPos, short skipNavigator,
			int skipCount, short returnNavigator, int returnCount, int returnMask, DHANDLE.ByReference rethBuffer,
			ShortByReference retBufferLength, IntByReference retNumEntriesSkipped, IntByReference retNumEntriesReturned,
			ShortByReference retSignalFlags);

	short NIFReadEntriesExt(DHANDLE.ByValue hCollection,
			NotesCollectionPositionStruct collectionPos,
            short skipNavigator, int skipCount,
            short returnNavigator, int returnCount, int returnMask,
            NotesTimeDateStruct diffTime, DHANDLE.ByValue diffIDTable, int columnNumber, int flags,
            DHANDLE.ByReference rethBuffer, ShortByReference retBufferLength,
            IntByReference retNumEntriesSkipped, IntByReference retNumEntriesReturned,
            ShortByReference retSignalFlags, NotesTimeDateStruct retDiffTime,
            NotesTimeDateStruct retModifiedTime, IntByReference retSequence);

	short NSFSearchExtended3 (HANDLE.ByValue hDB, 
			HANDLE.ByValue hFormula, 
			DHANDLE.ByValue hFilter, 
			int filterFlags, 
			Memory ViewTitle, 
			int SearchFlags, 
			int SearchFlags1, 
			int SearchFlags2, 
			int SearchFlags3, 
			int SearchFlags4, 
			short NoteClassMask, 
			NotesTimeDateStruct Since, 
			NotesCallbacks.NsfSearchProc EnumRoutine,
			Pointer EnumRoutineParameter, 
			NotesTimeDateStruct retUntil, 
			DHANDLE.ByValue namelist);

	short NIFGetIDTableExtended(DHANDLE.ByValue hCollection, short navigator, short Flags, DHANDLE.ByValue hIDTable);

	short NSFDbIsRemote(HANDLE.ByValue hDb);

	short NSFDbGetBuildVersion(HANDLE.ByValue hDB, ShortByReference retVersion);

	short NSFDbLocateByReplicaID(
			HANDLE.ByValue hDB,
			NotesTimeDateStruct ReplicaID,
			Memory retPathName,
			short PathMaxLen);
	
	short NSFDbReadACL(
			HANDLE.ByValue hDB,
			DHANDLE.ByReference rethACL);
	
	short NSFDbStoreACL(
			HANDLE.ByValue hDB,
			DHANDLE.ByValue hACL,
			int ObjectID,
			short Method);
	
	short ACLLookupAccess(
			DHANDLE.ByValue hACL,
			Pointer pNamesList,
			ShortByReference retAccessLevel,
			Memory retPrivileges,
			ShortByReference retAccessFlags,
			DHANDLE.ByReference rethPrivNames);
	
	short ACLSetAdminServer(
			DHANDLE.ByValue hACL,
			Memory ServerName);
	
	short ACLGetAdminServer(
			DHANDLE.ByValue hACL,
			Memory retServerName);
	
	short ACLGetPrivName(
			DHANDLE.ByValue hACL,
			short PrivNum,
			Memory retPrivName);
	
	short ACLSetPrivName(
			DHANDLE.ByValue hACL,
			short PrivNum,
			Memory privName);
	
	short ACLAddEntry(
			DHANDLE.ByValue hACL,
			Memory name,
			short AccessLevel,
			Memory privileges,
			short AccessFlags);
	
	short ACLDeleteEntry(
			DHANDLE.ByValue hACL,
			Memory name);
	
	short ACLUpdateEntry(
			DHANDLE.ByValue hACL,
			Memory name,
			short updateFlags,
			Memory newName,
			short newAccessLevel,
			Memory newPrivileges,
			short newAccessFlags);
	
	short ACLEnumEntries(
			DHANDLE.ByValue hACL,
			ACLENTRYENUMFUNC EnumFunc,
			Pointer EnumFuncParam);
	
	short ACLSetFlags(
			DHANDLE.ByValue hACL,
			int Flags);
	
	short ACLGetFlags(
			DHANDLE.ByValue hACL,
			IntByReference retFlags);

	
	// *******************************************************************************
	// * dxl.h
	// *******************************************************************************
	
	short DXLCreateExporter(
			IntByReference prethDXLExport);
	void DXLDeleteExporter(
			int hDXLExport);
	short DXLExportACL(
			int hDXLExport,
			NotesCallbacks.XML_WRITE_FUNCTION pDXLWriteFunc,
			HANDLE.ByValue hDB,
			Pointer pExAction);
	short DXLExportDatabase(
			int hDXLExport,
			NotesCallbacks.XML_WRITE_FUNCTION pDXLWriteFunc,
			HANDLE.ByValue hDB,
			Pointer pExAction);
	short DXLExportIDTable(
			int hDXLExport,
			NotesCallbacks.XML_WRITE_FUNCTION pDXLWriteFunc,
			HANDLE.ByValue hDB,
			DHANDLE.ByValue hIDTable,
			Pointer pExAction);
	short DXLExportNote(
			int hDXLExport,
			NotesCallbacks.XML_WRITE_FUNCTION pDXLWriteFunc,
			DHANDLE.ByValue hNote,
			Pointer pExAction);
	boolean DXLExportWasErrorLogged(
			int hDXLExport);
	short DXLGetExporterProperty(
			int hDXLExport,
			int prop,
			Pointer retPropValue);
	short DXLSetExporterProperty(
			int hDXLExport,
			int prop,
			Pointer propValue);
	
	short DXLCreateImporter(
			IntByReference prethDXLImport);
	void DXLDeleteImporter(
			int hDXLImport);
	void DXLGetImporterProperty(
			int hDxlImporter,
			int prop,
			Pointer retPropValue);
	short DXLImport(
			int hDXLImport,
			NotesCallbacks.XML_READ_FUNCTION pDXLReaderFunc,
			HANDLE.ByValue hDB,
			Pointer pImAction);
	boolean DXLImportWasErrorLogged(
			int hDXLImport);
	short DXLSetImporterProperty(
			int hDXLImport,
			int prop,
			Pointer propValue);
	/**
	 * Copys the ACL in hList into hNewList.
	 * Unlocks, then locks both handles.
	 * Allocates memory for the copied ACL in hNewList
	 * 
	 * @param hList the source list to copy
	 * @param hNewList a handle pointer to house the new copy
	 * @return the result status
	 */ 
	short ACLCopy(DHANDLE.ByValue hList, DHANDLE.ByReference hNewList);

	short ACLCreate(DHANDLE.ByReference rethACL);
	
	short CompoundTextCreate(
			DHANDLE.ByValue hNote,
			Memory pszItemName,
			DHANDLE.ByReference phCompound);

	Pointer OSGetLMBCSCLS();

	void CompoundTextInitStyle(NotesCompoundStyleStruct style);

	short CompoundTextDefineStyle(
			DHANDLE.ByValue hCompound,
			Memory pszStyleName,
			NotesCompoundStyleStruct pDefinition,
			IntByReference pdwStyleID);
	
	void CompoundTextDiscard(
			DHANDLE.ByValue hCompound);

	short CompoundTextAddTextExt(
			DHANDLE.ByValue hCompound,
			int dwStyleID,
			int FontID,
			Memory pchText,
			int dwTextLen,
			Memory pszLineDelim,
			int dwFlags,
			Pointer pInfo);

	short CompoundTextAddCDRecords(
			DHANDLE.ByValue hCompound,
			Pointer pvRecord,
			int dwRecordLength);

	short CompoundTextAddDocLink(
			DHANDLE.ByValue hCompound,
			NotesTimeDateStruct.ByValue DBReplicaID,
			NotesUniversalNoteIdStruct.ByValue ViewUNID,
			NotesUniversalNoteIdStruct.ByValue NoteUNID,
			Memory pszComment,
			int dwFlags);

	short CompoundTextAssimilateItem(
			DHANDLE.ByValue hCompound,
			DHANDLE.ByValue hNote,
			Memory pszItemName,
			int dwFlags);

	short CompoundTextClose(
			DHANDLE.ByValue hCompound,
			DHANDLE.ByReference phReturnBuffer,
			IntByReference pdwReturnBufferSize,
			Memory pchReturnFile,
			short wReturnFileNameSize);

	boolean IsRunAsWebUser(DHANDLE.ByValue hAgent);
	short AgentOpen (HANDLE.ByValue hDB, int AgentNoteID, DHANDLE.ByReference rethAgent);
	void AgentClose (DHANDLE.ByValue hAgent);
	short AgentCreateRunContext (DHANDLE.ByValue hAgent,
			 Pointer pReserved,
			 int dwFlags,
			 DHANDLE.ByReference rethContext);

	short AgentCreateRunContextExt (DHANDLE.ByValue hAgent, Pointer pReserved, int pOldContext, int dwFlags, DHANDLE.ByReference rethContext);
	short AgentSetDocumentContext(DHANDLE.ByValue hAgentCtx, DHANDLE.ByValue hNote);
	short AgentSetTimeExecutionLimit(DHANDLE.ByValue hAgentCtx, int timeLimit);
	boolean AgentIsEnabled(DHANDLE.ByValue hAgent);
	
	void SetParamNoteID(DHANDLE.ByValue hAgentCtx, int noteId);
	
	short AgentSetUserName(DHANDLE.ByValue hAgentCtx, DHANDLE.ByValue hNameList);
	short AgentRedirectStdout(DHANDLE.ByValue hAgentCtx, short redirType);
	void AgentQueryStdoutBuffer(DHANDLE.ByValue hAgentCtx, DHANDLE.ByReference retHdl, IntByReference retSize);
	void AgentDestroyRunContext (DHANDLE.ByValue hAgentCtx);
	short AgentRun (DHANDLE.ByValue hAgent,
			DHANDLE.ByValue hAgentCtx,
			DHANDLE.ByValue hSelection,
			int dwFlags);
	
	short AgentSetHttpStatusCode(DHANDLE.ByValue hAgentCtx, int httpStatus);
	
	short ClientRunServerAgent(HANDLE.ByValue hdb, int nidAgent, int nidParamDoc,
			int bForeignServer, int bSuppressPrintToConsole);

	short MQCreate(Memory queueName, short quota, int options);
	short MQOpen(Memory queueName, int options, IntByReference retQueue);
	short MQClose(int queue, int options);
	short MQPut(int queue, short priority, Pointer buffer, short length, int options);
	short MQGet(int queue, Pointer buffer, short bufLength, int options, int timeout, ShortByReference retMsgLength);
	void MQPutQuitMsg(int queue);
	boolean MQIsQuitPending(int queue);
	short MQScan(int queue, Pointer buffer, short bufLength, int options, NotesCallbacks.MQScanCallback actionRoutine,
			Pointer ctx, ShortByReference retMsgLength);
	
	short MMCreateConvControls(
			PointerByReference phCC);

	short MMDestroyConvControls(
			Pointer hCC);
	
	void MMConvDefaults(
			Pointer hCC);

	short MMGetAttachEncoding(
			Pointer hCC);
	
	void MMSetAttachEncoding(
			Pointer hCC,
			short wAttachEncoding);
	
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
			short wPointSize);

	short MMGetPointSize(
			Pointer hCC);
	
	short MMGetTypeFace(
			Pointer hCC);
	
	void MMSetTypeFace(
			Pointer hCC,
			short wTypeFace);

	void MMSetAddItems(
			Pointer hCC,
			Memory pszAddItems);

	Pointer MMGetAddItems(
			Pointer hCC);
	
	void MMSetMessageContentEncoding(
			Pointer hCC,
			short wMessageContentEncoding);

	short MMGetMessageContentEncoding(
			Pointer hCC);
	
	void MMSetReadReceipt(
			Pointer hCC,
			short wReadReceipt);

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

	short MIMEStreamItemize(
			DHANDLE.ByValue hNote,
			Memory pchItemName,
			short wItemNameLen,
			int dwFlags,
			Pointer hMIMEStream);
	
	short MIMEStreamOpen(
			DHANDLE.ByValue hNote,
			Memory pchItemName,
			short wItemNameLen,
			int dwOpenFlags,
			PointerByReference rethMIMEStream);

	short MIMEConvertCDParts(
			DHANDLE.ByValue hNote,
			boolean bCanonical,
			boolean bIsMIME,
			Pointer hCC);
	
	short MIMEConvertRFC822TextItemByBLOCKID(
			DHANDLE.ByValue hNote,
			NotesBlockIdStruct.ByValue bhItem,
			NotesBlockIdStruct.ByValue bhValue);
	
	short MIMEItemNameToHeaderName(
			short wMessageType,
			Memory pszItemName,
			Memory retszHeaderName,
			short wHeaderNameSize,
			ShortByReference retwHeaderType);

	short NSFProfileOpen(
			HANDLE.ByValue hDB,
			Memory ProfileName,
			short ProfileNameLength,
			Memory UserName,
			short UserNameLength,
			short CopyProfile,
			DHANDLE.ByReference rethProfileNote);
	short NSFProfileUpdate(
			DHANDLE.ByValue hProfile,
			Memory ProfileName,
			short ProfileNameLength,
			Memory UserName,
			short UserNameLength);
	short NSFProfileSetField(
			HANDLE.ByValue hDB,
			Memory ProfileName,
			short ProfileNameLength,
			Memory UserName,
			short UserNameLength,
			Memory FieldName,
			short FieldNameLength,
			short Datatype,
			Pointer Value,
			int ValueLength);
	short NSFProfileDelete(
			HANDLE.ByValue hDB,
			Memory ProfileName,
			short ProfileNameLength,
			Memory UserName,
			short UserNameLength);

	short NSFProfileEnum(
			HANDLE.ByValue hDB,
			Memory ProfileName,
			short ProfileNameLength,
			NotesCallbacks.NSFPROFILEENUMPROC Callback,
			Pointer CallbackCtx,
			int Flags);
	
	short NSFDbGetMultNoteInfoByUNID(
			HANDLE.ByValue hDB,
			short Count,
			short Options,
			DHANDLE.ByValue hInBuf,
			IntByReference retSize,
			DHANDLE.ByReference rethOutBuf);

	short NSFDbGetMultNoteInfo(
			HANDLE.ByValue  hDb,
			short  Count,
			short  Options,
			DHANDLE.ByValue hInBuf,
			IntByReference retSize,
			DHANDLE.ByReference rethOutBuf);

	short NSFDbGetNoteInfoExt(
			HANDLE.ByValue hDB,
			int  NoteID,
			NotesOriginatorIdStruct retNoteOID,
			NotesTimeDateStruct retModified,
			ShortByReference retNoteClass,
			NotesTimeDateStruct retAddedToFile,
			ShortByReference retResponseCount,
			IntByReference retParentNoteID);

	short NSFDbLargeSummaryEnabled(HANDLE.ByValue hDB);

	short NSFDbGetOptionsExt(HANDLE.ByValue hDB, Memory retDbOptions);
	short NSFDbSetOptionsExt(HANDLE.ByValue hDB, Memory dbOptions, Memory mask);

	short NSFDbIsLocallyEncrypted(HANDLE.ByValue hDB, IntByReference retVal);

	short FTGetLastIndexTime(HANDLE.ByValue hDB, NotesTimeDateStruct retTime);
	short FTDeleteIndex(HANDLE.ByValue hDB);

	short NSFDbNamedObjectEnum(HANDLE.ByValue hDB, NotesCallbacks.b64_NSFDbNamedObjectEnumPROC callback, Pointer param);

	short NSFDbNamedObjectEnum(HANDLE.ByValue hDB, NotesCallbacks.b32_NSFDbNamedObjectEnumPROC callback, Pointer param);

	short NSFDbGetNamedObjectID(HANDLE.ByValue hDB, short NameSpace,
            Memory Name, short NameLength,
            IntByReference rtnObjectID);

	short NSFNoteAttachFile(
			DHANDLE.ByValue note_handle,
			Memory item_name,
			short item_name_length,
			Memory file_name,
			Memory orig_path_name,
			short encoding_type);

	short NSFDbNoteLock(
			HANDLE.ByValue hDB,
			int NoteID,
			int Flags,
			Memory pLockers,
			DHANDLE.ByReference rethLockers,
			IntByReference retLength);
	
	short NSFDbNoteUnlock(
			HANDLE.ByValue hDB,
			int NoteID,
			int Flags);

	short NSFNoteOpenWithLock(
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

	short NSFDbCreateAndCopyExtended(
			Memory srcDb,
			Memory dstDb,
			short NoteClass,
			short limit,
			int flags,
			DHANDLE.ByValue hNames,
			HANDLE.ByReference hNewDb);

	short NIFCloseCollection(DHANDLE.ByValue hCollection);

	short NSFDbOpenTemplateExtended(
			Memory PathName,
			short Options,
			DHANDLE  hNames,
			NotesTimeDateStruct ModifiedTime,
			HANDLE.ByReference rethDB,
			NotesTimeDateStruct retDataModified,
			NotesTimeDateStruct retNonDataModified);

	short NSFQueryDB(HANDLE.ByValue hDb, Memory query, int flags,
			int maxDocsScanned, int maxEntriesScanned, int maxMsecs,
			DHANDLE.ByReference retResults, IntByReference retError, IntByReference retExplain);

	short FTOpenSearch(DHANDLE.ByReference rethSearch);

	short FTSearchExt(
			HANDLE.ByValue hDB,
			DHANDLE.ByReference phSearch,
			DHANDLE.ByValue hColl,
			Memory Query,
			int Options,
			short Limit,
			DHANDLE.ByValue hIDTable,
			IntByReference retNumDocs,
			DHANDLE.ByReference Reserved,
			DHANDLE.ByReference rethResults,
			IntByReference retNumHits,
			int Start,
			short Count,
			short Arg,
			DHANDLE.ByValue hNames);

	short FTCloseSearch(
			DHANDLE.ByValue hSearch);
	
	short NSFDesignHarvest (HANDLE.ByValue hDB, boolean rebuild);

	short FTIndex(HANDLE.ByValue hDB, short options, Memory stopFile, NotesFTIndexStatsStruct retStats);

	short ClientFTIndexRequest(HANDLE.ByValue hDB);

	// *******************************************************************************
	// * htmlapi.h
	// *******************************************************************************
	
	short HTMLProcessInitialize();
	void HTMLProcessTerminate();
	
	short NSFDbGetModifiedNoteTable(HANDLE.ByValue hDB, short NoteClassMask,
			NotesTimeDateStruct.ByValue Since, NotesTimeDateStruct.ByReference retUntil,
			DHANDLE.ByReference rethTable);

	short NSFDbGetModifiedNoteTableExt (HANDLE.ByValue hDB, short NoteClassMask, 
			short Option, NotesTimeDateStruct.ByValue Since,
			NotesTimeDateStruct.ByReference retUntil,
			DHANDLE.ByReference rethTable);

	short NSFDbGetModifiedNotesInfo(HANDLE.ByValue hDB,		/* Database handle */
            short noteClass,	/* 0 == default == NOTE_CLASS_DOCUMENT; */
            int infoRequestedFlags, /* Additional Info requested. DB_GET_MODIFIED_INFO_TRU for now */	
            NotesTimeDateStruct.ByValue Since,	/* Return "elements" from this timedate forward, TIMEDATE_WILDCARD */
            IntByReference arrayCount,		/* total count of "elements" in the array */
            NotesTimeDateStruct.ByReference retUntil, 		/* oldest modified/created value in this set.  */
            IntByReference retmhArray	/* Array of DB_MODIFIED_INFO_ELEMENTS */
            );
	
	short NSFDbModifiedTimeByName(
	  Memory DbName,
	  NotesTimeDateStruct.ByReference retDataModified,
	  NotesTimeDateStruct.ByReference retNonDataModified
	);

  short NIFUpdateFilters (DHANDLE.ByValue hCollection, short ModifyFlags);

	short NIFLocateNote (DHANDLE.ByValue hCollection, NotesCollectionPositionStruct indexPos, int noteID);

	short FolderCreate(
			HANDLE.ByValue hDataDB,
			HANDLE.ByValue hFolderDB,
			int FormatNoteID,
			HANDLE.ByValue hFormatDB,
			Memory pszName,
			short wNameLen,
			int FolderType,
			int dwFlags,
			IntByReference pNoteID);
	
	short FolderRename(
			HANDLE.ByValue hDataDB,
			HANDLE.ByValue  hFolderDB,
			int FolderNoteID,
			Memory pszName,
			short wNameLen,
			int dwFlags);

	short FolderDelete(
			HANDLE.ByValue  hDataDB,
			HANDLE.ByValue  hFolderDB,
			int FolderNoteID,
			int dwFlags);
	
	short FolderCopy(
			HANDLE.ByValue  hDataDB,
			HANDLE.ByValue  hFolderDB,
			int FolderNoteID,
			Memory pszName,
			short wNameLen,
			int dwFlags,
			IntByReference pNewNoteID);
	
	short FolderMove(
			HANDLE.ByValue hDataDB,
			HANDLE.ByValue hFolderDB,
			int FolderNoteID,
			HANDLE.ByValue hParentDB,
			int ParentNoteID,
			int dwFlags);

	short FolderDocAdd(
			HANDLE.ByValue  hDataDB,
			HANDLE.ByValue  hFolderDB,
			int  FolderNoteID,
			DHANDLE.ByValue hTable,
			int  dwFlags);
	short FolderDocCount(
			HANDLE.ByValue  hDataDB,
			HANDLE.ByValue  hFolderDB,
			int  FolderNoteID,
			int dwFlags,
			IntByReference pdwNumDocs);
	short FolderDocRemove(
			HANDLE.ByValue  hDataDB,
			HANDLE.ByValue  hFolderDB,
			int  FolderNoteID,
			DHANDLE.ByValue hTable,
			int dwFlags);
	short FolderDocRemoveAll(
			HANDLE.ByValue  hDataDB,
			HANDLE.ByValue  hFolderDB,
			int  FolderNoteID,
			int dwFlags);

	short NSFFolderGetIDTable(
			HANDLE.ByValue  hViewDB,
			HANDLE.ByValue hDataDB,
			int  viewNoteID,
			int  flags,
			DHANDLE.ByReference hTable);

	short NSFNoteSign(DHANDLE.ByValue hNote);
	short NSFNoteSignExt3(DHANDLE.ByValue hNote, 
			Pointer hKFC,
			Memory SignatureItemName,
			short ItemCount, DHANDLE.ByReference hItemIDs, 
			int Flags, int Reserved,
			Pointer pReserved);

	short NSFNoteSignUsingCtx (
			DHANDLE.ByValue hNote,
			Pointer hSignCtx,
			Memory pszSigItemName,
			short wItemCount,
			DHANDLE.ByValue hItemIDs);

	short NSFNoteUnsign(DHANDLE.ByValue hNote);

	// *******************************************************************************
	// * free-busy
	// *******************************************************************************
	
	/* public short SchFreeTimeSearch(
			NotesUniversalNoteIdStruct pApptUnid,
			NotesTimeDateStruct pApptOrigDate,
			short fFindFirstFit,
			int dwReserved,
			NotesTimeDatePairStruct pInterval,
			short Duration,
			DHANDLE.ByValue hNames,
			DHANDLE.ByReference rethRange);*/
	short SchFreeTimeSearch(
			NotesUniversalNoteIdStruct pApptUnid,
			NotesTimeDateStruct pApptOrigDate,
			short fFindFirstFit,
			int dwReserved,
			NotesTimeDatePairStruct pInterval,
			short Duration,
			Pointer pNames,
			DHANDLE.ByReference rethRange);
	
	void SchContainer_Free(DHANDLE.ByValue hCntnr);
	
	short Schedule_Free(DHANDLE.ByValue hCntnr, int hSched);
	
	short Schedule_ExtractBusyTimeRange(
			DHANDLE.ByValue hCntnr,
			int hSchedObj,
			NotesUniversalNoteIdStruct punidIgnore,
			NotesTimeDatePairStruct pInterval,
			IntByReference retdwSize,
			DHANDLE.ByReference rethRange,
			IntByReference rethMoreCtx);
	
	short Schedule_ExtractMoreBusyTimeRange(
			DHANDLE.ByValue hCntnr,
			int hMoreCtx,
			NotesUniversalNoteIdStruct punidIgnore,
			NotesTimeDatePairStruct pInterval,
			IntByReference retdwSize,
			DHANDLE.ByReference rethRange,
			IntByReference rethMore);
	
	short Schedule_ExtractFreeTimeRange(
			DHANDLE.ByValue hCntnr,
			int hSchedObj,
			NotesUniversalNoteIdStruct punidIgnore,
			short fFindFirstFit,
			short wDuration,
			NotesTimeDatePairStruct pInterval,
			IntByReference retdwSize,
			DHANDLE.ByReference rethRange);
	
	short SchContainer_GetFirstSchedule(
			DHANDLE.ByValue hCntnr,
			IntByReference rethObj,
			Memory retpSchedule);

	short SchContainer_GetNextSchedule(
			DHANDLE.ByValue hCntnr,
			int hCurSchedule,
			IntByReference rethNextSchedule,
			Memory retpNextSchedule);
	
	short Schedule_ExtractSchedList(
			DHANDLE.ByValue hCntnr,
			int hSchedObj,
			NotesTimeDatePairStruct pInterval,
			IntByReference retdwSize,
			DHANDLE.ByReference rethSchedList,
			IntByReference rethMore);
	
	short Schedule_ExtractMoreSchedList(
			DHANDLE.ByValue hCntnr,
			int hMoreCtx,
			NotesTimeDatePairStruct pInterval,
			IntByReference retdwSize,
			DHANDLE.ByReference rethSchedList,
			IntByReference rethMore);
	
	short SchRetrieve(
			NotesUniversalNoteIdStruct pApptUnid,
			NotesTimeDateStruct pApptOrigDate,
			int dwOptions,
			NotesTimeDatePairStruct pInterval,
			Pointer pNames,
			DHANDLE.ByReference rethCntnr,
			Pointer mustBeNull1,
			Pointer mustBeNull2,
			Pointer mustBeNull3);
	
	short HTMLCreateConverter(IntByReference phHTML);
	short HTMLDestroyConverter(int hHTML);
	short HTMLSetHTMLOptions(int hHTML, StringArray optionList);
	short HTMLConvertItem(
			int hHTML,
			HANDLE.ByValue hDB,
			DHANDLE.ByValue hNote,
			Memory pszItemName);
	short HTMLConvertNote(
			int hHTML,
			HANDLE.ByValue hDB,
			DHANDLE.ByValue hNote,
			int numArgs,
			HtmlApi_UrlComponentStruct pArgs);
	short HTMLGetProperty(
			int hHTML,
			int propertyType,
			Pointer pProperty);
	short HTMLSetProperty(
			int hHTML,
			int propertyType,
			Memory pProperty);
	short HTMLGetText(
			int hHTML,
			int startingOffset,
			IntByReference pTextLength,
			Memory pText);
	short HTMLGetReference(
			int hHTML,
			int index,
			IntByReference phRef);
	short HTMLLockAndFixupReference(
			int hRef,
			Memory ppRef);
	short HTMLConvertElement(
			int hHTML,
			HANDLE.ByValue hDB,
			DHANDLE.ByValue hNote,
			Memory pszItemName,
			int itemIndex,
			int offset);

	short DesignOpenCollection(HANDLE.ByValue hDB,
            boolean bPrivate,
            short OpenFlags,
            DHANDLE.ByReference rethCollection,
            IntByReference retCollectionNoteID);
	
	short NSFNoteCreateClone (DHANDLE.ByValue hSrcNote, DHANDLE.ByReference rethDstNote);

	short NSFNoteReplaceItems (DHANDLE.ByValue hSrcNote, DHANDLE.ByValue hDstNote, ShortByReference pwRetItemReplaceCount, boolean fAllowDuplicates);

	short StoredFormAddItems (HANDLE.ByValue hSrcDbHandle, DHANDLE.ByValue hSrcNote, DHANDLE.ByValue hDstNote, boolean bDoSubforms, int dwFlags);

	short StoredFormRemoveItems(DHANDLE.ByValue hNote, int dwFlags);

	short MailNoteJitEx2(Pointer vpRunCtx, DHANDLE.ByValue hNote, short wMailFlags, IntByReference retdwRecipients,
			short jitflag, short wMailNoteFlags, NotesCallbacks.FPMailNoteJitEx2CallBack vCallBack, Pointer vCallBackCtx);

	short MailSetSMTPMessageID(DHANDLE.ByValue hNote, Memory domain, Memory string, short stringLength);

	/*	This is exactly the same as lookup name BE, except it will use the
	cache of the design collection as known to the client.  This is safe
	to use outside the client since it will map to the BE code.  Using this
	version will only be as up to date as the last time the design was
	fetched for the client.  This was added explicitly to avoid cases of
	backend code executing in their world where backend changes to design
	things was not seen immediately. */
	short DesignLookupNameFE (HANDLE.ByValue hDB, short wClass, Pointer szFlagsPattern, Pointer szName,
									  	short wNameLen, int flags,
										IntByReference retNoteID, IntByReference retbIsPrivate,
										NotesCallbacks.DESIGN_COLL_OPENCLOSE_PROC OpenCloseRoutine, Pointer Ctx);

	short NSFItemDeleteByBLOCKID(DHANDLE.ByValue note_handle, NotesBlockIdStruct.ByValue item_blockid);

	void DesignGetNameAndAlias(Memory pString, PointerByReference ppName, ShortByReference pNameLen, PointerByReference ppAlias, ShortByReference pAliasLen);

	boolean StoredFormHasSubformToken(Memory pString);

	short ReplicateWithServerExt(
			Memory portName,
			Memory serverName,
			int options,
			short numFiles,
			Memory fileList,
			ReplExtensionsStruct extendedOptions,
			ReplServStatsStruct retStats);

	@NativeFunctionName(name="OSGetSignalHandler")
	public NotesCallbacks.OSSIGBREAKPROC OSGetBreakSignalHandler(short signalHandlerID);
	@NativeFunctionName(name="OSSetSignalHandler")
	public NotesCallbacks.OSSIGBREAKPROC OSSetBreakSignalHandler(short signalHandlerID, NotesCallbacks.OSSIGBREAKPROC routine);

	@NativeFunctionName(name="OSGetSignalHandler")
	public NotesCallbacks.OSSIGPROGRESSPROC OSGetProgressSignalHandler(short signalHandlerID);
	@NativeFunctionName(name="OSSetSignalHandler")
	public NotesCallbacks.OSSIGPROGRESSPROC OSSetProgressSignalHandler(short signalHandlerID, NotesCallbacks.OSSIGPROGRESSPROC routine);

	@NativeFunctionName(name="OSGetSignalHandler")
	public NotesCallbacks.OSSIGREPLPROC OSGetReplicationSignalHandler(short signalHandlerID);
	@NativeFunctionName(name="OSSetSignalHandler")
	public NotesCallbacks.OSSIGREPLPROC OSSetReplicationSignalHandler(short signalHandlerID, NotesCallbacks.OSSIGREPLPROC routine);

	
	short SECKFMSwitchToIDFile(Memory pIDFileName, Memory pPassword, Memory pUserName,
			short  MaxUserNameLength, int Flags, Pointer pReserved);

	short NSFDbMarkInService(Memory dbPath);
	short NSFDbMarkOutOfService(Memory dbPath);

	short NSGetServerClusterMates(
			Memory pServerName,
			int dwFlags,
			DHANDLE.ByReference phList);
	short NSPingServer(
			Memory pServerName,
			IntByReference pdwIndex,
			DHANDLE.ByReference phList);
	short NSGetServerList(
			Memory pPortName,
			DHANDLE.ByReference retServerTextList);

	short NSFRemoteConsole(
			Memory ServerName,
			Memory ConsoleCommand,
			DHANDLE.ByReference hResponseText);

	short NSFDbHasFullAccess(HANDLE.ByValue hDb);
	short NSFDbReopen (HANDLE.ByValue hDb, HANDLE.ByReference retHDb);
	short NSFDbReopenWithFullAccess (HANDLE.ByValue hDb, HANDLE.ByReference retHDb);
	short NSFDbReopenWithFullAccessExtended (HANDLE.ByValue hDb, HANDLE.ByReference retHDb, DHANDLE.ByValue namesList);

	short SECTokenGenerate(
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
	short SECidfGet(Memory pUserName, Memory pPassword, Memory pPutIDFileHere,
			PointerByReference phKFC, Memory pServerName, int dwReservedFlags, short wReservedType,
			Pointer pReserved);
	short SECidfPut(Memory pUserName, Memory pPassword, Memory pIDFilePath,
			PointerByReference phKFC, Memory pServerName, int dwReservedFlags, short wReservedType,
			Pointer pReserved);
	short SECidfSync( Memory pUserName, Memory pPassword, Memory pIDFilePath,
			PointerByReference phKFC, Memory pServerName, int dwReservedFlags, short wReservedType,
			Pointer pReserved, IntByReference retdwFlags);

	short SECKFMAccess(short param1, Pointer hKFC, Pointer retUsername, Pointer param4);

	short SECKFMOpen(PointerByReference phKFC, Memory pIDFileName, Memory pPassword,
			int Flags, int Reserved, Pointer pReserved);

	short SECKFMClose(PointerByReference phKFC, int flags, int reserved, Pointer pReserved);
	short SECidvResetUserPassword(Memory pServer, Memory pUserName, Memory pPassword,
			short wDownloadCount, int ReservedFlags, Pointer pReserved);

	short SECKFMChangePassword(Memory pIDFile, Memory pOldPassword, Memory pNewPassword);

	short REGGetIDInfo(
			Memory IDFileName,
			short InfoType,
			Memory OutBufr,
			short OutBufrLen,
			ShortByReference ActualLen);

	short SECidvIsIDInVault(Memory pServer, Memory pUserName);

	short NSFNoteCopyAndEncryptExt2(
			DHANDLE.ByValue hSrcNote,
			Pointer hKFC,
			short EncryptFlags,
			DHANDLE.ByReference rethDstNote,
			int Reserved,
			Pointer pReserved);
	short NSFNoteCopyAndEncrypt(
			DHANDLE.ByValue hSrcNote,
			short EncryptFlags,
			DHANDLE.ByReference rethDstNote);
	short NSFNoteCipherDecrypt(
			DHANDLE.ByValue hNote,
			Pointer hKFC,
			int DecryptFlags,
			IntByReference rethCipherForAttachments,
			int  Reserved,
			Pointer pReserved);

	short OOOStartOperation(
			Pointer pMailOwnerName,
			Pointer pHomeMailServer,
			int bHomeMailServer,
			HANDLE.ByValue hMailFile,
			IntByReference hOOOContext,
			PointerByReference pOOOOContext);

	short OOOEndOperation(int hOOContext, Pointer pOOOContext);

	short OOOInit();
	
	short OOOTerm();

	short OOOEnable(
			Pointer pOOOContext,
			int bState);
	
	short OOOGetAwayPeriod(
			Pointer pOOOContext,
			NotesTimeDateStruct tdStartAway,
			NotesTimeDateStruct tdEndAway);
	
	short OOOGetExcludeInternet(
			Pointer pOOOContext,
			IntByReference bExcludeInternet);
	
	short OOOGetGeneralMessage(
			Pointer pOOOContext,
			Memory pGeneralMessage,
			ShortByReference pGeneralMessageLen);
	
	short OOOGetGeneralSubject(
			Pointer pOOOContext,
			Memory pGeneralSubject);
	
	short OOOGetState(
			Pointer pOOOContext,
			ShortByReference retVersion,
			ShortByReference retState);

	short OOOSetAwayPeriod(
			Pointer pOOOContext,
			NotesTimeDateStruct.ByValue tdStartAway,
			NotesTimeDateStruct.ByValue tdEndAway);
	
	short OOOSetExcludeInternet(
			Pointer pOOOContext,
			int bExcludeInternet);

	short OOOSetGeneralMessage(
			Pointer pOOOContext,
			Memory pGeneralMessage,
			short wGeneralMessageLen);
	
	short OOOSetGeneralSubject(
			Pointer pOOOContext,
			Memory pGeneralSubject,
			int bDisplayReturnDate);

	short CESCreateCTXFromNote(DHANDLE.ByValue hNote, IntByReference rethCESCTX);
	short CESGetNoSigCTX(IntByReference rethCESCTX);
	void CESFreeCTX(int hCESCTX);
	short ECLUserTrustSigner ( int hCESCtx, 
			short ECLType,
			short bSessionOnly,
			short wCapabilities,
			short wCapabilities2,
			ShortByReference retwCurrentCapabilities,
			ShortByReference retwCurrentCapabilities2);

	short NSFDbGetUnreadNoteTable2(
			HANDLE.ByValue hDB,
			Memory userName,
			short userNameLength,
			boolean fCreateIfNotAvailable,
			boolean fUpdateUnread,
			DHANDLE.ByReference rethUnreadList);

	short NSFDbSetUnreadNoteTable(
			HANDLE.ByValue hDB,
			Memory UserName,
			short UserNameLength,
			boolean fFlushToDisk,
			DHANDLE.ByValue hOriginalUnreadList,
			DHANDLE.ByValue hUnreadUnreadList);
	
	short NSFDbUpdateUnread(
			HANDLE.ByValue hDataDB,
			DHANDLE.ByValue hUnreadList);
	
	short NSFNoteComputeWithForm(
			DHANDLE.ByValue hNote,
			DHANDLE.ByValue hFormNote,
			int dwFlags,
			NotesCallbacks.b64_CWFErrorProc ErrorRoutine,
			Pointer CallersContext);

	short NSFNoteComputeWithForm(
			DHANDLE.ByValue hNote,
			DHANDLE.ByValue hFormNote,
			int dwFlags,
			NotesCallbacks.b32_CWFErrorProc ErrorRoutine,
			Pointer CallersContext);

	short DesignRefresh(
			Memory Server,
			HANDLE.ByValue hDB,
			int dwFlags,
			NotesCallbacks.ABORTCHECKPROC AbortCheck,
			NotesCallbacks.OSSIGMSGPROC MessageProc);

	short SECVerifyPassword(
			short wPasswordLen,
			Pointer Password,
			short wDigestLen,
			Pointer Digest,
			int ReservedFlags,
			Pointer pReserved);

	short CompoundTextAssimilateFile(
			DHANDLE.ByValue hCompound,
			Memory pszFileName,
			int dwFlags);

	short NSFNoteHardDelete(HANDLE.ByValue hDB, int NoteID, int Reserved);

	short NAMELookup2(
			Memory ServerName,
			int Flags,
			short NumNameSpaces,
			Memory NameSpaces,
			short NumNames,
			Memory Names,
			short NumItems,
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
			short Item,
			ShortByReference retDataType,
			ShortByReference retSize
	);
	short NAMEGetTextItem2(
			Pointer pMatch,
			short Item,
			short Member,
			Memory Buffer,
			short BufLen
	);
	short NAMEGetAddressBooks(
			Memory pszServer,
			short wOptions,
			ShortByReference pwReturnCount,
			ShortByReference pwReturnLength,
			DHANDLE.ByReference phReturn
	);
	
	short ECLGetListCapabilities(Pointer pNamesList, short ECLType, ShortByReference retwCapabilities,
			ShortByReference retwCapabilities2, IntByReference retfUserCanModifyECL);

	short NSFDbModifiedTime(
			HANDLE.ByValue hDB,
			NotesTimeDateStruct.ByReference retDataModified,
			NotesTimeDateStruct.ByReference retNonDataModified);
	
	short NSFDbClearReplHistory(HANDLE.ByValue hDb, int dwFlags);

	short NSFDbGetNotes(
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

	short NSFDbGetNotes(
			HANDLE.ByValue hDB,
			int NumNotes,
			Memory NoteID, //NOTEID array
			Memory NoteOpenFlags, // DWORD array
			Memory SinceSeqNum, // DWORD array
			int ControlFlags,
			HANDLE.ByValue hObjectDB,
			Pointer CallbackParam,
			NotesCallbacks.NSFGetNotesCallback  GetNotesCallback,
			NotesCallbacks.b32_NSFNoteOpenCallback NoteOpenCallback,
			NotesCallbacks.b32_NSFObjectAllocCallback ObjectAllocCallback,
			NotesCallbacks.b32_NSFObjectWriteCallback ObjectWriteCallback,
			NotesTimeDateStruct FolderSinceTime,
			NotesCallbacks.NSFFolderAddCallback FolderAddCallback);

	short NSFDbGetReplHistorySummary(
			HANDLE.ByValue hDb,
			int Flags,
			DHANDLE.ByReference rethSummary,
			IntByReference retNumEntries);

	short NSFNoteSetTUAFromParent(DHANDLE.ByValue hParentNote, DHANDLE.ByValue hChildNote);

	short NSFNoteLSCompileExt(
			HANDLE.ByValue hDb,
			DHANDLE.ByValue hNote,
			int dwFlags,
			NotesCallbacks.LSCOMPILEERRPROC pfnErrProc,
			Pointer pCtx
			);
	
	short NSFDbCreateExtended4 (Memory pathName, short dbClass, 
			 boolean forceCreation, short options, int options2,
			 byte encryptStrength, int MaxFileSize,
			 Memory string1, Memory string2, 
			 short ReservedListLength, short ReservedListCount, 
			 DbOptionsStruct.ByValue dbOptions, DHANDLE.ByValue hNamesList, DHANDLE.ByValue hReservedList);


	// *******************************************************************************
	// * calendaring
	// *******************************************************************************
	
	short CalGetApptunidFromUID(
			Memory pszUID,
			Memory pszApptunid,
			int dwFlags,
			Pointer pCtx);
	
	short CalGetUIDfromNOTEID(
			HANDLE.ByValue hDB,
			int noteid,
			Memory pszUID,
			short wLen,
			Pointer pReserved,
			int dwFlags,
			Pointer pCtx);
	
	short CalGetUIDfromUNID(
			HANDLE.ByValue hDB,
			NotesUniversalNoteIdStruct.ByValue unid,
			Memory pszUID,
			short wLen,
			Pointer pReserved,
			int dwFlags,
			Pointer pCtx);

	short CalReadRange(
			HANDLE.ByValue hDB,
			NotesTimeDateStruct.ByValue tdStart,
			NotesTimeDateStruct.ByValue  tdEnd,
			int dwViewSkipCount,
			int dwMaxReturnCount,
			int dwReturnMask,
			int dwReturnMaskExt,
			Pointer pFilterInfo,
			DHANDLE.ByReference hRetCalData,
			ShortByReference retCalBufferLength,
			DHANDLE.ByReference hRetUIDData,
			IntByReference retNumEntriesProcessed,
			ShortByReference retSignalFlags,
			int dwFlags,
			Pointer pCtx);
	
	short CalGetRecurrenceID(
			NotesTimeDateStruct.ByValue tdInput,
			Memory pszRecurID,
			short wLenRecurId);
	
	short CalCreateEntry(
			HANDLE.ByValue hDB,
			Memory pszCalEntry,
			int dwFlags,
			DHANDLE.ByReference hRetUID,
			Pointer pCtx);
	
	short CalGetUnappliedNotices(
			HANDLE.ByValue hDB,
			Memory pszUID,
			ShortByReference pwNumNotices,
			DHANDLE.ByReference phRetNOTEIDs,
			DHANDLE.ByReference phRetUNIDs,
			Pointer pReserved,
			int dwFlags,
			Pointer pCtx);
	
	short CalGetNewInvitations(
			HANDLE.ByValue hDB,
			NotesTimeDateStruct ptdStart,
			Memory pszUID,
			NotesTimeDateStruct ptdSince,
			NotesTimeDateStruct ptdretUntil,
			ShortByReference pwNumInvites,
			DHANDLE.ByReference phRetNOTEIDs,
			DHANDLE.ByReference phRetUNIDs,
			Pointer pReserved,
			int dwFlags,
			Pointer pCtx);
	
	short CalReadNotice(
			HANDLE.ByValue hDB,
			int noteID,
			DHANDLE.ByReference hRetCalData,
			Pointer pReserved,
			int dwFlags,
			Pointer pCtx);
	
	short CalReadNoticeUNID(
			HANDLE.ByValue hDB,
			NotesUniversalNoteIdStruct.ByValue unid,
			DHANDLE.ByReference hRetCalData,
			Pointer pReserved,
			int dwFlags,
			Pointer pCtx);
	
	short CalEntryAction(
			HANDLE.ByValue hDB,
			Memory pszUID,
			Memory pszRecurID,
			int dwAction,
			int dwRange,
			Memory pszComments,
			NotesCalendarActionDataStruct pExtActionInfo,
			int dwFlags,
			Pointer pCtx);
	
	short CalNoticeAction(
			HANDLE.ByValue hDB,
			int noteID,
			int dwAction,
			Memory pszComments,
			NotesCalendarActionDataStruct pExtActionInfo,
			int dwFlags,
			Pointer pCtx);
	
	short CalNoticeActionUNID(
			HANDLE.ByValue hDB,
			NotesUniversalNoteIdStruct.ByValue unid,
			int dwAction,
			Memory pszComments,
			NotesCalendarActionDataStruct pExtActionInfo,
			int dwFlags,
			Pointer pCtx);
	
	short CalReadEntry(
			HANDLE.ByValue hDB,
			Memory pszUID,
			Memory pszRecurID,
			DHANDLE.ByReference hRetCalData,
			IntByReference pdwReserved,
			int dwFlags,
			Pointer pCtx);
	
	short CalOpenNoteHandle(
			HANDLE.ByValue hDB,
			Memory pszUID,
			Memory pszRecurID,
			DHANDLE.ByReference rethNote,
			int dwFlags,
			Pointer pCtx);
	
	short CalUpdateEntry(
			HANDLE.ByValue hDB,
			Memory pszCalEntry,
			Memory pszUID,
			Memory pszRecurID,
			Memory pszComments,
			int dwFlags,
			Pointer pCtx);

//	STATUS FAR PASCAL SECCheckAndParseSAMLResponse	(BYTE					*pSAML
//			,DWORD					dwSAMLSize
//			,DWORD					dwFlags
//			,char					*pSAMLIdPHostSite
//			,DHANDLE				hNFLVaultServerList
//			,SEC_SAML_ASSERTION_OBJ	*retAssertionObj
//			,DWORD					ctxId
//			);

//	typedef void far * SEC_SAML_ASSERTION_OBJ;

	short SECCheckAndParseSAMLResponse	(Pointer pSAML /* GetPostDataField (cmd, "SAMLResponse") */
			,int dwSAMLSize
			,int dwFlags
			,Memory pSAMLIdPHostSite
			,DHANDLE				hNFLVaultServerList
			,PointerByReference retAssertionObj
			,int ctxId
			);

	
//	STATUS FAR PASCAL SECSAMLDataGetItem		(SEC_SAML_ASSERTION_OBJ	assertionObj
//			,WORD					opCode
//			,DWORD					maxBytes
//			,void					*retValue
//			,DWORD					*usedBytes
//			);

	short SECSAMLDataGetItem		(Pointer	assertionObj
			,short opCode
			,int maxBytes
			,PointerByReference retValue
			,IntByReference usedBytes
			);


//	void FAR PASCAL SECFreeSAMLAssertionObj		(SEC_SAML_ASSERTION_OBJ	*assertionObj);

	void SECFreeSAMLAssertionObj		(PointerByReference assertionObj);


//	STATUS SECIdPCatMgmt(DWORD *pCtxId, WORD action);

	short SECIdPCatMgmt(IntByReference pCtxId, short action);

//	STATUS FAR PASCAL SECMiscProc		(WORD	SEC_mpfct
//			,void	*pP1
//			,void	*pP2
//			,void	*pP3
//			,void	*pP4
//			,void	*pP5
//			,void	*pP6
//			);	

	short SECMiscProc		(short SEC_mpfct
			,Pointer pP1
			,Pointer pP2
			,Pointer pP3
			,Pointer pP4
			,Pointer pP5
			,Pointer pP6
			);	


//	STATUS FAR PASCAL SECidvCreateVaultCheck
//	(WORD				CreateAction
//	,HCERTIFIER			hCreatorCertCtx
//	,char				*pVaultRDN
//	,char				*pVaultServer
//	,char				*pRegServer
//	,IDVLOG_CALLBACK	vIDVLogCallBack
//	,void			 	*pvIDVLogCtx	
//	,HIDVACTION	 		*rethActionCtx
//	);

	short SECidvCreateVaultCheck
	(short CreateAction
	,DHANDLE.ByValue hCreatorCertCtx
	,Memory pVaultRDN
	,Memory pVaultServer
	,Memory pRegServer
	,NotesCallbacks.IDVLOG_CALLBACK	vIDVLogCallBack
	,Pointer pvIDVLogCtx	
	,PointerByReference rethActionCtx
	);


//	typedef void far * HIDVACTION;	// This is a pointer to the ID Vault action context known only to the
	// internal vault routines.

	
//	STATUS FAR PASCAL SECidvAccessCtxItem	(HIDVACTION		hActionCtx
//			,BOOL			fPut
//			,WORD const		ItemCode
//			,DWORD const	dwDataLength
//			,void			*vpData
//			);

	short SECidvAccessCtxItem	(Pointer hActionCtx
			,boolean fPut
			,short ItemCode
			,int dwDataLength
			,Pointer vpData
			);

//	typedef struct 
//	{
//		int i_phase;
//		int i_samlVersion;
//		int i_samlProviderType;
//		char i_providerID[1024];
//		char i_singleSignOnUrl[MAX_DOMINO_URL_LENGTH * 4];
//		char i_assertionUserName[MAXUSERNAME*2];
//		char i_dominoUserName[MAXUSERNAME*2];
//		char i_vaultServerContext[MAXPATH*2];
//		char i_idFileName[MAXPATH*2];
//		char i_companyName[MAXUSERNAME*2];
//		void* i_vpHandlerCtx;
//		PH3_SAML_INTERCEPT	i_interceptInfo;
//		SECGETJWTFROMIDP i_ProtonCallback;
//	} SamlIDVaultContext;
	
//	int FAR PASCAL SECGetIdVaultAuthnRequestIDPContext (SamlIDVaultContext *idvCtx, DWORD debugLevel);

	int SECGetIdVaultAuthnRequestIDPContext (PointerByReference idvCtx, int debugLevel);

//	typedef struct
//	{
//	FLAG		fTryAgain:1;			// Try to connect to another server
//	FLAG		fReferralSrvsSet:1;		// Set if we connected to a vault-aware
//										// server and that server either
//										// indicated that it hosts the target
//										// vault or provided a list of referral
//										// servers which host the target vault
//	DHANDLE		hClusterMates;
//
//	IDV_OBJECT	*pCurrRefSrv;			// Ptr to current referral/replica server
//	IDV_OBJECT	*pReferralSrvs;			// Ptr to referral server list
//
//	char	ServerName [MAXUSERNAME];	// Name of server to connect to
//	}
//	IDV_SRV_CONN;
	
//	STATUS FAR PASCAL SECidvGetNextServer   (STATUS			CallerStatus
//            ,HIDVACTION     hActionCtx
//            ,IDV_SRV_CONN	*pSrvConn
//    		);

	short SECidvGetNextServer   (short CallerStatus
            ,Pointer hActionCtx
            ,PointerByReference pSrvConn
    		);

//	STATUS FAR PASCAL SECidvCreateCtx		(WORD				CreateAction
//			,char				*pName
//			,IDVLOG_CALLBACK	vIDVLogCallBack
//			,void			 	*pvIDVLogCtx	
//			,HIDVACTION	 		*rethActionCtx
//			);

	short SECidvCreateCtx		(short CreateAction
			,Memory pName
			,NotesCallbacks.IDVLOG_CALLBACK	vIDVLogCallBack
			,Pointer pvIDVLogCtx	
			,PointerByReference rethActionCtx
			);

//	void   FAR PASCAL SECMemFree		 (DHANDLE *);

	void SECMemFree		 (DHANDLE.ByReference hdl);

//	typedef	struct 
//	{
//	MEMHANDLE	 mhObj;
//	BYTE		 *pObj;
//	DWORD		 dwSize;
//	DWORD		 type;
//	}
//	SEC_MEMOBJ_DESC;
	
//	void   FAR PASCAL SECMemoryFreeDesc	(SEC_MEMOBJ_DESC *pDesc);

	void SECMemoryFreeDesc	(PointerByReference pDesc);

//	STATUS FAR PASCAL IDVFreeList (IDV_OBJECT **pListPtr);

	short IDVFreeList (PointerByReference pListPtr);

	short REGCrossCertifyID(
			HANDLE.ByValue  hCertCtx,
			short spare1,
			Memory regServer,
			Memory idFileName,
			Memory location,
			Memory comment,
			Memory forwardAddress,
			short spare2,
			NotesCallbacks.REGSIGNALPROC  pStatusFunc,
			Memory errorPathName);
	
	short SECKFMGetCertifierCtx(
			Memory pCertFile,
			KFM_PASSWORDStruct.ByReference pKfmPW,
			Memory pLogFile,
			NotesTimeDateStruct.ByReference pExpDate,
			Memory retCertName,
			HANDLE.ByReference rethKfmCertCtx,
			ShortByReference retfIsHierarchical,
			ShortByReference retwFileVersion);
	
	void SECKFMCreatePassword(
			Memory pPassword,
			KFM_PASSWORDStruct.ByReference retHashedPassword);
	
	void SECKFMFreeCertifierCtx(
			HANDLE.ByValue hKfmCertCtx);

	short SECKFMMakeSafeCopy(Pointer hKFC, short Type, short Version, Memory pFileName);
	
	void OSCurrentTIMEDATE(NotesTimeDateStruct retTimeDate);
	short TIMEDATEtoRFC3339Date(NotesTimeDateStruct ptdTimeDate, Memory pachText, short wTextLen);

	short NSFDbItemDefTableExt(
			HANDLE.ByValue hDB,
			HANDLE.ByReference retItemNameTable);

	short NSFItemDefExtLock(
			Pointer pItemDefTable,
			NotesItemDefinitionTableLock itemDefTableLock);

	short NSFItemDefExtEntries(
			NotesItemDefinitionTableLock itemDefTableLock,
			IntByReference numEntries);
	
	short NSFItemDefExtGetEntry(
			NotesItemDefinitionTableLock itemDefTableLock,
			int itemNum,
			ShortByReference itemType,
			ShortByReference itemLength,
			Pointer itemName);
	
	short NSFItemDefExtUnlock(
			NotesItemDefinitionTableExt itemDefTable,
			NotesItemDefinitionTableLock itemDefTableLock);
	
	short NSFItemDefExtFree(
			NotesItemDefinitionTableExt itemDeftable);

	short AssistantGetLSDataNote (HANDLE.ByValue hDB, int NoteID, NotesUniversalNoteIdStruct.ByReference retUNID);
	
	boolean AddInIdleDelay(int Delay);
	void AddInLogErrorText(Memory string, short AdditionalErrorCode, Memory Arg);
	void AddInLogMessageText(Memory string, short AdditionalErrorCode, Object... args);
	boolean AddInSecondsHaveElapsed(int Seconds);
	default boolean AddInMinutesHaveElapsed(int Minutes) {
		return AddInSecondsHaveElapsed(Minutes*60);
	}
	boolean AddInDayHasElapsed();
	boolean AddInShouldTerminate();
	long AddInCreateStatusLine(Memory TaskName);
	void AddInDeleteStatusLine(long hDesc);
	void AddInSetStatusLine(long hDesc, Memory string);
	void OSPreemptOccasionally();
	
	boolean CmemflagTestMultiple (Pointer s, short length, Pointer pattern);

	short DesignEnum2 (HANDLE.ByValue hDB,
			   short NoteClass,
			   Memory  pszFlagsPattern,
			   int dwFlags,
			   NotesCallbacks.b64_DESIGNENUMPROC proc,
			   Pointer parameters,
			   NotesCallbacks.DESIGN_COLL_OPENCLOSE_PROC openCloseRoutine,
			   Pointer ctx);

	short DesignEnum2 (HANDLE.ByValue hDB,
			   short NoteClass,
			   Memory  pszFlagsPattern,
			   int dwFlags,
			   NotesCallbacks.b32_DESIGNENUMPROC proc,
			   Pointer parameters,
			   NotesCallbacks.DESIGN_COLL_OPENCLOSE_PROC openCloseRoutine,
			   Pointer ctx);

	short DesignGetNoteTable(HANDLE.ByValue hDB, short NoteClass, DHANDLE.ByReference rethIDTable);

	short QueueCreate (DHANDLE.ByReference rethQueue);
	short QueueDelete (DHANDLE.ByValue hQueue);
	short QueueGet (DHANDLE.ByValue hQueue, DHANDLE.ByReference rethEntry);

	short QueuePut (DHANDLE.ByValue hQueue, DHANDLE.ByValue hEntry);
	short QueueRemoveAndPut (DHANDLE.ByValue hQueue, DHANDLE.ByValue hEntry);

	short QueueTest (DHANDLE.ByValue hQueue, ShortByReference retNumEntries);
	short QueueGetNext (DHANDLE.ByValue queue, DHANDLE.ByReference rtnhandle);
	short QueueRemoveEntry (DHANDLE.ByValue queue, DHANDLE.ByValue hentry);

	void NSFAsyncNotifyPoll(Pointer actx, IntByReference retMySessions, ShortByReference retFirstError);
	void NSFUpdateAsyncIOStatus(Pointer actx);
	void NSFCancelAsyncIO (Pointer actx);

	short NSFRemoteConsoleAsync (
			Memory serverName, Memory ConsoleCommand, int Flags,
			DHANDLE.ByReference phConsoleText, DHANDLE.ByReference phTasksText, DHANDLE.ByReference phUsersText,
			ShortByReference pSignals, IntByReference pConsoleBufferID, DHANDLE.ByValue hQueue,
			NotesCallbacks.ASYNCNOTIFYPROC Proc,Pointer param, PointerByReference retactx);

	int Csscanf(Pointer buffer, Pointer format, PointerByReference... retNextbuffer);
	short Cstrlen(Pointer ptr);
	Pointer Cmovmem(Pointer src, Pointer dst, int count);

	short ADMINReqChkAccessMoveReplica(
			HANDLE.ByValue dbhAdmin4,
			Memory chAuthor,
			Memory chSrcServer,
			Memory chSrcPathName,
			Memory chTitle,
			NotesDbReplicaInfo Replicainfo,
			Memory chDesServer,
			Memory chDesPathName,
			NotesAdminReqParams arpAdminReqParamsPtr,
			short wAdminReqParamsSize);
	
	short ADMINReqChkAccessNCMoveReplica(
			HANDLE.ByValue dbhAdmin4,
			Memory chAuthor,
			Memory chSrcServer,
			Memory chSrcPathName,
			Memory chTitle,
			NotesDbReplicaInfo Replicainfo,
			Memory chDesServer,
			Memory chDesPathName,
			NotesAdminReqParams arpAdminReqParamsPtr,
			short wAdminReqParamsSize);
	
	short ADMINReqChkAccessNewReplica(
			HANDLE.ByValue dbhAdmin4,
			Memory chAuthor,
			Memory chSrcServer,
			Memory chSrcPathName,
			Memory chTitle,
			NotesDbReplicaInfo Replicainfo,
			Memory chDesServer,
			Memory chDesPathName,
			NotesAdminReqParams arpAdminReqParamsPtr,
			short wAdminReqParamsSize);
	
	short ADMINReqDeleteInACL(
			HANDLE.ByValue dbhAdmin4,
			Memory chAuthor,
			Memory chUserName,
			Memory chMailServerName,
			Memory chMailFileName,
			Memory chDeleteMailFile,
			NotesAdminReqParams arpAdminReqParamsPtr,
			short wAdminReqParamsSize);
	
	short ADMINReqDeleteInNAB(
			HANDLE.ByValue dbhAdmin4,
			Memory chAuthor,
			Memory chUserName,
			Memory chMailServerName,
			Memory chMailFileName,
			Memory chDeleteMailFile,
			NotesAdminReqParams arpAdminReqParamsPtr,
			short wAdminReqParamsSize);
	
	short ADMINReqMoveComplete(
			HANDLE.ByValue hCertCtx,
			HANDLE.ByValue dbhAdmin4,
			int nhAdminReq,
			Memory pTargetCert,
			IntByReference retfWeLoggedThisEntry,
			IntByReference retfFatalError,
			NotesAdminReqParams arpAdminReqParamsPtr,
			short wAdminReqParamsSize);
	
	short ADMINReqMoveUserInHier(
			HANDLE.ByValue hCertCtx,
			HANDLE.ByValue dbhNab,
			int nhNote,
			Memory pTargetCert,
			IntByReference retfWeLoggedThisEntry,
			IntByReference retfFatalError,
			NotesAdminReqParams arpAdminReqParamsPtr,
			short wAdminReqParamsSize);

	short ADMINReqRecertify(
			HANDLE.ByValue hCertCtx,
			HANDLE.ByValue dbhNab,
			int nhNote,
			IntByReference retfWeLoggedThisEntry,
			IntByReference retfFatalError,
			NotesAdminReqParams arpAdminReqParamsPtr,
			short wAdminReqParamsSize);
	
	short ADMINReqRename(
			HANDLE.ByValue hCertCtx,
			HANDLE.ByValue dbhNab,
			int nhNote,
			Memory pFirstName,
			Memory pMiddleInitial,
			Memory pLastName,
			Memory pOU,
			IntByReference retfWeLoggedThisEntry,
			IntByReference retfFatalError,
			NotesAdminReqParams arpAdminReqParamsPtr,
			short wAdminReqParamsSize);
	
	short ADMINReqSetOutlookSupport(
			HANDLE.ByValue dbhAdmin4,
			Memory chAuthor,
			Memory chServerName,
			Memory chEnabled,
			NotesAdminReqParams arpAdminReqParamsPtr,
			short wAdminReqParamsSize);
	
	short ADMINReqUpgradeToHier(
			HANDLE.ByValue hCertCtx,
			HANDLE.ByValue dbhNab,
			int nhNote,
			Memory pOU,
			Memory retfWeLoggedThisEntry,
			IntByReference retfFatalError,
			NotesAdminReqParams arpAdminReqParamsPtr,
			short wAdminReqParamsSize);

	short SECOpenAddressBookOnServer(Memory serverName, HANDLE.ByReference rethDb);
	short AdminpProxyDbOpen(Memory serverName, HANDLE.ByReference rethDb);
	
	short AdminpCreateRequest(
			HANDLE.ByValue hProxyDb,
			Memory chRequestIdentifierPtr,
			short wRequestSpecificItemsNumber,
			Pointer arppRequestSpecificProfilePtr,
			int eIsAuthorProvided,
			int eUseAdminServerOfPAB,
			Memory chErrorObjectBufferPtr,
			short wErrorObjectBufferLen);
	
	short NSFDbLocalSecInfoGetLocal(HANDLE.ByValue hDb, IntByReference state, IntByReference strength);
	
	void NSFDbAccessGet(HANDLE.ByValue hDb, ShortByReference retAccessLevel, ShortByReference retAccessFlag);

	short NSFDbMajorMinorVersionGet (HANDLE.ByValue hDB, ShortByReference retMajorVersion, 
			ShortByReference retMinorVersion);

	short NSFDbGetMajMinVersion(HANDLE.ByValue hDb, NotesBuildVersionStruct retBuildVersion);

	short NSFDbCompactExtended4(Memory Pathname, int Options, int Options2, int TimeLimit,
			IntByReference retStats, IntByReference Granules, DHANDLE.ByValue hNamesList);

	short SECMakeProxyEntry (short fct, DHANDLE.ByValue hNABEntry,Memory pProxyDBServer,
			NotesStringDescStruct pName ,NotesStringDescStruct pExtraItem,
			NotesStringDescStruct pHAC_ABPI ,NotesStringDescStruct pHAC_Change,
			NotesStringDescStruct pHAC_ChangeSig ,Pointer pFctSpecific ,IntByReference retNoteID);

	short DirEntryIDTrim(Pointer entryID, int parts);

	int DirEntryIDGetType(Pointer entryId);

	short DirDomainGetInfo(Memory serverName, Memory domainName, int infoType, Pointer pInfo);

	void OSLocalizePath(Pointer ptr);
	short OSPathFileType(Pointer ptr, ShortByReference retValue);
	Pointer Cstrncat(Pointer to, Pointer from, int tosize);

	boolean IntlTextEqualCaseInsensitive(Pointer str1, Pointer str2, short length, boolean unused);

	short NSFItemAppendTextList(
			DHANDLE.ByValue note_handle,
			Memory item_name,
			Memory entry_text,
			short text_len,
			boolean duplicate_flag);

	boolean NetIsVirtualizedDirectory();

	short DirCtxAlloc2(Memory serverName, Memory domainName, DHANDLE.ByReference rethCtx);

	short DirCtxSetFlags(DHANDLE.ByValue hCtx, int flags);
	
	/* pServerName could (a) local resident server
    (b) user's home mail server 
	 (c) NULL, in which case we look locally */
	short LookupUserDirID (Memory pUserName,	   
					Memory pServerName,
					HANDLE.ByValue hUserMailFile,
					Memory pszDirEntryID);

	short REGSearchByFullnameOrInternetAddress (DHANDLE.ByValue hDirCtx,
			Memory Name,
			boolean fMatchOnFirstFullNameEntryOnly,
			Memory retDirEntryID);

	short DirCtxGetEntryByID(DHANDLE.ByValue hCtx, Memory entryId, Memory items, short numItems, IntByReference hEntry);

	short DirEntryGetType(int hEntry, IntByReference pwType);

	short DirEntryNoteGet(int hEntry, DHANDLE.ByReference hNote);

	short REGFindAddressBookEntryExtended(
			HANDLE.ByValue hAddressBook, 
			Memory NameSpace,
			Memory Name, 
			boolean fMatchOnFirstFullNameEntryOnly,
			IntByReference EntryNoteID,
			boolean fVirtDirLookup);

	void DirCtxFree(DHANDLE.ByValue hCtx);

	void DirEntryFree(int hEntry);

	short NSFItemGetText(
			DHANDLE.ByValue note_handle,
			Memory item_name,
			Memory item_text,
			short text_len);
	
	short DirEntryGetItemByName(int hDirEntry, Memory itemName, ShortByReference pItemDataType,
			DHANDLE.ByReference phItemValue, IntByReference pItemValLen);

	short NetOpenLocDB(HANDLE.ByReference rethDB);

	short NetGetCurrentLocNoteID(IntByReference retLocNoteID);
	
	short NSFDbGetNamesList(HANDLE.ByValue hDB, int Flags, DHANDLE.ByReference rethNamesList);
	
	int OSProcessGroup(int query);
}
