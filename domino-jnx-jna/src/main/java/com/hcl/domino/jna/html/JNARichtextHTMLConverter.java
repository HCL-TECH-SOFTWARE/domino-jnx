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
package com.hcl.domino.jna.html;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoException;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.html.CommandId;
import com.hcl.domino.commons.html.IHtmlApiReference;
import com.hcl.domino.commons.html.IHtmlApiUrlTargetComponent;
import com.hcl.domino.commons.html.ReferenceType;
import com.hcl.domino.commons.html.TargetType;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.PlatformUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Database.Action;
import com.hcl.domino.data.Document;
import com.hcl.domino.exception.ObjectDisposedException;
import com.hcl.domino.html.EmbeddedImage;
import com.hcl.domino.html.EmbeddedImage.HTMLImageReader;
import com.hcl.domino.html.HtmlConversionResult;
import com.hcl.domino.html.HtmlConvertOption;
import com.hcl.domino.html.RichTextHTMLConverter;
import com.hcl.domino.jna.JNADominoClient;
import com.hcl.domino.jna.data.JNADatabase;
import com.hcl.domino.jna.data.JNADocument;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.JNANotesConstants;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNADatabaseAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNADocumentAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.structs.HtmlAPIReference32Struct;
import com.hcl.domino.jna.internal.structs.HtmlAPIReference64Struct;
import com.hcl.domino.jna.internal.structs.HtmlApi_UrlTargetComponentStruct;
import com.hcl.domino.jna.internal.structs.NoteIdStruct;
import com.hcl.domino.jna.internal.structs.NotesUniversalNoteIdStruct;
import com.hcl.domino.misc.DominoClientDescendant;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.IntByReference;

/**
 * Implementation of {@link RichTextHTMLConverter} to render a document
 * or single item as HTML.
 * 
 * @author Karsten Lehmann
 */
public class JNARichtextHTMLConverter implements RichTextHTMLConverter, DominoClientDescendant {
	private JNADominoClient m_client;
	
	public JNARichtextHTMLConverter(JNADominoClient client) {
		m_client = client;
	}
	
	@Override
	public DominoClient getParentDominoClient() {
		return m_client;
	}
	
	@Override
	public Builder renderItem(Document doc, String itemName) {
		
		if (StringUtil.isEmpty(itemName)) {
			throw new NullPointerException("Item name cannot be empty");
		}
		
		return new JNAHtmlConverterBuilder(doc, itemName);
	}

	@Override
	public Builder render(Document doc) {
		return new JNAHtmlConverterBuilder(doc, null);
	}
	
	@Override
	public Builder render(Database database) {
		return new JNAHtmlConverterBuilder(database);
	}
	
	private class JNAHtmlConverterBuilder implements Builder {
		private final Set<String> options = new LinkedHashSet<>();
		private final Database database;
		private final Document doc;
		private final String itemName;
		private String userAgent;
		
		private JNAHtmlConverterBuilder(Database database) {
			this.database = database;
			this.doc = null;
			this.itemName = null;
		}
		
		private JNAHtmlConverterBuilder(Document doc, String itemName) {
			this.database = doc.getParentDatabase();
			this.doc = doc;
			this.itemName = itemName;
		}

		@Override
		public Builder option(HtmlConvertOption option, String value) {
			Objects.requireNonNull(option, "option cannot be null");
			options.add(option.toOption(value));
			return this;
		}

		@Override
		public Builder option(String option, String value) {
			Objects.requireNonNull(option, "option cannot be null");
			if(option.isEmpty()) {
				throw new IllegalArgumentException("option cannot be empty");
			}
			options.add(option + "=" + StringUtil.toString(value)); //$NON-NLS-1$
			return this;
		}

		@Override
		public Builder options(Collection<String> options) {
			if(options != null && !options.isEmpty()) {
				this.options.addAll(options);
			}
			return this;
		}

		@Override
		public Builder options(Collection<HtmlConvertOption> options, String value) {
			if(options != null && !options.isEmpty()) {
				for (HtmlConvertOption currOption : options) {
					this.options.add(currOption.toOption(value));
				}
			}
			return this;
		}
		
		@Override
		public Builder userAgent(String userAgent) {
			this.userAgent = userAgent;
			return this;
		}

		@Override
		public HtmlConversionResult convert() {
			return internalRenderDocumentOrItemToHTML(this, (Set<ReferenceType>) null, (Map<ReferenceType,Set<TargetType>>) null);
		}
		
	}

