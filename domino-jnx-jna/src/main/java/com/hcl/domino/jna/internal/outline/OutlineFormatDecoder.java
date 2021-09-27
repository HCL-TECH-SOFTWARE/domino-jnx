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
package com.hcl.domino.jna.internal.outline;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import com.hcl.domino.commons.design.outline.DominoOutlineEntry;
import com.hcl.domino.commons.design.outline.DominoOutlineFormat;
import com.hcl.domino.commons.misc.ODSTypes;
import com.hcl.domino.commons.richtext.RichTextUtil;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.design.OutlineEntry;
import com.hcl.domino.design.format.SiteMapEntry;
import com.hcl.domino.design.format.SiteMapHeaderFormat;
import com.hcl.domino.design.format.SiteMapOutlineHeader;
import com.hcl.domino.jna.internal.MemoryUtils;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.sun.jna.Pointer;

public class OutlineFormatDecoder {
  
  public static DominoOutlineFormat decodeOutlineFormat(Pointer dataPtr, int valueLength) {
    
    DominoOutlineFormat result = new DominoOutlineFormat();
    ByteBuffer data = dataPtr.getByteBuffer(0, valueLength);
    SiteMapHeaderFormat header = MemoryUtils.readMemory(data, ODSTypes._OUTLINE_FORMAT, SiteMapHeaderFormat.class);
    short items = header.getItems();
    short numEntries = header.getEntries();
//    byte  majorVersionByte = header.getMajorVersion();
//    byte  minorVersionByte = header.getMinorVersion();
//    short majorVersion = (short)majorVersionByte;
//    short minorVersion = (short)minorVersionByte;

    SiteMapOutlineHeader outlineHeader = MemoryUtils.readMemory(data, ODSTypes._OUTLINE_FORMAT, SiteMapOutlineHeader.class);
    int flags = outlineHeader.getFlags();
    
    short[] itemEntries = new short[items];
    for (int i=0; i<items; i++) {
      itemEntries[i] = MemoryUtils.readBuffer(data, 2).getShort();
    }
    for (int i=0; i<items; i++) {
      for (int j=0; j<itemEntries[i]; j++) {

        DominoOutlineEntry outlineEntry = new DominoOutlineEntry();
        SiteMapEntry sitemapEntry = MemoryUtils.readMemory(data, ODSTypes._OUTLINE_FORMAT, SiteMapEntry.class);
        int fixedSize  = sitemapEntry.getEntryFixedSize();
        short varSize  = sitemapEntry.getEntryVarSize();
        //int entryFlags = sitemapEntry.getEntryFlags();
        int id = sitemapEntry.getId();
        short titleSize =  sitemapEntry.getTitleSize();
        short aliasSize  = sitemapEntry.getAliasSize();
        short entryTypeVal = sitemapEntry.getEntryType();
        short entryClassVal = sitemapEntry.getEntryClass();
        short entryDesignType = sitemapEntry.getEntryDesignType();
        short imageSize  = sitemapEntry.getImagesSize();
        short popupSize = sitemapEntry.getPopupSize();
        short onclickSize = sitemapEntry.getOnClickSize();
        short sourceSize = sitemapEntry.getSourceSize();
        short targetFrameSize = sitemapEntry.getTargetFrameSize();
        short hideWhenSize = sitemapEntry.getHideWhenSize();
        short preferredServerSize = sitemapEntry.getPreferredServerSize();
        short toolbarManagerSize = sitemapEntry.getToolbarManagerSize();
        short toolbarEntrySize = sitemapEntry.getToolbarEntrySize();
        
        outlineEntry.setFlags(sitemapEntry.getEntryFlags());
        outlineEntry.setResourceDesignType(sitemapEntry.getEntryDesignType());
        outlineEntry.setLevel(sitemapEntry.getLevel());
        DominoEnumUtil.valueOf(OutlineEntry.Type.class, sitemapEntry.getEntryType())
          .ifPresent(outlineEntry::setResourceType);
        DominoEnumUtil.valueOf(CDResource.ResourceClass.class, sitemapEntry.getEntryClass())
          .ifPresent(outlineEntry::setResourceClass);
        
        //read variable data
        if (titleSize > 0) {
          titleSize = recalculateDataSize(data.getShort(data.position()), titleSize, data);
          //String title = new String(getBufferBytes(data, titleSize), Charset.forName("LMBCS"));
          outlineEntry.setTitle(new String(MemoryUtils.getBufferBytes(data, titleSize), Charset.forName("LMBCS"))); //$NON-NLS-1$
        }

        if (onclickSize > 0) {
          short onclickDataDatatype = data.getShort(data.position());
          onclickSize = recalculateDataSize(onclickDataDatatype, onclickSize, data);
          ByteBuffer onclickdata = MemoryUtils.readBuffer(data, onclickSize);
          byte[] onclickdataBytes = new byte[onclickSize];
          onclickdata.get(onclickdataBytes, 0, onclickSize);
          List<RichTextRecord<?>> onclickData =  null;
          if (onclickDataDatatype == ItemDataType.TYPE_COMPOSITE.getValue()) {
            onclickData = RichTextUtil.readMemoryRecords(onclickdataBytes, RecordType.Area.RESERVED_INTERNAL);
          }
          outlineEntry.setOnclickData(onclickData);
        }
        
        if (imageSize > 0) {
          short imageDataDatatype = data.getShort(data.position());
          imageSize = recalculateDataSize(imageDataDatatype, imageSize, data);
          ByteBuffer imagedata = MemoryUtils.readBuffer(data, imageSize);
          byte[] imagedataBytes = new byte[imageSize];
          imagedata.get(imagedataBytes, 0, imageSize);
          List<RichTextRecord<?>> imageData =  null;
          if (imageDataDatatype == ItemDataType.TYPE_COMPOSITE.getValue()) {
            imageData = RichTextUtil.readMemoryRecords(imagedataBytes, RecordType.Area.RESERVED_INTERNAL);
          }
          outlineEntry.setImageData(imageData);
        }
        
        if (targetFrameSize > 0) {
          targetFrameSize = recalculateDataSize(data.getShort(data.position()), targetFrameSize, data);
          outlineEntry.setTargetFrame(new String(MemoryUtils.getBufferBytes(data, targetFrameSize), Charset.forName("LMBCS"))); //$NON-NLS-1$
        }
        
        if (hideWhenSize > 0) {
          hideWhenSize = recalculateDataSize(data.getShort(data.position()), hideWhenSize, data);
          outlineEntry.setHideWhenFormula(new String(MemoryUtils.getBufferBytes(data, hideWhenSize), Charset.forName("LMBCS"))); //$NON-NLS-1$
        }
        
        if (aliasSize > 0) {
          aliasSize = recalculateDataSize(data.getShort(data.position()), aliasSize, data);
          outlineEntry.setAlias(new String(MemoryUtils.getBufferBytes(data, aliasSize), Charset.forName("LMBCS"))); //$NON-NLS-1$
        }
        
        if (sourceSize > 0) {
          sourceSize = recalculateDataSize(data.getShort(data.position()), sourceSize, data);
          outlineEntry.setSourceData(new String(MemoryUtils.getBufferBytes(data, sourceSize), Charset.forName("LMBCS"))); //$NON-NLS-1$
        }
        
        if (preferredServerSize > 0) {
          preferredServerSize = recalculateDataSize(data.getShort(data.position()), preferredServerSize, data);
          outlineEntry.setPreferredServer(new String(MemoryUtils.getBufferBytes(data, preferredServerSize), Charset.forName("LMBCS"))); //$NON-NLS-1$
        }

        //if (majorVersion == 1 && minorVersion > 3) {
        if (toolbarManagerSize > 0) {
          toolbarManagerSize = recalculateDataSize(data.getShort(data.position()), toolbarManagerSize, data);
          outlineEntry.setToolbarManager(new String(MemoryUtils.getBufferBytes(data, toolbarManagerSize), Charset.forName("LMBCS"))); //$NON-NLS-1$
        }

        if (toolbarEntrySize > 0) {
          toolbarEntrySize = recalculateDataSize(data.getShort(data.position()), toolbarEntrySize, data);
          outlineEntry.setToolbarEntry(new String(MemoryUtils.getBufferBytes(data, toolbarEntrySize), Charset.forName("LMBCS"))); //$NON-NLS-1$
        }
        //}

        //if (majorVersion == 1 && minorVersion >= 6) {
        if (popupSize > 0) {
          popupSize = recalculateDataSize(data.getShort(data.position()), popupSize, data);
          outlineEntry.setPopup(new String(MemoryUtils.getBufferBytes(data, popupSize), Charset.forName("LMBCS"))); //$NON-NLS-1$
        }
        //}
        
        result.addOutlineEntry(outlineEntry);
       
        //skip rest of variable size for now
        int  skipVarSize  = varSize - (titleSize + onclickSize + imageSize + targetFrameSize + hideWhenSize + aliasSize + sourceSize + preferredServerSize + toolbarManagerSize + toolbarEntrySize +popupSize ) ;
        MemoryUtils.readBuffer(data, skipVarSize);
      }
    }
        
    return  result;
  }
  
  private static short  recalculateDataSize(short dataType, short datasize, ByteBuffer buf)  {
    short retVal = datasize;
    if (dataType == ItemDataType.TYPE_FORMULA.getValue()) {
      retVal = (short) (datasize - 2);
      buf.position(buf.position()+2);
    }
    
    return retVal;
  }
  
  

}
