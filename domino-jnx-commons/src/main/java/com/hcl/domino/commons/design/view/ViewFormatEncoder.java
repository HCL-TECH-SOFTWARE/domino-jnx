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
package com.hcl.domino.commons.design.view;

import static com.hcl.domino.commons.util.NotesItemDataUtil.ensureBufferCapacity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.data.CollectionColumn;
import com.hcl.domino.data.NativeItemCoder;
import com.hcl.domino.data.NativeItemCoder.LmbcsVariant;
import com.hcl.domino.design.format.ViewCalendarFormat;
import com.hcl.domino.design.format.ViewCalendarFormat2;
import com.hcl.domino.design.format.ViewColumnFormat;
import com.hcl.domino.design.format.ViewColumnFormat2;
import com.hcl.domino.design.format.ViewColumnFormat4;
import com.hcl.domino.design.format.ViewColumnFormat5;
import com.hcl.domino.design.format.ViewColumnFormat6;
import com.hcl.domino.design.format.ViewTableFormat;
import com.hcl.domino.design.format.ViewTableFormat2;
import com.hcl.domino.design.format.ViewTableFormat3;
import com.hcl.domino.design.format.ViewTableFormat4;
import com.hcl.domino.richtext.records.CDResource;

/**
 * Utility class to encode the $ViewFormat item value
 * 
 * @author Karsten Lehmann
 * @since 1.2.4
 */
public class ViewFormatEncoder {