	/**
	 * Internal method doing the HTML conversion work
	 * 
	 * @param config the {@link JNAHtmlConverterBuilder} object used to configure the conversion
	 * @param refTypeFilter optional filter for ref types to be returned or null for no filter
	 * @param targetTypeFilter optional filter for target types to be returned or null for no filter
	 * @return conversion result
	 */
	private JNAHtmlConversionResult internalRenderDocumentOrItemToHTML(JNAHtmlConverterBuilder config, Set<ReferenceType> refTypeFilter,
			Map<ReferenceType, Set<TargetType>> targetTypeFilter) {
		Document doc = config.doc;
		
		if (doc != null && !(doc instanceof JNADocument)) {
			throw new IllegalArgumentException("Document must be a JNADocument");
		}
		
		DHANDLE noteHandle = null;
		if(doc instanceof JNADocument) {
			@SuppressWarnings("resource")
			JNADocument jnaDoc = (JNADocument) doc;
			if (jnaDoc.isDisposed()) {
				throw new ObjectDisposedException(jnaDoc);
			}
			if (jnaDoc.isEncrypted()) {
				throw new DominoException("The document is encrypted. Please decrypt the document first before rendering it as HTML.");
			}
			JNADocumentAllocations jnaDocAllocations = (JNADocumentAllocations) doc.getAdapter(APIObjectAllocations.class);
			noteHandle = jnaDocAllocations.getNoteHandle();
		}
		
		if(!(config.database instanceof JNADatabase)) {
			throw new IllegalArgumentException("Database must be a JNADatabase");
		}
		
		JNADatabase jnaDb = (JNADatabase) config.database;
		if (jnaDb.isDisposed()) {
			throw new ObjectDisposedException(jnaDb);
		}
		JNADatabaseAllocations jnaDbAllocations = (JNADatabaseAllocations) jnaDb.getAdapter(APIObjectAllocations.class);
		
		IntByReference phHTML = new IntByReference();
		phHTML.setValue(0);
		
		short result = NotesCAPI.get().HTMLCreateConverter(phHTML);
		NotesErrorUtils.checkResult(result);
		
		int hHTML = phHTML.getValue();
		
		try {
			Collection<String> options = config.options;
			if (options != null && !options.isEmpty()) {
				result = NotesCAPI.get().HTMLSetHTMLOptions(hHTML, new StringArray(options.toArray(new String[options.size()])));
				NotesErrorUtils.checkResult(result);
			}
			String userAgent = config.userAgent;
			if(StringUtil.isNotEmpty(userAgent)) {
				Memory userAgentMem = NotesStringUtils.toLMBCS(userAgent, true);
				NotesCAPI.get().HTMLSetProperty(hHTML, 4, userAgentMem); // TODO add enumeration for the properties
			}

			String itemName = config.itemName;
			Memory itemNameMem = NotesStringUtils.toLMBCS(itemName, true);
			
			int totalLen = LockUtil.lockHandles(jnaDbAllocations.getDBHandle(), noteHandle,
					(hDbByVal, hNoteByVal) -> {
						
				short convertResult;
				if (itemName==null) {
					convertResult = NotesCAPI.get().HTMLConvertNote(hHTML, hDbByVal, hNoteByVal, 0, null);
				}
				else {
					convertResult = NotesCAPI.get().HTMLConvertItem(hHTML, hDbByVal, hNoteByVal, itemNameMem);
				}
				NotesErrorUtils.checkResult(convertResult);
				
				try(DisposableMemory tLenMem = new DisposableMemory(4)) {
    				short getPropResult = NotesCAPI.get().HTMLGetProperty(hHTML, NotesConstants.HTMLAPI_PROP_TEXTLENGTH, tLenMem);
    				NotesErrorUtils.checkResult(getPropResult);
    				
    				return tLenMem.getInt(0);
				}
			});
			
			IntByReference len = new IntByReference();
			int startOffset=0;
			int bufSize = 4000;
			int iLen = bufSize;
			
			byte[] bufArr = new byte[bufSize];
			
			ByteArrayOutputStream htmlTextLMBCSOut = new ByteArrayOutputStream();
			
			try(DisposableMemory textMem = new DisposableMemory(bufSize+1)) {
				while (result==0 && iLen>0 && startOffset<totalLen) {
					len.setValue(bufSize);
					textMem.setByte(0, (byte) 0);

					result = NotesCAPI.get().HTMLGetText(hHTML, startOffset, len, textMem);
					NotesErrorUtils.checkResult(result);

					iLen = len.getValue();

					if (result==0 && iLen > 0) {
						textMem.read(0, bufArr, 0, iLen);
						htmlTextLMBCSOut.write(bufArr, 0, iLen);

						startOffset += iLen;
					}
				}
			}

			String htmlText = NotesStringUtils.fromLMBCS(htmlTextLMBCSOut.toByteArray());
			
			
			int iRefCount;
            try(DisposableMemory refCount = new DisposableMemory(4)) {
              result=NotesCAPI.get().HTMLGetProperty(hHTML, NotesConstants.HTMLAPI_PROP_NUMREFS, refCount);
              NotesErrorUtils.checkResult(result);
              iRefCount = refCount.getInt(0);
            }

			List<IHtmlApiReference> references = new ArrayList<>();
			
			for (int i=0; i<iRefCount; i++) {
				IntByReference phRef = new IntByReference();
				phRef.setValue(0);
				
				result = NotesCAPI.get().HTMLGetReference(hHTML, i, phRef);
				NotesErrorUtils.checkResult(result);
				
				try(DisposableMemory ppRef = new DisposableMemory(Native.POINTER_SIZE)) {
    				int hRef = phRef.getValue();
    				
    				result = NotesCAPI.get().HTMLLockAndFixupReference(hRef, ppRef);
    				NotesErrorUtils.checkResult(result);
    				
    				try {
    					int iRefType;
    					Pointer pRefText;
    					Pointer pFragment;
    					int iCmdId;
    					int nTargets;
    					Pointer pTargets;
    					
    					//use separate structs for 64/32, because RefType uses 8 bytes on 64 and 4 bytes on 32 bit
    					if (PlatformUtils.is64Bit()) {
    						HtmlAPIReference64Struct htmlApiRef = HtmlAPIReference64Struct.newInstance(ppRef.getPointer(0));
    						htmlApiRef.read();
    						iRefType = (int) htmlApiRef.RefType;
    						pRefText = htmlApiRef.pRefText;
    						pFragment = htmlApiRef.pFragment;
    						iCmdId = (int) htmlApiRef.CommandId;
    						nTargets = htmlApiRef.NumTargets;
    						pTargets = htmlApiRef.pTargets;
    					}
    					else {
    						HtmlAPIReference32Struct htmlApiRef = HtmlAPIReference32Struct.newInstance(ppRef.getPointer(0));
    						htmlApiRef.read();
    						iRefType = htmlApiRef.RefType;
    						pRefText = htmlApiRef.pRefText;
    						pFragment = htmlApiRef.pFragment;
    						iCmdId = htmlApiRef.CommandId;
    						nTargets = htmlApiRef.NumTargets;
    						pTargets = htmlApiRef.pTargets;
    					}
    
    					ReferenceType refType = ReferenceType.getType(iRefType);
    					
    					if (refTypeFilter==null || refTypeFilter.contains(refType)) {
    						String refText = NotesStringUtils.fromLMBCS(pRefText, -1);
    						String fragment = NotesStringUtils.fromLMBCS(pFragment, -1);
    						
    						CommandId cmdId = CommandId.getCommandId(iCmdId);
    						
    						List<IHtmlApiUrlTargetComponent<?>> targets = new ArrayList<>(nTargets);
    						
    						for (int t=0; t<nTargets; t++) {
    							Pointer pCurrTarget = pTargets.share(t * JNANotesConstants.htmlApiUrlComponentSize);
    							HtmlApi_UrlTargetComponentStruct currTarget = HtmlApi_UrlTargetComponentStruct.newInstance(pCurrTarget);
    							currTarget.read();
    							
    							int iTargetType = currTarget.AddressableType;
    							TargetType targetType = TargetType.getType(iTargetType);
    							
    							Set<TargetType> targetTypeFilterForRefType = targetTypeFilter==null ? null : targetTypeFilter.get(refType);
    							
    							if (targetTypeFilterForRefType==null || targetTypeFilterForRefType.contains(targetType)) {
    								switch (currTarget.ReferenceType) {
    								case NotesConstants.URT_Name:
    									currTarget.Value.setType(Pointer.class);
    									currTarget.Value.read();
    									String name = NotesStringUtils.fromLMBCS(currTarget.Value.name, -1);
    									targets.add(new HtmlApiUrlTargetComponent<>(targetType, String.class, name));
    									break;
    								case NotesConstants.URT_NoteId:
    									currTarget.Value.setType(NoteIdStruct.class);
    									currTarget.Value.read();
    									NoteIdStruct noteIdStruct = currTarget.Value.nid;
    									int iNoteId = noteIdStruct.nid;
    									targets.add(new HtmlApiUrlTargetComponent<>(targetType, Integer.class, iNoteId));
    									break;
    								case NotesConstants.URT_Unid:
    									currTarget.Value.setType(NotesUniversalNoteIdStruct.class);
    									currTarget.Value.read();
    									NotesUniversalNoteIdStruct unidStruct = currTarget.Value.unid;
    									unidStruct.read();
    									String unid = unidStruct.toString();
    									targets.add(new HtmlApiUrlTargetComponent<>(targetType, String.class, unid));
    									break;
    								case NotesConstants.URT_None:
    									targets.add(new HtmlApiUrlTargetComponent<>(targetType, Object.class, null));
    									break;
    								case NotesConstants.URT_RepId:
    									//TODO find out how to decode this one
    									break;
    								case NotesConstants.URT_Special:
    									//TODO find out how to decode this one
    									break;
    								default:
    			                      //TODO: is there anything to do
    								}
    							}
    						}
    						
    						IHtmlApiReference newRef = new HTMLApiReference(refType, refText, fragment,
    								cmdId, targets);
    						references.add(newRef);
    					}
    				}
    				finally {
    					if (hRef!=0) {
    						Mem.OSMemoryUnlock(hRef);
    						Mem.OSMemoryFree(hRef);
    					}
    				}
				}
			}
			
			return new JNAHtmlConversionResult(doc, htmlText, references, options);
		}
		finally {
			if (hHTML!=0) {
				result = NotesCAPI.get().HTMLDestroyConverter(hHTML);
			}
			NotesErrorUtils.checkResult(result);
		}
	}

