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
package com.hcl.domino.jna.mime;

import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.StringTokenizerExt;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNARichtextMimeConversionSettingsAllocations;
import com.hcl.domino.mime.RichTextMimeConversionSettings;
import com.hcl.domino.misc.DominoEnumUtil;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Settings to control the richtext to MIME conversion
 * 
 * @author Karsten Lehmann
 */
public class JNARichtextMimeConversionSettings extends BaseJNAAPIObject<JNARichtextMimeConversionSettingsAllocations> implements RichTextMimeConversionSettings, IAdaptable {
	
	/**
	 * Creates a Conversions Controls context for the reading and writing of various conversion
	 * configuration settings.<br>
	 * The various settings are initialized to their default settings (the same as those set by {@link #setDefaults()}).
	 * 
	 * @param parent parent API object
	 */
	public JNARichtextMimeConversionSettings(IAPIObject<?> parent) {
		super(parent);
		
		PointerByReference retConvControls = new PointerByReference();
		NotesCAPI.get().MMCreateConvControls(retConvControls);
		Pointer convControls = retConvControls.getValue();
		
		getAllocations().setSettingsPointer(convControls);
		
		setInitialized();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected JNARichtextMimeConversionSettingsAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		
		return new JNARichtextMimeConversionSettingsAllocations(parentDominoClient, parentAllocations, this, queue);
	}

	@Override
	public RichTextMimeConversionSettings setDefaults() {
		checkDisposed();
		
		getAllocations().lockAndGetSettingsPointer((ptr) -> {
			NotesCAPI.get().MMConvDefaults(ptr);
			return null;
		});
		
		return this;
	}
	
	@Override
	public Optional<AttachmentEncoding> getAttachmentEncoding() {
		checkDisposed();
		
		return getAllocations().lockAndGetSettingsPointer((ptr) -> {
			short enc = NotesCAPI.get().MMGetAttachEncoding(ptr);
			return DominoEnumUtil.valueOf(AttachmentEncoding.class, enc);
		});
	}
	
	@Override
	public RichTextMimeConversionSettings setAttachmentEncoding(AttachmentEncoding encoding) {
		checkDisposed();
		short newVal = Objects.requireNonNull(encoding, "encoding cannot be null").getValue();
		
		return getAllocations().lockAndGetSettingsPointer((ptr) -> {
			NotesCAPI.get().MMSetAttachEncoding(ptr, newVal);
			return this;
		});
	}
	
