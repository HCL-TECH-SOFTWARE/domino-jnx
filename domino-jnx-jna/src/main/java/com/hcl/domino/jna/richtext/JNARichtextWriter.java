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
package com.hcl.domino.jna.richtext;

import static java.text.MessageFormat.format;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.ref.ReferenceQueue;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.bmp.BmpHeaderDirectory;
import com.drew.metadata.gif.GifHeaderDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import com.hcl.domino.DominoException;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.richtext.IDefaultRichTextWriter;
import com.hcl.domino.commons.richtext.DefaultRichTextList;
import com.hcl.domino.commons.richtext.RichTextUtil;
import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Attachment;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.FontAttribute;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.data.StandardColors;
import com.hcl.domino.data.StandardFonts;
import com.hcl.domino.exception.IncompatibleImplementationException;
import com.hcl.domino.exception.ObjectDisposedException;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.data.JNADocument;
import com.hcl.domino.jna.data.JNAItem;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNADocumentAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNARichtextWriterAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNARichtextWriterAllocations.CloseResult;
import com.hcl.domino.jna.internal.gc.allocations.JNARichtextWriterAllocations.CloseResultType;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.richtext.JNACDFileRichTextNavigator;
import com.hcl.domino.jna.internal.richtext.JNACompoundTextStandaloneBuffer;
import com.hcl.domino.jna.internal.richtext.JNACompoundTextStandaloneBuffer.FileInfo;
import com.hcl.domino.jna.internal.structs.NotesCompoundStyleStruct;
import com.hcl.domino.jna.internal.structs.NotesTimeDateStruct;
import com.hcl.domino.jna.internal.structs.NotesUniversalNoteIdStruct;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.RichTextRecordList;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.TextStyle;
import com.hcl.domino.richtext.records.CDBegin;
import com.hcl.domino.richtext.records.CDCaption;
import com.hcl.domino.richtext.records.CDEnd;
import com.hcl.domino.richtext.records.CDHotspotBegin;
import com.hcl.domino.richtext.records.CDHotspotEnd;
import com.hcl.domino.richtext.records.CDImageHeader;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.FontStyle;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class JNARichtextWriter extends BaseJNAAPIObject<JNARichtextWriterAllocations> implements IDefaultRichTextWriter {
	private static final Logger log = Logger.getLogger(JNARichtextWriter.class.getPackage().getName());
	
	private JNADocument m_parentDoc;
	private String m_itemName;
	private boolean m_hasData;
	private Map<Integer,Integer> m_definedStyleId;

	/**
	 * Creates a new richtext writer to produce a richtext item in a document
	 * 
	 * @param parentDoc parent document
	 * @param itemName richtext item name
	 */
	public JNARichtextWriter(JNADocument parentDoc, String itemName) {
		super(parentDoc);
		m_parentDoc = parentDoc;
		m_itemName = itemName;
		
		JNADocumentAllocations docAllocations = (JNADocumentAllocations) parentDoc.getAdapter(APIObjectAllocations.class);
		
		Memory itemNameMem = NotesStringUtils.toLMBCS(itemName, true);
		
		LockUtil.lockHandle(docAllocations.getNoteHandle(), (hNoteByVal) -> {
			DHANDLE.ByReference rethCompound = DHANDLE.newInstanceByReference();
			
			short result = NotesCAPI.get().CompoundTextCreate(hNoteByVal, itemNameMem, rethCompound);
			NotesErrorUtils.checkResult(result);
			
			JNARichtextWriterAllocations writerAllocations = getAllocations();
			writerAllocations.setCompoundTextHandle(rethCompound, false);
			
			return 0;
		});
		m_definedStyleId = Collections.synchronizedMap(new HashMap<Integer, Integer>());
		
		setInitialized();
	}

	/**
	 * Creates standalone richtext
	 * 
	 * @param client parent Domino client
	 */
	public JNARichtextWriter(IGCDominoClient<?> client) {
		super(client);
		
		DHANDLE.ByReference rethCompound = DHANDLE.newInstanceByReference();
		
		short result = NotesCAPI.get().CompoundTextCreate(null, null, rethCompound);
		NotesErrorUtils.checkResult(result);
		
		JNARichtextWriterAllocations writerAllocations = getAllocations();
		writerAllocations.setCompoundTextHandle(rethCompound, true);
		
		m_definedStyleId = Collections.synchronizedMap(new HashMap<Integer, Integer>());
		
		setInitialized();
	}
	
	@Override
	public boolean isEmpty() {
		return !m_hasData;
	}
	
	@Override
	public Document getParentDocument() {
		return m_parentDoc;
	}
	
	@Override
	public String getItemName() {
		return m_itemName;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected JNARichtextWriterAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		
		return new JNARichtextWriterAllocations(parentDominoClient, parentAllocations, this, queue);
	}
	
	@Override
	public TextStyle createTextStyle(String styleName) {
		return new JNATextStyle(styleName);
	}
	
	private int getDefaultFontId() {
		return getFontId(NotesConstants.FONT_FACE_SWISS, (byte) 0, (byte) 0, (byte) 10);
	}

	private int getFontId(byte face, byte attrib, byte color, byte pointSize) {
		FontStyle fontIdStruct = MemoryStructureUtil.newStructure(FontStyle.class, 0);
		fontIdStruct.setStandardFont(DominoEnumUtil.valueOf(StandardFonts.class, face).orElse(StandardFonts.SWISS));
		fontIdStruct.setAttributes(DominoEnumUtil.valuesOf(FontAttribute.class, attrib));
		fontIdStruct.setColor(DominoEnumUtil.valueOf(StandardColors.class, color).orElse(StandardColors.BLACK));
		fontIdStruct.setPointSize(pointSize);

		int fontId = fontIdStruct.getData().getInt(0);
		return fontId;
	}

	/**
	 * Converts the {@link TextStyle} to a style id, reusing already defined
	 * styles if all attributes are matching
	 * 
	 * @param style text style
	 * @return style id
	 */
	private int getStyleId(TextStyle style) {
		int styleHash = style.hashCode();
		Integer styleId = m_definedStyleId.get(styleHash);
		
		if (styleId==null) {
			checkDisposed();

			IntByReference retStyleId = new IntByReference();

			NotesCompoundStyleStruct styleStruct = style.getAdapter(NotesCompoundStyleStruct.class);
			if (styleStruct==null) {
				throw new DominoException("Unable to get style struct from TextStyle");
			}

			Memory styleNameMem = NotesStringUtils.toLMBCS(style.getName(), true);

			JNARichtextWriterAllocations allocations = getAllocations();
			short result = LockUtil.lockHandle(allocations.getCompoundTextHandle(), (hCompoundTextByVal) -> {
				return NotesCAPI.get().CompoundTextDefineStyle(hCompoundTextByVal, styleNameMem, styleStruct, retStyleId);
			});
			NotesErrorUtils.checkResult(result);
			
			styleId = retStyleId.getValue();

			m_definedStyleId.put(styleHash, styleId);
			m_hasData=true;
		}
		return styleId;
	}
	
	@Override
	public RichTextWriter addText(String txt, TextStyle textStyle, FontStyle fontStyle, boolean createParagraphOnLinebreak) {
		checkDisposed();
		
		Memory txtMem = NotesStringUtils.toLMBCS(txt, false);
		Memory lineDelimMem = new Memory(3);
		lineDelimMem.setByte(0, (byte) '\r'); 
		lineDelimMem.setByte(1, (byte) '\n'); 
		lineDelimMem.setByte(2, (byte) 0);

		int fontId;
		if (fontStyle==null) {
			fontId = getDefaultFontId();
		}
		else {
			fontId = fontStyle.getData().getInt();
		}
		
		int dwStyleID = textStyle==null ? NotesConstants.STYLE_ID_SAMEASPREV : getStyleId(textStyle);

		Pointer nlsInfoPtr = NotesCAPI.get().OSGetLMBCSCLS();
		short result;
		int dwFlags = NotesConstants.COMP_PRESERVE_LINES | NotesConstants.COMP_PARA_BLANK_LINE;
		if (createParagraphOnLinebreak) {
			dwFlags = dwFlags | NotesConstants.COMP_PARA_LINE;
		}
		final int fDWFlags = dwFlags;
		
		JNARichtextWriterAllocations allocations = getAllocations();
		
		
		result = LockUtil.lockHandle(allocations.getCompoundTextHandle(), (hCompoundTextByVal) -> {
			return  NotesCAPI.get().CompoundTextAddTextExt(hCompoundTextByVal, dwStyleID, fontId, txtMem,
					txtMem==null ? 0 : (int) txtMem.size(),
					lineDelimMem, fDWFlags, nlsInfoPtr);
		});
		NotesErrorUtils.checkResult(result);
		
		m_hasData=true;
		
		return this;
	}

	@Override
	public RichTextWriter addImage(InputStream imageStream, int resizeToWidth, int resizeToHeight) {
		Path tmpFile = null;
		try {
			try {
				tmpFile = Files.createTempFile("jnx_image_", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
				Files.copy(imageStream, tmpFile, StandardCopyOption.REPLACE_EXISTING);
			}
			catch (IOException e) {
				throw new DominoException("Could not write image data to temp file", e);
			}
			
			return addImage(tmpFile, resizeToWidth, resizeToHeight);
		}
		finally {
			if (tmpFile!=null) {
				try {
					Files.deleteIfExists(tmpFile);
				} catch(IOException e) {
					if(log.isLoggable(Level.SEVERE)) {
						log.log(Level.SEVERE, "Encountered exception deleting temporary file", e);
					}
				}
			}
		}
	}
	
	@Override
	public RichTextWriter addImage(Path imagePath, int resizeToWidth, int resizeToHeight) {
		checkDisposed();
		
		FileType fileType;
		try (InputStream fIn = Files.newInputStream(imagePath); BufferedInputStream bufIn = new BufferedInputStream(fIn)) {
			fileType = FileTypeDetector.detectFileType(bufIn);
		} catch (IOException e1) {
			throw new DominoException(format("Error reading filetype of image with path {0}", imagePath), e1);
		}
		
		if (fileType == FileType.Unknown) {
			throw new DominoException("Unable to detect filetype of image");
		}
		
		boolean isSupported = false;
		if (fileType == FileType.Gif || fileType == FileType.Jpeg || fileType == FileType.Bmp || fileType == FileType.Png) {
			isSupported = true;
		}
		
		if (!isSupported) {
			throw new DominoException(format("Unsupported filetype {0}. Only GIF, PNG, JPEG and BMP are supported", fileType));
		}
		
		int imgWidth = -1;
		int imgHeight = -1;

		Metadata metadata;
		try (InputStream fIn = Files.newInputStream(imagePath)) {
			metadata = ImageMetadataReader.readMetadata(fIn);
		} catch (ImageProcessingException e) {
			throw new DominoException("Unable to read image metadata", e);
		} catch (NoSuchFileException e1) {
			throw new DominoException(format("Image with path {0} could not be found", imagePath), e1);
		} catch (IOException e1) {
			throw new DominoException(format("Error reading metadata of image with path {0}", imagePath), e1);
		}
		
		switch (fileType) {
		case Gif:
			Collection<GifHeaderDirectory> gifHeaderDirs = metadata.getDirectoriesOfType(GifHeaderDirectory.class);
			if (gifHeaderDirs!=null && !gifHeaderDirs.isEmpty()) {
				GifHeaderDirectory header = gifHeaderDirs.iterator().next();
				try {
					imgWidth = header.getInt(GifHeaderDirectory.TAG_IMAGE_WIDTH);
					imgHeight = header.getInt(GifHeaderDirectory.TAG_IMAGE_HEIGHT);
				} catch (MetadataException e) {
					throw new DominoException("Error reading GIF image size", e);
				}
			}
			break;
		case Jpeg:
			Collection<JpegDirectory> jpegHeaderDirs = metadata.getDirectoriesOfType(JpegDirectory.class);
			if (jpegHeaderDirs!=null && !jpegHeaderDirs.isEmpty()) {
				JpegDirectory jpegDir = jpegHeaderDirs.iterator().next();
				try {
					imgWidth = jpegDir.getImageWidth();
					imgHeight = jpegDir.getImageHeight();
				} catch (MetadataException e) {
					throw new DominoException("Error reading JPEG image size", e);
				}
			}
			break;
		case Bmp:
			Collection<BmpHeaderDirectory> bmpHeaderDirs = metadata.getDirectoriesOfType(BmpHeaderDirectory.class);
			if (bmpHeaderDirs!=null && !bmpHeaderDirs.isEmpty()) {
				BmpHeaderDirectory bmpHeader = bmpHeaderDirs.iterator().next();
				try {
					imgWidth = bmpHeader.getInt(BmpHeaderDirectory.TAG_IMAGE_WIDTH);
					imgHeight = bmpHeader.getInt(BmpHeaderDirectory.TAG_IMAGE_HEIGHT);
				} catch (MetadataException e) {
					throw new DominoException("Error reading BMP image size", e);
				}
			}
			break;
		case Png:
			Collection<PngDirectory> pngHeaderDirs = metadata.getDirectoriesOfType(PngDirectory.class);
			if (pngHeaderDirs!=null && !pngHeaderDirs.isEmpty()) {
				PngDirectory pngHeader = pngHeaderDirs.iterator().next();
				try {
					imgWidth = pngHeader.getInt(PngDirectory.TAG_IMAGE_WIDTH);
					imgHeight = pngHeader.getInt(PngDirectory.TAG_IMAGE_HEIGHT);
				} catch (MetadataException e) {
					throw new DominoException("Error reading BMP image size", e);
				}
			}
			break;
		default:
			//
		}
		
		if (imgWidth<=0 || imgHeight<=0) {
			throw new IllegalArgumentException("Width/Height cannot be extracted from the image data");
		}
		
		CDImageHeader.ImageType imageType;
		switch (fileType) {
		case Gif:
			imageType = CDImageHeader.ImageType.GIF;
			break;
		case Jpeg:
			imageType= CDImageHeader.ImageType.JPEG;
			break;
		case Png:
			imageType = CDImageHeader.ImageType.PNG;
			break;
		case Bmp:
			imageType = CDImageHeader.ImageType.BMP;
			break;
		default:
			throw new IllegalArgumentException(format("Unknown image type: {0}", fileType));
		}

		int fileSize;
		try {
			fileSize = (int) Files.size(imagePath);
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}
		
		try(InputStream is = Files.newInputStream(imagePath)) {
			RichTextUtil.writeImageRecords(this, is, fileSize, imgWidth, imgHeight, resizeToWidth, resizeToHeight, imageType);
		} catch (NoSuchFileException e1) {
			throw new DominoException(format("Image with path {0} could not be found", imagePath), e1);
		} catch(IOException e) {
			throw new DominoException(format("Image with path {0} could not be read", imagePath), e);
		}
		
		m_hasData=true;
		
		return this;
	}

	@Override
	public RichTextWriter addDocLink(String dbReplicaId, String viewUnid, String docUNID, String comment) {
		checkDisposed();

		if (!StringUtil.isEmpty(dbReplicaId)) {
			int[] dbReplicaIdInnards = NotesStringUtils.replicaIdToInnards(dbReplicaId);
			NotesTimeDateStruct.ByValue dbReplicaIdStructByVal = NotesTimeDateStruct.ByValue.newInstance();
			dbReplicaIdStructByVal.Innards[0] = dbReplicaIdInnards[0];
			dbReplicaIdStructByVal.Innards[1] = dbReplicaIdInnards[1];

			NotesUniversalNoteIdStruct.ByValue viewUNIDStructByVal = NotesUniversalNoteIdStruct.ByValue.newInstance();;
			NotesUniversalNoteIdStruct.ByValue noteUNIDStructByVal = NotesUniversalNoteIdStruct.ByValue.newInstance();
			
			if (!StringUtil.isEmpty(viewUnid)) {
				NotesUniversalNoteIdStruct viewUNIDStruct = NotesUniversalNoteIdStruct.fromString(viewUnid);
				viewUNIDStructByVal.File = viewUNIDStruct.File;
				viewUNIDStructByVal.Note = viewUNIDStruct.Note;
				
				if (!StringUtil.isEmpty(docUNID)) {
					NotesUniversalNoteIdStruct noteUNIDStruct = NotesUniversalNoteIdStruct.fromString(docUNID);
					noteUNIDStructByVal.File = noteUNIDStruct.File;
					noteUNIDStructByVal.Note = noteUNIDStruct.Note;
				}
			}
			
			//prevent NSD when passing a null value for the comment
			Memory commentMem = NotesStringUtils.toLMBCS(comment==null ? "" : comment, true);
			
			short result = LockUtil.lockHandle(getAllocations().getCompoundTextHandle(), (hCompoundTextByVal) -> {
				return NotesCAPI.get().CompoundTextAddDocLink(hCompoundTextByVal, dbReplicaIdStructByVal, viewUNIDStructByVal, noteUNIDStructByVal, commentMem, 0);
			});
			NotesErrorUtils.checkResult(result);
			
			m_hasData=true;
		}
		
		return this;
	}
	
	@Override
	public RichTextWriter addAttachmentIcon(String attachmentProgrammaticName, String captionTxt) {
		InputStream in = getClass().getResourceAsStream("file-icon.gif"); //$NON-NLS-1$
		if (in==null) {
			throw new IllegalStateException("Default icon file not found");
		}
		
		try {
			return addAttachmentIcon(attachmentProgrammaticName, captionTxt, captionTxt, createFontStyle(),
					CaptionPosition.BELOWCENTER, 0, 0, 0, -1, -1, null, in);
			
		} catch (IOException e) {
			throw new DominoException(format("Could not add attachment icon for {0}", attachmentProgrammaticName), e);
		}
		finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public RichTextWriter addAttachmentIcon(Attachment att, String filenameToDisplay, String captionText, FontStyle captionStyle,
			CaptionPosition captionPos, int captionColorRed, int captionColorGreen, int captionColorBlue,
			int resizeToWidth, int resizeToHeight, Path imagePath) {
		
		try {
			return addAttachmentIcon(att.getFileName(), filenameToDisplay, captionText, captionStyle,
					captionPos, captionColorRed, captionColorGreen, captionColorBlue, resizeToWidth,
					resizeToHeight, imagePath, null);
		} catch (IOException e) {
			throw new DominoException(format("Could not add attachment icon for {0}", att.getFileName()), e);
		}
	}

	@Override
	public RichTextWriter addAttachmentIcon(String attachmentProgrammaticName, String filenameToDisplay, String captionText,
			FontStyle captionStyle,
			CaptionPosition captionPos, int captionColorRed, int captionColorGreen, int captionColorBlue,
			int resizeToWidth, int resizeToHeight, InputStream imageData) throws IOException {
		
		return addAttachmentIcon(attachmentProgrammaticName, filenameToDisplay, captionText,
			captionStyle,
			captionPos, captionColorRed, captionColorGreen, captionColorBlue,
			resizeToWidth, resizeToHeight, null, imageData);
		
	}
	
	private RichTextWriter addAttachmentIcon(String attachmentProgrammaticName, String filenameToDisplay, String captionText,
			FontStyle captionStyle,
			CaptionPosition captionPos, int captionColorRed, int captionColorGreen, int captionColorBlue,
			int resizeToWidth, int resizeToHeight, Path imagePath, InputStream imageData) throws IOException {

		checkDisposed();

		if (captionColorRed<0 || captionColorRed>255) {
			throw new IllegalArgumentException("Red value of color can only be between 0 and 255");
		}
		if (captionColorGreen<0 || captionColorGreen>255) {
			throw new IllegalArgumentException("Green value of color can only be between 0 and 255");
		}
		if (captionColorBlue<0 || captionColorBlue>255) {
			throw new IllegalArgumentException("Blue value of color can only be between 0 and 255");
		}
		
		addRichTextRecord(CDBegin.class, begin -> {
			begin.setSignature(RecordType.V4HOTSPOTBEGIN.getConstant());
		});
		m_hasData=true;

		addRichTextRecord(CDHotspotBegin.class, hotspotBegin -> {
			hotspotBegin.setHotspotType(CDHotspotBegin.Type.FILE);
			hotspotBegin.setFlags(EnumSet.of(CDHotspotBegin.Flag.NOBORDER));
			
			hotspotBegin.setFileNames(attachmentProgrammaticName, filenameToDisplay);
		});
		
		if (imageData!=null) {
			addImage(imageData, resizeToWidth, resizeToHeight);
		}
		else {
			addImage(imagePath, resizeToWidth, resizeToHeight);
		}
		
		addRichTextRecord(CDCaption.class, caption -> {
			if (captionPos==CaptionPosition.BELOWCENTER) {
				caption.setPosition(CDCaption.Position.BELOW_CENTER);
			} else if (captionPos==CaptionPosition.MIDDLECENTER) {
				caption.setPosition(CDCaption.Position.MIDDLE_CENTER);
			}
			caption.getFontID().getData().putInt(0, captionStyle.getData().getInt());
			
			caption.getFontColor().setRed((short)captionColorRed);
			caption.getFontColor().setGreen((short)captionColorGreen);
			caption.getFontColor().setBlue((short)captionColorBlue);
			caption.getFontColor().setFlags(EnumSet.of(ColorValue.Flag.ISRGB));
			
			caption.setCaptionText(captionText);
		});
		
		addRichTextRecord(CDHotspotEnd.class, hotspotEnd -> { });
		addRichTextRecord(CDEnd.class, end -> {
			end.setVersion(0);
			end.setSignature(RecordType.V4HOTSPOTEND.getConstant());
		});
		
		return this;
	}
	
	@Override
	public RichTextWriter addRichText(Document doc, String itemName) {
		if (!(doc instanceof JNADocument)) {
			throw new IncompatibleImplementationException(doc, JNADocument.class);
		}
		
		JNADocument jnaDoc = (JNADocument) doc;
		
		if (jnaDoc.isDisposed()) {
			throw new ObjectDisposedException(jnaDoc);
		}

		checkDisposed();
		
		Memory itemNameMem = NotesStringUtils.toLMBCS(itemName, true);

		JNADocumentAllocations docAllocations = (JNADocumentAllocations) jnaDoc.getAdapter(APIObjectAllocations.class);
		
		short result = LockUtil.lockHandles(getAllocations().getCompoundTextHandle(), docAllocations.getNoteHandle(),
				(hCompoundTextByVal, hNoteByVal) -> {
			return NotesCAPI.get().CompoundTextAssimilateItem(hCompoundTextByVal, hNoteByVal, itemNameMem, 0);
		});
		
		NotesErrorUtils.checkResult(result);
		m_hasData=true;
		
		return this;
	}

	@Override
	public RichTextWriter addRichText(RichTextWriter rt) {
		if (!(rt instanceof JNARichtextWriter)) {
			throw new IncompatibleImplementationException(rt, JNARichtextWriter.class);
		}
		
		JNARichtextWriter jnaRT = (JNARichtextWriter) rt;
		if (jnaRT.isDisposed()) {
			throw new ObjectDisposedException(jnaRT);
		}

		checkDisposed();

		try {
			jnaRT.close();
		} catch (Exception e) {
			throw new DominoException("Error closing specified richtext writer", e);
		}
		
		Document parentDoc = jnaRT.getParentDocument();
		String itemName = jnaRT.getItemName();
		
		return addRichText(parentDoc, itemName);
	}

	@Override
	public RichTextWriter addRichTextRecord(RichTextRecord<?> record) {
		checkDisposed();

		ByteBuffer recordData = record.getData();
		int len = record.getCDRecordLength();
		
		short result = LockUtil.lockHandle(getAllocations().getCompoundTextHandle(), (hCompoundTextByVal) -> {
			Pointer recordMem;
			try {
				recordMem = Native.getDirectBufferPointer(recordData);
			} catch(IllegalArgumentException e) {
				recordMem = new Memory(len);
				recordMem.write(0, recordData.array(), 0, len);
			}
			return NotesCAPI.get().CompoundTextAddCDRecords(hCompoundTextByVal, recordMem, len);
		});
		NotesErrorUtils.checkResult(result);
		m_hasData=true;
		
		return this;
	}

	@Override
	public void close() {
		if (isDisposed()) {
			return;
		}
		
		JNARichtextWriterAllocations allocations = getAllocations();
		if (allocations.isStandalone()) {
			allocations.closeStandaloneContext();
		}
		else {
			allocations.closeItemContext();
		}
	}
	
	public CloseResult getStandaloneRichtextCloseResult() {
		return getAllocations().getStandaloneContextCloseResult();
	}
	
	@Override
	public void discard() {
		checkDisposed();
		
		getAllocations().discard();
	}
	
	@Override
	public String toStringLocal() {
		return format("JNARichtextWriter [parentDoc={0}, itemname={1}]", (m_parentDoc!=null ? m_parentDoc.getUNID() : "null"), m_itemName); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Closes this standalone richtext (so no more additions are allowed) and copies its content
	 * to a note. This method may be called multiple times if you need to copy the content
	 * to more than one document.
	 * 
	 * @param doc target document
	 * @param richTextItemName name of richtext item in target note, existing richtext items with this name will be removed
	 */
	public void closeAndCopyToDoc(final Document doc, String richTextItemName) {
		if (!getAllocations().isStandalone()) {
			throw new DominoException("This is no standalone richtext");
		}
		
		CloseResult result = getAllocations().closeStandaloneContext();
		if (result.getType()!=CloseResultType.Buffer && result.getType()!=CloseResultType.File) {
			//should not happen
			throw new IllegalStateException(format("Unexpected close type received from compound text: {0}", result.getType()));
		}
		
		//collect old composite items to prepare later deletion
		final LinkedList<JNAItem> items = new LinkedList<>();
		
		doc.forEachItem(richTextItemName, (item, loop) -> {
			if (item.getType()==ItemDataType.TYPE_COMPOSITE && item instanceof JNAItem) {
				items.add((JNAItem)item);
			}
		});
		
		//build new compound text
		try (RichTextWriter rt = doc.createRichTextItem(richTextItemName)) {
			if (!(rt instanceof JNARichtextWriter)) {
				throw new IllegalArgumentException(format("Returned RichtextWriter is not a {0}", JNARichtextWriter.class.getName()));
			}
			try(JNARichtextWriter jnaRTWriter = (JNARichtextWriter) rt) {

				//and transfer the CD records from this compound text
				if (result.getType()==CloseResultType.Buffer) {
					final JNACompoundTextStandaloneBuffer buffer = result.getBuffer();
					FileInfo fileInfo;
					try {
						fileInfo = buffer.asFileOnDisk();
					} catch (IOException e1) {
						throw new DominoException(format("Could not extract compound text buffer to disk: {0}", buffer), e1);
					}
					jnaRTWriter.addCompoundTextFromFile(fileInfo.getFilePath());
				}
				else {
					//Domino created a temp file
					String filePath = result.getFilePath();
					jnaRTWriter.addCompoundTextFromFile(filePath);
				}
			}
		}

		//cleanup obsolete items read earlier
		for (JNAItem currOldItem : items) {
			currOldItem.remove();
		}
	}
	
	public RichTextRecordList closeAndGetRichTextNavigator() {
		CloseResult result = getAllocations().closeStandaloneContext();
		InputStream fIn;
		String filePath;
		long fileSize;
		
		if (result.getType()==CloseResultType.Buffer) {
			final JNACompoundTextStandaloneBuffer buffer = result.getBuffer();
			try {
				FileInfo fileInfo = buffer.asFileOnDisk();
				
				fIn = fileInfo.getStream();
				filePath = fileInfo.getFilePath();
				fileSize = fileInfo.getFileSize();
			} catch (IOException e) {
				throw new DominoException(0, "Error opening file stream for standalone compound text", e);
			}
		}
		else {
			//Domino created a temp file
			filePath = result.getFilePath();
			Path tmpFile = Paths.get(filePath);
			try {
				fileSize = Files.size(tmpFile);
				fIn = Files.newInputStream(tmpFile);
			} catch (IOException e) {
				throw new DominoException("Error opening file stream for standalone compound text", e);
			}
		}
		try {
			JNACDFileRichTextNavigator nav = new JNACDFileRichTextNavigator(getParentDominoClient(), filePath, fIn, fileSize, true);
			return new DefaultRichTextList(nav, null);
		} catch (IOException e) {
			throw new DominoException("Error creating richtext navigator for file stream", e);
		}
	}
	
	/** This routine assimilates the contents of a CD file (a file containing rich text) into a CompoundText context.<br>
	 * The contents are assimilated in that PABIDs and styles are fixed up (renumbered/renamed as needed) before they are
	 * appended to the CompoundText context.<br>
	 * <br>
	 * A CD file is a file containing data in Domino rich text format. CompoundTextClose() creates a CD file when closing
	 * a stand alone context containing more than 64K bytes of rich text.<br>
	 * A CD file consists of a datatype word (usually TYPE_COMPOSITE) followed by any number of CD records.<br>
	 * <br>
	 * The data type word is always in Host (machine-specific) format.<br>
	 * The remainder of the data in a CD file is in Domino Canonical format.
	 * 
	 * @param filePath path to file
	 */
	public void addCompoundTextFromFile(final String filePath) {
		checkDisposed();
		
		if (getAllocations().isClosed()) {
			throw new DominoException("CompoundText already closed");
		}

		long fileSize;
		try {
			fileSize = AccessController.doPrivileged((PrivilegedExceptionAction<Long>) () -> {
				Path file = Paths.get(filePath);
				if (!Files.exists(file)) {
					throw new NoSuchFileException(format("File does not exist: {0}", filePath));
				}
				return Files.size(file);
			});
		} catch (PrivilegedActionException e) {
			throw new DominoException(format("Error reading file size of file {0}", filePath), e);
		}
		
		if (fileSize==0) {
			//nothing to do
			return;
		}
		
		Memory filePathMem = NotesStringUtils.toLMBCS(filePath, true);
		
		short result = LockUtil.lockHandle(getAllocations().getCompoundTextHandle(), (hCompoundTextByVal) -> {
			return NotesCAPI.get().CompoundTextAssimilateFile(hCompoundTextByVal, filePathMem, 0);
		});

		NotesErrorUtils.checkResult(result);
		m_hasData=true;
	}
}