	private static class HtmlApiUrlTargetComponent<T> implements IHtmlApiUrlTargetComponent<T> {
		private TargetType m_type;
		private Class<T> m_valueClazz;
		private T m_value;
		
		private HtmlApiUrlTargetComponent(TargetType type, Class<T> valueClazz, T value) {
			m_type = type;
			m_valueClazz = valueClazz;
			m_value = value;
		}
		
		@Override
		public TargetType getType() {
			return m_type;
		}

		@Override
		public Class<T> getValueClass() {
			return m_valueClazz;
		}

		@Override
		public T getValue() {
			return m_value;
		}
	}
	
	private static class HTMLApiReference implements IHtmlApiReference {
		private ReferenceType m_type;
		private String m_refText;
		private String m_fragment;
		private CommandId m_commandId;
		private List<IHtmlApiUrlTargetComponent<?>> m_targets;
		private Map<TargetType, IHtmlApiUrlTargetComponent<?>> m_targetByType;
		
		private HTMLApiReference(ReferenceType type, String refText, String fragment, CommandId commandId,
				List<IHtmlApiUrlTargetComponent<?>> targets) {
			m_type = type;
			m_refText = refText;
			m_fragment = fragment;
			m_commandId = commandId;
			m_targets = targets;
		}
		
		@Override
		public ReferenceType getType() {
			return m_type;
		}

