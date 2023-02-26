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
package com.hcl.domino.misc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.hcl.domino.data.NativeItemCoder;
import com.hcl.domino.data.NativeItemCoder.LmbcsVariant;
import com.hcl.domino.formula.FormulaCompiler;
import com.hcl.domino.richtext.structures.ListStructure;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;
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
    if(preLen > buf.limit()) {
      return ""; //$NON-NLS-1$
    }
    buf.position((int) Math.max(0, preLen));
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
    if(preLen > buf.limit()) {
      return ""; //$NON-NLS-1$
    }
    buf.position((int) Math.max(0, preLen));
    // Account for cases where the logical length is incorrect
    final int resolvedLen = Math.min((int)len, buf.remaining());
    final byte[] lmbcs = new byte[resolvedLen];
    buf.get(lmbcs);
    return new String(lmbcs, NativeItemCoder.get().getLmbcsCharset());
  }

  /**
   * Extracts a list of "packed" string values from the structure. For this use, "packed"
   * means that the string data is exactly as long as each of the lengths in {@code lengths},
   * with no null terminator or WORD-size padding.
   *
   * @param struct  the structure to extract from
   * @param preLen  the number of bytes before the value to extract
   * @param lengths the lengths of the string values
   * @return the extracted string values
   * @since 1.0.38
   */
  public static List<String> extractStringValues(final ResizableMemoryStructure struct, final long preLen, final long[] lengths) {
    if (lengths.length == 0) {
      return Collections.emptyList();
    }
    if (preLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to extract a string value with offset larger than {0} bytes", Integer.MAX_VALUE));
    }

    final ByteBuffer buf = struct.getVariableData();
    if(preLen > buf.limit()) {
      return Collections.emptyList();
    }
    buf.position((int) Math.max(0, preLen));
    List<String> result = new ArrayList<>(lengths.length);
    for(long len : lengths) {
      final byte[] lmbcs = new byte[(int) len];
      buf.get(lmbcs);
      result.add(new String(lmbcs, NativeItemCoder.get().getLmbcsCharset())); 
    }
    return result;
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
    if(preLen > buf.limit()) {
      return ""; //$NON-NLS-1$
    }
    buf.position((int) Math.max(0, preLen));
    final byte[] lmbcs = new byte[(int) len];
    buf.get(lmbcs);
    int nullPos = lmbcs.length - 1;
    while ((lmbcs[nullPos] == 0 || lmbcs[nullPos] == -1) && nullPos > 0) {
      nullPos--;
    }

    return new String(lmbcs, 0, nullPos + 1, NativeItemCoder.get().getLmbcsCharset());
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
    if(preLen > buf.limit()) {
      return ""; //$NON-NLS-1$
    }
    buf.position((int) Math.max(0, preLen));
    final byte[] lmbcs = new byte[(int) len];
    buf.get(lmbcs);
    if (lmbcs[lmbcs.length - 1] == '\0' || lmbcs[lmbcs.length - 1] == -1) {
      return new String(lmbcs, 0, lmbcs.length - 1, NativeItemCoder.get().getLmbcsCharset());
    } else {
      return new String(lmbcs, NativeItemCoder.get().getLmbcsCharset());
    }
  }
  
  /**
   * Extracts a list of strings stored using the {@link ListStructure} format.
   * 
   * @param struct the structure to read from
   * @param preLen the number of bytes before the value to extract
   * @return a {@link List} of extracted strings
   * @since 1.0.38
   */
  public static List<String> extractStringListStructure(ResizableMemoryStructure struct, int preLen) {
    ByteBuffer buf = struct.getVariableData();
    buf.position(Math.max(0, preLen));
    
    int count = Short.toUnsignedInt(buf.getShort());
    
    int[] sizes = new int[count];
    for(int i = 0; i < count; i++) {
      sizes[i] = Short.toUnsignedInt(buf.getShort());
    }
    
    List<String> result = new ArrayList<>();
    for(int i = 0; i < count; i++) {
      // Read sizes[i] bytes as LMBCS
      byte[] lmbcs = new byte[sizes[i]];
      buf.get(lmbcs);
      result.add(new String(lmbcs, NativeItemCoder.get().getLmbcsCharset()));
    }
    return result;
  }

  /**
   * Extracts raw byte data from a structure.
   *
   * @param struct the structure to extract from
   * @param preLen the number of bytes before the value to extract
   * @param len    the size of the stored value
   * @return the extracted byte data
   * @since 1.0.43
   */
  public static byte[] extractByteArray(final ResizableMemoryStructure struct, final long preLen, final long len) {
    if (len == 0) {
      return new byte[0];
    }
    if (preLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to extract a byte value with offset larger than {0} bytes", Integer.MAX_VALUE));
    }
    if (len > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to extract a byte value larger than {0} bytes", Integer.MAX_VALUE));
    }

    final ByteBuffer buf = struct.getVariableData();
    if(preLen > buf.limit()) {
      return new byte[0];
    }
    buf.position((int) Math.max(0, preLen));
    final byte[] result = new byte[(int) len];
    buf.get(result);
    return result;
  }

  /**
   * Extracts a signed int array from a structure.
   *
   * @param struct the structure to extract from
   * @param preLen the number of bytes before the value to extract
   * @param len    the size of the stored value
   * @return the extracted int array
   * @since 1.1.2
   */
  public static int[] extractIntArray(final ResizableMemoryStructure struct, final long preLen, final long len) {
    if (len == 0) {
      return new int[0];
    }
    if (preLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to extract a int value with offset larger than {0} bytes", Integer.MAX_VALUE));
    }
    if (len > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to extract a int value larger than {0} bytes", Integer.MAX_VALUE));
    }

    final ByteBuffer buf = struct.getVariableData();
    if(preLen > buf.limit()) {
      return new int[0];
    }
    buf.position((int) Math.max(0, preLen));
    final int[] result = new int[(int) len];
    for(int i = 0; i < len; i++) {
      result[i] = buf.getInt();
    }
    return result;
  }
  
  /**
   * Extracts an array of variable-sized structures from the variable-data portion of a structure.
   * 
   * @param <T> the type of structure to extract
   * @param struct the parent structure
   * @param preLen the number of bytes before the value to extract
   * @param type a {@link Class} representing {@code <T>}
   * @param count the count of structures to extract
   * @param sizeFunc a {@link Function} that determines the total length of each structure
   * @return a {@link List} of {@code T} objects
   * @since 1.0.44
   */
  public static <T extends ResizableMemoryStructure> List<T> extractResizableStructures(ResizableMemoryStructure struct, int preLen, Class<T> type, int count, Function<T, Integer> sizeFunc) {
    ByteBuffer data = struct.getVariableData();
    data.position(Math.max(0, preLen));
    
    MemoryStructureWrapperService svc = MemoryStructureWrapperService.get();
    List<T> result = new ArrayList<>(count);
    int baseSize = svc.sizeOf(type);
    for(int i = 0; i < count; i++) {
      ByteBuffer sub = data.slice();
      sub.limit(baseSize);
      T base = svc.wrapStructure(type, sub);
      int size = sizeFunc.apply(base);
      
      sub = data.slice();
      sub.limit(size);
      result.add(svc.wrapStructure(type, sub));
      
      data.position(data.position()+size);
    }
    
    return result;
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

    buf.position((int) (Math.max(0, preLen) + currentLen));
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

  public static <T extends ResizableMemoryStructure> T writeStringValue(final T struct, final int preLen, final int currentLen,
      final String value, boolean replaceLinebreaks, final IntConsumer sizeWriter) {
    return StructureSupport.writeStringValue(struct, preLen, currentLen, value, replaceLinebreaks,
        (final long newLen) -> sizeWriter.accept((int) newLen));
  }

  public static <T extends ResizableMemoryStructure> T writeStringValue(final T struct, final long preLen, final long currentLen,
      final String value, final LongConsumer sizeWriter) {
    
    return writeStringValue(struct, preLen, currentLen, value, true, sizeWriter);
  }
  
  public static <T extends ResizableMemoryStructure> T writeStringValue(final T struct, final long preLen, final long currentLen,
      final String value, final boolean replaceLinebreaks, final LongConsumer sizeWriter) {
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
    buf.position((int) (Math.max(0, preLen) + currentLen));
    final byte[] otherData = new byte[(int) otherLen];
    buf.get(otherData);

    final byte[] lmbcs;
    if (value == null) {
      lmbcs = new byte[0];
    }
    else if (replaceLinebreaks) {
      lmbcs = value.getBytes(NativeItemCoder.get().getLmbcsCharset());
    }
    else {
      lmbcs = value.getBytes(NativeItemCoder.get().getLmbcsCharset(LmbcsVariant.KEEPNEWLINES));
    }
    
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
  
  /**
   * Writes a list of string values as packed string into variable data.
   * 
   * @param <T> the type of structure containing the data
   * @param struct the structure to write to
   * @param preLen the number of bytes before the value to write
   * @param currentLengths the current length values of existing data
   * @param value the values to write
   * @param sizeWriter a callback to call with the new lengths
   * @return the value passed as {@code struct}
   * @since 1.0.38
   */
  public static <T extends ResizableMemoryStructure> T writeStringValues(final T struct, final int preLen, final long[] currentLengths,
      final List<String> value, final Consumer<long[]> sizeWriter) {
    if(currentLengths.length == 0) {
      return struct;
    }
    if(Objects.requireNonNull(currentLengths).length != Objects.requireNonNull(value).size()) {
      throw new IllegalArgumentException("The lengths of currentLengths and value must match");
    }
    byte[][] data = new byte[value.size()][];
    for(int i = 0; i < currentLengths.length; i++) {
      String val = value.get(i);
      if(val == null) {
        val = ""; //$NON-NLS-1$
      }
      data[i] = val.getBytes(NativeItemCoder.get().getLmbcsCharset());
    }

    ByteBuffer buf = struct.getVariableData();
    long currentLen = LongStream.of(currentLengths).sum();
    final long otherLen = buf.remaining() - preLen - currentLen;
    buf.position((int)(Math.max(0, preLen) + currentLen));
    final byte[] otherData = new byte[(int) otherLen];
    buf.get(otherData);

    long[] newLengths = Stream.of(data).mapToLong(d -> Integer.toUnsignedLong(d.length)).toArray();
    long lmbcsLen = LongStream.of(newLengths).sum();
    final long newLen = preLen + otherLen + lmbcsLen;
    if (newLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value exceeding {0} bytes", Integer.MAX_VALUE));
    }
    struct.resizeVariableData((int) newLen);
    buf = struct.getVariableData();
    buf.position(preLen);
    for(byte[] lmbcs : data) {
      buf.put(lmbcs);
    }
    buf.put(otherData);
    
    if(sizeWriter != null) {
      sizeWriter.accept(newLengths);
    }

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
    final long otherLen = buf.remaining() - Math.max(0, preLen) - currentLen;

    if (Math.max(0, preLen) + currentLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value with offset larger than {0} bytes", Integer.MAX_VALUE));
    }
    if (otherLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value with remaining offset larger than {0} bytes", Integer.MAX_VALUE));
    }
    buf.position(Math.max(0, preLen) + currentLen);
    final byte[] otherData = new byte[(int) otherLen];
    buf.get(otherData);

    final byte[] lmbcs = value == null ? new byte[0] : value.getBytes(NativeItemCoder.get().getLmbcsCharset());
    /* Pad for a \0 terminator and for if the result will then be odd-numbered */
    final int padLen = 1 + (lmbcs.length % 2 == 0 ? 1 : 0);
    if (sizeWriter != null) {
      sizeWriter.accept(lmbcs.length + padLen);
    }
    final long newLen = Math.max(0, preLen) + otherLen + lmbcs.length + padLen;
    if (newLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value exceeding {0} bytes", Integer.MAX_VALUE));
    }
    struct.resizeVariableData((int) newLen);
    buf = struct.getVariableData();
    buf.position(Math.max(0, preLen));
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

    if (Math.max(0, preLen) + currentLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value with offset larger than {0} bytes", Integer.MAX_VALUE));
    }
    if (otherLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value with remaining offset larger than {0} bytes", Integer.MAX_VALUE));
    }
    buf.position(Math.max(0, preLen) + currentLen);
    final byte[] otherData = new byte[(int) otherLen];
    buf.get(otherData);

    final byte[] lmbcs = value == null ? new byte[0] : value.getBytes(NativeItemCoder.get().getLmbcsCharset());
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
   * Writes a {@link ListStructure} value of null-delimited strings, using the provided
   * {@link IntConsumer} to update an associated length value.
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
   * @since 1.0.38
   */
  public static <T extends ResizableMemoryStructure> T writeStringListStructure(final T struct, final int preLen,
      final int currentLen, final List<String> value, final IntConsumer sizeWriter) {
    ByteBuffer buf = struct.getVariableData();
    final long otherLen = buf.remaining() - Math.max(0, preLen) - currentLen;

    if (Math.max(0, preLen) + currentLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value with offset larger than {0} bytes", Integer.MAX_VALUE));
    }
    if (otherLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value with remaining offset larger than {0} bytes", Integer.MAX_VALUE));
    }
    buf.position(Math.max(0, preLen) + currentLen);
    final byte[] otherData = new byte[(int) otherLen];
    buf.get(otherData);

    byte[] lmbcs;
    try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      
      for(int i = 0; i < value.size(); i++) {
        String val = value.get(i);
        byte[] data = val == null ? new byte[0] : val.getBytes(NativeItemCoder.get().getLmbcsCharset());
        baos.write(data);
        baos.write(0);
      }
      lmbcs = baos.toByteArray();
    } catch(IOException e) {
      throw new UncheckedIOException(e);
    }
    final long newLen = Math.max(0, preLen) + otherLen + 2 + lmbcs.length;
    if (newLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value exceeding {0} bytes", Integer.MAX_VALUE));
    }
    struct.resizeVariableData((int) newLen);
    buf = struct.getVariableData();
    buf.position(Math.max(0, preLen));
    buf.putShort((short)value.size());
    buf.put(lmbcs);
    buf.put(otherData);
    
    if(sizeWriter != null) {
      sizeWriter.accept(lmbcs.length + 2);
    }

    return struct;
  }
  
  /**
   * Writes a raw byte value into the variable-data portion of a structure, using the provided
   * {@link IntConsumer} to update an associated length value.
   * 
   * @param <T>        the class of the structure
   * @param struct     the structure to modify
   * @param preLen     the amount of variable data in bytes before the place to
   *                   write
   * @param currentLen the current length of the variable data to overwrite
   * @param valueParam the value to write
   * @param sizeWriter a callback to modify an associated length field, if
   *                   applicable
   * @return the provided structure
   * @since 1.0.43
   */
  public static <T extends ResizableMemoryStructure> T writeByteValue(final T struct, final long preLen, final long currentLen,
      final byte[] valueParam, final IntConsumer sizeWriter) {
    byte[] value = valueParam == null ? new byte[0] : valueParam;
    
    ByteBuffer buf = struct.getVariableData();
    final long otherLen = buf.remaining() - Math.max(0, preLen) - currentLen;

    if (Math.max(0, preLen) + currentLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value with offset larger than {0} bytes", Integer.MAX_VALUE));
    }
    if (otherLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value with remaining offset larger than {0} bytes", Integer.MAX_VALUE));
    }
    buf.position((int) (Math.max(0, preLen) + currentLen));
    final byte[] otherData = new byte[(int) otherLen];
    buf.get(otherData);
    
    if (sizeWriter != null) {
      sizeWriter.accept(value.length);
    }
    final long newLen = Math.max(0, preLen) + otherLen + value.length;
    if (newLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value exceeding {0} bytes", Integer.MAX_VALUE));
    }
    struct.resizeVariableData((int) newLen);
    buf = struct.getVariableData();
    buf.position((int) Math.max(0, preLen));
    buf.put(value);
    buf.put(otherData);

    return struct;
  }
  
  /**
   * Writes an int array value into the variable-data portion of a structure, using the provided
   * {@link IntConsumer} to update an associated length value.
   * 
   * @param <T>        the class of the structure
   * @param struct     the structure to modify
   * @param preLen     the amount of variable data in bytes before the place to
   *                   write
   * @param currentLen the current length of the variable data to overwrite
   * @param valueParam the value to write
   * @param sizeWriter a callback to modify an associated length field, if
   *                   applicable
   * @return the provided structure
   * @since 1.1.2
   */
  public static <T extends ResizableMemoryStructure> T writeIntValue(final T struct, final long preLen, final long currentLen,
      final int[] valueParam, final IntConsumer sizeWriter) {
    int[] value = valueParam == null ? new int[0] : valueParam;
    
    ByteBuffer buf = struct.getVariableData();
    final long otherLen = buf.remaining() - Math.max(0, preLen) - currentLen;

    if (Math.max(0, preLen) + currentLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value with offset larger than {0} bytes", Integer.MAX_VALUE));
    }
    if (otherLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value with remaining offset larger than {0} bytes", Integer.MAX_VALUE));
    }
    buf.position((int) (Math.max(0, preLen) + currentLen));
    final byte[] otherData = new byte[(int) otherLen];
    buf.get(otherData);
    
    int valueLen = value.length * 4;
    if (sizeWriter != null) {
      sizeWriter.accept(valueLen);
    }
    final long newLen = Math.max(0, preLen) + otherLen + valueLen;
    if (newLen > Integer.MAX_VALUE) {
      throw new UnsupportedOperationException(
          MessageFormat.format("Unable to write a new value exceeding {0} bytes", Integer.MAX_VALUE));
    }
    struct.resizeVariableData((int) newLen);
    buf = struct.getVariableData();
    buf.position((int)Math.max(0, preLen));
    Arrays.stream(value).forEach(buf::putInt);
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
    return new String(lmbcs, 0, firstNull, NativeItemCoder.get().getLmbcsCharset());
  }
}
