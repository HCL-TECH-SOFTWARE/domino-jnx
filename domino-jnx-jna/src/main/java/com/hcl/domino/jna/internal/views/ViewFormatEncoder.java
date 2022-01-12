package com.hcl.domino.jna.internal.views;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.hcl.domino.commons.design.view.DominoViewColumnFormat;
import com.hcl.domino.commons.design.view.DominoViewFormat;
import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.data.CollectionColumn;
import com.hcl.domino.data.NativeItemCoder;
import com.hcl.domino.data.NativeItemCoder.LmbcsVariant;
import com.hcl.domino.design.format.ViewColumnFormat;
import com.hcl.domino.design.format.ViewColumnFormat2;
import com.hcl.domino.design.format.ViewColumnFormat4;
import com.hcl.domino.design.format.ViewColumnFormat5;
import com.hcl.domino.design.format.ViewColumnFormat6;
import com.hcl.domino.design.format.ViewTableFormat;
import com.hcl.domino.design.format.ViewTableFormat2;
import com.hcl.domino.design.format.ViewTableFormat3;
import com.hcl.domino.design.format.ViewTableFormat4;
import com.hcl.domino.richtext.records.CDResource;

public class ViewFormatEncoder {

  /**
   * Checks if the specified buffer at least has the specified size. If not we create
   * a new one and copy over all data, keeping the current position
   * 
   * @param buf buffer
   * @param newSize new minimum size
   * @return buffer, either the same (if capacity was sufficient) or a new one
   */
  private static ByteBuffer ensureBufferCapacity(ByteBuffer buf, int newSize) {
    if (newSize < 1) {
      throw new IllegalArgumentException("New size must be greater than 0 bytes");
    }
    if (buf.capacity() > newSize) {
      return buf;
    }
    
    int pos = buf.position();
    
    final ByteBuffer newData = ByteBuffer.allocate(newSize).order(ByteOrder.nativeOrder());
    final int copySize = Math.min(newSize, buf.capacity());
    buf.position(0);
    buf.limit(copySize);
    newData.put(buf);
    newData.position(pos);
    
    return newData;
  }
  
