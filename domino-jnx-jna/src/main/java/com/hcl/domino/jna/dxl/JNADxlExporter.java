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
package com.hcl.domino.jna.dxl;

import static com.hcl.domino.commons.dxl.DxlExportProperty.AttachmentOmittedText;
import static com.hcl.domino.commons.dxl.DxlExportProperty.ConvertNotesbitmapsToGIF;
import static com.hcl.domino.commons.dxl.DxlExportProperty.DXLBannerComments;
import static com.hcl.domino.commons.dxl.DxlExportProperty.DefaultDoctypeSYSTEM;
import static com.hcl.domino.commons.dxl.DxlExportProperty.DoctypeSYSTEM;
import static com.hcl.domino.commons.dxl.DxlExportProperty.DxlDefaultSchemaLocation;
import static com.hcl.domino.commons.dxl.DxlExportProperty.DxlExportCharset;
import static com.hcl.domino.commons.dxl.DxlExportProperty.DxlExportResultLog;
import static com.hcl.domino.commons.dxl.DxlExportProperty.DxlExportResultLogComment;
import static com.hcl.domino.commons.dxl.DxlExportProperty.DxlMimeOption;
import static com.hcl.domino.commons.dxl.DxlExportProperty.DxlRichtextOption;
import static com.hcl.domino.commons.dxl.DxlExportProperty.DxlSchemaLocation;
import static com.hcl.domino.commons.dxl.DxlExportProperty.DxlValidationStyle;
import static com.hcl.domino.commons.dxl.DxlExportProperty.ForceNoteFormat;
import static com.hcl.domino.commons.dxl.DxlExportProperty.OLEObjectOmittedText;
import static com.hcl.domino.commons.dxl.DxlExportProperty.OmitItemNames;
import static com.hcl.domino.commons.dxl.DxlExportProperty.OmitMiscFileObjects;
import static com.hcl.domino.commons.dxl.DxlExportProperty.OmitOLEObjects;
import static com.hcl.domino.commons.dxl.DxlExportProperty.OmitPictures;
import static com.hcl.domino.commons.dxl.DxlExportProperty.OmitRichtextAttachments;
import static com.hcl.domino.commons.dxl.DxlExportProperty.OutputDOCTYPE;
import static com.hcl.domino.commons.dxl.DxlExportProperty.OutputXmlDecl;
import static com.hcl.domino.commons.dxl.DxlExportProperty.PictureOmittedText;
import static com.hcl.domino.commons.dxl.DxlExportProperty.RestrictToItemNames;
import static com.hcl.domino.commons.dxl.DxlExportProperty.UncompressAttachments;

import java.io.IOException;
import java.io.Writer;
import java.lang.ref.ReferenceQueue;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hcl.domino.commons.dxl.DxlExportProperty;
import com.hcl.domino.commons.dxl.DxlExporterLogImpl;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.PlatformUtils;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.dxl.DxlExporter;
import com.hcl.domino.dxl.DxlExporterLog;
import com.hcl.domino.exception.DxlExportException;
import com.hcl.domino.jna.JNADominoClient;
import com.hcl.domino.jna.internal.callbacks.NotesCallbacks;
import com.hcl.domino.jna.internal.callbacks.Win32NotesCallbacks;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNADxlExporterAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.HANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.utils.JNADominoUtils;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.INumberEnum;
import com.sun.jna.Pointer;

public class JNADxlExporter extends AbstractDxlProcessor<JNADxlExporterAllocations, DxlExportProperty> implements DxlExporter {

	public JNADxlExporter(JNADominoClient parent, int hDxlExporter) {
		super(parent);
		
		getAllocations().setDxlExporterHandle(hDxlExporter);
		
		setInitialized();
	}

