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
package com.hcl.domino.misc;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

import com.hcl.domino.formula.FormulaCompiler;
import com.hcl.domino.richtext.structures.ResizableMemoryStructure;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public enum StructureSupport {
	;
	
	/**
	 * Extracts a "packed" string value from the structure. For this use, "packed" means that the string
	 * data is exactly as long as {@code len}, with no null terminator or WORD-size padding.
	 * 
	 * @param struct the structure to extract from
	 * @param preLen the number of bytes before the value to extract
	 * @param len the size of the stored value
	 * @return the extracted string value
	 */
	public static String extractStringValue(ResizableMemoryStructure struct, long preLen, long len) {
		if(len == 0) {
			return ""; //$NON-NLS-1$
		}
		if(preLen > Integer.MAX_VALUE) {
			throw new UnsupportedOperationException(MessageFormat.format("Unable to extract a string value with offset larger than {0} bytes", Integer.MAX_VALUE));
		}
		if(len > Integer.MAX_VALUE) {
			throw new UnsupportedOperationException(MessageFormat.format("Unable to extract a string value larger than {0} bytes", Integer.MAX_VALUE));
		}

		ByteBuffer buf = struct.getVariableData();
		buf.position((int)preLen);
		byte[] lmbcs = new byte[(int)len];
		buf.get(lmbcs);
		return new String(lmbcs, Charset.forName("LMBCS")); //$NON-NLS-1$
	}
	
	/**
	 * Extracts a "padded" string value from the structure. For this use, "padded" means that:
	 * 
	 * <ul>
	 *   <li>The string is not null-terminated</li>
	 *   <li>If the length of the string is odd, there is another packing null value at the end</li> 
	 * </ul>
	 * 
	 * @param struct the structure to extract from
	 * @param preLen the number of bytes before the value to extract
	 * @param len the size of the stored value
	 * @return the extracted string value
	 */
	public static String extractStringValueWordPadded(ResizableMemoryStructure struct, long preLen, long len) {
		if(len == 0) {
			return ""; //$NON-NLS-1$
		}
		if(preLen > Integer.MAX_VALUE) {
			throw new UnsupportedOperationException(MessageFormat.format("Unable to extract a string value with offset larger than {0} bytes", Integer.MAX_VALUE));
		}
		if(len > Integer.MAX_VALUE) {
			throw new UnsupportedOperationException(MessageFormat.format("Unable to extract a string value larger than {0} bytes", Integer.MAX_VALUE));
		}

		ByteBuffer buf = struct.getVariableData();
		buf.position((int)preLen);
		byte[] lmbcs = new byte[(int)len];
		buf.get(lmbcs);
		if(lmbcs[lmbcs.length-1] == '\0' || lmbcs[lmbcs.length-1] == -1) {
			return new String(lmbcs, 0, lmbcs.length-1, Charset.forName("LMBCS")); //$NON-NLS-1$
		} else {
			return new String(lmbcs, Charset.forName("LMBCS")); //$NON-NLS-1$
		}
	}

	/**
	 * Extracts an "unpacked" string value from the structure. For this use, "unpacked" means that:
	 * 
	 * <ul>
	 *   <li>The string is null-terminated</li>
	 *   <li>If the length of the string plus its terminator is odd, there is another packing null
	 *     value at the end</li> 
	 * </ul>
	 * 
	 * @param struct the structure to extract from
	 * @param preLen the number of bytes before the value to extract
	 * @param len the size of the stored value
	 * @return the extracted string value
	 */
	public static String extractStringValueUnpacked(ResizableMemoryStructure struct, long preLen, long len) {
		if(len == 0 || len == -1) {
			return ""; //$NON-NLS-1$
		}
		if(preLen > Integer.MAX_VALUE) {
			throw new UnsupportedOperationException(MessageFormat.format("Unable to extract a string value with offset larger than {0} bytes", Integer.MAX_VALUE));
		}
		if(len > Integer.MAX_VALUE) {
			throw new UnsupportedOperationException(MessageFormat.format("Unable to extract a string value larger than {0} bytes", Integer.MAX_VALUE));
		}

		ByteBuffer buf = struct.getVariableData();
		buf.position((int)preLen);
		byte[] lmbcs = new byte[(int)len];
		buf.get(lmbcs);
		int nullPos = (int)lmbcs.length-1;
		while((lmbcs[nullPos] == 0 || lmbcs[nullPos] == -1) && nullPos > 0) {
			nullPos--;
		}
		
		return new String(lmbcs, 0, nullPos+1, Charset.forName("LMBCS")); //$NON-NLS-1$
	}
	public static String extractCompiledFormula(ResizableMemoryStructure struct, long preLen, long len) {
		if(len == 0) {
			return ""; //$NON-NLS-1$
		}
		if(preLen > Integer.MAX_VALUE) {
			throw new UnsupportedOperationException(MessageFormat.format("Unable to extract a formula value with offset larger than {0} bytes", Integer.MAX_VALUE));
		}
		if(len > Integer.MAX_VALUE) {
			throw new UnsupportedOperationException(MessageFormat.format("Unable to extract a formula value larger than {0} bytes", Integer.MAX_VALUE));
		}

		ByteBuffer buf = struct.getVariableData();
		buf.position((int)preLen);
		byte[] compiled = new byte[(int)len];
		buf.get(compiled);
		return FormulaCompiler.get().decompile(compiled);
	}
	
	public static <T extends ResizableMemoryStructure> T writeStringValue(T struct, long preLen, long currentLen, String value, LongConsumer sizeWriter) {
		ByteBuffer buf = struct.getVariableData();
		long otherLen = buf.remaining() - preLen - currentLen;

		if(preLen + currentLen > Integer.MAX_VALUE) {
			throw new UnsupportedOperationException(MessageFormat.format("Unable to write a new value with offset larger than {0} bytes", Integer.MAX_VALUE));
		}
		if(otherLen > Integer.MAX_VALUE) {
			throw new UnsupportedOperationException(MessageFormat.format("Unable to write a new value with remaining offset larger than {0} bytes", Integer.MAX_VALUE));
		}
		buf.position((int)(preLen + currentLen));
		byte[] otherData = new byte[(int)otherLen];
		buf.get(otherData);
		
		byte[] lmbcs = value == null ? new byte[0] : value.getBytes(Charset.forName("LMBCS")); //$NON-NLS-1$
		if(sizeWriter != null) {
			sizeWriter.accept(lmbcs.length);
		}
		long newLen = preLen + otherLen + lmbcs.length;
		if(newLen > Integer.MAX_VALUE) {
			throw new UnsupportedOperationException(MessageFormat.format("Unable to write a new value exceeding {0} bytes", Integer.MAX_VALUE));
		}
		struct.resizeVariableData((int)newLen);
		buf = struct.getVariableData();
		buf.position((int)preLen);
		buf.put(lmbcs);
		buf.put(otherData);
		
		return struct;
	}
	public static <T extends ResizableMemoryStructure> T writeStringValue(T struct, int preLen, int currentLen, String value, IntConsumer sizeWriter) {
		return writeStringValue(struct, preLen, currentLen, value, (long newLen) -> sizeWriter.accept((int)newLen));
	}
	public static <T extends ResizableMemoryStructure> T writeStringValueShort(T struct, int preLen, int currentLen, String value, Consumer<Short> sizeWriter) {
		return writeStringValue(struct, preLen, currentLen, value, (long newLen) -> sizeWriter.accept((short)newLen));
	}
	
	/**
	 * Writes a "padded" string value to the provided structure, using the provided {@link IntConsumer} to update
	 * an associated length value. For this use, "unpacked" means that:
	 * 
	 * <ul>
	 *   <li>The string is not null-terminated</li>
	 *   <li>If the length of the string plus its terminator is odd, there is another packing null
	 *     value at the end</li> 
	 * </ul>
	 * 
	 * @param <T> the class of the structure
	 * @param struct the structure to modify
	 * @param preLen the amount of variable data in bytes before the place to write
	 * @param currentLen the current length of the variable data to overwrite
	 * @param value the value to write
	 * @param sizeWriter a callback to modify an associated length field, if applicable
	 * @return the provided structure
	 */
	public static <T extends ResizableMemoryStructure> T writeStringValueWordPadded(T struct, int preLen, int currentLen, String value, IntConsumer sizeWriter) {
		ByteBuffer buf = struct.getVariableData();
		long otherLen = buf.remaining() - preLen - currentLen;

		if(preLen + currentLen > Integer.MAX_VALUE) {
			throw new UnsupportedOperationException(MessageFormat.format("Unable to write a new value with offset larger than {0} bytes", Integer.MAX_VALUE));
		}
		if(otherLen > Integer.MAX_VALUE) {
			throw new UnsupportedOperationException(MessageFormat.format("Unable to write a new value with remaining offset larger than {0} bytes", Integer.MAX_VALUE));
		}
		buf.position((int)(preLen + currentLen));
		byte[] otherData = new byte[(int)otherLen];
		buf.get(otherData);
		
		byte[] lmbcs = value == null ? new byte[0] : value.getBytes(Charset.forName("LMBCS")); //$NON-NLS-1$
		/* Pad for word boundaries */
		int padLen = lmbcs.length % 2 == 0 ? 1 : 0;
		if(sizeWriter != null) {
			sizeWriter.accept(lmbcs.length + padLen);
		}
		long newLen = preLen + otherLen + lmbcs.length + padLen;
		if(newLen > Integer.MAX_VALUE) {
			throw new UnsupportedOperationException(MessageFormat.format("Unable to write a new value exceeding {0} bytes", Integer.MAX_VALUE));
		}
		struct.resizeVariableData((int)newLen);
		buf = struct.getVariableData();
		buf.position((int)preLen);
		buf.put(lmbcs);
		for(int i = 0; i < padLen; i++) {
			buf.put((byte)0);
		}
		buf.put(otherData);
		
		return struct;
	}
	
	/**
	 * Writes an "unpacked" string value to the provided structure, using the provided {@link IntConsumer} to update
	 * an associated length value. For this use, "unpacked" means that:
	 * 
	 * <ul>
	 *   <li>The string is null-terminated</li>
	 *   <li>If the length of the string plus its terminator is odd, there is another packing null
	 *     value at the end</li> 
	 * </ul>
	 * 
	 * @param <T> the class of the structure
	 * @param struct the structure to modify
	 * @param preLen the amount of variable data in bytes before the place to write
	 * @param currentLen the current length of the variable data to overwrite
	 * @param value the value to write
	 * @param sizeWriter a callback to modify an associated length field, if applicable
	 * @return the provided structure
	 */
	public static <T extends ResizableMemoryStructure> T writeStringValueUnpacked(T struct, int preLen, int currentLen, String value, IntConsumer sizeWriter) {
		ByteBuffer buf = struct.getVariableData();
		long otherLen = buf.remaining() - preLen - currentLen;

		if(preLen + currentLen > Integer.MAX_VALUE) {
			throw new UnsupportedOperationException(MessageFormat.format("Unable to write a new value with offset larger than {0} bytes", Integer.MAX_VALUE));
		}
		if(otherLen > Integer.MAX_VALUE) {
			throw new UnsupportedOperationException(MessageFormat.format("Unable to write a new value with remaining offset larger than {0} bytes", Integer.MAX_VALUE));
		}
		buf.position((int)(preLen + currentLen));
		byte[] otherData = new byte[(int)otherLen];
		buf.get(otherData);
		
		byte[] lmbcs = value == null ? new byte[0] : value.getBytes(Charset.forName("LMBCS")); //$NON-NLS-1$
		/* Pad for a \0 terminator and for if the result will then be odd-numbered */
		int padLen = 1 + (lmbcs.length % 2 == 0 ? 1 : 0);
		if(sizeWriter != null) {
			sizeWriter.accept(lmbcs.length + padLen);
		}
		long newLen = preLen + otherLen + lmbcs.length + padLen;
		if(newLen > Integer.MAX_VALUE) {
			throw new UnsupportedOperationException(MessageFormat.format("Unable to write a new value exceeding {0} bytes", Integer.MAX_VALUE));
		}
		struct.resizeVariableData((int)newLen);
		buf = struct.getVariableData();
		buf.position((int)preLen);
		buf.put(lmbcs);
		for(int i = 0; i < padLen; i++) {
			buf.put((byte)0);
		}
		buf.put(otherData);
		
		return struct;
	}
	
	public static <T extends ResizableMemoryStructure> T writeCompiledFormula(T struct, long preLen, long currentLen, String formula, LongConsumer sizeWriter) {
		ByteBuffer buf = struct.getVariableData();
		long otherLen = buf.remaining() - preLen - currentLen;
		if(preLen + currentLen > Integer.MAX_VALUE) {
			throw new UnsupportedOperationException(MessageFormat.format("Unable to write a new value with offset larger than {0} bytes", Integer.MAX_VALUE));
		}
		if(otherLen > Integer.MAX_VALUE) {
			throw new UnsupportedOperationException(MessageFormat.format("Unable to write a new value with remaining offset larger than {0} bytes", Integer.MAX_VALUE));
		}

		buf.position((int)(preLen + currentLen));
		byte[] otherData = new byte[(int)otherLen];
		buf.get(otherData);
		
		byte[] compiled = FormulaCompiler.get().compile(formula);
		if(sizeWriter != null) {
			sizeWriter.accept(compiled.length);
		}
		long newLen = preLen + otherLen + compiled.length;
		if(newLen > Integer.MAX_VALUE) {
			throw new UnsupportedOperationException(MessageFormat.format("Unable to write a new value exceeding {0} bytes", Integer.MAX_VALUE));
		}
		struct.resizeVariableData((int)newLen);
		buf = struct.getVariableData();
		buf.position((int)preLen);
		buf.put(compiled);
		buf.put(otherData);
		
		return struct;
	}
	public static <T extends ResizableMemoryStructure> T writeCompiledFormula(T struct, int preLen, int currentLen, String formula, IntConsumer sizeWriter) {
		return writeCompiledFormula(struct, preLen, currentLen, formula, (long newLen) -> sizeWriter.accept((int)newLen));
	}
}