		@Override
		public String getReferenceText() {
			return m_refText;
		}

		@Override
		public String getFragment() {
			return m_fragment;
		}

		@Override
		public CommandId getCommandId() {
			return m_commandId;
		}

		@Override
		public List<IHtmlApiUrlTargetComponent<?>> getTargets() {
			return m_targets;
		}

		@Override
		public IHtmlApiUrlTargetComponent<?> getTargetByType(TargetType type) {
			if (m_targetByType==null) {
				m_targetByType = new HashMap<>();
				if (m_targets!=null && !m_targets.isEmpty()) {
					for (IHtmlApiUrlTargetComponent<?> currTarget : m_targets) {
						m_targetByType.put(currTarget.getType(), currTarget);
					}
				}
			}
			return m_targetByType.get(type);
		}
	}
	
	/**
	 * Implementation of {@link IHtmlConversionResult} that contains the HTML conversion result
	 * 
	 * @author Karsten Lehmann
	 */
	private class JNAHtmlConversionResult implements HtmlConversionResult {
		private Document m_doc;
		private String m_html;
		private List<IHtmlApiReference> m_references;
		private Collection<String> m_options;
		
		private JNAHtmlConversionResult(Document doc, String html, List<IHtmlApiReference> references, Collection<String> options) {
			m_doc = doc;
			m_html = html;
			m_references = references;
			m_options = options;
		}
		
