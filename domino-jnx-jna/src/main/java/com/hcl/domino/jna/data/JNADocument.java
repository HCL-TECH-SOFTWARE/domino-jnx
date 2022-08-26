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
package com.hcl.domino.jna.data;

import static java.text.MessageFormat.format;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.ReferenceQueue;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.hcl.domino.DominoException;
import com.hcl.domino.admin.idvault.UserId;
import com.hcl.domino.commons.constants.UpdateNote;
import com.hcl.domino.commons.data.AbstractTypedAccess;
import com.hcl.domino.commons.data.DefaultDominoDateRange;
import com.hcl.domino.commons.data.SignatureDataImpl;
import com.hcl.domino.commons.design.FormFieldImpl;
import com.hcl.domino.commons.design.view.CollationDecoder;
import com.hcl.domino.commons.design.view.CollationEncoder;
import com.hcl.domino.commons.design.view.DominoCalendarFormat;
import com.hcl.domino.commons.design.view.DominoCollationInfo;
import com.hcl.domino.commons.design.view.DominoViewFormat;
import com.hcl.domino.commons.design.view.ViewFormatDecoder;
import com.hcl.domino.commons.design.view.ViewFormatEncoder;
import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.commons.errors.UnsupportedItemValueError;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.mime.MimePartOptions;
import com.hcl.domino.commons.mime.NotesMIMEPart;
import com.hcl.domino.commons.mime.NotesMIMEPart.PartType;
import com.hcl.domino.commons.richtext.DefaultRichTextList;
import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.commons.util.ListUtil;
import com.hcl.domino.commons.util.NotesDateTimeUtils;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.NotesItemDataUtil;
import com.hcl.domino.commons.util.PlatformUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Attachment;
import com.hcl.domino.data.Attachment.Compression;
import com.hcl.domino.data.AutoCloseableDocument;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Database.OpenDocumentMode;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DocumentValueConverter;
import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.DominoOriginatorId;
import com.hcl.domino.data.DominoUniversalNoteId;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.data.Item;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.data.PreV3Author;
import com.hcl.domino.design.DesignAgent;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.exception.LotusScriptCompilationException;
import com.hcl.domino.exception.ObjectDisposedException;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.JNADominoClient;
import com.hcl.domino.jna.data.JNADatabaseObjectProducer.ObjectInfo;
import com.hcl.domino.jna.internal.AgentRunInfoDecoder;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.ItemDecoder;
import com.hcl.domino.jna.internal.JNAMemoryUtils;
import com.hcl.domino.jna.internal.JNANotesConstants;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.NotesNamingUtils;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.callbacks.NotesCallbacks;
import com.hcl.domino.jna.internal.callbacks.Win32NotesCallbacks;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNADatabaseAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNADocumentAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE32;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE64;
import com.hcl.domino.jna.internal.gc.handles.HANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.richtext.JNARichtextNavigator;
import com.hcl.domino.jna.internal.structs.NotesBlockIdStruct;
import com.hcl.domino.jna.internal.structs.NotesFileObjectStruct;
import com.hcl.domino.jna.internal.structs.NotesMIMEPartStruct;
import com.hcl.domino.jna.internal.structs.NotesNumberPairStruct;
import com.hcl.domino.jna.internal.structs.NotesObjectDescriptorStruct;
import com.hcl.domino.jna.internal.structs.NotesOriginatorIdStruct;
import com.hcl.domino.jna.internal.structs.NotesRangeStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDatePairStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.hcl.domino.jna.internal.structs.NotesUniversalNoteIdStruct;
import com.hcl.domino.jna.richtext.JNARichtextWriter;
import com.hcl.domino.jna.utils.JNADominoUtils;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.JNXServiceFinder;
import com.hcl.domino.misc.Loop;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.Ref;
import com.hcl.domino.richtext.FormField;
import com.hcl.domino.richtext.RichTextRecordList;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.conversion.IRichTextConversion;
import com.hcl.domino.richtext.records.CDField;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;
import com.hcl.domino.richtext.structures.ObjectDescriptor;
import com.hcl.domino.richtext.structures.RFC822ItemDesc;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetHeaders;

public class JNADocument extends BaseJNAAPIObject<JNADocumentAllocations> implements Document, AutoCloseableDocument {
	private Set<DocumentClass> m_documentClass;
	private AbstractTypedAccess m_typedAccess;
	private ThreadLocal<Set<Class<?>>> readingItemType = ThreadLocal.withInitial(HashSet::new);
	private boolean m_saveMessageOnSend;

	public JNADocument(JNADatabase parent, IAdaptable adaptable) {
		this(parent, adaptable, false);
	}
	
	public JNADocument(JNADatabase parent, IAdaptable adaptable, boolean noRecycle) {
		super(parent);
		
		DHANDLE handle = adaptable.getAdapter(DHANDLE.class);
		if (handle==null) {
			throw new DominoException(0, "Missing expected note handle");
		}
		getAllocations().setNoteHandle(handle);
		getAllocations().setNoRecycle(noRecycle);
		
		m_typedAccess = new AbstractTypedAccess() {
			@Override
			public boolean hasItem(String itemName) {
				return JNADocument.this.hasItem(itemName);
			}

			@Override
			public List<String> getItemNames() {
				return JNADocument.this.getItemNames();
			}
			
			@Override
			protected List<?> getItemValue(String itemName) {
				return JNADocument.this.getItemValue(itemName);
			}
			
			@Override
			protected <T> T getViaValueConverter(String itemName, Class<T> valueType, T defaultValue) {
				DocumentValueConverter converter = JNXServiceFinder.findServices(DocumentValueConverter.class)
					.filter(c -> c.supportsRead(valueType))
					.sorted(Comparator.comparing(DocumentValueConverter::getPriority).reversed())
					.findFirst()
					.orElse(null);

				if (converter!=null) {
					if (readingItemType.get().contains(converter.getClass())) {
						throw new IllegalStateException(format("Infinite loop detected reading the value of item {0} as type {1}",
								itemName, valueType.getName()));
					}
					readingItemType.get().add(converter.getClass());
					try {
						return converter.getValue(JNADocument.this, itemName, valueType, defaultValue);
					}
					finally {
						readingItemType.get().remove(converter.getClass());
					}
				}
				else {
					throw new IllegalArgumentException(format("Unsupported return value type: {0}", valueType.getName()));
				}
			}
			
			@Override
			protected <T> List<T> getAsListViaValueConverter(String itemName, Class<T> valueType, List<T> defaultValue) {
				DocumentValueConverter converter = JNXServiceFinder.findServices(DocumentValueConverter.class)
					.filter(c -> c.supportsRead(valueType))
					.sorted(Comparator.comparing(DocumentValueConverter::getPriority).reversed())
					.findFirst()
					.orElse(null);

				if (converter!=null) {
					if (readingItemType.get().contains(converter.getClass())) {
						throw new IllegalStateException(format("Infinite loop detected reading the value of item {0} as type {1}",
								itemName, valueType.getName()));
					}
					readingItemType.get().add(converter.getClass());
					try {
						return converter.getValueAsList(JNADocument.this, itemName, valueType, defaultValue);
					}
					finally {
						readingItemType.get().remove(converter.getClass());
					}
				}
				else {
					throw new IllegalArgumentException(format("Unsupported return value type: {0}", valueType.getName()));
				}
			}
		};
		
		setInitialized();
	}

	@Override
	public boolean isClosed() {
		JNADocumentAllocations allocations = getAllocations();
		return allocations.isDisposed();
	}
	
	@Override
	public void close() {
		JNADocumentAllocations allocations = getAllocations();
		
		if (allocations.isDisposed()) {
			allocations.dispose();
		}
	}
	
	@Override
	public AutoCloseableDocument autoClosable() {
		return this;
	}
	
