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

import static com.hcl.domino.commons.util.NotesItemDataUtil.readBuffer;
import static com.hcl.domino.commons.util.NotesItemDataUtil.readMemory;
import static com.hcl.domino.commons.util.NotesItemDataUtil.subBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.hcl.domino.commons.misc.ODSTypes;
import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.commons.util.DumpUtil;
import com.hcl.domino.data.NativeItemCoder;
import com.hcl.domino.design.format.ViewCalendarFormat;
import com.hcl.domino.design.format.ViewCalendarFormat2;
import com.hcl.domino.design.format.ViewColumnFormat;
import com.hcl.domino.design.format.ViewColumnFormat2;
import com.hcl.domino.design.format.ViewColumnFormat3;
import com.hcl.domino.design.format.ViewColumnFormat4;
import com.hcl.domino.design.format.ViewColumnFormat5;
import com.hcl.domino.design.format.ViewColumnFormat6;
import com.hcl.domino.design.format.ViewTableFormat;
import com.hcl.domino.design.format.ViewTableFormat2;
import com.hcl.domino.design.format.ViewTableFormat3;
import com.hcl.domino.design.format.ViewTableFormat4;
import com.hcl.domino.misc.ViewFormatConstants;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.records.CDResource;

public class ViewFormatDecoder {
	
