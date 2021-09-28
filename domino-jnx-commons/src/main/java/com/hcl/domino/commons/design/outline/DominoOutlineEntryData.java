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
package com.hcl.domino.commons.design.outline;

import java.nio.charset.Charset;
import java.util.Optional;
import com.hcl.domino.commons.richtext.RichTextUtil;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.design.OutlineEntryData;
import com.hcl.domino.formula.FormulaCompiler;
import com.hcl.domino.richtext.records.RecordType;

public class DominoOutlineEntryData implements IAdaptable, OutlineEntryData {
  
  private byte[] dataBuf;
  private short dataType;
  
  public DominoOutlineEntryData(final short type, final byte[] val) {
    this.dataBuf = val;
    this.dataType = type;
  }
  
  public DominoOutlineEntryData(final DominoOutlineEntryData val) {
    this.dataBuf = val.dataBuf;
    this.dataType = val.dataType;
  }
  
  @Override
  public short getDataType() {
    return this.dataType;
  }
  
  public void setDataType(final short type) {
    this.dataType = type;
  }
  
  @Override
  public Optional<Object> getDataValue() {
    if (this.dataType ==  ItemDataType.TYPE_FORMULA.getValue()) {
      if (this.dataBuf == null || this.dataBuf.length == 0) {
        return Optional.empty();
      } else {
        return Optional.of(FormulaCompiler.get().decompile(this.dataBuf));
      }
    } else if (this.dataType ==  ItemDataType.TYPE_TEXT.getValue()) {
      if (this.dataBuf == null || this.dataBuf.length == 0) {
        return Optional.empty();
      } else {
        return Optional.of(new String(this.dataBuf, Charset.forName("LMBCS"))); //$NON-NLS-1$
      }
    } else if (this.dataType ==  ItemDataType.TYPE_COMPOSITE.getValue()) {
      if (this.dataBuf == null || this.dataBuf.length == 0) {
        return Optional.empty();
      } else {
        try {
          return Optional.of(RichTextUtil.readMemoryRecords(this.dataBuf, RecordType.Area.RESERVED_INTERNAL));
        } catch (Exception e) {
          //exception reading ad rich text just return as String
          return Optional.of(new String(this.dataBuf, Charset.forName("LMBCS"))); //$NON-NLS-1$
        }
      }
    }
    
    return Optional.empty();
  }
  
  public void setDataValue(final byte[]  buf) {
    this.dataBuf = buf;
  }

  @Override
  public <T> T getAdapter(Class<T> clazz) {
    // TODO Auto-generated method stub
    return null;
  }

}
