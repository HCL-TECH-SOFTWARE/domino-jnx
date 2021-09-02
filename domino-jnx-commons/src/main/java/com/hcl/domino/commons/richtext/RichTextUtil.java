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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.design.action.DefaultJavaScriptEvent;
import com.hcl.domino.commons.richtext.records.AbstractCDRecord;
import com.hcl.domino.commons.richtext.records.GenericBSIGRecord;
import com.hcl.domino.commons.richtext.records.GenericLSIGRecord;
import com.hcl.domino.commons.richtext.records.GenericWSIGRecord;
import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.Item;
import com.hcl.domino.design.action.EventId;
import com.hcl.domino.design.action.ScriptEvent;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.conversion.IRichTextConversion;
import com.hcl.domino.richtext.records.CDBlobPart;
import com.hcl.domino.richtext.records.CDEvent;
import com.hcl.domino.richtext.records.CDEventEntry;
import com.hcl.domino.richtext.records.CDField;
import com.hcl.domino.richtext.records.CDGraphic;
import com.hcl.domino.richtext.records.CDImageHeader;
import com.hcl.domino.richtext.records.CDImageHeader2;
import com.hcl.domino.richtext.records.CDImageSegment;
import com.hcl.domino.richtext.records.CDPabDefinition;
import com.hcl.domino.richtext.records.CDPabReference;
import com.hcl.domino.richtext.records.CDParagraph;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RichTextRecord;

/**
 * Contains common routines for working with rich text in an
 * implementation-independent way.
 *
 * @since 1.0.15
 */
public enum RichTextUtil {
  ;

  public static final Charset LMBCS = Charset.forName("LMBCS-native"); //$NON-NLS-1$

  /**
   * Creates a {@link AbstractCDRecord} implementation wrapper for the provided
   * record data.
   * 
   * @param signature the signature of the record
   * @param data      a {@link ByteBuffer} referencing the record data
   * @return a {@link RichTextRecord} implementation wrapping the data
   */
  public static AbstractCDRecord<?> encapsulateRecord(final short signature, final ByteBuffer data) {
    ByteBuffer recordData = data.slice().order(ByteOrder.LITTLE_ENDIAN);
    
    byte byte1 = recordData.get(0);
    byte byte2 = recordData.get(1);
    int length;
    Function<ByteBuffer, AbstractCDRecord<?>> genericProvider;
    if((byte2 & 0xFF) == 0xFF) {
      // Then it's a WSIG, two WORD values
      length = Short.toUnsignedInt(recordData.getShort(2));
      genericProvider = GenericWSIGRecord::new;
    } else if(byte2 == 0) {
      // Then it's an LSIG, which zeros the high-order byte
      // Length is a DWORD following the initial WORD
      ByteBuffer sliced = recordData.slice().order(ByteOrder.LITTLE_ENDIAN);
      sliced.position(2);
      length = sliced.getInt();
      genericProvider = GenericLSIGRecord::new;
    } else {
      // Then it's a BSIG, with the length in the high-order byte
      length = byte2 & 0xFF;
      genericProvider = GenericBSIGRecord::new;
    }
    recordData.limit(length);

    return genericProvider.apply(recordData);
  }

  /**
   * Re-encapsulates the provided record (such as one provided by
   * {@link #encapsulateRecord(short, ByteBuffer)}) with
   * the new encapsulation interface.
   * 
   * @param record             the {@link AbstractCDRecord} implementation to
   *                           re-encapsulate
   * @param encapsulationClass the new interface to use
   * @return a {@link RichTextRecord} implementation using the data from
   *         {@code record} and implementing the new interface
   */
  public static RichTextRecord<?> reencapsulateRecord(final AbstractCDRecord<?> record,
      final Class<? extends RichTextRecord<?>> encapsulationClass) {
    if (record instanceof GenericBSIGRecord) {
      return MemoryStructureUtil.forStructure(encapsulationClass, new GenericBSIGRecord(record.getData(), encapsulationClass));
    } else if (record instanceof GenericWSIGRecord) {
      return MemoryStructureUtil.forStructure(encapsulationClass, new GenericWSIGRecord(record.getData(), encapsulationClass));
    } else if (record instanceof GenericLSIGRecord) {
      return MemoryStructureUtil.forStructure(encapsulationClass, new GenericLSIGRecord(record.getData(), encapsulationClass));
    } else {
      throw new IllegalArgumentException(
          MessageFormat.format("Unable to determine encapsulation for {0}", record.getClass().getName()));
    }
  }

