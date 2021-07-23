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
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.misc.ViewFormatConstants;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.structures.MemoryStructure;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class ViewFormatDecoder {
	
	public static DominoViewFormat decodeViewFormat(Pointer dataPtr, int valueLength) {
		PointerByReference ppData = new PointerByReference(dataPtr);
		
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
		
		ViewTableFormat format1 = readMemory(ppData, ODSTypes._VIEW_TABLE_FORMAT, ViewTableFormat.class);
		result.read(format1);
		
		List<DominoViewColumnFormat> columns = new ArrayList<>();
		
		int read = NotesCAPI.get().ODSLength(ODSTypes._VIEW_TABLE_FORMAT);
		
		int columnValuesIndex = 0;
		// Always present
		{
			int vcfSize = MemoryStructureProxy.sizeOf(ViewColumnFormat.class);
			Pointer pPackedData = ppData.getValue().share(vcfSize * format1.getColumnCount());
			
			for(int i = 0; i < format1.getColumnCount(); i++) {
				
				ViewColumnFormat tempCol = readMemory(ppData, ODSTypes._VIEW_COLUMN_FORMAT, ViewColumnFormat.class);
				
				// Find the actual size with variable data and re-read
				int varSize = tempCol.getItemNameLength() + tempCol.getTitleLength() + tempCol.getFormulaLength() + tempCol.getConstantValueLength();
				ViewColumnFormat fullCol = MemoryStructureProxy.newStructure(ViewColumnFormat.class, varSize);
				ByteBuffer fullColData = fullCol.getData();
				// Write the ODS value first
				fullColData.put(tempCol.getData());
				byte[] varData = pPackedData.getByteArray(0, varSize);
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
				
				// Shift our var data pointer to the start of the next column's data
				pPackedData = pPackedData.share(varSize);
				
				// Increment our expected read length
				read += vcfSize + varSize;
			}
			
			ppData.setValue(pPackedData);
		}
		
		ViewTableFormat2 format2 = null;
		if(read < valueLength) {
			/*
			 * Read FORMAT2 structures
			 * - VIEW_TABLE_FORMAT2
			 * - VIEW_COLUMN_FORMAT2 * colCount 
			 */
			int vtf2size = MemoryStructureProxy.sizeOf(ViewTableFormat2.class);
			format2 = readMemory(ppData, ODSTypes._VIEW_TABLE_FORMAT2, ViewTableFormat2.class);
			result.read(format2);
			read += vtf2size;
			
			int vcf2size = MemoryStructureProxy.sizeOf(ViewColumnFormat2.class);
			for(int i = 0; i < format1.getColumnCount(); i++) {
				ViewColumnFormat2 col = readMemory(ppData, ODSTypes._VIEW_COLUMN_FORMAT2, ViewColumnFormat2.class);
				columns.get(i).read(col);
				
				read += vcf2size;
			}	
		}
		
		ViewTableFormat3 format3 = null;
		if(read < valueLength) {
			format3 = readMemory(ppData, ODSTypes._VIEW_TABLE_FORMAT3, ViewTableFormat3.class);
			result.read(format3);
			read += format3.getLength();
		}
		
		// Followed by variable data defined by VIEW_COLUMN_FORMAT2
		for(int i = 0; i < format1.getColumnCount(); i++) {
			DominoViewColumnFormat col = columns.get(i);
			ViewColumnFormat2 fmt = col.getAdapter(ViewColumnFormat2.class);
			int hideWhenLen = fmt.getHideWhenFormulaLength();
			if(hideWhenLen > 0) {
				byte[] formula = ppData.getValue().getByteArray(0, hideWhenLen);
				col.readHideWhenFormula(formula);
				ppData.setValue(ppData.getValue().share(hideWhenLen));
				read += hideWhenLen;
			}
			int twistieLen = fmt.getTwistieResourceLength();
			if(twistieLen > 0) {
				ByteBuffer buf = ppData.getValue().getByteBuffer(0, twistieLen);
				CDResource res = MemoryStructureProxy.newStructure(CDResource.class, twistieLen - MemoryStructureProxy.sizeOf(CDResource.class));
				res.getData().put(buf);
				col.readTwistie(res);
				ppData.setValue(ppData.getValue().share(twistieLen));
				read += twistieLen;
			}
		}
		
		// Followed by VIEW_TABLE_FORMAT4 data
		ViewTableFormat4 format4 = null;
		if(read < valueLength) {
			int len = Short.toUnsignedInt(ppData.getValue().getShort(0));
			format4 = readMemory(ppData, (short)-1, ViewTableFormat4.class);
			result.read(format4);
			
			read += len;
		}
		
		// Background resource link
		short sig = ppData.getValue().getShort(0);
		CDResource backgroundRes = null;
		if(read < valueLength && sig == RichTextConstants.SIG_CD_HREF) {
			// Retrieve the fixed structure to determine the full length of the record
			ByteBuffer tempBuf = ppData.getValue().getByteBuffer(0, MemoryStructureProxy.sizeOf(CDResource.class));
			CDResource res = MemoryStructureProxy.forStructure(CDResource.class, () -> tempBuf);
			int len = res.getHeader().getLength();
			
			ByteBuffer buf = ppData.getValue().getByteBuffer(0, len);
			backgroundRes = MemoryStructureProxy.newStructure(CDResource.class, len - MemoryStructureProxy.sizeOf(CDResource.class));
			backgroundRes.getData().put(buf);
			result.readBackgroundResource(backgroundRes);
			
			ppData.setValue(ppData.getValue().share(len));
			read += len;
		}
		
		// VIEW_COLUMN_FORMAT3 - date/time format
		if(read < valueLength) {
			for(int i = 0; i < format1.getColumnCount(); i++) {
				DominoViewColumnFormat col = columns.get(i);
				ViewColumnFormat2 fmt = col.getAdapter(ViewColumnFormat2.class);
				if(fmt.getFlags().contains(ViewColumnFormat2.Flag3.ExtDate)) {
					
					int len = MemoryStructureProxy.sizeOf(ViewColumnFormat3.class);
					ByteBuffer tempBuf = ppData.getValue().getByteBuffer(0, len);
					ViewColumnFormat3 tempCol = MemoryStructureProxy.forStructure(ViewColumnFormat3.class, () -> tempBuf);
					if(tempCol.getSignature() != ViewFormatConstants.VIEW_COLUMN_FORMAT_SIGNATURE3) {
						throw new IllegalStateException("Encountered unexpected signature when looking for VIEW_COLUMN_FORMAT3: " + tempCol.getSignature());
					}
					
					// Re-read with variable data
					int varLen = tempCol.getDateSeparator1Length() + tempCol.getDateSeparator2Length() + tempCol.getDateSeparator3Length()
						+ tempCol.getTimeSeparatorLength();
					ByteBuffer buf = ppData.getValue().getByteBuffer(0, len+varLen);
					ViewColumnFormat3 col3 = MemoryStructureProxy.newStructure(ViewColumnFormat3.class, varLen);
					col3.getData().put(buf);
					col.read(col3);
					
					ppData.setValue(ppData.getValue().share(len+varLen));
					read += len + varLen;
				}
			}
		}
		
		// VIEW_COLUMN_FORMAT4 - number format
		if(read < valueLength) {
			for(int i = 0; i < format1.getColumnCount(); i++) {
				DominoViewColumnFormat col = columns.get(i);
				ViewColumnFormat2 fmt = col.getAdapter(ViewColumnFormat2.class);
				if(fmt.getFlags().contains(ViewColumnFormat2.Flag3.NumberFormat)) {
					
					int len = MemoryStructureProxy.sizeOf(ViewColumnFormat4.class);
					ByteBuffer tempBuf = ppData.getValue().getByteBuffer(0, len);
					ViewColumnFormat4 tempCol = MemoryStructureProxy.forStructure(ViewColumnFormat4.class, () -> tempBuf);
					if(tempCol.getSignature() != ViewFormatConstants.VIEW_COLUMN_FORMAT_SIGNATURE4) {
						throw new IllegalStateException("Encountered unexpected signature when looking for VIEW_COLUMN_FORMAT4: " + tempCol.getSignature());
					}
					
					// Re-read with variable data
					long varLen = tempCol.getCurrencySymbolLength() + tempCol.getDecimalSymbolLength() + tempCol.getMilliSeparatorLength()
						+ tempCol.getNegativeSymbolLength();
					ByteBuffer buf = ppData.getValue().getByteBuffer(0, len+varLen);
					ViewColumnFormat4 col4 = MemoryStructureProxy.newStructure(ViewColumnFormat4.class, (int)varLen);
					col4.getData().put(buf);
					col.read(col4);
					
					ppData.setValue(ppData.getValue().share(len+varLen));
					read += len + varLen;
				}
			}
		}
		
		// VIEW_COLUMN_FORMAT5 - names format
		if(read < valueLength) {
			for(int i = 0; i < format1.getColumnCount(); i++) {
				DominoViewColumnFormat col = columns.get(i);
				ViewColumnFormat2 fmt = col.getAdapter(ViewColumnFormat2.class);
				if(fmt.getFlags().contains(ViewColumnFormat2.Flag3.NamesFormat)) {
					int len = MemoryStructureProxy.sizeOf(ViewColumnFormat5.class);
					ByteBuffer tempBuf = ppData.getValue().getByteBuffer(0, len);
					ViewColumnFormat5 tempCol = MemoryStructureProxy.forStructure(ViewColumnFormat5.class, () -> tempBuf);
					if(tempCol.getSignature() != ViewFormatConstants.VIEW_COLUMN_FORMAT_SIGNATURE5) {
						throw new IllegalStateException("Encountered unexpected signature when looking for VIEW_COLUMN_FORMAT5: " + tempCol.getSignature());
					}
					
					// Re-read with variable data
					int totalLen = tempCol.getLength();
					ByteBuffer buf = ppData.getValue().getByteBuffer(0, totalLen);
					ViewColumnFormat5 col5 = MemoryStructureProxy.newStructure(ViewColumnFormat5.class, totalLen - MemoryStructureProxy.sizeOf(ViewColumnFormat5.class));
					col5.getData().put(buf);
					col.read(col5);
					
					ppData.setValue(ppData.getValue().share(totalLen));
					read += totalLen;
				}
			}
		}
		
		// Shared-column aliases follow as WORD-prefixed P-strings
		for(int i = 0; i < format1.getColumnCount(); i++) {
			DominoViewColumnFormat col = columns.get(i);
			ViewColumnFormat2 fmt = col.getAdapter(ViewColumnFormat2.class);
			if(fmt.getFlags().contains(ViewColumnFormat2.Flag3.IsSharedColumn)) {
				int aliasLen = Short.toUnsignedInt(ppData.getValue().getShort(0));
				ppData.setValue(ppData.getValue().share(2));
				byte[] lmbcs = ppData.getValue().getByteArray(0, aliasLen);
				String alias = new String(lmbcs, Charset.forName("LMBCS")); //$NON-NLS-1$
				col.readSharedColumnName(alias);
				
				ppData.setValue(ppData.getValue().share(aliasLen));
				read += aliasLen + 2;
			}
		}
		
		// It appears that columns may have "ghost" names here from when they _used_ to be
		//   shared columns
		// TODO figure out if this will need to be somehow interpolated with the above when
		//   a true shared column followed a former one
		while(read < valueLength && ppData.getValue().getShort(0) != ViewFormatConstants.VIEW_COLUMN_FORMAT_SIGNATURE6) {
			int ghostSharedColLen = Short.toUnsignedInt(ppData.getValue().getShort(0));
			ppData.setValue(ppData.getValue().share(2+ghostSharedColLen));
			read += 2 + ghostSharedColLen;
		}
		
		// VIEW_COLUMN_FORMAT6 - Hannover
		if(read < valueLength) {
			for(int i = 0; i < format1.getColumnCount(); i++) {
				DominoViewColumnFormat col = columns.get(i);
				ViewColumnFormat2 fmt = col.getAdapter(ViewColumnFormat2.class);
				if(fmt.getFlags().contains(ViewColumnFormat2.Flag3.ExtendedViewColFmt6)) {
					int len = MemoryStructureProxy.sizeOf(ViewColumnFormat6.class);
					
					ByteBuffer tempBuf = ppData.getValue().getByteBuffer(0, len);
					ViewColumnFormat6 tempCol = MemoryStructureProxy.forStructure(ViewColumnFormat6.class, () -> tempBuf);
					if(tempCol.getSignature() != ViewFormatConstants.VIEW_COLUMN_FORMAT_SIGNATURE6) {
						throw new IllegalStateException("Encountered unexpected signature when looking for VIEW_COLUMN_FORMAT6: " + tempCol.getSignature());
					}
					
					// Re-read with variable data
					int totalLen = tempCol.getLength();
					ByteBuffer buf = ppData.getValue().getByteBuffer(0, totalLen);
					ViewColumnFormat6 col6 = MemoryStructureProxy.newStructure(ViewColumnFormat6.class, totalLen - MemoryStructureProxy.sizeOf(ViewColumnFormat6.class));
					col6.getData().put(buf);
					col.read(col6);
					
					ppData.setValue(ppData.getValue().share(totalLen));
					read += totalLen;
				}
			}
		}
		
		// It seems like it ends with a terminating 0 byte - possibly for padding to hit a WORD boundary
		
		return result;
	}
	
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
}