	@Override
	public Database getParentDatabase() {
		return (Database) super.getParent();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public final <T> T getAdapterLocal(Class<T> clazz) {
		if (clazz == DHANDLE.class) {
			return (T) getAllocations().getNoteHandle();
		}
		else if (clazz == AutoCloseableDocument.class) {
			return (T) autoClosable();
		}
		
		return null;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected JNADocumentAllocations createAllocations(IGCDominoClient parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue queue) {

		return new JNADocumentAllocations(parentDominoClient, parentAllocations, this, queue);
	}

	@Override
	public List<?> getItemValue(String itemName) {
		checkDisposed();
		
		JNAItem item = (JNAItem) getFirstItem(itemName).orElse(null);
		if (item==null) {
			return Collections.emptyList();
		}
		
		int valueLength = item.getValueLength();
		
		//lock and decode value
		NotesBlockIdStruct valueBlockId = item.getValueBlockId();
		
		Pointer valuePtr = Mem.OSLockObject(valueBlockId);
		try {
			List<Object> values = getItemValue(itemName, item.getItemBlockId(), valueBlockId, valuePtr, valueLength);
			return values;
		}
		finally {
			Mem.OSUnlockObject(valueBlockId);
		}
	}

	/**
	 * Decodes an item value
	 * 
	 * @param itemName item name (for logging purpose)
	 * @param valueBlockId value block id
	 * @param valueLength item value length plus 2 bytes for the data type WORD
	 * @return item value as list
	 */
	List<Object> getItemValue(String itemName, NotesBlockIdStruct itemBlockId, NotesBlockIdStruct valueBlockId, int valueLength) {
		Pointer valuePtr = Mem.OSLockObject(valueBlockId);
		try {
			List<Object> values = getItemValue(itemName, itemBlockId, valueBlockId, valuePtr, valueLength);
			return values;
		}
		finally {
			Mem.OSUnlockObject(valueBlockId);
		}
	}
	
	/**
	 * Decodes an item value
	 * 
	 * @param notesAPI Notes API
	 * @param itemName item name (for logging purpose)
	 * @param itemBlockId item block id
	 * @param valueBlockId value block id
	 * @param valuePtr pointer to the item value
	 * @param valueLength item value length plus 2 bytes for the data type WORD
	 * @return item value as list
	 */
	List<Object> getItemValue(String itemName, NotesBlockIdStruct itemBlockId, NotesBlockIdStruct valueBlockId,
			Pointer valuePtr, int valueLength) {
		
		short dataType = valuePtr.getShort(0);
		int dataTypeAsInt = dataType & 0xffff;
		
		boolean supportedType = false;
		if (dataTypeAsInt == ItemDataType.TYPE_TEXT.getValue()) {
			supportedType = true;
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_TEXT_LIST.getValue()) {
			supportedType = true;
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_NUMBER.getValue()) {
			supportedType = true;
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_TIME.getValue()) {
			supportedType = true;
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_NUMBER_RANGE.getValue()) {
			supportedType = true;
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_TIME_RANGE.getValue()) {
			supportedType = true;
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_OBJECT.getValue()) {
			supportedType = true;
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_NOTEREF_LIST.getValue()) {
			supportedType = true;
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_COLLATION.getValue()) {
			supportedType = true;
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_VIEW_FORMAT.getValue()) {
			supportedType = true;
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_FORMULA.getValue()) {
			supportedType = true;
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_UNAVAILABLE.getValue()) {
			supportedType = true;
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_MIME_PART.getValue()) {
			supportedType = true;
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_RFC822_TEXT.getValue()) {
			supportedType = true;
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_COMPOSITE.getValue()) {
			supportedType = true;
		}
		else if(dataTypeAsInt == ItemDataType.TYPE_CALENDAR_FORMAT.getValue()) {
      supportedType = true;
    }
		else if(dataTypeAsInt == ItemDataType.TYPE_USERID.getValue()) {
		  supportedType = true;
		}
		
		if (!supportedType) {
			throw new DominoException(format("Data type for value of item {0} is currently unsupported: {1}", itemName, dataTypeAsInt));
		}

		int checkDataType = valuePtr.getShort(0) & 0xffff;
		Pointer valueDataPtr = valuePtr.share(2);
		int valueDataLength = valueLength - 2;
		
		if (checkDataType!=dataTypeAsInt) {
			throw new IllegalStateException(format("Value data type does not meet expected date type: found {0}, expected {1}", checkDataType,
					dataTypeAsInt));
		}
		if (dataTypeAsInt == ItemDataType.TYPE_TEXT.getValue()) {
			String txtVal = (String) ItemDecoder.decodeTextValue(valueDataPtr, valueDataLength, false);
			return txtVal==null ? Collections.emptyList() : Arrays.asList((Object) txtVal);
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_TEXT_LIST.getValue()) {
			List<Object> textList = valueDataLength==0 ? Collections.emptyList() : ItemDecoder.decodeTextListValue(valueDataPtr, false);
			return textList==null ? Collections.emptyList() : textList;
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_NUMBER.getValue()) {
			double numVal = ItemDecoder.decodeNumber(valueDataPtr, valueDataLength);
			return Arrays.asList((Object) Double.valueOf(numVal));
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_NUMBER_RANGE.getValue()) {
			List<Object> numberList = ItemDecoder.decodeNumberList(valueDataPtr, valueDataLength);
			return numberList==null ? Collections.emptyList() : numberList;
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_TIME.getValue()) {
			DominoDateTime td = ItemDecoder.decodeTimeDateAsNotesTimeDate(valueDataPtr, valueDataLength);
			return Arrays.asList((Object) td);
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_TIME_RANGE.getValue()) {
			List<Object> tdValues = ItemDecoder.decodeTimeDateListAsNotesTimeDate(valueDataPtr);
			return tdValues==null ? Collections.emptyList() : tdValues;
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_OBJECT.getValue()) {
		  ObjectDescriptor objDescriptor = JNAMemoryUtils.readStructure(ObjectDescriptor.class, valueDataPtr);
			
			int rrv = objDescriptor.getRRV();
			
			ObjectDescriptor.ObjectType type = objDescriptor.getObjectType().orElse(ObjectDescriptor.ObjectType.UNKNOWN); 
			switch(type) {
		  case FILE: {
		    Pointer fileObjectPtr = valueDataPtr;
        
        NotesFileObjectStruct fileObject = NotesFileObjectStruct.newInstance(fileObjectPtr);
        fileObject.read();
        
        short compressionType = fileObject.CompressionType;
        NotesTimeDateStruct fileCreated = Objects.requireNonNull(fileObject.FileCreated, "Unexpected null value for fileObject.FileCreated");
        NotesTimeDateStruct fileModified = Objects.requireNonNull(fileObject.FileModified, "Unexpected null value for fileObject.FileModified");
        DominoDateTime fileCreatedWrap = new JNADominoDateTime(fileCreated.Innards);
        DominoDateTime fileModifiedWrap = new JNADominoDateTime(fileModified.Innards);
        
        short fileNameLength = fileObject.FileNameLength;
        long fileSize = Integer.toUnsignedLong(fileObject.FileSize);
        short flags = fileObject.Flags;
        
        Compression compression = null;
        for (Compression currComp : Compression.values()) {
          if (compressionType == currComp.getValue()) {
            compression = currComp;
            break;
          }
        }
        
        Pointer fileNamePtr = fileObjectPtr.share(JNANotesConstants.fileObjectSize);
        String fileName = NotesStringUtils.fromLMBCS(fileNamePtr, fileNameLength);
        
        JNAAttachment attInfo = new JNAAttachment(fileName, compression, flags, fileSize,
            fileCreatedWrap, fileModifiedWrap, this,
            itemBlockId, rrv);
        
        return Arrays.asList((Object) attInfo);
		  }
		  case ASSIST_RUNDATA: {
		    Optional<DesignAgent.LastRunInfo> info = AgentRunInfoDecoder.decodeAgentRunInfo(getParentDatabase(), valueDataPtr, valueDataLength);
		    return info.isPresent() ? Arrays.asList(info) : Collections.emptyList();
		  }
		  default:
	      //TODO add support for other object types
		    break;
			}
			
			//clone values because value data gets unlocked, preventing invalid memory access
			NotesObjectDescriptorStruct clonedObjDescriptor = NotesObjectDescriptorStruct.newInstance();
			clonedObjDescriptor.ObjectType = objDescriptor.getObjectType().map(t -> t.getValue()).orElse((short)0);
			clonedObjDescriptor.RRV = objDescriptor.getRRV();
			return Arrays.asList((Object) clonedObjDescriptor);
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_NOTEREF_LIST.getValue()) {
			int numEntries = valueDataPtr.getShort(0) & 0xffff;
			
			//skip LIST structure, clone data to prevent invalid memory access when buffer gets disposed
			valueDataPtr = valueDataPtr.share(2);
			
			List<Object> unids = new ArrayList<>();
			
			for (int i=0; i<numEntries; i++) {
				byte[] unidBytes = valueDataPtr.getByteArray(0, JNANotesConstants.notesUniversalNoteIdSize);
				Memory unidMem = new Memory(JNANotesConstants.notesUniversalNoteIdSize);
				unidMem.write(0, unidBytes, 0, unidBytes.length);
				NotesUniversalNoteIdStruct unidStruct = NotesUniversalNoteIdStruct.newInstance(unidMem);
				unidStruct.read();
				DominoUniversalNoteId unid = new JNADominoUniversalNoteId(unidStruct);
				unids.add(unid);
				
				valueDataPtr = valueDataPtr.share(JNANotesConstants.notesUniversalNoteIdSize);
			}
			return unids;
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_COLLATION.getValue()) {
      ByteBuffer data = valueDataPtr.getByteBuffer(0, valueDataLength);

      DominoCollationInfo collateInfo = CollationDecoder.decodeCollation(data);
			return Arrays.asList((Object) collateInfo);
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_VIEW_FORMAT.getValue()) {
	    ByteBuffer data = valueDataPtr.getByteBuffer(0, valueDataLength);

			DominoViewFormat viewFormatInfo = ViewFormatDecoder.decodeViewFormat(this, data);
			return Arrays.asList((Object) viewFormatInfo);
		} else if (dataTypeAsInt == ItemDataType.TYPE_CALENDAR_FORMAT.getValue()) {
      ByteBuffer data = valueDataPtr.getByteBuffer(0, valueDataLength);
      
		  DominoCalendarFormat calendarFormat = ViewFormatDecoder.decodeCalendarFormat(data);
		  return Arrays.asList((Object)calendarFormat);
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_FORMULA.getValue()) {
			boolean isSelectionFormula = DesignConstants.VIEW_FORMULA_ITEM.equalsIgnoreCase(itemName) && getDocumentClass().contains(DocumentClass.VIEW);
			
			DHANDLE.ByReference rethFormulaText = DHANDLE.newInstanceByReference();
			ShortByReference retFormulaTextLength = new ShortByReference();

			short result = NotesCAPI.get().NSFFormulaDecompile(valueDataPtr, isSelectionFormula,
					rethFormulaText, retFormulaTextLength);
			NotesErrorUtils.checkResult(result);
			
			return LockUtil.lockHandle(rethFormulaText, (hFormulaTextByVal) -> {
				Pointer formulaPtr = Mem.OSLockObject(hFormulaTextByVal);
				try {
					int textLen = retFormulaTextLength.getValue() & 0xffff;
					String formula = NotesStringUtils.fromLMBCS(formulaPtr, textLen);
					return Arrays.asList((Object) formula);
				}
				finally {
					Mem.OSUnlockObject(hFormulaTextByVal);
					short localResult = Mem.OSMemFree(hFormulaTextByVal);
					NotesErrorUtils.checkResult(localResult);
				}
			});
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_UNAVAILABLE.getValue()) {
			return Collections.emptyList();
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_MIME_PART.getValue()) {
			NotesMIMEPartStruct mimePartStruct = NotesMIMEPartStruct.newInstance(valueDataPtr);
			mimePartStruct.read();
			
			int iByteCount = mimePartStruct.wByteCount & 0xffff;
			int iBoundaryLen = mimePartStruct.wBoundaryLen & 0xffff;
			int iHeadersLen = mimePartStruct.wHeadersLen & 0xffff;
			
			Pointer mimeBoundaryStrPtr = valueDataPtr.share(JNANotesConstants.mimePartSize);
			String boundaryStr = NotesStringUtils.fromLMBCS(mimeBoundaryStrPtr, iBoundaryLen);
			while (boundaryStr.startsWith("\r\n")) { //$NON-NLS-1$
				boundaryStr = boundaryStr.substring(2);
			}
			while (boundaryStr.endsWith("\r\n")) { //$NON-NLS-1$
				boundaryStr = boundaryStr.substring(0, boundaryStr.length()-2);
			}

			Pointer mimeHeadersPtr = mimeBoundaryStrPtr.share(mimePartStruct.wBoundaryLen & 0xffff);
			String headers = NotesStringUtils.fromLMBCS(mimeHeadersPtr, iHeadersLen);

			Pointer mimeDataPtr = mimeHeadersPtr.share(mimePartStruct.wHeadersLen & 0xffff);
			byte[] data = mimeDataPtr.getByteArray(0, iByteCount - iBoundaryLen - iHeadersLen);
			
			EnumSet<MimePartOptions> options = EnumSet.noneOf(MimePartOptions.class);
			
			for (MimePartOptions currOpt : MimePartOptions.values()) {
				if ((mimePartStruct.dwFlags & currOpt.getValue()) == currOpt.getValue()) {
					options.add(currOpt);
				}
			}
			
			byte cPartType = mimePartStruct.cPartType;
			PartType partType;
			if (cPartType==NotesConstants.MIME_PART_PROLOG) {
				partType = PartType.PROLOG;
			}
			else if (cPartType==NotesConstants.MIME_PART_BODY) {
				partType = PartType.BODY;
			}
			else if (cPartType==NotesConstants.MIME_PART_EPILOG) {
				partType = PartType.EPILOG;
			}
			else if (cPartType==NotesConstants.MIME_PART_RETRIEVE_INFO) {
				partType = PartType.RETRIEVE_INFO;
			}
			else if (cPartType==NotesConstants.MIME_PART_MESSAGE) {
				partType = PartType.MESSAGE;
			}
			else {
				partType = null;
			}
			NotesMIMEPart mimePart = new NotesMIMEPart(this, options, partType, boundaryStr, headers, data);
			return Arrays.asList((Object) mimePart);
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_RFC822_TEXT.getValue()) {
			// Read in the byte data and delegate to InternetHeaders
			ByteBuffer buf = valueDataPtr.getByteBuffer(0, valueDataLength);
			RFC822ItemDesc itemDesc = MemoryStructureUtil.forStructure(RFC822ItemDesc.class, () -> buf);
			int bodyOffset = 14;
			
			// Skip over NotesNative value
			bodyOffset += itemDesc.getNotesNativeLength();
			
			// Read the expected header name
			String inetName = NotesStringUtils.fromLMBCS(valueDataPtr.share(bodyOffset), itemDesc.getNameLength());
			
			int dataLen = itemDesc.getNameLength()+itemDesc.getDelimiterLength()+itemDesc.getBodyLength();
			
			// Read in the item data
			byte[] headerBytes = new byte[dataLen];
			valueDataPtr.read(bodyOffset, headerBytes, 0, dataLen);
			InternetHeaders headerHolder;
			try(InputStream is = new ByteArrayInputStream(headerBytes)) {
				headerHolder = new InternetHeaders(is);
			} catch (MessagingException | IOException e) {
				throw new DominoException("Exception while translating RFC 822 text", e);
			}
			String[] headers = headerHolder.getHeader(inetName);
			return headers == null ? Collections.emptyList() : Arrays.stream(headers).collect(Collectors.toList());
		}
		else if(dataTypeAsInt == ItemDataType.TYPE_COMPOSITE.getValue()) {
			@SuppressWarnings("unchecked")
			List<Object> result = (List<Object>)(List<?>)getRichTextItem(itemName);
			return result;
		}
		else if (dataTypeAsInt == ItemDataType.TYPE_USERID.getValue()) {
		  PreV3Author result = NotesItemDataUtil.parsePreV3Author(valueDataPtr.getByteBuffer(0, valueDataLength));
		  return Arrays.asList((Object)result);
		}
		else {
			throw new DominoException(format("Data type for value of item {0} is currently unsupported: {1}", itemName, dataTypeAsInt));
		}
	}

	@Override
	public int getNoteID() {
		checkDisposed();
		JNADocumentAllocations allocations = getAllocations();
		
		DisposableMemory retNoteId = new DisposableMemory(4);
		try {
			retNoteId.clear();

			return LockUtil.lockHandle(allocations.getNoteHandle(), (handleByVal) -> {
				NotesCAPI.get().NSFNoteGetInfo(handleByVal, NotesConstants._NOTE_ID, retNoteId);
				return retNoteId.getInt(0);
			});
		}
		finally {
			retNoteId.dispose();
		}
	}
	
	@Override
	public Optional<String> getThreadID() {
	  String id = get(NotesConstants.ITEM_THREAD_ID, String.class, ""); //$NON-NLS-1$
	  if(id != null && !id.isEmpty()) {
	    return Optional.of(id);
	  } else {
	    return Optional.empty();
	  }
	}

	@Override
	public String getUNID() {
		NotesOriginatorIdStruct oid = getOIDStruct();
		String unid = oid.getUNIDAsString();
		return unid;
	}

	@Override
	public int getSequenceNumber() {
		return getOIDStruct().Sequence;
	}

	/**
	 * Internal method to get the populated {@link NotesOriginatorIdStruct} object
	 * for this note
	 * 
	 * @return oid structure
	 */
	private NotesOriginatorIdStruct getOIDStruct() {
		checkDisposed();
		JNADocumentAllocations allocations = getAllocations();

		return LockUtil.lockHandle(allocations.getNoteHandle(), (handleByVal) -> {
			Memory retOid = new Memory(JNANotesConstants.oidSize);
			retOid.clear();
			
			NotesCAPI.get().NSFNoteGetInfo(handleByVal, NotesConstants._NOTE_OID, retOid);
			
			NotesOriginatorIdStruct oidStruct = NotesOriginatorIdStruct.newInstance(retOid);
			oidStruct.read();
			return oidStruct;
		});
	}

	@Override
	public DominoOriginatorId getOID() {
		NotesOriginatorIdStruct oidStruct = getOIDStruct();
		JNADominoOriginatorId oid = new JNADominoOriginatorId(oidStruct);
		return oid;
	}
	
	@Override
	public DominoDateTime getCreated() {
		checkDisposed();
		
		DominoDateTime creationDate = get("$CREATED", DominoDateTime.class, null); //$NON-NLS-1$
		if (creationDate!=null) {
			return creationDate;
		}
		
		NotesOriginatorIdStruct oidStruct = getOIDStruct();
		NotesTimeDateStruct creationDateStruct = oidStruct.Note;
		return new JNADominoDateTime(creationDateStruct.Innards);
	}

	@Override
	public DominoDateTime getLastModified() {
		DominoOriginatorId oid = getOID();
		return oid.getSequenceTime();
	}

	@Override
	public DominoDateTime getModifiedInThisFile() {
		checkDisposed();
		JNADocumentAllocations allocations = getAllocations();
		
		DisposableMemory retTimeDate = new DisposableMemory(JNANotesConstants.timeDateSize);
		try {
			retTimeDate.clear();

			return LockUtil.lockHandle(allocations.getNoteHandle(), (handleByVal) -> {
				NotesCAPI.get().NSFNoteGetInfo(handleByVal, NotesConstants._NOTE_MODIFIED, retTimeDate);
				NotesTimeDateStruct td = NotesTimeDateStruct.newInstance(retTimeDate);
				td.read();
				return new JNADominoDateTime(td.Innards);

			});
		}
		finally {
			retTimeDate.dispose();
		}
	}

	@Override
	public DominoDateTime getLastAccessed() {
		checkDisposed();
		JNADocumentAllocations allocations = getAllocations();
		
		DisposableMemory retTimeDate = new DisposableMemory(JNANotesConstants.timeDateSize);
		try {
			retTimeDate.clear();

			return LockUtil.lockHandle(allocations.getNoteHandle(), (handleByVal) -> {
				NotesCAPI.get().NSFNoteGetInfo(handleByVal, NotesConstants._NOTE_ACCESSED, retTimeDate);
				NotesTimeDateStruct td = NotesTimeDateStruct.newInstance(retTimeDate);
				td.read();
				return new JNADominoDateTime(td.Innards);

			});
		}
		finally {
			retTimeDate.dispose();
		}
	}

	@Override
	public DominoDateTime getAddedToFile() {
		checkDisposed();
		JNADocumentAllocations allocations = getAllocations();
		
		DisposableMemory retTimeDate = new DisposableMemory(JNANotesConstants.timeDateSize);
		try {
			retTimeDate.clear();

			return LockUtil.lockHandle(allocations.getNoteHandle(), (handleByVal) -> {
				NotesCAPI.get().NSFNoteGetInfo(handleByVal, NotesConstants._NOTE_ADDED_TO_FILE, retTimeDate);
				NotesTimeDateStruct td = NotesTimeDateStruct.newInstance(retTimeDate);
				td.read();
				return new JNADominoDateTime(td.Innards);
			});
		}
		finally {
			retTimeDate.dispose();
		}
	}
	
	@Override
	public Optional<Item> getFirstItem(String itemName) {
		Objects.requireNonNull(itemName, "Item name cannot be null. Use getItems() instead.");
		
		final Item[] retItem = new Item[1];
		
		getItems(itemName, new IItemCallback() {

			@Override
			public void itemNotFound() {
				retItem[0]=null;
			}

			@Override
			public Action itemFound(Item itemInfo) {
				retItem[0] = itemInfo;
				return Action.Stop;
			}
		});
		
		return Optional.ofNullable(retItem[0]);
	
	}

	/**
	 * Callback interface for {@link JNADocument#getItems(IItemCallback)}
	 * 
	 * @author Karsten Lehmann
	 */
	private interface IItemCallback {
		public enum Action {Continue, Stop};
		
		/**
		 * Method is called when an item could not be found in the note
		 */
		default void itemNotFound() {};
		
		/**
		 * Method is called for each item in the note. A note may contain the same item name
		 * multiple times. In this case, the method is called for each item instance
		 * 
		 * @param item item object with meta data and access method to decode item value
		 * @return next action, either continue or stop scan
		 */
		Action itemFound(Item item);
	}

	private static class LoopImpl extends Loop {
		
		@Override
		public void setIndex(int index) {
			super.setIndex(index);
		}

		public void next() {
			super.setIndex(getIndex()+1);
		}

		@Override
		public void setIsLast() {
			super.setIsLast();
		}
	}
	

	@Override
	public Document forEachItem(BiConsumer<Item, Loop> consumer) {
		return forEachItem(null, consumer);
	}

	@Override
	public Document forEachItem(final String searchForItemName, BiConsumer<Item, Loop> consumer) {
		LoopImpl loop = new LoopImpl();
		
		AtomicInteger itemIdx = new AtomicInteger(-1);
		//to be able to report that the item is the last one, we need to prefetch one
		AtomicReference<Item> lastReadItem = new AtomicReference<>();
		
		getItems(searchForItemName, item -> {
			if (itemIdx.get() == -1) {
				//first match
				lastReadItem.set(item);
			}
			else {
				//report last read
				consumer.accept(lastReadItem.get(), loop);
				//store item for next loop run
				lastReadItem.set(item);
			}
			
			loop.setIndex(itemIdx.incrementAndGet());
			
			if (loop.isStopped()) {
				return IItemCallback.Action.Stop;
			}
			else {
				return IItemCallback.Action.Continue;
			}
		});
		
		//report last item
		Item lastItem = lastReadItem.get();
		if (lastItem != null && !loop.isStopped()) {
			loop.setIsLast();
			
			consumer.accept(lastItem, loop);
		}
		return this;
	}
	
	@Override
	public Stream<Item> allItems() {
		List<Item> items = new ArrayList<>();
		getItems(null, item -> {
			items.add(item);
			return IItemCallback.Action.Continue;
		});
		
		return items.stream();
	}


	
	/**
	 * Scans through all items of this note that have the specified name
	 * 
	 * @param searchForItemName item name to search for or null to scan through all items
	 * @param callback callback is called for each scan result
	 */
	private void getItems(final String searchForItemName, final IItemCallback callback) {
		checkDisposed();
		
		Memory itemNameMem = StringUtil.isEmpty(searchForItemName) ? null : NotesStringUtils.toLMBCS(searchForItemName, false);
		
		NotesBlockIdStruct.ByReference itemBlockId = NotesBlockIdStruct.ByReference.newInstance();
		NotesBlockIdStruct.ByReference valueBlockId = NotesBlockIdStruct.ByReference.newInstance();
		ShortByReference retDataType = new ShortByReference();
		IntByReference retValueLen = new IntByReference();
		
		JNADocumentAllocations allocations = getAllocations();
		short result = LockUtil.lockHandle(allocations.getNoteHandle(), (noteHandleByVal) -> {
			return NotesCAPI.get().NSFItemInfo(noteHandleByVal, itemNameMem,
					itemNameMem==null ? 0 : (short) (itemNameMem.size() & 0xffff),
					itemBlockId, retDataType, valueBlockId, retValueLen);
		});
		
		if (result == INotesErrorConstants.ERR_ITEM_NOT_FOUND) {
			callback.itemNotFound();
			return;
		}

		NotesErrorUtils.checkResult(result);
		
		NotesBlockIdStruct itemBlockIdClone = NotesBlockIdStruct.newInstance();
		itemBlockIdClone.pool = itemBlockId.pool;
		itemBlockIdClone.block = itemBlockId.block;
		itemBlockIdClone.write();
		
		NotesBlockIdStruct valueBlockIdClone = NotesBlockIdStruct.newInstance();
		valueBlockIdClone.pool = valueBlockId.pool;
		valueBlockIdClone.block = valueBlockId.block;
		valueBlockIdClone.write();
		
		int dataType = retDataType.getValue();
		
		Item itemInfo = new JNAItem(this, itemBlockIdClone, dataType,
				valueBlockIdClone);
		
		IItemCallback.Action action = callback.itemFound(itemInfo);
		if (action != IItemCallback.Action.Continue) {
			return;
		}
		
		while (true) {
			IntByReference retNextValueLen = new IntByReference();
			
			NotesBlockIdStruct.ByValue itemBlockIdByVal = NotesBlockIdStruct.ByValue.newInstance();
			itemBlockIdByVal.pool = itemBlockId.pool;
			itemBlockIdByVal.block = itemBlockId.block;
			
			result = LockUtil.lockHandle(allocations.getNoteHandle(), (noteHandleByVal) -> {
				return  NotesCAPI.get().NSFItemInfoNext(noteHandleByVal, itemBlockIdByVal,
						itemNameMem, itemNameMem==null ? 0 : (short) (itemNameMem.size() & 0xffff), itemBlockId, retDataType,
						valueBlockId, retNextValueLen);
			});
			
			if (result == INotesErrorConstants.ERR_ITEM_NOT_FOUND) {
				return;
			}

			NotesErrorUtils.checkResult(result);

			itemBlockIdClone = NotesBlockIdStruct.newInstance();
			itemBlockIdClone.pool = itemBlockId.pool;
			itemBlockIdClone.block = itemBlockId.block;
			itemBlockIdClone.write();
			
			valueBlockIdClone = NotesBlockIdStruct.newInstance();
			valueBlockIdClone.pool = valueBlockId.pool;
			valueBlockIdClone.block = valueBlockId.block;
			valueBlockIdClone.write();
			
			dataType = retDataType.getValue();

			itemInfo = new JNAItem(this, itemBlockIdClone, dataType,
					valueBlockIdClone);
			
			action = callback.itemFound(itemInfo);
			if (action != IItemCallback.Action.Continue) {
				return;
			}
		}
	}
	
	@Override
	public Optional<Attachment> getAttachment(String fileName) {
		final JNAAttachment[] foundAttInfo = new JNAAttachment[1];
		
		getItems("$file", new IItemCallback() { //$NON-NLS-1$
			
			@Override
			public void itemNotFound() {
			}
			
			@Override
			public Action itemFound(Item item) {
				List<Object> values = item.getValue();
				if (values!=null && !values.isEmpty() && values.get(0) instanceof JNAAttachment) {
					JNAAttachment attInfo = (JNAAttachment) values.get(0);
					if (attInfo.getFileName().equalsIgnoreCase(fileName)) {
						foundAttInfo[0] = attInfo;
						return Action.Stop;
					}
				}
				return Action.Continue;
			}
		});
		return Optional.ofNullable(foundAttInfo[0]);
	}

	@Override
	public Document forEachAttachment(BiConsumer<Attachment, Loop> consumer) {
		List<Attachment> attachments = new ArrayList<>();
		
		getItems("$file", new IItemCallback() { //$NON-NLS-1$
			
			@Override
			public void itemNotFound() {
			}
			
			@Override
			public Action itemFound(Item item) {
				List<Object> values = item.getValue();
				if (values!=null && !values.isEmpty() && values.get(0) instanceof Attachment) {
					Attachment attInfo = (Attachment) values.get(0);
					attachments.add(attInfo);
				}
				return Action.Continue;
			}
		});
		
		LoopImpl loop = new LoopImpl();
		Iterator<Attachment> attachmentsIt = attachments.iterator();
		
		while (attachmentsIt.hasNext() && !loop.isStopped()) {
			Attachment currAtt = attachmentsIt.next();
			
			if (!attachmentsIt.hasNext()) {
				loop.setIsLast();
			}
			consumer.accept(currAtt, loop);
			
			loop.next();
		}
		
		return this;
	}

	@Override
	public Document replaceItemValue(String itemName, Object value) {
		return replaceItemValue(itemName, EnumSet.of(ItemFlag.SUMMARY), value, false);
	}

	@Override
	public Document replaceItemValue(String itemName, Object value, boolean allowDataTypeChanges) {
		return replaceItemValue(itemName, EnumSet.of(ItemFlag.SUMMARY), value, allowDataTypeChanges);
	}

	@Override
	public Document replaceItemValue(String itemName, Set<ItemFlag> flags, Object value) {
		return replaceItemValue(itemName, flags, value, false);
	}

	@SuppressWarnings("rawtypes")
	private boolean hasSupportedItemObjectType(Object value) {
		if (value==null) {
			return true;
		}
		else if (value instanceof String) {
			return true;
		}
		else if (value instanceof Number) {
			return true;
		}
		else if (value instanceof Calendar || value instanceof Date || value instanceof Temporal) {
			return true;
		}
		else if (value instanceof Iterable && !((Iterable)value).iterator().hasNext()) {
			return true;
		}
		else if (value instanceof Iterable && isStringList((Iterable) value)) {
			return true;
		}
		else if (value instanceof Iterable && isNumberOrNumberArrayList((Iterable) value)) {
			return true;
		}
		else if (value instanceof Iterable && isCalendarOrCalendarArrayList((Iterable) value)) {
			return true;
		}
		else if (value instanceof DominoDateRange) {
			return true;
		}
		else if (value instanceof DominoUniversalNoteId) {
			return true;
		}
		else if (value instanceof JNAFormula) {
			return true;
		}
    else if (value instanceof DominoViewFormat) {
      return true;
    }
    else if (value instanceof DominoCalendarFormat) {
      return true;
    }
    else if (value instanceof DominoCollationInfo) {
      return true;
    }
		return false;
	}

	private boolean isNumberOrNumberArrayList(Iterable<?> list) {
		if (list==null || !list.iterator().hasNext()) {
			return false;
		}
		for (Object currObj : list) {
			boolean isAccepted=false;
			
			if (currObj instanceof double[]) {
				double[] valArr = (double[]) currObj;
				if (valArr.length==2) {
					isAccepted = true;
				}
			}
			if (currObj instanceof int[]) {
				int[] valArr = (int[]) currObj;
				if (valArr.length==2) {
					isAccepted = true;
				}
			}
			if (currObj instanceof long[]) {
				long[] valArr = (long[]) currObj;
				if (valArr.length==2) {
					isAccepted = true;
				}
			}
			if (currObj instanceof float[]) {
				float[] valArr = (float[]) currObj;
				if (valArr.length==2) {
					isAccepted = true;
				}
			}
			else if (currObj instanceof Number[]) {
				Number[] valArr = (Number[]) currObj;
				if (valArr.length==2) {
					isAccepted = true;
				}
			}
			else if (currObj instanceof Integer[]) {
				Integer[] valArr = (Integer[]) currObj;
				if (valArr.length==2) {
					isAccepted = true;
				}
			}
			else if (currObj instanceof Long[]) {
				Long[] valArr = (Long[]) currObj;
				if (valArr.length==2) {
					isAccepted = true;
				}
			}
			else if (currObj instanceof Double[]) {
				Double[] valArr = (Double[]) currObj;
				if (valArr.length==2) {
					isAccepted = true;
				}
			}
			else if (currObj instanceof Float[]) {
				Float[] valArr = (Float[]) currObj;
				if (valArr.length==2) {
					isAccepted = true;
				}
			}
			else if (currObj instanceof Number) {
				isAccepted = true;
			}
			
			if (!isAccepted) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isStringList(Iterable<?> list) {
		if (list==null || !list.iterator().hasNext()) {
			return false;
		}
		if(!StreamSupport.stream(list.spliterator(), false).allMatch(String.class::isInstance)) {
		  return false;
		}
		return true;
	}

	private boolean isCalendarOrCalendarArrayList(Iterable<?> list) {
		if (list==null || !list.iterator().hasNext()) {
			return false;
		}
		for(Object currObj : list) {
			boolean isAccepted=false;
			
			if (currObj instanceof Calendar[]) {
				Calendar[] calArr = (Calendar[]) currObj;
				if (calArr.length==2) {
					isAccepted = true;
				}
			}
			else if (currObj instanceof Date[]) {
				Date[] dateArr = (Date[]) currObj;
				if (dateArr.length==2) {
					isAccepted = true;
				}
			}
			else if (currObj instanceof DominoDateTime[]) {
				DominoDateTime[] ndtArr = (DominoDateTime[]) currObj;
				if (ndtArr.length==2) {
					isAccepted = true;
				}
			}
			else if (currObj instanceof Calendar) {
				isAccepted = true;
			}
			else if (currObj instanceof Date) {
				isAccepted = true;
			}
			else if (currObj instanceof DominoDateTime) {
				isAccepted = true;
			}
			else if (currObj instanceof DominoDateRange) {
				isAccepted = true;
			}
			
			if (!isAccepted) {
				return false;
			}
		}
		return true;
	}

	private static String dumpValueType(Object value) {
		if (value instanceof List) {
			List<?> valueList = (List<?>) value;
			StringBuilder sb = new StringBuilder();
			sb.append(value.getClass().getName()).append(" ["); //$NON-NLS-1$
			for (int i=0; i<valueList.size(); i++) {
				if (i>0) {
					sb.append(", "); //$NON-NLS-1$
				}
				sb.append(dumpValueType(valueList.get(i)));
			}
			sb.append("]"); //$NON-NLS-1$
			return sb.toString();
		}
		else if (value!=null) {
			return value.getClass().getName();
		}
		else {
			return "null"; //$NON-NLS-1$
		}
	}

	@Override
	public Document replaceItemValue(String itemName, Set<ItemFlag> flags, Object value, boolean allowDataTypeChanges) {
		DocumentValueConverter converter = null;
		
		if (!hasSupportedItemObjectType(value)) {
			converter = JNXServiceFinder.findServices(DocumentValueConverter.class)
				.filter(c -> c.supportsWrite(value.getClass(), value))
				.sorted(Comparator.comparing(DocumentValueConverter::getPriority).reversed())
				.findFirst()
				.orElse(null);

			if (converter==null) {
				throw new IllegalArgumentException(format("Unsupported value type: {0}", dumpValueType(value)));
			}
		}
		
		while (hasItem(itemName)) {
			removeItem(itemName);
		}
		
		if (value!=null) {
			return appendItemValue(itemName, flags, value, converter, allowDataTypeChanges);
		}
		else {
			return this;
		}
	}

	private List<?> toNumberOrNumberArrayList(Iterable<?> list) {
		boolean allNumbers = StreamSupport.stream(list.spliterator(), false)
		    .allMatch(i -> i instanceof double[] || i instanceof Double);
		
		if (allNumbers) {
			return StreamSupport.stream(list.spliterator(), false).collect(Collectors.toList());
		}
		
		List<Object> convertedList = new ArrayList<>();
		for (Object obj : list) {
			if (obj instanceof Number) {
				//ok
				convertedList.add(((Number)obj).doubleValue());
			}
			else if (obj instanceof double[]) {
				if (((double[])obj).length!=2) {
					throw new IllegalArgumentException("Length of double array entry must be 2 for number ranges");
				}
				//ok
				convertedList.add(obj);
			}
			else if (obj instanceof Number[]) {
				Number[] numberArr = (Number[]) obj;
				if (numberArr.length!=2) {
					throw new IllegalArgumentException("Length of Number array entry must be 2 for number ranges");
				}
				
				convertedList.add(new double[] {
						numberArr[0].doubleValue(),
						numberArr[1].doubleValue()
				});
			}
			else if (obj instanceof Double[]) {
				Double[] doubleArr = (Double[]) obj;
				if (doubleArr.length!=2) {
					throw new IllegalArgumentException("Length of Number array entry must be 2 for number ranges");
				}
				
				convertedList.add(new double[] {
						doubleArr[0],
						doubleArr[1]
				});
			}
			else if (obj instanceof Integer[]) {
				Integer[] integerArr = (Integer[]) obj;
				if (integerArr.length!=2) {
					throw new IllegalArgumentException("Length of Integer array entry must be 2 for number ranges");
				}
				
				convertedList.add(new double[] {
						integerArr[0].doubleValue(),
						integerArr[1].doubleValue()
				});
			}
			else if (obj instanceof Long[]) {
				Long[] longArr = (Long[]) obj;
				if (longArr.length!=2) {
					throw new IllegalArgumentException("Length of Long array entry must be 2 for number ranges");
				}
				
				convertedList.add(new double[] {
						longArr[0].doubleValue(),
						longArr[1].doubleValue()
				});
			}
			else if (obj instanceof Float[]) {
				Float[] floatArr = (Float[]) obj;
				if (floatArr.length!=2) {
					throw new IllegalArgumentException("Length of Float array entry must be 2 for number ranges");
				}
				
				convertedList.add(new double[] {
						floatArr[0].doubleValue(),
						floatArr[1].doubleValue()
				});
			}
			else if (obj instanceof int[]) {
				int[] intArr = (int[]) obj;
				if (intArr.length!=2) {
					throw new IllegalArgumentException("Length of int array entry must be 2 for number ranges");
				}
				
				convertedList.add(new double[] {
						intArr[0],
						intArr[1]
				});
			}
			else if (obj instanceof long[]) {
				long[] longArr = (long[]) obj;
				if (longArr.length!=2) {
					throw new IllegalArgumentException("Length of long array entry must be 2 for number ranges");
				}
				
				convertedList.add(new double[] {
						longArr[0],
						longArr[1]
				});
			}
			else if (obj instanceof float[]) {
				float[] floatArr = (float[]) obj;
				if (floatArr.length!=2) {
					throw new IllegalArgumentException("Length of float array entry must be 2 for number ranges");
				}
				
				convertedList.add(new double[] {
						floatArr[0],
						floatArr[1]
				});
			}
			else {
				throw new IllegalArgumentException(format("Unsupported date format found in list: {0}", (obj==null ? "null" : obj.getClass().getName()))); //$NON-NLS-2$
			}
		}
		return convertedList;
	}
	
	private List<?> toDateTimeOrDateTimeRangeList(Iterable<?> list) {
		boolean allDateTime = StreamSupport.stream(list.spliterator(), false)
		    .allMatch(i -> i instanceof DominoDateTime[] || i instanceof DominoDateTime);
		
		if (allDateTime) {
			return StreamSupport.stream(list.spliterator(), false).collect(Collectors.toList());
		}
		
		List<Object> convertedList = new ArrayList<>();
		for (Object obj : list) {
			if (obj instanceof GregorianCalendar) {
				convertedList.add(new JNADominoDateTime(((GregorianCalendar) obj).toZonedDateTime()));
			}
			else if (obj instanceof Calendar[]) {
				Calendar[] calArr = (Calendar[]) obj;
				if (calArr.length!=2) {
					throw new IllegalArgumentException("Length of Calendar array entry must be 2 for date ranges");
				}
				DominoDateTime start = new JNADominoDateTime((((GregorianCalendar)calArr[0]).toZonedDateTime()));
				DominoDateTime end = new JNADominoDateTime((((GregorianCalendar)calArr[1]).toZonedDateTime()));
				convertedList.add(new DefaultDominoDateRange(start, end));
			}
			else if (obj instanceof Date) {
				Date dt = (Date) obj;
				convertedList.add(new JNADominoDateTime(dt.getTime()));
			}
			else if (obj instanceof DominoDateTime) {
				convertedList.add(obj);
			}
			else if (obj instanceof Date[]) {
				Date[] dateArr = (Date[]) obj;
				if (dateArr.length!=2) {
					throw new IllegalArgumentException("Length of Date array entry must be 2 for date ranges");
				}
				DominoDateTime start = new JNADominoDateTime(dateArr[0].getTime());
				DominoDateTime end = new JNADominoDateTime(dateArr[1].getTime());
				convertedList.add(new DefaultDominoDateRange(start, end));
			}
			else if (obj instanceof DominoDateTime[]) {
				DominoDateTime[] ntdArr = (DominoDateTime[]) obj;
				if (ntdArr.length!=2) {
					throw new IllegalArgumentException("Length of DominoDateTime array entry must be 2 for date ranges");
				}
				convertedList.add(new DefaultDominoDateRange(ntdArr[0], ntdArr[1]));
			}
			else if(obj instanceof DominoDateRange) {
				DominoDateRange range = (DominoDateRange)obj;
				convertedList.add(new DefaultDominoDateRange(range.getStartDateTime(), range.getEndDateTime()));
			}
			else {
				throw new IllegalArgumentException(format("Unsupported date format found in list: {0}", (obj==null ? "null" : obj.getClass().getName()))); //$NON-NLS-2$
			}
		}
		return convertedList;
	}
	
	private ThreadLocal<Set<Class<?>>> writingItemType = ThreadLocal.withInitial(HashSet::new);

	@Override
	public Document appendItemValue(String itemName, Object value) {
		return appendItemValue(itemName, EnumSet.of(ItemFlag.SUMMARY), value, false);
	}

	@Override
	public Document appendItemValue(String itemName, Set<ItemFlag> flags, Object value) {
		return appendItemValue(itemName, flags, value, false);
	}
	
	@Override
	public Document appendItemValue(String itemName, Set<ItemFlag> flags, Object value, boolean allowDataTypeChanges) {
		DocumentValueConverter converter = null;
		
		if (!hasSupportedItemObjectType(value)) {
			converter = JNXServiceFinder.findServices(DocumentValueConverter.class)
				.filter(c -> c.supportsWrite(value.getClass(), value))
				.sorted(Comparator.comparing(DocumentValueConverter::getPriority).reversed())
				.findFirst()
				.orElse(null);

			if (converter==null) {
				throw new IllegalArgumentException(format("Unsupported value type: {0}", dumpValueType(value)));
			}
		}
		
		return appendItemValue(itemName, flags, value, converter, true);
	}
	
	@SuppressWarnings("deprecation")
	private Document appendItemValue(String itemName, Set<ItemFlag> flagsOrig,
			Object value, DocumentValueConverter valueConverter, boolean allowDataTypeChanges) {
		
		checkDisposed();

		//remove our own pseudo flags:
		boolean keepLineBreaks = flagsOrig.contains(ItemFlag.KEEPLINEBREAKS);
		EnumSet<ItemFlag> flags = EnumSet.copyOf(flagsOrig);
		flags.remove(ItemFlag.KEEPLINEBREAKS);

		if (value instanceof JNAFormula) {
			//formulas are stored in compiled binary format
			flags.remove(ItemFlag.SUMMARY);
		}
		else if (value instanceof DominoViewFormat) {
		  flags.add(ItemFlag.SUMMARY);
		  flags.add(ItemFlag.SIGNED);
		}
		
		if (value instanceof String) {
			Memory strValueMem;
			if (keepLineBreaks) {
				strValueMem = NotesStringUtils.toLMBCS((String)value, false, false);
			}
			else {
				strValueMem = NotesStringUtils.toLMBCS((String)value, false);
			}

			int valueSize = (int) (2 + (strValueMem==null ? 0 : strValueMem.size()));
			
			DHANDLE.ByReference rethItem = DHANDLE.newInstanceByReference();
			short result = Mem.OSMemAlloc((short) 0, valueSize, rethItem);
			NotesErrorUtils.checkResult(result);
			
			return LockUtil.lockHandle(rethItem, (hItemByVal) -> {
				Pointer valuePtr = Mem.OSLockObject(hItemByVal);
				
				try {
					valuePtr.setShort(0, ItemDataType.TYPE_TEXT.getValue().shortValue());
					valuePtr = valuePtr.share(2);
					if (strValueMem!=null) {
						valuePtr.write(0, strValueMem.getByteArray(0, (int) strValueMem.size()), 0, (int) strValueMem.size());
					}
					return appendItemValue(itemName, flags, ItemDataType.TYPE_TEXT.getValue(), hItemByVal, valueSize);
				}
				finally {
					Mem.OSUnlockObject(hItemByVal);
				}
			});
		
		}
		else if (value instanceof Number) {
			int valueSize = 2 + 8;
			
			DHANDLE.ByReference rethItem = DHANDLE.newInstanceByReference();
			short result = Mem.OSMemAlloc((short) 0, valueSize, rethItem);
			NotesErrorUtils.checkResult(result);
			
			return LockUtil.lockHandle(rethItem, (hItemByVal) -> {
				Pointer valuePtr = Mem.OSLockObject(hItemByVal);
				
				try {
					valuePtr.setShort(0, ItemDataType.TYPE_NUMBER.getValue().shortValue());
					valuePtr = valuePtr.share(2);
					valuePtr.setDouble(0, ((Number)value).doubleValue());
					return appendItemValue(itemName, flags, ItemDataType.TYPE_NUMBER.getValue(), hItemByVal, valueSize);
				}
				finally {
					Mem.OSUnlockObject(hItemByVal);
				}
			});
		}
		else if (value instanceof Calendar || value instanceof Temporal || value instanceof Date) {
			int[] innards;
			
			if (value instanceof DominoDateTime) {
				//no date conversion to innards needing, we already have them
				innards = ((DominoDateTime)value).getAdapter(int[].class);
			}
			else if (value instanceof Calendar) {
				Calendar calValue = (Calendar) value;
				innards = NotesDateTimeUtils.calendarToInnards(calValue);
			}
			else if (value instanceof Date) {
				innards = new JNADominoDateTime(((Date)value).toInstant()).getInnards();
			}
			else if(value instanceof Temporal) {
				innards = new JNADominoDateTime((Temporal)value).getInnards();
			}
			else {
				throw new UnsupportedItemValueError(format("Unsupported value type: {0}", (value==null ? "null" : value.getClass().getName()))); //$NON-NLS-2$
			}

			int valueSize = 2 + 8;
			
			DHANDLE.ByReference rethItem = DHANDLE.newInstanceByReference();
			short result = Mem.OSMemAlloc((short) 0, valueSize, rethItem);
			NotesErrorUtils.checkResult(result);
			
			return LockUtil.lockHandle(rethItem, (hItemByVal) -> {
				Pointer valuePtr = Mem.OSLockObject(hItemByVal);
				
				try {
					valuePtr.setShort(0, ItemDataType.TYPE_TIME.getValue().shortValue());
					valuePtr = valuePtr.share(2);

					NotesTimeDateStruct timeDate = NotesTimeDateStruct.newInstance(valuePtr);
					timeDate.Innards[0] = innards[0];
					timeDate.Innards[1] = innards[1];
					timeDate.write();

					return appendItemValue(itemName, flags, ItemDataType.TYPE_TIME.getValue(), hItemByVal, valueSize);
				}
				finally {
					Mem.OSUnlockObject(hItemByVal);
				}
			});
		}
		else if (value instanceof Iterable && (!((Iterable<?>)value).iterator().hasNext() || isStringList((Iterable<?>) value))) {
			@SuppressWarnings("unchecked")
			List<String> strList = StreamSupport.stream(((Iterable<String>) value).spliterator(), false)
			  .collect(Collectors.toList());
			
			if (strList.size()> 65535) {
				throw new IllegalArgumentException(format("String list size must fit in a WORD ({0}>65535)", strList.size()));
			}
			
			DHANDLE.ByReference rethList = DHANDLE.newInstanceByReference();
			ShortByReference retListSize = new ShortByReference();
			Memory retpList = new Memory(Native.POINTER_SIZE);

			short result = NotesCAPI.get().ListAllocate((short) 0, 
					(short) 0,
					1, rethList, retpList, retListSize);
			
			NotesErrorUtils.checkResult(result);

			return LockUtil.lockHandle(rethList, (hListByVal) -> {
				Mem.OSUnlockObject(hListByVal);
				
				int i = 0;
				for(String currStr : strList) {
					Memory currStrMem = NotesStringUtils.toLMBCS(currStr, false);

					short localResult = NotesCAPI.get().ListAddEntry(hListByVal, 1, retListSize, (short) (i & 0xffff), currStrMem,
							(short) (currStrMem==null ? 0 : (currStrMem.size() & 0xffff)));
					NotesErrorUtils.checkResult(localResult);
					i++;
				}
				
				int listSize = retListSize.getValue() & 0xffff;
				
				@SuppressWarnings("unused")
				Pointer valuePtr = Mem.OSLockObject(hListByVal);
				try {
					return appendItemValue(itemName, flags, ItemDataType.TYPE_TEXT_LIST.getValue(), hListByVal, listSize);
				}
				finally {
					Mem.OSUnlockObject(hListByVal);
				}
			});
		}
		else if (value instanceof Iterable && isNumberOrNumberArrayList((Iterable<?>) value)) {
		  List<?> numberOrNumberArrList = toNumberOrNumberArrayList((Iterable<?>) value);
			
			List<Number> numberList = new ArrayList<>();
			List<double[]> numberArrList = new ArrayList<>();
			
			for (int i=0; i<numberOrNumberArrList.size(); i++) {
				Object currObj = numberOrNumberArrList.get(i);
				if (currObj instanceof Double) {
					numberList.add((Number) currObj);
				}
				else if (currObj instanceof double[]) {
					numberArrList.add((double[])currObj);
				}
			}
			
			if (numberList.size()> 65535) {
				throw new IllegalArgumentException(format("Number list size must fit in a WORD ({0}>65535)", numberList.size()));
			}

			if (numberArrList.size()> 65535) {
				throw new IllegalArgumentException(format("Number range list size must fit in a WORD ({0}>65535)", numberList.size()));
			}

			int valueSize = 2 + JNANotesConstants.rangeSize + 
					8 * numberList.size() +
					JNANotesConstants.numberPairSize * numberArrList.size();


			DHANDLE.ByReference rethItem = DHANDLE.newInstanceByReference();
			short result = Mem.OSMemAlloc((short) 0, valueSize, rethItem);
			NotesErrorUtils.checkResult(result);
			
			return LockUtil.lockHandle(rethItem, (hItemByVal) -> {
				Pointer valuePtr = Mem.OSLockObject(hItemByVal);
				
				try {
					valuePtr.setShort(0, ItemDataType.TYPE_NUMBER_RANGE.getValue().shortValue());
					valuePtr = valuePtr.share(2);
					
					Pointer rangePtr = valuePtr;
					NotesRangeStruct range = NotesRangeStruct.newInstance(rangePtr);
					range.ListEntries = (short) (numberList.size() & 0xffff);
					range.RangeEntries = (short) (numberArrList.size() & 0xffff);
					range.write();

					Pointer doubleListPtr = rangePtr.share(JNANotesConstants.rangeSize);
					
					for (int i=0; i<numberList.size(); i++) {
						doubleListPtr.setDouble(0, numberList.get(i).doubleValue());
						doubleListPtr = doubleListPtr.share(8);
					}

					Pointer doubleArrListPtr = doubleListPtr;
					
					for (int i=0; i<numberArrList.size(); i++) {
						double[] currNumberArr = numberArrList.get(i);
						
						NotesNumberPairStruct numberPair = NotesNumberPairStruct.newInstance(doubleArrListPtr);
						numberPair.Lower = currNumberArr[0];
						numberPair.Upper = currNumberArr[1];
						numberPair.write();

						doubleArrListPtr = doubleArrListPtr.share(JNANotesConstants.numberPairSize);
					}
					
					return appendItemValue(itemName, flags, ItemDataType.TYPE_NUMBER_RANGE.getValue(), hItemByVal,
							valueSize);
				}
				finally {
					Mem.OSUnlockObject(hItemByVal);
				}
			});
		}
		else if (value instanceof Calendar[]) {
			return appendItemValue(itemName, flags, Arrays.asList(value), valueConverter, allowDataTypeChanges);
		}
		else if (value instanceof Date[]) {
			return appendItemValue(itemName, flags, Arrays.asList(value), valueConverter, allowDataTypeChanges);
		}
		else if (value instanceof DominoDateTime[]) {
			return appendItemValue(itemName, flags, Arrays.asList((DominoDateTime[])value), valueConverter, allowDataTypeChanges);
		}
		else if (value instanceof DominoDateRange) {
			return appendItemValue(itemName, flags, Arrays.asList(value), valueConverter, allowDataTypeChanges);
		}
		else if (value instanceof Iterable && isCalendarOrCalendarArrayList((Iterable<?>) value)) {
			List<?> dateOrDateTimeRangeList = toDateTimeOrDateTimeRangeList((Iterable<?>) value);
			
			List<DominoDateTime> dateTimeList = new ArrayList<>();
			List<DominoDateRange> dateRangeList = new ArrayList<>();
			
			for (int i=0; i<dateOrDateTimeRangeList.size(); i++) {
				Object currObj = dateOrDateTimeRangeList.get(i);
				if (currObj instanceof DominoDateTime) {
					dateTimeList.add((DominoDateTime) currObj);
				}
				else if (currObj instanceof DominoDateRange) {
					dateRangeList.add((DominoDateRange) currObj);
				}
			}
			
			if (dateTimeList.size() > 65535) {
				throw new IllegalArgumentException(format("Date list size must fit in a WORD ({0}>65535)", dateTimeList.size()));
			}
			if (dateRangeList.size() > 65535) {
				throw new IllegalArgumentException(format("Date range list size must fit in a WORD ({0}>65535)", dateRangeList.size()));
			}

			int valueSize = 2 + JNANotesConstants.rangeSize + 
					8 * dateTimeList.size() +
					JNANotesConstants.timeDatePairSize * dateRangeList.size();
			

			DHANDLE.ByReference rethItem = DHANDLE.newInstanceByReference();
			short result = Mem.OSMemAlloc((short) 0, valueSize, rethItem);
			NotesErrorUtils.checkResult(result);
			
			return LockUtil.lockHandle(rethItem, (hItemByVal) -> {
				Pointer valuePtr = Mem.OSLockObject(hItemByVal);
				
				try {
					valuePtr.setShort(0, ItemDataType.TYPE_TIME_RANGE.getValue().shortValue());
					valuePtr = valuePtr.share(2);
					
					Pointer rangePtr = valuePtr;
					NotesRangeStruct range = NotesRangeStruct.newInstance(rangePtr);
					range.ListEntries = (short) (dateTimeList.size() & 0xffff);
					range.RangeEntries = (short) (dateRangeList.size() & 0xffff);
					range.write();

					Pointer dateListPtr = rangePtr.share(JNANotesConstants.rangeSize);
					
					for (DominoDateTime currDateTime : dateTimeList) {
						int[] innards = currDateTime.getAdapter(int[].class);

						dateListPtr.setInt(0, innards[0]);
						dateListPtr = dateListPtr.share(4);
						dateListPtr.setInt(0, innards[1]);
						dateListPtr = dateListPtr.share(4);
					}
					
					Pointer rangeListPtr = dateListPtr;
					
					for (int i=0; i<dateRangeList.size(); i++) {
						DominoDateRange currRangeVal = dateRangeList.get(i);
						DominoDateTime start = currRangeVal.getStartDateTime();
						DominoDateTime end = currRangeVal.getEndDateTime();
						
						int[] innardsStart = start.getAdapter(int[].class);
						int[] innardsEnd = end.getAdapter(int[].class);

						NotesTimeDateStruct timeDateStart = NotesTimeDateStruct.newInstance(innardsStart);
						NotesTimeDateStruct timeDateEnd = NotesTimeDateStruct.newInstance(innardsEnd);
						
						NotesTimeDatePairStruct timeDatePair = NotesTimeDatePairStruct.newInstance(rangeListPtr);
						timeDatePair.Lower = timeDateStart;
						timeDatePair.Upper = timeDateEnd;
						timeDatePair.write();

						rangeListPtr = rangeListPtr.share(JNANotesConstants.timeDatePairSize);
					}

					return appendItemValue(itemName, flags, ItemDataType.TYPE_TIME_RANGE.getValue(), hItemByVal, valueSize);
				}
				finally {
					Mem.OSUnlockObject(hItemByVal);
				}
			});
		}
		else if (value instanceof Number[]) {
			return appendItemValue(itemName, flags, Arrays.asList(value), valueConverter, allowDataTypeChanges);
		}
		else if (value instanceof Double[]) {
			return appendItemValue(itemName, flags, Arrays.asList(value), valueConverter, allowDataTypeChanges);
		}
		else if (value instanceof Integer[]) {
			return appendItemValue(itemName, flags, Arrays.asList(value), valueConverter, allowDataTypeChanges);
		}
		else if (value instanceof Float[]) {
			return appendItemValue(itemName, flags, Arrays.asList(value), valueConverter, allowDataTypeChanges);
		}
		else if (value instanceof Long[]) {
			return appendItemValue(itemName, flags, Arrays.asList(value), valueConverter, allowDataTypeChanges);
		}
		else if (value instanceof DominoUniversalNoteId) {
			NotesUniversalNoteIdStruct struct = ((DominoUniversalNoteId)value).getAdapter(NotesUniversalNoteIdStruct.class);

			//date type + LIST structure + UNIVERSALNOTEID
			int valueSize = 2 + 2 + 2 * JNANotesConstants.timeDateSize;
			

			DHANDLE.ByReference rethItem = DHANDLE.newInstanceByReference();
			short result = Mem.OSMemAlloc((short) 0, valueSize, rethItem);
			NotesErrorUtils.checkResult(result);
			
			return LockUtil.lockHandle(rethItem, (hItemByVal) -> {
				Pointer valuePtr = Mem.OSLockObject(hItemByVal);
				
				try {
					valuePtr.setShort(0, ItemDataType.TYPE_NOTEREF_LIST.getValue().shortValue());
					valuePtr = valuePtr.share(2);
					
					//LIST structure
					valuePtr.setShort(0, (short) 1);
					valuePtr = valuePtr.share(2);
					
					struct.write();
					valuePtr.write(0, struct.getAdapter(Pointer.class).getByteArray(0, 2*JNANotesConstants.timeDateSize), 0, 2*JNANotesConstants.timeDateSize);

					return appendItemValue(itemName, flags, ItemDataType.TYPE_NOTEREF_LIST.getValue(), hItemByVal, valueSize);
				}
				finally {
					Mem.OSUnlockObject(hItemByVal);
				}
			});
		}
		else if (value instanceof JNAFormula) {
			byte[] compiledFormula = ((JNAFormula)value).getAdapter(byte[].class);
			if (compiledFormula==null) {
				throw new IllegalArgumentException(format("Unable to read the data of the compiled formula: {0}", ((JNAFormula)value).getFormula()));
			}
			
			//date type + compiled formula
			int valueSize = 2 + compiledFormula.length;
			
			DHANDLE.ByReference rethItem = DHANDLE.newInstanceByReference();
			short result = Mem.OSMemAlloc((short) 0, valueSize, rethItem);
			NotesErrorUtils.checkResult(result);
			
			return LockUtil.lockHandle(rethItem, (hItemByVal) -> {
				Pointer valuePtr = Mem.OSLockObject(hItemByVal);
				
				try {
					valuePtr.setShort(0, ItemDataType.TYPE_FORMULA.getValue().shortValue());
					valuePtr = valuePtr.share(2);
					
					valuePtr.write(0, compiledFormula, 0, compiledFormula.length);

					return appendItemValue(itemName, flags, ItemDataType.TYPE_FORMULA.getValue(), hItemByVal, valueSize);
				}
				finally {
					Mem.OSUnlockObject(hItemByVal);
				}
			
			});
		}
		else if (value instanceof DominoViewFormat) {
		  ByteBuffer viewFormatData = ViewFormatEncoder.encodeViewFormat((DominoViewFormat) value);
		  viewFormatData.position(0);
		  byte[] viewFormatDataArr = new byte[viewFormatData.capacity()];
		  viewFormatData.get(viewFormatDataArr);
		  
		  //date type + compiled formula
      int valueSize = 2 + viewFormatDataArr.length;
      
      DHANDLE.ByReference rethItem = DHANDLE.newInstanceByReference();
      short result = Mem.OSMemAlloc((short) 0, valueSize, rethItem);
      NotesErrorUtils.checkResult(result);
      
      return LockUtil.lockHandle(rethItem, (hItemByVal) -> {
        Pointer valuePtr = Mem.OSLockObject(hItemByVal);
        
        try {
          valuePtr.setShort(0, ItemDataType.TYPE_VIEW_FORMAT.getValue().shortValue());
          valuePtr = valuePtr.share(2);
          
          valuePtr.write(0, viewFormatDataArr, 0, viewFormatDataArr.length);

          return appendItemValue(itemName, flags, ItemDataType.TYPE_VIEW_FORMAT.getValue(), hItemByVal, valueSize);
        }
        finally {
          Mem.OSUnlockObject(hItemByVal);
        }
      
      });
		}
		else if (value instanceof DominoCalendarFormat) {
      ByteBuffer calendarFormatData = ViewFormatEncoder.encodeCalendarFormat((DominoCalendarFormat) value);
      calendarFormatData.position(0);
      byte[] calendarFormatDataArr = new byte[calendarFormatData.capacity()];
      calendarFormatData.get(calendarFormatDataArr);
      
      //date type + compiled formula
      int valueSize = 2 + calendarFormatDataArr.length;
      
      DHANDLE.ByReference rethItem = DHANDLE.newInstanceByReference();
      short result = Mem.OSMemAlloc((short) 0, valueSize, rethItem);
      NotesErrorUtils.checkResult(result);
      
      return LockUtil.lockHandle(rethItem, (hItemByVal) -> {
        Pointer valuePtr = Mem.OSLockObject(hItemByVal);
        
        try {
          valuePtr.setShort(0, ItemDataType.TYPE_CALENDAR_FORMAT.getValue().shortValue());
          valuePtr = valuePtr.share(2);
          
          valuePtr.write(0, calendarFormatDataArr, 0, calendarFormatDataArr.length);

          return appendItemValue(itemName, flags, ItemDataType.TYPE_CALENDAR_FORMAT.getValue(), hItemByVal, valueSize);
        }
        finally {
          Mem.OSUnlockObject(hItemByVal);
        }
      
      });
    }
    else if (value instanceof DominoCollationInfo) {
      ByteBuffer collationData = CollationEncoder.encode((DominoCollationInfo) value);
      collationData.position(0);
      byte[] collationDataArr = new byte[collationData.capacity()];
      collationData.get(collationDataArr);
      
      //date type + compiled formula
      int valueSize = 2 + collationDataArr.length;
      
      DHANDLE.ByReference rethItem = DHANDLE.newInstanceByReference();
      short result = Mem.OSMemAlloc((short) 0, valueSize, rethItem);
      NotesErrorUtils.checkResult(result);
      
      return LockUtil.lockHandle(rethItem, (hItemByVal) -> {
        Pointer valuePtr = Mem.OSLockObject(hItemByVal);
        
        try {
          valuePtr.setShort(0, ItemDataType.TYPE_COLLATION.getValue().shortValue());
          valuePtr = valuePtr.share(2);
          
          valuePtr.write(0, collationDataArr, 0, collationDataArr.length);

          return appendItemValue(itemName, flags, ItemDataType.TYPE_COLLATION.getValue(), hItemByVal, valueSize);
        }
        finally {
          Mem.OSUnlockObject(hItemByVal);
        }
      
      });
    }
		else if (valueConverter!=null) {
			if (writingItemType.get().contains(valueConverter.getClass())) {
				throw new IllegalStateException(format("Infinite loop detected writing the value of item {0} as type {1}", itemName,
						value.getClass().getName()));
			}
			writingItemType.get().add(valueConverter.getClass());
			try {
			  if (valueConverter instanceof DocumentValueConverter) {
	        valueConverter.setValue(this, flags, itemName, value);
			  }
			  else {
	        valueConverter.setValue(this, itemName, value);
			  }
				return this;
			}
			finally {
				writingItemType.get().remove(valueConverter.getClass());
			}
		}
		else {
			throw new UnsupportedItemValueError(format("Unsupported value type: {0}", (value==null ? "null" : value.getClass().getName()))); //$NON-NLS-2$
		}
	}

	/**
	 * Internal method that calls the C API method to write the item
	 * 
	 * @param itemName item name
	 * @param flags item flags
	 * @param itemType item type
	 * @param hItemValue handle to memory block with item value
	 * @param valueLength length of binary item value (without data type short)
	 * @return this document
	 */
	public Document appendItemValue(String itemName, Set<ItemFlag> flags, int itemType, DHANDLE.ByValue hItemValue, int valueLength) {
		checkDisposed();

		Memory itemNameMem = NotesStringUtils.toLMBCS(itemName, false);
		
		short flagsShort = (short) (DominoEnumUtil.toBitField(ItemFlag.class, flags) & 0xffff);
		
		NotesBlockIdStruct.ByValue valueBlockIdByVal = NotesBlockIdStruct.ByValue.newInstance();
		if (PlatformUtils.is64Bit()) {
			valueBlockIdByVal.pool = (int) ((DHANDLE64.ByValue)hItemValue).hdl;
		}
		else {
			valueBlockIdByVal.pool = ((DHANDLE32.ByValue)hItemValue).hdl;
		}
		valueBlockIdByVal.block = 0;
		valueBlockIdByVal.write();
		
		NotesBlockIdStruct retItemBlockId = NotesBlockIdStruct.newInstance();
		retItemBlockId.pool = 0;
		retItemBlockId.block = 0;
		retItemBlockId.write();
		
		JNADocumentAllocations allocations = getAllocations();
		short result = LockUtil.lockHandle(allocations.getNoteHandle(), (noteHandlyByVal) -> {
			return NotesCAPI.get().NSFItemAppendByBLOCKID(noteHandlyByVal, flagsShort, itemNameMem,
					(short) (itemNameMem==null ? 0 : itemNameMem.size()), valueBlockIdByVal,
					valueLength, retItemBlockId);
		});
		NotesErrorUtils.checkResult(result);
		
		return this;
	}
	
	@Override
	public RichTextWriter createRichTextItem(String itemName) {
		checkDisposed();
		JNADocumentAllocations allocations = getAllocations();
		
		JNARichtextWriter writer = new JNARichtextWriter(this, itemName);
		allocations.registerRichtextWriter(itemName, writer);
		
		return writer;
	}

	@Override
	public RichTextRecordList getRichTextItem(String itemName, RecordType.Area variant) {
		return new DefaultRichTextList(new JNARichtextNavigator(this, itemName), variant);
	}

	@Override
	public Attachment attachFile(String filePathOnDisk, String uniqueFileNameInNote, Compression compression) {
		checkDisposed();

		//make sure that the unique filename is really unique, since it will be used to return the NotesAttachment object
		JNAFormula formula = (JNAFormula) getParentDominoClient().createFormula("@AttachmentNames"); //$NON-NLS-1$
		List<Object> existingFileItems = formula.evaluate(this);
		formula.dispose();

		String reallyUniqueFileName = uniqueFileNameInNote;
		if (existingFileItems.contains(reallyUniqueFileName)) {
			String newFileName=reallyUniqueFileName;
			int idx = 1;
			while (existingFileItems.contains(reallyUniqueFileName)) {
				idx++;
				
				int iPos = reallyUniqueFileName.lastIndexOf('.');
				if (iPos==-1) {
					newFileName = reallyUniqueFileName+"_"+idx; //$NON-NLS-1$
				}
				else {
					newFileName = reallyUniqueFileName.substring(0, iPos)+"_"+idx+reallyUniqueFileName.substring(iPos); //$NON-NLS-1$
				}
				reallyUniqueFileName = newFileName;
			}
		}
		
		Memory $fileItemName = NotesStringUtils.toLMBCS("$FILE", true); //$NON-NLS-1$
		Memory filePathOnDiskMem = NotesStringUtils.toLMBCS(filePathOnDisk, true);
		Memory uniqueFileNameInNoteMem = NotesStringUtils.toLMBCS(reallyUniqueFileName, true);
		short compressionAsShort = (short) (compression.getValue() & 0xffff);
		
		short result = LockUtil.lockHandle(getAllocations().getNoteHandle(), (noteHandleByVal) -> {
			return NotesCAPI.get().NSFNoteAttachFile(noteHandleByVal, $fileItemName,
					(short) (($fileItemName.size()-1) & 0xffff), filePathOnDiskMem, uniqueFileNameInNoteMem, compressionAsShort);
		});
		NotesErrorUtils.checkResult(result);
		
		String fUniqueName = reallyUniqueFileName;
		return getAttachment(reallyUniqueFileName).orElseThrow(() -> new IllegalStateException(MessageFormat.format("Unable to locate newly-created attachment \"{0}\"", fUniqueName)));
	}

	@Override
	public Attachment attachFile(String uniqueFileNameInDoc,
			TemporalAccessor fileCreated,
			TemporalAccessor fileModified, IAttachmentProducer producer) {

		checkDisposed();

		//currently we do not support compression, because we could not find a Java OutputStream
		//implementation for Huffman that produced compatible result and no implementation at all
		//for LZ1 (tried LZW, but that did not work either)
		final Compression compression = Compression.NONE;
		
		//make sure that the unique filename is really unique, since it will be used to return the NotesAttachment object
		JNAFormula formulaObj = (JNAFormula) getParentDominoClient().createFormula("@AttachmentNames"); //$NON-NLS-1$
		List<Object> existingFileItems = formulaObj.evaluate(this);
		formulaObj.dispose();
		
		String reallyUniqueFileName = uniqueFileNameInDoc;
		if (existingFileItems.contains(reallyUniqueFileName)) {
			String newFileName=reallyUniqueFileName;
			int idx = 1;
			while (existingFileItems.contains(reallyUniqueFileName)) {
				idx++;

				int iPos = reallyUniqueFileName.lastIndexOf('.');
				if (iPos==-1) {
					newFileName = reallyUniqueFileName+"_"+idx; //$NON-NLS-1$
				}
				else {
					newFileName = reallyUniqueFileName.substring(0, iPos)+"_"+idx+reallyUniqueFileName.substring(iPos); //$NON-NLS-1$
				}
				reallyUniqueFileName = newFileName;
			}
		}
		
		Memory fileItemNameMem = NotesStringUtils.toLMBCS("$FILE", false); //$NON-NLS-1$
		Memory reallyUniqueFileNameMem = NotesStringUtils.toLMBCS(reallyUniqueFileName, false);
		
		JNADatabaseAllocations dbAllocations = (JNADatabaseAllocations) getParentDatabase().getAdapter(APIObjectAllocations.class);
		dbAllocations.checkDisposed();
		
		ObjectInfo objectInfo = JNADatabaseObjectProducer.createDbObject((JNADatabase) getParentDatabase(),
				 NotesConstants.NOTE_CLASS_DOCUMENT, NotesConstants.OBJECT_FILE, producer);
		
		LockUtil.lockHandles(dbAllocations.getDBHandle(), getAllocations().getNoteHandle(),
				(dbHandleByVal, noteHandleByVal) -> {

			//allocate memory for the $FILE item value:
			//datatype WORD + FILEOBJECT structure + unique filename
			int sizeOfFileObjectWithFileName = (int) (2 + JNANotesConstants.fileObjectSize + reallyUniqueFileNameMem.size());
			DHANDLE.ByReference retFileObjectWithFileNameHandle = DHANDLE.newInstanceByReference();;
			short result = Mem.OSMemAlloc((short) 0, sizeOfFileObjectWithFileName, retFileObjectWithFileNameHandle);
			NotesErrorUtils.checkResult(result);
			
			//produce FILEOBJECT data structure
			LockUtil.lockHandle(retFileObjectWithFileNameHandle, (retFileObjectWithFileNameHandleByVal) -> {
				Pointer ptrFileObjectWithDatatype = Mem.OSLockObject(retFileObjectWithFileNameHandleByVal);
				try {
					//write datatype WORD
					ptrFileObjectWithDatatype.setShort(0, (short) (ItemDataType.TYPE_OBJECT.getValue() & 0xffff));
					NotesFileObjectStruct fileObjectStruct = NotesFileObjectStruct.newInstance(ptrFileObjectWithDatatype.share(2));
					fileObjectStruct.CompressionType = (short) (compression.getValue() & 0xffff);
					fileObjectStruct.FileAttributes = 0;
					
					JNADominoDateTime fileCreatedDt = JNADominoDateTime.from(fileCreated);
					fileObjectStruct.FileCreated = NotesTimeDateStruct.newInstance(fileCreatedDt.getAdapter(int[].class));
					JNADominoDateTime fileModifiedDt = JNADominoDateTime.from(fileModified);
					fileObjectStruct.FileModified = NotesTimeDateStruct.newInstance(fileModifiedDt.getAdapter(int[].class));
					fileObjectStruct.FileNameLength = (short) (reallyUniqueFileNameMem.size() & 0xffff);
					fileObjectStruct.FileSize = (int) (objectInfo.getObjectSize() & 0xffffffff);
					fileObjectStruct.Flags = 0;
					fileObjectStruct.Header.RRV = objectInfo.getObjectId();
					fileObjectStruct.Header.ObjectType = NotesConstants.OBJECT_FILE;
					
					fileObjectStruct.write();
					
					//append unique filename
					ptrFileObjectWithDatatype.share(2 + JNANotesConstants.fileObjectSize).write(0, reallyUniqueFileNameMem.getByteArray(0, (int) reallyUniqueFileNameMem.size()), 0, (int) reallyUniqueFileNameMem.size());
				}
				finally {
					Mem.OSUnlockObject(retFileObjectWithFileNameHandleByVal);
				}

				NotesBlockIdStruct.ByValue bhValue = NotesBlockIdStruct.ByValue.newInstance();
				if (retFileObjectWithFileNameHandleByVal instanceof DHANDLE64) {
					bhValue.pool = (int) ((DHANDLE64)retFileObjectWithFileNameHandleByVal).hdl;
				}
				else if (retFileObjectWithFileNameHandleByVal instanceof DHANDLE32) {
					bhValue.pool = ((DHANDLE32)retFileObjectWithFileNameHandleByVal).hdl;
				}

				int fDealloc = 1;
				//transfers ownership of the item value buffer to the note
				short itemAppendResult = NotesCAPI.get().NSFItemAppendObject(noteHandleByVal,
						NotesConstants.ITEM_SUMMARY,
						fileItemNameMem,
						(short) (fileItemNameMem.size() & 0xffff),
						bhValue,
						sizeOfFileObjectWithFileName,
						fDealloc);
				NotesErrorUtils.checkResult(itemAppendResult);

				return null;
			});
			
			return null;
		});

		//load and return created attachment
		String fUniqueName = reallyUniqueFileName;
		Attachment att = getAttachment(reallyUniqueFileName).orElseThrow(() -> new IllegalStateException(MessageFormat.format("Unable to locate newly-created attachment \"{0}\"", fUniqueName)));
		return att;
	
	}

	@Override
	public Document removeAttachment(String uniqueFileNameInDoc) {
		getAttachment(uniqueFileNameInDoc).ifPresent(Attachment::deleteFromDocument);
		return this;
	}

	@Override
	public Document makeResponse(Document doc) {
		return makeResponse(doc.getUNID());
	}

	@Override
	public Document makeResponse(String unid) {
		replaceItemValue("$REF", EnumSet.of(ItemFlag.SUMMARY), new JNADominoUniversalNoteId(unid)); //$NON-NLS-1$
		return this;
	}

	@Override
	public Document sign() {
		checkDisposed();

		Set<DocumentClass> docClass = getDocumentClass();

		LockUtil.lockHandle(getAllocations().getNoteHandle(), (hNoteByVal) -> {
			short result;
			boolean expandNote = false;
			if (docClass.contains(DocumentClass.FORM) || docClass.contains(DocumentClass.INFO) ||
					docClass.contains(DocumentClass.HELP) || docClass.contains(DocumentClass.FIELD)) {
				expandNote = true;
			}

			if (expandNote) {
				result = NotesCAPI.get().NSFNoteExpand(hNoteByVal);
				NotesErrorUtils.checkResult(result);
			}

			result = NotesCAPI.get().NSFNoteSign(hNoteByVal);
			NotesErrorUtils.checkResult(result);

			if (expandNote) {
				result = NotesCAPI.get().NSFNoteContract(hNoteByVal);
				NotesErrorUtils.checkResult(result);
			}

			return 0;
		});
		return this;
	}

	@Override
	public Document sign(UserId id, boolean signNotesIfMimePresent) {
		checkDisposed();
		
		LockUtil.lockHandle(getAllocations().getNoteHandle(), (hNoteByVal) -> {
			short result = NotesCAPI.get().NSFNoteExpand(hNoteByVal);
			NotesErrorUtils.checkResult(result);

			short signResult;
			if (id==null) {
				signResult = NotesCAPI.get().NSFNoteSignExt3(hNoteByVal, (Pointer) null, null,
						NotesConstants.MAXWORD, (DHANDLE.ByReference) null,
						signNotesIfMimePresent ? NotesConstants.SIGN_NOTES_IF_MIME_PRESENT : 0, 0, (Pointer) null);
			}
			else {
				signResult = JNADominoUtils.accessKFC(id, phKFC ->
					NotesCAPI.get().NSFNoteSignExt3(hNoteByVal, phKFC.getValue(), null,
						NotesConstants.MAXWORD, (DHANDLE.ByReference) null,
						signNotesIfMimePresent ? NotesConstants.SIGN_NOTES_IF_MIME_PRESENT : 0, 0, (Pointer) null)
				);
			}
			NotesErrorUtils.checkResult(signResult);

			result = NotesCAPI.get().NSFNoteContract(hNoteByVal);
			NotesErrorUtils.checkResult(result);
			
			//verify signature
			NotesTimeDateStruct retWhenSigned = NotesTimeDateStruct.newInstance();
			Memory retSigner = new Memory(NotesConstants.MAXUSERNAME);
			Memory retCertifier = new Memory(NotesConstants.MAXUSERNAME);

			result = NotesCAPI.get().NSFNoteVerifySignature (hNoteByVal, null, retWhenSigned, retSigner, retCertifier);
			NotesErrorUtils.checkResult(result);
			
			return 0;
		});
		
		return this;
	}

	@Override
	public Document unsign() {
		checkDisposed();
		
		short result = LockUtil.lockHandle(getAllocations().getNoteHandle(), (hNoteByVal) -> {
			return NotesCAPI.get().NSFNoteUnsign(hNoteByVal);
		});
		NotesErrorUtils.checkResult(result);
		return this;
	}

	@Override
	public Document copyAndEncrypt(UserId id, Collection<EncryptionMode> encryptionMode) {
		checkDisposed();
		
		int flags = 0;
		for (EncryptionMode currMode : encryptionMode) {
			flags = flags | currMode.getMode();
		}
		
		short flagsShort = (short) (flags & 0xffff);
		
		short result;

		DHANDLE.ByReference rethDstNote = DHANDLE.newInstanceByReference();

		result = LockUtil.lockHandle(getAllocations().getNoteHandle(), (hNoteByVal) -> {
			if (id!=null) {
				return JNADominoUtils.accessKFC(id, phKFC ->
					NotesCAPI.get().NSFNoteCopyAndEncryptExt2(hNoteByVal, phKFC.getValue(), flagsShort,
						rethDstNote, 0, null)
				);
			}
			else {
				return NotesCAPI.get().NSFNoteCopyAndEncryptExt2(hNoteByVal, null, flagsShort,
						rethDstNote, 0, null);
			}
		});
		NotesErrorUtils.checkResult(result);
		
		return new JNADocument((JNADatabase) getParentDatabase(), rethDstNote);
	}

	@Override
	public Document setUNID(String newUNID) {
		checkDisposed();
		JNADocumentAllocations allocations = getAllocations();
		
		DisposableMemory retOid = new DisposableMemory(JNANotesConstants.oidSize);
		try {
			retOid.clear();

			LockUtil.lockHandle(allocations.getNoteHandle(), (handleByVal) -> {
				NotesCAPI.get().NSFNoteGetInfo(handleByVal, NotesConstants._NOTE_OID, retOid);
				
				NotesOriginatorIdStruct oidStruct = NotesOriginatorIdStruct.newInstance(retOid);
				oidStruct.read();
				oidStruct.setUNID(newUNID);
				
				NotesCAPI.get().NSFNoteSetInfo(handleByVal, NotesConstants._NOTE_OID, retOid);
				
				return null;
			});
		}
		finally {
			retOid.dispose();
		}
		return this;
	}

	@Override
	public Document copyToDatabase(Database otherDb) {
		checkDisposed();
		JNADocumentAllocations allocations = getAllocations();
		
		if (!(otherDb instanceof JNADatabase)) {
			throw new DominoException("Unsupported target database type, not a JNADatabase");
		}
		JNADatabase otherJNADb = (JNADatabase) otherDb;
		JNADatabaseAllocations otherJNADbAllocations = (JNADatabaseAllocations) otherJNADb.getAdapter(APIObjectAllocations.class);
		otherJNADbAllocations.checkDisposed();
		
		DHANDLE.ByReference newNoteHandle = DHANDLE.newInstanceByReference();
		
		short result = LockUtil.lockHandle(allocations.getNoteHandle(), (noteHandleByVal) -> {
			return NotesCAPI.get().NSFNoteCopy(noteHandleByVal, newNoteHandle);
		});
		NotesErrorUtils.checkResult(result);
		
		NotesOriginatorIdStruct newOID = otherJNADb.generateOIDStruct();
		
		LockUtil.lockHandles(
				otherJNADbAllocations.getDBHandle(),
				newNoteHandle, (otherDbHandleByVal, newNoteHandleByVal) -> {
					NotesCAPI.get().NSFNoteSetInfo(newNoteHandleByVal, NotesConstants._NOTE_ID, null);
					
					NotesCAPI.get().NSFNoteSetInfo(newNoteHandleByVal, NotesConstants._NOTE_OID, newOID.getPointer());

					HANDLE.ByReference targetDbHdlByReference = HANDLE.newInstanceByReference(otherDbHandleByVal);
					
					NotesCAPI.get().NSFNoteSetInfo(newNoteHandleByVal, NotesConstants._NOTE_DB,
							((Structure) targetDbHdlByReference).getPointer());
					
					return null;
				});
		
		return new JNADocument(otherJNADb, newNoteHandle);
	}

	@Override
	public Document decrypt(UserId id) {
		checkDisposed();
		
		short decryptFlags = NotesConstants.DECRYPT_ATTACHMENTS_IN_PLACE;
		
		LockUtil.lockHandle(getAllocations().getNoteHandle(), (hNoteByVal) -> {
			short decryptResult;
			
			if (id!=null) {
				decryptResult = JNADominoUtils.accessKFC(id, phKFC ->
					NotesCAPI.get().NSFNoteCipherDecrypt(hNoteByVal, phKFC.getValue(), decryptFlags,
						null, 0, null)
				);
			}
			else {
				decryptResult = NotesCAPI.get().NSFNoteCipherDecrypt(hNoteByVal, null, decryptFlags,
						null, 0, null);
			}
			NotesErrorUtils.checkResult(decryptResult);
			return 0;
		});
		return this;
	}

	@Override
	public void delete() {
		if (checkForProfileAndDelete()) {
			return;
		}
		
		checkDisposed();
		((JNADatabase)getParent()).deleteDocument(getNoteID());
	}

	@Override
	public void delete(boolean noStub) {
		if (checkForProfileAndDelete()) {
			return;
		}

		((JNADatabase)getParent()).deleteDocument(getNoteID(), EnumSet.of(UpdateNote.NOSTUB));
	}
	
	@Override
	public Document undelete() {
		checkDisposed();
		JNADocumentAllocations allocations = getAllocations();
		
		Set<UpdateNote> updateFlags = EnumSet.of(UpdateNote.RESTORE_SOFT_DELETED);
		int updateFlagsBitmask = DominoEnumUtil.toBitField(UpdateNote.class, updateFlags);
		
		allocations.closeAllRichtextWriters();
		
		short result = LockUtil.lockHandle(allocations.getNoteHandle(), (handleByVal) -> {
			return NotesCAPI.get().NSFNoteUpdateExtended(handleByVal, updateFlagsBitmask);
		});
		
		NotesErrorUtils.checkResult(result);
		return this;
	}
	
	/**
	 * Checks if this note is a profile. If it is, we use a different C method
	 * to delete it in the database and also delete the profile cache entry.
	 * 
	 * @return true if note is a profile note that has been saved, false otherwise
	 */
	private boolean checkForProfileAndDelete() {
		checkDisposed();
		
		if (!isHiddenFromViews()) {
			return false;
		}
		
		String[] profileNameAndUsername = parseProfileAndUserName();
		
		if (profileNameAndUsername==null) {
			return false;
		}
		
		String profileName = profileNameAndUsername[0];
		String profileUsername = profileNameAndUsername[1];
		
		Memory profileNameMem = NotesStringUtils.toLMBCS(profileName, false);
		Memory profileUsernameMem = StringUtil.isEmpty(profileUsername) ? null : NotesStringUtils.toLMBCS(profileUsername, false);
		
		JNADatabase parentDb = (JNADatabase) getParentDatabase();
		if (parentDb.isDisposed()) {
			throw new ObjectDisposedException(parentDb);
		}
		JNADatabaseAllocations parentDbAllocations = (JNADatabaseAllocations) parentDb.getAdapter(APIObjectAllocations.class);
		
		JNADocumentAllocations docAllocations = getAllocations();
		
		short result = LockUtil.lockHandles(
				parentDbAllocations.getDBHandle(), docAllocations.getNoteHandle(),
				(dbHandleByVal, noteHandleByVal) -> {
					//delete note and remove from profile cache
					return NotesCAPI.get().NSFProfileDelete(dbHandleByVal,
							profileNameMem, (short) (profileNameMem.size() & 0xffff), profileUsernameMem, (short) ((profileUsernameMem==null ? 0 : profileUsernameMem.size()) & 0xffff));
				});
		NotesErrorUtils.checkResult(result);
		
		return true;
	}

	
	@Override
	public Document save() {
		return save(false);
	}

	@Override
	public Document save(boolean force) {
		if (force) {
			return save(EnumSet.of(UpdateNote.FORCE));
		}
		else {
			return save(EnumSet.noneOf(UpdateNote.class));
		}
	}

	Document save(Set<UpdateNote> updateFlags) {
		if (checkForProfileAndSave()) {
			return this;
		}
		
		checkDisposed();
		JNADocumentAllocations allocations = getAllocations();
		
		int updateFlagsBitmask = DominoEnumUtil.toBitField(UpdateNote.class, updateFlags);
		
		allocations.closeAllRichtextWriters();
		
		short result = LockUtil.lockHandle(allocations.getNoteHandle(), (handleByVal) -> {
			return NotesCAPI.get().NSFNoteUpdateExtended(handleByVal, updateFlagsBitmask);
		});
		
		NotesErrorUtils.checkResult(result);
		return this;
	}
	
	/**
	 * Checks if this note is a profile. If it is, we use a different C method
	 * to update it in the database and also update the profile cache.
	 * 
	 * @return true if note is a profile note that has been saved, false otherwise
	 */
	private boolean checkForProfileAndSave() {
		checkDisposed();
		
		if (!isHiddenFromViews()) {
			return false;
		}
		
		String[] profileNameAndUsername = parseProfileAndUserName();
		
		if (profileNameAndUsername==null) {
			return false;
		}
		String profileName = profileNameAndUsername[0];
		String profileUsername = profileNameAndUsername[1];
		
		parseProfileAndUserName();
		
		Memory profileNameMem = NotesStringUtils.toLMBCS(profileName, false);
		Memory userNameMem = StringUtil.isEmpty(profileUsername) ? null : NotesStringUtils.toLMBCS(profileUsername, false);

		JNADocumentAllocations allocations = getAllocations();
		
		short result = LockUtil.lockHandle(allocations.getNoteHandle(), (noteHandleByVal) -> {
			return  NotesCAPI.get().NSFProfileUpdate(noteHandleByVal,
					profileNameMem, (short) (profileNameMem.size() & 0xffff), userNameMem,
					(short) (userNameMem==null ? 0 : (userNameMem.size() & 0xffff)));
		});
		NotesErrorUtils.checkResult(result);
		
		return true;
 	}

	@Override
	public List<String> getLockHolders() {
		checkDisposed();

		if (isNew()) {
			return Collections.emptyList();
		}
		
		int lockFlags = NotesConstants.NOTE_LOCK_STATUS;
		
		JNADatabaseAllocations dbAllocations = (JNADatabaseAllocations) getParentDatabase().getAdapter(APIObjectAllocations.class);
		dbAllocations.checkDisposed();

		return LockUtil.lockHandle(dbAllocations.getDBHandle(),
				(dbHandleByVal) -> {

					DHANDLE.ByReference rethLockers = DHANDLE.newInstanceByReference();
					IntByReference retLength = new IntByReference();

					short result = NotesCAPI.get().NSFDbNoteLock(dbHandleByVal, getNoteID(), lockFlags,
							null, rethLockers, retLength);
					NotesErrorUtils.checkResult(result);

					if (rethLockers.isNull()) {
						return Collections.emptyList();
					}

					return LockUtil.lockHandle(rethLockers, (rethLockersByVal) -> {
						Pointer retLockersPtr = Mem.OSLockObject(rethLockersByVal);
						try {
							String retLockHoldersConc = NotesStringUtils.fromLMBCS(retLockersPtr, retLength.getValue());
							if (StringUtil.isEmpty(retLockHoldersConc)) {
								return Collections.emptyList();
							}

							String[] retLockHoldersArr = retLockHoldersConc.split(";"); //$NON-NLS-1$
							return Arrays.asList(retLockHoldersArr);
						}
						finally {
							Mem.OSUnlockObject(rethLockersByVal);
						}
					});
				});
	}

	@Override
	public Set<DocumentClass> getDocumentClass() {
		if (m_documentClass==null) {
			checkDisposed();
			
			Memory retNoteClass = new Memory(2);
			retNoteClass.clear();
			
			JNADocumentAllocations allocations = getAllocations();
			m_documentClass = LockUtil.lockHandle(allocations.getNoteHandle(), (noteHandleByVal) -> {
				NotesCAPI.get().NSFNoteGetInfo(noteHandleByVal, NotesConstants._NOTE_CLASS, retNoteClass);
				int noteClassMask = retNoteClass.getShort(0);

				Set<DocumentClass> docClass = EnumSet.noneOf(DocumentClass.class);
				
				if (noteClassMask==0) {
					docClass.add(DocumentClass.NONE);
				}
				else {
					for (DocumentClass currClass : DocumentClass.values()) {
						if (currClass.getValue()!=0) {
							if ((noteClassMask & currClass.getValue()) == currClass.getValue()) {
								docClass.add(currClass);
							}
						}
					}
				}
				
				return docClass;
			});
		}
		return m_documentClass;
	}

	 @Override
	 public Document setDocumentClass(DocumentClass docClass) {
	   return setDocumentClass(EnumSet.of(docClass));
	 }

	@Override
	public Document setDocumentClass(Collection<DocumentClass> docClass) {
		checkDisposed();
		
		m_documentClass = null;
		Short docClassVal = DominoEnumUtil.toBitField(DocumentClass.class, docClass);
		
		JNADocumentAllocations allocations = getAllocations();
		LockUtil.lockHandle(allocations.getNoteHandle(), (noteHandleByVal) -> {
			ShortByReference noteClassVal = new ShortByReference();
			noteClassVal.setValue(docClassVal.shortValue());
			NotesCAPI.get().NSFNoteSetInfo(noteHandleByVal, NotesConstants._NOTE_CLASS, noteClassVal.getPointer());
			return null;
		});
		
		return this;
	}

	@Override
	public boolean lock(String lockHolder, LockMode mode) {
		return lock(Arrays.asList(lockHolder), mode);
	}

	@Override
	public boolean lock(List<String> lockHolders, LockMode mode) {
		checkDisposed();

		if (isNew()) {
			throw new DominoException("Note must be saved before locking");
		}
		
		int lockFlags = 0;
		if (mode==LockMode.Hard || mode==LockMode.HardOrProvisional) {
			lockFlags = NotesConstants.NOTE_LOCK_HARD;
		}
		else if (mode==LockMode.Provisional) {
			lockFlags = NotesConstants.NOTE_LOCK_PROVISIONAL;
		} else {
			throw new IllegalArgumentException("Missing lock mode");
		}
		
		int fLockFlags = lockFlags;
		
		String lockHoldersConc = StringUtil.join(lockHolders, ";"); //$NON-NLS-1$
		Memory lockHoldersMem = NotesStringUtils.toLMBCS(lockHoldersConc, true);
		
		JNADatabaseAllocations dbAllocations = (JNADatabaseAllocations) getParentDatabase().getAdapter(APIObjectAllocations.class);
		dbAllocations.checkDisposed();

		short result = LockUtil.lockHandle(dbAllocations.getDBHandle(), (dbHandleByVal) -> {
			DHANDLE.ByReference rethLockers = DHANDLE.newInstanceByReference();
			IntByReference retLength = new IntByReference();
			
			short lockResult = NotesCAPI.get().NSFDbNoteLock(dbHandleByVal, getNoteID(), fLockFlags,
					lockHoldersMem, rethLockers, retLength);
			
			if (lockResult==1463) { //Unable to connect to Master Lock Database
				if (mode==LockMode.HardOrProvisional) {
					lockResult = NotesCAPI.get().NSFDbNoteLock(dbHandleByVal, getNoteID(), NotesConstants.NOTE_LOCK_PROVISIONAL,
							lockHoldersMem, rethLockers, retLength);
				}
			}
		
			return lockResult;
		});
		
		if (result == INotesErrorConstants.ERR_NOTE_LOCKED) {
			return false;
		}
		NotesErrorUtils.checkResult(result);

		return true;
	}

	@Override
	public Document unlock(LockMode mode) {
		checkDisposed();
		
		if (isNew()) {
			return this;
		}
		
		int lockFlags = 0;
		if (mode==LockMode.Hard || mode==LockMode.HardOrProvisional) {
			lockFlags = NotesConstants.NOTE_LOCK_HARD;
		}
		else if (mode==LockMode.Provisional) {
			lockFlags = NotesConstants.NOTE_LOCK_PROVISIONAL;
		} else {
			throw new IllegalArgumentException("Missing lock mode");
		}
		
		final int fLockFlags = lockFlags;
		
		JNADatabaseAllocations dbAllocations = (JNADatabaseAllocations) getParentDatabase().getAdapter(APIObjectAllocations.class);
		dbAllocations.checkDisposed();
		
		short result = LockUtil.lockHandle(dbAllocations.getDBHandle(), (dbHandleByVal) -> {
			short lockResult = NotesCAPI.get().NSFDbNoteUnlock(dbHandleByVal, getNoteID(), fLockFlags);

			if (lockResult==1463) { //Unable to connect to Master Lock Database
				if (mode==LockMode.HardOrProvisional) {
					lockResult = NotesCAPI.get().NSFDbNoteUnlock(dbHandleByVal, getNoteID(), NotesConstants.NOTE_LOCK_PROVISIONAL);
				}
			}
			
			return lockResult;
		});
	
		NotesErrorUtils.checkResult(result);
		return this;
	}

	/**
	 * Reads the note flags (e.g. {@link NotesConstants#NOTE_FLAG_READONLY})
	 * 
	 * @return flags
	 */
	private short getFlags() {
		checkDisposed();
		
		JNADocumentAllocations allocations = getAllocations();
		return LockUtil.lockHandle(allocations.getNoteHandle(), (noteHandleByVal) -> {
			Memory retFlags = new Memory(2);
			retFlags.clear();
			
			NotesCAPI.get().NSFNoteGetInfo(noteHandleByVal, NotesConstants._NOTE_FLAGS, retFlags);
			short flags = retFlags.getShort(0);
			return flags;
		});
	}
	
	/**
	 * Reads the note flags2 (e.g. {@link NotesConstants#NOTE_FLAG2_SOFT_DELETED})
	 * 
	 * @return flags
	 */
	private short getFlags2() {
		checkDisposed();
		
		JNADocumentAllocations allocations = getAllocations();
		return LockUtil.lockHandle(allocations.getNoteHandle(), (noteHandleByVal) -> {
			Memory retFlags = new Memory(2);
			retFlags.clear();
			
			NotesCAPI.get().NSFNoteGetInfo(noteHandleByVal, NotesConstants._NOTE_FLAGS2, retFlags);
			short flags = retFlags.getShort(0);
			return flags;
		});
	}
	
	private void setFlags(short flags) {
		checkDisposed();

		DisposableMemory flagsMem = new DisposableMemory(2);
		try {
			flagsMem.setShort(0, flags);

			LockUtil.lockHandle(getAllocations().getNoteHandle(), (noteHandleByVal) -> {
				NotesCAPI.get().NSFNoteSetInfo(noteHandleByVal, NotesConstants._NOTE_FLAGS, flagsMem);
				return 0;
			});
		}
		finally {
			flagsMem.dispose();
		}
	}
	
	@Override
	public boolean isEditable() {
		int flags = getFlags();
		return (flags & NotesConstants.NOTE_FLAG_READONLY) != NotesConstants.NOTE_FLAG_READONLY;
	}

	@Override
	public boolean hasReadersField() {
		checkDisposed();
		
		JNADocumentAllocations allocations = getAllocations();
		
		NotesBlockIdStruct blockId = NotesBlockIdStruct.newInstance();

		return LockUtil.lockHandle(allocations.getNoteHandle(), (noteHandleByVal) -> {
			return NotesCAPI.get().NSFNoteHasReadersField(noteHandleByVal, blockId) == 1;
		});
	}

	@Override
	public List<Item> getReadersFields() {
		checkDisposed();
		
		JNADocumentAllocations allocations = getAllocations();
		
		NotesBlockIdStruct blockId = NotesBlockIdStruct.newInstance();

		boolean hasReaders = LockUtil.lockHandle(allocations.getNoteHandle(), (noteHandleByVal) -> {
			return NotesCAPI.get().NSFNoteHasReadersField(noteHandleByVal, blockId) == 1;
		});
		
		if (!hasReaders) {
			return Collections.emptyList();
		}
		
		List<Item> readerFields = new ArrayList<>();
		
		NotesBlockIdStruct.ByValue itemBlockIdByVal = NotesBlockIdStruct.ByValue.newInstance();
		itemBlockIdByVal.pool = blockId.pool;
		itemBlockIdByVal.block = blockId.block;
		
		ByteByReference retSeqByte = new ByteByReference();
		ByteByReference retDupItemID = new ByteByReference();

		Memory item_name = new Memory(NotesConstants.MAXUSERNAME);
		ShortByReference retName_len = new ShortByReference();
		ShortByReference retItem_flags = new ShortByReference();
		ShortByReference retDataType = new ShortByReference();
		IntByReference retValueLen = new IntByReference();

		NotesBlockIdStruct retValueBid = NotesBlockIdStruct.newInstance();
		
		LockUtil.lockHandle(allocations.getNoteHandle(), (noteHandleByVal) -> {

			NotesCAPI.get().NSFItemQueryEx(noteHandleByVal,
					itemBlockIdByVal, item_name, (short) (item_name.size() & 0xffff), retName_len,
					retItem_flags, retDataType, retValueBid, retValueLen, retSeqByte, retDupItemID);
	
			NotesBlockIdStruct itemBlockIdForItemCreation = NotesBlockIdStruct.newInstance();
			itemBlockIdForItemCreation.pool = itemBlockIdByVal.pool;
			itemBlockIdForItemCreation.block = itemBlockIdByVal.block;
			itemBlockIdForItemCreation.write();
			
			if ((retItem_flags.getValue() & NotesConstants.ITEM_READERS) == NotesConstants.ITEM_READERS) {
				JNAItem firstItem = new JNAItem(this, itemBlockIdForItemCreation, retDataType.getValue() & 0xffff,
						retValueBid);
				readerFields.add(firstItem);
			}
	
			short result;

			//now search for more items with readers flag
			while (true) {
				IntByReference retNextValueLen = new IntByReference();
				
				NotesBlockIdStruct retItemBlockId = NotesBlockIdStruct.newInstance();
				
				result = NotesCAPI.get().NSFItemInfoNext(noteHandleByVal, itemBlockIdByVal,
						null, (short) 0, retItemBlockId, retDataType,
						retValueBid, retNextValueLen);
				
				if (result == INotesErrorConstants.ERR_ITEM_NOT_FOUND) {
					return readerFields;
				}
	
				NotesErrorUtils.checkResult(result);
	
				itemBlockIdForItemCreation = NotesBlockIdStruct.newInstance();
				itemBlockIdForItemCreation.pool = retItemBlockId.pool;
				itemBlockIdForItemCreation.block = retItemBlockId.block;
				itemBlockIdForItemCreation.write();
				
				NotesBlockIdStruct valueBlockIdClone = NotesBlockIdStruct.newInstance();
				valueBlockIdClone.pool = retValueBid.pool;
				valueBlockIdClone.block = retValueBid.block;
				valueBlockIdClone.write();
				
				short dataType = retDataType.getValue();
	
				JNAItem newItem = new JNAItem(this, itemBlockIdForItemCreation, dataType,
						valueBlockIdClone);
				if (newItem.isReaders()) {
					readerFields.add(newItem);
				}
				
				itemBlockIdByVal.pool = retItemBlockId.pool;
				itemBlockIdByVal.block = retItemBlockId.block;
				itemBlockIdByVal.write();
			}
		});
		
		return readerFields;
	}
	
	@Override
	public int getResponseCount() {
		checkDisposed();
		
		JNADocumentAllocations allocations = getAllocations();
		
		DisposableMemory retResponseCount = new DisposableMemory(4);
		try {
			retResponseCount.clear();

			return LockUtil.lockHandle(allocations.getNoteHandle(), (handleByVal) -> {
				NotesCAPI.get().NSFNoteGetInfo(handleByVal, NotesConstants._NOTE_RESPONSE_COUNT, retResponseCount);
				return retResponseCount.getInt(0);
			});
		}
		finally {
			retResponseCount.dispose();
		}
	}
	
	@Override
	public IDTable getResponses() {
		checkDisposed();
		
		JNADocumentAllocations allocations = getAllocations();

		DHANDLE dhandle = LockUtil.lockHandle(allocations.getNoteHandle(), (handleByVal) -> {
			DHANDLE ret = DHANDLE.newInstanceByValue();
			NotesCAPI.get().NSFNoteGetInfo(handleByVal, NotesConstants._NOTE_RESPONSES, ret.getAdapter(Pointer.class));
			ret.getAdapter(Structure.class).read();
			return ret;
		});
		
		if(dhandle.isNull()) {
			return getParentDominoClient().createIDTable();
		} else {
			JNAIDTable responseTable = new JNAIDTable(getParentDominoClient(), dhandle, true);
			//create a copy of the table because the original instance will get freed via NSFNoteClose
			//and we don't want users to store and access this id table after document disposal
			JNAIDTable responseTableCopy = (JNAIDTable) responseTable.clone();
			return responseTableCopy;
		}
	}

	@Override
	public boolean hasMIME() {
		checkDisposed();
		
		return LockUtil.lockHandle(getAllocations().getNoteHandle(), (noteHandleByVal) -> {
			return NotesCAPI.get().NSFNoteHasMIME(noteHandleByVal) == 1;
		});
	}

	@Override
	public boolean hasMIMEPart() {
		checkDisposed();
		
		return LockUtil.lockHandle(getAllocations().getNoteHandle(), (noteHandleByVal) -> {
			return NotesCAPI.get().NSFNoteHasMIMEPart(noteHandleByVal) == 1;
		});
	}

	@Override
	public boolean hasComposite() {
		checkDisposed();
		
		return LockUtil.lockHandle(getAllocations().getNoteHandle(), (noteHandleByVal) -> {
			return NotesCAPI.get().NSFNoteHasComposite(noteHandleByVal) == 1;
		});
	}

	@Override
	public boolean hasItem(String itemName) {
		checkDisposed();
		
		Memory itemNameMem = NotesStringUtils.toLMBCS(itemName, false);

		JNADocumentAllocations allocations = getAllocations();
		short result = LockUtil.lockHandle(allocations.getNoteHandle(), (noteHandleByVal) -> {
			short name_len = itemNameMem == null ? 0 : (short)(itemNameMem.size() & 0xffff);
			return NotesCAPI.get().NSFItemInfo(noteHandleByVal, itemNameMem, name_len,
					null, null, null, null);
			
		});
		return result == 0;	
	}

	@Override
	public List<String> getItemNames() {
		Set<String> itemNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		getItems("", item -> { //$NON-NLS-1$
			itemNames.add(item.getName());
			return IItemCallback.Action.Continue;
		});
		
		return new ArrayList<>(itemNames);
	}
	
	@Override
	public String getSigner() {
		try {
			SignatureData signatureData = verifySignature();
			
			return signatureData.getSigner();
		}
		catch (DominoException e) {
			if (e.getId()==INotesErrorConstants.ERR_NOTE_NOT_SIGNED) {
				return ""; //$NON-NLS-1$
			}
			else {
				throw e;
			}
		}
	}

	/**
	 * This function verifies a signature on a note or section(s) within a note.<br>
	 * It returns an error if a signature did not verify.<br>
	 * <br>

	 * @return signer data
	 */
	@Override
	public SignatureData verifySignature() {
		checkDisposed();
		
		NotesTimeDateStruct retWhenSigned = NotesTimeDateStruct.newInstance();
		Memory retSigner = new Memory(NotesConstants.MAXUSERNAME);
		Memory retCertifier = new Memory(NotesConstants.MAXUSERNAME);
		
		JNADocumentAllocations allocations = getAllocations();
		LockUtil.lockHandle(allocations.getNoteHandle(), (noteHandleByVal) -> {
			short result = NotesCAPI.get().NSFNoteExpand(noteHandleByVal);
			NotesErrorUtils.checkResult(result);

			result = NotesCAPI.get().NSFNoteVerifySignature (noteHandleByVal, null, retWhenSigned, retSigner, retCertifier);
			NotesErrorUtils.checkResult(result);
			
			result = NotesCAPI.get().NSFNoteContract(noteHandleByVal);
			NotesErrorUtils.checkResult(result);
			
			return 0;
		});

		String signer = NotesStringUtils.fromLMBCS(retSigner, NotesStringUtils.getNullTerminatedLength(retSigner));
		String certifier = NotesStringUtils.fromLMBCS(retCertifier, NotesStringUtils.getNullTerminatedLength(retCertifier));
		SignatureData data = new SignatureDataImpl(new JNADominoDateTime(retWhenSigned.Innards), signer, certifier);
		return data;
	}
	
	@Override
	public boolean isSigned() {
		checkDisposed();
		
		ByteByReference signed_flag_ptr = new ByteByReference();
		ByteByReference sealed_flag_ptr = new ByteByReference();
		
		JNADocumentAllocations allocations = getAllocations();
		return LockUtil.lockHandle(allocations.getNoteHandle(), (noteHandleByVal) -> {
			NotesCAPI.get().NSFNoteIsSignedOrSealed(noteHandleByVal, signed_flag_ptr, sealed_flag_ptr);
			byte signed = signed_flag_ptr.getValue();
			return signed == 1;
		});
	}

	@Override
	public boolean isNew() {
		return getNoteID() == 0;
	}

	@Override
	public boolean isEncrypted() {
		checkDisposed();
		
		ByteByReference signed_flag_ptr = new ByteByReference();
		ByteByReference sealed_flag_ptr = new ByteByReference();
		
		JNADocumentAllocations allocations = getAllocations();
		return LockUtil.lockHandle(allocations.getNoteHandle(), (noteHandleByVal) -> {
			NotesCAPI.get().NSFNoteIsSignedOrSealed(noteHandleByVal, signed_flag_ptr, sealed_flag_ptr);
			byte sealed = sealed_flag_ptr.getValue();
			return sealed == 1;
		});
	}

	@Override
	public boolean isTruncated() {
		int flags = getFlags();
		return (flags & NotesConstants.NOTE_FLAG_ABSTRACTED) == NotesConstants.NOTE_FLAG_ABSTRACTED;
	}
	
	@Override
	public Document removeItem(String itemName) {
		checkDisposed();
		
		Memory itemNameMem = NotesStringUtils.toLMBCS(itemName, false);
		
		JNADocumentAllocations allocations = getAllocations();
		short result = LockUtil.lockHandle(allocations.getNoteHandle(), (noteHandleByVal) -> {
		  //removes all items with this name (not just the first):
			return NotesCAPI.get().NSFItemDelete(noteHandleByVal, itemNameMem, (short) (itemNameMem.size() & 0xffff));

		});
		if (result==INotesErrorConstants.ERR_ITEM_NOT_FOUND) {
			return this;
		}
		NotesErrorUtils.checkResult(result);
		return this;
	}

	
	@Override
	public <T> T get(String itemName, Class<T> valueType, T defaultValue) {
		return m_typedAccess.get(itemName, valueType, defaultValue);
	}
	
	@Override
	public <T> List<T> getAsList(String itemName, Class<T> valueType, List<T> defaultValue) {
		return m_typedAccess.getAsList(itemName, valueType, defaultValue);
	}
	
	@Override
	public <T> Optional<T> getOptional(String itemName, Class<T> valueType) {
	  return m_typedAccess.getOptional(itemName, valueType);
	}
	
	@Override
	public <T> Optional<List<T>> getAsListOptional(String itemName, Class<T> valueType) {
	  return m_typedAccess.getAsListOptional(itemName, valueType);
	}
	
	private String[] parseProfileAndUserName() {
		String name = get("$name", String.class, ""); //$profile_015calendarprofile_<username> //$NON-NLS-1$ //$NON-NLS-2$
		if (StringUtil.isEmpty(name) || !name.startsWith("$profile_")) { //$NON-NLS-1$
			return null;
		}

		String remainder = name.substring(9); //"$profile_".length()
		if (remainder.length()<3) {
			return null;
		}
		
		String profileNameLengthStr = remainder.substring(0, 3);
		int profileNameLength = Integer.parseInt(profileNameLengthStr);
		
		remainder = remainder.substring(3);
		String profileName = remainder.substring(0, profileNameLength);
		
		remainder = remainder.substring(profileNameLength+1);
		
		String userName = remainder;
		
		return new String[] {profileName, userName};
	}
	
	@Override
	public String getProfileName() {
		if (isHiddenFromViews()) {
			String[] profileAndUsername = parseProfileAndUserName();
			if (profileAndUsername!=null) {
				return profileAndUsername[0];
			}
		}
		return ""; //$NON-NLS-1$
	}
	
	@Override
	public String getProfileUserName() {
		if (isHiddenFromViews()) {
			String[] profileAndUsername = parseProfileAndUserName();
			if (profileAndUsername!=null) {
				return profileAndUsername[1];
			}
		}
		return ""; //$NON-NLS-1$
	}
	
	/**
	 * Writes a primary key information to the note. This primary key can be used for
	 * efficient note retrieval without any lookup views.<br>
	 * <br>
	 * Both <code>category</code> and <code>objectKey</code> are combined
	 * to a string that is expected to be unique within the database.
	 * 
	 * @param category category part of primary key
	 * @param objectId object id part of primary key
	 */
	@Override
	public Document setPrimaryKey(String category, String objectId) {
		String name = JNADatabase.getApplicationNoteName(category, objectId);
		return replaceItemValue("$name", name); //$NON-NLS-1$
	}

	/**
	 * Returns the category part of the note primary key
	 * 
	 * @return category or empty string if no primary key has been assigned
	 */
	@Override
	public String getPrimaryKeyCategory() {
		String name = get("$name", String.class, ""); //$NON-NLS-1$ //$NON-NLS-2$
		if (!StringUtil.isEmpty(name)) {
			String[] parsedParts = JNADatabase.parseApplicationNamedNoteName(name);
			if (parsedParts!=null) {
				return parsedParts[0];
			}
		}
		
		return ""; //$NON-NLS-1$
	}
	
	/**
	 * Returns the object id part of the note primary key
	 * 
	 * @return object id or empty string if no primary key has been assigned
	 */
	@Override
	public String getPrimaryKeyObjectId() {
		String name = get("$name", String.class, ""); //$NON-NLS-1$ //$NON-NLS-2$
		if (!StringUtil.isEmpty(name)) {
			String[] parsedParts = JNADatabase.parseApplicationNamedNoteName(name);
			if (parsedParts!=null) {
				return parsedParts[1];
			}
		}
		
		return ""; //$NON-NLS-1$
	}
	
	@Override
	public boolean isHiddenFromViews() {
		int flags = getFlags();
		return (flags & NotesConstants.NOTE_FLAG_GHOST) == NotesConstants.NOTE_FLAG_GHOST;
	}

	/**
	 * Changes the note's ghost flag. Ghost notes do not appear in any view or search.
	 * 
	 * @param b true if ghost
	 */
	void setHiddenFromViews(boolean b) {
		short flags = getFlags();
		short newFlags;
		
		if (b) {
			if ((flags & NotesConstants.NOTE_FLAG_GHOST) == NotesConstants.NOTE_FLAG_GHOST) {
				return;
			}
			
			newFlags = (short) ((flags | NotesConstants.NOTE_FLAG_GHOST) & 0xffff);
		}
		else {
			if ((flags & NotesConstants.NOTE_FLAG_GHOST) == 0) {
				return;
			}
			
			newFlags = (short) ((flags & ~NotesConstants.NOTE_FLAG_GHOST) & 0xffff);
		}
		
		setFlags(newFlags);
	}
	
	@Override
	public String toStringLocal() {
		if (isDisposed()) {
			return "JNADocument [disposed]"; //$NON-NLS-1$
		}
		else {
			return format(
				"JNADocument [handle={0}, unid={1}, noteid={2}]", //$NON-NLS-1$
				getAllocations().getNoteHandle(), getUNID(), getNoteID()
			);
		}
	}
	
	@Override
	public boolean isSaveMessageOnSend() {
		return m_saveMessageOnSend;
	}
	
	@Override
	public Document setSaveMessageOnSend(boolean b) {
		m_saveMessageOnSend = b;
		return this;
	}
	
	@Override
	public Document send() {
		return send(false, (Collection<String>) null);
	}

	@Override
	public Document send(String recipient) {
		return send(false, Arrays.asList(recipient));
	}

	@Override
	public Document send(Collection<String> recipients) {
		return send(false, recipients);
	}

	@Override
	public Document send(boolean attachform) {
		return send(attachform, (Collection<String>) null);
	}

	@Override
	public Document send(boolean attachform, String recipient) {
		return send(attachform, Arrays.asList(recipient));
	}

	/**
	 * Clones the document without items
	 * 
	 * @return clone without items
	 */
	private JNADocument cloneDocument() {
		checkDisposed();
		
		return LockUtil.lockHandle(getAllocations().getNoteHandle(), (hNoteByVal) -> {
			DHANDLE.ByReference rethDstNote = DHANDLE.newInstanceByReference();
			short result = NotesCAPI.get().NSFNoteCreateClone(hNoteByVal, rethDstNote);
			NotesErrorUtils.checkResult(result);
			
			return new JNADocument((JNADatabase) getParentDatabase(), rethDstNote);
		});
	}

	/**
	 * copy specified items from form when mailing
	 * 
	 * @param formNote form note
	 * @param tmpNote target note
	 * @param excludeList exclude list of item names
	 */
	private void copyFormItems(JNADocument formNote, JNADocument tmpNote, List<String> excludeList) {
		copyFormItems(formNote, tmpNote, excludeList, (short) 0, null);
	}
	
	/**
	 * copy specified items from form when mailing
	 * 
	 * @param formNote form note
	 * @param tmpNote target note
	 * @param excludeList exclude list of item names
	 * @param namemod counter
	 * @param subformname subform name or null
	 */
	private void copyFormItems(JNADocument formNote, JNADocument tmpNote, List<String> excludeList,
			short namemod, String subformname) {
		
		formNote.checkDisposed();
		tmpNote.checkDisposed();
		
		// table of items which get renamed, if this is a subform copy
		List<String> rename_list = Arrays.asList(
				NotesConstants.FORM_SCRIPT_ITEM_NAME,
				NotesConstants.DOC_SCRIPT_ITEM,
				NotesConstants.DOC_SCRIPT_NAME,
				NotesConstants.DOC_ACTION_ITEM
				
				);
		
		/* These are the object-code item names, derived
		from the ones above (prepended '$', appended "_O") */
		List<String> rename_list_special = Arrays.asList(
				"$" + NotesConstants.FORM_SCRIPT_ITEM_NAME + "_O", //$NON-NLS-1$ //$NON-NLS-2$
				"$" + NotesConstants.DOC_SCRIPT_ITEM + "_O" //$NON-NLS-1$ //$NON-NLS-2$
				);
		
		/* Copy all items beginning with '$' from the form note to 
		 * the current note. Certain item names are skipped for the
		 * form ($FLAGS, $CLASS, $UPDATEDBY) and for subforms (indicated by
		 * namemod > 0) ($TITLE, $BODY).
		 * 
		 * If the "namemod" argument is > 0, then we also have to make
		 * the new item name different: append the integer to the name
		 * again, for selected item names.
		 */
		formNote.forEachItem((item, loop) -> {

			String itemName = item.getName();
			if (itemName.startsWith("$")) { //$NON-NLS-1$
				boolean exclude = false;
				
				if (ListUtil.containsIgnoreCase(excludeList, itemName)) {
					exclude = true;
				}
				else if (NotesConstants.ITEM_NAME_NOTE_SIGNATURE.equals(itemName)) {
					exclude = true;
				}
				
				if (!exclude) {
					/* If the item's name is not being modified, then use the
					easy ItemCopy call. If it is, though, we have to do a
					bit more work. If this is a subform, check the rename
					list to see if it really is getting renamed.

					Note that there are script object-code items with the
					same name as the source-code items, but with an appended
					"_O". If we find one of those, then we have to specially
					rename it, the number goes before the _O, not after
				*/
					
					boolean rename = false;
					boolean rename_special = false;
					boolean rename_signature = false;

					String itemname2 = ""; //$NON-NLS-1$
					
					if (NotesConstants.ITEM_NAME_NOTE_SIGNATURE.equalsIgnoreCase(itemName)) {
						rename = true;
						rename_signature = true;
						
						if (namemod == 0) {
							itemname2 = NotesConstants.ITEM_NAME_NOTE_STOREDFORM_SIG;
						}
						else {
							itemname2 = NotesConstants.ITEM_NAME_NOTE_STOREDFORM_SIG_PREFIX + subformname;
							if (itemname2.length() > NotesConstants.MAXPATH) {
								itemname2 = itemname2.substring(0, NotesConstants.MAXPATH);
							}
						}
						
					}
					
					if (namemod>0 && !rename_signature) {
						if (ListUtil.containsIgnoreCase(rename_list, itemName)) {
							rename = true;
						}
						else if (ListUtil.containsIgnoreCase(rename_list_special, itemName)) { // check for special
							rename = true;
							rename_special = true;
						}
					}
					
					// "special" rename?
					if (rename_special) {
						itemname2 = itemName.substring(0, itemName.length()-2); // trim _O
						itemname2 += Short.toString(namemod);
					}
					else if (!rename_signature) {
						itemname2 = itemName + Short.toString(namemod);
					}
					
					if (rename) {
						//we need to check if this call does the same as the C code below
						item.copyToDocument(tmpNote, itemname2, false);
					}
					else {
						item.copyToDocument(tmpNote, false);
					}
				}
			}
		
		});
	}

	/**
	 * copy relevant subform items
	 * 
	 * @param formNote source note
	 * @param tmpNote target note
	 */
	private void copySubformItems(JNADocument formNote, JNADocument tmpNote) {
		checkDisposed();
		
		JNADatabase parentDb = (JNADatabase) getParentDatabase();
		JNADatabaseAllocations parentDbAllocations = (JNADatabaseAllocations) parentDb.getAdapter(APIObjectAllocations.class);
		parentDbAllocations.checkDisposed();
		
		List<String> exclude_list = Arrays.asList(
				NotesConstants.DESIGN_CLASS,
				NotesConstants.DESIGN_FLAGS,
				NotesConstants.FIELD_UPDATED_BY,
				NotesConstants.ITEM_NAME_TEMPLATE,
				NotesConstants.FIELD_TITLE,
				NotesConstants.ITEM_NAME_DOCUMENT
				);
		
		/* If the form doesn't contain a subform name list, then there's nothing to do */
		if (!formNote.hasItem(NotesConstants.SUBFORM_ITEM_NAME)) {
			return;
		}
		
		// get the list, iterate over the subform names
		List<String> subformNames = formNote.getAsList(NotesConstants.SUBFORM_ITEM_NAME, String.class, Collections.emptyList());
		for (int i = 0; i < subformNames.size(); i++) {
			String subformname = subformNames.get(i);
			Memory subformnameMem = NotesStringUtils.toLMBCS(subformname, true);
			
			if (NotesCAPI.get().StoredFormHasSubformToken(subformnameMem)) {
				continue;
			}
			
			PointerByReference ppName = new PointerByReference();
			ShortByReference pNameLen = new ShortByReference();
			PointerByReference ppAlias = new PointerByReference();
			ShortByReference pAliasLen = new ShortByReference();
			
			NotesCAPI.get().DesignGetNameAndAlias(subformnameMem,
					ppName, pNameLen, ppAlias, pAliasLen);
			
			Pointer subformaliasMem;
			short subformaliasLen;
			
			if (Short.toUnsignedInt(pAliasLen.getValue()) > 0) {
				subformaliasMem = ppAlias.getValue();
				subformaliasLen = pAliasLen.getValue();
			}
			else {
				subformaliasMem = ppName.getValue();
				subformaliasLen = pNameLen.getValue();
			}
			String subformalias = NotesStringUtils.fromLMBCS(subformaliasMem, Short.toUnsignedInt(subformaliasLen));
			
			Memory designPatternMem = NotesStringUtils.toLMBCS(NotesConstants.DFLAGPAT_SUBFORM_ALL_VERSIONS, true);
			
			IntByReference fnid = new IntByReference();
			
			short lkSubformResult = LockUtil.lockHandle(parentDbAllocations.getDBHandle(), (hDbByVal) -> {
				return NotesCAPI.get().DesignLookupNameFE(hDbByVal, NotesConstants.NOTE_CLASS_FORM,
						designPatternMem, subformaliasMem, subformaliasLen, NotesConstants.DGN_ONLYSHARED,
						fnid, null, null, null);
			});
			
			if (lkSubformResult!=0) {
				/* Try private. */
				lkSubformResult = LockUtil.lockHandle(parentDbAllocations.getDBHandle(), (hDbByVal) -> {
					return NotesCAPI.get().DesignLookupNameFE(hDbByVal, NotesConstants.NOTE_CLASS_FORM,
							designPatternMem, subformaliasMem, subformaliasLen, NotesConstants.DGN_ONLYPRIVATE,
							fnid, null, null, null);
				});
			}
			
			if (lkSubformResult!=0) {
				continue;
			}

			try {
				JNADocument subformNote = (JNADocument) parentDb.getDocumentById(fnid.getValue(), EnumSet.of(OpenDocumentMode.CACHE)).orElse(null);
				if (subformNote!=null) {
					copyFormItems(subformNote, tmpNote, exclude_list, (short) ((i+2) & 0xffff), subformname);
				}
			}
			catch (DominoException e) {
				throw new DominoException(e.getId(), format("Error opening subform {0}", subformalias), e);
			}
		}
	}

	/**
	 * detect any MIME_PART items not named "Body"; if not found, detect any TYPE_MIME_PART item named "Body"
	 * 
	 * @param foundNonBodyMIME returns {@link Boolean#TRUE} if there are TYPE_MIME_PART items with a different name than "body"
	 * @param foundBodyMIME returns {@link Boolean#TRUE} if there are TYPE_MIME_PART items with the name than "body"
	 */
	private void searchForNonBodyMIME(Ref<Boolean> foundNonBodyMIME, Ref<Boolean> foundBodyMIME) {
		foundNonBodyMIME.set(Boolean.FALSE);
		foundBodyMIME.set(Boolean.FALSE);
		
		/* Optimization: assume by this point that if there is no $NoteHasNativeMIME item, then
		 * there aren't any MIME_PART items at all.
		 */	
		if (!hasItem(NotesConstants.ITEM_IS_NATIVE_MIME)) {
			return;
		}
		
		/* Finally, scan all the items, looking for non-"Body" item of TYPE_MIME_PART */
		forEachItem((item, loop) -> {
			if (item.getType() == ItemDataType.TYPE_MIME_PART) {
				String itemName = item.getName();
				if (NotesConstants.MAIL_BODY_ITEM.equalsIgnoreCase(itemName)) {
					// don't stop here; finding MIME "Body" is secondary to finding MIME non-"Body"s
					foundBodyMIME.set(Boolean.TRUE);
				}
				else {
					foundNonBodyMIME.set(Boolean.TRUE);
					loop.stop();
				}
			}
		});
	}

	@Override
	public Document send(boolean attachform, Collection<String> recipients) {
		checkDisposed();
		
		JNADatabase parentDb = (JNADatabase) getParentDatabase();
		
		if (parentDb.isDisposed()) {
			throw new ObjectDisposedException(parentDb);
		}
		
		short flags = 0;
		
		if (this.isSigned() || attachform) {
			flags |= NotesConstants.MSN_SIGN;
		}

		if (this.isEncrypted()) {
			flags |= NotesConstants.MSN_SEAL;
		}
		
		boolean cancelsend = false;
		if (cancelsend) {
			flags |= NotesConstants.MSN_PUBKEY_ONLY;
		}

		Ref<Boolean> foundNonBodyMIME = new Ref<>();
		Ref<Boolean> foundBodyMIME = new Ref<>();
		this.searchForNonBodyMIME(foundNonBodyMIME, foundBodyMIME);
		
		if (Boolean.TRUE.equals(foundNonBodyMIME.get())) {
			throw new DominoException("Found items of type MIME_PART that are not named 'Body'. This is currently unsupported.");
		}
		
		short wMailNoteFlags = NotesConstants.MAILNOTE_ANYRECIPIENT;
		
		if (Boolean.TRUE.equals(foundBodyMIME.get())) {
			wMailNoteFlags |= NotesConstants.MAILNOTE_MIMEBODY;
			wMailNoteFlags |= NotesConstants.MAILNOTE_NOTES_ENCRYPT_MIME;
		}
		
		/* SPR ajrs39vm4l: We're about to modify the user's note.
		 * If they do something stupid, like send it a second time,
		 * we're going to modify it again, ending up with all sorts
		 * of duplicate items that will cause problems for the recipient.
		 * So, we fix that by cloning the hnote here, making all
		 * modifications on the copy.
		 * 
		 * Note that we use NULL for the hdb when creating the note
		 * to prevent a network round trip. Then we have to set the
		 * hdb explicitly into the note. Later, after sending the thing,
		 * we have to check to see if the original hnote needs updating
		 * (for example, if the current note is not on disk, but the
		 * user specified "save on send", then we have to update the
		 * note id).
		 */

		//create a clone to not modify this note instance and prevent issues when sending a second time
		JNADocument tmpDoc = cloneDocument();
		
		JNADocumentAllocations docAllocations = getAllocations();
		JNADocumentAllocations tmpDocAllocations = (JNADocumentAllocations) tmpDoc.getAdapter(APIObjectAllocations.class);
		
		// now copy all items to the new note
		short copyItemsResult = LockUtil.lockHandles(
				docAllocations.getNoteHandle(), tmpDocAllocations.getNoteHandle(),
				(hNoteByVal, hTmpNoteByVal) -> {
			
				return NotesCAPI.get().NSFNoteReplaceItems(hNoteByVal, hTmpNoteByVal,
							null, true);
		});
		NotesErrorUtils.checkResult(copyItemsResult);
		
		// did caller provide a recipients list?
		if (recipients!=null) {
			// if there's already a SendTo item, delete it
			tmpDoc.getFirstItem(NotesConstants.MAIL_SENDTO_ITEM).ifPresent(Item::remove);

			recipients = NotesNamingUtils.toCanonicalNames(recipients);
			tmpDoc.replaceItemValue(NotesConstants.MAIL_SENDTO_ITEM, recipients);
		}

		if (tmpDoc.hasItem(NotesConstants.MAIL_COPYTO_ITEM)) {
			List<String> copyToNames = tmpDoc.getAsList(NotesConstants.MAIL_COPYTO_ITEM, String.class, Collections.emptyList());
			copyToNames = NotesNamingUtils.toCanonicalNames(copyToNames);
			tmpDoc.replaceItemValue(NotesConstants.MAIL_COPYTO_ITEM, copyToNames);
		}
		
		if (tmpDoc.hasItem(NotesConstants.MAIL_BLINDCOPYTO_ITEM)) {
			List<String> blindCopyToNames = tmpDoc.getAsList(NotesConstants.MAIL_COPYTO_ITEM, String.class, Collections.emptyList());
			blindCopyToNames = NotesNamingUtils.toCanonicalNames(blindCopyToNames);
			tmpDoc.replaceItemValue(NotesConstants.MAIL_BLINDCOPYTO_ITEM, blindCopyToNames);
		}

		if (!tmpDoc.hasItem(NotesConstants.MAIL_SENDTO_ITEM) && !tmpDoc.hasItem(NotesConstants.MAIL_COPYTO_ITEM) &&
				!tmpDoc.hasItem(NotesConstants.MAIL_BLINDCOPYTO_ITEM)) {
			throw new DominoException(0, "Missing mail recipient items");
		}

		/* To attach the form, find it in the database, get the all
		 * items from the form note beginning with '$' (with some
		 * exceptions), and copy them to the mail note. Then,
		 * we have to delete the Form item, which points to the original
		 * form name. By deleting it, we're telling the editor to look
		 * for the stored form instead.
		 * 
		 * If the form contains a SUBFORM_ITEM (textlist of the
		 * names of subforms used in the form), then we have
		 * to copy a bunch of sub-form stuff too
		 */

		if (attachform) {
			// Remove old stored form items before adding items from another form
			short removeStoredFormResult = LockUtil.lockHandle(tmpDocAllocations.getNoteHandle(), (hTmpNoteByVal) -> {
				return NotesCAPI.get().StoredFormRemoveItems(hTmpNoteByVal, 0);
			});
			NotesErrorUtils.checkResult(removeStoredFormResult);
			
			Item itmForm = tmpDoc.getFirstItem(NotesConstants.FIELD_FORM).orElse(null);
			Item itmBody = tmpDoc.getFirstItem(NotesConstants.ITEM_NAME_TEMPLATE).orElse(null);
			
			IntByReference retFormNoteId = new IntByReference();
			retFormNoteId.setValue(0);
			
			if (itmForm!=null) {
				String formnameStr = tmpDoc.get(NotesConstants.FIELD_FORM, String.class, ""); //$NON-NLS-1$
				/* Delete the form item from the note, we copied the name.
				 * We don't want a Form item on the note when we attach
				 * the form itself, otherwise the editor gets confused.
				 * Doing the Remove also deletes the object. */
				itmForm.remove();
				
				if (!StringUtil.isEmpty(formnameStr)) {
					Memory formnameStrMem = NotesStringUtils.toLMBCS(formnameStr, true);
					PointerByReference pName = new PointerByReference();
					ShortByReference wNameLen = new ShortByReference();
					PointerByReference pAlias = new PointerByReference();
					ShortByReference wAliasLen = new ShortByReference();
					
					NotesCAPI.get().DesignGetNameAndAlias(formnameStrMem, pName, wNameLen, pAlias, wAliasLen);
					
					DisposableMemory szBuffer = new DisposableMemory(NotesConstants.DESIGN_ALL_NAMES_MAX);
					szBuffer.clear();
					
					if (wAliasLen.getValue()>0) {
						byte[] aliasArr = pAlias.getValue().getByteArray(0, wAliasLen.getValue() & 0xffff);
						szBuffer.write(0, aliasArr, 0, aliasArr.length);
					}
					else {
						byte[] wNameArr = pName.getValue().getByteArray(0, wNameLen.getValue() & 0xffff);
						szBuffer.write(0, wNameArr, 0, wNameArr.length);
					}
					
					short lkFormResult;
					Memory szFlagsPatternMem = NotesStringUtils.toLMBCS(NotesConstants.DFLAGPAT_VIEWFORM_ALL_VERSIONS, true);
					
					JNADatabaseAllocations dbAllocations = (JNADatabaseAllocations) getParentDatabase().getAdapter(APIObjectAllocations.class);
					
					lkFormResult = LockUtil.lockHandle(dbAllocations.getDBHandle(), (hDbByVal) -> {
						return NotesCAPI.get().DesignLookupNameFE(hDbByVal,
								NotesConstants.NOTE_CLASS_FORM, szFlagsPatternMem,
								szBuffer, wAliasLen.getValue()>0 ? wAliasLen.getValue() : wNameLen.getValue(),
										NotesConstants.DGN_ONLYSHARED, retFormNoteId, (IntByReference) null, 
										(NotesCallbacks.DESIGN_COLL_OPENCLOSE_PROC) null, null);
					});
					
					if (lkFormResult!=0) {
						/* Try private. */
						lkFormResult = LockUtil.lockHandle(dbAllocations.getDBHandle(), (hDbByVal) -> {
							return NotesCAPI.get().DesignLookupNameFE(hDbByVal,
									NotesConstants.NOTE_CLASS_FORM, szFlagsPatternMem,
									szBuffer, wAliasLen.getValue()>0 ? wAliasLen.getValue() : wNameLen.getValue(),
											NotesConstants.DGN_ONLYPRIVATE, retFormNoteId, (IntByReference) null, 
											(NotesCallbacks.DESIGN_COLL_OPENCLOSE_PROC) null, null);
						});
					}

					if (lkFormResult==0) {
						// delete existing $body
						while (tmpDoc.hasItem(NotesConstants.ITEM_NAME_TEMPLATE)) {
							tmpDoc.removeItem(NotesConstants.ITEM_NAME_TEMPLATE);
						}

						JNADocument formNote = (JNADocument) getParentDatabase().getDocumentById(retFormNoteId.getValue(), EnumSet.of(OpenDocumentMode.CACHE)).orElse(null);
						if (formNote!=null) {
							List<String> excludeList = Arrays.asList(
									NotesConstants.DESIGN_CLASS,
									NotesConstants.DESIGN_FLAGS,
									NotesConstants.FIELD_UPDATED_BY);
							
							// do the $ items
							copyFormItems(formNote, tmpDoc, excludeList);
							
							// check for subforms
							copySubformItems(formNote, tmpDoc);
							
							// this will add the new, more secure style of stored form and subform items
							// to the target doc
							// Note: This has to be done after $Body item(s) have been added to the note
							// by copyFormItems

							JNADocumentAllocations formDocAllocations = (JNADocumentAllocations) formNote.getAdapter(APIObjectAllocations.class);
							
							short addStoredFormResult = LockUtil.lockHandles(
									dbAllocations.getDBHandle(),
									formDocAllocations.getNoteHandle(),
									tmpDocAllocations.getNoteHandle(),
									(hDbByVal, hFormNoteByVal, hTmpNoteByVal) -> {
								return NotesCAPI.get().StoredFormAddItems(hDbByVal,
										hFormNoteByVal,
										hTmpNoteByVal, true, 0);
							});
							NotesErrorUtils.checkResult(addStoredFormResult);
							
							formNote.dispose();
						}
					}
				}
			}
			else {
				/* If have body and blank form, use existing body */
				if (itmBody==null) {
					throw new DominoException("Found no form name in the document to look up the form to attach");
				}
			}
		}
		
		final boolean isMsgComingFromAgent = false;
		if (isMsgComingFromAgent) {
			if (!tmpDoc.hasItem(NotesConstants.ASSIST_MAIL_ITEM)) {
				tmpDoc.replaceItemValue(NotesConstants.ASSIST_MAIL_ITEM, "1"); //$NON-NLS-1$
			}
		}
		
		/* IETF standard auto flag  */
		tmpDoc.replaceItemValue(NotesConstants.MAIL_ITEM_AUTOSUBMITTED, NotesConstants.MAIL_AUTOGENERATED);
		
		/* If this is happening on a server, then we want to tag the
		 * message as being from the effective user, not from the server.
		 * Always check for the special "from" item, though, in case the
		 * user is getting tricky with us */

		// if there's already a From item, delete it
		tmpDoc.getFirstItem(NotesConstants.MAIL_FROM_ITEM).ifPresent(Item::remove);
		
		JNADominoClient parentClient = (JNADominoClient)getParentDominoClient();
		if (parentClient.isOnServer() || Boolean.TRUE.equals(parentClient.getCustomValue("notesnote.sendasotheruser"))) { // we added a flag here to test this in the client //$NON-NLS-1$
			String effUserName = parentClient.getEffectiveUserName();
			if (!StringUtil.isEmpty(effUserName)) {
				effUserName = NotesNamingUtils.toCanonicalName(effUserName);
				
				tmpDoc.replaceItemValue(NotesConstants.MAIL_FROM_ITEM, effUserName);
			}
		}
		
		// Contract before sending to be editor-compatible
		short contractResult = LockUtil.lockHandle(tmpDocAllocations.getNoteHandle(), (hTmpNoteByVal) -> {
			return NotesCAPI.get().NSFNoteContract(hTmpNoteByVal);
		});
		NotesErrorUtils.checkResult(contractResult);

		/*spr bban3kzhk9 -- allow sendto AND/OR copyto AND/OR blindcopyto */
		/* snis6z2taf et al. -- reinstate ability to MIME encrypt, 
		   by flagging any MIME body for the mailer */
		short fFlags = flags;
		short fwMailNoteFlags = wMailNoteFlags;
		short mailNoteResult = LockUtil.lockHandle(tmpDocAllocations.getNoteHandle(), (hTmpNoteByVal) -> {
			return NotesCAPI.get().MailNoteJitEx2(null, hTmpNoteByVal, fFlags, null,
					NotesConstants.MAIL_NO_JIT, fwMailNoteFlags, null, null);
		});
		NotesErrorUtils.checkResult(mailNoteResult);

		/* must replace certain critical item(s) on original note with their new after
		 * mailing values so that the note will appear in 'Sent' view, etc.
		 * Note: wholesale replacement of all items with new values will result in a
		 * regression problem with spr ajrs39vm4l
		 */

		/* here we query the new copy for posted date */
		DominoDateTime postedDate = tmpDoc.get(NotesConstants.MAIL_POSTEDDATE_ITEM, DominoDateTime.class, null);

		removeItem(NotesConstants.MAIL_POSTEDDATE_ITEM);
		if (postedDate!=null) {
			replaceItemValue(NotesConstants.MAIL_POSTEDDATE_ITEM, postedDate);
		}

		// save the msg?
		if (isSaveMessageOnSend()) {
			// Message recall does not work for mails sent from a DIIOP program
			// need to generate message id for the recall feature to work. 
			// Make sure deleting the existing message id if it is present and re-create new one to match with 
			// the one in local mail.box for the recall feature to work.

			if (hasItem(NotesConstants.MAIL_ID_ITEM)) {
				removeItem(NotesConstants.MAIL_ID_ITEM);
			}
			
			DisposableMemory messageId = new DisposableMemory(NotesConstants.MAXPATH+1);
			short setMsgIdResult = LockUtil.lockHandle(docAllocations.getNoteHandle(), (hNoteByVal) -> {
				return NotesCAPI.get().MailSetSMTPMessageID(hNoteByVal, null, messageId, (short) (NotesConstants.MAXPATH & 0xffff));
			});
			NotesErrorUtils.checkResult(setMsgIdResult);
			
			String messageIdStr = NotesStringUtils.fromLMBCS(messageId, -1);
			replaceItemValue(NotesConstants.MAIL_ID_ITEM, messageIdStr);
			
			// we save the original note, not the new one
			save();
		}
	
		// now we can kill the temp doc and reset
		tmpDoc.dispose();
		
		return this;
	}
	
	@Override
	public Document computeWithForm(boolean continueOnError, final ComputeWithFormCallback callback) {
		checkDisposed();

		int dwFlags = continueOnError ? NotesConstants.CWF_CONTINUE_ON_ERROR : 0;

		if (PlatformUtils.is64Bit()) {
			NotesCallbacks.b64_CWFErrorProc errorProc = (pCDField, phase, error, hErrorText, wErrorTextSize, ctx) -> {
				@SuppressWarnings("deprecation")
				DHANDLE hErrorTextObj = new DHANDLE64(hErrorText);

				String errorTxt;
				if (hErrorTextObj.isNull()) {
					errorTxt = ""; //$NON-NLS-1$
				} else {
					errorTxt = LockUtil.lockHandle(hErrorTextObj, (hErrorTextObjByVal) -> {
						Pointer errorTextPtr = Mem.OSLockObject(hErrorTextObjByVal);
						try {
							// TODO find out where this offset 6 comes from; hErrorText is a handle to
							// memory containing the text that caused the error. The handle is a handle to
							// TYPE_TEXT according to C API
							return NotesStringUtils.fromLMBCS(errorTextPtr.share(6), (wErrorTextSize & 0xffff) - 6);
						} finally {
							Mem.OSUnlockObject(hErrorTextObjByVal);
						}
					});
				}

				ComputeWithFormPhase phaseEnum = decodeValidationPhase(phase);

				ComputeWithFormAction action;
				if (callback == null) {
					action = ComputeWithFormAction.ABORT;
				} else {
					DominoException errorEx = NotesErrorUtils.toNotesError(error).orElse(null);

					FormField fieldInfo = readFormField(pCDField);
					action = callback.errorRaised(fieldInfo, phaseEnum, errorTxt, errorEx);
				}
				return action == null ? ComputeWithFormAction.ABORT.getShortVal() : action.getShortVal();
			};

			short result = LockUtil.lockHandle(getAllocations().getNoteHandle(), (hNoteByVal) -> {
				return NotesCAPI.get().NSFNoteComputeWithForm(hNoteByVal, null, dwFlags, errorProc, null);
			});
			NotesErrorUtils.checkResult(result);
		} else {
			NotesCallbacks.b32_CWFErrorProc errorProc;

			if (PlatformUtils.isWin32()) {
				errorProc = (pCDField, phase, error, hErrorText, wErrorTextSize, ctx) -> {
					@SuppressWarnings("deprecation")
					DHANDLE hErrorTextObj = new DHANDLE32(hErrorText);

					String errorTxt;
					if (hErrorTextObj.isNull()) {
						errorTxt = ""; //$NON-NLS-1$
					} else {
						errorTxt = LockUtil.lockHandle(hErrorTextObj, (hErrorTextObjByVal) -> {
							Pointer errorTextPtr = Mem.OSLockObject(hErrorTextObjByVal);
							try {
								// TODO find out where this offset 6 comes from; hErrorText is a handle to
								// memory containing the text that caused the error. The handle is a handle to
								// TYPE_TEXT according to C API
								return NotesStringUtils.fromLMBCS(errorTextPtr.share(6), (wErrorTextSize & 0xffff) - 6);
							} finally {
								Mem.OSUnlockObject(hErrorTextObjByVal);
							}
						});
					}

					ComputeWithFormPhase phaseEnum = decodeValidationPhase(phase);

					ComputeWithFormAction action;
					if (callback == null) {
						action = ComputeWithFormAction.ABORT;
					} else {
						DominoException errorEx = NotesErrorUtils.toNotesError(error).orElse(null);

						FormField fieldInfo = readFormField(pCDField);
						action = callback.errorRaised(fieldInfo, phaseEnum, errorTxt, errorEx);
					}
					return action == null ? ComputeWithFormAction.ABORT.getShortVal() : action.getShortVal();
				};
			} else {
				errorProc = (pCDField, phase, error, hErrorText, wErrorTextSize, ctx) -> {
					@SuppressWarnings("deprecation")
					DHANDLE hErrorTextObj = new DHANDLE32(hErrorText);

					String errorTxt;
					if (hErrorTextObj.isNull()) {
						errorTxt = ""; //$NON-NLS-1$
					} else {
						errorTxt = LockUtil.lockHandle(hErrorTextObj, (hErrorTextObjByVal) -> {
							Pointer errorTextPtr = Mem.OSLockObject(hErrorTextObjByVal);
							try {
								// TODO find out where this offset 6 comes from; hErrorText is a handle to
								// memory containing the text that caused the error. The handle is a handle to
								// TYPE_TEXT according to C API
								return NotesStringUtils.fromLMBCS(errorTextPtr.share(6), (wErrorTextSize & 0xffff) - 6);
							} finally {
								Mem.OSUnlockObject(hErrorTextObjByVal);
							}
						});
					}

					ComputeWithFormPhase phaseEnum = decodeValidationPhase(phase);

					ComputeWithFormAction action;
					if (callback == null) {
						action = ComputeWithFormAction.ABORT;
					} else {
						DominoException errorEx = NotesErrorUtils.toNotesError(error).orElse(null);

						FormField fieldInfo = readFormField(pCDField);
						action = callback.errorRaised(fieldInfo, phaseEnum, errorTxt, errorEx);
					}
					return action == null ? ComputeWithFormAction.ABORT.getShortVal() : action.getShortVal();
				};
			}

			short result = LockUtil.lockHandle(getAllocations().getNoteHandle(), (hNoteByVal) -> {
				return NotesCAPI.get().NSFNoteComputeWithForm(hNoteByVal, null, dwFlags, errorProc, null);
			});
			NotesErrorUtils.checkResult(result);
		}
		
		return this;
	}

	private static FormField readFormField(Pointer pCDField) {
		ByteBuffer cdBuf = pCDField.getByteBuffer(0, MemoryStructureUtil.sizeOf(CDField.class));
		CDField cdFieldStruct = MemoryStructureUtil.forStructure(CDField.class, () -> cdBuf);
		// Re-read with the now-known length
		ByteBuffer cdBuf2 = pCDField.getByteBuffer(0, cdFieldStruct.getHeader().getLength());
		cdFieldStruct = MemoryStructureWrapperService.get().wrapStructure(CDField.class, cdBuf2);
		return new FormFieldImpl(Collections.singleton(cdFieldStruct));
	}

	private ComputeWithFormPhase decodeValidationPhase(short phase) {
		ComputeWithFormPhase phaseEnum = null;
		switch (phase) {
		case NotesConstants.CWF_DV_FORMULA:
			phaseEnum = ComputeWithFormPhase.DEFAULT_VALUE_FORMULA;
			break;
		case NotesConstants.CWF_IT_FORMULA:
			phaseEnum = ComputeWithFormPhase.INPUT_TRANSLATION_FORMULA;
			break;
		case NotesConstants.CWF_IV_FORMULA:
			phaseEnum = ComputeWithFormPhase.INPUT_VALIDATION_FORMULA;
			break;
		case NotesConstants.CWF_COMPUTED_FORMULA: // Also CWF_COMPUTED_FORMULA_LOAD
			phaseEnum = ComputeWithFormPhase.COMPUTED_FIELD_FORMULA;
			break;
		case NotesConstants.CWF_DATATYPE_CONVERSION:
			phaseEnum = ComputeWithFormPhase.DATATYPE_VERIFICATION;
			break;
		case NotesConstants.CWF_COMPUTED_FORMULA_SAVE:
			phaseEnum = ComputeWithFormPhase.COMPUTED_FORMULA_SAVE;
			break;
		default:
			break;
		}

		return phaseEnum;
	}

	@Override
	public boolean isUnread() {
		return getParentDatabase().isDocumentUnread(null, getNoteID());
	}
	
	@Override
	public boolean isUnread(String userName) {
		return getParentDatabase().isDocumentUnread(userName, getNoteID());
	}
	
	@Override
	public boolean convertRichTextItem(String itemName, IRichTextConversion... conversions) {
		return convertRichTextItem(itemName, this, itemName, conversions);
	}
	
	@Override
	public void convertRFC822Items() {
	  short result = LockUtil.lockHandle(getAllocations().getNoteHandle(), (hNoteByVal) -> {
	    boolean isCanonical = (getFlags() & NotesConstants.NOTE_FLAG_CANONICAL) == NotesConstants.NOTE_FLAG_CANONICAL;
      return NotesCAPI.get().MIMEConvertRFC822TextItems(hNoteByVal, isCanonical);
    });
    NotesErrorUtils.checkResult(result);
	}
	
	@Override
	public boolean convertRichTextItem(String itemName, Document targetNote,
			String targetItemName, IRichTextConversion... conversions) {
		checkDisposed();
		
		if (conversions==null || conversions.length==0) {
			return false;
		}
		
		List<RichTextRecord<?>> navFromNote = getRichTextItem(itemName);
		List<RichTextRecord<?>> currNav = navFromNote;
		
		JNARichtextWriter tmpRichText = null;
		for (int i=0; i<conversions.length; i++) {
			IRichTextConversion currConversion = conversions[i];
			if (currConversion.isMatch(currNav)) {
				tmpRichText = new JNARichtextWriter(getParentDominoClient());
				currConversion.convert(currNav, tmpRichText);
				
				List<RichTextRecord<?>> nextNav = tmpRichText.closeAndGetRichTextNavigator();
				currNav = nextNav;
			}
		}
		
		if (tmpRichText!=null) {
			tmpRichText.closeAndCopyToDoc(targetNote, targetItemName);
			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	public boolean isSoftDeleted() {
		return (getFlags2() & NotesConstants.NOTE_FLAG2_SOFT_DELETED) == NotesConstants.NOTE_FLAG2_SOFT_DELETED;
	}
	
	@Override
	public Document setUnread(String userName, boolean unread) {
		if (unread) {
			getParentDatabase().updateUnreadDocumentTable(userName, null, Collections.singleton(getNoteID()));
		}
		else {
			getParentDatabase().updateUnreadDocumentTable(userName, Collections.singleton(getNoteID()), null);
		}
		return this;
	}
	
	@Override
	public Document compileLotusScript() {
		checkDisposed();
		
		JNADatabaseAllocations parentDbAllocations = (JNADatabaseAllocations) getParent().getAdapter(APIObjectAllocations.class);
		parentDbAllocations.checkDisposed();
		
		final LotusScriptCompilationException[] ex = new LotusScriptCompilationException[1];
		short result = LockUtil.lockHandles(parentDbAllocations.getDBHandle(), getAllocations().getNoteHandle(), (hDb, hNote) -> {
			NotesCallbacks.LSCOMPILEERRPROC callback = (pInfo, pCtx) -> {
				int version = Short.toUnsignedInt(pInfo.Version);
				int line = Short.toUnsignedInt(pInfo.Line);
				String errText = ""; //$NON-NLS-1$
				String errFile = ""; //$NON-NLS-1$
				if(!Platform.isMac()) {
					// TODO investigate why these segfault on first pointer access on macOS
					errText = NotesStringUtils.fromLMBCS(pInfo.pErrText, -1);
					errFile = NotesStringUtils.fromLMBCS(pInfo.pErrFile, -1);
				}
				
				ex[0] = new LotusScriptCompilationException(errText, errFile, version, line);
				
				return INotesErrorConstants.NOERROR;
			};
			
			if (PlatformUtils.isWin32()) {
				Win32NotesCallbacks.LSCOMPILEERRPROCWin32 callbackWin32 = (pInfo, pCtx) -> {
					return callback.invoke(pInfo, pCtx);
				};
				return NotesCAPI.get().NSFNoteLSCompileExt(hDb, hNote, 0, callbackWin32, null);
			}
			else {
				return NotesCAPI.get().NSFNoteLSCompileExt(hDb, hNote, 0, callback, null);
			}
		});
		if(ex[0] != null) {
			throw ex[0];
		}
		NotesErrorUtils.checkResult(result);
		
		return this;
	}
	
	@Override
	public String getAsText(String itemName, char separator) {
		checkDisposed();

		DisposableMemory returnBuf = new DisposableMemory(60 * 1024);
		try {
			short txtLengthAsShort = LockUtil.lockHandle(getAllocations().getNoteHandle(), (hNoteByVal) -> {
				Memory itemNameLmbcs = NotesStringUtils.toLMBCS(itemName, true);
				return NotesCAPI.get().NSFItemConvertToText(hNoteByVal, itemNameLmbcs, returnBuf, (short)(60 * 1024), separator);
			});
			int txtLength = txtLengthAsShort & 0xffff;
			if (txtLength==0) {
				return ""; //$NON-NLS-1$
			}
			else {
				return NotesStringUtils.fromLMBCS(returnBuf, txtLength);
			}
			
		} finally {
			returnBuf.dispose();
		}
	}

	@Override
	public int size() {
		checkDisposed();
		
		int[] totalSize = new int[1];
		
		getItems(null, (item) -> {
			totalSize[0] += item.getValueLength();
			
			if (item.getType() == ItemDataType.TYPE_OBJECT) {
				List<Object> fileDataAsList = item.getValue();
				if (!fileDataAsList.isEmpty() && fileDataAsList.get(0) instanceof Attachment) {
					Attachment att = (Attachment) fileDataAsList.get(0);
					totalSize[0] += att.getFileSize();
				}
			}
			
			return IItemCallback.Action.Continue;
		});
		
		return totalSize[0];
	}
	
	@Override
	public Document appendToTextList(String itemName, String value, boolean allowDuplicates) {
		checkDisposed();
		
		Memory itemNameMem = NotesStringUtils.toLMBCS(itemName, true);
		Memory valueMem = NotesStringUtils.toLMBCS(Objects.requireNonNull(value), false);
		if (valueMem.size() > 0xffff) {
			throw new IllegalArgumentException("Value exceeds max size of 65535 bytes");
		}
		short valueLen = (short) (valueMem.size() & 0xffff);

		short result = LockUtil.lockHandle(getAllocations().getNoteHandle(), (hNoteByVal) -> {
			
			return NotesCAPI.get().NSFItemAppendTextList(hNoteByVal, itemNameMem, valueMem,
					valueLen, allowDuplicates);
		});
		NotesErrorUtils.checkResult(result);
		return this;
	}
}