	@Override
	public void exportDocument(Document doc, Writer out) throws IOException {
		Objects.requireNonNull(doc, "Document cannot be null");
		Objects.requireNonNull(out, "Writer cannot be null");
		
		checkDisposed();
		
		LockUtil.lockHandle(doc.getAdapter(DHANDLE.class), handle -> {
			NotesCallbacks.XML_WRITE_FUNCTION func;
			
			if (PlatformUtils.isWin32()) {
				func = (Win32NotesCallbacks.XML_WRITE_FUNCTIONWin32)(pBuffer, length, pAction) -> {
					byte[] bytes = pBuffer.getByteArray(0, length);
					String chunk = new String(bytes, getJDKExportCharset().get());
					try {
						out.write(chunk);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}; 
			}
			else {
				func = (pBuffer, length, pAction) -> {
					byte[] bytes = pBuffer.getByteArray(0, length);
					String chunk = new String(bytes, getJDKExportCharset().get());
					try {
						out.write(chunk);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}	
				};
			}

			int exporterHandler = getAllocations().getDxlExporterHandle();
			short result = AccessController.doPrivileged((PrivilegedAction<Short>) ()-> {
				return NotesCAPI.get().DXLExportNote(exporterHandler, func, handle, (Pointer) null);
			});

			NotesErrorUtils.checkResult(result);
			checkError();
			
			return null;
		});

	}

	@Override
	public void exportIDs(Database db, Collection<Integer> ids, Writer out) throws IOException {
		Objects.requireNonNull(ids, "Ids cannot be null");
		Objects.requireNonNull(out, "Writer cannot be null");

		checkDisposed();
		
		IDTable idTable;
		if(ids instanceof IDTable) {
			idTable = (IDTable)ids;
		} else {
			idTable = db.getParentDominoClient().createIDTable();
			idTable.addAll(ids);
		}
		
		LockUtil.lockHandles(db.getAdapter(HANDLE.class), idTable.getAdapter(DHANDLE.class), (hDB, hTable) -> {
			NotesCallbacks.XML_WRITE_FUNCTION func;
			
			if (PlatformUtils.isWin32()) {
				func = (Win32NotesCallbacks.XML_WRITE_FUNCTIONWin32) (pBuffer, length, pAction) -> {
					byte[] bytes = pBuffer.getByteArray(0, length);
					String chunk = new String(bytes, getJDKExportCharset().get());
					try {
						out.write(chunk);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				};
			}
			else {
				func = (pBuffer, length, pAction) -> {
					byte[] bytes = pBuffer.getByteArray(0, length);
					String chunk = new String(bytes, getJDKExportCharset().get());
					try {
						out.write(chunk);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				};
			}
			
			short result = NotesCAPI.get().DXLExportIDTable(getAllocations().getDxlExporterHandle(), func, hDB, hTable, null);
			NotesErrorUtils.checkResult(result);
			checkError();
			
			return null;
		});
	}

	@Override
	public void exportDatabase(Database db, Writer out) throws IOException {
		Objects.requireNonNull(db, "Database cannot be null");
		Objects.requireNonNull(out, "Writer cannot be null");

		checkDisposed();
		LockUtil.lockHandle(db.getAdapter(HANDLE.class), handle -> {
			NotesCallbacks.XML_WRITE_FUNCTION func;
			
			if (PlatformUtils.isWin32()) {
				func = (Win32NotesCallbacks.XML_WRITE_FUNCTIONWin32) (pBuffer, length, pAction) -> {
					byte[] bytes = pBuffer.getByteArray(0, length);
					String chunk = new String(bytes, getJDKExportCharset().get());
					try {
						out.write(chunk);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				};
			}
			else {
				func = (pBuffer, length, pAction) -> {
					byte[] bytes = pBuffer.getByteArray(0, length);
					String chunk = new String(bytes, getJDKExportCharset().get());
					try {
						out.write(chunk);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				};
			}
			
			short result = NotesCAPI.get().DXLExportDatabase(getAllocations().getDxlExporterHandle(), func, handle, null);
			NotesErrorUtils.checkResult(result);
			checkError();
			
			return null;
		});
	}

	@Override
	public void exportACL(Database db, Writer out) throws IOException {
		Objects.requireNonNull(db, "Database cannot be null");
		Objects.requireNonNull(out, "Writer cannot be null");

		checkDisposed();
		LockUtil.lockHandle(db.getAdapter(HANDLE.class), handle -> {
			NotesCallbacks.XML_WRITE_FUNCTION func;
			
			if (PlatformUtils.isWin32()) {
				func = (Win32NotesCallbacks.XML_WRITE_FUNCTIONWin32)(pBuffer, length, pAction) -> {
					byte[] bytes = pBuffer.getByteArray(0, length);
					String chunk = new String(bytes, getJDKExportCharset().get());
					try {
						out.write(chunk);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				};
			}
			else {
				func = (pBuffer, length, pAction) -> {
					byte[] bytes = pBuffer.getByteArray(0, length);
					String chunk = new String(bytes, getJDKExportCharset().get());
					try {
						out.write(chunk);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				};
			}
			
			short result = NotesCAPI.get().DXLExportACL(getAllocations().getDxlExporterHandle(), func, handle, null);
			NotesErrorUtils.checkResult(result);
			checkError();
			return null;
		});
	}

	@Override
	public boolean exportErrorWasLogged() {
		checkDisposed();
		
		return NotesCAPI.get().DXLExportWasErrorLogged(getAllocations().getDxlExporterHandle());
	}

	@Override
	public boolean isOutputXmlDecl() {
		return isProp(OutputXmlDecl);
	}

	@Override
	public void setOutputXmlDecl(boolean b) {
		setProp(OutputXmlDecl, b);
	}

	@Override
	public boolean isOutputDoctype() {
		return isProp(OutputDOCTYPE);
	}

	@Override
	public void setOutputDoctype(boolean b) {
		setProp(OutputDOCTYPE, b);
	}

	@Override
	public boolean isConvertNotesbitmapsToGIF() {
		return isProp(ConvertNotesbitmapsToGIF);
	}

	@Override
	public void setConvertNotesbitmapsToGIF(boolean b) {
		setProp(ConvertNotesbitmapsToGIF, b);
	}

	@Override
	public boolean isOmitRichTextAttachments() {
		return isProp(OmitRichtextAttachments);
	}

	@Override
	public void setOmitRichTextAttachments(boolean b) {
		setProp(OmitRichtextAttachments, b);
	}

	@Override
	public boolean isOmitOLEObjects() {
		return isProp(OmitOLEObjects);
	}

	@Override
	public void setOmitOLEObjects(boolean b) {
		setProp(OmitOLEObjects, b);
	}

	@Override
	public boolean isOmitMiscFileObjects() {
		return isProp(OmitMiscFileObjects);
	}

	@Override
	public void setOmitMiscFileObjects(boolean b) {
		setProp(OmitMiscFileObjects, b);
	}

	@Override
	public boolean isOmitPictures() {
		return isProp(OmitPictures);
	}

	@Override
	public void setOmitPictures(boolean b) {
		setProp(OmitPictures, b);
	}

	@Override
	public boolean isUncompressAttachments() {
		return isProp(UncompressAttachments);
	}

	@Override
	public void setUncompressAttachments(boolean b) {
		setProp(UncompressAttachments, b);
	}

	@Override
	public String getDxlExportResultLog() {
		return getPropString(DxlExportResultLog);
	}

	@Override
	public String getDefaultDoctypeSYSTEM() {
		return getPropString(DefaultDoctypeSYSTEM);
	}

	@Override
	public String getDoctypeSYSTEM() {
		return getPropString(DoctypeSYSTEM);
	}

	@Override
	public void setDoctypeSYSTEM(String docType) {
		setProp(DoctypeSYSTEM, docType);
	}

	@Override
	public String getDXLBannerComments() {
		return getPropString(DXLBannerComments);
	}

	@Override
	public void setDXLBannerComments(String comments) {
		setProp(DXLBannerComments, comments);
	}

	@Override
	public String getDxlExportResultLogComment() {
		return getPropString(DxlExportResultLogComment);
	}

	@Override
	public void setDxlExportResultLogComment(String comment) {
		setProp(DxlExportResultLogComment, comment);
	}

	@Override
	public String getDxlDefaultSchemaLocation() {
		return getPropString(DxlDefaultSchemaLocation);
	}

	@Override
	public String getDxlSchemaLocation() {
		return getPropString(DxlSchemaLocation);
	}

	@Override
	public void setDxlSchemaLocation(String loc) {
		setProp(DxlSchemaLocation, loc);
	}

	@Override
	public String getAttachmentOmittedText() {
		return getPropString(AttachmentOmittedText);
	}

	@Override
	public void setAttachmentOmittedText(String txt) {
		setProp(AttachmentOmittedText, txt);
	}

	@Override
	public String getOLEObjectOmittedText() {
		return getPropString(OLEObjectOmittedText);
	}

	@Override
	public void setOLEObjectOmittedText(String txt) {
		setProp(OLEObjectOmittedText, txt);
	}

	@Override
	public String getPictureOmittedText() {
		return getPropString(PictureOmittedText);
	}

	@Override
	public void setPictureOmittedText(String txt) {
		setProp(PictureOmittedText, txt);
	}

	@Override
	public List<String> getOmitItemNames() {
		return getPropStringList(OmitItemNames);
	}

	@Override
	public void setOmitItemNames(List<String> itemNames) {
		setProp(OmitItemNames, itemNames);
	}

	@Override
	public List<String> getRestrictToItemNames() {
		return getPropStringList(RestrictToItemNames);
	}

	@Override
	public void setRestrictToItemNames(List<String> itemNames) {
		setProp(RestrictToItemNames, itemNames);
	}

	@Override
	public DXLExportCharset getExportCharset() {
		int value = getPropInt(DxlExportCharset);
		return DominoEnumUtil.valueOf(DXLExportCharset.class, value)
			.orElseThrow(() -> new IllegalStateException(MessageFormat.format("Cannot identify charset for {0}", value)));
	}

	@Override
	public Optional<Charset> getJDKExportCharset() {
		DXLExportCharset charset = getExportCharset();
		if(charset == null) {
			return Optional.empty();
		}
		switch(charset) {
		case UTF16:
			return Optional.of(StandardCharsets.UTF_16);
		case UTF8:
			return Optional.of(StandardCharsets.UTF_8);
		default:
			return Optional.empty();
		}
	}

	@Override
	public void setExportCharset(DXLExportCharset charset) {
		setProp(DxlExportCharset, charset.getValue());
	}

	@Override
	public DXLRichTextOption getRichTextOption() {
		int value = getPropInt(DxlRichtextOption);
		return DominoEnumUtil.valueOf(DXLRichTextOption.class, value)
			.orElseThrow(() -> new IllegalStateException(MessageFormat.format("Cannot identify rich text option for {0}", value)));
	}

	@Override
	public void setRichTextOption(DXLRichTextOption option) {
		setProp(DxlRichtextOption, option.getValue());
	}

	@Override
	public DXLValidationStyle getValidationStyle() {
		int value = getPropInt(DxlValidationStyle);
		return DominoEnumUtil.valueOf(DXLValidationStyle.class, value)
			.orElseThrow(() -> new IllegalStateException(MessageFormat.format("Cannot identify validation style for {0}", value)));
	}

	@Override
	public void setValidationStyle(DXLValidationStyle style) {
		setProp(DxlValidationStyle, style.getValue());
	}

	@Override
	public DXLMIMEOption getMIMEOption() {
		int value = getPropInt(DxlMimeOption);
		return DominoEnumUtil.valueOf(DXLMIMEOption.class, value)
			.orElseThrow(() -> new IllegalStateException(MessageFormat.format("Cannot identify MIME option for {0}", value)));
	}

	@Override
	public void setMIMEOption(DXLMIMEOption option) {
		setProp(DxlMimeOption, option.getValue());
	}
	
	@Override
	public boolean isForceNoteFormat() {
		return isProp(ForceNoteFormat);
	}
	
	@Override
	public void setForceNoteFormat(boolean forceNoteFormat) {
		setProp(ForceNoteFormat, forceNoteFormat);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected JNADxlExporterAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		return new JNADxlExporterAllocations(parentDominoClient, parentAllocations, this, queue);
	}

	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************
	
	@Override
	protected void setProperty(int hDxl, INumberEnum<Integer> prop, Pointer propValue) {
		NotesCAPI.get().DXLSetExporterProperty(hDxl, prop.getValue(), propValue);
	}
	
	@Override
	protected void getProperty(int hDxl, INumberEnum<Integer> prop, Pointer retPropValue) {
		NotesCAPI.get().DXLGetExporterProperty(hDxl, prop.getValue(), retPropValue);
	}
	
	@Override
	protected int getHandle() {
		return getAllocations().getDxlExporterHandle();
	}
	
	@Override
	protected void checkError() {
		if(exportErrorWasLogged()) {
			String xml = getDxlExportResultLog();
			try {
				org.w3c.dom.Document xmlDoc = JNADominoUtils.parseXml(xml);
				DxlExporterLogImpl.DXLErrorImpl error;
				{
					NodeList errorNodes = xmlDoc.getElementsByTagName("error"); //$NON-NLS-1$
					if(errorNodes.getLength() == 0) {
						error = null;
					} else {
						Element errorNode = (Element)errorNodes.item(0);
						error = new DxlExporterLogImpl.DXLErrorImpl();
						error.setId(Integer.parseInt(errorNode.getAttribute("id"))); //$NON-NLS-1$
						error.setText(errorNode.getTextContent());
					}
				}
				
				DxlExporterLog log = new DxlExporterLogImpl(error);
				throw new DxlExportException(log.getError().getText(), log.getError().getId(), log);
			} catch (ParserConfigurationException | SAXException e) {
				throw new RuntimeException("Encountered exception parsing DXL log", e);
			}
		}
	}
}