		@Override
		public <T> T getAdapter(Class<T> clazz) {
			return null;
		}
		
		@Override
		public String getHtml() {
			return m_html;
		}

		@SuppressWarnings("unused")
		public List<IHtmlApiReference> getReferences() {
			return m_references;
		}
		
		private EmbeddedImage createImageRef(final String refText, final String fieldName, final int itemIndex,
				final int itemOffset, final String format) {
			return new EmbeddedImage() {
				
				@Override
				public void readImage(HTMLImageReader callback) {
					convertHtmlElement(m_doc, this, callback);
				}
				
				@Override
				public void writeImage(Path path) throws IOException {
					if (Files.exists(path)) {
						Files.delete(path);
					}
						
					final IOException[] ex = new IOException[1];
					try(OutputStream fOut = Files.newOutputStream(path, StandardOpenOption.TRUNCATE_EXISTING)) {
						convertHtmlElement(m_doc, this, new HTMLImageReader() {

							@Override
							public int setSize(int size) {
								return 0;
							}

							@Override
							public Action read(byte[] data) {
								try {
									fOut.write(data);
									return Action.Continue;
								} catch (IOException e) {
									ex[0] = e;
									return Action.Stop;
								}
							}
						});
						
						if (ex[0]!=null) {
							throw ex[0];
						}
					}
				}
				
				@Override
				public void writeImage(final OutputStream out) throws IOException {
					final IOException[] ex = new IOException[1];

					convertHtmlElement(m_doc, this, new HTMLImageReader() {

						@Override
						public int setSize(int size) {
							return 0;
						}

						@Override
						public Action read(byte[] data) {
							try {
								out.write(data);
								return Action.Continue;
							} catch (IOException e) {
								ex[0] = e;
								return Action.Stop;
							}
						}
					});
					
					if (ex[0]!=null) {
						throw ex[0];
					}
					
					out.flush();
				}
				
				@Override
				public String getImageSrcAttr() {
					return refText;
				}
				
				@Override
				public Collection<String> getOptions() {
					return m_options;
				}
				
				@Override
				public int getItemOffset() {
					return itemOffset;
				}
				
				@Override
				public String getItemName() {
					return fieldName;
				}
				
				@Override
				public int getItemIndex() {
					return itemIndex;
				}
				
				@Override
				public String getFormat() {
					return format;
				}
			};
		}
		
		@Override
		public List<EmbeddedImage> getImages() {
			List<EmbeddedImage> imageRefs = new ArrayList<>();
			
			for (IHtmlApiReference currRef : m_references) {
				if (currRef.getType() == ReferenceType.IMG) {
					String refText = currRef.getReferenceText();
					String format = "gif"; //$NON-NLS-1$
					int iFormatPos = refText.indexOf("FieldElemFormat="); //$NON-NLS-1$
					if (iFormatPos!=-1) {
						String remainder = refText.substring(iFormatPos + "FieldElemFormat=".length()); //$NON-NLS-1$
						int iNextDelim = remainder.indexOf('&');
						if (iNextDelim==-1) {
							format = remainder;
						}
						else {
							format = remainder.substring(0, iNextDelim);
						}
					}
					
					IHtmlApiUrlTargetComponent<?> fieldOffsetTarget = currRef.getTargetByType(TargetType.FIELDOFFSET);
					if (fieldOffsetTarget!=null) {
						Object fieldOffsetObj = fieldOffsetTarget.getValue();
						if (fieldOffsetObj instanceof String) {
							String fieldOffset = (String) fieldOffsetObj;
							// 1.3E -> index=1, offset=63
							int iPos = fieldOffset.indexOf('.');
							if (iPos!=-1) {
								String indexStr = fieldOffset.substring(0, iPos);
								String offsetStr = fieldOffset.substring(iPos+1);
								
								int itemIndex = Integer.parseInt(indexStr, 10); // this one is in decimal format...
								int itemOffset = Integer.parseInt(offsetStr, 16); // this one is in hex format...
								
								IHtmlApiUrlTargetComponent<?> fieldTarget = currRef.getTargetByType(TargetType.FIELD);
								if (fieldTarget!=null) {
									Object fieldNameObj = fieldTarget.getValue();
									String fieldName = (fieldNameObj instanceof String) ? (String) fieldNameObj : null;
									
									EmbeddedImage newImgRef = createImageRef(refText, fieldName, itemIndex, itemOffset, format);
									imageRefs.add(newImgRef);
								}
							}
							
						}
					}
				}
			}
			
			return imageRefs;
		};
		
	}

