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
package com.hcl.domino.jna.internal;

import java.util.EnumSet;

import com.hcl.domino.commons.util.DumpUtil;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;

/**
 * Utility class to dump memory content
 * 
 * @author Karsten Lehmann
 */
public class JNADumpUtil extends DumpUtil {
	
	/**
	 * Reads memory content at the specified pointer and produces a String with hex codes and
	 * character data in case the memory contains bytes in ascii range. Calls {@link #dumpAsAscii(Pointer, int, int)}
	 * with cols = 8.
	 * 
	 * @param ptr pointer
	 * @param size number of bytes to read
	 * @return memory dump
	 */
	public static String dumpAsAscii(Pointer ptr, int size) {
		return dumpAsAscii(ptr, size, 8);
	}
	
	/**
	 * Reads memory content at the specified pointer and produces a String with hex codes and
	 * character data in case the memory contains bytes in ascii range.
	 * 
	 * @param ptr pointer
	 * @param size number of bytes to read
	 * @param cols number of bytes written in on eline
	 * @return memory dump
	 */
	public static String dumpAsAscii(Pointer ptr, int size, int cols) {
		StringBuilder sb = new StringBuilder();
		
		int i = 0;
		
		if (ptr instanceof Memory) {
			size = (int) Math.min(size, ((Memory)ptr).size());
		}
		
		while (i < size) {
			sb.append("["); //$NON-NLS-1$
			for (int c=0; c<cols; c++) {
				if (c>0) {
					sb.append(' ');
				}
				
				if ((i+c) < size) {
					byte b = ptr.getByte(i+c);
					 if (b >=0 && b < 16)
					 {
						sb.append("0"); //$NON-NLS-1$
					}
			            sb.append(Integer.toHexString(b & 0xFF));
				}
				else {
					sb.append("  "); //$NON-NLS-1$
				}
			}
			sb.append("]"); //$NON-NLS-1$
			
			sb.append("   "); //$NON-NLS-1$
			
			sb.append("["); //$NON-NLS-1$
			for (int c=0; c<cols; c++) {
				if ((i+c) < size) {
					byte b = ptr.getByte(i+c);
					int bAsInt = (b & 0xff);
					
					if (bAsInt >= 32 && bAsInt<=126) {
						sb.append((char) (b & 0xFF));
					}
					else {
						sb.append("."); //$NON-NLS-1$
					}
				}
				else {
					sb.append(" "); //$NON-NLS-1$
				}
			}
			sb.append("]\n"); //$NON-NLS-1$

			i += cols;
		}
		return sb.toString();
	}
	
	/**
	 * Specified which type of handle data to dump
	 */
	public enum MemDump {
		PRIVATE(0x00000000),
		SHARED(0x00000001),
		/** Dump binary content of the allocated memory */
		CONTENTS(0x00000002),
		POOL(0x00000004),
		/** Only dump our process's shared and private. */
		PROCESS(0x00000008),
		/** dump OSLocal stuff */
		LOCAL(0x00000010),
		COMMANDLINE(0x00000020),
		FULL(0x00000040);
		
		private int type;
		
		MemDump(int type) {
			this.type = type;
		}
		
		private int getType() {
			return this.type;
		}
		
	};
	
	/**
	 * Writes information about the currently accolated memory handles to disk
	 * (&lt;notesdata&gt;/IBM_TECHNICAL_SUPPORT/memory_*.dmp).
	 * 
	 * @param flags data to dump
	 * @param blkType dump blocks of this type, 0 for all 
	 */
	public static void dumpHandleTable(EnumSet<MemDump> flags, int blkType) {
		int typeAsInt = 0;
		
		for (MemDump currType : flags) {
			typeAsInt = typeAsInt | currType.getType();
		}
		
		NotesCAPI.get().DEBUGDumpHandleTable(typeAsInt, (short) (blkType & 0xffff));
	}
	
	/**
	 * Dumps the current richtext CD record
	 * 
	 * @param nav richtext navigator
	 * @param out output stream
	 */
	/*
	public static void dumpCurrentRichtextRecord(IRichTextNavigator nav, PrintStream out) {
		short cdRecordTypeAsShort = nav.getCurrentRecordTypeAsShort();
		if (cdRecordTypeAsShort!=0) {
			CDRecordType cdRecordType = CDRecordType.getRecordTypeForConstant(cdRecordTypeAsShort, Area.TYPE_COMPOSITE);
			if (cdRecordType==null) {
				cdRecordType = CDRecordType.getRecordTypeForConstant(cdRecordTypeAsShort, Area.RESERVED_INTERNAL);
			}
			if (cdRecordType==null) {
				cdRecordType = CDRecordType.getRecordTypeForConstant(cdRecordTypeAsShort, Area.ALTERNATE_SEQ);
			}
			if (cdRecordType==null) {
				cdRecordType = CDRecordType.getRecordTypeForConstant(cdRecordTypeAsShort, Area.TARGET_FRAME);
			}
			if (cdRecordType==null) {
				cdRecordType = CDRecordType.getRecordTypeForConstant(cdRecordTypeAsShort, Area.FRAMESETS);
			}
			if (cdRecordType==null) {
				cdRecordType = CDRecordType.getRecordTypeForConstant(cdRecordTypeAsShort, Area.TYPE_VIEWMAP);
			}

			out.println("Record type: "+(cdRecordTypeAsShort & 0xffff)+(cdRecordType==null ? "" : " ("+cdRecordType+")"));
			out.println("Total length: "+nav.getCurrentRecordTotalLength());
			out.println("Header length: "+nav.getCurrentRecordHeaderLength());
			out.println("Data length: "+nav.getCurrentRecordDataLength());

			Memory memWithHeader = nav.getCurrentRecordDataWithHeader();
			if (memWithHeader!=null) {
				out.println("\n" + DumpUtil.dumpAsAscii(memWithHeader, (int) memWithHeader.size())+"\n");
			}
		}
	}
	*/
}