  public static void writeImageRecords(final RichTextWriter w, final InputStream is, final long fileSize, final int imgWidth,
      final int imgHeight, final int resizeToWidth, final int resizeToHeight, final CDImageHeader.ImageType imageType) {
    // write graphic header
    w.addRichTextRecord(CDGraphic.class, graphic -> {
      final boolean isResized = resizeToWidth != -1 && resizeToHeight != -1;

      // DestSize : RECTSIZE (Word/Word)
      if (isResized) {
        graphic.getDestSize().setWidth(resizeToWidth);
        graphic.getDestSize().setHeight(resizeToHeight);
      } else {
        graphic.getDestSize().setWidth(0);
        graphic.getDestSize().setHeight(0);
      }

      // CropSize : RECTSIZE (Word/Word)
      graphic.getCropSize().setWidth(resizeToWidth);
      graphic.getCropSize().setHeight(resizeToHeight);
      // CropOffset : CROPRECT
      graphic.getCropOffset().setTop(0);
      graphic.getCropOffset().setLeft(0);
      graphic.getCropOffset().setRight(0);
      graphic.getCropOffset().setBottom(0);
      // fResize : WORD
      if (isResized) {
        graphic.setResize((short) 1);
      } else {
        graphic.setResize((short) 0);
      }
      // Version: BYTE
      graphic.setVersion(CDGraphic.Version.VERSION3);

      // Flags:
      graphic.setFlags(EnumSet.of(CDGraphic.Flag.DESTSIZE_IS_PIXELS));
    });

    final int fullSegments = (int) (fileSize / RichTextConstants.IMAGE_SEGMENT_MAX);
    int segments = fullSegments;
    final int dataBytesInLastSegment = (int) (fileSize - fullSegments * RichTextConstants.IMAGE_SEGMENT_MAX);
    if (dataBytesInLastSegment > 0) {
      segments++;
    }
    final int fSegments = segments;

    // write image header
    w.addRichTextRecord(CDImageHeader.class, imageHeader -> {
      if (imageType == CDImageHeader.ImageType.PNG) {
        // R9 introduced PNG rendering support, but for the type they use BMP
        // and added a new (undocumented) CD record type right after CDIMAGEHEADER that
        // probably contains the actual image type (0x0004 for PNG) and the image size
        // as DWORD
        imageHeader.setImageType(CDImageHeader.ImageType.BMP);

        // for PNG, set filesize 0x00000000, probably to prevent older clients from
        // reading the data
        imageHeader.setImageDataSize(0);

        // for PNG, set segments to 0x00000000, probably to prevent older clients from
        // reading the data
        imageHeader.setSegCount(0);
      } else {
        imageHeader.setImageType(imageType);
        imageHeader.setImageDataSize(fileSize);
        imageHeader.setSegCount(fSegments);
      }

      imageHeader.setWidth(imgWidth);
      imageHeader.setHeight(imgHeight);
    });

    if (imageType == CDImageHeader.ImageType.PNG) {
      // for PNG we add an undocumented CD record type to define the image type,
      // number of chunks and filesize

      w.addRichTextRecord(CDImageHeader2.class, imageHeader2 -> {
        imageHeader2.setImageType(CDImageHeader.ImageType.PNG);
        imageHeader2.setImageDataSize(fileSize);
        imageHeader2.setSegCount(fSegments);
      });
    }

    final byte[] buf = new byte[RichTextConstants.IMAGE_SEGMENT_MAX];
    int len;
    int bytesRead = 0;

    try {
      for (int i = 0; i < segments; i++) {
        Arrays.fill(buf, (byte) 0);

        len = is.read(buf);
        if (i < segments - 1) {
          if (len < RichTextConstants.IMAGE_SEGMENT_MAX) {
            throw new IllegalStateException(
                MessageFormat.format("The InputStream returned {0} instead of {1}", bytesRead + len, fileSize));
          }
        } else {
          // last segment
          if (len < 0) {
            throw new IllegalStateException(
                MessageFormat.format("The InputStream returned {0} instead of {1}", bytesRead, fileSize));
          }
        }
        bytesRead += len;
        final int fLen = len;

        // write image segment
        int segSize = len;
        if ((segSize & 1L) == 1) {
          segSize++;
        }

        final int fSegSize = segSize;

        w.addRichTextRecord(CDImageSegment.class, segSize, imageSeg -> {
          imageSeg.setDataSize(fLen);
          imageSeg.setSegSize(fSegSize);
          imageSeg.setImageSegmentData(buf, fLen);
        });
      }
    } catch (final IOException e1) {
      throw new DominoException("Error reading image data from stream", e1);
    }
  }