	/**
	 * Convenience method to read the binary data of a {@link EmbeddedImage}
	 * 
	 * @param doc document
	 * @param image image reference
	 * @param callback callback to receive the data
	 */
	private void convertHtmlElement(Document doc, EmbeddedImage image, HTMLImageReader callback) {
		String itemName = image.getItemName();
		int itemIndex = image.getItemIndex();
		int itemOffset = image.getItemOffset();
		Collection<String> options = image.getOptions();
		
		readEmbeddedImage(doc, itemName, options, itemIndex, itemOffset, callback);
	}
	
	@Override
	public void readEmbeddedImage(Document doc, String itemName, Collection<String> options, int itemIndex, int itemOffset, HTMLImageReader callback) {
		if (!(doc instanceof JNADocument)) {
			throw new IllegalArgumentException("Document must be a JNADocument");
		}
		JNADocument jnaDoc = (JNADocument) doc;
		if (jnaDoc.isDisposed()) {
			throw new DominoException("Document is diposed");
		}
		if (jnaDoc.isEncrypted()) {
			throw new DominoException("The document is encrypted. Please decrypt the document first before accessing embedded images.");
		}

		JNADocumentAllocations jnaDocAllocations = (JNADocumentAllocations) jnaDoc.getAdapter(APIObjectAllocations.class);
		
		JNADatabase jnaDb = (JNADatabase) jnaDoc.getParentDatabase();
		if (jnaDb.isDisposed()) {
			throw new ObjectDisposedException(jnaDb);
		}
		JNADatabaseAllocations jnaDbAllocations = (JNADatabaseAllocations) jnaDb.getAdapter(APIObjectAllocations.class);
		
		IntByReference phHTML = new IntByReference();
		
		short result = NotesCAPI.get().HTMLCreateConverter(phHTML);
		NotesErrorUtils.checkResult(result);
		
		int hHTML = phHTML.getValue();
		
		try {
			if (options != null && !options.isEmpty()) {
				result = NotesCAPI.get().HTMLSetHTMLOptions(hHTML, new StringArray(options.toArray(new String[options.size()])));
				NotesErrorUtils.checkResult(result);
			}

			Memory itemNameMem = NotesStringUtils.toLMBCS(itemName, true);

			
			int totalLen = LockUtil.lockHandles(jnaDbAllocations.getDBHandle(), jnaDocAllocations.getNoteHandle(), (hDbByVal, hNoteByVal) -> {
				short convertResult = NotesCAPI.get().HTMLConvertElement(hHTML, hDbByVal, hNoteByVal, itemNameMem, itemIndex, itemOffset);
				NotesErrorUtils.checkResult(convertResult);
				
				try(DisposableMemory tLenMem = new DisposableMemory(4)) {
    				short getPropResult = NotesCAPI.get().HTMLGetProperty(hHTML, NotesConstants.HTMLAPI_PROP_TEXTLENGTH, tLenMem);
    				NotesErrorUtils.checkResult(getPropResult);
    				
    				return tLenMem.getInt(0);
				}
			});
			int skip = callback.setSize(totalLen);
			

			if (skip > totalLen) {
				throw new IllegalArgumentException(MessageFormat.format("Skip value cannot be greater than size: {0} > {1}", skip, totalLen));
			}
			
			IntByReference len = new IntByReference();
			len.setValue(NotesConstants.MAXPATH);
			int startOffset=skip;
			try(DisposableMemory bufMem = new DisposableMemory(NotesConstants.MAXPATH+1)) {
    			while (result==0 && len.getValue()>0 && startOffset<totalLen) {
    				len.setValue(NotesConstants.MAXPATH);
    				
    				result = NotesCAPI.get().HTMLGetText(hHTML, startOffset, len, bufMem);
    				NotesErrorUtils.checkResult(result);
    				
    				byte[] data = bufMem.getByteArray(0, len.getValue());
    				Action action = callback.read(data);
    				if (action == Action.Stop) {
    					break;
    				}
    				
    				startOffset += len.getValue();
    			}
			}
		}
		finally {
			result = NotesCAPI.get().HTMLDestroyConverter(hHTML);
			NotesErrorUtils.checkResult(result);
		}
	}

}
