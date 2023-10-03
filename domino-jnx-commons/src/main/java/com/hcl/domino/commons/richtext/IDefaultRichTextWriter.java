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
import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.data.Attachment;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.data.DominoCollectionInfo;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.TextStyle;
import com.hcl.domino.richtext.records.CDEvent;
import com.hcl.domino.richtext.records.CDFileHeader;
import com.hcl.domino.richtext.records.CDFileSegment;
import com.hcl.domino.richtext.records.CDGraphic;
import com.hcl.domino.richtext.records.CDImageHeader;
import com.hcl.domino.richtext.records.CDImageHeader.ImageType;
import com.hcl.domino.richtext.records.CDImageSegment;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RichTextRecord;
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
  default RichTextWriter addAttachmentIcon(final Attachment att, final String captionTxt) {
    return this.addAttachmentIcon(att.getFileName(), captionTxt);
  }

  @Override
  default RichTextWriter addCollectionLink(final DominoCollection collection, final String comment) {
    return this.addDocLink(collection.getParentDatabase().getReplicaID(), collection.getUNID(), null, comment);
  }

  @Override
  default RichTextWriter addDatabaseLink(final Database db, final String comment) {
    return this.addDocLink(db.getReplicaID(), null, null, comment);
  }

  @Override
  default RichTextWriter addDocLink(final Document doc, final String comment) {
    final Database parentDb = doc.getParentDatabase();
    final String replicaId = parentDb.getReplicaID();
    final String noteUnid = doc.getUNID();
    String collectionUnid;

    // we need the UNID of any collection in the db
    final DominoCollection defaultCollection = parentDb.openDefaultCollection().orElse(null);
    if (defaultCollection != null) {
      collectionUnid = defaultCollection.getUNID();
    } else {
      final Optional<String> anyCollectionUnid = parentDb
          .getAllCollections()
          .map(DominoCollectionInfo::getUNID)
          .findFirst();

      if (anyCollectionUnid.isPresent()) {
        collectionUnid = anyCollectionUnid.get();
      } else {
        throw new DominoException(
            MessageFormat.format("Unable to find any view in database {0}!!{1} which is required to produce the doclink",
                parentDb.getServer(), parentDb.getRelativeFilePath()));
      }
    }

    return this.addDocLink(replicaId, collectionUnid, noteUnid, comment);
  }

  @Override
  default RichTextWriter addFileResource(final InputStream isParam, final long fileLengthParam) throws IOException {
    if (fileLengthParam > 0xFFFFFFFFL) {
      throw new UnsupportedOperationException(
          MessageFormat.format("File is too large to store in rich text: {0} bytes", fileLengthParam));
    }
    Path tempFile;
    InputStream is;
    long fileLength;
    if (fileLengthParam == -1) {
      // Then read the full content into a temp file
      // TODO skip this, instead going back and altering the header to write total sizes after completion
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
      int segCount = (int) fileLength / RichTextConstants.FILE_SEGMENT_SIZE_CAP;
      if (fileLength % RichTextConstants.FILE_SEGMENT_SIZE_CAP > 0) {
        segCount++;
      }
      final int fSegCount = segCount;

      this.addRichTextRecord(CDFileHeader.class, header -> {
        header.setFileExtLen(0)
            .setFileDataSize(fileLength)
            .setSegCount(fSegCount);
      });

      for (int i = 0; i < segCount; i++) {
        final int dataOffset = RichTextConstants.FILE_SEGMENT_SIZE_CAP * i;
        final short dataSize = (short) Math.min(fileLength - dataOffset, RichTextConstants.FILE_SEGMENT_SIZE_CAP);
        final short segSize = (short) (dataSize + dataSize % 2);

        final byte[] segData = new byte[dataSize];
        is.read(segData);

        this.addRichTextRecord(CDFileSegment.class, segSize, segment -> {
          segment.setDataSize(dataSize);
          segment.setSegSize(segSize);
          segment.setFileSegmentData(segData);
        });
      }
    } finally {
      if (tempFile != null) {
        is.close();
        try {
          Files.deleteIfExists(tempFile);
        } catch (final IOException e) {
          // Ignore, since we can't do anything about it anyway
        }
      }
    }

    return this;
  }

  @Override
  default RichTextWriter addImage(final InputStream imageStream) {
    return this.addImage(imageStream, -1, -1);
  }

  @Override
  default RichTextWriter addImage(final Path imagePath) {
    return this.addImage(imagePath, -1, -1);
  }

  @Override
  default RichTextWriter addImageResource(final InputStream isParam, final long fileLengthParam) throws IOException {
    if (fileLengthParam > 0xFFFFFFFFL) {
      throw new UnsupportedOperationException(
          MessageFormat.format("File is too large to store in rich text: {0} bytes", fileLengthParam));
    }
    Path tempFile;
    InputStream is;
    long fileLength;
    if (fileLengthParam == -1) {
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
      int segCount = (int) (fileLength / RichTextConstants.IMAGE_SEGMENT_MAX);
      if (fileLength % RichTextConstants.IMAGE_SEGMENT_MAX > 0) {
        segCount++;
      }
      final int fSegCount = segCount;

      this.addRichTextRecord(CDGraphic.class, graphic -> {
        graphic.setVersion(CDGraphic.Version.VERSION3);
      });
      this.addRichTextRecord(CDImageHeader.class, header -> {
        header.setImageType(ImageType.GIF);
        // The actual height and width are not stored for image resources
        header.setWidth(0);
        header.setHeight(0);
        header.setImageDataSize(fileLength);
        header.setSegCount(fSegCount);
      });

      for (int i = 0; i < segCount; i++) {
        final int dataOffset = RichTextConstants.IMAGE_SEGMENT_MAX * i;
        final int dataSize = (int) Math.min(fileLength - dataOffset, RichTextConstants.IMAGE_SEGMENT_MAX);
        final int segSize = dataSize + dataSize % 2;
        final byte[] segData = new byte[segSize];
        is.read(segData);

        this.addRichTextRecord(CDImageSegment.class, segSize, segment -> {
          segment.setDataSize(dataSize);
          segment.setSegSize(segSize);
          segment.setImageSegmentData(segData);
        });
      }
    } finally {
      if (tempFile != null) {
        is.close();
        try {
          Files.deleteIfExists(tempFile);
        } catch (final IOException e) {
          // Ignore, since we can't do anything about it anyway
        }
      }
    }

    return this;
  }

  @Override
  default RichTextWriter addJavaScriptLibraryData(final String script) {
    RichTextUtil.writeScriptLibrary(this, script, CDEvent.ActionType.JAVASCRIPT);
    return this;
  }

  @Override
  default <T extends RichTextRecord<?>> RichTextWriter addRichTextRecord(final Class<T> recordClass, final Consumer<T> processor) {
    return this.addRichTextRecord(recordClass, 0, processor);
  }

  @SuppressWarnings("unchecked")
  @Override
  default <T extends RichTextRecord<?>> RichTextWriter addRichTextRecord(final Class<T> recordClass, final int variableDataLength,
      final Consumer<T> processor) {
    final RecordType type = RecordType.forEncapsulationClass(recordClass);
    final T record = MemoryStructureUtil.newStructure(recordClass, variableDataLength);
    record.getHeader().setSignature(type.getConstant());
    record.getHeader().setLength(MemoryStructureUtil.sizeOf(recordClass) + variableDataLength);

    processor.accept(record);

    this.addRichTextRecord(record);

    return this;
  }

  @Override
  default <T extends RichTextRecord<?>> RichTextWriter addRichTextRecord(final RecordType recordType, final Consumer<T> processor) {
    return this.addRichTextRecord(recordType, 0, processor);
  }

  @SuppressWarnings("unchecked")
  @Override
  default <T extends RichTextRecord<?>> RichTextWriter addRichTextRecord(final RecordType recordType, final int variableDataLength,
      final Consumer<T> processor) {
    final Class<T> recordClass = (Class<T>) recordType.getEncapsulation();
    final T record = MemoryStructureUtil.newStructure(recordClass, variableDataLength);
    record.getHeader().setSignature(recordType.getConstant());
    record.getHeader().setLength(MemoryStructureUtil.sizeOf(recordClass) + variableDataLength);

    processor.accept(record);

    this.addRichTextRecord(record);

    return this;
  }

  @Override
  default RichTextWriter addText(final String txt) {
    return this.addText(txt, (TextStyle) null, (FontStyle) null);
  }

  @Override
  default RichTextWriter addText(final String txt, final TextStyle textStyle, final FontStyle fontStyle) {
    return this.addText(txt, textStyle, fontStyle, true);
  }
  
  @Override
  default RichTextWriter addAttachmentIcon(String attachmentProgrammaticName, String captionTxt) {
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
  default RichTextWriter addAttachmentIcon(Attachment att, String filenameToDisplay, String captionText, FontStyle captionStyle,
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
  default RichTextWriter addAttachmentIcon(String attachmentProgrammaticName, String filenameToDisplay, String captionText,
          FontStyle captionStyle,
          CaptionPosition captionPos, int captionColorRed, int captionColorGreen, int captionColorBlue,
          int resizeToWidth, int resizeToHeight, InputStream imageData) throws IOException {
      
      return addAttachmentIcon(attachmentProgrammaticName, filenameToDisplay, captionText,
          captionStyle,
          captionPos, captionColorRed, captionColorGreen, captionColorBlue,
          resizeToWidth, resizeToHeight, null, imageData);
      
  }
  
  RichTextWriter addAttachmentIcon(String attachmentProgrammaticName, String filenameToDisplay, String captionText,
      FontStyle captionStyle,
      CaptionPosition captionPos, int captionColorRed, int captionColorGreen, int captionColorBlue,
      int resizeToWidth, int resizeToHeight, Path imagePath, InputStream imageData) throws IOException;

  @Override
  default FontStyle createFontStyle() {
    return MemoryStructureUtil.newStructure(FontStyle.class, 0);
  }

  @Override
  default <T extends MemoryStructure> T createStructure(final Class<T> structureClass, final int variableDataLength) {
    return MemoryStructureUtil.newStructure(structureClass, variableDataLength);
  }
}