  public static void writeScriptLibrary(final RichTextWriter w, final String script, final CDEvent.ActionType libraryType) {
    final byte[] bytes = script.getBytes(RichTextUtil.LMBCS);

    final int fileLength = bytes.length;
    int segCount = fileLength / RichTextConstants.BLOBPART_SIZE_CAP;
    if (fileLength % RichTextConstants.BLOBPART_SIZE_CAP > 0) {
      segCount++;
    }

    final int paddedLength = fileLength + 1; // Make sure there's at least one \0 at the end
    w.addRichTextRecord(CDEvent.class, event -> {
      event.setEventType(EventId.LIBRARY);
      event.setActionType(libraryType);
      event.setActionLength(paddedLength + paddedLength % 2);
    });
    for (int i = 0; i < segCount; i++) {
      final int dataOffset = RichTextConstants.BLOBPART_SIZE_CAP * i;
      final short dataSize = (short) Math.min(paddedLength - dataOffset, RichTextConstants.BLOBPART_SIZE_CAP);
      final short segSize = (short) (dataSize + dataSize % 2);
      final byte[] segData = Arrays.copyOfRange(bytes, dataOffset, dataOffset + dataSize);

      w.addRichTextRecord(CDBlobPart.class, segSize, part -> {
        part.setOwnerSig(RichTextConstants.SIG_CD_EVENT);
        part.setLength(dataSize);
        part.setBlobMax(RichTextConstants.BLOBPART_SIZE_CAP);
        part.setBlobPartData(segData);
      });
    }
  }
  
  /**
   * Reads the provided byte array to provide a {@link List} of encapsulated
   * rich text records.
   * 
   * <p>It is expected that the buffer starts with a type WORD - almost definitely
   * {@code TYPE_ACTION} ({@code 0x0010}), but this value will be ignored to avoid
   * ODS bugs described in the C API examples.</p>
   * 
   * @param data the in-memory composite data to parse
   * @param area the {@link RecordType.Area} type to use to interpret signatures
   * @return a {@link List} of {@link RichTextRecord} instances
   */
  public static List<RichTextRecord<?>> readMemoryRecords(byte[] data, RecordType.Area area) {
    // Each record begins with one of three headers: BSIG, WSIG, or LSIG
    // WSIG: identifiable by 0xFF in the high-order byte
    // LSIG: identifiable by 0x00 in the high-order byte
    // BSIG: anything else
    
    List<RichTextRecord<?>> result = new ArrayList<>();
    ByteBuffer buf = ByteBuffer.wrap(data);
    
    // Read and discard the type WORD
    buf.getShort();
    
    while(buf.hasRemaining()) {
      ByteBuffer recordData = buf.slice().order(ByteOrder.LITTLE_ENDIAN);
      
      byte byte1 = recordData.get(0);
      byte byte2 = recordData.get(1);
      short constant;
      int length;
      Function<ByteBuffer, AbstractCDRecord<?>> genericProvider;
      if((byte2 & 0xFF) == 0xFF) {
        // Then it's a WSIG, two WORD values
        constant = (short)(byte1 | 0xFF00);
        length = Short.toUnsignedInt(recordData.getShort(2));
        genericProvider = GenericWSIGRecord::new;
      } else if(byte2 == 0) {
        // Then it's an LSIG, which zeros the high-order byte
        constant = (short)(byte1 & 0x00FF);
        // Length is a DWORD following the initial WORD
        ByteBuffer sliced = recordData.slice().order(ByteOrder.LITTLE_ENDIAN);
        sliced.position(2);
        length = sliced.getInt();
        genericProvider = GenericLSIGRecord::new;
      } else {
        // Then it's a BSIG, with the length in the high-order byte
        constant = (short)(byte1 & 0xFF);
        length = byte2 & 0xFF;
        genericProvider = GenericBSIGRecord::new;
      }
      recordData.limit(length);

      RecordType type = RecordType.getRecordTypeForConstant(constant, area);
      AbstractCDRecord<?> record = genericProvider.apply(recordData);
      Class<? extends RichTextRecord<?>> encapsulation = type.getEncapsulation();
      if(encapsulation == null) {
        result.add(record);
      } else {
        result.add(reencapsulateRecord(record, encapsulation));
      }
      
      // These are always stored at WORD boundaries
      buf.position(buf.position() + length + (length % 2));
    }
    
    return result;
  }
  
