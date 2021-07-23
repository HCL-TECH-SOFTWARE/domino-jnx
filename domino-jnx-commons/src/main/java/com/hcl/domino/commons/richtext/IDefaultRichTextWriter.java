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
package com.hcl.domino.commons.richtext;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.function.Consumer;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.richtext.records.MemoryStructureProxy;
import com.hcl.domino.data.Attachment;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.TextStyle;
import com.hcl.domino.richtext.records.CDEvent;
import com.hcl.domino.richtext.records.CDFileHeader;
import com.hcl.domino.richtext.records.CDFileSegment;
import com.hcl.domino.richtext.records.CDGraphic;
import com.hcl.domino.richtext.records.CDImageHeader;
import com.hcl.domino.richtext.records.CDImageSegment;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.records.CDImageHeader.ImageType;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.MemoryStructure;

/**
 * This sub-interface of {@link RichTextWriter} provides default behavior
 * that is potentially common among different implementations.
 * 
 * @author Jesse Gallagher
 * @since 1.0.15
 */
public interface IDefaultRichTextWriter extends RichTextWriter {
	@Override
	default <T extends MemoryStructure> T createStructure(Class<T> structureClass, int variableDataLength) {
		return MemoryStructureProxy.newStructure(structureClass, variableDataLength);
	}

	@Override
	default RichTextWriter addText(String txt) {
		return addText(txt, (TextStyle) null, (FontStyle) null);
	}

	@Override
	default RichTextWriter addText(String txt, TextStyle textStyle, FontStyle fontStyle) {
		return addText(txt, textStyle, fontStyle, true);
	}
	
	@Override
	default RichTextWriter addImage(Path imagePath) {
		return addImage(imagePath, -1, -1);
	}

	@Override
	default RichTextWriter addImage(InputStream imageStream) {
		return addImage(imageStream, -1, -1);
	}
	
	@Override
	default RichTextWriter addImageResource(InputStream isParam, long fileLengthParam) throws IOException {
		if(fileLengthParam > 0xFFFFFFFFL) {
			throw new UnsupportedOperationException(MessageFormat.format("File is too large to store in rich text: {0} bytes", fileLengthParam));
		}
		Path tempFile;
		InputStream is;
		long fileLength;
		if(fileLengthParam == -1) {
			// Then read the full content into a temp file
			tempFile = Files.createTempFile(getClass().getSimpleName(), ".dat"); //$NON-NLS-1$
			Files.copy(isParam, tempFile, StandardCopyOption.REPLACE_EXISTING);
			is = Files.newInputStream(tempFile);
			fileLength = Files.size(tempFile);
		} else {
			is = isParam;
			tempFile = null;
			fileLength = fileLengthParam;
		}
		
		try {
			int segCount = (int)(fileLength / RichTextConstants.IMAGE_SEGMENT_MAX);
			if(fileLength % RichTextConstants.IMAGE_SEGMENT_MAX > 0) {
				segCount++;
			}
			int fSegCount = segCount;
			
			addRichTextRecord(CDGraphic.class, graphic -> {
				graphic.setVersion(CDGraphic.Version.VERSION3);
			});
			addRichTextRecord(CDImageHeader.class, header -> {
				header.setImageType(ImageType.GIF);
				// The actual height and width are not stored for image resources
				header.setWidth(0);
				header.setHeight(0);
				header.setImageDataSize(fileLength);
				header.setSegCount(fSegCount);
			});
			
			for(int i = 0; i < segCount; i++) {
				int dataOffset = RichTextConstants.IMAGE_SEGMENT_MAX * i;
				int dataSize = (int)Math.min(fileLength - dataOffset, RichTextConstants.IMAGE_SEGMENT_MAX);
				int segSize = dataSize + (dataSize % 2);
				byte[] segData = new byte[segSize];
				is.read(segData);
				
				addRichTextRecord(CDImageSegment.class, segSize, segment -> {
					segment.setDataSize(dataSize);
					segment.setSegSize(segSize);
					segment.setImageSegmentData(segData);
				});
			}
		} finally {
			if(tempFile != null) {
				is.close();
				try {
					Files.deleteIfExists(tempFile);
				} catch(IOException e) {
					// Ignore, since we can't do anything about it anyway
				}
			}
		}
		
		return this;
	}
	
	@Override
	default RichTextWriter addDocLink(Document doc, String comment) {
		Database parentDb = doc.getParentDatabase();
		String replicaId = parentDb.getReplicaID();
		String noteUnid = doc.getUNID();
		String collectionUnid;
		
		//we need the UNID of any collection in the db
		DominoCollection defaultCollection = parentDb.openDefaultCollection().orElse(null);
		if (defaultCollection!=null) {
			collectionUnid = defaultCollection.getUNID();
		}
		else {
			Optional<String> anyCollectionUnid = parentDb
					.getAllCollections()
					.map((colInfo) -> {
						String unid = parentDb.toUNID(colInfo.getNoteID());
						return unid;
					})
					.filter((unid) -> {
						return !(unid == null || unid.isEmpty());
					})
					.findFirst();

			if (anyCollectionUnid.isPresent()) {
				collectionUnid = anyCollectionUnid.get();
			}
			else {
				throw new DominoException(format("Unable to find any view in database {0}!!{1} which is required to produce the doclink", parentDb.getServer(), parentDb.getRelativeFilePath()));
			}
		}

		return addDocLink(replicaId, collectionUnid, noteUnid, comment);
	}
	