	public static DominoViewFormat decodeViewFormat(ByteBuffer data) {
	  {
	    int pos = data.position();
	    int remaining = data.remaining();
	    byte[] remainingData = new byte[remaining];
	    data.get(remainingData);
	    System.out.println(DumpUtil.dumpAsAscii(remainingData));
	    data.position(pos);

	  }
		/*
		 * All views have:
		 * - VIEW_TABLE_FORMAT (starts with VIEW_FORMAT_HEADER)
		 * - VIEW_COLUMN_FORMAT * colCount
		 * - var data * colCount
		 */
		
		DominoViewFormat result = new DominoViewFormat();
		
		ViewTableFormat format1 = readMemory(data, ODSTypes._VIEW_TABLE_FORMAT, ViewTableFormat.class);
		result.read(format1);
		
		List<DominoCollectionColumn> columns = new ArrayList<>();
		
		// Always present
		{
			int vcfSize = MemoryStructureUtil.sizeOf(ViewColumnFormat.class);
			ByteBuffer pPackedData = data.duplicate().order(ByteOrder.nativeOrder());
			pPackedData.position(pPackedData.position()+(vcfSize * format1.getColumnCount()));

			int colCount = format1.getColumnCount();
			//reset column count; will be increased when the column format is added
			format1.setColumnCount(0);
			
			for(int i = 0; i < colCount; i++) {
				ViewColumnFormat tempCol = readMemory(data, ODSTypes._VIEW_COLUMN_FORMAT, ViewColumnFormat.class);
				
				// Find the actual size with variable data and re-read
				int varSize = tempCol.getItemNameLength() + tempCol.getTitleLength() + tempCol.getFormulaLength() + tempCol.getConstantValueLength();
				ViewColumnFormat fullCol = MemoryStructureUtil.newStructure(ViewColumnFormat.class, varSize);
				ByteBuffer fullColData = fullCol.getData();
				// Write the ODS value first
				fullColData.put(tempCol.getData());
				byte[] varData = new byte[varSize];
				pPackedData.get(varData);
        System.out.println("VarData ViewColumnFormat #"+i+"\n"+DumpUtil.dumpAsAscii(varData));
				fullColData.put(varData);

				DominoCollectionColumn viewCol = (DominoCollectionColumn) result.addColumn(-1);
				viewCol.read(fullCol);
				columns.add(viewCol);
			}
			
			data.position(pPackedData.position());
		}

    int vtf2size = MemoryStructureUtil.sizeOf(ViewTableFormat2.class);
    if(data.remaining() < vtf2size) {
      return result;
    }
		/*
		 * Read FORMAT2 structures
		 * - VIEW_TABLE_FORMAT2
		 * - VIEW_COLUMN_FORMAT2 * colCount 
		 */
		ViewTableFormat2 format2 = readMemory(data, ODSTypes._VIEW_TABLE_FORMAT2, ViewTableFormat2.class);
		result.read(format2);
		
		// In case VIEW_TABLE_FORMAT2.Length ever disagrees, increment further
		data.position(data.position() + Math.max(0, format2.getLength()-vtf2size));
		
		for(int i = 0; i < format1.getColumnCount(); i++) {
			ViewColumnFormat2 col = readMemory(data, ODSTypes._VIEW_COLUMN_FORMAT2, ViewColumnFormat2.class);
			if(col.getSignature() != ViewFormatConstants.VIEW_COLUMN_FORMAT_SIGNATURE2) {
			  throw new IllegalStateException(MessageFormat.format("Read unexpected VIEW_COLUMN_FORMAT2 signature: 0x{0}", Integer.toHexString(col.getSignature())));
			}
			columns.get(i).read(col);
		}

    int vtf3size = MemoryStructureUtil.sizeOf(ViewTableFormat3.class);
    if(data.remaining() < vtf3size) {
      return result;
    }
	  ViewTableFormat3 format3 = readMemory(data, ODSTypes._VIEW_TABLE_FORMAT3, ViewTableFormat3.class);
		result.read(format3);

    // In case VIEW_TABLE_FORMAT3.Length ever disagrees, increment further
    data.position(data.position() + Math.max(0, format3.getLength()-vtf3size));
		
		// Followed by variable data defined by VIEW_COLUMN_FORMAT2
		for(int i = 0; i < format1.getColumnCount(); i++) {
			DominoCollectionColumn col = columns.get(i);
			ViewColumnFormat2 fmt = col.getAdapter(ViewColumnFormat2.class);
			int hideWhenLen = fmt.getHideWhenFormulaLength();
      int twistieLen = fmt.getTwistieResourceLength();
      
      if(hideWhenLen > 0 || twistieLen > 0) {
        //add hide when formula and twistie resource to the vardata of VIEW_COLUMN_FORMAT2 although it's stored separately
        fmt.resizeVariableData(hideWhenLen + twistieLen);
        ByteBuffer fmtVarData = fmt.getVariableData();
        
        if (hideWhenLen > 0) {
          byte[] formula = new byte[hideWhenLen];
          data.get(formula);
          fmtVarData.put(formula);
        }
        
        if (twistieLen > 0) {
          ByteBuffer buf = readBuffer(data, twistieLen);
          CDResource res = MemoryStructureUtil.newStructure(CDResource.class, twistieLen - MemoryStructureUtil.sizeOf(CDResource.class));
          res.getData().put(buf);
          fmtVarData.put(buf);
        }
      }
		}
		
		// Followed by VIEW_TABLE_FORMAT4 data
    int vtf4size = MemoryStructureUtil.sizeOf(ViewTableFormat4.class);
    if(data.remaining() < vtf4size) {
      return result;
    }
		int format4len = Short.toUnsignedInt(data.getShort(data.position()));
		ViewTableFormat4 format4 = readMemory(data, (short)-1, ViewTableFormat4.class);
		result.read(format4);
    // In case VIEW_TABLE_FORMAT4.Length ever disagrees, increment further
    data.position(data.position() + Math.max(0, format4len-vtf4size));
		
		// Background resource link
    int cdresLen = MemoryStructureUtil.sizeOf(CDResource.class);
    if(data.remaining() < 2) {
      return result;
    }
		short sig = data.getShort(data.position());
		CDResource backgroundRes = null;
		if(data.hasRemaining() && sig == RichTextConstants.SIG_CD_HREF) {
			// Retrieve the fixed structure to determine the full length of the record
		  ByteBuffer tempBuf = subBuffer(data, MemoryStructureUtil.sizeOf(CDResource.class));
			CDResource res = MemoryStructureUtil.forStructure(CDResource.class, () -> tempBuf);
			int len = res.getHeader().getLength();
			
			ByteBuffer buf = readBuffer(data, len);
			backgroundRes = MemoryStructureUtil.newStructure(CDResource.class, len - cdresLen);
			backgroundRes.getData().put(buf);
			result.readBackgroundResource(backgroundRes);
		}
		
		// Remaining parts are optional based on previous column flags
		// Certainly, nothing afterward is smaller than a WORD
		if(data.remaining() < 2) {
		  return result;
		}
		
		// VIEW_COLUMN_FORMAT3 - date/time format
		for(int i = 0; i < format1.getColumnCount(); i++) {
			DominoCollectionColumn col = columns.get(i);
			ViewColumnFormat2 fmt = col.getAdapter(ViewColumnFormat2.class);
			if(fmt.getFlags().contains(ViewColumnFormat2.Flag3.ExtDate)) {
				
				int len = MemoryStructureUtil.sizeOf(ViewColumnFormat3.class);
				ByteBuffer tempBuf = subBuffer(data, len);
				ViewColumnFormat3 tempCol = MemoryStructureUtil.forStructure(ViewColumnFormat3.class, () -> tempBuf);
				if(tempCol.getSignature() != ViewFormatConstants.VIEW_COLUMN_FORMAT_SIGNATURE3) {
					throw new IllegalStateException("Encountered unexpected signature when looking for VIEW_COLUMN_FORMAT3: 0x" + Integer.toHexString(tempCol.getSignature()));
				}
				
				// Re-read with variable data
				int varLen = tempCol.getDateSeparator1Length() + tempCol.getDateSeparator2Length() + tempCol.getDateSeparator3Length()
					+ tempCol.getTimeSeparatorLength();
				ByteBuffer buf = readBuffer(data, len+varLen);
				ViewColumnFormat3 col3 = MemoryStructureUtil.newStructure(ViewColumnFormat3.class, varLen);
				col3.getData().put(buf);
				col.read(col3);
			}
		}
		
		if(data.remaining() < 2) {
      return result;
    }
		
		// VIEW_COLUMN_FORMAT4 - number format
		for(int i = 0; i < format1.getColumnCount(); i++) {
			DominoCollectionColumn col = columns.get(i);
			ViewColumnFormat2 fmt = col.getAdapter(ViewColumnFormat2.class);
			if(fmt.getFlags().contains(ViewColumnFormat2.Flag3.NumberFormat)) {
				
				int len = MemoryStructureUtil.sizeOf(ViewColumnFormat4.class);
				ByteBuffer tempBuf = subBuffer(data, len);
				ViewColumnFormat4 tempCol = MemoryStructureUtil.forStructure(ViewColumnFormat4.class, () -> tempBuf);
				if(tempCol.getSignature() != ViewFormatConstants.VIEW_COLUMN_FORMAT_SIGNATURE4) {
					throw new IllegalStateException("Encountered unexpected signature when looking for VIEW_COLUMN_FORMAT4: 0x" + Integer.toHexString(tempCol.getSignature()));
				}
				
				// Re-read with variable data
				long varLen = tempCol.getCurrencySymbolLength() + tempCol.getDecimalSymbolLength() + tempCol.getMilliSeparatorLength()
					+ tempCol.getNegativeSymbolLength();
        ByteBuffer buf = readBuffer(data, len+varLen);
				ViewColumnFormat4 col4 = MemoryStructureUtil.newStructure(ViewColumnFormat4.class, (int)varLen);
				col4.getData().put(buf);
				col.read(col4);
			}
		}
		
		if(data.remaining() < 2) {
      return result;
    }
		
		// VIEW_COLUMN_FORMAT5 - names format
		for(int i = 0; i < format1.getColumnCount(); i++) {
			DominoCollectionColumn col = columns.get(i);
			ViewColumnFormat2 fmt = col.getAdapter(ViewColumnFormat2.class);
			if(fmt.getFlags().contains(ViewColumnFormat2.Flag3.NamesFormat)) {
				int len = MemoryStructureUtil.sizeOf(ViewColumnFormat5.class);
        ByteBuffer tempBuf = subBuffer(data, len);
				ViewColumnFormat5 tempCol = MemoryStructureUtil.forStructure(ViewColumnFormat5.class, () -> tempBuf);
				if(tempCol.getSignature() != ViewFormatConstants.VIEW_COLUMN_FORMAT_SIGNATURE5) {
					throw new IllegalStateException("Encountered unexpected signature when looking for VIEW_COLUMN_FORMAT5: 0x" + Integer.toHexString(tempCol.getSignature()));
				}
				
				// Re-read with variable data
				int totalLen = tempCol.getLength();
        ByteBuffer buf = readBuffer(data, totalLen);
				ViewColumnFormat5 col5 = MemoryStructureUtil.newStructure(ViewColumnFormat5.class, totalLen - MemoryStructureUtil.sizeOf(ViewColumnFormat5.class));
				col5.getData().put(buf);
				col.read(col5);
			}
		}
		
		
		// Hidden titles follow as WORD-prefixed P-strings
		for(int i = 0; i < format1.getColumnCount(); i++) {
      DominoCollectionColumn col = columns.get(i);
      if(col.isHideTitle()) {
        int titleLen = Short.toUnsignedInt(data.getShort());
        byte[] lmbcs = new byte[titleLen];
        data.get(lmbcs);
        String title = new String(lmbcs, NativeItemCoder.get().getLmbcsCharset());
        col.readHiddenTitle(title);
      }
    }
    
    if(data.remaining() < 2) {
      return result;
    }
		
		// Shared-column aliases follow as WORD-prefixed P-strings
		for(int i = 0; i < format1.getColumnCount(); i++) {
			DominoCollectionColumn col = columns.get(i);
      if(col.isSharedColumn()) {
        int aliasLen = Short.toUnsignedInt(data.getShort());
        byte[] lmbcs = new byte[aliasLen];
        data.get(lmbcs);
        String alias = new String(lmbcs, NativeItemCoder.get().getLmbcsCharset());
        col.readSharedColumnName(alias);
      }
		}
		
		if(data.remaining() < 2) {
      return result;
    }

		{
		  int pos = data.position();
		  int remaining = data.remaining();
		  byte[] remainingData = new byte[remaining];
		  data.get(remainingData);
		  System.out.println(DumpUtil.dumpAsAscii(remainingData));
		  data.position(pos);
		  
	    short v1 = data.getShort(data.position());
	    short v2 = ViewFormatConstants.VIEW_COLUMN_FORMAT_SIGNATURE6;
		}
		
		// TODO figure out where these ghost bits come from. I'd thought they were "ghost" shared-column names, but
		//      it turns out they were instead hidden column titles. It seems in practice (test data and mail12.ntf) that
		//      they're all 0, so the below algorithm "works" to move past them
		while(data.remaining() > 1 && data.getShort(data.position()) != ViewFormatConstants.VIEW_COLUMN_FORMAT_SIGNATURE6) {
			int ghostSharedColLen = Short.toUnsignedInt(data.getShort());
			
			if(data.remaining() < ghostSharedColLen) {
	      return result;
	    }
			data.position(data.position()+ghostSharedColLen);
		}
		
		if(data.remaining() < 2) {
      return result;
    }
		
		// VIEW_COLUMN_FORMAT6 - Hannover
		for(int i = 0; i < format1.getColumnCount(); i++) {
			DominoCollectionColumn col = columns.get(i);
			ViewColumnFormat2 fmt = col.getAdapter(ViewColumnFormat2.class);
			if(fmt.getFlags().contains(ViewColumnFormat2.Flag3.ExtendedViewColFmt6)) {
				int len = MemoryStructureUtil.sizeOf(ViewColumnFormat6.class);
				
        ByteBuffer tempBuf = subBuffer(data, len);
				ViewColumnFormat6 tempCol = MemoryStructureUtil.forStructure(ViewColumnFormat6.class, () -> tempBuf);
				if(tempCol.getSignature() != ViewFormatConstants.VIEW_COLUMN_FORMAT_SIGNATURE6) {
					throw new IllegalStateException("Encountered unexpected signature when looking for VIEW_COLUMN_FORMAT6: 0x" + Integer.toHexString(tempCol.getSignature()));
				}
				
				// Re-read with variable data
				int totalLen = tempCol.getLength();
        ByteBuffer buf = readBuffer(data, totalLen);
				ViewColumnFormat6 col6 = MemoryStructureUtil.newStructure(ViewColumnFormat6.class, totalLen - MemoryStructureUtil.sizeOf(ViewColumnFormat6.class));
				col6.getData().put(buf);
				col.read(col6);
			}
		}
		
		// It seems like it ends with a terminating 0 byte - possibly for padding to hit a WORD boundary
		// TODO figure out why where are sometimes > 1 bytes remaining (observed up to 6 in mail12.ntf)
		
		return result;
	}
	
	public static DominoCalendarFormat decodeCalendarFormat(ByteBuffer data) {
    // All entries should have a VIEW_CALENDAR_FORMAT
    DominoCalendarFormat result = new DominoCalendarFormat();
    
    ViewCalendarFormat format1 = readMemory(data, ODSTypes._VIEW_CALENDAR_FORMAT, ViewCalendarFormat.class);
    result.read(format1);
    
    int format2size = MemoryStructureUtil.sizeOf(ViewCalendarFormat2.class);
    if(data.remaining() >= format2size) {
      ViewCalendarFormat2 format2 = readMemory(data, (short)-1, ViewCalendarFormat2.class);
      result.read(format2);
    }
    
    return result;
	}
}
