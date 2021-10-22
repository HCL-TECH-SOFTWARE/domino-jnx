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
package com.hcl.domino.richtext.records;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.structures.CDSignature;
import com.hcl.domino.richtext.structures.ResizableMemoryStructure;

/**
 * Base interface for rich text records. We provide sub interfaces for
 * selected record types with extended functionality.
 *
 * @param <T> the signature type of this record
 */
@SuppressWarnings("rawtypes")
public interface RichTextRecord<T extends CDSignature> extends ResizableMemoryStructure {
  /**
   * Returns the total length of the CD record including the signature bytes
   * containing the type and length
   *
   * @return length
   */
  default int getCDRecordLength() {
    return this.getData().capacity();
  }

  /**
   * Returns the raw CD record data <b>including</b> the header signature
   * (containing the type and length of the record)
   *
   * @return data with header
   */
  @Override
  ByteBuffer getData();

  /**
   * Returns the raw CD record data <b>without</b> the header signature
   * (containing the type and length of the record)
   *
   * @return data without header
   */
  default ByteBuffer getDataWithoutHeader() {
    final ByteBuffer data = this.getData();
    data.position(this.getRecordHeaderLength());
    return data.slice().order(ByteOrder.nativeOrder());
  }

  T getHeader();

  /**
   * Returns the size of the CD record payload (the data without the record header
   * bytes)
   *
   * @return payload length
   */
  default int getPayloadLength() {
    return this.getCDRecordLength() - this.getRecordHeaderLength();
  }

  /**
   * Returns the length of the signature bytes containing the record
   * type and total lengths at the beginning of {@link #getData()}
   *
   * @return signature length
   */
  default int getRecordHeaderLength() {
    final short typeAsShort = this.getData().getShort(0);
    final short highOrderByte = (short) (typeAsShort & 0xFF00);
    switch (highOrderByte) {
      case RichTextConstants.LONGRECORDLENGTH: /* LSIG */
        return 6;
      case RichTextConstants.WORDRECORDLENGTH: /* WSIG */
        return 4;
      default: /* BSIG */
        return 2;
    }
  }

  /**
   * Returns the type of the record, if known. If this record is unknown,
   * this method will return all potential matching signatures.
   *
   * @return a set of {@link RecordType} values that have the value
   *         {@link #getTypeValue()} (there may be duplicates like
   *         PABHIDE/VMTEXTBOX or ACTION/VMPOLYRGN)
   */
  default Set<RecordType> getType() {
    return RecordType.getRecordTypesForConstant(this.getTypeValue());
  }

  /**
   * Returns a numeric type constant for the record
   *
   * @return type
   */
  default short getTypeValue() {
    final short typeAsShort = this.getData().getShort(0);
    final short highOrderByte = (short) (typeAsShort & 0xFF00);
    switch (highOrderByte) {
      case RichTextConstants.LONGRECORDLENGTH: /* LSIG */
      case RichTextConstants.WORDRECORDLENGTH: /* WSIG */
        return typeAsShort;
      default: /* BSIG */
        return (short) (typeAsShort & 0x00FF); /* Length not part of signature */
    }
  }
}