	@Override
	default RichTextWriter addDatabaseLink(Database db, String comment) {
		return addDocLink(db.getReplicaID(), null, null, comment);
	}

	@Override
	default RichTextWriter addCollectionLink(DominoCollection collection, String comment) {
		return addDocLink(collection.getParentDatabase().getReplicaID(), collection.getUNID(), null, comment);
	}
	
	@Override
	default RichTextWriter addAttachmentIcon(Attachment att, String captionTxt) {
		return addAttachmentIcon(att.getFileName(), captionTxt);
	}
	
	@Override
	default RichTextWriter addFileResource(InputStream isParam, long fileLengthParam) throws IOException {
		if(fileLengthParam > 0xFFFFFFFFL) {
			throw new UnsupportedOperationException(MessageFormat.format("File is too large to store in rich text: {0} bytes", fileLengthParam));
		}
		Path tempFile;
		InputStream is;
		long fileLength;
		if(fileLengthParam == -1) {
			// Then read the full content into a temp file
			tempFile = Files.createTempFile(getClass().getSimpleName(), ".dat"); //$NON-NLS-1$
			Files.copy(isParam, tempFile, StandardCopyOption.REPLACE_EXISTING);
			is = Files.newInputStream(tempFile);
			fileLength = Files.size(tempFile);
		} else {
			is = isParam;
			tempFile = null;
			fileLength = fileLengthParam;
		}
		
		try {
			int segCount = (int)fileLength / RichTextConstants.FILE_SEGMENT_SIZE_CAP;
			if (fileLength % RichTextConstants.FILE_SEGMENT_SIZE_CAP > 0) {
				segCount++;
			}
			int fSegCount = segCount;
			
			addRichTextRecord(CDFileHeader.class, header -> {
				header.setFileExtLen(0)
					.setFileDataSize(fileLength)
					.setSegCount(fSegCount);
			});
			
			for(int i = 0; i < segCount; i++) {
				int dataOffset = RichTextConstants.FILE_SEGMENT_SIZE_CAP * i;
				short dataSize = (short)Math.min((fileLength - dataOffset), RichTextConstants.FILE_SEGMENT_SIZE_CAP);
				short segSize = (short)(dataSize + (dataSize % 2));
				
				byte[] segData = new byte[dataSize];
				is.read(segData);
				
				addRichTextRecord(CDFileSegment.class, segSize, segment -> {
					segment.setDataSize(dataSize);
					segment.setSegSize(segSize);
					segment.setFileSegmentData(segData);
				});
			}
		} finally {
			if(tempFile != null) {
				is.close();
				try {
					Files.deleteIfExists(tempFile);
				} catch(IOException e) {
					// Ignore, since we can't do anything about it anyway
				}
			}
		}
		
		return this;
	}
	
	@Override
	default RichTextWriter addJavaScriptLibraryData(String script) {
		RichTextUtil.writeScriptLibrary(this, script, CDEvent.ActionType.JAVASCRIPT);
		return this;
	}
	
	@Override
	default <T extends RichTextRecord<?>> RichTextWriter addRichTextRecord(Class<T> recordClass, Consumer<T> processor) {
		return addRichTextRecord(recordClass, 0, processor);
	}

	@SuppressWarnings("unchecked")
	@Override
	default <T extends RichTextRecord<?>> RichTextWriter addRichTextRecord(Class<T> recordClass, int variableDataLength,
			Consumer<T> processor) {
		RecordType type = RecordType.forEncapsulationClass(recordClass);
		T record = MemoryStructureProxy.newStructure(recordClass, variableDataLength);
		record.getHeader().setSignature(type.getConstant());
		record.getHeader().setLength(MemoryStructureProxy.sizeOf(recordClass)+variableDataLength);
		
		processor.accept(record);
		
		addRichTextRecord(record);
		
		return this;
	}
	
	@Override
	default <T extends RichTextRecord<?>> RichTextWriter addRichTextRecord(RecordType recordType, Consumer<T> processor) {
		return addRichTextRecord(recordType, 0, processor);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	default <T extends RichTextRecord<?>> RichTextWriter addRichTextRecord(RecordType recordType, int variableDataLength, Consumer<T> processor) {
		Class<T> recordClass = (Class<T>)recordType.getEncapsulation();
		T record = MemoryStructureProxy.newStructure(recordClass, variableDataLength);
		record.getHeader().setSignature(recordType.getConstant());
		record.getHeader().setLength(MemoryStructureProxy.sizeOf(recordClass)+variableDataLength);
		
		processor.accept(record);
		
		addRichTextRecord(record);
		
		return this;
	}
	
	@Override
	default FontStyle createFontStyle() {
		return MemoryStructureProxy.newStructure(FontStyle.class, 0);
	}
}