	@Override
	public List<String> getDropItems() {
		checkDisposed();
		
		return getAllocations().lockAndGetSettingsPointer((ptr) -> {
			List<String> itemNames = new ArrayList<>();
			
			Pointer dropItemsPtr = NotesCAPI.get().MMGetDropItems(ptr);
			String itemNamesConc = NotesStringUtils.fromLMBCS(dropItemsPtr, -1);
			StringTokenizerExt st = new StringTokenizerExt(itemNamesConc, ","); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				String currToken = st.nextToken();
				if (!StringUtil.isEmpty(currToken)) {
					itemNames.add(currToken);
				}
			}
			return itemNames;
		});
	}
	
	@Override
	public RichTextMimeConversionSettings setDropItems(List<String> itemNames) {
		checkDisposed();
		
		return getAllocations().lockAndGetSettingsPointer((ptr) -> {
			StringBuilder sb = new StringBuilder();
			for (String currItemName : itemNames) {
				if (sb.length()>0) {
					sb.append(","); //$NON-NLS-1$
				}
				sb.append(currItemName.trim());
			}
			String itemNamesConc = sb.toString();
			Memory itemNamesConcMem = NotesStringUtils.toLMBCS(itemNamesConc, true);
			
			NotesCAPI.get().MMSetDropItems(ptr, itemNamesConcMem);
			
			return this;
		});
	}

	@Override
	public boolean isKeepTabs() {
		checkDisposed();
		
		return getAllocations().lockAndGetSettingsPointer((ptr) -> {
			return NotesCAPI.get().MMGetKeepTabs(ptr);
		});
	}
	
	@Override
	public RichTextMimeConversionSettings setKeepTabs(boolean b) {
		checkDisposed();
		
		return getAllocations().lockAndGetSettingsPointer((ptr) -> {
			NotesCAPI.get().MMSetKeepTabs(ptr, b);
			return this;
		});
	}
	
	@Override
	public int getPointSize() {
		checkDisposed();
		
		return getAllocations().lockAndGetSettingsPointer((ptr) -> {
			return (int) (NotesCAPI.get().MMGetPointSize(ptr) & 0xffff);
		});
	}
	
	@Override
	public RichTextMimeConversionSettings setPointSize(int size) {
		checkDisposed();
		
		return getAllocations().lockAndGetSettingsPointer((ptr) -> {
			NotesCAPI.get().MMSetPointSize(ptr, size> 65535 ? (short) 0xffff : (short) (size & 0xffff));
			return this;
		});
	}
	
	
	@Override
	public Optional<Typeface> getTypeface() {
		checkDisposed();
		
		return getAllocations().lockAndGetSettingsPointer((ptr) -> {
			short tf = NotesCAPI.get().MMGetTypeFace(ptr);
			return DominoEnumUtil.valueOf(Typeface.class, tf);
		});
	}
	
	@Override
	public RichTextMimeConversionSettings setTypeface(Typeface tf) {
		checkDisposed();
		short tfAsShort = Objects.requireNonNull(tf, "typeface cannot be null").getValue();
		
		return getAllocations().lockAndGetSettingsPointer((ptr) -> {
			NotesCAPI.get().MMSetTypeFace(ptr, tfAsShort);
			return this;
		});
	}
	
	@Override
	public List<String> getAddItems() {
		checkDisposed();
		
		return getAllocations().lockAndGetSettingsPointer((ptr) -> {
			List<String> itemNames = new ArrayList<>();
			
			Pointer addItemsPtr = NotesCAPI.get().MMGetAddItems(ptr);
			String itemNamesConc = NotesStringUtils.fromLMBCS(addItemsPtr, -1);
			StringTokenizerExt st = new StringTokenizerExt(itemNamesConc, ","); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				String currToken = st.nextToken();
				if (!StringUtil.isEmpty(currToken)) {
					itemNames.add(currToken);
				}
			}
			return itemNames;
		});
	}
	
	@Override
	public RichTextMimeConversionSettings setAddItems(List<String> itemNames) {
		checkDisposed();
		
		return getAllocations().lockAndGetSettingsPointer((ptr) -> {
			StringBuilder sb = new StringBuilder();
			for (String currItemName : itemNames) {
				if (sb.length()>0) {
					sb.append(","); //$NON-NLS-1$
				}
				sb.append(currItemName.trim());
			}
			String itemNamesConc = sb.toString();
			Memory itemNamesConcMem = NotesStringUtils.toLMBCS(itemNamesConc, true);
			
			NotesCAPI.get().MMSetAddItems(ptr, itemNamesConcMem);
			
			return this;
		});
	}

	@Override
	public Optional<MessageContentEncoding> getMessageContentEncoding() {
		checkDisposed();

		return getAllocations().lockAndGetSettingsPointer((ptr) -> {
			short enc = NotesCAPI.get().MMGetMessageContentEncoding(ptr);
			return DominoEnumUtil.valueOf(MessageContentEncoding.class, enc);
		});
	}
	
	@Override
	public RichTextMimeConversionSettings setMessageContentEncoding(MessageContentEncoding enc) {
		checkDisposed();
		short encAsShort = Objects.requireNonNull(enc, "encoding cannot be null").getValue();
		
		return getAllocations().lockAndGetSettingsPointer((ptr) -> {
			NotesCAPI.get().MMSetMessageContentEncoding(ptr, encAsShort);
			return this;
		});
	}

	@Override
	public Optional<ReadReceipt> getReadReceipt() {
		checkDisposed();
		
		return getAllocations().lockAndGetSettingsPointer((ptr) -> {
			short rc = NotesCAPI.get().MMGetReadReceipt(ptr);
			return DominoEnumUtil.valueOf(ReadReceipt.class, rc);
		});
	}
	
	@Override
	public RichTextMimeConversionSettings setReadReceipt(ReadReceipt rc) {
		checkDisposed();
		short rcAsShort = Objects.requireNonNull(rc, "readReceipt cannot be null").getValue();
		
		return getAllocations().lockAndGetSettingsPointer((ptr) -> {
			NotesCAPI.get().MMSetReadReceipt(ptr, rcAsShort);
			return this;
		});
	}
	
	@Override
	public boolean getSkipX() {
		checkDisposed();
		
		return getAllocations().lockAndGetSettingsPointer((ptr) -> {
			return NotesCAPI.get().MMGetSkipX(ptr);
		});
	}
	
	@Override
	public JNARichtextMimeConversionSettings setSkipX(boolean b) {
		checkDisposed();
		
		return getAllocations().lockAndGetSettingsPointer((ptr) -> {
			NotesCAPI.get().MMSetSkipX(ptr, b);
			return this;
		});
	}
}