  /**
   * Encodes a {@link DominoViewFormat} describing the style of a view and of its columns in
   * binary form. The result is expected to be readible via {@link ViewFormatDecoder#decodeViewFormat(com.sun.jna.Pointer, int)}
   * without data loss.
   * 
   * @param viewFormat view design
   * @return bytebuffer with data
   */
  public static ByteBuffer encodeViewFormat(DominoViewFormat viewFormat) {
    if (viewFormat.getColumnCount()==0) {
      throw new IllegalArgumentException("Columns cannot be empty");
    }
    
    //validate that we have the current column type
    for (CollectionColumn currCol : viewFormat.getColumns()) {
      if (currCol instanceof DominoViewColumnFormat ==false) {
        throw new IllegalArgumentException(MessageFormat.format("Column must be of type {0}: {1}", DominoViewColumnFormat.class.getName(), currCol.getClass().getName()));
      }
    }

    List<DominoViewColumnFormat> columns = viewFormat.getColumns()
        .stream()
        .map(DominoViewColumnFormat.class::cast)
        .collect(Collectors.toList());

    ByteBuffer buf;

    {
      //write basic table format
      ViewTableFormat format1 = viewFormat.getAdapter(ViewTableFormat.class);
      if (format1==null) {
        format1 = createViewTableFormat(viewFormat);
      }
      ByteBuffer format1Data = format1.getData();
      int format1Capacity = format1Data.capacity();

      buf = ByteBuffer.allocate(format1Capacity).order(ByteOrder.nativeOrder());
      buf.put(format1Data);
    }
    
    
    for (int i=0; i<columns.size(); i++) {
      DominoViewColumnFormat col = columns.get(i);
      
      ViewColumnFormat fmt = col.getAdapter(ViewColumnFormat.class);
      if (fmt==null) {
        fmt = createDefaultViewColumnFormat();
      }
      ByteBuffer fmtData = fmt.getData();
      int fmtCapacity = fmtData.capacity();
      
      buf = ensureBufferCapacity(buf, buf.capacity() + fmtCapacity);
      buf.put(fmtData);
    }

    {
      //write ViewTableFormat2
      ViewTableFormat2 fmt2 = viewFormat.getAdapter(ViewTableFormat2.class);
      if (fmt2==null) {
        fmt2 = createViewTableFormat2(viewFormat);
      }
      ByteBuffer fmt2Data = fmt2.getData();
      int fmt2Capacity = fmt2Data.capacity();
      
      buf = ensureBufferCapacity(buf, buf.capacity() + fmt2Capacity);
      buf.put(fmt2Data);
    }
    
    for (int i=0; i<columns.size(); i++) {
      DominoViewColumnFormat col = columns.get(i);
      
      ViewColumnFormat2 fmt2 = col.getAdapter(ViewColumnFormat2.class);
      if (fmt2==null) {
        fmt2 = createDefaultViewColumnFormat2();
      }
      
      //ignore var data with hide-when formula and twistie resource here, will be stored later after the ViewTableFormat3
      ByteBuffer fmt2VarData = fmt2.getVariableData();
      ByteBuffer fmt2Data = fmt2.getData();
      
      byte[] fmt2FixedData = new byte[fmt2Data.capacity() - fmt2VarData.capacity()];
      fmt2Data.get(fmt2FixedData);
      
      buf = ensureBufferCapacity(buf, buf.capacity() + fmt2FixedData.length);
      buf.put(fmt2FixedData);
    }

    {
      //write ViewTableFormat3
      ViewTableFormat3 fmt3 = viewFormat.getAdapter(ViewTableFormat3.class);
      if (fmt3==null) {
        fmt3 = createViewTableFormat3(viewFormat);
      }
      ByteBuffer fmt3Data = fmt3.getData();
      int fmt3Capacity = fmt3Data.capacity();
      
      buf = ensureBufferCapacity(buf, buf.capacity() + fmt3Capacity);
      buf.put(fmt3Data);
    }

    // Followed by variable data defined by VIEW_COLUMN_FORMAT2
    for (int i=0; i<columns.size(); i++) {
      DominoViewColumnFormat col = columns.get(i);
      
      ViewColumnFormat2 fmt2 = col.getAdapter(ViewColumnFormat2.class);
      if (fmt2==null) {
        fmt2 = createDefaultViewColumnFormat2();
      }
      
      ByteBuffer fmt2VarData = fmt2.getVariableData();
      
      int hideWhenFormulaLength = fmt2.getHideWhenFormulaLength();
      int twistieResourceLength = fmt2.getTwistieResourceLength();
      
      buf = ensureBufferCapacity(buf, buf.capacity() + hideWhenFormulaLength + twistieResourceLength);
      
      if (hideWhenFormulaLength>0) {
        byte[] compiledHideWhenFormula = new byte[hideWhenFormulaLength];
        fmt2VarData.get(compiledHideWhenFormula);
        buf.put(compiledHideWhenFormula);
      }
      
      if (twistieResourceLength>0) {
        byte[] twistieResourceData = new byte[twistieResourceLength];
        fmt2VarData.get(twistieResourceData);
        buf.put(twistieResourceData);
      }
    }
    

    {
      //write ViewTableFormat4
      ViewTableFormat4 fmt4 = viewFormat.getAdapter(ViewTableFormat4.class);
      if (fmt4==null) {
        fmt4 = createViewTableFormat4(viewFormat);
      }
      ByteBuffer fmt4Data = fmt4.getData();
      int fmt4Capacity = fmt4Data.capacity();
      
      buf = ensureBufferCapacity(buf, buf.capacity() + fmt4Capacity);
      buf.put(fmt4Data);
    }

    CDResource backgroundResource = viewFormat.getBackgroundResource();
    if (backgroundResource!=null) {
      //write background image
      ByteBuffer backgroundResourceData = backgroundResource.getData();
      buf = ensureBufferCapacity(buf, buf.capacity() + backgroundResourceData.capacity());
      buf.put(backgroundResourceData);
    }


    // VIEW_COLUMN_FORMAT4 - number format
    for (int i=0; i<columns.size(); i++) {
      DominoViewColumnFormat col = columns.get(i);
      ViewColumnFormat2 fmt2 = col.getAdapter(ViewColumnFormat2.class);
      
      if(fmt2.getFlags().contains(ViewColumnFormat2.Flag3.NumberFormat)) {
        ViewColumnFormat4 fmt4 = col.getAdapter(ViewColumnFormat4.class);
        if (fmt4==null) {
          fmt4 = createDefaultViewColumnFormat4();
        }
        
        ByteBuffer fmt4Data = fmt4.getData();
        buf = ensureBufferCapacity(buf, buf.capacity() + fmt4Data.capacity());
        buf.put(fmt4Data);
      }
    }
    
    // VIEW_COLUMN_FORMAT5 - names format
    for (int i=0; i<columns.size(); i++) {
      DominoViewColumnFormat col = columns.get(i);
      ViewColumnFormat2 fmt2 = col.getAdapter(ViewColumnFormat2.class);
      
      if(fmt2.getFlags().contains(ViewColumnFormat2.Flag3.NamesFormat)) {
        ViewColumnFormat5 fmt5 = col.getAdapter(ViewColumnFormat5.class);
        if (fmt5==null) {
          fmt5 = createDefaultViewColumnFormat5();
        }
        
        ByteBuffer fmt5Data = fmt5.getData();
        buf = ensureBufferCapacity(buf, buf.capacity() + fmt5Data.capacity());
        buf.put(fmt5Data);
      }
    }

    Charset lmbcsCharset = NativeItemCoder.get().getLmbcsCharset(LmbcsVariant.NORMAL);
    
    // Hidden titles follow as WORD-prefixed P-strings
    for(int i = 0; i < columns.size(); i++) {
      DominoViewColumnFormat col = columns.get(i);
      if(col.isHideTitle()) {
        String hiddenTitle = col.getTitle();
        byte[] hiddenTitleData = hiddenTitle.getBytes(lmbcsCharset);
        if (hiddenTitleData.length > 65535) {
          throw new IllegalArgumentException("Length of hidden title exceeds max length in bytes (0xffff)");
        }
        buf = ensureBufferCapacity(buf, buf.capacity() + 2 + hiddenTitleData.length);
        buf.putShort((short) (hiddenTitleData.length & 0xffff));
        buf.put(hiddenTitleData);
      }
    }

    // Shared-column aliases follow as WORD-prefixed P-strings
    for(int i = 0; i < columns.size(); i++) {
      DominoViewColumnFormat col = columns.get(i);
      if(col.isSharedColumn()) {
        Optional<String> sharedColumnName = col.getSharedColumnName();
        if (sharedColumnName.isPresent()) {
          byte[] sharedColumnNameData = sharedColumnName.get().getBytes(lmbcsCharset);
          if (sharedColumnNameData.length > 65535) {
            throw new IllegalArgumentException("Length of shared column name exceeds max length in bytes (0xffff)");
          }
          buf = ensureBufferCapacity(buf, buf.capacity() + 2 + sharedColumnNameData.length);
          buf.putShort((short) (sharedColumnNameData.length & 0xffff));
          buf.put(sharedColumnNameData);
        }
      }
    }
    
    // TODO figure out where these ghost bits come from. I'd thought they were "ghost" shared-column names, but
    //      it turns out they were instead hidden column titles. It seems in practice (test data and mail12.ntf) that
    //      they're all 0, so the below algorithm "works" to move past them
//    while(data.remaining() > 1 && data.getShort(data.position()) != ViewFormatConstants.VIEW_COLUMN_FORMAT_SIGNATURE6) {
//      int ghostSharedColLen = Short.toUnsignedInt(data.getShort());
//      data.position(data.position()+ghostSharedColLen);
//    }
    
    // VIEW_COLUMN_FORMAT6 - Hannover
    for(int i = 0; i < columns.size(); i++) {
      DominoViewColumnFormat col = columns.get(i);
      ViewColumnFormat2 fmt = col.getAdapter(ViewColumnFormat2.class);
      if(fmt.getFlags().contains(ViewColumnFormat2.Flag3.ExtendedViewColFmt6)) {
        ViewColumnFormat6 fmt6 = col.getAdapter(ViewColumnFormat6.class);
        if (fmt6==null) {
          fmt6 = createDefaultViewColumnFormat6();
        }

        ByteBuffer fmt6Data = fmt6.getData();
        buf = ensureBufferCapacity(buf, buf.capacity() + fmt6Data.capacity());
        buf.put(fmt6Data);
      }
    }

    // It seems like it ends with a terminating 0 byte - possibly for padding to hit a WORD boundary
    // TODO figure out why where are sometimes > 1 bytes remaining (observed up to 6 in mail12.ntf)

    
    return buf;
  }