  /**
   * Encodes a {@link DominoViewFormat} describing the style of a view and of its columns in
   * binary form. The result is expected to be readible via {@link ViewFormatDecoder#decodeViewFormat(com.sun.jna.Pointer, int)}
   * without data loss.
   * 
   * @param viewFormat view design
   * @return bytebuffer with data
   */
  public static ByteBuffer encodeViewFormat(DominoViewFormat viewFormat) {
    if (viewFormat.getColumnCount()==0) {
      throw new IllegalArgumentException("Columns cannot be empty");
    }
    
    //validate that we have the current column type
    for (CollectionColumn currCol : viewFormat.getColumns()) {
      if (currCol instanceof DominoCollectionColumn ==false) {
        throw new IllegalArgumentException(MessageFormat.format("Column must be of type {0}: {1}", DominoCollectionColumn.class.getName(), currCol.getClass().getName()));
      }
    }

    List<DominoCollectionColumn> columns = viewFormat.getColumns()
        .stream()
        .map(DominoCollectionColumn.class::cast)
        .collect(Collectors.toList());

    ByteBuffer buf;

    {
      //write basic table format
      ViewTableFormat format1 = viewFormat.getAdapter(ViewTableFormat.class);
      if (format1==null) {
        format1 = ViewTableFormat.newInstanceWithDefaults();
        format1.setColumnCount(viewFormat.getColumnCount());
      }
      ByteBuffer format1Data = format1.getData();
      int format1Capacity = format1Data.capacity();

      buf = ByteBuffer.allocate(format1Capacity).order(ByteOrder.nativeOrder());
      buf.put(format1Data);
    }
    
    //write fixed part of VIEW_COLUMN_FORMAT first
    for (int i=0; i<columns.size(); i++) {
      DominoCollectionColumn col = columns.get(i);
      
      ViewColumnFormat fmt = col.getAdapter(ViewColumnFormat.class);
      if (fmt==null) {
        fmt = ViewColumnFormat.newInstanceWithDefaults();
      }
      
      //ignore var data with hide-when formula and twistie resource here, will be stored later after the ViewTableFormat3
      ByteBuffer fmtVarData = fmt.getVariableData();
      ByteBuffer fmtData = fmt.getData();
      
      byte[] fmtFixedData = new byte[fmtData.capacity() - fmtVarData.capacity()];
      fmtData.get(fmtFixedData);
      buf = ensureBufferCapacity(buf, buf.capacity() + fmtFixedData.length);
      buf.put(fmtFixedData);
    }

    //followed by the var data of VIEW_COLUMN_FORMAT
    for (int i=0; i<columns.size(); i++) {
      DominoCollectionColumn col = columns.get(i);
      
      ViewColumnFormat fmt = col.getAdapter(ViewColumnFormat.class);
      if (fmt==null) {
        fmt = ViewColumnFormat.newInstanceWithDefaults();
      }
      
      //ignore var data with hide-when formula and twistie resource here, will be stored later after the ViewTableFormat3
      ByteBuffer fmtVarData = fmt.getVariableData();
      buf = ensureBufferCapacity(buf, buf.capacity() + fmtVarData.capacity());
      buf.put(fmtVarData);
    }

    {
      //write VIEW_TABLE_FORMAT2
      ViewTableFormat2 fmt2 = viewFormat.getAdapter(ViewTableFormat2.class);
      if (fmt2==null) {
        fmt2 = ViewTableFormat2.newInstanceWithDefaults();
      }
      ByteBuffer fmt2Data = fmt2.getData();
      int fmt2Capacity = fmt2Data.capacity();
      
      buf = ensureBufferCapacity(buf, buf.capacity() + fmt2Capacity);
      buf.put(fmt2Data);
    }

    //write fixed part of VIEW_COLUMN_FORMAT2 first
    for (int i=0; i<columns.size(); i++) {
      DominoCollectionColumn col = columns.get(i);
      
      ViewColumnFormat2 fmt2 = col.getAdapter(ViewColumnFormat2.class);
      if (fmt2==null) {
        fmt2 = ViewColumnFormat2.newInstanceWithDefaults();
      }
      
      //ignore var data with hide-when formula and twistie resource here, will be stored later after the ViewTableFormat3
      ByteBuffer fmt2VarData = fmt2.getVariableData();
      ByteBuffer fmt2Data = fmt2.getData();
      
      byte[] fmt2FixedData = new byte[fmt2Data.capacity() - fmt2VarData.capacity()];
      fmt2Data.get(fmt2FixedData);

      buf = ensureBufferCapacity(buf, buf.capacity() + fmt2FixedData.length);
      buf.put(fmt2FixedData);
    }

    {
      //write VIEW_TABLE_FORMAT3
      ViewTableFormat3 fmt3 = viewFormat.getAdapter(ViewTableFormat3.class);
      if (fmt3==null) {
        fmt3 = ViewTableFormat3.newInstanceWithDefaults();
      }
      ByteBuffer fmt3Data = fmt3.getData();
      int fmt3Capacity = fmt3Data.capacity();
      
      buf = ensureBufferCapacity(buf, buf.capacity() + fmt3Capacity);
      buf.put(fmt3Data);
    }

    // write variable data defined by VIEW_COLUMN_FORMAT2 (hide-when formula and twistie CDRESOURCE)
    for (int i=0; i<columns.size(); i++) {
      DominoCollectionColumn col = columns.get(i);
      
      ViewColumnFormat2 fmt2 = col.getAdapter(ViewColumnFormat2.class);
      if (fmt2==null) {
        fmt2 = ViewColumnFormat2.newInstanceWithDefaults();
      }
      
      ByteBuffer fmt2VarData = fmt2.getVariableData();
      
      int hideWhenFormulaLength = fmt2.getHideWhenFormulaLength();
      int twistieResourceLength = fmt2.getTwistieResourceLength();
      
      buf = ensureBufferCapacity(buf, buf.capacity() + hideWhenFormulaLength + twistieResourceLength);
      
      if (hideWhenFormulaLength>0) {
        byte[] compiledHideWhenFormula = new byte[hideWhenFormulaLength];
        fmt2VarData.get(compiledHideWhenFormula);
        buf.put(compiledHideWhenFormula);
      }
      
      if (twistieResourceLength>0) {
        byte[] twistieResourceData = new byte[twistieResourceLength];
        fmt2VarData.get(twistieResourceData);
        buf.put(twistieResourceData);
      }
    }
    
    {
      //write VIEW_TABLE_FORMAT4
      ViewTableFormat4 fmt4 = viewFormat.getAdapter(ViewTableFormat4.class);
      if (fmt4==null) {
        fmt4 = ViewTableFormat4.newInstanceWithDefaults();
      }
      ByteBuffer fmt4Data = fmt4.getData();
      int fmt4Capacity = fmt4Data.capacity();
      
      buf = ensureBufferCapacity(buf, buf.capacity() + fmt4Capacity);
      buf.put(fmt4Data);
    }

    CDResource backgroundResource = viewFormat.getBackgroundResource();
    if (backgroundResource!=null) {
      //write background image
      ByteBuffer backgroundResourceData = backgroundResource.getData();
      buf = ensureBufferCapacity(buf, buf.capacity() + backgroundResourceData.capacity());
      buf.put(backgroundResourceData);
    }


    // VIEW_COLUMN_FORMAT4 - number format
    for (int i=0; i<columns.size(); i++) {
      DominoCollectionColumn col = columns.get(i);
      ViewColumnFormat2 fmt2 = col.getAdapter(ViewColumnFormat2.class);
      
      if(fmt2.getFlags().contains(ViewColumnFormat2.Flag3.NumberFormat)) {
        ViewColumnFormat4 fmt4 = col.getAdapter(ViewColumnFormat4.class);
        if (fmt4==null) {
          fmt4 = ViewColumnFormat4.newInstanceWithDefaults();
        }
        
        ByteBuffer fmt4Data = fmt4.getData();
        buf = ensureBufferCapacity(buf, buf.capacity() + fmt4Data.capacity());
        buf.put(fmt4Data);
      }
    }
    
    // VIEW_COLUMN_FORMAT5 - names format
    for (int i=0; i<columns.size(); i++) {
      DominoCollectionColumn col = columns.get(i);
      ViewColumnFormat2 fmt2 = col.getAdapter(ViewColumnFormat2.class);
      
      if(fmt2.getFlags().contains(ViewColumnFormat2.Flag3.NamesFormat)) {
        ViewColumnFormat5 fmt5 = col.getAdapter(ViewColumnFormat5.class);
        if (fmt5==null) {
          fmt5 = ViewColumnFormat5.newInstanceWithDefaults();
        }
        
        ByteBuffer fmt5Data = fmt5.getData();
        buf = ensureBufferCapacity(buf, buf.capacity() + fmt5Data.capacity());
        buf.put(fmt5Data);
      }
    }

    Charset lmbcsCharset = NativeItemCoder.get().getLmbcsCharset(LmbcsVariant.NORMAL);
    
    // Hidden titles follow as WORD-prefixed P-strings
    for(int i = 0; i < columns.size(); i++) {
      DominoCollectionColumn col = columns.get(i);
      if(col.isHideTitle()) {
        String hiddenTitle = col.getTitle();
        byte[] hiddenTitleData = hiddenTitle.getBytes(lmbcsCharset);
        if (hiddenTitleData.length > 65535) {
          throw new IllegalArgumentException("Length of hidden title exceeds max length in bytes (0xffff)");
        }
        buf = ensureBufferCapacity(buf, buf.capacity() + 2 + hiddenTitleData.length);
        buf.putShort((short) (hiddenTitleData.length & 0xffff));
        buf.put(hiddenTitleData);
      }
    }

    // Shared-column aliases follow as WORD-prefixed P-strings
    for(int i = 0; i < columns.size(); i++) {
      DominoCollectionColumn col = columns.get(i);
      if(col.isSharedColumn()) {
        Optional<String> sharedColumnName = col.getSharedColumnName();
        if (sharedColumnName.isPresent()) {
          byte[] sharedColumnNameData = sharedColumnName.get().getBytes(lmbcsCharset);
          if (sharedColumnNameData.length > 65535) {
            throw new IllegalArgumentException("Length of shared column name exceeds max length in bytes (0xffff)");
          }
          buf = ensureBufferCapacity(buf, buf.capacity() + 2 + sharedColumnNameData.length);
          buf.putShort((short) (sharedColumnNameData.length & 0xffff));
          buf.put(sharedColumnNameData);
        }
      }
    }
    
    // TODO figure out where these ghost bits come from. I'd thought they were "ghost" shared-column names, but
    //      it turns out they were instead hidden column titles. It seems in practice (test data and mail12.ntf) that
    //      they're all 0, so the below algorithm "works" to move past them
//    while(data.remaining() > 1 && data.getShort(data.position()) != ViewFormatConstants.VIEW_COLUMN_FORMAT_SIGNATURE6) {
//      int ghostSharedColLen = Short.toUnsignedInt(data.getShort());
//      data.position(data.position()+ghostSharedColLen);
//    }
    
    // VIEW_COLUMN_FORMAT6 - Hannover
    for(int i = 0; i < columns.size(); i++) {
      DominoCollectionColumn col = columns.get(i);
      ViewColumnFormat2 fmt = col.getAdapter(ViewColumnFormat2.class);
      if(fmt.getFlags().contains(ViewColumnFormat2.Flag3.ExtendedViewColFmt6)) {
        ViewColumnFormat6 fmt6 = col.getAdapter(ViewColumnFormat6.class);
        if (fmt6==null) {
          fmt6 = ViewColumnFormat6.newInstanceWithDefaults();
        }

        ByteBuffer fmt6Data = fmt6.getData();
        buf = ensureBufferCapacity(buf, buf.capacity() + fmt6Data.capacity());
        buf.put(fmt6Data);
      }
    }

    // It seems like it ends with a terminating 0 byte - possibly for padding to hit a WORD boundary
    // TODO figure out why where are sometimes > 1 bytes remaining (observed up to 6 in mail12.ntf)

    
    return buf;
  }
  
  public static ByteBuffer encodeCalendarFormat(DominoCalendarFormat calFormat) {
    ViewCalendarFormat format1 = Objects.requireNonNull(calFormat.getAdapter(ViewCalendarFormat.class));
    
    ByteBuffer format1Data = format1.getData();
    int format1Capacity = format1Data.capacity();

    ByteBuffer buf = ByteBuffer.allocate(format1Capacity).order(ByteOrder.nativeOrder());
    buf.put(format1Data);
    
    ViewCalendarFormat2 format2 = calFormat.getAdapter(ViewCalendarFormat2.class);
    
    if (format2!=null) {
      ByteBuffer format2Data = format2.getData();
      int format2Capacity = format2Data.capacity();
      
      buf = ensureBufferCapacity(buf, buf.capacity() + format2Capacity);
      buf.put(format2Data);
    }
    
    return buf;
  }
}
