package com.hcl.domino.commons.design.view;

import static com.hcl.domino.commons.util.NotesItemDataUtil.ensureBufferCapacity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

import com.hcl.domino.design.format.CollateDescriptor;
import com.hcl.domino.design.format.Collation;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;

public class CollationEncoder {

  /**
   * Encodes the collation info of a view
   * 
   * @param collationInfo collation info
   * @return encoded buffer
   */
  public static ByteBuffer encode(DominoCollationInfo collationInfo) {
    Collation collation = Objects.requireNonNull(collationInfo.getAdapter(Collation.class));
    CollateDescriptor[] colDescriptors = Objects.requireNonNull(collationInfo.getAdapter(CollateDescriptor[].class));
    
    MemoryStructureWrapperService memService = MemoryStructureWrapperService.get();
    
    int collationSize = memService.sizeOf(Collation.class);
    int collateDescriptorFixedDataSize = memService.sizeOf(CollateDescriptor.class);
    //offset of first packed string
    int currPackedDataOffset = 0; //collationSize + colDescriptors.length * collateDescriptorFixedDataSize;

    int packedDataSize = 0;
    
    //compute total size of packed data
    for (int i=0; i<colDescriptors.length; i++) {
      CollateDescriptor currCol = colDescriptors[i];
      packedDataSize += currCol.getNameLength();
    }

    ByteBuffer packedDataBuf = ByteBuffer.allocate(packedDataSize);
    
    //extract packed data from col descriptor var data
    for (int i=0; i<colDescriptors.length; i++) {
      CollateDescriptor currCol = colDescriptors[i];
      
      //update offset for packed data
      currCol.setNameOffset(currPackedDataOffset);
      currPackedDataOffset += currCol.getNameLength();
      
      ByteBuffer currColVarData = currCol.getVariableData();
      packedDataBuf.put(currColVarData);
    }
    
    int totalBufferSize = collationSize + colDescriptors.length * collateDescriptorFixedDataSize + packedDataSize;
    //update real buffer size in collation object
    collation.setBufferSize(totalBufferSize);
    ByteBuffer buf = ByteBuffer.allocate(totalBufferSize).order(ByteOrder.nativeOrder());
    //write collation data
    buf.put(collation.getData());

    //now write the fixed data for the collate descriptors
    for (int i=0; i<colDescriptors.length; i++) {
      CollateDescriptor currCol = colDescriptors[i];
      
      ByteBuffer currColData = currCol.getData();
      ByteBuffer currColVarData = currCol.getVariableData();
      
      int fixedDataLen = currColData.capacity() - currColVarData.capacity();
      byte[] fixedData = new byte[fixedDataLen];
      currColData.get(fixedData);
      buf.put(fixedData);
    }
    
    //followed by the package data with column names
    packedDataBuf.position(0);
    buf.put(packedDataBuf);
    
    buf.position(0);
    
    return buf;
  }
}
