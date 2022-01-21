package com.hcl.domino.commons.design.view;

import static com.hcl.domino.commons.util.NotesItemDataUtil.readMemory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import com.hcl.domino.commons.misc.ODSTypes;
import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.commons.util.DumpUtil;
import com.hcl.domino.design.format.CollateDescriptor;
import com.hcl.domino.design.format.Collation;

public class CollationDecoder {

  public static DominoCollationInfo decodeCollation(ByteBuffer data) {
    {
      int pos = data.position();
      int remaining = data.remaining();
      byte[] remainingData = new byte[remaining];
      data.get(remainingData);
      System.out.println(DumpUtil.dumpAsAscii(remainingData));
      data.position(pos);
    }
    
    /*
     * Data contains:
     * - COLLATION
     * - COLLATE_DESCRIPTOR * colCount
     * - var data * colCount
     */
    
    Collation collationHeader = readMemory(data, ODSTypes._COLLATION, Collation.class);
    
    List<CollateDescriptor> collateDescriptors = new ArrayList<>();
    
    // Always present
    {
      int colDescSize = MemoryStructureUtil.sizeOf(CollateDescriptor.class);
      ByteBuffer pPackedData = data.duplicate().order(ByteOrder.nativeOrder());
      int itemCount = collationHeader.getItems();
      pPackedData.position(pPackedData.position()+(colDescSize * itemCount));

      int colCount = collationHeader.getItems();
      
      for(int i = 0; i < colCount; i++) {
        CollateDescriptor tempCol = readMemory(data, ODSTypes._COLLATE_DESCRIPTOR, CollateDescriptor.class);
        
        // Find the actual size with variable data and re-read
        int varSize = tempCol.getNameLength();
        CollateDescriptor fullCol = MemoryStructureUtil.newStructure(CollateDescriptor.class, varSize);
        ByteBuffer fullColData = fullCol.getData();
        // Write the ODS value first
        fullColData.put(tempCol.getData());
        byte[] varData = new byte[varSize];
        pPackedData.get(varData);
        fullColData.put(varData);

        collateDescriptors.add(fullCol);
      }
      
      data.position(pPackedData.position());
    }
    
    DominoCollationInfo collateInfo = new DominoCollationInfo();
    collateInfo.read(collationHeader);
    collateInfo.read(collateDescriptors);
    return collateInfo;
  }
  
}
