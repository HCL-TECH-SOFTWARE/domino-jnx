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
package com.hcl.domino.data;

import java.nio.charset.Charset;
import java.util.List;

import com.hcl.domino.misc.JNXServiceFinder;
import com.hcl.domino.richtext.records.RecordType;

/**
 * This service interface represents an implementation-contributed service that
 * is able to decode item-type data from native memory
 *
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public interface NativeItemCoder {
  static NativeItemCoder get() {
    return JNXServiceFinder.findRequiredService(NativeItemCoder.class, NativeItemCoder.class.getClassLoader());
  }
  
  /**
   * Represents different modes for null-termination and newline
   * handling in LMBCS charsets.
   * 
   * @since 1.0.46
   */
  enum LmbcsVariant {
    NORMAL, NULLTERM, KEEPNEWLINES, NULLTERM_KEEPNEWLINES
  }

  List<String> decodeStringList(byte[] buf);

  byte[] encodeStringList(List<String> values);
  
  /**
   * Decodes the provided raw item data to a Java-compatible type.
   * 
   * @param buf the value to decode
   * @param area the rich-text record category to use when interpreting
   *             composite data
   * @return an object corresponding to the native data
   * @since 1.0.43
   */
  List<Object> decodeItemValue(byte[] buf, RecordType.Area area);
  
  /**
   * Retrieves an NIO {@code Charset} suitable for encoding and decoding LMBCS
   * strings.
   * 
   * @return a {@link Charset} instance for LMBCS
   * @since 1.0.46
   */
  default Charset getLmbcsCharset() {
    return getLmbcsCharset(LmbcsVariant.NORMAL);
  }
  
  /**
   * Retrieves an NIO {@code Charset} suitable for encoding and decoding LMBCS
   * strings with the provided newline and null-termination characteristics.
   * 
   * @param variant the {@link LmbcsVariant} to retrieve
   * @return a {@link Charset} instance for LMBCS
   * @since 1.0.46
   */
  Charset getLmbcsCharset(LmbcsVariant variant);
}
