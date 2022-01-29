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

/**
 * Utility class to decode the $Collation item value
 * 
 * @author Karsten Lehmann
 * @since 1.2.4
 */
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