  /**
   * Processes the provided CD record stream and reads JavaScript events into objects.
   * 
   * @param records a {@link List} of {@link RichTextRecord}s to process
   * @return a {@link List} of encapsulated {@link ScriptEvent}s
   * @since 1.0.34
   */
  public static List<ScriptEvent> readJavaScriptEvents(List<RichTextRecord<?>> records) {
    List<ScriptEvent> events = new ArrayList<>();
    // This is formatted as a series of CDEVENT records followed by CDBLOBPARTs
    // Client ones are distinguished by using SIG_CD_CLIENT_EVENT and SIG_CD_CLIENT_BLOBPART
    EventId currentEvent = null;
    boolean currentClient = false;
    StringBuilder currentScript = new StringBuilder();
    long currentLength = 0;
    for(RichTextRecord<?> record : records) {
      if(record instanceof CDEvent) {
        // Start of a new event - flush any existing value and start a new one
        if(currentEvent != null) {
          events.add(new DefaultJavaScriptEvent(currentEvent, currentClient, currentScript.toString()));
          currentScript.setLength(0);
          currentLength = 0;
        }
        
        currentEvent = ((CDEvent)record).getEventType();
        currentClient = ((CDEvent)record).getHeader().getSignature() == RichTextConstants.SIG_CD_CLIENT_EVENT;
        currentLength = ((CDEvent)record).getActionLength();
      }
      if(record instanceof CDBlobPart) {
        // The length stored here may be longer than the actual action length if it's to fit a WORD boundary
        long byteCount = Math.min(((CDBlobPart)record).getLength(), currentLength);
        byte[] data = ((CDBlobPart)record).getBlobPartData();
        currentScript.append(new String(data, 0, (int)byteCount, Charset.forName("LMBCS"))); //$NON-NLS-1$
        currentLength -= byteCount;
      }
      if(record instanceof CDEventEntry) {
        // These are present for each permutation, but have unclear use when the above handles it all
      }
    }
    // Finish any laggards
    if(currentEvent != null) {
      events.add(new DefaultJavaScriptEvent(currentEvent, currentClient, currentScript.toString()));
    }
    return events;
  }

  /**
   * Adds richtext from another document to a {@link RichTextWriter} with optimizations
   * 
   * @param docTarget target document
   * @param targetRTWriter target richtext writer
   * @param docSource source document
   * @param itemNameSource source richtext item name
   * @param removeFirstPar true to remove the first CDParagraph/CDPabDefinition/CDPabReference block
   */
  public static void addOtherRichTextItem(Document docTarget, RichTextWriter targetRTWriter,
		  Document docSource, String itemNameSource, boolean removeFirstPar) {
	  
	  Document docWork = docSource;
	  boolean disposeDocWork = false;
	  
	  if (removeFirstPar) {
		  docWork = docSource.copyToDatabase(docSource.getParentDatabase());
		  
		  docWork.convertRichTextItem(itemNameSource, new StripFirstParConversion());
		  disposeDocWork = true;
	  }
	  
	  targetRTWriter.addRichText(docWork, itemNameSource);
	  
	  if (docWork.getDocumentClass().contains(DocumentClass.FORM)) {
		  //copy LS code for fields
		  Set<String> fieldNames = docSource.getRichTextItem(itemNameSource)
				  .stream()
				  .filter(CDField.class::isInstance)
				  .map(CDField.class::cast)
				  .map(CDField::getName)
				  .collect(Collectors.toSet());

		  for (String currFieldName : fieldNames) {
			  Optional<Item> lsCodeItem = docWork.getFirstItem("$$" + currFieldName);
			  if (lsCodeItem.isPresent()) {
				  lsCodeItem.get().copyToDocument(docTarget, false);
			  }
		  }
	  }
	  
	  if (disposeDocWork) {
		  docWork.autoClosable().close();
	  }
  }

  private static class StripFirstParConversion implements IRichTextConversion {

	  @Override
	  public boolean isMatch(List<RichTextRecord<?>> nav) {
		  return nav
				  .stream()
				  .anyMatch(CDParagraph.class::isInstance);
	  }

	  @Override
	  public void richTextNavigationStart() {
	  }

	  @Override
	  public void richTextNavigationEnd() {
	  }

	  @Override
	  public void convert(List<RichTextRecord<?>> source, RichTextWriter target) {
		  boolean parFound = false;

		  ListIterator<RichTextRecord<?>> sourceRecordIt = source.listIterator();
		  while (sourceRecordIt.hasNext()) {
			  RichTextRecord<?> record = sourceRecordIt.next();
			  if (record instanceof CDParagraph) {
				  if (!parFound) {
					  parFound = true;

					  if (sourceRecordIt.hasNext()) {
						  RichTextRecord<?> nextRecord = sourceRecordIt.next();
						  if (nextRecord instanceof CDPabDefinition) {
							  //keep paragraph definition in case it is used somewhere else
							  target.addRichTextRecord(nextRecord);
							  
							  //ignore pab definition as well
							  if (sourceRecordIt.hasNext()) {
								  RichTextRecord<?> nextRecord2 = sourceRecordIt.next();
								  if (nextRecord2 instanceof CDPabReference) {
									  //ignore pab reference
								  }
								  else {
									  target.addRichTextRecord(nextRecord2);
								  }
							  }
						  }
						  else {
							  target.addRichTextRecord(nextRecord);
						  }
					  }
				  }
				  else {
					  target.addRichTextRecord(record);
				  }
			  }
			  else {
				  target.addRichTextRecord(record);
			  }
		  }
	  }

  }
}
