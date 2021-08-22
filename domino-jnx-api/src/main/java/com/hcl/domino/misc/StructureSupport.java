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

  public static String extractCompiledFormula(final ResizableMemoryStructure struct, final long preLen, final long len) {
    if (len == 0) {
      return ""; //$NON-NLS-1$
    }
    if (preLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to extract a formula value with offset larger than {0} bytes", Integer.MAX_VALUE));
    }
    if (len > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to extract a formula value larger than {0} bytes", Integer.MAX_VALUE));
    }

    final ByteBuffer buf = struct.getVariableData();
    buf.position((int) preLen);
    final byte[] compiled = new byte[(int) len];
    buf.get(compiled);
    return FormulaCompiler.get().decompile(compiled);
  }

  /**
   * Extracts a "packed" string value from the structure. For this use, "packed"
   * means that the string
   * data is exactly as long as {@code len}, with no null terminator or WORD-size
   * padding.
   *
   * @param struct the structure to extract from
   * @param preLen the number of bytes before the value to extract
   * @param len    the size of the stored value
   * @return the extracted string value
   */
  public static String extractStringValue(final ResizableMemoryStructure struct, final long preLen, final long len) {
    if (len == 0) {
      return ""; //$NON-NLS-1$
    }
    if (preLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to extract a string value with offset larger than {0} bytes", Integer.MAX_VALUE));
    }
    if (len > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to extract a string value larger than {0} bytes", Integer.MAX_VALUE));
    }

    final ByteBuffer buf = struct.getVariableData();
    buf.position((int) preLen);
    final byte[] lmbcs = new byte[(int) len];
    buf.get(lmbcs);
    return new String(lmbcs, Charset.forName("LMBCS")); //$NON-NLS-1$
  }

  /**
   * Extracts an "unpacked" string value from the structure. For this use,
   * "unpacked" means that:
   * <ul>
   * <li>The string is null-terminated</li>
   * <li>If the length of the string plus its terminator is odd, there is another
   * packing null
   * value at the end</li>
   * </ul>
   *
   * @param struct the structure to extract from
   * @param preLen the number of bytes before the value to extract
   * @param len    the size of the stored value
   * @return the extracted string value
   */
  public static String extractStringValueUnpacked(final ResizableMemoryStructure struct, final long preLen, final long len) {
    if (len == 0 || len == -1) {
      return ""; //$NON-NLS-1$
    }
    if (preLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to extract a string value with offset larger than {0} bytes", Integer.MAX_VALUE));
    }
    if (len > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to extract a string value larger than {0} bytes", Integer.MAX_VALUE));
    }

    final ByteBuffer buf = struct.getVariableData();
    buf.position((int) preLen);
    final byte[] lmbcs = new byte[(int) len];
    buf.get(lmbcs);
    int nullPos = lmbcs.length - 1;
    while ((lmbcs[nullPos] == 0 || lmbcs[nullPos] == -1) && nullPos > 0) {
      nullPos--;
    }

    return new String(lmbcs, 0, nullPos + 1, Charset.forName("LMBCS")); //$NON-NLS-1$
  }

  /**
   * Extracts a "padded" string value from the structure. For this use, "padded"
   * means that:
   * <ul>
   * <li>The string is not null-terminated</li>
   * <li>If the length of the string is odd, there is another packing null value
   * at the end</li>
   * </ul>
   *
   * @param struct the structure to extract from
   * @param preLen the number of bytes before the value to extract
   * @param len    the size of the stored value
   * @return the extracted string value
   */
  public static String extractStringValueWordPadded(final ResizableMemoryStructure struct, final long preLen, final long len) {
    if (len == 0) {
      return ""; //$NON-NLS-1$
    }
    if (preLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to extract a string value with offset larger than {0} bytes", Integer.MAX_VALUE));
    }
    if (len > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to extract a string value larger than {0} bytes", Integer.MAX_VALUE));
    }

    final ByteBuffer buf = struct.getVariableData();
    buf.position((int) preLen);
    final byte[] lmbcs = new byte[(int) len];
    buf.get(lmbcs);
    if (lmbcs[lmbcs.length - 1] == '\0' || lmbcs[lmbcs.length - 1] == -1) {
      return new String(lmbcs, 0, lmbcs.length - 1, Charset.forName("LMBCS")); //$NON-NLS-1$
    } else {
      return new String(lmbcs, Charset.forName("LMBCS")); //$NON-NLS-1$
    }
  }

  public static <T extends ResizableMemoryStructure> T writeCompiledFormula(final T struct, final int preLen, final int currentLen,
      final String formula, final IntConsumer sizeWriter) {
    return StructureSupport.writeCompiledFormula(struct, preLen, currentLen, formula,
    		(final long newLen) -> {
    			if (sizeWriter!=null) {
    				sizeWriter.accept((int) newLen);
    			}
    		});
  }

  public static <T extends ResizableMemoryStructure> T writeCompiledFormula(final T struct, final long preLen,
      final long currentLen, final String formula, final LongConsumer sizeWriter) {
    ByteBuffer buf = struct.getVariableData();
    final long otherLen = buf.remaining() - preLen - currentLen;
    if (preLen + currentLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value with offset larger than {0} bytes", Integer.MAX_VALUE));
    }
    if (otherLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value with remaining offset larger than {0} bytes", Integer.MAX_VALUE));
    }

    buf.position((int) (preLen + currentLen));
    final byte[] otherData = new byte[(int) otherLen];
    buf.get(otherData);

    final byte[] compiled = FormulaCompiler.get().compile(formula);
    if (sizeWriter != null) {
      sizeWriter.accept(compiled.length);
    }
    final long newLen = preLen + otherLen + compiled.length;
    if (newLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value exceeding {0} bytes", Integer.MAX_VALUE));
    }
    struct.resizeVariableData((int) newLen);
    buf = struct.getVariableData();
    buf.position((int) preLen);
    buf.put(compiled);
    buf.put(otherData);

    return struct;
  }

  public static <T extends ResizableMemoryStructure> T writeStringValue(final T struct, final int preLen, final int currentLen,
      final String value, final IntConsumer sizeWriter) {
    return StructureSupport.writeStringValue(struct, preLen, currentLen, value,
        (final long newLen) -> sizeWriter.accept((int) newLen));
  }

  public static <T extends ResizableMemoryStructure> T writeStringValue(final T struct, final long preLen, final long currentLen,
      final String value, final LongConsumer sizeWriter) {
    ByteBuffer buf = struct.getVariableData();
    final long otherLen = buf.remaining() - preLen - currentLen;

    if (preLen + currentLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value with offset larger than {0} bytes", Integer.MAX_VALUE));
    }
    if (otherLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value with remaining offset larger than {0} bytes", Integer.MAX_VALUE));
    }
    buf.position((int) (preLen + currentLen));
    final byte[] otherData = new byte[(int) otherLen];
    buf.get(otherData);

    final byte[] lmbcs = value == null ? new byte[0] : value.getBytes(Charset.forName("LMBCS")); //$NON-NLS-1$
    if (sizeWriter != null) {
      sizeWriter.accept(lmbcs.length);
    }
    final long newLen = preLen + otherLen + lmbcs.length;
    if (newLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value exceeding {0} bytes", Integer.MAX_VALUE));
    }
    struct.resizeVariableData((int) newLen);
    buf = struct.getVariableData();
    buf.position((int) preLen);
    buf.put(lmbcs);
    buf.put(otherData);

    return struct;
  }

  public static <T extends ResizableMemoryStructure> T writeStringValueShort(final T struct, final int preLen, final int currentLen,
      final String value, final Consumer<Short> sizeWriter) {
    return StructureSupport.writeStringValue(struct, preLen, currentLen, value,
        (final long newLen) -> sizeWriter.accept((short) newLen));
  }

  /**
   * Writes an "unpacked" string value to the provided structure, using the
   * provided {@link IntConsumer} to update
   * an associated length value. For this use, "unpacked" means that:
   * <ul>
   * <li>The string is null-terminated</li>
   * <li>If the length of the string plus its terminator is odd, there is another
   * packing null
   * value at the end</li>
   * </ul>
   *
   * @param <T>        the class of the structure
   * @param struct     the structure to modify
   * @param preLen     the amount of variable data in bytes before the place to
   *                   write
   * @param currentLen the current length of the variable data to overwrite
   * @param value      the value to write
   * @param sizeWriter a callback to modify an associated length field, if
   *                   applicable
   * @return the provided structure
   */
  public static <T extends ResizableMemoryStructure> T writeStringValueUnpacked(final T struct, final int preLen,
      final int currentLen, final String value, final IntConsumer sizeWriter) {
    ByteBuffer buf = struct.getVariableData();
    final long otherLen = buf.remaining() - preLen - currentLen;

    if (preLen + currentLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value with offset larger than {0} bytes", Integer.MAX_VALUE));
    }
    if (otherLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value with remaining offset larger than {0} bytes", Integer.MAX_VALUE));
    }
    buf.position(preLen + currentLen);
    final byte[] otherData = new byte[(int) otherLen];
    buf.get(otherData);

    final byte[] lmbcs = value == null ? new byte[0] : value.getBytes(Charset.forName("LMBCS")); //$NON-NLS-1$
    /* Pad for a \0 terminator and for if the result will then be odd-numbered */
    final int padLen = 1 + (lmbcs.length % 2 == 0 ? 1 : 0);
    if (sizeWriter != null) {
      sizeWriter.accept(lmbcs.length + padLen);
    }
    final long newLen = preLen + otherLen + lmbcs.length + padLen;
    if (newLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value exceeding {0} bytes", Integer.MAX_VALUE));
    }
    struct.resizeVariableData((int) newLen);
    buf = struct.getVariableData();
    buf.position(preLen);
    buf.put(lmbcs);
    for (int i = 0; i < padLen; i++) {
      buf.put((byte) 0);
    }
    buf.put(otherData);

    return struct;
  }

  /**
   * Writes a "padded" string value to the provided structure, using the provided
   * {@link IntConsumer} to update
   * an associated length value. For this use, "unpacked" means that:
   * <ul>
   * <li>The string is not null-terminated</li>
   * <li>If the length of the string plus its terminator is odd, there is another
   * packing null
   * value at the end</li>
   * </ul>
   *
   * @param <T>        the class of the structure
   * @param struct     the structure to modify
   * @param preLen     the amount of variable data in bytes before the place to
   *                   write
   * @param currentLen the current length of the variable data to overwrite
   * @param value      the value to write
   * @param sizeWriter a callback to modify an associated length field, if
   *                   applicable
   * @return the provided structure
   */
  public static <T extends ResizableMemoryStructure> T writeStringValueWordPadded(final T struct, final int preLen,
      final int currentLen, final String value, final IntConsumer sizeWriter) {
    ByteBuffer buf = struct.getVariableData();
    final long otherLen = buf.remaining() - preLen - currentLen;

    if (preLen + currentLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value with offset larger than {0} bytes", Integer.MAX_VALUE));
    }
    if (otherLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value with remaining offset larger than {0} bytes", Integer.MAX_VALUE));
    }
    buf.position(preLen + currentLen);
    final byte[] otherData = new byte[(int) otherLen];
    buf.get(otherData);

    final byte[] lmbcs = value == null ? new byte[0] : value.getBytes(Charset.forName("LMBCS")); //$NON-NLS-1$
    /* Pad for word boundaries */
    final int padLen = lmbcs.length % 2 == 0 ? 1 : 0;
    if (sizeWriter != null) {
      sizeWriter.accept(lmbcs.length + padLen);
    }
    final long newLen = preLen + otherLen + lmbcs.length + padLen;
    if (newLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value exceeding {0} bytes", Integer.MAX_VALUE));
    }
    struct.resizeVariableData((int) newLen);
    buf = struct.getVariableData();
    buf.position(preLen);
    buf.put(lmbcs);
    for (int i = 0; i < padLen; i++) {
      buf.put((byte) 0);
    }
    buf.put(otherData);

    return struct;
  }
  
  /**
   * Reads an LMBCS string from a byte array, such as those of fixed-size struct members.
   * 
   * <p>This method handles reading only up to the first null value.</p>
   * 
   * @param lmbcs the fixed-size array containing the string
   * @return the read value
   * @since 1.0.34
   */
  public static String readLmbcsValue(byte[] lmbcs) {
    int firstNull = 0;
    for(firstNull = 0; firstNull < lmbcs.length; firstNull++) {
      if(lmbcs[firstNull] == '\0') {
        break;
      }
    }
    return new String(lmbcs, 0, firstNull, Charset.forName("LMBCS")); //$NON-NLS-1$
  }
}
