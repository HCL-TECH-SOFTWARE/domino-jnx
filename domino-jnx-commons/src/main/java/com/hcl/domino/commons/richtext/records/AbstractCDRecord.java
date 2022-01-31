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
package com.hcl.domino.commons.richtext.records;

import java.nio.ByteBuffer;

import com.hcl.domino.commons.richtext.structures.GenericResizableMemoryStructure;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.richtext.structures.CDSignature;
import com.hcl.domino.richtext.structures.MemoryStructure;

public abstract class AbstractCDRecord<T extends CDSignature<?, ?, ?>> extends GenericResizableMemoryStructure
    implements RichTextRecord<T> {
  public AbstractCDRecord(final ByteBuffer data, final Class<? extends MemoryStructure> recordClass) {
    super(data, recordClass);
  }

  protected abstract void _updateHeaderLength(long value);

  @Override
  public void resize(final int size) {
    super.resize(size);
    this._updateHeaderLength(size);
  }

  @Override
  public String toString() {
    return super.toString() + " - " + RecordType.getRecordTypesForConstant(this.getTypeValue());
  }
}
