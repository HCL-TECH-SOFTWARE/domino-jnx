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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.hcl.domino.commons.design.view.DominoCollationInfo.DominoCollateColumn;
import com.hcl.domino.data.CollectionColumn;
import com.hcl.domino.data.CollectionColumn.SortConfiguration;
import com.hcl.domino.design.format.CollateDescriptor;
import com.hcl.domino.design.format.Collation;
import com.hcl.domino.design.format.CollateDescriptor.CollateType;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;

/**
 * Utility class to encode the binary structures for the $Collation item
 * 
 * @author Karsten Lehmann
 * @since 1.2.4
 */
public class CollationEncoder {

  /**
   * Analyzes the {@link DominoViewFormat} object and produces matching {@link DominoCollationInfo}
   * objects for sorted/categorized columns and columns with resort options.
   * 
   * @param viewFormat view format
   * @return list of collations, to be written as items $Collation, $Collation1, $Collation2 etc.
   */
  public static List<DominoCollationInfo> newCollationFromViewFormat(DominoViewFormat viewFormat) {
    List<DominoCollationInfo> collations = new ArrayList<>();
    List<CollectionColumn> viewColumns = viewFormat.getColumns();

    {
      //add COLLATE structure for $Collation containing buildOnDemand and Unique property
      //as well as any already sorted column
      DominoCollationInfo collation = new DominoCollationInfo();
      collations.add(collation);

      collation.setBuildOnDemand(false);
      collation.setUnique(false);

      for (CollectionColumn currViewCol : viewColumns) {
        SortConfiguration currSortCfg = currViewCol.getSortConfiguration();
        DominoCollateColumn collateCol = null;

        if (currSortCfg.isCategory()) {
          collateCol = collation.addColumn(currViewCol.getItemName(), currSortCfg.isSortedDescending());
          collateCol.setType(CollateType.CATEGORY);
        }
        else if (currSortCfg.isSorted()) {
          collateCol = collation.addColumn(currViewCol.getItemName(), currSortCfg.isSortedDescending());
          collateCol.setType(CollateType.KEY);
        }

        if (collateCol!=null) {
          boolean isAccentSensitive = currSortCfg.isAccentSensitive();
          collateCol.setAccentSensitive(isAccentSensitive);
          boolean isCaseSensitive = currSortCfg.isCaseSensitive();
          collateCol.setCaseSensitive(isCaseSensitive);
          boolean isIgnorePrefixes = currSortCfg.isIgnorePrefixes();
          collateCol.setIgnorePrefixes(isIgnorePrefixes);
        }
      }
    }
    
    {
      //now go through the columns to find resortable ones
      for (CollectionColumn currViewCol : viewColumns) {
        SortConfiguration currSortCfg = currViewCol.getSortConfiguration();
        if (currSortCfg.isResortAscending()) {
          
          DominoCollationInfo collation = new DominoCollationInfo();
          collations.add(collation);

          collation.setBuildOnDemand(currSortCfg.isDeferResortIndexing());
          //TODO find out where the unique property comes from:
          collation.setUnique(false);

          DominoCollateColumn collateCol = collation.addColumn(currViewCol.getItemName(), false);
          collateCol.setType(currSortCfg.isCategory() ? CollateType.CATEGORY : CollateType.KEY);
          
          boolean isAccentSensitive = currSortCfg.isAccentSensitive();
          collateCol.setAccentSensitive(isAccentSensitive);
          boolean isCaseSensitive = currSortCfg.isCaseSensitive();
          collateCol.setCaseSensitive(isCaseSensitive);
          boolean isIgnorePrefixes = currSortCfg.isIgnorePrefixes();
          collateCol.setIgnorePrefixes(isIgnorePrefixes);

          if (currSortCfg.isSecondaryResort()) {
            //second sort column is specified
            
            int secSortCol = currSortCfg.getSecondResortColumnIndex();

            if (secSortCol < viewColumns.size()) {
              //column still exists
              CollectionColumn secondarySortCol = viewColumns.get(secSortCol);
              SortConfiguration secondarySortCfg = secondarySortCol.getSortConfiguration();
              
              DominoCollateColumn secondaryCollateCol = collation.addColumn(secondarySortCol.getItemName(), currSortCfg.isSecondaryResortDescending());
              secondaryCollateCol.setType(secondarySortCfg.isCategory() ? CollateType.CATEGORY : CollateType.KEY);

              secondaryCollateCol.setAccentSensitive(secondarySortCfg.isAccentSensitive());
              secondaryCollateCol.setCaseSensitive(secondarySortCfg.isCaseSensitive());
              secondaryCollateCol.setIgnorePrefixes(secondarySortCfg.isIgnorePrefixes());
            }
          }
        }
        
        if (currSortCfg.isResortDescending()) {
          
          DominoCollationInfo collation = new DominoCollationInfo();
          collations.add(collation);

          collation.setBuildOnDemand(currSortCfg.isDeferResortIndexing());
          //TODO find out where the unique property comes from:
          collation.setUnique(false);

          DominoCollateColumn collateCol = collation.addColumn(currViewCol.getItemName(), true);
          collateCol.setType(currSortCfg.isCategory() ? CollateType.CATEGORY : CollateType.KEY);
          
          boolean isAccentSensitive = currSortCfg.isAccentSensitive();
          collateCol.setAccentSensitive(isAccentSensitive);
          boolean isCaseSensitive = currSortCfg.isCaseSensitive();
          collateCol.setCaseSensitive(isCaseSensitive);
          boolean isIgnorePrefixes = currSortCfg.isIgnorePrefixes();
          collateCol.setIgnorePrefixes(isIgnorePrefixes);

          if (currSortCfg.isSecondaryResort()) {
            //second sort column is specified
            
            int secSortCol = currSortCfg.getSecondResortColumnIndex();

            if (secSortCol < viewColumns.size()) {
              //column still exists
              CollectionColumn secondarySortCol = viewColumns.get(secSortCol);
              SortConfiguration secondarySortCfg = secondarySortCol.getSortConfiguration();
              
              DominoCollateColumn secondaryCollateCol = collation.addColumn(secondarySortCol.getItemName(), currSortCfg.isSecondaryResortDescending());
              secondaryCollateCol.setType(secondarySortCfg.isCategory() ? CollateType.CATEGORY : CollateType.KEY);

              secondaryCollateCol.setAccentSensitive(secondarySortCfg.isAccentSensitive());
              secondaryCollateCol.setCaseSensitive(secondarySortCfg.isCaseSensitive());
              secondaryCollateCol.setIgnorePrefixes(secondarySortCfg.isIgnorePrefixes());
            }
          }
        }
      }
    }
    return collations;
  }
  
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
