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
  
  public short getDataType() {
    return this.dataType;
  }
  
  public void setDataType(final short type) {
    this.dataType = type;
  }
  
  public Optional<Object> getDataValue() {
    if (this.dataType ==  ItemDataType.TYPE_FORMULA.getValue()) {
      if (this.dataBuf == null || this.dataBuf.length == 0) {
        return Optional.empty(); //$NON-NLS-1$
      } else {
        return Optional.of(FormulaCompiler.get().decompile(this.dataBuf));
      }
    } else if (this.dataType ==  ItemDataType.TYPE_TEXT.getValue()) {
      if (this.dataBuf == null || this.dataBuf.length == 0) {
        return Optional.empty(); //$NON-NLS-1$
      } else {
        return Optional.of(new String(this.dataBuf, Charset.forName("LMBCS")));
      }
    } else if (this.dataType ==  ItemDataType.TYPE_COMPOSITE.getValue()) {
      if (this.dataBuf == null || this.dataBuf.length == 0) {
        return Optional.empty(); //$NON-NLS-1$
      } else {
        try {
          return Optional.of(RichTextUtil.readMemoryRecords(this.dataBuf, RecordType.Area.RESERVED_INTERNAL));
        } catch (Exception e) {
          //exception reading ad rich text just return as String
          return Optional.of(new String(this.dataBuf, Charset.forName("LMBCS")));
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
