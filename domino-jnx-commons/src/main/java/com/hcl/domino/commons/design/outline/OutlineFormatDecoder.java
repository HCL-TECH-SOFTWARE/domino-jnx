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
package com.hcl.domino.commons.design.outline;

import java.nio.ByteBuffer;
import java.text.MessageFormat;
import com.hcl.domino.commons.misc.ODSTypes;
import com.hcl.domino.commons.util.NotesItemDataUtil;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.design.OutlineEntry;
import com.hcl.domino.design.format.SiteMapEntry;
import com.hcl.domino.design.format.SiteMapHeaderFormat;
import com.hcl.domino.design.format.SiteMapOutlineHeader;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;

public class OutlineFormatDecoder {
  
  /**
   * Reads outline data from the provided raw buffer. This buffer should contain the outline
   * item data, without data-type markers, and potentially concatenated from across multiple
   * items.
   * 
   * @param data the data to read
   * @return a decoded {@link DominoOutlineFormat}
   */
  public static DominoOutlineFormat decodeOutlineFormat(ByteBuffer data) {
    
    DominoOutlineFormat result = new DominoOutlineFormat();
    SiteMapHeaderFormat header = NotesItemDataUtil.readMemory(data, ODSTypes._OUTLINE_FORMAT, SiteMapHeaderFormat.class);
    short items = header.getItems();
    short numEntries = header.getEntries();
//    byte  majorVersionByte = header.getMajorVersion();
//    byte  minorVersionByte = header.getMinorVersion();
//    short majorVersion = (short)majorVersionByte;
//    short minorVersion = (short)minorVersionByte;

    SiteMapOutlineHeader outlineHeader = NotesItemDataUtil.readMemory(data, ODSTypes._OUTLINE_FORMAT, SiteMapOutlineHeader.class);
    int flags = outlineHeader.getFlags();
    
    short[] itemEntries = new short[items];
    for (int i=0; i<items; i++) {
      itemEntries[i] = NotesItemDataUtil.readBuffer(data, 2).getShort();
    }
    for (int i=0; i<items; i++) {
      for (int j=0; j<itemEntries[i]; j++) {

        DominoOutlineEntry outlineEntry = new DominoOutlineEntry();
        SiteMapEntry sitemapEntry = NotesItemDataUtil.readMemory(data, ODSTypes._OUTLINE_FORMAT, SiteMapEntry.class);
        int fixedSize  = sitemapEntry.getEntryFixedSize();
        int varSize  = sitemapEntry.getEntryVarSize();
        if (fixedSize != MemoryStructureWrapperService.get().sizeOf(SiteMapEntry.class))
          break;
        
        int id = sitemapEntry.getId();
        int titleSize =  sitemapEntry.getTitleSize();
        int aliasSize  = sitemapEntry.getAliasSize();
        int entryTypeVal = sitemapEntry.getEntryType();
        int entryClassVal = sitemapEntry.getEntryClass();
        int entryDesignType = sitemapEntry.getEntryDesignType();
        int imageSize  = sitemapEntry.getImagesSize();
        int popupSize = sitemapEntry.getPopupSize();
        int onclickSize = sitemapEntry.getOnClickSize();
        int sourceSize = sitemapEntry.getSourceSize();
        int targetFrameSize = sitemapEntry.getTargetFrameSize();
        int hideWhenSize = sitemapEntry.getHideWhenSize();
        int preferredServerSize = sitemapEntry.getPreferredServerSize();
        int toolbarManagerSize = sitemapEntry.getToolbarManagerSize();
        int toolbarEntrySize = sitemapEntry.getToolbarEntrySize();
        
        outlineEntry.setFlags(sitemapEntry.getEntryFlags());
        outlineEntry.setResourceDesignType(sitemapEntry.getEntryDesignType());
        outlineEntry.setLevel(sitemapEntry.getLevel());
        DominoEnumUtil.valueOf(OutlineEntry.Type.class, sitemapEntry.getEntryType())
          .ifPresent(outlineEntry::setResourceType);
        DominoEnumUtil.valueOf(CDResource.ResourceClass.class, sitemapEntry.getEntryClass())
          .ifPresent(outlineEntry::setResourceClass);
        
      //read variable data
        if (titleSize > 0) {
          outlineEntry.setTitle(getEntryData(data, titleSize));
        }

        if (onclickSize > 0) {
          outlineEntry.setOnclickData(getEntryData(data, onclickSize));
        }
        
        if (imageSize > 0) {
          outlineEntry.setImageData(getEntryData(data, imageSize));
        }
        
        if (targetFrameSize > 0) {
          outlineEntry.setTargetFrame(getEntryData(data, targetFrameSize));
        }
        
        if (hideWhenSize > 0) {
          outlineEntry.setHideWhenFormula(getEntryData(data, hideWhenSize));
        }
        
        if (aliasSize > 0) {
          outlineEntry.setAlias(getEntryData(data, aliasSize));
        }
        
        if (sourceSize > 0) {
          outlineEntry.setSourceData(getEntryData(data, sourceSize));
        }
        
        if (preferredServerSize > 0) {
          outlineEntry.setPreferredServer(getEntryData(data, preferredServerSize));
        }

        //if (majorVersion == 1 && minorVersion > 3) {
        if (toolbarManagerSize > 0) {
          outlineEntry.setToolbarManager(getEntryData(data, toolbarManagerSize));
        }

        if (toolbarEntrySize > 0) {
          outlineEntry.setToolbarEntry(getEntryData(data, toolbarEntrySize));
        }
        //}

        //if (majorVersion == 1 && minorVersion >= 6) {
        if (popupSize > 0) {
          outlineEntry.setPopup(getEntryData(data, popupSize));
        }
        //}
        
        result.addOutlineEntry(outlineEntry);
       
        //skip rest of variable size for now
        int  skipVarSize  = varSize - (titleSize + onclickSize + imageSize + targetFrameSize + hideWhenSize + aliasSize + sourceSize + preferredServerSize + toolbarManagerSize + toolbarEntrySize +popupSize ) ;
        if(skipVarSize < 0) {
          throw new IllegalStateException(MessageFormat.format("skipVarSize resulted in a negative value: {0}", skipVarSize));
        }
        NotesItemDataUtil.readBuffer(data, skipVarSize);
      }
    }
        
    return  result;
  }
  
  private static DominoOutlineEntryData getEntryData(ByteBuffer data, int size)  {
    short dataType = readVariableDataDatatype(data);
    int sizeResized = recalculateDataSize(dataType, size, data);
    return new DominoOutlineEntryData(dataType, NotesItemDataUtil.getBufferBytes(data, sizeResized));
  }
  
  private static int  recalculateDataSize(short dataType, int datasize, ByteBuffer buf)  {
    int retVal = datasize;
    if (dataType == ItemDataType.TYPE_FORMULA.getValue() || dataType == ItemDataType.TYPE_TEXT.getValue()) {
      retVal = (short) (datasize - 2);
      buf.position(buf.position()+2);
    }
    
    return retVal;
  }
  
  private static short readVariableDataDatatype(ByteBuffer buf) {
    ByteBuffer result = NotesItemDataUtil.subBuffer(buf, 2);
    return result.getShort();
  }
}
