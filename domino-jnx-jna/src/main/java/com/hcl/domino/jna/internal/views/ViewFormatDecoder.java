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
package com.hcl.domino.jna.internal.views;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.hcl.domino.commons.design.view.DominoViewColumnFormat;
import com.hcl.domino.commons.design.view.DominoViewFormat;
import com.hcl.domino.commons.misc.ODSTypes;
import com.hcl.domino.commons.richtext.records.MemoryStructureProxy;
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
import com.hcl.domino.richtext.structures.MemoryStructure;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class ViewFormatDecoder {
	
	public static DominoViewFormat decodeViewFormat(Pointer dataPtr, int valueLength) {
	  // Since it appears that ODSReadMemory is harmful on platforms we support, use a ByteBuffer for safety
	  ByteBuffer data = dataPtr.getByteBuffer(0, valueLength);
		
		// TODO see how this differs when VIEW_TABLE_FORMAT.Type is Calendar
		// VIEW_CALENDAR_FORMAT doesn't include a column count, which implies that VIEW_TABLE_FORMAT
		//   may still be present
		
		/*
		 * All views have:
		 * - VIEW_TABLE_FORMAT (starts with VIEW_FORMAT_HEADER)
		 * - VIEW_COLUMN_FORMAT * colCount
		 * - var data * colCount
		 */
		
		DominoViewFormat result = new DominoViewFormat();
		
		ViewTableFormat format1 = readMemory(data, ODSTypes._VIEW_TABLE_FORMAT, ViewTableFormat.class);
		result.read(format1);
		
		List<DominoViewColumnFormat> columns = new ArrayList<>();
		
		int columnValuesIndex = 0;
		// Always present
		{
			int vcfSize = MemoryStructureProxy.sizeOf(ViewColumnFormat.class);
			ByteBuffer pPackedData = data.duplicate().order(ByteOrder.nativeOrder());
			pPackedData.position(pPackedData.position()+(vcfSize * format1.getColumnCount()));
			
			for(int i = 0; i < format1.getColumnCount(); i++) {
				
				ViewColumnFormat tempCol = readMemory(data, ODSTypes._VIEW_COLUMN_FORMAT, ViewColumnFormat.class);
				
				// Find the actual size with variable data and re-read
				int varSize = tempCol.getItemNameLength() + tempCol.getTitleLength() + tempCol.getFormulaLength() + tempCol.getConstantValueLength();
				ViewColumnFormat fullCol = MemoryStructureProxy.newStructure(ViewColumnFormat.class, varSize);
				ByteBuffer fullColData = fullCol.getData();
				// Write the ODS value first
				fullColData.put(tempCol.getData());
				byte[] varData = new byte[varSize];
				pPackedData.get(varData);
				fullColData.put(varData);
				
				DominoViewColumnFormat viewCol = result.addColumn();
				viewCol.read(fullCol);
				columns.add(viewCol);
				
				// Mark the column-values index now that it's knowable
				if(fullCol.getConstantValueLength() == 0) {
					viewCol.readColumnValuesIndex(columnValuesIndex);
					columnValuesIndex++;
				} else {
					viewCol.readColumnValuesIndex(0xFFFF);
				}
			}
			
			data.position(pPackedData.position());
		}

    int vtf2size = MemoryStructureProxy.sizeOf(ViewTableFormat2.class);
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
			  throw new IllegalStateException("Read unexpected VIEW_COLUMN_FORMAT2 signature: 0x" + Integer.toHexString(col.getSignature()));
			}
			columns.get(i).read(col);
		}

    int vtf3size = MemoryStructureProxy.sizeOf(ViewTableFormat3.class);
    if(data.remaining() < vtf3size) {
      return result;
    }
	  ViewTableFormat3 format3 = readMemory(data, ODSTypes._VIEW_TABLE_FORMAT3, ViewTableFormat3.class);
		result.read(format3);

    // In case VIEW_TABLE_FORMAT3.Length ever disagrees, increment further
    data.position(data.position() + Math.max(0, format3.getLength()-vtf3size));
		
		// Followed by variable data defined by VIEW_COLUMN_FORMAT2
		for(int i = 0; i < format1.getColumnCount(); i++) {
			DominoViewColumnFormat col = columns.get(i);
			ViewColumnFormat2 fmt = col.getAdapter(ViewColumnFormat2.class);
			int hideWhenLen = fmt.getHideWhenFormulaLength();
			if(hideWhenLen > 0) {
			  byte[] formula = new byte[hideWhenLen];
			  data.get(formula);
				col.readHideWhenFormula(formula);
			}
			int twistieLen = fmt.getTwistieResourceLength();
			if(twistieLen > 0) {
			  ByteBuffer buf = readBuffer(data, twistieLen);
				CDResource res = MemoryStructureProxy.newStructure(CDResource.class, twistieLen - MemoryStructureProxy.sizeOf(CDResource.class));
				res.getData().put(buf);
				col.readTwistie(res);
			}
		}
		
		// Followed by VIEW_TABLE_FORMAT4 data
    int vtf4size = MemoryStructureProxy.sizeOf(ViewTableFormat4.class);
    if(data.remaining() < vtf4size) {
      return result;
    }
		int format4len = Short.toUnsignedInt(data.getShort(data.position()));
		ViewTableFormat4 format4 = readMemory(data, (short)-1, ViewTableFormat4.class);
		result.read(format4);
    // In case VIEW_TABLE_FORMAT4.Length ever disagrees, increment further
    data.position(data.position() + Math.max(0, format4len-vtf4size));
		
		// Background resource link
    int cdresLen = MemoryStructureProxy.sizeOf(CDResource.class);
    if(data.remaining() < 2) {
      return result;
    }
		short sig = data.getShort(data.position());
		CDResource backgroundRes = null;
		if(data.hasRemaining() && sig == RichTextConstants.SIG_CD_HREF) {
			// Retrieve the fixed structure to determine the full length of the record
		  ByteBuffer tempBuf = subBuffer(data, MemoryStructureProxy.sizeOf(CDResource.class));
			CDResource res = MemoryStructureProxy.forStructure(CDResource.class, () -> tempBuf);
			int len = res.getHeader().getLength();
			
			ByteBuffer buf = readBuffer(data, len);
			backgroundRes = MemoryStructureProxy.newStructure(CDResource.class, len - cdresLen);
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
			DominoViewColumnFormat col = columns.get(i);
			ViewColumnFormat2 fmt = col.getAdapter(ViewColumnFormat2.class);
			if(fmt.getFlags().contains(ViewColumnFormat2.Flag3.ExtDate)) {
				
				int len = MemoryStructureProxy.sizeOf(ViewColumnFormat3.class);
				ByteBuffer tempBuf = subBuffer(data, len);
				ViewColumnFormat3 tempCol = MemoryStructureProxy.forStructure(ViewColumnFormat3.class, () -> tempBuf);
				if(tempCol.getSignature() != ViewFormatConstants.VIEW_COLUMN_FORMAT_SIGNATURE3) {
					throw new IllegalStateException("Encountered unexpected signature when looking for VIEW_COLUMN_FORMAT3: 0x" + Integer.toHexString(tempCol.getSignature()));
				}
				
				// Re-read with variable data
				int varLen = tempCol.getDateSeparator1Length() + tempCol.getDateSeparator2Length() + tempCol.getDateSeparator3Length()
					+ tempCol.getTimeSeparatorLength();
				ByteBuffer buf = readBuffer(data, len+varLen);
				ViewColumnFormat3 col3 = MemoryStructureProxy.newStructure(ViewColumnFormat3.class, varLen);
				col3.getData().put(buf);
				col.read(col3);
			}
		}
		
		if(data.remaining() < 2) {
      return result;
    }
		
		// VIEW_COLUMN_FORMAT4 - number format
		for(int i = 0; i < format1.getColumnCount(); i++) {
			DominoViewColumnFormat col = columns.get(i);
			ViewColumnFormat2 fmt = col.getAdapter(ViewColumnFormat2.class);
			if(fmt.getFlags().contains(ViewColumnFormat2.Flag3.NumberFormat)) {
				
				int len = MemoryStructureProxy.sizeOf(ViewColumnFormat4.class);
				ByteBuffer tempBuf = subBuffer(data, len);
				ViewColumnFormat4 tempCol = MemoryStructureProxy.forStructure(ViewColumnFormat4.class, () -> tempBuf);
				if(tempCol.getSignature() != ViewFormatConstants.VIEW_COLUMN_FORMAT_SIGNATURE4) {
					throw new IllegalStateException("Encountered unexpected signature when looking for VIEW_COLUMN_FORMAT4: 0x" + Integer.toHexString(tempCol.getSignature()));
				}
				
				// Re-read with variable data
				long varLen = tempCol.getCurrencySymbolLength() + tempCol.getDecimalSymbolLength() + tempCol.getMilliSeparatorLength()
					+ tempCol.getNegativeSymbolLength();
        ByteBuffer buf = readBuffer(data, len+varLen);
				ViewColumnFormat4 col4 = MemoryStructureProxy.newStructure(ViewColumnFormat4.class, (int)varLen);
				col4.getData().put(buf);
				col.read(col4);
			}
		}
		
		if(data.remaining() < 2) {
      return result;
    }
		
		// VIEW_COLUMN_FORMAT5 - names format
		for(int i = 0; i < format1.getColumnCount(); i++) {
			DominoViewColumnFormat col = columns.get(i);
			ViewColumnFormat2 fmt = col.getAdapter(ViewColumnFormat2.class);
			if(fmt.getFlags().contains(ViewColumnFormat2.Flag3.NamesFormat)) {
				int len = MemoryStructureProxy.sizeOf(ViewColumnFormat5.class);
        ByteBuffer tempBuf = subBuffer(data, len);
				ViewColumnFormat5 tempCol = MemoryStructureProxy.forStructure(ViewColumnFormat5.class, () -> tempBuf);
				if(tempCol.getSignature() != ViewFormatConstants.VIEW_COLUMN_FORMAT_SIGNATURE5) {
					throw new IllegalStateException("Encountered unexpected signature when looking for VIEW_COLUMN_FORMAT5: 0x" + Integer.toHexString(tempCol.getSignature()));
				}
				
				// Re-read with variable data
				int totalLen = tempCol.getLength();
        ByteBuffer buf = readBuffer(data, totalLen);
				ViewColumnFormat5 col5 = MemoryStructureProxy.newStructure(ViewColumnFormat5.class, totalLen - MemoryStructureProxy.sizeOf(ViewColumnFormat5.class));
				col5.getData().put(buf);
				col.read(col5);
			}
		}
		
		// Shared-column aliases follow as WORD-prefixed P-strings
		for(int i = 0; i < format1.getColumnCount(); i++) {
			DominoViewColumnFormat col = columns.get(i);
			if(col.isSharedColumn()) {
		    int aliasLen = Short.toUnsignedInt(data.getShort());
				byte[] lmbcs = new byte[aliasLen];
				data.get(lmbcs);
				String alias = new String(lmbcs, Charset.forName("LMBCS")); //$NON-NLS-1$
				col.readSharedColumnName(alias);
			}
		}
		
		if(data.remaining() < 2) {
      return result;
    }
		
		// It appears that columns may have "ghost" names here from when they _used_ to be
		//   shared columns
		// TODO figure out if this will need to be somehow interpolated with the above when
		//   a true shared column followed a former one
		while(data.remaining() > 1 && data.getShort(data.position()) != ViewFormatConstants.VIEW_COLUMN_FORMAT_SIGNATURE6) {
			int ghostSharedColLen = Short.toUnsignedInt(data.getShort());
			data.position(data.position()+ghostSharedColLen);
		}
		
		if(data.remaining() < 2) {
      return result;
    }
		
		// VIEW_COLUMN_FORMAT6 - Hannover
		for(int i = 0; i < format1.getColumnCount(); i++) {
			DominoViewColumnFormat col = columns.get(i);
			ViewColumnFormat2 fmt = col.getAdapter(ViewColumnFormat2.class);
			if(fmt.getFlags().contains(ViewColumnFormat2.Flag3.ExtendedViewColFmt6)) {
				int len = MemoryStructureProxy.sizeOf(ViewColumnFormat6.class);
				
        ByteBuffer tempBuf = subBuffer(data, len);
				ViewColumnFormat6 tempCol = MemoryStructureProxy.forStructure(ViewColumnFormat6.class, () -> tempBuf);
				if(tempCol.getSignature() != ViewFormatConstants.VIEW_COLUMN_FORMAT_SIGNATURE6) {
					throw new IllegalStateException("Encountered unexpected signature when looking for VIEW_COLUMN_FORMAT6: 0x" + Integer.toHexString(tempCol.getSignature()));
				}
				
				// Re-read with variable data
				int totalLen = tempCol.getLength();
        ByteBuffer buf = readBuffer(data, totalLen);
				ViewColumnFormat6 col6 = MemoryStructureProxy.newStructure(ViewColumnFormat6.class, totalLen - MemoryStructureProxy.sizeOf(ViewColumnFormat6.class));
				col6.getData().put(buf);
				col.read(col6);
			}
		}
		
		// It seems like it ends with a terminating 0 byte - possibly for padding to hit a WORD boundary
		
		return result;
	}
	
	@SuppressWarnings("unused")
  private static <T extends MemoryStructure> T readMemory(PointerByReference ppData, short odsType, Class<T> struct) {
		// TODO determine if any architectures need ODSReadMemory. On x64 macOS, it seems harmful.
		//    Docs just say "Intel", but long predate x64. On Windows, it says it should be harmless, but
		//    care has to be taken on "UNIX", which is everything else.
		//    Additionally, not all structures here have ODS numbers
		
		// ODSReadMemory cariant
//		Memory mem = new Memory(MemoryStructureProxy.sizeOf(struct));
//		NotesCAPI.get().ODSReadMemory(ppData, odsType, mem, (short)1);
//		return MemoryStructureProxy.forStructure(struct, () -> mem.getByteBuffer(0, mem.size()));
		
		// Straight-read variant
		T result = MemoryStructureProxy.newStructure(struct, 0);
		int len = MemoryStructureProxy.sizeOf(struct);
		result.getData().put(ppData.getValue().getByteBuffer(0, len));
		ppData.setValue(ppData.getValue().share(len));
		
		return result;
	}
	
	/**
	 * Reads a structure from the provided ByteBuffer, incrementing its position the size of the struct.
	 * 
	 * @param <T> the class of structure to read
	 * @param data the containing data buffer
	 * @param odsType the ODS type, or {@code -1} if not known
	 * @param struct a {@link Class} represening {@code <T>}
	 * @return the read structure
	 */
	private static <T extends MemoryStructure> T readMemory(ByteBuffer data, short odsType, Class<T> struct) {
	  T result = MemoryStructureProxy.newStructure(struct, 0);
    int len = MemoryStructureProxy.sizeOf(struct);
    byte[] bytes = new byte[len];
    data.get(bytes);
    result.getData().put(bytes);
    return result;
	}
	
	private static ByteBuffer readBuffer(ByteBuffer buf, long len) {
	  ByteBuffer result = subBuffer(buf, (int)len);
	  buf.position(buf.position()+(int)len);
	  return result;
	}
	private static ByteBuffer subBuffer(ByteBuffer buf, int len) {
	  ByteBuffer tempBuf = buf.slice().order(ByteOrder.nativeOrder());
    tempBuf.limit(len);
    return tempBuf;
	}
}