  private static ViewTableFormat createViewTableFormat(DominoViewFormat viewFormat) {
    ViewTableFormat fmt = MemoryStructureUtil.newStructure(ViewTableFormat.class, 0);
    fmt.setColumnCount(viewFormat.getColumnCount());
    
    return fmt;
  }

  private static ViewTableFormat2 createViewTableFormat2(DominoViewFormat viewFormat) {
    ViewTableFormat2 fmt = MemoryStructureUtil.newStructure(ViewTableFormat2.class, 0);
    return fmt;
  }

  private static ViewTableFormat3 createViewTableFormat3(DominoViewFormat viewFormat) {
    ViewTableFormat3 fmt = MemoryStructureUtil.newStructure(ViewTableFormat3.class, 0);
    return fmt;
  }

  private static ViewTableFormat4 createViewTableFormat4(DominoViewFormat viewFormat) {
    ViewTableFormat4 fmt = MemoryStructureUtil.newStructure(ViewTableFormat4.class, 0);
    return fmt;
  }

  private static ViewColumnFormat6 createDefaultViewColumnFormat6() {
    ViewColumnFormat6 fmt = MemoryStructureUtil.newStructure(ViewColumnFormat6.class, 0);
    return fmt;
  }

  private static ViewColumnFormat5 createDefaultViewColumnFormat5() {
    ViewColumnFormat5 fmt = MemoryStructureUtil.newStructure(ViewColumnFormat5.class, 0);
    return fmt;
  }

  private static ViewColumnFormat4 createDefaultViewColumnFormat4() {
    ViewColumnFormat4 fmt = MemoryStructureUtil.newStructure(ViewColumnFormat4.class, 0);
    return fmt;
  }

  private static ViewColumnFormat2 createDefaultViewColumnFormat2() {
    ViewColumnFormat2 fmt = MemoryStructureUtil.newStructure(ViewColumnFormat2.class, 0);
    return fmt;
  }

  private static ViewColumnFormat createDefaultViewColumnFormat() {
    ViewColumnFormat fmt = MemoryStructureUtil.newStructure(ViewColumnFormat.class, 0);
    return fmt;
  }
  
}
